package de.stm.oses;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.google.firebase.analytics.FirebaseAnalytics;

import de.stm.oses.helper.OSESBase;


public class BrowserFragment extends Fragment {

	private String request;
    private OSESBase OSES;
	private WebView engine;
    private int errorCode = 0;
    private SwipeRefreshLayout mRefreshLayout;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollChangedListener;
    private FirebaseAnalytics mFirebaseAnalytics;

    public BrowserFragment() {
		// Required empty public constructor
	}

    @Override
    public void onStart() {
        super.onStart();

        mRefreshLayout.getViewTreeObserver().addOnScrollChangedListener(mOnScrollChangedListener =
                new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        if (engine.getScrollY() == 0)
                            mRefreshLayout.setEnabled(true);
                        else
                            mRefreshLayout.setEnabled(false);

                    }
                });

    }

    @Override
    public void onStop() {
        mRefreshLayout.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
        super.onStop();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRefreshLayout = (SwipeRefreshLayout) getActivity().findViewById(R.id.refreshlayout);
        mRefreshLayout.setColorSchemeResources(R.color.oses_green, R.color.oses_green_dark, R.color.oses_green, R.color.oses_green_dark);
        mRefreshLayout.setSize(SwipeRefreshLayout.LARGE);


        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                engine.loadUrl("https://oses.mobi/api.php?request="+request+"&session="+OSES.getSession().getIdentifier());
            }
        });


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        OSES = new OSESBase(getActivity());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getActivity());
    }
	
	@Override
	public void onCreateOptionsMenu(
	   Menu menu, MenuInflater inflater) {
	   inflater.inflate(R.menu.browser_menu, menu);  
	   
	  
	}

	// Called when the activity is first created.
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		
		return inflater.inflate(R.layout.browser, container, false);
		
	}
	
	@Override
	public void onActivityCreated (Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        request = getTag();

        if (request.equals("aktuell"))
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Aktuelles");

        if (request.equals("impressum"))
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Impressum");

        if (request.equals("anb"))
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Nutzungsbedingungen");

        Bundle extra = new Bundle();
        extra.putString("request", request);
        mFirebaseAnalytics.logEvent("OSES_view_webpage", extra);
        
     // WebView definieren
        engine = (WebView) getActivity().findViewById(R.id.browser_view);
        
        // WEbViewClient festlegen       			
		engine.setWebViewClient(new WebViewClient() {

            @Override
			public void onPageStarted (WebView view, String url, Bitmap favicon) {

                View error = getActivity().findViewById(R.id.error_container);
                if (error.getVisibility() == View.VISIBLE) {
                    error.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
                    error.setVisibility(View.GONE);
                }
				
			}
			
			// Dialog nach dem Laden verstecken			
			@Override  
		    public void onPageFinished(WebView view, String url) {

                super.onPageFinished(engine, url);

                mRefreshLayout.setRefreshing(false);

                if (errorCode == 0)
                 setBrowserShown(true);
                else {
                    View error = getActivity().findViewById(R.id.error_container);
                    error.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
                    error.setVisibility(View.VISIBLE);
                }
		        
		    }  

			// Fehlermeldung bei Fehlschlag anzeigen
		    @Override
		    public void onReceivedError(WebView view, int ierrorCode, String description, String failingUrl) {

                errorCode = ierrorCode;
                super.onReceivedError(view, errorCode, description, failingUrl);


		    }
	        	
	
		});
		
		engine.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        engine.loadUrl("https://oses.mobi/api.php?request="+request+"&session="+OSES.getSession().getIdentifier());

	}


    private void setBrowserShown(boolean show) {

        LinearLayout progress = (LinearLayout)   getActivity().findViewById(R.id.progress_container_id);

        if (progress == null)
           return;

        if (show)
            if (progress.getVisibility() == View.GONE)
                return;

        if (show)   {
            progress.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
            progress.setVisibility(View.GONE);
        } else {
            progress.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
            progress.setVisibility(View.VISIBLE);

        }

    }


}
	
