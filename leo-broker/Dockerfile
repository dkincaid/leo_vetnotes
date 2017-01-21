FROM ubuntu:16.04

MAINTAINER Dave Kincaid <kincaid.dave@gmail.com

RUN apt-get update && \
    apt-get upgrade -y && \
    apt-get install -y  software-properties-common && \
    add-apt-repository ppa:webupd8team/java -y && \
    apt-get update && \
    echo oracle-java7-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections && \
    apt-get install -y oracle-java8-installer && \
    apt-get install -y bzip2 && \
    apt-get install -y curl && \
    apt-get clean

RUN curl http://ftp.wayne.edu/apache/uima/uima-as-2.9.0/uima-as-2.9.0-bin.tar.gz \
  | tar -xzC /opt

ENV UIMA_HOME /opt/apache-uima-as-2.9.0

EXPOSE 61616

CMD $UIMA_HOME/bin/startBroker.sh