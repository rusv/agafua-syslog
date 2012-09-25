/*
Copyright (c) 2012 Vitaly Russinkovsky

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
 */

package com.agafua.syslog;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.BlockingQueue;

/**
 * Worker for sending of syslog messages by UDP transport.
 */
class UdpSender implements Runnable {
    private static final int FAILURE_TIMEOUT = 5000;

    private final String hostName;
    private final int port;
    private final Thread worker;
    private final BlockingQueue<Message> blockingQueue;

    public UdpSender(String hostName, int port, BlockingQueue<Message> blockingQueue) {
        this.hostName = hostName;
        this.port = port;
        this.worker = new Thread(new Worker());
        this.blockingQueue = blockingQueue;
    }

    @Override
    public void run() {
        worker.start();
    }

    private class Worker implements Runnable {

        private DatagramSocket socket;
        private InetAddress address;

        @Override
        public void run() {
            while (true) {
                try {
                    if (socket == null) {
                        address = InetAddress.getByName(hostName);
                        socket = new DatagramSocket();
                    }
                    Message message = blockingQueue.take();
                    DatagramPacket packet = new DatagramPacket(message.getBytes(), message.getLength(), address, port);
                    socket.send(packet);
                } catch (InterruptedException e) {
                    releaseResources();
                    return;
                } catch (IOException e) {
                    releaseResources();
                    try {
                        Thread.sleep(FAILURE_TIMEOUT);
                    } catch (InterruptedException ie) {
                        return;
                    }

                } catch (Throwable t) {
                    releaseResources();
                    try {
                        Thread.sleep(FAILURE_TIMEOUT);
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        }

        private void releaseResources() {
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Throwable t) {
                socket = null;
            }
        }
    }
}
