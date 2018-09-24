package vn.mycitypay.mpos;

import android.content.SharedPreferences;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public abstract class WorkerActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {
    private PosProcessingWorker worker;
    protected TextView currentMessage;
    protected TextView messageLog;

    @Override
    public void onResume() {
        super.onResume();
        enableReaderMode();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopWorker();
        disableReaderMode();
    }

    @Override
    public void onTagDiscovered(Tag tag) {
        // Stop active Worker before starting a new one
        stopWorker();
        // Start new Worker
        NFCReader nfcReader = NFCReader.get(tag, this);
        if (nfcReader != null) {
            startWorker(nfcReader);
        }
    }

    protected byte[] processControlCommand(int controlCode, byte[] params) throws IOException {
        switch (controlCode){
            case Constants.CTRL_CODE_FINISH:
                stopWorker();
                return new byte[0];
            case Constants.CTRL_CODE_SHOW_MESSAGE:
                String message = new String(params, "UTF-8");
                showMessage(message);
                return new byte[0];
            case Constants.CTRL_CODE_GET_FUNCTION_CODE:
                Log.v(Constants.TAG,"hello1");
                return processGetFunctionCode();
            case Constants.CTRL_CODE_GET_FUNCTION_PARAMS:
                Log.v(Constants.TAG,"hello");
                return processGetFunctionParams();
        }
        return processControlCommand2(controlCode, params);
    }
    protected abstract byte[] processGetFunctionCode();
    protected byte[] processGetFunctionParams(){
        Log.w(Constants.TAG, "Unhandled  GetFunctionParams");
        return new byte[0];
    }
    protected byte[] processControlCommand2(int controlCode, byte[] params){
        Log.w(Constants.TAG, "Unknown control code " + controlCode);
        return new byte[0];
    }

    protected void showMessage(String message){
        if (currentMessage != null) {
            if (messageLog != null){
                String text = "" + messageLog.getText() + currentMessage.getText() + "\n";
                if (text.equals("\n")){
                    text = "";
                }
                messageLog.setText(text);
            }
            // currentMessage.setText(message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    currentMessage.setText(message);
                }
            });
        }
    }

    protected void onWorkerFinished(){
        Toast toast = Toast.makeText(this, "Transaction is completed", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setGravity(Gravity.TOP, 0, 40);
//        View view = toast.getView();
//        view.setBackgroundResource(R.color.colorAccent);
        toast.show();
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            public void run(){
                showMessage(getString(R.string.tap_your_card));
                timer.cancel();
            }
        }, 7000);
    }

    private void enableReaderMode() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);

        int timeout = 500;
        Bundle bundle = new Bundle();
        bundle.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, timeout * 10);
        adapter.enableReaderMode(this, this,
                NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                bundle);
    }

    private void disableReaderMode() {
        NfcAdapter adapter = NfcAdapter.getDefaultAdapter(this);
        if (adapter != null){
            adapter.disableReaderMode(this);
        }
    }

    private void startWorker(NFCReader nfcReader){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        int port = Integer.parseInt(preferences.getString("port", getString(R.string.pref_default_port)));
        String hostname = preferences.getString("host", getString(R.string.pref_default_host));

        worker = new PosProcessingWorker();
        worker.execute(new PosProcessingWorker.WorkerParams(hostname, port, nfcReader, this));
    }

    private void stopWorker(){
        if (worker != null){
            worker.cancel(true);
            worker = null;
        }
    }
}
