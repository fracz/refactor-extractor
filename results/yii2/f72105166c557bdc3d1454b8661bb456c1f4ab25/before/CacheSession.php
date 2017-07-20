<?php
/**
 * @link http://www.yiiframework.com/
 * @copyright Copyright (c) 2008 Yii Software LLC
 * @license http://www.yiiframework.com/license/
 */

namespace yii\web;

use Yii;
use yii\caching\Cache;
use yii\base\InvalidConfigException;

/**
 * CacheSession implements a session component using cache as storage medium.
 *
 * The cache being used can be any cache application component.
 * The ID of the cache application component is specified via [[cache]], which defaults to 'cache'.
 *
 * Beware, by definition cache storage are volatile, which means the data stored on them
 * may be swapped out and get lost. Therefore, you must make sure the cache used by this component
 * is NOT volatile. If you want to use database as storage medium, use [[DbSession]] is a better choice.
 *
 * @author Qiang Xue <qiang.xue@gmail.com>
 * @since 2.0
 */
class CacheSession extends Session
{
	/**
	 * @var Cache|string the cache object or the application component ID of the cache object.
	 * The session data will be stored using this cache object.
	 *
	 * After the CacheSession object is created, if you want to change this property,
	 * you should only assign it with a cache object.
	 */
	public $cache = 'cache';

	/**
	 * Initializes the application component.
	 */
	public function init()
	{
		parent::init();
		if (is_string($this->cache)) {
			$this->cache = Yii::$app->getComponent($this->cache);
		}
		if (!$this->cache instanceof Cache) {
			throw new InvalidConfigException('CacheSession::cache must refer to the application component ID of a cache object.');
		}
	}

	/**
	 * Returns a value indicating whether to use custom session storage.
	 * This method overrides the parent implementation and always returns true.
	 * @return boolean whether to use custom storage.
	 */
	public function getUseCustomStorage()
	{
		return true;
	}

	/**
	 * Session read handler.
	 * Do not call this method directly.
	 * @param string $id session ID
	 * @return string the session data
	 */
	public function readSession($id)
	{
		$data = $this->cache->get($this->calculateKey($id));
		return $data === false ? '' : $data;
	}

	/**
	 * Session write handler.
	 * Do not call this method directly.
	 * @param string $id session ID
	 * @param string $data session data
	 * @return boolean whether session write is successful
	 */
	public function writeSession($id, $data)
	{
		return $this->cache->set($this->calculateKey($id), $data, $this->getTimeout());
	}

	/**
	 * Session destroy handler.
	 * Do not call this method directly.
	 * @param string $id session ID
	 * @return boolean whether session is destroyed successfully
	 */
	public function destroySession($id)
	{
		return $this->cache->delete($this->calculateKey($id));
	}

	/**
	 * Generates a unique key used for storing session data in cache.
	 * @param string $id session variable name
	 * @return string a safe cache key associated with the session variable name
	 */
	protected function calculateKey($id)
	{
		return $this->cache->buildKey(array(__CLASS__, $id));
	}
}