package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainUiState
import com.example.Order
import com.example.ui.components.OrderCard
import com.example.ui.components.OrderInvoiceDialog
import com.example.ui.theme.CardBorder
import com.example.ui.theme.DairyGold
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryDarkBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted

@Composable
fun OrdersScreen(
    uiState: MainUiState,
    onSaveShipping: (String, Double) -> Unit,
    onAdvanceStatus: (String, String) -> Unit,
    onArchive: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedColumnTab by remember { mutableIntStateOf(0) } // 0: Received, 1: Delivering, 2: Delivered
    var selectedInvoiceOrder by remember { mutableStateOf<Order?>(null) }

    val receivedOrders = remember(uiState.orders) { uiState.orders.filter { it.status == "received" } }
    val deliveringOrders = remember(uiState.orders) { uiState.orders.filter { it.status == "delivering" } }
    val deliveredOrders = remember(uiState.orders) { uiState.orders.filter { it.status == "delivered" } }
    val totalRevenue = remember(uiState.orders) {
        uiState.orders.filter { it.status == "delivered" || it.status == "archived" }.sumOf { it.grandTotal() }
    }

    val currentColumnOrders = when (selectedColumnTab) {
        0 -> receivedOrders
        1 -> deliveringOrders
        2 -> deliveredOrders
        else -> receivedOrders
    }

    if (selectedInvoiceOrder != null) {
        OrderInvoiceDialog(
            order = selectedInvoiceOrder!!,
            invoiceSize = uiState.settings.invoiceSize,
            onDismiss = { selectedInvoiceOrder = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Stats Summary Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatCard(
                title = "إجمالي الطلبات",
                value = "${uiState.orders.size}",
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "بانتظار التوصيل",
                value = "${receivedOrders.size}",
                color = DairyGold,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "جاري التوصيل",
                value = "${deliveringOrders.size}",
                color = PrimaryBlue,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "الإيرادات",
                value = "${totalRevenue.toInt()} ج",
                color = SuccessGreen,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Column Tabs
        val tabs = listOf(
            "تم استلام الطلب (${receivedOrders.size})",
            "جاري التوصيل (${deliveringOrders.size})",
            "تم التوصيل (${deliveredOrders.size})"
        )

        ScrollableTabRow(
            selectedTabIndex = selectedColumnTab,
            containerColor = Color.White,
            contentColor = PrimaryBlue,
            edgePadding = 0.dp,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(tabPositions[selectedColumnTab]),
                    color = PrimaryBlue
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(12.dp))
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedColumnTab == index,
                    onClick = { selectedColumnTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedColumnTab == index) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Orders List or Empty State
        if (uiState.isLoadingOrders) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (currentColumnOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📦 لا توجد طلبات في هذا القسم حالياً",
                        fontWeight = FontWeight.Bold,
                        color = TextMuted,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "أي طلب جديد يطلبه العملاء سيظهر هنا فوراً مع تنبيه صوتي وإشعار خلفية",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(currentColumnOrders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onSaveShipping = { price -> onSaveShipping(order.id, price) },
                        onAdvanceStatus = { newStatus -> onAdvanceStatus(order.id, newStatus) },
                        onArchive = { onArchive(order.id) },
                        onPrintInvoice = { selectedInvoiceOrder = order }
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color = PrimaryDarkBlue,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Black, fontSize = 16.sp, color = color)
            Text(title, fontSize = 10.sp, color = TextMuted, fontWeight = FontWeight.SemiBold)
        }
    }
}
