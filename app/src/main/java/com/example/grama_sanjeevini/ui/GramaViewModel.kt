package com.example.grama_sanjeevini.ui

import android.location.Location
import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import com.example.grama_sanjeevini.data.*
import kotlin.math.*

class GramaViewModel : ViewModel() {
    // UI State
    var searchQuery by mutableStateOf("")
    var selectedTab by mutableIntStateOf(0)
    
    var isOwnerMode by mutableStateOf(false)
    var currentPharmacyId by mutableStateOf<String?>(null)
    
    var userLocation by mutableStateOf<Location?>(null)
    var hasLocationPermission by mutableStateOf(false)

    // Dialog states
    var showLoginDialog by mutableStateOf(false)
    var showRegisterDialog by mutableStateOf(false)
    var showAddMedDialog by mutableStateOf(false)

    val filteredMedicines: List<Medicine>
        get() {
            return medicines.filter {
                val matchesSearch = it.name.contains(searchQuery, ignoreCase = true)
                val matchesTab = if (selectedTab == 1) it.isLifeSaving else true
                val matchesOwner = if (isOwnerMode) it.pharmacyId == currentPharmacyId else true
                matchesSearch && matchesTab && matchesOwner
            }.sortedBy { med ->
                val pharmacy = pharmacies.find { it.id == med.pharmacyId }
                val loc = userLocation
                if (loc != null && pharmacy?.latitude != null && pharmacy.longitude != null) {
                    calculateDistance(
                        loc.latitude, loc.longitude,
                        pharmacy.latitude, pharmacy.longitude
                    )
                } else {
                    Double.MAX_VALUE
                }
            }
        }

    fun getDistanceString(pharmacyId: String): String? {
        val pharmacy = pharmacies.find { it.id == pharmacyId }
        val loc = userLocation
        if (loc != null && pharmacy?.latitude != null && pharmacy.longitude != null) {
            val dist = calculateDistance(
                loc.latitude, loc.longitude,
                pharmacy.latitude, pharmacy.longitude
            )
            return "%.1f km away".format(dist)
        }
        return null
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = (lat2 - lat1) * PI / 180.0
        val dLon = (lon2 - lon1) * PI / 180.0
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1 * PI / 180.0) * cos(lat2 * PI / 180.0) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun logout() {
        isOwnerMode = false
        currentPharmacyId = null
    }

    fun login(pharmacyId: String) {
        isOwnerMode = true
        currentPharmacyId = pharmacyId
        showLoginDialog = false
    }
}
