<?php
/**
*
* @package acm
* @version $Id$
* @copyright (c) 2005 phpBB Group
* @license http://opensource.org/licenses/gpl-license.php GNU Public License
*
*/

/**
* @ignore
*/
if (!defined('IN_PHPBB'))
{
	exit;
}

/**
* ACM Memcache Based Caching
* @package acm
*/
class acm
{
	private $vars = array();
	private $is_modified = false;

	public $sql_rowset = array();
	private $sql_row_pointer = array();
	public $cache_dir = '';
	private $memcache;

	/**
	* Set cache path
	*/
	function __construct()
	{
		global $phpbb_root_path;
		$this->memcache = memcache_connect('localhost', 11211);
	}

	/**
	* Load global cache
	*/
	private function load()
	{
		global $phpEx;

		// grab the global cache
		if ($this->vars = memcache_get($this->memcache, 'global'))
		{
			return true;
		}

		return false;
	}

	/**
	* Unload cache object
	*/
	public function unload()
	{
		$this->save();
		unset($this->vars);
		unset($this->sql_rowset);
		unset($this->sql_row_pointer);

		$this->vars = array();
		$this->sql_rowset = array();
		$this->sql_row_pointer = array();
	}

	/**
	* Save modified objects
	*/
	private function save()
	{
		if (!$this->is_modified)
		{
			return;
		}

		memcache_set($this->memcache, 'global', $this->vars, 0, 2592000);

		$this->is_modified = false;
	}

	/**
	* Tidy cache
	*/
	public function tidy()
	{
		global $phpEx;

		// cache has auto GC, no need to have any code here :)

		set_config('cache_last_gc', time(), true);
	}

	/**
	* Get saved cache object
	*/
	public function get($var_name)
	{
		if ($var_name[0] === '_')
		{
			global $phpEx;

			return memcache_get($this->memcache, $var_name);
		}
		else
		{
			if (!sizeof($this->vars))
			{
				$this->load();
			}
			return (isset($this->vars[$var_name])) ? $this->vars[$var_name] : false;
		}
	}

	/**
	* Put data into cache
	*/
	public function put($var_name, $var, $ttl = 31536000)
	{
		if ($var_name[0] === '_')
		{
			memcache_set($this->memcache, $var_name, $var, 0, $ttl);
		}
		else
		{
			$this->vars[$var_name] = $var;
			$this->is_modified = true;
		}
	}

	/**
	* Purge cache data
	*/
	public function purge()
	{
		// Purge all phpbb cache files
		$dir = @opendir($this->cache_dir);

		if (!$dir)
		{
			return;
		}

		while (($entry = readdir($dir)) !== false)
		{
			if (strpos($entry, 'sql_') !== 0 && strpos($entry, 'data_') !== 0 && strpos($entry, 'ctpl_') !== 0 && strpos($entry, 'tpl_') !== 0)
			{
				continue;
			}

			@unlink($this->cache_dir . $entry);
		}
		closedir($dir);

		memcache_flush($this->memcache);

		unset($this->vars);
		unset($this->sql_rowset);
		unset($this->sql_row_pointer);

		$this->vars = array();
		$this->var_expires = array();
		$this->sql_rowset = array();
		$this->sql_row_pointer = array();

		$this->is_modified = false;
	}

	/**
	* Destroy cache data
	*/
	public function destroy($var_name, $table = '')
	{
		global $phpEx;

		if ($var_name === 'sql' && !empty($table))
		{
			if (!is_array($table))
			{
				$table = array($table);
			}

			foreach ($table as $table_name)
			{
				// gives us the md5s that we want
				$temp = memcache_get($this->memcache, 'sql_' . $table_name);
				if ($temp === false)
				{
					continue;
				}

				// delete each query ref
				foreach ($temp as $md5_id => $void)
				{
					memcache_delete($this->memcache, 'sql_' . $md5_id);
				}

				// delete the table ref
				memcache_delete($this->memcache, 'sql_' . $table_name);
			}

			return;
		}

		if ($var_name[0] === '_')
		{
			memcache_delete($this->memcache, $var_name);
		}
		else if (isset($this->vars[$var_name]))
		{
			$this->is_modified = true;
			unset($this->vars[$var_name]);

			// We save here to let the following cache hits succeed
			$this->save();
		}
	}

	/**
	* Load cached sql query
	*/
	public function sql_load($query)
	{
		global $phpEx;

		// Remove extra spaces and tabs
		$query = preg_replace('/[\n\r\s\t]+/', ' ', $query);
		$query_id = sizeof($this->sql_rowset);

		$temp = memcache_get($this->memcache, 'sql_' . md5($query));

		if ($temp === false)
		{
			return false;
		}

		$this->sql_rowset[$query_id] = $temp;

		$this->sql_row_pointer[$query_id] = 0;

		return $query_id;
	}

	/**
	* Save sql query
	*/
	public function sql_save($query, &$query_result, $ttl)
	{
		global $db, $phpEx;

		// Remove extra spaces and tabs
		$query = preg_replace('/[\n\r\s\t]+/', ' ', $query);

		// determine which tables this query belongs to:

		// grab all the FROM tables, avoid getting a LEFT JOIN
		preg_match('/FROM \(?(\w+(?: (?!LEFT JOIN)\w+)?(?:, ?\w+(?: (?!LEFT JOIN)\w+)?)*)\)?/', $query, $regs);
		$tables = array_map('trim', explode(',', $regs[1]));

		// now get the LEFT JOIN
		preg_match_all('/LEFT JOIN\s+(\w+)(?: \w+)?/', $query, $result, PREG_PATTERN_ORDER);
		$tables = array_merge($tables, $result[1]);

		$query_hash = md5($query);

		foreach ($tables as $table_name)
		{
			if (($pos = strpos($table_name, ' ')) !== false)
			{
				$table_name = substr($table_name, 0, $pos);
			}

			$temp = memcache_get($this->memcache, 'sql_' . $table_name);
			if ($temp === false)
			{
				$temp = array();
			}
			$temp[$query_hash] = true;
			memcache_set($this->memcache, 'sql_' . $table_name, $temp, 0, $ttl);
		}

		// store them in the right place
		$query_id = sizeof($this->sql_rowset);
		$this->sql_rowset[$query_id] = array();
		$this->sql_row_pointer[$query_id] = 0;

		while ($row = $db->sql_fetchrow($query_result))
		{
			$this->sql_rowset[$query_id][] = $row;
		}
		$db->sql_freeresult($query_result);

		memcache_set($this->memcache, 'sql_' . $query_hash, $this->sql_rowset[$query_id], 0, $ttl);

		$query_result = $query_id;
	}

	/**
	* Fetch row from cache (database)
	*/
	public function sql_fetchrow($query_id)
	{
		if ($this->sql_row_pointer[$query_id] < sizeof($this->sql_rowset[$query_id]))
		{
			return $this->sql_rowset[$query_id][$this->sql_row_pointer[$query_id]++];
		}

		return false;
	}

	/**
	* Fetch a field from the current row of a cached database result (database)
	*/
	public function sql_fetchfield($query_id, $field)
	{
		if ($this->sql_row_pointer[$query_id] < sizeof($this->sql_rowset[$query_id]))
		{
			return (isset($this->sql_rowset[$query_id][$this->sql_row_pointer[$query_id]][$field])) ? $this->sql_rowset[$query_id][$this->sql_row_pointer[$query_id]][$field] : false;
		}

		return false;
	}

	/**
	* Seek a specific row in an a cached database result (database)
	*/
	public function sql_rowseek($rownum, $query_id)
	{
		if ($rownum >= sizeof($this->sql_rowset[$query_id]))
		{
			return false;
		}

		$this->sql_row_pointer[$query_id] = $rownum;
		return true;
	}

	/**
	* Free memory used for a cached database result (database)
	*/
	public function sql_freeresult($query_id)
	{
		if (!isset($this->sql_rowset[$query_id]))
		{
			return false;
		}

		unset($this->sql_rowset[$query_id]);
		unset($this->sql_row_pointer[$query_id]);

		return true;
	}
}

?>