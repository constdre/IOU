package com.ammuyutan.iou.Models;

import android.app.Dialog;
import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableDialog implements Parcelable {

    Dialog dialog;


    public ParcelableDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    public ParcelableDialog(){}

    protected ParcelableDialog(Parcel in) {
        dialog = (Dialog)in.readValue(ParcelableDialog.class.getClassLoader());
    }

    public static final Creator<ParcelableDialog> CREATOR = new Creator<ParcelableDialog>() {
        @Override
        public ParcelableDialog createFromParcel(Parcel in) {
            return new ParcelableDialog(in);
        }

        @Override
        public ParcelableDialog[] newArray(int size) {
            return new ParcelableDialog[size];
        }
    };

    public Dialog getDialog() {
        return dialog;
    }

    public void setContentDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeValue(dialog);
    }
}
