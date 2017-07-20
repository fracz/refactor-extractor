<?php
/**
 * configures general layout
 * for detailed layout configuration please refer to the css files
 */

/**
 * navi frame
 */
// navi frame width
$GLOBALS['cfg']['NaviWidth']            = 200;

// foreground (text) color for the navi frame
$GLOBALS['cfg']['NaviColor']            = '#000000';

// background for the navi frame
$GLOBALS['cfg']['NaviBackground']       = '#D0DCE0';

// color of the pointer in navi frame
$GLOBALS['cfg']['NaviPointerColor']     = '#CCFFCC';

/**
 * main frame
 */
// foreground (text) color for the main frame
$GLOBALS['cfg']['MainColor']            = '#000000';

// background for the main frame
$GLOBALS['cfg']['MainBackground']       = '#F5F5F5';
//$GLOBALS['cfg']['MainBackground']       = 'url(../' . $_SESSION['PMA_Theme']->getImgPath() . 'vertical_line.png)';

// color of the pointer in browse mode
$GLOBALS['cfg']['BrowsePointerColor']   = '#CCFFCC';

// color of the marker (visually marks row by clicking on it) in browse mode
$GLOBALS['cfg']['BrowseMarkerColor']    = '#FFCC99';

/**
 * fonts
 */
/**
 * the font family as a valid css font family value,
 * if not set the browser default will be used
 * (depending on browser, DTD and system settings)
 */
$GLOBALS['cfg']['FontFamily']           = '';
/**
 * fixed width font family, used in textarea
 */
$GLOBALS['cfg']['FontFamilyFixed']      = 'monospace';
/**
 * font size as a valid css font size value,
 * if not set the browser default will be used
 * (depending on browser, DTD and system settings)
 */
$GLOBALS['cfg']['FontSize']             = '';

/**
 * tables
 */
// border
$GLOBALS['cfg']['Border']               = 0;
// table header and footer color
$GLOBALS['cfg']['ThBackground']         = '#D3DCE3';
// table header and footer background
$GLOBALS['cfg']['ThColor']              = '#000000';
// table data row background
$GLOBALS['cfg']['BgOne']                = '#E5E5E5';
// table data row background, alternate
$GLOBALS['cfg']['BgTwo']                = '#D5D5D5';

/**
 * query window
 */
// Width of Query window
$GLOBALS['cfg']['QueryWindowWidth']    = 600;
// Height of Query window
$GLOBALS['cfg']['QueryWindowHeight']   = 400;

/**
 * SQL Parser Settings
 * Syntax colouring data
 */
$GLOBALS['cfg']['SQP']['fmtColor']     = array(
    'comment'            => '#808000',
    'comment_mysql'      => '',
    'comment_ansi'       => '',
    'comment_c'          => '',
    'digit'              => '',
    'digit_hex'          => 'teal',
    'digit_integer'      => 'teal',
    'digit_float'        => 'aqua',
    'punct'              => 'fuchsia',
    'alpha'              => '',
    'alpha_columnType'   => '#FF9900',
    'alpha_columnAttrib' => '#0000FF',
    'alpha_reservedWord' => '#990099',
    'alpha_functionName' => '#FF0000',
    'alpha_identifier'   => 'black',
    'alpha_charset'      => '#6495ed',
    'alpha_variable'     => '#800000',
    'quote'              => '#008000',
    'quote_double'       => '',
    'quote_single'       => '',
    'quote_backtick'     => ''
);
?>