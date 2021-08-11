package ir.rayantec.callsms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import android.os.Bundle
import android.telephony.SmsMessage
import java.lang.Exception


class SMSBroadcastReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent!!.action.equals("android.provider.Telephony.SMS_RECEIVED")) {
            val mBundle = intent!!.extras
            val msg: Array<SmsMessage?>
            var smsFrom: String
            if (mBundle != null) {
                try {
                    val mPdus = mBundle["pdus"] as Array<Any>?
                    msg = arrayOfNulls<SmsMessage>(mPdus!!.size)
                    for (i in mPdus!!.indices) {
                        msg[i] = SmsMessage.createFromPdu(mPdus!![i] as ByteArray)
                        smsFrom = msg[i]?.originatingAddress.toString()
                        val smsBody: String? = msg[i]?.messageBody
                        Toast.makeText(
                            context,
                            "شماره: $smsFrom / پیام: $smsBody",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}