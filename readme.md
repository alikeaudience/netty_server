# netty_server
created by kiwi 23/9/2016

## Overview
A lightweight server using netty that can accommodate around 20,000 request/s on a normal server.

## Dependencies:
### Kafka APIs
- Download: https://www.apache.org/dyn/closer.cgi?path=/kafka/0.10.0.1/kafka_2.11-0.10.0.1.tgz

- The entire libs directory should be imported as external libraries.

### Netty APIs
- Download: https://netty.io/downloads.html

- Version: netty-4.1.5.Final

- Just importing netty-all-4.1.5.Final is enough for our development.

### Java SDK (7 or 8)

## Project structure:
### Netty server program

- HttpJsonServer

- HttpJsonServerHandler

- HttpJsonServerInitializer

### Saving request data to files

- FileWriteHelper

### Sending request data to Kafka servers

- JsonKafkaProducer

### Saving request data to local files
1. A scheduler is started as the Netty server starts, which schedules the swap (or write to new files) of saving file names every interval of time

2. Only one instance of FileWriter is instantiated, in order to improve the performance.

3. Request data are immediately flushed to disk, in order to prevent loss due to memory overflow.

4. Writing to files is async, thus imposing little effect on the server's request serving performance.

#### Notes

Which method to schedule (swap or writeToNewFile) is now hard coded. For now, writetoNewFile is used.

### Sending request data of Kafka servers
1. The producer API documentation is found here. https://kafka.apache.org/0100/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html

2. Only one instance of Producer is instantiated, in order to improve the performance.

3. As we are going to directly invoke the Java API provided by Kafka, the following changes must be made to the *server.properties* config file on the kafka servers. The addresses must be provided, if we want to use the Java APIs.

	```
	############################# Socket Server Settings ##########################$

	# The address the socket server listens on. It will get the value returned from
	# java.net.InetAddress.getCanonicalHostName() if not configured.
	#   FORMAT:
	#     listeners = security_protocol://host_name:port
	#   EXAMPLE:
	#     listeners = PLAINTEXT://your.host.name:9092
	listeners=PLAINTEXT://192.168.1.22:9092

	# Hostname and port the broker will advertise to producers and consumers. If no$
	# it uses the value for "listeners" if configured.  Otherwise, it will use the $
	# returned from java.net.InetAddress.getCanonicalHostName().
	advertised.listeners=PLAINTEXT://192.168.1.22:9092
	```

#### Notes

Note, now it supports to import a custom Kafka producer properties file from the arguments.

## Deployment

- Install JDK, and supervisor

- Build articfacts, which will generate a jar file

- Send the jar file to netty server, and create a custom Kafka producer properties file (remember its path)

- Create netty.conf, and add it to /etc/supervisor/conf.d/. An example may look like
	```
	[program:netty]
	command:java -cp /home/alikeaudience/KiwisExperiments/JsonServer.jar jsonserver.alikeaudience.com.HttpJsonServer 10 /home/alikeaudience/netty_kafka.properties test4 sdk
	autostart:true
	autorestart:true
	```
- start supervisor and load conf
  - `sudo service supervisor restart`
  - `sudo supervisorctl reread` 
  - `sudo supervisorctl update`

- To stop netty program, simply stop supversior, or stop netty using `sudo supervisorctl`

## Recommended IDE and test software
- IntelliJ IDEA

- JMeter


