package de.stm.oses.dialogs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import de.stm.oses.R;


public class IndexInfoDialog extends DialogFragment {

    public static IndexInfoDialog newInstance() {
        return new IndexInfoDialog();
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        @SuppressLint("InflateParams")
        final View dialogLayout = inflater.inflate(R.layout.index_dialog, null);

        return new AlertDialog.Builder(requireActivity())
                .setTitle("DiLoc|Sync-Index wird erstellt")
                .setView(dialogLayout)
                .setPositiveButton("Schließen", null)
                .create();
    }
}
