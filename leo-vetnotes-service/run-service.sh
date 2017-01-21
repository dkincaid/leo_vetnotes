#!/bin/bash

java -cp "/opt/leo-vetnotes/leo-vetnotes-service/leo-vetnotes-service-1.0-SNAPSHOT.jar:/opt/leo-vetnotes/leo-vetnotes-service/lib/*" com.kincaidweb.nlp.uima.leo.CtakesPipelineService -dictionaryDescriptor resources/org/apache/ctakes/dictionary/lookup/fast/vetdict.xml "$@"