package com.rapnap.panpar.view

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.TranslateAnimation
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.rapnap.panpar.R
import com.rapnap.panpar.model.Contenuto
import com.rapnap.panpar.model.Paniere
import com.rapnap.panpar.model.PuntoRitiro
import com.rapnap.panpar.model.Utility.getClearedUtc
import com.rapnap.panpar.model.Utility.getDate
import com.rapnap.panpar.viewmodel.NuovoPaniereViewModel
import kotlinx.android.synthetic.main.fragment_nuovo_paniere_detail.*
import kotlinx.android.synthetic.main.fragment_nuovo_paniere_detail.view.*
import java.util.*


class NuovoPaniereDetailFragment : Fragment() {

    private val nuovoPaniereVM: NuovoPaniereViewModel
            by navGraphViewModels(R.id.new_paniere_graph)

    private var today: Long = 0
    private var tomorrow: Long = 0
    private var oneYearForward: Long = 0
    private lateinit var picker: MaterialDatePicker<*>
    private var isPickingDate = false
    private lateinit var nuovoPaniere: Paniere
    private lateinit var selectedDate: Date

    private var isDataSelected = false
    private var isPuntoRitiroSelected = false
    private var isPuntoRitiroNew = false
    private var isContenutoSelected = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_nuovo_paniere_detail, container, false)

        initDataPicker()
        view.data_consegna_prevista_text_input.inputType = InputType.TYPE_NULL
        view.data_consegna_prevista_text_input.keyListener = null

        view.data_consegna_prevista_text_input.setOnFocusChangeListener { v, hasFocus ->

            if (hasFocus && !isPickingDate) {
                showDatePicker()
            }

        }

        view.data_consegna_prevista_text_input.setOnClickListener {

            if (!isPickingDate) {
                showDatePicker()
            }
        }

        //Scegli punto di ritiro
        view.scegli_punto_di_ritiro.inputType = InputType.TYPE_NULL
        view.scegli_punto_di_ritiro.keyListener = null

        view.scegli_punto_di_ritiro.setOnFocusChangeListener { v, hasFocus ->

            if (hasFocus && !isPickingDate) {
                Navigation.findNavController(this.requireView())
                    .navigate(R.id.nuovoPaniereToScegliPuntoRitiro)
            }

        }

        view.scegli_punto_di_ritiro.setOnClickListener {

            Navigation.findNavController(this.requireView())
                .navigate(R.id.nuovoPaniereToScegliPuntoRitiro)
        }


        //Configurazione chips per la tipologia contenuto
        view.contenuto_group.isSelectionRequired = true
        Contenuto.values().forEach { value ->

            val chip = createChip(value.toString().toLowerCase().capitalize())
            view.contenuto_group.addView(chip)
        }


        //Bottone per completare
        view.complete_paniere_details_btn.isEnabled = false
        view.complete_paniere_details_btn.isVisible = false
        view.complete_paniere_details_btn.setOnClickListener {
            /* RAGGIUNGIBILE SOLO SE IL BOTTONE è ATTIVO -> devo assicurarmi che succeda solo quando
            * è possibile la creazione*/


            /* Il VM sa già lo stato attuale del paniere in creazione devo solo dirgli di confermarle sulla repository*/
            nuovoPaniereVM.creaNuovoPaniere() {

                requireActivity().finish()

            }
        }

        //Osservo cambiamenti ai dati
        nuovoPaniereVM.puntoRitiroScelto.observe(viewLifecycleOwner, Observer<PuntoRitiro> {

            scegli_punto_di_ritiro.setText(it.nome)

            /* Se presente... aggiungi il punto di ritiro al paniere, altrimenti creane uno nuovo (in locale)
             e aggiungi le info */
            if (safeNuovoPaniereEdit { paniere ->

                    paniere.puntoRitiro = it
                    isPuntoRitiroSelected = true

                }) {
                //Segnala che nel paniere vi è solo il punto di ritiro,
                isPuntoRitiroNew = true
            } else {

                activateContinuaIfPossible()
            }


        })

        nuovoPaniereVM.nuovoPaniereInCreazione.observe(viewLifecycleOwner, Observer<Paniere> {

            /*Cosa faccio quando i dati del nuovo paniere sono cambiati?*/

            //Se sto mostrando solo un punto di ritiro e solo ora ho settato altri dati, cambio solo quelli
            if (isPuntoRitiroNew) {
                //Tramite copy preservo il punto ritiro che ho in locale
                nuovoPaniere = it.copy(puntoRitiro = nuovoPaniere.puntoRitiro)
            } else {
                //Altrimenti il VM mi sta dando tutti i dati
                nuovoPaniere = it
            }

            //Aggiorna UI
            it.dataConsegnaPrevista?.let{ date->

                data_consegna_prevista_text_input.setText(getDate(date))
            }

            scegli_punto_di_ritiro.setText(it.puntoRitiro.nome)

            with (it.contenuto.iterator()){
                forEach { contenuto ->

                    with((contenuto_group.children as Sequence<Chip>).iterator()) {
                        forEach { chip ->
                            val condition = (chip.text as String).toUpperCase() == contenuto.name
                            if(condition){
                                   chip.isChecked = condition
                            }
                        }
                    }
                }
                //Attiva il bottone di continua se possibile
                activateContinuaIfPossible()
            }

        })


        return view
    }

    private fun initDataPicker() {

        today = MaterialDatePicker.todayInUtcMilliseconds()

        val calendar: Calendar = getClearedUtc()
        calendar.timeInMillis = today

        calendar.roll(Calendar.DAY_OF_MONTH, 1);
        tomorrow = calendar.timeInMillis

        calendar.roll(Calendar.YEAR, 1);
        oneYearForward = calendar.timeInMillis

        //Constraints Builder
        val constraintsBuilder = CalendarConstraints.Builder()

        constraintsBuilder.setStart(today);
        constraintsBuilder.setEnd(oneYearForward);
        constraintsBuilder.setValidator(DateValidatorPointForward.now())

        //DatePicker Builder
        val builder = MaterialDatePicker.Builder.datePicker()
        builder.setSelection(tomorrow)
        builder.setCalendarConstraints(constraintsBuilder.build())

        picker = builder.build()

        picker.addOnCancelListener {
            isPickingDate = false
        }

        picker.addOnDismissListener {
            isPickingDate = false
        }

        picker.addOnNegativeButtonClickListener {
            isPickingDate = false
        }

        picker.addOnPositiveButtonClickListener {

            isPickingDate = false
            val calendar: Calendar = getClearedUtc()
            calendar.timeInMillis = it as Long

            val dateString = getDate(calendar.timeInMillis)

            Log.d(TAG, "[Nuovo paniere] Imposto la data a: ${dateString}")

            selectedDate = Date(calendar.timeInMillis)
            data_consegna_prevista_text_input.setText(dateString)
            isDataSelected = true

            //Modifica paniere nel VM
            safeNuovoPaniereEdit { paniere ->

                paniere.dataConsegnaPrevista = selectedDate
                nuovoPaniereVM.setNuovoPaniereInCreazione(paniere)
            }

        }

    }


    private fun showDatePicker() {

        isPickingDate = true
        picker.show(requireActivity().supportFragmentManager, picker.toString())

    }


    private fun createChip(title: String): Chip {

        val chip = Chip(requireContext())

        chip.text = title
        chip.isCheckable = true

        chip.setOnCheckedChangeListener { buttonView, isChecked ->

            val value = Contenuto.valueOf((buttonView.text as String).toUpperCase())

            when (isChecked) {

                true -> {
                    //Modifica il paniere aggiungendo il Tipo
                    safeNuovoPaniereEdit {
                        it.contenuto.add(value)
                        nuovoPaniereVM.setNuovoPaniereInCreazione(it)
                        isContenutoSelected = true
                        //Attiva il bottone di continua se possibile
                        activateContinuaIfPossible()
                    }
                }
                false -> {
                    //Modifica il paniere rimuovendo il Tipo
                    //Do per scontato che ci sia poichè per avere un chip attivo devo averlo prima
                    //osservato dal VM
                    nuovoPaniere.contenuto.remove(value)
                    nuovoPaniereVM.setNuovoPaniereInCreazione(nuovoPaniere)
                }
            }
        }
        return chip

    }


    private fun safeNuovoPaniereEdit(operation: (Paniere) -> Unit): Boolean {

        val result = ::nuovoPaniere.isInitialized

        when (result) {
            true -> {
                operation(nuovoPaniere)
            }
            false -> {
                nuovoPaniere = Paniere()
                operation(nuovoPaniere)
            }
        }

        return result
    }

    private fun activateContinuaIfPossible(): Boolean {

        val result = isContenutoSelected && isDataSelected && isPuntoRitiroSelected

        complete_paniere_details_btn.isEnabled = result

        if(!(complete_paniere_details_btn.visibility == View.VISIBLE) && result){
            slideUp(complete_paniere_details_btn)
        }

        return result
    }

    // slide the view from below itself to the current position
    private fun slideUp(view: View) {
        view.visibility = View.VISIBLE
        val animate = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            view.height.toFloat(),  // fromYDelta
            0F
        ) // toYDelta
        animate.duration = 500
        animate.fillAfter = true
        view.startAnimation(animate)
    }




}
