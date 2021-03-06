/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.aries.jax.rs.whiteboard.activator;

import java.util.Dictionary;
import java.util.Hashtable;

import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.ext.RuntimeDelegate;

import org.apache.aries.jax.rs.whiteboard.internal.ClientBuilderFactory;
import org.apache.aries.jax.rs.whiteboard.internal.Whiteboard;
import org.apache.aries.osgi.functional.OSGi;
import org.apache.aries.osgi.functional.OSGiResult;
import org.apache.cxf.jaxrs.impl.RuntimeDelegateImpl;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.aries.jax.rs.whiteboard.internal.Whiteboard.createWhiteboard;
import static org.apache.aries.osgi.functional.OSGi.configurations;
import static org.apache.aries.osgi.functional.OSGi.register;

public class CXFJaxRsBundleActivator implements BundleActivator {

    private static final Logger _log = LoggerFactory.getLogger(
        CXFJaxRsBundleActivator.class);

    static {
        RuntimeDelegate.setInstance(new RuntimeDelegateImpl());
    }

    private OSGiResult<?> _defaultOSGiResult;
    private OSGiResult<?> _whiteboardsResult;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        if (_log.isDebugEnabled()) {
            _log.debug("Starting the whiteboard factory");
        }

        OSGi<?> whiteboards =
            configurations("org.apache.aries.jax.rs.whiteboard").
                flatMap(Whiteboard::createWhiteboard);

        _whiteboardsResult = whiteboards.run(bundleContext);

        Dictionary<String, Object> defaultConfiguration = new Hashtable<>();

        defaultConfiguration.put(
            Constants.SERVICE_PID,
            "org.apache.aries.jax.rs.whiteboard.default");

        _defaultOSGiResult =
            register(
                ClientBuilder.class, new ClientBuilderFactory(), null).then(
            createWhiteboard(defaultConfiguration))
        .run(bundleContext);

        if (_log.isDebugEnabled()) {
            _log.debug("Whiteboard factory started");
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        if (_log.isDebugEnabled()) {
            _log.debug("Stopping whiteboard factory");
        }

        _defaultOSGiResult.close();

        _whiteboardsResult.close();

        if (_log.isDebugEnabled()) {
            _log.debug("Stopped whiteboard factory");
        }
    }

}