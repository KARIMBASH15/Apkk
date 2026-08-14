package com.example

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MainUiState(
    val orders: List<Order> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val settings: StoreSettings = StoreSettings(),
    val notifications: List<NotificationPayload> = emptyList(),
    val catalogProducts: List<CatalogProduct> = emptyList(),
    val catalogCategories: Map<String, CatalogCategory> = emptyMap(),
    val catalogCatOrder: List<String> = emptyList(),
    val isAlarmActive: Boolean = false,
    val alarmMessage: String = "",
    val activeTab: Int = 0, // 0: Orders, 1: Archive, 2: Customers, 3: Notifications, 4: Settings
    val isLoadingOrders: Boolean = true,
    val isLoadingCustomers: Boolean = true,
    val isLoadingNotifs: Boolean = true,
    val isServiceRunning: Boolean = true
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    private val db by lazy {
        FirebaseConfig.initialize(application)
        FirebaseFirestore.getInstance()
    }

    private var ordersListener: ListenerRegistration? = null
    private var customersListener: ListenerRegistration? = null
    private var settingsListener: ListenerRegistration? = null
    private var catalogListener: ListenerRegistration? = null
    private var notifsListener: ListenerRegistration? = null

    private var isFirstOrdersSnapshot = true

    private val orderReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == OrderForegroundService.ACTION_NEW_ORDER_ARRIVED) {
                val name = intent.getStringExtra("customerName") ?: "عميل"
                val amount = intent.getDoubleExtra("totalAmount", 0.0)
                triggerInAppAlarm("🔔 طلب جديد من $name بقيمة $amount جنيه")
            }
        }
    }

    init {
        registerBroadcastReceiver()
        startFirestoreListeners()
        // Start background service
        OrderForegroundService.startService(application)
    }

    private fun registerBroadcastReceiver() {
        val filter = IntentFilter(OrderForegroundService.ACTION_NEW_ORDER_ARRIVED)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getApplication<Application>().registerReceiver(orderReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            getApplication<Application>().registerReceiver(orderReceiver, filter)
        }
    }

    override fun onCleared() {
        super.onCleared()
        try {
            getApplication<Application>().unregisterReceiver(orderReceiver)
        } catch (e: Exception) {
            Log.e("MainViewModel", "Receiver unregister error", e)
        }
        ordersListener?.remove()
        customersListener?.remove()
        settingsListener?.remove()
        catalogListener?.remove()
        notifsListener?.remove()
    }

    private fun isCustomerMatch(orderPhone: String, orderName: String, custPhone: String, custName: String): Boolean {
        val p1 = orderPhone.replace(Regex("\\D"), "")
        val p2 = custPhone.replace(Regex("\\D"), "")
        if (p1.isNotEmpty() && p2.isNotEmpty()) {
            if (p1 == p2) return true
            if (p1.length >= 8 && p2.length >= 8 && p1.takeLast(8) == p2.takeLast(8)) return true
        }
        if (orderName.isNotBlank() && custName.isNotBlank()) {
            if (orderName.trim().lowercase() == custName.trim().lowercase()) return true
        }
        return false
    }

    private fun startFirestoreListeners() {
        // Orders Listener
        ordersListener = db.collection("aldwaar_orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update { it.copy(isLoadingOrders = false) }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val itemsRaw = doc.get("items") as? List<Map<String, Any>> ?: emptyList()
                            val items = itemsRaw.map { itemMap ->
                                OrderItem(
                                    id = itemMap["id"] as? String ?: "",
                                    name = itemMap["name"] as? String ?: "",
                                    qty = (itemMap["qty"] as? Long)?.toInt() ?: 1,
                                    price = (itemMap["price"] as? Number)?.toDouble() ?: 0.0,
                                    weight = itemMap["weight"] as? String ?: "",
                                    flavor = itemMap["flavor"] as? String ?: ""
                                )
                            }
                            Order(
                                id = doc.id,
                                orderId = doc.getString("orderId") ?: doc.id.take(8),
                                customerName = doc.getString("customerName") ?: "",
                                customerPhone = doc.getString("customerPhone") ?: "",
                                customerAddress = doc.getString("customerAddress") ?: "",
                                items = items,
                                subtotal = doc.getDouble("subtotal") ?: 0.0,
                                discount = doc.getDouble("discount") ?: 0.0,
                                promoCode = doc.getString("promoCode") ?: "",
                                shippingPrice = doc.getDouble("shippingPrice"),
                                total = doc.getDouble("total") ?: 0.0,
                                status = doc.getString("status") ?: "received",
                                createdAt = doc.getTimestamp("createdAt"),
                                updatedAt = doc.getTimestamp("updatedAt")
                            )
                        } catch (e: Exception) {
                            Log.e("MainViewModel", "Parse order error", e)
                            null
                        }
                    }

                    // Auto-apply customer fixed shipping or store fixed shipping if missing
                    val updatedList = list.map { ord ->
                        if (ord.shippingPrice == null || ord.shippingPrice == 0.0) {
                            val matchingCust = _uiState.value.customers.firstOrNull { c ->
                                c.fixedShippingPrice != null && c.fixedShippingPrice!! > 0 &&
                                        isCustomerMatch(ord.customerPhone, ord.customerName, c.phone, c.name)
                            }
                            val targetPrice = matchingCust?.fixedShippingPrice
                                ?: if (_uiState.value.settings.shippingMode == "fixed" && _uiState.value.settings.fixedShippingPrice > 0) {
                                    _uiState.value.settings.fixedShippingPrice
                                } else null

                            if (targetPrice != null) {
                                db.collection("aldwaar_orders").document(ord.id).update("shippingPrice", targetPrice)
                                ord.copy(shippingPrice = targetPrice)
                            } else ord
                        } else ord
                    }

                    if (!isFirstOrdersSnapshot) {
                        val hasNewReceived = snapshot.documentChanges.any {
                            it.type == com.google.firebase.firestore.DocumentChange.Type.ADDED &&
                                    (it.document.getString("status") ?: "received") == "received"
                        }
                        if (hasNewReceived) {
                            val latest = updatedList.firstOrNull { it.status == "received" }
                            val msg = if (latest != null) {
                                val itemsStr = latest.items.joinToString("\n• ") { "${it.name} (×${it.qty})" }
                                "🔔 طلب جديد من: ${latest.customerName}\n💰 المبلغ: ${latest.total} جنيه\n📦 المنتجات بالطلب:\n• $itemsStr"
                            } else {
                                "🔔 وصل طلب جديد الآن!"
                            }
                            triggerInAppAlarm(msg)
                        }
                    }
                    isFirstOrdersSnapshot = false

                    _uiState.update { it.copy(orders = updatedList, isLoadingOrders = false) }
                }
            }

        // Settings Listener
        settingsListener = db.collection("aldwaar_settings").document("config")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val mode = snapshot.getString("shippingMode") ?: "manual"
                    val fixedPrice = snapshot.getDouble("fixedShippingPrice") ?: 0.0
                    val invSize = snapshot.getString("invoiceSize") ?: "88mm"
                    val alarmTone = snapshot.getString("alarmTone") ?: "default"
                    val alarmVibration = snapshot.getString("alarmVibration") ?: "standard"
                    val alarmStyle = snapshot.getString("alarmStyle") ?: "banner_full"
                    _uiState.update {
                        it.copy(
                            settings = StoreSettings(
                                shippingMode = mode,
                                fixedShippingPrice = fixedPrice,
                                invoiceSize = invSize,
                                alarmTone = alarmTone,
                                alarmVibration = alarmVibration,
                                alarmStyle = alarmStyle
                            )
                        )
                    }
                }
            }

        // Customers Listener
        customersListener = db.collection("aldwaar_customers")
            .orderBy("lastOrderAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update { it.copy(isLoadingCustomers = false) }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            Customer(
                                id = doc.id,
                                name = doc.getString("name") ?: "",
                                phone = doc.getString("phone") ?: "",
                                address = doc.getString("address") ?: "",
                                ordersCount = doc.getLong("ordersCount")?.toInt() ?: 0,
                                totalSpent = doc.getDouble("totalSpent") ?: 0.0,
                                fixedShippingPrice = doc.getDouble("fixedShippingPrice"),
                                lastOrderAt = doc.getTimestamp("lastOrderAt")
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _uiState.update { it.copy(customers = list, isLoadingCustomers = false) }
                }
            }

        // Catalog Listener
        catalogListener = db.collection("aldwaar_store").document("catalog")
            .addSnapshotListener { snapshot, error ->
                if (snapshot != null && snapshot.exists()) {
                    val prodsRaw = snapshot.get("products") as? List<Map<String, Any>> ?: emptyList()
                    val products = prodsRaw.map { p ->
                        CatalogProduct(
                            id = p["id"] as? String ?: "",
                            name = p["name"] as? String ?: "",
                            cat = p["cat"] as? String ?: "",
                            price = (p["price"] as? Number)?.toDouble() ?: 0.0,
                            image = p["image"] as? String ?: ""
                        )
                    }
                    val catInfoRaw = snapshot.get("catInfo") as? Map<String, Map<String, Any>> ?: emptyMap()
                    val categories = catInfoRaw.mapValues { entry ->
                        CatalogCategory(
                            key = entry.key,
                            name = entry.value["name"] as? String ?: entry.key,
                            icon = entry.value["icon"] as? String ?: ""
                        )
                    }
                    val catOrder = snapshot.get("catOrder") as? List<String> ?: categories.keys.toList()

                    _uiState.update {
                        it.copy(catalogProducts = products, catalogCategories = categories, catalogCatOrder = catOrder)
                    }
                }
            }

        // Notifications Listener
        notifsListener = db.collection("aldwaar_notifications")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update { it.copy(isLoadingNotifs = false) }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        try {
                            val linkMap = doc.get("link") as? Map<String, Any> ?: emptyMap()
                            val link = NotificationLink(
                                type = linkMap["type"] as? String ?: "none",
                                catKey = linkMap["catKey"] as? String,
                                itemIndex = (linkMap["itemIndex"] as? Long)?.toInt(),
                                productId = linkMap["productId"] as? String,
                                productIds = linkMap["productIds"] as? List<String> ?: emptyList()
                            )
                            NotificationPayload(
                                id = doc.id,
                                title = doc.getString("title") ?: "",
                                message = doc.getString("message") ?: "",
                                type = doc.getString("type") ?: "info",
                                target = doc.getString("target") ?: "all",
                                targetPhone = doc.getString("targetPhone"),
                                targetCustomerName = doc.getString("targetCustomerName"),
                                link = link,
                                expiresAt = doc.getTimestamp("expiresAt"),
                                createdAt = doc.getTimestamp("createdAt"),
                                viewedBy = doc.get("viewedBy") as? List<String> ?: emptyList()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    _uiState.update { it.copy(notifications = list, isLoadingNotifs = false) }
                }
            }
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(activeTab = index) }
    }

    fun triggerInAppAlarm(message: String) {
        val currentSettings = _uiState.value.settings
        AudioAlarmManager.startAlarm(
            getApplication(),
            currentSettings.alarmTone,
            currentSettings.alarmVibration
        )
        _uiState.update { it.copy(isAlarmActive = true, alarmMessage = message) }
    }

    fun playPreviewTone(tone: String, vibration: String) {
        AudioAlarmManager.playPreview(getApplication(), tone, vibration)
    }

    fun stopAlarm() {
        AudioAlarmManager.stopAlarm(getApplication())
        try {
            OrderForegroundService.stopAlarm(getApplication())
        } catch (_: Exception) {}
        _uiState.update { it.copy(isAlarmActive = false) }
    }

    fun saveShippingPrice(orderId: String, price: Double) {
        viewModelScope.launch {
            try {
                db.collection("aldwaar_orders").document(orderId).update(
                    mapOf(
                        "shippingPrice" to price,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                _toastEvent.emit("✅ تم حفظ سعر الشحن ($price جنيه)")
            } catch (e: Exception) {
                _toastEvent.emit("❌ تعذر حفظ سعر الشحن: ${e.message}")
            }
        }
    }

    fun advanceOrderStatus(orderId: String, newStatus: String) {
        viewModelScope.launch {
            try {
                db.collection("aldwaar_orders").document(orderId).update(
                    mapOf(
                        "status" to newStatus,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                )
                _toastEvent.emit("✅ تم تحديث حالة الطلب")
            } catch (e: Exception) {
                _toastEvent.emit("❌ تعذر تحديث حالة الطلب")
            }
        }
    }

    fun archiveOrder(orderId: String) {
        advanceOrderStatus(orderId, "archived")
    }

    fun saveStoreSettings(
        mode: String,
        fixedPrice: Double,
        invoiceSize: String,
        alarmTone: String = "default",
        alarmVibration: String = "standard",
        alarmStyle: String = "banner_full"
    ) {
        viewModelScope.launch {
            try {
                db.collection("aldwaar_settings").document("config").set(
                    mapOf(
                        "shippingMode" to mode,
                        "fixedShippingPrice" to fixedPrice,
                        "invoiceSize" to invoiceSize,
                        "alarmTone" to alarmTone,
                        "alarmVibration" to alarmVibration,
                        "alarmStyle" to alarmStyle,
                        "updatedAt" to FieldValue.serverTimestamp()
                    ),
                    com.google.firebase.firestore.SetOptions.merge()
                )

                // If fixed shipping mode enabled, auto-apply fixed price to received orders without shipping price
                if (mode == "fixed" && fixedPrice > 0) {
                    _uiState.value.orders.filter { it.status == "received" && (it.shippingPrice == null || it.shippingPrice == 0.0) }.forEach { ord ->
                        db.collection("aldwaar_orders").document(ord.id).update(
                            mapOf(
                                "shippingPrice" to fixedPrice,
                                "updatedAt" to FieldValue.serverTimestamp()
                            )
                        )
                    }
                }

                _toastEvent.emit("✅ تم حفظ إعدادات المتجر والنغمات وتطبيق الشحن الثابت")
            } catch (e: Exception) {
                _toastEvent.emit("❌ تعذر حفظ الإعدادات: ${e.message}")
            }
        }
    }

    fun saveCustomerShipPrice(customerId: String, price: Double) {
        viewModelScope.launch {
            try {
                db.collection("aldwaar_customers").document(customerId).update("fixedShippingPrice", price)

                db.collection("aldwaar_customers").document(customerId).get().addOnSuccessListener { custDoc ->
                    if (custDoc != null && custDoc.exists()) {
                        val custPhone = custDoc.getString("phone") ?: ""
                        val custName = custDoc.getString("name") ?: ""

                        _uiState.value.orders.filter { ord ->
                            isCustomerMatch(ord.customerPhone, ord.customerName, custPhone, custName)
                        }.forEach { ord ->
                            db.collection("aldwaar_orders").document(ord.id).update(
                                mapOf(
                                    "shippingPrice" to price,
                                    "updatedAt" to FieldValue.serverTimestamp()
                                )
                            )
                        }
                    }
                }

                _toastEvent.emit("✅ تم حفظ وتطبيق سعر الشحن الثابت (${price} ج) لهذا العميل على طلباته")
            } catch (e: Exception) {
                _toastEvent.emit("❌ تعذر حفظ سعر العميل: ${e.message}")
            }
        }
    }

    fun clearCustomerShipPrice(customerId: String) {
        viewModelScope.launch {
            try {
                db.collection("aldwaar_customers").document(customerId).update("fixedShippingPrice", FieldValue.delete())
                _toastEvent.emit("✅ سيتم تحديد سعر الشحن عند الاستلام لهذا العميل")
            } catch (e: Exception) {
                _toastEvent.emit("❌ تعذر إلغاء السعر الثابت")
            }
        }
    }

    fun sendNotification(
        title: String,
        message: String,
        type: String,
        target: String,
        targetCust: Customer?,
        link: NotificationLink,
        durationHours: Double
    ) {
        viewModelScope.launch {
            try {
                val expiresAt = if (durationHours > 0) {
                    Timestamp(java.util.Date(System.currentTimeMillis() + (durationHours * 3600 * 1000).toLong()))
                } else null

                val payload = hashMapOf(
                    "title" to title,
                    "message" to message,
                    "type" to type,
                    "target" to target,
                    "targetPhone" to if (target == "customer") targetCust?.phone else null,
                    "targetCustomerName" to if (target == "customer") targetCust?.name else null,
                    "link" to hashMapOf(
                        "type" to link.type,
                        "catKey" to link.catKey,
                        "itemIndex" to link.itemIndex,
                        "productId" to link.productId,
                        "productIds" to link.productIds
                    ),
                    "expiresAt" to expiresAt,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "viewedBy" to emptyList<String>()
                )

                db.collection("aldwaar_notifications").add(payload)
                _toastEvent.emit("🚀 تم إرسال الإشعار بنجاح")
            } catch (e: Exception) {
                _toastEvent.emit("❌ تعذر إرسال الإشعار: ${e.message}")
            }
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            try {
                db.collection("aldwaar_notifications").document(id).delete()
                _toastEvent.emit("🗑 تم حذف الإشعار")
            } catch (e: Exception) {
                _toastEvent.emit("❌ تعذر حذف الإشعار")
            }
        }
    }
}
