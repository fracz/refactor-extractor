<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 *
 * @package PhpMyAdmin
 */

/**
 *
 */
require_once 'libraries/common.inc.php';

/**
 * functions implementation for this script
 */
require_once 'libraries/server_privileges.lib.php';

/**
 * Does the common work
 */
$response = PMA_Response::getInstance();
$header   = $response->getHeader();
$scripts  = $header->getScripts();
$scripts->addFile('server_privileges.js');
$common_functions = PMA_CommonFunctions::getInstance();

$_add_user_error = false;

/**
 * Sets globals from $_GET
 */

$get_params = array(
    'checkprivs',
    'db',
    'dbname',
    'hostname',
    'initial',
    'old_username',
    'old_hostname',
    'tablename',
    'username',
    'viewing_mode'
);
foreach ($get_params as $one_get_param) {
    if (isset($_REQUEST[$one_get_param])) {
        $GLOBALS[$one_get_param] = $_REQUEST[$one_get_param];
    }
}
/**
 * Sets globals from $_POST
 */

$post_params = array(
    'createdb-1',
    'createdb-2',
    'createdb-3',
    'grant_count',
    'hostname',
    'pma_pw',
    'pma_pw2',
    'pred_hostname',
    'pred_password',
    'pred_username',
    'update_privs',
    'username'
);
foreach ($post_params as $one_post_param) {
    if (isset($_POST[$one_post_param])) {
        $GLOBALS[$one_post_param] = $_POST[$one_post_param];
    }
}

/**
 * Sets globals from $_POST patterns, for privileges and max_* vars
 */

$post_patterns = array(
    '/_priv$/i',
    '/^max_/i'
);
foreach (array_keys($_POST) as $post_key) {
    foreach ($post_patterns as $one_post_pattern) {
        if (preg_match($one_post_pattern, $post_key)) {
            $GLOBALS[$post_key] = $_POST[$post_key];
        }
    }
}

require 'libraries/server_common.inc.php';

if ($GLOBALS['cfg']['AjaxEnable']) {
    $conditional_class = 'ajax';
} else {
    $conditional_class = '';
}

/**
 * Messages are built using the message name
 */
$strPrivDescAllPrivileges = __('Includes all privileges except GRANT.');
$strPrivDescAlter = __('Allows altering the structure of existing tables.');
$strPrivDescAlterRoutine = __('Allows altering and dropping stored routines.');
$strPrivDescCreateDb = __('Allows creating new databases and tables.');
$strPrivDescCreateRoutine = __('Allows creating stored routines.');
$strPrivDescCreateTbl = __('Allows creating new tables.');
$strPrivDescCreateTmpTable = __('Allows creating temporary tables.');
$strPrivDescCreateUser = __('Allows creating, dropping and renaming user accounts.');
$strPrivDescCreateView = __('Allows creating new views.');
$strPrivDescDelete = __('Allows deleting data.');
$strPrivDescDropDb = __('Allows dropping databases and tables.');
$strPrivDescDropTbl = __('Allows dropping tables.');
$strPrivDescEvent = __('Allows to set up events for the event scheduler');
$strPrivDescExecute = __('Allows executing stored routines.');
$strPrivDescFile = __('Allows importing data from and exporting data into files.');
$strPrivDescGrant = __('Allows adding users and privileges without reloading the privilege tables.');
$strPrivDescIndex = __('Allows creating and dropping indexes.');
$strPrivDescInsert = __('Allows inserting and replacing data.');
$strPrivDescLockTables = __('Allows locking tables for the current thread.');
$strPrivDescMaxConnections = __('Limits the number of new connections the user may open per hour.');
$strPrivDescMaxQuestions = __('Limits the number of queries the user may send to the server per hour.');
$strPrivDescMaxUpdates = __('Limits the number of commands that change any table or database the user may execute per hour.');
$strPrivDescMaxUserConnections = __('Limits the number of simultaneous connections the user may have.');
$strPrivDescProcess = __('Allows viewing processes of all users');
$strPrivDescReferences = __('Has no effect in this MySQL version.');
$strPrivDescReload = __('Allows reloading server settings and flushing the server\'s caches.');
$strPrivDescReplClient = __('Allows the user to ask where the slaves / masters are.');
$strPrivDescReplSlave = __('Needed for the replication slaves.');
$strPrivDescSelect = __('Allows reading data.');
$strPrivDescShowDb = __('Gives access to the complete list of databases.');
$strPrivDescShowView = __('Allows performing SHOW CREATE VIEW queries.');
$strPrivDescShutdown = __('Allows shutting down the server.');
$strPrivDescSuper = __('Allows connecting, even if maximum number of connections is reached; required for most administrative operations like setting global variables or killing threads of other users.');
$strPrivDescTrigger = __('Allows creating and dropping triggers');
$strPrivDescUpdate = __('Allows changing data.');
$strPrivDescUsage = __('No privileges.');

/**
 * Checks if a dropdown box has been used for selecting a database / table
 */
if (PMA_isValid($_REQUEST['pred_tablename'])) {
    $tablename = $_REQUEST['pred_tablename'];
} elseif (PMA_isValid($_REQUEST['tablename'])) {
    $tablename = $_REQUEST['tablename'];
} else {
    unset($tablename);
}

if (PMA_isValid($_REQUEST['pred_dbname'])) {
    $dbname = $_REQUEST['pred_dbname'];
    unset($pred_dbname);
} elseif (PMA_isValid($_REQUEST['dbname'])) {
    $dbname = $_REQUEST['dbname'];
} else {
    unset($dbname);
    unset($tablename);
}

if (isset($dbname)) {
    $db_and_table = $common_functions->backquote($common_functions->unescapeMysqlWildcards($dbname)) . '.';
    if (isset($tablename)) {
        $db_and_table .= $common_functions->backquote($tablename);
    } else {
        $db_and_table .= '*';
    }
} else {
    $db_and_table = '*.*';
}

$dbname_is_wildcard = false;
// check if given $dbname is a wildcard or not
if (isset($dbname)) {
    //if (preg_match('/\\\\(?:_|%)/i', $dbname)) {
    if (preg_match('/(?<!\\\\)(?:_|%)/i', $dbname)) {
        $dbname_is_wildcard = true;
    }
}

/**
 * Checks if the user is allowed to do what he tries to...
 */
if (! $is_superuser) {
    echo '<h2>' . "\n"
       . $common_functions->getIcon('b_usrlist.png')
       . __('Privileges') . "\n"
       . '</h2>' . "\n";
    PMA_Message::error(__('No Privileges'))->display();
    exit;
}

// a random number that will be appended to the id of the user forms
$random_n = mt_rand(0, 1000000);

/**
 * Changes / copies a user, part I
 */
if (isset($_REQUEST['change_copy'])) {
    $user_host_condition = ' WHERE `User`'
        .' = \'' . $common_functions->sqlAddSlashes($old_username) . "'"
        .' AND `Host`'
        .' = \'' . $common_functions->sqlAddSlashes($old_hostname) . '\';';
    $row = PMA_DBI_fetch_single_row('SELECT * FROM `mysql`.`user` ' . $user_host_condition);
    if (! $row) {
        PMA_Message::notice(__('No user found.'))->display();
        unset($_REQUEST['change_copy']);
    } else {
        extract($row, EXTR_OVERWRITE);
        // Recent MySQL versions have the field "Password" in mysql.user,
        // so the previous extract creates $Password but this script
        // uses $password
        if (! isset($password) && isset($Password)) {
            $password = $Password;
        }
        $queries = array();
    }
}

/**
 * Adds a user
 *   (Changes / copies a user, part II)
 */
if (isset($_REQUEST['adduser_submit']) || isset($_REQUEST['change_copy'])) {
    $sql_query = '';
    if ($pred_username == 'any') {
        $username = '';
    }
    switch ($pred_hostname) {
    case 'any':
        $hostname = '%';
        break;
    case 'localhost':
        $hostname = 'localhost';
        break;
    case 'hosttable':
        $hostname = '';
        break;
    case 'thishost':
        $_user_name = PMA_DBI_fetch_value('SELECT USER()');
        $hostname = substr($_user_name, (strrpos($_user_name, '@') + 1));
        unset($_user_name);
        break;
    }
    $sql = "SELECT '1' FROM `mysql`.`user`"
        . " WHERE `User` = '" . $common_functions->sqlAddSlashes($username) . "'"
        . " AND `Host` = '" . $common_functions->sqlAddSlashes($hostname) . "';";
    if (PMA_DBI_fetch_value($sql) == 1) {
        $message = PMA_Message::error(__('The user %s already exists!'));
        $message->addParam('[i]\'' . $username . '\'@\'' . $hostname . '\'[/i]');
        $_REQUEST['adduser'] = true;
        $_add_user_error = true;
    } else {

        $create_user_real = 'CREATE USER \'' . $common_functions->sqlAddSlashes($username) . '\'@\'' . $common_functions->sqlAddSlashes($hostname) . '\'';

        $real_sql_query = 'GRANT ' . join(', ', PMA_extractPrivInfo()) . ' ON *.* TO \''
            . $common_functions->sqlAddSlashes($username) . '\'@\'' . $common_functions->sqlAddSlashes($hostname) . '\'';
        if ($pred_password != 'none' && $pred_password != 'keep') {
            $sql_query = $real_sql_query . ' IDENTIFIED BY \'***\'';
            $real_sql_query .= ' IDENTIFIED BY \'' . $common_functions->sqlAddSlashes($pma_pw) . '\'';
            if (isset($create_user_real)) {
                $create_user_show = $create_user_real . ' IDENTIFIED BY \'***\'';
                $create_user_real .= ' IDENTIFIED BY \'' . $common_functions->sqlAddSlashes($pma_pw) . '\'';
            }
        } else {
            if ($pred_password == 'keep' && ! empty($password)) {
                $real_sql_query .= ' IDENTIFIED BY PASSWORD \'' . $password . '\'';
                if (isset($create_user_real)) {
                    $create_user_real .= ' IDENTIFIED BY PASSWORD \'' . $password . '\'';
                }
            }
            $sql_query = $real_sql_query;
            if (isset($create_user_real)) {
                $create_user_show = $create_user_real;
            }
        }

        if ((isset($Grant_priv) && $Grant_priv == 'Y')
            || (isset($max_questions) || isset($max_connections)
            || isset($max_updates) || isset($max_user_connections))
        ) {
            $real_sql_query .= PMA_getWithClauseForAddUserAndUpdatePrivs();
            $sql_query .= $real_sql_query;
        }
        if (isset($create_user_real)) {
            $create_user_real .= ';';
            $create_user_show .= ';';
        }
        $real_sql_query .= ';';
        $sql_query .= ';';
        if (empty($_REQUEST['change_copy'])) {
            $_error = false;

            if (isset($create_user_real)) {
                if (! PMA_DBI_try_query($create_user_real)) {
                    $_error = true;
                }
                $sql_query = $create_user_show . $sql_query;
            }

            if ($_error || ! PMA_DBI_try_query($real_sql_query)) {
                $_REQUEST['createdb-1'] = $_REQUEST['createdb-2'] = $_REQUEST['createdb-3'] = false;
                $message = PMA_Message::rawError(PMA_DBI_getError());
            } else {
                $message = PMA_Message::success(__('You have added a new user.'));
            }

            if (isset($_REQUEST['createdb-1'])) {
                // Create database with same name and grant all privileges
                $q = 'CREATE DATABASE IF NOT EXISTS '
                    . $common_functions->backquote($common_functions->sqlAddSlashes($username)) . ';';
                $sql_query .= $q;
                if (! PMA_DBI_try_query($q)) {
                    $message = PMA_Message::rawError(PMA_DBI_getError());
                }


                /**
                 * If we are not in an Ajax request, we can't reload navigation now
                 */
                if ($GLOBALS['is_ajax_request'] != true) {
                    // this is needed in case tracking is on:
                    $GLOBALS['db'] = $username;
                    $GLOBALS['reload'] = true;
                    echo $common_functions->getReloadNavigationScript();
                }

                $q = 'GRANT ALL PRIVILEGES ON '
                    . $common_functions->backquote($common_functions->escapeMysqlWildcards($common_functions->sqlAddSlashes($username))) . '.* TO \''
                    . $common_functions->sqlAddSlashes($username) . '\'@\'' . $common_functions->sqlAddSlashes($hostname) . '\';';
                $sql_query .= $q;
                if (! PMA_DBI_try_query($q)) {
                    $message = PMA_Message::rawError(PMA_DBI_getError());
                }
            }

            if (isset($_REQUEST['createdb-2'])) {
                // Grant all privileges on wildcard name (username\_%)
                $q = 'GRANT ALL PRIVILEGES ON '
                    . $common_functions->backquote($common_functions->sqlAddSlashes($username) . '\_%') . '.* TO \''
                    . $common_functions->sqlAddSlashes($username) . '\'@\'' . $common_functions->sqlAddSlashes($hostname) . '\';';
                $sql_query .= $q;
                if (! PMA_DBI_try_query($q)) {
                    $message = PMA_Message::rawError(PMA_DBI_getError());
                }
            }

            if (isset($_REQUEST['createdb-3'])) {
                // Grant all privileges on the specified database to the new user
                $q = 'GRANT ALL PRIVILEGES ON '
                . $common_functions->backquote($common_functions->sqlAddSlashes($dbname)) . '.* TO \''
                . $common_functions->sqlAddSlashes($username) . '\'@\'' . $common_functions->sqlAddSlashes($hostname) . '\';';
                $sql_query .= $q;
                if (! PMA_DBI_try_query($q)) {
                    $message = PMA_Message::rawError(PMA_DBI_getError());
                }
            }
        } else {
            if (isset($create_user_real)) {
                $queries[]             = $create_user_real;
            }
            $queries[]             = $real_sql_query;
            // we put the query containing the hidden password in
            // $queries_for_display, at the same position occupied
            // by the real query in $queries
            $tmp_count = count($queries);
            if (isset($create_user_real)) {
                $queries_for_display[$tmp_count - 2] = $create_user_show;
            }
            $queries_for_display[$tmp_count - 1] = $sql_query;
        }
        unset($res, $real_sql_query);
    }
}


/**
 * Changes / copies a user, part III
 */
if (isset($_REQUEST['change_copy'])) {
    $user_host_condition = ' WHERE `User`'
        .' = \'' . $common_functions->sqlAddSlashes($old_username) . "'"
        .' AND `Host`'
        .' = \'' . $common_functions->sqlAddSlashes($old_hostname) . '\';';
    $res = PMA_DBI_query('SELECT * FROM `mysql`.`db`' . $user_host_condition);
    while ($row = PMA_DBI_fetch_assoc($res)) {
        $queries[] = 'GRANT ' . join(', ', PMA_extractPrivInfo($row))
            .' ON ' . $common_functions->backquote($row['Db']) . '.*'
            .' TO \'' . $common_functions->sqlAddSlashes($username) . '\'@\'' . $common_functions->sqlAddSlashes($hostname) . '\''
            . ($row['Grant_priv'] == 'Y' ? ' WITH GRANT OPTION;' : ';');
    }
    PMA_DBI_free_result($res);
    $res = PMA_DBI_query(
        'SELECT `Db`, `Table_name`, `Table_priv` FROM `mysql`.`tables_priv`' . $user_host_condition,
        $GLOBALS['userlink'],
        PMA_DBI_QUERY_STORE
    );
    while ($row = PMA_DBI_fetch_assoc($res)) {

        $res2 = PMA_DBI_QUERY(
            'SELECT `Column_name`, `Column_priv`'
            .' FROM `mysql`.`columns_priv`'
            .' WHERE `User`'
            .' = \'' . $common_functions->sqlAddSlashes($old_username) . "'"
            .' AND `Host`'
            .' = \'' . $common_functions->sqlAddSlashes($old_hostname) . '\''
            .' AND `Db`'
            .' = \'' . $common_functions->sqlAddSlashes($row['Db']) . "'"
            .' AND `Table_name`'
            .' = \'' . $common_functions->sqlAddSlashes($row['Table_name']) . "'"
            .';',
            null,
            PMA_DBI_QUERY_STORE
        );

        $tmp_privs1 = PMA_extractPrivInfo($row);
        $tmp_privs2 = array(
            'Select' => array(),
            'Insert' => array(),
            'Update' => array(),
            'References' => array()
        );

        while ($row2 = PMA_DBI_fetch_assoc($res2)) {
            $tmp_array = explode(',', $row2['Column_priv']);
            if (in_array('Select', $tmp_array)) {
                $tmp_privs2['Select'][] = $row2['Column_name'];
            }
            if (in_array('Insert', $tmp_array)) {
                $tmp_privs2['Insert'][] = $row2['Column_name'];
            }
            if (in_array('Update', $tmp_array)) {
                $tmp_privs2['Update'][] = $row2['Column_name'];
            }
            if (in_array('References', $tmp_array)) {
                $tmp_privs2['References'][] = $row2['Column_name'];
            }
            unset($tmp_array);
        }
        if (count($tmp_privs2['Select']) > 0 && ! in_array('SELECT', $tmp_privs1)) {
            $tmp_privs1[] = 'SELECT (`' . join('`, `', $tmp_privs2['Select']) . '`)';
        }
        if (count($tmp_privs2['Insert']) > 0 && ! in_array('INSERT', $tmp_privs1)) {
            $tmp_privs1[] = 'INSERT (`' . join('`, `', $tmp_privs2['Insert']) . '`)';
        }
        if (count($tmp_privs2['Update']) > 0 && ! in_array('UPDATE', $tmp_privs1)) {
            $tmp_privs1[] = 'UPDATE (`' . join('`, `', $tmp_privs2['Update']) . '`)';
        }
        if (count($tmp_privs2['References']) > 0 && ! in_array('REFERENCES', $tmp_privs1)) {
            $tmp_privs1[] = 'REFERENCES (`' . join('`, `', $tmp_privs2['References']) . '`)';
        }
        unset($tmp_privs2);
        $queries[] = 'GRANT ' . join(', ', $tmp_privs1)
            . ' ON ' . $common_functions->backquote($row['Db']) . '.' . $common_functions->backquote($row['Table_name'])
            . ' TO \'' . $common_functions->sqlAddSlashes($username) . '\'@\'' . $common_functions->sqlAddSlashes($hostname) . '\''
            . (in_array('Grant', explode(',', $row['Table_priv'])) ? ' WITH GRANT OPTION;' : ';');
    }
}

/**
 * Updates privileges
 */
if (! empty($update_privs)) {
    list($sql_query, $message) = PMA_updatePrivileges($username, $hostname);
}

/**
 * Revokes Privileges
 */
if (isset($_REQUEST['revokeall'])) {
    list ($message, $sql_query) = PMA_getMessageAndSqlQueryForPrivilegesRevoke(
        $db_and_table, $dbname, $tablename, $username, $hostname
    );
}

/**
 * Updates the password
 */
if (isset($_REQUEST['change_pw'])) {
    $message = PMA_getMessageForUpdatePassword(
        $pma_pw, $pma_pw2, $err_url, $username, $hostname
    );
}

/**
 * Deletes users
 *   (Changes / copies a user, part IV)
 */
if (isset($_REQUEST['delete'])
    || (isset($_REQUEST['change_copy']) && $_REQUEST['mode'] < 4)
) {
    if (isset($_REQUEST['change_copy'])) {
        $selected_usr = array($_REQUEST['old_username'] . '&amp;#27;' . $_REQUEST['old_hostname']);
    } else {
        $selected_usr = $_REQUEST['selected_usr'];
        $queries = array();
    }
    foreach ($selected_usr as $each_user) {
        list($this_user, $this_host) = explode('&amp;#27;', $each_user);
        $queries[] = '# ' . sprintf(__('Deleting %s'), '\'' . $this_user . '\'@\'' . $this_host . '\'') . ' ...';
        $queries[] = 'DROP USER \'' . $common_functions->sqlAddSlashes($this_user) . '\'@\'' . $common_functions->sqlAddSlashes($this_host) . '\';';

        if (isset($_REQUEST['drop_users_db'])) {
            $queries[] = 'DROP DATABASE IF EXISTS ' . $common_functions->backquote($this_user) . ';';
            $GLOBALS['reload'] = true;

            if ($GLOBALS['is_ajax_request'] != true) {
                echo $common_functions->getReloadNavigationScript();
            }
        }
    }
    if (empty($_REQUEST['change_copy'])) {
        list($sql_query, $message) = PMA_deleteUser($queries);
    }
}

/**
 * Changes / copies a user, part V
 */
if (isset($_REQUEST['change_copy'])) {
    $tmp_count = 0;
    foreach ($queries as $sql_query) {
        if ($sql_query{0} != '#') {
            PMA_DBI_query($sql_query);
        }
        // when there is a query containing a hidden password, take it
        // instead of the real query sent
        if (isset($queries_for_display[$tmp_count])) {
            $queries[$tmp_count] = $queries_for_display[$tmp_count];
        }
        $tmp_count++;
    }
    $message = PMA_Message::success();
    $sql_query = join("\n", $queries);
}

/**
 * Reloads the privilege tables into memory
 */
if (isset($_REQUEST['flush_privileges'])) {
    $sql_query = 'FLUSH PRIVILEGES;';
    PMA_DBI_query($sql_query);
    $message = PMA_Message::success(__('The privileges were reloaded successfully.'));
}

/**
 * some standard links
 */
list($link_edit, $link_revoke, $link_export, $link_export_all)
    = PMA_getStandardLinks($conditional_class);

/**
 * If we are in an Ajax request for Create User/Edit User/Revoke User/
 * Flush Privileges, show $message and exit.
 */
if ($GLOBALS['is_ajax_request']
    && ! isset($_REQUEST['export'])
    && (! isset($_REQUEST['submit_mult']) || $_REQUEST['submit_mult'] != 'export')
    && (! isset($_REQUEST['adduser']) || $_add_user_error)
    && (! isset($_REQUEST['initial']) || empty($_REQUEST['initial']))
    && ! isset($_REQUEST['showall'])
    && ! isset($_REQUEST['edit_user_dialog'])
    && ! isset($_REQUEST['db_specific']))
{
    $isPass = false;
    if (isset($password)) {
        $isPass = true;
    }
    $extra_data = PMA_getExtraDataForAjaxBehavior( $isPass,
        $sql_query, $link_edit, $dbname_is_wildcard, $link_export
    );

    if ($message instanceof PMA_Message) {
        $response = PMA_Response::getInstance();
        $response->isSuccess($message->isSuccess());
        $response->addJSON('message', $message);
        $response->addJSON($extra_data);
        exit;
    }
}

/**
 * Displays the links
 */
if (isset($viewing_mode) && $viewing_mode == 'db') {
    $db = $checkprivs;
    $url_query .= '&amp;goto=db_operations.php';

    // Gets the database structure
    $sub_part = '_structure';
    include 'libraries/db_info.inc.php';
    echo "\n";
} else {
    if (! empty($GLOBALS['message'])) {
        echo $common_functions->getMessage($GLOBALS['message']);
        unset($GLOBALS['message']);
    }
}

/**
 * Displays the page
 */

// export user definition
if (isset($_REQUEST['export'])
    || (isset($_REQUEST['submit_mult']) && $_REQUEST['submit_mult'] == 'export')
) {
    list($title, $export) = PMA_getHtmlForExportUserDefinition($username, $hostname);

    unset($username, $hostname, $grants, $one_grant);

    $response = PMA_Response::getInstance();
    if ($GLOBALS['is_ajax_request']) {
        $response->addJSON('message', $export);
        $response->addJSON('title', $title);
        exit;
    } else {
        $response->addHTML("<h2>$title</h2>$export");
    }
}

if (empty($_REQUEST['adduser']) && (! isset($checkprivs) || ! strlen($checkprivs))) {
    if (! isset($username)) {
        // No username is given --> display the overview
        $response->addHTML(
            PMA_getHtmlForDisplayUserOverviewPage($link_edit, $pmaThemeImage,
                $text_dir, $conditional_class, $link_export, $link_export_all
            )
        );
    } else {
        // A user was selected -> display the user's properties

        // In an Ajax request, prevent cached values from showing
        if ($GLOBALS['is_ajax_request'] == true) {
            header('Cache-Control: no-cache');
        }
        $response->addHTML(
            PMA_getHtmlForDisplayUserProperties($dbname_is_wildcard,$url_dbname,
                $random_n, $username, $hostname, $link_edit, $link_revoke
            )
        );
    }
} elseif (isset($_REQUEST['adduser'])) {
    // Add user
    $response->addHTML(
        PMA_getHtmlForAddUser($random_n, $dbname)
    );
} else {
    // check the privileges for a particular database.
    $response->addHTML(
        PMA_getUserForm($checkprivs, $link_edit, $conditional_class)
    );
} // end if (empty($_REQUEST['adduser']) && empty($checkprivs)) ... elseif ... else ...

?>