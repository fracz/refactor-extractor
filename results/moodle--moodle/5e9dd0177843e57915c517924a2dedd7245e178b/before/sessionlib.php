<?php  //$Id$

/**
 * Factory method returning moodle_session object.
 * @return moodle_session
 */
function session_get_instance() {
    global $CFG;

    static $session = null;

    if (is_null($session)) {
        if (defined('SESSION_CUSTOM')) {
            // this is a hook for custom session handling, webservices, etc.
            if (defined('SESSION_CUSTOM_FILE')) {
                require_once($CFG->dirroot.SESSION_CUSTOM_FILE);
            }
            $session_class = SESSION_CUSTOM;
            $session = new $session_class();

        } else if (!isset($CFG->dbsessions) or $CFG->dbsessions) {
            // default recommended session type
            $session = new database_session();

        } else {
            // legacy limited file based storage - some features and auth plugins will not work, sorry
            $session = new legacy_file_session();
        }
    }

    return $session;
}

interface moodle_session {
    public function terminate();
}

/**
 * Class handling all session and cookies related stuff.
 */
abstract class session_stub implements moodle_session {
    public function __construct() {
        global $CFG;

        if (!defined('NO_MOODLE_COOKIES')) {
            if (CLI_SCRIPT) {
                // CLI scripts can not have session
                define('NO_MOODLE_COOKIES', true);
            } else {
                define('NO_MOODLE_COOKIES', false);
            }
        }

        if (NO_MOODLE_COOKIES) {
            // session not used at all
            $CFG->usesid = false;

            $_SESSION = array();
            $_SESSION['SESSION'] = new object();
            $_SESSION['USER']    = new object();

        } else {
            $this->prepare_cookies();
            $this->init_session_storage();

            if (!empty($CFG->usesid) && empty($_COOKIE['MoodleSession'.$CFG->sessioncookie])) {
                sid_start_ob();
            }

            session_name('MoodleSession'.$CFG->sessioncookie);
            session_set_cookie_params(0, $CFG->sessioncookiepath, $CFG->sessioncookiedomain, $CFG->cookiesecure, $CFG->cookiehttponly);
            @session_start();
            if (!isset($_SESSION['SESSION'])) {
                $_SESSION['SESSION'] = new object();
            }
            if (!isset($_SESSION['USER'])) {
                $_SESSION['USER'] = new object();
            }
        }

        $this->check_user_initialised();

        $this->check_security();
    }

    /**
     * Initialise $USER object, handles google access.
     *
     * @return void
     */
    protected function check_user_initialised() {
        if (isset($_SESSION['USER']->id)) {
            // already set up $USER
            return;
        }

        $user = null;

        if (!empty($CFG->opentogoogle) and !NO_MOODLE_COOKIES) {
            if (!empty($_SERVER['HTTP_USER_AGENT'])) {
                // allow web spiders in as guest users
                if (strpos($_SERVER['HTTP_USER_AGENT'], 'Googlebot') !== false ) {
                    $user = guest_user();
                } else if (strpos($_SERVER['HTTP_USER_AGENT'], 'google.com') !== false ) { // Google
                    $user = guest_user();
                } else if (strpos($_SERVER['HTTP_USER_AGENT'], 'Yahoo! Slurp') !== false ) {  // Yahoo
                    $user = guest_user();
                } else if (strpos($_SERVER['HTTP_USER_AGENT'], '[ZSEBOT]') !== false ) {  // Zoomspider
                    $user = guest_user();
                } else if (strpos($_SERVER['HTTP_USER_AGENT'], 'MSNBOT') !== false ) {  // MSN Search
                    $user = guest_user();
                }
            }
            if (!empty($CFG->guestloginbutton) and !$user and !empty($_SERVER['HTTP_REFERER'])) {
                // automaticaly log in users coming from search engine results
                if (strpos($_SERVER['HTTP_REFERER'], 'google') !== false ) {
                    $user = guest_user();
                } else if (strpos($_SERVER['HTTP_REFERER'], 'altavista') !== false ) {
                    $user = guest_user();
                }
            }
        }

        if (!$user) {
            $user = new object();
            $user->id = 0; // to enable proper function of $CFG->notloggedinroleid hack
            if (isset($CFG->mnet_localhost_id)) {
                $user->mnethostid = $CFG->mnet_localhost_id;
            } else {
                $user->mnethostid = 1;
            }
        }
        session_set_user($user);
    }

    /**
     * Does various session security checks
     * @global void
     */
    protected function check_security() {
        global $CFG;

        if (NO_MOODLE_COOKIES) {
            return;
        }

        if (!empty($_SESSION['USER']->id) and !empty($CFG->tracksessionip)) {
            /// Make sure current IP matches the one for this session
            $remoteaddr = getremoteaddr();

            if (empty($_SESSION['USER']->sessionip)) {
                $_SESSION['USER']->sessionip = $remoteaddr;
            }

            if ($_SESSION['USER']->sessionip != $remoteaddr) {
                // this is a security feature - terminate the session in case of any doubt
                $this->terminate();
                print_error('sessionipnomatch2', 'error');
            }
        }
    }

    /**
     * Terminates active moodle session
     */
    public function terminate() {
        global $CFG, $SESSION, $USER;

        $_SESSION = array();

        $SESSION  = new object();
        $USER     = new object();
        $USER->id = 0;
        if (isset($CFG->mnet_localhost_id)) {
            $USER->mnethostid = $CFG->mnet_localhost_id;
        }

        // Initialize variable to pass-by-reference to headers_sent(&$file, &$line)
        $file = null;
        $line = null;
        if (headers_sent($file, $line)) {
            error_log('Can not terminate session properly - headers were already sent in file: '.$file.' on line '.$line);
        }

        // now let's try to get a new session id and destroy the old one
        @session_regenerate_id(true);

        // close the session
        @session_write_close();
    }

    /**
     * Prepare cookies and varions system settings
     */
    protected function prepare_cookies() {
        global $CFG;

        if (!isset($CFG->cookiesecure) or (strpos($CFG->wwwroot, 'https://') !== 0 and empty($CFG->sslproxy))) {
            $CFG->cookiesecure = 0;
        }

        if (!isset($CFG->cookiehttponly)) {
            $CFG->cookiehttponly = 0;
        }

    /// Set sessioncookie and sessioncookiepath variable if it isn't already
        if (!isset($CFG->sessioncookie)) {
            $CFG->sessioncookie = '';
        }
        if (!isset($CFG->sessioncookiedomain)) {
            $CFG->sessioncookiedomain = '';
        }
        if (!isset($CFG->sessioncookiepath)) {
            $CFG->sessioncookiepath = '/';
        }

        //discard session ID from POST, GET and globals to tighten security,
        //this session fixation prevention can not be used in cookieless mode
        if (empty($CFG->usesid)) {
            unset(${'MoodleSession'.$CFG->sessioncookie});
            unset($_GET['MoodleSession'.$CFG->sessioncookie]);
            unset($_POST['MoodleSession'.$CFG->sessioncookie]);
            unset($_REQUEST['MoodleSession'.$CFG->sessioncookie]);
        }
        //compatibility hack for Moodle Cron, cookies not deleted, but set to "deleted" - should not be needed with NO_MOODLE_COOKIES in cron.php now
        if (!empty($_COOKIE['MoodleSession'.$CFG->sessioncookie]) && $_COOKIE['MoodleSession'.$CFG->sessioncookie] == "deleted") {
            unset($_COOKIE['MoodleSession'.$CFG->sessioncookie]);
        }
    }

    /**
     * Inits session storage.
     */
    protected abstract function init_session_storage();

}

/**
 * Legacy moodle sessions stored in files, not recommended any more.
 */
class legacy_file_session extends session_stub {
    protected function init_session_storage() {
        global $CFG;

        ini_set('session.save_handler', 'files');

        // Some distros disable GC by setting probability to 0
        // overriding the PHP default of 1
        // (gc_probability is divided by gc_divisor, which defaults to 1000)
        if (ini_get('session.gc_probability') == 0) {
            ini_set('session.gc_probability', 1);
        }

        if (empty($CFG->sessiontimeout)) {
            $CFG->sessiontimeout = 7200;
        }
        ini_set('session.gc_maxlifetime', $CFG->sessiontimeout);

        if (!file_exists($CFG->dataroot .'/sessions')) {
            make_upload_directory('sessions');
        }
        if (!is_writable($CFG->dataroot .'/sessions/')) {
            print_error('sessionnotwritable', 'error');
        }
        ini_set('session.save_path', $CFG->dataroot .'/sessions');
    }
}

/**
 * Recommended moodle session storage.
 */
class database_session extends session_stub {
    protected $record   = null;
    protected $database = null;

    protected function init_session_storage() {
        global $CFG;

        if (ini_get('session.gc_probability') == 0) {
            ini_set('session.gc_probability', 1);
        }

        if (!empty($CFG->sessiontimeout)) {
            ini_set('session.gc_maxlifetime', $CFG->sessiontimeout);
        }

        $result = session_set_save_handler(array($this, 'handler_open'),
                                           array($this, 'handler_close'),
                                           array($this, 'handler_read'),
                                           array($this, 'handler_write'),
                                           array($this, 'handler_destroy'),
                                           array($this, 'handler_gc'));
        if (!$result) {
            print_error('dbsessionhandlerproblem', 'error');
        }
    }

    public function handler_open($save_path, $session_name) {
        global $DB;

        $this->database = $DB;
        $this->database->used_for_db_sessions();

        return true;
    }

    public function handler_close() {
        $this->record = null;
        return true;
    }

    public function handler_read($sid) {
        global $CFG;

        if ($this->record and $this->record->sid != $sid) {
            error_log('Weird error reading session - mismatched sid');
            return '';
        }

        try {
            if ($record = $this->database->get_record('sessions', array('sid'=>$sid))) {
                $this->database->get_session_lock($record->id);

            } else {
                $record = new object();
                $record->state        = 0;
                $record->sid          = $sid;
                $record->sessdata     = null;
                $record->sessdatahash = null;
                $record->userid       = 0;
                $record->timecreated  = $record->timemodified = time();
                $record->firstip      = $record->lastip = getremoteaddr();
                $record->id = $this->database->insert_record_raw('sessions', $record);

                $this->database->get_session_lock($record->id);
            }
        } catch (dml_exception $ex) {
            if (!empty($CFG->rolesactive)) {
                error_log('Can not read or insert database sessions');
            }
            return '';
        }

        // verify timeout
        if ($record->timemodified + $CFG->sessiontimeout < time()) {
            // TODO: implement auth plugin timeout hook (see gc)
            $record->state        = 0;
            $record->sessdata     = null;
            $record->sessdatahash = null;
            $record->userid       = 0;
            $record->timecreated  = $record->timemodified = time();
            $record->firstip      = $record->lastip = getremoteaddr();
            try {
                $this->database->update_record('sessions', $record);
            } catch (dml_exception $ex) {
                error_log('Can not time out database session');
                return '';
            }
        }

        if ($record->sessdatahash !== null) {
            if (md5($record->sessdata) !== $record->sessdatahash) {
                // probably this is caused by misconfigured mysql - the allowed request size might be too small
                try {
                    $this->database->delete_records('sessions', array('sid'=>$record->sid));
                } catch (dml_exception $ignored) {
                }
                print_error('dbsessionbroken', 'error');
            }

            $data = base64_decode($record->sessdata);
        } else {
            $data = '';
        }

        unset($record->sessdata); // conserve memory
        $this->record = $record;

        return $data;
    }

    public function handler_write($sid, $session_data) {
        global $USER;

        if (!$this->record) {
            error_log('Weird error writing session');
            return true;
        }

        $this->database->release_session_lock($this->record->id);

        $this->record->sid          = $sid;                         // it might be regenerated
        $this->record->sessdata     = base64_encode($session_data); // there might be some binary mess :-(
        $this->record->sessdatahash = md5($this->record->sessdata);
        $this->record->userid       = empty($USER->realuser) ? $USER->id : $USER->realuser;
        $this->record->timemodified = time();
        $this->record->lastip       = getremoteaddr();

        // TODO: verify session changed before doing update

        try {
            $this->database->update_record_raw('sessions', $this->record);
        } catch (dml_exception $ex) {
            error_log('Can not write session to database.');
        }

        return true;
    }

    public function handler_destroy($sid) {
        if (!$this->record or $this->record->sid != $sid) {
            error_log('Weird error destroying session - mismatched sid');
            return true;
        }

        $this->database->release_session_lock($this->record->id);

        try {
            $this->database->delete_records('sessions', array('sid'=>$this->record->sid));
        } catch (dml_exception $ex) {
            error_log('Can not destroy database session.');
        }

        return true;
    }

    public function handler_gc($ignored_maxlifetime) {
        global $CFG;
        $maxlifetime = $CFG->sessiontimeout;

        $select = "timemodified + :maxlifetime < :now";
        $params = array('now'=>time(), 'maxlifetime'=>$maxlifetime);

        // TODO: add auth plugin hook that would allow extending of max lifetime

        try {
            $this->database->delete_records_select('sessions', $select, $params);
        } catch (dml_exception $ex) {
            error_log('Can not garbage collect database sessions.');
        }

        return true;
    }

}

/**
 * Makes sure that $USER->sesskey exists, if $USER itself exists. It sets a new sesskey
 * if one does not already exist, but does not overwrite existing sesskeys. Returns the
 * sesskey string if $USER exists, or boolean false if not.
 *
 * @uses $USER
 * @return string
 */
function sesskey() {
    global $USER;

    if (empty($USER->sesskey)) {
        $USER->sesskey = random_string(10);
    }

    return $USER->sesskey;
}


/**
 * For security purposes, this function will check that the currently
 * given sesskey (passed as a parameter to the script or this function)
 * matches that of the current user.
 *
 * @param string $sesskey optionally provided sesskey
 * @return bool
 */
function confirm_sesskey($sesskey=NULL) {
    global $USER;

    if (!empty($USER->ignoresesskey)) {
        return true;
    }

    if (empty($sesskey)) {
        $sesskey = required_param('sesskey', PARAM_RAW);  // Check script parameters
    }

    return (sesskey() === $sesskey);
}

/**
 * Sets a moodle cookie with a weakly encrypted string
 *
 * @uses $CFG
 * @uses DAYSECS
 * @uses HOURSECS
 * @param string $thing The string to encrypt and place in a cookie
 */
function set_moodle_cookie($thing) {
    global $CFG;

    if (NO_MOODLE_COOKIES) {
        return;
    }

    if ($thing == 'guest') {  // Ignore guest account
        return;
    }

    $cookiename = 'MOODLEID_'.$CFG->sessioncookie;

    $days = 60;
    $seconds = DAYSECS*$days;

    // no need to set secure or http cookie only here - it is not secret
    setcookie($cookiename, '', time() - HOURSECS, $CFG->sessioncookiepath, $CFG->sessioncookiedomain);
    setcookie($cookiename, rc4encrypt($thing), time()+$seconds, $CFG->sessioncookiepath, $CFG->sessioncookiedomain);
}

/**
 * Gets a moodle cookie with a weakly encrypted string
 *
 * @uses $CFG
 * @return string
 */
function get_moodle_cookie() {
    global $CFG;

    if (NO_MOODLE_COOKIES) {
        return '';
    }

    $cookiename = 'MOODLEID_'.$CFG->sessioncookie;

    if (empty($_COOKIE[$cookiename])) {
        return '';
    } else {
        $thing = rc4decrypt($_COOKIE[$cookiename]);
        return ($thing == 'guest') ? '': $thing;  // Ignore guest account
    }
}

/**
 * Setup $USER object - called during login, loginas, etc.
 * Preloads capabilities and checks enrolment plugins
 *
 * @param object $user full user record object
 * @return void
 */
function session_set_user($user) {
    $_SESSION['USER'] = $user;
    check_enrolment_plugins($_SESSION['USER']);
    load_all_capabilities();
    sesskey(); // init session key
}

/**
 * Is current $USER logged-in-as somebody else?
 * @return bool
 */
function session_is_loggedinas() {
    return !empty($_SESSION['USER']->realuser);
}

/**
 * Returns the $USER object ignoring current login-as session
 * @return object user object
 */
function session_get_realuser() {
    if (session_is_loggedinas()) {
        return $_SESSION['REALUSER'];
    } else {
        return $_SESSION['USER'];
    }
}

/**
 * Login as another user - no security checks here.
 * @param int $userid
 * @param object $context
 * @return void
 */
function session_loginas($userid, $context) {
    if (session_is_loggedinas()) {
        return;
    }

    // switch to fresh new $SESSION
    $_SESSION['REALSESSION'] = $_SESSION['SESSION'];
    $_SESSION['SESSION']     = new object();

    /// Create the new $USER object with all details and reload needed capabilitites
    $_SESSION['REALUSER'] = $_SESSION['USER'];
    $user = get_complete_user_data('id', $userid);
    $user->realuser       = $_SESSION['REALUSER']->id;
    $user->loginascontext = $context;
    session_set_user($user);
}

/**
 * Terminate login-as session
 * @return void
 */
function session_unloginas() {
    if (!session_is_loggedinas()) {
        return;
    }

    $_SESSION['SESSION'] = $_SESSION['REALSESSION'];
    unset($_SESSION['REALSESSION']);

    $_SESSION['USER'] = $_SESSION['REALUSER'];
    unset($_SESSION['REALUSER']);
}

/**
 * Sets up current user and course enviroment (lang, etc.) in cron.
 * Do not use outside of cron script!
 *
 * @param object $user full user object, null means default cron user (admin)
 * @param $course full course record, null means $SITE
 * @return void
 */
function cron_setup_user($user=null, $course=null) {
    global $CFG, $SITE;

    static $cronuser    = null;
    static $cronsession = null;

    if (empty($cronuser)) {
        /// ignore admins timezone, language and locale - use site deafult instead!
        $cronuser = get_admin();
        $cronuser->timezone = $CFG->timezone;
        $cronuser->lang = '';
        $cronuser->theme = '';

        $cronsession = array();
    }

    if (!$user) {
        // cached default cron user (==modified admin for now)
        session_set_user($cronuser);
        $_SESSION['SESSION'] = $cronsession;

    } else {
        // emulate real user session - needed for caps in cron
        if ($_SESSION['USER']->id != $user->id) {
            session_set_user($user);
            $_SESSION['SESSION'] = array();
        }
    }

    if ($course) {
        course_setup($course);
    } else {
        course_setup($SITE);
    }

    // TODO: it should be possible to improve perf by caching some limited number of users here ;-)

}

/**
* Enable cookieless sessions by including $CFG->usesid=true;
* in config.php.
* Based on code from php manual by Richard at postamble.co.uk
* Attempts to use cookies if cookies not present then uses session ids attached to all urls and forms to pass session id from page to page.
* If site is open to google, google is given guest access as usual and there are no sessions. No session ids will be attached to urls for googlebot.
* This doesn't require trans_sid to be turned on but this is recommended for better performance
* you should put :
* session.use_trans_sid = 1
* in your php.ini file and make sure that you don't have a line like this in your php.ini
* session.use_only_cookies = 1
* @author Richard at postamble.co.uk and Jamie Pratt
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
*/
/**
* You won't call this function directly. This function is used to process
* text buffered by php in an output buffer. All output is run through this function
* before it is ouput.
* @param string $buffer is the output sent from php
* @return string the output sent to the browser
*/
function sid_ob_rewrite($buffer){
    $replacements = array(
        '/(<\s*(a|link|script|frame|area)\s[^>]*(href|src)\s*=\s*")([^"]*)(")/i',
        '/(<\s*(a|link|script|frame|area)\s[^>]*(href|src)\s*=\s*\')([^\']*)(\')/i');

    $buffer = preg_replace_callback($replacements, 'sid_rewrite_link_tag', $buffer);
    $buffer = preg_replace('/<form\s[^>]*>/i',
        '\0<input type="hidden" name="' . session_name() . '" value="' . session_id() . '"/>', $buffer);

      return $buffer;
}
/**
* You won't call this function directly. This function is used to process
* text buffered by php in an output buffer. All output is run through this function
* before it is ouput.
* This function only processes absolute urls, it is used when we decide that
* php is processing other urls itself but needs some help with internal absolute urls still.
* @param string $buffer is the output sent from php
* @return string the output sent to the browser
*/
function sid_ob_rewrite_absolute($buffer){
    $replacements = array(
        '/(<\s*(a|link|script|frame|area)\s[^>]*(href|src)\s*=\s*")((?:http|https)[^"]*)(")/i',
        '/(<\s*(a|link|script|frame|area)\s[^>]*(href|src)\s*=\s*\')((?:http|https)[^\']*)(\')/i');

    $buffer = preg_replace_callback($replacements, 'sid_rewrite_link_tag', $buffer);
    $buffer = preg_replace('/<form\s[^>]*>/i',
        '\0<input type="hidden" name="' . session_name() . '" value="' . session_id() . '"/>', $buffer);
    return $buffer;
}

/**
* A function to process link, a and script tags found
* by preg_replace_callback in {@link sid_ob_rewrite($buffer)}.
*/
function sid_rewrite_link_tag($matches){
    $url = $matches[4];
    $url = sid_process_url($url);
    return $matches[1].$url.$matches[5];
}

/**
* You can call this function directly. This function is used to process
* urls to add a moodle session id to the url for internal links.
* @param string $url is a url
* @return string the processed url
*/
function sid_process_url($url) {
    global $CFG;

    if ((preg_match('/^(http|https):/i', $url)) // absolute url
        &&  ((stripos($url, $CFG->wwwroot)!==0) && stripos($url, $CFG->httpswwwroot)!==0)) { // and not local one
        return $url; //don't attach sessid to non local urls
    }
    if ($url[0]=='#' || (stripos($url, 'javascript:')===0)) {
        return $url; //don't attach sessid to anchors
    }
    if (strpos($url, session_name())!==FALSE) {
        return $url; //don't attach sessid to url that already has one sessid
    }
    if (strpos($url, "?")===FALSE) {
        $append = "?".strip_tags(session_name() . '=' . session_id());
    }    else {
        $append = "&amp;".strip_tags(session_name() . '=' . session_id());
    }
    //put sessid before any anchor
    $p = strpos($url, "#");
    if ($p!==FALSE){
        $anch = substr($url, $p);
        $url = substr($url, 0, $p).$append.$anch ;
    } else  {
        $url .= $append ;
    }
    return $url;
}

/**
* Call this function before there has been any output to the browser to
* buffer output and add session ids to all internal links.
*/
function sid_start_ob(){
    global $CFG;
    //don't attach sess id for bots

    if (!empty($_SERVER['HTTP_USER_AGENT'])) {
        if (!empty($CFG->opentogoogle)) {
            if (strpos($_SERVER['HTTP_USER_AGENT'], 'Googlebot') !== false) {
                @ini_set('session.use_trans_sid', '0'); // try and turn off trans_sid
                $CFG->usesid=false;
                return;
            }
            if (strpos($_SERVER['HTTP_USER_AGENT'], 'google.com') !== false) {
                @ini_set('session.use_trans_sid', '0'); // try and turn off trans_sid
                $CFG->usesid=false;
                return;
            }
        }
        if (strpos($_SERVER['HTTP_USER_AGENT'], 'W3C_Validator') !== false) {
            @ini_set('session.use_trans_sid', '0'); // try and turn off trans_sid
            $CFG->usesid=false;
            return;
        }
    }

    @ini_set('session.use_trans_sid', '1'); // try and turn on trans_sid

    if (ini_get('session.use_trans_sid') != 0) {
        // use trans sid as its available
        ini_set('url_rewriter.tags', 'a=href,area=href,script=src,link=href,frame=src,form=fakeentry');
        ob_start('sid_ob_rewrite_absolute');
    } else {
        //rewrite all links ourselves
        ob_start('sid_ob_rewrite');
    }
}
