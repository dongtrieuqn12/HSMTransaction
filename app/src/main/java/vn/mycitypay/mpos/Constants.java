package vn.mycitypay.mpos;

public interface Constants {
    String TAG = "MCP";

    public static final int CTRL_CODE_SHOW_MESSAGE = 0x01;
    public static final int CTRL_CODE_GET_FUNCTION_CODE = 0x20;
    public static final int CTRL_CODE_GET_FUNCTION_PARAMS = 0x21;
    public static final int CTRL_CODE_FINISH = 0xFFFF;

    public static final int FUNCTION_BALANCE = 0x01;
    public static final int FUNCTION_PURCHASE = 0x02;
    public static final int FUNCTION_TOPUP = 0x03;
}
