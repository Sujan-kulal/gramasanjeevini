package com.example.grama_sanjeevini

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.grama_sanjeevini.data.*
import com.example.grama_sanjeevini.ui.GramaViewModel
import com.example.grama_sanjeevini.ui.MedicineItem
import com.example.grama_sanjeevini.ui.theme.GramaSanjeeviniTheme
import com.google.android.gms.location.LocationServices
import java.util.Date

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Initialize Firebase Real-time Sync
        FirebaseRepository.startSync()
        
        enableEdgeToEdge()
        setContent {
            GramaSanjeeviniTheme {
                HomeScreen()
            }
        }
    }
}

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(viewModel: GramaViewModel = viewModel()) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.hasLocationPermission = isGranted
        if (isGranted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    viewModel.userLocation = location
                }
            } catch (e: SecurityException) { }
        }
    }

    LaunchedEffect(viewModel.hasLocationPermission) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.hasLocationPermission = true
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    viewModel.userLocation = location
                }
            } catch (e: SecurityException) { }
        } else {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Grama-Sanjeevini", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                        Text("Rural Pharmacy Network", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    if (!viewModel.isOwnerMode) {
                        TextButton(onClick = { viewModel.showLoginDialog = true }) {
                            Text("Login", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.showRegisterDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text("Register Store", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        TextButton(onClick = { viewModel.logout() }) {
                            Text("Logout", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            if (viewModel.isOwnerMode && viewModel.currentPharmacyId != null) {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.showAddMedDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Add Medicine") }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            if (viewModel.isOwnerMode) {
                val myShop = pharmacies.find { it.id == viewModel.currentPharmacyId }
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "OWNER MODE: Managing ${myShop?.name ?: "Store"}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
            } else if (!viewModel.hasLocationPermission) {
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    Row(modifier = Modifier.padding(8.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text("Location access needed for nearest shops.", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall)
                        Button(onClick = { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                            Text("Allow")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search Medicine (e.g. Paracetamol)") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TabRow(selectedTabIndex = viewModel.selectedTab, containerColor = Color.Transparent, divider = {}) {
                val tabTitles = if (viewModel.isOwnerMode) listOf("My Medicines", "Emergency Items") else listOf("Find Nearest", "Emergency Only")
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = viewModel.selectedTab == index,
                        onClick = { viewModel.selectedTab = index },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (index == 1) Icon(Icons.Default.Warning, null, tint = if (viewModel.selectedTab == 1) Color.Red else Color.Gray, modifier = Modifier.size(18.dp).padding(end = 4.dp))
                                Text(title, fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (viewModel.filteredMedicines.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(if (viewModel.isOwnerMode) "You haven't added any medicines yet." else "No results found nearby.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 120.dp)
                ) {
                    items(viewModel.filteredMedicines, key = { it.id }) { medicine ->
                        MedicineItem(
                            medicine = medicine,
                            isPharmacistMode = viewModel.isOwnerMode && medicine.pharmacyId == viewModel.currentPharmacyId,
                            distanceText = if (viewModel.isOwnerMode) null else viewModel.getDistanceString(medicine.pharmacyId),
                            onStockChange = { newStock ->
                                FirebaseRepository.updateStock(medicine.id, newStock)
                            },
                            onDeleteClick = {
                                FirebaseRepository.deleteMedicine(medicine.id)
                            },
                            onCallClick = { phoneNumber: String ->
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber"))
                                context.startActivity(intent)
                            }
                        )
                    }
                }
            }
        }

        // --- DIALOGS ---

        if (viewModel.showLoginDialog) {
            OwnerLoginDialog(onDismiss = { viewModel.showLoginDialog = false }) { id ->
                viewModel.login(id)
            }
        }

        if (viewModel.showRegisterDialog) {
            RegisterShopDialog(
                currentLocation = viewModel.userLocation,
                onDismiss = { viewModel.showRegisterDialog = false },
                onConfirm = { newShop ->
                    FirebaseRepository.addPharmacy(newShop)
                    viewModel.login(newShop.id)
                    viewModel.showRegisterDialog = false
                }
            )
        }

        if (viewModel.showAddMedDialog) {
            AddMedicineDialog(currentShopId = viewModel.currentPharmacyId ?: "", onDismiss = { viewModel.showAddMedDialog = false }) { med ->
                FirebaseRepository.addMedicine(med)
                viewModel.showAddMedDialog = false
            }
        }
    }
}

@Composable
fun OwnerLoginDialog(onDismiss: () -> Unit, onSuccess: (String) -> Unit) {
    var pin by remember { mutableStateOf("") }
    var selectedShopId by remember { mutableStateOf(pharmacies.firstOrNull()?.id ?: "") }
    var error by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Owner Login") },
        text = {
            Column {
                Text("Select pharmacy and enter PIN:")
                pharmacies.take(10).forEach { shop ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = selectedShopId == shop.id, onClick = { selectedShopId = shop.id })
                        Text(shop.name)
                    }
                }
                OutlinedTextField(
                    value = pin,
                    onValueChange = { pin = it; error = false },
                    label = { Text("PIN") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                if (error) Text("Incorrect PIN", color = Color.Red, style = MaterialTheme.typography.labelSmall)
            }
        },
        confirmButton = { 
            Button(onClick = { 
                val shop = pharmacies.find { it.id == selectedShopId }
                if (shop != null && pin == shop.pin) onSuccess(selectedShopId) else error = true 
            }) { Text("Login") } 
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun RegisterShopDialog(currentLocation: Location?, onDismiss: () -> Unit, onConfirm: (Pharmacy) -> Unit) {
    var name by remember { mutableStateOf("") }
    var village by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var lat by remember { mutableStateOf(currentLocation?.latitude?.toString() ?: "") }
    var lon by remember { mutableStateOf(currentLocation?.longitude?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register Your Shop") },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text("Enter details and GPS location to list your shop.")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Shop Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = village, onValueChange = { village = it }, label = { Text("Village") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = address, onValueChange = { address = it }, label = { Text("Detailed Landmark") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = pin, 
                    onValueChange = { pin = it }, 
                    label = { Text("Create Login PIN") }, 
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(Modifier.height(12.dp))
                Text("Auto-Detect GPS Location:", style = MaterialTheme.typography.labelMedium)
                Row {
                    OutlinedTextField(value = lat, onValueChange = { lat = it }, label = { Text("Lat") }, modifier = Modifier.weight(1f))
                    Spacer(Modifier.width(4.dp))
                    OutlinedTextField(value = lon, onValueChange = { lon = it }, label = { Text("Long") }, modifier = Modifier.weight(1f))
                }
                if (currentLocation != null) Text("Current GPS detected!", color = Color.Green, style = MaterialTheme.typography.labelSmall)
                else Text("GPS not found. Enter Lat/Long manually.", color = Color.Red, style = MaterialTheme.typography.labelSmall)
            }
        },
        confirmButton = { 
            Button(onClick = { 
                if (name.isNotBlank() && pin.isNotBlank()) {
                    onConfirm(Pharmacy(
                        name = name, 
                        village = village, 
                        address = address, 
                        contact = contact, 
                        pin = pin,
                        latitude = lat.toDoubleOrNull(), 
                        longitude = lon.toDoubleOrNull()
                    ))
                }
            }) { Text("Register Store") } 
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
fun AddMedicineDialog(currentShopId: String, onDismiss: () -> Unit, onConfirm: (Medicine) -> Unit) {
    var name by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var isEmergency by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Medicine") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Medicine Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = stock, onValueChange = { stock = it }, label = { Text("Initial Stock") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Price (₹)") }, modifier = Modifier.fillMaxWidth())
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isEmergency, onCheckedChange = { isEmergency = it })
                    Text("Emergency Drug")
                }
            }
        },
        confirmButton = { 
            Button(onClick = { 
                if (name.isNotBlank()) {
                    onConfirm(Medicine(
                        name = name, 
                        pharmacyId = currentShopId, 
                        stockLevel = stock.toIntOrNull() ?: 0, 
                        price = price.toDoubleOrNull() ?: 0.0, 
                        isLifeSaving = isEmergency, 
                        expiryDate = Date(System.currentTimeMillis() + 10000000000L)
                    )) 
                }
            }) { Text("Add Item") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
