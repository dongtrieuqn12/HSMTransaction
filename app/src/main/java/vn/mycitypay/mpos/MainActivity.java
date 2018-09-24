package vn.mycitypay.mpos;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.nfc.NfcAdapter;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import vn.mycitypay.mpos.R;

public class MainActivity extends AppCompatActivity {
    private AlertDialog dialog;
    private int oldOrientation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnTopup).setOnClickListener((v) -> topup());
        findViewById(R.id.btnBalance).setOnClickListener((v) -> balance());
        findViewById(R.id.btnPurchase).setOnClickListener((v) -> purchase());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        enableNfc();

        // avoid re-starting the App and loosing the tag by rotating screen
        oldOrientation = getRequestedOrientation();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
    }

    @Override
    public void onPause() {
        super.onPause();

        // Restore screen mode
        setRequestedOrientation(oldOrientation);
    }

    private void enableNfc() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (!adapter.isEnabled()) {
            if (dialog == null) {
                dialog = new AlertDialog.Builder(this)
                        .setMessage("NFC is required to communicate with a contactless smart card. Do you want to enable NFC now?")
                        .setTitle("Enable NFC")
                        .setPositiveButton(android.R.string.yes, (dialog, id) -> startActivity(new Intent(Settings.ACTION_NFC_SETTINGS)))
                        .setNegativeButton(android.R.string.no, (dialog, id) -> {})
                        .create();
            }
            dialog.show();
        }
    }

    private void topup(){
        startActivity(new Intent(this, TopupActivity.class));
    }

    private void balance(){
        startActivity(new Intent(this, BalanceActivity.class));
    }

    private void purchase(){
        startActivity(new Intent(this, PurchaseActivity.class));
    }
}
