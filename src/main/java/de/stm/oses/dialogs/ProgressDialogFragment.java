package de.stm.oses.dialogs;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import com.google.firebase.messaging.RemoteMessage;


public class ProgressDialogFragment extends DialogFragment {

    private String mTitle;
    private String mMessage;
    private int mStyle;
    private String mProgressNumberFormat = "";

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
        dialog.setCanceledOnTouchOutside(false);
        dialog.setProgressStyle(mStyle);
        dialog.setProgressNumberFormat("");
        dialog.setIndeterminate(true);

        if (savedInstanceState != null) {
            dialog.setProgressNumberFormat(savedInstanceState.getString("numberFormat"));
            dialog.setMax(savedInstanceState.getInt("max"));
            dialog.setProgress(savedInstanceState.getInt("progress"));
            dialog.setIndeterminate(savedInstanceState.getBoolean("indeterminate"));
            dialog.setTitle(savedInstanceState.getString("title"));
            dialog.setMessage(savedInstanceState.getString("message"));
        }
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
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("numberFormat", mProgressNumberFormat);
        outState.putInt("max", getDialog().getMax());
        outState.putInt("progress", getDialog().getProgress());
        outState.putBoolean("indeterminate", getDialog().isIndeterminate());
        outState.putString("title", mTitle);
        outState.putString("message", mMessage);
        super.onSaveInstanceState(outState);
    }

    public void setProgressNumberFormat(String format) {
        mProgressNumberFormat = format;
        getDialog().setProgressNumberFormat(mProgressNumberFormat);
    }

    public void setMessage(String message) {
        mMessage = message;
        getDialog().setMessage(message);
    }

    public void setTitle(String title) {
        mTitle = title;
        getDialog().setTitle(title);
    }

    @Override
    public ProgressDialog getDialog() {
        return ((ProgressDialog) super.getDialog());
    }

}
