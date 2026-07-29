package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.Customer
import com.example.MainUiState
import com.example.NotificationLink
import com.example.NotificationPayload
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DairyGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryDarkBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NotificationsScreen(
    uiState: MainUiState,
    onSendNotification: (String, String, String, String, Customer?, NotificationLink, Double) -> Unit,
    onDeleteNotification: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var titleInput by remember { mutableStateOf("") }
    var msgInput by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("info") } // info, offer, urgent, order
    var selectedTarget by remember { mutableStateOf("all") } // all, registered, customer
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }
    var custQuery by remember { mutableStateOf("") }

    var selectedLinkType by remember { mutableStateOf("none") } // none, category, product, group
    var selectedCatKey by remember { mutableStateOf("") }
    var durationHours by remember { mutableDoubleStateOf(24.0) }

    val typeColor = when (selectedType) {
        "offer" -> DairyGold
        "urgent" -> AlarmRed
        "order" -> SuccessGreen
        else -> PrimaryBlue
    }

    val typeIcon = when (selectedType) {
        "offer" -> "🎁"
        "urgent" -> "⚡"
        "order" -> "📦"
        else -> "🔔"
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Text(
            text = "🔔 إرسال إشعار للعملاء",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = PrimaryDarkBlue,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Live Phone Notification Preview
        Text("📱 معاينة حية — هكذا سيظهر للعميل", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = TextMuted)
        Spacer(modifier = Modifier.height(4.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, typeColor, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(typeColor.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(typeIcon, fontSize = 20.sp)
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = titleInput.ifEmpty { "عنوان الإشعار هنا..." },
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = PrimaryDarkBlue
                        )
                        Box(
                            modifier = Modifier
                                .background(typeColor, RoundedCornerShape(10.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("جديد", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = msgInput.ifEmpty { "نص الإشعار والتفاصيل ستظهر هنا..." },
                        fontSize = 12.sp,
                        color = TextDark,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notification Type Selector
        Text("🎨 نوع الإشعار", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryDarkBlue)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TypeChip("عام 🔔", "info", selectedType == "info", PrimaryBlue) { selectedType = "info" }
            TypeChip("عرض 🎁", "offer", selectedType == "offer", DairyGold) { selectedType = "offer" }
            TypeChip("عاجل ⚡", "urgent", selectedType == "urgent", AlarmRed) { selectedType = "urgent" }
            TypeChip("طلبات 📦", "order", selectedType == "order", SuccessGreen) { selectedType = "order" }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Title and Message Fields
        OutlinedTextField(
            value = titleInput,
            onValueChange = { titleInput = it },
            label = { Text("عنوان الإشعار") },
            placeholder = { Text("مثال: عرض خاص اليوم فقط!") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = msgInput,
            onValueChange = { msgInput = it },
            label = { Text("نص الإشعار") },
            placeholder = { Text("اكتب تفاصيل العرض أو التنبيه للعملاء...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Target Selector
        Text("🎯 المستهدفون", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryDarkBlue)

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedTarget == "all",
                onClick = { selectedTarget = "all" },
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
            )
            Text("كل العملاء (عام)", fontSize = 13.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedTarget == "registered",
                onClick = { selectedTarget = "registered" },
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
            )
            Text("العملاء المسجلين فقط", fontSize = 13.sp)
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = selectedTarget == "customer",
                onClick = { selectedTarget = "customer" },
                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
            )
            Text("عميل محدد", fontSize = 13.sp)
        }

        if (selectedTarget == "customer") {
            OutlinedTextField(
                value = custQuery,
                onValueChange = { custQuery = it },
                placeholder = { Text("ابحث عن عميل بالاسم أو رقم الهاتف...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            if (custQuery.isNotEmpty()) {
                val matches = uiState.customers.filter { it.name.contains(custQuery) || it.phone.contains(custQuery) }.take(5)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, RoundedCornerShape(8.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(8.dp))
                ) {
                    matches.forEach { c ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCustomer = c
                                    custQuery = ""
                                }
                                .padding(10.dp)
                        ) {
                            Text("👤 ${c.name} (${c.phone})", fontSize = 12.sp)
                        }
                    }
                }
            }

            if (selectedCustomer != null) {
                Text("تم اختيار: ${selectedCustomer!!.name} (${selectedCustomer!!.phone})", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Expiry Duration
        Text("⏳ مدة ظهور الإشعار (بالساعات):", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryDarkBlue)
        FlowRow(
            modifier = Modifier.padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DurationChip("دائم (0)", 0.0, durationHours == 0.0) { durationHours = 0.0 }
            DurationChip("1 ساعة", 1.0, durationHours == 1.0) { durationHours = 1.0 }
            DurationChip("6 ساعات", 6.0, durationHours == 6.0) { durationHours = 6.0 }
            DurationChip("24 ساعة", 24.0, durationHours == 24.0) { durationHours = 24.0 }
            DurationChip("3 أيام", 72.0, durationHours == 72.0) { durationHours = 72.0 }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Send Button
        Button(
            onClick = {
                val link = NotificationLink(type = selectedLinkType, catKey = if (selectedLinkType == "category") selectedCatKey else null)
                onSendNotification(titleInput, msgInput, selectedType, selectedTarget, selectedCustomer, link, durationHours)
                titleInput = ""
                msgInput = ""
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
            Text("🚀 إرسال الإشعار الآن", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = CardBorder)

        // Sent Notifications History List
        Text(
            text = "📜 الإشعارات المرسلة سابقاً (${uiState.notifications.size})",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = PrimaryDarkBlue,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (uiState.notifications.isEmpty()) {
            Text("لم يتم إرسال أي إشعار حتى الآن", color = TextMuted, fontSize = 12.sp)
        } else {
            uiState.notifications.forEach { notif ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .border(1.dp, CardBorder, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = PrimaryDarkBlue)
                            IconButton(onClick = { onDeleteNotification(notif.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "حذف", tint = AlarmRed)
                            }
                        }
                        Text(notif.message, fontSize = 12.sp, color = TextDark)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Visibility, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(16.dp))
                                Text(" ${notif.viewedBy.size} شخص فتحوه", fontSize = 11.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                            Text("النوع: ${notif.type}", fontSize = 11.sp, color = TextMuted)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun TypeChip(label: String, type: String, isSelected: Boolean, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .border(1.5.dp, if (isSelected) color else CardBorder, RoundedCornerShape(20.dp)),
        color = if (isSelected) color.copy(alpha = 0.15f) else Color.White,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) color else TextDark,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun DurationChip(label: String, value: Double, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .clickable { onClick() }
            .border(1.dp, if (isSelected) PrimaryBlue else CardBorder, RoundedCornerShape(16.dp)),
        color = if (isSelected) PrimaryBlue else Color.White,
        shape = RoundedCornerShape(16.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) Color.White else TextDark,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}
