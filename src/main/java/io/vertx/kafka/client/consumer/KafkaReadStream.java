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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.Deserializer;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.streams.ReadStream;
import io.vertx.kafka.client.consumer.impl.KafkaReadStreamImpl;

/**
 * A {@link ReadStream} for consuming Kafka {@link ConsumerRecord}.
 * <p>
 * The {@link #pause()} and {@link #resume()} provides global control over reading the records from the consumer.
 * <p>
 * The {@link #pause(Set)} and {@link #resume(Set)} provides finer grained control over reading records
 * for specific Topic/Partition, these are Kafka's specific operations.
 *
 */
public interface KafkaReadStream<K, V> extends ReadStream<ConsumerRecord<K, V>> {

  /**
   * Returns the current demand.
   *
   * <ul>
   *   <i>If the stream is in <i>flowing</i> mode will return {@link Long#MAX_VALUE}.</i>
   *   <li>If the stream is in <i>fetch</i> mode, will return the current number of elements still to be delivered or 0 if paused.</li>
   * </ul>
   *
   * @return current demand
   */
  long demand();

  /**
   * Create a new KafkaReadStream instance
   *
   * @param vertx Vert.x instance to use
   * @param config  Kafka consumer configuration
   * @return  an instance of the KafkaReadStream
   */
  static <K, V> KafkaReadStream<K, V> create(Vertx vertx, Properties config) {
    return create(vertx, new org.apache.kafka.clients.consumer.KafkaConsumer<>(config));
  }

  /**
   * Create a new KafkaReadStream instance
   *
   * @param vertx Vert.x instance to use
   * @param config  Kafka consumer configuration
   * @param keyDeserializer key deserializer
   * @param valueDeserializer value deserializer
   * @return  an instance of the KafkaReadStream
   */
  static <K, V> KafkaReadStream<K, V> create(Vertx vertx, Properties config, Deserializer<K> keyDeserializer, Deserializer<V> valueDeserializer) {
    return create(vertx, new org.apache.kafka.clients.consumer.KafkaConsumer<>(config, keyDeserializer, valueDeserializer));
  }

  /**
   * Create a new KafkaReadStream instance
   *
   * @param vertx Vert.x instance to use
   * @param config  Kafka consumer configuration
   * @return  an instance of the KafkaReadStream
   */
  static <K, V> KafkaReadStream<K, V> create(Vertx vertx, Map<String, Object> config) {
    return create(vertx, new org.apache.kafka.clients.consumer.KafkaConsumer<>(config));
  }

  @Fluent
  @Override
  KafkaReadStream<K, V> exceptionHandler(Handler<Throwable> handler);

  @Fluent
  @Override
  KafkaReadStream<K, V> handler(@Nullable Handler<ConsumerRecord<K, V>> handler);

  @Fluent
  @Override
  KafkaReadStream<K, V> pause();

  @Fluent
  @Override
  KafkaReadStream<K, V> resume();

  @Fluent
  @Override
  KafkaReadStream<K, V> fetch(long amount);

  @Fluent
  @Override
  KafkaReadStream<K, V> endHandler(@Nullable Handler<Void> endHandler);

  /**
   * Create a new KafkaReadStream instance
   *
   * @param vertx Vert.x instance to use
   * @param config  Kafka consumer configuration
   * @param keyDeserializer key deserializer
   * @param valueDeserializer value deserializer
   * @return  an instance of the KafkaReadStream
   */
  static <K, V> KafkaReadStream<K, V> create(Vertx vertx, Map<String, Object> config, Deserializer<K> keyDeserializer, Deserializer<V> valueDeserializer) {
    return create(vertx, new org.apache.kafka.clients.consumer.KafkaConsumer<>(config, keyDeserializer, valueDeserializer));
  }

  /**
   * Create a new KafkaReadStream instance
   *
   * @param vertx Vert.x instance to use
   * @param consumer  native Kafka consumer instance
   * @return  an instance of the KafkaReadStream
   */
  static <K, V> KafkaReadStream<K, V> create(Vertx vertx, Consumer<K, V> consumer) {
    return new KafkaReadStreamImpl<>(vertx.getOrCreateContext(), consumer);
  }

  /**
   * Get the last committed offset for the given partition (whether the commit happened by this process or another).
   *
   * @param topicPartition  topic partition for getting last committed offset
   * @param handler handler called on operation completed
   */
  void committed(TopicPartition topicPartition, Handler<AsyncResult<OffsetAndMetadata>> handler);

  /**
   * Suspend fetching from the requested partitions.
   *
   * @param topicPartitions topic partition from which suspend fetching
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> pause(Set<TopicPartition> topicPartitions);

  /**
   * Suspend fetching from the requested partitions.
   * <p>
   * Due to internal buffering of messages,
   * the {@linkplain #handler(Handler) record handler} will
   * continue to observe messages from the given {@code topicPartitions}
   * until some time <em>after</em> the given {@code completionHandler}
   * is called. In contrast, the once the given {@code completionHandler}
   * is called the {@link #batchHandler(Handler)} will not see messages
   * from the given {@code topicPartitions}.
   *
   * @param topicPartitions topic partition from which suspend fetching
   * @param completionHandler handler called on operation completed
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> pause(Set<TopicPartition> topicPartitions, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Resume specified partitions which have been paused with pause.
   *
   * @param topicPartitions topic partition from which resume fetching
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> resume(Set<TopicPartition> topicPartitions);

  /**
   * Resume specified partitions which have been paused with pause.
   *
   * @param topicPartitions topic partition from which resume fetching
   * @param completionHandler handler called on operation completed
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> resume(Set<TopicPartition> topicPartitions, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Seek to the last offset for each of the given partitions.
   *
   * @param topicPartitions topic partition for which seek
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> seekToEnd(Set<TopicPartition> topicPartitions);

  /**
   * Seek to the last offset for each of the given partitions.
   * <p>
   * Due to internal buffering of messages,
   * the {@linkplain #handler(Handler) record handler} will
   * continue to observe messages fetched with respect to the old offset
   * until some time <em>after</em> the given {@code completionHandler}
   * is called. In contrast, the once the given {@code completionHandler}
   * is called the {@link #batchHandler(Handler)} will only see messages
   * consistent with the new offset.
   *
   * @param topicPartitions topic partition for which seek
   * @param completionHandler handler called on operation completed
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> seekToEnd(Set<TopicPartition> topicPartitions, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Seek to the first offset for each of the given partitions.
   *
   * @param topicPartitions topic partition for which seek
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> seekToBeginning(Set<TopicPartition> topicPartitions);

  /**
   * Seek to the first offset for each of the given partitions.
   * <p>
   * Due to internal buffering of messages,
   * the {@linkplain #handler(Handler) record handler} will
   * continue to observe messages fetched with respect to the old offset
   * until some time <em>after</em> the given {@code completionHandler}
   * is called. In contrast, the once the given {@code completionHandler}
   * is called the {@link #batchHandler(Handler)} will only see messages
   * consistent with the new offset.
   *
   * @param topicPartitions topic partition for which seek
   * @param completionHandler handler called on operation completed
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> seekToBeginning(Set<TopicPartition> topicPartitions, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Overrides the fetch offsets that the consumer will use on the next poll.
   *
   * @param topicPartition  topic partition for which seek
   * @param offset  offset to seek inside the topic partition
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> seek(TopicPartition topicPartition, long offset);

  /**
   * Overrides the fetch offsets that the consumer will use on the next poll.
   * <p>
   * Due to internal buffering of messages,
   * the {@linkplain #handler(Handler) record handler} will
   * continue to observe messages fetched with respect to the old offset
   * until some time <em>after</em> the given {@code completionHandler}
   * is called. In contrast, the once the given {@code completionHandler}
   * is called the {@link #batchHandler(Handler)} will only see messages
   * consistent with the new offset.
   *
   * @param topicPartition  topic partition for which seek
   * @param offset  offset to seek inside the topic partition
   * @param completionHandler handler called on operation completed
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> seek(TopicPartition topicPartition, long offset, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Set the handler called when topic partitions are revoked to the consumer
   *
   * @param handler handler called on revoked topic partitions
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> partitionsRevokedHandler(Handler<Set<TopicPartition>> handler);

  /**
   * Set the handler called when topic partitions are assigned to the consumer
   *
   * @param handler handler called on assigned topic partitions
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> partitionsAssignedHandler(Handler<Set<TopicPartition>> handler);

  /**
   * Subscribe to the given list of topics to get dynamically assigned partitions.
   *
   * @param topics  topics to subscribe to
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> subscribe(List<String> topics);

  /**
   * Subscribe to the given list of topics to get dynamically assigned partitions.
   * <p>
   * Due to internal buffering of messages, when changing the subscribed topics
   * the old set of topics may remain in effect
   * (as observed by the {@linkplain #handler(Handler)} record handler})
   * until some time <em>after</em> the given {@code completionHandler}
   * is called. In contrast, the once the given {@code completionHandler}
   * is called the {@link #batchHandler(Handler)} will only see messages
   * consistent with the new set of topics.
   *
   * @param topics  topics to subscribe to
   * @param completionHandler handler called on operation completed
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> subscribe(List<String> topics, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Subscribe to all topics matching specified pattern to get dynamically assigned partitions.
   * <p>
   * Due to internal buffering of messages, when changing the subscribed topics
   * the old set of topics may remain in effect
   * (as observed by the {@linkplain #handler(Handler)} record handler})
   * until some time <em>after</em> the given {@code completionHandler}
   * is called. In contrast, the once the given {@code completionHandler}
   * is called the {@link #batchHandler(Handler)} will only see messages
   * consistent with the new set of topics.
   *
   * @param pattern  Pattern to subscribe to
   * @param completionHandler handler called on operation completed
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> subscribe(Pattern pattern, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Subscribe to all topics matching specified pattern to get dynamically assigned partitions.
   *
   * @param pattern  Pattern to subscribe to
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> subscribe(Pattern pattern);

  /**
   * Unsubscribe from topics currently subscribed with subscribe.
   *
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> unsubscribe();

  /**
   * Unsubscribe from topics currently subscribed with subscribe.
   *
   * @param completionHandler handler called on operation completed
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> unsubscribe(Handler<AsyncResult<Void>> completionHandler);

  /**
   * Get the current subscription.
   *
   * @param handler handler called on operation completed
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> subscription(Handler<AsyncResult<Set<String>>> handler);

  /**
   * Manually assign a set of partitions to this consumer.
   *
   * @param partitions  partitions which want assigned
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> assign(List<TopicPartition> partitions);

  /**
   * Manually assign a set of partitions to this consumer.
   * <p>
   * Due to internal buffering of messages, when reassigning
   * the old set of partitions may remain in effect
   * (as observed by the {@linkplain #handler(Handler)} record handler)}
   * until some time <em>after</em> the given {@code completionHandler}
   * is called. In contrast, the once the given {@code completionHandler}
   * is called the {@link #batchHandler(Handler)} will only see messages
   * consistent with the new set of partitions.
   *
   * @param partitions  partitions which want assigned
   * @param completionHandler handler called on operation completed
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> assign(List<TopicPartition> partitions, Handler<AsyncResult<Void>> completionHandler);

  /**
   * Get the set of partitions currently assigned to this consumer.
   *
   * @param handler handler called on operation completed
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> assignment(Handler<AsyncResult<Set<TopicPartition>>> handler);

  /**
   * Get metadata about partitions for all topics that the user is authorized to view.
   *
   * @param handler handler called on operation completed
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> listTopics(Handler<AsyncResult<Map<String,List<PartitionInfo>>>> handler);

  /**
   * Commit current offsets for all the subscribed list of topics and partition.
   */
  void commit();

  /**
   * Commit current offsets for all the subscribed list of topics and partition.
   *
   * @param completionHandler handler called on operation completed
   */
  void commit(Handler<AsyncResult<Map<TopicPartition, OffsetAndMetadata>>> completionHandler);

  /**
   * Commit the specified offsets for the specified list of topics and partitions to Kafka.
   *
   * @param offsets offsets list to commit
   */
  void commit(Map<TopicPartition, OffsetAndMetadata> offsets);

  /**
   * Commit the specified offsets for the specified list of topics and partitions to Kafka.
   *
   * @param offsets offsets list to commit
   * @param completionHandler handler called on operation completed
   */
  void commit(Map<TopicPartition, OffsetAndMetadata> offsets, Handler<AsyncResult<Map<TopicPartition, OffsetAndMetadata>>> completionHandler);

  /**
   * Get metadata about the partitions for a given topic.
   *
   * @param topic topic partition for which getting partitions info
   * @param handler handler called on operation completed
   * @return  current KafkaReadStream instance
   */
  KafkaReadStream<K, V> partitionsFor(String topic, Handler<AsyncResult<List<PartitionInfo>>> handler);

  /**
   * Close the stream
   */
  default void close() {
    close(null);
  }

  /**
   * Close the stream
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
   * @return the underlying consumer
   */
  Consumer<K, V> unwrap();

  /**
   * Set the handler that will be called when a new batch of records is
   * returned from Kafka. Batch handlers need to take care not to block
   * the event loop when dealing with large batches. It is better to process
   * records individually using the {@link #handler(Handler) record handler}.
   *
   * @param handler handler called each time Kafka returns a batch of records.
   * @return current KafkaReadStream instance.
   */
  KafkaReadStream<K, V> batchHandler(Handler<ConsumerRecords<K, V>> handler);

  /**
   * Sets the poll timeout for the underlying native Kafka Consumer. Defaults to 1000 ms.
   * Setting timeout to a lower value results in a more 'responsive' client, because it will block for a shorter period
   * if no data is available in the assigned partition and therefore allows subsequent actions to be executed with a shorter
   * delay. At the same time, the client will poll more frequently and thus will potentially create a higher load on the Kafka Broker.
   *
   * @param timeout The time, spent waiting in poll if data is not available in the buffer.
   * If 0, returns immediately with any records that are available currently in the native Kafka consumer's buffer,
   * else returns empty. Must not be negative.
   */
  KafkaReadStream<K, V> pollTimeout(Duration timeout);

  /**
   * Sets the poll timeout (in ms) for the underlying native Kafka Consumer. Defaults to 1000.
   *
   * @deprecated use {@link #pollTimeout(Duration)}
   */
  @Deprecated
  KafkaReadStream<K, V> pollTimeout(long timeout);

  /**
   * Executes a poll for getting messages from Kafka
   *
   * @param timeout The time, in milliseconds, spent waiting in poll if data is not available in the buffer.
   *                If 0, returns immediately with any records that are available currently in the native Kafka consumer's buffer,
   *                else returns empty. Must not be negative.
   * @param handler handler called after the poll with batch of records (can be empty).
   *
   * @deprecated use {@link #poll(Duration, Handler)}
   */
  @Deprecated
  void poll(long timeout, Handler<AsyncResult<ConsumerRecords<K, V>>> handler);

  /**
   * Executes a poll for getting messages from Kafka.
   *
   * @param timeout The maximum time to block (must not be greater than {@link Long#MAX_VALUE} milliseconds)
   * @param handler handler called after the poll with batch of records (can be empty).
   */
  void poll(Duration timeout, Handler<AsyncResult<ConsumerRecords<K, V>>> handler);
}
