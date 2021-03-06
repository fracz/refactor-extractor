<?php

// This file is part of Moodle - http://moodle.org/
//
// Moodle is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Moodle is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Moodle.  If not, see <http://www.gnu.org/licenses/>.

/**
 * Library of functions for web output
 *
 * Library of all general-purpose Moodle PHP functions and constants
 * that produce HTML output
 *
 * Other main libraries:
 * - datalib.php - functions that access the database.
 * - moodlelib.php - general-purpose Moodle functions.
 *
 * @package moodlecore
 * @copyright 1999 onwards Martin Dougiamas {@link http://moodle.com}
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */

/// Constants

/// Define text formatting types ... eventually we can add Wiki, BBcode etc

/**
 * Does all sorts of transformations and filtering
 */
define('FORMAT_MOODLE',   '0');   // Does all sorts of transformations and filtering

/**
 * Plain HTML (with some tags stripped)
 */
define('FORMAT_HTML',     '1');   // Plain HTML (with some tags stripped)

/**
 * Plain text (even tags are printed in full)
 */
define('FORMAT_PLAIN',    '2');   // Plain text (even tags are printed in full)

/**
 * Wiki-formatted text
 * Deprecated: left here just to note that '3' is not used (at the moment)
 * and to catch any latent wiki-like text (which generates an error)
 */
define('FORMAT_WIKI',     '3');   // Wiki-formatted text

/**
 * Markdown-formatted text http://daringfireball.net/projects/markdown/
 */
define('FORMAT_MARKDOWN', '4');   // Markdown-formatted text http://daringfireball.net/projects/markdown/

/**
 * TRUSTTEXT marker - if present in text, text cleaning should be bypassed
 */
define('TRUSTTEXT', '#####TRUSTTEXT#####');

/**
 * A moodle_url comparison using this flag will return true if the base URLs match, params are ignored
 */
define('URL_MATCH_BASE', 0);
/**
 * A moodle_url comparison using this flag will return true if the base URLs match and the params of url1 are part of url2
 */
define('URL_MATCH_PARAMS', 1);
/**
 * A moodle_url comparison using this flag will return true if the two URLs are identical, except for the order of the params
 */
define('URL_MATCH_EXACT', 2);

/**
 * Allowed tags - string of html tags that can be tested against for safe html tags
 * @global string $ALLOWED_TAGS
 * @name $ALLOWED_TAGS
 */
global $ALLOWED_TAGS;
$ALLOWED_TAGS =
'<p><br><b><i><u><font><table><tbody><thead><tfoot><span><div><tr><td><th><ol><ul><dl><li><dt><dd><h1><h2><h3><h4><h5><h6><hr><img><a><strong><emphasis><em><sup><sub><address><cite><blockquote><pre><strike><param><acronym><nolink><lang><tex><algebra><math><mi><mn><mo><mtext><mspace><ms><mrow><mfrac><msqrt><mroot><mstyle><merror><mpadded><mphantom><mfenced><msub><msup><msubsup><munder><mover><munderover><mmultiscripts><mtable><mtr><mtd><maligngroup><malignmark><maction><cn><ci><apply><reln><fn><interval><inverse><sep><condition><declare><lambda><compose><ident><quotient><exp><factorial><divide><max><min><minus><plus><power><rem><times><root><gcd><and><or><xor><not><implies><forall><exists><abs><conjugate><eq><neq><gt><lt><geq><leq><ln><log><int><diff><partialdiff><lowlimit><uplimit><bvar><degree><set><list><union><intersect><in><notin><subset><prsubset><notsubset><notprsubset><setdiff><sum><product><limit><tendsto><mean><sdev><variance><median><mode><moment><vector><matrix><matrixrow><determinant><transpose><selector><annotation><semantics><annotation-xml><tt><code>';

/**
 * Allowed protocols - array of protocols that are safe to use in links and so on
 * @global string $ALLOWED_PROTOCOLS
 * @name $ALLOWED_PROTOCOLS
 */
$ALLOWED_PROTOCOLS = array('http', 'https', 'ftp', 'news', 'mailto', 'rtsp', 'teamspeak', 'gopher', 'mms',
                           'color', 'callto', 'cursor', 'text-align', 'font-size', 'font-weight', 'font-style', 'font-family',
                           'border', 'margin', 'padding', 'background', 'background-color', 'text-decoration');   // CSS as well to get through kses


/// Functions

/**
 * Add quotes to HTML characters
 *
 * Returns $var with HTML characters (like "<", ">", etc.) properly quoted.
 * This function is very similar to {@link p()}
 *
 * @todo Remove obsolete param $obsolete if not used anywhere
 *
 * @param string $var the string potentially containing HTML characters
 * @param boolean $obsolete no longer used.
 * @return string
 */
function s($var, $obsolete = false) {

    if ($var == '0') {  // for integer 0, boolean false, string '0'
        return '0';
    }

    return preg_replace("/&amp;(#\d+);/i", "&$1;", htmlspecialchars($var));
}

/**
 * Add quotes to HTML characters
 *
 * Prints $var with HTML characters (like "<", ">", etc.) properly quoted.
 * This function simply calls {@link s()}
 * @see s()
 *
 * @todo Remove obsolete param $obsolete if not used anywhere
 *
 * @param string $var the string potentially containing HTML characters
 * @param boolean $obsolete no longer used.
 * @return string
 */
function p($var, $obsolete = false) {
    echo s($var, $obsolete);
}

/**
 * Does proper javascript quoting.
 *
 * Do not use addslashes anymore, because it does not work when magic_quotes_sybase is enabled.
 *
 * @param mixed $var String, Array, or Object to add slashes to
 * @return mixed quoted result
 */
function addslashes_js($var) {
    if (is_string($var)) {
        $var = str_replace('\\', '\\\\', $var);
        $var = str_replace(array('\'', '"', "\n", "\r", "\0"), array('\\\'', '\\"', '\\n', '\\r', '\\0'), $var);
        $var = str_replace('</', '<\/', $var);   // XHTML compliance
    } else if (is_array($var)) {
        $var = array_map('addslashes_js', $var);
    } else if (is_object($var)) {
        $a = get_object_vars($var);
        foreach ($a as $key=>$value) {
          $a[$key] = addslashes_js($value);
        }
        $var = (object)$a;
    }
    return $var;
}

/**
 * Remove query string from url
 *
 * Takes in a URL and returns it without the querystring portion
 *
 * @param string $url the url which may have a query string attached
 * @return string The remaining URL
 */
 function strip_querystring($url) {

    if ($commapos = strpos($url, '?')) {
        return substr($url, 0, $commapos);
    } else {
        return $url;
    }
}

/**
 * Returns the URL of the HTTP_REFERER, less the querystring portion if required
 *
 * @uses $_SERVER
 * @param boolean $stripquery if true, also removes the query part of the url.
 * @return string The resulting referer or emtpy string
 */
function get_referer($stripquery=true) {
    if (isset($_SERVER['HTTP_REFERER'])) {
        if ($stripquery) {
            return strip_querystring($_SERVER['HTTP_REFERER']);
        } else {
            return $_SERVER['HTTP_REFERER'];
        }
    } else {
        return '';
    }
}


/**
 * Returns the name of the current script, WITH the querystring portion.
 *
 * This function is necessary because PHP_SELF and REQUEST_URI and SCRIPT_NAME
 * return different things depending on a lot of things like your OS, Web
 * server, and the way PHP is compiled (ie. as a CGI, module, ISAPI, etc.)
 * <b>NOTE:</b> This function returns false if the global variables needed are not set.
 *
 * @global string
 * @return mixed String, or false if the global variables needed are not set
 */
function me() {
    global $ME;
    return $ME;
}

/**
 * Returns the name of the current script, WITH the full URL.
 *
 * This function is necessary because PHP_SELF and REQUEST_URI and SCRIPT_NAME
 * return different things depending on a lot of things like your OS, Web
 * server, and the way PHP is compiled (ie. as a CGI, module, ISAPI, etc.
 * <b>NOTE:</b> This function returns false if the global variables needed are not set.
 *
 * Like {@link me()} but returns a full URL
 * @see me()
 *
 * @global string
 * @return mixed String, or false if the global variables needed are not set
 */
function qualified_me() {
    global $FULLME;
    return $FULLME;
}

/**
 * Class for creating and manipulating urls.
 *
 * It can be used in moodle pages where config.php has been included without any further includes.
 *
 * It is useful for manipulating urls with long lists of params.
 * One situation where it will be useful is a page which links to itself to perfrom various actions
 * and / or to process form data. A moodle_url object :
 * can be created for a page to refer to itself with all the proper get params being passed from page call to
 * page call and methods can be used to output a url including all the params, optionally adding and overriding
 * params and can also be used to
 *     - output the url without any get params
 *     - and output the params as hidden fields to be output within a form
 *
 * One important usage note is that data passed to methods out, out_action, get_query_string and
 * hidden_params_out affect what is returned by the function and do not change the data stored in the object.
 * This is to help with typical usage of these objects where one object is used to output urls
 * in many places in a page.
 *
 * @link http://docs.moodle.org/en/Development:lib/weblib.php_moodle_url See short write up here
 * @license http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @package moodlecore
 */
class moodle_url {
    /**
     * @var string
     * @access protected
     */
    protected $scheme = ''; // e.g. http
    protected $host = '';
    protected $port = '';
    protected $user = '';
    protected $pass = '';
    protected $path = '';
    protected $fragment = '';
    /**
     * @var array
     * @access protected
     */
    protected $params = array(); // Associative array of query string params

    /**
     * Pass no arguments to create a url that refers to this page.
     * Use empty string to create empty url.
     *
     * @global string
     * @param mixed $url a number of different forms are accespted:
     *      null - create a URL that is the same as the URL used to load this page, but with no query string
     *      '' - and empty URL
     *      string - a URL, will be parsed into it's bits, including query string
     *      array - as returned from the PHP function parse_url
     *      moodle_url - make a copy of another moodle_url
     * @param array $params these params override anything in the query string
     *      where params have the same name.
     */
    public function __construct($url = null, $params = array()) {
        if ($url === '') {
            // Leave URL blank.
        } else if (is_a($url, 'moodle_url')) {
            $this->scheme = $url->scheme;
            $this->host = $url->host;
            $this->port = $url->port;
            $this->user = $url->user;
            $this->pass = $url->pass;
            $this->path = $url->path;
            $this->fragment = $url->fragment;
            $this->params = $url->params;
        } else {
            if ($url === null) {
                global $ME;
                $url = $ME;
            }
            if (is_string($url)) {
                $url = parse_url($url);
            }
            $parts = $url;
            if ($parts === FALSE) {
                throw new moodle_exception('invalidurl');
            }
            if (isset($parts['query'])) {
                parse_str(str_replace('&amp;', '&', $parts['query']), $this->params);
            }
            unset($parts['query']);
            foreach ($parts as $key => $value) {
                $this->$key = $value;
            }
        }
        $this->params($params);
    }

    /**
     * Add an array of params to the params for this page.
     *
     * The added params override existing ones if they have the same name.
     *
     * @param array $params Defaults to null. If null then returns all params.
     * @return array Array of Params for url.
     */
    public function params($params = null) {
        if (!is_null($params)) {
            return $this->params = $params + $this->params;
        } else {
            return $this->params;
        }
    }

    /**
     * Remove all params if no arguments passed.
     * Remove selected params if arguments are passed.
     *
     * Can be called as either remove_params('param1', 'param2')
     * or remove_params(array('param1', 'param2')).
     *
     * @param mixed $params either an array of param names, or a string param name,
     * @param string $params,... any number of additional param names.
     */
    public function remove_params($params = NULL) {
        if (empty($params)) {
            $this->params = array();
            return;
        }
        if (!is_array($params)) {
            $params = func_get_args();
        }
        foreach ($params as $param) {
            if (isset($this->params[$param])) {
                unset($this->params[$param]);
            }
        }
    }

    /**
     * Add a param to the params for this page.
     *
     * The added param overrides existing one if theyhave the same name.
     *
     * @param string $paramname name
     * @param string $param Param value. Defaults to null. If null then return value of param 'name'
     * @return void|string If $param was null then the value of $paramname was returned
     *      (null is returned if that param does not exist).
     */
    public function param($paramname, $param = null) {
        if (!is_null($param)) {
            $this->params = array($paramname => $param) + $this->params;
        } else if (array_key_exists($paramname, $this->params)) {
            return $this->params[$paramname];
        } else {
            return null;
        }
    }

    /**
     * Get the params as as a query string.
     *
     * @param array $overrideparams params to add to the output params, these
     *      override existing ones with the same name.
     * @param boolean $escaped Use &amp; as params separator instead of plain &
     * @return string query string that can be added to a url.
     */
    public function get_query_string($overrideparams = array(), $escaped = true) {
        $arr = array();
        $params = $overrideparams + $this->params;
        foreach ($params as $key => $val) {
           $arr[] = urlencode($key)."=".urlencode($val);
        }
        if ($escaped) {
            return implode('&amp;', $arr);
        } else {
            return implode('&', $arr);
        }
    }

    /**
     * Outputs params as hidden form elements.
     *
     * @param array $exclude params to ignore
     * @param integer $indent indentation
     * @param array $overrideparams params to add to the output params, these
     *      override existing ones with the same name.
     * @return string html for form elements.
     */
    public function hidden_params_out($exclude = array(), $indent = 0, $overrideparams=array()) {
        $tabindent = str_repeat("\t", $indent);
        $str = '';
        $params = $overrideparams + $this->params;
        foreach ($params as $key => $val) {
            if (FALSE === array_search($key, $exclude)) {
                $val = s($val);
                $str.= "$tabindent<input type=\"hidden\" name=\"$key\" value=\"$val\" />\n";
            }
        }
        return $str;
    }

    /**
     * Output url
     *
     * If you use the returned URL in HTML code, you want the escaped ampersands. If you use
     * the returned URL in HTTP headers, you want $escaped=false.
     *
     * @param boolean $omitquerystring whether to output page params as a query string in the url.
     * @param array $overrideparams params to add to the output url, these override existing ones with the same name.
     * @param boolean $escaped Use &amp; as params separator instead of plain &
     * @return string Resulting URL
     */
    public function out($omitquerystring = false, $overrideparams = array(), $escaped = true) {
        $uri = $this->scheme ? $this->scheme.':'.((strtolower($this->scheme) == 'mailto') ? '':'//'): '';
        $uri .= $this->user ? $this->user.($this->pass? ':'.$this->pass:'').'@':'';
        $uri .= $this->host ? $this->host : '';
        $uri .= $this->port ? ':'.$this->port : '';
        $uri .= $this->path ? $this->path : '';
        if (!$omitquerystring) {
            $querystring = $this->get_query_string($overrideparams, $escaped);
            if ($querystring) {
                $uri .= '?' . $querystring;
            }
        }
        $uri .= $this->fragment ? '#'.$this->fragment : '';
        return $uri;
    }

    /**
     * Return a URL relative to $CFG->wwwroot.
     *
     * Throws an exception if this URL does not start with $CFG->wwwroot.
     *
     * The main use for this is when you want to pass a returnurl from one script to another.
     * In this case the returnurl should be relative to $CFG->wwwroot for two reasons.
     * First, it is shorter. More imporatantly, some web servers (e.g. IIS by default)
     * give a 'security' error if you try to pass a full URL as a GET parameter in another URL.
     *
     * @return string the URL relative to $CFG->wwwroot. Note, you will need to urlencode
     * this result if you are outputting a URL manually (but not if you are adding
     * it to another moodle_url).
     */
    public function out_returnurl() {
        global $CFG;
        $fulluri = $this->out(false, array(), false);
        $uri = str_replace($CFG->wwwroot . '/', '', $fulluri);
        if ($uri == $fulluri) {
            throw new coding_exception('This URL (' . $fulluri . ') is not relative to $CFG->wwwroot.');
        }
        return $uri;
    }

    /**
     * Output action url with sesskey
     *
     * Adds sesskey and overriderparams then calls {@link out()}
     * @see out()
     *
     * @param array $overrideparams Allows you to override params
     * @return string url
     */
    public function out_action($overrideparams = array()) {
        $overrideparams = array('sesskey'=> sesskey()) + $overrideparams;
        return $this->out(false, $overrideparams);
    }

    /**
     * Compares this moodle_url with another
     * See documentation of constants for an explanation of the comparison flags.
     * @param moodle_url $url The moodle_url object to compare
     * @param int $matchtype The type of comparison (URL_MATCH_BASE, URL_MATCH_PARAMS, URL_MATCH_EXACT)
     * @return boolean
     */
    public function compare(moodle_url $url, $matchtype = URL_MATCH_EXACT) {

        $baseself = $this->out(true);
        $baseother = $url->out(true);

        // Append index.php if there is no specific file
        if (substr($baseself,-1)=='/') {
            $baseself .= 'index.php';
        }
        if (substr($baseother,-1)=='/') {
            $baseother .= 'index.php';
        }

        // Compare the two base URLs
        if ($baseself != $baseother) {
            return false;
        }

        if ($matchtype == URL_MATCH_BASE) {
            return true;
        }

        $urlparams = $url->params();
        foreach ($this->params() as $param => $value) {
            if ($param == 'sesskey') {
                continue;
            }
            if (!array_key_exists($param, $urlparams) || $urlparams[$param] != $value) {
                return false;
            }
        }

        if ($matchtype == URL_MATCH_PARAMS) {
            return true;
        }

        foreach ($urlparams as $param => $value) {
            if ($param == 'sesskey') {
                continue;
            }
            if (!array_key_exists($param, $this->params()) || $this->param($param) != $value) {
                return false;
            }
        }

        return true;
    }

    /**
     * Sets the anchor for the URI (the bit after the hash)
     * @param string $anchor
     */
    public function set_anchor($anchor) {
        // Match the anchor against the NMTOKEN spec
        if (preg_match('#[a-zA-Z\_\:][a-zA-Z0-9\_\-\.\:]*#', $anchor)) {
            $this->fragment = $anchor;
        }
    }
}

/**
 * Given an unknown $url type (string or moodle_url), returns a string URL.
 * A relative URL is handled with $PAGE->url but gives a debugging error.
 *
 * @param mixed $url The URL (moodle_url or string)
 * @param bool $stripformparams Whether or not to strip the query params from the URL
 * @return string the URL. &s are unescaped. You must use s(...) to output this to XHTML. ($OUTPUT normally does this automatically.)
 */
function prepare_url($url, $stripformparams=false) {
    global $CFG, $PAGE;

    $output = $url;

    if ($url instanceof moodle_url) {
        $output = $url->out($stripformparams, array(), false);
    }

    // Handle relative URLs
    if (substr($output, 0, 4) != 'http' && substr($output, 0, 1) != '/') {
        if (preg_match('/(.*)\/([A-Za-z0-9-_]*\.php)$/', $PAGE->url->out(true), $matches)) {
            return $matches[1] . "/$output";
        } else if ($output == '') {
            return $PAGE->url->out(false, array(), false) . '#';
        } else {
            throw new coding_exception('Unrecognied URL scheme. Please check the formatting of the URL passed to this function. Absolute URLs are the preferred scheme.');
        }
    }

    // Add wwwroot only if the URL does not already start with http:// or https://
    if (!preg_match('|https?://|', $output) ) {
        $output = $CFG->wwwroot . $output;
    }

    return $output;
}

/**
 * Determine if there is data waiting to be processed from a form
 *
 * Used on most forms in Moodle to check for data
 * Returns the data as an object, if it's found.
 * This object can be used in foreach loops without
 * casting because it's cast to (array) automatically
 *
 * Checks that submitted POST data exists and returns it as object.
 *
 * @uses $_POST
 * @return mixed false or object
 */
function data_submitted() {

    if (empty($_POST)) {
        return false;
    } else {
        return (object)$_POST;
    }
}

/**
 * Given some normal text this function will break up any
 * long words to a given size by inserting the given character
 *
 * It's multibyte savvy and doesn't change anything inside html tags.
 *
 * @param string $string the string to be modified
 * @param int $maxsize maximum length of the string to be returned
 * @param string $cutchar the string used to represent word breaks
 * @return string
 */
function break_up_long_words($string, $maxsize=20, $cutchar=' ') {

/// Loading the textlib singleton instance. We are going to need it.
    $textlib = textlib_get_instance();

/// First of all, save all the tags inside the text to skip them
    $tags = array();
    filter_save_tags($string,$tags);

/// Process the string adding the cut when necessary
    $output = '';
    $length = $textlib->strlen($string);
    $wordlength = 0;

    for ($i=0; $i<$length; $i++) {
        $char = $textlib->substr($string, $i, 1);
        if ($char == ' ' or $char == "\t" or $char == "\n" or $char == "\r" or $char == "<" or $char == ">") {
            $wordlength = 0;
        } else {
            $wordlength++;
            if ($wordlength > $maxsize) {
                $output .= $cutchar;
                $wordlength = 0;
            }
        }
        $output .= $char;
    }

/// Finally load the tags back again
    if (!empty($tags)) {
        $output = str_replace(array_keys($tags), $tags, $output);
    }

    return $output;
}

/**
 * Try and close the current window using JavaScript, either immediately, or after a delay.
 *
 * Echo's out the resulting XHTML & javascript
 *
 * @global object
 * @global object
 * @param integer $delay a delay in seconds before closing the window. Default 0.
 * @param boolean $reloadopener if true, we will see if this window was a pop-up, and try
 *      to reload the parent window before this one closes.
 */
function close_window($delay = 0, $reloadopener = false) {
    global $THEME, $PAGE, $OUTPUT;

    if (!$PAGE->headerprinted) {
        $PAGE->set_title(get_string('closewindow'));
        echo $OUTPUT->header();
    } else {
        print_container_end_all(false, $THEME->open_header_containers);
    }

    if ($reloadopener) {
        $function = 'close_window_reloading_opener';
    } else {
        $function = 'close_window';
    }
    echo '<p class="centerpara">' . get_string('windowclosing') . '</p>';

    $PAGE->requires->js_function_call($function)->after_delay($delay);

    echo $OUTPUT->footer();
    exit;
}

/**
 * Returns a string containing a link to the user documentation for the current
 * page. Also contains an icon by default. Shown to teachers and admin only.
 *
 * @global object
 * @global object
 * @param string $text The text to be displayed for the link
 * @param string $iconpath The path to the icon to be displayed
 * @return string The link to user documentation for this current page
 */
function page_doc_link($text='', $iconpath='') {
    global $CFG, $PAGE, $OUTPUT;

    if (empty($CFG->docroot) || during_initial_install()) {
        return '';
    }
    if (!has_capability('moodle/site:doclinks', $PAGE->context)) {
        return '';
    }

    $path = $PAGE->docspath;
    if (!$path) {
        return '';
    }
    return $OUTPUT->doc_link($path, $text, $iconpath);
}


/**
 * Validates an email to make sure it makes sense.
 *
 * @param string $address The email address to validate.
 * @return boolean
 */
function validate_email($address) {

    return (preg_match('#^[-!\#$%&\'*+\\/0-9=?A-Z^_`a-z{|}~]+'.
                 '(\.[-!\#$%&\'*+\\/0-9=?A-Z^_`a-z{|}~]+)*'.
                  '@'.
                  '[-!\#$%&\'*+\\/0-9=?A-Z^_`a-z{|}~]+\.'.
                  '[-!\#$%&\'*+\\./0-9=?A-Z^_`a-z{|}~]+$#',
                  $address));
}

/**
 * Extracts file argument either from file parameter or PATH_INFO
 * Note: $scriptname parameter is not needed anymore
 *
 * @global string
 * @uses $_SERVER
 * @uses PARAM_PATH
 * @return string file path (only safe characters)
 */
function get_file_argument() {
    global $SCRIPT;

    $relativepath = optional_param('file', FALSE, PARAM_PATH);

    // then try extract file from PATH_INFO (slasharguments method)
    if ($relativepath === false and isset($_SERVER['PATH_INFO']) and $_SERVER['PATH_INFO'] !== '') {
        // check that PATH_INFO works == must not contain the script name
        if (strpos($_SERVER['PATH_INFO'], $SCRIPT) === false) {
            $relativepath = clean_param(urldecode($_SERVER['PATH_INFO']), PARAM_PATH);
        }
    }

    // note: we are not using any other way because they are not compatible with unicode file names ;-)

    return $relativepath;
}

/**
 * Just returns an array of text formats suitable for a popup menu
 *
 * @uses FORMAT_MOODLE
 * @uses FORMAT_HTML
 * @uses FORMAT_PLAIN
 * @uses FORMAT_MARKDOWN
 * @return array
 */
function format_text_menu() {

    return array (FORMAT_MOODLE => get_string('formattext'),
                  FORMAT_HTML   => get_string('formathtml'),
                  FORMAT_PLAIN  => get_string('formatplain'),
                  FORMAT_MARKDOWN  => get_string('formatmarkdown'));
}

/**
 * Given text in a variety of format codings, this function returns
 * the text as safe HTML.
 *
 * This function should mainly be used for long strings like posts,
 * answers, glossary items etc. For short strings @see format_string().
 *
 * @todo Finish documenting this function
 *
 * @global object
 * @global object
 * @global object
 * @global object
 * @uses FORMAT_MOODLE
 * @uses FORMAT_HTML
 * @uses FORMAT_PLAIN
 * @uses FORMAT_WIKI
 * @uses FORMAT_MARKDOWN
 * @uses CLI_SCRIPT
 * @staticvar array $croncache
 * @param string $text The text to be formatted. This is raw text originally from user input.
 * @param int $format Identifier of the text format to be used
 *            [FORMAT_MOODLE, FORMAT_HTML, FORMAT_PLAIN, FORMAT_WIKI, FORMAT_MARKDOWN]
 * @param object $options ?
 * @param int $courseid The courseid to use, defaults to $COURSE->courseid
 * @return string
 */
function format_text($text, $format=FORMAT_MOODLE, $options=NULL, $courseid=NULL) {
    global $CFG, $COURSE, $DB, $PAGE;

    static $croncache = array();

    $hashstr = '';

    if ($text === '') {
        return ''; // no need to do any filters and cleaning
    }
    if (!empty($options->comments) && !empty($CFG->usecomments)) {
        require_once($CFG->libdir . '/commentlib.php');
        $comment = new comment($options->comments);
        $cmt = $comment->init(true);
    } else {
        $cmt = '';
    }


    if (!isset($options->trusted)) {
        $options->trusted = false;
    }
    if (!isset($options->noclean)) {
        if ($options->trusted and trusttext_active()) {
            // no cleaning if text trusted and noclean not specified
            $options->noclean=true;
        } else {
            $options->noclean=false;
        }
    }
    if (!isset($options->nocache)) {
        $options->nocache=false;
    }
    if (!isset($options->smiley)) {
        $options->smiley=true;
    }
    if (!isset($options->filter)) {
        $options->filter=true;
    }
    if (!isset($options->para)) {
        $options->para=true;
    }
    if (!isset($options->newlines)) {
        $options->newlines=true;
    }
    if (empty($courseid)) {
        $courseid = $COURSE->id;
    }

    if ($options->filter) {
        $filtermanager = filter_manager::instance();
    } else {
        $filtermanager = new null_filter_manager();
    }
    $context = $PAGE->context;

    if (!empty($CFG->cachetext) and empty($options->nocache)) {
        $hashstr .= $text.'-'.$filtermanager->text_filtering_hash($context, $courseid).'-'.(int)$courseid.'-'.current_language().'-'.
                (int)$format.(int)$options->trusted.(int)$options->noclean.(int)$options->smiley.
                (int)$options->filter.(int)$options->para.(int)$options->newlines;

        $time = time() - $CFG->cachetext;
        $md5key = md5($hashstr);
        if (CLI_SCRIPT) {
            if (isset($croncache[$md5key])) {
                return $croncache[$md5key].$cmt;
            }
        }

        if ($oldcacheitem = $DB->get_record('cache_text', array('md5key'=>$md5key), '*', IGNORE_MULTIPLE)) {
            if ($oldcacheitem->timemodified >= $time) {
                if (CLI_SCRIPT) {
                    if (count($croncache) > 150) {
                        reset($croncache);
                        $key = key($croncache);
                        unset($croncache[$key]);
                    }
                    $croncache[$md5key] = $oldcacheitem->formattedtext;
                }
                return $oldcacheitem->formattedtext.$cmt;
            }
        }
    }

    switch ($format) {
        case FORMAT_HTML:
            if ($options->smiley) {
                replace_smilies($text);
            }
            if (!$options->noclean) {
                $text = clean_text($text, FORMAT_HTML);
            }
            $text = $filtermanager->filter_text($text, $context, $courseid);
            break;

        case FORMAT_PLAIN:
            $text = s($text); // cleans dangerous JS
            $text = rebuildnolinktag($text);
            $text = str_replace('  ', '&nbsp; ', $text);
            $text = nl2br($text);
            break;

        case FORMAT_WIKI:
            // this format is deprecated
            $text = '<p>NOTICE: Wiki-like formatting has been removed from Moodle.  You should not be seeing
                     this message as all texts should have been converted to Markdown format instead.
                     Please post a bug report to http://moodle.org/bugs with information about where you
                     saw this message.</p>'.s($text);
            break;

        case FORMAT_MARKDOWN:
            $text = markdown_to_html($text);
            if ($options->smiley) {
                replace_smilies($text);
            }
            if (!$options->noclean) {
                $text = clean_text($text, FORMAT_HTML);
            }
            $text = $filtermanager->filter_text($text, $context, $courseid);
            break;

        default:  // FORMAT_MOODLE or anything else
            $text = text_to_html($text, $options->smiley, $options->para, $options->newlines);
            if (!$options->noclean) {
                $text = clean_text($text, FORMAT_HTML);
            }
            $text = $filtermanager->filter_text($text, $context, $courseid);
            break;
    }

    // Warn people that we have removed this old mechanism, just in case they
    // were stupid enough to rely on it.
    if (isset($CFG->currenttextiscacheable)) {
        debugging('Once upon a time, Moodle had a truly evil use of global variables ' .
                'called $CFG->currenttextiscacheable. The good news is that this no ' .
                'longer exists. The bad news is that you seem to be using a filter that '.
                'relies on it. Please seek out and destroy that filter code.', DEBUG_DEVELOPER);
    }

    if (empty($options->nocache) and !empty($CFG->cachetext)) {
        if (CLI_SCRIPT) {
            // special static cron cache - no need to store it in db if its not already there
            if (count($croncache) > 150) {
                reset($croncache);
                $key = key($croncache);
                unset($croncache[$key]);
            }
            $croncache[$md5key] = $text;
            return $text.$cmt;
        }

        $newcacheitem = new object();
        $newcacheitem->md5key = $md5key;
        $newcacheitem->formattedtext = $text;
        $newcacheitem->timemodified = time();
        if ($oldcacheitem) {                               // See bug 4677 for discussion
            $newcacheitem->id = $oldcacheitem->id;
            try {
                $DB->update_record('cache_text', $newcacheitem);   // Update existing record in the cache table
            } catch (dml_exception $e) {
               // It's unlikely that the cron cache cleaner could have
               // deleted this entry in the meantime, as it allows
               // some extra time to cover these cases.
            }
        } else {
            try {
                $DB->insert_record('cache_text', $newcacheitem);   // Insert a new record in the cache table
            } catch (dml_exception $e) {
               // Again, it's possible that another user has caused this
               // record to be created already in the time that it took
               // to traverse this function.  That's OK too, as the
               // call above handles duplicate entries, and eventually
               // the cron cleaner will delete them.
            }
        }
    }
    return $text.$cmt;

}

/**
 * Converts the text format from the value to the 'internal'
 * name or vice versa.
 *
 * $key can either be the value or the name and you get the other back.
 *
 * @uses FORMAT_MOODLE
 * @uses FORMAT_HTML
 * @uses FORMAT_PLAIN
 * @uses FORMAT_MARKDOWN
 * @param mixed $key int 0-4 or string one of 'moodle','html','plain','markdown'
 * @return mixed as above but the other way around!
 */
function text_format_name( $key ) {
  $lookup = array();
  $lookup[FORMAT_MOODLE] = 'moodle';
  $lookup[FORMAT_HTML] = 'html';
  $lookup[FORMAT_PLAIN] = 'plain';
  $lookup[FORMAT_MARKDOWN] = 'markdown';
  $value = "error";
  if (!is_numeric($key)) {
    $key = strtolower( $key );
    $value = array_search( $key, $lookup );
  }
  else {
    if (isset( $lookup[$key] )) {
      $value =  $lookup[ $key ];
    }
  }
  return $value;
}

/**
 * Resets all data related to filters, called during upgrade or when filter settings change.
 *
 * @global object
 * @global object
 * @return void
 */
function reset_text_filters_cache() {
    global $CFG, $DB;

    $DB->delete_records('cache_text');
    $purifdir = $CFG->dataroot.'/cache/htmlpurifier';
    remove_dir($purifdir, true);
}

/**
 * Given a simple string, this function returns the string
 * processed by enabled string filters if $CFG->filterall is enabled
 *
 * This function should be used to print short strings (non html) that
 * need filter processing e.g. activity titles, post subjects,
 * glossary concepts.
 *
 * @global object
 * @global object
 * @global object
 * @staticvar bool $strcache
 * @param string $string The string to be filtered.
 * @param boolean $striplinks To strip any link in the result text.
                              Moodle 1.8 default changed from false to true! MDL-8713
 * @param int $courseid Current course as filters can, potentially, use it
 * @return string
 */
function format_string($string, $striplinks=true, $courseid=NULL ) {
    global $CFG, $COURSE, $PAGE;

    //We'll use a in-memory cache here to speed up repeated strings
    static $strcache = false;

    if ($strcache === false or count($strcache) > 2000 ) { // this number might need some tuning to limit memory usage in cron
        $strcache = array();
    }

    //init course id
    if (empty($courseid)) {
        $courseid = $COURSE->id;
    }

    //Calculate md5
    $md5 = md5($string.'<+>'.$striplinks.'<+>'.$courseid.'<+>'.current_language());

    //Fetch from cache if possible
    if (isset($strcache[$md5])) {
        return $strcache[$md5];
    }

    // First replace all ampersands not followed by html entity code
    // Regular expression moved to its own method for easier unit testing
    $string = replace_ampersands_not_followed_by_entity($string);

    if (!empty($CFG->filterall) && $CFG->version >= 2009040600) { // Avoid errors during the upgrade to the new system.
        $context = $PAGE->context;
        $string = filter_manager::instance()->filter_string($string, $context, $courseid);
    }

    // If the site requires it, strip ALL tags from this string
    if (!empty($CFG->formatstringstriptags)) {
        $string = strip_tags($string);

    } else {
        // Otherwise strip just links if that is required (default)
        if ($striplinks) {  //strip links in string
            $string = strip_links($string);
        }
        $string = clean_text($string);
    }

    //Store to cache
    $strcache[$md5] = $string;

    return $string;
}

/**
 * Given a string, performs a negative lookahead looking for any ampersand character
 * that is not followed by a proper HTML entity. If any is found, it is replaced
 * by &amp;. The string is then returned.
 *
 * @param string $string
 * @return string
 */
function replace_ampersands_not_followed_by_entity($string) {
    return preg_replace("/\&(?![a-zA-Z0-9#]{1,8};)/", "&amp;", $string);
}

/**
 * Given a string, replaces all <a>.*</a> by .* and returns the string.
 *
 * @param string $string
 * @return string
 */
function strip_links($string) {
    return preg_replace('/(<a\s[^>]+?>)(.+?)(<\/a>)/is','$2',$string);
}

/**
 * This expression turns links into something nice in a text format. (Russell Jungwirth)
 *
 * @param string $string
 * @return string
 */
function wikify_links($string) {
    return preg_replace('~(<a [^<]*href=["|\']?([^ "\']*)["|\']?[^>]*>([^<]*)</a>)~i','$3 [ $2 ]', $string);
}

/**
 * Replaces non-standard HTML entities
 *
 * @param string $string
 * @return string
 */
function fix_non_standard_entities($string) {
    $text = preg_replace('/&#0*([0-9]+);?/', '&#$1;', $string);
    $text = preg_replace('/&#x0*([0-9a-fA-F]+);?/', '&#x$1;', $text);
    return $text;
}

/**
 * Given text in a variety of format codings, this function returns
 * the text as plain text suitable for plain email.
 *
 * @uses FORMAT_MOODLE
 * @uses FORMAT_HTML
 * @uses FORMAT_PLAIN
 * @uses FORMAT_WIKI
 * @uses FORMAT_MARKDOWN
 * @param string $text The text to be formatted. This is raw text originally from user input.
 * @param int $format Identifier of the text format to be used
 *            [FORMAT_MOODLE, FORMAT_HTML, FORMAT_PLAIN, FORMAT_WIKI, FORMAT_MARKDOWN]
 * @return string
 */
function format_text_email($text, $format) {

    switch ($format) {

        case FORMAT_PLAIN:
            return $text;
            break;

        case FORMAT_WIKI:
            $text = wiki_to_html($text);
            $text = wikify_links($text);
            return strtr(strip_tags($text), array_flip(get_html_translation_table(HTML_ENTITIES)));
            break;

        case FORMAT_HTML:
            return html_to_text($text);
            break;

        case FORMAT_MOODLE:
        case FORMAT_MARKDOWN:
        default:
            $text = wikify_links($text);
            return strtr(strip_tags($text), array_flip(get_html_translation_table(HTML_ENTITIES)));
            break;
    }
}

/**
 * Given some text in HTML format, this function will pass it
 * through any filters that have been configured for this context.
 *
 * @global object
 * @global object
 * @global object
 * @param string $text The text to be passed through format filters
 * @param int $courseid The current course.
 * @return string the filtered string.
 */
function filter_text($text, $courseid=NULL) {
    global $CFG, $COURSE, $PAGE;

    if (empty($courseid)) {
        $courseid = $COURSE->id;       // (copied from format_text)
    }

    $context = $PAGE->context;

    return filter_manager::instance()->filter_text($text, $context, $courseid);
}
/**
 * Formats activity intro text
 *
 * @global object
 * @uses CONTEXT_MODULE
 * @param string $module name of module
 * @param object $activity instance of activity
 * @param int $cmid course module id
 * @param bool $filter filter resulting html text
 * @return text
 */
function format_module_intro($module, $activity, $cmid, $filter=true) {
    global $CFG;
    require_once("$CFG->libdir/filelib.php");
    $options = (object)array('noclean'=>true, 'para'=>false, 'filter'=>false);
    $context = get_context_instance(CONTEXT_MODULE, $cmid);
    $intro = file_rewrite_pluginfile_urls($activity->intro, 'pluginfile.php', $context->id, $module.'_intro', null);
    return trim(format_text($intro, $activity->introformat, $options));
}

/**
 * Legacy function, used for cleaning of old forum and glossary text only.
 *
 * @global object
 * @param string $text text that may contain TRUSTTEXT marker
 * @return text without any TRUSTTEXT marker
 */
function trusttext_strip($text) {
    global $CFG;

    while (true) { //removing nested TRUSTTEXT
        $orig = $text;
        $text = str_replace('#####TRUSTTEXT#####', '', $text);
        if (strcmp($orig, $text) === 0) {
            return $text;
        }
    }
}

/**
 * Must be called before editing of all texts
 * with trust flag. Removes all XSS nasties
 * from texts stored in database if needed.
 *
 * @param object $object data object with xxx, xxxformat and xxxtrust fields
 * @param string $field name of text field
 * @param object $context active context
 * @return object updated $object
 */
function trusttext_pre_edit($object, $field, $context) {
    $trustfield  = $field.'trust';
    $formatfield = $field.'format';

    if (!$object->$trustfield or !trusttext_trusted($context)) {
        $object->$field = clean_text($object->$field, $object->$formatfield);
    }

    return $object;
}

/**
 * Is current user trusted to enter no dangerous XSS in this context?
 *
 * Please note the user must be in fact trusted everywhere on this server!!
 *
 * @param object $context
 * @return bool true if user trusted
 */
function trusttext_trusted($context) {
    return (trusttext_active() and has_capability('moodle/site:trustcontent', $context));
}

/**
 * Is trusttext feature active?
 *
 * @global object
 * @param object $context
 * @return bool
 */
function trusttext_active() {
    global $CFG;

    return !empty($CFG->enabletrusttext);
}

/**
 * Given raw text (eg typed in by a user), this function cleans it up
 * and removes any nasty tags that could mess up Moodle pages.
 *
 * @uses FORMAT_MOODLE
 * @uses FORMAT_PLAIN
 * @global string
 * @global object
 * @param string $text The text to be cleaned
 * @param int $format Identifier of the text format to be used
 *            [FORMAT_MOODLE, FORMAT_HTML, FORMAT_PLAIN, FORMAT_WIKI, FORMAT_MARKDOWN]
 * @return string The cleaned up text
 */
function clean_text($text, $format=FORMAT_MOODLE) {

    global $ALLOWED_TAGS, $CFG;

    if (empty($text) or is_numeric($text)) {
       return (string)$text;
    }

    switch ($format) {
        case FORMAT_PLAIN:
        case FORMAT_MARKDOWN:
            return $text;

        default:

            if (!empty($CFG->enablehtmlpurifier)) {
                $text = purify_html($text);
            } else {
            /// Fix non standard entity notations
                $text = fix_non_standard_entities($text);

            /// Remove tags that are not allowed
                $text = strip_tags($text, $ALLOWED_TAGS);

            /// Clean up embedded scripts and , using kses
                $text = cleanAttributes($text);

            /// Again remove tags that are not allowed
                $text = strip_tags($text, $ALLOWED_TAGS);

            }

        /// Remove potential script events - some extra protection for undiscovered bugs in our code
            $text = preg_replace("~([^a-z])language([[:space:]]*)=~i", "$1Xlanguage=", $text);
            $text = preg_replace("~([^a-z])on([a-z]+)([[:space:]]*)=~i", "$1Xon$2=", $text);

            return $text;
    }
}

/**
 * KSES replacement cleaning function - uses HTML Purifier.
 *
 * @global object
 * @param string $text The (X)HTML string to purify
 */
function purify_html($text) {
    global $CFG;

    // this can not be done only once because we sometimes need to reset the cache
    $cachedir = $CFG->dataroot.'/cache/htmlpurifier';
    $status = check_dir_exists($cachedir, true, true);

    static $purifier = false;
    static $config;
    if ($purifier === false) {
        require_once $CFG->libdir.'/htmlpurifier/HTMLPurifier.safe-includes.php';
        $config = HTMLPurifier_Config::createDefault();
        $config->set('Core.ConvertDocumentToFragment', true);
        $config->set('Core.Encoding', 'UTF-8');
        $config->set('HTML.Doctype', 'XHTML 1.0 Transitional');
        $config->set('Cache.SerializerPath', $cachedir);
        $config->set('URI.AllowedSchemes', array('http'=>1, 'https'=>1, 'ftp'=>1, 'irc'=>1, 'nntp'=>1, 'news'=>1, 'rtsp'=>1, 'teamspeak'=>1, 'gopher'=>1, 'mms'=>1));
        $config->set('Attr.AllowedFrameTargets', array('_blank'));
        $purifier = new HTMLPurifier($config);
    }
    return $purifier->purify($text);
}

/**
 * This function takes a string and examines it for HTML tags.
 *
 * If tags are detected it passes the string to a helper function {@link cleanAttributes2()}
 * which checks for attributes and filters them for malicious content
 *
 * @param string $str The string to be examined for html tags
 * @return string
 */
function cleanAttributes($str){
    $result = preg_replace_callback(
            '%(<[^>]*(>|$)|>)%m', #search for html tags
            "cleanAttributes2",
            $str
            );
    return  $result;
}

/**
 * This function takes a string with an html tag and strips out any unallowed
 * protocols e.g. javascript:
 *
 * It calls ancillary functions in kses which are prefixed by kses
 *
 * @global object
 * @global string
 * @param array $htmlArray An array from {@link cleanAttributes()}, containing in its 1st
 *              element the html to be cleared
 * @return string
 */
function cleanAttributes2($htmlArray){

    global $CFG, $ALLOWED_PROTOCOLS;
    require_once($CFG->libdir .'/kses.php');

    $htmlTag = $htmlArray[1];
    if (substr($htmlTag, 0, 1) != '<') {
        return '&gt;';  //a single character ">" detected
    }
    if (!preg_match('%^<\s*(/\s*)?([a-zA-Z0-9]+)([^>]*)>?$%', $htmlTag, $matches)) {
        return ''; // It's seriously malformed
    }
    $slash = trim($matches[1]); //trailing xhtml slash
    $elem = $matches[2];    //the element name
    $attrlist = $matches[3]; // the list of attributes as a string

    $attrArray = kses_hair($attrlist, $ALLOWED_PROTOCOLS);

    $attStr = '';
    foreach ($attrArray as $arreach) {
        $arreach['name'] = strtolower($arreach['name']);
        if ($arreach['name'] == 'style') {
            $value = $arreach['value'];
            while (true) {
                $prevvalue = $value;
                $value = kses_no_null($value);
                $value = preg_replace("/\/\*.*\*\//Us", '', $value);
                $value = kses_decode_entities($value);
                $value = preg_replace('/(&#[0-9]+)(;?)/', "\\1;", $value);
                $value = preg_replace('/(&#x[0-9a-fA-F]+)(;?)/', "\\1;", $value);
                if ($value === $prevvalue) {
                    $arreach['value'] = $value;
                    break;
                }
            }
            $arreach['value'] = preg_replace("/j\s*a\s*v\s*a\s*s\s*c\s*r\s*i\s*p\s*t/i", "Xjavascript", $arreach['value']);
            $arreach['value'] = preg_replace("/e\s*x\s*p\s*r\s*e\s*s\s*s\s*i\s*o\s*n/i", "Xexpression", $arreach['value']);
            $arreach['value'] = preg_replace("/b\s*i\s*n\s*d\s*i\s*n\s*g/i", "Xbinding", $arreach['value']);
        } else if ($arreach['name'] == 'href') {
            //Adobe Acrobat Reader XSS protection
            $arreach['value'] = preg_replace('/(\.(pdf|fdf|xfdf|xdp|xfd)[^#]*)#.*$/i', '$1', $arreach['value']);
        }
        $attStr .=  ' '.$arreach['name'].'="'.$arreach['value'].'"';
    }

    $xhtml_slash = '';
    if (preg_match('%/\s*$%', $attrlist)) {
        $xhtml_slash = ' /';
    }
    return '<'. $slash . $elem . $attStr . $xhtml_slash .'>';
}

/**
 * Replaces all known smileys in the text with image equivalents
 *
 * @global object
 * @staticvar array $e
 * @staticvar array $img
 * @staticvar array $emoticons
 * @param string $text Passed by reference. The string to search for smily strings.
 * @return string
 */
function replace_smilies(&$text) {
    global $CFG, $OUTPUT;

    if (empty($CFG->emoticons)) { /// No emoticons defined, nothing to process here
        return;
    }

    $lang = current_language();
    $emoticonstring = $CFG->emoticons;
    static $e = array();
    static $img = array();
    static $emoticons = null;

    if (is_null($emoticons)) {
        $emoticons = array();
        if ($emoticonstring) {
            $items = explode('{;}', $CFG->emoticons);
            foreach ($items as $item) {
               $item = explode('{:}', $item);
              $emoticons[$item[0]] = $item[1];
            }
        }
    }

    if (empty($img[$lang])) {  /// After the first time this is not run again
        $e[$lang] = array();
        $img[$lang] = array();
        foreach ($emoticons as $emoticon => $image){
            $alttext = get_string($image, 'pix');
            $alttext = preg_replace('/^\[\[(.*)\]\]$/', '$1', $alttext); /// Clean alttext in case there isn't lang string for it.
            $e[$lang][] = $emoticon;
            $img[$lang][] = '<img alt="'. $alttext .'" width="15" height="15" src="'. $OUTPUT->old_icon_url('s/' . $image) . '" />';
        }
    }

    // Exclude from transformations all the code inside <script> tags
    // Needed to solve Bug 1185. Thanks to jouse 2001 detecting it. :-)
    // Based on code from glossary fiter by Williams Castillo.
    //       - Eloy

    // Detect all the <script> zones to take out
    $excludes = array();
    preg_match_all('/<script language(.+?)<\/script>/is',$text,$list_of_excludes);

    // Take out all the <script> zones from text
    foreach (array_unique($list_of_excludes[0]) as $key=>$value) {
        $excludes['<+'.$key.'+>'] = $value;
    }
    if ($excludes) {
        $text = str_replace($excludes,array_keys($excludes),$text);
    }

/// this is the meat of the code - this is run every time
    $text = str_replace($e[$lang], $img[$lang], $text);

    // Recover all the <script> zones to text
    if ($excludes) {
        $text = str_replace(array_keys($excludes),$excludes,$text);
    }
}

/**
 * This code is called from help.php to inject a list of smilies into the
 * emoticons help file.
 *
 * @global object
 * @global object
 * @return string HTML for a list of smilies.
 */
function get_emoticons_list_for_help_file() {
    global $CFG, $SESSION, $PAGE, $OUTPUT;
    if (empty($CFG->emoticons)) {
        return '';
    }

    $items = explode('{;}', $CFG->emoticons);
    $output = '<ul id="emoticonlist">';
    foreach ($items as $item) {
        $item = explode('{:}', $item);
        $output .= '<li><img src="' . $OUTPUT->old_icon_url('s/' . $item[1]) . '" alt="' .
                $item[0] . '" /><code>' . $item[0] . '</code></li>';
    }
    $output .= '</ul>';
    if (!empty($SESSION->inserttextform)) {
        $formname = $SESSION->inserttextform;
        $fieldname = $SESSION->inserttextfield;
    } else {
        $formname = 'theform';
        $fieldname = 'message';
    }

    $PAGE->requires->yui_lib('event');
    $PAGE->requires->js_function_call('emoticons_help.init', array($formname, $fieldname, 'emoticonlist'));
    return $output;

}

/**
 * Given plain text, makes it into HTML as nicely as possible.
 * May contain HTML tags already
 *
 * @global object
 * @param string $text The string to convert.
 * @param boolean $smiley Convert any smiley characters to smiley images?
 * @param boolean $para If true then the returned string will be wrapped in div tags
 * @param boolean $newlines If true then lines newline breaks will be converted to HTML newline breaks.
 * @return string
 */

function text_to_html($text, $smiley=true, $para=true, $newlines=true) {
///

    global $CFG;

/// Remove any whitespace that may be between HTML tags
    $text = preg_replace("~>([[:space:]]+)<~i", "><", $text);

/// Remove any returns that precede or follow HTML tags
    $text = preg_replace("~([\n\r])<~i", " <", $text);
    $text = preg_replace("~>([\n\r])~i", "> ", $text);

    convert_urls_into_links($text);

/// Make returns into HTML newlines.
    if ($newlines) {
        $text = nl2br($text);
    }

/// Turn smileys into images.
    if ($smiley) {
        replace_smilies($text);
    }

/// Wrap the whole thing in a div if required
    if ($para) {
        //return '<p>'.$text.'</p>'; //1.9 version
        return '<div class="text_to_html">'.$text.'</div>';
    } else {
        return $text;
    }
}

/**
 * Given Markdown formatted text, make it into XHTML using external function
 *
 * @global object
 * @param string $text The markdown formatted text to be converted.
 * @return string Converted text
 */
function markdown_to_html($text) {
    global $CFG;

    require_once($CFG->libdir .'/markdown.php');

    return Markdown($text);
}

/**
 * Given HTML text, make it into plain text using external function
 *
 * @global object
 * @param string $html The text to be converted.
 * @return string
 */
function html_to_text($html) {

    global $CFG;

    require_once($CFG->libdir .'/html2text.php');

    $h2t = new html2text($html);
    $result = $h2t->get_text();

    return $result;
}

/**
 * Given some text this function converts any URLs it finds into HTML links
 *
 * @param string $text Passed in by reference. The string to be searched for urls.
 */
function convert_urls_into_links(&$text) {
/// Make lone URLs into links.   eg http://moodle.com/
    $text = preg_replace("~([[:space:]]|^|\(|\[)([[:alnum:]]+)://([^[:space:]]*)([[:alnum:]#?/&=])~i",
                          '$1<a href="$2://$3$4">$2://$3$4</a>', $text);

/// eg www.moodle.com
    $text = preg_replace("~([[:space:]]|^|\(|\[)www\.([^[:space:]]*)([[:alnum:]#?/&=])~i",
                          '$1<a href="http://www.$2$3">www.$2$3</a>', $text);
}

/**
 * This function will highlight search words in a given string
 *
 * It cares about HTML and will not ruin links.  It's best to use
 * this function after performing any conversions to HTML.
 *
 * @param string $needle The search string. Syntax like "word1 +word2 -word3" is dealt with correctly.
 * @param string $haystack The string (HTML) within which to highlight the search terms.
 * @param boolean $matchcase whether to do case-sensitive. Default case-insensitive.
 * @param string $prefix the string to put before each search term found.
 * @param string $suffix the string to put after each search term found.
 * @return string The highlighted HTML.
 */
function highlight($needle, $haystack, $matchcase = false,
        $prefix = '<span class="highlight">', $suffix = '</span>') {

/// Quick bail-out in trivial cases.
    if (empty($needle) or empty($haystack)) {
        return $haystack;
    }

/// Break up the search term into words, discard any -words and build a regexp.
    $words = preg_split('/ +/', trim($needle));
    foreach ($words as $index => $word) {
        if (strpos($word, '-') === 0) {
            unset($words[$index]);
        } else if (strpos($word, '+') === 0) {
            $words[$index] = '\b' . preg_quote(ltrim($word, '+'), '/') . '\b'; // Match only as a complete word.
        } else {
            $words[$index] = preg_quote($word, '/');
        }
    }
    $regexp = '/(' . implode('|', $words) . ')/u'; // u is do UTF-8 matching.
    if (!$matchcase) {
        $regexp .= 'i';
    }

/// Another chance to bail-out if $search was only -words
    if (empty($words)) {
        return $haystack;
    }

/// Find all the HTML tags in the input, and store them in a placeholders array.
    $placeholders = array();
    $matches = array();
    preg_match_all('/<[^>]*>/', $haystack, $matches);
    foreach (array_unique($matches[0]) as $key => $htmltag) {
        $placeholders['<|' . $key . '|>'] = $htmltag;
    }

/// In $hastack, replace each HTML tag with the corresponding placeholder.
    $haystack = str_replace($placeholders, array_keys($placeholders), $haystack);

/// In the resulting string, Do the highlighting.
    $haystack = preg_replace($regexp, $prefix . '$1' . $suffix, $haystack);

/// Turn the placeholders back into HTML tags.
    $haystack = str_replace(array_keys($placeholders), $placeholders, $haystack);

    return $haystack;
}

/**
 * This function will highlight instances of $needle in $haystack
 *
 * It's faster that the above function {@link highlight()} and doesn't care about
 * HTML or anything.
 *
 * @param string $needle The string to search for
 * @param string $haystack The string to search for $needle in
 * @return string The highlighted HTML
 */
function highlightfast($needle, $haystack) {

    if (empty($needle) or empty($haystack)) {
        return $haystack;
    }

    $parts = explode(moodle_strtolower($needle), moodle_strtolower($haystack));

    if (count($parts) === 1) {
        return $haystack;
    }

    $pos = 0;

    foreach ($parts as $key => $part) {
        $parts[$key] = substr($haystack, $pos, strlen($part));
        $pos += strlen($part);

        $parts[$key] .= '<span class="highlight">'.substr($haystack, $pos, strlen($needle)).'</span>';
        $pos += strlen($needle);
    }

    return str_replace('<span class="highlight"></span>', '', join('', $parts));
}

/**
 * Return a string containing 'lang', xml:lang and optionally 'dir' HTML attributes.
 * Internationalisation, for print_header and backup/restorelib.
 *
 * @param bool $dir Default false
 * @return string Attributes
 */
function get_html_lang($dir = false) {
    $direction = '';
    if ($dir) {
        if (get_string('thisdirection') == 'rtl') {
            $direction = ' dir="rtl"';
        } else {
            $direction = ' dir="ltr"';
        }
    }
    //Accessibility: added the 'lang' attribute to $direction, used in theme <html> tag.
    $language = str_replace('_', '-', str_replace('_utf8', '', current_language()));
    @header('Content-Language: '.$language);
    return ($direction.' lang="'.$language.'" xml:lang="'.$language.'"');
}


/// STANDARD WEB PAGE PARTS ///////////////////////////////////////////////////

/**
 * Send the HTTP headers that Moodle requires.
 * @param $cacheable Can this page be cached on back?
 */
function send_headers($contenttype, $cacheable = true) {
    @header('Content-Type: ' . $contenttype);
    @header('Content-Script-Type: text/javascript');
    @header('Content-Style-Type: text/css');

    if ($cacheable) {
        // Allow caching on "back" (but not on normal clicks)
        @header('Cache-Control: private, pre-check=0, post-check=0, max-age=0');
        @header('Pragma: no-cache');
        @header('Expires: ');
    } else {
        // Do everything we can to always prevent clients and proxies caching
        @header('Cache-Control: no-store, no-cache, must-revalidate');
        @header('Cache-Control: post-check=0, pre-check=0', false);
        @header('Pragma: no-cache');
        @header('Expires: Mon, 20 Aug 1969 09:23:00 GMT');
        @header('Last-Modified: ' . gmdate('D, d M Y H:i:s') . ' GMT');
    }
    @header('Accept-Ranges: none');
}

/**
 * Returns text to be displayed to the user which reflects their login status
 *
 * @global object
 * @global object
 * @global object
 * @global object
 * @uses CONTEXT_COURSE
 * @param course $course {@link $COURSE} object containing course information
 * @param user $user {@link $USER} object containing user information
 * @return string HTML
 */
function user_login_string($course=NULL, $user=NULL) {
    global $USER, $CFG, $SITE, $DB;

    if (during_initial_install()) {
        return '';
    }

    if (empty($user) and !empty($USER->id)) {
        $user = $USER;
    }

    if (empty($course)) {
        $course = $SITE;
    }

    if (session_is_loggedinas()) {
        $realuser = session_get_realuser();
        $fullname = fullname($realuser, true);
        $realuserinfo = " [<a $CFG->frametarget
        href=\"$CFG->wwwroot/course/loginas.php?id=$course->id&amp;return=1&amp;sesskey=".sesskey()."\">$fullname</a>] ";
    } else {
        $realuserinfo = '';
    }

    $loginurl = get_login_url();

    if (empty($course->id)) {
        // $course->id is not defined during installation
        return '';
    } else if (!empty($user->id)) {
        $context = get_context_instance(CONTEXT_COURSE, $course->id);

        $fullname = fullname($user, true);
        $username = "<a $CFG->frametarget href=\"$CFG->wwwroot/user/view.php?id=$user->id&amp;course=$course->id\">$fullname</a>";
        if (is_mnet_remote_user($user) and $idprovider = $DB->get_record('mnet_host', array('id'=>$user->mnethostid))) {
            $username .= " from <a $CFG->frametarget href=\"{$idprovider->wwwroot}\">{$idprovider->name}</a>";
        }
        if (isset($user->username) && $user->username == 'guest') {
            $loggedinas = $realuserinfo.get_string('loggedinasguest').
                      " (<a $CFG->frametarget href=\"$loginurl\">".get_string('login').'</a>)';
        } else if (!empty($user->access['rsw'][$context->path])) {
            $rolename = '';
            if ($role = $DB->get_record('role', array('id'=>$user->access['rsw'][$context->path]))) {
                $rolename = ': '.format_string($role->name);
            }
            $loggedinas = get_string('loggedinas', 'moodle', $username).$rolename.
                      " (<a $CFG->frametarget
                      href=\"$CFG->wwwroot/course/view.php?id=$course->id&amp;switchrole=0&amp;sesskey=".sesskey()."\">".get_string('switchrolereturn').'</a>)';
        } else {
            $loggedinas = $realuserinfo.get_string('loggedinas', 'moodle', $username).' '.
                      " (<a $CFG->frametarget href=\"$CFG->wwwroot/login/logout.php?sesskey=".sesskey()."\">".get_string('logout').'</a>)';
        }
    } else {
        $loggedinas = get_string('loggedinnot', 'moodle').
                      " (<a $CFG->frametarget href=\"$loginurl\">".get_string('login').'</a>)';
    }

    $loggedinas = '<div class="logininfo">'.$loggedinas.'</div>';

    if (isset($SESSION->justloggedin)) {
        unset($SESSION->justloggedin);
        if (!empty($CFG->displayloginfailures)) {
            if (!empty($USER->username) and $USER->username != 'guest') {
                if ($count = count_login_failures($CFG->displayloginfailures, $USER->username, $USER->lastlogin)) {
                    $loggedinas .= '&nbsp;<div class="loginfailures">';
                    if (empty($count->accounts)) {
                        $loggedinas .= get_string('failedloginattempts', '', $count);
                    } else {
                        $loggedinas .= get_string('failedloginattemptsall', '', $count);
                    }
                    if (has_capability('coursereport/log:view', get_context_instance(CONTEXT_SYSTEM))) {
                        $loggedinas .= ' (<a href="'.$CFG->wwwroot.'/course/report/log/index.php'.
                                             '?chooselog=1&amp;id=1&amp;modid=site_errors">'.get_string('logs').'</a>)';
                    }
                    $loggedinas .= '</div>';
                }
            }
        }
    }

    return $loggedinas;
}

/**
 * Tests whether $THEME->rarrow, $THEME->larrow have been set (theme/-/config.php).
 * If not it applies sensible defaults.
 *
 * Accessibility: right and left arrow Unicode characters for breadcrumb, calendar,
 * search forum block, etc. Important: these are 'silent' in a screen-reader
 * (unlike &gt; &raquo;), and must be accompanied by text.
 *
 * @global object
 * @uses $_SERVER
 */
function check_theme_arrows() {
    global $THEME;

    if (!isset($THEME->rarrow) and !isset($THEME->larrow)) {
        // Default, looks good in Win XP/IE 6, Win/Firefox 1.5, Win/Netscape 8...
        // Also OK in Win 9x/2K/IE 5.x
        $THEME->rarrow = '&#x25BA;';
        $THEME->larrow = '&#x25C4;';
        if (empty($_SERVER['HTTP_USER_AGENT'])) {
            $uagent = '';
        } else {
            $uagent = $_SERVER['HTTP_USER_AGENT'];
        }
        if (false !== strpos($uagent, 'Opera')
            || false !== strpos($uagent, 'Mac')) {
            // Looks good in Win XP/Mac/Opera 8/9, Mac/Firefox 2, Camino, Safari.
            // Not broken in Mac/IE 5, Mac/Netscape 7 (?).
            $THEME->rarrow = '&#x25B6;';
            $THEME->larrow = '&#x25C0;';
        }
        elseif (false !== strpos($uagent, 'Konqueror')) {
            $THEME->rarrow = '&rarr;';
            $THEME->larrow = '&larr;';
        }
        elseif (isset($_SERVER['HTTP_ACCEPT_CHARSET'])
            && false === stripos($_SERVER['HTTP_ACCEPT_CHARSET'], 'utf-8')) {
            // (Win/IE 5 doesn't set ACCEPT_CHARSET, but handles Unicode.)
            // To be safe, non-Unicode browsers!
            $THEME->rarrow = '&gt;';
            $THEME->larrow = '&lt;';
        }

    /// RTL support - in RTL languages, swap r and l arrows
        if (right_to_left()) {
            $t = $THEME->rarrow;
            $THEME->rarrow = $THEME->larrow;
            $THEME->larrow = $t;
        }
    }
}


/**
 * Return the right arrow with text ('next'), and optionally embedded in a link.
 * See function above, check_theme_arrows.
 *
 * @global object
 * @param string $text HTML/plain text label (set to blank only for breadcrumb separator cases).
 * @param string $url An optional link to use in a surrounding HTML anchor.
 * @param bool $accesshide True if text should be hidden (for screen readers only).
 * @param string $addclass Additional class names for the link, or the arrow character.
 * @return string HTML string.
 */
function link_arrow_right($text, $url='', $accesshide=false, $addclass='') {
    global $THEME;
    check_theme_arrows();
    $arrowclass = 'arrow ';
    if (! $url) {
        $arrowclass .= $addclass;
    }
    $arrow = '<span class="'.$arrowclass.'">'.$THEME->rarrow.'</span>';
    $htmltext = '';
    if ($text) {
        $htmltext = '<span class="arrow_text">'.$text.'</span>&nbsp;';
        if ($accesshide) {
            $htmltext = get_accesshide($htmltext);
        }
    }
    if ($url) {
        $class = 'arrow_link';
        if ($addclass) {
            $class .= ' '.$addclass;
        }
        return '<a class="'.$class.'" href="'.$url.'" title="'.preg_replace('/<.*?>/','',$text).'">'.$htmltext.$arrow.'</a>';
    }
    return $htmltext.$arrow;
}

/**
 * Return the left arrow with text ('previous'), and optionally embedded in a link.
 * See function above, check_theme_arrows.
 *
 * @global object
 * @param string $text HTML/plain text label (set to blank only for breadcrumb separator cases).
 * @param string $url An optional link to use in a surrounding HTML anchor.
 * @param bool $accesshide True if text should be hidden (for screen readers only).
 * @param string $addclass Additional class names for the link, or the arrow character.
 * @return string HTML string.
 */
function link_arrow_left($text, $url='', $accesshide=false, $addclass='') {
    global $THEME;
    check_theme_arrows();
    $arrowclass = 'arrow ';
    if (! $url) {
        $arrowclass .= $addclass;
    }
    $arrow = '<span class="'.$arrowclass.'">'.$THEME->larrow.'</span>';
    $htmltext = '';
    if ($text) {
        $htmltext = '&nbsp;<span class="arrow_text">'.$text.'</span>';
        if ($accesshide) {
            $htmltext = get_accesshide($htmltext);
        }
    }
    if ($url) {
        $class = 'arrow_link';
        if ($addclass) {
            $class .= ' '.$addclass;
        }
        return '<a class="'.$class.'" href="'.$url.'" title="'.preg_replace('/<.*?>/','',$text).'">'.$arrow.$htmltext.'</a>';
    }
    return $arrow.$htmltext;
}

/**
 * Return a HTML element with the class "accesshide", for accessibility.
 * Please use cautiously - where possible, text should be visible!
 *
 * @param string $text Plain text.
 * @param string $elem Lowercase element name, default "span".
 * @param string $class Additional classes for the element.
 * @param string $attrs Additional attributes string in the form, "name='value' name2='value2'"
 * @return string HTML string.
 */
function get_accesshide($text, $elem='span', $class='', $attrs='') {
    return "<$elem class=\"accesshide $class\" $attrs>$text</$elem>";
}

/**
 * Return the breadcrumb trail navigation separator.
 *
 * @return string HTML string.
 */
function get_separator() {
    //Accessibility: the 'hidden' slash is preferred for screen readers.
    return ' '.link_arrow_right($text='/', $url='', $accesshide=true, 'sep').' ';
}

/**
 * Print (or return) a collapisble region, that has a caption that can
 * be clicked to expand or collapse the region.
 *
 * If JavaScript is off, then the region will always be exanded.
 *
 * @param string $contents the contents of the box.
 * @param string $classes class names added to the div that is output.
 * @param string $id id added to the div that is output. Must not be blank.
 * @param string $caption text displayed at the top. Clicking on this will cause the region to expand or contract.
 * @param string $userpref the name of the user preference that stores the user's preferred deafault state.
 *      (May be blank if you do not wish the state to be persisted.
 * @param boolean $default Inital collapsed state to use if the user_preference it not set.
 * @param boolean $return if true, return the HTML as a string, rather than printing it.
 * @return string|void If $return is false, returns nothing, otherwise returns a string of HTML.
 */
function print_collapsible_region($contents, $classes, $id, $caption, $userpref = '', $default = false, $return = false) {
    $output  = print_collapsible_region_start($classes, $id, $caption, $userpref, true);
    $output .= $contents;
    $output .= print_collapsible_region_end(true);

    if ($return) {
        return $output;
    } else {
        echo $output;
    }
}

/**
 * Print (or return) the start of a collapisble region, that has a caption that can
 * be clicked to expand or collapse the region. If JavaScript is off, then the region
 * will always be exanded.
 *
 * @global object
 * @param string $classes class names added to the div that is output.
 * @param string $id id added to the div that is output. Must not be blank.
 * @param string $caption text displayed at the top. Clicking on this will cause the region to expand or contract.
 * @param boolean $userpref the name of the user preference that stores the user's preferred deafault state.
 *      (May be blank if you do not wish the state to be persisted.
 * @param boolean $default Inital collapsed state to use if the user_preference it not set.
 * @param boolean $return if true, return the HTML as a string, rather than printing it.
 * @return string|void if $return is false, returns nothing, otherwise returns a string of HTML.
 */
function print_collapsible_region_start($classes, $id, $caption, $userpref = false, $default = false, $return = false) {
    global $CFG, $PAGE, $OUTPUT;

    // Include required JavaScript libraries.
    $PAGE->requires->yui_lib('animation');

    // Work out the initial state.
    if (is_string($userpref)) {
        user_preference_allow_ajax_update($userpref, PARAM_BOOL);
        $collapsed = get_user_preferences($userpref, $default);
    } else {
        $collapsed = $default;
        $userpref = false;
    }

    if ($collapsed) {
        $classes .= ' collapsed';
    }

    $output = '';
    $output .= '<div id="' . $id . '" class="collapsibleregion ' . $classes . '">';
    $output .= '<div id="' . $id . '_sizer">';
    $output .= '<div id="' . $id . '_caption" class="collapsibleregioncaption">';
    $output .= $caption . ' ';
    $output .= '</div><div id="' . $id . '_inner" class="collapsibleregioninner">';
    $PAGE->requires->js_function_call('new collapsible_region',
            array($id, $userpref, get_string('clicktohideshow'),
            $OUTPUT->old_icon_url('t/collapsed'), $OUTPUT->old_icon_url('t/expanded')));

    if ($return) {
        return $output;
    } else {
        echo $output;
    }
}

/**
 * Close a region started with print_collapsible_region_start.
 *
 * @param boolean $return if true, return the HTML as a string, rather than printing it.
 * @return string|void if $return is false, returns nothing, otherwise returns a string of HTML.
 */
function print_collapsible_region_end($return = false) {
    $output = '</div></div></div>';

    if ($return) {
        return $output;
    } else {
        echo $output;
    }
}

/**
 * Returns number of currently open containers
 *
 * @global object
 * @return int number of open containers
 */
function open_containers() {
    global $THEME;

    if (!isset($THEME->open_containers)) {
        $THEME->open_containers = array();
    }

    return count($THEME->open_containers);
}

/**
 * Force closing of open containers
 *
 * @param boolean $return, return as string or just print it
 * @param int $keep number of containers to be kept open - usually theme or page containers
 * @return mixed string or void
 */
function print_container_end_all($return=false, $keep=0) {
    global $OUTPUT;
    $output = '';
    while (open_containers() > $keep) {
        $output .= $OUTPUT->container_end();
    }
    if ($return) {
        return $output;
    } else {
        echo $output;
    }
}

/**
 * Internal function - do not use directly!
 * Starting part of the surrounding divs for custom corners
 *
 * @param boolean $clearfix, add CLASS "clearfix" to the inner div against collapsing
 * @param string $classes
 * @param mixed $idbase, optionally, define one idbase to be added to all the elements in the corners
 * @return string
 */
function _print_custom_corners_start($clearfix=false, $classes='', $idbase='') {
/// Analise if we want ids for the custom corner elements
    $id = '';
    $idbt = '';
    $idi1 = '';
    $idi2 = '';
    $idi3 = '';

    if ($idbase) {
        $id   = 'id="'.$idbase.'" ';
        $idbt = 'id="'.$idbase.'-bt" ';
        $idi1 = 'id="'.$idbase.'-i1" ';
        $idi2 = 'id="'.$idbase.'-i2" ';
        $idi3 = 'id="'.$idbase.'-i3" ';
    }

/// Calculate current level
    $level = open_containers();

/// Output begins
    $output = '<div '.$id.'class="wrap wraplevel'.$level.' '.$classes.'">'."\n";
    $output .= '<div '.$idbt.'class="bt"><div>&nbsp;</div></div>';
    $output .= "\n";
    $output .= '<div '.$idi1.'class="i1"><div '.$idi2.'class="i2">';
    $output .= (!empty($clearfix)) ? '<div '.$idi3.'class="i3 clearfix">' : '<div '.$idi3.'class="i3">';

    return $output;
}


/**
 * Internal function - do not use directly!
 * Ending part of the surrounding divs for custom corners
 *
 * @param string $idbase
 * @return string HTML sttring
 */
function _print_custom_corners_end($idbase) {
/// Analise if we want ids for the custom corner elements
    $idbb = '';

    if ($idbase) {
        $idbb = 'id="' . $idbase . '-bb" ';
    }

/// Output begins
    $output = '</div></div></div>';
    $output .= "\n";
    $output .= '<div '.$idbb.'class="bb"><div>&nbsp;</div></div>'."\n";
    $output .= '</div>';

    return $output;
}


/**
 * Print a specified group's avatar.
 *
 * @global object
 * @uses CONTEXT_COURSE
 * @param array $group A single {@link group} object OR array of groups.
 * @param int $courseid The course ID.
 * @param boolean $large Default small picture, or large.
 * @param boolean $return If false print picture, otherwise return the output as string
 * @param boolean $link Enclose image in a link to view specified course?
 * @return string|void Depending on the setting of $return
 */
function print_group_picture($group, $courseid, $large=false, $return=false, $link=true) {
    global $CFG;

    if (is_array($group)) {
        $output = '';
        foreach($group as $g) {
            $output .= print_group_picture($g, $courseid, $large, true, $link);
        }
        if ($return) {
            return $output;
        } else {
            echo $output;
            return;
        }
    }

    $context = get_context_instance(CONTEXT_COURSE, $courseid);

    if ($group->hidepicture and !has_capability('moodle/course:managegroups', $context)) {
        return '';
    }

    if ($link or has_capability('moodle/site:accessallgroups', $context)) {
        $output = '<a href="'. $CFG->wwwroot .'/user/index.php?id='. $courseid .'&amp;group='. $group->id .'">';
    } else {
        $output = '';
    }
    if ($large) {
        $file = 'f1';
    } else {
        $file = 'f2';
    }
    if ($group->picture) {  // Print custom group picture
        require_once($CFG->libdir.'/filelib.php');
        $grouppictureurl = get_file_url($group->id.'/'.$file.'.jpg', null, 'usergroup');
        $output .= '<img class="grouppicture" src="'.$grouppictureurl.'"'.
            ' alt="'.s(get_string('group').' '.$group->name).'" title="'.s($group->name).'"/>';
    }
    if ($link or has_capability('moodle/site:accessallgroups', $context)) {
        $output .= '</a>';
    }

    if ($return) {
        return $output;
    } else {
        echo $output;
    }
}


/**
 * Display a recent activity note
 *
 * @uses CONTEXT_SYSTEM
 * @staticvar string $strftimerecent
 * @param object A time object
 * @param object A user object
 * @param string $text Text for display for the note
 * @param string $link The link to wrap around the text
 * @param bool $return If set to true the HTML is returned rather than echo'd
 * @param string $viewfullnames
 */
function print_recent_activity_note($time, $user, $text, $link, $return=false, $viewfullnames=null) {
    static $strftimerecent = null;
    $output = '';

    if (is_null($viewfullnames)) {
        $context = get_context_instance(CONTEXT_SYSTEM);
        $viewfullnames = has_capability('moodle/site:viewfullnames', $context);
    }

    if (is_null($strftimerecent)) {
        $strftimerecent = get_string('strftimerecent');
    }

    $output .= '<div class="head">';
    $output .= '<div class="date">'.userdate($time, $strftimerecent).'</div>';
    $output .= '<div class="name">'.fullname($user, $viewfullnames).'</div>';
    $output .= '</div>';
    $output .= '<div class="info"><a href="'.$link.'">'.format_string($text,true).'</a></div>';

    if ($return) {
        return $output;
    } else {
        echo $output;
    }
}

/**
 * Returns a little popup menu for switching roles
 *
 * @global object
 * @global object
 * @uses CONTEXT_COURSE
 * @param int $courseid The course  to update by id as found in 'course' table
 * @return string
 */
function switchroles_form($courseid) {

    global $CFG, $USER, $OUTPUT;


    if (!$context = get_context_instance(CONTEXT_COURSE, $courseid)) {
        return '';
    }

    if (!empty($USER->access['rsw'][$context->path])){  // Just a button to return to normal
        $options = array();
        $options['id'] = $courseid;
        $options['sesskey'] = sesskey();
        $options['switchrole'] = 0;

        return $OUTPUT->button(html_form::make_button($CFG->wwwroot.'/course/view.php', $options, get_string('switchrolereturn')));
    }

    if (has_capability('moodle/role:switchroles', $context)) {
        if (!$roles = get_switchable_roles($context)) {
            return '';   // Nothing to show!
        }
        // unset default user role - it would not work
        unset($roles[$CFG->guestroleid]);
        $popupurl = $CFG->wwwroot.'/course/view.php?id='.$courseid.'&sesskey='.sesskey();
        $select = html_select::make_popup_form($popupurl, 'switchrole', $roles, 'switchrole', '');
        $select->nothinglabel = get_string('switchroleto');
        $select->set_help_icon('switchrole', get_string('switchroleto'));
        return $OUTPUT->select($select);
    }

    return '';
}

/**
 * Returns a popup menu with course activity modules
 *
 * Given a course
 * This function returns a small popup menu with all the
 * course activity modules in it, as a navigation menu
 * outputs a simple list structure in XHTML
 * The data is taken from the serialised array stored in
 * the course record
 *
 * @todo Finish documenting this function
 *
 * @global object
 * @uses CONTEXT_COURSE
 * @param course $course A {@link $COURSE} object.
 * @param string $sections
 * @param string $modinfo
 * @param string $strsection
 * @param string $strjumpto
 * @param int $width
 * @param string $cmid
 * @return string The HTML block
 */
function navmenulist($course, $sections, $modinfo, $strsection, $strjumpto, $width=50, $cmid=0) {

    global $CFG;

    $section = -1;
    $url = '';
    $menu = array();
    $doneheading = false;

    $coursecontext = get_context_instance(CONTEXT_COURSE, $course->id);

    $menu[] = '<ul class="navmenulist"><li class="jumpto section"><span>'.$strjumpto.'</span><ul>';
    foreach ($modinfo->cms as $mod) {
        if ($mod->modname == 'label') {
            continue;
        }

        if ($mod->sectionnum > $course->numsections) {   /// Don't show excess hidden sections
            break;
        }

        if (!$mod->uservisible) { // do not icnlude empty sections at all
            continue;
        }

        if ($mod->sectionnum >= 0 and $section != $mod->sectionnum) {
            $thissection = $sections[$mod->sectionnum];

            if ($thissection->visible or !$course->hiddensections or
                      has_capability('moodle/course:viewhiddensections', $coursecontext)) {
                $thissection->summary = strip_tags(format_string($thissection->summary,true));
                if (!$doneheading) {
                    $menu[] = '</ul></li>';
                }
                if ($course->format == 'weeks' or empty($thissection->summary)) {
                    $item = $strsection ." ". $mod->sectionnum;
                } else {
                    if (strlen($thissection->summary) < ($width-3)) {
                        $item = $thissection->summary;
                    } else {
                        $item = substr($thissection->summary, 0, $width).'...';
                    }
                }
                $menu[] = '<li class="section"><span>'.$item.'</span>';
                $menu[] = '<ul>';
                $doneheading = true;

                $section = $mod->sectionnum;
            } else {
                // no activities from this hidden section shown
                continue;
            }
        }

        $url = $mod->modname .'/view.php?id='. $mod->id;
        $mod->name = strip_tags(format_string(urldecode($mod->name),true));
        if (strlen($mod->name) > ($width+5)) {
            $mod->name = substr($mod->name, 0, $width).'...';
        }
        if (!$mod->visible) {
            $mod->name = '('.$mod->name.')';
        }
        $class = 'activity '.$mod->modname;
        $class .= ($cmid == $mod->id) ? ' selected' : '';
        $menu[] = '<li class="'.$class.'">'.
                  '<img src="'.$OUTPUT->mod_icon_url('icon', $mod->modname) . '" alt="" />'.
                  '<a href="'.$CFG->wwwroot.'/mod/'.$url.'">'.$mod->name.'</a></li>';
    }

    if ($doneheading) {
        $menu[] = '</ul></li>';
    }
    $menu[] = '</ul></li></ul>';

    return implode("\n", $menu);
}

/**
 * Prints a grade menu (as part of an existing form) with help
 * Showing all possible numerical grades and scales
 *
 * @todo Finish documenting this function
 * @todo Deprecate: this is only used in a few contrib modules
 *
 * @global object
 * @param int $courseid The course ID
 * @param string $name
 * @param string $current
 * @param boolean $includenograde Include those with no grades
 * @param boolean $return If set to true returns rather than echo's
 * @return string|bool Depending on value of $return
 */
function print_grade_menu($courseid, $name, $current, $includenograde=true, $return=false) {

    global $CFG, $OUTPUT;

    $output = '';
    $strscale = get_string('scale');
    $strscales = get_string('scales');

    $scales = get_scales_menu($courseid);
    foreach ($scales as $i => $scalename) {
        $grades[-$i] = $strscale .': '. $scalename;
    }
    if ($includenograde) {
        $grades[0] = get_string('nograde');
    }
    for ($i=100; $i>=1; $i--) {
        $grades[$i] = $i;
    }
    $output .= $OUTPUT->select(html_select::make($grades, $name, $current, false));

    $linkobject = '<span class="helplink"><img class="iconhelp" alt="'.$strscales.'" src="'.$OUTPUT->old_icon_url('help') . '" /></span>';
    $link = html_link::make('/course/scales.php?id='. $courseid .'&list=true', $linkobject);
    $link->add_action(new popup_action('click', $link->url, 'ratingscales', array('height' => 400, 'width' => 500)));
    $link->title = $strscales;
    $output .= $OUTPUT->link($link);

    if ($return) {
        return $output;
    } else {
        echo $output;
    }
}

/**
 * Print an error to STDOUT and exit with a non-zero code. For commandline scripts.
 * Default errorcode is 1.
 *
 * Very useful for perl-like error-handling:
 *
 * do_somethting() or mdie("Something went wrong");
 *
 * @param string  $msg       Error message
 * @param integer $errorcode Error code to emit
 */
function mdie($msg='', $errorcode=1) {
    trigger_error($msg);
    exit($errorcode);
}

/**
 * Returns a string of html with an image of a help icon linked to a help page on a number of help topics.
 * Should be used only with htmleditor or textarea.
 *
 * @global object
 * @global object
 * @param mixed $helptopics variable amount of params accepted. Each param may be a string or an array of arguments for
 *                  helpbutton.
 * @return string Link to help button
 */
function editorhelpbutton(){
    global $CFG, $SESSION, $OUTPUT;
    $items = func_get_args();
    $i = 1;
    $urlparams = array();
    $titles = array();
    foreach ($items as $item){
        if (is_array($item)){
            $urlparams[] = "keyword$i=".urlencode($item[0]);
            $urlparams[] = "title$i=".urlencode($item[1]);
            if (isset($item[2])){
                $urlparams[] = "module$i=".urlencode($item[2]);
            }
            $titles[] = trim($item[1], ". \t");
        } else if (is_string($item)) {
            $urlparams[] = "button$i=".urlencode($item);
            switch ($item) {
                case 'reading' :
                    $titles[] = get_string("helpreading");
                    break;
                case 'writing' :
                    $titles[] = get_string("helpwriting");
                    break;
                case 'questions' :
                    $titles[] = get_string("helpquestions");
                    break;
                case 'emoticons2' :
                    $titles[] = get_string("helpemoticons");
                    break;
                case 'richtext2' :
                    $titles[] = get_string('helprichtext');
                    break;
                case 'text2' :
                    $titles[] = get_string('helptext');
                    break;
                default :
                    print_error('unknownhelp', '', '', $item);
            }
        }
        $i++;
    }
    if (count($titles)>1){
        //join last two items with an 'and'
        $a = new object();
        $a->one = $titles[count($titles) - 2];
        $a->two = $titles[count($titles) - 1];
        $titles[count($titles) - 2] = get_string('and', '', $a);
        unset($titles[count($titles) - 1]);
    }
    $alttag = join (', ', $titles);

    $paramstring = join('&', $urlparams);
    $linkobject = '<img alt="'.$alttag.'" class="iconhelp" src="'.$OUTPUT->old_icon_url('help') . '" />';
    $link = html_link::make(s('/lib/form/editorhelp.php?'.$paramstring), $linkobject);
    $link->add_action(new popup_action('click', $link->url, 'popup', array('height' => 400, 'width' => 500)));
    $link->title = $alttag;
    return $OUTPUT->link($link);
}

/**
 * Print a help button.
 *
 * Prints a special help button that is a link to the "live" emoticon popup
 *
 * @todo Finish documenting this function
 *
 * @global object
 * @global object
 * @param string $form ?
 * @param string $field ?
 * @param boolean $return If true then the output is returned as a string, if false it is printed to the current page.
 * @return string|void Depending on value of $return
 */
function emoticonhelpbutton($form, $field, $return = false) {

    global $SESSION, $OUTPUT;

    $SESSION->inserttextform = $form;
    $SESSION->inserttextfield = $field;
    $helpicon = moodle_help_icon::make('emoticons2', get_string('helpemoticons'), 'moodle', true);
    $helpicon->image->src = $OUTPUT->old_icon_url('s/smiley');
    $helpicon->image->add_class('emoticon');
    $helpicon->style = "margin-left:3px; padding-right:1px;width:15px;height:15px;";
    $help = $OUTPUT->help_icon($helpicon);
    if (!$return){
        echo $help;
    } else {
        return $help;
    }
}

/**
 * Print a help button.
 *
 * Prints a special help button for html editors (htmlarea in this case)
 *
 * @todo Write code into this function! detect current editor and print correct info
 * @global object
 * @return string Only returns an empty string at the moment
 */
function editorshortcutshelpbutton() {

    global $CFG;
    //TODO: detect current editor and print correct info
/*    $imagetext = '<img src="' . $CFG->httpswwwroot . '/lib/editor/htmlarea/images/kbhelp.gif" alt="'.
        get_string('editorshortcutkeys').'" class="iconkbhelp" />';

    return helpbutton('editorshortcuts', get_string('editorshortcutkeys'), 'moodle', true, false, '', true, $imagetext);*/
    return '';
}

/**
 * Print a message and exit.
 *
 * @param string $message The message to print in the notice
 * @param string $link The link to use for the continue button
 * @param object $course A course object
 * @return void This function simply exits
 */
function notice ($message, $link='', $course=NULL) {
    global $CFG, $SITE, $THEME, $COURSE, $PAGE, $OUTPUT;

    $message = clean_text($message);   // In case nasties are in here

    if (CLI_SCRIPT) {
        echo("!!$message!!\n");
        exit(1); // no success
    }

    if (!$PAGE->headerprinted) {
        //header not yet printed
        $PAGE->set_title(get_string('notice'));
        echo $OUTPUT->header();
    } else {
        print_container_end_all(false, $THEME->open_header_containers);
    }

    echo $OUTPUT->box($message, 'generalbox', 'notice');
    echo $OUTPUT->continue_button($link);

    echo $OUTPUT->footer();
    exit(1); // general error code
}

/**
 * Redirects the user to another page, after printing a notice
 *
 * This function calls the OUTPUT redirect method, echo's the output
 * and then dies to ensure nothing else happens.
 *
 * <strong>Good practice:</strong> You should call this method before starting page
 * output by using any of the OUTPUT methods.
 *
 * @param moodle_url $url A moodle_url to redirect to. Strings are not to be trusted!
 * @param string $message The message to display to the user
 * @param int $delay The delay before redirecting
 * @return void
 */
function redirect($url, $message='', $delay=-1) {
    global $OUTPUT, $PAGE, $SESSION, $CFG;

    if ($url instanceof moodle_url) {
        $url = $url->out(false, array(), false);
    }

    if (!empty($CFG->usesid) && !isset($_COOKIE[session_name()])) {
       $url = $SESSION->sid_process_url($url);
    }

    if (function_exists('error_get_last')) {
        $lasterror = error_get_last();
    }
    $debugdisableredirect = defined('DEBUGGING_PRINTED') ||
            (!empty($CFG->debugdisplay) && !empty($lasterror) && ($lasterror['type'] & DEBUG_DEVELOPER));

    $usingmsg = false;
    if (!empty($message)) {
        if ($delay === -1 || !is_numeric($delay)) {
            $delay = 3;
        }
        $message = clean_text($message);
    } else {
        $message = get_string('pageshouldredirect');
        $delay = 0;
        // We are going to try to use a HTTP redirect, so we need a full URL.
        if (!preg_match('|^[a-z]+:|', $url)) {
            // Get host name http://www.wherever.com
            $hostpart = preg_replace('|^(.*?[^:/])/.*$|', '$1', $CFG->wwwroot);
            if (preg_match('|^/|', $url)) {
                // URLs beginning with / are relative to web server root so we just add them in
                $url = $hostpart.$url;
            } else {
                // URLs not beginning with / are relative to path of current script, so add that on.
                $url = $hostpart.preg_replace('|\?.*$|','',me()).'/../'.$url;
            }
            // Replace all ..s
            while (true) {
                $newurl = preg_replace('|/(?!\.\.)[^/]*/\.\./|', '/', $url);
                if ($newurl == $url) {
                    break;
                }
                $url = $newurl;
            }
        }
    }

    if (defined('MDL_PERF') || (!empty($CFG->perfdebug) and $CFG->perfdebug > 7)) {
        if (defined('MDL_PERFTOLOG') && !function_exists('register_shutdown_function')) {
            $perf = get_performance_info();
            error_log("PERF: " . $perf['txt']);
        }
    }

    $encodedurl = preg_replace("/\&(?![a-zA-Z0-9#]{1,8};)/", "&amp;", $url);
    $encodedurl = preg_replace('/^.*href="([^"]*)".*$/', "\\1", clean_text('<a href="'.$encodedurl.'" />'));

    if ($delay == 0 && !$debugdisableredirect && !headers_sent()) {
        //302 might not work for POST requests, 303 is ignored by obsolete clients.
        @header($_SERVER['SERVER_PROTOCOL'] . ' 303 See Other');
        @header('Location: '.$url);
        echo bootstrap_renderer::plain_redirect_message($encodedurl);
        exit;
    }

    // Include a redirect message, even with a HTTP redirect, because that is recommended practice.
    $PAGE->set_generaltype('embedded');  // No header and footer needed
    $CFG->docroot = false; // to prevent the link to moodle docs from being displayed on redirect page.
    echo $OUTPUT->redirect_message($encodedurl, $message, $delay, $debugdisableredirect);
    exit;
}

/**
 * Given an email address, this function will return an obfuscated version of it
 *
 * @param string $email The email address to obfuscate
 * @return string The obfuscated email address
 */
 function obfuscate_email($email) {

    $i = 0;
    $length = strlen($email);
    $obfuscated = '';
    while ($i < $length) {
        if (rand(0,2)) {
            $obfuscated.='%'.dechex(ord($email{$i}));
        } else {
            $obfuscated.=$email{$i};
        }
        $i++;
    }
    return $obfuscated;
}

/**
 * This function takes some text and replaces about half of the characters
 * with HTML entity equivalents.   Return string is obviously longer.
 *
 * @param string $plaintext The text to be obfuscated
 * @return string The obfuscated text
 */
function obfuscate_text($plaintext) {

    $i=0;
    $length = strlen($plaintext);
    $obfuscated='';
    $prev_obfuscated = false;
    while ($i < $length) {
        $c = ord($plaintext{$i});
        $numerical = ($c >= ord('0')) && ($c <= ord('9'));
        if ($prev_obfuscated and $numerical ) {
            $obfuscated.='&#'.ord($plaintext{$i}).';';
        } else if (rand(0,2)) {
            $obfuscated.='&#'.ord($plaintext{$i}).';';
            $prev_obfuscated = true;
        } else {
            $obfuscated.=$plaintext{$i};
            $prev_obfuscated = false;
        }
      $i++;
    }
    return $obfuscated;
}

/**
 * This function uses the {@link obfuscate_email()} and {@link obfuscate_text()}
 * to generate a fully obfuscated email link, ready to use.
 *
 * @param string $email The email address to display
 * @param string $label The text to dispalyed as hyperlink to $email
 * @param boolean $dimmed If true then use css class 'dimmed' for hyperlink
 * @return string The obfuscated mailto link
 */
function obfuscate_mailto($email, $label='', $dimmed=false) {

    if (empty($label)) {
        $label = $email;
    }
    if ($dimmed) {
        $title = get_string('emaildisable');
        $dimmed = ' class="dimmed"';
    } else {
        $title = '';
        $dimmed = '';
    }
    return sprintf("<a href=\"%s:%s\" $dimmed title=\"$title\">%s</a>",
                    obfuscate_text('mailto'), obfuscate_email($email),
                    obfuscate_text($label));
}

/**
 * This function is used to rebuild the <nolink> tag because some formats (PLAIN and WIKI)
 * will transform it to html entities
 *
 * @param string $text Text to search for nolink tag in
 * @return string
 */
function rebuildnolinktag($text) {

    $text = preg_replace('/&lt;(\/*nolink)&gt;/i','<$1>',$text);

    return $text;
}

/**
 * Prints a maintenance message from $CFG->maintenance_message or default if empty
 * @return void
 */
function print_maintenance_message() {
    global $CFG, $SITE, $PAGE, $OUTPUT;

    $PAGE->set_pagetype('maintenance-message');
    $PAGE->set_generaltype('maintenance');
    $PAGE->set_title(strip_tags($SITE->fullname));
    $PAGE->set_heading($SITE->fullname);
    echo $OUTPUT->header();
    echo $OUTPUT->heading(get_string('sitemaintenance', 'admin'));
    if (isset($CFG->maintenance_message) and !html_is_blank($CFG->maintenance_message)) {
        echo $OUTPUT->box_start('maintenance_message generalbox boxwidthwide boxaligncenter');
        echo $CFG->maintenance_message;
        echo $OUTPUT->box_end();
    }
    echo $OUTPUT->footer();
    die;
}

/**
 * Adjust the list of allowed tags based on $CFG->allowobjectembed and user roles (admin)
 *
 * @global object
 * @global string
 * @return void
 */
function adjust_allowed_tags() {

    global $CFG, $ALLOWED_TAGS;

    if (!empty($CFG->allowobjectembed)) {
        $ALLOWED_TAGS .= '<embed><object>';
    }
}

/**
 * A class for tabs, Some code to print tabs
 *
 * @license http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @package moodlecore
 */
class tabobject {
    /**
     * @var string
     */
    var $id;
    var $link;
    var $text;
    /**
     * @var bool
     */
    var $linkedwhenselected;

    /**
     * A constructor just because I like constructors
     *
     * @param string $id
     * @param string $link
     * @param string $text
     * @param string $title
     * @param bool $linkedwhenselected
     */
    function tabobject ($id, $link='', $text='', $title='', $linkedwhenselected=false) {
        $this->id   = $id;
        $this->link = $link;
        $this->text = $text;
        $this->title = $title ? $title : $text;
        $this->linkedwhenselected = $linkedwhenselected;
    }
}



/**
 * Returns a string containing a nested list, suitable for formatting into tabs with CSS.
 *
 * @global object
 * @param array $tabrows An array of rows where each row is an array of tab objects
 * @param string $selected  The id of the selected tab (whatever row it's on)
 * @param array  $inactive  An array of ids of inactive tabs that are not selectable.
 * @param array  $activated An array of ids of other tabs that are currently activated
 * @param bool $return If true output is returned rather then echo'd
 **/
function print_tabs($tabrows, $selected=NULL, $inactive=NULL, $activated=NULL, $return=false) {
    global $CFG;

/// $inactive must be an array
    if (!is_array($inactive)) {
        $inactive = array();
    }

/// $activated must be an array
    if (!is_array($activated)) {
        $activated = array();
    }

/// Convert the tab rows into a tree that's easier to process
    if (!$tree = convert_tabrows_to_tree($tabrows, $selected, $inactive, $activated)) {
        return false;
    }

/// Print out the current tree of tabs (this function is recursive)

    $output = convert_tree_to_html($tree);

    $output = "\n\n".'<div class="tabtree">'.$output.'</div><div class="clearer"> </div>'."\n\n";

/// We're done!

    if ($return) {
        return $output;
    }
    echo $output;
}

/**
 * Converts a nested array tree into HTML ul:li [recursive]
 *
 * @param array $tree A tree array to convert
 * @param int $row Used in identifing the iteration level and in ul classes
 * @return string HTML structure
 */
function convert_tree_to_html($tree, $row=0) {

    $str = "\n".'<ul class="tabrow'.$row.'">'."\n";

    $first = true;
    $count = count($tree);

    foreach ($tree as $tab) {
        $count--;   // countdown to zero

        $liclass = '';

        if ($first && ($count == 0)) {   // Just one in the row
            $liclass = 'first last';
            $first = false;
        } else if ($first) {
            $liclass = 'first';
            $first = false;
        } else if ($count == 0) {
            $liclass = 'last';
        }

        if ((empty($tab->subtree)) && (!empty($tab->selected))) {
            $liclass .= (empty($liclass)) ? 'onerow' : ' onerow';
        }

        if ($tab->inactive || $tab->active || $tab->selected) {
            if ($tab->selected) {
                $liclass .= (empty($liclass)) ? 'here selected' : ' here selected';
            } else if ($tab->active) {
                $liclass .= (empty($liclass)) ? 'here active' : ' here active';
            }
        }

        $str .= (!empty($liclass)) ? '<li class="'.$liclass.'">' : '<li>';

        if ($tab->inactive || $tab->active || ($tab->selected && !$tab->linkedwhenselected)) {
            // The a tag is used for styling
            $str .= '<a class="nolink"><span>'.$tab->text.'</span></a>';
        } else {
            $str .= '<a href="'.$tab->link.'" title="'.$tab->title.'"><span>'.$tab->text.'</span></a>';
        }

        if (!empty($tab->subtree)) {
            $str .= convert_tree_to_html($tab->subtree, $row+1);
        } else if ($tab->selected) {
            $str .= '<div class="tabrow'.($row+1).' empty">&nbsp;</div>'."\n";
        }

        $str .= ' </li>'."\n";
    }
    $str .= '</ul>'."\n";

    return $str;
}

/**
 * Convert nested tabrows to a nested array
 *
 * @param array $tabrows A [nested] array of tab row objects
 * @param string $selected The tabrow to select (by id)
 * @param array $inactive An array of tabrow id's to make inactive
 * @param array $activated An array of tabrow id's to make active
 * @return array The nested array
 */
function convert_tabrows_to_tree($tabrows, $selected, $inactive, $activated) {

/// Work backwards through the rows (bottom to top) collecting the tree as we go.

    $tabrows = array_reverse($tabrows);

    $subtree = array();

    foreach ($tabrows as $row) {
        $tree = array();

        foreach ($row as $tab) {
            $tab->inactive = in_array((string)$tab->id, $inactive);
            $tab->active = in_array((string)$tab->id, $activated);
            $tab->selected = (string)$tab->id == $selected;

            if ($tab->active || $tab->selected) {
                if ($subtree) {
                    $tab->subtree = $subtree;
                }
            }
            $tree[] = $tab;
        }
        $subtree = $tree;
    }

    return $subtree;
}

/**
 * Returns the Moodle Docs URL in the users language
 *
 * @global object
 * @param string $path the end of the URL.
 * @return string The MoodleDocs URL in the user's language. for example {@link http://docs.moodle.org/en/ http://docs.moodle.org/en/$path}
 */
function get_docs_url($path) {
    global $CFG;
    return $CFG->docroot . '/' . str_replace('_utf8', '', current_language()) . '/' . $path;
}


/**
 * Standard Debugging Function
 *
 * Returns true if the current site debugging settings are equal or above specified level.
 * If passed a parameter it will emit a debugging notice similar to trigger_error(). The
 * routing of notices is controlled by $CFG->debugdisplay
 * eg use like this:
 *
 * 1)  debugging('a normal debug notice');
 * 2)  debugging('something really picky', DEBUG_ALL);
 * 3)  debugging('annoying debug message only for develpers', DEBUG_DEVELOPER);
 * 4)  if (debugging()) { perform extra debugging operations (do not use print or echo) }
 *
 * In code blocks controlled by debugging() (such as example 4)
 * any output should be routed via debugging() itself, or the lower-level
 * trigger_error() or error_log(). Using echo or print will break XHTML
 * JS and HTTP headers.
 *
 * It is also possible to define NO_DEBUG_DISPLAY which redirects the message to error_log.
 *
 * @uses DEBUG_NORMAL
 * @param string $message a message to print
 * @param int $level the level at which this debugging statement should show
 * @param array $backtrace use different backtrace
 * @return bool
 */
function debugging($message = '', $level = DEBUG_NORMAL, $backtrace = null) {
    global $CFG, $UNITTEST;

    if (empty($CFG->debug) || $CFG->debug < $level) {
        return false;
    }

    if (!isset($CFG->debugdisplay)) {
        $CFG->debugdisplay = ini_get_bool('display_errors');
    }

    if ($message) {
        if (!$backtrace) {
            $backtrace = debug_backtrace();
        }
        $from = format_backtrace($backtrace, CLI_SCRIPT);
        if (!empty($UNITTEST->running)) {
            // When the unit tests are running, any call to trigger_error
            // is intercepted by the test framework and reported as an exception.
            // Therefore, we cannot use trigger_error during unit tests.
            // At the same time I do not think we should just discard those messages,
            // so displaying them on-screen seems like the only option. (MDL-20398)
            echo '<div class="notifytiny">' . $message . $from . '</div>';

        } else if (NO_DEBUG_DISPLAY) {
            // script does not want any errors or debugging in output,
            // we send the info to error log instead
            error_log('Debugging: ' . $message . $from);

        } else if ($CFG->debugdisplay) {
            if (!defined('DEBUGGING_PRINTED')) {
                define('DEBUGGING_PRINTED', 1); // indicates we have printed something
            }
            echo '<div class="notifytiny">' . $message . $from . '</div>';

        } else {
            trigger_error($message . $from, E_USER_NOTICE);
        }
    }
    return true;
}

/**
 *  Returns string to add a frame attribute, if required
 *
 * @global object
 * @return bool
 */
function frametarget() {
    global $CFG;

    if (empty($CFG->framename) or ($CFG->framename == '_top')) {
        return '';
    } else {
        return ' target="'.$CFG->framename.'" ';
    }
}

/**
* Outputs a HTML comment to the browser. This is used for those hard-to-debug
* pages that use bits from many different files in very confusing ways (e.g. blocks).
*
* <code>print_location_comment(__FILE__, __LINE__);</code>
*
* @param string $file
* @param integer $line
* @param boolean $return Whether to return or print the comment
* @return string|void Void unless true given as third parameter
*/
function print_location_comment($file, $line, $return = false)
{
    if ($return) {
        return "<!-- $file at line $line -->\n";
    } else {
        echo "<!-- $file at line $line -->\n";
    }
}


/**
 * @return boolean true if the current language is right-to-left (Hebrew, Arabic etc)
 */
function right_to_left() {
    static $result;

    if (!isset($result)) {
        $result = get_string('thisdirection') == 'rtl';
    }
    return $result;
}


/**
 * Returns swapped left<=>right if in RTL environment.
 * part of RTL support
 *
 * @param string $align align to check
 * @return string
 */
function fix_align_rtl($align) {
    if (!right_to_left()) {
        return $align;
    }
    if ($align=='left')  { return 'right'; }
    if ($align=='right') { return 'left'; }
    return $align;
}


/**
 * Returns true if the page is displayed in a popup window.
 * Gets the information from the URL parameter inpopup.
 *
 * @todo Use a central function to create the popup calls allover Moodle and
 * In the moment only works with resources and probably questions.
 *
 * @return boolean
 */
function is_in_popup() {
    $inpopup = optional_param('inpopup', '', PARAM_BOOL);

    return ($inpopup);
}

/**
 * To use this class.
 * - construct
 * - call create (or use the 3rd param to the constructor)
 * - call update or update_full repeatedly
 *
 * @license http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @package moodlecore
 */
class progress_bar {
    /** @var string html id */
    private $html_id;
    /** @var int */
    private $percent;
    /** @var int */
    private $width;
    /** @var int */
    private $lastcall;
    /** @var int */
    private $time_start;
    private $minimum_time = 2; //min time between updates.
    /**
     * Contructor
     *
     * @param string $html_id
     * @param int $width
     * @param bool $autostart Default to false
     */
    public function __construct($html_id = '', $width = 500, $autostart = false){
        if (!empty($html_id)) {
            $this->html_id  = $html_id;
        } else {
            $this->html_id  = uniqid();
        }
        $this->width = $width;
        $this->restart();
        if($autostart){
            $this->create();
        }
    }
    /**
      * Create a new progress bar, this function will output html.
      *
      * @return void Echo's output
      */
    public function create(){
            flush();
            $this->lastcall->pt = 0;
            $this->lastcall->time = microtime(true);
            if (CLI_SCRIPT) {
                return; // temporary solution for cli scripts
            }
            $htmlcode = <<<EOT
            <div style="text-align:center;width:{$this->width}px;clear:both;padding:0;margin:0 auto;">
                <h2 id="status_{$this->html_id}" style="text-align: center;margin:0 auto"></h2>
                <p id="time_{$this->html_id}"></p>
                <div id="bar_{$this->html_id}" style="border-style:solid;border-width:1px;width:500px;height:50px;">
                    <div id="progress_{$this->html_id}"
                    style="text-align:center;background:#FFCC66;width:4px;border:1px
                    solid gray;height:38px; padding-top:10px;">&nbsp;<span id="pt_{$this->html_id}"></span>
                    </div>
                </div>
            </div>
EOT;
            echo $htmlcode;
            flush();
    }
    /**
     * Update the progress bar
     *
     * @param int $percent from 1-100
     * @param string $msg
     * @param mixed $es
     * @return void Echo's output
     */
    private function _update($percent, $msg, $es){
        global $PAGE;
        if(empty($this->time_start)){
            $this->time_start = microtime(true);
        }
        $this->percent = $percent;
        $this->lastcall->time = microtime(true);
        $this->lastcall->pt   = $percent;
        $w = $this->percent * $this->width;
        if (CLI_SCRIPT) {
            return; // temporary solution for cli scripts
        }
        if ($es === null){
            $es = "Infinity";
        }
        echo $PAGE->requires->js_function_call('update_progress_bar', Array($this->html_id, $w, $this->percent, $msg, $es))->asap();
        flush();
    }
    /**
      * estimate time
      *
      * @param int $curtime the time call this function
      * @param int $percent from 1-100
      * @return mixed Null, or int
      */
    private function estimate($curtime, $pt){
        $consume = $curtime - $this->time_start;
        $one = $curtime - $this->lastcall->time;
        $this->percent = $pt;
        $percent = $pt - $this->lastcall->pt;
        if ($percent != 0) {
            $left = ($one / $percent) - $consume;
        } else {
            return null;
        }
        if($left < 0) {
            return 0;
        } else {
            return $left;
        }
    }
    /**
      * Update progress bar according percent
      *
      * @param int $percent from 1-100
      * @param string $msg the message needed to be shown
      */
    public function update_full($percent, $msg){
        $percent = max(min($percent, 100), 0);
        if ($percent != 100 && ($this->lastcall->time + $this->minimum_time) > microtime(true)){
            return;
        }
        $this->_update($percent/100, $msg);
    }
    /**
      * Update progress bar according the nubmer of tasks
      *
      * @param int $cur current task number
      * @param int $total total task number
      * @param string $msg message
      */
    public function update($cur, $total, $msg){
        $cur = max($cur, 0);
        if ($cur >= $total){
            $percent = 1;
        } else {
            $percent = $cur / $total;
        }
        /**
        if ($percent != 1 && ($this->lastcall->time + $this->minimum_time) > microtime(true)){
            return;
        }
        */
        $es = $this->estimate(microtime(true), $percent);
        $this->_update($percent, $msg, $es);
    }
    /**
     * Restart the progress bar.
     */
    public function restart(){
        $this->percent  = 0;
        $this->lastcall = new stdClass;
        $this->lastcall->pt = 0;
        $this->lastcall->time = microtime(true);
        $this->time_start  = 0;
    }
}

/**
 * Use this class from long operations where you want to output occasional information about
 * what is going on, but don't know if, or in what format, the output should be.
 *
 * @license http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @package moodlecore
 */
abstract class moodle_progress_trace {
    /**
     * Ouput an progress message in whatever format.
     * @param string $message the message to output.
     * @param integer $depth indent depth for this message.
     */
    abstract public function output($message, $depth = 0);

    /**
     * Called when the processing is finished.
     */
    public function finished() {
    }
}

/**
 * This subclass of moodle_progress_trace does not ouput anything.
 *
 * @license http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @package moodlecore
 */
class null_progress_trace extends moodle_progress_trace {
    /**
     * Does Nothing
     *
     * @param string $message
     * @param int $depth
     * @return void Does Nothing
     */
    public function output($message, $depth = 0) {
    }
}

/**
 * This subclass of moodle_progress_trace outputs to plain text.
 *
 * @license http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @package moodlecore
 */
class text_progress_trace extends moodle_progress_trace {
    /**
     * Output the trace message
     *
     * @param string $message
     * @param int $depth
     * @return void Output is echo'd
     */
    public function output($message, $depth = 0) {
        echo str_repeat('  ', $depth), $message, "\n";
        flush();
    }
}

/**
 * This subclass of moodle_progress_trace outputs as HTML.
 *
 * @license http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @package moodlecore
 */
class html_progress_trace extends moodle_progress_trace {
    /**
     * Output the trace message
     *
     * @param string $message
     * @param int $depth
     * @return void Output is echo'd
     */
    public function output($message, $depth = 0) {
        echo '<p>', str_repeat('&#160;&#160;', $depth), htmlspecialchars($message), "</p>\n";
        flush();
    }
}

/**
 * HTML List Progress Tree
 *
 * @license http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @package moodlecore
 */
class html_list_progress_trace extends moodle_progress_trace {
    /** @var int */
    protected $currentdepth = -1;

    /**
     * Echo out the list
     *
     * @param string $message The message to display
     * @param int $depth
     * @return void Output is echoed
     */
    public function output($message, $depth = 0) {
        $samedepth = true;
        while ($this->currentdepth > $depth) {
            echo "</li>\n</ul>\n";
            $this->currentdepth -= 1;
            if ($this->currentdepth == $depth) {
                echo '<li>';
            }
            $samedepth = false;
        }
        while ($this->currentdepth < $depth) {
            echo "<ul>\n<li>";
            $this->currentdepth += 1;
            $samedepth = false;
        }
        if ($samedepth) {
            echo "</li>\n<li>";
        }
        echo htmlspecialchars($message);
        flush();
    }

    /**
     * Called when the processing is finished.
     */
    public function finished() {
        while ($this->currentdepth >= 0) {
            echo "</li>\n</ul>\n";
            $this->currentdepth -= 1;
        }
    }
}

/**
 * Return the authentication plugin title
 *
 * @param string $authtype plugin type
 * @return string
 */
function auth_get_plugin_title($authtype) {
    $authtitle = get_string("auth_{$authtype}title", "auth");
    if ($authtitle == "[[auth_{$authtype}title]]") {
        $authtitle = get_string("auth_{$authtype}title", "auth_{$authtype}");
    }
    return $authtitle;
}
