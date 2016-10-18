/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// copy from ExoPlayer-r1.5.8/library/src/main/java/com/google/android/exoplayer/upstreamUdpDataSource.java
package meng.FileBrowser;

/**
 * Created by mengyouxu on 2016/8/23.
 */

import android.net.Uri;
import android.util.Log;
import android.util.TimeUtils;

import com.google.android.exoplayer.C;
import com.google.android.exoplayer.upstream.DataSpec;
import com.google.android.exoplayer.upstream.TransferListener;
import com.google.android.exoplayer.upstream.UriDataSource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.locks.Lock;

public final class UdpDataSource3 implements UriDataSource {
    private String TAG = "UdpDataSource3";

    /**
     * Thrown when an error is encountered when trying to read from a {@link UdpDataSource3}.
     */
    public static final class UdpDataSourceException extends IOException {

        public UdpDataSourceException(String message) {
            super(message);
        }
        public UdpDataSourceException(IOException cause) {
            super(cause);
        }
    }

    /**
     * The default maximum datagram packet size, in bytes.
     */
    public static final int DEFAULT_MAX_PACKET_SIZE = 2000;

    /**
     * The default socket timeout, in milliseconds.
     */
    public static final int DEAFULT_SOCKET_TIMEOUT_MILLIS = 8 * 1000;

    private DataSpec dataSpec;
    /**
     * @param listener An optional listener.
     */
    public UdpDataSource3(TransferListener listener) {
        this(listener, DEFAULT_MAX_PACKET_SIZE);
    }

    /**
     * @param listener An optional listener.
     * @param maxPacketSize The maximum datagram packet size, in bytes.
     */
    public UdpDataSource3(TransferListener listener, int maxPacketSize) {
        this(listener, maxPacketSize, DEAFULT_SOCKET_TIMEOUT_MILLIS);
        Log.i(TAG, "UdpDataSource3 A " + stringFromJNI());
    }

    /**
     * @param listener An optional listener.
     * @param maxPacketSize The maximum datagram packet size, in bytes.
     * @param socketTimeoutMillis The socket timeout in milliseconds. A timeout of zero is interpreted
     *     as an infinite timeout.
     */
    public UdpDataSource3(TransferListener listener, int maxPacketSize, int socketTimeoutMillis) {
        Log.i(TAG, "new UdpDataSource3 B");
    }

    @Override
    protected void finalize() throws Throwable {
        Log.i(TAG, "stopCaching");

        super.finalize();
    }

    @Override
    public long open(DataSpec dataSpec) throws UdpDataSourceException {
        this.dataSpec = dataSpec;
        String host = dataSpec.uri.getHost();
        int port = dataSpec.uri.getPort();
        nativeOpen(host, port);

        byte[] data = {'a','b','c','d','e',1,2,3,4,5};
        byte[] data_r = nativeByteArrayTest(data);
        Log.i(TAG, "array test: ret: " + data_r[0] + " " + data_r[1] + " " + data_r[2] + " " + data_r[3] + " " + data_r[4] + " " + data_r[5] + " " + data_r[6] + " " + data_r[7]);
        Log.i(TAG, "array test: data : " + data[0] + " " + data[1] + " " + data[2] + " " + data[3] + " " + data[4]  + " " + data[5] + " " + data[6] + " " + data[7]);
        return 0;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws UdpDataSourceException {

        return nativeRead(buffer,offset,readLength);
    }

    @Override
    public void close() {
        nativeClose();
        Log.i(TAG, "new UdpDataSource3 close");

    }

    @Override
    public String getUri() {
        return dataSpec == null ? null : dataSpec.uri.toString();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
    public native boolean nativeOpen(String udpAddr, int port);
    public native boolean nativeClose();
    public native int nativeRead(byte[] buffer, int offset, int readLength);
    public native byte[] nativeByteArrayTest(byte[] buffer);
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }
}
