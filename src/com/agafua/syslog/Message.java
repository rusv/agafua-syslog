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

import java.io.*;

/**
 * Message for sending by worker implementation.
 *
 * No message capacity restrictions.
 * rfc5424(The Syslog Protocol) recommends supporting at least 2048 bytes
 * for messages and the capacity depends of the transport protocol.
 * rfc5426(Transmission of Syslog Messages over UDP) mentions size of 65535
 * minus the udp headers. Over the network udp messages will be truncated
 * by MTU. Localhost MTU in linux systems is 65536.
 * Although the default message size for some syslog daemons by default is 8k
 * (http://www.rsyslog.com/doc/v8-stable/configuration/global/index.html)
 */
class Message {
    private static final byte NON_ASCII_SYMBOL = (byte) '.';
    private static final byte LF_SYMBOL = (byte) '\\';
    private ByteArrayOutputStream value = new ByteArrayOutputStream();

    public int getLength() {
        return value.size();
    }

    public byte[] getBytes() {
        return value.toByteArray();
    }

    public void print(String s) throws IOException {
        byte[] b = s.getBytes();
        for (int i = 0; i < b.length; i++) {
            byte c = b[i];

            if (c >= 32 && c <= 126)
                continue;

            if (c == 10) {
                b[i] = LF_SYMBOL;
            } else {
                b[i] = NON_ASCII_SYMBOL;
            }
        }
        value.write(b);
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
