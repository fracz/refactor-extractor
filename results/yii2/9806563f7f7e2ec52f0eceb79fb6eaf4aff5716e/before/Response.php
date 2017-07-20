<?php
/**
 * @link http://www.yiiframework.com/
 * @copyright Copyright (c) 2008 Yii Software LLC
 * @license http://www.yiiframework.com/license/
 */

namespace yii\web;

use Yii;
use yii\base\HttpException;
use yii\base\InvalidParamException;
use yii\helpers\FileHelper;
use yii\helpers\Html;
use yii\helpers\Json;
use yii\helpers\SecurityHelper;
use yii\helpers\StringHelper;

/**
 * @author Qiang Xue <qiang.xue@gmail.com>
 * @author Carsten Brandt <mail@cebe.cc>
 * @since 2.0
 */
class Response extends \yii\base\Response
{
	/**
	 * @var integer the HTTP status code that should be used when redirecting in AJAX mode.
	 * This is used by [[redirect()]]. A 2xx code should normally be used for this purpose
	 * so that the AJAX handler will treat the response as a success.
	 * @see redirect
	 */
	public $ajaxRedirectCode = 278;
	/**
	 * @var string
	 */
	public $content;
	/**
	 * @var string
	 */
	public $statusText;
	/**
	 * @var string the charset to use. If not set, [[\yii\base\Application::charset]] will be used.
	 */
	public $charset;
	/**
	 * @var string the version of the HTTP protocol to use
	 */
	public $version = '1.0';

	/**
	 * @var array list of HTTP status codes and the corresponding texts
	 */
	public static $statusTexts = array(
		100 => 'Continue',
		101 => 'Switching Protocols',
		102 => 'Processing',
		118 => 'Connection timed out',
		200 => 'OK',
		201 => 'Created',
		202 => 'Accepted',
		203 => 'Non-Authoritative',
		204 => 'No Content',
		205 => 'Reset Content',
		206 => 'Partial Content',
		207 => 'Multi-Status',
		208 => 'Already Reported',
		210 => 'Content Different',
		226 => 'IM Used',
		300 => 'Multiple Choices',
		301 => 'Moved Permanently',
		302 => 'Found',
		303 => 'See Other',
		304 => 'Not Modified',
		305 => 'Use Proxy',
		306 => 'Reserved',
		307 => 'Temporary Redirect',
		308 => 'Permanent Redirect',
		310 => 'Too many Redirect',
		400 => 'Bad Request',
		401 => 'Unauthorized',
		402 => 'Payment Required',
		403 => 'Forbidden',
		404 => 'Not Found',
		405 => 'Method Not Allowed',
		406 => 'Not Acceptable',
		407 => 'Proxy Authentication Required',
		408 => 'Request Time-out',
		409 => 'Conflict',
		410 => 'Gone',
		411 => 'Length Required',
		412 => 'Precondition Failed',
		413 => 'Request Entity Too Large',
		414 => 'Request-URI Too Long',
		415 => 'Unsupported Media Type',
		416 => 'Requested range unsatisfiable',
		417 => 'Expectation failed',
		418 => 'I’m a teapot',
		422 => 'Unprocessable entity',
		423 => 'Locked',
		424 => 'Method failure',
		425 => 'Unordered Collection',
		426 => 'Upgrade Required',
		428 => 'Precondition Required',
		429 => 'Too Many Requests',
		431 => 'Request Header Fields Too Large',
		449 => 'Retry With',
		450 => 'Blocked by Windows Parental Controls',
		500 => 'Internal Server Error',
		501 => 'Not Implemented',
		502 => 'Bad Gateway ou Proxy Error',
		503 => 'Service Unavailable',
		504 => 'Gateway Time-out',
		505 => 'HTTP Version not supported',
		507 => 'Insufficient storage',
		508 => 'Loop Detected',
		509 => 'Bandwidth Limit Exceeded',
		510 => 'Not Extended',
		511 => 'Network Authentication Required',
	);

	private $_statusCode;
	/**
	 * @var HeaderCollection
	 */
	private $_headers;


	public function init()
	{
		if ($this->charset === null) {
			$this->charset = Yii::$app->charset;
		}
	}

	public function begin()
	{
		parent::begin();
		$this->beginOutput();
	}

	public function end()
	{
		$this->content .= $this->endOutput();
		$this->send();
		parent::end();
	}

	public function getStatusCode()
	{
		return $this->_statusCode;
	}

	public function setStatusCode($value, $text = null)
	{
		$this->_statusCode = (int)$value;
		if ($this->isInvalid()) {
			throw new InvalidParamException("The HTTP status code is invalid: $value");
		}
		if ($text === null) {
			$this->statusText = isset(self::$statusTexts[$this->_statusCode]) ? self::$statusTexts[$this->_statusCode] : '';
		} else {
			$this->statusText = $text;
		}
	}

	/**
	 * Returns the header collection.
	 * The header collection contains the currently registered HTTP headers.
	 * @return HeaderCollection the header collection
	 */
	public function getHeaders()
	{
		if ($this->_headers === null) {
			$this->_headers = new HeaderCollection;
		}
		return $this->_headers;
	}

	public function renderJson($data)
	{
		$this->getHeaders()->set('content-type', 'application/json');
		$this->content = Json::encode($data);
	}

	public function renderJsonp($data, $callbackName)
	{
		$this->getHeaders()->set('content-type', 'text/javascript');
		$data = Json::encode($data);
		$this->content = "$callbackName($data);";
	}

	/**
	 * Sends the response to the client.
	 * @return boolean true if the response was sent
	 */
	public function send()
	{
		$this->sendHeaders();
		$this->sendContent();
	}

	public function reset()
	{
		$this->_headers = null;
		$this->_statusCode = null;
		$this->statusText = null;
		$this->content = null;
	}

	/**
	 * Sends the response headers to the client
	 */
	protected function sendHeaders()
	{
		if (headers_sent()) {
			return;
		}
		$statusCode = $this->getStatusCode();
		if ($statusCode !== null) {
			header("HTTP/{$this->version} $statusCode {$this->statusText}");
		}
		if ($this->_headers) {
			$headers = $this->getHeaders();
			foreach ($headers as $name => $values) {
				foreach ($values as $value) {
					header("$name: $value", false);
				}
			}
			$headers->removeAll();
		}
		$this->sendCookies();
	}

	/**
	 * Sends the cookies to the client.
	 */
	protected function sendCookies()
	{
		if ($this->_cookies === null) {
			return;
		}
		$request = Yii::$app->getRequest();
		if ($request->enableCookieValidation) {
			$validationKey = $request->getCookieValidationKey();
		}
		foreach ($this->getCookies() as $cookie) {
			$value = $cookie->value;
			if ($cookie->expire != 1  && isset($validationKey)) {
				$value = SecurityHelper::hashData(serialize($value), $validationKey);
			}
			setcookie($cookie->name, $value, $cookie->expire, $cookie->path, $cookie->domain, $cookie->secure, $cookie->httpOnly);
		}
		$this->getCookies()->removeAll();
	}

	/**
	 * Sends the response content to the client
	 */
	protected function sendContent()
	{
		echo $this->content;
		$this->content = null;
	}

	/**
	 * Sends a file to user.
	 * @param string $fileName file name
	 * @param string $content content to be set.
	 * @param string $mimeType mime type of the content. If null, it will be guessed automatically based on the given file name.
	 * @param boolean $terminate whether to terminate the current application after calling this method
	 * @throws HttpException when range request is not satisfiable.
	 */
	public function sendFile($fileName, $content, $mimeType = null, $terminate = true)
	{
		if ($mimeType === null && (($mimeType = FileHelper::getMimeTypeByExtension($fileName)) === null)) {
			$mimeType = 'application/octet-stream';
		}

		$fileSize = StringHelper::strlen($content);
		$contentStart = 0;
		$contentEnd = $fileSize - 1;

		$headers = $this->getHeaders();

		// tell the client that we accept range requests
		$headers->set('Accept-Ranges', 'bytes');

		if (isset($_SERVER['HTTP_RANGE'])) {
			// client sent us a multibyte range, can not hold this one for now
			if (strpos($_SERVER['HTTP_RANGE'], ',') !== false) {
				$headers->set('Content-Range', "bytes $contentStart-$contentEnd/$fileSize");
				throw new HttpException(416, 'Requested Range Not Satisfiable');
			}

			$range = str_replace('bytes=', '', $_SERVER['HTTP_RANGE']);

			// range requests starts from "-", so it means that data must be dumped the end point.
			if ($range[0] === '-') {
				$contentStart = $fileSize - substr($range, 1);
			} else {
				$range = explode('-', $range);
				$contentStart = $range[0];

				// check if the last-byte-pos presents in header
				if ((isset($range[1]) && is_numeric($range[1]))) {
					$contentEnd = $range[1];
				}
			}

			/* Check the range and make sure it's treated according to the specs.
			 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
			 */
			// End bytes can not be larger than $end.
			$contentEnd = ($contentEnd > $fileSize) ? $fileSize - 1 : $contentEnd;

			// Validate the requested range and return an error if it's not correct.
			$wrongContentStart = ($contentStart > $contentEnd || $contentStart > $fileSize - 1 || $contentStart < 0);

			if ($wrongContentStart) {
				$headers->set('Content-Range', "bytes $contentStart-$contentEnd/$fileSize");
				throw new HttpException(416, 'Requested Range Not Satisfiable');
			}

			$this->setStatusCode(206);
			$headers->set('Content-Range', "bytes $contentStart-$contentEnd/$fileSize");
		} else {
			$this->setStatusCode(200);
		}

		$length = $contentEnd - $contentStart + 1; // Calculate new content length

		$headers->set('Pragma', 'public')
			->set('Expires', '0')
			->set('Cache-Control', 'must-revalidate, post-check=0, pre-check=0')
			->set('Content-Type', $mimeType)
			->set('Content-Length', $length)
			->set('Content-Disposition', "attachment; filename=\"$fileName\"")
			->set('Content-Transfer-Encoding', 'binary');

		$content = StringHelper::substr($content, $contentStart, $length);

		if ($terminate) {
			// clean up the application first because the file downloading could take long time
			// which may cause timeout of some resources (such as DB connection)
			ob_start();
			Yii::$app->end(0, false);
			ob_end_clean();
			$this->content = $content;
			exit(0);
		} else {
			$this->content = $content;
		}
	}

	public function sendStream($handle, $options = array())
	{
		fseek($handle, 0, SEEK_END);
		$fileSize = ftell($handle);

		$contentStart = 0;
		$contentEnd = $fileSize - 1;

		$headers = $this->getHeaders();

		if (isset($_SERVER['HTTP_RANGE'])) {
			// client sent us a multibyte range, can not hold this one for now
			if (strpos($_SERVER['HTTP_RANGE'], ',') !== false) {
				$headers->set('Content-Range', "bytes $contentStart-$contentEnd/$fileSize");
				throw new HttpException(416, Yii::t('yii', 'Requested range not satisfiable'));
			}

			$range = str_replace('bytes=', '', $_SERVER['HTTP_RANGE']);

			// range requests starts from "-", so it means that data must be dumped the end point.
			if ($range[0] === '-') {
				$contentStart = $fileSize - substr($range, 1);
			} else {
				$range = explode('-', $range);
				$contentStart = $range[0];

				// check if the last-byte-pos presents in header
				if ((isset($range[1]) && is_numeric($range[1]))) {
					$contentEnd = $range[1];
				}
			}

			/* Check the range and make sure it's treated according to the specs.
			 * http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html
			 */
			// End bytes can not be larger than $end.
			$contentEnd = $contentEnd > $fileSize ? $fileSize - 1 : $contentEnd;

			// Validate the requested range and return an error if it's not correct.
			if ($contentStart > $contentEnd || $contentStart > $fileSize - 1 || $contentStart < 0) {
				$headers->set('Content-Range', "bytes $contentStart-$contentEnd/$fileSize");
				throw new HttpException(416, Yii::t('yii', 'Requested range not satisfiable'));
			}

			$this->setStatusCode(206);
			$headers->set('Content-Range', "bytes $contentStart-$contentEnd/$fileSize");
		} else {
			$this->setStatusCode(200);
		}

		if (isset($options['mimeType'])) {
			$headers->set('Content-Type', $options['mimeType']);
		}

		$length = $contentEnd - $contentStart + 1;
		$disposition = empty($options['disposition']) ? 'attachment' : $options['disposition'];
		if (!isset($options['saveName'])) {
			$options['saveName'] = 'data';
		}

		$headers->set('Pragma', 'public')
			->set('Expires', '0')
			->set('Cache-Control', 'must-revalidate, post-check=0, pre-check=0')
			->set('Content-Disposition', "$disposition; filename=\"{$options['saveName']}\"")
			->set('Content-Length', $length)
			->set('Content-Transfer-Encoding', 'binary');

		if (isset($options['headers'])) {
			foreach ($options['headers'] as $header => $value) {
				$headers->add($header, $value);
			}
		}

		fseek($handle, $contentStart);

		set_time_limit(0); // Reset time limit for big files

		$chunkSize = 8 * 1024 * 1024; // 8MB per chunk
		while (!feof($handle) && ($fPointer = ftell($handle)) <= $contentEnd) {
			if ($fPointer + $chunkSize > $contentEnd) {
				$chunkSize = $contentEnd - $fPointer + 1;
			}
			echo fread($handle, $chunkSize);
			flush(); // Free up memory. Otherwise large files will trigger PHP's memory limit.
		}

		fclose($handle);
	}

	/**
	 * Sends existing file to a browser as a download using x-sendfile.
	 *
	 * X-Sendfile is a feature allowing a web application to redirect the request for a file to the webserver
	 * that in turn processes the request, this way eliminating the need to perform tasks like reading the file
	 * and sending it to the user. When dealing with a lot of files (or very big files) this can lead to a great
	 * increase in performance as the web application is allowed to terminate earlier while the webserver is
	 * handling the request.
	 *
	 * The request is sent to the server through a special non-standard HTTP-header.
	 * When the web server encounters the presence of such header it will discard all output and send the file
	 * specified by that header using web server internals including all optimizations like caching-headers.
	 *
	 * As this header directive is non-standard different directives exists for different web servers applications:
	 *
	 * - Apache: [X-Sendfile](http://tn123.org/mod_xsendfile)
	 * - Lighttpd v1.4: [X-LIGHTTPD-send-file](http://redmine.lighttpd.net/projects/lighttpd/wiki/X-LIGHTTPD-send-file)
	 * - Lighttpd v1.5: [X-Sendfile](http://redmine.lighttpd.net/projects/lighttpd/wiki/X-LIGHTTPD-send-file)
	 * - Nginx: [X-Accel-Redirect](http://wiki.nginx.org/XSendfile)
	 * - Cherokee: [X-Sendfile and X-Accel-Redirect](http://www.cherokee-project.com/doc/other_goodies.html#x-sendfile)
	 *
	 * So for this method to work the X-SENDFILE option/module should be enabled by the web server and
	 * a proper xHeader should be sent.
	 *
	 * **Note**
	 *
	 * This option allows to download files that are not under web folders, and even files that are otherwise protected
	 * (deny from all) like `.htaccess`.
	 *
	 * **Side effects**
	 *
	 * If this option is disabled by the web server, when this method is called a download configuration dialog
	 * will open but the downloaded file will have 0 bytes.
	 *
	 * **Known issues**
	 *
	 * There is a Bug with Internet Explorer 6, 7 and 8 when X-SENDFILE is used over an SSL connection, it will show
	 * an error message like this: "Internet Explorer was not able to open this Internet site. The requested site
	 * is either unavailable or cannot be found.". You can work around this problem by removing the `Pragma`-header.
	 *
	 * **Example**
	 *
	 * ~~~
	 * Yii::app()->request->xSendFile('/home/user/Pictures/picture1.jpg', array(
	 *      'saveName' => 'image1.jpg',
	 *      'mimeType' => 'image/jpeg',
	 *      'terminate' => false,
	 * ));
	 *
	 * @param string $filePath file name with full path
	 * @param array $options additional options:
	 *
	 * - saveName: file name shown to the user. If not set, the name will be determined from `$filePath`.
	 * - mimeType: MIME type of the file. If not set, it will be determined based on the file name.
	 * - xHeader: appropriate x-sendfile header, defaults to "X-Sendfile".
	 * - disposition: either "attachment" or "inline". This specifies whether the file will be downloaded
	 *   or shown inline. Defaults to "attachment".
	 * - headers: an array of additional http headers in name-value pairs.
	 */
	public function xSendFile($filePath, $options = array())
	{
		$headers = $this->getHeaders();

		$headers->set(empty($options['xHeader']) ? 'X-Sendfile' : $options['xHeader'], $filePath);

		if (!isset($options['mimeType'])) {
			if (($options['mimeType'] = FileHelper::getMimeTypeByExtension($filePath)) === null) {
				$options['mimeType'] = 'text/plain';
			}
		}
		$headers->set('Content-Type', $options['mimeType']);

		$disposition = empty($options['disposition']) ? 'attachment' : $options['disposition'];
		if (!isset($options['saveName'])) {
			$options['saveName'] = basename($filePath);
		}
		$headers->set('Content-Disposition', "$disposition; filename=\"{$options['saveName']}\"");

		if (isset($options['headers'])) {
			foreach ($options['headers'] as $header => $value) {
				$headers->add($header, $value);
			}
		}

		$this->send();
	}

	/**
	 * Redirects the browser to the specified URL.
	 * This method will send out a "Location" header to achieve the redirection.
	 * In AJAX mode, this normally will not work as expected unless there are some
	 * client-side JavaScript code handling the redirection. To help achieve this goal,
	 * this method will use [[ajaxRedirectCode]] as the HTTP status code when performing
	 * redirection in AJAX mode. The following JavaScript code may be used on the client
	 * side to handle the redirection response:
	 *
	 * ~~~
	 * $(document).ajaxSuccess(function(event, xhr, settings) {
	 *     if (xhr.status == 278) {
	 *         window.location = xhr.getResponseHeader('Location');
	 *     }
	 * });
	 * ~~~
	 *
	 * @param array|string $url the URL to be redirected to. [[\yii\helpers\Html::url()]]
	 * will be used to normalize the URL. If the resulting URL is still a relative URL
	 * (one without host info), the current request host info will be used.
	 * @param boolean $terminate whether to terminate the current application
	 * @param integer $statusCode the HTTP status code. Defaults to 302.
	 * See [[http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html]]
	 * for details about HTTP status code.
	 * Note that if the request is an AJAX request, [[ajaxRedirectCode]] will be used instead.
	 */
	public function redirect($url, $terminate = true, $statusCode = 302)
	{
		$url = Html::url($url);
		if (strpos($url, '/') === 0 && strpos($url, '//') !== 0) {
			$url = Yii::$app->getRequest()->getHostInfo() . $url;
		}
		if (Yii::$app->getRequest()->getIsAjax()) {
			$statusCode = $this->ajaxRedirectCode;
		}
		$this->getHeaders()->set('Location', $url);
		$this->setStatusCode($statusCode);
		if ($terminate) {
			Yii::$app->end();
		}
	}

	/**
	 * Refreshes the current page.
	 * The effect of this method call is the same as the user pressing the refresh button of his browser
	 * (without re-posting data).
	 * @param boolean $terminate whether to terminate the current application after calling this method
	 * @param string $anchor the anchor that should be appended to the redirection URL.
	 * Defaults to empty. Make sure the anchor starts with '#' if you want to specify it.
	 */
	public function refresh($terminate = true, $anchor = '')
	{
		$this->redirect(Yii::$app->getRequest()->getUrl() . $anchor, $terminate);
	}

	private $_cookies;

	/**
	 * Returns the cookie collection.
	 * Through the returned cookie collection, you add or remove cookies as follows,
	 *
	 * ~~~
	 * // add a cookie
	 * $response->cookies->add(new Cookie(array(
	 *     'name' => $name,
	 *     'value' => $value,
	 * ));
	 *
	 * // remove a cookie
	 * $response->cookies->remove('name');
	 * // alternatively
	 * unset($response->cookies['name']);
	 * ~~~
	 *
	 * @return CookieCollection the cookie collection.
	 */
	public function getCookies()
	{
		if ($this->_cookies === null) {
			$this->_cookies = new CookieCollection;
		}
		return $this->_cookies;
	}

	/**
	 * @return boolean whether this response has a valid [[statusCode]].
	 */
	public function isInvalid()
	{
		return $this->getStatusCode() < 100 || $this->getStatusCode() >= 600;
	}

	/**
	 * @return boolean whether this response is informational
	 */
	public function isInformational()
	{
		return $this->getStatusCode() >= 100 && $this->getStatusCode() < 200;
	}

	/**
	 * @return boolean whether this response is successfully
	 */
	public function isSuccessful()
	{
		return $this->getStatusCode() >= 200 && $this->getStatusCode() < 300;
	}

	/**
	 * @return boolean whether this response is a redirection
	 */
	public function isRedirection()
	{
		return $this->getStatusCode() >= 300 && $this->getStatusCode() < 400;
	}

	/**
	 * @return boolean whether this response indicates a client error
	 */
	public function isClientError()
	{
		return $this->getStatusCode() >= 400 && $this->getStatusCode() < 500;
	}

	/**
	 * @return boolean whether this response indicates a server error
	 */
	public function isServerError()
	{
		return $this->getStatusCode() >= 500 && $this->getStatusCode() < 600;
	}

	/**
	 * @return boolean whether this response is OK
	 */
	public function isOk()
	{
		return 200 === $this->getStatusCode();
	}

	/**
	 * @return boolean whether this response indicates the current request is forbidden
	 */
	public function isForbidden()
	{
		return 403 === $this->getStatusCode();
	}

	/**
	 * @return boolean whether this response indicates the currently requested resource is not found
	 */
	public function isNotFound()
	{
		return 404 === $this->getStatusCode();
	}

	/**
	 * @return boolean whether this response is empty
	 */
	public function isEmpty()
	{
		return in_array($this->getStatusCode(), array(201, 204, 304));
	}
}