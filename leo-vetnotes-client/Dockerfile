FROM ubuntu:16.04

LABEL maintainer "Dave Kincaid <kincaid.dave@gmail.com>"

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y  software-properties-common && \
    add-apt-repository ppa:webupd8team/java -y && \
    apt-get update && \
    echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-get install -y oracle-java8-installer && \
    apt-get install -y curl && \
    apt-get clean

RUN curl http://ftp.wayne.edu/apache/uima/uima-as-2.9.0/uima-as-2.9.0-bin.tar.gz \
  | tar -xzC /opt

ENV UIMA_HOME /opt/apache-uima-as-2.9.0

COPY target/leo-vetnotes-client-1.0-SNAPSHOT.jar /opt/leo-vetnotes/leo-vetnotes-client/leo-vetnotes-client-1.0-SNAPSHOT.jar
COPY target/lib /opt/leo-vetnotes/leo-vetnotes-client/lib
COPY run-client.sh /opt/leo-vetnotes/run-client.sh

VOLUME /data-in
VOLUME /data-out
WORKDIR /opt/leo-vetnotes

ENTRYPOINT ["/opt/leo-vetnotes/run-client.sh"]

