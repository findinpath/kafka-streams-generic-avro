package com.findinpath;

import static org.awaitility.Awaitility.await;
import static org.awaitility.Durations.ONE_HUNDRED_MILLISECONDS;
import static org.awaitility.Durations.ONE_MINUTE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import com.findinpath.avro.BookmarkEvent;
import com.findinpath.avro.UserCreatedEvent;
import com.findinpath.testcontainers.KafkaContainer;
import com.findinpath.testcontainers.SchemaRegistryContainer;
import com.findinpath.testcontainers.ZookeeperContainer;
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient;
import io.confluent.kafka.schemaregistry.client.rest.exceptions.RestClientException;
import io.confluent.kafka.serializers.AbstractKafkaAvroSerDeConfig;
import io.confluent.kafka.serializers.KafkaAvroDeserializerConfig;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import io.confluent.kafka.serializers.KafkaAvroSerializerConfig;
import io.confluent.kafka.serializers.subject.RecordNameStrategy;
import io.confluent.kafka.streams.serdes.avro.GenericAvroDeserializer;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroDeserializer;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.specific.SpecificRecord;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.KafkaAdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.KafkaStreams.State;
import org.junit.After;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;

/**
 * This is a showcase on how to test the serialization/deserialization of several types of AVRO
 * messages over Apache Kafka into the same topic  with the help of Docker containers (via
 * <a href="https://www.testcontainers.org/>testcontainers</a> library).
 *
 * For the test environment the following containers will be started:
 *
 * <ul>
 *   <li>Apache Zookeeper</li>
 *   <li>Apache Kafka</li>
 *   <li>Confluent Schema Registry</li>
 * </ul>
 *
 * Once the test environment is started,there will be written:
 * <ul>
 *   <li>a <code>BookmarkEvent</code></li>
 *   <li>a <code>UserCreatedEvent</code></li>
 * </ul>
 * there will be written into the input topic.
 * These entries are processed by two different stateless kafka-streams topologies:
 * <ul>
 *   <li>a topology which analyses specific AVRO records</li>
 *   <li>a topology which analyses generic AVRO records</li>
 * </ul>
 *
 * that don't do anything else than echoing the input event to another topic.
 *
 * The {@link #demo()} test will simply verify whether multiple message types
 * serialized in AVRO format can be successfully processed by the kafka-streams topologies.
 */
public class GenericKafkaStreamsAvroDemoTest {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(GenericKafkaStreamsAvroDemoTest.class);
  private static final String URL = "https://www.findinpath.com";
  private static final long POLL_INTERVAL_MS = 100L;
  private static final long POLL_TIMEOUT_MS = 10_000L;

  private static Network network;

  private static ZookeeperContainer zookeeperContainer;
  private static KafkaContainer kafkaContainer;
  private static SchemaRegistryContainer schemaRegistryContainer;


  private GenericRecordTopology genericRecordTopology;
  private SpecificRecordTopology specificRecordTopology;


  @BeforeAll
  public static void confluentSetup() throws Exception {
    network = Network.newNetwork();
    zookeeperContainer = new ZookeeperContainer()
        .withNetwork(network);
    kafkaContainer = new KafkaContainer(zookeeperContainer.getZookeeperConnect())
        .withNetwork(network);
    schemaRegistryContainer = new SchemaRegistryContainer(zookeeperContainer.getZookeeperConnect())
        .withNetwork(network);

    Runtime.getRuntime()
        .addShutdownHook(new Thread(() ->
                Arrays.asList(zookeeperContainer, kafkaContainer, schemaRegistryContainer)
                    .parallelStream().forEach(GenericContainer::stop)
            )
        );

    Stream.of(zookeeperContainer, kafkaContainer, schemaRegistryContainer).parallel()
        .forEach(GenericContainer::start);

    registerSchemaRegistryTypes();
  }


  @After
  public void topologyTearDown() {
    if (genericRecordTopology != null) {
      genericRecordTopology.close();
      genericRecordTopology = null;
    }
  }

  @Test
  public void demo() throws Exception {
    // GIVEN
    var scenarioName = "demo";
    var inputTopic = scenarioName + "-input";
    var genericApplicationId = scenarioName + "-generic";
    var genericOutputTopic = scenarioName + "-generic-output";
    var specificApplicationId = scenarioName + "-specific";
    var specificOutputTopic = scenarioName + "-specific-output";

    createTopics(inputTopic, genericOutputTopic, specificOutputTopic);

    genericRecordTopology = startGenericTopology(genericApplicationId, inputTopic,
        genericOutputTopic);
    specificRecordTopology = startSpecificTopology(specificApplicationId, inputTopic,
        specificOutputTopic);

    final UUID userUuid = UUID.randomUUID();
    final UserCreatedEvent userCreatedEvent = new UserCreatedEvent(userUuid.toString(),
        "findinpath", Instant.now().toEpochMilli());
    final BookmarkEvent bookmarkEvent = new BookmarkEvent(userUuid.toString(), URL,
        Instant.now().toEpochMilli());

    // WHEN
    produce(inputTopic, userUuid.toString(), userCreatedEvent);
    produce(inputTopic, userUuid.toString(), bookmarkEvent);
    LOGGER.info(
        String.format(
            "Successfully sent 1 UserCreatedEvent and 1 BookmarkEvent message to the topic called %s",
            inputTopic));

    // THEN
    var genericRecords = dumpGenericRecordTopic(genericOutputTopic, 2, POLL_TIMEOUT_MS);
    LOGGER.info(String.format("Retrieved %d consumer records from the topic %s",
        genericRecords.size(), genericOutputTopic));
    assertThat(genericRecords.size(), equalTo(2));
    assertThat(genericRecords.get(0).key(), equalTo(userCreatedEvent.getUserUuid()));
    assertThat(genericRecords.get(0).value().get("userUuid"),
        equalTo(bookmarkEvent.getUserUuid()));
    assertThat(genericRecords.get(0).value().get("username"),
        equalTo(userCreatedEvent.getUsername()));
    assertThat(genericRecords.get(1).key(), equalTo(bookmarkEvent.getUserUuid()));
    assertThat(genericRecords.get(1).value().get("userUuid"),
        equalTo(bookmarkEvent.getUserUuid()));
    assertThat(genericRecords.get(1).value().get("url"), equalTo(bookmarkEvent.getUrl()));

    var specificRecords = dumpSpecificRecordTopic(genericOutputTopic, 2, POLL_TIMEOUT_MS);
    LOGGER.info(String.format("Retrieved %d consumer records from the topic %s",
        specificRecords.size(), specificOutputTopic));
    assertThat(specificRecords.size(), equalTo(2));
    assertThat(specificRecords.get(0).key(), equalTo(userCreatedEvent.getUserUuid()));
    assertThat(specificRecords.get(0).value(), equalTo(userCreatedEvent));
    assertThat(specificRecords.get(1).key(), equalTo(bookmarkEvent.getUserUuid()));
    assertThat(specificRecords.get(1).value(), equalTo(bookmarkEvent));
  }


  private GenericRecordTopology startGenericTopology(String applicationId, String inputTopic,
      String outputTopic) {
    var genericRecordTopology = new GenericRecordTopology(
        kafkaContainer.getBootstrapServers(),
        schemaRegistryContainer.getServiceURL(),
        applicationId,
        inputTopic,
        outputTopic);
    genericRecordTopology.start();

    // It is mandatory for the accuracy tests to make sure that the
    // kafka-streams topology is started before sending messages
    // to the input topic in order to make sure that the events sent
    // for processing are being taken into account by the kafka-streams topology.
    await()
        .pollInterval(ONE_HUNDRED_MILLISECONDS)
        .atMost(ONE_MINUTE)
        .until(() -> genericRecordTopology.getState() == State.RUNNING);

    return genericRecordTopology;
  }

  private SpecificRecordTopology startSpecificTopology(String applicationId, String inputTopic,
      String outputTopic) {
    var specificRecordTopology = new SpecificRecordTopology(
        kafkaContainer.getBootstrapServers(),
        schemaRegistryContainer.getServiceURL(),
        applicationId,
        inputTopic,
        outputTopic);
    specificRecordTopology.start();

    await()
        .pollInterval(ONE_HUNDRED_MILLISECONDS)
        .atMost(ONE_MINUTE)
        .until(() -> specificRecordTopology.getState() == State.RUNNING);

    return specificRecordTopology;
  }

  private static void produce(String topic, String key, SpecificRecord value) {
    try (KafkaProducer<String, SpecificRecord> producer = createSpecificRecordKafkaProducer()) {
      final ProducerRecord<String, SpecificRecord> record = new ProducerRecord<>(topic, key, value);
      producer.send(record);
      producer.flush();
    } catch (final SerializationException e) {
      LOGGER.error(String.format(
          "Serialization exception occurred while trying to send message with key %s and value %s to the topic %s",
          key, value, topic), e);
    }
  }

  private static <T extends SpecificRecord> KafkaProducer<String, T> createSpecificRecordKafkaProducer() {
    final Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
    props.put(ProducerConfig.ACKS_CONFIG, "all");
    props.put(ProducerConfig.RETRIES_CONFIG, 0);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, KafkaAvroSerializer.class);
    props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
        schemaRegistryContainer.getServiceURL());
    props.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY,
        RecordNameStrategy.class.getName());

    return new KafkaProducer<>(props);
  }


  private <T extends SpecificRecord> List<ConsumerRecord<String, T>> dumpSpecificRecordTopic(
      String topic,
      int minMessageCount,
      long pollTimeoutMillis) {

    List<ConsumerRecord<String, T>> consumerRecords = new ArrayList<>();
    var consumerGroupId = UUID.randomUUID().toString();
    try (final KafkaConsumer<String, T> consumer = createSpecificRecordKafkaConsumer(
        consumerGroupId)) {

      // assign the consumer to all the partitions of the topic
      var topicPartitions = consumer.partitionsFor(topic).stream()
          .map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition()))
          .collect(Collectors.toList());
      consumer.assign(topicPartitions);

      var start = System.currentTimeMillis();
      while (true) {
        final ConsumerRecords<String, T> records = consumer
            .poll(Duration.ofMillis(POLL_INTERVAL_MS));

        records.forEach(consumerRecords::add);
        if (consumerRecords.size() >= minMessageCount) {
          break;
        }

        if (System.currentTimeMillis() - start > pollTimeoutMillis) {
          throw new IllegalStateException(
              String.format(
                  "Timed out while waiting for %d messages from the %s. Only %d messages received so far.",
                  minMessageCount, topic, consumerRecords.size()));
        }
      }
    }
    return consumerRecords;
  }


  private <T extends GenericRecord> List<ConsumerRecord<String, T>> dumpGenericRecordTopic(
      String topic,
      int minMessageCount,
      long pollTimeoutMillis) {

    List<ConsumerRecord<String, T>> consumerRecords = new ArrayList<>();
    var consumerGroupId = UUID.randomUUID().toString();
    try (final KafkaConsumer<String, T> consumer = createGenericRecordKafkaConsumer(
        consumerGroupId)) {

      // assign the consumer to all the partitions of the topic
      var topicPartitions = consumer.partitionsFor(topic).stream()
          .map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition()))
          .collect(Collectors.toList());
      consumer.assign(topicPartitions);

      var start = System.currentTimeMillis();
      while (true) {
        final ConsumerRecords<String, T> records = consumer
            .poll(Duration.ofMillis(POLL_INTERVAL_MS));

        records.forEach(consumerRecords::add);
        if (consumerRecords.size() >= minMessageCount) {
          break;
        }

        if (System.currentTimeMillis() - start > pollTimeoutMillis) {
          throw new IllegalStateException(
              String.format(
                  "Timed out while waiting for %d messages from the %s. Only %d messages received so far.",
                  minMessageCount, topic, consumerRecords.size()));
        }
      }
    }
    return consumerRecords;
  }

  private static <T extends SpecificRecord> KafkaConsumer<String, T> createSpecificRecordKafkaConsumer(
      String consumerGroupId) {
    final Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
        schemaRegistryContainer.getServiceURL());
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, SpecificAvroDeserializer.class);
    props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
    props.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY,
        RecordNameStrategy.class.getName());

    return new KafkaConsumer<>(props);
  }

  private static <T extends GenericRecord> KafkaConsumer<String, T> createGenericRecordKafkaConsumer(
      String consumerGroupId) {
    final Properties props = new Properties();
    props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());
    props.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
    props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "true");
    props.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, "1000");
    props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
    props.put(AbstractKafkaAvroSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG,
        schemaRegistryContainer.getServiceURL());
    props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, GenericAvroDeserializer.class);
    props.put(KafkaAvroDeserializerConfig.SPECIFIC_AVRO_READER_CONFIG, true);
    props.put(KafkaAvroSerializerConfig.VALUE_SUBJECT_NAME_STRATEGY,
        RecordNameStrategy.class.getName());

    return new KafkaConsumer<>(props);
  }


  private static AdminClient createAdminClient() {
    var properties = new Properties();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers());

    return KafkaAdminClient.create(properties);
  }

  private static void registerSchemaRegistryTypes() throws IOException, RestClientException {
    // the type BookmarkEvent needs to be registered manually in the Confluent schema registry
    // to setup the tests.
    LOGGER.info("Registering manually in the Schema Registry the types used in the tests");
    var schemaRegistryClient = new CachedSchemaRegistryClient(
        schemaRegistryContainer.getServiceURL(), 1000);
    schemaRegistryClient
        .register(BookmarkEvent.getClassSchema().getFullName(), BookmarkEvent.getClassSchema());
    schemaRegistryClient
        .register(UserCreatedEvent.getClassSchema().getFullName(),
            UserCreatedEvent.getClassSchema());

  }

  private static void createTopics(String... topics)
      throws InterruptedException, ExecutionException {
    try (var adminClient = createAdminClient()) {
      short replicationFactor = 1;
      int partitions = 1;

      LOGGER.info("Creating topics in Apache Kafka");

      adminClient.createTopics(
          Stream.of(topics)
              .map(topic -> new NewTopic(topic, partitions, replicationFactor))
              .collect(Collectors.toList())
      ).all().get();
    }
  }
}
