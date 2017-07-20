<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html Gpl v3 or later
 * @version $Id$
 *
 * @package Piwik
 */

require_once "Config.php";
require_once "Zend/Db.php";
require_once "Zend/Db/Table.php";
require_once "Log.php";
require_once "PluginsManager.php";

/**
 *
 * @package Piwik
 */
class Piwik
{
	const CLASSES_PREFIX = "Piwik_";

	public static $idPeriods =  array(
			'day'	=> 1,
			'week'	=> 2,
			'month'	=> 3,
			'year'	=> 4,
		);

	/**
	 * ending WITHOUT slashs
	 */
	static public function getPathToPiwikRoot()
	{
		return realpath( dirname(__FILE__). "/../" );
	}

	/**
	 * path without trailing slash
	 */
	static public function createHtAccess( $path )
	{
		@file_put_contents($path . "/.htaccess", "Deny from all");
	}

	// path without trailing slash!!
	static public function mkdir( $path, $mode = 0755, $denyAccess = true )
	{
		$path = PIWIK_INCLUDE_PATH . '/' . $path;

		if(!is_dir($path))
		{
			$directoryParent = realpath(dirname($path));

			// test that the dir we are trying to write has write access
			if( is_writable($directoryParent) )
			{
				mkdir($path, $mode, true);
			}
		}

		if($denyAccess)
		{
			Piwik::createHtAccess($path);
		}
	}

	/**
	 * Throws an exception if the directories are not writable
	 */
	static public function checkDirectoriesWritable()
	{
		$directoriesToCheck = array(
			'/config',
			'/tmp',
			'/tmp/templates_c',
			'/tmp/cache',
		);

		$resultCheck = array();

		foreach($directoriesToCheck as $name)
		{
			$directoryToCheck = PIWIK_INCLUDE_PATH . $name;

			$directory = realpath($directoryToCheck);

			$resultCheck[$directory] = false;

			if(!is_writable($directoryToCheck))
			{
				Piwik::mkdir($directoryToCheck, 0755, false);
			}


			if(is_writable($directoryToCheck))
			{
				$resultCheck[$directory] = true;
			}
		}

		return $resultCheck;
	}

	static public function getJavascriptCode($idSite, $piwikUrl, $actionName = "''")
	{
		$jsTag = file_get_contents( PIWIK_INCLUDE_PATH . "/modules/LogStats/javascriptTag.tpl");
		$jsTag = nl2br(htmlentities($jsTag));
		$jsTag = str_replace('{$actionName}', $actionName, $jsTag);
		$jsTag = str_replace('{$idSite}', $idSite, $jsTag);
		$jsTag = str_replace('{$piwikUrl}', $piwikUrl, $jsTag);
		return $jsTag;
	}

	static public function getMemoryLimitValue()
	{
		if($memory = ini_get('memory_limit'))
		{
			return substr($memory, 0, strlen($memory) - 1);
		}
		return false;
	}

	static public function setMemoryLimit($minimumMemoryLimit)
	{
		$currentValue = self::getMemoryLimitValue();
		if( ($currentValue === false
			|| $currentValue < $minimumMemoryLimit )
			&& @ini_set('memory_limit', $minimumMemoryLimit.'M'))
		{
			return true;
		}
		return false;
	}

	static public function raiseMemoryLimitIfNecessary()
	{
		$minimumMemoryLimit = Zend_Registry::get('config')->General->minimumMemoryLimit;
		if(self::getMemoryLimitValue() < $minimumMemoryLimit)
		{
			return self::setMemoryLimit($minimumMemoryLimit);
		}

		return false;
	}

	static public function log($message = '')
	{
		Zend_Registry::get('logger_message')->log($message);
		Zend_Registry::get('logger_message')->log( "<br>" . PHP_EOL);
	}


	static public function error($message = '')
	{
		trigger_error($message, E_USER_ERROR);
	}

	/**
	 * Display the message in a nice red font with a nice icon
	 * ... and dies
	 */
	static public function exitWithErrorMessage( $message )
	{
		print("<style> a { color:red } </style>
			<div style='color:red;font-family:Georgia;font-size:120%'>
			<p><img src='themes/default/images/error_medium.png' style='vertical-align:middle; float:left;padding:20 20 20 20'>
			$message
			</p></div>"
		);
		exit;
	}

	/**
	 * Computes the division of i1 by i2. If either i1 or i2 are not number, or if i2 has a value of zero
	 * we return 0 to avoid the division by zero.
	 *
	 * @param numeric $i1
	 * @param numeric $i2
	 * @return numeric The result of the division or zero
	 */
	static public function secureDiv( $i1, $i2 )
	{
	    if ( is_numeric($i1) && is_numeric($i2) && floatval($i2) != 0)
		{
			return $i1 / $i2;
		}
		return 0;
	}
	static public function getQueryCount()
	{
		$profiler = Zend_Registry::get('db')->getProfiler();
		return $profiler->getTotalNumQueries();
	}
	static public function getDbElapsedSecs()
	{
		$profiler = Zend_Registry::get('db')->getProfiler();
		return $profiler->getTotalElapsedSecs();
	}
	static public function printQueryCount()
	{
		$totalTime = self::getDbElapsedSecs();
		$queryCount = self::getQueryCount();
		Piwik::log("Total queries = $queryCount (total sql time = ".round($totalTime,2)."s)");
	}

	static public function printLogStatsSQLProfiling()
	{
		function maxSumMsFirst($a,$b)
		{
			return $a['sum_time_ms'] < $b['sum_time_ms'];
		}

		$db = Zend_Registry::get('db');
		$all = $db->fetchAll('	SELECT *, sum_time_ms / count as avg_time_ms
								FROM '.Piwik::prefixTable('log_profiling')
						);
		usort($all, 'maxSumMsFirst');

		$str='<br><br>Query Profiling<br>----------------------<br>';
		foreach($all as $infoQuery)
		{
			$query = $infoQuery['query'];
			$count = $infoQuery['count'];
			$sum_time_ms = $infoQuery['sum_time_ms'];
			$avg_time_ms = round($infoQuery['avg_time_ms'],1);
			$query = str_replace("\t", "", $query);

			$str .= "	$query <br>
						$count times, <b>$sum_time_ms ms total</b><br>
						$avg_time_ms ms average<br>
						<br>";
		}

		Piwik::log($str);
	}

	static function printZendProfiler()
	{
		$profiler = Zend_Registry::get('db')->getProfiler();

		if(!$profiler->getEnabled())
		{
			throw new Exception("To display the profiler you should turn on profiling on your config/config.ini.php file");
		}
		$totalTime    = $profiler->getTotalElapsedSecs();
		$queryCount   = $profiler->getTotalNumQueries();
		$longestTime  = 0;
		$longestQuery = null;

		foreach ($profiler->getQueryProfiles() as $query) {
		    if ($query->getElapsedSecs() > $longestTime) {
		        $longestTime  = $query->getElapsedSecs();
		        $longestQuery = $query->getQuery();
		    }
		}
		$str = '';
		$str .= '<br>Executed ' . $queryCount . ' queries in ' . $totalTime . ' seconds' . "\n";
		$str .= '<br>Average query length: ' . $totalTime / $queryCount . ' seconds' . "\n";
		$str .= '<br>Queries per second: ' . $queryCount / $totalTime . "\n";
		$str .= '<br>Longest query length: ' . $longestTime . "\n";
		$str .= '<br>Longest query: <br>' . $longestQuery . "\n";

		Piwik::log($str);

		foreach($profiler->getQueryProfiles() as $query)
		{
			if(isset($indexByQuery[$query->getQuery()]))
			{
				$existing =  $indexByQuery[$query->getQuery()];
			}
			else
			{
				$existing = array( 'count' => 0, 'sumTimeSeconds' => 0);
			}
			$new = array( 'count' => $existing['count'] + 1,
							'sumTimeSeconds' =>  $existing['count'] + $query->getElapsedSecs()
				);
			$indexByQuery[$query->getQuery()] = $new;
		}
		function sortTimeDesc($a,$b)
		{
			return $a['sumTimeSeconds'] < $b['sumTimeSeconds'];
		}
		uasort( $indexByQuery, 'sortTimeDesc');
		var_dump($indexByQuery);

	}

	static public function printTimer()
	{
		echo Zend_Registry::get('timer');
	}

	static public function printMemoryUsage( $prefixString = null )
	{
		$memory = false;
		if(function_exists('xdebug_memory_usage'))
		{
			$memory = xdebug_memory_usage();
		}
		elseif(function_exists('memory_get_usage'))
		{
			$memory = memory_get_usage();
		}

		if($memory !== false)
		{
			$usage = round( $memory / 1024 / 1024, 2);
			if(!is_null($prefixString))
			{
				Piwik::log($prefixString);
			}
			Piwik::log("Memory usage = $usage Mb");
			Piwik::log();
		}
		else
		{
			Piwik::log("Memory usage function not found.");
		}
	}

	static public function isNumeric($value)
	{
		return !is_array($value) && ereg('^([-]{0,1}[0-9]{1,}[.]{0,1}[0-9]*)$', $value);
	}


	static public function loadPlugins()
	{
		Piwik_PluginsManager::getInstance()->setLanguageToLoad(  Piwik_Translate::getInstance()->getLanguageToLoad() );
		Piwik_PluginsManager::getInstance()->setPluginsToLoad( Zend_Registry::get('config')->Plugins->Plugins->toArray() );
	}

	static public function terminateLoadPlugins()
	{
		Piwik_PluginsManager::getInstance()->installLoadedPlugins();
	}

	static public function getTableCreateSql( $tableName )
	{
		$tables = Piwik::getTablesCreateSql();

		if(!isset($tables[$tableName]))
		{
			throw new Exception("The table '$tableName' SQL creation code couldn't be found.");
		}

		return $tables[$tableName];
	}

	static public function getTablesCreateSql()
	{
		$config = Zend_Registry::get('config');
		$prefixTables = $config->database->tables_prefix;
		$tables = array(
			'user' => "CREATE TABLE {$prefixTables}user (
						  login VARCHAR(20) NOT NULL,
						  password CHAR(32) NOT NULL,
						  alias VARCHAR(45) NOT NULL,
						  email VARCHAR(100) NOT NULL,
						  token_auth CHAR(32) NOT NULL,
						  date_registered TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
						  PRIMARY KEY(login),
						  UNIQUE INDEX uniq_keytoken(token_auth)
						)
			",

			'access' => "CREATE TABLE {$prefixTables}access (
						  login VARCHAR(20) NOT NULL,
						  idsite INTEGER UNSIGNED NOT NULL,
						  access VARCHAR(10) NULL,
						  PRIMARY KEY(login, idsite)
						)
			",

			'site' => "CREATE TABLE {$prefixTables}site (
						  idsite INTEGER(10) UNSIGNED NOT NULL AUTO_INCREMENT,
						  name VARCHAR(90) NOT NULL,
						  main_url VARCHAR(255) NOT NULL,
  						  ts_created TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
						  PRIMARY KEY(idsite)
						)
			",

			'site_url' => "CREATE TABLE {$prefixTables}site_url (
							  idsite INTEGER(10) UNSIGNED NOT NULL,
							  url VARCHAR(255) NOT NULL,
							  PRIMARY KEY(idsite, url)
						)
			",


			'logger_message' => "CREATE TABLE {$prefixTables}logger_message (
									  idlogger_message INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
									  timestamp TIMESTAMP NULL,
									  message TINYTEXT NULL,
									  PRIMARY KEY(idlogger_message)
									)
			",

			'logger_api_call' => "CREATE TABLE {$prefixTables}logger_api_call (
									  idlogger_api_call INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
									  class_name VARCHAR(255) NULL,
									  method_name VARCHAR(255) NULL,
									  parameter_names_default_values TINYTEXT NULL,
									  parameter_values TINYTEXT NULL,
									  execution_time FLOAT NULL,
									  caller_ip BIGINT NULL,
									  timestamp TIMESTAMP NULL,
									  returned_value TINYTEXT NULL,
									  PRIMARY KEY(idlogger_api_call)
									)
			",

			'logger_error' => "CREATE TABLE {$prefixTables}logger_error (
									  idlogger_error INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
									  timestamp TIMESTAMP NULL,
									  message TINYTEXT NULL,
									  errno INTEGER UNSIGNED NULL,
									  errline INTEGER UNSIGNED NULL,
									  errfile VARCHAR(255) NULL,
									  backtrace TEXT NULL,
									  PRIMARY KEY(idlogger_error)
									)
			",

			'logger_exception' => "CREATE TABLE {$prefixTables}logger_exception (
									  idlogger_exception INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
									  timestamp TIMESTAMP NULL,
									  message TINYTEXT NULL,
									  errno INTEGER UNSIGNED NULL,
									  errline INTEGER UNSIGNED NULL,
									  errfile VARCHAR(255) NULL,
									  backtrace TEXT NULL,
									  PRIMARY KEY(idlogger_exception)
									)
			",


			'log_action' => "CREATE TABLE {$prefixTables}log_action (
									  idaction INTEGER(10) UNSIGNED NOT NULL AUTO_INCREMENT,
									  name VARCHAR(255) NOT NULL,
  									  type TINYINT UNSIGNED NULL,
									  PRIMARY KEY(idaction)
						)
			",

			'log_visit' => "CREATE TABLE {$prefixTables}log_visit (
  idvisit INTEGER(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  idsite INTEGER(10) UNSIGNED NOT NULL,
  visitor_localtime TIME NOT NULL,
  visitor_idcookie CHAR(32) NOT NULL,
  visitor_returning TINYINT(1) NOT NULL,
  visit_first_action_time DATETIME NOT NULL,
  visit_last_action_time DATETIME NOT NULL,
  visit_server_date DATE NOT NULL,
  visit_exit_idaction INTEGER(11) NOT NULL,
  visit_entry_idaction INTEGER(11) NOT NULL,
  visit_total_actions SMALLINT(5) UNSIGNED NOT NULL,
  visit_total_time SMALLINT(5) UNSIGNED NOT NULL,
  referer_type INTEGER UNSIGNED NULL,
  referer_name VARCHAR(70) NULL,
  referer_url TEXT NOT NULL,
  referer_keyword VARCHAR(255) NULL,
  config_md5config CHAR(32) NOT NULL,
  config_os CHAR(3) NOT NULL,
  config_browser_name VARCHAR(10) NOT NULL,
  config_browser_version VARCHAR(20) NOT NULL,
  config_resolution VARCHAR(9) NOT NULL,
  config_color_depth TINYINT(2) UNSIGNED NOT NULL,
  config_pdf TINYINT(1) NOT NULL,
  config_flash TINYINT(1) NOT NULL,
  config_java TINYINT(1) NOT NULL,
  config_director TINYINT(1) NOT NULL,
  config_quicktime TINYINT(1) NOT NULL,
  config_realplayer TINYINT(1) NOT NULL,
  config_windowsmedia TINYINT(1) NOT NULL,
  config_cookie TINYINT(1) NOT NULL,
  location_ip BIGINT(11) NOT NULL,
  location_browser_lang VARCHAR(20) NOT NULL,
  location_country CHAR(3) NOT NULL,
  location_continent CHAR(3) NOT NULL,
  PRIMARY KEY(idvisit)
)
			",

			'log_link_visit_action' => "CREATE TABLE {$prefixTables}log_link_visit_action (
											  idlink_va INTEGER(11) NOT NULL AUTO_INCREMENT,
											  idvisit INTEGER(10) UNSIGNED NOT NULL,
											  idaction INTEGER(10) UNSIGNED NOT NULL,
											  idaction_ref INTEGER(11) UNSIGNED NOT NULL,
											  time_spent_ref_action INTEGER(10) UNSIGNED NOT NULL,
											  PRIMARY KEY(idlink_va)
											)
			",

			'log_profiling' => "CREATE TABLE {$prefixTables}log_profiling (
								  query TEXT NOT NULL,
								  count INTEGER UNSIGNED NULL,
								  sum_time_ms FLOAT NULL,
								  UNIQUE INDEX query(query(100))
								)
			",

			'archive_numeric'	=> "CREATE TABLE {$prefixTables}archive_numeric (
										idarchive INTEGER UNSIGNED NOT NULL,
										name VARCHAR(255) NOT NULL,
									  idsite INTEGER UNSIGNED NULL,
									  date1 DATE NULL,
								  	  date2 DATE NULL,
									  period TINYINT UNSIGNED NULL,
								  	  ts_archived DATETIME NULL,
								  	  value FLOAT NULL,
									  PRIMARY KEY(idarchive, name)
									)
			",
			'archive_blob'	=> "CREATE TABLE {$prefixTables}archive_blob (
  idarchive INTEGER UNSIGNED NOT NULL,
  name VARCHAR(255) NOT NULL,
  idsite INTEGER UNSIGNED NULL,
  date1 DATE NULL,
  date2 DATE NULL,
  period TINYINT UNSIGNED NULL,
  ts_archived DATETIME NULL,
  value BLOB NULL,
  PRIMARY KEY(idarchive, name)
)
			",
		);
		return $tables;
	}

	static public function getCurrentUserLogin()
	{
		return Zend_Registry::get('access')->getIdentity();
	}

	static public function isUserIsSuperUserOrTheUser( $theUser )
	{
		try{
			self::checkUserIsSuperUserOrTheUser( $theUser );
			return true;
		} catch( Exception $e){
			return false;
		}
	}

	// Accessible either to the user itself
	static public function checkUserIsSuperUserOrTheUser( $theUser )
	{
		try{
			if( Piwik::getCurrentUserLogin() !== $theUser)
			{
				// or to the super user
				Piwik::checkUserIsSuperUser();
			}
		} catch( Piwik_Access_NoAccessException $e){
			throw new Piwik_Access_NoAccessException("The user has to be either the Super User or the user '$theUser' itself.");
		}
	}

	static public function isUserIsSuperUser()
	{
		try{
			self::checkUserIsSuperUser();
			return true;
		} catch( Exception $e){
			return false;
		}
	}

	static public function checkUserIsSuperUser()
	{
		Zend_Registry::get('access')->checkUserIsSuperUser();
	}

	static public function isUserHasAdminAccess( $idSites )
	{
		try{
			self::checkUserHasAdminAccess( $idSites );
			return true;
		} catch( Exception $e){
			return false;
		}
	}

	static public function checkUserHasAdminAccess( $idSites )
	{
		Zend_Registry::get('access')->checkUserHasAdminAccess( $idSites );
	}

	static public function isUserHasSomeAdminAccess()
	{
		try{
			self::checkUserHasSomeAdminAccess();
			return true;
		} catch( Exception $e){
			return false;
		}
	}

	static public function checkUserHasSomeAdminAccess()
	{
		Zend_Registry::get('access')->checkUserHasSomeAdminAccess();
	}

	static public function isUserHasViewAccess( $idSites )
	{
		try{
			self::checkUserHasViewAccess( $idSites );
			return true;
		} catch( Exception $e){
			return false;
		}
	}

	static public function checkUserHasViewAccess( $idSites )
	{
		Zend_Registry::get('access')->checkUserHasViewAccess( $idSites );
	}

	static public function prefixClass( $class )
	{
		if(substr_count($class, Piwik::CLASSES_PREFIX) > 0)
		{
			return $class;
		}
		return Piwik::CLASSES_PREFIX.$class;
	}
	static public function unprefixClass( $class )
	{
		$lenPrefix = strlen(Piwik::CLASSES_PREFIX);
		if(substr($class, 0, $lenPrefix) == Piwik::CLASSES_PREFIX)
		{
			return substr($class, $lenPrefix);
		}
		return $class;
	}

	/**
	 * returns false if the URL to redirect to is already this URL
	 */
	static public function redirectToModule( $newModule, $newAction = '' )
	{
		$currentModule = Piwik_Common::getRequestVar('module', '', 'string');
		$currentAction = Piwik_Common::getRequestVar('action', '', 'string');

		if($currentModule != $newModule
			||  $currentAction != $newAction )
		{

			$newUrl = Piwik_URL::getCurrentUrlWithoutQueryString()
				. Piwik_Url::getCurrentQueryStringWithParametersModified(
						array('module' => $newModule, 'action' => $newAction)
				);

			Piwik_Url::redirectToUrl($newUrl);
		}
		return false;
	}

	static public function prefixTable( $table )
	{
		$config = Zend_Registry::get('config');
		$prefixTables = $config->database->tables_prefix;
		return $prefixTables . $table;
	}

	/**
	 * Names of all the prefixed tables in piwik
	 * Doesn't use the DB
	 */
	static public function getTablesNames()
	{
		$aTables = array_keys(self::getTablesCreateSql());
		$config = Zend_Registry::get('config');
		$prefixTables = $config->database->tables_prefix;
		$return = array();
		foreach($aTables as $table)
		{
			$return[] = $prefixTables.$table;
		}
		return $return;
	}

	static $tablesInstalled = null;

	static public function getTablesInstalled( $forceReload = true )
	{
		if(is_null(self::$tablesInstalled)
			|| $forceReload === true)
		{

			$db = Zend_Registry::get('db');
			$config = Zend_Registry::get('config');
			$prefixTables = $config->database->tables_prefix;

			$allTables = $db->fetchCol("SHOW TABLES");

			// all the tables to be installed
			$allMyTables = self::getTablesNames();

			// we get the intersection between all the tables in the DB and the tables to be installed
			$tablesInstalled = array_intersect($allMyTables, $allTables);

			// at this point we have only the piwik tables which is good
			// but we still miss the piwik generated tables (using the class Piwik_TablePartitioning)

			$allArchiveNumeric = $db->fetchCol("SHOW TABLES LIKE '".$prefixTables."archive_numeric%'");
			$allArchiveBlob = $db->fetchCol("SHOW TABLES LIKE '".$prefixTables."archive_blob%'");

			$allTablesReallyInstalled = array_merge($tablesInstalled, $allArchiveNumeric, $allArchiveBlob);

			self::$tablesInstalled = $allTablesReallyInstalled;
		}
		return 	self::$tablesInstalled;
	}

	static public function createDatabase()
	{
		$db = Zend_Registry::get('db');
		$dbName = Zend_Registry::get('config')->database->dbname;
		$db->query("CREATE DATABASE IF NOT EXISTS ".$dbName);
	}

	static public function dropDatabase()
	{
		$db = Zend_Registry::get('db');
		$dbName = Zend_Registry::get('config')->database->dbname;
		$db->query("DROP DATABASE IF EXISTS ".$dbName);
	}


	static public function createDatabaseObject( $dbInfos = null )
	{
		$config = Zend_Registry::get('config');

		if(is_null($dbInfos))
		{
			$dbInfos = $config->database->toArray();
		}

//		var_dump($dbInfos);
		// test with the password ='][{}!3456&&^#gegq"eQ for example
		if(substr($dbInfos['password'],0,1) == '"'
			&& substr($dbInfos['password'],-1,1) == '"'
			&& strlen($dbInfos['password']) >= 2 )
		{
			$dbInfos['password'] = substr($dbInfos['password'], 1, -1);
		}
		$dbInfos['password'] = htmlspecialchars_decode($dbInfos['password']);
//		var_dump($dbInfos);exit;// we remove the slashes

		$db = Zend_Db::factory($config->database->adapter, $dbInfos);
		$db->getConnection();
		// see http://framework.zend.com/issues/browse/ZF-1398
		$db->getConnection()->setAttribute(PDO::ATTR_EMULATE_PREPARES, true);
		Zend_Db_Table::setDefaultAdapter($db);
		Zend_Registry::set('db', $db);

	}

	static public function createLogObject()
	{
		require_once "Log/APICall.php";
		require_once "Log/Exception.php";
		require_once "Log/Error.php";
		require_once "Log/Message.php";

		$configAPI = Zend_Registry::get('config')->log;

		$aLoggers = array(
				'logger_api_call' => new Piwik_Log_APICall,
				'logger_exception' => new Piwik_Log_Exception,
				'logger_error' => new Piwik_Log_Error,
				'logger_message' => new Piwik_Log_Message,
			);

		foreach($configAPI as $loggerType => $aRecordTo)
		{
			if(isset($aLoggers[$loggerType]))
			{
				$logger = $aLoggers[$loggerType];

				foreach($aRecordTo as $recordTo)
				{
					switch($recordTo)
					{
						case 'screen':
							$logger->addWriteToScreen();
						break;

						case 'database':
							$logger->addWriteToDatabase();
						break;

						case 'file':
							$logger->addWriteToFile();
						break;

						default:
							throw new Exception("TODO");
						break;
					}
				}
			}
		}

		foreach($aLoggers as $loggerType =>$logger)
		{
			if($logger->getWritersCount() == 0)
			{
				$logger->addWriteToNull();
			}
			Zend_Registry::set($loggerType, $logger);
		}
	}


	static public function createConfigObject( $pathConfigFile = null )
	{
		$config = new Piwik_Config($pathConfigFile);
	}

	static public function dropTables( $doNotDelete = array() )
	{
		$tablesAlreadyInstalled = self::getTablesInstalled();
		$db = Zend_Registry::get('db');

		$doNotDeletePattern = "(".implode("|",$doNotDelete).")";

		foreach($tablesAlreadyInstalled as $tableName)
		{

			if( count($doNotDelete) == 0
				|| (!in_array($tableName,$doNotDelete)
					&& !ereg($doNotDeletePattern,$tableName)
					)
				)
			{
				$db->query("DROP TABLE $tableName");
			}
		}
	}

	/**
	 * Returns true if the email is a valid email
	 *
	 * @param string email
	 * @return bool
	 */
    static public function isValidEmailString( $email )
    {
		return (preg_match('/^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9_.-]+\.[a-zA-Z]{2,4}$/', $email) > 0);
    }

    /**
     * Creates an entry in the User table for the "anonymous" user.
     *
     * @return void
     */
    static public function createAnonymousUser()
    {
    	// The anonymous user is the user that is assigned by default
    	// note that the token_auth value is anonymous, which is assigned by default as well in the Login plugin
		$db = Zend_Registry::get('db');
		$db->query("INSERT INTO ". Piwik::prefixTable("user") . "
					VALUES ( 'anonymous', '', 'anonymous', 'anonymous@example.org', 'anonymous', CURRENT_TIMESTAMP );" );
    }

	static public function createTables()
	{
		$db = Zend_Registry::get('db');

		$config = Zend_Registry::get('config');
		$prefixTables = $config->database->tables_prefix;

		//Piwik::log("Creating ". implode(", ", self::getTablesNames()));

		$tablesToCreate = self::getTablesCreateSql();

		unset($tablesToCreate['archive_blob']);
		unset($tablesToCreate['archive_numeric']);

		$tablesAlreadyInstalled = self::getTablesInstalled();

		foreach($tablesToCreate as $tableName => $tableSql)
		{
			$tableName = $prefixTables . $tableName;

			// if the table doesn't exist already
			if(!in_array($tableName, $tablesAlreadyInstalled))
			{
				$db->query( $tableSql );
			}
		}
	}

	static public function install()
	{
		// create directories
		Piwik::mkdir(Zend_Registry::get('config')->smarty->compile_dir);
		Piwik::mkdir(Zend_Registry::get('config')->smarty->cache_dir);
	}

	static public function uninstall()
	{
		// delete tables
		$db = Zend_Registry::get('db');
		$db->query( "DROP TABLE IF EXISTS ". implode(", ", self::getTablesNames()) );
	}
}
