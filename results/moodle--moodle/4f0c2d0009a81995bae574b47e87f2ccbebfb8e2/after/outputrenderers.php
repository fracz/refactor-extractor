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
 * Classes for rendering HTML output for Moodle.
 *
 * Please see http://docs.moodle.org/en/Developement:How_Moodle_outputs_HTML
 * for an overview.
 *
 * @package   moodlecore
 * @copyright 2009 Tim Hunt
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */

/**
 * Simple base class for Moodle renderers.
 *
 * Tracks the xhtml_container_stack to use, which is passed in in the constructor.
 *
 * Also has methods to facilitate generating HTML output.
 *
 * @copyright 2009 Tim Hunt
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @since     Moodle 2.0
 */
class renderer_base {
    /** @var xhtml_container_stack the xhtml_container_stack to use. */
    protected $opencontainers;
    /** @var moodle_page the page we are rendering for. */
    protected $page;
    /** @var requested rendering target conatnt */
    protected $target;

    /**
     * Constructor
     * @param moodle_page $page the page we are doing output for.
     * @param string $target one of rendering target constants
     */
    public function __construct(moodle_page $page, $target) {
        $this->opencontainers = $page->opencontainers;
        $this->page = $page;
        $this->target = $target;
    }

    /**
     * Returns rendered widget.
     * @param renderable $widget intence with renderable interface
     * @return string
     */
    public function render(renderable $widget) {
        $rendermethod = 'render_'.get_class($widget);
        if (method_exists($this, $rendermethod)) {
            return $this->$rendermethod($widget);
        }
        throw new coding_exception('Can not render widget, renderer method ('.$rendermethod.') not found.');
    }

    /**
     * Adds JS handlers needed for event execution for one html element id
     * @param component_action $actions
     * @param string $id
     * @return string id of element, either original submitted or random new if not supplied
     */
    public function add_action_handler(component_action $action, $id=null) {
        if (!$id) {
            $id = html_writer::random_id($action->event);
        }
        $this->page->requires->event_handler("#$id", $action->event, $action->jsfunction, $action->jsfunctionargs);
        return $id;
    }

    /**
     * Have we started output yet?
     * @return boolean true if the header has been printed.
     */
    public function has_started() {
        return $this->page->state >= moodle_page::STATE_IN_BODY;
    }

    /**
     * Given an array or space-separated list of classes, prepares and returns the HTML class attribute value
     * @param mixed $classes Space-separated string or array of classes
     * @return string HTML class attribute value
     */
    public static function prepare_classes($classes) {
        if (is_array($classes)) {
            return implode(' ', array_unique($classes));
        }
        return $classes;
    }

    /**
     * Return the moodle_url for an image.
     * The exact image location and extension is determined
     * automatically by searching for gif|png|jpg|jpeg, please
     * note there can not be diferent images with the different
     * extension. The imagename is for historical reasons
     * a relative path name, it may be changed later for core
     * images. It is recommended to not use subdirectories
     * in plugin and theme pix directories.
     *
     * There are three types of images:
     * 1/ theme images  - stored in theme/mytheme/pix/,
     *                    use component 'theme'
     * 2/ core images   - stored in /pix/,
     *                    overridden via theme/mytheme/pix_core/
     * 3/ plugin images - stored in mod/mymodule/pix,
     *                    overridden via theme/mytheme/pix_plugins/mod/mymodule/,
     *                    example: pix_url('comment', 'mod_glossary')
     *
     * @param string $imagename the pathname of the image
     * @param string $component full plugin name (aka component) or 'theme'
     * @return moodle_url
     */
    public function pix_url($imagename, $component = 'moodle') {
        return $this->page->theme->pix_url($imagename, $component);
    }
}


/**
 * Basis for all plugin renderers.
 *
 * @author    Petr Skoda (skodak)
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @since     Moodle 2.0
 */
class plugin_renderer_base extends renderer_base {
    /**
     * A reference to the current general renderer probably {@see core_renderer}
     * @var renderer_base
     */
    protected $output;

    /**
     * Contructor method, calls the parent constructor
     * @param moodle_page $page
     * @param string $target one of rendering target constants
     */
    public function __construct(moodle_page $page, $target) {
        $this->output = $page->get_renderer('core', null, $target);
        parent::__construct($page, $target);
    }

    /**
     * Returns rendered widget.
     * @param renderable $widget intence with renderable interface
     * @return string
     */
    public function render(renderable $widget) {
        $rendermethod = 'render_'.get_class($widget);
        if (method_exists($this, $rendermethod)) {
            return $this->$rendermethod($widget);
        }
        // pass to core renderer if method not found here
        $this->output->render($widget);
    }

    /**
     * Magic method used to pass calls otherwise meant for the standard renderer
     * to it to ensure we don't go causing unnessecary greif.
     *
     * @param string $method
     * @param array $arguments
     * @return mixed
     */
    public function __call($method, $arguments) {
        if (method_exists('renderer_base', $method)) {
            throw new coding_exception('Protected method called against '.__CLASS__.' :: '.$method);
        }
        if (method_exists($this->output, $method)) {
            return call_user_func_array(array($this->output, $method), $arguments);
        } else {
            throw new coding_exception('Unknown method called against '.__CLASS__.' :: '.$method);
        }
    }
}


/**
 * The standard implementation of the core_renderer interface.
 *
 * @copyright 2009 Tim Hunt
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @since     Moodle 2.0
 */
class core_renderer extends renderer_base {
    /** @var string used in {@link header()}. */
    const PERFORMANCE_INFO_TOKEN = '%%PERFORMANCEINFO%%';
    /** @var string used in {@link header()}. */
    const END_HTML_TOKEN = '%%ENDHTML%%';
    /** @var string used in {@link header()}. */
    const MAIN_CONTENT_TOKEN = '[MAIN CONTENT GOES HERE]';
    /** @var string used to pass information from {@link doctype()} to {@link standard_head_html()}. */
    protected $contenttype;
    /** @var string used by {@link redirect_message()} method to communicate with {@link header()}. */
    protected $metarefreshtag = '';

    /**
     * Get the DOCTYPE declaration that should be used with this page. Designed to
     * be called in theme layout.php files.
     * @return string the DOCTYPE declaration (and any XML prologue) that should be used.
     */
    public function doctype() {
        global $CFG;

        $doctype = '<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">' . "\n";
        $this->contenttype = 'text/html; charset=utf-8';

        if (empty($CFG->xmlstrictheaders)) {
            return $doctype;
        }

        // We want to serve the page with an XML content type, to force well-formedness errors to be reported.
        $prolog = '<?xml version="1.0" encoding="utf-8"?>' . "\n";
        if (isset($_SERVER['HTTP_ACCEPT']) && strpos($_SERVER['HTTP_ACCEPT'], 'application/xhtml+xml') !== false) {
            // Firefox and other browsers that can cope natively with XHTML.
            $this->contenttype = 'application/xhtml+xml; charset=utf-8';

        } else if (preg_match('/MSIE.*Windows NT/', $_SERVER['HTTP_USER_AGENT'])) {
            // IE can't cope with application/xhtml+xml, but it will cope if we send application/xml with an XSL stylesheet.
            $this->contenttype = 'application/xml; charset=utf-8';
            $prolog .= '<?xml-stylesheet type="text/xsl" href="' . $CFG->httpswwwroot . '/lib/xhtml.xsl"?>' . "\n";

        } else {
            $prolog = '';
        }

        return $prolog . $doctype;
    }

    /**
     * The attributes that should be added to the <html> tag. Designed to
     * be called in theme layout.php files.
     * @return string HTML fragment.
     */
    public function htmlattributes() {
        return get_html_lang(true) . ' xmlns="http://www.w3.org/1999/xhtml"';
    }

    /**
     * The standard tags (meta tags, links to stylesheets and JavaScript, etc.)
     * that should be included in the <head> tag. Designed to be called in theme
     * layout.php files.
     * @return string HTML fragment.
     */
    public function standard_head_html() {
        global $CFG, $SESSION;
        $output = '';
        $output .= '<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />' . "\n";
        $output .= '<meta name="keywords" content="moodle, ' . $this->page->title . '" />' . "\n";
        if (!$this->page->cacheable) {
            $output .= '<meta http-equiv="pragma" content="no-cache" />' . "\n";
            $output .= '<meta http-equiv="expires" content="0" />' . "\n";
        }
        // This is only set by the {@link redirect()} method
        $output .= $this->metarefreshtag;

        // Check if a periodic refresh delay has been set and make sure we arn't
        // already meta refreshing
        if ($this->metarefreshtag=='' && $this->page->periodicrefreshdelay!==null) {
            $output .= '<meta http-equiv="refresh" content="'.$this->page->periodicrefreshdelay.';url='.$this->page->url->out().'" />';
        }

        $this->page->requires->js_function_call('setTimeout', array('fix_column_widths()', 20));

        $focus = $this->page->focuscontrol;
        if (!empty($focus)) {
            if (preg_match("#forms\['([a-zA-Z0-9]+)'\].elements\['([a-zA-Z0-9]+)'\]#", $focus, $matches)) {
                // This is a horrifically bad way to handle focus but it is passed in
                // through messy formslib::moodleform
                $this->page->requires->js_function_call('old_onload_focus', array($matches[1], $matches[2]));
            } else if (strpos($focus, '.')!==false) {
                // Old style of focus, bad way to do it
                debugging('This code is using the old style focus event, Please update this code to focus on an element id or the moodleform focus method.', DEBUG_DEVELOPER);
                $this->page->requires->js_function_call('old_onload_focus', explode('.', $focus, 2));
            } else {
                // Focus element with given id
                $this->page->requires->js_function_call('focuscontrol', array($focus));
            }
        }

        // Get the theme stylesheet - this has to be always first CSS, this loads also styles.css from all plugins;
        // any other custom CSS can not be overridden via themes and is highly discouraged
        $urls = $this->page->theme->css_urls($this->page);
        foreach ($urls as $url) {
            $this->page->requires->css_theme($url);
        }

        // Get the theme javascript head and footer
        $jsurl = $this->page->theme->javascript_url(true);
        $this->page->requires->js($jsurl, true);
        $jsurl = $this->page->theme->javascript_url(false);
        $this->page->requires->js($jsurl);

        // Perform a browser environment check for the flash version.  Should only run once per login session.
        if (!NO_MOODLE_COOKIES && isloggedin() && !empty($CFG->excludeoldflashclients) && empty($SESSION->flashversion)) {
            $this->page->requires->js('/lib/swfobject/swfobject.js');
            $this->page->requires->js_init_call('M.core_flashdetect.init');
        }

        // Get any HTML from the page_requirements_manager.
        $output .= $this->page->requires->get_head_code($this->page, $this);

        // List alternate versions.
        foreach ($this->page->alternateversions as $type => $alt) {
            $output .= html_writer::empty_tag('link', array('rel' => 'alternate',
                    'type' => $type, 'title' => $alt->title, 'href' => $alt->url));
        }

        return $output;
    }

    /**
     * The standard tags (typically skip links) that should be output just inside
     * the start of the <body> tag. Designed to be called in theme layout.php files.
     * @return string HTML fragment.
     */
    public function standard_top_of_body_html() {
        return  $this->page->requires->get_top_of_body_code();
    }

    /**
     * The standard tags (typically performance information and validation links,
     * if we are in developer debug mode) that should be output in the footer area
     * of the page. Designed to be called in theme layout.php files.
     * @return string HTML fragment.
     */
    public function standard_footer_html() {
        global $CFG;

        // This function is normally called from a layout.php file in {@link header()}
        // but some of the content won't be known until later, so we return a placeholder
        // for now. This will be replaced with the real content in {@link footer()}.
        $output = self::PERFORMANCE_INFO_TOKEN;
        if (!empty($CFG->debugpageinfo)) {
            $output .= '<div class="performanceinfo">This page is: ' . $this->page->debug_summary() . '</div>';
        }
        if (!empty($CFG->debugvalidators)) {
            $output .= '<div class="validators"><ul>
              <li><a href="http://validator.w3.org/check?verbose=1&amp;ss=1&amp;uri=' . urlencode(qualified_me()) . '">Validate HTML</a></li>
              <li><a href="http://www.contentquality.com/mynewtester/cynthia.exe?rptmode=-1&amp;url1=' . urlencode(qualified_me()) . '">Section 508 Check</a></li>
              <li><a href="http://www.contentquality.com/mynewtester/cynthia.exe?rptmode=0&amp;warnp2n3e=1&amp;url1=' . urlencode(qualified_me()) . '">WCAG 1 (2,3) Check</a></li>
            </ul></div>';
        }
        return $output;
    }

    /**
     * The standard tags (typically script tags that are not needed earlier) that
     * should be output after everything else, . Designed to be called in theme layout.php files.
     * @return string HTML fragment.
     */
    public function standard_end_of_body_html() {
        // This function is normally called from a layout.php file in {@link header()}
        // but some of the content won't be known until later, so we return a placeholder
        // for now. This will be replaced with the real content in {@link footer()}.
        echo self::END_HTML_TOKEN;
    }

    /**
     * Return the standard string that says whether you are logged in (and switched
     * roles/logged in as another user).
     * @return string HTML fragment.
     */
    public function login_info() {
        global $USER, $CFG, $DB;

        if (during_initial_install()) {
            return '';
        }

        $course = $this->page->course;

        if (session_is_loggedinas()) {
            $realuser = session_get_realuser();
            $fullname = fullname($realuser, true);
            $realuserinfo = " [<a href=\"$CFG->wwwroot/course/loginas.php?id=$course->id&amp;return=1&amp;sesskey=".sesskey()."\">$fullname</a>] ";
        } else {
            $realuserinfo = '';
        }

        $loginurl = get_login_url();

        if (empty($course->id)) {
            // $course->id is not defined during installation
            return '';
        } else if (isloggedin()) {
            $context = get_context_instance(CONTEXT_COURSE, $course->id);

            $fullname = fullname($USER, true);
            $username = "<a href=\"$CFG->wwwroot/user/view.php?id=$USER->id&amp;course=$course->id\">$fullname</a>";
            if (is_mnet_remote_user($USER) and $idprovider = $DB->get_record('mnet_host', array('id'=>$USER->mnethostid))) {
                $username .= " from <a href=\"{$idprovider->wwwroot}\">{$idprovider->name}</a>";
            }
            if (isset($USER->username) && $USER->username == 'guest') {
                $loggedinas = $realuserinfo.get_string('loggedinasguest').
                          " (<a href=\"$loginurl\">".get_string('login').'</a>)';
            } else if (!empty($USER->access['rsw'][$context->path])) {
                $rolename = '';
                if ($role = $DB->get_record('role', array('id'=>$USER->access['rsw'][$context->path]))) {
                    $rolename = ': '.format_string($role->name);
                }
                $loggedinas = get_string('loggedinas', 'moodle', $username).$rolename.
                          " (<a href=\"$CFG->wwwroot/course/view.php?id=$course->id&amp;switchrole=0&amp;sesskey=".sesskey()."\">".get_string('switchrolereturn').'</a>)';
            } else {
                $loggedinas = $realuserinfo.get_string('loggedinas', 'moodle', $username).' '.
                          " (<a href=\"$CFG->wwwroot/login/logout.php?sesskey=".sesskey()."\">".get_string('logout').'</a>)';
            }
        } else {
            $loggedinas = get_string('loggedinnot', 'moodle').
                          " (<a href=\"$loginurl\">".get_string('login').'</a>)';
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
     * Return the 'back' link that normally appears in the footer.
     * @return string HTML fragment.
     */
    public function home_link() {
        global $CFG, $SITE;

        if ($this->page->pagetype == 'site-index') {
            // Special case for site home page - please do not remove
            return '<div class="sitelink">' .
                   '<a title="Moodle" href="http://moodle.org/">' .
                   '<img style="width:100px;height:30px" src="' . $this->pix_url('moodlelogo') . '" alt="moodlelogo" /></a></div>';

        } else if (!empty($CFG->target_release) && $CFG->target_release != $CFG->release) {
            // Special case for during install/upgrade.
            return '<div class="sitelink">'.
                   '<a title="Moodle" href="http://docs.moodle.org/en/Administrator_documentation" onclick="this.target=\'_blank\'">' .
                   '<img style="width:100px;height:30px" src="' . $this->pix_url('moodlelogo') . '" alt="moodlelogo" /></a></div>';

        } else if ($this->page->course->id == $SITE->id || strpos($this->page->pagetype, 'course-view') === 0) {
            return '<div class="homelink"><a href="' . $CFG->wwwroot . '/">' .
                    get_string('home') . '</a></div>';

        } else {
            return '<div class="homelink"><a href="' . $CFG->wwwroot . '/course/view.php?id=' . $this->page->course->id . '">' .
                    format_string($this->page->course->shortname) . '</a></div>';
        }
    }

    /**
     * Redirects the user by any means possible given the current state
     *
     * This function should not be called directly, it should always be called using
     * the redirect function in lib/weblib.php
     *
     * The redirect function should really only be called before page output has started
     * however it will allow itself to be called during the state STATE_IN_BODY
     *
     * @param string $encodedurl The URL to send to encoded if required
     * @param string $message The message to display to the user if any
     * @param int $delay The delay before redirecting a user, if $message has been
     *         set this is a requirement and defaults to 3, set to 0 no delay
     * @param boolean $debugdisableredirect this redirect has been disabled for
     *         debugging purposes. Display a message that explains, and don't
     *         trigger the redirect.
     * @return string The HTML to display to the user before dying, may contain
     *         meta refresh, javascript refresh, and may have set header redirects
     */
    public function redirect_message($encodedurl, $message, $delay, $debugdisableredirect) {
        global $CFG;
        $url = str_replace('&amp;', '&', $encodedurl);

        switch ($this->page->state) {
            case moodle_page::STATE_BEFORE_HEADER :
                // No output yet it is safe to delivery the full arsenal of redirect methods
                if (!$debugdisableredirect) {
                    // Don't use exactly the same time here, it can cause problems when both redirects fire at the same time.
                    $this->metarefreshtag = '<meta http-equiv="refresh" content="'. $delay .'; url='. $encodedurl .'" />'."\n";
                    $this->page->requires->js_function_call('document.location.replace', array($url), false, ($delay + 3));
                }
                $output = $this->header();
                break;
            case moodle_page::STATE_PRINTING_HEADER :
                // We should hopefully never get here
                throw new coding_exception('You cannot redirect while printing the page header');
                break;
            case moodle_page::STATE_IN_BODY :
                // We really shouldn't be here but we can deal with this
                debugging("You should really redirect before you start page output");
                if (!$debugdisableredirect) {
                    $this->page->requires->js_function_call('document.location.replace', array($url), false, $delay);
                }
                $output = $this->opencontainers->pop_all_but_last();
                break;
            case moodle_page::STATE_DONE :
                // Too late to be calling redirect now
                throw new coding_exception('You cannot redirect after the entire page has been generated');
                break;
        }
        $output .= $this->notification($message, 'redirectmessage');
        $output .= '<a href="'. $encodedurl .'">'. get_string('continue') .'</a>';
        if ($debugdisableredirect) {
            $output .= '<p><strong>Error output, so disabling automatic redirect.</strong></p>';
        }
        $output .= $this->footer();
        return $output;
    }

    /**
     * Start output by sending the HTTP headers, and printing the HTML <head>
     * and the start of the <body>.
     *
     * To control what is printed, you should set properties on $PAGE. If you
     * are familiar with the old {@link print_header()} function from Moodle 1.9
     * you will find that there are properties on $PAGE that correspond to most
     * of the old parameters to could be passed to print_header.
     *
     * Not that, in due course, the remaining $navigation, $menu parameters here
     * will be replaced by more properties of $PAGE, but that is still to do.
     *
     * @return string HTML that you must output this, preferably immediately.
     */
    public function header() {
        global $USER, $CFG;

        $this->page->set_state(moodle_page::STATE_PRINTING_HEADER);

        // Find the appropriate page layout file, based on $this->page->pagelayout.
        $layoutfile = $this->page->theme->layout_file($this->page->pagelayout);
        // Render the layout using the layout file.
        $rendered = $this->render_page_layout($layoutfile);

        // Slice the rendered output into header and footer.
        $cutpos = strpos($rendered, self::MAIN_CONTENT_TOKEN);
        if ($cutpos === false) {
            throw new coding_exception('page layout file ' . $layoutfile .
                    ' does not contain the string "' . self::MAIN_CONTENT_TOKEN . '".');
        }
        $header = substr($rendered, 0, $cutpos);
        $footer = substr($rendered, $cutpos + strlen(self::MAIN_CONTENT_TOKEN));

        if (empty($this->contenttype)) {
            debugging('The page layout file did not call $OUTPUT->doctype()');
            $header = $this->doctype() . $header;
        }

        send_headers($this->contenttype, $this->page->cacheable);

        $this->opencontainers->push('header/footer', $footer);
        $this->page->set_state(moodle_page::STATE_IN_BODY);

        return $header . $this->skip_link_target();
    }

    /**
     * Renders and outputs the page layout file.
     * @param string $layoutfile The name of the layout file
     * @return string HTML code
     */
    protected function render_page_layout($layoutfile) {
        global $CFG, $SITE, $USER;
        // The next lines are a bit tricky. The point is, here we are in a method
        // of a renderer class, and this object may, or may not, be the same as
        // the global $OUTPUT object. When rendering the page layout file, we want to use
        // this object. However, people writing Moodle code expect the current
        // renderer to be called $OUTPUT, not $this, so define a variable called
        // $OUTPUT pointing at $this. The same comment applies to $PAGE and $COURSE.
        $OUTPUT = $this;
        $PAGE = $this->page;
        $COURSE = $this->page->course;

        ob_start();
        include($layoutfile);
        $rendered = ob_get_contents();
        ob_end_clean();
        return $rendered;
    }

    /**
     * Outputs the page's footer
     * @return string HTML fragment
     */
    public function footer() {
        global $CFG, $DB;

        $output = $this->container_end_all(true);

        $footer = $this->opencontainers->pop('header/footer');

        if (debugging() and $DB and $DB->is_transaction_started()) {
            // TODO: MDL-20625 print warning - transaction will be rolled back
        }

        // Provide some performance info if required
        $performanceinfo = '';
        if (defined('MDL_PERF') || (!empty($CFG->perfdebug) and $CFG->perfdebug > 7)) {
            $perf = get_performance_info();
            if (defined('MDL_PERFTOLOG') && !function_exists('register_shutdown_function')) {
                error_log("PERF: " . $perf['txt']);
            }
            if (defined('MDL_PERFTOFOOT') || debugging() || $CFG->perfdebug > 7) {
                $performanceinfo = $perf['html'];
            }
        }
        $footer = str_replace(self::PERFORMANCE_INFO_TOKEN, $performanceinfo, $footer);

        $footer = str_replace(self::END_HTML_TOKEN, $this->page->requires->get_end_code(), $footer);

        $this->page->set_state(moodle_page::STATE_DONE);


        return $output . $footer;
    }

    /**
     * Close all but the last open container. This is useful in places like error
     * handling, where you want to close all the open containers (apart from <body>)
     * before outputting the error message.
     * @param bool $shouldbenone assert that the stack should be empty now - causes a
     *      developer debug warning if it isn't.
     * @return string the HTML required to close any open containers inside <body>.
     */
    public function container_end_all($shouldbenone = false) {
        return $this->opencontainers->pop_all_but_last($shouldbenone);
    }

    /**
     * Returns lang menu or '', this method also checks forcing of languages in courses.
     * @return string
     */
    public function lang_menu() {
        global $CFG;

        if (empty($CFG->langmenu)) {
            return '';
        }

        if ($this->page->course != SITEID and !empty($this->page->course->lang)) {
            // do not show lang menu if language forced
            return '';
        }

        $currlang = current_language();
        $langs = get_list_of_languages();

        if (count($langs) < 2) {
            return '';
        }

        $s = new single_select($this->page->url, 'lang', $langs, $currlang, null);
        $s->label = get_accesshide(get_string('language'));
        $s->class = 'langmenu';
        return $this->render($s);
    }

    /**
     * Output the row of editing icons for a block, as defined by the controls array.
     * @param array $controls an array like {@link block_contents::$controls}.
     * @return HTML fragment.
     */
    public function block_controls($controls) {
        if (empty($controls)) {
            return '';
        }
        $controlshtml = array();
        foreach ($controls as $control) {
            $controlshtml[] = html_writer::tag('a',
                    html_writer::empty_tag('img',  array('src' => $this->pix_url($control['icon'])->out(false), 'alt' => $control['caption'])),
                    array('class' => 'icon','title' => $control['caption'], 'href' => $control['url']));
        }
        return html_writer::tag('div', implode('', $controlshtml), array('class' => 'commands'));
    }

    /**
     * Prints a nice side block with an optional header.
     *
     * The content is described
     * by a {@link block_contents} object.
     *
     * @param block_contents $bc HTML for the content
     * @param string $region the region the block is appearing in.
     * @return string the HTML to be output.
     */
    function block(block_contents $bc, $region) {
        $bc = clone($bc); // Avoid messing up the object passed in.
        if (empty($bc->blockinstanceid) || !strip_tags($bc->title)) {
            $bc->collapsible = block_contents::NOT_HIDEABLE;
        }
        if ($bc->collapsible == block_contents::HIDDEN) {
            $bc->add_class('hidden');
        }
        if (!empty($bc->controls)) {
            $bc->add_class('block_with_controls');
        }

        $skiptitle = strip_tags($bc->title);
        if (empty($skiptitle)) {
            $output = '';
            $skipdest = '';
        } else {
            $output = html_writer::tag('a', get_string('skipa', 'access', $skiptitle), array('href' => '#sb-' . $bc->skipid, 'class' => 'skip-block'));
            $skipdest = html_writer::tag('span', '', array('id' => 'sb-' . $bc->skipid, 'class' => 'skip-block-to'));
        }

        $output .= html_writer::start_tag('div', $bc->attributes);

        $controlshtml = $this->block_controls($bc->controls);

        $title = '';
        if ($bc->title) {
            $title = html_writer::tag('h2', $bc->title, null);
        }

        if ($title || $controlshtml) {
            $output .= html_writer::tag('div', html_writer::tag('div', html_writer::tag('div', '', array('class'=>'block_action')). $title . $controlshtml, array('class' => 'title')), array('class' => 'header'));
        }

        $output .= html_writer::start_tag('div', array('class' => 'content'));
        if (!$title && !$controlshtml) {
            $output .= html_writer::tag('div', '', array('class'=>'block_action notitle'));
        }
        $output .= $bc->content;

        if ($bc->footer) {
            $output .= html_writer::tag('div', $bc->footer, array('class' => 'footer'));
        }

        $output .= html_writer::end_tag('div');
        $output .= html_writer::end_tag('div');

        if ($bc->annotation) {
            $output .= html_writer::tag('div', $bc->annotation, array('class' => 'blockannotation'));
        }
        $output .= $skipdest;

        $this->init_block_hider_js($bc);
        return $output;
    }

    /**
     * Calls the JS require function to hide a block.
     * @param block_contents $bc A block_contents object
     * @return void
     */
    protected function init_block_hider_js(block_contents $bc) {
        if (!empty($bc->attributes['id']) and $bc->collapsible != block_contents::NOT_HIDEABLE) {
            $userpref = 'block' . $bc->blockinstanceid . 'hidden';
            user_preference_allow_ajax_update($userpref, PARAM_BOOL);
            $this->page->requires->yui2_lib('dom');
            $this->page->requires->yui2_lib('event');
            $plaintitle = strip_tags($bc->title);
            $this->page->requires->js_function_call('new block_hider', array($bc->attributes['id'], $userpref,
                    get_string('hideblocka', 'access', $plaintitle), get_string('showblocka', 'access', $plaintitle),
                    $this->pix_url('t/switch_minus')->out(false), $this->pix_url('t/switch_plus')->out(false)));
        }
    }

    /**
     * Render the contents of a block_list.
     * @param array $icons the icon for each item.
     * @param array $items the content of each item.
     * @return string HTML
     */
    public function list_block_contents($icons, $items) {
        $row = 0;
        $lis = array();
        foreach ($items as $key => $string) {
            $item = html_writer::start_tag('li', array('class' => 'r' . $row));
            if (!empty($icons[$key])) { //test if the content has an assigned icon
                $item .= html_writer::tag('div', $icons[$key], array('class' => 'icon column c0'));
            }
            $item .= html_writer::tag('div', $string, array('class' => 'column c1'));
            $item .= html_writer::end_tag('li');
            $lis[] = $item;
            $row = 1 - $row; // Flip even/odd.
        }
        return html_writer::tag('ul', implode("\n", $lis), array('class' => 'list'));
    }

    /**
     * Output all the blocks in a particular region.
     * @param string $region the name of a region on this page.
     * @return string the HTML to be output.
     */
    public function blocks_for_region($region) {
        $blockcontents = $this->page->blocks->get_content_for_region($region, $this);

        $output = '';
        foreach ($blockcontents as $bc) {
            if ($bc instanceof block_contents) {
                $output .= $this->block($bc, $region);
            } else if ($bc instanceof block_move_target) {
                $output .= $this->block_move_target($bc);
            } else {
                throw new coding_exception('Unexpected type of thing (' . get_class($bc) . ') found in list of block contents.');
            }
        }
        return $output;
    }

    /**
     * Output a place where the block that is currently being moved can be dropped.
     * @param block_move_target $target with the necessary details.
     * @return string the HTML to be output.
     */
    public function block_move_target($target) {
        return html_writer::tag('a', html_writer::tag('span', $target->text, array('class' => 'accesshide')), array('href' => $target->url, 'class' => 'blockmovetarget'));
    }

    /**
     * Renders a sepcial html link with attached action
     *
     * @param string|moodle_url $url
     * @param string $text HTML fragment
     * @param component_action $action
     * @param array $attributes associative array of html link attributes + disabled
     * @return HTML fragment
     */
    public function action_link($url, $text, component_action $action = null, array $attributes=null) {
        if (!($url instanceof moodle_url)) {
            $url = new moodle_url($url);
        }
        $link = new action_link($url, $text, $action, $attributes);

        return $this->render($link);
    }

    /**
     * Implementation of action_link rendering
     * @param action_link $link
     * @return string HTML fragment
     */
    protected function render_action_link(action_link $link) {
        global $CFG;

        // A disabled link is rendered as formatted text
        if (!empty($link->attributes['disabled'])) {
            // do not use div here due to nesting restriction in xhtml strict
            return html_writer::tag('span', $link->text, array('class'=>'currentlink'));
        }

        $attributes = $link->attributes;
        unset($link->attributes['disabled']);
        $attributes['href'] = $link->url;

        if ($link->actions) {
            if (empty($attributes['id'])) {
                $id = html_writer::random_id('action_link');
                $attributes['id'] = $id;
            } else {
                $id = $attributes['id'];
            }
            foreach ($link->actions as $action) {
                $this->add_action_handler($action, $id);
            }
        }

        return html_writer::tag('a', $link->text, $attributes);
    }


    /**
     * Similar to action_link, image is used instead of the text
     *
     * @param string|moodle_url $url A string URL or moodel_url
     * @param pix_icon $pixicon
     * @param component_action $action
     * @param array $attributes associative array of html link attributes + disabled
     * @param bool $linktext show title next to image in link
     * @return string HTML fragment
     */
    public function action_icon($url, pix_icon $pixicon, component_action $action = null, array $attributes = null, $linktext=false) {
        if (!($url instanceof moodle_url)) {
            $url = new moodle_url($url);
        }
        $attributes = (array)$attributes;

        if (empty($attributes['class'])) {
            // let ppl override the class via $options
            $attributes['class'] = 'action-icon';
        }

        $icon = $this->render($pixicon);

        if ($linktext) {
            $text = $pixicon->attributes['alt'];
        } else {
            $text = '';
        }

        return $this->action_link($url, $text.$icon, $action, $attributes);
    }

   /**
    * Print a message along with button choices for Continue/Cancel
    *
    * If a string or moodle_url is given instead of a single_button, method defaults to post.
    *
    * @param string $message The question to ask the user
    * @param single_button|moodle_url|string $continue The single_button component representing the Continue answer. Can also be a moodle_url or string URL
    * @param single_button|moodle_url|string $cancel The single_button component representing the Cancel answer. Can also be a moodle_url or string URL
    * @return string HTML fragment
    */
    public function confirm($message, $continue, $cancel) {
        if ($continue instanceof single_button) {
            // ok
        } else if (is_string($continue)) {
            $continue = new single_button(new moodle_url($continue), get_string('continue'), 'post');
        } else if ($continue instanceof moodle_url) {
            $continue = new single_button($continue, get_string('continue'), 'post');
        } else {
            throw new coding_exception('The continue param to $OUTPUT->confirm() must be either a URL (string/moodle_url) or a single_button instance.');
        }

        if ($cancel instanceof single_button) {
            // ok
        } else if (is_string($cancel)) {
            $cancel = new single_button(new moodle_url($cancel), get_string('cancel'), 'get');
        } else if ($cancel instanceof moodle_url) {
            $cancel = new single_button($cancel, get_string('cancel'), 'get');
        } else {
            throw new coding_exception('The cancel param to $OUTPUT->confirm() must be either a URL (string/moodle_url) or a single_button instance.');
        }

        $output = $this->box_start('generalbox', 'notice');
        $output .= html_writer::tag('p', $message);
        $output .= html_writer::tag('div', $this->render($continue) . $this->render($cancel), array('class' => 'buttons'));
        $output .= $this->box_end();
        return $output;
    }

    /**
     * Returns a form with a single button.
     *
     * @param string|moodle_url $url
     * @param string $label button text
     * @param string $method get or post submit method
     * @param array $options associative array {disabled, title, etc.}
     * @return string HTML fragment
     */
    public function single_button($url, $label, $method='post', array $options=null) {
        if (!($url instanceof moodle_url)) {
            $url = new moodle_url($url);
        }
        $button = new single_button($url, $label, $method);

        foreach ((array)$options as $key=>$value) {
            if (array_key_exists($key, $button)) {
                $button->$key = $value;
            }
        }

        return $this->render($button);
    }

    /**
     * Internal implementation of single_button rendering
     * @param single_button $button
     * @return string HTML fragment
     */
    protected function render_single_button(single_button $button) {
        $attributes = array('type'     => 'submit',
                            'value'    => $button->label,
                            'disabled' => $button->disabled ? 'disabled' : null,
                            'title'    => $button->tooltip);

        if ($button->actions) {
            $id = html_writer::random_id('single_button');
            $attributes['id'] = $id;
            foreach ($button->actions as $action) {
                $this->add_action_handler($action, $id);
            }
        }

        // first the input element
        $output = html_writer::empty_tag('input', $attributes);

        // then hidden fields
        $params = $button->url->params();
        if ($button->method === 'post') {
            $params['sesskey'] = sesskey();
        }
        foreach ($params as $var => $val) {
            $output .= html_writer::empty_tag('input', array('type' => 'hidden', 'name' => $var, 'value' => $val));
        }

        // then div wrapper for xhtml strictness
        $output = html_writer::tag('div', $output);

        // now the form itself around it
        $url = $button->url->out_omit_querystring(); // url without params
        if ($url === '') {
            $url = '#'; // there has to be always some action
        }
        $attributes = array('method' => $button->method,
                            'action' => $url,
                            'id'     => $button->formid);
        $output = html_writer::tag('form', $output, $attributes);

        // and finally one more wrapper with class
        return html_writer::tag('div', $output, array('class' => $button->class));
    }

    /**
     * Returns a form with a single select widget.
     * @param moodle_url $url form action target, includes hidden fields
     * @param string $name name of selection field - the changing parameter in url
     * @param array $options list of options
     * @param string $selected selected element
     * @param array $nothing
     * @param string $formid
     * @return string HTML fragment
     */
    public function single_select($url, $name, array $options, $selected='', $nothing=array(''=>'choosedots'), $formid=null) {
        if (!($url instanceof moodle_url)) {
            $url = new moodle_url($url);
        }
        $select = new single_select($url, $name, $options, $selected, $nothing, $formid);

        return $this->render($select);
    }

    /**
     * Internal implementation of single_select rendering
     * @param single_select $select
     * @return string HTML fragment
     */
    protected function render_single_select(single_select $select) {
        $select = clone($select);
        if (empty($select->formid)) {
            $select->formid = html_writer::random_id('single_select_f');
        }

        $output = '';
        $params = $select->url->params();
        if ($select->method === 'post') {
            $params['sesskey'] = sesskey();
        }
        foreach ($params as $name=>$value) {
            $output .= html_writer::empty_tag('input', array('type'=>'hidden', 'name'=>$name, 'value'=>$value));
        }

        if (empty($select->attributes['id'])) {
            $select->attributes['id'] = html_writer::random_id('single_select');
        }

        if ($select->disabled) {
            $select->attributes['disabled'] = 'disabled';
        }

        if ($select->tooltip) {
            $select->attributes['title'] = $select->tooltip;
        }

        if ($select->label) {
            $output .= html_writer::tag('label', $select->label, array('for'=>$select->attributes['id']));
        }

        if ($select->helpicon instanceof help_icon) {
            $output .= $this->render($select->helpicon);
        }

        $output .= html_writer::select($select->options, $select->name, $select->selected, $select->nothing, $select->attributes);

        $go = html_writer::empty_tag('input', array('type'=>'submit', 'value'=>get_string('go')));
        $output .= html_writer::tag('noscript', $go, array('style'=>'inline'));

        $nothing = empty($select->nothing) ? false : key($select->nothing);
        $this->page->requires->js_init_call('M.util.init_select_autosubmit', array($select->formid, $select->attributes['id'], $nothing));

        // then div wrapper for xhtml strictness
        $output = html_writer::tag('div', $output);

        // now the form itself around it
        $formattributes = array('method' => $select->method,
                                'action' => $select->url->out_omit_querystring(),
                                'id'     => $select->formid);
        $output = html_writer::tag('form', $output, $formattributes);

        // and finally one more wrapper with class
        return html_writer::tag('div', $output, array('class' => $select->class));
    }

    /**
     * Returns a form with a url select widget.
     * @param array $urls list of urls - array('/course/view.php?id=1'=>'Frontpage', ....)
     * @param string $selected selected element
     * @param array $nothing
     * @param string $formid
     * @return string HTML fragment
     */
    public function url_select(array $urls, $selected, $nothing=array(''=>'choosedots'), $formid=null) {
        $select = new url_select($urls, $selected, $nothing, $formid);
        return $this->render($select);
    }

    /**
     * Internal implementation of url_select rendering
     * @param single_select $select
     * @return string HTML fragment
     */
    protected function render_url_select(url_select $select) {
        $select = clone($select);
        if (empty($select->formid)) {
            $select->formid = html_writer::random_id('url_select_f');
        }

        if (empty($select->attributes['id'])) {
            $select->attributes['id'] = html_writer::random_id('url_select');
        }

        if ($select->disabled) {
            $select->attributes['disabled'] = 'disabled';
        }

        if ($select->tooltip) {
            $select->attributes['title'] = $select->tooltip;
        }

        $output = '';

        if ($select->label) {
            $output .= html_writer::tag('label', $select->label, array('for'=>$select->attributes['id']));
        }

        if ($select->helpicon instanceof help_icon) {
            $output .= $this->render($select->helpicon);
        }

        $output .= html_writer::empty_tag('input', array('type'=>'hidden', 'name'=>'sesskey', 'value'=>sesskey()));
        $output .= html_writer::select($select->urls, 'jump', $select->selected, $select->nothing, $select->attributes);

        $go = html_writer::empty_tag('input', array('type'=>'submit', 'value'=>get_string('go')));
        $output .= html_writer::tag('noscript', $go, array('style'=>'inline'));

        $nothing = empty($select->nothing) ? false : key($select->nothing);
        $output .= $this->page->requires->js_init_call('M.util.init_url_select', array($select->formid, $select->attributes['id'], $nothing));

        // then div wrapper for xhtml strictness
        $output = html_writer::tag('div', $output);

        // now the form itself around it
        $formattributes = array('method' => 'post',
                                'action' => new moodle_url('/course/jumpto.php'),
                                'id'     => $select->formid);
        $output = html_writer::tag('form', $output, $formattributes);

        // and finally one more wrapper with class
        return html_writer::tag('div', $output, array('class' => $select->class));
    }

    /**
     * Returns a string containing a link to the user documentation.
     * Also contains an icon by default. Shown to teachers and admin only.
     * @param string $path The page link after doc root and language, no leading slash.
     * @param string $text The text to be displayed for the link
     * @retrun string
     */
    public function doc_link($path, $text) {
        global $CFG;

        $icon = $this->pix_icon('docs', $text, 'moodle', array('class'=>'iconhelp'));

        $url = new moodle_url(get_docs_url($path));

        $attributes = array('href'=>$url);
        if (!empty($CFG->doctonewwindow)) {
            $attributes['id'] = $this->add_action_handler(new popup_action('click', $url));
        }

        return html_writer::tag('a', $icon.$text, $attributes);
    }

    /**
     * Render icon
     * @param string $pix short pix name
     * @param string $alt mandatory alt attribute
     * @param strin $component standard compoennt name like 'moodle', 'mod_form', etc.
     * @param array $attributes htm lattributes
     * @return string HTML fragment
     */
    public function pix_icon($pix, $alt, $component='moodle', array $attributes = null) {
        $icon = new pix_icon($pix, $alt, $component, $attributes);
        return $this->render($icon);
    }

    /**
     * Render icon
     * @param pix_icon $icon
     * @return string HTML fragment
     */
    protected function render_pix_icon(pix_icon $icon) {
        $attributes = $icon->attributes;
        $attributes['src'] = $this->pix_url($icon->pix, $icon->component);
        return html_writer::empty_tag('img', $attributes);
    }

    /**
    * Produces the html that represents this rating in the UI
    * @param $page the page object on which this rating will appear
    */
    function render_rating(rating $rating) {
        global $CFG, $USER;
        static $havesetupjavascript = false;

        $useajax = !empty($CFG->enableajax);

        //include required Javascript
        if( !$havesetupjavascript && $useajax ) {
            $this->page->requires->js_init_call('M.core_ratings.init');
            $havesetupjavascript = true;
        }

        $strrate = get_string("rate", "rating");
        $strratings = ''; //the string we'll return

        if($rating->settings->permissions->canview || $rating->settings->permissions->canviewall) {
            switch ($rating->settings->aggregationmethod) {
                case RATING_AGGREGATE_AVERAGE :
                    $strratings .= get_string("aggregateavg", "forum");
                    break;
                case RATING_AGGREGATE_COUNT :
                    $strratings .= get_string("aggregatecount", "forum");
                    break;
                case RATING_AGGREGATE_MAXIMUM :
                    $strratings .= get_string("aggregatemax", "forum");
                    break;
                case RATING_AGGREGATE_MINIMUM :
                    $strratings .= get_string("aggregatemin", "forum");
                    break;
                case RATING_AGGREGATE_SUM :
                    $strratings .= get_string("aggregatesum", "forum");
                    break;
            }

            if (empty($strratings)) {
                $strratings .= $strrate;
            }
            $strratings .= ': ';

            $scalemax = 0;
            $ratingstr = null;

            if ( is_array($rating->settings->scale->scaleitems) ) {
                $scalemax = $rating->settings->scale->scaleitems[ count($rating->settings->scale->scaleitems)-1 ];
                $ratingstr = $rating->settings->scale->scaleitems[$rating->rating];
            }
            else { //its numeric
                $scalemax = $rating->settings->scale->scaleitems;
                $ratingstr = round($rating->aggregate,1);
            }

            $aggstr = "{$ratingstr} / $scalemax ({$rating->count}) ";

            if ($rating->settings->permissions->canviewall) {
                $link = new moodle_url("/rating/index.php?contextid={$rating->context->id}&itemid={$rating->itemid}&scaleid={$rating->scaleid}");
                $action = new popup_action('click', $link, 'ratings', array('height' => 400, 'width' => 600));
                $strratings .= $this->action_link($link, $aggstr, $action);
            } else if ($rating->settings->permissions->canview) {
                $strratings .= $aggstr;
            }
        }

        //todo andrew alter the below if to deny guest users the ability to post ratings.
        //Petr to define "guest"
        $formstart = null;
        if($rating->settings->permissions->canrate) {
            //dont use $rating->userid below as it will be null if the user hasnt already rated the item
            $formstart = <<<END
<form id="postrating{$rating->itemid}" class="postratingform" method="post" action="rating/rate.php">
<div class="ratingform">
<input type="hidden" class="ratinginput" name="contextid" value="{$rating->context->id}" />
<input type="hidden" class="ratinginput" name="itemid" value="{$rating->itemid}" />
<input type="hidden" class="ratinginput" name="scaleid" value="{$rating->settings->scale->id}" />
<input type="hidden" class="ratinginput" name="returnurl" value="{$rating->settings->returnurl}" />
END;
            $strratings = $formstart.$strratings;

            //generate an array of values for numeric scales
            $scalearray = $rating->settings->scale->scaleitems;
            if( !is_array($scalearray) ) { //almost certainly a numerical scale
                $intscalearray = intval($scalearray);//just in case theyve passed "5" instead of 5
                if( is_int($intscalearray) && $intscalearray>0 ){
                    $scalearray = array();
                    for($i=0; $i<=$rating->settings->scale->scaleitems; $i++) {
                        $scalearray[$i] = $i;
                    }
                }
            }

            $scalearray = array(RATING_UNSET_RATING => $strrate.'...') + $scalearray;
            $strratings .= html_writer::select($scalearray, 'rating', $rating->rating, false, array('class'=>'postratingmenu ratinginput','id'=>'menurating'.$rating->itemid));

            //output submit button
            $strratings .= '<span class="ratingsubmit"><input type="submit" class="postratingmenusubmit" id="postratingsubmit'.$rating->itemid.'" value="'.s(get_string('rate', 'rating')).'" />';

            if ( is_array($rating->settings->scale) ) {
                //todo andrew where can we get the course id from?
                //$strratings .= $this->help_icon_scale($course->id, $scale);
                $strratings .= $this->help_icon_scale(1, $rating->settings->scale);
            }
            $strratings .= '</span></div></form>';
        }

        return $strratings;
    }

    /*
     * Centered heading with attached help button (same title text)
     * and optional icon attached
     * @param string $text A heading text
     * @param string $page The keyword that defines a help page
     * @param string $component component name
     * @param string|moodle_url $icon
     * @param string $iconalt icon alt text
     * @return string HTML fragment
     */
    public function heading_with_help($text, $helppage, $component='moodle', $icon='', $iconalt='') {
        $image = '';
        if ($icon) {
            $image = $this->pix_icon($icon, $iconalt, $component, array('class'=>'icon'));
        }

        $help = $this->help_icon($helppage, $text, $component);

        return $this->heading($image.$text.$help, 2, 'main help');
    }

    /**
     * Print a help icon.
     *
     * @param string $page The keyword that defines a help page
     * @param string $title A descriptive text for accessibility only
     * @param string $component component name
     * @param string|bool $linktext true means use $title as link text, string means link text value
     * @return string HTML fragment
     */
    public function help_icon($helppage, $title, $component = 'moodle', $linktext='') {
        $icon = new help_icon($helppage, $title, $component);
        if ($linktext === true) {
            $icon->linktext = $title;
        } else if (!empty($linktext)) {
            $icon->linktext = $linktext;
        }
        return $this->render($icon);
    }

    /**
     * Implementation of user image rendering.
     * @param help_icon $helpicon
     * @return string HTML fragment
     */
    protected function render_help_icon(help_icon $helpicon) {
        global $CFG;

        // first get the help image icon
        $src = $this->pix_url('help');

        if (empty($helpicon->linktext)) {
            $alt = $helpicon->title;
        } else {
            $alt = get_string('helpwiththis');
        }

        $attributes = array('src'=>$src, 'alt'=>$alt, 'class'=>'iconhelp');
        $output = html_writer::empty_tag('img', $attributes);

        // add the link text if given
        if (!empty($helpicon->linktext)) {
            // the spacing has to be done through CSS
            $output .= $helpicon->linktext;
        }

        // now create the link around it - TODO: this will be changed during the big lang cleanup in 2.0
        $url = new moodle_url('/help.php', array('module' => $helpicon->component, 'file' => $helpicon->helppage .'.html'));

        // note: this title is displayed only if JS is disabled, otherwise the link will have the new ajax tooltip
        $title = get_string('helpprefix2', '', trim($helpicon->title, ". \t"));

        $attributes = array('href'=>$url, 'title'=>$title);
        $id = html_writer::random_id('helpicon');
        $attributes['id'] = $id;
        $this->add_action_handler(new popup_action('click', $url), $id);
        $output = html_writer::tag('a', $output, $attributes);

        // and finally span
        return html_writer::tag('span', $output, array('class' => 'helplink'));
    }

    /**
     * Print scale help icon.
     *
     * @param int $courseid
     * @param object $scale instance
     * @return string  HTML fragment
     */
    public function help_icon_scale($courseid, stdClass $scale) {
        global $CFG;

        $title = get_string('helpprefix2', '', $scale->name) .' ('.get_string('newwindow').')';

        $icon = $this->pix_icon('help', get_string('scales'), 'moodle', array('class'=>'iconhelp'));

        $link = new moodle_url('/course/scales.php', array('id' => $courseid, 'list' => true, 'scaleid' => $scale->id));
        $action = new popup_action('click', $link, 'ratingscale');

        return html_writer::tag('span', $this->action_link($link, $icon, $action), array('class' => 'helplink'));
    }

    /**
     * Creates and returns a spacer image with optional line break.
     *
     * @param array $attributes
     * @param boo spacer
     * @return string HTML fragment
     */
    public function spacer(array $attributes = null, $br = false) {
        $attributes = (array)$attributes;
        if (empty($attributes['width'])) {
            $attributes['width'] = 1;
        }
        if (empty($options['height'])) {
            $attributes['height'] = 1;
        }
        $attributes['class'] = 'spacer';

        $output = $this->pix_icon('spacer', '', 'moodle', $attributes);

        if (!empty($br)) {
            $output .= '<br />';
        }

        return $output;
    }

    /**
     * Print the specified user's avatar.
     *
     * User avatar may be obtained in two ways:
     * <pre>
     * // Option 1: (shortcut for simple cases, preferred way)
     * // $user has come from the DB and has fields id, picture, imagealt, firstname and lastname
     * $OUTPUT->user_picture($user, array('popup'=>true));
     *
     * // Option 2:
     * $userpic = new user_picture($user);
     * // Set properties of $userpic
     * $userpic->popup = true;
     * $OUTPUT->render($userpic);
     * </pre>
     *
     * @param object Object with at least fields id, picture, imagealt, firstname, lastname
     *     If any of these are missing, the database is queried. Avoid this
     *     if at all possible, particularly for reports. It is very bad for performance.
     * @param array $options associative array with user picture options, used only if not a user_picture object,
     *     options are:
     *     - courseid=$this->page->course->id (course id of user profile in link)
     *     - size=35 (size of image)
     *     - link=true (make image clickable - the link leads to user profile)
     *     - popup=false (open in popup)
     *     - alttext=true (add image alt attribute)
     *     - class = image class attribute (default 'userpicture')
     * @return string HTML fragment
     */
    public function user_picture(stdClass $user, array $options = null) {
        $userpicture = new user_picture($user);
        foreach ((array)$options as $key=>$value) {
            if (array_key_exists($key, $userpicture)) {
                $userpicture->$key = $value;
            }
        }
        return $this->render($userpicture);
    }

    /**
     * Internal implementation of user image rendering.
     * @param user_picture $userpicture
     * @return string
     */
    protected function render_user_picture(user_picture $userpicture) {
        global $CFG, $DB;

        $user = $userpicture->user;

        if ($userpicture->alttext) {
            if (!empty($user->imagealt)) {
                $alt = $user->imagealt;
            } else {
                $alt = get_string('pictureof', '', fullname($user));
            }
        } else {
            $alt = '';
        }

        if (empty($userpicture->size)) {
            $file = 'f2';
            $size = 35;
        } else if ($userpicture->size === true or $userpicture->size == 1) {
            $file = 'f1';
            $size = 100;
        } else if ($userpicture->size >= 50) {
            $file = 'f1';
            $size = $userpicture->size;
        } else {
            $file = 'f2';
            $size = $userpicture->size;
        }

        $class = $userpicture->class;

        if (!empty($user->picture)) {
            require_once($CFG->libdir.'/filelib.php');
            $src = new moodle_url(get_file_url($user->id.'/'.$file.'.jpg', null, 'user'));
        } else { // Print default user pictures (use theme version if available)
            $class .= ' defaultuserpic';
            $src = $this->pix_url('u/' . $file);
        }

        $attributes = array('src'=>$src, 'alt'=>$alt, 'class'=>$class, 'width'=>$size, 'height'=>$size);

        // get the image html output fisrt
        $output = html_writer::empty_tag('img', $attributes);;

        // then wrap it in link if needed
        if (!$userpicture->link) {
            return $output;
        }

        if (empty($userpicture->courseid)) {
            $courseid = $this->page->course->id;
        } else {
            $courseid = $userpicture->courseid;
        }

        $url = new moodle_url('/user/view.php', array('id' => $user->id, 'course' => $courseid));

        $attributes = array('href'=>$url);

        if ($userpicture->popup) {
            $id = html_writer::random_id('userpicture');
            $attributes['id'] = $id;
            $this->add_action_handler(new popup_action('click', $url), $id);
        }

        return html_writer::tag('a', $output, $attributes);
    }

    /**
     * Prints the 'Update this Modulename' button that appears on module pages.
     *
     * @param string $cmid the course_module id.
     * @param string $modulename the module name, eg. "forum", "quiz" or "workshop"
     * @return string the HTML for the button, if this user has permission to edit it, else an empty string.
     */
    public function update_module_button($cmid, $modulename) {
        global $CFG;
        if (has_capability('moodle/course:manageactivities', get_context_instance(CONTEXT_MODULE, $cmid))) {
            $modulename = get_string('modulename', $modulename);
            $string = get_string('updatethis', '', $modulename);
            $url = new moodle_url("$CFG->wwwroot/course/mod.php", array('update' => $cmid, 'return' => true, 'sesskey' => sesskey()));
            return $this->single_button($url, $string);
        } else {
            return '';
        }
    }

    /**
     * Prints a "Turn editing on/off" button in a form.
     * @param moodle_url $url The URL + params to send through when clicking the button
     * @return string HTML the button
     */
    public function edit_button(moodle_url $url) {
        global $USER;
        if (!empty($USER->editing)) {
            $string = get_string('turneditingoff');
            $edit = '0';
        } else {
            $string = get_string('turneditingon');
            $edit = '1';
        }

        $url = new moodle_url($url, array('edit'=>$edit));

        return $this->single_button($url, $string);
    }

    /**
     * Prints a simple button to close a window
     *
     * @param string $text The lang string for the button's label (already output from get_string())
     * @return string html fragment
     */
    public function close_window_button($text='') {
        if (empty($text)) {
            $text = get_string('closewindow');
        }
        $button = new single_button(new moodle_url('#'), $text, 'get');
        $button->add_action(new component_action('click', 'close_window'));

        return $this->container($this->render($button), 'closewindow');
    }

    /**
     * Output an error message. By default wraps the error message in <span class="error">.
     * If the error message is blank, nothing is output.
     * @param string $message the error message.
     * @return string the HTML to output.
     */
    public function error_text($message) {
        if (empty($message)) {
            return '';
        }
        return html_writer::tag('span', $message, array('class' => 'error'));
    }

    /**
     * Do not call this function directly.
     *
     * To terminate the current script with a fatal error, call the {@link print_error}
     * function, or throw an exception. Doing either of those things will then call this
     * function to display the error, before terminating the execution.
     *
     * @param string $message The message to output
     * @param string $moreinfourl URL where more info can be found about the error
     * @param string $link Link for the Continue button
     * @param array $backtrace The execution backtrace
     * @param string $debuginfo Debugging information
     * @return string the HTML to output.
     */
    public function fatal_error($message, $moreinfourl, $link, $backtrace, $debuginfo = null) {

        $output = '';
        $obbuffer = '';

        if ($this->has_started()) {
            // we can not always recover properly here, we have problems with output buffering,
            // html tables, etc.
            $output .= $this->opencontainers->pop_all_but_last();

        } else {
            // It is really bad if library code throws exception when output buffering is on,
            // because the buffered text would be printed before our start of page.
            // NOTE: this hack might be behave unexpectedly in case output buffering is enabled in PHP.ini
            while (ob_get_level() > 0) {
                $obbuffer .= ob_get_clean();
            }

            // Header not yet printed
            if (isset($_SERVER['SERVER_PROTOCOL'])) {
                // server protocol should be always present, because this render
                // can not be used from command line or when outputting custom XML
                @header($_SERVER['SERVER_PROTOCOL'] . ' 404 Not Found');
            }
            $this->page->set_url('/'); // no url
            //$this->page->set_pagelayout('base'); //TODO: MDL-20676 blocks on error pages are weird, unfortunately it somehow detect the pagelayout from URL :-(
            $this->page->set_title(get_string('error'));
            $output .= $this->header();
        }

        $message = '<p class="errormessage">' . $message . '</p>'.
                '<p class="errorcode"><a href="' . $moreinfourl . '">' .
                get_string('moreinformation') . '</a></p>';
        $output .= $this->box($message, 'errorbox');

        if (debugging('', DEBUG_DEVELOPER)) {
            if (!empty($debuginfo)) {
                $debuginfo = s($debuginfo); // removes all nasty JS
                $debuginfo = str_replace("\n", '<br />', $debuginfo); // keep newlines
                $output .= $this->notification('<strong>Debug info:</strong> '.$debuginfo, 'notifytiny');
            }
            if (!empty($backtrace)) {
                $output .= $this->notification('<strong>Stack trace:</strong> '.format_backtrace($backtrace), 'notifytiny');
            }
            if ($obbuffer !== '' ) {
                $output .= $this->notification('<strong>Output buffer:</strong> '.s($obbuffer), 'notifytiny');
            }
        }

        if (!empty($link)) {
            $output .= $this->continue_button($link);
        }

        $output .= $this->footer();

        // Padding to encourage IE to display our error page, rather than its own.
        $output .= str_repeat(' ', 512);

        return $output;
    }

    /**
     * Output a notification (that is, a status message about something that has
     * just happened).
     *
     * @param string $message the message to print out
     * @param string $classes normally 'notifyproblem' or 'notifysuccess'.
     * @return string the HTML to output.
     */
    public function notification($message, $classes = 'notifyproblem') {
        return html_writer::tag('div', clean_text($message), array('class' => renderer_base::prepare_classes($classes)));
    }

    /**
     * Print a continue button that goes to a particular URL.
     *
     * @param string|moodle_url $url The url the button goes to.
     * @return string the HTML to output.
     */
    public function continue_button($url) {
        if (!($url instanceof moodle_url)) {
            $url = new moodle_url($url);
        }
        $button = new single_button($url, get_string('continue'), 'get');
        $button->class = 'continuebutton';

        return $this->render($button);
    }

    /**
     * Prints a single paging bar to provide access to other pages  (usually in a search)
     *
     * @param int $totalcount Thetotal number of entries available to be paged through
     * @param int $page The page you are currently viewing
     * @param int $perpage The number of entries that should be shown per page
     * @param string|moodle_url $baseurl url of the current page, the $pagevar parameter is added
     * @param string $pagevar name of page parameter that holds the page number
     * @return string the HTML to output.
     */
    public function paging_bar($totalcount, $page, $perpage, $baseurl, $pagevar = 'page') {
        $pb = new paging_bar($totalcount, $page, $perpage, $baseurl, $pagevar);
        return $this->render($pb);
    }

    /**
     * Internal implementation of paging bar rendering.
     * @param paging_bar $pagingbar
     * @return string
     */
    protected function render_paging_bar(paging_bar $pagingbar) {
        $output = '';
        $pagingbar = clone($pagingbar);
        $pagingbar->prepare($this, $this->page, $this->target);

        if ($pagingbar->totalcount > $pagingbar->perpage) {
            $output .= get_string('page') . ':';

            if (!empty($pagingbar->previouslink)) {
                $output .= '&#160;(' . $pagingbar->previouslink . ')&#160;';
            }

            if (!empty($pagingbar->firstlink)) {
                $output .= '&#160;' . $pagingbar->firstlink . '&#160;...';
            }

            foreach ($pagingbar->pagelinks as $link) {
                $output .= "&#160;&#160;$link";
            }

            if (!empty($pagingbar->lastlink)) {
                $output .= '&#160;...' . $pagingbar->lastlink . '&#160;';
            }

            if (!empty($pagingbar->nextlink)) {
                $output .= '&#160;&#160;(' . $pagingbar->nextlink . ')';
            }
        }

        return html_writer::tag('div', $output, array('class' => 'paging'));
    }

    /**
     * Output the place a skip link goes to.
     * @param string $id The target name from the corresponding $PAGE->requires->skip_link_to($target) call.
     * @return string the HTML to output.
     */
    public function skip_link_target($id = null) {
        return html_writer::tag('span', '', array('id' => $id));
    }

    /**
     * Outputs a heading
     * @param string $text The text of the heading
     * @param int $level The level of importance of the heading. Defaulting to 2
     * @param string $classes A space-separated list of CSS classes
     * @param string $id An optional ID
     * @return string the HTML to output.
     */
    public function heading($text, $level = 2, $classes = 'main', $id = null) {
        $level = (integer) $level;
        if ($level < 1 or $level > 6) {
            throw new coding_exception('Heading level must be an integer between 1 and 6.');
        }
        return html_writer::tag('h' . $level, $text, array('id' => $id, 'class' => renderer_base::prepare_classes($classes)));
    }

    /**
     * Outputs a box.
     * @param string $contents The contents of the box
     * @param string $classes A space-separated list of CSS classes
     * @param string $id An optional ID
     * @return string the HTML to output.
     */
    public function box($contents, $classes = 'generalbox', $id = null) {
        return $this->box_start($classes, $id) . $contents . $this->box_end();
    }

    /**
     * Outputs the opening section of a box.
     * @param string $classes A space-separated list of CSS classes
     * @param string $id An optional ID
     * @return string the HTML to output.
     */
    public function box_start($classes = 'generalbox', $id = null) {
        $this->opencontainers->push('box', html_writer::end_tag('div'));
        return html_writer::start_tag('div', array('id' => $id,
                'class' => 'box ' . renderer_base::prepare_classes($classes)));
    }

    /**
     * Outputs the closing section of a box.
     * @return string the HTML to output.
     */
    public function box_end() {
        return $this->opencontainers->pop('box');
    }

    /**
     * Outputs a container.
     * @param string $contents The contents of the box
     * @param string $classes A space-separated list of CSS classes
     * @param string $id An optional ID
     * @return string the HTML to output.
     */
    public function container($contents, $classes = null, $id = null) {
        return $this->container_start($classes, $id) . $contents . $this->container_end();
    }

    /**
     * Outputs the opening section of a container.
     * @param string $classes A space-separated list of CSS classes
     * @param string $id An optional ID
     * @return string the HTML to output.
     */
    public function container_start($classes = null, $id = null) {
        $this->opencontainers->push('container', html_writer::end_tag('div'));
        return html_writer::start_tag('div', array('id' => $id,
                'class' => renderer_base::prepare_classes($classes)));
    }

    /**
     * Outputs the closing section of a container.
     * @return string the HTML to output.
     */
    public function container_end() {
        return $this->opencontainers->pop('container');
    }

   /**
     * Make nested HTML lists out of the items
     *
     * The resulting list will look something like this:
     *
     * <pre>
     * <<ul>>
     * <<li>><div class='tree_item parent'>(item contents)</div>
     *      <<ul>
     *      <<li>><div class='tree_item'>(item contents)</div><</li>>
     *      <</ul>>
     * <</li>>
     * <</ul>>
     * </pre>
     *
     * @param array[]tree_item $items
     * @param array[string]string $attrs html attributes passed to the top of
     * the list
     * @return string HTML
     */
    function tree_block_contents($items, $attrs=array()) {
        // exit if empty, we don't want an empty ul element
        if (empty($items)) {
            return '';
        }
        // array of nested li elements
        $lis = array();
        foreach ($items as $item) {
            // this applies to the li item which contains all child lists too
            $content = $item->content($this);
            $liclasses = array($item->get_css_type());
            if (!$item->forceopen || (!$item->forceopen && $item->collapse) || (count($item->children)==0  && $item->nodetype==navigation_node::NODETYPE_BRANCH)) {
                $liclasses[] = 'collapsed';
            }
            if ($item->isactive === true) {
                $liclasses[] = 'current_branch';
            }
            $liattr = array('class'=>join(' ',$liclasses));
            // class attribute on the div item which only contains the item content
            $divclasses = array('tree_item');
            if (!empty($item->children)  || $item->nodetype==navigation_node::NODETYPE_BRANCH) {
                $divclasses[] = 'branch';
            } else {
                $divclasses[] = 'leaf';
            }
            if (!empty($item->classes) && count($item->classes)>0) {
                $divclasses[] = join(' ', $item->classes);
            }
            $divattr = array('class'=>join(' ', $divclasses));
            if (!empty($item->id)) {
                $divattr['id'] = $item->id;
            }
            $content = html_writer::tag('p', $content, $divattr) . $this->tree_block_contents($item->children);
            if (!empty($item->preceedwithhr) && $item->preceedwithhr===true) {
                $content = html_writer::empty_tag('hr') . $content;
            }
            $content = html_writer::tag('li', $content, $liattr);
            $lis[] = $content;
        }
        return html_writer::tag('ul', implode("\n", $lis), $attrs);
    }

    /**
     * Return the navbar content so that it can be echoed out by the layout
     * @return string XHTML navbar
     */
    public function navbar() {
        return $this->page->navbar->content();
    }

    /**
     * Accessibility: Right arrow-like character is
     * used in the breadcrumb trail, course navigation menu
     * (previous/next activity), calendar, and search forum block.
     * If the theme does not set characters, appropriate defaults
     * are set automatically. Please DO NOT
     * use &lt; &gt; &raquo; - these are confusing for blind users.
     * @return string
     */
    public function rarrow() {
        return $this->page->theme->rarrow;
    }

    /**
     * Accessibility: Right arrow-like character is
     * used in the breadcrumb trail, course navigation menu
     * (previous/next activity), calendar, and search forum block.
     * If the theme does not set characters, appropriate defaults
     * are set automatically. Please DO NOT
     * use &lt; &gt; &raquo; - these are confusing for blind users.
     * @return string
     */
    public function larrow() {
        return $this->page->theme->larrow;
    }

    /**
     * Returns the colours of the small MP3 player
     * @return string
     */
    public function filter_mediaplugin_colors() {
        return $this->page->theme->filter_mediaplugin_colors;
    }

    /**
     * Returns the colours of the big MP3 player
     * @return string
     */
    public function resource_mp3player_colors() {
        return $this->page->theme->resource_mp3player_colors;
    }
}


/// RENDERERS

/**
 * A renderer that generates output for command-line scripts.
 *
 * The implementation of this renderer is probably incomplete.
 *
 * @copyright 2009 Tim Hunt
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @since     Moodle 2.0
 */
class core_renderer_cli extends core_renderer {
    /**
     * Returns the page header.
     * @return string HTML fragment
     */
    public function header() {
        output_starting_hook();
        return $this->page->heading . "\n";
    }

    /**
     * Returns a template fragment representing a Heading.
     * @param string $text The text of the heading
     * @param int $level The level of importance of the heading
     * @param string $classes A space-separated list of CSS classes
     * @param string $id An optional ID
     * @return string A template fragment for a heading
     */
    public function heading($text, $level, $classes = 'main', $id = null) {
        $text .= "\n";
        switch ($level) {
            case 1:
                return '=>' . $text;
            case 2:
                return '-->' . $text;
            default:
                return $text;
        }
    }

    /**
     * Returns a template fragment representing a fatal error.
     * @param string $message The message to output
     * @param string $moreinfourl URL where more info can be found about the error
     * @param string $link Link for the Continue button
     * @param array $backtrace The execution backtrace
     * @param string $debuginfo Debugging information
     * @return string A template fragment for a fatal error
     */
    public function fatal_error($message, $moreinfourl, $link, $backtrace, $debuginfo = null) {
        $output = "!!! $message !!!\n";

        if (debugging('', DEBUG_DEVELOPER)) {
            if (!empty($debuginfo)) {
                $this->notification($debuginfo, 'notifytiny');
            }
            if (!empty($backtrace)) {
                $this->notification('Stack trace: ' . format_backtrace($backtrace, true), 'notifytiny');
            }
        }
    }

    /**
     * Returns a template fragment representing a notification.
     * @param string $message The message to include
     * @param string $classes A space-separated list of CSS classes
     * @return string A template fragment for a notification
     */
    public function notification($message, $classes = 'notifyproblem') {
        $message = clean_text($message);
        if ($classes === 'notifysuccess') {
            return "++ $message ++\n";
        }
        return "!! $message !!\n";
    }
}


/**
 * A renderer that generates output for ajax scripts.
 *
 * This renderer prevents accidental sends back only json
 * encoded error messages, all other output is ignored.
 *
 * @copyright 2010 Petr Skoda
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @since     Moodle 2.0
 */
class core_renderer_ajax extends core_renderer {
    /**
     * Returns a template fragment representing a fatal error.
     * @param string $message The message to output
     * @param string $moreinfourl URL where more info can be found about the error
     * @param string $link Link for the Continue button
     * @param array $backtrace The execution backtrace
     * @param string $debuginfo Debugging information
     * @return string A template fragment for a fatal error
     */
    public function fatal_error($message, $moreinfourl, $link, $backtrace, $debuginfo = null) {
        $e = new stdClass();
        $e->error      = $message;
        $e->stacktrace = NULL;
        $e->debuginfo  = NULL;
        if (!empty($CFG->debug) and $CFG->debug >= DEBUG_DEVELOPER) {
            if (!empty($debuginfo)) {
                $e->debuginfo = $debuginfo;
            }
            if (!empty($backtrace)) {
                $e->stacktrace = format_backtrace($backtrace, true);
            }
        }
        return json_encode($e);
    }

    public function notification($message, $classes = 'notifyproblem') {
    }
    public function redirect_message($encodedurl, $message, $delay, $debugdisableredirect) {
    }
    public function header() {
    }
    public function footer() {
    }
    public function heading($text, $level, $classes = 'main', $id = null) {
    }
}
