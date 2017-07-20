<?php
/**
 * Misc Thoughts about optimization
 *
 * - after a day is archived, we delete all the useless information from the log table, keeping only the useful data for weeks/month
 *   maybe we create a new table containing only these aggregate and we can delete the rows of the day in the log table
 * - To avoid join two huge tables (log_visit, log_link_visit_action) we may have to denormalize (idsite, date)#
 * -
 */
error_reporting(E_ALL|E_NOTICE);
define('PIWIK_INCLUDE_PATH', '.');
define('PIWIK_DATAFILES_INCLUDE_PATH', PIWIK_INCLUDE_PATH . "/modules/DataFiles");

@ignore_user_abort(true);
@set_time_limit(0);

set_include_path(PIWIK_INCLUDE_PATH
					. PATH_SEPARATOR . PIWIK_INCLUDE_PATH . '/libs/'
					. PATH_SEPARATOR . PIWIK_INCLUDE_PATH . '/core/'
					. PATH_SEPARATOR . PIWIK_INCLUDE_PATH . '/modules'
					. PATH_SEPARATOR . PIWIK_INCLUDE_PATH . '/core/models'
					. PATH_SEPARATOR . get_include_path() );

require_once "Event/Dispatcher.php";
require_once "Common.php";
require_once "LogStats.php";
require_once "PluginManager.php";
require_once "LogStats/Plugins.php";

$GLOBALS['DEBUGPIWIK'] = true;

/*
 * Some benchmarks
 *
 * - with the config parsing + db connection
 * Requests per second:    471.91 [#/sec] (mean)
 *
 * - with the main algorithm working + one visitor requesting 5000 times
 * Requests per second:    155.00 [#/sec] (mean)
 *
 */

ob_start();
printDebug($_GET);
$process = new Piwik_LogStats_Controller;
Piwik_PostEvent( 'LogsStats.NewVisitor' );
$process->main();

// yet to do
// known visitor test 1h
// known visitor update 1h
// unit testing the module 7h
ob_end_flush();
printDebug($_COOKIE);
?>