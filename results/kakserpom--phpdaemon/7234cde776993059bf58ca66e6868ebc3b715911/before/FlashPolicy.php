<?php

/**
 * @package Applications
 * @subpackage FlashPolicy
 *
 * @author Zorin Vasily <kak.serpom.po.yaitsam@gmail.com>
 */
class FlashPolicy extends AsyncServer {

	public $sessions = array();  // Active sessions
	public $policyData;          // Cached policy-file.

	/**
	 * Setting default config options
	 * Overriden from AppInstance::getConfigDefaults
	 * @return array|false
	 */
	protected function getConfigDefaults() {
		return array(
			// listen to
			'listen'     => 'tcp://0.0.0.0',
			// listen port
			'listenport' => 843,
			// crossdomain file path
			'file'       => Daemon::$dir . '/conf/crossdomain.xml',
			// disabled by default
			'enable'     => 0
		);
	}

	/**
	 * Constructor.
	 * @return void
	 */
	public function init() {
		if ($this->config->enable->value) {
			Daemon::log(__CLASS__ . ' up.');

			$this->bindSockets(
				$this->config->listen->value,
				$this->config->listenport->value
			);

			$this->update();
		}
	}

	/**
	 * Called when worker is going to update configuration.
	 * @return void
	 */
	public function update() {
		$this->policyData = file_get_contents($this->config->file->value);
	}

	/**
	 * Called when new connection is accepted
	 * @param integer Connection's ID
	 * @param string Address of the connected peer
	 * @return void
	 */
	protected function onAccepted($connId, $addr) {
		$this->sessions[$connId] = new FlashPolicySession($connId, $this);
	}

}

class FlashPolicySession extends SocketSession {

	/**
	 * Called when new data received.
	 * @param string New data.
	 * @return void
	 */
	public function stdin($buf) {
		$this->buf .= $buf;
		if (strpos($this->buf, '<policy-file-request/>') !== FALSE) {

			if ($this->appInstance->policyData) {
				$this->write($this->appInstance->policyData . "\x00");
			} else {
				$this->write("<error/>\x00");
			}
			$this->finish();
		}
		elseif (
			(strlen($this->buf) > 64)
			|| (strpos($this->buf, "\xff\xf4\xff\xfd\x06") !== FALSE)  // telnet ^C code
			|| (strpos($this->buf, "\xff\xec") !== FALSE)		// telnet EOF code
		) {
			$this->finish();
		}
	}

}