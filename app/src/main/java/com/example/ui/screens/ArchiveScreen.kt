package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainUiState
import com.example.Order
import com.example.ui.components.OrderCard
import com.example.ui.components.OrderInvoiceDialog
import com.example.ui.theme.PrimaryDarkBlue
import com.example.ui.theme.TextMuted

@Composable
fun ArchiveScreen(
    uiState: MainUiState,
    onAdvanceStatus: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedInvoiceOrder by remember { mutableStateOf<Order?>(null) }
    val archivedOrders = remember(uiState.orders) { uiState.orders.filter { it.status == "archived" } }

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
        Text(
            text = "📥 أرشيف الطلبات المكتملة (${archivedOrders.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = PrimaryDarkBlue,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (archivedOrders.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "لا توجد طلبات مؤرشفة بعد",
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(archivedOrders, key = { it.id }) { order ->
                    OrderCard(
                        order = order,
                        onSaveShipping = { _ -> },
                        onAdvanceStatus = { newStatus -> onAdvanceStatus(order.id, newStatus) },
                        onArchive = {},
                        onPrintInvoice = { selectedInvoiceOrder = order }
                    )
                }
            }
        }
    }
}
