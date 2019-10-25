package com.ammuyutan.iou.Controllers;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;

import com.ammuyutan.iou.Util.AndroidDatabaseManager;
import com.ammuyutan.iou.CustomAdapter;
import com.ammuyutan.iou.DebtorDAO;
import com.ammuyutan.iou.DebtorDAOLocalCache;
import com.ammuyutan.iou.Models.Debtor;
import com.ammuyutan.iou.Util.Statics;
import com.ammuyutan.iou.Util.ActivityUtil;
import com.ammuyutan.iou.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class ViewDebtorsActivity extends AppCompatActivity {

    ListView listDebtors;
    CustomAdapter customAdapter;
    SearchView txtSearch;
    TextView appHeader;
    FloatingActionButton fabAddDebtor;
    ImageButton iconUser;


    DebtorDAO debtorDao;
    static SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_debtors);
        init();
    }
    @Override
    protected void onResume() {
        super.onResume();
       refreshList(debtorDao.getDebtors());
    }

    private void init(){
        debtorDao = new DebtorDAOLocalCache(getApplicationContext());
        listDebtors = findViewById(R.id.listDebtors);
        appHeader = findViewById(R.id.appTitle);
        //make a hamburger fragment from it
        iconUser  = findViewById(R.id.iconUser);
        appHeader.setText(Html.fromHtml("I<font color=red>O</font><font color=#D96B0B>U</font>", Html.FROM_HTML_MODE_LEGACY));
        txtSearch = findViewById(R.id.txtSearch);
        txtSearch.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                refreshList(debtorDao.searchDebtors(query));
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                refreshList(debtorDao.searchDebtors(newText));
                return false;
            }
        });

        fabAddDebtor = findViewById(R.id.fabAddDebtor);
        fabAddDebtor.setOnClickListener(v -> {
            Intent addDebtorActivity = new Intent(getApplicationContext(), ManageDebtorActivity.class);
            startActivity(addDebtorActivity);
        });

        sharedPref = ViewDebtorsActivity.this.getPreferences(Context.MODE_PRIVATE);
        checkAppUsername();


        customAdapter = new CustomAdapter(this,  debtorDao.getDebtors(), null);
        listDebtors.setAdapter(customAdapter);

        listDebtors.setOnItemClickListener((adapterView, view, i, l) -> {

            int  debtorId =  ((Debtor)adapterView.getItemAtPosition(i)).getId();
            adapterView.setSelection(i);
            Intent intent = new Intent(ViewDebtorsActivity.this, ManageDebtorActivity.class);
            intent.putExtra(Statics.DEBTOR_ID_TAG, debtorId);
            startActivity(intent);
        });

        //for the abstract method onCreateContextMenu that registers this view's id for a context menu service
        registerForContextMenu(listDebtors);

    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if(v.getId() == R.id.listDebtors) {
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
        }
    }



    void checkAppUsername(){
        if(getUserName().equals(Statics.NAME_DEFAULT)){
            showEnterUsernameDialog(null);
            iconUser.setImageResource(R.drawable.ic_person_40dp);
        }
    }
    public static String getUserName(){
        return sharedPref.getString(Statics.USER_NAME, Statics.NAME_DEFAULT);
    }



    public void showEnterUsernameDialog(View v){

        View customView = getLayoutInflater().inflate(R.layout.dialog_enter_name, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(ViewDebtorsActivity.this);
        builder.setView(customView)
                .setCancelable(true);

        Dialog enterUsernameDialog = builder.create();
        EditText txtName = customView.findViewById(R.id.et_dialogEnterName_name);
        Button btnSave = customView.findViewById(R.id.btn_dialogEnterName_name);
        btnSave.setOnClickListener((view)->{

            writeToSharedPref(txtName.getText().toString());

            //show another dialog as a confirmation message:
            ActivityUtil.showAlertDialog(ViewDebtorsActivity.this,
                    ActivityUtil.buildSimpleOkDialog(ViewDebtorsActivity.this,null,"Name set!",null, null).create(),
                    "success_name_set_dialog");


            enterUsernameDialog.dismiss();
        });

        String username = getUserName();
        if(username!=null){
            txtName.setText(username);
        }
        ActivityUtil.showAlertDialog(ViewDebtorsActivity.this,enterUsernameDialog,"dialog_enter_username");

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Debtor debtor = debtorDao.getDebtors().get(info.position);
        switch (item.getItemId()) {
            case R.id.resolveDebtor:

                String message = "Resolve the "+ getString(R.string.peso_sign_space) + debtor.getBalance() + " debt of " + debtor.getName();
                String successMessage = debtor.getName()+"'s debt is settled.";
                DialogInterface.OnClickListener positiveListener = ((dialog,which)->{
                    debtorDao.deleteDebtor(debtor.getId());
                    ActivityUtil.showAlertDialog(this,
                            ActivityUtil.buildSimpleOkDialog(ViewDebtorsActivity.this, null, successMessage,null,null).create(),
                            "resolved_dialog");
                    refreshList(debtorDao.getDebtors());
                });
                DialogInterface.OnClickListener negativeListener = ((dialog,which)->{dialog.dismiss();});
                AlertDialog.Builder yesNoDialog = ActivityUtil.buildSimpleYesNoDialog(ViewDebtorsActivity.this,null, message,null,
                        positiveListener,null,negativeListener);
                ActivityUtil.showAlertDialog(ViewDebtorsActivity.this, yesNoDialog.create(),"resolve_debt_dialog");
                return true;
            case R.id.updateBalance:
                ActivityUtil.showUpdateDebtDialog(ViewDebtorsActivity.this, debtor);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    public void refreshList(List<Debtor> dataset){

        //is adapter and listener reassign the best approach here?
        customAdapter = new CustomAdapter(this, dataset, null);
        listDebtors.setAdapter(customAdapter);
        listDebtors.setOnItemClickListener((adapterView, view, i, l) -> {
            int  debtorId =  ((Debtor)adapterView.getItemAtPosition(i)).getId();
            Intent intent = new Intent(ViewDebtorsActivity.this, ManageDebtorActivity.class);
            intent.putExtra(Statics.DEBTOR_ID_TAG, debtorId);
            startActivity(intent);
        });

    }


    public void openADM(View v){
        startActivity(new Intent(getApplicationContext(), AndroidDatabaseManager.class));
    }

    void writeToSharedPref(String name){
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Statics.USER_NAME, name);
        editor.apply();
    }






/*    public void checkAllDebtors(){
        overdueList = new ArrayList<>();
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault());
        String currentDate = dateFormat.format(calendar.getTime());

        Log.i("date", currentDate + "");


        for(int i=0; i<debtorDao.getDebtors().size(); i++){
            if(debtorDao.getDebtors().get(i).getDeadline().compareTo(currentDate) == 1){
                debtorDao.getDebtors().get(i).setName(debtorDao.getDebtors().get(i).getName() + "(Overdue)");
                overdueList.add(debtorDao.getDebtors().get(i));

            }
            else{
                try {

                    if(debtorDao.getDebtors().get(i).getDeadline().equals(currentDate)){
                        debtorDao.getDebtors().get(i).setName(debtorDao.getDebtors().get(i).getName() + "(Overdue)");
                        overdueList.add(debtorDao.getDebtors().get(i));

                    }

                }catch (Exception ex){
                    ex.printStackTrace();
                }
            }
        }


    }*/



}
