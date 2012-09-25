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
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;

/*
Worker for sending syslog messages with TCP transport
 */
class TcpSender implements Runnable {

    private static final int FAILURE_TIMEOUT = 5000;

    private final String hostName;
    private final int port;
    private final Thread worker;
    private final BlockingQueue<Message> blockingQueue;

    public TcpSender(String hostName, int port, BlockingQueue<Message> blockingQueue) {
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

        private Socket socket;
        private OutputStream os;

        @Override
        public void run() {
            while (true) {
                try {
                    if (os == null) {
                        socket = new Socket(hostName, port);
                        os = socket.getOutputStream();
                    }
                    Message message = blockingQueue.take();
                    os.write(message.getBytes(), 0, message.getLength());
                    os.write('\n');
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
                    } catch (InterruptedException ie) {
                        return;
                    }
                }
            }
        }

        private void releaseResources() {

            try {
                if (os != null) {
                    os.flush();
                }
            } catch (Throwable t) {

            }

            try {
                if (os != null) {
                    os.close();
                }
            } catch (Throwable t) {

            }
            os = null;
            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (Throwable t) {

            }
            socket = null;
        }
    }
}
