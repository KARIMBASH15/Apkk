package com.example.ui.screens

import android.content.Context
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import com.example.AudioAlarmManager
import com.example.MainUiState
import com.example.OrderForegroundService
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryDarkBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    uiState: MainUiState,
    onSaveSettings: (String, Double, String) -> Unit,
    onTriggerTestAlarm: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var shipMode by remember(uiState.settings.shippingMode) {
        mutableStateOf(uiState.settings.shippingMode)
    }
    var fixedPriceInput by remember(uiState.settings.fixedShippingPrice) {
        mutableStateOf(if (uiState.settings.fixedShippingPrice > 0) uiState.settings.fixedShippingPrice.toString() else "")
    }
    var invoiceSize by remember(uiState.settings.invoiceSize) {
        mutableStateOf(uiState.settings.invoiceSize)
    }

    var isSizeDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "⚙️ الإعدادات العامة",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = PrimaryDarkBlue,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Shipping Settings Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = PrimaryBlue)
                    Text(
                        text = " 🚚 سعر التوصيل والخدمة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryDarkBlue
                    )
                }
                Text(
                    text = "اختر كيفية حساب سعر الشحن لطلبات العملاء الجديدة.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = shipMode == "manual",
                        onClick = { shipMode = "manual" },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                    )
                    Column {
                        Text("تحديد السعر يدوياً عند استلام كل طلب", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("يظهر للعميل: 'سيتم تحديد سعر التوصيل بعد استلام الطلب' وتدخله بنفسك.", fontSize = 11.sp, color = TextMuted)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = shipMode == "fixed",
                        onClick = { shipMode = "fixed" },
                        colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                    )
                    Column {
                        Text("سعر توصيل ثابت لكل الطلبات", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("يُطبق تلقائياً على كل طلب جديد ويظهر للعميل أثناء الشراء.", fontSize = 11.sp, color = TextMuted)
                    }
                }

                if (shipMode == "fixed") {
                    OutlinedTextField(
                        value = fixedPriceInput,
                        onValueChange = { fixedPriceInput = it },
                        label = { Text("سعر التوصيل الثابت (جنيه)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Invoice Print Settings Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = PrimaryBlue)
                    Text(
                        text = " 🖨 إعدادات طباعة وتصدير الفاتورة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryDarkBlue
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = isSizeDropdownExpanded,
                    onExpandedChange = { isSizeDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = if (invoiceSize == "a4") "A4 (ورقة طباعة عادية)" else "88مل (طابعة إيصالات حرارية)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("مقاس الفاتورة الافتراضي") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSizeDropdownExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = isSizeDropdownExpanded,
                        onDismissRequest = { isSizeDropdownExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("88مل (طابعة إيصالات حرارية)") },
                            onClick = {
                                invoiceSize = "88mm"
                                isSizeDropdownExpanded = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("A4 (ورقة طباعة عادية)") },
                            onClick = {
                                invoiceSize = "a4"
                                isSizeDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Background Service & Notifications Test Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(14.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = SuccessGreen)
                    Text(
                        text = " 🔔 خدمة التنبيهات وإشعار الخلفية",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryDarkBlue
                    )
                }
                Text(
                    text = "خدمة الفايبرستور Foreground Service تعمل في الخلفية لإصدار صوت تنبيه فور وصول أي طلب جديد.",
                    fontSize = 11.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            OrderForegroundService.startService(context)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إعادة تشغيل الخدمة", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = onTriggerTestAlarm,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("اختبار صوت التنبيه 🔔", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Save Button
        Button(
            onClick = {
                val price = fixedPriceInput.toDoubleOrNull() ?: 0.0
                onSaveSettings(shipMode, price, invoiceSize)
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
            Text("💾 حفظ الإعدادات", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
