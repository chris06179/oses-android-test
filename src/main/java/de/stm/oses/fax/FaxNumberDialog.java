package de.stm.oses.fax;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import de.stm.oses.R;


public class FaxNumberDialog extends DialogFragment {

    public static FaxNumberDialog newInstance() {
        return new FaxNumberDialog();
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.fax_dialog_number, null);

        final AlertDialog faxDialog = new AlertDialog.Builder(getActivity())
                .setTitle("Faxziel eingeben")
                .setMessage("Bitte gib eine Faxnummer im folgenden Format ein: +49123456789")
                .setView(dialogLayout)
                .setPositiveButton("Senden", null)
                .setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // Do nothing.
                    }
                }).create();
        faxDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                faxDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final EditText numberView = ((EditText) dialogLayout.findViewById(R.id.fax_dialog_number));
                        String number;
                        number = numberView.getText().toString();

                        if (number.startsWith("+49") && !number.startsWith("+490")) {
                            ((FaxActivity) getActivity()).DoSend(number);
                            faxDialog.dismiss();
                        } else {
                            numberView.setError("Keine gültige Faxnummer im Format +49...!");
                            numberView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    numberView.setError(null);
                                }
                            });
                        }

                    }
                });
            }
        });
        return faxDialog;
    }
}
