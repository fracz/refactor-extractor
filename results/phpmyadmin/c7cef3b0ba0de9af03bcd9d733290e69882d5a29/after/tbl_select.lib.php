<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * Functions for the table-search page and zoom-search page
 *
 * Funtion PMA_tbl_getFields : Returns the fields of a table
 * Funtion PMA_tbl_search_getWhereClause : Returns the where clause for query generation
 *
 * @package phpMyAdmin
 */

require_once 'url_generating.lib.php';



 /**
 * PMA_tbl_setTitle() sets the title for foreign keys display link
 *
 * @param    $propertiesIconic    Type of icon property
 * @param    $themeImage          Icon Image
 * @return   string $str          Value of the Title
 *
 */

function PMA_tbl_setTitle($propertiesIconic,$pmaThemeImage){
	if ($propertiesIconic == true) {
	    $str = '<img class="icon" width="16" height="16" src="' . $pmaThemeImage
	           .'b_browse.png" alt="' . __('Browse foreign values') . '" title="'
	           . __('Browse foreign values') . '" />';

	    if ($propertiesIconic === 'both') {
	        $str .= __('Browse foreign values');
	    return $str;
	    }
	} else {
	    return __('Browse foreign values');
	}
}

 /**
 * PMA_tbl_getFields() gets all the fields of a table along with their types,collations and whether null or not.
 *
 * @uses     PMA_DBI_query()
 * @uses     PMA_backquote()
 * @uses     PMA_DBI_num_rows()
 * @uses     PMA_DBI_fetch_assoc()
 * @uses     PMA_DBI_free_result()
 * @uses     preg_replace()
 * @uses     str_replace()
 * @uses     strncasecmp()
 * @uses     empty()
 *
 * @param    $db    								Selected database
 * @param    $table    								Selected table
 *
 * @return   array($fields_list,$fields_type,$fields_collation,$fields_null)    Array containing the field list, field types, collations and null constatint
 *
 */

function PMA_tbl_getFields($table,$db) {

    // Gets the list and number of fields

    $result     = PMA_DBI_query('SHOW FULL FIELDS FROM ' . PMA_backquote($table) . ' FROM ' . PMA_backquote($db) . ';', null, PMA_DBI_QUERY_STORE);
    $fields_cnt = PMA_DBI_num_rows($result);
    $fields_list = $fields_null = $fields_type = $fields_collation = array();
    while ($row = PMA_DBI_fetch_assoc($result)) {
        $fields_list[] = $row['Field'];
        $type          = $row['Type'];
        // reformat mysql query output
        if (strncasecmp($type, 'set', 3) == 0
            || strncasecmp($type, 'enum', 4) == 0) {
            $type = str_replace(',', ', ', $type);
        } else {

            // strip the "BINARY" attribute, except if we find "BINARY(" because
            // this would be a BINARY or VARBINARY field type
            if (!preg_match('@BINARY[\(]@i', $type)) {
                $type = preg_replace('@BINARY@i', '', $type);
            }
            $type = preg_replace('@ZEROFILL@i', '', $type);
            $type = preg_replace('@UNSIGNED@i', '', $type);

            $type = strtolower($type);
        }
        if (empty($type)) {
            $type = '&nbsp;';
        }
        $fields_null[] = $row['Null'];
        $fields_type[] = $type;
        $fields_collation[] = !empty($row['Collation']) && $row['Collation'] != 'NULL'
                          ? $row['Collation']
                          : '';
    } // end while
    PMA_DBI_free_result($result);
    unset($result, $type);

    return array($fields_list,$fields_type,$fields_collation,$fields_null);

}

/* PMA_tbl_setTableHeader() sets the table header for displaying a table in query-by-example format
 *
 * @return   HTML content, the tags and content for table header
 *
 */

function PMA_tbl_setTableHeader(){

return '<thead>
  	<tr><th>' .  __('Column') . '</th>
        <th>' .  __('Type') . '</th>
        <th>' .  __('Collation') . '</th>
        <th>' .  __('Operator') . '</th>
        <th>' .  __('Value') . '</th>
   	</tr>
        </thead>';


}

/* PMA_tbl_getSubTabs() returns an array with necessary configrations to create sub-tabs(Table Search and Zoom Search) in the table_select page
 *
 * @return   array $subtabs    Array containing configuration (icon,text,link,id,args) of sub-tabs for Table Search and Zoom search
 *
 */

function PMA_tbl_getSubTabs(){

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


/* PMA_tbl_getForeignFields_Values() creates the HTML content for: 1) Browsing foreign data for a field. 2) Creating elements for search criteria input on fields.
 *
 * @uses     PMA_foreignDropdown
 * @uses     PMA_generate_common_url
 * @uses     isset()
 * @uses     is_array()
 * @uses     in_array()
 * @uses     urlencode()
 * @uses     str_replace()
 * @uses     stbstr()
 *
 * @param    $foreigners          Array of foreign keys
 * @param    $foreignData         Foreign keys data
 * @param    $field               Column name
 * @param    $tbl_fields_type     Column type
 * @param    $i                   Column index
 * @param    $db                  Selected database
 * @param    $table               Selected table
 * @param    $titles              Selected title
 * @param    $foreignMaxLimit     Max limit of displaying foreign elements
 * @param    $fields              Array of search criteria inputs
 *
 * @return   string $str    HTML content for viewing foreing data and elements for search criteria input.
 *
 */

function PMA_getForeignFields_Values($foreigners, $foreignData, $field, $tbl_fields_type, $i, $db, $table,$titles,$foreignMaxLimit, $fields){

	$str = '';

	if ($foreigners && isset($foreigners[$field]) && is_array($foreignData['disp_row'])) {

                // f o r e i g n    k e y s
                $str .=  '            <select name="fields[' . $i . ']">' . "\n";
                // go back to first row

                // here, the 4th parameter is empty because there is no current
                // value of data for the dropdown (the search page initial values
                // are displayed empty)
                $str .= PMA_foreignDropdown($foreignData['disp_row'],
                                $foreignData['foreign_field'],
                                $foreignData['foreign_display'],
                                '', $foreignMaxLimit);
                $str .= '            </select>' . "\n";
        } elseif ($foreignData['foreign_link'] == true) {

                       $str .=  '<input type="text" name="fields[' . $i . '] "';
				 'id="field_' . md5($field) . '[' . $i .']"
				 class="textfield" />' ; ?>

			<?php $str .= '<script type="text/javascript">';
                        // <![CDATA[
			 $str .=  <<<EOT
document.writeln('<a target="_blank" onclick="window.open(this.href, \'foreigners\', \'width=640,height=240,scrollbars=yes\'); return false" href="browse_foreigners.php?
EOT;
			$str .= '' . PMA_generate_common_url($db, $table) .  '&amp;field=' . urlencode($field) . '&amp;fieldkey=' . $i . '">' . str_replace("'", "\'", $titles['Browse']) . '</a>\');';
                // ]]
                	$str .= '</script>';
			?>
                        <?php
        } elseif (strncasecmp($tbl_fields_type[$i], 'enum', 4) == 0) {
                // e n u m s
                $enum_value=explode(', ', str_replace("'", '', substr($tbl_fields_type[$i], 5, -1)));
                $cnt_enum_value = count($enum_value);
                $str .= '            <select name="fields[' . ($i) . '][]"'
                        .' multiple="multiple" size="' . min(3, $cnt_enum_value) . '">' . "\n";
                for ($j = 0; $j < $cnt_enum_value; $j++) {
                        if(isset($fields[$i]) && is_array($fields[$i]) && in_array($enum_value[$j],$fields[$i])){
                                $str .= '                <option value="' . $enum_value[$j] . '" Selected>'
                                        . $enum_value[$j] . '</option>';
                        }
                        else{
                                $str .= '                <option value="' . $enum_value[$j] . '">'
                                        . $enum_value[$j] . '</option>';
                        }
                } // end for
                $str .= '            </select>' . "\n";
        } else {
                // o t h e r   c a s e s
                $the_class = 'textfield';
                $type = $tbl_fields_type[$i];
                if ($type == 'date') {
                        $the_class .= ' datefield';
                } elseif ($type == 'datetime' || substr($type, 0, 9) == 'timestamp') {
                        $the_class .= ' datetimefield';
                }
                if(isset($fields[$i]) && is_string($fields[$i])){
                $str .= '            <input type="text" name="fields[' . $i . ']"'
                        .' size="40" class="' . $the_class . '" id="field_' . $i . '" value = "' . $fields[$i] . '"/>' .  "\n";
                }
                else{
                        $str .= '            <input type="text" name="fields[' . $i . ']"'
                        .' size="40" class="' . $the_class . '" id="field_' . $i . '" />' .  "\n";
                }
        };
	return $str;

}


/* PMA_tbl_search_getWhereClause() Return the where clause for query generation based on the inputs provided.
 *
 * @uses     PMA_backquote
 * @uses     PMA_sqlAddslashes
 * @uses     preg_match
 * @uses     isset()
 * @uses     in_array()
 * @uses     str_replace()
 * @uses     strpos()
 * @uses     explode()
 * @uses     trim()
 *
 * @param    $fields        Search criteria input
 * @param    $names         Name of the field(column) on which search criteria is submitted
 * @param    $types         Type of the field
 * @param    $collations    Field collation
 * @param    $func_type     Search fucntion/operator
 * @param    $unaryFlag     Whether operator unary or not
 *
 * @return   string $str    HTML content for viewing foreing data and elements for search criteria input.
 *
 */

function PMA_tbl_search_getWhereClause($fields, $names, $types, $collations, $func_type, $unaryFlag){


	    $w = '';
            if($unaryFlag){
		$fields = '';
                $w = PMA_backquote($names) . ' ' . $func_type;

            } elseif (strncasecmp($types, 'enum', 4) == 0) {
                if (!empty($fields)) {
                    if (! is_array($fields)) {
                        $fields = explode(',', $fields);
                    }
                    $enum_selected_count = count($fields);
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
                    $enum_where = '\'' . PMA_sqlAddslashes($fields[0]) . '\'';
                    for ($e = 1; $e < $enum_selected_count; $e++) {
                        $enum_where .= ', \'' . PMA_sqlAddslashes($fields[$e]) . '\'';
                    }

                    $w = PMA_backquote($names) . ' ' . $func_type . ' ' . $parens_open . $enum_where . $parens_close;
                }

            } elseif ($fields != '') {
                // For these types we quote the value. Even if it's another type (like INT),
                // for a LIKE we always quote the value. MySQL converts strings to numbers
                // and numbers to strings as necessary during the comparison
                if (preg_match('@char|binary|blob|text|set|date|time|year@i', $types) || strpos(' ' . $func_type, 'LIKE')) {
                    $quot = '\'';
                } else {
                    $quot = '';
                }

                // LIKE %...%
                if ($func_type == 'LIKE %...%') {
                    $func_type = 'LIKE';
                    $fields = '%' . $fields . '%';
                }
                if ($func_type == 'REGEXP ^...$') {
                    $func_type = 'REGEXP';
                    $fields = '^' . $fields . '$';
                }

                if ($func_type == 'IN (...)' || $func_type == 'NOT IN (...)' || $func_type == 'BETWEEN' || $func_type == 'NOT BETWEEN') {
                    $func_type = str_replace(' (...)', '', $func_type);

                    // quote values one by one
                    $values = explode(',', $fields);
                    foreach ($values as &$value)
                        $value = $quot . PMA_sqlAddslashes(trim($value)) . $quot;

                    if ($func_type == 'BETWEEN' || $func_type == 'NOT BETWEEN')
                        $w = PMA_backquote($names) . ' ' . $func_type . ' ' . (isset($values[0]) ? $values[0] : '')  . ' AND ' . (isset($values[1]) ? $values[1] : '');
                    else
                        $w = PMA_backquote($names) . ' ' . $func_type . ' (' . implode(',', $values) . ')';
                }
                else {
                    $w = PMA_backquote($names) . ' ' . $func_type . ' ' . $quot . PMA_sqlAddslashes($fields) . $quot;;
                }

            } // end if

	return $w;
}

?>