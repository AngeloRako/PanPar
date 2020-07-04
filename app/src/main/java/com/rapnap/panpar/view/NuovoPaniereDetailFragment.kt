package com.rapnap.panpar.view

import android.content.ContentValues.TAG
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.Navigation
import androidx.navigation.navGraphViewModels
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.snackbar.Snackbar
import com.rapnap.panpar.R
import com.rapnap.panpar.model.PuntoRitiro
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

    private lateinit var selectedDate: Date

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

        //Bottone per completare
        view.complete_paniere_details_btn.setOnClickListener {

            val snackbar = Snackbar.make(requireView(), "Paniere creato!", Snackbar.LENGTH_LONG)
            snackbar.show()

            requireActivity().finish()

        }

        nuovoPaniereVM.puntoRitiroScelto.observe(viewLifecycleOwner, Observer<PuntoRitiro> {

            scegli_punto_di_ritiro.setText(it.nome)

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

        }

        /*
        Alternativa:
        val dpd = DatePickerDialog(requireContext(), this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        dpd.show()
        */

    }


    private fun showDatePicker() {

        isPickingDate = true
        picker.show(requireActivity().supportFragmentManager, picker.toString())

    }

    private fun getClearedUtc(): Calendar {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.clear()
        return utc
    }


}