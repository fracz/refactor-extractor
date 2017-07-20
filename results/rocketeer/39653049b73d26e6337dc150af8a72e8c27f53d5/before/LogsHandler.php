<?php
/*
 * This file is part of Rocketeer
 *
 * (c) Maxime Fabre <ehtnam6@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */
namespace Rocketeer;

use Rocketeer\Traits\HasLocator;

/**
 * Handles rotation of logs
 */
class LogsHandler
{
	use HasLocator;

	/**
	 * The loggers instances
	 *
	 * @var array
	 */
	protected $loggers = array();

	/**
	 * Log by level
	 *
	 * @param string $method
	 * @param array  $parameters
	 */
	public function __call($method, $parameters)
	{
		$this->log($parameters[0], $method);
	}

	/**
	 * Log a piece of text
	 *
	 * @param string $informations
	 * @param string $level
	 */
	public function log($informations, $level = 'info')
	{
		if ($file = $this->getCurrentLogsFile()) {
			$this->getLogger($file)->$level($informations);
		}
	}

	/**
	 * Get the logs file being currently used
	 *
	 * @return string|boolean
	 */
	public function getCurrentLogsFile()
	{
		/** @type \Callable $logs */
		$logs = $this->config->get('rocketeer::logs');
		if (!$logs) {
			return false;
		}

		$file = $logs($this->connections);
		$file = $this->app['path.rocketeer.logs'].'/'.$file;

		return $file;
	}

	/**
	 * Get a logger instance by context
	 *
	 * @param string $file
	 *
	 * @return \Illuminate\Log\Writer
	 */
	protected function getLogger($file)
	{
		// Create logger instance if necessary
		if (!array_key_exists($file, $this->loggers)) {
			$this->createLogsFile($file);

			// Store specific logger instance
			$logger = clone $this->app['log'];
			$logger->useFiles($file);
			$this->loggers[$file] = $logger;
		}

		return $this->loggers[$file];
	}

	/**
	 * Create a logs file if it doesn't exist
	 *
	 * @param string $file
	 */
	protected function createLogsFile($file)
	{
		$directory = dirname($file);

		// Create directory
		if (!is_dir($directory)) {
			$this->app['files']->makeDirectory($directory, 0777, true);
		}

		// Create file
		if (!file_exists($file)) {
			$this->app['files']->put($file, '');
		}
	}
}