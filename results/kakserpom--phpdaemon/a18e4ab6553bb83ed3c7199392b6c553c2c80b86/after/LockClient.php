<?php

/**
 * @package Applications
 * @subpackage LockClient
 *
 * @author Zorin Vasily <kak.serpom.po.yaitsam@gmail.com>
 */
class LockClient extends AsyncServer {

	public $sessions = array();     // Active sessions
	public $servers = array();      // Array of servers
	public $servConn = array();     // Active connections
	public $prefix = '';            // Prefix
	public $jobs = array();         // Active jobs
	public $dtags_enabled = FALSE;  // Enables tags for distibution

	/**
	 * Setting default config options
	 * Overriden from AppInstance::getConfigDefaults
	 * @return array|false
	 */
	protected function getConfigDefaults() {
		return array(
			// default server
			'servers' => '127.0.0.1',
			// default port
			'port'    => 833,
			// @todo add description
			'prefix'  => '',
			'protologging' 	 => true
		);
	}

	/**
	 * Constructor
	 * @return void
	 */
	public function init() {
		$this->prefix = $this->config->prefix->value;
		$servers = explode(',', $this->config->servers->value);

		foreach ($servers as $s) {
			$e = explode(':',$s);
			$this->addServer($e[0], isset($e[1]) ? $e[1] : NULL);
		}
	}

	/**
	 * Adds memcached server
	 * @param string Server's host
	 * @param string Server's port
	 * @param integer Weight
	 * @return void
	*/
	public function addServer($host, $port = NULL, $weight = NULL) {
		if ($port === NULL) {
			$port = $this->config->port->value;
		}

		$this->servers[$host . ':' . $port] = $weight;
	}

	/**
	 * Runs a job
	 * @param string Name of job
	 * @param bool wait. If true - will wait in queue for lock.
	 * @param callback onRun. Job's runtime.
	 * @param callback onSuccess. Called when job successfully done.
	 * @param callback onFailure. Called when job failed.
	 * @return void
	 */
	public function job($name, $wait, $onRun, $onSuccess = NULL, $onFailure = NULL) {
		$name = $this->prefix . $name;
		$connId = $this->getConnectionByName($name);

		if (!isset($this->sessions[$connId])) {
			return;
		}

		$sess = $this->sessions[$connId];
		$this->jobs[$name] = array($onRun, $onSuccess, $onFailure);
		$sess->writeln('acquire' . ($wait ? 'Wait' : '') . ' ' . $name);
	}

	/**
	 * Sends done-event
	 * @param string Name of job
	 * @return void
	 */
	public function done($name) {
		$connId = $this->getConnectionByName($name);
		$sess = $this->sessions[$connId];
		$sess->writeln('done ' . $name);
	}

	/**
	 * Sends failed-event
	 * @param string Name of job
	 * @return void
	 */
	public function failed($name) {
		$connId = $this->getConnectionByName($name);
		$sess = $this->sessions[$connId];
		$sess->writeln('failed ' . $name);
	}

	/**
	 * Establishes connection
	 * @param string Address
	 * @return integer Connection's ID
	 */
	public function getConnection($addr) {
		if (isset($this->servConn[$addr])) {
			foreach ($this->servConn[$addr] as &$c) {
				return $c;
			}
		} else {
			$this->servConn[$addr] = array();
		}

		$e = explode(':', $addr);

		$connId = $this->connectTo($e[0], $e[1]);

		$this->sessions[$connId] = new LockClientSession($connId, $this);
		$this->sessions[$connId]->addr = $addr;
		$this->servConn[$addr][$connId] = $connId;

		return $connId;
	}

	/**
	 * Returns available connection from the pool by name
	 * @param string Key
	 * @return object MemcacheSession
	 */
	public function getConnectionByName($name) {
		if (
			($this->dtags_enabled)
			&& (($sp = strpos($name, '[')) !== FALSE)
			&& (($ep = strpos($name, ']')) !== FALSE)
			&& ($ep > $sp)
		) {
			$name = substr($name,$sp + 1, $ep - $sp - 1);
		}

		srand(crc32($name));
		$addr = array_rand($this->servers);
		srand();

		return $this->getConnection($addr);
	}
}

class LockClientSession extends SocketSession {

	/**
	 * Called when new data received
	 * @param string New data
	 * @return void
	 */
	public function stdin($buf) {
		$this->buf .= $buf;

		while (($l = $this->gets()) !== FALSE) {
			$e = explode(' ', rtrim($l, "\r\n"));

			if ($e[0] === 'RUN') {
				if (isset($this->appInstance->jobs[$e[1]])) {
					call_user_func($this->appInstance->jobs[$e[1]][0], $e[0], $e[1], $this->appInstance);
				}
			}
			elseif ($e[0] === 'DONE') {
				if (isset($this->appInstance->jobs[$e[1]][1])) {
					call_user_func($this->appInstance->jobs[$e[1]][1], $e[0], $e[1], $this->appInstance);
				}
			}
			elseif ($e[0] === 'FAILED') {
				if (isset($this->appInstance->jobs[$e[1]][2])) {
					call_user_func($this->appInstance->jobs[$e[1]][2], $e[0], $e[1], $this->appInstance);
				}
			}
			if($this->appInstance->config->protologging->value) {
				Daemon::log('Lock client <-- Lock server: ' . Debug::exportBytes(implode(' ', $e)) . "\n");
			}
		}
	}

	/**
	 * Called when session finishes
	 * @return void
	 */
	public function onFinish() {
		$this->finished = TRUE;

		unset($this->appInstance->servConn[$this->addr][$this->connId]);
		unset($this->appInstance->sessions[$this->connId]);
	}
}