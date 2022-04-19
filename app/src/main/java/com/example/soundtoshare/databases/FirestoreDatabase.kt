package com.example.soundtoshare.databases

import android.location.Location
import android.util.Log
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.getField
import com.google.firebase.ktx.Firebase

class FirestoreDatabase {
    private val database = Firebase.firestore
    var users = mutableListOf<User>()

    fun updateUserLocation(latitude: Double, longitude: Double, VKAccount: String) {
        val user = hashMapOf(
            "VKAccount" to VKAccount,
            "geoPoint" to GeoPoint(latitude,longitude),
            "geoHash" to GeoFireUtils.getGeoHashForLocation(GeoLocation(latitude, longitude))
        )

        database.collection("Users").document(VKAccount)
            .set(user)
            .addOnSuccessListener {
                Log.d("Firestore", "DocumentSnapshot added  ")
            }
            .addOnFailureListener { e ->
                Log.w("Firestore", "Error adding document", e)
            }

    }
    fun getClosest(latitude: Double, longitude: Double, map: GoogleMap) {
        users.clear()

        val center = GeoLocation(latitude, longitude)
        val results = FloatArray(1)
        Location.distanceBetween(
            latitude, longitude,
            map.projection.visibleRegion.latLngBounds.northeast.latitude, map.projection.visibleRegion.latLngBounds.northeast.longitude, results
        )
        // Радиус поиска в метрах
        val radiusInM = results[0].toDouble()
//        map.projection.visibleRegion.latLngBounds.
        Log.d("Scale params", results[0].toString())

        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM)
        val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
        for (b in bounds) {
            val q = database.collection("Users")
                    .orderBy("geoHash")
                    .startAt(b.startHash)
                    .endAt(b.endHash)
            tasks.add(q.get())
        }

        // Collect all the query results together into a single list
        Tasks.whenAllComplete(tasks)
                .addOnCompleteListener {
                    val matchingDocs: MutableList<DocumentSnapshot> = ArrayList()
                    for (task in tasks) {
                        val snap = task.result
                        for (doc in snap!!.documents) {
                            val geoPoint = doc.getGeoPoint("geoPoint")!!
                            val lat = geoPoint.latitude
                            val lng = geoPoint.longitude

                            // We have to filter out a few false positives due to GeoHash
                            // accuracy, but most will match
                            val docLocation = GeoLocation(lat, lng)
                            val distanceInM = GeoFireUtils.getDistanceBetween(docLocation, center)
                            if (distanceInM <= radiusInM) {
                                matchingDocs.add(doc)
                            }
                        }
                    }

                    // matchingDocs contains the results

                    matchingDocs.forEach {
                        users.add(
                            User(
                                it.getField<GeoPoint>("geoPoint")!!,
                                it.getField<String>("VKAccount").toString()
                            )
                        )
                        Log.d(
                            "Firestore",
                            it.getField<GeoPoint>("geoPoint")!!
                                .toString() + it.getField<String>("VKAccount").toString()
                        )
                    }

                    val myVkAccount= "kek"
                    users.forEach() { user ->
                        if (user.VKAccount != myVkAccount) {
                            val userIndicator = MarkerOptions()
                                .position(LatLng(user.geoPoint.latitude, user.geoPoint.longitude))
                                .title(user.VKAccount)
                                .snippet("lat:" + user.geoPoint.latitude + ", lng:" + user.geoPoint.longitude)
                            map.addMarker(userIndicator)
                            Log.d("Placed user", user.VKAccount)
                        }

                    }
                }
    }
}
data class User( val geoPoint: GeoPoint, val VKAccount: String)
