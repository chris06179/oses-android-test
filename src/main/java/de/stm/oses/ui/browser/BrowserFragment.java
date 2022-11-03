package de.stm.oses.ui.browser;

import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.analytics.FirebaseAnalytics;

import java.io.File;

import de.stm.oses.R;
import de.stm.oses.dialogs.NoPdfReaderInstalledDialog;
import de.stm.oses.helper.FileDownload;
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
                () -> {
                    if (engine.getScrollY() == 0)
                        mRefreshLayout.setEnabled(true);
                    else
                        mRefreshLayout.setEnabled(false);

                });

    }

    @Override
    public void onStop() {
        mRefreshLayout.getViewTreeObserver().removeOnScrollChangedListener(mOnScrollChangedListener);
        super.onStop();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mRefreshLayout = requireActivity().findViewById(R.id.refreshlayout);
        mRefreshLayout.setColorSchemeResources(R.color.oses_green, R.color.oses_green_dark, R.color.oses_green, R.color.oses_green_dark);
        mRefreshLayout.setSize(SwipeRefreshLayout.LARGE);


        mRefreshLayout.setOnRefreshListener(() -> engine.loadUrl("https://oses.mobi/api.php?request=" + request + "&session=" + OSES.getSession().getIdentifier()));


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        OSES = new OSESBase(requireContext());

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(requireActivity());
    }

    @Override
    public void onCreateOptionsMenu(
            @NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.browser_menu, menu);


    }

    // Called when the activity is first created.
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.browser, container, false);

    }

    public void setRequest(String request) {
        setBrowserShown(false);
        setRequestInternal(request);
    }

    private void setRequestInternal(String newRequest) {

        request = newRequest;

        ActionBar bar = ((AppCompatActivity) requireActivity()).getSupportActionBar();
        assert bar != null;

        if (request.equals("aktuell"))
            bar.setTitle("Aktuelles");

        if (request.equals("impressum"))
            bar.setTitle("Impressum");

        if (request.equals("anb"))
            bar.setTitle("Nutzungsbedingungen");

        if (request.equals("spenden"))
            bar.setTitle("Spenden");

        if (request.equals("hilfe"))
            bar.setTitle("Hilfe");

        if (request.equals("datenschutz"))
            bar.setTitle("Datenschutzerklärung");


        Bundle extra = new Bundle();
        extra.putString("request", request);
        mFirebaseAnalytics.logEvent("OSES_view_webpage", extra);

        engine.loadUrl("https://oses.mobi/api.php?request=" + request + "&session=" + OSES.getSession().getIdentifier());

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // WebView definieren
        engine = requireActivity().findViewById(R.id.browser_view);

        // WEbViewClient festlegen       			
        engine.setWebViewClient(new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {

                if (getActivity() == null)
                    return;

                View error = getActivity().findViewById(R.id.error_container);
                if (error != null && error.getVisibility() == View.VISIBLE) {
                    error.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
                    error.setVisibility(View.GONE);
                }

            }

            // Dialog nach dem Laden verstecken
            @Override
            public void onPageFinished(WebView view, String url) {

                super.onPageFinished(view, url);

                if (getActivity() == null || mRefreshLayout == null)
                    return;

                mRefreshLayout.setRefreshing(false);

                if (errorCode == 0)
                    setBrowserShown(true);
                else {
                    View error = getActivity().findViewById(R.id.error_container);
                    if (error != null) {
                        error.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
                        error.setVisibility(View.VISIBLE);
                    }
                }

            }

            @Override
            public void onReceivedError(WebView view, int ierrorCode, String description, String failingUrl) {
                errorCode = ierrorCode;
                super.onReceivedError(view, errorCode, description, failingUrl);
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public void onReceivedError(WebView view, WebResourceRequest req, WebResourceError rerr) {
                errorCode = rerr.getErrorCode();
                super.onReceivedError(view, req, rerr);
            }

            @TargetApi(android.os.Build.VERSION_CODES.M)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                String host = request.getUrl().getHost();
                if (host != null && (host.contains("paypal.com") || host.contains("wa.me") || host.contains("youtube.com"))) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, request.getUrl()));
                    return true;
                } else if (request.getUrl().toString().equals("https://oses.mobi/system.php?action=hinweise")) {
                    DoDokumentation();
                    return true;
                } else {
                    return false;
                }

            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                if (url != null && (url.startsWith("https://www.paypal.com") || url.startsWith("https://wa.me") || url.startsWith("https://www.youtube.com"))) {
                    view.getContext().startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                    return true;
                } else if (url != null && url.equals("https://oses.mobi/system.php?action=hinweise")) {
                    DoDokumentation();
                    return true;
                } else {
                    return false;
                }
            }
        });

        engine.setScrollBarStyle(View.SCROLLBARS_INSIDE_OVERLAY);
        setRequestInternal(getTag());

    }


    private void setBrowserShown(boolean show) {

        if (getActivity() == null)
            return;

        LinearLayout progress = getActivity().findViewById(R.id.progress_container_id);

        if (progress == null)
            return;

        if (show)
            if (progress.getVisibility() == View.GONE)
                return;

        if (show) {
            progress.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
            progress.setVisibility(View.GONE);
        } else {
            progress.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
            progress.setVisibility(View.VISIBLE);

        }

    }

    private void DoDokumentation() {
        FileDownload download = new FileDownload(requireContext());
        download.setTitle("Dokumentation");
        download.setMessage("Die Dokumentation wird heruntergeladen, dieser Vorgang kann einen Moment dauern...");
        download.setURL("https://oses.mobi/docs/oses_nutzungshinweise.pdf");
        download.setLocalDirectory("/Dokumente/");
        download.setCancelable(true);
        download.setOnDownloadFinishedListener(new FileDownload.OnDownloadFinishedListener() {
            @Override
            public void onDownloadFinished(File file) {
                Uri fileUri = FileProvider.getUriForFile(requireContext(), "de.stm.oses.FileProvider", file);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(fileUri, "application/pdf");
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION + Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                // Falls keine entsprechende Activity existiert (kein PDF-Reader installiert)
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    new NoPdfReaderInstalledDialog().show(getChildFragmentManager(), "no_pdf_dialog");
                }
            }

            @Override
            public void onTextReceived(String res) {

            }

            @Override
            public void onException(Exception e) {

            }

            @Override
            public void onUnknownStatus(int status) {

            }
        });

        download.execute();
    }


}
	
