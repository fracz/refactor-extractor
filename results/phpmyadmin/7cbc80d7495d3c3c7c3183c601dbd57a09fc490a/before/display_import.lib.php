<?php
/* vim: set expandtab sw=4 ts=4 sts=4: */
/**
 *
 * @package PhpMyAdmin
 */
if (! defined('PHPMYADMIN')) {
    exit;
}

/**
 *
 */
require_once './libraries/file_listing.lib.php';
require_once './libraries/plugin_interface.lib.php';
require_once './libraries/display_import_ajax.lib.php';

/* Scan for plugins */
$import_list = PMA_getPlugins(
    "import",
    'libraries/plugins/import/',
    $import_type
);

/* Fail if we didn't find any plugin */
if (empty($import_list)) {
    PMA_Message::error(
        __(
            'Could not load import plugins, please check your installation!'
        )
    )->display();
    exit;
}

$html = '';
$html .= '<iframe id="import_upload_iframe" name="import_upload_iframe" width="1" height="1" style="display: none;"></iframe>';
$html .= '<div id="import_form_status" style="display: none;"></div>';
$html .= '<div id="importmain">';
$html .= '    <img src="' . $GLOBALS['pmaThemeImage'] . 'ajax_clock_small.gif" width="16" height="16" alt="ajax clock" style="display: none;" />';
$html .= '    <script type="text/javascript">
        //<![CDATA[
        $( function() {
            // add event when user click on "Go" button
            $("#buttonGo").bind("click", function() {
                $("#upload_form_form").css("display", "none"); // hide form
                $("#upload_form_status").css("display", "inline"); // show progress bar
                $("#upload_form_status_info").css("display", "inline"); // - || -';

if ($_SESSION[$SESSION_KEY]["handler"] != "UploadNoplugin") {

    $ajax_url = "import_status.php?id=" . $upload_id . "&" . PMA_generate_common_url(array('import_status'=>1), '&') + ';';
    $html .= '     var ajax_url =' . $ajax_url + ';';
    $html .= '     var pmaThemeImage =' . $GLOBALS['pmaThemeImage'] + ';';
    $html .= '     var promotStr =' . PMA_jsFormat(__('The file being uploaded is probably larger than the maximum allowed size or this is a known bug in webkit based (Safari, Google Chrome, Arora etc.) browsers.'), false) + ';';
    $html .= '     var statustext_str =' . PMA_escapeJsString(__('%s of %s')) + ';';
    $html .= '     var import_str =' . PMA_jsFormat(__('Uploading your import file...'), false) + ';';
    $html .= '     var second_str =' . PMA_jsFormat(__('%s/sec.'), false) + ';';
    $html .= '     var remaining_str1 =' . PMA_jsFormat(__('About %MIN min. %SEC sec. remaining.'), false) + ';';
    $html .= '     var remaining_str2 =' . PMA_jsFormat(__('About %SEC sec. remaining.'), false) + ';';
    $html .= '     var proceed_str =' . PMA_jsFormat(__('The file is being processed, please be patient.'), false) + ';';
    $html .= '     var import_url =' . PMA_generate_common_url(array('import_status'=>1), '&') + ';';
    $html .= '     var ajax_url =' . $ajax_url + ';';
    $html .= '     var ajax_url =' . $ajax_url + ';';
    $html .= '     var ajax_url =' . $ajax_url + ';';
    $html .= '     var ajax_url =' . $ajax_url + ';';
    $html .= '     var ajax_url =' . $ajax_url + ';';

    $html .= '     $.include("display_import_no_plugin.js"); ';

    // reload the left sidebar when the import is finished
    $GLOBALS['reload'] = true;

    $html .= '                      } // if finished';
    $html .= '                    else {';
    $html .= '                        setTimeout(perform_upload, 1000);';
    $html .= '                    }';
    $html .= '                });';
    $html .= '            };';
    $html .= '            setTimeout(perform_upload, 1000);';

} else { // no plugin available
    $html .= '                $("#upload_form_status_info").html("<img src="' . $GLOBALS['pmaThemeImage'] . 'ajax_clock_small.gif" width="16" height="16" alt="ajax clock" /> ' . PMA_jsFormat(__('Please be patient, the file is being uploaded. Details about the upload are not available.'), false) . PMA_Util::showDocu('faq', 'faq2-9') . ');';
    $html .= '                       $("#upload_form_status").css("display", "none");';
} // else

$html .= '                    }); // onclick';
$html .= '                }); // domready';
$html .= '                //]]>';
$html .= '    </script>';

$html .= '    <form action="import.php" method="post" enctype="multipart/form-data"';
$html .= '        name="import"';
if ($_SESSION[$SESSION_KEY]["handler"] != "UploadNoplugin") {
    $html .= ' target="import_upload_iframe"';
}
$html .= ' class="ajax"';
$html .= '>';
$html .= '    <input type="hidden" name="';
        $html .= $_SESSION[$SESSION_KEY]['handler']::getIdKey();
$html .= '" value="' . $upload_id . '" />';


if ($import_type == 'server') {
    $html .= PMA_generate_common_hidden_inputs('', '', 1);
} elseif ($import_type == 'database') {
    $html .= PMA_generate_common_hidden_inputs($db, '', 1);
} else {
    $html .= PMA_generate_common_hidden_inputs($db, $table, 1);
}
$html .= '    <input type="hidden" name="import_type" value="' . $import_type . '" />'."\n";

$html .= '    <div class="exportoptions" id="header">';
$html .= '        <h2>';
$html .= PMA_Util::getImage('b_import.png', __('Import'));

if ($import_type == 'server') {
    $html .= __('Importing into the current server');
} elseif ($import_type == 'database') {
    printf(__('Importing into the database "%s"'), htmlspecialchars($db));
} else {
    printf(__('Importing into the table "%s"'), htmlspecialchars($table));
}
$html .= '        </h2>';
$html .= '    </div>';

$html .= '    <div class="importoptions">';
$html .= '         <h3>'  . __('File to Import:') . '</h3>';

// zip, gzip and bzip2 encode features
$compressions = array();

if ($cfg['GZipDump'] && @function_exists('gzopen')) {
    $compressions[] = 'gzip';
}
if ($cfg['BZipDump'] && @function_exists('bzopen')) {
    $compressions[] = 'bzip2';
}
if ($cfg['ZipDump'] && @function_exists('zip_open')) {
    $compressions[] = 'zip';
}
// We don't have show anything about compression, when no supported
if ($compressions != array()) {
    $html .= '<div class="formelementrow" id="compression_info">';
    printf(__('File may be compressed (%s) or uncompressed.'), implode(", ", $compressions));
    $html .= '<br />';
    $html .= __('A compressed file\'s name must end in <b>.[format].[compression]</b>. Example: <b>.sql.zip</b>');
    $html .= '</div>';
}

$html .= '        <div class="formelementrow" id="upload_form">';

if ($GLOBALS['is_upload'] && !empty($cfg['UploadDir'])) {
    $html .= '            <ul>';
    $html .= '            <li>';
    $html .= '                <input type="radio" name="file_location" id="radio_import_file" />';

    $html .= PMA_Util::getBrowseUploadFileBlock($max_upload_size);

    $html .= '            </li>';
    $html .= '            <li>';
    $html .= '               <input type="radio" name="file_location" id="radio_local_import_file" />';

    $html .= PMA_Util::getSelectUploadFileBlock($import_list, $cfg['UploadDir']);

    $html .= '            </li>';
    $html .= '            </ul>';

} elseif ($GLOBALS['is_upload']) {
    $uid = uniqid('');
    $html .= PMA_Util::getBrowseUploadFileBlock($max_upload_size);
} elseif (!$GLOBALS['is_upload']) {
    PMA_Message::notice(__('File uploads are not allowed on this server.'))->display();
} elseif (!empty($cfg['UploadDir'])) {
    $html .= PMA_Util::getSelectUploadFileBlock($import_list, $cfg['UploadDir']);
} // end if (web-server upload directory)

$html .= '        </div>';

$html .= '       <div class="formelementrow" id="charaset_of_file">';
// charset of file
if ($GLOBALS['PMA_recoding_engine'] != PMA_CHARSET_NONE) {
    $html .= '<label for="charset_of_file">' . __('Character set of the file:') . '</label>';
    reset($cfg['AvailableCharsets']);
    $html .= '<select id="charset_of_file" name="charset_of_file" size="1">';
    foreach ($cfg['AvailableCharsets'] as $temp_charset) {
        $html .= '<option value="' . htmlentities($temp_charset) .  '"';
        if ((empty($cfg['Import']['charset']) && $temp_charset == 'utf-8')
            || $temp_charset == $cfg['Import']['charset']
        ) {
            $html .= ' selected="selected"';
        }
        $html .= '>' . htmlentities($temp_charset) . '</option>';
    }
    $html .= ' </select><br />';
} else {
    $html .= '<label for="charset_of_file">' . __('Character set of the file:') . '</label>' . "\n";
    $html .= PMA_generateCharsetDropdownBox(PMA_CSDROPDOWN_CHARSET, 'charset_of_file', 'charset_of_file', 'utf8', false);
} // end if (recoding)

    $html .= '        </div>';
    $html .= '   </div>';
    $html .= '    <div class="importoptions">';
    $html .= '        <h3>' . __('Partial Import:') . '</h3>';


if (isset($timeout_passed) && $timeout_passed) {
    $html .= '<div class="formelementrow">' . "\n";
    $html .= '<input type="hidden" name="skip" value="' . $offset . '" />';
    $html .= sprintf(__('Previous import timed out, after resubmitting will continue from position %d.'), $offset);
    $html .= '</div>' . "\n";
}

    $html .= '        <div class="formelementrow">';
    $html .= '           <input type="checkbox" name="allow_interrupt" value="yes"';
    $html .= '                  id="checkbox_allow_interrupt" ' . PMA_pluginCheckboxCheck('Import', 'allow_interrupt') . '/>';
    $html .= '            <label for="checkbox_allow_interrupt">' . __('Allow the interruption of an import in case the script detects it is close to the PHP timeout limit. <i>(This might be a good way to import large files, however it can break transactions.)</i>') . '</label><br />';
    $html .= '        </div>';


if (! (isset($timeout_passed) && $timeout_passed)) {

    $html .= '        <div class="formelementrow">';
    $html .= '            <label for="text_skip_queries">' .  __('Number of rows to skip, starting from the first row:') . '</label>';
    $html .= '            <input type="text" name="skip_queries" value="' . PMA_pluginGetDefault('Import', 'skip_queries'). '" id="text_skip_queries" />';
    $html .= '        </div>';

} else {
    // If timeout has passed,
    // do not show the Skip dialog to avoid the risk of someone
    // entering a value here that would interfere with "skip"
    $html .= '         <input type="hidden" name="skip_queries" value="' . PMA_pluginGetDefault('Import', 'skip_queries') . '" id="text_skip_queries" />';

}

$html .= '    </div>';

$html .= '   <div class="importoptions">';
$html .= '       <h3>' . __('Format:') . '</h3>';
$html .= PMA_pluginGetChoice('Import', 'format', $import_list);
$html .= '       <div id="import_notification"></div>';
$html .= '   </div>';

$html .= '    <div class="importoptions" id="format_specific_opts">';
$html .= '        <h3>' . __('Format-Specific Options:') . '</h3>';
$html .= '        <p class="no_js_msg" id="scroll_to_options_msg">Scroll down to fill in the options for the selected format and ignore the options for other formats.</p>';
$html .= PMA_pluginGetOptions('Import', $import_list);
$html .= '    </div>';
$html .= '        <div class="clearfloat"></div>';

// Encoding setting form appended by Y.Kawada
if (function_exists('PMA_Kanji_encodingForm')) {
    $html .= '        <div class="importoptions" id="kanji_encoding">';
    $html .= '            <h3>' . __('Encoding Conversion:') . '</h3>';
    $html .= PMA_Kanji_encodingForm();
    $html .= '        </div>';

}
$html .= "\n";

$html .= '    <div class="importoptions" id="submit">';
$html .= '       <input type="submit" value="' . __('Go') . '" id="buttonGo" />';
$html .= '   </div>';
$html .= '</form>';
$html .= '</div>';

$response = PMA_Response::getInstance();
$response->addHTML('</div>');

?>