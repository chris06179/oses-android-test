package de.stm.oses.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.webkit.URLUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class FileDownload extends AsyncTask<String, Integer, Object> {

    private ProgressDialog mProgressDialog;
    private Context context;
    private String title = "";
    private String message = "";
    private String surl;
    private String localDirectory;
    private String localFilename;
    private boolean isCancelable = false;
    private boolean isCanceld = false;
    private OnDownloadFinishedListener mListener;

    public interface OnDownloadFinishedListener {
        void onDownloadFinished(File file);
        void onTextReceived(String res);
        void onException(Exception e);
        void onUnknownStatus(int status);
    }

    public void setOnDownloadFinishedListener(OnDownloadFinishedListener listener) {
        mListener = listener;
    }

    public FileDownload(Context context) {
        this.context = context;
    }

    public void setTitle(String title) {

        this.title = title;
    }

    public void setMessage(String message) {

        this.message = message;
    }

    public void setLocalDirectory(String localDirectory) {
        this.localDirectory = localDirectory;
    }

    public void setURL(String surl) {
        this.surl = surl;
    }

    public void setLocalFilename(String localFilename) {
        this.localFilename = localFilename;
    }

    public void setCancelable(boolean isCancelable) {
        this.isCancelable = isCancelable;
    }

    protected void onPreExecute() {

        ((AppCompatActivity) context).setRequestedOrientation(context.getResources().getConfiguration().orientation);
        mProgressDialog = new ProgressDialog(context);
        mProgressDialog.setTitle(title);
        mProgressDialog.setMessage(message);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(isCancelable);
        mProgressDialog.setCanceledOnTouchOutside(isCancelable);
        mProgressDialog.setProgressNumberFormat("");

        if (isCancelable) {
            mProgressDialog.setButton(ProgressDialog.BUTTON_NEGATIVE, "Abbrechen", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    isCanceld = true;
                }
            });
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    isCanceld = true;
                }
            });
        }
        mProgressDialog.show();

    }

    protected Object doInBackground(String... sUrl) {

        try {
            URL url = new URL(surl);
            HttpsURLConnection connection = ((HttpsURLConnection) url.openConnection());
            connection.setUseCaches(false);

            int responseCode = connection.getResponseCode();
            // always check HTTP response code first

            switch (responseCode) {
                case 200:
                case 201:
                    // this will be useful so that you can show a typical 0-100% progress bar
                    int fileLength = connection.getContentLength();
                    mProgressDialog.setMax(fileLength / 1024);


                    if (localFilename == null) {

                        String disposition = connection.getHeaderField("Content-Disposition");

                        if (disposition != null) {
                            // extracts file name from header field
                            int index = disposition.indexOf("filename=");
                            if (index > 0) {
                                localFilename = disposition.substring(index + 10,
                                        disposition.length() - 1);
                            }
                        }
                    }

                    if (localFilename == null) {
                        localFilename = URLUtil.guessFileName(surl, null, null);
                    }

                    File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),"/OSES/"+localDirectory+localFilename);

                        file.getParentFile().mkdirs();
                        if (!file.exists())
                            file.createNewFile();
                        else {
                            file.delete();
                            file.createNewFile();
                        }


                    // download the file
                    InputStream input = new BufferedInputStream(url.openStream());
                    OutputStream output = new FileOutputStream(file);

                    byte data[] = new byte[1024];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        total += count;
                        publishProgress((int) total / 1024);
                        output.write(data, 0, count);
                        if (isCanceld) {
                            connection.disconnect();
                            return null;
                        }
                    }

                    output.flush();
                    output.close();
                    input.close();
                    connection.disconnect();
                    return file;
                case 303:
                    BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                        if (isCanceld) {
                            connection.disconnect();
                            return null;
                        }
                    }
                    br.close();
                    connection.disconnect();
                    return sb.toString().trim();
                default:
                    connection.disconnect();
                    return responseCode;
            }

        } catch (Exception e) {
            return e;
        }

    }

    protected void onProgressUpdate(Integer... progress) {

        if (mProgressDialog.isIndeterminate()) {
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setProgressNumberFormat("%1d kB / %2d kB");
        }
        mProgressDialog.setProgress(progress[0]);

    }

    protected void onPostExecute(Object o) {

        ((AppCompatActivity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

        mProgressDialog.dismiss();

        if (o instanceof File) { // ERFOLG
            if (mListener != null)
                mListener.onDownloadFinished((File) o);
        }

        if (o instanceof String) { // Fehlermeldung
            if (mListener != null)
                mListener.onTextReceived((String) o);
        }

        if (o instanceof Integer) { // Unbekannter Status
            if (mListener != null)
                mListener.onUnknownStatus((int) o);
        }

        if (o instanceof Exception) { // Programmfehler
            if (mListener != null)
                mListener.onException((Exception) o);
        }

    }
}