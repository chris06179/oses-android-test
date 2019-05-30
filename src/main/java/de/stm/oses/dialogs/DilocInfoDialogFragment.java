package de.stm.oses.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import de.stm.oses.R;


public class DilocInfoDialogFragment extends DialogFragment {

    public static DilocInfoDialogFragment newInstance() {
        return new DilocInfoDialogFragment();
    }

    DilocInfoDialogListener mCallback;

    public interface DilocInfoDialogListener {
        void onDilocInfoDialogRequestPermission();
        void onDilocInfoDialogDecline();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mCallback = (DilocInfoDialogListener) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getTargetFragment().toString()
                    + " must implement DilocInfoDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.diloc_dialog, null);

        final AlertDialog dilocInfoDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Synchronisation mit Diloc|Sync (BETA)")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCallback.onDilocInfoDialogRequestPermission();
                    }
                })
                .setNeutralButton("Später", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCallback.onDilocInfoDialogDecline();
                    }
                })
                .setView(dialogLayout)
                .create();


        return dilocInfoDialog;
    }
}
