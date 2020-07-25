package com.example.trackingdevice

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast


class startWithOs : BroadcastReceiver(){
    override fun onReceive(context: Context?, intent: Intent?) {


          /*  Toast.makeText(context!!,"boot completed",Toast.LENGTH_LONG).show()
            val intent = Intent(context!!,MyServices::class.java)
            context!!.startService(intent)*/
            Toast.makeText(context!!, "OnBootReceiver Received a broadcast!!", Toast.LENGTH_LONG).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context!!.startForegroundService(Intent(context, MyServices::class.java))
            } else {
                context!!.startService(Intent(context, MyServices::class.java))
            }



    }

}