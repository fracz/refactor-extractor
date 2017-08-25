<?php
namespace PHPDaemon\Clients\Redis;

use PHPDaemon\Network\ClientConnection;
use PHPDaemon\Core\Daemon;
use PHPDaemon\Core\Debug;
use PHPDaemon\Core\CallbackWrapper;

/**
 * @package    NetworkClients
 * @subpackage RedisClient
 *
 * @author     Zorin Vasily <maintainer@daemon.io>
 */
class Connection extends ClientConnection {

	/**
	 * Current result
	 * @var array|null
	 */
	public $result = null;

	/**
	 * Current error message
	 * @var string
	 */
	public $error;

	/**
	 * Current incoming key
	 * @var string
	 */
	protected $key;


	protected $stack = [];

	protected $ptr;

	/**
	 * Current value length
	 * @var integer
	 */
	protected $valueLength = 0;


	/**
	 * Current level length
	 * @var integer
	 */
	protected $levelLength = null;

	/**
	 * EOL
	 * @var string "\r\n"
	 */
	protected $EOL = "\r\n";

	protected $subscribed = false;

	/**
	 * Timeout
	 * @var float
	 */
	protected $timeoutRead = 5;

	/**
	 * Subcriptions
	 * @var array
	 */
	public $subscribeCb = [];
	public $psubscribeCb = [];

	protected $maxQueue = 10;

	/**
	 * In the middle of binary response part
	 * @const integer
	 */
	const STATE_BINARY = 1;

	public function getLocalSubscribersCount($chan) {
		if (!isset($this->subscribeCb[$chan])) {
			return 0;
		}
		return sizeof($this->subscribeCb[$chan]);
	}

	/**
	 * Called when the connection is handshaked (at low-level), and peer is ready to recv. data
	 * @return void
	 */
	public function onReady() {
		$this->ptr =& $this->result;
		if (!isset($this->password)) {
			parent::onReady();
			$this->setWatermark(null, $this->pool->maxAllowedPacket + 2);
			return;
		}
		$this->sendCommand('AUTH', [$this->password], function () {
			if ($this->result !== 'OK') {
				$this->log('Auth. error: ' . json_encode($this->result));
				$this->finish();
			}
			parent::onReady();
			$this->setWatermark(null, $this->pool->maxAllowedPacket + 2);
		});
	}

	public function subscribed() {
		if ($this->subscribed) {
			return;
		}
		$this->subscribed = true;
		$this->pool->servConnSub[$this->url] = $this;
		$this->checkFree();
		$this->setTimeouts(86400, 86400); // @TODO: remove timeout
	}

	public function isSubscribed() {
		return $this->subscribed;
	}

	/**
	 * Magic __call.
	 * @method $cmd
	 * @param string $cmd
	 * @param array $args
	 * @usage $ .. Command-dependent set of arguments ..
	 * @usage $ [callback Callback. Optional.
	 * @example  $redis->lpush('mylist', microtime(true));
	 * @return void
	 */
	public function __call($cmd, $args) {
		$cb = null;
		for ($i = sizeof($args) - 1; $i >= 0; --$i) {
			$a = $args[$i];
			if ((is_array($a) || is_object($a)) && is_callable($a)) {
				$cb = CallbackWrapper::wrap($a);
				$args = array_slice($args, 0, $i);
				break;
			}
			elseif ($a !== null) {
				break;
			}
		}
		$cmd = strtoupper($cmd);
		$this->command($cmd, $args, $cb);
	}

	public function command($name, $args, $cb = null) {
		// PUB/SUB handling
		if (substr($name, -9) === 'SUBSCRIBE') {
			$opcb = null;
			for ($i = sizeof($args) - 1; $i >= 0; --$i) {
				$a = $args[$i];
				if ((is_array($a) || is_object($a)) && is_callable($a)) {
					$opcb = $cb;
					$cb = CallbackWrapper::wrap($a);
					$args = array_slice($args, 0, $i);
					break;
				}
				elseif ($a !== null) {
					break;
				}
			}
		}
		if ($name === 'SUBSCRIBE') {
			$this->subscribed();
			foreach ($args as $arg) {
				$b = !isset($this->subscribeCb[$arg]);
				CallbackWrapper::addToArray($this->subscribeCb[$arg], $cb);
				//D([$name, $arg, $b]);
				if ($b) {
					$this->sendCommand($name, $arg, $opcb);
				} else {
					if ($opcb !== null) {
						call_user_func($opcb, $this);
					}
				}
			}
		}
		elseif ($name === 'PSUBSCRIBE') {
			$this->subscribed();
			foreach ($args as $arg) {
				$b = !isset($this->psubscribeCb[$arg]);
				CallbackWrapper::addToArray($this->psubscribeCb[$arg], $cb);
				if ($b) {
					$this->sendCommand($name, $arg, $opcb);
				} else {
					if ($opcb !== null) {
						call_user_func($opcb, $this);
					}
				}
			}
		}
		elseif ($name === 'UNSUBSCRIBE') {
			foreach ($args as $arg) {
				//D([$name, $arg, 'pre']);
				if (!isset($this->subscribeCb[$arg]) || !sizeof($this->subscribeCb[$arg])) {
					if ($opcb !== null) {
						call_user_func($opcb, $this);
					}
					continue;
				}
				CallbackWrapper::removeFromArray($this->subscribeCb[$arg], $cb);
				if (sizeof($this->subscribeCb[$arg]) === 0) {
					//D([$name, $arg, 'send']);
					$this->sendCommand($name, $arg, $opcb);
					unset($this->subscribeCb[$arg]);
				} else {
					if ($opcb !== null) {
						call_user_func($opcb, $this);
					}
				}
			}
		}
		elseif ($name === 'PUNSUBSCRIBE') {
			foreach ($args as $arg) {
				CallbackWrapper::removeFromArray($this->psubscribeCb[$arg], $cb);
				if (sizeof($this->psubscribeCb[$arg]) === 0) {
					$this->sendCommand($name, $arg, $opcb);
					unset($this->psubscribeCb[$arg]);
				} else {
					if ($opcb !== null) {
						call_user_func($opcb, $this);
					}
				}
			}
		} else {
			$this->sendCommand($name, $args, $cb);
		}
 	}

 	public function sendCommand($name, $args, $cb = null) {
 		$this->onResponse($cb);
 		if (!is_array($args)) {
			$args = [$args];
		}
 		array_unshift($args, $name);
		$this->writeln('*' . sizeof($args));
		foreach ($args as $arg) {
			$this->writeln('$' . strlen($arg) . $this->EOL . $arg);
		}
 	}

	/**
	 * Check if arrived data is message from subscription
	 */
	protected function isSubMessage() {
		if (sizeof($this->result) < 3) {
			return false;
		}
		if (!$this->subscribed) {
			return false;
		}
		$mtype = strtolower($this->result[0]);
		if ($mtype !== 'message' && $mtype !== 'pmessage') {
			return false;
		}
		return $mtype;
	}

	/**
	 * Called when connection finishes
	 * @return void
	 */
	public function onFinish() {
		parent::onFinish();
		/* we should reassign subscriptions */
		foreach ($this->subscribeCb as $sub => $cbs) {
			foreach ($cbs as $cb) {
				call_user_func([$this->pool, 'subscribe'], $sub, $cb);
			}
		}
		foreach ($this->psubscribeCb as $sub => $cbs) {
			foreach ($cbs as $cb) {
				call_user_func([$this->pool, 'psubscribe'], $sub, $cb);
			}
		}
	}

	protected function onPacket() {
		$this->result = $this->ptr;
		if (($mtype = $this->isSubMessage()) !== false) { // sub callback
			$chan = $this->result[1];
			if ($mtype === 'pmessage') {
				$t = $this->psubscribeCb;
			} else {
				$t = $this->subscribeCb;
			}
			if (isset($t[$chan])) {
				foreach ($t[$chan] as $cb) {
					if (is_callable($cb)) {
						call_user_func($cb, $this);
					}
				}
			}
		}
		else { // request callback
			$this->onResponse->executeOne($this);
		}
		$this->checkFree();
		$this->result       = null;
		$this->error        = false;
	}

	public function pushValue($val) {
		if (is_array($this->ptr)) {
			$this->ptr[] = $val;
		} else {
			$this->ptr = $val;
		}
		start:
		if (sizeof($this->ptr) < $this->levelLength) {
			return;
		}
		array_pop($this->stack);
		if (!sizeof($this->stack)) {
			$this->levelLength = null;
			$this->onPacket();
			$this->ptr =& $dummy;
			$this->ptr = null;
			return;
		}

		$this->ptr =& $dummy;

		list ($this->ptr, $this->levelLength) = end($this->stack);

		goto start;
	}

	public function pushLevel($length) {
		if ($length <= 0) {
			$this->pushValue([]);
			return;
		}

		$ptr = [];

		if (is_array($this->ptr)) {
			$this->ptr[] =& $ptr;
		} else {
			$this->ptr =& $ptr;
		}

		$this->ptr =& $ptr;
		$this->stack[] = [&$ptr, $length];
		$this->levelLength = $length;
		$this->ptr =& $ptr;
	}

	/**
	 * Called when new data received
	 * @return void
	 */
	protected function onRead() {
		start:
		if ($this->state === self::STATE_STANDBY) { // outside of packet
			while (($l = $this->readline()) !== null) {
				if ($l === '') {
					continue;
				}
				$char = $l{0};
				if ($char === ':') { // inline integer
					$this->pushValue((int) substr($l, 1));
					goto start;
				}
				elseif (($char === '+') || ($char === '-')) { // inline string
					$this->error        = ($char === '-');
					$this->pushValue(substr($l, 1));
					goto start;
				}
				elseif ($char === '*') { // defines number of elements of incoming array
					$this->pushLevel((int) substr($l, 1));
					goto start;
				}
				elseif ($char === '$') { // defines size of the data block
					if ($l{1} === '-') {
						$this->pushValue(null);
						goto start;
					}
					$this->valueLength = (int)substr($l, 1);
					if ($this->valueLength + 2 > $this->pool->maxAllowedPacket) {
						$this->log('max-allowed-packet ('.$this->pool->config->maxallowedpacket->getHumanValue().') exceed, aborting connection');
						$this->finish();
						return;
					}
					$this->setWatermark($this->valueLength + 2);
					$this->state = self::STATE_BINARY; // binary data block
					break; // stop reading line-by-line
				}
			}
			if ($this->state === self::STATE_STANDBY && $this->getInputLength() > $this->pool->maxAllowedPacket) {
				$this->log('max-allowed-packet ('.$this->pool->config->maxallowedpacket->getHumanValue().') exceed, aborting connection');
				$this->finish();
				return;
			}
		}

		if ($this->state === self::STATE_BINARY) { // inside of binary data block
			if ($this->getInputLength() < $this->valueLength + 2) {
				return; //we do not have a whole packet
			}
			$value = $this->read($this->valueLength);
			if ($this->read(2) !== $this->EOL) {
				$this->finish();
				return;
			}
			$this->state = self::STATE_STANDBY;
			$this->setWatermark(3);
			$this->pushValue($value);
			goto start;
		}
	}

	/**
	 * Set connection free/busy according to onResponse emptiness and ->finished
	 * @return void
	 */
	public function checkFree() {
		$this->setFree(!$this->finished && !$this->subscribed && $this->onResponse && $this->onResponse->count() < $this->maxQueue);
	}
}