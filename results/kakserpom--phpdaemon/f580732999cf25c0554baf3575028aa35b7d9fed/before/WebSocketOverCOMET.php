<?php
/**
 * @package Applications
 * @subpackage WebSocketOverCOMET
 *
 * @author Zorin Vasily <kak.serpom.po.yaitsam@gmail.com>
 */
class WebSocketOverCOMET extends AppInstance {

	public $WS;
	public $requests = array();
	public $sessions = array();
	public $enableRPC = true;
	public $sessCounter = 0;

	/**
	 * Constructor.
	 * @return void
	 */
	public function init() {
		$this->WS = WebSocketServer::getInstance();
	}

	public function initSession($route, $req) {
		if (!isset($this->WS->routes[$route])) {
			if (
				isset(Daemon::$config->logerrors)
				&& Daemon::$config->logerrors
			) {
				Daemon::log(__METHOD__ . ': undefined route \'' . $route . '\'.');
			}
			return array('error' => 404);
		}
		$sess = new WebSocketOverCOMET_Session(
				$route,
				$this,
				sprintf('%x', crc32(microtime(true) . "\x00" . $req->attrs->server['REMOTE_ADDR']))
		);
		if (!$sess->downstream) {
			return array('error' => 403);
		}
		$id = Daemon::$process->id . '.' . $sess->id . '.' . $sess->authKey;
		return array('id' => $id);
	}
	/**
	 * Creates Request.
	 * @param object Request.
	 * @param object Upstream application instance.
	 * @return object Request.
	 */
	public function beginRequest($req, $upstream) {
		return $this->requests[$req->id] = new WebSocketOverCOMET_Request($this, $upstream, $req);
	}
	public function s2c($reqId, $data, $ts) {
		if (!isset($this->requests[$reqId])) {
			return;
		}
		$req = $this->requests[$reqId];
		if ($req->jsid) {
			$body = 'Response' . $req->jsid . ' = ' . $data . ";\n";
		} else {
			$body = '<script type="text/javascript">WebSocket.onmessage(' . $data . ");</script>\n";
		}
		$req->out($body);
		$req->finish();
	}

	public function c2s($fullId, $body) {
		list($sessId, $authKey) = explode('.', $fullId, 2);
		$sessId = (int) $sessId;
		if (!isset($this->sessions[$sessId])) {
			return;
		}
		$sess = $this->sessions[$sessId];
		if (!isset($sess->authKey) || $authKey !== $sess->authKey) {
			return;
		}
		if (!isset($sess->downstream)) {
			return;
		}
		$sess->downstream->onFrame($body, WebSocketServer::STRING);
		Timer::setTimeout($sess->finishTimer);
	}

	public function poll($pollWorker, $pollReqId, $fullId, $ts) {
		list($sessId, $authKey) = explode('.', $fullId, 2);
		$sessId = (int) $sessId;
		if (!isset($this->sessions[$sessId])) {
			return;
		}
		$sess = $this->sessions[$sessId];
		if (!isset($sess->polling)) {
			return;
		}
		if (!isset($sess->authKey) || $authKey !== $sess->authKey) {
			return;
		}
		$sess->polling->push(array($pollWorker, $pollReqId));
		$sess->flushBufferedPackets($ts);
		Timer::setTimeout($sess->finishTimer);


	}
}
class WebSocketOverCOMET_Session {
	public $downstream;
	public $polling;
	public $callbacks;
	public $authKey;
	public $id;
	public $appInstance;
	public $bufferedPackets = array();
	public $finished = false;
	public $timeout = 30; // 30
	public function __construct($route, $appInstance, $authKey) {
		$this->polling = new SplStack;
		$this->callbacks = new SplStackCallbacks;
		$this->authKey = $authKey;
		$this->id = ++$appInstance->sessCounter;
		$this->appInstance = $appInstance;
		if (!$this->downstream = call_user_func($this->appInstance->WS->routes[$route], $this)) {
			return;
		}
		$this->finishTimer = setTimeout(array($this, 'finishTimer'), $this->timeout * 1e6);
		$this->appInstance->sessions[$this->id] = $this;
	}

	public function finish() {
		if ($this->finished) {
			return;
		}
		$this->finished = true;
		$this->onFinish();
	}

	public function onFinish() {
		if (isset($this->upstream)) {
			$this->upstream->onFinish();
		}
		unset($this->downstream);
		if ($this->finishTimer !== null) {
			Timer::remove($this->finishTimer);
			$this->finishTimer = null;
		}
		unset($this->appInstance->sessions[$this->id]);
	}
	public function finishTimer($timer) {
		$this->finish();
	}

	public function onWrite() {
		$this->callbacks->executeAll($this->downstream);
		if (is_callable(array($this->downstream, 'onWrite'))) {
			$this->downstream->onWrite();
		}
	}

	public function compareFloats($a, $b, $precision = 3) {
		$k = pow(10, $precision);
		$a = round($a * $k) / $k;
		$b = round($b * $k) / $k;
		$cmp = strnatcmp((string) $a, (string) $b);

		return $cmp;
	}

	/**
	 * Flushes buffered packets (only for the long-polling method)
	 * @param string Optional. Last timestamp.
	 * @return void
	 */
	public function flushBufferedPackets($ts = NULL) {
		if ($this->polling->isEmpty()|| !sizeof($this->bufferedPackets)) {
			return;
		}

		if ($ts !== NULL) {
			$ts = (float) $ts;

			for ($i = sizeof($this->bufferedPackets) - 1; $i >= 0; --$i) {
				if ($this->compareFloats($this->bufferedPackets[$i][2], $ts) <= 0) {
					$this->bufferedPackets = array_slice($this->bufferedPackets, $i + 1);
					break;
				}
			}
		}

		if (!sizeof($this->bufferedPackets)) {
			return;
		}

		$ts = microtime(true);

		$data = json_encode($this->bufferedPackets);
		while (!$this->polling->isEmpty()) {
			list ($workerId, $reqId) = $this->polling->pop();
			$workerId = (int) $workerId;
			$this->appInstance->directCall($workerId, 's2c', array($reqId, $data, $ts));
		}

		$this->onWrite();
	}

	/**
	 * Sends a frame.
	 * @param string Frame's data.
	 * @param integer Frame's type. See the constants.
	 * @param callback Optional. Callback called when the frame is received by client.
	 * @return boolean Success.
	 */
	public function sendFrame($data, $type = 0x00, $callback = NULL) {
		$this->bufferedPackets[] = array($type, $data, microtime(TRUE));
		if ($callback !== null) {
			$this->callbacks->push($callback);
		}
		$this->flushBufferedPackets();
		return true;
	}

}
class WebSocketOverCOMET_Request extends HTTPRequest {

	public $inited = FALSE;
	public $authKey;
	public $type;
	public $reqIdAuthKey;
	public $jsid;

	/**
	 * Constructor.
	 * @return void
	 */
	public function init() {
		if (isset($this->attrs->get['_pull'])) {
			$this->type = 'pull';
		}
		elseif (
			isset($this->attrs->get['_poll'])
			&& isset($this->attrs->get['_init'])
		) {
			$this->type = 'pollInit';
		}
		elseif (isset($this->attrs->get['_poll'])) {
			$this->type = 'poll';
		} else {
			$this->type = 'push';
		}

		$this->header('Cache-Control: no-cache, must-revalidate');
		$this->header('Expires: Sat, 26 Jul 1997 05:00:00 GMT');
	}

	/**
	 * Called when request iterated.
	 * @return integer Status.
	 */
	public function run() {
		if ($this->type === 'push') {
			$ret = array();
			$id = self::getString($_REQUEST['_id']);
			if (strpos($id, '.') === false) {
					$ret['error'] = 'Bad cookie.';
			}
			elseif (
				!isset($_REQUEST['data'])
				|| !is_string($_REQUEST['data'])
			) {
				$ret['error'] = 'No data.';
			}
			else {
				list ($workerId, $this->reqIdAuthKey) = explode('.', $id, 2);
				$workerId = (int) $workerId;
				$this->appInstance->directCall($workerId, 'c2s', array($this->reqIdAuthKey,	self::getString($_REQUEST['data'])));
			}

			echo json_encode($ret);

			return;
		}
		elseif ($this->type === 'pull') {
			if (!$this->inited) {
				$this->authKey = sprintf('%x', crc32(microtime() . "\x00" . $this->attrs->server['REMOTE_ADDR']));
				$this->header('Content-Type: text/html; charset=utf-8');
				$this->inited = TRUE;
				$this->out('<!--' . str_repeat('-', 1024) . '->'); // Padding
				$this->out('<script type="text/javascript"> WebSocket.onopen("' . $this->appInstance->ipcId
					. '.' . $this->id . '.' . $this->authKey
					. '"); </script>'."\n"
				);

				$appName = self::getString($_REQUEST['_route']);

				if (!isset($this->appInstance->WS->routes[$appName])) {
					if (
						isset(Daemon::$config->logerrors->value)
						&& Daemon::$config->logerrors->value
					) {
						Daemon::log(__METHOD__ . ': undefined route \'' . $appName . '\'.');
					}

					return;
				}

				if (!$this->downstream = call_user_func($this->appInstance->WS->routes[$appName], $this)) {
					return;
				}
			}

			$this->sleep(1);
		}
		elseif ($this->type === 'pollInit') {
			$this->header('Content-Type: application/x-javascript; charset=utf-8');
			$route = self::getString($_REQUEST['_route']);
			$res = $this->appInstance->initSession($route, $this);
			if (isset($_REQUEST['_script'])) {
				$q = self::getString($_GET['q']);
				if (ctype_digit($q)) {
					$this->out('Response' . $q . ' = ' . json_encode($res) . ";\n");
				}
			} else {
				$this->out(json_encode($res));
			}
		}
		elseif ($this->type === 'poll') {
			if (!$this->inited) {
				$this->header('Content-Type: text/plain; charset=utf-8');
				$this->inited = TRUE;

				$ret = array();
				$id = self::getString($_REQUEST['_id']);
				if (strpos($id, '.') === false) {
					$ret['error'] = 'Bad cookie.';
				}
				else {
					list ($workerId, $this->reqIdAuthKey) = explode('.', $id, 2);
					$workerId = (int) $workerId;
					$this->appInstance->directCall($workerId, 'poll', array(
						Daemon::$process->id,
						$this->id,
						$this->reqIdAuthKey,
						self::getString($_REQUEST['ts'])
					));
				}

				if (isset($req->attrs->get['_script'])) {
					$req->header('Content-Type: application/x-javascript; charset=utf-8');
					$q = self::getString($req->attrs->get['q']);

					if (!ctype_digit($q)) {
						$ret['error'] = 'Bad q.';
					}
					$this->jsid = $q;
				}

				if (sizeof($ret)) {
					echo json_encode($ret);
					return;
				}

				$this->out("\n");
				$this->sleep(15);
			}

			return;
		}
	}

	/**
	 * Called when the request aborted.
	 * @return void
	 */
	public function onAbort() {
		if ($this->type !== 'pollInit') {
			if (isset($this->downstream)) {
				$this->downstream->onFinish();
				unset($this->downstream);
			}

			$this->finish();
		}
	}

	/**
	 * Called when the request finished.
	 * @return void
	 */
	public function onFinish() {
		unset($this->appInstance->queue[$this->id]);
	}

}
