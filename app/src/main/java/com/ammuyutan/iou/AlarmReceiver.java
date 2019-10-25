package com.ammuyutan.iou;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import android.telephony.SmsManager;

import com.ammuyutan.iou.Controllers.ViewDebtorsActivity;
import com.ammuyutan.iou.Models.Debtor;
import com.ammuyutan.iou.Util.Statics;

public class AlarmReceiver extends BroadcastReceiver {

    Debtor passedDebtor;
    DebtorDAO debtorDAO;
    @Override
    public void onReceive(Context context, Intent intent) {
        debtorDAO = new DebtorDAOLocalCache(context);
        int debtorId = intent.getIntExtra(Statics.DEBTOR_ID_TAG,0);
        passedDebtor = debtorDAO.getDebtorById(debtorId);
        //onReceive, display the notification in the intent extra
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notif = intent.getParcelableExtra(Statics.NOTIF_TAG);
        nm.notify(0,notif);
        sendSmsNotif(passedDebtor.getPhoneNo(), passedDebtor.getName(), String.valueOf(passedDebtor.getBalance()), ViewDebtorsActivity.getUserName());
    }


    public void sendSmsNotif(String phoneNo, String debtorName, String debt, String recipient) {
        SmsManager smsManager = SmsManager.getDefault();
        String phoneNum = "sms:" + phoneNo;
        String message = "Hi " + debtorName + ", your  Php. " + debt + " to " +recipient+ " is due today.";
        //smsManager.sendTextMessage(phoneNum, null, message, null, null);
    }
}
