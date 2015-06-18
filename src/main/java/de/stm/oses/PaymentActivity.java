package de.stm.oses;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.android.vending.billing.IInAppBillingService;

import java.util.ArrayList;

public class PaymentActivity extends AppCompatActivity implements View.OnClickListener {

    IInAppBillingService mService;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.payment);

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);

        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Zahlungen");

        findViewById(R.id.button2).setOnClickListener(this);
        findViewById(R.id.payment_button2).setOnClickListener(this);
        findViewById(R.id.payment_button3).setOnClickListener(this);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_payment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button2:
                try {
                    Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(), "oses_5euro_guthaben", "inapp", "oses_4987497489hhfjdfhsgfd");

                    PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");

                    startIntentSenderForResult(pendingIntent.getIntentSender(),  1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            case R.id.payment_button3:
                try {
                    int response = mService.consumePurchase(3, getPackageName(), "inapp:de.stm.oses:android.test.purchased");
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.payment_button2:
                try {
                    Bundle ownedItems = mService.getPurchases(3, getPackageName(), "inapp", null);

                    int response = ownedItems.getInt("RESPONSE_CODE");
                    if (response == 0) {
                        ArrayList<String> ownedSkus =
                                ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                        ArrayList<String>  purchaseDataList =
                                ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                        ArrayList<String>  signatureList =
                                ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");
                        String continuationToken =
                                ownedItems.getString("INAPP_CONTINUATION_TOKEN");

                        for (int i = 0; i < purchaseDataList.size(); ++i) {
                            String purchaseData = purchaseDataList.get(i);
                            String signature = signatureList.get(i);
                            String sku = ownedSkus.get(i);

                            getSupportActionBar();

                            // do something with this purchase information
                            // e.g. display the updated list of products owned by user
                        }

                        // if continuationToken != null, call getPurchases again
                        // and pass in the token to retrieve more items
                    }

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
        }
    }
}
