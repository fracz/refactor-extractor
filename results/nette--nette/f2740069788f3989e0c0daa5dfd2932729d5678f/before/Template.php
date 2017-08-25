<?php

/**
 * Nette Framework
 *
 * Copyright (c) 2004, 2009 David Grudl (http://davidgrudl.com)
 *
 * This source file is subject to the "Nette license" that is bundled
 * with this package in the file license.txt.
 *
 * For more information please see http://nettephp.com
 *
 * @copyright  Copyright (c) 2004, 2009 David Grudl
 * @license    http://nettephp.com/license  Nette license
 * @link       http://nettephp.com
 * @category   Nette
 * @package    Nette\Templates
 * @version    $Id$
 */

/*namespace Nette\Templates;*/



require_once dirname(__FILE__) . '/../Object.php';

require_once dirname(__FILE__) . '/../Templates/IFileTemplate.php';



/**
 * Template.
 *
 * @author     David Grudl
 * @copyright  Copyright (c) 2004, 2009 David Grudl
 * @package    Nette\Templates
 */
class Template extends /*Nette\*/Object implements IFileTemplate
{
	/** @var bool */
	public $warnOnUndefined = TRUE;

	/** @var array of function(Template $sender); Occurs before a template is compiled - implement to customize the filters */
	public $onPrepareFilters = array();

	/** @var string */
	private $file;

	/** @var array */
	private $params = array();

	/** @var array compile-time filters */
	private $filters = array();

	/** @var array run-time helpers */
	private $helpers = array();

	/** @var array */
	private $helperLoaders = array();

	/** @var int */
	public static $cacheExpire = FALSE;

	/** @var Nette\Caching\ICacheStorage */
	private static $cacheStorage;



	/**
	 * Sets the path to the template file.
	 * @param  string  template file path
	 * @return void
	 */
	public function setFile($file)
	{
		$this->file = $file;
	}



	/**
	 * Returns the path to the template file.
	 * @return string  template file path
	 */
	public function getFile()
	{
		return $this->file;
	}



	/**
	 * Creates subtemplate.
	 * @param  string  file name
	 * @param  array   parameters (optional)
	 * @return Template
	 */
	public function subTemplate($file, array $params = array())
	{
		if ($file instanceof self) {
			$tpl = $file;

		} elseif ($file == NULL) { // intentionally ==
			throw new /*\*/InvalidArgumentException("Template file name was not specified.");

		} else {
			$tpl = clone $this;
			if (substr($file, 0, 1) !== '/' && substr($file, 1, 1) !== ':') {
				$file = dirname($this->file) . '/' . $file;
			}
			$tpl->setFile($file);
		}

		$tpl->params = $params;
		return $tpl;
	}



	/**
	 * Registers callback as template compile-time filter.
	 * @param  callback
	 * @return void
	 */
	public function registerFilter($callback)
	{
		/**/fixCallback($callback);/**/
		if (!is_callable($callback)) {
			$able = is_callable($callback, TRUE, $textual);
			throw new /*\*/InvalidArgumentException("Filter '$textual' is not " . ($able ? 'callable.' : 'valid PHP callback.'));
		}
		if (in_array($callback, $this->filters, TRUE)) {
			is_callable($callback, TRUE, $textual);
			throw new /*\*/InvalidStateException("Filter '$textual' was registered twice.");
		}
		$this->filters[] = $callback;
	}



	/**
	 * Returns all registered compile-time filters.
	 * @return array
	 */
	final public function getFilters()
	{
		return $this->filters;
	}



	/********************* rendering ****************d*g**/



	/**
	 * Renders template to output.
	 * @return void
	 */
	public function render()
	{
		if ($this->file == NULL) { // intentionally ==
			throw new /*\*/InvalidStateException("Template file name was not specified.");

		} elseif (!is_file($this->file) || !is_readable($this->file)) {
			throw new /*\*/FileNotFoundException("Missing template file '$this->file'.");
		}

		$this->params['template'] = $this;

		$cache = new /*Nette\Caching\*/Cache($this->getCacheStorage(), 'Nette.Template');
		$key = md5($this->file) . '.' . basename($this->file);
		$cached = $content = $cache[$key];

		if ($content === NULL) {
			if (!$this->filters) {
				$this->onPrepareFilters($this);
			}

			if (!$this->filters) {
				/*Nette\Loaders\*/LimitedScope::load($this->file, $this->params);
				return;
			}

			// compiling
			$content = file_get_contents($this->file);

			foreach ($this->filters as $filter) {
				// remove PHP code
				$res = '';
				$blocks = array();
				unset($php);
				foreach (token_get_all($content) as $token) {
					if (is_array($token)) {
						if ($token[0] === T_INLINE_HTML) {
							$res .= $token[1];
							unset($php);
						} else {
							if (!isset($php)) {
								$res .= $php = "\x01@php:p" . count($blocks) . "@\x02";
								$php = & $blocks[$php];
							}
							$php .= $token[1];
						}
					} else {
						$php .= $token;
					}
				}

				try {
					$content = call_user_func($filter, $res);
				} catch (Exception $e) {
					is_callable($filter, TRUE, $textual);
					$file = $this->file;
					try {
						$file = str_replace(/*Nette\*/Environment::getVariable('templatesDir'), "\xE2\x80\xA6", $file);
					} catch (Exception $foo) {
					}
					throw new /*\*/InvalidStateException("Filter $textual: " . $e->getMessage() . " (in file $file)", 0, $e);
				}

				$content = strtr($content, $blocks); // put PHP code back
			}

			$content = "<?php\n// template $this->file\n?>$content";
			$cache->save(
				$key,
				$content,
				array(
					/*Nette\Caching\*/Cache::FILES => $this->file,
					/*Nette\Caching\*/Cache::EXPIRE => self::$cacheExpire,
				)
			);
			$cached = $cache[$key];
		}

		if ($cached !== NULL && self::$cacheStorage instanceof TemplateCacheStorage) {
			/*Nette\Loaders\*/LimitedScope::load($cached['file'], $this->params);
			fclose($cached['handle']);

		} else {
			/*Nette\Loaders\*/LimitedScope::evaluate($content, $this->params);
		}
	}



	/**
	 * Renders template to string.
	 * @param bool  can throw exceptions? (hidden parameter)
	 * @return string
	 */
	public function __toString()
	{
		ob_start();
		try {
			$this->render();
			return ob_get_clean();

		} catch (/*\*/Exception $e) {
			ob_end_clean();
			if (func_num_args() && func_get_arg(0)) {
				throw $e;
			} else {
				trigger_error($e->getMessage(), E_USER_WARNING);
				return '';
			}
		}
	}



	/**
	 * Converts to SimpleXML. (experimental)
	 * @return SimpleXMLElement
	 */
	public function toXml()
	{
		$dom = new DOMDocument;
		$dom->loadHTML('<html><meta http-equiv="Content-Type" content="text/html;charset=utf-8">' . str_replace("\r", '', $this->__toString()) . '</html>');
		return simplexml_import_dom($dom)->body;
		//return simplexml_load_string('<xml>' . $this->__toString() . '</xml>');
	}



	/********************* template helpers ****************d*g**/



	/**
	 * Registers callback as template run-time helper.
	 * @param  string
	 * @param  callback
	 * @return void
	 */
	public function registerHelper($name, $callback)
	{
		/**/fixCallback($callback);/**/
		if (!is_callable($callback)) {
			$able = is_callable($callback, TRUE, $textual);
			throw new /*\*/InvalidArgumentException("Helper handler '$textual' is not " . ($able ? 'callable.' : 'valid PHP callback.'));
		}
		$this->helpers[strtolower($name)] = $callback;
	}



	/**
	 * Registers callback as template run-time helpers loader.
	 * @param  callback
	 * @return void
	 */
	public function registerHelperLoader($callback)
	{
		/**/fixCallback($callback);/**/
		if (!is_callable($callback)) {
			$able = is_callable($callback, TRUE, $textual);
			throw new /*\*/InvalidArgumentException("Helper loader '$textual' is not " . ($able ? 'callable.' : 'valid PHP callback.'));
		}
		$this->helperLoaders[] = $callback;
	}



	/**
	 * Returns all registered run-time helpers.
	 * @return array
	 */
	final public function getHelpers()
	{
		return $this->helpers;
	}



	/**
	 * Call a template run-time helper. Do not call directly.
	 * @param  string  helper name
	 * @param  array   arguments
	 * @return mixed
	 */
	public function __call($name, $args)
	{
		$lname = strtolower($name);
		if (!isset($this->helpers[$lname])) {
			foreach ($this->helperLoaders as $loader) {
				$helper = call_user_func($loader, $lname);
				if ($helper) {
					$this->registerHelper($lname, $helper);
					return call_user_func_array($helper, $args);
				}
			}
			return parent::__call($name, $args);
		}

		return call_user_func_array($this->helpers[$lname], $args);
	}



	/**
	 * Sets translate adapter.
	 * @param  Nette\ITranslator
	 * @return void
	 */
	public function setTranslator(/*Nette\*/ITranslator $translator = NULL)
	{
		$this->registerHelper('translate', $translator === NULL ? NULL : array($translator, 'translate'));
	}



	/********************* template parameters ****************d*g**/



	/**
	 * Adds new template parameter.
	 * @param  string  name
	 * @param  mixed   value
	 * @return void
	 */
	public function add($name, $value)
	{
		if (array_key_exists($name, $this->params)) {
			throw new /*\*/InvalidStateException("The variable '$name' exists yet.");
		}

		$this->params[$name] = $value;
	}



	/**
	 * @deprecated
	 */
	public function addTemplate($name, $file)
	{
		throw new /*\*/DeprecatedException(__METHOD__ . '() is deprecated.');
	}



	/**
	 * Returns array of all parameters.
	 * @return array
	 */
	public function getParams()
	{
		return $this->params;
	}



	/**
	 * Sets a template parameter. Do not call directly.
	 * @param  string  name
	 * @param  mixed   value
	 * @return void
	 */
	public function __set($name, $value)
	{
		$this->params[$name] = $value;
	}



	/**
	 * Returns a template parameter. Do not call directly.
	 * @param  string  name
	 * @return mixed  value
	 */
	public function &__get($name)
	{
		if ($this->warnOnUndefined && !array_key_exists($name, $this->params)) {
			trigger_error("The variable '$name' does not exist in template '$this->file'", E_USER_NOTICE);
		}

		return $this->params[$name];
	}



	/**
	 * Determines whether parameter is defined. Do not call directly.
	 * @param  string    name
	 * @return bool
	 */
	public function __isset($name)
	{
		return isset($this->params[$name]);
	}



	/**
	 * Removes a template parameter. Do not call directly.
	 * @param  string    name
	 * @return void
	 */
	public function __unset($name)
	{
		unset($this->params[$name]);
	}



	/********************* caching ****************d*g**/



	/**
	 * Set cache storage.
	 * @param  Nette\Caching\Cache
	 * @return void
	 */
	public static function setCacheStorage(/*Nette\Caching\*/ICacheStorage $storage)
	{
		self::$cacheStorage = $storage;
	}



	/**
	 * @return Nette\Caching\ICacheStorage
	 */
	public static function getCacheStorage()
	{
		if (self::$cacheStorage === NULL) {
			self::$cacheStorage = new TemplateCacheStorage(/*Nette\*/Environment::getVariable('cacheBase'));
		}
		return self::$cacheStorage;
	}

}