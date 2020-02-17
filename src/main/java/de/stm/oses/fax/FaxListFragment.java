package de.stm.oses.fax;


import android.os.Bundle;

import androidx.fragment.app.Fragment;

public class FaxListFragment extends Fragment {

    private FaxAdapter adapter;

    public FaxListFragment () {
        // empty constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Retain this fragment across configuration changes.
        setRetainInstance(true);
    }

    public FaxAdapter getAdapter() {
        return adapter;
    }

    public void setAdapter(FaxAdapter adapter) {
        this.adapter = adapter;
    }
}