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

/**
 * Message for sending by worker implementation.
 */
class Message {
    private static final int MESSAGE_SIZE = 1024;
    private static final byte NON_ASCII_SYMBOL = (byte) '.';
    private static final byte LF_SYMBOL = (byte) '\\';
    private byte[] value = new byte[MESSAGE_SIZE];
    private int pos = 0;

    public int getLength() {
        return pos;
    }

    public byte[] getBytes() {
        return value;
    }

    public void print(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (pos >= MESSAGE_SIZE) {
                break;
            }
            char c = s.charAt(i);
            if (c >= 32 && c <= 126) {
                value[pos] = (byte) c;
            } else if (c == 10) {
                value[pos] = LF_SYMBOL;
            } else {
                value[pos] = NON_ASCII_SYMBOL;
            }
            pos++;
        }
    }

    @Override
    public String toString() {
        byte[] b = new byte[pos];
        System.arraycopy(value, 0, b, 0, pos);
        return new String(b);
    }
}
