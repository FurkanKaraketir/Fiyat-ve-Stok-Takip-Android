package com.karaketir.fiyatvestoktakip.models

import com.google.firebase.Timestamp

class Product(
    val name: String,
    val buyPrice: Double,
    val sellPrice: Double,
    val stock: Int,
    val description: String,
    val imageLink: String,
    val timestamp: Timestamp,
    val documentID: String,
    val sellerName: String,
    val sellerNumber: String,
    val barcode: Long
)