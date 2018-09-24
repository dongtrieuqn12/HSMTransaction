package vn.mycitypay.mpos;

import android.os.AsyncTask;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class PosProcessingWorker extends AsyncTask<PosProcessingWorker.WorkerParams, Void, PosProcessingWorker.WorkerParams> {
    private static final int VPCD_CTRL_LEN = 1;
    private static final byte VPCD_CTRL_OFF = 0;
    private static final byte VPCD_CTRL_ON = 1;
    private static final byte VPCD_CTRL_RESET = 2;
    private static final byte VPCD_CTRL_ATR = 4;
    private static final byte CTRL_CONTROL_COMMAND = 0x08;

    static class WorkerParams {
        final String hostname;
        final int port;
        final NFCReader reader;
        final WorkerActivity activity;
        WorkerParams(String hostname, int port, NFCReader reader, WorkerActivity activity){
            this.hostname = hostname;
            this.port = port;
            this.reader = reader;
            this.activity = activity;
        }
    }

    private NFCReader reader;
    private Socket socket;
    private InputStream inputStream;
    private OutputStream outputStream;

    public PosProcessingWorker(){
    }

    @Override
    protected WorkerParams doInBackground(WorkerParams... params) {
        Log.i(Constants.TAG, "doInBackground");
        boolean done = false;

        try{
            WorkerParams param = params[0];
            reader = param.reader;

            Log.i(Constants.TAG, "Connecting to " + param.hostname + ":" + Integer.toString(param.port) + "...");
            connect(param.hostname, param.port);
            Log.i(Constants.TAG, "Connected to " + param.hostname + ":" + Integer.toString(param.port));

            while (!done && !isCancelled()) {
                byte[] out = null;
                byte[] in = receiveFromServer();
                if (in == null) {
                    break;
                }

                if (in.length == VPCD_CTRL_LEN) {
                    switch (in[0]) {
                        case VPCD_CTRL_OFF:
                            reader.powerOff();
                            done = true;
                            Log.i(Constants.TAG, "Powered down the card (cold reset)");
                            break;
                        case VPCD_CTRL_ON:
                            reader.powerOn();
                            Log.i(Constants.TAG, "Powered up the card with ATR " + Hex.getHexString(reader.getATR()));
                            break;
                        case VPCD_CTRL_RESET:
                            reader.reset();
                            Log.i(Constants.TAG, "Resetted the card (warm reset)");
                            done = true;
                            break;
                        case VPCD_CTRL_ATR:
                            out = reader.getATR();
                            break;
                        default:
                            throw new IOException("Unhandled command from VPCD.");
                    }
                } else if (in[0] == CTRL_CONTROL_COMMAND){
                    int controlCode = (int)(in[1] << 24) + (int)(in[2] << 16) + (int)(in[3] << 8) + (int)(in[4] << 0);
                    byte[] parameters = new byte[in.length - 5];
                    System.arraycopy(in, 5, parameters, 0, parameters.length);
                    out = param.activity.processControlCommand(controlCode, parameters);
                } else {
                    out = reader.transmit(in);
                }

                if (out != null) {
                    sendToServer(out);
                }
            }
        }catch (Exception e){
            if (!isCancelled()) {
                Log.e(Constants.TAG, "Error", e);
            }
        }

        try {
            disconnect();
        } catch (Exception e) {
            Log.e(Constants.TAG, "Error", e);
        }

        return params[0];
    }

    @Override
    protected void onPostExecute(WorkerParams result) {
        result.activity.onWorkerFinished();
    }

    @Override
    protected void onCancelled () {
        if (socket != null){
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void connect(String hostname, int port) throws IOException {
        socket = new Socket(InetAddress.getByName(hostname), port);
        outputStream = socket.getOutputStream();
        inputStream = socket.getInputStream();
    }

    private void disconnect() throws IOException {
        if (reader != null) {
            reader.eject();
        }
        if  (socket != null) {
            socket.close();
            Log.i(Constants.TAG, "Disconnected");
        }
    }

    private byte[] receiveFromServer() throws IOException {
        int b1 = inputStream.read();
        int b2 = inputStream.read();
        if (b1 == -1 || b2 == -1) {
            // EOF
            return null;
        }
        int length = (b1 << 8) + b2;

        byte[] data = new byte[length];

        int offset = 0;
        while (length > 0) {
            int read = inputStream.read(data, offset, length);
            if (read == -1) {
                // EOF
                return null;
            }
            offset += read;
            length -= read;
        }

        return data;
    }

    private void sendToServer(byte[] data) throws IOException {
        byte[] length = new byte[2];
        length[0] = (byte) (data.length >> 8);
        length[1] = (byte) (data.length & 0xff);
        outputStream.write(length);

        outputStream.write(data, 0, data.length);

        outputStream.flush();
    }
}
