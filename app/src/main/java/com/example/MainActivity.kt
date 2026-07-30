package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.AlarmBanner
import com.example.ui.components.PermissionDialog
import com.example.ui.screens.ArchiveScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.OrdersScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.AlarmRed
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryDarkBlue

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    var hasNotifPermission by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }
    var showPermissionDialog by remember { mutableStateOf(!hasNotifPermission) }

    if (showPermissionDialog && !hasNotifPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        PermissionDialog(
            onDismiss = { showPermissionDialog = false },
            onPermissionGranted = {
                hasNotifPermission = true
                showPermissionDialog = false
            }
        )
    }

    LaunchedEffect(Unit) {
        viewModel.toastEvent.collect { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        }
    }

    val receivedCount = remember(uiState.orders) {
        uiState.orders.count { it.status == "received" }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_app_icon_1785367666124),
                                    contentDescription = "Logo",
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "لوحة استقبال الطلبات 📦",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "البان الدوار - Al Dwaar Dairy",
                                    fontSize = 11.sp,
                                    color = Color(0xFFD0E2FF)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = PrimaryDarkBlue),
                    actions = {
                        if (uiState.isAlarmActive) {
                            Button(
                                onClick = { viewModel.stopAlarm() },
                                colors = ButtonDefaults.buttonColors(containerColor = AlarmRed, contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Icon(Icons.Default.VolumeOff, contentDescription = "كتم الصوت", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("🔕 إيقاف الصوت", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                )

                AnimatedVisibility(visible = uiState.isAlarmActive) {
                    AlarmBanner(
                        message = uiState.alarmMessage,
                        style = uiState.settings.alarmStyle,
                        onStopAlarm = { viewModel.stopAlarm() }
                    )
                }
            }
        },
        floatingActionButton = {
            if (uiState.isAlarmActive) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.stopAlarm() },
                    containerColor = AlarmRed,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(16.dp),
                    icon = { Icon(Icons.Default.VolumeOff, contentDescription = "إيقاف الجرس", modifier = Modifier.size(24.dp)) },
                    text = { Text("🔕 إيقاف صوت الإشعار التنبيهي", fontWeight = FontWeight.Black, fontSize = 14.sp) }
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                contentColor = PrimaryBlue
            ) {
                NavigationBarItem(
                    selected = uiState.activeTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (receivedCount > 0) {
                                    Badge(containerColor = Color.Red, contentColor = Color.White) {
                                        Text("$receivedCount")
                                    }
                                }
                            }
                        ) {
                            Icon(Icons.Default.ListAlt, contentDescription = "الطلبات")
                        }
                    },
                    label = { Text("الطلبات", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        indicatorColor = Color(0xFFE8F4FD)
                    )
                )

                NavigationBarItem(
                    selected = uiState.activeTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    icon = { Icon(Icons.Default.Archive, contentDescription = "الأرشيف") },
                    label = { Text("الأرشيف", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        indicatorColor = Color(0xFFE8F4FD)
                    )
                )

                NavigationBarItem(
                    selected = uiState.activeTab == 2,
                    onClick = { viewModel.selectTab(2) },
                    icon = { Icon(Icons.Default.People, contentDescription = "العملاء") },
                    label = { Text("العملاء", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        indicatorColor = Color(0xFFE8F4FD)
                    )
                )

                NavigationBarItem(
                    selected = uiState.activeTab == 3,
                    onClick = { viewModel.selectTab(3) },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = "الإشعارات") },
                    label = { Text("الإشعارات", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        indicatorColor = Color(0xFFE8F4FD)
                    )
                )

                NavigationBarItem(
                    selected = uiState.activeTab == 4,
                    onClick = { viewModel.selectTab(4) },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "الإعدادات") },
                    label = { Text("الإعدادات", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = PrimaryBlue,
                        indicatorColor = Color(0xFFE8F4FD)
                    )
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (uiState.isAlarmActive) {
                AlarmBanner(
                    message = uiState.alarmMessage,
                    style = uiState.settings.alarmStyle,
                    onStopAlarm = { viewModel.stopAlarm() }
                )
            }

            when (uiState.activeTab) {
                0 -> OrdersScreen(
                    uiState = uiState,
                    onSaveShipping = { id, price -> viewModel.saveShippingPrice(id, price) },
                    onAdvanceStatus = { id, status -> viewModel.advanceOrderStatus(id, status) },
                    onArchive = { id -> viewModel.archiveOrder(id) }
                )
                1 -> ArchiveScreen(
                    uiState = uiState,
                    onAdvanceStatus = { id, status -> viewModel.advanceOrderStatus(id, status) }
                )
                2 -> CustomersScreen(
                    uiState = uiState,
                    onSaveCustomerShipPrice = { id, price -> viewModel.saveCustomerShipPrice(id, price) },
                    onClearCustomerShipPrice = { id -> viewModel.clearCustomerShipPrice(id) }
                )
                3 -> NotificationsScreen(
                    uiState = uiState,
                    onSendNotification = { title, msg, type, target, cust, link, hours ->
                        viewModel.sendNotification(title, msg, type, target, cust, link, hours)
                    },
                    onDeleteNotification = { id -> viewModel.deleteNotification(id) }
                )
                4 -> SettingsScreen(
                    uiState = uiState,
                    onSaveSettings = { mode, price, invSize, tone, vib, style ->
                        viewModel.saveStoreSettings(mode, price, invSize, tone, vib, style)
                    },
                    onPlayPreviewTone = { tone, vib ->
                        viewModel.playPreviewTone(tone, vib)
                    },
                    onTriggerTestAlarm = {
                        viewModel.triggerInAppAlarm("🔔 اختبار جرس التنبيه بالطلبات الجديدة!")
                    }
                )
            }
        }
    }
}
