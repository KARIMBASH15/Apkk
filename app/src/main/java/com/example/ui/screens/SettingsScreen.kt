package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Vibration
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
    onSaveSettings: (String, Double, String, String, String, String) -> Unit,
    onPlayPreviewTone: (String, String) -> Unit,
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
    var selectedTone by remember(uiState.settings.alarmTone) {
        mutableStateOf(uiState.settings.alarmTone)
    }
    var selectedVibration by remember(uiState.settings.alarmVibration) {
        mutableStateOf(uiState.settings.alarmVibration)
    }
    var selectedStyle by remember(uiState.settings.alarmStyle) {
        mutableStateOf(uiState.settings.alarmStyle)
    }

    var isSizeDropdownExpanded by remember { mutableStateOf(false) }

    val toneOptions = listOf(
        Triple("default", "🔔 جرس متجر الدوار الكلاسيكي", "نغمة جرس متجر موسيقية مبهجة (نغمة التطبيق الخاصة)"),
        Triple("chime", "🚚 نغمة التوصيل السريعة", "أربيجيو نغمي سريع ومميز لوصول الطلبات"),
        Triple("siren", "⚡ تنبيه نغمي متسارع", "نغمة تنبيه ثنائية متتالية واضحة ومسموعة"),
        Triple("soft", "🎵 نغمة هادئة (ماريمبا وهارب)", "نغمة ناعمة ومريحة للأذن بأوتار متناسقة"),
        Triple("triple", "🔊 تنبيه بلوري ثلاثي", "3 دقات بلورية كريستالية واضحة ورنانة")
    )

    val vibrationOptions = listOf(
        Pair("standard", "📳 هزاز عادي متقطع"),
        Pair("strong", "💥 هزاز قوي متصل"),
        Pair("soft", "🍃 هزاز خفيف قصير"),
        Pair("none", "🔇 بدون هزاز")
    )

    val styleOptions = listOf(
        Triple("banner_full", "📣 شريط عريض بالأعلى", "شريط إشعار أحمر يظهر على كامل أعلى الشاشة"),
        Triple("dialog_modal", "💬 نافذة منبثقة وسط الشاشة", "نافذة حوار مركزية تمنع تجاهل الطلب بأزرار كبيرة"),
        Triple("banner_compact", "🔹 كارت عائم أنيق", "كارت عائم بحواف منحنية وظل مرتفع يظهر أعلى التطبيق")
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "⚙️ الإعدادات وتخصيص النغمات",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = PrimaryDarkBlue,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Notification Sound Tones Customization Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, PrimaryBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MusicNote, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🎵 اختر نغمة إشعارات الطلبات الجديدة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryDarkBlue
                    )
                }
                Text(
                    text = "اختر الصوت المفضل الذي يصدر فور وصول طلب جديد، ويمكنك الاستماع لكل نغمة قبل الاعتماد.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                )

                toneOptions.forEachIndexed { index, (key, label, desc) ->
                    val isSelected = selectedTone == key
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedTone = key },
                        color = if (isSelected) Color(0xFFEFF5FF) else Color(0xFFF9FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) PrimaryBlue else CardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { selectedTone = key },
                                    colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                                )
                                Column {
                                    Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                                    Text(desc, fontSize = 11.sp, color = TextMuted)
                                }
                            }

                            // Play tone preview button
                            Surface(
                                modifier = Modifier.clickable {
                                    onPlayPreviewTone(key, selectedVibration)
                                },
                                color = PrimaryBlue,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "استماع", tint = Color.White, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text("استماع", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Vibration Pattern Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Vibration, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "📳 نمط اهتزاز الهاتـف (Vibration)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryDarkBlue
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                vibrationOptions.forEach { (key, label) ->
                    val isSelected = selectedVibration == key
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedVibration = key }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedVibration = key },
                            colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                        )
                        Text(label, fontSize = 13.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = TextDark)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notification Display Style Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, PrimaryBlue.copy(alpha = 0.4f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Style, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🎨 أسلوب وشكل تنبيه الطلب بالشاشة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryDarkBlue
                    )
                }
                Text(
                    text = "حدد كيف تود أن يظهر كارت التنبيه داخل التطبيق عند استلام طلب جديد.",
                    fontSize = 12.sp,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                styleOptions.forEach { (key, title, desc) ->
                    val isSelected = selectedStyle == key
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedStyle = key },
                        color = if (isSelected) Color(0xFFEFF5FF) else Color(0xFFF9FAFC),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) PrimaryBlue else CardBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = isSelected,
                                onClick = { selectedStyle = key },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                            )
                            Column {
                                Text(title, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)
                                Text(desc, fontSize = 11.sp, color = TextMuted)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Shipping Settings Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalShipping, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🚚 سعر التوصيل والخدمة",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryDarkBlue
                    )
                }
                Text(
                    text = "اختر كيفية حساب سعر الشحن لطلبات العملاء الجديدة.",
                    fontSize = 12.sp,
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
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Print, contentDescription = null, tint = PrimaryBlue)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🖨 إعدادات طباعة وتصدير الفاتورة",
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
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Notifications, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "🔔 خدمة التنبيهات واختبار الجـرس",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = PrimaryDarkBlue
                    )
                }
                Text(
                    text = "إعادة تشغيل خدمة الخلفية أو تجربة شكل وصوت التنبيه بالشكل المختار.",
                    fontSize = 12.sp,
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
                        Text("تشغيل الخدمة", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = onTriggerTestAlarm,
                        modifier = Modifier.weight(1.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("اختبار التنبيه والنغمة 🔔", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Save Button
        Button(
            onClick = {
                val price = fixedPriceInput.toDoubleOrNull() ?: 0.0
                onSaveSettings(shipMode, price, invoiceSize, selectedTone, selectedVibration, selectedStyle)
            },
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
            Text("💾 حفظ الإعدادات والنغمات", fontWeight = FontWeight.Black, fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(80.dp))
    }
}
