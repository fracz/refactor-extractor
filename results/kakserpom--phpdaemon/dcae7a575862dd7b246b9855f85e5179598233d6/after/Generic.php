<?php
namespace PHPDaemon\DNode;
use PHPDaemon\Core\Daemon;
use PHPDaemon\Core\Debug;

/**
 * Generic
 *
 * @package DNode
 *
 * @author  Zorin Vasily <maintainer@daemon.io>
 */
abstract class Generic extends \PHPDaemon\WebSocket\Route {
	use \PHPDaemon\Traits\ClassWatchdog;
	use \PHPDaemon\Traits\StaticObjectWatchdog;

	protected $callbacks = [];
	protected $counter = 0;
	protected $remoteMethods = [];
	protected $localMethods = [];

	/**
	 * Called when the connection is handshaked.
	 * @return void
	 */
	public function onHandshake() {
		parent::onHandshake();
	}

	public function defineLocalMethods($arr) {
		foreach ($arr as $k => $v) {
			$this->localMethods[$k] = $v;
		}
		$this->callRemote('methods', $arr);

	}

	public function extractCallbacks($args, &$list, &$path) {
		foreach ($args as $k => &$v) {
			if (is_array($v)) {
				$path[] = $k;
				$this->extractCallbacks($v, $list, $path);
				array_pop($path);
			} elseif (is_callable($v)) {
				$id = ++$this->counter;
				$this->callbacks[$id] = $v;
				$list[$id] = array_merge($path, [$k]);
			}
		}
	}

	public function callRemote() {
		$args = func_get_args();
		if (!sizeof($args)) {
			return $this;
		}
		$method = array_shift($args);
		$this->callRemoteArray($method, $args);
	}


	public function callRemoteArray($method, $args) {
		if (isset($this->remoteMethods[$method])) {
			call_user_func_array($this->remoteMethods[$method], $args);
			return;
		}
		$pct = [
			'method' => $method,
		];
		if (sizeof($args)) {
			$pct['arguments'] = $args;
			$callbacks = [];
			$path = [];
			$this->extractCallbacks($args, $callbacks, $path);
			if (sizeof($callbacks)) {
				$pct['callbacks'] = $callbacks;
			}
		}
		$this->sendPacket($pct);
	}

	public function methodsMethod($methods) {
		$this->remoteMethods = $methods;
	}

	public function sendPacket($p) {
		if (is_string($p['method']) && ctype_digit($p['method'])) {
			$p['method'] = (int) $p['method'];
		}
		$this->client->sendFrame(json_encode($p, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES)."\n", 'STRING');
	}


	public function fakeIncomingCallExtractCallbacks($args, &$list, &$path) {
		foreach ($args as $k => &$v) {
			if (is_array($v)) {
				$path[] = $k;
				$this->fakeIncomingCallExtractCallbacks($v, $list, $path);
				array_pop($path);
			} elseif (is_callable($v)) {
				$id = ++$this->counter;
				$this->callbacks[$id] = $v;
				$list[$id] = array_merge($path, [$k]);
			}
		}
	}

	public function fakeIncomingCall() {
		$args = func_get_args();
		if (!sizeof($args)) {
			return $this;
		}
		$method = array_shift($args);
		if (sizoef($args)) {
			$path = [];
			$this->fakeIncomingCallExtractCallbacks($args, $callbacks, $path);
			$p = [
				'method' => $method,
				'arguments' => $args,
				'callbacks' => $callbacks,
			];
		}
		$this->onFrame(json_encode($p, JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES)."\n", 'STRING');
	}

	protected static function setPath(&$m, $path, $val) {
		foreach ($path as $p) {
			$m =& $m[$p];
		}
		$m = $val;
	}

	protected static function &getPath(&$m, $path) {
		foreach ($path as $p) {
			$m =& $m[$p];
		}
		return $m;
	}

	/**
	 * @param string $method
	 * @param array $args
	 * @return null|mixed
	 */
	public function __call($m, $args) {
		if (strncmp($m, 'remote_', 7) === 0) {
			$this->callRemoteArray(substr($m, 7), $args);
		} else {
			throw new UndefinedMethodCalled('Call to undefined method ' . get_class($this) . '->' . $method);
		}
	}

	/**
	 * Called when new frame received.
	 * @param string  Frame's contents.
	 * @param integer Frame's type.
	 * @return void
	 */
	public function onFrame($data, $type) {
		foreach (explode("\n", $data) as $pct) {
			if ($pct === '') {
				continue;
			}
			$pct = json_decode($pct, true);
			$m = isset($pct['method']) ? $pct['method'] : null;
			$args = isset($pct['arguments']) ? $pct['arguments'] : [];
			if (isset($pct['callbacks']) && is_array($pct['callbacks'])) {
				foreach ($pct['callbacks'] as $id => $path) {
					static::setPath($args, $path, [$this, 'remote_' . $id]);
				}
			}
			if (isset($pct['links']) && is_array($pct['links'])) {
				foreach ($pct['links'] as $link) {
					static::setPath($args, $link['to'], static::getPath($args, $link['from']));
					unset($r);
				}
			}
			if (is_string($m)) {
				if (isset($this->localMethods[$m])) {
					call_user_func_array($this->localMethods[$m], $args);
				}
				elseif (is_callable($c = [$this, $m . 'Method'])) {
					call_user_func_array($c, $args);
				} else {
					$this->handleException(new UndefinedMethodException);
					continue;
				}
			}
			elseif (is_int($m)) {
				if (!isset($this->callbacks[$m])) {
					$this->handleException(new UndefinedMethodException);
					continue;
				}
				call_user_func_array($this->callbacks[$m], $args);
			} else {
				$this->handleException(new ProtoException);
				continue;
			}
		}
	}
}