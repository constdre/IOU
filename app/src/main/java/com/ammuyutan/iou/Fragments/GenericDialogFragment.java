package com.ammuyutan.iou.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.ammuyutan.iou.Models.ParcelableDialog;
import com.ammuyutan.iou.Models.YesNoDialogViewModel;
import com.ammuyutan.iou.Util.Statics;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class GenericDialogFragment extends DialogFragment {

    public static GenericDialogFragment newInstance(Bundle args){
        GenericDialogFragment dialogFragment = new GenericDialogFragment();
        dialogFragment.setArguments(args);
        return dialogFragment;

    }


    @Override
    public void onAttach(Context context) {
//        yesNoActionListener = (YesNoActionListener)context;
        super.onAttach(context);
    }

    @NonNull
    @Override

    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        //Dialog is built from source/passer activity
        Bundle args = getArguments();
        ParcelableDialog dialogWrapper = args.getParcelable(Statics.DIALOG_EXTRA_TAG);


//        Dialog ViewModel approach:
//        YesNoDialogViewModel viewModel = args.getParcelable(Statics.YES_NO_VM_TAG);
//        String message = viewModel.getMessage();
//        String yesText = viewModel.getYesText();
//
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//        builder.setCancelable(false)
//                .setMessage(message)
//                .setPositiveButton(yesText, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        yesNoActionListener.positiveAction(Statics.PURPOSE_SIMPLE_AFFIRMATION);
//                        dialog.dismiss();
//                    }
//                });


        return dialogWrapper.getDialog();
    }

    @Override
    public void onResume() {
        Log.i(Statics.LOG_TAG_MAIN,"dialog created!");

        super.onResume();
    }

    @Override
    public void onDestroyView() {
        Log.i(Statics.LOG_TAG_MAIN,"dialog destroyed!");
        super.onDestroyView();
    }

    public AlertDialog.Builder fillUpAlertDialog(Bundle args, Context context){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(false);

        //retrieve AlertDialog text components:

        YesNoDialogViewModel viewModel = args.getParcelable(Statics.YES_NO_VM_TAG);
        String title  = viewModel.getSimpleTitle();
        String message = viewModel.getMessage();
        String yesText = viewModel.getYesText();
        String noText = viewModel.getNoText();
        String neutralText = viewModel.getNeutralText();

        //build according to availability

        if(title!=null){
            builder.setTitle(title);
        }
        if(message!=null){
            builder.setMessage(message);
        }
        if(yesText != null){
            builder.setPositiveButton(yesText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //yesNoActionListener.positiveAction(Statics.PURPOSE_BASIC_AFFIRMATION);
                    dialog.dismiss();
                }
            });
        }
        if(noText != null){
            builder.setNegativeButton(noText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //yesNoActionListener.negativeAction();
                    dialog.dismiss();
                }
            });
        }
        if(neutralText != null){
            builder.setNeutralButton(neutralText, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //yesNoActionListener.neutralAction();
                    dialog.dismiss();
                }
            });
        }



        return builder;
    }
}
