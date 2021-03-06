<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * Database with allowed values for configuration stored in the $cfg array,
 * used by setup script and user preferences to generate forms.
 *
 * @package phpMyAdmin
 */

/**
 * Value meaning:
 * o array - select field, array contains allowed values
 * o string - type override
 *
 * Use normal array, paths won't be expanded
 */
$cfg_db = array();

$cfg_db['Servers'] = array(1 => array(
    'port'         => 'integer',
    'connect_type' => array('tcp', 'socket'),
    'extension'    => array('mysql', 'mysqli'),
    'auth_type'    => array('config', 'http', 'signon', 'cookie'),
    'AllowDeny'    => array(
        'order'    => array('', 'deny,allow', 'allow,deny', 'explicit')),
    'only_db'      => 'array'));
$cfg_db['RecodingEngine'] = array('auto', 'iconv', 'recode');
$cfg_db['DefaultCharset'] = $GLOBALS['cfg']['AvailableCharsets'];
$cfg_db['OBGzip'] = array('auto', true, false);
$cfg_db['ShowTooltipAliasTB'] = array('nested', true, false);
$cfg_db['DisplayDatabasesList'] = array('auto', true, false);
$cfg_db['LeftLogoLinkWindow'] = array('main', 'new');
$cfg_db['LeftDefaultTabTable'] = array(
    'tbl_structure.php', // fields list
    'tbl_sql.php',       // SQL form
    'tbl_select.php',    // search page
    'tbl_change.php',    // insert row page
    'sql.php');          // browse page
$cfg_db['NavigationBarIconic'] = array(true, false, 'both');
$cfg_db['Order'] = array('ASC', 'DESC', 'SMART');
$cfg_db['ProtectBinary'] = array(false, 'blob', 'all');
$cfg_db['CharEditing'] = array('input', 'textarea');
$cfg_db['PropertiesIconic'] = array(true, false, 'both');
$cfg_db['DefaultTabServer'] = array(
    'main.php',                // the welcome page (recommended for multiuser setups)
    'server_databases.php',    // list of databases
    'server_status.php',       // runtime information
    'server_variables.php',    // MySQL server variables
    'server_privileges.php',   // user management
    'server_processlist.php'); // process list
$cfg_db['DefaultTabDatabase'] = array(
    'db_structure.php',   // tables list
    'db_sql.php',         // SQL form
    'db_search.php',      // search query
    'db_operations.php'); // operations on database
$cfg_db['DefaultTabTable'] = array(
    'tbl_structure.php', // fields list
    'tbl_sql.php',       // SQL form
    'tbl_select.php',    // search page
    'tbl_change.php',    // insert row page
    'sql.php');          // browse page
$cfg_db['QueryWindowDefTab'] = array(
	'sql',     // SQL
	'files',   // Import files
	'history', // SQL history
	'full');   // All (SQL and SQL history)
$cfg_db['Import']['format'] = array(
    'csv',    // CSV
    'docsql', // DocSQL
    'ldi',    // CSV using LOAD DATA
    'sql');   // SQL
$cfg_db['Import']['sql_compatibility'] = array(
    'NONE', 'ANSI', 'DB2', 'MAXDB', 'MYSQL323', 'MYSQL40', 'MSSQL', 'ORACLE',
    // removed; in MySQL 5.0.33, this produces exports that
    // can't be read by POSTGRESQL (see our bug #1596328)
    //'POSTGRESQL',
    'TRADITIONAL');
$cfg_db['Import']['ldi_local_option'] = array('auto', true, false);
$cfg_db['Export']['format'] = array('codegen', 'csv', 'excel', 'htmlexcel',
    'htmlword', 'latex', 'ods', 'odt', 'pdf', 'sql', 'texytext', 'xls', 'xml',
    'yaml');
$cfg_db['Export']['compression'] = array('none', 'zip', 'gzip', 'bzip2');
$cfg_db['Export']['charset'] = array_merge(array(''), $GLOBALS['cfg']['AvailableCharsets']);

?>