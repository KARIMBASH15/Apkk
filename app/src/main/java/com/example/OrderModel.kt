package com.example

import com.google.firebase.Timestamp

data class OrderItem(
    val id: String = "",
    val name: String = "",
    val qty: Int = 1,
    val price: Double = 0.0,
    val weight: String = "",
    val flavor: String = ""
)

data class Order(
    val id: String = "",
    val orderId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    val discount: Double = 0.0,
    val promoCode: String = "",
    val shippingPrice: Double? = null,
    val total: Double = 0.0,
    val status: String = "received", // received, delivering, delivered, archived
    val createdAt: Timestamp? = null,
    val updatedAt: Timestamp? = null
) {
    fun grandTotal(): Double {
        val ship = shippingPrice ?: 0.0
        return total + ship
    }
}

data class Customer(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val address: String = "",
    val ordersCount: Int = 0,
    val totalSpent: Double = 0.0,
    val fixedShippingPrice: Double? = null,
    val lastOrderAt: Timestamp? = null
)

data class StoreSettings(
    val shippingMode: String = "manual", // manual, fixed
    val fixedShippingPrice: Double = 0.0,
    val invoiceSize: String = "88mm", // 88mm, a4
    val alarmTone: String = "default", // default, chime, siren, soft, triple
    val alarmVibration: String = "standard", // standard, strong, soft, none
    val alarmStyle: String = "banner_full" // banner_full, banner_compact, dialog_modal
)

data class NotificationLink(
    val type: String = "none", // none, category, product, group
    val catKey: String? = null,
    val itemIndex: Int? = null,
    val productId: String? = null,
    val productIds: List<String> = emptyList()
)

data class NotificationPayload(
    val id: String = "",
    val title: String = "",
    val message: String = "",
    val type: String = "info", // info, offer, urgent, order
    val target: String = "all", // all, registered, customer
    val targetPhone: String? = null,
    val targetCustomerName: String? = null,
    val link: NotificationLink = NotificationLink(),
    val expiresAt: Timestamp? = null,
    val createdAt: Timestamp? = null,
    val viewedBy: List<String> = emptyList()
)

data class CatalogProduct(
    val id: String = "",
    val name: String = "",
    val cat: String = "",
    val price: Double = 0.0,
    val image: String = ""
)

data class CatalogCategory(
    val key: String = "",
    val name: String = "",
    val icon: String = ""
)
