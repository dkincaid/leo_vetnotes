#!/bin/bash

java -cp "/opt/leo-vetnotes/leo-vetnotes-client/leo-vetnotes-client-1.0-SNAPSHOT.jar:/opt/leo-vetnotes/leo-vetnotes-client/lib/*" com.kincaidweb.nlp.uima.leo.AvroToXmiClient "$@"