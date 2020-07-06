package com.rapnap.panpar.adapter


/* Interfaccia per gestire gli eventi legati a oggetti   */
interface OnItemEventListener<Type> {
    fun onEventHappened(item: Type)
}
