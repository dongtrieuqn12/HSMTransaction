package vn.mycitypay.mpos;

import android.app.Activity;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.util.Log;

import java.io.IOException;

public class NFCReader {
    public static final int TIMEOUT = 500;

    public static NFCReader get(Tag tag, Activity activity) {
        NFCReader nfcReader = null;
        try {
            nfcReader = new NFCReader(IsoDep.get(tag), activity);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return nfcReader;
    }

    private final IsoDep card;
    private Activity activity;

    private NFCReader(IsoDep sc, Activity activity) throws IOException {
        this.card = sc;
        sc.connect();
        card.setTimeout(TIMEOUT);
        this.activity = activity;
    }

    public void powerOn() {
        /* should already be connected... */
    }

    public void powerOff() throws IOException {
        selectMF();
    }

    public void reset() throws IOException {
        selectMF();
    }

    /* calculation based on https://code.google.com/p/ifdnfc/source/browse/src/atr.c */
    public byte[] getATR() {
        // get historical bytes for 14443-A
        byte[] historicalBytes = card.getHistoricalBytes();
        if (historicalBytes == null) {
            // get historical bytes for 14443-B
            historicalBytes = card.getHiLayerResponse();
        }
        if (historicalBytes == null) {
            historicalBytes = new byte[0];
        }

        /* copy historical bytes if available */
        byte[] atr = new byte[4+historicalBytes.length+1];
        atr[0] = (byte) 0x3b;
        atr[1] = (byte) (0x80+historicalBytes.length);
        atr[2] = (byte) 0x80;
        atr[3] = (byte) 0x01;
        System.arraycopy(historicalBytes, 0, atr, 4, historicalBytes.length);

        /* calculate TCK */
        byte tck = atr[1];
        for (int idx = 2; idx < atr.length; idx++) {
            tck ^= atr[idx];
        }
        atr[atr.length-1] = tck;

        return atr;
    }

    public void eject() throws IOException {
        card.close();
//        resetScreenTimeout();
    }

    public byte[] transmit(byte[] apdu) throws IOException {
        return card.transceive(apdu);
    }

    private static final byte[] SELECT_MF = {(byte) 0x00, (byte) 0xa4, (byte) 0x00, (byte) 0x0C};
    private void selectMF() throws IOException {
        byte[] response = card.transceive(SELECT_MF);
        if (response.length == 2 && response[0] == (byte) 0x90 && response[1] == (byte) 0x00) {
            Log.d(Constants.TAG, "Resetting the card by selecting the MF results in " + Hex.getHexString(response));
        }
    }
}
