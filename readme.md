# netty_server
created by kiwi 23/9/2016

## Overview
A lightweight server using netty that can accommodate around 20,000 request/s on a normal server.

## Dependencies:
- Kafka APIs
https://www.apache.org/dyn/closer.cgi?path=/kafka/0.10.0.1/kafka_2.11-0.10.0.1.tgz

The entire libs directory should be imported as external libraries.

- Netty APIs
https://netty.io/downloads.html

Version: netty-4.1.5.Final

Just importing netty-all-4.1.5.Final is enough for our development.

- Java SDK (7 or 8)

## Project structure:
- Netty server program

HttpJsonServer

HttpJsonServerHandler

HttpJsonServerInitializer

- Saving request data to files

FileWriteHelper

- Sending request data to Kafka servers

JsonKafkaProducer

## Saving request data to local files
- A scheduler is started as the Netty server starts, which schedules the swap of saving file names every interval of time

- Only one instance of FileWriter is instantiated, in order to improve the performance.

- Request data are immediately flushed to disk, in order to prevent loss due to memory overflow.

- Writing to files is async, thus imposing little effect on the server's request serving performance.

## Sending request data of Kafka servers
- The producer API documentation is found here. https://kafka.apache.org/0100/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html

- Only one instance of Producer is instantiated, in order to improve the performance.

## Recommended IDE and test software
IntelliJ IDEA
JMeter

