<?php
/**
 * Fuel
 *
 * Fuel is a fast, lightweight, community driven PHP5 framework.
 *
 * @package		Fuel
 * @version		1.0
 * @author		Fuel Development Team
 * @license		MIT License
 * @copyright	2010 Dan Horrigan
 * @link		http://fuelphp.com
 */

namespace Fuel;

class Config {

	public static $loaded_files = array();

	public static $items = array();

	public static function load($file, $group = null)
	{
		if (array_key_exists($file, static::$loaded_files))
		{
			return false;
		}

		$config = array();
		if ($path = Fuel::find_file('config', $file))
		{
			$config = Fuel::load($path);
		}
		if ($group === null)
		{
			static::$items = static::$items + $config;
		}
		else
		{
			$group = ($group === true) ? $file : $group;
			if ( ! isset(static::$items[$group]))
			{
				static::$items[$group] = array();
			}
			static::$items[$group] = static::$items[$group] + $config;
		}

		static::$loaded_files[$file] = true;
		return $config;
	}

	public static function get($item, $default = null)
	{
		if (isset(static::$items[$item]))
		{
			return static::$items[$item];
		}

		if (strpos($item, '.') !== false)
		{
			$parts = explode('.', $item);

			$return = false;
			foreach ($parts as $part)
			{
				if ($return === false and isset(static::$items[$part]))
				{
					$return = static::$items[$part];
				}
				elseif (isset($return[$part]))
				{
					$return = $return[$part];
				}
				else
				{
					return $default;
				}
			}
			return $return;
		}

		return $default;
	}

	public static function set($item, $value)
	{
		$parts = explode('.', $item);

		$item =& static::$items;
		foreach ($parts as $part)
		{
			// if it's not an array it can't have a subvalue
			if ( ! is_array($item))
			{
				return false;
			}

			// if the part didn't exist yet: add it
			if ( ! isset($item[$part]))
			{
				$item[$part] = array();
			}

			$item =& $item[$part];
		}
		$item = $value;

		return true;
	}
}

/* End of file config.php */