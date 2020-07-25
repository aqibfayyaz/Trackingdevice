package com.example.trackingdevice

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.*
import android.widget.AdapterView
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_my_trackers.*
import kotlinx.android.synthetic.main.contact_ticket.view.*

class Mytracker : AppCompatActivity(){

    var listOfContact = ArrayList<UserContact>()
    var adapter:ContactAdapter?=null
    var userdata:UserData?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_trackers)
        userdata = UserData(applicationContext)


        try{
            userdata!!.loadContactInfo()
                // this is new changing addition of refreshdata()
            refreshData()
        }catch(e:Exception){
            Toast.makeText(this,e.message,Toast.LENGTH_LONG).show()
        }
        adapter = ContactAdapter(this,listOfContact)
        lvContactlist.adapter = adapter

        lvContactlist.onItemClickListener = AdapterView.OnItemClickListener{
            parent,view,position,id ->
            val userInfo = listOfContact[position]
            UserData.myTrackers.remove(userInfo.name)
            refreshData()
            userdata!!.saveContactInfo()

            // remove from firebase
            val firebaseDatabase = FirebaseDatabase.getInstance().reference
            val myuserdata = UserData(applicationContext);
            firebaseDatabase.child("Users").child(userInfo.phoneNumber.toString()).child("Finders").child(userdata!!.loadPhoneNumber()).removeValue()



        }

        // yeh shared reference say data utha rha tha lekin kuch error ki waja say kam ni kr rha so agar app band kr kay on kro to data save ki va show ni honaw
        //userdata!!.loadContactInfo()
        refreshData()
    }



    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.tracker_menu,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item!!.itemId){

            R.id.finishActivity ->{
                    finish()
            }
            R.id.addContact ->{
                checkPermission()
            }
            else ->{
                return super.onOptionsItemSelected(item)
            }
        }
        return true
    }

    // check permission function
    val CONTACT_CODE =123
    fun checkPermission(){
        if(Build.VERSION.SDK_INT>=23){
            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.READ_CONTACTS)!=
                    PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_CONTACTS),CONTACT_CODE)
                return
            }
        }
        pickContact()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when(requestCode){
            CONTACT_CODE ->{
                if(grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    pickContact()
                }
                else{
                    Toast.makeText(this,"Cannot access to contact",Toast.LENGTH_LONG).show()
                }
            }
            else->{
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }

        }
    }

    // function for pick contact
    val PICK_CODE = 1234
    fun pickContact(){
        val intent = Intent(Intent.ACTION_PICK,ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(intent,PICK_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        when(requestCode){
            PICK_CODE ->{

                if(resultCode == Activity.RESULT_OK)
                {
                    val contactData=data!!.data
                    val c = contentResolver.query(contactData!!,null,null,null,null)

                    if(c!!.moveToFirst()){

                        val id=c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                        val hasPhone = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER))

                        if(hasPhone.equals("1")){
                            val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + id, null,null)

                            if (phones != null) {
                                phones.moveToFirst()
                            }
                            var phoneNumber = phones!!.getString(phones.getColumnIndex("data1"))
                            Toast.makeText(this,phoneNumber,Toast.LENGTH_LONG).show()
                            Log.d(phoneNumber,"phnumber1")
                            phoneNumber = phoneNumber.replace("\\s".toRegex(), "")
                            Log.d(phoneNumber,"phnumber2")
                            val name = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))

                            UserData.myTrackers.put(name,phoneNumber)
                            refreshData()
                            userdata!!.saveContactInfo()
                       //     listOfContact.add(UserContact(name,phoneNumber))
                            //       adapter!!.notifyDataSetChanged()

                            // save to databases
                            val firebaseDatabase = FirebaseDatabase.getInstance().reference
                            val myuserdata = UserData(applicationContext);
                            firebaseDatabase.child("Users").child(phoneNumber).child("Finders").child(myuserdata.loadPhoneNumber()).setValue(true)

                        }
                    }
                }

            }
            else ->{
                super.onActivityResult(requestCode, resultCode, data)
            }
        }
}


      fun refreshData(){
        listOfContact.clear()
          for((key,value) in UserData.myTrackers){
              listOfContact.add(UserContact(key,value))
          }
          Toast.makeText(this, "refresh data is working", Toast.LENGTH_SHORT).show()
          adapter!!.notifyDataSetChanged()
    }


    class ContactAdapter:BaseAdapter{

        var listOfContact = ArrayList<UserContact>()
        var context:Context?=null
        constructor(context: Context,listOfContact:ArrayList<UserContact>){
            this.listOfContact=listOfContact
            this.context=context
        }


        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val userContact = listOfContact[p0]
            val inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val contactTicketView = inflator.inflate(R.layout.contact_ticket,null)
            contactTicketView.tvName.text=userContact.name
            contactTicketView.tvPhoneNumber.text=userContact.phoneNumber
            return contactTicketView
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



}