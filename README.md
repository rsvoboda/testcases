# CXF / WSS4J microbenchmark
CXF / WSS4J microbenchmark using Maven & JMH - http://openjdk.java.net/projects/code-tools/jmh/

## How to run it
```bash
CXF_VERSION=3.2.1
cd apache/cxf/cxf-benchmarks/cxf-jmh
mvn clean integration-test -Dcxf.version=${CXF_VERSION}
```

## Where are results
```bash
ls apache/cxf/cxf-benchmarks/cxf-jmh/results.*
```

## What if I want to run CXF 3.1.4+
Use https://github.com/rsvoboda/testcases/tree/CxfJmhStandaloneFor3.1.x tag, run command is the same