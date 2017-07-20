<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * set of functions for the sql executor
 *
 * @package PhpMyAdmin
 */
if (!defined('PHPMYADMIN')) {
    exit;
}

/**
 * Get the database name inside a USE query
 *
 * @param string $sql       SQL query
 * @param array  $databases array with all databases
 *
 * @return strin $db new database name
 */
function PMA_getNewDatabase($sql, $databases)
{
    $db = '';
    // loop through all the databases
    foreach ($databases as $database) {
        if (strpos($sql, $database['SCHEMA_NAME']) !== false) {
            $db = $database;
            break;
        }
    }
    return $db;
}

/**
 * Get the table name in a sql query
 * If there are several tables in the SQL query,
 * first table wil lreturn
 *
 * @param string $sql    SQL query
 * @param array  $tables array of names in current database
 *
 * @return string $table table name
 */
function PMA_getTableNameBySQL($sql, $tables)
{
    $table = '';

    // loop through all the tables in the database
    foreach ($tables as $tbl) {
        if (strpos($sql, $tbl)) {
            $table .= ' ' . $tbl;
        }
    }

    if (count(explode(' ', trim($table))) > 1) {
        $tmp_array = explode(' ', trim($table));
        return $tmp_array[0];
    }

    return trim($table);
}


/**
 * Generate table html when SQL statement have multiple queries
 * which return displayable results
 *
 * @param object $displayResultsObject PMA_DisplayResults object
 * @param string $db                   database name
 * @param array  $sql_data             information about SQL statement
 * @param string $goto                 URL to go back in case of errors
 * @param string $pmaThemeImage        path for theme images  directory
 * @param string $text_dir             text direction
 * @param string $printview            whether printview is enabled
 * @param string $url_query            URL query
 * @param array  $disp_mode            the display mode
 * @param string $sql_limit_to_append  limit clause
 * @param bool   $editable             whether editable or not
 *
 * @return string   $table_html   html content
 */
function getTableHtmlForMultipleQueries(
    $displayResultsObject, $db, $sql_data, $goto, $pmaThemeImage,
    $text_dir, $printview, $url_query, $disp_mode, $sql_limit_to_append,
    $editable
) {
    $table_html = '';

    $tables_array = $GLOBALS['dbi']->getTables($db);
    $databases_array = $GLOBALS['dbi']->getDatabasesFull();
    $multi_sql = implode(";", $sql_data['valid_sql']);
    $querytime_before = array_sum(explode(' ', microtime()));

    // Assignment for variable is not needed since the results are
    // looping using the connection
    @$GLOBALS['dbi']->tryMultiQuery($multi_sql);

    $querytime_after = array_sum(explode(' ', microtime()));
    $querytime = $querytime_after - $querytime_before;
    $sql_no = 0;

    do {
        $analyzed_sql = array();
        $is_affected = false;

        $result = $GLOBALS['dbi']->storeResult();
        $fields_meta = ($result !== false)
            ? $GLOBALS['dbi']->getFieldsMeta($result)
            : array();
        $fields_cnt  = count($fields_meta);

        // Initialize needed params related to each query in multiquery statement
        if (isset($sql_data['valid_sql'][$sql_no])) {
            // 'Use' query can change the database
            if (stripos($sql_data['valid_sql'][$sql_no], "use ")) {
                $db = PMA_getNewDatabase(
                    $sql_data['valid_sql'][$sql_no],
                    $databases_array
                );
            }

            $table = PMA_getTableNameBySQL(
                $sql_data['valid_sql'][$sql_no],
                $tables_array
            );

            // for the use of the parse_analyze.inc.php
            $sql_query = $sql_data['valid_sql'][$sql_no];

            // Parse and analyze the query
            include 'libraries/parse_analyze.inc.php';

            $unlim_num_rows = PMA_Table::countRecords($db, $table, true);
            $showtable = PMA_Table::sGetStatusInfo($db, $table, null, true);
            $url_query = PMA_generate_common_url($db, $table);

            // Handle remembered sorting order, only for single table query
            if ($GLOBALS['cfg']['RememberSorting']
                && ! ($is_count || $is_export || $is_func || $is_analyse)
                && isset($analyzed_sql[0]['select_expr'])
                && (count($analyzed_sql[0]['select_expr']) == 0)
                && isset($analyzed_sql[0]['queryflags']['select_from'])
                && count($analyzed_sql[0]['table_ref']) == 1
            ) {
                PMA_handleSortOrder(
                    $db,
                    $table,
                    $analyzed_sql,
                    $sql_data['valid_sql'][$sql_no]
                );
            }

            // Do append a "LIMIT" clause?
            if (($_SESSION['tmp_user_values']['max_rows'] != 'all')
                && ! ($is_count || $is_export || $is_func || $is_analyse)
                && isset($analyzed_sql[0]['queryflags']['select_from'])
                && ! isset($analyzed_sql[0]['queryflags']['offset'])
                && empty($analyzed_sql[0]['limit_clause'])
            ) {
                $sql_limit_to_append = ' LIMIT '
                    . $_SESSION['tmp_user_values']['pos']
                    . ', ' . $_SESSION['tmp_user_values']['max_rows'] . " ";
                $sql_data['valid_sql'][$sql_no] = PMA_getSqlWithLimitClause(
                    $sql_data['valid_sql'][$sql_no],
                    $analyzed_sql,
                    $sql_limit_to_append
                );
            }

            // Set the needed properties related to executing sql query
            $displayResultsObject->__set('db', $db);
            $displayResultsObject->__set('table', $table);
            $displayResultsObject->__set('goto', $goto);
        }

        if (! $is_affected) {
            $num_rows = ($result) ? @$GLOBALS['dbi']->numRows($result) : 0;
        } elseif (! isset($num_rows)) {
            $num_rows = @$GLOBALS['dbi']->affectedRows();
        }

        if (isset($sql_data['valid_sql'][$sql_no])) {

            $displayResultsObject->__set(
                'sql_query',
                $sql_data['valid_sql'][$sql_no]
            );
            $displayResultsObject->setProperties(
                $unlim_num_rows, $fields_meta, $is_count, $is_export, $is_func,
                $is_analyse, $num_rows, $fields_cnt, $querytime, $pmaThemeImage,
                $text_dir, $is_maint, $is_explain, $is_show, $showtable,
                $printview, $url_query, $editable
            );
        }

        if ($num_rows == 0) {
            continue;
        }

        // With multiple results, operations are limied
        $disp_mode = 'nnnn000000';
        $is_limited_display = true;

        // Collect the tables
        $table_html .= $displayResultsObject->getTable(
            $result, $disp_mode, $analyzed_sql, $is_limited_display
        );

        // Free the result to save the memory
        $GLOBALS['dbi']->freeResult($result);

        $sql_no++;

    } while ($GLOBALS['dbi']->moreResults() && $GLOBALS['dbi']->nextResult());

    return $table_html;
}

/**
 * Handle remembered sorting order, only for single table query
 *
 * @param string $db              database name
 * @param string $table           table name
 * @param array  &$analyzed_sql   the analyzed query
 * @param string &$full_sql_query SQL query
 *
 * @return void
 */
function PMA_handleSortOrder($db, $table, &$analyzed_sql, &$full_sql_query)
{
    $pmatable = new PMA_Table($table, $db);
    if (empty($analyzed_sql[0]['order_by_clause'])) {
        $sorted_col = $pmatable->getUiProp(PMA_Table::PROP_SORTED_COLUMN);
        if ($sorted_col) {
            // retrieve the remembered sorting order for current table
            $sql_order_to_append = ' ORDER BY ' . $sorted_col . ' ';
            $full_sql_query = $analyzed_sql[0]['section_before_limit']
                . $sql_order_to_append . $analyzed_sql[0]['limit_clause']
                . ' ' . $analyzed_sql[0]['section_after_limit'];

            // update the $analyzed_sql
            $analyzed_sql[0]['section_before_limit'] .= $sql_order_to_append;
            $analyzed_sql[0]['order_by_clause'] = $sorted_col;
        }
    } else {
        // store the remembered table into session
        $pmatable->setUiProp(
            PMA_Table::PROP_SORTED_COLUMN,
            $analyzed_sql[0]['order_by_clause']
        );
    }
}

/**
 * Append limit clause to SQL query
 *
 * @param string $full_sql_query      SQL query
 * @param array  $analyzed_sql        the analyzed query
 * @param string $sql_limit_to_append clause to append
 *
 * @return string limit clause appended SQL query
 */
function PMA_getSqlWithLimitClause($full_sql_query, $analyzed_sql,
    $sql_limit_to_append
) {
    return $analyzed_sql[0]['section_before_limit'] . "\n"
        . $sql_limit_to_append . $analyzed_sql[0]['section_after_limit'];
}


/**
 * Get column name from a drop SQL statement
 *
 * @param string $sql SQL query
 *
 * @return string $drop_column Name of the column
 */
function PMA_getColumnNameInColumnDropSql($sql)
{
    $tmpArray1 = explode('DROP', $sql);
    $str_to_check = trim($tmpArray1[1]);

    if (stripos($str_to_check, 'COLUMN') !== false) {
        $tmpArray2 = explode('COLUMN', $str_to_check);
        $str_to_check = trim($tmpArray2[1]);
    }

    $tmpArray3 = explode(' ', $str_to_check);
    $str_to_check = trim($tmpArray3[0]);

    $drop_column = str_replace(';', '', trim($str_to_check));
    $drop_column = str_replace('`', '', $drop_column);

    return $drop_column;
}

/**
 * Verify whether the result set contains all the columns
 * of at least one unique key
 *
 * @param string $db          database name
 * @param string $table       table name
 * @param string $fields_meta meta fields
 *
 * @return boolean whether the result set contains a unique key
 */
function PMA_resultSetContainsUniqueKey($db, $table, $fields_meta)
{
    $resultSetColumnNames = array();
    foreach ($fields_meta as $oneMeta) {
        $resultSetColumnNames[] = $oneMeta->name;
    }
    foreach (PMA_Index::getFromTable($table, $db) as $index) {
        if ($index->isUnique()) {
            $indexColumns = $index->getColumns();
            $numberFound = 0;
            foreach ($indexColumns as $indexColumnName => $dummy) {
                if (in_array($indexColumnName, $resultSetColumnNames)) {
                    $numberFound++;
                }
            }
            if ($numberFound == count($indexColumns)) {
                return true;
            }
        }
    }
    return false;
}

/**
 * Get the HTML for relational column dropdown
 * During grid edit, if we have a relational field, returns the html for the
 * dropdown
 *
 * @param string $db         current database
 * @param string $table      current table
 * @param string $column     current column
 * @param string $curr_value current selected value
 *
 * @return string $dropdown html for the dropdown
 */
function PMA_getHtmlForRelationalColumnDropdown($db, $table, $column, $curr_value)
{
    $foreigners = PMA_getForeigners($db, $table, $column);

    $display_field = PMA_getDisplayField(
        $foreigners[$column]['foreign_db'],
        $foreigners[$column]['foreign_table']
    );

    $foreignData = PMA_getForeignData($foreigners, $column, false, '', '');

    if ($foreignData['disp_row'] == null) {
        //Handle the case when number of values
        //is more than $cfg['ForeignKeyMaxLimit']
        $_url_params = array(
                'db' => $db,
                'table' => $table,
                'field' => $column
        );

        $dropdown = '<span class="curr_value">'
            . htmlspecialchars($_REQUEST['curr_value'])
            . '</span>'
            . '<a href="browse_foreigners.php'
            . PMA_generate_common_url($_url_params) . '"'
            . ' target="_blank" class="browse_foreign" ' .'>'
            . __('Browse foreign values')
            . '</a>';
    } else {
        $dropdown = PMA_foreignDropdown(
            $foreignData['disp_row'],
            $foreignData['foreign_field'],
            $foreignData['foreign_display'],
            $curr_value,
            $GLOBALS['cfg']['ForeignKeyMaxLimit']
        );
        $dropdown = '<select>' . $dropdown . '</select>';
    }

    return $dropdown;
}

/**
 * Get the HTML for the header of the page in print view
 *
 * @param string $db        current database
 * @param string $sql_query current sql query
 * @param int    $num_rows  the number of rows in result
 *
 * @return string $header html for the header
 */
function PMA_getHtmlForPrintViewHeader($db, $sql_query, $num_rows)
{
    $hostname = '';
    if ( $GLOBALS['cfg']['Server']['verbose']) {
        $hostname =  $GLOBALS['cfg']['Server']['verbose'];
    } else {
        $hostname =  $GLOBALS['cfg']['Server']['host'];
        if (! empty( $GLOBALS['cfg']['Server']['port'])) {
            $hostname .=  $GLOBALS['cfg']['Server']['port'];
        }
    }

    $versions  = "phpMyAdmin&nbsp;" . PMA_VERSION;
    $versions .= "&nbsp;/&nbsp;";
    $versions .= "MySQL&nbsp;" . PMA_MYSQL_STR_VERSION;

    $header = '';
    $header .= "<h1>" . __('SQL result') . "</h1>";
    $header .= "<p>";
    $header .= "<strong>" . __('Host:') . "</strong> $hostname<br />";
    $header .= "<strong>" . __('Database:') . "</strong> "
        . htmlspecialchars($db) . "<br />";
    $header .= "<strong>" . __('Generation Time:') . "</strong> "
        . PMA_Util::localisedDate() . "<br />";
    $header .= "<strong>" . __('Generated by:') . "</strong> $versions<br />";
    $header .= "<strong>" . __('SQL query:') . "</strong> "
        . htmlspecialchars($sql_query) . ";";
    if (isset($num_rows)) {
        $header .= "<br />";
        $header .= "<strong>" . __('Rows:') . "</strong> $num_rows";
    }
    $header .= "</p>";

    return $header;
}

/**
 * Get the HTML for the profiling table and accompanying chart
 *
 * @param string $url_query         the url query
 * @param string $pma_token         the pma token
 * @param array  $profiling_results array containing the profiling info
 *
 * @return string $profiling_table html for the profiling table and chart
 */
function PMA_getHtmlForProfilingChart($url_query, $pma_token, $profiling_results)
{
    $profiling_stats = array(
        'total_time' => 0,
        'states' => array(),
    );
    $profiling_table = '';

    $profiling_table .= '<fieldset><legend>' . __('Profiling') . '</legend>' . "\n";
    $profiling_table .= '<div style="float: left;">';
    $profiling_table .= '<h3>' . __('Detailed profile') . '</h3>';
    $profiling_table .= '<table id="profiletable"><thead>' . "\n";
    $profiling_table .= ' <tr>' . "\n";
    $profiling_table .= '  <th>' . __('Order')
        . '<div class="sorticon"></div></th>' . "\n";
    $profiling_table .= '  <th>' . __('State')
        . PMA_Util::showMySQLDocu(
            'general-thread-states', 'general-thread-states'
        )
        . '<div class="sorticon"></div></th>' . "\n";
    $profiling_table .= '  <th>' . __('Time')
        . '<div class="sorticon"></div></th>' . "\n";
    $profiling_table .= ' </tr></thead><tbody>' . "\n";

    $chart_json = Array();
    $i          = 1;
    foreach ($profiling_results as $one_result) {
        if (isset($profiling_stats['states'][ucwords($one_result['Status'])])) {
            $profiling_stats['states'][ucwords($one_result['Status'])]['time']
                += $one_result['Duration'];
            $profiling_stats['states'][ucwords($one_result['Status'])]['calls']++;
        } else {
            $profiling_stats['states'][ucwords($one_result['Status'])] = array(
                'total_time' => $one_result['Duration'],
                'calls' => 1,
            );
        }
        $profiling_stats['total_time'] += $one_result['Duration'];

        $profiling_table .= ' <tr>' . "\n";
        $profiling_table .= '<td>' . $i++ . '</td>' . "\n";
        $profiling_table .= '<td>' . ucwords($one_result['Status']) . '</td>' . "\n";
        $profiling_table .= '<td class="right">'
            . (PMA_Util::formatNumber($one_result['Duration'], 3, 1))
            . 's<span style="display:none;" class="rawvalue">'
            . $one_result['Duration'] . '</span></td>' . "\n";
        if (isset($chart_json[ucwords($one_result['Status'])])) {
            $chart_json[ucwords($one_result['Status'])]
                += $one_result['Duration'];
        } else {
            $chart_json[ucwords($one_result['Status'])]
                = $one_result['Duration'];
        }
    }

    $profiling_table .= '</tbody></table>' . "\n";
    $profiling_table .= '</div>';

    $profiling_table .= '<div style="float: left; margin-left:10px;">';
    $profiling_table .= '<h3>' . __('Summary by state') . '</h3>';
    $profiling_table .= '<table id="profilesummarytable"><thead>' . "\n";
    $profiling_table .= ' <tr>' . "\n";
    $profiling_table .= '  <th>' . __('State')
        . PMA_Util::showMySQLDocu(
            'general-thread-states', 'general-thread-states'
        )
        . '<div class="sorticon"></div></th>' . "\n";
    $profiling_table .= '  <th>' . __('Total Time')
        . '<div class="sorticon"></div></th>' . "\n";
    $profiling_table .= '  <th>' . __('% Time')
        . '<div class="sorticon"></div></th>' . "\n";
    $profiling_table .= '  <th>' . __('Calls')
        . '<div class="sorticon"></div></th>' . "\n";
    $profiling_table .= '  <th>' . __('ø Time')
        . '<div class="sorticon"></div></th>' . "\n";
    $profiling_table .= ' </tr></thead><tbody>' . "\n";
    foreach ($profiling_stats['states'] as $name => $stats) {
        $profiling_table .= ' <tr>' . "\n";
        $profiling_table .= '<td>' . $name . '</td>' . "\n";
        $profiling_table .= '<td align="right">'
            . PMA_Util::formatNumber($stats['total_time'], 3, 1)
            . 's<span style="display:none;" class="rawvalue">'
            . $stats['total_time'] . '</span></td>' . "\n";
        $profiling_table .= '<td align="right">'
            . PMA_Util::formatNumber(
                100 * ($stats['total_time'] / $profiling_stats['total_time']), 0, 2
            )
            . '%</td>' . "\n";
        $profiling_table .= '<td align="right">' . $stats['calls'] . '</td>' . "\n";
        $profiling_table .= '<td align="right">'
            . PMA_Util::formatNumber($stats['total_time'] / $stats['calls'], 3, 1)
            . 's<span style="display:none;" class="rawvalue">'
            . number_format($stats['total_time'] / $stats['calls'], 8, '.', '')
            . '</span></td>' . "\n";
        $profiling_table .= ' </tr>' . "\n";
    }

    $profiling_table .= '</tbody></table>' . "\n";

    $profiling_table .= <<<EOT
<script type="text/javascript">
    pma_token = '$pma_token';
    url_query = '$url_query';
</script>
EOT;
    $profiling_table .= "</div>";

    //require_once 'libraries/chart.lib.php';
    $profiling_table .= '<div id="profilingChartData" style="display:none;">';
    $profiling_table .= json_encode($chart_json);
    $profiling_table .= '</div>';
    $profiling_table .= '<div id="profilingchart" style="display:none;">';
    $profiling_table .= '</div>';
    $profiling_table .= '<script type="text/javascript">';
    $profiling_table .= 'makeProfilingChart();';
    $profiling_table .= 'initProfilingTables();';
    $profiling_table .= '</script>';
    $profiling_table .= '</fieldset>' . "\n";

    return $profiling_table;
}

/**
 * Get the HTML for the enum column dropdown
 * During grid edit, if we have a enum field, returns the html for the
 * dropdown
 *
 * @param string $db         current database
 * @param string $table      current table
 * @param string $column     current column
 * @param string $curr_value currently selected value
 *
 * @return string $dropdown html for the dropdown
 */
function PMA_getHtmlForEnumColumnDropdown($db, $table, $column, $curr_value)
{
    $values = PMA_getValuesForColumn($db, $table, $column);
    $dropdown = '<option value="">&nbsp;</option>';
    $dropdown .= PMA_getHtmlForOptionsList($values, array($curr_value));
    $dropdown = '<select>' . $dropdown . '</select>';
    return $dropdown;
}

/**
 * Get the HTML for the set column dropdown
 * During grid edit, if we have a set field, returns the html for the
 * dropdown
 *
 * @param string $db         current database
 * @param string $table      current table
 * @param string $column     current column
 * @param string $curr_value currently selected value
 *
 * @return string $dropdown html for the set column
 */
function PMA_getHtmlForSetColumn($db, $table, $column, $curr_value)
{
    $values = PMA_getValuesForColumn($db, $table, $column);
    $dropdown = '';

    //converts characters of $curr_value to HTML entities
    $converted_curr_value = htmlentities(
        $curr_value, ENT_COMPAT, "UTF-8"
    );

    $selected_values = explode(',', $converted_curr_value);
    $dropdown .= PMA_getHtmlForOptionsList($values, $selected_values);

    $select_size = (sizeof($values) > 10) ? 10 : sizeof($values);
    $dropdown = '<select multiple="multiple" size="' . $select_size . '">'
        . $dropdown . '</select>';

    return $dropdown;
}

/**
 * Get all the values for a enum column or set column in a table
 *
 * @param string $db     current database
 * @param string $table  current table
 * @param string $column current column
 *
 * @return array $values array containing the value list for the column
 */
function PMA_getValuesForColumn($db, $table, $column)
{
    $field_info_query = $GLOBALS['dbi']->getColumnsSql($db, $table, $column);

    $field_info_result = $GLOBALS['dbi']->fetchResult(
        $field_info_query, null, null, null, PMA_DatabaseInterface::QUERY_STORE
    );

    $values = PMA_Util::parseEnumSetValues($field_info_result[0]['Type']);

    return $values;
}

/**
 * Get HTML for options list
 *
 * @param array $values          set of values
 * @param array $selected_values currently selected values
 *
 * @return string $options HTML for options list
 */
function PMA_getHtmlForOptionsList($values, $selected_values)
{
    $options = '';
    foreach ($values as $value) {
        $options .= '<option value="' . $value . '"';
        if (in_array($value, $selected_values, true)) {
            $options .= ' selected="selected" ';
        }
        $options .= '>' . $value . '</option>';
    }
    return $options;
}

/**
 * Get HTML for the Bookmark form
 *
 * @param string $db            the current database
 * @param string $goto          goto page url
 * @param string $bkm_sql_query the query to be bookmarked
 * @param string $bkm_user      the user creating the bookmark
 *
 * @return void
 */
function PMA_getHtmlForBookmark($db, $goto, $bkm_sql_query, $bkm_user)
{
    $html = '<form action="sql.php" method="post"'
        . ' onsubmit="return ! emptyFormElements(this, \'bkm_fields[bkm_label]\');"'
        . ' id="bookmarkQueryForm">';
    $html .= PMA_generate_common_hidden_inputs();
    $html .= '<input type="hidden" name="goto" value="' . $goto . '" />';
    $html .= '<input type="hidden" name="bkm_fields[bkm_database]"'
        . ' value="' . htmlspecialchars($db) . '" />';
    $html .= '<input type="hidden" name="bkm_fields[bkm_user]"'
        . ' value="' . $bkm_user . '" />';
    $html .= '<input type="hidden" name="bkm_fields[bkm_sql_query]"' . ' value="'
        . $bkm_sql_query
        . '" />';
    $html .= '<fieldset>';
    $html .= '<legend>';
    $html .= PMA_Util::getIcon(
        'b_bookmark.png', __('Bookmark this SQL query'), true
    );
    $html .= '</legend>';
    $html .= '<div class="formelement">';
    $html .= '<label for="fields_label_">' . __('Label:') . '</label>';
    $html .= '<input type="text" id="fields_label_"'
        . ' name="bkm_fields[bkm_label]" value="" />';
    $html .= '</div>';
    $html .= '<div class="formelement">';
    $html .= '<input type="checkbox" name="bkm_all_users"'
        . ' id="bkm_all_users" value="true" />';
    $html .= '<label for="bkm_all_users">'
        . __('Let every user access this bookmark')
        . '</label>';
    $html .= '</div>';
    $html .= '<div class="clearfloat"></div>';
    $html .= '</fieldset>';
    $html .= '<fieldset class="tblFooters">';
    $html .= '<input type="hidden" name="store_bkm" value="1" />';
    $html .= '<input type="submit"'
        . ' value="' . __('Bookmark this SQL query') . '" />';
    $html .= '</fieldset>';
    $html .= '</form>';

    return $html;
}

/**
 * Function to check whether to remember the sorting order or not
 *
 * @param array $analyzed_sql_results the analyzed query and other varibles set
 *                                    after analyzing the query
 *
 * @return boolean
 */
function PMA_isRememberSortingOrder($analyzed_sql_results)
{
    $select_from = isset(
        $analyzed_sql_results['analyzed_sql'][0]['queryflags']['select_from']
    );
    if ($GLOBALS['cfg']['RememberSorting']
        && ! ($analyzed_sql_results['is_count']
        || $analyzed_sql_results['is_export']
        || $analyzed_sql_results['is_func']
        || $analyzed_sql_results['is_analyse'])
        && isset($analyzed_sql_results['analyzed_sql'][0]['select_expr'])
        && (count($analyzed_sql_results['analyzed_sql'][0]['select_expr']) == 0)
        && $select_from
        && count($analyzed_sql_results['analyzed_sql'][0]['table_ref']) == 1
    ) {
        return true;
    } else {
        return false;
    }
}

/**
 * Function to check whether the LIMIT clause should be appended or not
 *
 * @param array $analyzed_sql_results the analyzed query and other varibles set
 *                                    after analyzing the query
 *
 * @return boolean
 */
function PMA_isAppendLimitClause($analyzed_sql_results)
{
    $select_from = isset(
        $analyzed_sql_results['analyzed_sql'][0]['queryflags']['select_from']
    );
    if (($_SESSION['tmp_user_values']['max_rows'] != 'all')
        && ! ($analyzed_sql_results['is_count']
        || $analyzed_sql_results['is_export']
        || $analyzed_sql_results['is_func']
        || $analyzed_sql_results['is_analyse'])
        && $select_from
        && ! isset($analyzed_sql_results['analyzed_sql'][0]['queryflags']['offset'])
        && empty($analyzed_sql_results['analyzed_sql'][0]['limit_clause'])
    ) {
        return true;
    } else {
        return false;
    }
}

/**
 * Function to check whether this query is for just browsing
 *
 * @param array   $analyzed_sql_results the analyzed query and other varibles set
 *                                      after analyzing the query
 * @param boolean $find_real_end        whether the real end should be found
 *
 * @return boolean
 */
function PMA_isJustBrowsing($analyzed_sql_results, $find_real_end)
{
    $distinct = isset(
        $analyzed_sql_results['analyzed_sql'][0]['queryflags']['distinct']
    );

    $table_name = isset(
        $analyzed_sql_results['analyzed_sql'][0]['table_ref'][1]['table_name']
    );
    if (! $analyzed_sql_results['is_group']
        && ! isset($analyzed_sql_results['analyzed_sql'][0]['queryflags']['union'])
        && ! $distinct
        && ! $table_name
        && (empty($analyzed_sql_results['analyzed_sql'][0]['where_clause'])
        || $analyzed_sql_results['analyzed_sql'][0]['where_clause'] == '1 ')
        && ! isset($find_real_end)
    ) {
        return true;
    } else {
        return false;
    }
}

/**
 * Function to check whether the reated transformation information shoul be deleted
 *
 * @param array $analyzed_sql_results the analyzed query and other varibles set
 *                                    after analyzing the query
 *
 * @return boolean
 */
function PMA_isDeleteTransformationInfo($analyzed_sql_results)
{
    if (!empty($analyzed_sql_results['analyzed_sql'][0]['querytype'])
        && (($analyzed_sql_results['analyzed_sql'][0]['querytype'] == 'ALTER')
        || ($analyzed_sql_results['analyzed_sql'][0]['querytype'] == 'DROP'))
    ) {
        return true;
    } else {
        return false;
    }
}

/**
 * Function to check whether the user has rights to drop the database
 *
 * @param array   $analyzed_sql_results  the analyzed query and other varibles set
 *                                       after analyzing the query
 * @param boolean $allowUserDropDatabase whether the user is allowed to drop db
 * @param boolean $is_superuser          whether this user is a superuser
 *
 * @return boolean
 */
function PMA_hasNoRightsToDropDatabase($analyzed_sql_results,
    $allowUserDropDatabase, $is_superuser
) {
    if (! defined('PMA_CHK_DROP')
        && ! $allowUserDropDatabase
        && isset ($analyzed_sql_results['drop_database'])
        && $analyzed_sql_results['drop_database'] == 1
        && ! $is_superuser
    ) {
        return true;
    } else {
        return false;
    }
}

/**
 * Function to set the column order
 *
 * @param PMA_Table $pmatable PMA_Table instance
 *
 * @return boolean $retval
 */
function PMA_setColumnOrder($pmatable)
{
    $col_order = explode(',', $_REQUEST['col_order']);
    $retval = $pmatable->setUiProp(
        PMA_Table::PROP_COLUMN_ORDER,
        $col_order,
        $_REQUEST['table_create_time']
    );
    if (gettype($retval) != 'boolean') {
        $response = PMA_Response::getInstance();
        $response->isSuccess(false);
        $response->addJSON('message', $retval->getString());
        exit;
    }

    return $retval;
}

/**
 * Function to set the column visibility
 *
 * @param PMA_Table $pmatable PMA_Table instance
 *
 * @return boolean $retval
 */
function PMA_setColumnVisibility($pmatable)
{
    $col_visib = explode(',', $_REQUEST['col_visib']);
    $retval = $pmatable->setUiProp(
        PMA_Table::PROP_COLUMN_VISIB, $col_visib,
        $_REQUEST['table_create_time']
    );
    if (gettype($retval) != 'boolean') {
        $response = PMA_Response::getInstance();
        $response->isSuccess(false);
        $response->addJSON('message', $retval->getString());
        exit;
    }
    return $retval;
}

/**
 * Function to check the request for setting the column order or visibility
 *
 * @param String $table the current table
 * @param String $db    the current database
 *
 * @return void
 */
function PMA_setColumnOrderOrVisibility($table, $db)
{
    $pmatable = new PMA_Table($table, $db);
    $retval = false;

    // set column order
    if (isset($_REQUEST['col_order'])) {
        $retval = PMA_setColumnOrder($pmatable);
    }

    // set column visibility
    if ($retval === true && isset($_REQUEST['col_visib'])) {
        $retval = PMA_setColumnVisibility($pmatable);
    }

    $response = PMA_Response::getInstance();
    $response->isSuccess($retval == true);
    exit;
}

/**
 * Function to add a bookmark
 *
 * @param String $pmaAbsoluteUri absolute URI
 * @param String $goto           goto page URL
 *
 * @return void
 */
function PMA_addBookmark($pmaAbsoluteUri, $goto)
{
    $result = PMA_Bookmark_save(
        $_POST['bkm_fields'],
        (isset($_POST['bkm_all_users'])
            && $_POST['bkm_all_users'] == 'true' ? true : false
        )
    );
    $response = PMA_Response::getInstance();
    if ($response->isAjax()) {
        if ($result) {
            $msg = PMA_message::success(__('Bookmark %s created'));
            $msg->addParam($_POST['bkm_fields']['bkm_label']);
            $response->addJSON('message', $msg);
        } else {
            $msg = PMA_message::error(__('Bookmark not created'));
            $response->isSuccess(false);
            $response->addJSON('message', $msg);
        }
        exit;
    } else {
        // go back to sql.php to redisplay query; do not use &amp; in this case:
        PMA_sendHeaderLocation(
            $pmaAbsoluteUri . $goto
            . '&label=' . $_POST['bkm_fields']['bkm_label']
        );
    }
}

/**
 * Function to find the real end of rows
 *
 * @param String $db    the current database
 * @param String $table the current table
 *
 * @return mixed the number of rows if "retain" param is true, otherwise true
 */
function PMA_findRealEndOfRows($db, $table)
{
    $unlim_num_rows = PMA_Table::countRecords($db, $table, true);
    $_SESSION['tmp_user_values']['pos'] = @((ceil(
        $unlim_num_rows / $_SESSION['tmp_user_values']['max_rows']
    ) - 1) * $_SESSION['tmp_user_values']['max_rows']);

    return $unlim_num_rows;
}

/**
 * Function to get values for the relational columns
 *
 * @param String $db            the current database
 * @param String $table         the current table
 * @param String $display_field display field
 *
 * @return void
 */
function PMA_getRelationalValues($db, $table, $display_field)
{
    $column = $_REQUEST['column'];
    if ($_SESSION['tmp_user_values']['relational_display'] == 'D'
        && isset($display_field)
        && strlen($display_field)
        && isset($_REQUEST['relation_key_or_display_column'])
        && $_REQUEST['relation_key_or_display_column']
    ) {
        $curr_value = $_REQUEST['relation_key_or_display_column'];
    } else {
        $curr_value = $_REQUEST['curr_value'];
    }
    $dropdown = PMA_getHtmlForRelationalColumnDropdown(
        $db, $table, $column, $curr_value
    );
    $response = PMA_Response::getInstance();
    $response->addJSON('dropdown', $dropdown);
    exit;
}

/**
 * Function to get values for Enum or Set Columns
 *
 * @param String $db         the current database
 * @param String $table      the current table
 * @param String $columnType whether enum or set
 *
 * @return void
 */
function PMA_getEnumOrSetValues($db, $table, $columnType)
{
    $column = $_REQUEST['column'];
    $curr_value = $_REQUEST['curr_value'];
    $response = PMA_Response::getInstance();
    if ($columnType == "enum") {
        $dropdown = PMA_getHtmlForEnumColumnDropdown(
            $db, $table, $column, $curr_value
        );
        $response->addJSON('dropdown', $dropdown);
    } else {
        $select = PMA_getHtmlForSetColumn($db, $table, $column, $curr_value);
        $response->addJSON('select', $select);
    }
    exit;
}

/**
 * Function to append the limit clause
 *
 * @param String $full_sql_query full sql query
 * @param array  $analyzed_sql   analyzed sql query
 * @param String $display_query  display query
 *
 * @return array
 */
function PMA_appendLimitClause($full_sql_query, $analyzed_sql, $display_query)
{
    $sql_limit_to_append = ' LIMIT ' . $_SESSION['tmp_user_values']['pos']
        . ', ' . $_SESSION['tmp_user_values']['max_rows'] . " ";
    $full_sql_query = PMA_getSqlWithLimitClause(
        $full_sql_query,
        $analyzed_sql,
        $sql_limit_to_append
    );

    /**
     * @todo pretty printing of this modified query
     */
    if ($display_query) {
        // if the analysis of the original query revealed that we found
        // a section_after_limit, we now have to analyze $display_query
        // to display it correctly

        if (! empty($analyzed_sql[0]['section_after_limit'])
            && trim($analyzed_sql[0]['section_after_limit']) != ';'
        ) {
            $analyzed_display_query = PMA_SQP_analyze(
                PMA_SQP_parse($display_query)
            );
            $display_query  = $analyzed_display_query[0]['section_before_limit']
                . "\n" . $sql_limit_to_append
                . $analyzed_display_query[0]['section_after_limit'];
        }
    }

    return array($sql_limit_to_append, $full_sql_query, isset(
        $analyzed_display_query)
        ? $analyzed_display_query : null,
        isset($display_query) ? $display_query : null
    );
}

/**
 * Function to get the default sql query for browsing page
 *
 * @param String $db    the current database
 * @param String $table the current table
 *
 * @return String $sql_query the default $sql_query for browse page
 */
function PMA_getDefaultSqlQueryForBrowse($db, $table)
{
    include_once 'libraries/bookmark.lib.php';
    $book_sql_query = PMA_Bookmark_get(
        $db,
        '\'' . PMA_Util::sqlAddSlashes($table) . '\'',
        'label',
        false,
        true
    );

    if (! empty($book_sql_query)) {
        $GLOBALS['using_bookmark_message'] = PMA_message::notice(
            __('Using bookmark "%s" as default browse query.')
        );
        $GLOBALS['using_bookmark_message']->addParam($table);
        $GLOBALS['using_bookmark_message']->addMessage(
            PMA_Util::showDocu('faq', 'faq6-22')
        );
        $sql_query = $book_sql_query;
    } else {
        $sql_query = 'SELECT * FROM ' . PMA_Util::backquote($table);
    }
    unset($book_sql_query);

    return $sql_query;
}

/**
 * Responds an error when an error happens when executing the query
 *
 * @param boolean $is_gotofile whether goto file or not
 * @param String  $error       error after executing the query
 *
 * @return void
 */
function PMA_handleQueryExecuteError($is_gotofile, $error)
{
    if ($is_gotofile) {
        $message = PMA_Message::rawError($error);
        $response = PMA_Response::getInstance();
        $response->isSuccess(false);
        $response->addJSON('message', $message);
        exit;
    }
}

/**
 * Function to store the query as a bookmark
 *
 * @param String  $db          the current database
 * @param String  $bkm_user    the bookmarking user
 * @param String  $import_text import text
 * @param String  $bkm_label   bookmark label
 * @param boolean $bkm_replace whether to replace existing bookmarks
 *
 * @return void
 */
function PMA_storeTheQueryAsBookmark($db, $bkm_user, $import_text,
    $bkm_label, $bkm_replace
) {
    include_once 'libraries/bookmark.lib.php';
    $bfields = array(
                 'bkm_database' => $db,
                 'bkm_user'  => $bkm_user,
                 'bkm_sql_query' => urlencode($import_text),
                 'bkm_label' => $bkm_label
    );

    // Should we replace bookmark?
    if (isset($bkm_replace)) {
        $bookmarks = PMA_Bookmark_getList($db);
        foreach ($bookmarks as $key => $val) {
            if ($val == $bkm_label) {
                PMA_Bookmark_delete($db, $key);
            }
        }
    }

    PMA_Bookmark_save($bfields, isset($_POST['bkm_all_users']));

}

/**
 * Function to execute the SQL query and set the execution time
 *
 * @param String $full_sql_query the full sql query
 *
 * @return mixed  $result the results after running the query
 */
function PMA_executeQueryAndStoreResults($full_sql_query)
{
    // Measure query time.
    $querytime_before = array_sum(explode(' ', microtime()));

    $result = @$GLOBALS['dbi']->tryQuery(
        $full_sql_query, null, PMA_DatabaseInterface::QUERY_STORE
    );
    $querytime_after = array_sum(explode(' ', microtime()));

    $GLOBALS['querytime'] = $querytime_after - $querytime_before;

    // If a stored procedure was called, there may be more results that are
    // queued up and waiting to be flushed from the buffer. So let's do that.
    do {
        $GLOBALS['dbi']->storeResult();
        if (! $GLOBALS['dbi']->moreResults()) {
            break;
        }
    } while ($GLOBALS['dbi']->nextResult());

    return $result;
}

/**
 * Function to get the affected or changed number of rows after executing a query
 *
 * @param boolean $is_affected whether the query affected a table
 * @param mixed   $result      results of executing the query
 * @param int     $num_rows    number of rows affected or changed
 *
 * @return int    $num_rows    number of rows affected or changed
 */
function PMA_getNumberOfRowsAffectedOrChanged($is_affected, $result, $num_rows)
{
    if (! $is_affected) {
        $num_rows = ($result) ? @$GLOBALS['dbi']->numRows($result) : 0;
    } elseif (! isset($num_rows)) {
        $num_rows = @$GLOBALS['dbi']->affectedRows();
    }

    return $num_rows;
}

/**
 * Checks if the current database has changed
 * This could happen if the user sends a query like "USE `database`;"
 *
 * @param String $db the database in the query
 *
 * @return int $reload whether to reload the navigation(1) or not(0)
 */
function PMA_hasCurrentDbChanged($db)
{
    // Checks if the current database has changed
    // This could happen if the user sends a query like "USE `database`;"
    $reload = 0;
    if (strlen($db)) {
        $current_db = $GLOBALS['dbi']->fetchValue('SELECT DATABASE()');
        if ($db !== $current_db) {
            $reload = 1;
        }
        $GLOBALS['dbi']->selectDb($db);
    }

    return $reload;
}

/**
 * If a table, database or column gets dropped, clean comments.
 *
 * @param String $db             current database
 * @param String $table          current table
 * @param String $dropped_column dropped column if any
 * @param bool   $purge
 * @param array  $extra_data
 *
 * @return array $extra_data
 */
function PMA_cleanupRelations($db, $table, $dropped_column, $purge, $extra_data)
{
    include_once 'libraries/relation_cleanup.lib.php';

    if (isset($purge) && $purge == 1) {
        if (strlen($table) && strlen($db)) {
            PMA_relationsCleanupTable($db, $table);
        } elseif (strlen($db)) {
            PMA_relationsCleanupDatabase($db);
        }
    }

    if (isset($dropped_column)
        && !empty($dropped_column)
        && strlen($db)
        && strlen($table)
    ) {
        PMA_relationsCleanupColumn($db, $table, $dropped_column);
        if (isset($extra_data)) {
            // to refresh the list of indexes (Ajax mode)
            $extra_data['indexes_list'] = PMA_Index::getView($table, $db);
        }
    }

    return $extra_data;
}

/**
 * Function to count the total number of rows for the same 'SELECT' query without
 * the 'LIMIT' clause that may have been programatically added
 *
 * @param int    $num_rows             number of rows affected/changed by the query
 * @param bool   $is_select            whether the query is SELECT or not
 * @param bool   $justBrowsing         whether just browsing or not
 * @param string $db                   the current database
 * @param string $table                the current table
 * @param array  $parsed_sql           parsed sql
 * @param array  $analyzed_sql_results the analyzed query and other varibles set
 *                                     after analyzing the query
 *
 * @return int $unlim_num_rows unlimited number of rows
 */
function PMA_countQueryResults($num_rows, $is_select, $justBrowsing,
    $db, $table, $parsed_sql, $analyzed_sql_results
) {
    if (!PMA_isAppendLimitClause($analyzed_sql_results)) {
        // if we did not append a limit, set this to get a correct
        // "Showing rows..." message
        // $_SESSION['tmp_user_values']['max_rows'] = 'all';
        $unlim_num_rows         = $num_rows;
    } elseif ($is_select) {
        //    c o u n t    q u e r y

        // If we are "just browsing", there is only one table,
        // and no WHERE clause (or just 'WHERE 1 '),
        // we do a quick count (which uses MaxExactCount) because
        // SQL_CALC_FOUND_ROWS is not quick on large InnoDB tables

        // However, do not count again if we did it previously
        // due to $find_real_end == true
        if ($justBrowsing) {
            $unlim_num_rows = PMA_Table::countRecords(
                $db,
                $table,
                true
            );

        } else {
            // add select expression after the SQL_CALC_FOUND_ROWS

            // for UNION, just adding SQL_CALC_FOUND_ROWS
            // after the first SELECT works.

            // take the left part, could be:
            // SELECT
            // (SELECT

            $analyzed_sql = $analyzed_sql_results['analyzed_sql'];

            $count_query = PMA_SQP_format(
                $parsed_sql,
                'query_only',
                0,
                $analyzed_sql[0]['position_of_first_select'] + 1
            );
            $count_query .= ' SQL_CALC_FOUND_ROWS ';
            // add everything that was after the first SELECT
            $count_query .= PMA_SQP_format(
                $parsed_sql,
                'query_only',
                $analyzed_sql[0]['position_of_first_select'] + 1
            );
            // ensure there is no semicolon at the end of the
            // count query because we'll probably add
            // a LIMIT 1 clause after it
            $count_query = rtrim($count_query);
            $count_query = rtrim($count_query, ';');

            // if using SQL_CALC_FOUND_ROWS, add a LIMIT to avoid
            // long delays. Returned count will be complete anyway.
            // (but a LIMIT would disrupt results in an UNION)

            if (! isset($analyzed_sql[0]['queryflags']['union'])) {
                $count_query .= ' LIMIT 1';
            }

            // run the count query

            $GLOBALS['dbi']->tryQuery($count_query);
            // if (mysql_error()) {
            // void.
            // I tried the case
            // (SELECT `User`, `Host`, `Db`, `Select_priv` FROM `db`)
            // UNION (SELECT `User`, `Host`, "%" AS "Db",
            // `Select_priv`
            // FROM `user`) ORDER BY `User`, `Host`, `Db`;
            // and although the generated count_query is wrong
            // the SELECT FOUND_ROWS() work! (maybe it gets the
            // count from the latest query that worked)
            //
            // another case where the count_query is wrong:
            // SELECT COUNT(*), f1 from t1 group by f1
            // and you click to sort on count(*)
            // }
            $unlim_num_rows = $GLOBALS['dbi']->fetchValue('SELECT FOUND_ROWS()');
        } // end else "just browsing"
    } else {// not $is_select
        $unlim_num_rows = 0;
    }

    return $unlim_num_rows;
}

/**
 * Function to handle all aspects relating to executing the query
 *
 * @param array $analyzed_sql_results
 * @param String  $full_sql_query full sql query
 * @param boolean $is_gotofile    whether to go to a file
 * @param String  $db             current database
 * @param String  $table          current table
 * @param boolean $find_real_end  whether to find the real end
 * @param String  $import_text    sql command
 * @param String  $bkm_user       bookmarking user
 *
 * @return mixed
 */
function PMA_executeTheQuery($analyzed_sql_results, $full_sql_query, $is_gotofile,
    $db, $table, $find_real_end, $import_text, $bkm_user, $extra_data
) {
    // Only if we ask to see the php code
    if (isset($GLOBALS['show_as_php']) || ! empty($GLOBALS['validatequery'])) {
        $result = null;
        $num_rows = 0;
        $unlim_num_rows = 0;
    } else { // If we don't ask to see the php code
        if (isset($_SESSION['profiling']) && PMA_Util::profilingSupported()) {
            $GLOBALS['dbi']->query('SET PROFILING=1;');
        }

        $result = PMA_executeQueryAndStoreResults($full_sql_query);

        // Displays an error message if required and stop parsing the script
        $error = $GLOBALS['dbi']->getError();
        if ($error) {
            PMA_handleQueryExecuteError($is_gotofile, $error);
        }

        // If there are no errors and bookmarklabel was given,
        // store the query as a bookmark
        if (! empty($_POST['bkm_label']) && ! empty($import_text)) {
            PMA_storeTheQueryAsBookmark($db, $bkm_user,
                $import_text, $_POST['bkm_label'],
                isset($_POST['bkm_replace']) ? $_POST['bkm_replace'] : null
            );
        } // end store bookmarks

        // Gets the number of rows affected/returned
        // (This must be done immediately after the query because
        // mysql_affected_rows() reports about the last query done)
        $num_rows = PMA_getNumberOfRowsAffectedOrChanged(
            $analyzed_sql_results['is_affected'], $result,
            isset($num_rows) ? $num_rows : null
        );

        // Grabs the profiling results
        if (isset($_SESSION['profiling']) && PMA_Util::profilingSupported()) {
            $profiling_results = $GLOBALS['dbi']->fetchResult('SHOW PROFILE;');
        }

        $justBrowsing = PMA_isJustBrowsing(
            $analyzed_sql_results, isset($find_real_end) ? $find_real_end : null
        );

        $unlim_num_rows = PMA_countQueryResults($num_rows,
            $analyzed_sql_results['is_select'], $justBrowsing, $db,
            $table, $analyzed_sql_results['parsed_sql'], $analyzed_sql_results
        );

        $extra_data = PMA_cleanupRelations(
            isset($db) ? $db : '', isset($table) ? $table : '',
            isset($_REQUEST['dropped_column']) ? $_REQUEST['dropped_column'] : null,
            isset($_REQUEST['purge']) ? $_REQUEST['purge'] : null,
            isset($extra_data) ? $extra_data : null
        );
    }

    return array($result, $num_rows, $unlim_num_rows,
        isset($profiling_results) ? $profiling_results : null,
        isset($justBrowsing) ? $justBrowsing : null, $extra_data
    );
}
/**
 * Delete related tranformatioinformationn information
 *
 * @param String $db           current database
 * @param String $table        current table
 * @param array  $analyzed_sql analyzed sql query
 *
 * @return void
 */
function PMA_deleteTransformationInfo($db, $table, $analyzed_sql)
{
    include_once 'libraries/transformations.lib.php';
    if ($analyzed_sql[0]['querytype'] == 'ALTER') {
        if (stripos($analyzed_sql[0]['unsorted_query'], 'DROP') !== false) {
            $drop_column = PMA_getColumnNameInColumnDropSql(
                $analyzed_sql[0]['unsorted_query']
            );

            if ($drop_column != '') {
                PMA_clearTransformations($db, $table, $drop_column);
            }
        }

    } else if (($analyzed_sql[0]['querytype'] == 'DROP') && ($table != '')) {
        PMA_clearTransformations($db, $table);
    }
}

/**
 * Function to get the message for the no rows returned case
 *
 * @param string $message_to_show      message to show
 * @param array  $analyzed_sql_results analyzed sql results
 * @param int    $num_rows             number of rows
 *
 * @return string $message
 */
function PMA_getMessageForNoRowsReturned(
    $message_to_show, $analyzed_sql_results, $num_rows
) {
    if ($analyzed_sql_results['is_delete']) {
        $message = PMA_Message::getMessageForDeletedRows($num_rows);
    } elseif ($analyzed_sql_results['is_insert']) {
        if ($analyzed_sql_results['is_replace']) {
            // For replace we get DELETED + INSERTED row count,
            // so we have to call it affected
            $message = PMA_Message::getMessageForAffectedRows($num_rows);
        } else {
            $message = PMA_Message::getMessageForInsertedRows($num_rows);
        }
        $insert_id = $GLOBALS['dbi']->insertId();
        if ($insert_id != 0) {
            // insert_id is id of FIRST record inserted in one insert,
            // so if we inserted multiple rows, we had to increment this
            $message->addMessage('[br]');
            // need to use a temporary because the Message class
            // currently supports adding parameters only to the first
            // message
            $_inserted = PMA_Message::notice(__('Inserted row id: %1$d'));
            $_inserted->addParam($insert_id + $num_rows - 1);
            $message->addMessage($_inserted);
        }
    } elseif ($analyzed_sql_results['is_affected']) {
        $message = PMA_Message::getMessageForAffectedRows($num_rows);

        // Ok, here is an explanation for the !$is_select.
        // The form generated by sql_query_form.lib.php
        // and db_sql.php has many submit buttons
        // on the same form, and some confusion arises from the
        // fact that $message_to_show is sent for every case.
        // The $message_to_show containing a success message and sent with
        // the form should not have priority over errors
    } elseif (! empty($message_to_show) && ! $analyzed_sql_results['is_select']) {
        $message = PMA_Message::rawSuccess(htmlspecialchars($message_to_show));
    } elseif (! empty($GLOBALS['show_as_php'])) {
        $message = PMA_Message::success(__('Showing as PHP code'));
    } elseif (isset($GLOBALS['show_as_php'])) {
        /* User disable showing as PHP, query is only displayed */
        $message = PMA_Message::notice(__('Showing SQL query'));
    } elseif (! empty($GLOBALS['validatequery'])) {
        $message = PMA_Message::notice(__('Validated SQL'));
    } else {
        $message = PMA_Message::success(
            __('MySQL returned an empty result set (i.e. zero rows).')
        );
    }

    if (isset($GLOBALS['querytime'])) {
        $_querytime = PMA_Message::notice('(' . __('Query took %01.4f sec') . ')');
        $_querytime->addParam($GLOBALS['querytime']);
        $message->addMessage($_querytime);
    }

    return $message;
}

/**
 * Function to send the Ajax response when no rows returned
 *
 * @param string $message              message to be send
 * @param array  $analyzed_sql         analyzed sql
 * @param object $displayResultsObject DisplayResult instance
 * @param bool   $showSql              whether to show sql or not
 * @param array  $extra_data           extra data
 *
 * @return void
 */
function PMA_sendAjaxResponseForNoResultsReturned(
    $message, $analyzed_sql, $displayResultsObject, $showSql, $extra_data
) {
    /**
     * @todo find a better way to make getMessage() in Header.class.php
     *       output the intended message
     */
    $GLOBALS['message'] = $message;

    if ($showSql) {
        $extra_data['sql_query'] = PMA_Util::getMessage(
            $message, $GLOBALS['sql_query'], 'success'
        );
    }
    if (isset($GLOBALS['reload']) && $GLOBALS['reload'] == 1) {
        $extra_data['reload'] = 1;
        $extra_data['db'] = $GLOBALS['db'];
    }
    $response = PMA_Response::getInstance();
    $response->isSuccess($message->isSuccess());
    // No need to manually send the message
    // The Response class will handle that automatically
    $query__type = PMA_DisplayResults::QUERY_TYPE_SELECT;
    if ($analyzed_sql[0]['querytype'] == $query__type) {
        $createViewHTML = $displayResultsObject->getCreateViewQueryResultOp(
            $analyzed_sql
        );
        $response->addHTML($createViewHTML.'<br />');
    }

    $response->addJSON(isset($extra_data) ? $extra_data : array());
    if (empty($_REQUEST['ajax_page_request'])) {
        $response->addJSON('message', $message);
        exit;
    }
}

/**
 * Function to respond back when the query returns zero rows
 * This method is called
 * 1-> When browsing an empty table
 * 2-> When executing a query on a non empty table which returns zero results
 * 3-> When executing a query on an empty table
 * 4-> When executing an INSERT, UPDATE, DEDETE query from the SQL  tab
 * 5-> When deleting a row from BROWSE tab
 * 6-> When searching using the SEARCH tab which returns zero results
 * 7-> When changing the structure of the table except change operation
 *
 * @param array  $analyzed_sql_results analyzed sql results
 * @param string $db                   current database
 * @param string $table                current table
 * @param string $message_to_show      message to show
 * @param int    $num_rows             number of rows
 * @param object $displayResultsObject DisplayResult instance
 * @param array  $extra_data           extra data
 * @param array  $cfg                  configuration
 *
 * @return void
 */
function PMA_sendResponseForNoResultsReturned($analyzed_sql_results, $db, $table,
    $message_to_show, $num_rows, $displayResultsObject, $extra_data, $cfg
) {
    if (PMA_isDeleteTransformationInfo($analyzed_sql_results)) {
        PMA_deleteTransformationInfo(
            $db, $table, $analyzed_sql_results['analyzed_sql']
        );
    }

    $message = PMA_getMessageForNoRowsReturned(
        isset($message_to_show) ? $message_to_show : null, $analyzed_sql_results,
        $num_rows
    );
    if ($GLOBALS['is_ajax_request'] == true) {
        PMA_sendAjaxResponseForNoResultsReturned($message,
            $analyzed_sql_results['analyzed_sql'],
            $displayResultsObject, $cfg['ShowSQL'],
            isset($extra_data) ? $extra_data : null
        );
    }
    exit();
}
?>