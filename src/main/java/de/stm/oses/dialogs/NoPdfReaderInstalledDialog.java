package de.stm.oses.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import de.stm.oses.R;

public class NoPdfReaderInstalledDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setTitle(R.string.no_pdf_reader_title);
        builder.setMessage(R.string.no_pdf_reader_message)
                .setPositiveButton("Play Store öffnen", (dialog, id) -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("market://details?id=com.adobe.reader"));
                    startActivity(intent);
                })
                .setNegativeButton("Schließen", (dialog, id) -> {

                });

        return builder.create();
    }
}
