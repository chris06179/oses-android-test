package de.stm.oses.dialogs;


import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.io.Serializable;


public class ZeitraumDialogFragment extends DialogFragment {

    private OnDateSetListener mListener;

    private int selectedYear;
    private int selectedMonth;
    private int selectedDay;

    private boolean hideMonth = false;

    public interface OnDateSetListener extends Serializable {
        void onDateSet(int year, int monthOfYear, int dayOfMonth);
    }

    public void setHideMonth(boolean hideMonth) {
        this.hideMonth = hideMonth;
    }

    public static ZeitraumDialogFragment newInstance(OnDateSetListener listener, int year, int monthOfYear,
                                                     int dayOfMonth) {
        ZeitraumDialogFragment f = new ZeitraumDialogFragment();
        f.selectedYear = year;
        f.selectedMonth = monthOfYear;
        f.selectedDay = dayOfMonth;
        f.mListener = listener;

        return f;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("listener", mListener);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null)
            mListener = (OnDateSetListener) savedInstanceState.getSerializable("listener");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new ZeitraumDialog(getActivity(), new ZeitraumDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                if (mListener != null)
                    mListener.onDateSet(year, monthOfYear, dayOfMonth);
            }
        }, selectedYear, selectedMonth, selectedDay, hideMonth);


    }
}
