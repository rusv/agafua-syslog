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

import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.*;

/**
 * Implementation of java.util.logging.Handler for syslog protocol RFC 3164.
 */
public class SyslogHandler extends Handler {

    private static final int LOG_QUEUE_SIZE = 1024;

    private static final String LOCALHOST = "localhost";
    private static final String MY_HOST_NAME = "0.0.0.0";
    private static final int DEFAULT_PORT = 514;
    private static final int MIN_PORT = 0;
    private static final int MAX_PORT = 65535;

    private static final String TRANSPORT_PROPERTY = "transport";
    private static final String HOSTNAME_PROPERTY = "hostname";
    private static final String PORT_PROPERTY = "port";
    private static final String FACILITY_PROPERTY = "facility";
    private static final String DAEMON_MODE_PROPERTY = "daemon";

    private final String hostName;
    private final int port;
    private final Facility facility;
    private final Transport transport;
    private final boolean daemonMode;

    private BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<Message>(LOG_QUEUE_SIZE);
    private boolean closed = false;
    private Thread worker;
    private volatile String myHostName;

    private Adaptor adaptor = new Adaptor();

    public SyslogHandler() {
        super();
        transport = parseTransport();
        hostName = parseHostName();
        port = parsePort();
        facility = parseFacility();
        daemonMode = parseDaemonMode();
        setFormatter(new SimpleFormatter());
        if (Transport.TCP.equals(transport)) {
            worker = new Thread(new TcpSender(hostName, port, blockingQueue, daemonMode));
        } else {
            worker = new Thread(new UdpSender(hostName, port, blockingQueue, daemonMode));
        }
        worker.start();
    }

    @Override
    public void publish(LogRecord record) {
        if (closed) {
            return;
        }
        try {
            Message message = new Message();
            String pri = adaptor.adaptPriority(record, facility);
            message.print(pri);
            String timestamp = adaptor.adaptTimeStamp(record);
            message.print(timestamp);
            message.print(" ");
            String host = getMyHostName();
            message.print(host);
            message.print(" ");
            String msg = getFormatter().format(record);
            message.print(msg);
            blockingQueue.offer(message);
        } catch (Throwable t) {

        }
    }

    @Override
    public void flush() {
        // Does nothing because sending is asynchronous
    }

    @Override
    public void close() throws SecurityException {
        blockingQueue.clear();
        if (worker != null) {
            worker.interrupt();
        }
        worker = null;
        closed = true;
    }

    public String getTransport() {
        return transport.name();
    }

    public String getHostName() {
        return hostName;
    }

    public int getPort() {
        return port;
    }

    public String getFacility() {
        return facility.name();
    }

    private Transport parseTransport() {
        String transportProperty = SyslogHandler.class.getName() + "." + TRANSPORT_PROPERTY;
        String transportValue = LogManager.getLogManager().getProperty(transportProperty);
        for (Transport t : Transport.values()) {
            if (t.name().equalsIgnoreCase(transportValue)) {
                return t;
            }
        }
        return Transport.UDP;
    }

    private String parseHostName() {
        String hostNameProperty = SyslogHandler.class.getName() + "." + HOSTNAME_PROPERTY;
        String hostNameValue = LogManager.getLogManager().getProperty(hostNameProperty);
        if (hostNameValue != null && hostNameValue.length() > 0) {
            return hostNameValue;
        }
        return LOCALHOST;
    }

    private int parsePort() {
        String portProperty = SyslogHandler.class.getName() + "." + PORT_PROPERTY;
        String portValue = LogManager.getLogManager().getProperty(portProperty);
        if (portValue != null) {
            Integer p = null;
            try {
                p = Integer.parseInt(portValue);
            } catch (NumberFormatException e) {

            }
            if (p != null && p >= MIN_PORT && p < MAX_PORT) {
                return p;
            }
        }
        return DEFAULT_PORT;
    }

    private Facility parseFacility() {
        String facilityProperty = SyslogHandler.class.getName() + "." + FACILITY_PROPERTY;
        String facilityValue = LogManager.getLogManager().getProperty(facilityProperty);
        for (Facility f : Facility.values()) {
            if (f.name().equalsIgnoreCase(facilityValue)) {
                return f;
            }
        }
        return Facility.USER;
    }

    private boolean parseDaemonMode() {
        String daemonModeProperty = SyslogHandler.class.getName() + "." + DAEMON_MODE_PROPERTY;
        String daemonModeValue = LogManager.getLogManager().getProperty(daemonModeProperty);
        for(String s : Arrays.asList("yes", "true")) {
            if(s.equalsIgnoreCase(daemonModeValue)) {
                return true;
            }
        }
        return false;
    }

    private String getMyHostName() {
        if (myHostName != null) {
            return myHostName;
        }
        try {
            String localHostName = java.net.InetAddress.getLocalHost().getHostName();
            if (localHostName != null) {
                myHostName = localHostName;
                return localHostName;
            }
        } catch (UnknownHostException e) {
            // Nothing
        }
        try {
            String localHostAddress = java.net.Inet4Address.getLocalHost().getHostAddress();
            if (localHostAddress != null) {
                myHostName = localHostAddress;
                return localHostAddress;
            }
        } catch (UnknownHostException e) {
            // Nothing
        }
        myHostName = MY_HOST_NAME;
        return myHostName;
    }
}
