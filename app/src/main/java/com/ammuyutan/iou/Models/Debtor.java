package com.ammuyutan.iou.Models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by YeahBaby on 8/31/2016.
 */
public class Debtor implements  Parcelable {

    private int id;//Auto-increment column
    private String name;
    private String balance;
    private String deadline;
    private String phoneNo;
    private String email;
    private byte[] image;

    public Debtor(String name, String balance, String deadline, String phoneNo, String email, byte[] image) {
        this.name = name;
        this.balance = balance;
        this.deadline = deadline;
        this.phoneNo = phoneNo;
        this.email = email;
        this.image = image;
    }
    public Debtor(int id,String name, String balance, String deadline, String phoneNo, String email, byte[] image) {
        this.id = id;
        this.name = name;
        this.balance = balance;
        this.deadline = deadline;
        this.phoneNo = phoneNo;
        this.email = email;
        this.image = image;
    }

    public Debtor(){};

    //Parcelable interface implementation
    public void writeToParcel(Parcel out, int flags){
        out.writeInt(id);
        out.writeString(name);
        out.writeString(balance);
        out.writeString(deadline);
        out.writeString(phoneNo);
        out.writeString(email);
        out.writeByteArray(image);
    }
    public static final Parcelable.Creator<Debtor> CREATOR = new Creator<Debtor>() {
        @Override
        public Debtor createFromParcel(Parcel source) {
            return new Debtor(source);
        }

        @Override
        public Debtor[] newArray(int size) {
            return new Debtor[size];
        }
    };
    public int describeContents(){
        return 0;
    }
    protected Debtor(Parcel in){
        id = in.readInt();
        name = in.readString();
        balance = in.readString();
        deadline = in.readString();
        phoneNo = in.readString();
        email = in.readString();
        image = in.createByteArray();
    }

    //common getter and setter methods
    public int getId(){return id;}
    public void setId(int id){
        this.id = id;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBalance() {
        return balance;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }

    public String getDeadline() {
        return deadline;
    }

    public void setDeadline(String deadline) {
        this.deadline = deadline;
    }

    public String getPhoneNo() {
        return phoneNo;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public byte[] getImage() {
        return image;
    }

    public void setImage(byte[] image) {
        this.image = image;
    }

}
