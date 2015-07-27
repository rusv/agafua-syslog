# Getting Started #

## Intoduction ##

Syslog protocol is described in
  * RFC 3164 - The BSD Syslog Protocol

In the example below we will send log messages to facility "local0" of syslog / rsyslog service located on localhost and listening UDP on port 514. We will use logging configuration file **logging.properties**.

## Library ##

Download the latest version of agafua-syslog JAR. It should be available on the Java classpath of your project.

## Logging Configuration Example ##

Here is an example of java.util.logging configuration file

```
handlers= java.util.logging.ConsoleHandler, com.agafua.syslog.SyslogHandler

.level= FINEST

# Console handler configuration
java.util.logging.ConsoleHandler.level = FINEST
java.util.logging.ConsoleHandler.formatter = java.util.logging.SimpleFormatter

# Syslog logger
com.agafua.syslog.SyslogHandler.transport = udp
com.agafua.syslog.SyslogHandler.facility = local0
com.agafua.syslog.SyslogHandler.port = 514
com.agafua.syslog.SyslogHandler.hostname = localhost
```

In example above two handlers are configured - Console handler and Syslog handler that
sends log records for facility local0 to port 514 of localhost using UDP transport.

Selection of formatter and log level is not available in current version.

To tell java virtual machine to use specific logging configuration add a VM option:

```
-Djava.util.logging.config.file=logging.properties
```

## Rsyslog / Syslog configutation ##

### Ubuntu 10.04 ###

Enable listening of UDP on port 514 in /etc/rsyslog.conf
```
$ModLoad imudp
$UDPServerRun 514
```

Logging rules are configured in files in directory /etc/rsyslog.d
Create a file 60-java.conf and configure logging
```
local0.* /var/log/local0.log
```
Configuration above will log all records to addressed to facility local0 to file /var/log/local0.log
You can achieve better logging performance if you disable log records synchronization by adding "-" before file name, but this way some time could pass before record will become visible in log
```
local0.* -/var/log/local0.log
```

## Advanced Options ##

### TCP Transport ###
Usage of syslog with TCP transport is supported by specifying option
```
com.agafua.syslog.SyslogHandler.transport = tcp
```