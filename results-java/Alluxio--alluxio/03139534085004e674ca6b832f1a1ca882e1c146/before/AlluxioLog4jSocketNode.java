/*
 * The Alluxio Open Foundation licenses this work under the Apache License, version 2.0
 * (the "License"). You may not use this work except in compliance with the License, which is
 * available at www.apache.org/licenses/LICENSE-2.0
 *
 * This software is distributed on an "AS IS" basis, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied, as more fully set forth in the License.
 *
 * See the NOTICE file distributed with this work for information regarding copyright ownership.
 */

package alluxio.logserver;

import alluxio.AlluxioRemoteLogFilter;

import com.google.common.base.Preconditions;
import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.log4j.spi.LoggerRepository;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.RootLogger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * Reads {@link org.apache.log4j.spi.LoggingEvent} objects from remote logging
 * clients and writes the objects to designated log files. For each logging client,
 * logging server creates a {@link AlluxioLog4jSocketNode} object and starts
 * a thread serving the logging requests of the client.
 */
public class AlluxioLog4jSocketNode implements Runnable {
  private static final org.slf4j.Logger LOG =
      org.slf4j.LoggerFactory.getLogger(AlluxioLog4jSocketNode.class);
  private final AlluxioLogServerProcess mLogServerProcess;
  private final Socket mSocket;

  /**
   * Constructor of {@link AlluxioLog4jSocketNode}.
   *
   * Callers construct AlluxioLog4jSocketNode instances, passing the ownership of the socket
   * parameter. From now on, the AlluxioLog4jSocketNode is responsible for closing the socket.
   *
   * @param process main log server process
   * @param socket client socket from which to read {@link org.apache.log4j.spi.LoggingEvent}
   */
  public AlluxioLog4jSocketNode(AlluxioLogServerProcess process, Socket socket) {
    mLogServerProcess = Preconditions.checkNotNull(process,
        "The log server process could not be null.");
    mSocket = Preconditions.checkNotNull(socket, "Client socket could not be null");
  }

  @Override
  public void run() {
    LoggerRepository hierarchy = null;
    LoggingEvent event;
    Logger remoteLogger;
    try (ObjectInputStream objectInputStream = new ObjectInputStream(
        new BufferedInputStream(mSocket.getInputStream()))) {
      // Check for interrupt status of current thread and preserve it.
      while (!Thread.currentThread().isInterrupted()) {
        event = (LoggingEvent) objectInputStream.readObject();
        if (hierarchy == null) {
          hierarchy = configureHierarchy(
              event.getMDC(AlluxioRemoteLogFilter.REMOTE_LOG_MDC_APPENDER_NAME_KEY).toString());
        }
        remoteLogger = hierarchy.getLogger(event.getLoggerName());
        if (event.getLevel().isGreaterOrEqual(remoteLogger.getEffectiveLevel())) {
          remoteLogger.callAppenders(event);
        }
      }
    } catch (IOException | ClassNotFoundException e) {
      // Something went wrong, cannot recover.
      LOG.error("Cannot read logging event from client due to I/O error.");
      throw new RuntimeException(e);
    } finally {
      try {
        mSocket.close();
      } catch (Exception e) {
        // Ignore the exception caused by closing socket.
      }
    }
  }

  /**
   * Configure a {@link Hierarchy} instance used to retrive logger by name and maintain the logger
   * hierarchy. {@link AlluxioLog4jSocketNode} instance can retrieve the logger to log incoming
   * {@link org.apache.log4j.spi.LoggingEvent}s.
   *
   * @param logAppenderName name of the appender to use for this client
   * @return a {@link Hierarchy} instance to retrieve logger
   * @throws IOException if fails to create an {@link FileInputStream} to read log4j.properties
   */
  private LoggerRepository configureHierarchy(String logAppenderName)
      throws IOException {
    Hierarchy clientHierarchy;
    String inetAddressStr = mSocket.getInetAddress().getHostAddress();
    Properties properties = new Properties();
    File configFile;
    try {
      configFile = new File(new URI(System.getProperty("log4j.configuration")));
    } catch (URISyntaxException e) {
      // Alluxio log server cannot derive a valid path to log4j.properties. Since this
      // properties file is global, we should throw an exception.
      LOG.error("Cannot derive a valid URI to log4j.properties file.");
      throw new RuntimeException(e);
    }
    try (FileInputStream inputStream = new FileInputStream(configFile)) {
      properties.load(inputStream);
    }
    Level level = Level.INFO;
    clientHierarchy = new Hierarchy(new RootLogger(level));
    // Startup script should guarantee that mBaseLogsDir already exists.
    String logDirectoryPath = mLogServerProcess.getBaseLogsDir() + "/" + logAppenderName.toLowerCase();
    File logDirectory = new File(logDirectoryPath);
    if (!logDirectory.exists()) {
      logDirectory.mkdir();
    }
    String logFilePath = logDirectoryPath + "/" + inetAddressStr + ".log";
    properties.setProperty("log4j.rootLogger",
        level.toString() + "," + AlluxioLogServerProcess.LOGSERVER_CLIENT_LOGGER_APPENDER_NAME);
    properties.setProperty(
        "log4j.appender."
            + AlluxioLogServerProcess.LOGSERVER_CLIENT_LOGGER_APPENDER_NAME + ".File",
        logFilePath);
    new PropertyConfigurator().doConfigure(properties, clientHierarchy);
    return clientHierarchy;
  }
}