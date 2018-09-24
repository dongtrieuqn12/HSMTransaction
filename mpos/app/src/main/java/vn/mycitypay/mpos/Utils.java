package vn.mycitypay.mpos;

import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

public class Utils {
    public static boolean isHostAvailable(final String host, final int port) {
        final int timeout = 1000;
        try (final Socket socket = new Socket()) {
            final SocketAddress sockaddr = new InetSocketAddress(host, port);
            socket.connect(sockaddr, timeout);
            return true;
        } catch (java.io.IOException e) {
            return false;
        }
    }

    public static int bytes2Int(byte[] bytes) {
        return new BigInteger(bytes).intValue();
    }

    public static byte[] int2Bytes(int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }
}
