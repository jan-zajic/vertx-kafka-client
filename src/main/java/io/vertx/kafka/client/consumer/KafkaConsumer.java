/*
 * Copyright 2016 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.vertx.kafka.client.consumer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.kafka.clients.consumer.Consumer;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.streams.ReadStream;
import io.vertx.kafka.client.common.PartitionInfo;
import io.vertx.kafka.client.common.TopicPartition;
import io.vertx.kafka.client.consumer.impl.KafkaConsumerImpl;

/**
 * Vert.x Kafka consumer.
 * <p>
 * You receive Kafka records by providing a {@link KafkaConsumer#handler(Handler)}. As messages arrive the handler
 * will be called with the records.
 * <p>
 * The {@link #pause()} and {@link #resume()} provides global control over reading the records from the consumer.
 * <p>
 * The {@link #pause(Set)} and {@link #resume(Set)} provides finer grained control over reading records
 * for specific Topic/Partition, these are Kafka's specific operations.
 */
@VertxGen
public interface KafkaConsumer<K, V> extends ReadStream<KafkaConsumerRecord<K, V>> {

  /**
   * Create a new KafkaConsumer instance from a native {@link Consumer}.
   *
   * @param vertx Vert.x instance to use
   * @param consumer the Kafka consumer to wrap
   * @return  an instance of the KafkaConsumer
   */
  @GenIgnore
  static <K, V> KafkaConsumer<K, V> create(Vertx vertx, Consumer<K, V> consumer) {
    KafkaReadStream<K, V> stream = KafkaReadStream.create(vertx, consumer);
    return new KafkaConsumerImpl<K, V>(stream);
  }

  /**
   * Create a new KafkaConsumer instance
   *
   * @param vertx Vert.x instance to use
   * @param config Kafka consumer configuration
   * @return  an instance of the KafkaConsumer
   */
  static <K, V> KafkaConsumer<K, V> create(Vertx vertx, Map<String, String> config) {
    KafkaReadStream<K, V> stream = KafkaReadStream.create(vertx, new HashMap<>(config));
    return new KafkaConsumerImpl<>(stream).registerCloseHook();
  }

  /**
   * Create a new KafkaConsumer instance
   *
   * @param vertx Vert.x instance to use
   * @param config Kafka consumer configuration
   * @return  an instance of the KafkaConsumer
   */
  @GenIgnore
  static <K, V> KafkaConsumer<K, V> create(Vertx vertx, Properties config) {
    KafkaReadStream<K, V> stream = KafkaReadStream.create(vertx, config);
    return new KafkaConsumerImpl<>(stream).registerCloseHook();
  }

  @Fluent
  @Override
  KafkaConsumer<K, V> exceptionHandler(Handler<Throwable> handler);

  @Fluent
  @Override
  KafkaConsumer<K, V> handler(Handler<KafkaConsumerRecord<K, V>> handler);

  @Fluent
  @Override
  KafkaConsumer<K, V> pause();

  @Fluent
  @Override
  KafkaConsumer<K, V> resume();

  @Fluent
  @Override
  KafkaConsumer<K, V> endHandler(Handler<Void> endHandler);

  /**
   * Subscribe to the given topic to get dynamically assigned partitions.
   *
   * @param topic  topic to subscribe to
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> subscribe(String topic);

  /**
   * Subscribe to the given list of topics to get dynamically assigned partitions.
   *
   * @param topics  topics to subscribe to
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> subscribe(Set<String> topics);

  /**
   * Subscribe to the given topic to get dynamically assigned partitions.
   *
   * @param topic  topic to subscribe to
   * @param completionHandler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> subscribe(String topic, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Subscribe to the given list of topics to get dynamically assigned partitions.
   *
   * @param topics  topics to subscribe to
   * @param completionHandler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> subscribe(Set<String> topics, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Manually assign a partition to this consumer.
   *
   * @param topicPartition  partition which want assigned
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> assign(TopicPartition topicPartition);

  /**
   * Manually assign a list of partition to this consumer.
   *
   * @param topicPartitions  partitions which want assigned
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> assign(Set<TopicPartition> topicPartitions);

  /**
   * Manually assign a partition to this consumer.
   *
   * @param topicPartition  partition which want assigned
   * @param completionHandler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> assign(TopicPartition topicPartition, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Manually assign a list of partition to this consumer.
   *
   * @param topicPartitions  partitions which want assigned
   * @param completionHandler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> assign(Set<TopicPartition> topicPartitions, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Get the set of partitions currently assigned to this consumer.
   *
   * @param handler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> assignment(Handler<AsyncResult<Set<TopicPartition>>> handler);

  /**
   * Get metadata about partitions for all topics that the user is authorized to view.
   *
   * @param handler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  @GenIgnore
  KafkaConsumer<K, V> listTopics(Handler<AsyncResult<Map<String, List<PartitionInfo>>>> handler);

  /**
   * Unsubscribe from topics currently subscribed with subscribe.
   *
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> unsubscribe();

  /**
   * Unsubscribe from topics currently subscribed with subscribe.
   *
   * @param completionHandler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> unsubscribe(Handler<AsyncResult<Void>> completionHandler);

  /**
   * Get the current subscription.
   *
   * @param handler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> subscription(Handler<AsyncResult<Set<String>>> handler);

  /**
   * Suspend fetching from the requested partition.
   *
   * @param topicPartition topic partition from which suspend fetching
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> pause(TopicPartition topicPartition);

  /**
   * Suspend fetching from the requested partitions.
   *
   * @param topicPartitions topic partition from which suspend fetching
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> pause(Set<TopicPartition> topicPartitions);

  /**
   * Suspend fetching from the requested partition.
   *
   * @param topicPartition topic partition from which suspend fetching
   * @param completionHandler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> pause(TopicPartition topicPartition, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Suspend fetching from the requested partitions.
   *
   * @param topicPartitions topic partition from which suspend fetching
   * @param completionHandler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> pause(Set<TopicPartition> topicPartitions, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Resume specified partition which have been paused with pause.
   *
   * @param topicPartition topic partition from which resume fetching
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> resume(TopicPartition topicPartition);

  /**
   * Resume specified partitions which have been paused with pause.
   *
   * @param topicPartitions topic partition from which resume fetching
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> resume(Set<TopicPartition> topicPartitions);

  /**
   * Resume specified partition which have been paused with pause.
   *
   * @param topicPartition topic partition from which resume fetching
   * @param completionHandler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> resume(TopicPartition topicPartition, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Resume specified partitions which have been paused with pause.
   *
   * @param topicPartitions topic partition from which resume fetching
   * @param completionHandler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> resume(Set<TopicPartition> topicPartitions, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Set the handler called when topic partitions are revoked to the consumer
   *
   * @param handler handler called on revoked topic partitions
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> partitionsRevokedHandler(Handler<Set<TopicPartition>> handler);

  /**
   * Set the handler called when topic partitions are assigned to the consumer
   *
   * @param handler handler called on assigned topic partitions
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> partitionsAssignedHandler(Handler<Set<TopicPartition>> handler);

  /**
   * Overrides the fetch offsets that the consumer will use on the next poll.
   *
   * @param topicPartition  topic partition for which seek
   * @param offset  offset to seek inside the topic partition
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> seek(TopicPartition topicPartition, long offset);

  /**
   * Overrides the fetch offsets that the consumer will use on the next poll.
   *
   * @param topicPartition  topic partition for which seek
   * @param offset  offset to seek inside the topic partition
   * @param completionHandler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> seek(TopicPartition topicPartition, long offset, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Seek to the first offset for each of the given partition.
   *
   * @param topicPartition topic partition for which seek
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> seekToBeginning(TopicPartition topicPartition);

  /**
   * Seek to the first offset for each of the given partitions.
   *
   * @param topicPartitions topic partition for which seek
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> seekToBeginning(Set<TopicPartition> topicPartitions);

  /**
   * Seek to the first offset for each of the given partition.
   *
   * @param topicPartition topic partition for which seek
   * @param completionHandler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> seekToBeginning(TopicPartition topicPartition, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Seek to the first offset for each of the given partitions.
   *
   * @param topicPartitions topic partition for which seek
   * @param completionHandler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> seekToBeginning(Set<TopicPartition> topicPartitions, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Seek to the last offset for each of the given partition.
   *
   * @param topicPartition topic partition for which seek
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> seekToEnd(TopicPartition topicPartition);

  /**
   * Seek to the last offset for each of the given partitions.
   *
   * @param topicPartitions topic partition for which seek
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> seekToEnd(Set<TopicPartition> topicPartitions);

  /**
   * Seek to the last offset for each of the given partition.
   *
   * @param topicPartition topic partition for which seek
   * @param completionHandler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> seekToEnd(TopicPartition topicPartition, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Seek to the last offset for each of the given partitions.
   *
   * @param topicPartitions topic partition for which seek
   * @param completionHandler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> seekToEnd(Set<TopicPartition> topicPartitions, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Commit current offsets for all the subscribed list of topics and partition.
   */
  void commit();

  /**
   * Commit current offsets for all the subscribed list of topics and partition.
   *
   * @param completionHandler handler called on operation completed
   */
  void commit(Handler<AsyncResult<Void>> completionHandler);

  /**
   * Commit the specified offsets for the specified list of topics and partitions to Kafka.
   *
   * @param offsets offsets list to commit
   */
  @GenIgnore
  void commit(Map<TopicPartition, OffsetAndMetadata> offsets);

  /**
   * Commit the specified offsets for the specified list of topics and partitions to Kafka.
   *
   * @param offsets offsets list to commit
   * @param completionHandler handler called on operation completed
   */
  @GenIgnore
  void commit(Map<TopicPartition, OffsetAndMetadata> offsets, Handler<AsyncResult<Map<TopicPartition, OffsetAndMetadata>>> completionHandler);

  /**
   * Get the last committed offset for the given partition (whether the commit happened by this process or another).
   *
   * @param topicPartition  topic partition for getting last committed offset
   * @param handler handler called on operation completed
   */
  void committed(TopicPartition topicPartition, Handler<AsyncResult<OffsetAndMetadata>> handler);

  /**
   * Get metadata about the partitions for a given topic.
   *
   * @param topic topic partition for which getting partitions info
   * @param handler handler called on operation completed
   * @return  current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> partitionsFor(String topic, Handler<AsyncResult<List<PartitionInfo>>> handler);
  
  /**
   * Set the handler to be used when batches of messages are fetched 
   * from the Kafka server. Batch handlers need to take care not to block 
   * the event loop when dealing with large batches. It is better to process
   * records individually using the {@link #handler(Handler) record handler}.
   * @param handler handler called when batches of messages are fetched
   * @return current KafkaConsumer instance
   */
  @Fluent
  KafkaConsumer<K, V> batchHandler(Handler<KafkaConsumerRecords<K, V>> handler);

  /**
   * Close the consumer
   */
  default void close() {
    close(null);
  }

  /**
   * Close the consumer
   *
   * @param completionHandler handler called on operation completed
   */
  void close(Handler<AsyncResult<Void>> completionHandler);

  /**
   * Get the offset of the next record that will be fetched (if a record with that offset exists).
   *
   * @param partition The partition to get the position for
   * @param handler handler called on operation completed
   */
  void position(TopicPartition partition, Handler<AsyncResult<Long>> handler);

  /**
   * @return  underlying the {@link KafkaReadStream} instance
   */
  @GenIgnore
  KafkaReadStream<K, V> asStream();

  /**
   * @return the underlying consumer
   */
  @GenIgnore
  Consumer<K, V> unwrap();
}
