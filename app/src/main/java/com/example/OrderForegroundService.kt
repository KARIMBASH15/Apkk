package com.example

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class OrderForegroundService : Service() {

    private var firestoreListener: ListenerRegistration? = null
    private var isFirstSnapshot = true
    private val knownOrderIds = mutableSetOf<String>()

    companion object {
        const val CHANNEL_ID_SERVICE = "aldwaar_service_channel"
        const val CHANNEL_ID_NEW_ORDERS = "aldwaar_new_orders_channel"
        const val NOTIF_ID_FOREGROUND = 1001
        const val NOTIF_ID_NEW_ORDER = 1002
        const val ACTION_START_SERVICE = "com.example.action.START_SERVICE"
        const val ACTION_STOP_SERVICE = "com.example.action.STOP_SERVICE"
        const val ACTION_STOP_ALARM = "com.example.action.STOP_ALARM"
        const val ACTION_NEW_ORDER_ARRIVED = "com.example.action.NEW_ORDER_ARRIVED"

        fun startService(context: Context) {
            val intent = Intent(context, OrderForegroundService::class.java).apply {
                action = ACTION_START_SERVICE
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, OrderForegroundService::class.java).apply {
                action = ACTION_STOP_SERVICE
            }
            context.stopService(intent)
        }

        fun stopAlarm(context: Context) {
            val intent = Intent(context, OrderForegroundService::class.java).apply {
                action = ACTION_STOP_ALARM
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        FirebaseConfig.initialize(applicationContext)
        createNotificationChannels()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP_SERVICE) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            return START_NOT_STICKY
        }

        if (intent?.action == ACTION_STOP_ALARM) {
            AudioAlarmManager.stopAlarm(applicationContext)
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(NOTIF_ID_NEW_ORDER)
            return START_STICKY
        }

        startForeground(NOTIF_ID_FOREGROUND, createServiceNotification())
        listenForNewOrders()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        firestoreListener?.remove()
        firestoreListener = null
        AudioAlarmManager.stopAlarm(applicationContext)
        super.onDestroy()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            // Service persistent channel
            val serviceChannel = NotificationChannel(
                CHANNEL_ID_SERVICE,
                "خدمة المتابعة في الخلفية",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "قناة إبقاء الخدمة تعمل للتنبيه الفوري بالطلبات"
            }
            notificationManager.createNotificationChannel(serviceChannel)

            // High priority alert channel without default device alarm tone
            val newOrderChannel = NotificationChannel(
                CHANNEL_ID_NEW_ORDERS,
                "تنبيهات الطلبات الجديدة 🔔",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "إشعارات هامة ذات أولوية عالية عند وصول طلب جديد"
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 450, 250, 450, 250)
                setSound(null, null) // In-app AudioAlarmManager handles pleasant custom bells
            }
            notificationManager.createNotificationChannel(newOrderChannel)
        }
    }

    private fun createServiceNotification(): android.app.Notification {
        val tapIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID_SERVICE)
            .setContentTitle("البان الدوار - خدمة التنبيهات")
            .setContentText("تنبيهات الطلبات الجديدة تعمل وتراقب الفايبرستور في الخلفية 📦")
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun listenForNewOrders() {
        if (firestoreListener != null) return

        val db = FirebaseFirestore.getInstance()
        firestoreListener = db.collection("aldwaar_orders")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("OrderService", "Firestore error: ${error.message}", error)
                    return@addSnapshotListener
                }

                if (snapshot == null) return@addSnapshotListener

                if (isFirstSnapshot) {
                    // Collect existing order IDs without alerting
                    snapshot.documents.forEach { doc ->
                        knownOrderIds.add(doc.id)
                    }
                    isFirstSnapshot = false
                } else {
                    for (dc in snapshot.documentChanges) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            val orderId = dc.document.id
                            val status = dc.document.getString("status") ?: "received"
                            val custName = dc.document.getString("customerName") ?: "عميل"
                            val total = dc.document.getDouble("total") ?: 0.0

                            if (!knownOrderIds.contains(orderId) && status == "received") {
                                knownOrderIds.add(orderId)
                                triggerNewOrderAlert(orderId, custName, total)
                            }
                        }
                    }
                }
            }
    }

    private fun triggerNewOrderAlert(orderDocId: String, customerName: String, totalAmount: Double) {
        // Play audio & vibration
        AudioAlarmManager.startAlarm(applicationContext)

        // Broadcast to Activity if active
        val broadcastIntent = Intent(ACTION_NEW_ORDER_ARRIVED).apply {
            putExtra("orderDocId", orderDocId)
            putExtra("customerName", customerName)
            putExtra("totalAmount", totalAmount)
            setPackage(packageName)
        }
        sendBroadcast(broadcastIntent)

        // Show High Priority System Notification with Stop Alarm Action
        val tapIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_tab", "orders")
            putExtra("highlight_order", orderDocId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            orderDocId.hashCode(),
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopAlarmIntent = Intent(this, OrderForegroundService::class.java).apply {
            action = ACTION_STOP_ALARM
        }
        val stopAlarmPendingIntent = PendingIntent.getService(
            this,
            10099,
            stopAlarmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID_NEW_ORDERS)
            .setContentTitle("🔔 طلب جديد وارد! - البان الدوار")
            .setContentText("العميل: $customerName | الإجمالي: $totalAmount جنيه")
            .setStyle(NotificationCompat.BigTextStyle().bigText("وصل طلب جديد الآن من $customerName بقيمة $totalAmount جنيه.\nاضغط لمعاينة الطلب وتحديد سعر التوصيل."))
            .setSmallIcon(android.R.drawable.stat_notify_chat)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_EVENT)
            .setContentIntent(pendingIntent)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "🔕 إيقاف الصوت", stopAlarmPendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID_NEW_ORDER, notification)
    }
}
