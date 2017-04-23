package de.stm.oses.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import de.stm.oses.R;


public class FaxProtokollDetailDialogFragment extends DialogFragment {

    public static FaxProtokollDetailDialogFragment newInstance(Bundle bundle) {
        FaxProtokollDetailDialogFragment f = new FaxProtokollDetailDialogFragment();
        f.setArguments(bundle);
        return f;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.fax_protokoll_item_detail, null);

        ((TextView) dialogLayout.findViewById(R.id.fax_detail_id)).setText(getArguments().getString("id"));
        ((TextView) dialogLayout.findViewById(R.id.fax_detail_doc)).setText(getArguments().getString("doc"));
        ((TextView) dialogLayout.findViewById(R.id.fax_detail_date)).setText(getArguments().getString("date"));
        ((TextView) dialogLayout.findViewById(R.id.fax_detail_number)).setText(getArguments().getString("number"));
        ((TextView) dialogLayout.findViewById(R.id.fax_detail_ort)).setText(getArguments().getString("ort"));
        ((TextView) dialogLayout.findViewById(R.id.fax_detail_status)).setText(getArguments().getString("status"));
        ((TextView) dialogLayout.findViewById(R.id.fax_detail_statustext)).setText(getArguments().getString("statustext"));
        ((TextView) dialogLayout.findViewById(R.id.fax_detail_completion)).setText(getArguments().getString("completion"));
        ((TextView) dialogLayout.findViewById(R.id.fax_detail_units)).setText(getArguments().getString("units"));
        ((TextView) dialogLayout.findViewById(R.id.fax_detail_costs)).setText(getArguments().getString("costs"));
        ((TextView) dialogLayout.findViewById(R.id.fax_detail_sum)).setText(getArguments().getString("sum"));



        return new AlertDialog.Builder(getActivity())
                .setTitle("Faxdetails")
                .setView(dialogLayout)
                .setPositiveButton("Schließen", null)
                .create();
    }


}
