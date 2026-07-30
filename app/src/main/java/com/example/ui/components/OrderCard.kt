package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Order
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DairyGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryDarkBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun OrderCard(
    order: Order,
    onSaveShipping: (Double) -> Unit,
    onAdvanceStatus: (String) -> Unit,
    onArchive: () -> Unit,
    onPrintInvoice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showInvoicePreview by remember { mutableStateOf(false) }
    var shipInput by remember(order.shippingPrice) {
        mutableStateOf(order.shippingPrice?.toString() ?: "")
    }

    if (showInvoicePreview) {
        OrderInvoiceDialog(
            order = order,
            invoiceSize = "88mm",
            onDismiss = { showInvoicePreview = false }
        )
    }

    val formattedDate = remember(order.createdAt) {
        try {
            order.createdAt?.toDate()?.let {
                SimpleDateFormat("MM/dd - hh:mm a", Locale("ar")).format(it)
            } ?: "الآن"
        } catch (e: Exception) { "الآن" }
    }

    val hasShip = order.shippingPrice != null

    val (statusLabel, statusColor, statusBg) = when (order.status) {
        "received" -> Triple("طلب جديد 📦", PrimaryBlue, PrimaryBlue.copy(alpha = 0.12f))
        "delivering" -> Triple("جاري التوصيل 🚚", DairyGold, DairyGold.copy(alpha = 0.18f))
        "delivered" -> Triple("تم التوصيل ✅", SuccessGreen, SuccessGreen.copy(alpha = 0.14f))
        else -> Triple("مؤرشف 📥", TextMuted, Color(0xFFF0F4FA))
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, statusColor.copy(alpha = 0.5f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Header: Order ID + Status Pill + Date Stamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = PrimaryDarkBlue,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "#${order.orderId}",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = statusBg,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = statusLabel,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "📅 $formattedDate",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Customer Info Box
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF9FBFF),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(PrimaryBlue.copy(alpha = 0.12f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(order.customerName, fontWeight = FontWeight.Black, fontSize = 16.sp, color = PrimaryDarkBlue)
                        }

                        // Dial button
                        Surface(
                            modifier = Modifier.clickable {
                                try {
                                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${order.customerPhone}"))
                                    context.startActivity(intent)
                                } catch (_: Exception) {}
                            },
                            color = PrimaryBlue.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Phone, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(order.customerPhone, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = AlarmRed, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(order.customerAddress, fontSize = 13.sp, color = TextDark, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Prominent Items Box
            Text(
                text = "🛒 المنتجات المطلوبة (${order.items.size}):",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = PrimaryDarkBlue,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF2F6FE),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    order.items.forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${item.name} ${if (item.weight.isNotEmpty()) "(" + item.weight + ")" else ""}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextDark,
                                modifier = Modifier.weight(1f)
                            )
                            Surface(
                                color = PrimaryBlue,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "×${item.qty}",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        if (idx < order.items.size - 1) {
                            HorizontalDivider(color = Color(0xFFE2ECFA), modifier = Modifier.padding(vertical = 2.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Financials Breakdown & Shipping Input
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("سعر المنتجات:", fontSize = 13.sp, color = TextMuted)
                Text("${order.subtotal} جنيه", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextDark)
            }
            if (order.discount > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("خصم كود (${order.promoCode}):", fontSize = 13.sp, color = TextMuted)
                    Text("-${order.discount} جنيه", fontSize = 13.sp, color = AlarmRed, fontWeight = FontWeight.Bold)
                }
            }

            if (order.status == "received" && !hasShip) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = shipInput,
                        onValueChange = { shipInput = it },
                        label = { Text("أدخل سعر التوصيل 🚚", fontSize = 12.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = CardBorder,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        )
                    )
                    Button(
                        onClick = {
                            val p = shipInput.toDoubleOrNull() ?: 0.0
                            onSaveShipping(p)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Text("حفظ السعر", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("سعر التوصيل:", fontSize = 13.sp, color = TextMuted)
                    Text(if (hasShip) "${order.shippingPrice} جنيه" else "سيتم تحديده", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = CardBorder)

            // Grand Total Prominent Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryDarkBlue,
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("إجمالي المطلوب من العميل:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD0E2FF))
                    Text("${order.grandTotal()} جنيه", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Big Touch-Target Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (order.status) {
                    "received" -> {
                        Button(
                            onClick = { onAdvanceStatus("delivering") },
                            enabled = hasShip,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("🚚 بدء التوصيل", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    "delivering" -> {
                        Button(
                            onClick = { onAdvanceStatus("delivered") },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("✅ تم التوصيل", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onAdvanceStatus("received") },
                            modifier = Modifier
                                .weight(0.8f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("↩ رجوع", fontSize = 13.sp)
                        }
                    }
                    "delivered" -> {
                        Button(
                            onClick = onArchive,
                            colors = ButtonDefaults.buttonColors(containerColor = DairyGold),
                            modifier = Modifier
                                .weight(1.2f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                            Text("📥 أرشفة الطلب", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { onAdvanceStatus("delivering") },
                            modifier = Modifier
                                .weight(0.8f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("↩ رجوع", fontSize = 13.sp)
                        }
                    }
                    "archived" -> {
                        Button(
                            onClick = { onAdvanceStatus("delivered") },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("استعادة للتم التوصيل", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                OutlinedButton(
                    onClick = { showInvoicePreview = true },
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Visibility, contentDescription = "عرض الفاتورة", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("عرض 👁️", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }

                OutlinedButton(
                    onClick = onPrintInvoice,
                    modifier = Modifier.height(48.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = "طباعة", tint = PrimaryBlue, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("طباعة 🖨", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                }
            }
        }
    }
}

