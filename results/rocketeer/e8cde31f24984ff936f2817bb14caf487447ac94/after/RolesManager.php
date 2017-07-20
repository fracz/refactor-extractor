<?php

/*
 * This file is part of Rocketeer
 *
 * (c) Maxime Fabre <ehtnam6@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Rocketeer\Services;

use Illuminate\Support\Arr;
use Rocketeer\Services\Connections\Connections\Connection;
use Rocketeer\Tasks\AbstractTask;
use Rocketeer\Traits\HasLocator;

/**
 * Manages roles and decides which servers
 * can execute which tasks.
 *
 * @author Maxime Fabre <ehtnam6@gmail.com>
 */
class RolesManager
{
    use HasLocator;

    /**
     * Get the roles of a server.
     *
     * @param string|null $connection
     * @param int|null    $server
     *
     * @return array
     */
    public function getConnectionRoles($connection = null, $server = null)
    {
        $credentials = $this->credentials->getConnectionServer($connection, $server);

        return Arr::get($credentials, 'roles', []);
    }

    /**
     * Assign roles to multiple tasks.
     *
     * @param array $roles
     */
    public function assignTasksRoles(array $roles)
    {
        foreach ($roles as $roles => $tasks) {
            $tasks = (array) $tasks;
            $roles = (array) $roles;

            foreach ($tasks as $task) {
                $this->builder->buildTask($task)->addRoles($roles);
            }
        }
    }

    /**
     * Check if a connection can execute a task.
     *
     * @param Connection                    $connection
     * @param \Rocketeer\Tasks\AbstractTask $task
     *
     * @return bool
     */
    public function canExecuteTask(Connection $connection, AbstractTask $task)
    {
        return $connection->isCompatibleWith($task);
    }
}