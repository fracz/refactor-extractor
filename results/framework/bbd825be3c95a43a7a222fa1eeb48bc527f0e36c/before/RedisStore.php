<?php

namespace Illuminate\Cache;

use Illuminate\Contracts\Cache\Store;
use Illuminate\Redis\Database as Redis;

class RedisStore extends TaggableStore implements Store
{
    /**
     * The Redis database connection.
     *
     * @var \Illuminate\Redis\Database
     */
    protected $redis;

    /**
     * A string that should be prepended to keys.
     *
     * @var string
     */
    protected $prefix;

    /**
     * The Redis connection that should be used.
     *
     * @var string
     */
    protected $connection;

    /**
     * Create a new Redis store.
     *
     * @param  \Illuminate\Redis\Database  $redis
     * @param  string  $prefix
     * @param  string  $connection
     * @return void
     */
    public function __construct(Redis $redis, $prefix = '', $connection = 'default')
    {
        $this->redis = $redis;
        $this->setPrefix($prefix);
        $this->connection = $connection;
    }

    /**
     * Retrieve an item from the cache by key.
     *
     * @param  string  $key
     * @return mixed
     */
    public function get($key)
    {
        if (! is_null($value = $this->connection()->get($this->prefix.$key))) {
            return is_numeric($value) ? $value : unserialize($value);
        }
    }

    /**
     * Retrieve multiple items from the cache by key.
     *
     * Items not found in the cache will have a null value for the key.
     *
     * @param  array  $keys
     * @return array
     */
    public function getMultiple(array $keys)
    {
        $returnValues = [];
        $prefixedKeys = [];

        foreach ($keys as $keyToPrefix) {
            $prefixedKeys[] = $this->prefix.$keyToPrefix;
        }

        $cacheValues = $this->connection()->mget($prefixedKeys);

        foreach ($cacheValues as $i => $value) {
            $key = $keys[$i];
            $returnValues[$key] = is_numeric($value) ? $value : unserialize($value);
        }

        return $returnValues;
    }

    /**
     * Store an item in the cache for a given number of minutes.
     *
     * @param  string  $key
     * @param  mixed   $value
     * @param  int     $minutes
     * @return void
     */
    public function put($key, $value, $minutes)
    {
        $value = is_numeric($value) ? $value : serialize($value);

        $minutes = max(1, $minutes);

        $this->connection()->setex($this->prefix.$key, $minutes * 60, $value);
    }

    /**
     * Store multiple items in the cache for a set number of minutes.
     *
     * @param  array  $values
     * @param  int  $minutes
     * @return void
     */
    public function putMultiple(array $values, $minutes)
    {
        $this->connection()->multi();

        foreach ($values as $key => $singleValue) {
            $this->put($key, $singleValue, $minutes);
        }

        $this->connection()->exec();
    }

    /**
     * Increment the value of an item in the cache.
     *
     * @param  string  $key
     * @param  mixed   $value
     * @return int
     */
    public function increment($key, $value = 1)
    {
        return $this->connection()->incrby($this->prefix.$key, $value);
    }

    /**
     * Decrement the value of an item in the cache.
     *
     * @param  string  $key
     * @param  mixed   $value
     * @return int
     */
    public function decrement($key, $value = 1)
    {
        return $this->connection()->decrby($this->prefix.$key, $value);
    }

    /**
     * Store an item in the cache indefinitely.
     *
     * @param  string  $key
     * @param  mixed   $value
     * @return void
     */
    public function forever($key, $value)
    {
        $value = is_numeric($value) ? $value : serialize($value);

        $this->connection()->set($this->prefix.$key, $value);
    }

    /**
     * Remove an item from the cache.
     *
     * @param  string  $key
     * @return bool
     */
    public function forget($key)
    {
        return (bool) $this->connection()->del($this->prefix.$key);
    }

    /**
     * Remove all items from the cache.
     *
     * @return void
     */
    public function flush()
    {
        $this->connection()->flushdb();
    }

    /**
     * Begin executing a new tags operation.
     *
     * @param  array|mixed  $names
     * @return \Illuminate\Cache\RedisTaggedCache
     */
    public function tags($names)
    {
        return new RedisTaggedCache($this, new TagSet($this, is_array($names) ? $names : func_get_args()));
    }

    /**
     * Get the Redis connection instance.
     *
     * @return \Predis\ClientInterface
     */
    public function connection()
    {
        return $this->redis->connection($this->connection);
    }

    /**
     * Set the connection name to be used.
     *
     * @param  string  $connection
     * @return void
     */
    public function setConnection($connection)
    {
        $this->connection = $connection;
    }

    /**
     * Get the Redis database instance.
     *
     * @return \Illuminate\Redis\Database
     */
    public function getRedis()
    {
        return $this->redis;
    }

    /**
     * Get the cache key prefix.
     *
     * @return string
     */
    public function getPrefix()
    {
        return $this->prefix;
    }

    /**
     * Set the cache key prefix.
     *
     * @param  string  $prefix
     * @return void
     */
    public function setPrefix($prefix)
    {
        $this->prefix = ! empty($prefix) ? $prefix.':' : '';
    }
}