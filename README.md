# Leo for Veterinary Notes

[![Build Status](https://travis-ci.org/dkincaid/leo_vetnotes.svg?branch=master)](https://travis-ci.org/dkincaid/leo_vetnotes)

# Starting up the stack
The Leo stack consists of three components. 
1. The *Broker* which handles messages to and from the other two components.
2. The *Service* which receives documents and processes them through a UIMA pipeline and returns the CAS result. The service listens for messages coming through the *Broker* and processes them.
3. The *Client* which reads documents, sends them to the *Broker* for processing by the *Service* and also receives the resulting CAS back from the *Service*.

You should first create a network named *leonet*
```bash
docker network create leonet
```

## Broker
The broker must be started first. It runs an ActiveMQ instance and will receive and send messages to/from the Service and the Client.

```bash
docker run -p 61616:61616 --net leonet --name leo-broker \
  dkincaid/leo-broker
```

## Service
Make sure to set the `brokerUrl` to the ip address of the broker
```bash
docker run --net leonet --name ctakes-service \
  dkincaid/leo-vetnotes-service \
  --brokerUrl "tcp://leo-broker:61616"
```

## Client
Here you need to customize a few settings. 
1. Map the `/data-in` container volume to a local directory containing a file that you want to read. 
2. Map the `/data-out` container volume to a local directory that you want the output written to.
3. Set `--inputFile` to the file you want to read, `--outputDir` to `/data-out`
4. Set `--brokerUrl` to the address of the broker container.
```bash
docker run --net leonet --name avro-client \
  -v "/home/davek/data/medical-notes:/data-in" \
  -v "/tmp/leo-out:/data-out" \ 
  dkincaid/leo-vetnotes-client \
   --inputFile /data-in/avro-sample/part-00002.avro \
   --outputDir /data-out \
   --brokerUrl "tcp://leo-broker:61616"
```
