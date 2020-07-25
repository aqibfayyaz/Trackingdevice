package com.example.trackingdevice

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_my_trackers.*
import kotlinx.android.synthetic.main.contact_ticket.view.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    var listOfContact = ArrayList<UserContact>()
    var adapter: ContactAdapter?=null
    var databaseRef : DatabaseReference?=null
    var userdata:UserData?=null
    private val MY_PERMISSIONS_REQUEST_CODE = 123


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
         this.userdata = UserData(applicationContext)
        userdata!!.loadPhoneNumber()



         databaseRef = FirebaseDatabase.getInstance().reference


        adapter = ContactAdapter(this, listOfContact)
        lvContactlist.adapter = adapter
        lvContactlist.onItemClickListener = AdapterView.OnItemClickListener{
                parent,view,position,id ->
            val userInfo = listOfContact[position]

            val df = SimpleDateFormat("yyyy/MM/dd HH:MM:ss")
            val date = Date()
            databaseRef!!.child("Users").child(userInfo.phoneNumber!!).child("request").setValue(df.format(date).toString())

                var intent = Intent(this,MapsActivity::class.java)
                intent.putExtra("phoneNumber",userInfo.phoneNumber!!)
                startActivity(intent)
        }


    }

    var IsAccessLocation=false
   override fun onResume() {
        super.onResume()
         val userData= UserData(this)
       if (userData.getPhoneNumber()=="empty"){

           return
       }
       refreshUser()


       if(IsAccessLocation){
           true
       }
    else{
           try{

               // now code starts here after successfully experminetally with multiple run time permissions

               checkAndroidVersion()




           }catch (e:Exception){
               Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()
               Log.d(e.message,"mainerror4")
           }
       }
    }


    fun checkAndroidVersion() {
        if (Build.VERSION.SDK_INT>=23 ) {
            multiplecheckPermission()
            return
        }
         loadContact()
        getUserLocation()


    }


    protected fun multiplecheckPermission() {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    + ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_CONTACTS
            ))
            != PackageManager.PERMISSION_GRANTED
        ) {

            // Do something, when permissions not granted
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.ACCESS_FINE_LOCATION
                )

                || ActivityCompat.shouldShowRequestPermissionRationale(
                    this, Manifest.permission.READ_CONTACTS
                )
            ) {
                // If we should give explanation of requested permissions

                // Show an alert dialog here with request explanation
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setMessage(
                    "Location and Read Contacts" +
                            "  permissions are required to do the task."
                )
                builder.setTitle("Please grant those permissions")
                builder.setPositiveButton("OK",
                    DialogInterface.OnClickListener { dialogInterface, i ->
                        ActivityCompat.requestPermissions(this, arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.READ_CONTACTS
                        ),
                            MY_PERMISSIONS_REQUEST_CODE
                        )
                    })
                builder.setNeutralButton("Cancel", null)
                val dialog: AlertDialog = builder.create()
                dialog.show()
            } else {
                // Directly request for required permissions, without explanation
                ActivityCompat.requestPermissions(
                    this, arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.READ_CONTACTS
                    ),
                    MY_PERMISSIONS_REQUEST_CODE
                )
            }
        } else {
            loadContact()
            getUserLocation()
            Toast.makeText(this, "Permissions already granted", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>, grantResults: IntArray
    ) {
        when (requestCode) {

            MY_PERMISSIONS_REQUEST_CODE -> {

                // When request is cancelled, the results array are empty
                if (grantResults.size > 0 &&
                    ((grantResults[0]
                            + grantResults[1]
                            )
                            == PackageManager.PERMISSION_GRANTED)
                ) {
                    loadContact()
                    getUserLocation()
                    Toast.makeText(this, "Permissions granted.", Toast.LENGTH_SHORT).show()
                } else {
                    // Permissions are denied
                    Toast.makeText(this, "Permissions denied.", Toast.LENGTH_SHORT).show()
                }
                return
            }

        }
    }



    class thread():Thread(){

        override fun run() {
            super.run()
         val obj:MainActivity = MainActivity()
           // obj.checkLocationPermission()
        }
    }


    class thread2():Thread(){

        override fun run() {
            super.run()
            val obj:MainActivity = MainActivity()
           // obj.checkPermission()
        }
    }


    fun refreshUser(){
        //Toast.makeText(this, "refresh User is working", Toast.LENGTH_LONG).show()

        databaseRef!!.child("Users").child(userdata!!.loadPhoneNumber()).child("Finders").addValueEventListener(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onDataChange(snapshot: DataSnapshot) {

                try {

                    val td = snapshot.value as? HashMap<String,Any>

                    //  Log.d(td.toString(),"mytd")

                    listOfContact.clear()
                    if(td == null){
                        listOfContact.add(UserContact("No_user","nothing"))
                        adapter!!.notifyDataSetChanged()
                        return
                    }

                    for(key in td.keys){
                        val name = listOfContacts[key]
                        listOfContact.add(UserContact(name!!,key))

                    }
                    adapter!!.notifyDataSetChanged()
                    //Toast.makeText(applicationContext," No error this time",Toast.LENGTH_LONG).show()
                }catch (e:Exception){
                    Log.d(e.message.toString(),"mykey")
                    Toast.makeText(applicationContext, e.message,Toast.LENGTH_LONG).show()
                    //Toast.makeText(applicationContext,"Error found",Toast.LENGTH_LONG).show()
                    listOfContact.add(UserContact("No_user","nothing"))
                    adapter!!.notifyDataSetChanged()
                }

            }

        })


    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item!!.itemId){

            R.id.addTracker ->{
                val intent = Intent(this,Mytracker::class.java)
                startActivity(intent)
            }
            R.id.help ->{
                //TODO:: ask fro help from friend
            }
            else ->{
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }


    class ContactAdapter: BaseAdapter {

        var listOfContact = ArrayList<UserContact>()
        var context: Context?=null
        constructor(context: Context, listOfContact:ArrayList<UserContact>){
            this.listOfContact=listOfContact
            this.context=context
        }


        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val userContact = listOfContact[p0]
            if(userContact.name.equals("No_user")){
                val inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val contactTicketView = inflator.inflate(R.layout.no_user, null)
                return contactTicketView
            }
            else {
                val inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                val contactTicketView = inflator.inflate(R.layout.contact_ticket, null)
                try{
                    contactTicketView.tvName.text = userContact.name
                    contactTicketView.tvPhoneNumber.text = userContact.phoneNumber
                }catch (e:Exception){
                    Log.d(e.message,"errormessage");

                }
                return contactTicketView
            }
        }

        override fun getItem(p0: Int): Any {
            return listOfContact[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getCount(): Int {
            return listOfContact.size
        }

    }


    // load contact will take all the contacts from phone and will place it on listOfContacts one by one
    var listOfContacts = HashMap<String,String>()
     fun loadContact(){
        listOfContacts.clear()
         val cursor = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,null,null,null)
             cursor!!.moveToFirst()
         do{
            val name = cursor.getString(cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
             val phoneNumber = cursor.getString(cursor!!.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
            listOfContacts.put(phoneNumber,name)
        }while (cursor!!.moveToNext())
    }



    // code for locaion
    fun getUserLocation(){


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
            Toast.makeText(this,"Got it",Toast.LENGTH_LONG).show()

            MainActivity.myLocation=loc
            Log.d(MainActivity.myLocation!!.latitude.toString(),"latitude")
            Log.d(MainActivity.myLocation!!.longitude.toString(),"longitude")
            IsAccessLocation=true
        }
        else{
            Toast.makeText(this,"could not get location",Toast.LENGTH_LONG).show()
        }



        var userData = UserData(this)
        var phoneNumber = userData.loadPhoneNumber()
        databaseRef!!.child("Users").child(phoneNumber).child("request").addValueEventListener(
            object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    try {
                        if(MainActivity.myLocation==null) {
                            Toast.makeText(applicationContext,MainActivity.myLocation.toString(),Toast.LENGTH_LONG).show()
                            return
                        }
                        Toast.makeText(applicationContext,MainActivity.myLocation.toString(),Toast.LENGTH_LONG).show()
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
                        Toast.makeText(applicationContext,"location error",Toast.LENGTH_LONG).show()
                    }
                }
            })
    }


    companion object{
        var myLocation:Location?=null
    }


}

