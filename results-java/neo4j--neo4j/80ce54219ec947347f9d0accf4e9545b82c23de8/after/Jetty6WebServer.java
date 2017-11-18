/**
 * Copyright (c) 2002-2010 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.neo4j.server.web;

import java.net.URL;
import java.util.HashMap;
import java.util.List;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.servlet.Context;
import org.mortbay.jetty.servlet.ServletHolder;
import org.mortbay.jetty.webapp.WebAppContext;
import org.mortbay.resource.Resource;
import org.mortbay.thread.QueuedThreadPool;

import com.sun.jersey.spi.container.servlet.ServletContainer;

public class Jetty6WebServer implements WebServer {

    private Server jetty;
    private int jettyPort = 80;

    private HashMap<String, String> staticContent = new HashMap<String, String>();
    private HashMap<String, ServletHolder> jaxRSPackages = new HashMap<String, ServletHolder>();

    public void start() {
        jetty = new Server(jettyPort);
        jetty.setStopAtShutdown(true);

        loadStaticContent();
        loadJAXRSPackages();

        try {
            jetty.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            jetty.stop();
            jetty.join();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setPort(int portNo) {
        jettyPort = portNo;
    }

    public void setMaxThreads(int maxThreads) {
        jetty.setThreadPool(new QueuedThreadPool(maxThreads));
    }

    public void addJAXRSPackages(List<String> packageNames, String serverMountPoint) {
        ServletHolder servletHolder = new ServletHolder(ServletContainer.class);
        servletHolder.setInitParameter("com.sun.jersey.config.property.packages", toCommaSeparatedList(packageNames));
        jaxRSPackages.put(serverMountPoint, servletHolder);
    }

    public void addStaticContent(String contentLocation, String serverMountPoint) {
        staticContent.put(serverMountPoint, contentLocation);
    }

    private void loadStaticContent() {
        for (String mountPoint : staticContent.keySet()) {
            String contentLocation = staticContent.get(mountPoint);
            try {
                final WebAppContext webadmin = new WebAppContext();
                webadmin.setServer(jetty);
                webadmin.setContextPath(mountPoint);
                URL url = getClass().getClassLoader().getResource(contentLocation).toURI().toURL();
                final Resource resource = Resource.newResource(url);
                webadmin.setBaseResource(resource);
                jetty.addHandler(webadmin);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void loadJAXRSPackages() {
        for(String mountPoint : jaxRSPackages.keySet()) {
             ServletHolder servletHolder = jaxRSPackages.get(mountPoint);
             Context jerseyContext = new Context(jetty, mountPoint);
             jerseyContext.addServlet(servletHolder, "/*");
        }
    }

    private String toCommaSeparatedList(List<String> packageNames) {
        StringBuilder sb = new StringBuilder();

        for(String str : packageNames) {
            sb.append(str);
            sb.append(", ");
        }

        String result = sb.toString();
        return result.substring(0, result.length() -2);
    }
}