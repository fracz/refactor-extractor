<?php
/*
 * This file is part of Rocketeer
 *
 * (c) Maxime Fabre <ehtnam6@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
namespace Rocketeer\Traits;

use Illuminate\Container\Container;

/**
 * An abstract for Service Locator-based classes with adds
 * a few shortcuts to Rocketeer classes
 *
 * @property \Illuminate\Config\Repository config
 * @property \Illuminate\Console\Command   command
 * @property \Illuminate\Remote\Connection remote
 * @property \Rocketeer\Abstracts\Scm      scm
 * @property \Rocketeer\ConnectionsHandler connections
 * @property \Rocketeer\Bash               bash
 * @property \Rocketeer\ReleasesManager    releasesManager
 * @property \Rocketeer\Rocketeer          rocketeer
 * @property \Rocketeer\Server             server
 * @property \Rocketeer\TasksHandler       tasks
 * @property \Rocketeer\TasksQueue         queue
 * @author Maxime Fabre <ehtnam6@gmail.com>
 */
trait HasLocator
{
	/**
	 * The IoC Container
	 *
	 * @var Container
	 */
	protected $app;

	/**
	 * Build a new Task
	 *
	 * @param Container $app
	 */
	public function __construct(Container $app)
	{
		$this->app = $app;
	}

	/**
	 * Get an instance from the Container
	 *
	 * @param  string $key
	 *
	 * @return object
	 */
	public function __get($key)
	{
		$shortcuts = array(
			'bash'            => 'rocketeer.bash',
			'connections'     => 'rocketeer.connections',
			'command'         => 'rocketeer.command',
			'console'         => 'rocketeer.console',
			'logs'            => 'rocketeer.logs',
			'queue'           => 'rocketeer.queue',
			'releasesManager' => 'rocketeer.releases',
			'rocketeer'       => 'rocketeer.rocketeer',
			'scm'             => 'rocketeer.scm',
			'server'          => 'rocketeer.server',
			'tasks'           => 'rocketeer.tasks',
		);

		// Replace shortcuts
		if (array_key_exists($key, $shortcuts)) {
			$key = $shortcuts[$key];
		}

		return $this->app[$key];
	}

	/**
	 * Set an instance on the Container
	 *
	 * @param string $key
	 * @param object $value
	 */
	public function __set($key, $value)
	{
		$this->app[$key] = $value;
	}

	/**
	 * Check if the current instance has a Command bound
	 *
	 * @return boolean
	 */
	protected function hasCommand()
	{
		return $this->app->bound('rocketeer.command');
	}
}