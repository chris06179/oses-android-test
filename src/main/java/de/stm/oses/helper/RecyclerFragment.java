package de.stm.oses.helper;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import de.stm.oses.R;


public class RecyclerFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private TextView mStatusText;
    private ProgressBar mProgress;
    private ConstraintLayout mConstraint;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.recycler_fragment, container, false);

        mRecyclerView = rootView.findViewById(R.id.recyclerfragment_recycler);
        mSwipeRefreshLayout = rootView.findViewById(R.id.rf_swipe_layout);
        mStatusText = rootView.findViewById(R.id.recyclerfragment_statusText);
        mProgress = rootView.findViewById(R.id.recyclerfragment_progress);
        mConstraint = rootView.findViewById(R.id.main_constraint);

        mConstraint.getLayoutTransition().setAnimateParentHierarchy(false);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));


        return rootView;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }
    public RecyclerView.Adapter getRecyclerAdpater() {
        return mRecyclerView.getAdapter();
    }
    public TextView getStatusText() {
        return mStatusText;
    }
    public ProgressBar getProgressBar() {
        return mProgress;
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
