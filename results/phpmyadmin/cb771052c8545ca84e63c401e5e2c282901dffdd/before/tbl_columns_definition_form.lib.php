<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 * set of functions used by tbl_columns_definitions_form.inc.php
 *
 * @package PhpMyAdmin
 */
if (!defined('PHPMYADMIN')) {
    exit;
}

require_once 'libraries/Template.class.php';

/**
 * Function to get form parameters
 *
 * @param string $db         database
 * @param string $table      table
 * @param string $action     action
 * @param int    $num_fields number of fields
 * @param bool   $selected   selected
 *
 * @return array $form_params form parameters
 */
function PMA_getFormsParameters(
    $db, $table, $action, $num_fields, $selected
) {
    $form_params = array(
        'db' => $db
    );

    if ($action == 'tbl_create.php') {
        $form_params['reload'] = 1;
    } elseif ($action == 'tbl_addfield.php') {
        $form_params['field_where'] = $_REQUEST['field_where'];
        $form_params['after_field'] = $_REQUEST['after_field'];
        $form_params['table'] = $table;
    } else {
        $form_params['table'] = $table;
    }

    if (isset($num_fields)) {
        $form_params['orig_num_fields'] = $num_fields;
    }

    if (isset($_REQUEST['field_where'])) {
        $form_params['orig_field_where'] = $_REQUEST['field_where'];
    }

    if (isset($_REQUEST['after_field'])) {
        $form_params['orig_after_field'] = $_REQUEST['after_field'];
    }

    if (isset($selected) && is_array($selected)) {
        foreach ($selected as $o_fld_nr => $o_fld_val) {
            $form_params['selected[' . $o_fld_nr . ']'] = $o_fld_val;
        }
    }

    return $form_params;
}

/**
 * Function to get html for the create table or field add view
 *
 * @param string $action        action
 * @param array  $form_params   forms parameters
 * @param array  $content_cells content cells
 * @param array  $header_cells  header cells
 *
 * @return string
 */
function PMA_getHtmlForTableCreateOrAddField($action, $form_params, $content_cells,
    $header_cells
) {
    return PMA\Template::get('columns_definitions/column_definitions_form')->render(
        array(
            'action' => $action,
            'form_params' => $form_params,
            'content_cells' => $content_cells,
            'header_cells' => $header_cells
        )
    );
}

/**
 * Function to get header cells
 *
 * @param bool       $is_backup  whether backup or not
 * @param array|null $columnMeta column meta data
 * @param bool       $mimework   whether mimework or not
 *
 * @return array
 */
function PMA_getHeaderCells($is_backup, $columnMeta, $mimework)
{
    $header_cells = array();
    $header_cells[] = __('Name');
    $header_cells[] = __('Type')
        . PMA_Util::showMySQLDocu('data-types');
    $header_cells[] = __('Length/Values')
        . PMA_Util::showHint(
            __(
                'If column type is "enum" or "set", please enter the values using'
                . ' this format: \'a\',\'b\',\'c\'…<br />If you ever need to put'
                . ' a backslash ("\") or a single quote ("\'") amongst those'
                . ' values, precede it with a backslash (for example \'\\\\xyz\''
                . ' or \'a\\\'b\').'
            )
        );
    $header_cells[] = __('Default')
        . PMA_Util::showHint(
            __(
                'For default values, please enter just a single value,'
                . ' without backslash escaping or quotes, using this format: a'
            )
        );
    $header_cells[] = __('Collation');
    $header_cells[] = __('Attributes');
    $header_cells[] = __('Null');

    // Only for 'Edit' Column(s)
    if (isset($_REQUEST['change_column'])
        && ! empty($_REQUEST['change_column'])
    ) {
        $header_cells[] = __('Adjust Privileges') . PMA_Util::showDocu('faq', 'faq6-39');
    }

    // We could remove this 'if' and let the key information be shown and
    // editable. However, for this to work, structure.lib.php must be modified
    // to use the key fields, as tbl_addfield does.
    if (! $is_backup) {
        $header_cells[] = __('Index');
    }

    $header_cells[] = '<abbr title="AUTO_INCREMENT">A_I</abbr>';
    $header_cells[] = __('Comments');

    if (isset($columnMeta)) {
        $header_cells[] = __('Move column');
    }

    if ($mimework && $GLOBALS['cfg']['BrowseMIME']) {
        $header_cells[] = __('MIME type');
        $header_link = '<a href="transformation_overview.php'
            . PMA_URL_getCommon()
            . '#%s" title="' . __(
                'List of available transformations and their options'
            )
            . '" target="_blank">%s</a>';
        $transformations_hint = PMA_Util::showHint(
            __(
                'Please enter the values for transformation options using this'
                . ' format: \'a\', 100, b,\'c\'…<br />If you ever need to put'
                . ' a backslash ("\") or a single quote ("\'") amongst those'
                . ' values, precede it with a backslash (for example \'\\\\xyz\''
                . ' or \'a\\\'b\').'
            )
        );
        $header_cells[] = sprintf(
            $header_link, 'transformation', __('Browser display transformation')
        );
        $header_cells[] = __('Browser display transformation options')
            . $transformations_hint;
        $header_cells[] = sprintf(
            $header_link, 'input_transformation', __('Input transformation')
        );
        $header_cells[] = __('Input transformation options')
            . $transformations_hint;
    }

    return $header_cells;
}

/**
 * Function for moving, load all available column names
 *
 * @param string $db    current database
 * @param string $table current table
 *
 * @return array
 */
function PMA_getMoveColumns($db, $table)
{
    $move_columns_sql_query    = 'SELECT * FROM '
        . PMA_Util::backquote($db)
        . '.'
        . PMA_Util::backquote($table)
        . ' LIMIT 1';
    $move_columns_sql_result = $GLOBALS['dbi']->tryQuery($move_columns_sql_query);
    $move_columns = $GLOBALS['dbi']->getFieldsMeta($move_columns_sql_result);

    return $move_columns;
}

/**
 * Function to get row data for regenerating previous when error occurred.
 *
 * @param int   $columnNumber    column number
 * @param array $submit_fulltext submit full text
 *
 * @return array
 */
function PMA_getRowDataForRegeneration($columnNumber, $submit_fulltext)
{
    $columnMeta = array();
    $columnMeta['Field'] = isset($_REQUEST['field_name'][$columnNumber])
        ? $_REQUEST['field_name'][$columnNumber]
        : false;
    $columnMeta['Type'] = isset($_REQUEST['field_type'][$columnNumber])
        ? $_REQUEST['field_type'][$columnNumber]
        : false;
    $columnMeta['Collation'] = isset($_REQUEST['field_collation'][$columnNumber])
        ? $_REQUEST['field_collation'][$columnNumber]
        : '';
    $columnMeta['Null'] = isset($_REQUEST['field_null'][$columnNumber])
        ? $_REQUEST['field_null'][$columnNumber]
        : '';

    $columnMeta['Key'] = '';
    if (isset($_REQUEST['field_key'][$columnNumber])) {
        $parts = explode('_', $_REQUEST['field_key'][$columnNumber], 2);
        if (count($parts) == 2 && $parts[1] == $columnNumber) {
            switch ($parts[0]) {
            case 'primary':
                $columnMeta['Key'] = 'PRI';
                break;
            case 'index':
                $columnMeta['Key'] = 'MUL';
                break;
            case 'unique':
                $columnMeta['Key'] = 'UNI';
                break;
            case 'fulltext':
                $columnMeta['Key'] = 'FULLTEXT';
                break;
            case 'spatial':
                $columnMeta['Key'] = 'SPATIAL';
                break;
            }
        }
    }

    // put None in the drop-down for Default, when someone adds a field
    $columnMeta['DefaultType']
        = isset($_REQUEST['field_default_type'][$columnNumber])
            ? $_REQUEST['field_default_type'][$columnNumber]
            : 'NONE';
    $columnMeta['DefaultValue']
        = isset($_REQUEST['field_default_value'][$columnNumber])
            ? $_REQUEST['field_default_value'][$columnNumber]
            : '';

    switch ($columnMeta['DefaultType']) {
    case 'NONE' :
        $columnMeta['Default'] = null;
        break;
    case 'USER_DEFINED' :
        $columnMeta['Default'] = $columnMeta['DefaultValue'];
        break;
    case 'NULL' :
    case 'CURRENT_TIMESTAMP' :
        $columnMeta['Default'] = $columnMeta['DefaultType'];
        break;
    }

    $columnMeta['Extra']
        = (isset($_REQUEST['field_extra'][$columnNumber])
        ? $_REQUEST['field_extra'][$columnNumber]
        : false);
    $columnMeta['Comment']
        = (isset($submit_fulltext[$columnNumber])
            && ($submit_fulltext[$columnNumber] == $columnNumber)
        ? 'FULLTEXT'
        : false);

    return $columnMeta;
}

/**
 * Function to get submit properties for regenerating previous when error occurred.
 *
 * @param int $columnNumber column number
 *
 * @return array
 */
function PMA_getSubmitPropertiesForRegeneration($columnNumber)
{
    $submit_length
        = (isset($_REQUEST['field_length'][$columnNumber])
        ? $_REQUEST['field_length'][$columnNumber]
        : false);
    $submit_attribute
        = (isset($_REQUEST['field_attribute'][$columnNumber])
        ? $_REQUEST['field_attribute'][$columnNumber]
        : false);

    $submit_default_current_timestamp
        = (isset($_REQUEST['field_default_current_timestamp'][$columnNumber])
        ? true
        : false);

    return array(
        $submit_length, $submit_attribute, $submit_default_current_timestamp
    );
}

/**
 * An error happened with previous inputs, so we will restore the data
 * to embed it once again in this form.
 *
 * @param int   $columnNumber    column number
 * @param array $submit_fulltext submit full text
 * @param array $comments_map    comments map
 * @param array $mime_map        mime map
 *
 * @return array
 */
function PMA_handleRegeneration($columnNumber, $submit_fulltext, $comments_map,
    $mime_map
) {
    $columnMeta = PMA_getRowDataForRegeneration(
        $columnNumber, isset($submit_fulltext) ? $submit_fulltext : null
    );

    list($submit_length, $submit_attribute, $submit_default_current_timestamp)
        = PMA_getSubmitPropertiesForRegeneration($columnNumber);

    if (isset($_REQUEST['field_comments'][$columnNumber])) {
        $comments_map[$columnMeta['Field']]
            = $_REQUEST['field_comments'][$columnNumber];
    }

    if (isset($_REQUEST['field_mimetype'][$columnNumber])) {
        $mime_map[$columnMeta['Field']]['mimetype']
            = $_REQUEST['field_mimetype'][$columnNumber];
    }

    if (isset($_REQUEST['field_transformation'][$columnNumber])) {
        $mime_map[$columnMeta['Field']]['transformation']
            = $_REQUEST['field_transformation'][$columnNumber];
    }

    if (isset($_REQUEST['field_transformation_options'][$columnNumber])) {
        $mime_map[$columnMeta['Field']]['transformation_options']
            = $_REQUEST['field_transformation_options'][$columnNumber];
    }

    return array(
        $columnMeta, $submit_length, $submit_attribute,
        $submit_default_current_timestamp, $comments_map, $mime_map
    );
}

/**
 * Function to update default value info in $columnMeta and get this array
 *
 * @param array $columnMeta column meta
 * @param bool  $isDefault  whether the row value is default
 *
 * @return array
 */
function PMA_getColumnMetaForDefault($columnMeta, $isDefault)
{
    switch ($columnMeta['Default']) {
    case null:
        if ($columnMeta['Null'] == 'YES') {
            $columnMeta['DefaultType']  = 'NULL';
            $columnMeta['DefaultValue'] = '';
            // SHOW FULL COLUMNS does not report the case
            // when there is a DEFAULT value which is empty so we need to use the
            // results of SHOW CREATE TABLE
        } elseif ($isDefault) {
            $columnMeta['DefaultType']  = 'USER_DEFINED';
            $columnMeta['DefaultValue'] = $columnMeta['Default'];
        } else {
            $columnMeta['DefaultType']  = 'NONE';
            $columnMeta['DefaultValue'] = '';
        }
        break;
    case 'CURRENT_TIMESTAMP':
        $columnMeta['DefaultType']  = 'CURRENT_TIMESTAMP';
        $columnMeta['DefaultValue'] = '';
        break;
    default:
        $columnMeta['DefaultType']  = 'USER_DEFINED';
        $columnMeta['DefaultValue'] = $columnMeta['Default'];
        break;
    }

    return $columnMeta;
}

/**
 * Function to get html for the column name
 *
 * @param int        $columnNumber column number
 * @param int        $ci           cell index
 * @param int        $ci_offset    cell index offset
 * @param array|null $columnMeta   column meta
 * @param array      $cfgRelation  configuration relation
 *
 * @return string
 */
function PMA_getHtmlForColumnName(
    $columnNumber, $ci, $ci_offset, $columnMeta, $cfgRelation
) {
    $title = '';
    if (isset($columnMeta['column_status'])) {
        if ($columnMeta['column_status']['isReferenced']) {
            $title .= sprintf(
                __('Referenced by %s.'),
                implode(",", $columnMeta['column_status']['references'])
            );
        }
        if ($columnMeta['column_status']['isForeignKey']) {
            if (!empty($title)) {
                $title .= "\n";
            }
            $title .=  __('Is a foreign key.');
        }
    }
    if (empty($title)) {
        $title = __('Column');
    }
    $html = '<input' . (isset($columnMeta['column_status'])
        && !$columnMeta['column_status']['isEditable']?' disabled="disabled" ':' ')
        . 'id="field_' . $columnNumber . '_' . ($ci - $ci_offset)
        . '"' . ' type="text" name="field_name[' . $columnNumber . ']"'
        . ' maxlength="64" class="textfield" title="' . $title . '"'
        . ' size="10"'
        . ' value="'
        . (isset($columnMeta['Field'])
            ? htmlspecialchars($columnMeta['Field']) : '')
        . '"' . ' />';
    if (isset($cfgRelation['central_columnswork'])
        && $cfgRelation['central_columnswork']
        && !(isset($columnMeta['column_status'])
        && !$columnMeta['column_status']['isEditable'])
    ) {
        $html .=  '<p style="font-size:80%;margin:5px 2px" '
            . 'id="central_columns_' . $columnNumber . '_'
            . ($ci - $ci_offset)
            . '">';
        $html .= '<a data-maxrows="' . $GLOBALS['cfg']['MaxRows'] . '" '
            . 'href="#" class="central_columns_dialog"> '
            . __('Pick from Central Columns') . '</a>'
            . '</p>';
    }
    return $html;
}

/**
 * Function to get html for the column type
 *
 * @param int    $columnNumber column number
 * @param int    $ci           cell index
 * @param int    $ci_offset    cell index offset
 * @param string $type_upper   type inuppercase
 * @param array  $columnMeta   meta data
 *
 * @return string
 */
function PMA_getHtmlForColumnType($columnNumber, $ci, $ci_offset,
    $type_upper, $columnMeta
) {
    $select_id = 'field_' . $columnNumber . '_' . ($ci - $ci_offset);

    return PMA\Template::get('columns_definitions/column_type')->render(
        array(
            'columnNumber' => $columnNumber,
            'columnMeta' => $columnMeta,
            'type_upper' => $type_upper,
            'select_id' => $select_id
        )
    );
}

/**
 * Function to get html for transformation option
 *
 * @param int        $columnNumber column number
 * @param int        $ci           cell index
 * @param int        $ci_offset    cell index offset
 * @param array|null $columnMeta   column meta
 * @param array      $mime_map     mime map
 * @param string     $type_prefix  prefix for type of transformation
 *                                 '' or 'input'
 *
 * @return string
 */
function PMA_getHtmlForTransformationOption($columnNumber, $ci, $ci_offset,
    $columnMeta, $mime_map, $type_prefix
) {
    $options_key = $type_prefix . 'transformation_options';
    $val = isset($columnMeta['Field'])
            && isset($mime_map[$columnMeta['Field']][$options_key])
                ? htmlspecialchars(
                    $mime_map[$columnMeta['Field']]
                    [$options_key]
                )
                : '';

    return PMA\Template::get('columns_definitions/transformation_option')->render(
        array(
            'columnNumber' => $columnNumber,
            'ci' => $ci,
            'ci_offset' => $ci_offset,
            'options_key' => $options_key,
            'val' => $val
        )
    );
}

/**
 * Function to get html for mime type
 *
 * @param int   $columnNumber   column number
 * @param int   $ci             cell index
 * @param int   $ci_offset      cell index offset
 * @param array $available_mime available mime
 * @param array $columnMeta     column meta
 * @param array $mime_map       mime map
 *
 * @return string
 */
function PMA_getHtmlForMimeType($columnNumber, $ci, $ci_offset,
    $available_mime, $columnMeta, $mime_map
) {
    return PMA\Template::get('columns_definitions/mime_type')->render(
        array(
            'columnNumber' => $columnNumber,
            'ci' => $ci,
            'ci_offset' => $ci_offset,
            'available_mime' => $available_mime,
            'columnMeta' => $columnMeta,
            'mime_map' => $mime_map
        )
    );
}

/**
 * Function to get html for transformations
 *
 * @param int        $columnNumber   column number
 * @param int        $ci             cell index
 * @param int        $ci_offset      cell index offset
 * @param array      $available_mime available mime
 * @param array|null $columnMeta     column meta
 * @param array      $mime_map       mime map
 * @param string     $type_prefix    prefix for type of transformation
 *                                   '' or 'input'
 *
 * @return string
 */
function PMA_getHtmlForTransformation($columnNumber, $ci, $ci_offset,
    $available_mime, $columnMeta, $mime_map, $type_prefix
) {
    $type = $type_prefix . 'transformation';

    return PMA\Template::get('columns_definitions/transformation')->render(
        array(
            'columnNumber' => $columnNumber,
            'ci' => $ci,
            'ci_offset' => $ci_offset,
            'available_mime' => $available_mime,
            'columnMeta' => $columnMeta,
            'mime_map' => $mime_map,
            'type' => $type
        )
    );
}

/**
 * Function to get html for move column
 *
 * @param int   $columnNumber column number
 * @param int   $ci           cell index
 * @param int   $ci_offset    cell index offset
 * @param array $move_columns move columns
 * @param array $columnMeta   column meta
 *
 * @return string
 */
function PMA_getHtmlForMoveColumn($columnNumber, $ci, $ci_offset, $move_columns,
    $columnMeta
) {
    $current_index = 0;
    for ($mi = 0, $cols = count($move_columns); $mi < $cols; $mi++) {
        if ($move_columns[$mi]->name == $columnMeta['Field']) {
            $current_index = $mi;
            break;
        }
    }

    return PMA\Template::get('columns_definitions/move_column')->render(
        array(
            'columnNumber' => $columnNumber,
            'ci' => $ci,
            'ci_offset' => $ci_offset,
            'columnMeta' => $columnMeta,
            'move_columns' => $move_columns,
            'current_index' => $current_index
        )
    );
}

/**
 * Function to get html for column comment
 *
 * @param int   $columnNumber column number
 * @param int   $ci           cell index
 * @param int   $ci_offset    cell index offset
 * @param array $columnMeta   column meta
 * @param array $comments_map comments map
 *
 * @return string
 */
function PMA_getHtmlForColumnComment($columnNumber, $ci, $ci_offset, $columnMeta,
    $comments_map
) {
    return PMA\Template::get('columns_definitions/column_comment')->render(
        array(
            'columnNumber' => $columnNumber,
            'ci' => $ci,
            'ci_offset' => $ci_offset,
            'columnMeta' => $columnMeta,
            'comments_map' => $comments_map
        )
    );
}

/**
 * Function get html for column auto increment
 *
 * @param int   $columnNumber column number
 * @param int   $ci           cell index
 * @param int   $ci_offset    cell index offset
 * @param array $columnMeta   column meta
 *
 * @return string
 */
function PMA_getHtmlForColumnAutoIncrement($columnNumber, $ci, $ci_offset,
    $columnMeta
) {
    return PMA\Template::get('columns_definitions/column_auto_increment')->render(
        array(
            'columnNumber' => $columnNumber,
            'ci' => $ci,
            'ci_offset' => $ci_offset,
            'columnMeta' => $columnMeta
        )
    );
}

/**
 * Function to get html for the column indexes
 *
 * @param int   $columnNumber column number
 * @param int   $ci           cell index
 * @param int   $ci_offset    cell index offset
 * @param array $columnMeta   column meta
 *
 * @return string
 */
function PMA_getHtmlForColumnIndexes($columnNumber, $ci, $ci_offset, $columnMeta)
{
    return PMA\Template::get('columns_definitions/column_indexes')->render(
        array(
            'columnNumber' => $columnNumber,
            'ci' => $ci,
            'ci_offset' => $ci_offset,
            'columnMeta' => $columnMeta
        )
    );
}

/**
 * Function to get html for column Adjust Privileges
 *
 * @param int $columnNumber column number
 * @param int $ci           cell index
 * @param int $ci_offset    cell index offset
 *
 * @return string
 */
function PMA_getHtmlForColumnAdjustPrivileges($columnNumber, $ci, $ci_offset)
{
    return PMA\Template::get('columns_definitions/column_adjust_privileges')->render(
        array(
            'columnNumber' => $columnNumber,
            'ci' => $ci,
            'ci_offset' => $ci_offset
        )
    );
}

/**
 * Function to get html for column null
 *
 * @param int   $columnNumber column number
 * @param int   $ci           cell index
 * @param int   $ci_offset    cell index offset
 * @param array $columnMeta   column meta
 *
 * @return string
 */
function PMA_getHtmlForColumnNull($columnNumber, $ci, $ci_offset, $columnMeta)
{
    return PMA\Template::get('columns_definitions/column_null')->render(
        array(
            'columnNumber' => $columnNumber,
            'ci' => $ci,
            'ci_offset' => $ci_offset,
            'columnMeta' => $columnMeta
        )
    );
}

/**
 * Function to get html for column A_I
 *
 * @param int   $columnNumber column number
 * @param int   $ci           cell index
 * @param int   $ci_offset    cell index offset
 * @param array $columnMeta   column meta
 *
 * @return string
 */
function PMA_getHtmlForColumnExtra($columnNumber, $ci, $ci_offset, $columnMeta)
{
    return PMA\Template::get('columns_definitions/column_extra')->render(
        array(
            'columnNumber' => $columnNumber,
            'ci' => $ci,
            'ci_offset' => $ci_offset,
            'columnMeta' => $columnMeta
        )
    );
}

/**
 * Function to get html for column attribute
 *
 * @param int   $columnNumber         column number
 * @param int   $ci                   cell index
 * @param int   $ci_offset            cell index offset
 * @param array $extracted_columnspec extracted column
 * @param array $columnMeta           column meta
 * @param bool  $submit_attribute     submit attribute
 * @param array $analyzed_sql         analyzed sql
 *
 * @return string
 */
function PMA_getHtmlForColumnAttribute($columnNumber, $ci, $ci_offset,
    $extracted_columnspec, $columnMeta, $submit_attribute, $analyzed_sql
) {
    $attribute     = '';
    if (isset($extracted_columnspec['attribute'])) {
        $attribute = $extracted_columnspec['attribute'];
    }

    if (isset($columnMeta['Extra'])
        && $columnMeta['Extra'] == 'on update CURRENT_TIMESTAMP'
    ) {
        $attribute = 'on update CURRENT_TIMESTAMP';
    }

    if (isset($submit_attribute) && $submit_attribute != false) {
        $attribute = $submit_attribute;
    }

    // here, we have a TIMESTAMP that SHOW FULL COLUMNS reports as having the
    // NULL attribute, but SHOW CREATE TABLE says the contrary. Believe
    // the latter.
    $create_table_fields = $analyzed_sql[0]['create_table_fields'];

    // MySQL 4.1.2+ TIMESTAMP options
    // (if on_update_current_timestamp is set, then it's TRUE)
    if (isset($columnMeta['Field'])) {
        $field = $create_table_fields[$columnMeta['Field']];
    }

    if (isset($field)
        && isset($field['on_update_current_timestamp'])
    ) {
        $attribute = 'on update CURRENT_TIMESTAMP';
    }

    $attribute_types = $GLOBALS['PMA_Types']->getAttributes();
    $cnt_attribute_types = count($attribute_types);

    return PMA\Template::get('columns_definitions/column_attribute')->render(
        array(
            'columnNumber' => $columnNumber,
            'ci' => $ci,
            'ci_offset' => $ci_offset,
            'attribute_types' => $attribute_types,
            'cnt_attribute_types' => $cnt_attribute_types,
            'attribute' => $attribute
        )
    );
}

/**
 * Function to get html for column collation
 *
 * @param int   $columnNumber column number
 * @param int   $ci           cell index
 * @param int   $ci_offset    cell index offset
 * @param array $columnMeta   column meta
 *
 * @return string
 */
function PMA_getHtmlForColumnCollation($columnNumber, $ci, $ci_offset, $columnMeta)
{
    $tmp_collation
        = empty($columnMeta['Collation']) ? null : $columnMeta['Collation'];
    $html = PMA_generateCharsetDropdownBox(
        PMA_CSDROPDOWN_COLLATION, 'field_collation[' . $columnNumber . ']',
        'field_' . $columnNumber . '_' . ($ci - $ci_offset), $tmp_collation, false
    );

    return $html;
}

/**
 * Function get html for column length
 *
 * @param int $columnNumber             column number
 * @param int $ci                       cell index
 * @param int $ci_offset                cell index offset
 * @param int $length_values_input_size length values input size
 * @param int $length_to_display        length to display
 *
 * @return string
 */
function PMA_getHtmlForColumnLength($columnNumber, $ci, $ci_offset,
    $length_values_input_size, $length_to_display
) {
    return PMA\Template::get('columns_definitions/column_length')->render(
        array(
            'columnNumber' => $columnNumber,
            'ci' => $ci,
            'ci_offset' => $ci_offset,
            'length_values_input_size' => $length_values_input_size,
            'length_to_display' => $length_to_display
        )
    );
}

/**
 * Function to get html for the default column
 *
 * @param int    $columnNumber              column number
 * @param int    $ci                        cell index
 * @param int    $ci_offset                 cell index offset
 * @param string $type_upper                type upper
 * @param string $default_current_timestamp default current timestamp
 * @param array  $columnMeta                column meta
 *
 * @return string
 */
function PMA_getHtmlForColumnDefault($columnNumber, $ci, $ci_offset, $type_upper,
    $default_current_timestamp, $columnMeta
) {
    // here we put 'NONE' as the default value of drop-down; otherwise
    // users would have problems if they forget to enter the default
    // value (example, for an INT)
    $default_options = array(
        'NONE'              =>  _pgettext('for default', 'None'),
        'USER_DEFINED'      =>  __('As defined:'),
        'NULL'              => 'NULL',
        'CURRENT_TIMESTAMP' => 'CURRENT_TIMESTAMP',
    );

    // for a TIMESTAMP, do not show the string "CURRENT_TIMESTAMP" as a default
    // value
    if ($type_upper == 'TIMESTAMP'
        && ! empty($default_current_timestamp)
        && isset($columnMeta['Default'])
    ) {
        $columnMeta['Default'] = '';
    } elseif ($type_upper == 'BIT') {
        $columnMeta['DefaultValue']
            = PMA_Util::convertBitDefaultValue($columnMeta['DefaultValue']);
    } elseif ($type_upper == 'BINARY' || $type_upper == 'VARBINARY') {
        $columnMeta['DefaultValue'] = bin2hex($columnMeta['DefaultValue']);
    }

    $default_value = isset($columnMeta['DefaultValue'])
        ? htmlspecialchars($columnMeta['DefaultValue'])
        : '';

    return PMA\Template::get('columns_definitions/column_default')->render(
        array(
            'columnNumber' => $columnNumber,
            'ci' => $ci,
            'ci_offset' => $ci_offset,
            'default_value' => $default_value,
            'default_options' => $default_options,
            'column_meta' => $columnMeta
        )
    );
}

/**
 * Function to get html for column attributes
 *
 * @param int        $columnNumber              column number
 * @param array      $columnMeta                column meta
 * @param string     $type_upper                type upper
 * @param int        $length_values_input_size  length values input size
 * @param int        $length                    length
 * @param string     $default_current_timestamp default current time stamp
 * @param array|null $extracted_columnspec      extracted column spec
 * @param string     $submit_attribute          submit attribute
 * @param array|null $analyzed_sql              analyzed sql
 * @param array      $comments_map              comments map
 * @param array|null $fields_meta               fields map
 * @param bool       $is_backup                 is backup
 * @param array      $move_columns              move columns
 * @param array      $cfgRelation               configuration relation
 * @param array      $available_mime            available mime
 * @param array      $mime_map                  mime map
 *
 * @return array
 */
function PMA_getHtmlForColumnAttributes($columnNumber, $columnMeta, $type_upper,
    $length_values_input_size, $length, $default_current_timestamp,
    $extracted_columnspec, $submit_attribute, $analyzed_sql,
    $comments_map, $fields_meta, $is_backup,
    $move_columns, $cfgRelation, $available_mime, $mime_map
) {
    // Cell index: If certain fields get left out, the counter shouldn't change.
    $ci = 0;
    // Every time a cell shall be left out the STRG-jumping feature, $ci_offset
    // has to be incremented ($ci_offset++)
    $ci_offset = -1;

    $content_cell = array();

    // column name
    $content_cell[$ci] = PMA_getHtmlForColumnName(
        $columnNumber, $ci, $ci_offset, isset($columnMeta) ? $columnMeta : null,
        $cfgRelation
    );
    $ci++;

    // column type
    $content_cell[$ci] = PMA_getHtmlForColumnType(
        $columnNumber, $ci, $ci_offset, $type_upper,
        isset($columnMeta) ? $columnMeta : null
    );
    $ci++;

    // column length
    $content_cell[$ci] = PMA_getHtmlForColumnLength(
        $columnNumber, $ci, $ci_offset, $length_values_input_size, $length
    );
    $ci++;

    // column default
    $content_cell[$ci] = PMA_getHtmlForColumnDefault(
        $columnNumber, $ci, $ci_offset,
        isset($type_upper) ? $type_upper : null,
        isset($default_current_timestamp) ? $default_current_timestamp : null,
        isset($columnMeta) ? $columnMeta : null
    );
    $ci++;

    // column collation
    $content_cell[$ci] = PMA_getHtmlForColumnCollation(
        $columnNumber, $ci, $ci_offset, $columnMeta
    );
    $ci++;

    // column attribute
    $content_cell[$ci] = PMA_getHtmlForColumnAttribute(
        $columnNumber, $ci, $ci_offset,
        isset($extracted_columnspec) ? $extracted_columnspec : null,
        isset($columnMeta) ? $columnMeta : null,
        isset($submit_attribute) ? $submit_attribute : null,
        isset($analyzed_sql) ? $analyzed_sql : null
    );
    $ci++;

    // column NULL
    $content_cell[$ci] = PMA_getHtmlForColumnNull(
        $columnNumber, $ci, $ci_offset, isset($columnMeta) ? $columnMeta : null
    );
    $ci++;

    // column Adjust Privileges
    // Only for 'Edit' Column(s)
    if (isset($_REQUEST['change_column'])
        && ! empty($_REQUEST['change_column'])
    ) {
        $content_cell[$ci] = PMA_getHtmlForColumnAdjustPrivileges(
            $columnNumber, $ci, $ci_offset
        );
        $ci++;
    }

    // column indexes
    // See my other comment about  this 'if'.
    if (!$is_backup) {
        $content_cell[$ci] = PMA_getHtmlForColumnIndexes(
            $columnNumber, $ci, $ci_offset, $columnMeta
        );
        $ci++;
    } // end if ($action ==...)

    // column auto_increment
    $content_cell[$ci] = PMA_getHtmlForColumnAutoIncrement(
        $columnNumber, $ci, $ci_offset, $columnMeta
    );
    $ci++;

    // column comments
    $content_cell[$ci] = PMA_getHtmlForColumnComment(
        $columnNumber, $ci, $ci_offset, isset($columnMeta) ? $columnMeta : null,
        $comments_map
    );
    $ci++;

    // move column
    if (isset($fields_meta)) {
        $content_cell[$ci] = PMA_getHtmlForMoveColumn(
            $columnNumber, $ci, $ci_offset, $move_columns, $columnMeta
        );
        $ci++;
    }

    if ($cfgRelation['mimework']
        && $GLOBALS['cfg']['BrowseMIME']
        && $cfgRelation['commwork']
    ) {
        // Column Mime-type
        $content_cell[$ci] = PMA_getHtmlForMimeType(
            $columnNumber, $ci, $ci_offset, $available_mime, $columnMeta, $mime_map
        );
        $ci++;

        // Column Browser transformation
        $content_cell[$ci] = PMA_getHtmlForTransformation(
            $columnNumber, $ci, $ci_offset, $available_mime,
            $columnMeta, $mime_map, ''
        );
        $ci++;

        // column Transformation options
        $content_cell[$ci] = PMA_getHtmlForTransformationOption(
            $columnNumber, $ci, $ci_offset, $columnMeta, $mime_map, ''
        );
        $ci++;

        // Column Input transformation
        $content_cell[$ci] = PMA_getHtmlForTransformation(
            $columnNumber, $ci, $ci_offset, $available_mime,
            $columnMeta, $mime_map, 'input_'
        );
        $ci++;

        // column Input transformation options
        $content_cell[$ci] = PMA_getHtmlForTransformationOption(
            $columnNumber, $ci, $ci_offset, $columnMeta, $mime_map, 'input_'
        );
    }

    return $content_cell;
}

/**
 * Function to get form parameters for old column
 *
 * @param array  $columnMeta           column meta
 * @param int    $length               length
 * @param array  $form_params          form parameters
 * @param int    $columnNumber         column/field number
 * @param string $type                 type in lowercase without the length
 * @param array  $extracted_columnspec details about the column spec
 *
 * @return array
 */
function PMA_getFormParamsForOldColumn(
    $columnMeta, $length, $form_params, $columnNumber, $type,
    $extracted_columnspec
) {
    // old column name
    if (isset($columnMeta['Field'])) {
        $form_params['field_orig[' . $columnNumber . ']']
            = $columnMeta['Field'];
        if (isset($columnMeta['column_status'])
            && !$columnMeta['column_status']['isEditable']
        ) {
            $form_params['field_name[' . $columnNumber . ']']
                = $columnMeta['Field'];
        }
    } else {
        $form_params['field_orig[' . $columnNumber . ']'] = '';
    }

    // old column type
    if (isset($columnMeta['Type'])) {
        // keep in uppercase because the new type will be in uppercase
        $form_params['field_type_orig[' . $columnNumber . ']']
            = /*overload*/mb_strtoupper($type);
        if (isset($columnMeta['column_status'])
            && !$columnMeta['column_status']['isEditable']
        ) {
            $form_params['field_type[' . $columnNumber . ']']
                = /*overload*/mb_strtoupper($type);
        }
    } else {
        $form_params['field_type_orig[' . $columnNumber . ']'] = '';
    }

    // old column length
    $form_params['field_length_orig[' . $columnNumber . ']'] = $length;

    // old column default
    $form_params['field_default_value_orig[' . $columnNumber . ']']
        = (isset($columnMeta['Default']) ? $columnMeta['Default'] : '');
    $form_params['field_default_type_orig[' . $columnNumber . ']']
        = (isset($columnMeta['DefaultType']) ? $columnMeta['DefaultType'] : '');

    // old column collation
    if (isset($columnMeta['Collation'])) {
        $form_params['field_collation_orig[' . $columnNumber . ']']
            = $columnMeta['Collation'];
    } else {
        $form_params['field_collation_orig[' . $columnNumber . ']'] = '';
    }

    // old column attribute
    if (isset($extracted_columnspec['attribute'])) {
        $form_params['field_attribute_orig[' . $columnNumber . ']']
            = trim($extracted_columnspec['attribute']);
    } else {
        $form_params['field_attribute_orig[' . $columnNumber . ']'] = '';
    }

    // old column null
    if (isset($columnMeta['Null'])) {
        $form_params['field_null_orig[' . $columnNumber . ']']
            = $columnMeta['Null'];
    } else {
        $form_params['field_null_orig[' . $columnNumber . ']'] = '';
    }

    // old column extra (for auto_increment)
    if (isset($columnMeta['Extra'])) {
        $form_params['field_extra_orig[' . $columnNumber . ']']
            = $columnMeta['Extra'];
    } else {
        $form_params['field_extra_orig[' . $columnNumber . ']'] = '';
    }

    // old column comment
    if (isset($columnMeta['Comment'])) {
        $form_params['field_comments_orig[' . $columnNumber . ']']
            = $columnMeta['Comment'];
    } else {
        $form_params['field_comment_orig[' . $columnNumber . ']'] = '';
    }

    return $form_params;
}
?>