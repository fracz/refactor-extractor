<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html Gpl v3 or later
 * @version $Id: FrontController.php 583 2008-07-28 00:37:19Z matt $
 *
 * @package Piwik
 */


/**
 * Zend classes
 */
require_once "Zend/Exception.php";
require_once "Zend/Loader.php";
require_once "Zend/Auth.php";
require_once "Zend/Auth/Adapter/DbTable.php";

/**
 * Piwik classes
 */
require_once "Timer.php";
require_once "core/Piwik.php";
require_once "Access.php";
require_once "Auth.php";
require_once "API/Proxy.php";
require_once "Archive.php";
require_once "Site.php";
require_once "Date.php";
require_once "DataTable.php";
require_once "Translate.php";
require_once "Mail.php";
require_once "Url.php";
require_once "Controller.php";
require_once "Option.php";
require_once "View.php";

require_once "PluginsFunctions/Menu.php";
require_once "PluginsFunctions/AdminMenu.php";
require_once "PluginsFunctions/WidgetsList.php";
require_once "PluginsFunctions/Sql.php";

/**
 * Front controller.
 * This is the class hit in the first place.
 * It dispatches the request to the right controller.
 *
 * For a detailed explanation, see the documentation on http://dev.piwik.org/trac/wiki/MainSequenceDiagram
 *
 * @package Piwik
 */
class Piwik_FrontController
{
	/**
	 * Set to false and the Front Controller will not dispatch the request
	 *
	 * @var bool
	 */
	static public $enableDispatch = true;

	static private $instance = null;

	/**
	 * returns singleton
	 *
	 * @return Piwik_FrontController
	 */
	static public function getInstance()
	{
		if (self::$instance == null)
		{
			$c = __CLASS__;
			self::$instance = new $c();
		}
		return self::$instance;
	}

	/**
	 * Dispatches the request to the right plugin and executes the requested action on the plugin controller.
	 *
	 * @throws Exception in case the plugin doesn't exist, the action doesn't exist, there is not enough permission, etc.
	 *
	 * @param string $module
	 * @param string $action
	 * @param array $parameters
	 * @return mixed The returned value of the calls, often nothing as the module print but don't return data
	 * @see fetchDispatch()
	 */
	function dispatch( $module = null, $action = null, $parameters = null)
	{
		if( self::$enableDispatch === false)
		{
			return;
		}

		if(is_null($module))
		{
			$defaultModule = 'CoreHome';
			$module = Piwik_Common::getRequestVar('module', $defaultModule, 'string');
		}

		if(is_null($action))
		{
			$action = Piwik_Common::getRequestVar('action', false);
		}

		if(is_null($parameters))
		{
			$parameters = array();
		}

		if(!ctype_alnum($module))
		{
			throw new Exception("Invalid module name '$module'");
		}

		if( ! Piwik_PluginsManager::getInstance()->isPluginActivated( $module ))
		{
			throw new Exception_PluginDeactivated($module);
		}

		$controllerClassName = "Piwik_".$module."_Controller";
		if(!class_exists($controllerClassName))
		{
			$moduleController = "plugins/" . $module . "/Controller.php";
			if( !Zend_Loader::isReadable($moduleController))
			{
				throw new Exception("Module controller $moduleController not found!");
			}
			require_once $moduleController;
		}

		$controller = new $controllerClassName;
		if($action === false)
		{
			$action = $controller->getDefaultAction();
		}

		if( !is_callable(array($controller, $action)))
		{
			throw new Exception("Action $action not found in the controller $controllerClassName.");
		}
		try {
			return call_user_func_array( array($controller, $action ), $parameters);
		} catch(Piwik_Access_NoAccessException $e) {
			Piwik_PostEvent('FrontController.NoAccessException', $e);
		}
	}

	/**
	 * Often plugins controller display stuff using echo/print.
	 * Using this function instead of dispath() returns the output string form the actions calls.
	 *
	 * @param string $controllerName
	 * @param string $actionName
	 * @param array $parameters
	 * @return string
	 */
	function fetchDispatch( $controllerName = null, $actionName = null, $parameters = null)
	{
		ob_start();
		$output = $this->dispatch( $controllerName, $actionName, $parameters);
		// if nothing returned we try to load something that was printed on the screen
		if(empty($output))
		{
			$output = ob_get_contents();
		}
	    ob_end_clean();
	    return $output;
	}

	/**
	 * Called at the end of the page generation
	 *
	 */
	function __destruct()
	{
		try {
			Piwik::printSqlProfilingReportZend();
			Piwik::printQueryCount();
		} catch(Exception $e) {}

		if(Piwik::getModule() !== 'API')
		{
//			Piwik::printMemoryUsage();
//			Piwik::printTimer();
		}
	}

	/**
	 * Must be called before dispatch()
	 * - checks that directories are writable,
	 * - loads the configuration file,
	 * - loads the plugin,
	 * - inits the DB connection,
	 * - etc.
	 *
	 * @return void
	 */
	function init()
	{
		try {
			Zend_Registry::set('timer', new Piwik_Timer);

			$directoriesToCheck = array(
					'/tmp',
					'/tmp/templates_c',
					'/tmp/cache',
			);

			Piwik::checkDirectoriesWritableOrDie($directoriesToCheck);
			self::assignCliParametersToRequest();

			$exceptionToThrow = false;

			try {
				Piwik::createConfigObject();
			} catch(Exception $e) {
				Piwik_PostEvent('FrontController.NoConfigurationFile', $e);
				$exceptionToThrow = $e;
			}
			Piwik_Translate::getInstance()->loadEnglishTranslation();

			$pluginsManager = Piwik_PluginsManager::getInstance();
			$pluginsManager->setPluginsToLoad( Zend_Registry::get('config')->Plugins->Plugins->toArray() );

			if($exceptionToThrow)
			{
				throw $exceptionToThrow;
			}
			Piwik::createDatabaseObject();
			Piwik::createLogObject();

			Piwik_Translate::getInstance()->loadUserTranslation();
			$pluginsManager->setLanguageToLoad( Piwik_Translate::getInstance()->getLanguageToLoad() );
			$pluginsManager->postLoadPlugins();

			require_once "CoreUpdater/Controller.php";
			$updaterController = new Piwik_CoreUpdater_Controller();
			$updaterController->checkForCoreAndPluginsUpdates();

			Piwik_PluginsManager::getInstance()->installLoadedPlugins();
			Piwik::install();

			Piwik_PostEvent('FrontController.initAuthenticationObject');
			try {
				$authAdapter = Zend_Registry::get('auth');
			} catch(Exception $e){
				throw new Exception("Authentication object 'auth' cannot be found in the Registry. Maybe the Login plugin is not activated?
									<br>You can activate the plugin by adding:<br>
									<code>Plugins[] = Login</code><br>
									under the <code>[Plugins]</code> section in your config/config.inc.php");
			}

			$access = new Piwik_Access($authAdapter);
			Zend_Registry::set('access', $access);
			Zend_Registry::get('access')->loadAccess();

			Piwik::raiseMemoryLimitIfNecessary();
		} catch(Exception $e) {
			Piwik_ExitWithMessage($e->getMessage(), $e->getTraceAsString(), true);
		}
	}

	/**
	 * Assign CLI parameters as if they were REQUEST or GET parameters.
	 * You can trigger Piwik from the command line by
	 * # /usr/bin/php5 /path/to/piwik/index.php -- "module=API&method=Actions.getActions&idSite=1&period=day&date=previous8&format=php"
	 *
	 * @return void
	 */
	static protected function assignCliParametersToRequest()
	{
		if(isset($_SERVER['argc'])
			&& $_SERVER['argc'] > 0)
		{
			for ($i=1; $i < $_SERVER['argc']; $i++)
			{
				parse_str($_SERVER['argv'][$i],$tmp);
				$_REQUEST = array_merge($_REQUEST, $tmp);
				$_GET = array_merge($_GET, $tmp);
			}
		}
	}
}

/**
 * Exception thrown when the requested plugin is not activated in the config file
 *
 * @package Piwik
 */
class Exception_PluginDeactivated extends Exception
{
	function __construct($module)
	{
		parent::__construct("The plugin '$module' is not activated. You can activate the plugin on the 'Plugins admin' page.");
	}
}