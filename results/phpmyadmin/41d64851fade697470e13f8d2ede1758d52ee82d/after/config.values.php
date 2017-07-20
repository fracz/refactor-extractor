<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * Database with allowed values for configuration stored in the $cfg array,
 * used by setup script and user preferences to generate forms.
 *
 * @package PhpMyAdmin
 */

if (!defined('PHPMYADMIN')) {
    exit;
}

/**
 * Value meaning:
 * o array - select field, array contains allowed values
 * o string - type override
 *
 * Use normal array, paths won't be expanded
 */
$cfg_db = array();

$cfg_db['Servers'] = array(
    1 => array(
        'port'         => 'integer',
        'connect_type' => array('tcp', 'socket'),
        'extension'    => array('mysql', 'mysqli'),
        'auth_type'    => array('config', 'http', 'signon', 'cookie'),
        'AllowDeny'    => array(
            'order' => array('', 'deny,allow', 'allow,deny', 'explicit')
        ),
        'only_db'      => 'array'
    )
);
$cfg_db['RecodingEngine'] = array('auto', 'iconv', 'recode', 'none');
$cfg_db['OBGzip'] = array('auto', true, false);
$cfg_db['MemoryLimit'] = 'short_string';
$cfg_db['NavigationLogoLinkWindow'] = array('main', 'new');
$cfg_db['NavigationTreeDefaultTabTable'] = array(
    'tbl_structure.php', // fields list
    'tbl_sql.php',       // SQL form
    'tbl_select.php',    // search page
    'tbl_change.php',    // insert row page
    'sql.php'            // browse page
);
$cfg_db['NavigationTreeDbSeparator'] = 'short_string';
$cfg_db['NavigationTreeTableSeparator'] = 'short_string';
$cfg_db['NavigationBarIconic'] = array(
    true   => __('Icons'),
    false  => __('Text'),
    'both' => __('Both')
);
$cfg_db['MaxRows'] = array(25,50,100,250,500);
$cfg_db['Order'] = array('ASC', 'DESC', 'SMART');
$cfg_db['RowActionLinks'] = array(
    'none'  => __('Nowhere'),
    'left'  => __('Left'),
    'right' => __('Right'),
    'both'  => __('Both')
);
$cfg_db['ProtectBinary'] = array(false, 'blob', 'noblob', 'all');
$cfg_db['DefaultDisplay'] = array('horizontal', 'vertical', 'horizontalflipped');
$cfg_db['CharEditing'] = array('input', 'textarea');
$cfg_db['PropertiesIconic'] = array(
    true   => __('Icons'),
    false  => __('Text'),
    'both' => __('Both')
);
$cfg_db['GridEditing'] = array(
    'click' => __('Click'),
    'double-click' => __('Double click'),
    'disabled' => __('Disabled'),
);
$cfg_db['DefaultTabServer'] = array(
    'index.php',               // the welcome page (recommended for multiuser setups)
    'server_databases.php',    // list of databases
    'server_status.php',       // runtime information
    'server_variables.php',    // MySQL server variables
    'server_privileges.php'    // user management
);
$cfg_db['DefaultTabDatabase'] = array(
    'db_structure.php',   // tables list
    'db_sql.php',         // SQL form
    'db_search.php',      // search query
    'db_operations.php'   // operations on database
);
$cfg_db['DefaultTabTable'] = array(
    'tbl_structure.php', // fields list
    'tbl_sql.php',       // SQL form
    'tbl_select.php',    // search page
    'tbl_change.php',    // insert row page
    'sql.php'            // browse page
);
$cfg_db['QueryWindowDefTab'] = array(
    'sql',     // SQL
    'files',   // Import files
    'history', // SQL history
    'full'     // All (SQL and SQL history)
);
$cfg_db['InitialSlidersState'] = array(
    'open'     => __('Open'),
    'closed'   => __('Closed'),
    'disabled' => __('Disabled')
);
$cfg_db['Import']['format'] = array(
    'csv',    // CSV
    'docsql', // DocSQL
    'ldi',    // CSV using LOAD DATA
    'sql'     // SQL
);
$cfg_db['Import']['charset'] = array_merge(
    array(''),
    $GLOBALS['cfg']['AvailableCharsets']
);
$cfg_db['Import']['sql_compatibility']
    = $cfg_db['Export']['sql_compatibility'] = array(
    'NONE', 'ANSI', 'DB2', 'MAXDB', 'MYSQL323',
    'MYSQL40', 'MSSQL', 'ORACLE',
    // removed; in MySQL 5.0.33, this produces exports that
    // can't be read by POSTGRESQL (see our bug #1596328)
    //'POSTGRESQL',
    'TRADITIONAL'
);
$cfg_db['Import']['csv_terminated'] = 'short_string';
$cfg_db['Import']['csv_enclosed'] = 'short_string';
$cfg_db['Import']['csv_escaped'] = 'short_string';
$cfg_db['Import']['ldi_terminated'] = 'short_string';
$cfg_db['Import']['ldi_enclosed'] = 'short_string';
$cfg_db['Import']['ldi_escaped'] = 'short_string';
$cfg_db['Import']['ldi_local_option'] = array('auto', true, false);
$cfg_db['Export']['_sod_select'] = array(
    'structure'          => __('structure'),
    'data'               => __('data'),
    'structure_and_data' => __('structure and data')
);
$cfg_db['Export']['method'] = array(
    'quick'          => __('Quick - display only the minimal options to configure'),
    'custom'         => __('Custom - display all possible options to configure'),
    'custom-no-form' => __('Custom - like above, but without the quick/custom choice')
);
$cfg_db['Export']['format'] = array(
    'codegen', 'csv', 'excel', 'htmlexcel','htmlword', 'latex', 'ods',
    'odt', 'pdf', 'sql', 'texytext', 'xls', 'xml', 'yaml'
);
$cfg_db['Export']['compression'] = array('none', 'zip', 'gzip', 'bzip2');
$cfg_db['Export']['charset'] = array_merge(
    array(''),
    $GLOBALS['cfg']['AvailableCharsets']
);
$cfg_db['Export']['codegen_format'] = array(
    '#', 'NHibernate C# DO', 'NHibernate XML'
);
$cfg_db['Export']['csv_separator'] = 'short_string';
$cfg_db['Export']['csv_terminated'] = 'short_string';
$cfg_db['Export']['csv_enclosed'] = 'short_string';
$cfg_db['Export']['csv_escaped'] = 'short_string';
$cfg_db['Export']['csv_null'] = 'short_string';
$cfg_db['Export']['excel_null'] = 'short_string';
$cfg_db['Export']['excel_edition'] = array(
    'win'           => 'Windows',
    'mac_excel2003' => 'Excel 2003 / Macintosh',
    'mac_excel2008' => 'Excel 2008 / Macintosh'
);
$cfg_db['Export']['sql_structure_or_data'] = $cfg_db['Export']['_sod_select'];
$cfg_db['Export']['sql_type'] = array('INSERT', 'UPDATE', 'REPLACE');
$cfg_db['Export']['sql_insert_syntax'] = array(
    'complete' => __('complete inserts'),
    'extended' => __('extended inserts'),
    'both'     => __('both of the above'),
    'none'     => __('neither of the above')
);
$cfg_db['Export']['xls_null'] = 'short_string';
$cfg_db['Export']['xlsx_null'] = 'short_string';
$cfg_db['Export']['htmlword_structure_or_data'] = $cfg_db['Export']['_sod_select'];
$cfg_db['Export']['htmlword_null'] = 'short_string';
$cfg_db['Export']['ods_null'] = 'short_string';
$cfg_db['Export']['odt_null'] = 'short_string';
$cfg_db['Export']['odt_structure_or_data'] = $cfg_db['Export']['_sod_select'];
$cfg_db['Export']['texytext_structure_or_data'] = $cfg_db['Export']['_sod_select'];
$cfg_db['Export']['texytext_null'] = 'short_string';

/**
 * Default values overrides
 * Use only full paths
 */
$cfg_db['_overrides'] = array();
$cfg_db['_overrides']['Servers/1/extension'] = extension_loaded('mysqli')
    ? 'mysqli' : 'mysql';

/**
 * Basic validator assignments (functions from libraries/config/validate.lib.php
 * and 'validators' object in js/config.js)
 * Use only full paths and form ids
 */
$cfg_db['_validators'] = array(
    'CharTextareaCols' => 'PMA_validatePositiveNumber',
    'CharTextareaRows' => 'PMA_validatePositiveNumber',
    'ExecTimeLimit' => 'PMA_validateNonNegativeNumber',
    'Export/sql_max_query_size' => 'PMA_validatePositiveNumber',
    'ForeignKeyMaxLimit' => 'PMA_validatePositiveNumber',
    'Import/csv_enclosed' => array(array('PMA_validateByRegex', '/^.?$/')),
    'Import/csv_escaped' => array(array('PMA_validateByRegex', '/^.$/')),
    'Import/csv_terminated' => array(array('PMA_validateByRegex', '/^.$/')),
    'Import/ldi_enclosed' => array(array('PMA_validateByRegex', '/^.?$/')),
    'Import/ldi_escaped' => array(array('PMA_validateByRegex', '/^.$/')),
    'Import/ldi_terminated' => array(array('PMA_validateByRegex', '/^.$/')),
    'Import/skip_queries' => 'PMA_validateNonNegativeNumber',
    'InsertRows' => 'PMA_validatePositiveNumber',
    'NumRecentTables' => 'PMA_validateNonNegativeNumber',
    'LimitChars' => 'PMA_validatePositiveNumber',
    'LoginCookieValidity' => 'PMA_validatePositiveNumber',
    'LoginCookieStore' => 'PMA_validateNonNegativeNumber',
    'MaxDbList' => 'PMA_validatePositiveNumber',
    'MaxNavigationItems' => 'PMA_validatePositiveNumber',
    'MaxCharactersInDisplayedSQL' => 'PMA_validatePositiveNumber',
    'MaxRows' => 'PMA_validatePositiveNumber',
    'MaxTableList' => 'PMA_validatePositiveNumber',
    'MemoryLimit' => array(array('PMA_validateByRegex', '/^\d+(?:[kmg])?$/i')),
    'NavigationTreeTableLevel' => 'PMA_validatePositiveNumber',
    'QueryHistoryMax' => 'PMA_validatePositiveNumber',
    'QueryWindowWidth' => 'PMA_validatePositiveNumber',
    'QueryWindowHeight' => 'PMA_validatePositiveNumber',
    'RepeatCells' => 'PMA_validateNonNegativeNumber',
    'Server' => 'PMA_validateServer',
    'Server_pmadb' => 'PMA_validatePMAStorage',
    'Servers/1/port' => 'PMA_validatePortNumber',
    'Servers/1/hide_db' => 'PMA_validateRegex',
    'TextareaCols' => 'PMA_validatePositiveNumber',
    'TextareaRows' => 'PMA_validatePositiveNumber',
    'TrustedProxies' => 'PMA_validateTrustedProxies');

/**
 * Additional validators used for user preferences
 */
$cfg_db['_userValidators'] = array(
    'MaxDbList'       => array(
        array('PMA_validateUpperBound', 'value:MaxDbList')
    ),
    'MaxTableList'    => array(
        array('PMA_validateUpperBound', 'value:MaxTableList')
    ),
    'QueryHistoryMax' => array(
        array('PMA_validateUpperBound', 'value:QueryHistoryMax')
    )
);
?>