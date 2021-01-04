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

package io.vertx.kafka.client.tests;

import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.apache.kafka.clients.consumer.OffsetResetStrategy;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.kafka.client.consumer.KafkaReadStream;
import io.vertx.kafka.client.producer.KafkaWriteStream;
import io.vertx.kafka.client.serialization.BufferDeserializer;
import io.vertx.kafka.client.serialization.BufferSerializer;

/**
 * Codec tests
 */
public class CodecsTest extends KafkaClusterTestBase {

  final private String topic = "the_topic";
  private Vertx vertx;
  private KafkaWriteStream<?, ?> producer;
  private KafkaReadStream<?, ?> consumer;

  @Before
  public void beforeTest() {
    vertx = Vertx.vertx();
  }

  @After
  public void afterTest(TestContext ctx) {
    close(ctx, producer);
    close(ctx, consumer);
    vertx.close(ctx.asyncAssertSuccess());
  }


  @Test
  public void testBufferCodecString(TestContext ctx) throws Exception {
    testCodec(ctx,
      "testBufferCodecString",
      cfg -> {
        cfg.put("key.serializer", BufferSerializer.class);
        cfg.put("value.serializer", BufferSerializer.class);
        return KafkaWriteStream.create(vertx, cfg);
      },
      cfg -> {
        cfg.put("key.deserializer", BufferDeserializer.class);
        cfg.put("value.deserializer", BufferDeserializer.class);
        return KafkaReadStream.create(vertx, cfg);
      },
      i -> Buffer.buffer("key-" + i),
      i -> Buffer.buffer("value-" + i));
  }

  private <K, V> void testCodec(TestContext ctx,
                                String prefix,
                                Function<Properties,KafkaWriteStream<K, V>> producerFactory,
                                Function<Properties, KafkaReadStream<K, V>> consumerFactory,
                                Function<Integer, K> keyConv,
                                Function<Integer, V> valueConv) throws Exception {
    Properties producerConfig = kafkaCluster.useTo().getProducerProperties(prefix+"the_producer");
    KafkaWriteStream<K, V> writeStream = producerFactory.apply(producerConfig);
    producer = writeStream;
    writeStream.exceptionHandler(ctx::fail);
    int numMessages = 100000;
    ConcurrentLinkedDeque<K> keys = new ConcurrentLinkedDeque<K>();
    ConcurrentLinkedDeque<V> values = new ConcurrentLinkedDeque<V>();
    for (int i = 0;i < numMessages;i++) {
      K key = keyConv.apply(i);
      V value = valueConv.apply(i);
      keys.add(key);
      values.add(value);
      writeStream.write(new ProducerRecord<>(prefix + topic, 0, key, value));
    }
    Async done = ctx.async();
    Properties consumerConfig = kafkaCluster.useTo().getConsumerProperties(prefix+"the_consumer", prefix+"the_consumer", OffsetResetStrategy.EARLIEST);
    KafkaReadStream<K, V> readStream = consumerFactory.apply(consumerConfig);
    consumer = readStream;
    AtomicInteger count = new AtomicInteger(numMessages);
    readStream.exceptionHandler(ctx::fail);
    readStream.handler(rec -> {
      ctx.assertEquals(keys.pop(), rec.key());
      ctx.assertEquals(values.pop(), rec.value());
      if (count.decrementAndGet() == 0) {
        done.complete();
      }
    });
    readStream.subscribe(Arrays.asList(prefix + topic));
  }
}
