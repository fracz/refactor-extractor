<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * handles miscellaneous db operations:
 *  - move/rename
 *  - copy
 *  - changing collation
 *  - changing comment
 *  - adding tables
 *  - viewing PDF schemas
 *
 * @package PhpMyAdmin
 */

/**
 * requirements
 */
require_once 'libraries/common.inc.php';
require_once 'libraries/mysql_charsets.lib.php';

/**
 * functions implementation for this script
 */
require_once 'libraries/db_operations.lib.php';

// add a javascript file for jQuery functions to handle Ajax actions
$response = PMA_Response::getInstance();
$header = $response->getHeader();
$scripts = $header->getScripts();
$scripts->addFile('db_operations.js');
$common_functions = PMA_CommonFunctions::getInstance();

/**
 * Sets globals from $_REQUEST (we're using GET on ajax, POST otherwise)
 */
$request_params = array(
    'add_constraints',
    'comment',
    'create_database_before_copying',
    'db_collation',
    'db_copy',
    'db_rename',
    'drop_if_exists',
    'newname',
    'sql_auto_increment',
    'submitcollation',
    'switch_to_new',
    'what'
);
foreach ($request_params as $one_request_param) {
    if (isset($_REQUEST[$one_request_param])) {
        $GLOBALS[$one_request_param] = $_REQUEST[$one_request_param];
    }
}

/**
 * Rename/move or copy database
 */
if (strlen($db) && (! empty($db_rename) || ! empty($db_copy))) {

    if (! empty($db_rename)) {
        $move = true;
    } else {
        $move = false;
    }

    if (! isset($newname) || ! strlen($newname)) {
        $message = PMA_Message::error(__('The database name is empty!'));
    } else {
        $sql_query = ''; // in case target db exists
        $_error = false;
        if ($move
            || (isset($create_database_before_copying)
            && $create_database_before_copying)
        ) {
            $sql_query = PMA_getSqlQueryAndCreateDbBeforeCopy();
        }

        // here I don't use DELIMITER because it's not part of the
        // language; I have to send each statement one by one

        // to avoid selecting alternatively the current and new db
        // we would need to modify the CREATE definitions to qualify
        // the db name
        PMA_runProcedureAndFunctionDefinitions();

        // go back to current db, just in case
        PMA_DBI_select_db($db);

        $tables_full = PMA_DBI_get_tables_full($db);

        require_once "libraries/plugin_interface.lib.php";
        // remove all foreign key constraints, otherwise we can get errors
        $export_sql_plugin = PMA_getPlugin(
            "export",
            "sql",
            'libraries/plugins/export/',
            array(
                'export_type' => $export_type,
                'single_table' => isset($single_table)
            )
        );
        $GLOBALS['sql_constraints_query_full_db'] =
            PMA_getSqlConstraintsQueryForFullDb(
                $tables_full, $export_sql_plugin, $move
            );

        $views = PMA_getViewsAndCreateSqlViewStandIn($tables_full, $export_sql_plugin);

        foreach ($tables_full as $each_table => $tmp) {
            // skip the views; we have creted stand-in definitions
            if (PMA_Table::isView($db, $each_table)) {
                continue;
            }
            $back = $sql_query;
            $sql_query = '';

            // value of $what for this table only
            $this_what = $what;

            // do not copy the data from a Merge table
            // note: on the calling FORM, 'data' means 'structure and data'
            if (PMA_Table::isMerge($db, $each_table)) {
                if ($this_what == 'data') {
                    $this_what = 'structure';
                }
                if ($this_what == 'dataonly') {
                    $this_what = 'nocopy';
                }
            }

            if ($this_what != 'nocopy') {
                // keep the triggers from the original db+table
                // (third param is empty because delimiters are only intended
                //  for importing via the mysql client or our Import feature)
                $triggers = PMA_DBI_get_triggers($db, $each_table, '');

                if (! PMA_Table::moveCopy(
                    $db, $each_table, $newname, $each_table,
                    isset($this_what) ? $this_what : 'data',
                    $move, 'db_copy'
                )) {
                    $_error = true;
                    // $sql_query is filled by PMA_Table::moveCopy()
                    $sql_query = $back . $sql_query;
                    break;
                }
                // apply the triggers to the destination db+table
                if ($triggers) {
                    PMA_DBI_select_db($newname);
                    foreach ($triggers as $trigger) {
                        PMA_DBI_query($trigger['create']);
                        $GLOBALS['sql_query'] .= "\n" . $trigger['create'] . ';';
                    }
                    unset($trigger);
                }
                unset($triggers);

                // this does not apply to a rename operation
                if (isset($GLOBALS['add_constraints'])
                    && ! empty($GLOBALS['sql_constraints_query'])
                ) {
                    $GLOBALS['sql_constraints_query_full_db'][] = $GLOBALS['sql_constraints_query'];
                    unset($GLOBALS['sql_constraints_query']);
                }
            }
            // $sql_query is filled by PMA_Table::moveCopy()
            $sql_query = $back . $sql_query;
        } // end (foreach)
        unset($each_table);

        // handle the views
        if (! $_error) {
            // temporarily force to add DROP IF EXIST to CREATE VIEW query,
            // to remove stand-in VIEW that was created earlier
            if (isset($GLOBALS['drop_if_exists'])) {
                $temp_drop_if_exists = $GLOBALS['drop_if_exists'];
            }
            $GLOBALS['drop_if_exists'] = 'true';

            foreach ($views as $view) {
                if (! PMA_Table::moveCopy($db, $view, $newname, $view, 'structure', $move, 'db_copy')) {
                    $_error = true;
                    break;
                }
            }
            unset($GLOBALS['drop_if_exists']);
            if (isset($temp_drop_if_exists)) {
                // restore previous value
                $GLOBALS['drop_if_exists'] = $temp_drop_if_exists;
                unset($temp_drop_if_exists);
            }
        }
        unset($view, $views);

        // now that all tables exist, create all the accumulated constraints
        if (! $_error && count($GLOBALS['sql_constraints_query_full_db']) > 0) {
            PMA_DBI_select_db($newname);
            foreach ($GLOBALS['sql_constraints_query_full_db'] as $one_query) {
                PMA_DBI_query($one_query);
                // and prepare to display them
                $GLOBALS['sql_query'] .= "\n" . $one_query;
            }

            unset($GLOBALS['sql_constraints_query_full_db'], $one_query);
        }

        if (! PMA_DRIZZLE && PMA_MYSQL_INT_VERSION >= 50100) {
            // here DELIMITER is not used because it's not part of the
            // language; each statement is sent one by one

            // to avoid selecting alternatively the current and new db
            // we would need to modify the CREATE definitions to qualify
            // the db name
            $event_names = PMA_DBI_fetch_result(
                'SELECT EVENT_NAME FROM information_schema.EVENTS WHERE EVENT_SCHEMA= \''
                . $common_functions->sqlAddSlashes($db, true) . '\';'
            );
            if ($event_names) {
                foreach ($event_names as $event_name) {
                    PMA_DBI_select_db($db);
                    $tmp_query = PMA_DBI_get_definition($db, 'EVENT', $event_name);
                    // collect for later display
                    $GLOBALS['sql_query'] .= "\n" . $tmp_query;
                    PMA_DBI_select_db($newname);
                    PMA_DBI_query($tmp_query);
                }
            }
        }

        // go back to current db, just in case
        PMA_DBI_select_db($db);

        // Duplicate the bookmarks for this db (done once for each db)
        if (! $_error && $db != $newname) {
            $get_fields = array('user', 'label', 'query');
            $where_fields = array('dbase' => $db);
            $new_fields = array('dbase' => $newname);
            PMA_Table::duplicateInfo(
                'bookmarkwork', 'bookmark', $get_fields,
                $where_fields, $new_fields
            );
        }

        if (! $_error && $move) {
            /**
             * cleanup pmadb stuff for this db
             */
            include_once 'libraries/relation_cleanup.lib.php';
            PMA_relationsCleanupDatabase($db);

            // if someday the RENAME DATABASE reappears, do not DROP
            $local_query = 'DROP DATABASE ' . $common_functions->backquote($db) . ';';
            $sql_query .= "\n" . $local_query;
            PMA_DBI_query($local_query);

            $message = PMA_Message::success(__('Database %1$s has been renamed to %2$s'));
            $message->addParam($db);
            $message->addParam($newname);
        } elseif (! $_error) {
            $message = PMA_Message::success(__('Database %1$s has been copied to %2$s'));
            $message->addParam($db);
            $message->addParam($newname);
        }
        $reload     = true;

        /* Change database to be used */
        if (! $_error && $move) {
            $db = $newname;
        } elseif (! $_error) {
            if (isset($switch_to_new) && $switch_to_new == 'true') {
                $GLOBALS['PMA_Config']->setCookie('pma_switch_to_new', 'true');
                $db = $newname;
            } else {
                $GLOBALS['PMA_Config']->setCookie('pma_switch_to_new', '');
            }
        }

        if ($_error && ! isset($message)) {
            $message = PMA_Message::error();
        }
    }

    /**
     * Database has been successfully renamed/moved.  If in an Ajax request,
     * generate the output with {@link PMA_Response} and exit
     */
    if ($GLOBALS['is_ajax_request'] == true) {
        $response = PMA_Response::getInstance();
        $response->isSuccess($message->isSuccess());
        $response->addJSON('message', $message);
        $response->addJSON('newname', $newname);
        $response->addJSON(
            'sql_query',
            $common_functions->getMessage(null, $sql_query)
        );
        exit;
    }
}


/**
 * Settings for relations stuff
 */

$cfgRelation = PMA_getRelationsParam();

/**
 * Check if comments were updated
 * (must be done before displaying the menu tabs)
 */
if (isset($_REQUEST['comment'])) {
    PMA_setDbComment($db, $comment);
}

/**
 * Prepares the tables list if the user where not redirected to this script
 * because there is no table in the database ($is_info is true)
 */
if (empty($is_info)) {
    include 'libraries/db_common.inc.php';
    $url_query .= '&amp;goto=db_operations.php';

    // Gets the database structure
    $sub_part = '_structure';
    include 'libraries/db_info.inc.php';
    echo "\n";

    if (isset($message)) {
        echo $common_functions->getMessage($message, $sql_query);
        unset($message);
    }
}

$db_collation = PMA_getDbCollation($db);
$is_information_schema = PMA_is_system_schema($db);

if (!$is_information_schema) {
    if ($cfgRelation['commwork']) {
        /**
         * database comment
         */
        echo PMA_getHtmlForDatabaseComment();
    }
    ?>
    <div class="operations_half_width">
    <?php include 'libraries/display_create_table.lib.php'; ?>
    </div>
    <?php
    /**
     * rename database
     */
    if ($db != 'mysql') {
        echo PMA_getHtmlForRenameDatabase();
    }

    // Drop link if allowed
    // Don't even try to drop information_schema. You won't be able to. Believe me. You won't.
    // Don't allow to easily drop mysql database, RFE #1327514.
    if (($is_superuser || $GLOBALS['cfg']['AllowUserDropDatabase'])
        && ! $db_is_information_schema
        && (PMA_DRIZZLE || $db != 'mysql')
    ) {
        echo PMA_getHtmlForDropDatabaseLink();
    }
    /**
     * Copy database
     */
    echo PMA_getHtmlForCopyDatabase();

    /**
     * Change database charset
     */
    echo PMA_getHtmlForChangeDatabaseCharset();

    if ($num_tables > 0
        && ! $cfgRelation['allworks']
        && $cfg['PmaNoRelation_DisableWarning'] == false
    ) {
        $message = PMA_Message::notice(
            __('The phpMyAdmin configuration storage has been deactivated. To find out why click %shere%s.')
        );
        $message->addParam(
            '<a href="' . $cfg['PmaAbsoluteUri'] . 'chk_rel.php?' . $url_query . '">',
            false
        );
        $message->addParam('</a>', false);
        /* Show error if user has configured something, notice elsewhere */
        if (!empty($cfg['Servers'][$server]['pmadb'])) {
            $message->isError(true);
        }
        echo '<div class="operations_full_width">';
        $message->display();
        echo '</div>';
    } // end if
} // end if (!$is_information_schema)


// not sure about displaying the PDF dialog in case db is information_schema
if ($cfgRelation['pdfwork'] && $num_tables > 0) {
    // We only show this if we find something in the new pdf_pages table
    $test_query = '
         SELECT *
           FROM ' . $common_functions->backquote($GLOBALS['cfgRelation']['db'])
            . '.' . $common_functions->backquote($cfgRelation['pdf_pages']) . '
          WHERE db_name = \'' . $common_functions->sqlAddSlashes($db) . '\'';
    $test_rs    = PMA_queryAsControlUser($test_query, null, PMA_DBI_QUERY_STORE);

    /*
     * Export Relational Schema View
     */
    echo PMA_getHtmlForExportRelationalSchemaView($url_query);
} // end if

?>