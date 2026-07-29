package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.Customer
import com.example.Order
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryDarkBlue
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun OrderInvoiceDialog(
    order: Order,
    invoiceSize: String, // 88mm or a4
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val formattedDate = try {
        order.createdAt?.toDate()?.let {
            SimpleDateFormat("yyyy/MM/dd - hh:mm a", Locale("ar")).format(it)
        } ?: "الآن"
    } catch (e: Exception) { "الآن" }

    val statusText = when (order.status) {
        "received" -> "تم استلام الطلب"
        "delivering" -> "جاري توصيل الطلب"
        "delivered" -> "تم التوصيل"
        "archived" -> "مؤرشف"
        else -> order.status
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🖨 معاينة الفاتورة (${if (invoiceSize == "a4") "A4" else "88mm"})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = PrimaryDarkBlue
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CardBorder)

                // Thermal Receipt Preview Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFCFDFF), RoundedCornerShape(12.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "البان الدوار",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = PrimaryBlue
                        )
                        Text(
                            text = "Al Dwaar Dairy",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "فاتورة طلب رقم: ${order.orderId}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = PrimaryBlue
                        )

                        // Customer Info
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("👤 العميل: ${order.customerName}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("📞 الهاتف: ${order.customerPhone}", fontSize = 12.sp, color = TextDark)
                            Text("📍 العنوان: ${order.customerAddress}", fontSize = 12.sp, color = TextDark)
                            Text("📅 التاريخ: $formattedDate", fontSize = 11.sp, color = TextMuted)
                            Text("🏷 الحالة: $statusText", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = CardBorder
                        )

                        // Items List
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("المنتج", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(2f))
                            Text("الكمية", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                            Text("السعر", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp), color = CardBorder)

                        order.items.forEach { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(2f)) {
                                    Text(item.name, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    if (item.weight.isNotEmpty() || item.flavor.isNotEmpty()) {
                                        Text(
                                            "${item.weight} ${if (item.flavor.isNotEmpty()) "- " + item.flavor else ""}",
                                            fontSize = 10.sp,
                                            color = TextMuted
                                        )
                                    }
                                }
                                Text("×${item.qty}", fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                                Text("${item.price * item.qty} ج", fontSize = 12.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = CardBorder
                        )

                        // Money Breakdown
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("المجموع:", fontSize = 12.sp, color = TextMuted)
                            Text("${order.subtotal} جنيه", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        if (order.discount > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("خصم (${order.promoCode}):", fontSize = 12.sp, color = TextMuted)
                                Text("-${order.discount} جنيه", fontSize = 12.sp, color = Color.Red)
                            }
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("سعر التوصيل:", fontSize = 12.sp, color = TextMuted)
                            Text(if (order.shippingPrice != null) "${order.shippingPrice} جنيه" else "غير محدد", fontSize = 12.sp)
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = PrimaryBlue
                        )

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("الإجمالي النهائي:", fontSize = 15.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
                            Text("${order.grandTotal()} جنيه", fontSize = 15.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            shareInvoiceText(context, order, formattedDate, statusText)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                        Text("مشاركة الفاتورة")
                    }

                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("إغلاق")
                    }
                }
            }
        }
    }
}

private fun shareInvoiceText(context: Context, order: Order, formattedDate: String, statusText: String) {
    val itemsTxt = order.items.joinToString("\n") {
        "• ${it.name} (${it.weight} ${it.flavor}) ×${it.qty} = ${it.price * it.qty} ج"
    }

    val invoiceTxt = """
        🧾 *فاتورة البان الدوار - Al Dwaar Dairy*
        ==============================
        رقم الطلب: ${order.orderId}
        العميل: ${order.customerName}
        الهاتف: ${order.customerPhone}
        العنوان: ${order.customerAddress}
        التاريخ: $formattedDate
        الحالة: $statusText
        ==============================
        *المنتجات:*
        $itemsTxt
        ==============================
        المجموع: ${order.subtotal} جنيه
        ${if (order.discount > 0) "الخصم: -${order.discount} جنيه\n" else ""}سعر التوصيل: ${order.shippingPrice ?: 0} جنيه
        *الإجمالي النهائي: ${order.grandTotal()} جنيه*
        ==============================
        شكراً لتسوقكم من البان الدوار 🥛
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, invoiceTxt)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "مشاركة الفاتورة عبر"))
}
