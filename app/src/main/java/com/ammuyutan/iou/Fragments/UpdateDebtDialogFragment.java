package com.ammuyutan.iou.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.ammuyutan.iou.DebtorDAO;
import com.ammuyutan.iou.DebtorDAOLocalCache;
import com.ammuyutan.iou.Controllers.ManageDebtorActivity;
import com.ammuyutan.iou.Models.Debtor;
import com.ammuyutan.iou.Util.ActivityUtil;
import com.ammuyutan.iou.Util.CurrencyEditText;
import com.ammuyutan.iou.Util.Statics;
import com.ammuyutan.iou.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class UpdateDebtDialogFragment extends DialogFragment {

    private DebtorDAO dao;

    public UpdateDebtDialogFragment() {

    }

    public static UpdateDebtDialogFragment newInstance(Bundle args){
        UpdateDebtDialogFragment dialogFragment  = new UpdateDebtDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        Bundle args = getArguments();
        dao = new DebtorDAOLocalCache(getActivity());

        final View customView = inflater.inflate(R.layout.dialog_update_balance, container);
        //get the UI components:
        final CurrencyEditText txtNewBalance = customView.findViewById(R.id.txtNewBalance);





        final Debtor debtor = args.getParcelable(Statics.PASSED_DEBTOR_TAG);
        TextView txtCurBalance = customView.findViewById(R.id.txtCurBalance);
        txtCurBalance.setText("Php. " + debtor.getBalance());

        Button btnSaveUpdate = customView.findViewById(R.id.btnSaveUpdate);
        btnSaveUpdate.setOnClickListener(view -> {
            String newBalance = txtNewBalance.getText().toString();
            //Database Operation:
            int rowsAffected = dao.updateBalance(newBalance, debtor.getId());
            Log.i(Statics.LOG_TAG_MAIN, "DEBT UPDATE! Rows affected:" + rowsAffected);
            //Check if successful:
            if (rowsAffected == 1) {
                //Show a simple ok dialog - just message and positive action
                String message = debtor.getName() + "'s debt updated to $" + newBalance + ".00";
                //AlertDialog.Builder parcelable wrapper
                AlertDialog.Builder successDialog = ActivityUtil.buildSimpleOkDialog(UpdateDebtDialogFragment.this.getActivity(), null, message, null, null);

                //close this dialog for the simple ok dialog
                if((getActivity().getClass().equals(ManageDebtorActivity.class))){
                    ((ManageDebtorActivity) UpdateDebtDialogFragment.this.getActivity()).updateDebtEditText(newBalance);
                }
                UpdateDebtDialogFragment.this.dismiss();

                //utility method that adds and displays a YesNo Dialog:
                ActivityUtil.showAlertDialog(UpdateDebtDialogFragment.this.getActivity(), successDialog.create(), "debt_updated_dialog");
                Log.i(Statics.LOG_TAG_MAIN, "YesNo Dialog shown");

            }
        });

        Button btnCancel = customView.findViewById(R.id.btnCancelDebtUpdate);
        btnCancel.setOnClickListener(v -> dismiss());


        setCancelable(false);
        return customView;
    }

}
