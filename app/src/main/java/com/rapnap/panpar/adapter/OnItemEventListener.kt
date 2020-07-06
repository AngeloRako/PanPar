package com.rapnap.panpar.adapter

import android.view.View


/* Interfaccia per gestire gli eventi legati a oggetti da views (fornite opzionalmente) */
interface OnItemEventListener<Type> {
    fun onEventHappened(item: Type, view: View? = null)
}
