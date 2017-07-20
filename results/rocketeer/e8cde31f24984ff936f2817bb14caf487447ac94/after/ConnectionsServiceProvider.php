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

use League\Container\ServiceProvider\AbstractServiceProvider;
use Rocketeer\Bash;
use Rocketeer\Services\Connections\Connections\LocalConnection;
use Rocketeer\Services\Connections\Credentials\CredentialsGatherer;
use Rocketeer\Services\Connections\Credentials\CredentialsHandler;

class ConnectionsServiceProvider extends AbstractServiceProvider
{
    /**
     * @var array
     */
    protected $provides = [
        Bash::class,
        ConnectionsFactory::class,
        'remote.local',
        'connections',
        'coordinator',
        'credentials.handler',
        'credentials.gatherer',
    ];

    /**
     * Use the register method to register items with the container via the
     * protected $this->container property or the `getContainer` method
     * from the ContainerAwareTrait.
     */
    public function register()
    {
        $this->container->share(Bash::class, function () {
            return new Bash($this->container);
        });

        $this->container->share(ConnectionsFactory::class, function () {
            return new ConnectionsFactory($this->container);
        });

        $this->container->share('remote.local', function () {
            return new LocalConnection($this->container);
        });

        $this->container->share('connections', function () {
            return new ConnectionsHandler($this->container);
        });

        $this->container->share('coordinator', function () {
            return new Coordinator($this->container);
        });

        $this->container->share('credentials.handler', function () {
            return new CredentialsHandler($this->container);
        });

        $this->container->share('credentials.gatherer', function () {
            return new CredentialsGatherer($this->container);
        });
    }
}