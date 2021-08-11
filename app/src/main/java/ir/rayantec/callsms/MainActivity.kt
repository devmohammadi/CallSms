package ir.rayantec.callsms

import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.telephony.SmsManager.*
import android.text.TextUtils
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import android.content.Intent
import android.app.PendingIntent
import android.telephony.TelephonyManager
import android.widget.ArrayAdapter


class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 200
    private val SEND_SMS_REQUEST_CODE = 300
    private val RECEIVED_SMS_REQUEST_CODE = 100
    private var SMS_SENT = "SMS_SENT"
    private var SMS_DELIVERED = "SMS_DELIVERED"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        inputSim.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, arrayOf(1, 2)))

        if (checkPermissionReceiveSms()) {
            requestPermissionReceiveSms()
        }

        registerReceiver(sendSmsBroadcast, IntentFilter(SMS_SENT))
        registerReceiver(deliveredBroadcast, IntentFilter(SMS_DELIVERED))

        simState()

        buttonCall.setOnClickListener {
            if (checkPermissionCall()) {
                call()
            } else {
                requestPermissionCall()
            }
        }

        buttonSms.setOnClickListener {
            if (checkPermissionSendSms()) {
                sendSms()
            } else {
                requestPermissionSendSms()
            }
        }

    }

    private fun sendSms() {
        validationPhoneNumber("sms")
    }

    private fun call() {
        validationPhoneNumber("call")
    }

    private fun validationPhoneNumber(type: String) {
        val phone: String = inputTell.text.toString().trim()
        val sim: String = inputSim.text.toString().trim()
        when {
            TextUtils.isEmpty(phone) -> {
                Toast.makeText(this, "لطفا یک شماره وارد کنید", Toast.LENGTH_SHORT).show()
            }
            TextUtils.isEmpty(sim) -> {
                Toast.makeText(this, "لطفا سیم کارت را وارد کنید", Toast.LENGTH_SHORT).show()
            }
            phone.length != 11 -> {
                Toast.makeText(this, "شماره تلفن نا معتبر است", Toast.LENGTH_SHORT).show()
            }
            else -> {
                if (type == "call") {
                    val intent = Intent(Intent.ACTION_CALL)
                    intent.putExtra("simSlot",inputSim.text.trim().toString().toInt()-1 )
                    intent.data = Uri.parse("tel:$phone")
                    startActivity(intent)
                } else if (type == "sms") {
                    val textMessage: String = inputTextMessage.text.toString().trim()
                    if (TextUtils.isEmpty(textMessage)) {
                        Toast.makeText(this, "لطفا متن پیام را کنید", Toast.LENGTH_SHORT).show()
                    } else {
                        try {
                            val sentSMS = PendingIntent.getBroadcast(this, 0, Intent(SMS_SENT), 0)
                            val deliverSMS =
                                PendingIntent.getBroadcast(this, 0, Intent(SMS_DELIVERED), 0)
                            val smsManager: SmsManager = SmsManager.getSmsManagerForSubscriptionId(
                                inputSim.text.trim().toString().toInt()
                            )


                            smsManager.sendTextMessage(
                                "+98${removeFirstChar(phone)}",
                                null,
                                "$textMessage",
                                sentSMS,
                                deliverSMS
                            )
                            Toast.makeText(
                                this@MainActivity,
                                " پیامک ارسال شد",
                                Toast.LENGTH_SHORT
                            )
                                .show()
                            inputTextMessage.text?.clear()
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@MainActivity,
                                " پیامک ارسال نشد",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "مجوز داده شد", Toast.LENGTH_SHORT).show()
                call()
            } else {
                Toast.makeText(applicationContext, "مجوز رد شد", Toast.LENGTH_SHORT).show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.CALL_PHONE
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        showMessageOKCancel(
                            "در خواست مجوز تماس",
                            "برای عملکرد صحیح برنامه باید دسترسی به تماس تایید شود"
                        ) { _, _ ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissionCall()
                            }
                        }
                    }
                }
            }
            SEND_SMS_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "مجوز داده شد", Toast.LENGTH_SHORT).show()
                sendSms()
            } else {
                Toast.makeText(this, "مجوز رد شد", Toast.LENGTH_SHORT).show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.SEND_SMS
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        AlertDialog.Builder(this)
                        showMessageOKCancel(
                            "درخواست مجوز پیامک",
                            "برای عملکرد صحیح برنامه باید دسترسی به ارسال پیامک تایید شود"
                        ) { _, _ ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissionSendSms()
                            }
                        }
                    }
                }
            }
            RECEIVED_SMS_REQUEST_CODE -> if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(applicationContext, "مجوز داده شد", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "مجوز رد شد", Toast.LENGTH_SHORT).show()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            android.Manifest.permission.RECEIVE_SMS
                        )
                        != PackageManager.PERMISSION_GRANTED
                    ) {
                        AlertDialog.Builder(this)
                        showMessageOKCancel(
                            "درخواست مجوز دریافت پیامک",
                            "برای عملکرد صحیح برنامه باید دسترسی به دریافت پیامک تایید شود"
                        ) { _, _ ->
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissionReceiveSms()
                            }
                        }
                    }
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    private fun showMessageOKCancel(
        title: String,
        message: String,
        okListener: DialogInterface.OnClickListener
    ) {
        AlertDialog.Builder(this@MainActivity)
            .setMessage(message)
            .setTitle(title)
            .setPositiveButton("تایید", okListener)
            .setNegativeButton("لغو", null)
            .create()
            .show()
    }

    private fun checkPermissionCall(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.CALL_PHONE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkPermissionSendSms(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

    }

    private fun checkPermissionReceiveSms(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.RECEIVE_SMS
        ) != PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionCall() {
        ActivityCompat.requestPermissions(
            this, arrayOf(android.Manifest.permission.CALL_PHONE),
            PERMISSION_REQUEST_CODE
        )
    }

    private fun requestPermissionSendSms() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.SEND_SMS),
            SEND_SMS_REQUEST_CODE
        )
    }

    private fun requestPermissionReceiveSms() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.RECEIVE_SMS,
            ),
            RECEIVED_SMS_REQUEST_CODE
        )
    }

    private fun removeFirstChar(phone: String): String? {
        return phone.substring(1)
    }

    private fun simState() {
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val SIM_STATE = telephonyManager.simState
        if (SIM_STATE == TelephonyManager.SIM_STATE_READY) {
            Toast.makeText(this, "سیم کارت در دسترس است", Toast.LENGTH_SHORT).show()
        } else {
            var state = ""
            when (SIM_STATE) {
                TelephonyManager.SIM_STATE_ABSENT -> state = "Sim absent"
                TelephonyManager.SIM_STATE_NETWORK_LOCKED -> state = "Network locked"
                TelephonyManager.SIM_STATE_PIN_REQUIRED -> state = "Pin required"
                TelephonyManager.SIM_STATE_PUK_REQUIRED -> state = "PUK required"
                TelephonyManager.SIM_STATE_UNKNOWN -> state = "Unknown"
            }
            Toast.makeText(this, state, Toast.LENGTH_SHORT).show()
        }
    }

    private val sendSmsBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var state: String? = null
            when (resultCode) {
                Activity.RESULT_OK -> state = "پیامک ارسال شد"
                SmsManager.RESULT_ERROR_GENERIC_FAILURE -> state = "یک خطای عمومی رخ داد"
                SmsManager.RESULT_ERROR_NO_SERVICE -> state = "اپراتور در دسترس نیست"
                SmsManager.RESULT_ERROR_NULL_PDU -> state = " پروتکل PDU در دسترس نیست"
                SmsManager.RESULT_ERROR_RADIO_OFF -> state = "سیم کارت در دسترس نیست"
            }
            Toast.makeText(context, state, Toast.LENGTH_SHORT).show()
        }
    }

    private val deliveredBroadcast = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var state: String? = null
            when (resultCode) {
                Activity.RESULT_OK -> state = "پیامک تحویل داده شد"
                Activity.RESULT_CANCELED -> state = "پیامک تحویل داده نشد"
            }
            Toast.makeText(context, state, Toast.LENGTH_SHORT).show()
        }
    }

}


