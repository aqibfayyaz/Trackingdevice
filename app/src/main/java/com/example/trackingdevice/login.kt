package com.example.trackingdevice


import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern


class login : AppCompatActivity(){

    var firebaseDatabase: DatabaseReference?=null
    var mAuth:FirebaseAuth?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        mAuth= FirebaseAuth.getInstance()
    }



    fun buRegisterEvent(view:View){
        FirebaseApp.initializeApp(this)
        firebaseDatabase = FirebaseDatabase.getInstance().reference

        // checking phoneNumber for validity
        val userdata=UserData(this)
            var checknumber = etphonenumber.text.toString()
        if(checknumber == null || checknumber.isEmpty() || checknumber.length!=11){
            Toast.makeText(this,"Please Enter  a Valid phone Number",Toast.LENGTH_LONG).show()
            return
        }

        // saving phoneNumber to sharedReference
        userdata.savePhone(etphonenumber.text.toString())

        // saving data to firebasedatabase
        val df = SimpleDateFormat("yyyy/MM/dd HH:MM:ss")
        val date = Date()
        firebaseDatabase!!.child("Users").child(etphonenumber.text.toString()).child("request").setValue(df.format(date).toString())
        firebaseDatabase!!.child("Users").child(etphonenumber.text.toString()).child("Finders").setValue(df.format(date).toString())
        finish()
    }
}