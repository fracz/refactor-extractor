<?php

/**
 * @package    Examples
 * @subpackage UDPEchoServer
 *
 * @author     Zorin Vasily <maintainer@daemon.io>
 */
class UDPEchoServer extends \PHPDaemon\Servers\NetworkServer {

	/**
	 * Setting default config options
	 * Overriden from ConnectionPool::getConfigDefaults
	 * @return array|false
	 */
	protected function getConfigDefaults() {
		return array(
			'listen' => 'udp://0.0.0.0',
			'port'   => 1111,
		);
	}

	public function onConfigUpdated() {
		parent::onConfigUpdated();
	}

}

class UDPEchoServerConnection extends \PHPDaemon\Connection {
	/**
	 * Called when UDP packet received.
	 * @param string New data.
	 * @return void
	 */
	public function onUdpPacket($pct) {
		$this->write('got: ' . $pct);
	}

}