package de.stm.oses.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import de.stm.oses.R;


public class FaxNumberDialogFragment extends DialogFragment {

    public static FaxNumberDialogFragment newInstance() {
        return new FaxNumberDialogFragment();
    }

    FaxNumberDialogListener mCallback;

    public interface FaxNumberDialogListener {
        void onDialogSend(String destination);
        void onDialogCancel();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            mCallback = (FaxNumberDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement FaxNumberDialogListener");
        }

    }

    @NonNull
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
                        mCallback.onDialogCancel();
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
                            mCallback.onDialogSend(number);
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
