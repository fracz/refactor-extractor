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

package org.neo4j.server;

import java.io.File;

import org.apache.commons.configuration.Configuration;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.server.configuration.Configurator;
import org.neo4j.server.configuration.validation.DatabaseLocationMustBeSpecifiedRule;
import org.neo4j.server.configuration.validation.Validator;
import org.neo4j.server.database.Database;
import org.neo4j.server.logging.Logger;
import org.neo4j.server.startup.healthcheck.StartupHealthCheckFailedException;
import org.neo4j.server.startup.healthcheck.ConfigFileMustBePresentRule;
import org.neo4j.server.startup.healthcheck.StartupHealthCheck;
import org.neo4j.server.web.Jetty6WebServer;
import org.neo4j.server.web.WebServer;
import org.tanukisoftware.wrapper.WrapperListener;

import com.sun.tools.javac.util.List;

/**
 * Application entry point for the Neo4j Server.
 */
public class NeoServer implements WrapperListener {
    private static final String WEB_ADMIN_REST_API_PACKAGE = "org.neo4j.webadmin.rest";

    public static final String REST_API_PATH = "/rest/api";
    public static final String REST_API_PACKAGE = "org.neo4j.rest.web";

    public static final Logger log = Logger.getLogger(NeoServer.class);

    public static final String NEO_CONFIG_FILE_PROPERTY = "org.neo4j.server.properties";
    public static final String DEFAULT_NEO_CONFIGDIR = File.separator + "etc" + File.separator + "neo";

    private static final String DATABASE_LOCATION = "org.neo4j.database.location";
    private static final String WEBSERVER_PORT = "org.neo4j.webserver.port";
    private static final int DEFAULT_WEBSERVER_PORT = 7474;

    private Configurator configurator;
    private Database database;
    private WebServer webServer;

    private int webServerPort;

    /**
     * For test purposes only.
     */
    NeoServer(Configurator configurator, Database db, WebServer ws) {
        this.configurator = configurator;
        this.database = db;
        this.webServer = ws;
    }

    public NeoServer() {
        StartupHealthCheck healthCheck = new StartupHealthCheck(new ConfigFileMustBePresentRule());
        if(!healthCheck.run()) {
            throw new StartupHealthCheckFailedException("Startup healthcheck failed, server is not properly configured. Check logs for details.");
        }

        this.configurator = new Configurator(new Validator(new DatabaseLocationMustBeSpecifiedRule()), getConfigFile());
        this.webServer = new Jetty6WebServer();
        this.database = new Database(configurator.configuration().getString(DATABASE_LOCATION));
    }

    public Integer start(String[] args) {
        webServerPort = configurator.configuration().getInt(WEBSERVER_PORT, DEFAULT_WEBSERVER_PORT);
        try {
            webServer.setPort(webServerPort);
            webServer.addJAXRSPackages(List.from(new String[] {REST_API_PACKAGE}), REST_API_PATH);

            // webadmin assumes root
            webServer.addStaticContent("html", "/webadmin");
            webServer.addJAXRSPackages(List.from(new String[] {WEB_ADMIN_REST_API_PACKAGE}), "/");

            webServer.start();

            log.info("Started Neo Server on port [%s]", webServerPort);

            return 0;
        } catch (Exception e) {
            log.error("Failed to start Neo Server on port [%s]", webServerPort);
            return 1;
        }
    }

    protected void stop() {
        stop(0);
    }

    public int stop(int stopArg) {
        String location = "unknown";
        try {
            if (database != null) {
                location = database.getLocation();
                database.shutdown();
                database = null;
            }
            if (webServer != null) {
                webServer.stop();
                webServer = null;
            }
            configurator = null;

            log.info("Successfully shutdown Neo Server on port [%d], database [%s]", webServerPort, location);
            return 0;
        } catch (Exception e) {
            log.error("Failed to cleanly shutdown Neo Server on port [%d], database [%s]. Reason [%s] ", webServerPort, location, e.getMessage());
            return 1;
        }
    }

    public GraphDatabaseService database() {
        return database.db;
    }

    public WebServer webServer() {
        return webServer;
    }

    public Configuration configuration() {
        return configurator.configuration();
    }

    public void controlEvent(int controlArg) {
       // Do nothing for now, this is needed by the WrapperListener interface
    }

    private static File getConfigFile() {
        return new File(System.getProperty(NEO_CONFIG_FILE_PROPERTY, DEFAULT_NEO_CONFIGDIR));
    }

    public static void main(String args[]) {
        final NeoServer neo = new NeoServer();

        Runtime.getRuntime().addShutdownHook(new Thread() {
	            @Override
	            public void run() {
	                log.info("Neo Server shutdown initiated by kill signal");
	                neo.stop();
	            }
	        });

        neo.start(args);
    }
}