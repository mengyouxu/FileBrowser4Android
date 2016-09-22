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

public final class UdpDataSource2 implements UriDataSource {

    /**
     * Thrown when an error is encountered when trying to read from a {@link UdpDataSource2}.
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
    private UdpCacher mCacher;
    /**
     * @param listener An optional listener.
     */
    public UdpDataSource2(TransferListener listener) {
        this(listener, DEFAULT_MAX_PACKET_SIZE);
    }

    /**
     * @param listener An optional listener.
     * @param maxPacketSize The maximum datagram packet size, in bytes.
     */
    public UdpDataSource2(TransferListener listener, int maxPacketSize) {
        this(listener, maxPacketSize, DEAFULT_SOCKET_TIMEOUT_MILLIS);
    }

    /**
     * @param listener An optional listener.
     * @param maxPacketSize The maximum datagram packet size, in bytes.
     * @param socketTimeoutMillis The socket timeout in milliseconds. A timeout of zero is interpreted
     *     as an infinite timeout.
     */
    public UdpDataSource2(TransferListener listener, int maxPacketSize, int socketTimeoutMillis) {
        Log.i("UdpDataSource2", "new UdpDataSource2");
        mCacher = new UdpCacher(listener, maxPacketSize, socketTimeoutMillis);
        mCacher.start();
    }

    @Override
    protected void finalize() throws Throwable {
        Log.i("UdpDataSource2", "stopCaching");
        mCacher.stopCaching();
        mCacher.join();
        Log.i("UdpDataSource2", "call super.finalize()");
        super.finalize();
    }

    @Override
    public long open(DataSpec dataSpec) throws UdpDataSourceException {
        this.dataSpec = dataSpec;
        long ret_val = mCacher.open(dataSpec);
        // mCacher.start();
        Log.i("UdpDataSource2", "new UdpDataSource2 open");
        return ret_val;
    }

    @Override
    public int read(byte[] buffer, int offset, int readLength) throws UdpDataSourceException {

        return mCacher.read(buffer,offset,readLength);
    }

    @Override
    public void close() {
        Log.i("UdpDataSource2", "new UdpDataSource2 close");
        mCacher.close();
    }

    @Override
    public String getUri() {
        return dataSpec == null ? null : dataSpec.uri.toString();
    }

    private class UdpCacher extends Thread{
        private final TransferListener listener;
        private final DatagramPacket packet;
        private final int socketTimeoutMillis;

        private DataSpec dataSpec;
        private DatagramSocket socket;
        private MulticastSocket multicastSocket;
        private InetAddress address;
        private InetSocketAddress socketAddress;
        private boolean opened;

        private byte[] packetBuffer;
        private Queue<dataPacket> dataQueue;
        private int packetRemaining;
        private boolean mWorking;
        private dataPacket readBuffer;
        private Object lock;

        private FileOutputStream dumpTsFile;
        public UdpCacher(TransferListener listener, int maxPacketSize, int socketTimeoutMillis){
            this.listener = listener;
            this.socketTimeoutMillis = socketTimeoutMillis;
            Log.i("UdpDataSource2", "new UdpCacher");
            dataQueue = new LinkedList<dataPacket>();
            packetBuffer = new byte[maxPacketSize];
            packet = new DatagramPacket(packetBuffer, 0, maxPacketSize);
            mWorking = true;
            packetRemaining = 0;
            lock = new Object();

            socket = null;
            try {
                String[] fileNameList =  null;
                File dir = null;
                dir =  new File("/sdcard/");
                fileNameList = dir.list();
                int index = 0;
                for(int i = 0; i < fileNameList.length; i++){
                    if(fileNameList[i].indexOf("dump.") > -1){
                        index++;
                    };
                }
                dumpTsFile = new FileOutputStream("/sdcard/dump." + index + ".ts");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        public void stopCaching(){
            mWorking = false;
        }
        @Override
        public void run() {
            Log.i("UdpDataSource2", "UdpCacheThread run");
            while (mWorking == true) {
                if(socket == null){
                    continue;
                } else if(socket.isClosed() == true){
                    continue;
                }
                // We've read all of the data from the current packet. Get another.
                try {
                    socket.receive(packet);
                } catch (IOException e) {
                    Log.e("t", "socket.receive Exception: " + e.getMessage());
                    continue;
                }

                dataPacket data = new dataPacket();
                data.size = packet.getLength();
                data.data = packetBuffer.clone();
                try {
                    dumpTsFile.write(packetBuffer);
                } catch (IOException e) {
                    Log.e("UdpDataSource2", "dumpTsFile.write : " + e.getMessage());
                }
                if (listener != null) {
                    //listener.onBytesTransferred(data.size);
                }
                synchronized(lock) {
                    //dataQueue.offer(data);
                }
            }
        }

        public long open(DataSpec dataSpec) throws UdpDataSourceException {
            this.dataSpec = dataSpec;
            this.mWorking = true;
            String host = dataSpec.uri.getHost();
            int port = dataSpec.uri.getPort();

            try {
                address = InetAddress.getByName(host);
                socketAddress = new InetSocketAddress(address, port);
                if (address.isMulticastAddress()) {
                    multicastSocket = new MulticastSocket(socketAddress);
                    multicastSocket.joinGroup(address);
                    socket = multicastSocket;
                } else {
                    socket = new DatagramSocket(socketAddress);
                }
            } catch (IOException e) {
                throw new UdpDataSourceException(e);
            }

            try {
                socket.setSoTimeout(socketTimeoutMillis);
            } catch (SocketException e) {
                throw new UdpDataSourceException(e);
            }

            opened = true;
            if (listener != null) {
                listener.onTransferStart();
            }
            return C.LENGTH_UNBOUNDED;
        }

        public int read(byte[] buffer, int offset, int readLength) throws UdpDataSourceException {

            if(packetRemaining <= 0){
                try {
                    synchronized(lock) {
                        readBuffer = dataQueue.poll();
                    }
                }catch (NoSuchElementException e){
                    Log.i("UdpDataSource2", "dataQueue Exception : " + e.getMessage());
                }
                if(readBuffer == null){
                    return 0;
                }
                /*
                try {
                    dumpTsFile.write(readBuffer.data);
                } catch (IOException e) {
                    Log.e("UdpDataSource2", "dumpTsFile.write : " + e.getMessage());
                }
                */
                packetRemaining = readBuffer.size;
                // Log.i("UdpDataSource2", "dataQueue.poll data len " + packetRemaining);
            }

            int packetOffset = readBuffer.size - packetRemaining;
            int bytesToRead = Math.min(packetRemaining, readLength);
            System.arraycopy(readBuffer.data, packetOffset, buffer, offset, bytesToRead);
            //Log.i("UdpDataSource2", "Want off " + offset + ", size " + readLength + ", real size " + packetRemaining);
            packetRemaining -= bytesToRead;
            return bytesToRead;
        }

        public void close() {

            if (multicastSocket != null) {
                try {
                    multicastSocket.leaveGroup(address);
                } catch (IOException e) {
                    // Do nothing.
                }
                multicastSocket = null;
            }
            if (socket != null) {
                socket.close();
                socket = null;
            }
            address = null;
            socketAddress = null;
            packetRemaining = 0;
            if (opened) {
                opened = false;
                if (listener != null) {
                    listener.onTransferEnd();
                }
            }
        }
    };

    class dataPacket{
        int size;
        byte[] data;
    }
}
