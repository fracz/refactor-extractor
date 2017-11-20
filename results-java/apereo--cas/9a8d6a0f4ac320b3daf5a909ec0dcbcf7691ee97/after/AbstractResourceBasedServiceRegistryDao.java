package org.apereo.cas.services;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.util.LockedOutputStream;
import org.apereo.cas.util.ResourceUtils;
import org.apereo.cas.util.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;

import javax.annotation.PreDestroy;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This is {@link AbstractResourceBasedServiceRegistryDao}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public abstract class AbstractResourceBasedServiceRegistryDao implements ServiceRegistryDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractResourceBasedServiceRegistryDao.class);

    /**
     * Map of service ID to registered service.
     */
    private Map<Long, RegisteredService> serviceMap = new ConcurrentHashMap<>();

    /**
     * The Service registry directory.
     */
    private Path serviceRegistryDirectory;

    /**
     * The Registered service json serializer.
     */
    private StringSerializer<RegisteredService> registeredServiceSerializer;

    @Autowired
    private ApplicationContext applicationContext;

    private Thread serviceRegistryWatcherThread;
    private ServiceRegistryConfigWatcher serviceRegistryConfigWatcher;

    /**
     * Instantiates a new service registry dao.
     *
     * @param configDirectory the config directory
     * @param serializer      the registered service json serializer
     * @param enableWatcher   enable watcher thread
     */
    public AbstractResourceBasedServiceRegistryDao(final Path configDirectory,
                                                   final StringSerializer<RegisteredService> serializer,
                                                   final boolean enableWatcher) {
        initializeRegistry(configDirectory, serializer, enableWatcher);
    }

    /**
     * Instantiates a new Abstract resource based service registry dao.
     *
     * @param configDirectory the config directory
     * @param serializer      the serializer
     * @param enableWatcher   the enable watcher
     * @throws Exception the exception
     */
    public AbstractResourceBasedServiceRegistryDao(final Resource configDirectory,
                                                   final StringSerializer<RegisteredService> serializer,
                                                   final boolean enableWatcher) throws Exception {

        final Resource servicesDirectory = ResourceUtils.prepareClasspathResourceIfNeeded(configDirectory, true, getExtension());
        initializeRegistry(Paths.get(servicesDirectory.getFile().getCanonicalPath()), serializer, enableWatcher);
    }

    private void initializeRegistry(final Path configDirectory,
                                    final StringSerializer<RegisteredService> registeredServiceJsonSerializer,
                                    final boolean enableWatcher) {
        this.serviceRegistryDirectory = configDirectory;
        Assert.isTrue(this.serviceRegistryDirectory.toFile().exists(), this.serviceRegistryDirectory + " does not exist");
        Assert.isTrue(this.serviceRegistryDirectory.toFile().isDirectory(), this.serviceRegistryDirectory + " is not a directory");
        this.registeredServiceSerializer = registeredServiceJsonSerializer;

        if (enableWatcher) {
            this.serviceRegistryConfigWatcher = new ServiceRegistryConfigWatcher(this);
            this.serviceRegistryWatcherThread = new Thread(this.serviceRegistryConfigWatcher);
            this.serviceRegistryWatcherThread.setName(this.getClass().getName());
            this.serviceRegistryWatcherThread.start();
            LOGGER.debug("Started service registry watcher thread");
        }
    }

    /**
     * Refreshes the services manager, forcing it to reload.
     */
    public void refreshServicesManager() {
        if (this.applicationContext == null) {
            LOGGER.debug("Application context has failed to initialize because it's null. "
                    + "Service definition may not be immediately available to CAS, which suggests a configuration problem");
            return;
        }
        final ReloadableServicesManager manager = this.applicationContext.getBean(ReloadableServicesManager.class);
        if (manager != null) {
            manager.reload();
        } else {
            LOGGER.warn("Services manger could not be obtained from the application context. "
                    + "Service definition may not take immediate effect, which suggests a configuration problem");
        }
    }

    /**
     * Destroy the watch service thread.
     */
    @PreDestroy
    public void destroy() {
        this.serviceRegistryConfigWatcher.close();
        this.serviceRegistryWatcherThread.interrupt();
    }

    /**
     * Insert registered service into the existing map.
     *
     * @param service the service
     */
    public void updateRegisteredService(final RegisteredService service) {
        this.serviceMap.put(service.getId(), service);
    }

    public Path getServiceRegistryDirectory() {
        return this.serviceRegistryDirectory;
    }

    @Override
    public long size() {
        return this.serviceMap.size();
    }

    @Override
    public RegisteredService findServiceById(final long id) {
        return this.serviceMap.get(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

    @Override
    public synchronized boolean delete(final RegisteredService service) {
        try {
            final File f = makeFile(service);
            final boolean result = f.delete();
            if (!result) {
                LOGGER.warn("Failed to delete service definition file [{}]", f.getCanonicalPath());
            } else {
                this.serviceMap.remove(service.getId());
                LOGGER.debug("Successfully deleted service definition file [{}]", f.getCanonicalPath());
            }
            return result;
        } catch (final IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public synchronized List<RegisteredService> load() {
        final Map<Long, RegisteredService> temp = new ConcurrentHashMap<>();
        final int[] errorCount = {0};
        final Collection<File> c = FileUtils.listFiles(this.serviceRegistryDirectory.toFile(), new String[]{getExtension()}, true);
        c.stream().filter(file -> file.length() > 0).forEach(file -> {
            final RegisteredService service = loadRegisteredServiceFromFile(file);
            if (service == null) {
                LOGGER.warn("Could not load service definition from file {}", file);
                errorCount[0]++;
            } else {
                if (temp.containsKey(service.getId())) {
                    LOGGER.warn("Found a service definition [{}] with a duplicate id [{}]. "
                                    + "This will overwrite previous service definitions and is likely a "
                                    + "configuration problem. Make sure all services have a unique id and try again.",
                            service.getServiceId(), service.getId());
                }
                temp.put(service.getId(), service);
            }
        });

        if (errorCount[0] == 0) {
            this.serviceMap = temp;
        } else {
            LOGGER.warn("{} errors encountered when loading service definitions. New definitions are not loaded until errors are "
                    + "corrected", errorCount[0]);
        }
        return new ArrayList(this.serviceMap.values());
    }

    /**
     * Load registered service from file.
     *
     * @param file the file
     * @return the registered service, or null if file cannot be read, is not found, is empty or parsing error occurs.
     */
    public RegisteredService loadRegisteredServiceFromFile(final File file) {
        if (!file.canRead()) {
            LOGGER.warn("[{}] is not readable. Check file permissions", file.getName());
            return null;
        }

        if (!file.exists()) {
            LOGGER.warn("[{}] is not found at the path specified", file.getName());
            return null;
        }

        if (file.length() == 0) {
            LOGGER.debug("[{}] appears to be empty so no service definition will be loaded", file.getName());
            return null;
        }

        try (final BufferedInputStream in = new BufferedInputStream(new FileInputStream(file))) {
            return this.registeredServiceSerializer.from(in);
        } catch (final Exception e) {
            LOGGER.error("Error reading configuration file {}", file.getName(), e);
        }
        return null;
    }


    @Override
    public RegisteredService save(final RegisteredService service) {
        if (service.getId() == RegisteredService.INITIAL_IDENTIFIER_VALUE && service instanceof AbstractRegisteredService) {
            LOGGER.debug("Service id not set. Calculating id based on system time...");
            ((AbstractRegisteredService) service).setId(System.nanoTime());
        }
        final File f = makeFile(service);
        try (final LockedOutputStream out = new LockedOutputStream(new FileOutputStream(f))) {
            this.registeredServiceSerializer.to(out, service);

            if (this.serviceMap.containsKey(service.getId())) {
                LOGGER.debug("Found existing service definition by id [{}]. Saving...", service.getId());
            }
            this.serviceMap.put(service.getId(), service);
            LOGGER.debug("Saved service to [{}]", f.getCanonicalPath());
        } catch (final IOException e) {
            throw new IllegalArgumentException("IO error opening file stream.", e);
        }
        return findServiceById(service.getId());
    }


    /**
     * Creates a file for a registered service.
     * The file is named as {@code [SERVICE-NAME]-[SERVICE-ID]-.{@value #getExtension()}}
     *
     * @param service Registered service.
     * @return file in service registry directory.
     * @throws IllegalArgumentException if file name is invalid
     */
    protected File makeFile(final RegisteredService service) {
        final String fileName = StringUtils.remove(service.getName() + '-' + service.getId() + '.' + getExtension(), " ");
        try {
            final File svcFile = new File(this.serviceRegistryDirectory.toFile(), fileName);
            LOGGER.debug("Using [{}] as the service definition file", svcFile.getCanonicalPath());
            return svcFile;
        } catch (final IOException e) {
            LOGGER.warn("Service file name {} is invalid; Examine for illegal characters in the name.", fileName);
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Gets extension associated with files in the given resource directory.
     *
     * @return the extension
     */
    protected abstract String getExtension();

}