package com.ammuyutan.iou.Util;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

public class CurrencyEditText extends AppCompatEditText {
    int selBoundary = Statics.APP_CURRENCY.length();

    public CurrencyEditText(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setText(Statics.APP_CURRENCY);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                //prevent erasing the currency:
                if(length() <= selBoundary-1){
                    //currency length -1 because backspace erases 1 char
                    setText(Statics.APP_CURRENCY);
                }
            }
        };
        addTextChangedListener(textWatcher);
    }




    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {

        if(selStart < selBoundary){
            //put cursor at end of string:
            setSelection(this.length());
        }
        super.onSelectionChanged(selStart, selEnd);
    }

    @Override
    public int length() {
        return getText().toString().length();
    }

    @Override
    public String toString() {
        //
        int startIndex = Statics.APP_CURRENCY.length();
        return getText().toString().substring(startIndex);
    }
}
