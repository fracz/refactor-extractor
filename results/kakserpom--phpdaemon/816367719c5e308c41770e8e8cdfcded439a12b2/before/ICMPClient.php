<?php

/**
 * @package Applications
 * @subpackage ICMPClient
 *
 * @author Zorin Vasily <kak.serpom.po.yaitsam@gmail.com>
 */
class ICMPClient extends NetworkClient {

	/**
	 * Establishes connection
	 * @param string Address
	 * @return integer Connection ID
	 */

	public function sendPing($host, $cb) {
		$this->connectTo('raw:' . $host, 0, function($conn) use ($cb) {
			$conn->sendEcho($cb);
		});
	}
}

class ICMPClientConnection extends NetworkClientConnection {
	public $seq = 0;

	public function sendEcho($cb) {
		++$this->seq;

		$data = 'phpdaemon';
		$packet = pack('ccnnn', strlen($data), 0, 0, Daemon::$process->pid,	$this->seq) . $data;
		$packet = substr_replace($packet, self::checksum($packet), 2, 2);
		$this->write($packet);
		$this->onResponse->push(array($cb, microtime(true)));
	}

	public static function checksum($data) {
		$bit = unpack('n*', $data);
		$sum = array_sum($bit);
		if (strlen($data) % 2) {
			$temp = unpack('C*', $data[strlen($data) - 1]);
			$sum += $temp[1];
		}
		$sum = ($sum >> 16) + ($sum & 0xffff);
		$sum += ($sum >> 16);
		return pack('n*', ~$sum);
	}


	/**
	 * Called when new data received
	 * @param string New data
	 * @return void
	 */
	public function stdin($buf) {
		// TODO: implement sequential packet exchange, incoming packet check
		while (!$this->onResponse->isEmpty()) {
			$el = $this->onResponse->shift();
			list ($cb, $st) = $el;
			call_user_func($cb, microtime(true) - $st);
		}
		$this->finish();
	}
}