package com.example.grama_sanjeevini.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

object FirebaseRepository {
    private val db = FirebaseFirestore.getInstance()
    private val pharmacyCollection = db.collection("pharmacies")
    private val medicineCollection = db.collection("medicines")

    fun startSync() {
        Log.d("GramaSync", "Starting real-time sync...")

        // Sync Pharmacies
        pharmacyCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("GramaSync", "Pharmacy sync error: ${error.message}")
                return@addSnapshotListener
            }
            snapshot?.let {
                pharmacies.clear()
                for (doc in it.documents) {
                    val pharmacy = Pharmacy(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        village = doc.getString("village") ?: "",
                        address = doc.getString("address") ?: "",
                        contact = doc.getString("contact") ?: "",
                        pin = doc.getString("pin") ?: "1234",
                        latitude = doc.getDouble("latitude"),
                        longitude = doc.getDouble("longitude")
                    )
                    pharmacies.add(pharmacy)
                }
                Log.d("GramaSync", "Synced ${pharmacies.size} pharmacies")
            }
        }

        // Sync Medicines
        medicineCollection.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("GramaSync", "Medicine sync error: ${error.message}")
                return@addSnapshotListener
            }
            snapshot?.let {
                medicines.clear()
                for (doc in it.documents) {
                    val medicine = Medicine(
                        id = doc.id,
                        name = doc.getString("name") ?: "",
                        pharmacyId = doc.getString("pharmacyId") ?: "",
                        stockLevel = doc.getLong("stockLevel")?.toInt() ?: 0,
                        isLifeSaving = doc.getBoolean("isLifeSaving") ?: false,
                        expiryDate = doc.getDate("expiryDate") ?: Date(),
                        price = doc.getDouble("price") ?: 0.0
                    )
                    medicines.add(medicine)
                }
                Log.d("GramaSync", "Synced ${medicines.size} medicines")
            }
        }
    }

    fun addPharmacy(pharmacy: Pharmacy, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val data = hashMapOf(
            "name" to pharmacy.name,
            "village" to pharmacy.village,
            "address" to pharmacy.address,
            "contact" to pharmacy.contact,
            "pin" to pharmacy.pin,
            "latitude" to pharmacy.latitude,
            "longitude" to pharmacy.longitude
        )
        pharmacyCollection.document(pharmacy.id).set(data)
            .addOnSuccessListener { onComplete(true, "Shop Registered Successfully!") }
            .addOnFailureListener { e -> onComplete(false, "Error: ${e.message}") }
    }

    fun addMedicine(medicine: Medicine, onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val data = hashMapOf(
            "name" to medicine.name,
            "pharmacyId" to medicine.pharmacyId,
            "stockLevel" to medicine.stockLevel,
            "isLifeSaving" to medicine.isLifeSaving,
            "expiryDate" to medicine.expiryDate,
            "price" to medicine.price
        )
        medicineCollection.document(medicine.id).set(data)
            .addOnSuccessListener { onComplete(true, "Medicine Added to Cloud!") }
            .addOnFailureListener { e -> onComplete(false, "Error: ${e.message}") }
    }

    fun updateStock(medicineId: String, newStock: Int) {
        medicineCollection.document(medicineId).update("stockLevel", newStock)
            .addOnFailureListener { e -> Log.e("GramaSync", "Stock update failed", e) }
    }

    fun deleteMedicine(medicineId: String) {
        medicineCollection.document(medicineId).delete()
            .addOnSuccessListener { Log.d("GramaSync", "Medicine deleted: $medicineId") }
            .addOnFailureListener { e -> Log.e("GramaSync", "Error deleting medicine", e) }
    }
}
