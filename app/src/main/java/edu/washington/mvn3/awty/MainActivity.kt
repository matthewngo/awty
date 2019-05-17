package edu.washington.mvn3.awty

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.telephony.SmsManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val messageText = findViewById<EditText>(R.id.messageText)
        val phoneText = findViewById<TextView>(R.id.phoneText)
        val minText = findViewById<EditText>(R.id.minText)
        val startButton = findViewById<Button>(R.id.startButton)
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        if (checkCallingOrSelfPermission(Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.SEND_SMS), 1)
        }

        startButton.setOnClickListener {
            if (startButton.text == "Start") {
                if (messageText.text.isEmpty() || phoneText.text.length != 10 || minText.text.isEmpty() || minText.text.toString() == "0") {
                    val message = "Cannot start alarm without all parameters"
                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                } else {
                    startButton.text = "Stop"
                    intent.putExtra("Message", messageText.text.toString())
                    intent.putExtra("Phone", phoneText.text.toString())
                    val alarmIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
                    val interval = minText.text.toString().toLong() * 60000
                    alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), interval, alarmIntent)
                }
            } else {
                startButton.text = "Start"
                val cancelIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT)
                alarmManager.cancel(cancelIntent)
            }
        }
    }

    class AlarmReceiver: BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val caption = "Texting " + formatPhone(intent!!.getStringExtra("Phone")) + "\n"
            val message = formatPhone(intent!!.getStringExtra("Phone")) + ":" + intent!!.getStringExtra("Message")
            Toast.makeText(context, caption + message, Toast.LENGTH_SHORT).show()
            println("PHONE: " + intent!!.getStringExtra("Phone"))
            SmsManager.getDefault().sendTextMessage(intent!!.getStringExtra("Phone"), null, message, null, null)
        }

        private fun formatPhone(phone: String): String {
            return "(" + phone.substring(0, 3) + ")" + phone.substring(3, 6) + "-" + phone.substring(6)
        }
    }
}
