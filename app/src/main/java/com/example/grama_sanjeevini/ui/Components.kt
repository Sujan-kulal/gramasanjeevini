package com.example.grama_sanjeevini.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.grama_sanjeevini.data.Medicine
import com.example.grama_sanjeevini.data.pharmacies

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicineItem(
    medicine: Medicine,
    isPharmacistMode: Boolean = false,
    distanceText: String? = null,
    onStockChange: (Int) -> Unit = {},
    onDeleteClick: () -> Unit = {},
    onCallClick: (String) -> Unit = {}
) {
    val pharmacy = pharmacies.find { it.id == medicine.pharmacyId }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (medicine.isLifeSaving) Color(0xFFFFEBEE) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicine.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    pharmacy?.let {
                        Text(
                            text = "${it.name} • ${it.village}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Address: ${it.address}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (distanceText != null) {
                            Text(
                                text = "Distance: $distanceText",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (medicine.isLifeSaving) {
                        Badge(
                            containerColor = Color.Red,
                            contentColor = Color.White
                        ) {
                            Text(
                                "LIFE SAVING",
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    if (isPharmacistMode) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Stock: ${medicine.stockLevel}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (medicine.stockLevel < 5) Color.Red else Color.Unspecified,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (isPharmacistMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { if (medicine.stockLevel > 0) onStockChange(medicine.stockLevel - 1) }) {
                            Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(text = "Stock", style = MaterialTheme.typography.labelSmall)
                        IconButton(onClick = { onStockChange(medicine.stockLevel + 1) }) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                } else {
                    Button(onClick = { 
                        pharmacy?.let { onCallClick(it.contact) }
                    }) {
                        Text("Call Pharmacy")
                    }
                }
            }
            
            if (medicine.isNearExpiry()) {
                Surface(
                    color = Color(0xFFFFF3E0),
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(
                        text = "⚠ Near Expiry - Sell at Discount!",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFE65100)
                    )
                }
            }
        }
    }
}
