/*
 * Copyright (c) 2012, Converger Co.,ltd..
 * All rights reserved.
 */

package com.xxworkshop.ui;

import android.content.Context;

/**
 * Created by brochexu on 6/19/14.
 */
public class DatePickerDialog extends android.app.DatePickerDialog {
    public DatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, callBack, year, monthOfYear, dayOfMonth);
    }

    public DatePickerDialog(Context context, int theme, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context, theme, callBack, year, monthOfYear, dayOfMonth);
    }

    @Override
    protected void onStop() {
//        super.onStop();
    }
}
