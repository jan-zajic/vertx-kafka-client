/*
 * Copyright 2017 Red Hat Inc.
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

/**
 * = Vert.x Kafka AdminUtils
 * :toc: left
 * :lang: $lang
 * :$lang: $lang
 *
 * This component provides a vert.x wrapper around the most important functions of Kafka's AdminUtils.
 * AdminUtils are used to create, modify, and delete topics. Other functionality covered by AdminUtils,
 * but not this wrapper, includes Partition Management, Broker Configuration management, etc.
 *
 * == Using the AdminUtils
 *
 * === Create a topic
 *
 * You can call {@link io.vertx.kafka.admin.AdminUtils#createTopic} to create a topic.
 * Parameters are: topic name, number of partitions, number of replicas, and the usual callback to handle the result.
 * It might return an error, e.g. if the number of requested replicas is greater than the number of brokers.
 *
 *
 * === Delete a topic
 *
 * You can call {@link io.vertx.kafka.admin.AdminUtils#deleteTopic} to delete a topic.
 * Parameters are: topic name, and the usual callback to handle the result.
 * It might return an error, e.g. if the topic does not exist.
 *
 *
 * === Change a topic's configuration
 *
 * If you need to update the configuration of a topic, e.g., you want to update the retention policy,
 * you can call {@link io.vertx.kafka.admin.AdminUtils#changeTopicConfig} to update a topic.
 * Parameters are: topic name, a Map (String -> String) with parameters to be changed,
 * and the usual callback to handle the result.
 * It might return an error, e.g. if the topic does not exist.
 *
 *
 * === Check if a topic exists
 *
 * If you want to check if a topic exists, you can call {@link io.vertx.kafka.admin.AdminUtils#topicExists}.
 * Parameters are: topic name, and the usual callback to handle the result.
 * It might return an error, e.g. if the topic does not exist.
 *
 */
@Document(fileName = "adminUtils.adoc")
@ModuleGen(name = "vertx-kafka-client", groupPackage = "io.vertx")
package io.vertx.kafka.admin;

import io.vertx.codegen.annotations.ModuleGen;
import io.vertx.docgen.Document;
