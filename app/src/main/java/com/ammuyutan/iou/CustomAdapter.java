package com.ammuyutan.iou;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ammuyutan.iou.Models.Debtor;
import com.ammuyutan.iou.R;

import java.util.List;

public class CustomAdapter extends BaseAdapter {

    private List<Debtor> debtors;
    private Context context;

    private static LayoutInflater inflater;

    public CustomAdapter(Context context, List<Debtor> debtors) {
        this.context = context;
        this.debtors = debtors;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return debtors.size();
    }

    @Override
    public Object getItem(int i) {
        return debtors.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //position is the index

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_layout, parent, false);
        }

        TextView textName =  convertView.findViewById(R.id.textName);
        TextView textBalance =  convertView.findViewById(R.id.textBalance);
        TextView textDeadline = convertView.findViewById(R.id.textDeadline);
        ImageView imgView = convertView.findViewById(R.id.imageDebtor);

        //convertView.setLayoutParams(new ViewGroup.LayoutParams(1000,150));

        textName.setText(debtors.get(position).getName());
        String balance = context.getResources().getString(R.string.peso_sign_space) + " " + debtors.get(position).getBalance();
        textBalance.setText(balance);
        String onDeadline = "on " + debtors.get(position).getDeadline();
        textDeadline.setText(onDeadline);

        //convert byte[] to Bitmap
        //byte[] byteArray = Base64.decode(Base64.encodeToString(debtors.get(position).getImage(), Base64.DEFAULT), Base64.DEFAULT);
        byte[] byteArray = debtors.get(position).getImage();
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        imgView.setImageBitmap(bitmap);

        return convertView;
    }

}
