<?php
/**
 * @package     Joomla.Platform
 * @subpackage  Session
 *
 * @copyright   Copyright (C) 2005 - 2014 Open Source Matters, Inc. All rights reserved.
 * @license     GNU General Public License version 2 or later; see LICENSE
 */

defined('JPATH_PLATFORM') or die;

/**
 * Interface for managing HTTP sessions
 *
 * @since  3.5
 */
class JSessionHandlerNative implements JSessionHandlerInterface
{
	/**
	 * Has the session been started
	 *
	 * @var    boolean
	 * @since  3.5
	 */
	private $started;

	/**
	 * Has the session been closed
	 *
	 * @var    boolean
	 * @since  3.5
	 */
	private $closed;

	/**
	 * Starts the session
	 *
	 * @return  boolean  True if started
	 *
	 * @since   3.5
	 * @throws  RuntimeException If something goes wrong starting the session.
	 */
	public function start()
	{
		if ($this->isStarted())
		{
			return true;
		}

		/**
		 * Write and Close handlers are called after destructing objects since PHP 5.0.5.
		 * Thus destructors can use sessions but session handler can't use objects.
		 * So we are moving session closure before destructing objects.
		 */
		if (version_compare(PHP_VERSION, '5.4', 'ge'))
		{
			session_register_shutdown();
		}
		else
		{
			register_shutdown_function('session_write_close');
		}

		// Disable the cache limiter
		session_cache_limiter('none');

		/*
		 * Extended checks to determine if the session has already been started
		 */

		// If running PHP 5.4, try to use the native API
		if (version_compare(PHP_VERSION, '5.4', 'ge') && PHP_SESSION_ACTIVE === session_status())
		{
			throw new RuntimeException('Failed to start the session: already started by PHP.');
		}

		// Fallback check for PHP 5.3
		if (version_compare(PHP_VERSION, '5.4', 'lt') && !$this->closed && isset($_SESSION) && $this->getId())
		{
			throw new RuntimeException('Failed to start the session: already started by PHP ($_SESSION is set).');
		}

		// If we are using cookies (default true) and headers have already been started (early output),
		if (ini_get('session.use_cookies') && headers_sent($file, $line))
		{
			throw new RuntimeException(sprintf('Failed to start the session because headers have already been sent by "%s" at line %d.', $file, $line));
		}

		// Ok to try and start the session
		if (!session_start())
		{
			throw new RuntimeException('Failed to start the session');
		}

		return true;
	}

	/**
	 * Checks if the session is started.
	 *
	 * @return  boolean  True if started, false otherwise.
	 *
	 * @since   3.5
	 */
	public function isStarted()
	{
		return $this->started;
	}

	/**
	 * Returns the session ID
	 *
	 * @return  string  The session ID
	 *
	 * @since   3.5
	 */
	public function getId()
	{
		return session_id();
	}

	/**
	 * Sets the session ID
	 *
	 * @param   string  $id  The session ID
	 *
	 * @return  void
	 *
	 * @since   3.5
	 * @throws  LogicException
	 */
	public function setId($id)
	{
		if ($this->isStarted())
		{
			throw new LogicException('Cannot change the ID of an active session');
		}

		session_id($id);
	}

	/**
	 * Returns the session name
	 *
	 * @return  mixed  The session name
	 *
	 * @since   3.5
	 */
	public function getName()
	{
		return session_name();
	}

	/**
	 * Sets the session name
	 *
	 * @param   string  $name  The name of the session
	 *
	 * @return  void
	 *
	 * @since   3.5
	 * @throws  LogicException
	 */
	public function setName($name)
	{
		if ($this->isStarted())
		{
			throw new LogicException('Cannot change the name of an active session');
		}

		session_name($name);
	}

	/**
	 * Regenerates ID that represents this storage.
	 *
	 * Note regenerate+destroy should not clear the session data in memory only delete the session data from persistent storage.
	 *
	 * @param   boolean  $destroy   Destroy session when regenerating?
	 * @param   integer  $lifetime  Sets the cookie lifetime for the session cookie. A null value will leave the system settings unchanged,
	 *                              0 sets the cookie to expire with browser session. Time is in seconds, and is not a Unix timestamp.
	 *
	 * @return  boolean  True if session regenerated, false if error
	 *
	 * @since   3.5
	 */
	public function regenerate($destroy = false, $lifetime = null)
	{
		if (null !== $lifetime)
		{
			ini_set('session.cookie_lifetime', $lifetime);
		}

		$return = session_regenerate_id($destroy);

		// Workaround for https://bugs.php.net/bug.php?id=61470 as suggested by David Grudl
		session_write_close();

		if (isset($_SESSION))
		{
			$backup = $_SESSION;
			session_start();
			$_SESSION = $backup;
		}
		else
		{
			session_start();
		}

		return $return;
	}

	/**
	 * Force the session to be saved and closed.
	 *
	 * This method must invoke session_write_close() unless this interface is used for a storage object design for unit or functional testing where
	 * a real PHP session would interfere with testing, in which case it should actually persist the session data if required.
	 *
	 * @return  void
	 *
	 * @see     session_write_close()
	 * @since   3.5
	 * @throws  RuntimeException  If the session is saved without being started, or if the session is already closed.
	 */
	public function save()
	{
		if (!$this->isStarted())
		{
			throw new RuntimeException('The session is not started.');
		}

		session_write_close();

		$this->closed  = true;
		$this->started = false;
	}

	/**
	 * Clear all session data in memory.
	 *
	 * @return  void
	 *
	 * @since   3.5
	 */
	public function clear()
	{
		// Need to destroy any existing sessions started with session.auto_start
		if ($this->getId())
		{
			session_unset();
			session_destroy();
		}

		$this->closed  = true;
		$this->started = false;
	}
}