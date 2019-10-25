package com.ammuyutan.iou.Util;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.ammuyutan.iou.Models.ParcelableDialog;
import com.ammuyutan.iou.Models.Debtor;
import com.ammuyutan.iou.Models.YesNoDialogViewModel;
import com.ammuyutan.iou.Fragments.UpdateDebtDialogFragment;
import com.ammuyutan.iou.Fragments.GenericDialogFragment;

import java.math.BigDecimal;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ActivityUtil {
    public static boolean validateInputFields(EditText[] fields){
        for(EditText field : fields){
            if(field.getText().toString().length()<=0){
                return false;
            }
        }
        return true;
    }
    public static void getScreenWidthDp(AppCompatActivity appCompatActivity){
        //Get usable screen height and width in DP:
        Configuration configuration = appCompatActivity.getResources().getConfiguration();
        int screenWidthDp = configuration.screenWidthDp; //The current width of the available screen space, in dp units, corresponding to screen width resource qualifier.
        int smallestScreenWidthDp = configuration.smallestScreenWidthDp; //The smallest screen size an application will see in normal operation, corresponding to smallest screen width resource qualifier.
        Log.i(Statics.LOG_TAG_MAIN, "Screen width dp: " + screenWidthDp);
    }

    public static float getTextSizeInSp(Context context, EditText e) {
        float px = e.getTextSize();
        float scaledDensity = context.getResources().getDisplayMetrics().scaledDensity;
        float sp = px / scaledDensity;
        return sp;
    }

    public static Debtor buildDebtor(Cursor cursor){
        int id;
        String balance;
        String name, email, phoneNo, deadline;
        byte[] image;

        id = cursor.getInt(cursor.getColumnIndex("id"));
        balance = cursor.getString(cursor.getColumnIndex("balance"));
        name = cursor.getString(cursor.getColumnIndex("name"));
        email = cursor.getString(cursor.getColumnIndex("email"));
        phoneNo = cursor.getString(cursor.getColumnIndex("phoneNo"));
        deadline = cursor.getString(cursor.getColumnIndex("deadline"));
        image = cursor.getBlob(cursor.getColumnIndex("image"));


        return new Debtor(id,name,balance,deadline,phoneNo,email,image);
    }

    public static Debtor buildDebtor(String name, String balance, String email, String phoneNo, String deadline, byte[] image){
        Debtor debtor  = new Debtor();
        debtor.setName(name);
        debtor.setBalance(balance);
        debtor.setEmail(email);
        debtor.setPhoneNo(phoneNo);
        debtor.setDeadline(deadline);
        debtor.setImage(image);
        return debtor;
    }

    public String parseToBigDecimalFormat(String debt) {
        //for those decimal units:
        BigDecimal bigDecimal = new BigDecimal(debt);
        return bigDecimal.toString();
    }



    //Dialog Helper Methods
    public static AlertDialog.Builder buildSimpleOkDialog(Context context, View displayLayout, String message, @Nullable String positiveText, @Nullable DialogInterface.OnClickListener posListener){
        positiveText = (positiveText == null) ? "Ok" : positiveText;
        posListener = (posListener == null)?((dialog,which)->{dialog.dismiss();}):posListener;
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);
        if(displayLayout!=null){
            builder.setView(displayLayout);
        }else{
            builder.setMessage(message)
                    .setPositiveButton(positiveText, posListener);

        }
        return builder;
    }
    public static AlertDialog buildTitledOkDialog(Context context, String message, @Nullable String title, @Nullable String positiveText, @Nullable DialogInterface.OnClickListener posListener){

        //Form the dynamic alert dialog according to available arguments:
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setCancelable(false);


        positiveText = (positiveText == null) ? "Ok" : positiveText;
        if(posListener == null){
            //for normal displaying of info/status:
            posListener = (dialog, which) -> {
                dialog.dismiss();
            };

            builder.setCancelable(true);
        }
        builder.setPositiveButton(positiveText, posListener);


        if(title != null){
            builder.setTitle(title);
        }

        return builder.create();

    }
    public static AlertDialog.Builder buildSimpleYesNoDialog(Context context, @Nullable String title, String message, @Nullable String positiveText, @Nullable DialogInterface.OnClickListener positiveListener, @Nullable String negativeText, @Nullable DialogInterface.OnClickListener negativeListener){

        //Establish defaults on null case:
        positiveText = (positiveText == null) ? "Yes" : positiveText;
        negativeText = (negativeText == null) ? "No": negativeText;

        positiveListener = (positiveListener == null)? ((dialog, which) -> dialog.dismiss()) : positiveListener;
        negativeListener = (negativeListener == null)? ((dialog,which)->{dialog.dismiss();}) : negativeListener;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(message)
                .setNegativeButton(negativeText, negativeListener)
                .setPositiveButton(positiveText, positiveListener);


        if(title!=null){
            builder.setTitle(title);
        }
        return builder;
    }


    public static void showAlertDialog(FragmentActivity fragmentActivity, YesNoDialogViewModel vm){
        Bundle args = new Bundle();
        args.putParcelable(Statics.YES_NO_VM_TAG, vm);
        GenericDialogFragment ynDialogFragment = GenericDialogFragment.newInstance(args);

        FragmentManager fm = fragmentActivity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment yesNoFragment = fm.findFragmentByTag(Statics.YESNO_FRAGMENT_DIALOG);
        if(yesNoFragment!=null){
            ft.remove(yesNoFragment);
        }
        ft.addToBackStack(null);

        ynDialogFragment.show(ft, Statics.YESNO_FRAGMENT_DIALOG);
    }

    public static void showAlertDialog(FragmentActivity fragmentActivity, Dialog dialog, String tagName){

        Log.i(Statics.LOG_TAG_MAIN, "dialog tagname passed:" + tagName);

        FragmentManager fm = fragmentActivity.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();

        //check if there's an instance of this dialogfragment open and removie it:
        Fragment yesNoFragment = fm.findFragmentByTag(tagName);
        Log.i(Statics.LOG_TAG_MAIN, "is the fragment existing already: "+(yesNoFragment!=null));
        if(yesNoFragment!=null){
            Log.i(Statics.LOG_TAG_MAIN, "will remove existing same fragment first.");
            ft.remove(yesNoFragment);
        }
        ft.addToBackStack(null);

        //pass on builtDialog, display yesNo fragment
        Bundle args = new Bundle();
        args.putParcelable(Statics.DIALOG_EXTRA_TAG, new ParcelableDialog(dialog));
        DialogFragment dialogFragment = GenericDialogFragment.newInstance(args);
        Log.i(Statics.LOG_TAG_MAIN, "will show an alertdialog with tag: " + tagName);
        dialogFragment.show(fragmentActivity.getSupportFragmentManager().beginTransaction(), tagName); //ft.commit() is called here as well
    }

    public static void showUpdateDebtDialog(FragmentActivity fragAct, Debtor debtor){
        FragmentManager fm = fragAct.getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment activeFragment =  fm.findFragmentByTag(Statics.YESNO_FRAGMENT_DIALOG);
        if(activeFragment!=null){
            ft.remove(activeFragment);
        }
        ft.addToBackStack(null);

        Bundle args = new Bundle();
        args.putParcelable(Statics.PASSED_DEBTOR_TAG, debtor);
        DialogFragment updateDebtDialog  = UpdateDebtDialogFragment.newInstance(args);
        updateDebtDialog.show(ft,Statics.FRAGMENT_DIALOG_TAG);
    }

}
