package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.PrimaryDarkBlue

@Composable
fun AlarmBanner(
    message: String,
    onStopAlarm: () -> Unit,
    style: String = "banner_full",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "alarmBlink")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    when (style) {
        "dialog_modal" -> {
            AlertDialog(
                onDismissRequest = {},
                icon = {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(AlarmRed.copy(alpha = alpha), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                },
                title = {
                    Text(
                        text = "🔔 تنبيه طلب جديد وارد!",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = PrimaryDarkBlue,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Text(
                        text = message.ifEmpty { "وصل طلب جديد الآن للمتجر. يُرجى مراجعة تفاصيل الطلب وتحديد سعر التوصيل." },
                        fontSize = 14.sp,
                        color = PrimaryDarkBlue,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    Button(
                        onClick = onStopAlarm,
                        colors = ButtonDefaults.buttonColors(containerColor = AlarmRed),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                        Text("إيقاف الصوت والتنبيه 🔕", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }
                },
                shape = RoundedCornerShape(20.dp),
                containerColor = Color.White
            )
        }
        "banner_compact" -> {
            Card(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .border(2.dp, AlarmRed, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = AlarmRed.copy(alpha = alpha),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                            }
                        }

                        Column {
                            Text("طلب جديد 🔔", fontWeight = FontWeight.Black, fontSize = 14.sp, color = AlarmRed)
                            Text(
                                text = message.ifEmpty { "تأكد من تحديد سعر الشحن للطلب" },
                                fontSize = 12.sp,
                                color = PrimaryDarkBlue,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Button(
                        onClick = onStopAlarm,
                        colors = ButtonDefaults.buttonColors(containerColor = AlarmRed),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.padding(2.dp))
                        Text("إيقاف", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        }
        else -> { // "banner_full"
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .background(AlarmRed.copy(alpha = alpha))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Text(
                        text = message.ifEmpty { "🔔 طلب جديد وارد الآن!" },
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }

                Button(
                    onClick = onStopAlarm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = AlarmRed
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Stop,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text("إيقاف الجرس", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}
