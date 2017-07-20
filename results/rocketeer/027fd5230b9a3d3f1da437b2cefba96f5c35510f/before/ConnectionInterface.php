<?php

/*
 * This file is part of Rocketeer
 *
 * (c) Maxime Fabre <ehtnam6@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace Rocketeer\Services\Connections\Connections;

use Closure;
use League\Flysystem\FilesystemInterface;

interface ConnectionInterface extends FilesystemInterface
{
    /**
     * Run a set of commands against the connection.
     *
     * @param string|array $commands
     * @param Closure|null $callback
     */
    public function run($commands, Closure $callback = null);

    /**
     * Get the exit status of the last command.
     *
     * @return int|bool
     */
    public function status();
}