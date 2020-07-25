package com.example.trackingdevice

import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class MyServices:Service(){
    var databaseRef : DatabaseReference?=null

    override fun onBind(intent: Intent?): IBinder? {
        return null!!
    }

    override fun onCreate() {
        super.onCreate()
        databaseRef = FirebaseDatabase.getInstance().reference
        IsServiceRunning=true

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {



        val locManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        var loc =locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        if(loc == null){
            loc = locManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
        }
        if(loc == null){
            loc = locManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        }

        if(loc != null){
            val lat = loc.latitude
            val lng = loc.longitude
            Toast.makeText(this,"Got it", Toast.LENGTH_LONG).show()

            MainActivity.myLocation=loc
            Log.d(MainActivity.myLocation!!.latitude.toString(),"latitude")
            Log.d(MainActivity.myLocation!!.longitude.toString(),"longitude")

        }
        else{
            Toast.makeText(this,"could not get location", Toast.LENGTH_LONG).show()
        }



        var userData = UserData(this)
        var phoneNumber = userData.loadPhoneNumber()
        databaseRef!!.child("Users").child(phoneNumber).child("request").addValueEventListener(
            object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    try {
                        if(MainActivity.myLocation==null) {
                            Toast.makeText(applicationContext,MainActivity.myLocation.toString(),
                                Toast.LENGTH_LONG).show()
                            return
                        }
                        Toast.makeText(applicationContext,MainActivity.myLocation.toString(), Toast.LENGTH_LONG).show()
                        val df = SimpleDateFormat("yyyy/MM/dd HH:MM:ss")
                        val date = Date()
                        databaseRef!!.child("Users").child(phoneNumber).child(
                            "location").child("lat").setValue(MainActivity.myLocation!!.latitude)
                        databaseRef!!.child("Users").child(phoneNumber).child(
                            "location").child("log").setValue(MainActivity.myLocation!!.longitude)
                        databaseRef!!.child("Users").child(phoneNumber).child(
                            "location").child("lastOnline").setValue(df.format(date).toString())


                    }
                    catch (e:Exception){
                        Toast.makeText(applicationContext,"location error", Toast.LENGTH_LONG).show()
                    }
                }
            })
        return Service.START_NOT_STICKY
    }


    companion object{
        var IsServiceRunning=false
    }



}