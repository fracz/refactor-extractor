<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * Functions for the table-search page and zoom-search page
 *
 * @package PhpMyAdmin
 */
if (! defined('PHPMYADMIN')) {
    exit;
}

/**
 * Gets all the fields of a table along with their types, collations
 * and whether null or not.
 *
 * @param string $db    Selected database
 * @param string $table Selected table
 *
 * @return array Array containing the field list, field types, collations
 * and null constraint
 */
function PMA_tbl_getFields($db, $table)
{
    // Gets the list and number of fields
    $columns = PMA_DBI_get_columns($db, $table, null, true);
    $columnNames = $columnNullFlags = $columnTypes = $columnCollations = array();
    $geomColumnFlag = false;
    $geom_types = PMA_getGISDatatypes();

    foreach ($columns as $key => $row) {
        $columnNames[] = $row['Field'];
        $type          = $row['Type'];

        // check whether table contains geometric columns
        if (in_array($type, $geom_types)) {
            $geomColumnFlag = true;
        }

        // reformat mysql query output
        if (strncasecmp($type, 'set', 3) == 0
            || strncasecmp($type, 'enum', 4) == 0
        ) {
            $type = str_replace(',', ', ', $type);
        } else {
            // strip the "BINARY" attribute, except if we find "BINARY(" because
            // this would be a BINARY or VARBINARY field type
            if (! preg_match('@BINARY[\(]@i', $type)) {
                $type = preg_replace('@BINARY@i', '', $type);
            }
            $type = preg_replace('@ZEROFILL@i', '', $type);
            $type = preg_replace('@UNSIGNED@i', '', $type);

            $type = strtolower($type);
        }
        if (empty($type)) {
            $type = '&nbsp;';
        }
        $columnNullFlags[] = $row['Null'];
        $columnTypes[] = $type;
        $columnCollations[]
            = ! empty($row['Collation']) && $row['Collation'] != 'NULL'
            ? $row['Collation']
            : '';
    } // end while

    return array(
         $columnNames,
         $columnTypes,
         $columnCollations,
         $columnNullFlags,
         $geomColumnFlag
    );
}

/**
 * Sets the table header for displaying a table in query-by-example format.
 *
 * @param bool $geomColumnFlag whether a geometry column is present
 *
 * @return HTML content, the tags and content for table header
 */
function PMA_tbl_getTableHeader($geomColumnFlag = false)
{
    // Display the Function column only if there is at least one geometry column
    $func = '';
    if ($geomColumnFlag) {
        $func = '<th>' . __('Function') . '</th>';
    }

    return '<thead>
        <tr>' . $func . '<th>' .  __('Column') . '</th>
        <th>' .  __('Type') . '</th>
        <th>' .  __('Collation') . '</th>
        <th>' .  __('Operator') . '</th>
        <th>' .  __('Value') . '</th>
        </tr>
        </thead>';
}

/**
 * Returns an array with necessary configrations to create
 * sub-tabs(Table Search and Zoom Search) in the table_select page.
 *
 * @return array Array containing configuration (icon, text, link, id, args)
 * of sub-tabs for Table Search and Zoom search
 */
function PMA_tbl_getSubTabs()
{
    $subtabs = array();
    $subtabs['search']['icon'] = 'b_search.png';
    $subtabs['search']['text'] = __('Table Search');
    $subtabs['search']['link'] = 'tbl_select.php';
    $subtabs['search']['id'] = 'tbl_search_id';
    $subtabs['search']['args']['pos'] = 0;

    $subtabs['zoom']['icon'] = 'b_props.png';
    $subtabs['zoom']['link'] = 'tbl_zoom_select.php';
    $subtabs['zoom']['text'] = __('Zoom Search');
    $subtabs['zoom']['id'] = 'zoom_search_id';

    return $subtabs;
}

/**
 * Provides html elements for search criteria inputbox
 * in case the column's type is geometrical
 *
 * @param int  $column_index Column's index
 * @param bool $in_fbs       Whether we are in 'function based search'
 *
 * @return HTML elements.
 */
function PMA_tblSearchGetGeometricalInputBox($column_index, $in_fbs)
{
    $html_output = '<input type="text" name="criteriaValues[' . $column_index . ']"'
        . ' size="40" class="textfield" id="field_' . $column_index . '" />';

    if ($in_fbs) {
        $edit_url = 'gis_data_editor.php?' . PMA_generate_common_url();
        $edit_str = PMA_getIcon('b_edit.png', __('Edit/Insert'));
        $html_output .= '<span class="open_search_gis_editor">';
        $html_output .= PMA_linkOrButton(
            $edit_url, $edit_str, array(), false, false, '_blank'
        );
        $html_output .= '</span>';
    }
    return $html_output;
}

/**
 * Provides html elements for search criteria inputbox
 * in case the column is a Foreign Key
 *
 * @param array  $foreignData         Foreign keys data
 * @param string $field               Column name
 * @param int    $column_index        Column index
 * @param string $db                  Selected database
 * @param string $table               Selected table
 * @param array  $titles              Selected title
 * @param int    $foreignMaxLimit     Max limit of displaying foreign elements
 * @param array  $criteriaValues      Array of search criteria inputs
 * @param string $field_id            Column's inputbox's id
 * @param bool   $in_zoom_search_edit Whether we are in zoom search edit
 *
 * @return HTML elements.
 */
function PMA_tblSearchGetForeignKeyInputBox($foreignData, $field, $column_index,
    $db, $table, $titles, $foreignMaxLimit, $criteriaValues,
    $field_id, $in_zoom_search_edit = false
) {
    $html_output = '';
    if (is_array($foreignData['disp_row'])) {
        $html_output .=  '<select name="criteriaValues[' . $column_index . ']" id="'
            . $field_id . $column_index .'">';
        $html_output .= PMA_foreignDropdown(
            $foreignData['disp_row'], $foreignData['foreign_field'],
            $foreignData['foreign_display'], '', $foreignMaxLimit
        );
        $html_output .= '</select>';

    } elseif ($foreignData['foreign_link'] == true) {
        $html_output .= '<input type="text" id="' . $field_id . $column_index . '"'
            . ' name="criteriaValues[' . $column_index . ']" id="field_'
            . md5($field) . '[' . $column_index .']" class="textfield"'
            . (isset($criteriaValues[$column_index])
                && is_string($criteriaValues[$column_index])
                ? (' value="' . $criteriaValues[$column_index] . '"')
                : '')
            . ' />';

        $html_output .=  <<<EOT
<a target="_blank" onclick="window.open(this.href, 'foreigners', 'width=640,height=240,scrollbars=yes'); return false" href="browse_foreigners.php?
EOT;
        $html_output .= '' . PMA_generate_common_url($db, $table)
            . '&amp;field=' . urlencode($field) . '&amp;fieldkey=' . $column_index
            . '"';
        if ($in_zoom_search_edit) {
            $html_output .= ' class="browse_foreign"';
        }
        $html_output .= '>' . str_replace("'", "\'", $titles['Browse']) . '</a>';
    }
    return $html_output;
}

/**
 * Provides html elements for search criteria inputbox
 * in case the column is of ENUM or SET type
 *
 * @param int    $column_index        Column index
 * @param array  $criteriaValues      Array of search criteria inputs
 * @param string $field_type          Column type
 * @param string $field_id            Column's inputbox's id
 * @param bool   $in_zoom_search_edit Whether we are in zoom search edit
 *
 * @return HTML elements.
 */
function PMA_tblSearchGetEnumSetInputBox($column_index, $criteriaValues, $field_type,
    $field_id, $in_zoom_search_edit = false
) {
    $html_output = '';
    $value = explode(
        ', ',
        str_replace("'", '', substr($field_type, 5, -1))
    );
    $cnt_value = count($value);

    /*
     * Enum in edit mode   --> dropdown
     * Enum in search mode --> multiselect
     * Set in edit mode    --> multiselect
     * Set in search mode  --> input (skipped here, so the 'else'
     *                                 section would handle it)
     */
    if ((strncasecmp($field_type, 'enum', 4) && ! $in_zoom_search_edit)
        || (strncasecmp($field_type, 'set', 3) && $in_zoom_search_edit)
    ) {
        $html_output .= '<select name="criteriaValues[' . ($column_index)
            . '][]" id="' . $field_id . $column_index .'">';
    } else {
        $html_output .= '<select name="criteriaValues[' . ($column_index)
            . '][]" id="' . $field_id . $column_index .'" multiple="multiple" size="'
            . min(3, $cnt_value) . '">';
    }

    //Add select options
    for ($j = 0; $j < $cnt_value; $j++) {
        if (isset($criteriaValues[$column_index])
            && is_array($criteriaValues[$column_index])
            && in_array($value[$j], $criteriaValues[$column_index])
        ) {
            $html_output .= '<option value="' . $value[$j] . '" Selected>'
                . $value[$j] . '</option>';
        } else {
            $html_output .= '<option value="' . $value[$j] . '">'
                . $value[$j] . '</option>';
        }
    } // end for
    $html_output .= '</select>';
    return $html_output;
}

/**
 * Creates the HTML content for:
 * 1) Browsing foreign data for a field.
 * 2) Creating elements for search criteria input on fields.
 *
 * @param array  $foreigners          Array of foreign keys
 * @param array  $foreignData         Foreign keys data
 * @param string $field               Column name
 * @param string $field_type          Column type
 * @param int    $column_index        Column index
 * @param string $db                  Selected database
 * @param string $table               Selected table
 * @param array  $titles              Selected title
 * @param int    $foreignMaxLimit     Max limit of displaying foreign elements
 * @param array  $criteriaValues      Array of search criteria inputs
 * @param bool   $in_fbs              Whether we are in 'function based search'
 * @param bool   $in_zoom_search_edit Whether we are in zoom search edit
 *
 * @return string HTML content for viewing foreing data and elements
 * for search criteria input.
 */
function PMA_tblSearchGetInputbox($foreigners, $foreignData, $field,
    $field_type, $column_index, $db, $table, $titles, $foreignMaxLimit,
    $criteriaValues, $in_fbs = false, $in_zoom_search_edit = false
) {
    $str = '';
    $field_type = (string)$field_type;
    $field_id = ($in_zoom_search_edit) ? 'edit_fieldID_' : 'fieldID_';

    //Get inputbox based on different column types (Foreign key, geometrical, enum)
    if ($foreigners && isset($foreigners[$field])) {
        $str .= PMA_tblSearchGetForeignKeyInputBox(
            $foreignData, $field, $column_index, $db, $table, $titles,
            $foreignMaxLimit, $criteriaValues, $field_id
        );

    } elseif (in_array($field_type, PMA_getGISDatatypes())) {
        $str .= PMA_tblSearchGetGeometricalInputBox($column_index, $in_fbs);

    } elseif (strncasecmp($field_type, 'enum', 4) == 0
        || (strncasecmp($field_type, 'set', 3) == 0 && $in_zoom_search_edit)
    ) {
        $str .= PMA_tblSearchGetEnumSetInputBox(
            $column_index, $criteriaValues, $field_type, $field_id,
            $in_zoom_search_edit = false
        );

    } else {
        // other cases
        $the_class = 'textfield';

        if ($field_type == 'date') {
            $the_class .= ' datefield';
        } elseif ($field_type == 'datetime' || substr($field_type, 0, 9) == 'timestamp') {
            $the_class .= ' datetimefield';
        } elseif (substr($field_type, 0, 3) == 'bit') {
            $the_class .= ' bit';
        }

        $str .= '<input type="text" name="criteriaValues[' . $column_index . ']"'
            .' size="40" class="' . $the_class . '" id="'
            . $field_id . $column_index . '"'
            . (isset($criteriaValues[$column_index])
                && is_string($criteriaValues[$column_index])
                ? (' value="' . $criteriaValues[$column_index] . '"')
                : '')
            . ' />';
    }
    return $str;
}

/**
 * Return the where clause for query generation based on the inputs provided.
 *
 * @param mixed  $criteriaValues Search criteria input
 * @param string $names          Name of the column on which search is submitted
 * @param string $types          Type of the field
 * @param string $collations     Field collation
 * @param string $func_type      Search fucntion/operator
 * @param bool   $unaryFlag      Whether operator unary or not
 * @param bool   $geom_func      Whether geometry functions should be applied
 *
 * @return string HTML content for viewing foreing data and elements
 * for search criteria input.
 */
function PMA_tbl_search_getWhereClause($criteriaValues, $names, $types, $collations,
    $func_type, $unaryFlag, $geom_func = null
) {
    /**
     * @todo move this to a more apropriate place
     */
    $geom_unary_functions = array(
        'IsEmpty' => 1,
        'IsSimple' => 1,
        'IsRing' => 1,
        'IsClosed' => 1,
    );

    $w = '';
    // If geometry function is set apply it to the field name
    if ($geom_func != null && trim($geom_func) != '') {
        // Get details about the geometry fucntions
        $geom_funcs = PMA_getGISFunctions($types, true, false);

        // If the function takes a single parameter
        if ($geom_funcs[$geom_func]['params'] == 1) {
            $backquoted_name = $geom_func . '(' . PMA_backquote($names) . ')';
        } else {
            // If the function takes two parameters
            // create gis data from the string
            $gis_data = PMA_createGISData($criteriaValues);

            $w = $geom_func . '(' . PMA_backquote($names) . ',' . $gis_data . ')';
            return $w;
        }

        // New output type is the output type of the function being applied
        $types = $geom_funcs[$geom_func]['type'];

        // If the where clause is something like 'IsEmpty(`spatial_col_name`)'
        if (isset($geom_unary_functions[$geom_func]) && trim($criteriaValues) == '') {
            $w = $backquoted_name;
            return $w;
        }
    } else {
        $backquoted_name = PMA_backquote($names);
    }

    if ($unaryFlag) {
        $criteriaValues = '';
        $w = $backquoted_name . ' ' . $func_type;

    } elseif (in_array($types, PMA_getGISDatatypes()) && ! empty($criteriaValues)) {
        // create gis data from the string
        $gis_data = PMA_createGISData($criteriaValues);
        $w = $backquoted_name . ' ' . $func_type . ' ' . $gis_data;

    } elseif (strncasecmp($types, 'enum', 4) == 0) {
        if (! empty($criteriaValues)) {
            if (! is_array($criteriaValues)) {
                $criteriaValues = explode(',', $criteriaValues);
            }
            $enum_selected_count = count($criteriaValues);
            if ($func_type == '=' && $enum_selected_count > 1) {
                $func_type    = 'IN';
                $parens_open  = '(';
                $parens_close = ')';

            } elseif ($func_type == '!=' && $enum_selected_count > 1) {
                $func_type    = 'NOT IN';
                $parens_open  = '(';
                $parens_close = ')';

            } else {
                $parens_open  = '';
                $parens_close = '';
            }
            $enum_where = '\'' . PMA_sqlAddslashes($criteriaValues[0]) . '\'';
            for ($e = 1; $e < $enum_selected_count; $e++) {
                $enum_where .= ', \'' . PMA_sqlAddslashes($criteriaValues[$e])
                    . '\'';
            }

            $w = $backquoted_name . ' ' . $func_type . ' ' . $parens_open
                . $enum_where . $parens_close;
        }

    } elseif ($criteriaValues != '') {
        // For these types we quote the value. Even if it's another type (like INT),
        // for a LIKE we always quote the value. MySQL converts strings to numbers
        // and numbers to strings as necessary during the comparison
        if (preg_match('@char|binary|blob|text|set|date|time|year@i', $types)
            || strpos(' ' . $func_type, 'LIKE')
        ) {
            $quot = '\'';
        } else {
            $quot = '';
        }

        // LIKE %...%
        if ($func_type == 'LIKE %...%') {
            $func_type = 'LIKE';
            $criteriaValues = '%' . $criteriaValues . '%';
        }
        if ($func_type == 'REGEXP ^...$') {
            $func_type = 'REGEXP';
            $criteriaValues = '^' . $criteriaValues . '$';
        }

        if ($func_type == 'IN (...)'
            || $func_type == 'NOT IN (...)'
            || $func_type == 'BETWEEN'
            || $func_type == 'NOT BETWEEN'
        ) {
            $func_type = str_replace(' (...)', '', $func_type);

            // quote values one by one
            $values = explode(',', $criteriaValues);
            foreach ($values as &$value) {
                $value = $quot . PMA_sqlAddslashes(trim($value)) . $quot;
            }

            if ($func_type == 'BETWEEN' || $func_type == 'NOT BETWEEN') {
                $w = $backquoted_name . ' ' . $func_type . ' '
                    . (isset($values[0]) ? $values[0] : '')
                    . ' AND ' . (isset($values[1]) ? $values[1] : '');
            } else {
                $w = $backquoted_name . ' ' . $func_type
                    . ' (' . implode(',', $values) . ')';
            }
        } else {
            $w = $backquoted_name . ' ' . $func_type . ' '
                . $quot . PMA_sqlAddslashes($criteriaValues) . $quot;;
        }
    } // end if

    return $w;
}

/**
 * Builds the sql search query from the post parameters
 *
 * @return string the generated SQL query
 */
function PMA_tblSearchBuildSqlQuery()
{
    $sql_query = 'SELECT ';

    // If only distinct values are needed
    $is_distinct = (isset($_POST['distinct'])) ? 'true' : 'false';
    if ($is_distinct == 'true') {
        $sql_query .= 'DISTINCT ';
    }

    // if all column names were selected to display, we do a 'SELECT *'
    // (more efficient and this helps prevent a problem in IE
    // if one of the rows is edited and we come back to the Select results)
    if (isset($_POST['zoom_submit'])) {
        $sql_query .= '* ';
    } else {
        $sql_query .= (count($_POST['columnsToDisplay'])
            == count($_POST['criteriaColumnNames'])
            ? '* '
            : implode(', ', PMA_backquote($_POST['columnsToDisplay'])));
    } // end if

    $sql_query .= ' FROM ' . PMA_backquote($_POST['table']);
    $whereClause = PMA_tblSearchGenerateWhereClause();
    $sql_query .= $whereClause;

    // if the search results are to be ordered
    if (isset($_POST['orderByColumn']) && $_POST['orderByColumn'] != '--nil--') {
        $sql_query .= ' ORDER BY ' . PMA_backquote($_POST['orderByColumn'])
            . ' ' . $_POST['order'];
    } // end if
    return $sql_query;
}

/**
 * Generates the where clause for the SQL search query to be executed
 *
 * @return string the generated where clause
 */
function PMA_tblSearchGenerateWhereClause()
{
    $fullWhereClause = '';

    if (isset($_POST['customWhereClause']) && trim($_POST['customWhereClause']) != '') {
        $fullWhereClause .= ' WHERE ' . $_POST['customWhereClause'];
        return $fullWhereClause;
    }

    // If there are no search criteria set, return
    if (! array_filter($_POST['criteriaValues'])) {
        return $fullWhereClause;
    }

    // else continue to form the where clause from column criteria values
    $fullWhereClause = $charsets = array();
    reset($_POST['criteriaColumnOperators']);
    while (list($column_index, $operator) = each($_POST['criteriaColumnOperators'])) {
        list($charsets[$column_index]) = explode(
            '_', $_POST['criteriaColumnCollations'][$column_index]
        );
        $unaryFlag =  $GLOBALS['PMA_Types']->isUnaryOperator($operator);
        $tmp_geom_func = isset($geom_func[$column_index])
            ? $geom_func[$column_index] : null;

        $whereClause = PMA_tbl_search_getWhereClause(
            $_POST['criteriaValues'][$column_index],
            $_POST['criteriaColumnNames'][$column_index],
            $_POST['criteriaColumnTypes'][$column_index],
            $_POST['criteriaColumnCollations'][$column_index], $operator, $unaryFlag,
            $tmp_geom_func
        );

        if ($whereClause) {
            $fullWhereClause[] = $whereClause;
        }
    } // end while

    if ($fullWhereClause) {
        $fullWhereClause = ' WHERE ' . implode(' AND ', $fullWhereClause);
    }
    return $fullWhereClause;
}

/**
 * Generates HTML for a geometrical function column to be displayed in table
 * search selection form
 *
 * @param boolean $geomColumnFlag whether a geometry column is present
 * @param array   $columnTypes    array containing types of all columns in the table
 * @param array   $geom_types     array of GIS data types
 * @param integer $column_index   index of current column in $columnTypes array
 *
 * @return string the generated HTML
 */
function PMA_tblSearchGetGeomFuncHtml($geomColumnFlag, $columnTypes, $geom_types,
    $column_index
) {
    $html_output = '';
    // return if geometrical column is not present
    if (! $geomColumnFlag) {
        return $html_output;
    }

    /**
     * Displays 'Function' column if it is present
     */
    $html_output .= '<td>';
    // if a geometry column is present
    if (in_array($columnTypes[$column_index], $geom_types)) {
        $html_output .= '<select class="geom_func" name="geom_func['
            . $column_index . ']">';
        // get the relevant list of GIS functions
        $funcs = PMA_getGISFunctions($columnTypes[$column_index], true, true);
        /**
         * For each function in the list of functions, add an option to select list
         */
        foreach ($funcs as $func_name => $func) {
            $name = isset($func['display']) ? $func['display'] : $func_name;
            $html_output .= '<option value="' . htmlspecialchars($name) . '">'
                    . htmlspecialchars($name) . '</option>';
        }
        $html_output .= '</select>';
    } else {
        $html_output .= '&nbsp;';
    }
    $html_output .= '</td>';
    return $html_output;
}

/**
 * Generates formatted HTML for extra search options in table search form
 *
 * @param array $columnNames Array containing types of all columns in the table
 *
 * @return string the generated HTML
 */
function PMA_tblSearchGetOptions($columnNames)
{
    $html_output = '';
    $html_output .= PMA_getDivForSliderEffect('searchoptions', __('Options'));
    /**
     * Displays columns select list for selecting distinct columns in the search
     */
    $html_output .= '<fieldset id="fieldset_select_fields">'
        . '<legend>' . __('Select columns (at least one):') . '</legend>'
        . '<select name="columnsToDisplay[]" size="' . min(count($columnNames), 10)
        . '" multiple="multiple">';
    // Displays the list of the fields
    foreach ($columnNames as $each_field) {
        $html_output .= '        '
            . '<option value="' . htmlspecialchars($each_field) . '"'
            . ' selected="selected">' . htmlspecialchars($each_field)
            . '</option>' . "\n";
    } // end for
    $html_output .= '</select>'
        . '<input type="checkbox" name="distinct" value="DISTINCT" id="oDistinct" />'
        . '<label for="oDistinct">DISTINCT</label></fieldset>';

    /**
     * Displays input box for custom 'Where' clause to be used in the search
     */
    $html_output .= '<fieldset id="fieldset_search_conditions">'
        . '<legend>' . '<em>' . __('Or') . '</em> '
        . __('Add search conditions (body of the "where" clause):') . '</legend>';
    $html_output .= PMA_showMySQLDocu('SQL-Syntax', 'Functions');
    $html_output .= '<input type="text" name="customWhereClause" class="textfield"'
        . 'size="64" />';
    $html_output .= '</fieldset>';

    /**
     * Displays option of changing default number of rows displayed per page
     */
    $html_output .= '<fieldset id="fieldset_limit_rows">'
        . '<legend>' . __('Number of rows per page') . '</legend>'
        . '<input type="text" size="4" name="session_max_rows" '
        . 'value="' . $GLOBALS['cfg']['MaxRows'] . '" class="textfield" />'
        . '</fieldset>';

    /**
     * Displays option for ordering search results by a column value (Asc or Desc)
     */
    $html_output .= '<fieldset id="fieldset_display_order">'
        . '<legend>' . __('Display order:') . '</legend>'
        . '<select name="orderByColumn"><option value="--nil--"></option>';
    foreach ($columnNames as $each_field) {
        $html_output .= '        '
            . '<option value="' . htmlspecialchars($each_field) . '">'
            . htmlspecialchars($each_field) . '</option>' . "\n";
    } // end for
    $html_output .= '</select>';
    $choices = array(
        'ASC' => __('Ascending'),
        'DESC' => __('Descending')
    );
    $html_output .= PMA_getRadioFields(
        'order', $choices, 'ASC', false, true, "formelement"
    );
    unset($choices);

    $html_output .= '</fieldset><br style="clear: both;"/></div></fieldset>';
    return $html_output;
}

/**
 * Other search criteria like data label
 * (for tbl_zoom_select.php)
 *
 * @param array $columnNames Names of columns in the table
 * @param array $dataLabel   Label for points in zoom plot
 *
 * @return string the generated html
 */
function PMA_tblSearchGetOptionsZoom($columnNames, $dataLabel)
{
    $html_output = '';
    $html_output .= '<table class="data">';
    //Select options for datalabel
    $html_output .= '<tr>';
    $html_output .= '<td><label for="dataLabel">'
        . __("Use this column to label each point") . '</label></td>';
    $html_output .= '<td><select name="dataLabel" id="dataLabel" >'
        . '<option value = "">' . __('None') . '</option>';
    for ($j = 0; $j < count($columnNames); $j++) {
        if (isset($dataLabel)
            && $dataLabel == htmlspecialchars($columnNames[$j])
        ) {
            $html_output .= '<option value="'
                . htmlspecialchars($columnNames[$j]) . '" selected="selected">'
                . htmlspecialchars($columnNames[$j]) . '</option>';
        } else {
            $html_output .= '<option value="'
                . htmlspecialchars($columnNames[$j]) . '" >'
                . htmlspecialchars($columnNames[$j]) . '</option>';
        }
    }
    $html_output .= '</select></td>';
    $html_output .= '</tr>';
    //Inputbox for changing default maximum rows to plot
    $html_output .= '<tr>';
    $html_output .= '<td><label for="maxRowPlotLimit">'
        . __("Maximum rows to plot") . '</label></td>';
    $html_output .= '<td>';
    $html_output .= '<input type="text" name="maxPlotLimit" id="maxRowPlotLimit" '
        . 'value="' . ((! empty($_POST['maxPlotLimit']))
            ? htmlspecialchars($_POST['maxPlotLimit'])
            : $GLOBALS['cfg']['maxRowPlotLimit'])
        . '" />';
    $html_output .= '</td></tr>';
    $html_output .= '</table>';
    return $html_output;
}

/**
 * Provides a column's type, collation, operators list, and crietria value
 * to display in table search form
 *
 * @param string  $db               Selected Database
 * @param string  $table            Selected Table
 * @param array   $columnNames      Names of columns in the table
 * @param array   $columnTypes      Types of columns in the table
 * @param array   $columnCollations Collation of all columns
 * @param array   $columnNullFlags  Null information of columns
 * @param array   $foreigners       Array of foreign keys
 * @param integer $search_index     Row number in table search form
 * @param integer $column_index     Column index in ColumnNames array
 *
 * @return array Array contaning column's properties
 */
function PMA_tblSearchGetColumnProperties($db, $table, $columnNames, $columnTypes,
    $columnCollations, $columnNullFlags, $foreigners, $search_index, $column_index
) {
    $selected_operator = (isset($_POST['criteriaColumnOperators'])
        ? $_POST['criteriaColumnOperators'][$search_index] : '');
    $entered_value = (isset($_POST['criteriaValues'])
        ? $_POST['criteriaValues'] : '');
    $titles['Browse'] = PMA_getIcon('b_browse.png', __('Browse foreign values'));
    //Gets column's type and collation
    $type = $columnTypes[$column_index];
    $collation = $columnCollations[$column_index];
    //Gets column's comparison operators depending on column type
    $func = '<select name="criteriaColumnOperators[' . $search_index . ']">';
    $func .= $GLOBALS['PMA_Types']->getTypeOperatorsHtml(
        preg_replace('@\(.*@s', '', $columnTypes[$column_index]),
        $columnNullFlags[$column_index], $selected_operator
    );
    $func .= '</select>';
    //Gets link to browse foreign data(if any) and criteria inputbox
    $foreignData = PMA_getForeignData(
        $foreigners, $columnNames[$column_index], false, '', ''
    );
    $value =  PMA_tblSearchGetInputbox(
        $foreigners, $foreignData, $columnNames[$column_index], $type, $search_index,
        $db, $table, $titles, $GLOBALS['cfg']['ForeignKeyMaxLimit'], $entered_value
    );
    return array(
        'type' => $type,
        'collation' => $collation,
        'func' => $func,
        'value' => $value
    );
}

/**
 * Provides the search form's table row in case of Normal Search
 * (for tbl_select.php)
 *
 * @param string  $db               Selected Database
 * @param string  $table            Selected Table
 * @param array   $columnNames      Names of columns in the table
 * @param array   $columnTypes      Types of columns in the table
 * @param array   $columnCollations Collation of all columns
 * @param array   $columnNullFlags  Null information of columns
 * @param boolean $geomColumnFlag   Whether a geometry column is present
 * @param array   $foreigners       Array of foreign keys
 *
 * @return string the generated table row
 */
function PMA_tblSearchGetRowsNormal($db, $table, $columnNames, $columnTypes,
    $columnCollations, $columnNullFlags, $geomColumnFlag, $foreigners
) {
    $geom_types = PMA_getGISDatatypes();
    $odd_row = true;
    $html_output = '';
    // for every column present in table
    for ($column_index = 0; $column_index < count($columnNames); $column_index++) {
        $html_output .= '<tr class="noclick ' . ($odd_row ? 'odd' : 'even') . '">';
        $odd_row = !$odd_row;
        //If 'Function' column is present
        $html_output .= PMA_tblSearchGetGeomFuncHtml(
            $geomColumnFlag, $columnTypes, $geom_types, $column_index
        );
        //Displays column's name, type, collation and value
        $html_output .= '<th>' . htmlspecialchars($columnNames[$column_index])
            . '</th>';
        $properties = PMA_tblSearchGetColumnProperties(
            $db, $table, $columnNames, $columnTypes, $columnCollations,
            $columnNullFlags, $foreigners, $column_index, $column_index
        );
        $html_output .= '<td>' . $properties['type'] . '</td>';
        $html_output .= '<td>' . $properties['collation'] . '</td>';
        $html_output .= '<td>' . $properties['func'] . '</td>';
        $html_output .= '<td>' . $properties['value'] . '</td>';
        $html_output .= '</tr>';
        //Displays hidden fields
        $html_output .= '<tr><td>';
        $html_output .= '<input type="hidden" name="criteriaColumnNames['
            . $column_index . ']" value="'
            . htmlspecialchars($columnNames[$column_index]) . '" />';
        $html_output .= '<input type="hidden" name="criteriaColumnTypes['
            . $column_index . ']" value="' . $columnTypes[$column_index] . '" />';
        $html_output .= '<input type="hidden" name="criteriaColumnCollations['
            . $column_index . ']" value="' . $columnCollations[$column_index]
            . '" /></td></tr>';
    } // end for

    return $html_output;
}

/**
 * Provides the search form's table row in case of Zoom Search
 * (for tbl_zoom_select.php)
 *
 * @param string $db               Selected Database
 * @param string $table            Selected Table
 * @param array  $columnNames      Names of columns in the table
 * @param array  $columnTypes      Types of columns in the table
 * @param array  $columnCollations Collation of all columns
 * @param array  $columnNullFlags  Null information of columns
 * @param array  $foreigners       Array of foreign keys
 *
 * @return string the generated table row
 */
function PMA_tblSearchGetRowsZoom($db, $table, $columnNames, $columnTypes,
    $columnCollations, $columnNullFlags, $foreigners
) {
    $odd_row = true;
    $html_output = '';
    /**
     * Get already set search criteria (if any)
     */

    //Displays column rows for search criteria input
    for ($i = 0; $i < 4; $i++) {
        //After X-Axis and Y-Axis column rows, display additional criteria option
        if ($i == 2) {
            $html_output .= '<tr><td>';
            $html_output .= __("Additional search criteria");
            $html_output .= '</td></tr>';
        }
        $html_output .= '<tr class="noclick ' . ($odd_row ? 'odd' : 'even') . '">';
        $odd_row = ! $odd_row;
        //Select options for column names
        $html_output .= '<th><select name="criteriaColumnNames[]" id="'
            . 'tableid_' . $i . '" >';
        $html_output .= '<option value="' . 'pma_null' . '">' . __('None')
            . '</option>';
        for ($j = 0 ; $j < count($columnNames); $j++) {
            if (isset($_POST['criteriaColumnNames'][$i])
                && $_POST['criteriaColumnNames'][$i] == htmlspecialchars($columnNames[$j])
            ) {
                $html_output .= '<option value="'
                    . htmlspecialchars($columnNames[$j]) . '" selected="selected">'
                    . htmlspecialchars($columnNames[$j]) . '</option>';
            } else {
                $html_output .= '<option value="'
                    . htmlspecialchars($columnNames[$j]) . '">'
                    . htmlspecialchars($columnNames[$j]) . '</option>';
            }
        }
        $html_output .= '</select></th>';
        if (isset($_POST['criteriaColumnNames'])
            && $_POST['criteriaColumnNames'][$i] != 'pma_null'
        ) {
            $key = array_search($_POST['criteriaColumnNames'][$i], $columnNames);
            $properties = PMA_tblSearchGetColumnProperties(
                $db, $table, $columnNames, $columnTypes, $columnCollations,
                $columnNullFlags, $foreigners, $i, $key
            );
            $type[$i] = $properties['type'];
            $collation[$i] = $properties['collation'];
            $func[$i] = $properties['func'];
            $value[$i] = $properties['value'];
        }
        //Column type
        $html_output .= '<td>' . (isset($type[$i]) ? $type[$i] : '') . '</td>';
        //Column Collation
        $html_output .= '<td>' . (isset($collation[$i]) ? $collation[$i] : '')
            . '</td>';
        //Select options for column operators
        $html_output .= '<td>' . (isset($func[$i]) ? $func[$i] : '') . '</td>';
        //Inputbox for search criteria value
        $html_output .= '<td>' . (isset($value[$i]) ? $value[$i] : '') . '</td>';
        $html_output .= '</tr>';
        //Displays hidden fields
        $html_output .= '<tr><td>';
        $html_output .= '<input type="hidden" name="criteriaColumnTypes[' . $i . ']"'
            . ' id="types_' . $i . '" ';
        if (isset($_POST['criteriaColumnTypes'][$i])) {
            $html_output .= 'value="' . $_POST['criteriaColumnTypes'][$i] . '" ';
        }
        $html_output .= '/>';
        $html_output .= '<input type="hidden" name="criteriaColumnCollations['
            . $i . ']" id="collations_' . $i . '" />';
        $html_output .= '</td></tr>';
    }//end for
    return $html_output;
}

/**
 * Generates HTML for displaying fields table in search form
 *
 * @param string  $db               Selected Database
 * @param string  $table            Selected Table
 * @param array   $columnNames      Names of columns in the table
 * @param array   $columnTypes      Types of columns in the table
 * @param array   $columnCollations Collation of all columns
 * @param array   $columnNullFlags  Null information of columns
 * @param boolean $geomColumnFlag   Whether a geometry column is present
 * @param array   $foreigners       Array of foreign keys
 * @param string  $searchType       Whether normal search or zoom search
 *
 * @return string the generated HTML
 */
function PMA_tblSearchGetFieldsTableHtml($db, $table, $columnNames, $columnTypes,
    $columnCollations, $columnNullFlags, $geomColumnFlag, $foreigners, $searchType
) {
    $html_output = '';
    $html_output .= '<table class="data"'
        . ($searchType == 'zoom' ? ' id="tableFieldsId"' : '') . '>';
    $html_output .= PMA_tbl_getTableHeader($geomColumnFlag);
    $html_output .= '<tbody>';

    if ($searchType == 'zoom') {
        $html_output .= PMA_tblSearchGetRowsZoom(
            $db, $table, $columnNames, $columnTypes, $columnCollations,
            $columnNullFlags, $foreigners
        );
    } else {
        $html_output .= PMA_tblSearchGetRowsNormal(
            $db, $table, $columnNames, $columnTypes, $columnCollations,
            $columnNullFlags, $geomColumnFlag, $foreigners
        );
    }

    $html_output .= '</tbody></table>';
    return $html_output;
}

/**
 * Provides the form tag for table search form
 * (normal search or zoom search)
 *
 * @param string $goto       Goto URL
 * @param string $db         Selected Database
 * @param string $table      Selected Table
 * @param string $searchType Whether normal search or zoom search
 *
 * @return string the HTML for form tag
 */
function PMA_tblSearchGetFormTag($goto, $db, $table, $searchType)
{
    $html_output = '';
    $scriptName = ($searchType == 'zoom' ? 'tbl_zoom_select.php' : 'tbl_select.php');
    $formId = ($searchType == 'zoom' ? 'zoom_search_form' : 'tbl_search_form');

    $html_output .= '<form method="post" action="' . $scriptName . '" '
        . 'name="insertForm" id="' . $formId . '" '
        . ($GLOBALS['cfg']['AjaxEnable'] ? 'class="ajax"' : '') . '>';

    $html_output .= PMA_generate_common_hidden_inputs($db, $table);
    $html_output .= '<input type="hidden" name="goto" value="' . $goto . '" />';
    $html_output .= '<input type="hidden" name="back" value="' . $scriptName
        . '" />';

    return $html_output;
}

/**
 * Generates the table search form under table search tab
 *
 * @param string  $goto             Goto URL
 * @param string  $db               Selected Database
 * @param string  $table            Selected Table
 * @param array   $columnNames      Names of columns in the table
 * @param array   $columnTypes      Types of columns in the table
 * @param array   $columnCollations Collation of all columns
 * @param array   $columnNullFlags  Null information of columns
 * @param boolean $geomColumnFlag   Whether a geometry column is present
 * @param array   $foreigners       Array of foreign keys
 * @param string  $searchType       Whether normal search or zoom search
 * @param string  $dataLabel        Label for points in zoom plot
 *
 * @return string the generated HTML for table search form
 */
function PMA_tblSearchGetSelectionForm($goto, $db, $table, $columnNames,
    $columnTypes, $columnCollations, $columnNullFlags, $geomColumnFlag, $foreigners,
    $searchType, $dataLabel = null
) {
    $html_output = '';
    $html_output .= '<fieldset id="fieldset_subtab">';
    $url_params = array();
    $url_params['db'] = $db;
    $url_params['table'] = $table;

    $html_output .= PMA_generateHtmlTabs(
        PMA_tbl_getSubTabs(), $url_params, 'topmenu2'
    );
    $html_output .= PMA_tblSearchGetFormTag($goto, $db, $table, $searchType);

    $html_output .= '<fieldset id='
        . ($searchType == 'zoom' ? '"inputSection"' : '"fieldset_table_search"') . '>';
    $html_output .= ($searchType == 'zoom'
        ? '' : '<fieldset id="fieldset_table_qbe">');

    // Set caption for fieldset
    if ($searchType == 'zoom') {
        $html_output .= '<legend>'
            . __('Do a "query by example" (wildcard: "%") for two different columns')
            . '</legend>';
    } else {
        $html_output .= '<legend>'
            . __('Do a "query by example" (wildcard: "%")')
            . '</legend>';
    }

    /**
     * Displays fields table in search form
     */
    $html_output .= PMA_tblSearchGetFieldsTableHtml(
        $db, $table, $columnNames, $columnTypes, $columnCollations, $columnNullFlags,
        $geomColumnFlag, $foreigners, $searchType
    );

    /**
     * Displays more search options
     */
    if ($searchType == 'zoom') {
        $html_output .= PMA_tblSearchGetOptionsZoom($columnNames, $dataLabel);
    } else {
        $html_output .= '<div id="gis_editor"></div>'
            . '<div id="popup_background"></div>'
            . '</fieldset>';
        $html_output .= PMA_tblSearchGetOptions($columnNames);
    }

    /**
     * Displays selection form's footer elements
     */
    $html_output .= '<fieldset class="tblFooters">';
    $html_output .= '<input type="submit" name="'
        . ($searchType == 'zoom' ? 'zoom_submit' : 'submit')
        . ($searchType == 'zoom' ? '" id="inputFormSubmitId"' : '" ')
        . 'value="' . __('Go') . '" />';
    $html_output .= '</fieldset></form>';
    if ($searchType == 'zoom') {
        $html_output = '<div id="sqlqueryresults"></div>'
            . $html_output . '</fieldset>';
    } else {
        $html_output .= '<div id="sqlqueryresults"></div></fieldset>';
    }
    return $html_output;
}

/**
 * Provides form for displaying point data and also the scatter plot
 * (for tbl_zoom_select.php)
 *
 * @param string $goto            Goto URL
 * @param string $db              Selected Database
 * @param string $table           Selected Table
 * @param array  $columnNames     Names of columns in the table
 * @param array  $columnTypes     Types of columns in the table
 * @param array  $columnNullFlags Null information of columns
 * @param array  $foreigners      Array of foreign keys
 * @param array  $data            Array containing SQL query data
 *
 * @return string form's html
 */
function PMA_tblSearchGetZoomResultsForm($goto, $db, $table, $columnNames,
    $columnTypes, $columnNullFlags, $foreigners, $data
) {
    $html_output = '';
    $titles['Browse'] = PMA_getIcon('b_browse.png', __('Browse foreign values'));
    $html_output .= '<form method="post" action="tbl_zoom_select.php"'
        . ' name="displayResultForm" id="zoom_display_form"'
        . ($GLOBALS['cfg']['AjaxEnable'] ? ' class="ajax"' : '') . '>';
    $html_output .= PMA_generate_common_hidden_inputs($db, $table);
    $html_output .= '<input type="hidden" name="goto" value="' . $goto . '" />';
    $html_output .= '<input type="hidden" name="back" value="tbl_zoom_select.php" />';

    $html_output .= '<fieldset id="displaySection">';
    $html_output .= '<legend>' . __('Browse/Edit the points') . '</legend>';

    //JSON encode the data(query result)
    $html_output .= '<center>';
    if (isset($_POST['zoom_submit']) && ! empty($data)) {
        $html_output .= '<div id="resizer">';
        $html_output .= '<center><a href="#" onclick="displayHelp();">'
            . __('How to use') . '</a></center>';
        $html_output .= '<div id="querydata" style="display:none">'
            . json_encode($data) . '</div>';
        $html_output .= '<div id="querychart"></div>';
        $html_output .= '<button class="button-reset">'
            . __('Reset zoom') . '</button>';
        $html_output .= '</div>';
    }
    $html_output .= '</center>';

    //Displays rows in point edit form
    $html_output .= '<div id="dataDisplay" style="display:none">';
    $html_output .= '<table><thead>';
    $html_output .= '<tr>';
    $html_output .= '<th>' . __('Column') . '</th>'
        . '<th>' . __('Null') . '</th>'
        . '<th>' . __('Value') . '</th>';
    $html_output .= '</tr>';
    $html_output .= '</thead>';

    $html_output .= '<tbody>';
    $odd_row = true;
    for ($column_index = 0; $column_index < count($columnNames); $column_index++) {
        $fieldpopup = $columnNames[$column_index];
        $foreignData = PMA_getForeignData($foreigners, $fieldpopup, false, '', '');
        $html_output .= '<tr class="noclick ' . ($odd_row ? 'odd' : 'even') . '">';
        $odd_row = ! $odd_row;
        //Display column Names
        $html_output .= '<th>' . htmlspecialchars($columnNames[$column_index])
            . '</th>';
        //Null checkbox if column can be null
        $html_output .= '<th>' . (($columnNullFlags[$column_index] == 'YES')
            ? '<input type="checkbox" class="checkbox_null"'
                . ' name="criteriaColumnNullFlags[' . $column_index . ']"'
                . ' id="edit_fields_null_id_' . $column_index . '" />'
            : '');
        $html_output .= '</th>';
        //Column's Input box
        $html_output .= '<th>';
        $html_output .= PMA_tblSearchGetInputbox(
            $foreigners, $foreignData, $fieldpopup, $columnTypes, $column_index, $db,
            $table, $titles, $GLOBALS['cfg']['ForeignKeyMaxLimit'], '', false, true
        );
        $html_output .= '</th></tr>';
    }
    $html_output .= '</tbody></table>';
    $html_output .= '</div>';
    $html_output .= '<input type="hidden" id="queryID" name="sql_query" />';
    $html_output .= '</form>';
    return $html_output;
}
?>