<?php
/* SVN FILE: $Id$ */

/**
 * Session class for Cake.
 *
 * Cake abstracts the handling of sessions. There are several convenient methods to access session information. This class is the implementation of those methods. They are mostly used by the Session Component.
 *
 * PHP versions 4 and 5
 *
 * CakePHP :  Rapid Development Framework <http://www.cakephp.org/>
 * Copyright (c) 2006, Cake Software Foundation, Inc.
 *                     1785 E. Sahara Avenue, Suite 490-204
 *                     Las Vegas, Nevada 89104
 *
 * Licensed under The MIT License
 * Redistributions of files must retain the above copyright notice.
 *
 * @filesource
 * @copyright    Copyright (c) 2006, Cake Software Foundation, Inc.
 * @link         http://www.cakefoundation.org/projects/info/cakephp CakePHP Project
 * @package      cake
 * @subpackage   cake.cake.libs
 * @since        CakePHP v .0.10.0.1222
 * @version      $Revision$
 * @modifiedby   $LastChangedBy$
 * @lastmodified $Date$
 * @license      http://www.opensource.org/licenses/mit-license.php The MIT License
 */

/**
 * Session class for Cake.
 *
 * Cake abstracts the handling of sessions. There are several convenient methods to access session information. This class is the implementation of those methods. They are mostly used by the Session Component.
 *
 * @package    cake
 * @subpackage cake.cake.libs
 * @since      CakePHP v .0.10.0.1222
 */
class CakeSession extends Object
{
/**
 * True if the Session is still valid
 *
 * @var boolean
 */
     var $valid      = false;
/**
 * Error messages for this session
 *
 * @var array
 */
    var $error      = false;
/**
 * User agent string
 *
 * @var string
 */
    var $userAgent  = false;
/**
 * Path to where the session is active.
 *
 * @var string
 */
    var $path        = false;
/**
 * Error number of last occurred error
 *
 * @var integer
 */
    var $lastError  = null;
/**
 * Enter description here...
 *
 * @var unknown_type
 * @todo Is this still in use? OJ 30 jan 2006
 */
    var $sessionId     = null;
/**
 * CAKE_SECURITY setting, "high", "medium", or "low".
 *
 * @var string
 */
    var $security     = null;
/**
 * Start time for this session.
 *
 * @var integer
 */
    var $time        = false;
/**
 * Time when this session becomes invalid.
 *
 * @var integer
 */
    var $sessionTime = false;
/**
 * Constructor.
 *
 * @param string $base The base path for the Session
 */
    function __construct($base = null)
    {
        $this->host = env('HTTP_HOST');

        if (empty($base))
        {
            $this->path = '/';
        }
        else
        {
            $this->path = $base;
        }

        if (strpos($this->host, ':') !== false)
        {
            $this->host = substr($this->host,0, strpos($this->host, ':'));
        }

        if(env('HTTP_USER_AGENT') != null)
        {
            $this->userAgent = md5(env('HTTP_USER_AGENT').CAKE_SESSION_STRING);
        }
        else
        {
            $this->userAgent = "";
        }

        $this->time = time();
        $this->sessionTime = $this->time + (Security::inactiveMins() * CAKE_SESSION_TIMEOUT);
        $this->security = CAKE_SECURITY;
        session_write_close();

        if (!isset($_SESSION))
        {
            $this->_initSession();
        }

        session_cache_limiter("must-revalidate");
        session_start();

        if (!isset($_SESSION))
        {
        	$this->_begin();
        }

        $this->_checkValid();
        parent::__construct();
    }

/**
 * Returns true if given variable is set in session.
 *
 * @param string $name Variable name to check for
 * @return boolean True if variable is there
 */
    function checkSessionVar($name)
    {
        $expression = "return isset(".$this->_sessionVarNames($name).");";
        return eval($expression);
    }

/**
 * Removes a variable from session.
 *
 * @param string $name Session variable to remove
 * @return boolean Success
 */
    function delSessionVar($name)
    {
        if($this->checkSessionVar($name))
        {
            $var = $this->_sessionVarNames($name);
            eval("unset($var);");
            return true;
        }
        $this->_setError(2, "$name doesn't exist");
        return false;
    }

/**
 * Return error description for given error number.
 *
 * @param int $errorNumber
 * @return string Error as string
 */
    function getError($errorNumber)
    {
        if(!is_array($this->error) || !array_key_exists($errorNumber, $this->error))
        {
            return false;
        }
        else
        {
        return $this->error[$errorNumber];
        }
    }

/**
 * Returns last occurred error as a string, if any.
 *
 * @return mixed Error description as a string, or false.
 */
    function getLastError()
    {
        if($this->lastError)
        {
            return $this->getError($this->lastError);
        }
        else
        {
            return false;
        }
    }

/**
 * Returns true if session is valid.
 *
 * @return boolean
 */
    function isValid()
    {
        return $this->valid;
    }

/**
 * Returns given session variable, or all of them, if no parameters given.
 *
 * @param mixed $name
 * @return unknown
 */
    function readSessionVar($name = null)
    {
        if(is_null($name))
        {
            return $this->returnSessionVars();
        }

        if($this->checkSessionVar($name))
        {
            $result = eval("return ".$this->_sessionVarNames($name).";");
            return $result;
        }
        $this->_setError(2, "$name doesn't exist");
        $return = null;
        return $return;
    }

/**
 * Returns all session variables.
 *
 * @return mixed Full $_SESSION array, or false on error.
 */
    function returnSessionVars()
    {
        if(!empty($_SESSION))
        {
            $result = eval("return \$_SESSION;");
            return $result;
        }
        $this->_setError(2, "No Session vars set");
        return false;
    }

/**
 * Writes value to given session variable name.
 *
 * @param mixed $name
 * @param string $value
 */
    function writeSessionVar($name, $value)
    {
        $expression = $this->_sessionVarNames($name);
        $expression .= " = \$value;";
        eval($expression);
    }

/**
 * Begins a session.
 *
 * @access private
 */
    function _begin()
    {
        header('P3P: CP="NOI ADM DEV PSAi COM NAV OUR OTRo STP IND DEM"');
    }

/**
 * Enter description here... To be implemented.
 *
 * @access private
 */
    function _close()
    {
        return true;
    }

/**
 * Enter description here... To be implemented.
 *
 * @access private
 */
    function _destroy($key)
    {
    	$db =& ConnectionManager::getDataSource('default');
    	$db->execute("DELETE FROM ".$db->name('cake_sessions')." WHERE ".$db->name('id')." = ".$db->value($key));
    	return true;
    }

/**
 * Private helper method to destroy invalid sessions.
 *
 * @access private
 */
    function _destroyInvalid()
    {
        $sessionpath = session_save_path();
        if (empty($sessionpath))
        {
            $sessionpath = "/tmp";
        }
        if (isset($_COOKIE[session_name()]))
        {
            setcookie(CAKE_SESSION_COOKIE, '', time()-42000, $this->path);
        }
        $file = $sessionpath.DS."sess_".session_id();
        @session_destroy();
        @unlink($file);
        $this->__construct($this->path);
    }

/**
 * Enter description here... To be implemented.
 *
 * @access private
 */
    function _gc($expires)
    {
		$db =& ConnectionManager::getDataSource('default');
    	$db->execute("DELETE FROM ".$db->name('cake_sessions')." WHERE ".$db->name('expires')." < " . $db->value(time()));
    	return true;
    }

/**
 * Private helper method to initialize a session, based on Cake core settings.
 *
 * @access private
 */
    function _initSession()
    {
        switch ($this->security)
        {
            case 'high':
                $this->cookieLifeTime = 0;
                ini_set('session.referer_check', $this->host);
            break;
            case 'medium':
                $this->cookieLifeTime = 7 * 86400;
            break;
            case 'low':
            default :
                $this->cookieLifeTime = 788940000;
            break;
        }

        switch (CAKE_SESSION_SAVE)
        {
            case 'cake':
                ini_set('session.use_trans_sid', 0);
                ini_set('url_rewriter.tags', '');
                ini_set('session.serialize_handler', 'php');
                ini_set('session.use_cookies', 1);
                ini_set('session.name', CAKE_SESSION_COOKIE);
                ini_set('session.cookie_lifetime', $this->cookieLifeTime);
                ini_set('session.cookie_path', $this->path);
                ini_set('session.gc_probability', 1);
                ini_set('session.auto_start', 0);
                ini_set('session.save_path', TMP.'sessions');
            break;
            case 'database':
                ini_set('session.use_trans_sid', 0);
                ini_set('url_rewriter.tags', '');
                ini_set('session.save_handler', 'user');
                ini_set('session.serialize_handler', 'php');
                ini_set('session.use_cookies', 1);
                ini_set('session.name', CAKE_SESSION_COOKIE);
                ini_set('session.cookie_lifetime', $this->cookieLifeTime);
                ini_set('session.cookie_path', $this->path);
                ini_set('session.gc_probability', 1);
                ini_set('session.auto_start', 0);
                session_set_save_handler(array('CakeSession', '_open'),
                                         array('CakeSession', '_close'),
                                         array('CakeSession', '_read'),
                                         array('CakeSession', '_write'),
                                         array('CakeSession', '_destroy'),
                                         array('CakeSession', '_gc'));
            break;
            case 'php':
                ini_set('session.use_trans_sid', 0);
                ini_set('session.name', CAKE_SESSION_COOKIE);
                ini_set('session.cookie_lifetime', $this->cookieLifeTime);
                ini_set('session.cookie_path', $this->path);
                ini_set('session.gc_probability', 1);
            break;
            default :
                $config = CONFIGS.CAKE_SESSION_SAVE.'.php';
                if(is_file($config))
                {
                    require($config);
                }
                else
                {
                    ini_set('session.use_trans_sid', 0);
                    ini_set('session.name', CAKE_SESSION_COOKIE);
                    ini_set('session.cookie_lifetime', $this->cookieLifeTime);
                    ini_set('session.cookie_path', $this->path);
                    ini_set('session.gc_probability', 1);
                }
            break;
        }
    }

/**
 * Private helper method to create a new session. P3P headers are also sent.
 *
 * @access private
 *
 */
    function _checkValid()
    {
        if($this->readSessionVar("Config"))
        {
            if($this->userAgent == $this->readSessionVar("Config.userAgent")
                && $this->time <= $this->readSessionVar("Config.time"))
            {
                $this->writeSessionVar("Config.time", $this->sessionTime);
                $this->valid = true;
            }
            else
            {
                $this->valid = false;
                $this->_setError(1, "Session Highjacking Attempted !!!");
                $this->_destroyInvalid();
            }
        }
        else
        {
            srand((double)microtime() * 1000000);
            $this->writeSessionVar('Config.rand', rand());
            $this->writeSessionVar("Config.time", $this->sessionTime);
            $this->writeSessionVar("Config.userAgent", $this->userAgent);
            $this->valid = true;
            $this->_setError(1, "Session is valid");
        }
    }

/**
 * Enter description here... To be implemented.
 *
 * @access private
 *
 */
    function _open()
    {
        return true;
    }

/**
 * Enter description here... To be implemented.
 *
 * @access private
 *
 */
    function _read($key)
    {
        $db =& ConnectionManager::getDataSource('default');

        $row = $db->query("SELECT ".$db->name('data')." FROM ".$db->name('cake_sessions')." WHERE ".$db->name('id')." =  ".$db->value($key));

		if ($row && $row[0]['cake_sessions']['data'])
		{
			return $row[0]['cake_sessions']['data'];
		}
		else
		{
			return false;
		}
    }

/**
 * Private helper method to restart a session.
 *
 *
 * @access private
 *
 */
    function _regenerateId()
    {
        $oldSessionId = session_id();
        $sessionpath = session_save_path();
        if (empty($sessionpath))
        {
            $sessionpath = "/tmp";
        }
        if (isset($_COOKIE[session_name()]))
        {
            setcookie(CAKE_SESSION_COOKIE, '', time()-42000, $this->path);
        }
        session_regenerate_id();
        $newSessid = session_id();
        $file = $sessionpath.DS."sess_$oldSessionId";
        @unlink($file);
        $this->_initSession();
        session_id($newSessid);
        session_start();
    }

/**
 * Restarts this session.
 *
 * @access public
 *
 */
    function renew()
    {
        $this->_regenerateId();
    }

/**
 * Private helper method to extract variable names.
 *
 * @param mixed $name Variable names as array or string.
 * @return string
 * @access private
 */
    function _sessionVarNames($name)
    {
        if(is_string($name))
        {
            if(strpos($name, "."))
            {
                $names = explode(".", $name);
            }
            else
            {
                $names = array($name);
            }
            $expression = "\$_SESSION";

            foreach($names as $item)
            {
                $expression .= is_numeric($item) ? "[$item]" : "['$item']";
            }
            return $expression;
        }
        $this->setError(3, "$name is not a string");
        return false;
    }

/**
 * Private helper method to set an internal error message.
 *
 * @param int $errorNumber Number of the error
 * @param string $errorMessage Description of the error
 * @access private
 */
    function _setError($errorNumber, $errorMessage)
    {
        if($this->error === false)
        {
            $this->error = array();
        }

        $this->error[$errorNumber] = $errorMessage;
        $this->lastError = $errorNumber;
    }

/**
 * Enter description here... To be implemented.
 *
 * @access private
 */
    function _write($key, $value)
    {
        $db =& ConnectionManager::getDataSource('default');

        switch (CAKE_SECURITY) {
        	case 'high':
        		$factor = 10;
        		break;
        	case 'medium':
        		$factor = 100;
        		break;
        	case 'low':
        		$factor = 300;
        		break;

        	default:
        		$factor = 10;
        		break;
        }

        $expires = time() + CAKE_SESSION_TIMEOUT * $factor;

		$row = $db->query("SELECT COUNT(*) AS count FROM ".$db->name('cake_sessions')." WHERE ".$db->name('id')." = ".$db->value($key));

		if($row[0][0]['count'] > 0)
		{
			$db->execute("UPDATE ".$db->name('cake_sessions')." SET ".$db->name('data')." = ".$db->value($value).", ".$db->name('expires')." = ".$db->name($expires)." WHERE ".$db->name('id')." = ".$db->value($key));
		}
		else
		{
			$db->execute("INSERT INTO ".$db->name('cake_sessions')." (".$db->name('data').",".$db->name('expires').",".$db->name('id').") VALUES (".$db->value($value).", ".$db->value($expires).", ".$db->value($key).")");
		}

        return true;
    }
}
?>