/*
 * Copyright 2015-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.qe.common;

import io.jaegertracing.reporters.CompositeReporter;
import io.jaegertracing.reporters.LoggingReporter;
import io.jaegertracing.reporters.RemoteReporter;
import io.jaegertracing.reporters.Reporter;
import io.jaegertracing.samplers.ConstSampler;
import io.jaegertracing.samplers.ProbabilisticSampler;
import io.jaegertracing.samplers.Sampler;
import io.jaegertracing.senders.HttpSender;
import io.jaegertracing.senders.Sender;
import io.jaegertracing.senders.UdpSender;
import io.opentracing.Tracer;
import io.opentracing.noop.NoopTracerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class TracerUtil {
    private static final Map<String, String> envs = System.getenv();

    private static final Integer JAEGER_FLUSH_INTERVAL = new Integer(envs.getOrDefault("JAEGER_FLUSH_INTERVAL", "100"));
    private static final Integer JAEGER_MAX_PACKET_SIZE = new Integer(envs.getOrDefault("JAEGER_MAX_PACKET_SIZE", "0"));
    private static final Integer JAEGER_MAX_QUEUE_SIZE = new Integer(envs.getOrDefault("JAEGER_MAX_QUEUE_SIZE", "50"));
    private static final Double JAEGER_SAMPLING_RATE = new Double(envs.getOrDefault("JAEGER_SAMPLING_RATE", "1.0"));
    private static final Integer JAEGER_UDP_PORT = new Integer(envs.getOrDefault("JAEGER_UDP_PORT", "6831"));
    private static final String JAEGER_COLLECTOR_PORT = envs.getOrDefault("JAEGER_COLLECTOR_PORT", "14268");
    private static final String JAEGER_AGENT_HOST = envs.getOrDefault("JAEGER_AGENT_HOST", "localhost");
    private static final String JAEGER_COLLECTOR_HOST = envs.getOrDefault("JAEGER_COLLECTOR_HOST", "localhost");
    private static final String USE_AGENT_OR_COLLECTOR = envs.getOrDefault("USE_AGENT_OR_COLLECTOR", "AGENT");
    private static final String TRACER_TYPE = envs.getOrDefault("TRACER_TYPE", "jaeger");
    private static final String TEST_SERVICE_NAME = envs.getOrDefault("TEST_SERVICE_NAME", "vertx-opentracing-demo");
    private static final String USE_LOGGING_REPORTER = envs.getOrDefault("USE_LOGGING_REPORTER", "false");

    private static final Logger logger = LoggerFactory.getLogger(TracerUtil.class.getName());

    public static Tracer jaegerTracer() {
        Tracer tracer;
        Sender sender;
        CompositeReporter compositeReporter;

        if (TRACER_TYPE.equalsIgnoreCase("jaeger")) {
            if (USE_AGENT_OR_COLLECTOR.equalsIgnoreCase("agent")) {
                sender = new UdpSender(JAEGER_AGENT_HOST, JAEGER_UDP_PORT, JAEGER_MAX_PACKET_SIZE);
                logger.info("Using JAEGER tracer using agent on host [" + JAEGER_AGENT_HOST + "] port [" + JAEGER_UDP_PORT +
                        "] Service Name [" + TEST_SERVICE_NAME + "] Sampling rate [" + JAEGER_SAMPLING_RATE
                        + "] Max queue size: [" + JAEGER_MAX_QUEUE_SIZE + "]");
            } else {
                // use the collector
                String httpEndpoint = "http://" + JAEGER_COLLECTOR_HOST + ":" + JAEGER_COLLECTOR_PORT + "/api/traces";
                sender = new HttpSender(httpEndpoint);
                logger.info("Using JAEGER tracer using collector on host [" + JAEGER_COLLECTOR_HOST + "] port [" + JAEGER_COLLECTOR_PORT +
                        "] Service Name [" + TEST_SERVICE_NAME + "] Sampling rate [" + JAEGER_SAMPLING_RATE
                        + "] Max queue size: [" + JAEGER_MAX_QUEUE_SIZE + "]");
            }

            RemoteReporter remoteReporter = new RemoteReporter.Builder()
                    .withSender(sender)
                    .withFlushInterval(JAEGER_FLUSH_INTERVAL)
                    .withMaxQueueSize(JAEGER_MAX_QUEUE_SIZE)
                    .build();

            if (USE_LOGGING_REPORTER.equalsIgnoreCase("true")) {
                Reporter loggingRepoter = new LoggingReporter(logger);
                compositeReporter = new CompositeReporter(remoteReporter, loggingRepoter);
            } else {
                compositeReporter = new CompositeReporter(remoteReporter);
            }

            Sampler sampler = new ConstSampler(true);
            tracer = new io.jaegertracing.Tracer.Builder(TEST_SERVICE_NAME)
                    .withReporter(compositeReporter)
                    .withSampler(sampler)
                    .build();

            return tracer;
        } else {
            logger.info("Using NOOP Tracer");
            tracer = NoopTracerFactory.create();
        }

        return tracer;
    }

}
