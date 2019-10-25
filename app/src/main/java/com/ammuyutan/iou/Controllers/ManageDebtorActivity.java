package com.ammuyutan.iou.Controllers;

import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.SpannedString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ammuyutan.iou.AlarmReceiver;
import com.ammuyutan.iou.DebtorDAO;
import com.ammuyutan.iou.DebtorDAOLocalCache;
import com.ammuyutan.iou.Fragments.UpdateDebtDialogFragment;
import com.ammuyutan.iou.R;
import com.ammuyutan.iou.Util.CurrencyEditText;
import com.bumptech.glide.Glide;
import com.ammuyutan.iou.Models.Debtor;
import com.ammuyutan.iou.Util.PhotoUtil;
import com.ammuyutan.iou.Util.Statics;
import com.ammuyutan.iou.Util.ActivityUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;


public class ManageDebtorActivity extends AppCompatActivity {


    EditText txtName, txtDeadline, txtPhoneNo, txtEmail;
    CurrencyEditText txtBalance;
    ImageView imgPhoto;
    TextView labelActions;
    Button btnUpdateBalance, btnResolveDebt, btnExtendDeadline, btnSubmit;
    FloatingActionButton btnTakePhoto, btnChoosePhoto;
    ImageButton btnEdit;

    Debtor passedDebtor, refDebtor;
    DebtorDAO debtorDao;
    private Date currentDeadline;


    //state-change(flags) variables, readonly and final variables
    String mCurrentPhotoPath;
    boolean isForCreatePurpose = true;
    boolean isEditFieldsMode = false;
    final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());//MMM dd, yyyy hh:mm a
    final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm a");//a try for this new api

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_debtor);
        init();
        assessActivityPurpose();
    }

    private void init() {
        debtorDao = new DebtorDAOLocalCache(getApplicationContext());
        txtName = findViewById(R.id.txtName);
        txtBalance = findViewById(R.id.txtBalance);

        txtDeadline = findViewById(R.id.txtDeadline);

        txtPhoneNo = findViewById(R.id.txtPhoneNo);
        txtEmail = findViewById(R.id.txtEmail);
        imgPhoto = findViewById(R.id.imgPhoto);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnEdit = findViewById(R.id.btnEdit);


        btnResolveDebt = findViewById(R.id.btnResolveDebt);
        btnUpdateBalance = findViewById(R.id.btnUpdateBalance);
        btnUpdateBalance.setOnClickListener(this::updateDebtAmount);
        btnExtendDeadline = findViewById(R.id.btnExtendDeadline);
        btnTakePhoto = findViewById(R.id.btnTakePhoto);
        btnChoosePhoto = findViewById(R.id.btnChoosePhoto);
        labelActions = findViewById(R.id.labelActions);

        //assign view event listeners
        setDeadlineOnClick();


        //if there's a debtor id/phoneNo passed, get the debtor from db.
        int passedDebtorId = getIntent().getIntExtra(Statics.DEBTOR_ID_TAG, 0);//no id that is 0, SQLite's rowId starts at 1
        passedDebtor = debtorDao.getDebtorById(passedDebtorId);
        if (passedDebtor == null) {
            //get object from phoneNo
            String phone = getIntent().getStringExtra(Statics.DEBTOR_PHONE_TAG);
            passedDebtor = debtorDao.getDebtorByPhoneNo(phone);
        }
        setActivityDebtor(passedDebtor);
        Log.i(Statics.LOG_TAG_MAIN, "passed debtor is null? = " + (passedDebtor == null));
    }


    void setDeadlineOnClick() {
        txtDeadline.setOnClickListener(view -> {
            //set current date or current debtor deadline for initial date in DatePickerDialog
            Log.i(Statics.LOG_TAG_MAIN, "txtdeadline onclick");
            Calendar cal = Calendar.getInstance();
            Date deadline = getCurrentDeadline();
            if (deadline != null) {
                cal.setTime(deadline);
            }
            hideSoftKeyboard();
            showDatePicker(cal);
        });
    }


    private void assessActivityPurpose() {
        //Transform the UI acc. to the purpose (Adding or Viewing)

        if (passedDebtor != null) {
            //Configure activity for debtor viewing
            try {
                Date deadline = DATE_FORMAT.parse(passedDebtor.getDeadline());//Format: Oct 15, 2019 08:17 PM
                setCurrentDeadline(deadline);
            } catch (ParseException e) {
                e.printStackTrace();
            }

            //populate fields:
            isForCreatePurpose = false;

            txtDeadline.setFocusable(false);
            txtDeadline.setEnabled(false);

            populateFields();

            btnSubmit.setVisibility(View.GONE);
            btnSubmit.setText(R.string.update);


            //disable fields (name,email,balance,deadline)
            configureFieldEnabledState(false);



        } else {
            //configure UI for debtor adding
            configureVisibility(View.GONE);//(submit, update debt, resolve debt, extend deadline)
        }


    }

    private void populateFields(){
        Debtor referenceDebtor = getActivityDebtor();
        txtName.setText(referenceDebtor.getName());
        String balance = Statics.APP_CURRENCY + referenceDebtor.getBalance();
        txtBalance.setText(balance);
        txtDeadline.setText(referenceDebtor.getDeadline());
        txtEmail.setText(referenceDebtor.getEmail());
        txtPhoneNo.setText(referenceDebtor.getPhoneNo());

        //image = Base64.decode(Base64.encodeToString(referenceDebtor.getImage(), Base64.DEFAULT), Base64.DEFAULT);
        byte[] image = referenceDebtor.getImage();
        Bitmap imageBitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        imgPhoto.setImageBitmap(imageBitmap);
    }
    private void configureFieldEnabledState(boolean flag) {

        txtName.setFocusableInTouchMode(flag);
        txtPhoneNo.setFocusableInTouchMode(flag);
        txtEmail.setFocusableInTouchMode(flag);
        txtBalance.setFocusableInTouchMode(flag);

        //greyed out
        txtName.setEnabled(flag);
        txtPhoneNo.setEnabled(flag);
        txtEmail.setEnabled(flag);
        txtBalance.setEnabled(flag);
        txtDeadline.setEnabled(flag);
        btnTakePhoto.setEnabled(flag);
        btnChoosePhoto.setEnabled(flag);


        if (!flag) {
            Log.i(Statics.LOG_TAG_MAIN, "Viewing only. Hide those edittext lines.");

            btnEdit.setImageResource(R.drawable.ic_create_white_24dp);

            //fields just for viewing, remove underline visual effect
            txtName.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);
            txtPhoneNo.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);
            txtEmail.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);
            txtBalance.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);
            txtDeadline.getBackground().setColorFilter(Color.TRANSPARENT, PorterDuff.Mode.SRC_IN);

            txtName.setTextColor(getResources().getColor(android.R.color.white,null));
            txtPhoneNo.setTextColor(getResources().getColor(android.R.color.white,null));
            txtEmail.setTextColor(getResources().getColor(android.R.color.white,null));
            txtBalance.setTextColor(getResources().getColor(R.color.red,null));
            txtDeadline.setTextColor(getResources().getColor(android.R.color.white,null));

            btnTakePhoto.setSupportBackgroundTintList(ContextCompat.getColorStateList(this,android.R.color.darker_gray));
            btnChoosePhoto.setSupportBackgroundTintList(ContextCompat.getColorStateList(this,android.R.color.darker_gray));
            btnSubmit.setVisibility(View.GONE);


        } else {
            Log.i(Statics.LOG_TAG_MAIN, "Editing. Gonna show the underlines again");

            btnEdit.setImageResource(R.drawable.arrow_back_white);
            txtName.getBackground().clearColorFilter();
            txtPhoneNo.getBackground().clearColorFilter();
            txtEmail.getBackground().clearColorFilter();
            txtBalance.getBackground().clearColorFilter();
            txtDeadline.getBackground().clearColorFilter();

            btnTakePhoto.setSupportBackgroundTintList(ContextCompat.getColorStateList(this,R.color.colorPrimary));
            btnChoosePhoto.setSupportBackgroundTintList(ContextCompat.getColorStateList(this,R.color.colorPrimary));
            btnSubmit.setVisibility(View.VISIBLE);

        }

    }


    private void configureVisibility(int flag) {
        //Make "GONE" ui elements for managing activity
        btnEdit.setVisibility(flag);
        labelActions.setVisibility(flag);
        btnResolveDebt.setVisibility(flag);
        btnUpdateBalance.setVisibility(flag);
        btnExtendDeadline.setVisibility(flag);

    }


    @Override
    protected void onDestroy() {
        Log.i(Statics.LOG_TAG_MAIN, "ManageDebtorActivity destroyed");
        configureFieldEnabledState(true);
        super.onDestroy();
    }

    public void editFields(View v) {
        /*edit fields toggle*/
        Log.i(Statics.LOG_TAG_MAIN, "edit fields pressed..performing commands");

        if (isEditFieldsMode) {
            populateFields();
            Log.i(Statics.LOG_TAG_MAIN, "going back to view mode...");
            configureFieldEnabledState(false);
            isEditFieldsMode = false;
            //prevent showing date picker in view mode
            txtDeadline.setOnClickListener(null);
            return;
        }

        Log.i(Statics.LOG_TAG_MAIN, "enabling edit mode...");
        configureFieldEnabledState(true);
        isEditFieldsMode = true;

        //only allow opening date picker on edit mode
        setDeadlineOnClick();

    }

    void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    public void updateDebtAmount(View v) {
        //For extend deadline button, show dialog that handles this task
        Bundle args = new Bundle();
        args.putParcelable(Statics.PASSED_DEBTOR_TAG, getActivityDebtor());

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment existingDialog = getSupportFragmentManager().findFragmentByTag(Statics.FRAGMENT_DIALOG_TAG);

        if (existingDialog != null) {
            fragmentTransaction.remove(existingDialog);
        }

        fragmentTransaction.addToBackStack(null);
        DialogFragment updateDebtDialog = UpdateDebtDialogFragment.newInstance(args);
        updateDebtDialog.show(fragmentTransaction, Statics.FRAGMENT_DIALOG_TAG);

    }

    public void resolveDebt(View v) {
        Debtor refDebtor = getActivityDebtor();
        Context context = ManageDebtorActivity.this;
        String redBalance = Html.fromHtml("<font color=red>"+Statics.APP_CURRENCY+refDebtor.getBalance()+"</font> debt of ",Html.FROM_HTML_MODE_LEGACY).toString();
        String message = "Resolve the "+ redBalance + refDebtor.getName();
        String successMessage = refDebtor.getName() + "'s debt is settled.";
        AlertDialog.OnClickListener posListener = (dialog, which) -> {
            debtorDao.deleteDebtor(refDebtor.getId());
            showSTRDialog(successMessage, ViewDebtorsActivity.class);
        };


        AlertDialog.Builder resolveDialog = ActivityUtil.buildSimpleYesNoDialog(context, null, message, null, posListener, null, null);
        ActivityUtil.showAlertDialog(ManageDebtorActivity.this, resolveDialog.create(), "resolve_dialog");

    }

    public void updateDebtEditText(String newBalance) {
        String text = Statics.APP_CURRENCY + newBalance;
        txtBalance.setText(text);
    }

    public void executeProcess(View v) {
        /*For add or update*/

        String name, phoneNo, email, deadline, balance;

        name = txtName.getText().toString();
        deadline = txtDeadline.getText().toString();
        phoneNo = txtPhoneNo.getText().toString();
        email = txtEmail.getText().toString();
        balance = txtBalance.getText().toString().substring(Statics.APP_CURRENCY.length()); //remove the 'Php.'



        //For image (Bitmap to byte[] conversion):
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        //convert the image to byte[]
        Bitmap imageBitmap = ((BitmapDrawable) imgPhoto.getDrawable()).getBitmap();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 10, stream);//fills up the 'stream' object
        byte[] image = stream.toByteArray();

        Debtor newDebtor;
        /*check if this is for an add action; setCurrentDeadline if yes*/
        if (isForCreatePurpose) {
            //no id, it's auto-increment by SQLite
            newDebtor = new Debtor(name, balance, deadline, phoneNo, email, image);
            try {
                Date date_deadline = DATE_FORMAT.parse(deadline);
                setCurrentDeadline(date_deadline);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } else {
            //for update purpose, give the existing pk (ID)
            newDebtor = new Debtor(passedDebtor.getId(), name, balance, deadline, phoneNo, email, image);
        }

        //create the object with the view data


        boolean isFieldsOk = ActivityUtil.validateInputFields(new EditText[]{txtName, txtBalance, txtDeadline, txtPhoneNo, txtEmail});

        //validate input fields:
        if (isFieldsOk) {

            if (isEditFieldsMode) {
                /*Update*/


                newDebtor.setBalance(balance);
                Log.i(Statics.LOG_TAG_MAIN, "new deadline: " + newDebtor.getDeadline() + "new balance: " + newDebtor.getBalance());
                updateDebtor(newDebtor);
            } else {
                /*Add*/
                addDebtor(newDebtor);
            }

        } else {
            showFillFieldsDialog();
        }

    }

    private void addDebtor(Debtor debtor) {

        setAlarm(debtor);
        //DB operation
        debtorDao.addDebtor(debtor);
        //Clean-up local directory
        deleteLocalImageFiles();

        //prepare alert dialog content:
        String title = "Debt recorded!";
        String message = debtor.getName()+"'s " +Statics.APP_CURRENCY + debtor.getBalance() +" on " + debtor.getDeadline();
        AlertDialog.OnClickListener posListener = ((dialog, which) -> {
            dialog.dismiss();
            Intent intent = new Intent(ManageDebtorActivity.this, ViewDebtorsActivity.class);
            startActivity(intent);
            finish();
        });
        AlertDialog redirectHomeDialog = ActivityUtil.buildTitledOkDialog(ManageDebtorActivity.this, message, title,null, posListener);
        ActivityUtil.showAlertDialog(ManageDebtorActivity.this, redirectHomeDialog, "redirect_home_dialog");
    }
    private void deleteLocalImageFiles(){
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String[]files = dir.list();
        for (String path : files) {
            boolean deleteAction = new File(dir,path).delete();
            Log.i(Statics.LOG_TAG_MAIN, "file deleted: " + deleteAction);
        }
    }

    private void updateDebtor(Debtor newDebtor) {

        debtorDao.updateDebtor(newDebtor);
        //update db record and activity passed debtor accordingly:
        //show success and change summary dialog:
        displayChangeSummaryDialog(getActivityDebtor(), newDebtor);
        setActivityDebtor(newDebtor);
        //delete existing and set new alarm:
        deleteAlarm(newDebtor);
        setAlarm(newDebtor);
    }

    @SuppressLint("InflateParams")
    void displayChangeSummaryDialog(Debtor origDebtor, Debtor newDebtor){

        View changesView = getLayoutInflater().inflate(R.layout.dialog_changes_ok,null);
        ImageView ivName, ivPhone, ivEmail, ivBalance, ivDeadline;
        TextView tvName, tvPhone, tvEmail, tvBalance, tvDeadline;

        ivName = changesView.findViewById(R.id.iv_dialog_changes_name);
        ivPhone = changesView.findViewById(R.id.iv_dialog_changes_phone);
        ivEmail = changesView.findViewById(R.id.iv_dialog_changes_email);
        ivBalance = changesView.findViewById(R.id.iv_dialog_changes_balance);
        ivDeadline = changesView.findViewById(R.id.iv_dialog_changes_deadline);

        tvName = changesView.findViewById(R.id.tv_dialog_changes_name);
        tvPhone = changesView.findViewById(R.id.tv_dialog_changes_phone);
        tvEmail = changesView.findViewById(R.id.tv_dialog_changes_email);
        tvBalance = changesView.findViewById(R.id.tv_dialog_changes_balance);
        tvDeadline = changesView.findViewById(R.id.tv_dialog_changes_deadline);

        Button btnOk = changesView.findViewWithTag(getResources().getString(R.string.tag_dialog_ok_btn));



        tvName.setText(getString(R.string.updated_field_message,"Name",newDebtor.getName()));
        tvPhone.setText(getString(R.string.updated_field_message,"Phone",newDebtor.getPhoneNo()));
        tvEmail.setText(getString(R.string.updated_field_message,"Email",newDebtor.getEmail()));
        tvBalance.setText(getString(R.string.updated_debt_message,"Balance",newDebtor.getBalance()));
        tvDeadline.setText(getString(R.string.updated_date_message,"Deadline",newDebtor.getDeadline()));


        int changes = 5;
        //only display properties changed/updated:
        if(origDebtor.getName().equals(newDebtor.getName())){
            changes--;
            Log.d(Statics.LOG_TAG_MAIN, "name nothing changed");
            ivName.setVisibility(View.GONE);
            tvName.setVisibility(View.GONE);
        }
        if(origDebtor.getPhoneNo().equals(newDebtor.getPhoneNo())){
            changes--;
            Log.d(Statics.LOG_TAG_MAIN, "phone nothing changed");
            ivPhone.setVisibility(View.GONE);
            tvPhone.setVisibility(View.GONE);
        }
        if(origDebtor.getEmail().equals(newDebtor.getEmail())){
            changes--;
            Log.d(Statics.LOG_TAG_MAIN, "email nothing changed");
            ivEmail.setVisibility(View.GONE);
            tvEmail.setVisibility(View.GONE);
        }
        if(origDebtor.getBalance().equals(newDebtor.getBalance())) {
            changes--;
            Log.d(Statics.LOG_TAG_MAIN, "balance nothing changed");
            ivBalance.setVisibility(View.GONE);
            tvBalance.setVisibility(View.GONE);
        }
        if(origDebtor.getDeadline().equals(newDebtor.getDeadline())){
            changes--;
            Log.d(Statics.LOG_TAG_MAIN, "deadline nothing changed");
            ivDeadline.setVisibility(View.GONE);
            tvDeadline.setVisibility(View.GONE);
        }


        AlertDialog dialog;
        if(changes == 0){
            String message = "Nothing changed.";
            dialog = ActivityUtil.buildSimpleOkDialog(ManageDebtorActivity.this,null,message,null,null).create();
        }else{
            dialog = ActivityUtil.buildSimpleOkDialog(ManageDebtorActivity.this, changesView, null,null,null).create();
            btnOk.setOnClickListener((View v)->{
                populateFields();
                configureFieldEnabledState(false);
                dialog.dismiss();
            });
        }
//        dialog = ActivityUtil.buildSimpleOkDialog(ManageDebtorActivity.this,null,"Update successful!",null,null).create();
        ActivityUtil.showAlertDialog(ManageDebtorActivity.this, dialog,"dialog_changes");
    }

    HashMap<String,SpannableStringBuilder> createChangesTextSpannables(Debtor origDebtor, Debtor newDebtor){

        HashMap<String,SpannableStringBuilder> spans = new HashMap<>();
        spans.put(Statics.SpannableKeys.NAME_KEY,createSpannable("Name: "+origDebtor.getName()+" to " +newDebtor.getName()));
        spans.put(Statics.SpannableKeys.PHONE_KEY,createSpannable("PhoneNo: "+origDebtor.getPhoneNo()+" to "+newDebtor.getPhoneNo()));
        spans.put(Statics.SpannableKeys.EMAIL_KEY,createSpannable("Email: "+origDebtor.getEmail()+" to " + newDebtor.getEmail()));
        spans.put(Statics.SpannableKeys.BALANCE_KEY,createSpannable("Balance: Php."+origDebtor.getBalance()+" to Php."+newDebtor.getBalance()));
        spans.put(Statics.SpannableKeys.DEADLINE_KEY,createSpannable("Deadline: "+origDebtor.getDeadline()+" to "+newDebtor.getDeadline()));
        return spans;


    }

    SpannableStringBuilder createSpannable(String source){
        //Produces the desired colors of "(white)Property: (white)beforeValue (white)to (orange)newValue"
        String[] segments = source.split(" "); //by normal whitespace
        String propName = segments[0];
        String beforeVal = segments[1];
        String to = segments[2];
        String newVal = segments[3];

        SpannableString propSpann = new SpannableString(propName);
        propSpann.setSpan(new ForegroundColorSpan(Color.WHITE),
                        0, propName.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString beforeSpann = new SpannableString(beforeVal);
        beforeSpann.setSpan(new ForegroundColorSpan(Color.WHITE),
                0, beforeVal.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString toSpann = new SpannableString(to);
        toSpann.setSpan(new ForegroundColorSpan(Color.WHITE),
                0, to.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableString newSpann = new SpannableString(newVal);
        newSpann.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.IOU_Orange,null)),
                0, newVal.length(),Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        SpannableStringBuilder finalSpannable = new SpannableStringBuilder();
        finalSpannable.append(propSpann).append(" ").append(beforeSpann)
                .append(" ").append(toSpann).append(" ").append(newSpann);


        return finalSpannable;

    }

    HashMap<String,String> getPropertyChanges(Debtor origDebtor, Debtor newDebtor){
        //no I'm not going to use reflection to iterate on object's properties
        HashMap changes = new HashMap<String, String>();
        if(!origDebtor.getName().equals(newDebtor.getName())){
            changes.put("name", newDebtor.getName());
        }
        if(!origDebtor.getPhoneNo().equals(newDebtor.getPhoneNo())){
            changes.put("phoneNo", newDebtor.getPhoneNo());
        }
        if(!origDebtor.getEmail().equals(newDebtor.getEmail())){
            changes.put("email", newDebtor.getEmail());
        }
        if(!origDebtor.getBalance().equals(newDebtor.getBalance())) {
            changes.put("balance", newDebtor.getBalance());
        }
        if(!origDebtor.getDeadline().equals(newDebtor.getDeadline())){
            changes.put("deadline", newDebtor.getDeadline());
        }

        return changes;
    }

    private File createImageFile() throws IOException {

        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";

        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);//private directory as against getExternalStoragePublicDirectory()

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        Log.i(Statics.LOG_TAG_MAIN, "absolutePath = " + mCurrentPhotoPath);

        return image;
    }

    public void takePhoto(View view) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //condition ensures there's an app to handle the intent:
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

            //if you don't save it as a File in a URI, it will be saved as a "small" bitmap good for icons only.
            try {

                File photoFile = createImageFile();
                if (photoFile == null) {

                    Log.i(Statics.LOG_TAG_MAIN, "photoFile is null. Check method createImageFile()");
                    return;

                }
                //method returns a "content://" which is the latest URI after API 24 (Android 7.0 Nougat) -> requires FileProvider in manifest.
                Uri photoURI = FileProvider.getUriForFile(this, "com.ammuyutan.iou.fileprovider", photoFile);//second argument must match the "authorities" in <provider> Manifest
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);//saves the selected image in the URI


                startActivityForResult(takePictureIntent, Statics.REQUEST_IMAGE_CAPTURE);

            } catch (IOException ex) {
                ex.printStackTrace();
            }

//            //local data, settle with small bitmaps
//            startActivityForResult(takePictureIntent, Statics.REQUEST_IMAGE_CAPTURE);

        }

    }

    public void choosePhoto(View v) {
        Intent choosePhotoIntent = new Intent();
        choosePhotoIntent.setType("image/*");
        choosePhotoIntent.setAction(Intent.ACTION_GET_CONTENT);//implicit intents
        startActivityForResult(Intent.createChooser(choosePhotoIntent, "Select Picture"), Statics.PICK_IMAGE_REQUEST);
    }

    /*Capture image/choose image activity result handler:*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == Statics.REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            //scale image acc to the size of the image view
            Bitmap sourceBitmap = PhotoUtil.scaleDownBitmapToImageView(mCurrentPhotoPath, imgPhoto);
            Glide.with(ManageDebtorActivity.this).load(sourceBitmap).into(imgPhoto);


            /*Manual rotation using Exif:
            //holds 2 bitmaps, expensive. -Try the APIs available in GIT - Glide Picasso, etc.
            Bitmap rotatedBitmap = PhotoUtil.rotateBitmap(mCurrentPhotoPath, sourceBitmap);
            imgPhoto.setImageBitmap(rotatedBitmap);
            */

            /* Original approach, get it from extra in a very compressed version. For icons only
            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            imgPhoto.setImageBitmap(imageBitmap);
            */


        }

        if (requestCode == Statics.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            /*
            String[] projection = { MediaStore.MediaColumns.DATA };
            CursorLoader cursorLoader = new CursorLoader(this,uri, projection, null, null,null);
            Cursor cursor = cursorLoader.loadInBackground();
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            cursor.moveToFirst();
            String selectedImagePath = cursor.getString(column_index);
            Log.i(Statics.LOG_TAG_MAIN, "selectedImagePath = " + selectedImagePath);
            try {

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                Bitmap rotatedBitmap = PhotoUtil.rotateBitmap(selectedImagePath,bitmap);
                imgPhoto.setImageBitmap(rotatedBitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }
            */

            Glide.with(ManageDebtorActivity.this).load(uri).into(imgPhoto);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void setAlarm(Debtor debtor) {

        LocalDateTime deadline = LocalDateTime.parse(debtor.getDeadline(), DATETIME_FORMAT);
        long deadlineMillis = deadline.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        int pi_id = debtor.getId();


        //create intent for broadcast receiver, include the notif to be displayed
        Intent broadcastIntent = new Intent(this, AlarmReceiver.class);
        Notification deadlineNotif = createNotification(debtor, getManageDebtorPI(debtor),
                getCallDebtorPI(debtor), getManageDebtorPI(debtor));
        broadcastIntent.putExtra(Statics.NOTIF_TAG, deadlineNotif);
        broadcastIntent.putExtra(Statics.DEBTOR_ID_TAG, debtor.getId());

        //wrap source intent in a  PendingIntent
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), pi_id, broadcastIntent, PendingIntent.FLAG_ONE_SHOT);//ONE_SHOT because the alarm is only meant to fire once.

        //give pending intent to the alarm service
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, deadlineMillis, pendingIntent);
//        Log.i(Statics.LOG_TAG_MAIN, "Alarm set: " + debtor.getDeadline() + "\t Id of alarm: " + pi_id);

        //test if alarm is really set:
        if (PendingIntent.getBroadcast(getApplicationContext(), pi_id, broadcastIntent, PendingIntent.FLAG_NO_CREATE) != null) {
            Log.i(Statics.LOG_TAG_MAIN, "Alarm " + pi_id + " is set!");
        }

    }

    public Notification createNotification(Debtor debtor, PendingIntent contentIntent,
                                           PendingIntent callIntent, PendingIntent extendIntent) {

        long[] vibrationPattern = {0, 300, 0, 300};
        String notifChannelId = "n_channel", debtorName = debtor.getName();
        NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

        //For Android SDK 26 above, NotificationChannel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel nChannel = new NotificationChannel(notifChannelId, "nChannel",
                    NotificationManager.IMPORTANCE_HIGH);
            nChannel.enableLights(true);
            nChannel.setLightColor(Color.rgb(255, 128, 0));//orange color
            notificationManager.createNotificationChannel(nChannel);
        }

        String title = "Deadline - " + debtorName;
        String contentText = getString(R.string.peso_sign_space) + debtor.getBalance() + " debt due";
        NotificationCompat.Builder notifBuilder = new NotificationCompat.Builder(this, notifChannelId);
        notifBuilder.setContentTitle(title)
                .setContentText(contentText)
                .setTicker(title)
                .setSmallIcon(R.drawable.ic_applogo)
                .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                .setGroup(Statics.GROUP_KEY)
                .setVibrate(vibrationPattern)
                .setVisibility(Notification.VISIBILITY_PRIVATE)//notification content when screen is locked
                .setContentIntent(contentIntent)
                .addAction(0, "Call", callIntent) //Icon, Text, Pending Intent
                .addAction(0, "Manage", extendIntent);

        return notifBuilder.build();
    }

    PendingIntent getManageDebtorPI(Debtor debtor) {


        int mdId = debtor.getId();
        Intent mdIntent = new Intent(getApplicationContext(), ManageDebtorActivity.class);
        Log.i(Statics.LOG_TAG_MAIN, "Debtor id" + mdId + " getting added as extra for notification intent.");
        mdIntent.putExtra(Statics.DEBTOR_PHONE_TAG, debtor.getPhoneNo());


        //Create a synthetic back stack to display an intended activity on back navigation:
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(ManageDebtorActivity.this);
        stackBuilder.addNextIntentWithParentStack(mdIntent);

        return stackBuilder.getPendingIntent(mdId, PendingIntent.FLAG_UPDATE_CURRENT);

    }


    PendingIntent getCallDebtorPI(Debtor debtor) {

        String phoneNum = "tel: " + debtor.getPhoneNo();
        int requestCode = debtor.getId();

        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(phoneNum));
        PendingIntent pIntent = PendingIntent.getActivity(getApplicationContext(), requestCode, intent, 0);
        return pIntent;
    }

    public void callDebtor(View view) {

        try {
            Log.i(Statics.LOG_TAG_MAIN, "inside call debtor");
            String phoneNo = "tel:" + txtPhoneNo.getText().toString();
            Intent phoneCallIntent = getPhoneCallIntent(phoneNo);

            if (ContextCompat.checkSelfPermission(ManageDebtorActivity.this,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                Log.i(Statics.LOG_TAG_MAIN, "Permission denied");
                ActivityCompat.requestPermissions(ManageDebtorActivity.this,
                        new String[]{Manifest.permission.CALL_PHONE}, Statics.REQUEST_PHONE_CALL);
            }

            startActivity(phoneCallIntent);

        } catch (SecurityException ex) {
            ex.printStackTrace();
        }
    }

    /*handles the response for the service permission request*/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case Statics.REQUEST_PHONE_CALL: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    String phoneNo = "tel:" + txtPhoneNo.getText().toString();
                    startActivity(getPhoneCallIntent(phoneNo));
                } else {
                    Toast.makeText(ManageDebtorActivity.this, "Phone call permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private Intent getPhoneCallIntent(String phoneNo) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse(phoneNo));
        return callIntent;
    }


    public void deleteAlarm(Debtor refDebtor) {
        AlarmManager alarmManager = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);

//        Intent intent = new Intent(this, AlarmReceiver.class);
//        intent.putExtra(Statics.VIEW_DETAILS_TAG, getActivityDebtor());
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, pi_id, intent, 0);

        //create the same pending intent to delete it:
        Intent broadcastIntent = new Intent(this, AlarmReceiver.class);
        Notification deadlineNotif = createNotification(refDebtor, getManageDebtorPI(refDebtor),
                getCallDebtorPI(refDebtor), getManageDebtorPI(refDebtor));
        broadcastIntent.putExtra(Statics.NOTIF_TAG, deadlineNotif);
        broadcastIntent.putExtra(Statics.DEBTOR_ID_TAG, refDebtor.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), refDebtor.getId(), broadcastIntent, PendingIntent.FLAG_ONE_SHOT);//ONE_SHOT because the alarm is only meant to fire once.
        alarmManager.cancel(pendingIntent);
        pendingIntent.cancel();
    }

    private boolean checkIfPhoneNoExists(String phoneNo) {
        return debtorDao.getDebtorByPhoneNo(phoneNo) != null;
    }


    public void extendDeadline(View view) {
        /*
        calendar.setTime(currentDeadline);
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        hour = calendar.get(Calendar.HOUR);
        minute = calendar.get(Calendar.MINUTE);

        datePickerDialog = new DatePickerDialog(ManageDebtorActivity.this,R.style.CustomPickerDialog, date, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
        */

        Calendar cal = Calendar.getInstance();
        cal.setTime(getCurrentDeadline());
        showDatePicker(cal);
    }

    //properties:

    private void setCurrentDeadline(Date deadline) {
        currentDeadline = deadline;
    }
    private Date getCurrentDeadline() {
        return currentDeadline;
        //return dateFormat.parse(passedDebtor.getDeadline());
    }


    public Debtor getActivityDebtor() {
        return refDebtor;
    }

    void setActivityDebtor(Debtor d) {
        refDebtor = d;
    }


    /*Pop-up Components*/
    private void showDatePicker(Calendar initialCal) {

        //initial date displayed in DatePicker:
        int iYear = initialCal.get(Calendar.YEAR);
        int iMonth = initialCal.get(Calendar.MONTH);
        int iDay = initialCal.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpDialog = new DatePickerDialog(ManageDebtorActivity.this, null, iYear, iMonth, iDay);

        dpDialog.setOnDateSetListener((view, year, month, dayOfMonth) -> {

            //If deadline is same day, bring-up a TimePicker.
            //get the selected date and store it in a Calendar object for comparison
            Calendar calNow = Calendar.getInstance();
            Calendar selectedCalendar = new GregorianCalendar();
            selectedCalendar.set(Calendar.YEAR, year);
            selectedCalendar.set(Calendar.MONTH, month);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            Log.i(Statics.LOG_TAG_MAIN, "comparing " + selectedCalendar.getTime() + " and " + calNow.getTime());
            int calendarComparison = selectedCalendar.compareTo(calNow);

            if (calendarComparison > 0) {
                txtDeadline.setText(DATE_FORMAT.format(selectedCalendar.getTime()));
            } else if (calendarComparison == 0) {
                //selected date is same day, bring-up a time-picker dialog:
                showTimePicker(selectedCalendar);


            } else {
                showErrorDateSelected(initialCal);
            }
        });

        ActivityUtil.showAlertDialog(ManageDebtorActivity.this, dpDialog, "date_picker_dialog");


/*        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            //If deadline is same day, bring-up a TimePicker.

            //get the selected date and store it in a Calendar object for comparison
            Calendar selectedCalendar = new GregorianCalendar();
            selectedCalendar.set(Calendar.YEAR, year);
            selectedCalendar.set(Calendar.MONTH, month);
            selectedCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            int calendarComparison = selectedCalendar.compareTo(Calendar.getInstance());
            if (calendarComparison > 0) {
                txtDeadline.setText(DATE_FORMAT.format(selectedCalendar.getTime()));
            } else if (calendarComparison == 0) {
                //selected date is same day
                ManageDebtorActivity.this.showTimePicker(selectedCalendar);
            } else {
                ManageDebtorActivity.this.showErrorDateSelected();
            }
        };*/

    }

    public void showTimePicker(Calendar selectedCal) {

        Calendar cal = selectedCal;
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);

        TimePickerDialog.OnTimeSetListener timeSetListener = (timePicker, i, i1) -> {
            //selected time should be greater than than the args passed
            if (i >= hour && i1 > minute) {
                cal.set(Calendar.HOUR_OF_DAY, i);
                cal.set(Calendar.MINUTE, i1);
                txtDeadline.setText(DATE_FORMAT.format(cal.getTime()));
                Log.i(Statics.LOG_TAG_MAIN, "time selected!");
            } else {
                showErrorTimeSelected(selectedCal);
            }
        };

        //where the time is initially at:
        TimePickerDialog timePickerDialog = new TimePickerDialog(ManageDebtorActivity.this, timeSetListener, hour, minute, false);
        ActivityUtil.showAlertDialog(ManageDebtorActivity.this, timePickerDialog, "time_picker_dialog");
    }


    public void showFillFieldsDialog() {
        String message = "Please fill-in all fields";
        AlertDialog.Builder dialog = ActivityUtil.buildSimpleOkDialog(ManageDebtorActivity.this, null, message, null, null );
        ActivityUtil.showAlertDialog(ManageDebtorActivity.this, dialog.create(), "fill_fields_dialog");
    }


    public void showSTRDialog(String message, Class cls) {
        //show status then redirect  dialog
        DialogInterface.OnClickListener posListener = ((dialog, which) -> {
            Intent intent = new Intent(ManageDebtorActivity.this, cls);
            startActivity(intent);
            finish();
        });
        AlertDialog.Builder strDialog = ActivityUtil.buildSimpleOkDialog(ManageDebtorActivity.this, null, message, null, posListener);
        ActivityUtil.showAlertDialog(ManageDebtorActivity.this, strDialog.create(), "str_dialog");
    }

    public void showErrorDateSelected(Calendar initialCal) {
        String message = "Deadline should be the date today or after";
        AlertDialog.OnClickListener posListener = (dialog, which) -> {
            showDatePicker(initialCal);
        };
        AlertDialog.Builder deadlineErrDialog = ActivityUtil.buildSimpleOkDialog(ManageDebtorActivity.this, null, message, null, posListener);
        ActivityUtil.showAlertDialog(ManageDebtorActivity.this, deadlineErrDialog.create(), "date_error_dialog");


    }

    public void showErrorTimeSelected(Calendar selectedCal) {
        //show an error dialog:
        String message = "Select a valid time beyond this moment";
        AlertDialog.OnClickListener posListener = (dialog, which) -> {
            showTimePicker(selectedCal);
        };
        AlertDialog.Builder errorDialog = ActivityUtil.buildSimpleOkDialog(ManageDebtorActivity.this, null, message, null, posListener);
        ActivityUtil.showAlertDialog(ManageDebtorActivity.this, errorDialog.create(), "error_dialog");

    }


}
