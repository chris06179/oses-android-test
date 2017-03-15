package de.stm.oses.helper;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import de.stm.oses.OSESActivity;


public class ProgressDialogFragment extends DialogFragment {

    private String mTitle;
    private String mMessage;
    private int mStyle;

    public static ProgressDialogFragment newInstance(String title, String message, int style) {
        ProgressDialogFragment f = new ProgressDialogFragment();

        Bundle args = new Bundle();
        args.putString("title", title);
        args.putString("message", message);
        args.putInt("style", style);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTitle = getArguments().getString("title");
        mMessage = getArguments().getString("message");
        mStyle = getArguments().getInt("style");
        setRetainInstance(true);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog dialog;
        dialog = new ProgressDialog(getActivity(), getTheme());
        dialog.setTitle(mTitle);
        dialog.setMessage(mMessage);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(mStyle);
        dialog.setIndeterminate(true);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public ProgressDialog getDialog() {
        return ((ProgressDialog) super.getDialog());
    }

}
