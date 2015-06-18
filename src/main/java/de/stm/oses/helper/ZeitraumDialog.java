/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.stm.oses.helper;

import android.support.v7.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

import java.lang.reflect.Field;

import de.stm.oses.R;

/**
 * A simple dialog containing an {@link android.widget.DatePicker}.
 * <p/>
 * <p>See the <a href="{@docRoot}guide/topics/ui/controls/pickers.html">Pickers</a>
 * guide.</p>
 */
public class ZeitraumDialog extends AlertDialog implements OnClickListener,
        OnDateChangedListener {

    private static final String YEAR = "year";
    private static final String MONTH = "month";
    private static final String DAY = "day";

    private boolean hideMonth = false;

    private final DatePicker mDatePicker;
    private final OnDateSetListener mCallBack;

    /**
     * The callback used to indicate the user is done filling in the date.
     */
    public interface OnDateSetListener {

        /**
         * @param view        The view associated with this listener.
         * @param year        The year that was set.
         * @param monthOfYear The month that was set (0-11) for compatibility
         *                    with {@link java.util.Calendar}.
         * @param dayOfMonth  The day of the month that was set.
         */
        void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

    /**
     * @param context     The context the dialog is to run in.
     * @param callBack    How the parent is notified that the date is set.
     * @param year        The initial year of the dialog.
     * @param monthOfYear The initial month of the dialog.
     * @param dayOfMonth  The initial day of the dialog.
     */
    public ZeitraumDialog(Context context,
                          OnDateSetListener callBack,
                          int year,
                          int monthOfYear,
                          int dayOfMonth, boolean hideMonth) {
        this(context, 0, callBack, year, monthOfYear, dayOfMonth, hideMonth);
    }

    /**
     * @param context     The context the dialog is to run in.
     * @param theme       the theme to apply to this dialog
     * @param callBack    How the parent is notified that the date is set.
     * @param year        The initial year of the dialog.
     * @param monthOfYear The initial month of the dialog.
     * @param dayOfMonth  The initial day of the dialog.
     */
    public ZeitraumDialog(Context context,
                          int theme,
                          OnDateSetListener callBack,
                          int year,
                          int monthOfYear,
                          int dayOfMonth, boolean hideMonth) {
        super(context, theme);

        mCallBack = callBack;

        this.hideMonth = hideMonth;

        Context themeContext = getContext();
        setTitle("Zeitraum auswählen");
        setButton(BUTTON_POSITIVE, "Fertig", this);
        setIcon(0);

        LayoutInflater inflater =
                (LayoutInflater) themeContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.date_picker_dialog, null);
        setView(view);
        mDatePicker = (DatePicker) view.findViewById(R.id.datePicker);
        mDatePicker.init(year, monthOfYear, dayOfMonth, this);

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            findAndToggleResource(mDatePicker, "day", View.GONE);

        } else
            findAndToggleField(mDatePicker, "mDaySpinner", View.GONE);

    }

    @Override
    public void show() {
        if (hideMonth) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                findAndToggleResource(mDatePicker, "month", View.GONE);
            } else
                findAndToggleField(mDatePicker, "mMonthSpinner", View.GONE);
        }

        super.show();
    }

    public void onClick(DialogInterface dialog, int which) {
        tryNotifyDateSet();
    }

    public void onDateChanged(DatePicker view, int year,
                              int month, int day) {
        mDatePicker.init(year, month, day, this);
        //updateTitle(year, month, day);
    }

    /**
     * Gets the {@link DatePicker} contained in this dialog.
     *
     * @return The calendar view.
     */
    public DatePicker getDatePicker() {
        return mDatePicker;
    }

    private void tryNotifyDateSet() {
        if (mCallBack != null) {
            mDatePicker.clearFocus();
            mCallBack.onDateSet(mDatePicker, mDatePicker.getYear(),
                    mDatePicker.getMonth(), mDatePicker.getDayOfMonth());
        }
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(YEAR, mDatePicker.getYear());
        state.putInt(MONTH, mDatePicker.getMonth());
        state.putInt(DAY, mDatePicker.getDayOfMonth());
        return state;
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int year = savedInstanceState.getInt(YEAR);
        int month = savedInstanceState.getInt(MONTH);
        int day = savedInstanceState.getInt(DAY);
        mDatePicker.init(year, month, day, this);
    }

    /**
     * find a member field by given name and hide it
     */
    private void findAndToggleField(DatePicker datepicker, String name, int visibility) {
        try {
            Field field = DatePicker.class.getDeclaredField(name);
            field.setAccessible(true);
            View fieldInstance = (View) field.get(datepicker);
            fieldInstance.setVisibility(visibility);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void findAndToggleResource(DatePicker datepicker, String name, int visibility) {
        int SpinnerId = Resources.getSystem().getIdentifier(name, "id", "android");
        if (SpinnerId != 0) {
            View Spinner = datepicker.findViewById(SpinnerId);
            if (Spinner != null) {
                Spinner.setVisibility(visibility);
            }
        }
    }
}
