package de.stm.oses.helper;

import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.stm.oses.R;


public class RecyclerFragment extends Fragment {

    protected RecyclerView mRecyclerView;
    protected SwipeRefreshLayout mSwipeRefreshLayout;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recycler_fragment, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.rf_rv);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        mSwipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.rf_swipe_layout);


        return rootView;
    }

    public RecyclerView getListView() {
        return mRecyclerView;
    }

    public RecyclerView.Adapter getListAdapter() {
        return mRecyclerView.getAdapter();
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        mSwipeRefreshLayout.setOnRefreshListener(listener);
    }

    /**
     * Returns whether the {@link SwipeRefreshLayout} is currently
     * refreshing or not.
     *
     * @see SwipeRefreshLayout#isRefreshing()
     */
    public boolean isRefreshing() {
        return mSwipeRefreshLayout.isRefreshing();
    }

    /**
     * Set whether the {@link SwipeRefreshLayout} should be displaying
     * that it is refreshing or not.
     *
     * @see SwipeRefreshLayout#setRefreshing(boolean)
     */
    public void setRefreshing(boolean refreshing) {
        mSwipeRefreshLayout.setRefreshing(refreshing);
    }

    /**
     * Set the color scheme for the {@link SwipeRefreshLayout}.
     *
     * @see SwipeRefreshLayout setColorScheme(int, int, int, int)
     */
    public void setColorScheme(int colorRes1, int colorRes2, int colorRes3, int colorRes4) {
        mSwipeRefreshLayout.setColorSchemeResources(colorRes1, colorRes2, colorRes3, colorRes4);
    }

    /**
     * @return the fragment's {@link SwipeRefreshLayout} widget.
     */
    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }
}
