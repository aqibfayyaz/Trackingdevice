package com.example.trackingdevice

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log


class UserData{

    var context:Context?=null
    var sharedRef:SharedPreferences?=null

    constructor(context: Context){
        this.context=context
        this.sharedRef=context.getSharedPreferences("userData",Context.MODE_PRIVATE)
    }

    fun savePhone(phoneNumber:String){
        val editor=sharedRef!!.edit()
        editor.putString("phoneNumber",phoneNumber)
        editor.commit()
    }

    fun loadPhoneNumber():String{
        val phoneNumber = sharedRef!!.getString("phoneNumber","empty")
        if(phoneNumber.equals("empty")){
            val intent = Intent(context,login::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context!!.startActivity(intent)
        }
        return phoneNumber!!
    }

    fun getPhoneNumber():String{
        val phoneNumber = sharedRef!!.getString("phoneNumber","empty")
        return phoneNumber!!
    }


    // save data of tracker to shared preferenece

    fun saveContactInfo(){

        var listOfTrackers = ""
        for((key,value) in myTrackers){

            if(listOfTrackers.length==0){
                listOfTrackers =  key + "%" + value
            }else{
                listOfTrackers += "%" +  key + "%" + value
            }
        }
        if(listOfTrackers.length == 0){
            listOfTrackers = "empty"
        }

        val editor = sharedRef!!.edit()
        editor.putString("listOfTrackers",listOfTrackers)
        editor.commit()
    }


    // load data from shared preference
    fun loadContactInfo(){
        myTrackers.clear()
        var listOfTrackers = sharedRef!!.getString("listOfTrackers","empty")
        if(!listOfTrackers.equals("empty")){
            var userInfo = listOfTrackers!!.split("%").toTypedArray()

          //  Log.d("myTag", userInfo.toString()) // Log.d("mylength",userInfo.size.toString())

          /*  for(items in userInfo){
                Log.d("myitems",items)
            }*/


            var i = 0
            while(i < userInfo.size){
                myTrackers.put(userInfo[i],userInfo[i+1])
                i+=2
            }
        }
    }



    companion object {
        var myTrackers:MutableMap<String,String> = HashMap()
    }



}