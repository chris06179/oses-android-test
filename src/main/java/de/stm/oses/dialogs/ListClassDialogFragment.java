package de.stm.oses.dialogs;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import de.stm.oses.helper.ListAdapter;


public class ListClassDialogFragment extends DialogFragment {

    private OnItemClickListener mListener;
    private ListAdapter adapter;
    private String title;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }


    public static ListClassDialogFragment newInstance(String title, ListAdapter adapter, OnItemClickListener listener) {
        ListClassDialogFragment f = new ListClassDialogFragment();
        f.mListener = listener;
        f.adapter = adapter;
        f.title = title;
        f.setRetainInstance(true);

        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        return new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mListener != null)
                            mListener.onItemClick(i);
                    }
                })
                .create();


    }
}
