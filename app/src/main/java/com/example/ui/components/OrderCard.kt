package com.example.ui.components

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Order
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
    var shipInput by remember(order.shippingPrice) {
        mutableStateOf(order.shippingPrice?.toString() ?: "")
    }

    val formattedDate = remember(order.createdAt) {
        try {
            order.createdAt?.toDate()?.let {
                SimpleDateFormat("MM/dd - hh:mm a", Locale("ar")).format(it)
            } ?: "الآن"
        } catch (e: Exception) { "الآن" }
    }

    val hasShip = order.shippingPrice != null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Order ID & Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "طلب #${order.orderId}",
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp,
                    color = PrimaryDarkBlue
                )
                Text(
                    text = formattedDate,
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Customer Details
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Person, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.padding(end = 4.dp))
                Text(order.customerName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted, modifier = Modifier.padding(end = 4.dp))
                Text(order.customerPhone, fontSize = 12.sp, color = TextMuted)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.padding(end = 4.dp))
                Text(order.customerAddress, fontSize = 12.sp, color = TextMuted, maxLines = 2)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Items List Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF7FAFF), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Column {
                    order.items.forEach { item ->
                        Text(
                            text = "• ${item.name} ${if (item.weight.isNotEmpty()) "(" + item.weight + ")" else ""} ×${item.qty}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextDark,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Money Details
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("المجموع:", fontSize = 12.sp, color = TextMuted)
                Text("${order.subtotal} جنيه", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            if (order.discount > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("خصم (${order.promoCode}):", fontSize = 12.sp, color = TextMuted)
                    Text("-${order.discount} جنيه", fontSize = 12.sp, color = Color.Red)
                }
            }

            // Shipping Price Input or Display
            if (order.status == "received" && !hasShip) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = shipInput,
                        onValueChange = { shipInput = it },
                        label = { Text("سعر الشحن", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = CardBorder
                        )
                    )
                    Button(
                        onClick = {
                            val p = shipInput.toDoubleOrNull() ?: 0.0
                            onSaveShipping(p)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("حفظ", fontSize = 12.sp)
                    }
                }
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("سعر الشحن:", fontSize = 12.sp, color = TextMuted)
                    Text(if (hasShip) "${order.shippingPrice} جنيه" else "غير محدد", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CardBorder)

            // Grand Total
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("الإجمالي النهائي:", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = PrimaryDarkBlue)
                Text("${order.grandTotal()} جنيه", fontSize = 15.sp, fontWeight = FontWeight.Black, color = PrimaryBlue)
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                when (order.status) {
                    "received" -> {
                        Button(
                            onClick = { onAdvanceStatus("delivering") },
                            enabled = hasShip,
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("🚚 بدء التوصيل", fontSize = 12.sp)
                        }
                    }
                    "delivering" -> {
                        Button(
                            onClick = { onAdvanceStatus("delivered") },
                            colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("✅ تم التوصيل", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onAdvanceStatus("received") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("↩ رجوع", fontSize = 12.sp)
                        }
                    }
                    "delivered" -> {
                        Button(
                            onClick = onArchive,
                            colors = ButtonDefaults.buttonColors(containerColor = DairyGold),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Archive, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("📥 أرشفة", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = { onAdvanceStatus("delivering") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("↩ رجوع", fontSize = 12.sp)
                        }
                    }
                    "archived" -> {
                        Button(
                            onClick = { onAdvanceStatus("delivered") },
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Undo, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("استعادة للتم التوصيل", fontSize = 12.sp)
                        }
                    }
                }

                OutlinedButton(
                    onClick = onPrintInvoice,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = "طباعة")
                }
            }
        }
    }
}
