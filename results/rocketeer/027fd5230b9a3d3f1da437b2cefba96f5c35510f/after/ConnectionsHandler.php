<?php

/*
 * This file is part of Rocketeer
 *
 * (c) Maxime Fabre <ehtnam6@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Rocketeer\Services\Connections;

use Illuminate\Support\Arr;
use Illuminate\Support\Collection;
use Rocketeer\Exceptions\ConnectionException;
use Rocketeer\Services\Connections\Connections\Connection;
use Rocketeer\Services\Connections\Connections\ConnectionInterface;
use Rocketeer\Services\Connections\Connections\LocalConnection;
use Rocketeer\Services\Connections\Credentials\Keys\ConnectionKey;
use Rocketeer\Traits\HasLocator;

/**
 * Handles, get and return, the various connections/stages
 * and their credentials.
 *
 * @author Maxime Fabre <ehtnam6@gmail.com>
 */
class ConnectionsHandler
{
    use HasLocator;

    /**
     * @var Collection<ConnectionInterface>
     */
    protected $available;

    /**
     * @var string
     */
    protected $current;

    ////////////////////////////////////////////////////////////////////
    //////////////////////// AVAILABLE CONNECTIONS /////////////////////
    ////////////////////////////////////////////////////////////////////

    /**
     * @return string[]
     */
    public function getDefaultConnectionsHandles()
    {
        return (array) $this->config->get('default');
    }

    /**
     * Get the available connections.
     *
     * @return array
     */
    public function getAvailableConnections()
    {
        // Fetch stored credentials
        $storage = $this->localStorage->get('connections');
        $storage = $this->unifyMultiserversDeclarations($storage);

        // Merge with defaults from config file
        $configuration = $this->config->get('connections');
        $configuration = $this->unifyMultiserversDeclarations($configuration);

        // Merge configurations
        $connections = array_replace_recursive($configuration, $storage);

        return $connections;
    }

    /**
     * @return Collection<Connection>
     */
    public function getConnections()
    {
        // Get default and available connections
        $defaults = $this->getDefaultConnectionsHandles();
        $available = $this->getAvailableConnections();

        // Convert to ConnectionKey/Connection instances
        if (!$this->available) {
            $connections = [];
            foreach ($available as $name => $connection) {
                // Skip connections without any defined servers
                $servers = $connection['servers'];
                if (!$servers) {
                    continue;
                }

                foreach ($servers as $key => $server) {
                    $connectionKey = new ConnectionKey([
                        'name' => $name,
                        'server' => $key,
                        'servers' => $servers,
                    ]);

                    $handle = $connectionKey->toHandle();
                    $connection = $this->remote->make($connectionKey);
                    $isActive = in_array($handle, $defaults) || in_array($connectionKey->name, $defaults) || $connection->isActive();
                    $connection->setActive($isActive);

                    $connections[$handle] = $connection;
                }
            }

            // Add local connection
            $connections['local'] = new LocalConnection();
            $this->available = new Collection($connections);
        }

        return $this->available->map(function (ConnectionInterface $connection) use ($defaults) {
            $handle = $connection->getConnectionKey()->toHandle();
            $isActive = in_array($handle, $defaults) || $connection->isActive();
            $connection->setActive($isActive);

            return $connection;
        });
    }

    /**
     * @param ConnectionKey|string $connection
     * @param int|null             $server
     *
     * @return Connection
     * @throws ConnectionException
     */
    public function getConnection($connection, $server = null)
    {
        $connectionKey = $this->credentials->sanitizeConnection($connection, $server);
        $handle = $connectionKey->toHandle();

        $connections = $this->getConnections();
        if (!$connections->has($handle)) {
            throw new ConnectionException('Invalid connection: '.$handle);
        }

        return $connections[$handle];
    }

    /**
     * Check if a connection has credentials related to it.
     *
     * @param ConnectionKey|string $connection
     *
     * @return bool
     */
    public function isValidConnection($connection)
    {
        $connection = $this->credentials->sanitizeConnection($connection);
        $available = (array) $this->getAvailableConnections();

        return (bool) Arr::get($available, $connection->name.'.servers');
    }

    ////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////// ACTIVE CONNECTIONS //////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Get the active connections for this session.
     *
     * @return Collection<Connection>
     */
    public function getActiveConnections()
    {
        return $this->getConnections()->filter(function (ConnectionInterface $connection) {
            return $connection->isActive();
        });
    }

    /**
     * Override the active connections.
     *
     * @param string|string[] $connections
     *
     * @throws ConnectionException
     */
    public function setActiveConnections($connections)
    {
        if (!is_array($connections)) {
            $connections = explode(',', $connections);
        }

        // Sanitize and set connections
        $filtered = array_filter($connections, [$this, 'isValidConnection']);
        if (!$filtered) {
            throw new ConnectionException('Invalid connection(s): '.implode(', ', $connections));
        }

        $this->available = $this->getConnections()->map(function (ConnectionInterface $connection) use ($connections) {
            $connectionKey = $connection->getConnectionKey();
            $handle = $connectionKey->toHandle();

            $connection->setActive(in_array($handle, $connections) || in_array($connectionKey->name, $connections));
            $connection->setCurrent(false);

            return $connection;
        });
    }

    ////////////////////////////////////////////////////////////////////////////////
    ///////////////////////////// CURRENT CONNECTIONS //////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * @return ConnectionInterface
     */
    public function getCurrentConnection()
    {
        if ($this->rocketeer->isLocal()) {
            return $this->getConnection('local');
        }

        $connections = $this->getConnections();
        if ($this->current && $connections->has($this->current)) {
            return $connections->get($this->current);
        }

        /** @var ConnectionInterface $connection */
        $connection = $connections->first(function ($key, ConnectionInterface $connection) {
            return $connection->isCurrent();
        });

        // If no connection is marked as current, get the first active one
        if (!$connection) {
            $connection = $this->getActiveConnections()->first();
        }

        // Fire connected event the first time
        $handle = $connection ? $connection->getConnectionKey()->toHandle() : null;
        if ($connection && !$connection->isConnected()) {
            $event = 'connected.'.$handle;
            $this->events->emit($event);
        }

        // Cache which connection is the first
        $this->current = $handle;

        return $connection;
    }

    /**
     * Get the current connection.
     *
     * @return ConnectionKey
     */
    public function getCurrentConnectionKey()
    {
        $current = $this->getCurrentConnection();

        return $current ? $current->getConnectionKey() : $this->credentials->createConnectionKey();
    }

    /**
     * Set the current connection.
     *
     * @param ConnectionKey|string $connection
     * @param int|null             $server
     */
    public function setCurrentConnection($connection = null, $server = null)
    {
        $connectionKey = $connection instanceof ConnectionKey ? $connection : $this->credentials->createConnectionKey($connection, $server);
        if ($this->current === $connectionKey->toHandle()) {
            return;
        }

        $this->current = $connectionKey->toHandle();
        $this->available = $this->getConnections()->map(function(ConnectionInterface $connection) use ($connectionKey) {
            $isCurrent = $connectionKey->is($connection->getConnectionKey());
            $connection->setCurrent($isCurrent);

            return $connection;
        });

        // Update events
        $this->tasks->registerConfiguredEvents();
    }

    /**
     * @return bool
     */
    public function hasCurrentConnection()
    {
        return (bool) $this->getCurrentConnection();
    }

    /**
     * Flush active connection(s).
     */
    public function disconnect()
    {
        $this->current = null;
        $this->available = [];
    }

    ////////////////////////////////////////////////////////////////////
    //////////////////////////////// STAGES ////////////////////////////
    ////////////////////////////////////////////////////////////////////

    /**
     * Get the various stages provided by the User.
     *
     * @return array
     */
    public function getAvailableStages()
    {
        return (array) $this->config->getContextually('stages.stages');
    }

    /**
     * Set the stage on the current connection.
     *
     * @param string|null $stage
     */
    public function setStage($stage)
    {
        $connectionKey = $this->getCurrentConnectionKey();
        if ($stage === $connectionKey->stage) {
            return;
        }

        $connectionKey->stage = $stage;
        $this->getConnections()->map(function (ConnectionInterface $connection) use ($connectionKey) {
           if ($connection->isCurrent() || $connection->getConnectionKey()->is($connectionKey)) {
               $connection->setConnectionKey($connectionKey);
           }

           return $connection;
        });

        // If we do have a stage, cleanup previous events
        if ($stage) {
            $this->tasks->registerConfiguredEvents();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////// HELPERS ////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Unify a connection's declaration into the servers form.
     *
     * @param array $connection
     *
     * @return array
     */
    protected function unifyMultiserversDeclarations($connection)
    {
        $connection = (array) $connection;
        foreach ($connection as $key => $servers) {
            $servers = Arr::get($servers, 'servers', [$servers]);
            $connection[$key] = ['servers' => array_values($servers)];
        }

        return $connection;
    }
}