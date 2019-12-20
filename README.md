Processing of generic Apache Kafka AVRO messages with kafka-streams
=======================================================================================

This is a very simple showcase on how to process in a kafka-streams
topology [AVRO](https://avro.apache.org/) messages of different types. 

Related to the blog post 
 
https://www.confluent.io/blog/put-several-event-types-kafka-topic/ 
 
this proof of concept project demonstrates how to consume via
kafka-streams several types of events from the [Apache Kafka](https://kafka.apache.org/) 
input topic.

The project provides two implementations of kafka-streams topologies:

- GenericRecordTopology: consumes instances of `GenericRecord` deserialized objects
- SpecificRecordTopology: consumes instances of `SpecificRecord` deserialized objects
 
 
 
## Testcontainers
The [testcontainers](https://www.testcontainers.org/) library already
offers a [Kafka](https://www.testcontainers.org/modules/kafka/) module
for interacting with [Apache Kafka](https://kafka.apache.org/), but
it there is, at the moment, not an testcontainer module for the whole
Confluent environment (Confluent Schema Registry container support is
missing from the module previously mentioned).
As a side note, the containers used do not use the default ports exposed
by default in the artifacts (e.g. : Apache Zookeeper 2181, Apache Kafka 9092,
Confluent Schema Registry 8081), but rather free ports available on the
test machine avoiding therefor possible conflicts with already running
services on the test machine. 

This project provides a functional prototype on how to setup the whole
Confluent environment (including Confluent Schema Registry) via testcontainers.
 
 
For the test environment the following containers will be started:
 
- Apache Zookeeper
- Apache Kafka
- Confluent Schema Registry


## Demo

The test demo will start verify whether a message serialized in AVRO 
format can be successfully serialized and sent over Apache Kafka
in order to subsequently be deserialized and read by a consumer.


Use 

```bash
mvn clean install
```

for trying out the project.

The file [testcontainers.properties](src/test/resources/testcontainers.properties) can be
used for overriding the default [docker](https://www.docker.com/) images used for the containers needed in setting 
up the Confluent test environment.