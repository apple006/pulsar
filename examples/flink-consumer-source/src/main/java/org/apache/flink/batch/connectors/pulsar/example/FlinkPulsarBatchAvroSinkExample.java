/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.flink.batch.connectors.pulsar.example;


import org.apache.flink.api.common.io.OutputFormat;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.batch.connectors.pulsar.PulsarAvroOutputFormat;
import org.apache.flink.batch.connectors.pulsar.avro.generated.NasaMission;

import java.util.Arrays;
import java.util.List;

/**
 * Implements a batch program on Pulsar topic by writing Flink DataSet as Avro.
 */
public class FlinkPulsarBatchAvroSinkExample {

    private static final List<NasaMission> nasaMissions = Arrays.asList(
            NasaMission.newBuilder().setId(1).setName("Mercury program").setStartYear(1959).setEndYear(1963).build(),
            NasaMission.newBuilder().setId(2).setName("Apollo program").setStartYear(1961).setEndYear(1972).build(),
            NasaMission.newBuilder().setId(3).setName("Gemini program").setStartYear(1963).setEndYear(1966).build(),
            NasaMission.newBuilder().setId(4).setName("Skylab").setStartYear(1973).setEndYear(1974).build(),
            NasaMission.newBuilder().setId(5).setName("Apollo–Soyuz Test Project").setStartYear(1975).setEndYear(1975).build());

    private static final String SERVICE_URL = "pulsar://127.0.0.1:6650";
    private static final String TOPIC_NAME = "my-flink-topic";

    public static void main(String[] args) throws Exception {

        // set up the execution environment
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();

        // create PulsarAvroOutputFormat instance
        final OutputFormat<NasaMission> pulsarAvroOutputFormat = new PulsarAvroOutputFormat<>(SERVICE_URL, TOPIC_NAME);

        // create DataSet
        DataSet<NasaMission> nasaMissionDS = env.fromCollection(nasaMissions);
        // map nasa mission names to upper-case
        nasaMissionDS.map(nasaMission -> new NasaMission(
                nasaMission.getId(),
                nasaMission.getName(),
                nasaMission.getStartYear(),
                nasaMission.getEndYear()))
                // filter missions which started after 1970
                .filter(nasaMission -> nasaMission.getStartYear() > 1970)
                // write batch data to Pulsar
                .output(pulsarAvroOutputFormat);

        // set parallelism to write Pulsar in parallel (optional)
        env.setParallelism(2);

        // execute program
        env.execute("Flink - Pulsar Batch Avro");
    }

}
