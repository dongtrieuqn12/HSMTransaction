package vn.mycitypay.mpos;

import android.os.Bundle;
import android.support.design.widget.TextInputEditText;

import java.nio.ByteBuffer;

public class TopupActivity extends WorkerActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_topup);

        super.currentMessage = findViewById(R.id.currentMessage);
        super.messageLog = findViewById(R.id.messageLog);
        showMessage(getString(R.string.tap_your_card));
    }

    @Override
    protected byte[] processGetFunctionCode(){
        return ByteBuffer.allocate(4).putInt(Constants.FUNCTION_TOPUP).array();
    }

    protected byte[] processGetFunctionParams(){
        TextInputEditText amountTextView = findViewById(R.id.amount);
        String s = amountTextView.getText().toString().trim();
        int amount = Integer.parseInt(s);
        return ByteBuffer.allocate(4).putInt(amount).array();
    }
}
