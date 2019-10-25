package com.ammuyutan.iou.Models;

import android.os.Parcel;
import android.os.Parcelable;

public class YesNoDialogViewModel implements Parcelable {
    String simpleTitle, message, yesText, noText, neutralText;

    public YesNoDialogViewModel(String simpleTitle, String message, String yesText, String noText, String neutralText) {
        this.simpleTitle = simpleTitle;
        this.message = message;
        this.yesText = yesText;
        this.noText = noText;
        this.neutralText = neutralText;
    }

    public YesNoDialogViewModel() {
    }

    //Parcelable Implementation:
    protected YesNoDialogViewModel(Parcel in) {
        simpleTitle = in.readString();
        message = in.readString();
        yesText = in.readString();
        noText = in.readString();
        neutralText = in.readString();
    }

    public static final Creator<YesNoDialogViewModel> CREATOR = new Creator<YesNoDialogViewModel>() {
        @Override
        public YesNoDialogViewModel createFromParcel(Parcel in) {
            return new YesNoDialogViewModel(in);
        }

        @Override
        public YesNoDialogViewModel[] newArray(int size) {
            return new YesNoDialogViewModel[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(simpleTitle);
        dest.writeString(message);
        dest.writeString(yesText);
        dest.writeString(noText);
        dest.writeString(neutralText);
    }

    //common Getters and Setters
    public String getSimpleTitle() {
        return simpleTitle;
    }

    public void setSimpleTitle(String simpleTitle) {
        this.simpleTitle = simpleTitle;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getYesText() {
        return yesText;
    }

    public void setYesText(String yesText) {
        this.yesText = yesText;
    }

    public String getNoText() {
        return noText;
    }

    public void setNoText(String noText) {
        this.noText = noText;
    }

    public String getNeutralText() {
        return neutralText;
    }

    public void setNeutralText(String neutralText) {
        this.neutralText = neutralText;
    }


}

