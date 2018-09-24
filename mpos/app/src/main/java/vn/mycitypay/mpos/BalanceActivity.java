package vn.mycitypay.mpos;

import android.os.Bundle;

import java.nio.ByteBuffer;

public class BalanceActivity extends WorkerActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_balance);

        super.currentMessage = findViewById(R.id.currentMessage);
        super.messageLog = findViewById(R.id.messageLog);
        showMessage(getString(R.string.tap_your_card));
    }

    @Override
    protected byte[] processGetFunctionCode(){
        return ByteBuffer.allocate(4).putInt(Constants.FUNCTION_BALANCE).array();
    }
}
