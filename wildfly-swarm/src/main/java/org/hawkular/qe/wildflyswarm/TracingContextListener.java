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
package org.hawkular.qe.wildflyswarm;

import io.opentracing.Tracer;
import io.opentracing.util.GlobalTracer;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.logging.Logger;

@WebListener
public class TracingContextListener implements ServletContextListener {
    private static Logger logger = Logger.getLogger(TracingContextListener.class.getName());

    @Inject
    private io.opentracing.Tracer tracer;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        GlobalTracer.register(tracer);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {}

    @Produces
    @Singleton
    public static io.opentracing.Tracer jaegerTracer() {
        Tracer tracer = org.hawkular.qe.common.TracerUtil.jaegerTracer();

        logger.info(">>>>>> Returning tracer " + tracer.toString());
        return tracer;
    }
}