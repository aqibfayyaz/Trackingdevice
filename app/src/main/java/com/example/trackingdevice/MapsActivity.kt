package com.example.trackingdevice

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.*
import java.lang.Exception

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    var databaseRef : DatabaseReference?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        val bundle:Bundle=intent.extras!!
        val phoneNumber = bundle.getString("phoneNumber")
        databaseRef = FirebaseDatabase.getInstance().reference

        databaseRef!!.child("Users").child(phoneNumber!!).child(
            "location").addValueEventListener(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(data: DataSnapshot) {
             try{
                 val td = data!!.value as HashMap<String,Any>
                 val lat = td["lat"]!!.toString()
                 val log = td["log"]!!.toString()
                 MapsActivity.sydney = LatLng(lat!!.toDouble(), log!!.toDouble())
                 MapsActivity.lastOnline = td["lastOnline"]!!.toString()
                 loadMap()
             }catch(e:Exception){
                 Toast.makeText(applicationContext,"Error in loading Map",Toast.LENGTH_LONG).show()
                 Log.d(e.message.toString(),"Maperror")
             }
            }

        })

    }

    fun loadMap(){
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    companion object{
        var sydney = LatLng(-34.0, 151.0)
        var lastOnline = "not Defined"
    }
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Add a marker in Sydney and move the camera
        mMap.addMarker(MarkerOptions().position(MapsActivity.sydney!!).title(MapsActivity.lastOnline!!))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(MapsActivity.sydney!!))
    }
}