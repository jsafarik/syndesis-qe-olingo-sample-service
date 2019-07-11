/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.jts.trippin.web;

import java.io.File;
import java.io.IOException;
import java.lang.Override;
import java.lang.RuntimeException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.jts.trippin.service.*;
import com.jts.trippin.data.Storage;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.startup.Tomcat;
import org.apache.olingo.commons.api.edm.Edm;
import org.apache.olingo.server.api.OData;
import org.apache.olingo.server.api.ODataHttpHandler;
import org.apache.olingo.server.api.ServiceMetadata;
import org.apache.olingo.server.api.processor.EntityCollectionProcessor;

@Slf4j
public class TripPinServlet extends HttpServlet {

    private static final int serialVersionUID = 1;

    private Storage storage;

    public static void main(String[] args) throws LifecycleException {
        String webAppDirLocation = "src/main/webapp/";
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8082);
        tomcat.getConnector();
        StandardContext ctx = (StandardContext) tomcat.addWebapp("", new File(webAppDirLocation).getAbsolutePath());
        log.info(new File(webAppDirLocation).getAbsolutePath());
        tomcat.start();
        tomcat.getServer();
    }

    private Storage getStorage(OData odata, Edm edm) {
        if (this.storage == null) {
            this.storage = new Storage(odata, edm);
        }
        return this.storage;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        OData odata = OData.newInstance();
        ServiceMetadata edm = odata.createServiceMetadata(new DemoEdmProvider(), new ArrayList<>());
        try {
            HttpSession session = req.getSession(true);
            log.info("Session id: " + session.getId());
            Storage storage = getStorage(odata, edm.getEdm());
            //Storage storage = (Storage) session.getAttribute(Storage.class.getName());
            if (storage == null) {
                storage = new Storage(odata, edm.getEdm());
                session.setAttribute(Storage.class.getName(), storage);
            }

            log.info("Received request: " + req.getMethod() + ": " + req.getRequestURI() + (req.getQueryString() == null ? "" : "?" + req.getQueryString()));

            // create odata handler and configure it with EdmProvider and Processors
            ODataHttpHandler handler = odata.createHandler(edm);
            handler.register(new DemoEntityCollectionProcessor(storage));
            handler.register(new CustomEntityProcessor(storage));
            handler.register(new DemoPrimitiveProcessor(storage));
            handler.register(new DemoActionProcessor(storage));
            handler.register(new DemoBatchProcessor(storage));
            handler.register(new CustomDefaultProcessor());

            // let the handler do the work
            handler.process(req, resp);
        } catch (RuntimeException e) {
            log.error("Server Error occurred in ExampleServlet", e);
            throw new ServletException(e);
        }

    }

}
