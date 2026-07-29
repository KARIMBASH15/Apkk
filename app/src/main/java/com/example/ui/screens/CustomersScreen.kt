package com.example.ui.screens

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.window.Dialog
import com.example.Customer
import com.example.MainUiState
import com.example.Order
import com.example.ui.theme.CardBorder
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryDarkBlue
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextDark
import com.example.ui.theme.TextMuted

@Composable
fun CustomersScreen(
    uiState: MainUiState,
    onSaveCustomerShipPrice: (String, Double) -> Unit,
    onClearCustomerShipPrice: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCustomer by remember { mutableStateOf<Customer?>(null) }

    val filteredCustomers = remember(uiState.customers, searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) uiState.customers
        else uiState.customers.filter { it.name.contains(q, ignoreCase = true) || it.phone.contains(q) }
    }

    if (selectedCustomer != null) {
        CustomerDetailDialog(
            customer = selectedCustomer!!,
            orders = uiState.orders.filter { it.customerPhone == selectedCustomer!!.phone },
            onSaveShipPrice = { price -> onSaveCustomerShipPrice(selectedCustomer!!.id, price) },
            onClearShipPrice = { onClearCustomerShipPrice(selectedCustomer!!.id) },
            onDismiss = { selectedCustomer = null }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "👥 العملاء المسجلين (${uiState.customers.size})",
            fontSize = 18.sp,
            fontWeight = FontWeight.Black,
            color = PrimaryDarkBlue,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Search Input
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ابحث بالاسم أو رقم الهاتف...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = PrimaryBlue) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PrimaryBlue,
                unfocusedBorderColor = CardBorder,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            )
        )

        if (uiState.isLoadingCustomers) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = PrimaryBlue)
            }
        } else if (filteredCustomers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "لا يوجد عملاء مطابقون للبحث",
                    fontWeight = FontWeight.Bold,
                    color = TextMuted,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredCustomers, key = { it.id }) { customer ->
                    CustomerCard(
                        customer = customer,
                        onClick = { selectedCustomer = customer }
                    )
                }
            }
        }
    }
}

@Composable
fun CustomerCard(
    customer: Customer,
    onClick: () -> Unit
) {
    val hasFixed = customer.fixedShippingPrice != null

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(customer.name, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = PrimaryDarkBlue)
                Box(
                    modifier = Modifier
                        .background(
                            if (hasFixed) SuccessGreen.copy(alpha = 0.15f) else Color(0xFFF0F4FA),
                            RoundedCornerShape(6.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (hasFixed) "🚚 شحن ثابت: ${customer.fixedShippingPrice} ج" else "🚚 السعر عند الاستلام",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (hasFixed) SuccessGreen else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Phone, contentDescription = null, tint = TextMuted, modifier = Modifier.padding(end = 4.dp))
                Text(customer.phone, fontSize = 12.sp, color = TextMuted)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = TextMuted, modifier = Modifier.padding(end = 4.dp))
                Text(customer.address, fontSize = 12.sp, color = TextMuted, maxLines = 1)
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = CardBorder)

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("عدد الطلبات: ${customer.ordersCount}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = PrimaryBlue)
                Text("إجمالي المشتريات: ${customer.totalSpent} جنيه", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryDarkBlue)
            }
        }
    }
}

@Composable
fun CustomerDetailDialog(
    customer: Customer,
    orders: List<Order>,
    onSaveShipPrice: (Double) -> Unit,
    onClearShipPrice: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var shipInput by remember(customer.fixedShippingPrice) {
        mutableStateOf(customer.fixedShippingPrice?.toString() ?: "")
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(customer.name, fontWeight = FontWeight.Black, fontSize = 17.sp, color = PrimaryDarkBlue)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                Text("📞 ${customer.phone} | 📍 ${customer.address}", fontSize = 12.sp, color = TextMuted)
                Text("🛒 ${customer.ordersCount} طلبات | 💰 إجمالي الإنفاق: ${customer.totalSpent} جنيه", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Fixed Shipping Price Setting Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF5F9FF), RoundedCornerShape(12.dp))
                        .border(1.dp, CardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp)
                ) {
                    Column {
                        Text("🚚 سعر شحن ثابت لهذا العميل", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryDarkBlue)
                        Text("سيتم تطبيق هذا السعر تلقائياً على كل طلبات هذا العميل بدلاً من التحديد اليدوي.", fontSize = 11.sp, color = TextMuted)

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = shipInput,
                                onValueChange = { shipInput = it },
                                placeholder = { Text("أدخل السعر (جنيه)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                shape = RoundedCornerShape(8.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White
                                )
                            )
                            Button(
                                onClick = {
                                    val price = shipInput.toDoubleOrNull() ?: 0.0
                                    onSaveShipPrice(price)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("حفظ") }

                            OutlinedButton(
                                onClick = {
                                    shipInput = ""
                                    onClearShipPrice()
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("إلغاء") }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Print Statement Button
                Button(
                    onClick = {
                        shareCustomerStatementText(context, customer, orders)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text("🖨 مشاركة/طباعة كشف حساب العميل (A4)")
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("سجل الطلبات السابق:", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextDark)

                if (orders.isEmpty()) {
                    Text("لا يوجد سجل طلبات لهذا العميل", fontSize = 12.sp, color = TextMuted, modifier = Modifier.padding(vertical = 12.dp))
                } else {
                    orders.forEach { o ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FBFF)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("طلب #${o.orderId}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("${o.grandTotal()} جنيه", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 12.sp)
                                }
                                Text(
                                    text = o.items.joinToString { "${it.name} ×${it.qty}" },
                                    fontSize = 11.sp,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun shareCustomerStatementText(context: Context, customer: Customer, orders: List<Order>) {
    val ordersTxt = orders.joinToString("\n-----------------------\n") { o ->
        "رقم الطلب: ${o.orderId}\nالمنتجات: ${o.items.joinToString { "${it.name} ×${it.qty}" }}\nالمبلغ: ${o.grandTotal()} جنيه"
    }

    val statementTxt = """
        📑 *كشف حساب العميل - البان الدوار (A4)*
        ===================================
        اسم العميل: ${customer.name}
        الهاتف: ${customer.phone}
        العنوان: ${customer.address}
        عدد الطلبات الإجمالي: ${customer.ordersCount}
        إجمالي الإنفاق: ${customer.totalSpent} جنيه
        ===================================
        *سجل المعاملات:*
        $ordersTxt
        ===================================
        البان الدوار - Al Dwaar Dairy
    """.trimIndent()

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, statementTxt)
        type = "text/plain"
    }
    context.startActivity(Intent.createChooser(sendIntent, "مشاركة كشف الحساب عبر"))
}
