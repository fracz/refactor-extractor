<?php
/**
 * Moodle - Modular Object-Oriented Dynamic Learning Environment
 *          http://moodle.org
 * Copyright (C) 1999 onwards Martin Dougiamas  http://dougiamas.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @package    moodle
 * @subpackage portfolio
 * @author     Penny Leach <penny@catalyst.net.nz>
 * @license    http://www.gnu.org/copyleft/gpl.html GNU GPL
 * @copyright  (C) 1999 onwards Martin Dougiamas  http://dougiamas.com
 *
 * This file contains all global functions to do with manipulating portfolios
 * everything else that is logically namespaced by class is in its own file
 * in lib/portfolio/ directory.
 */

// require all the sublibraries first.
require_once($CFG->libdir . '/portfolio/constants.php');   // all the constants for time, export format etc.
require_once($CFG->libdir . '/portfolio/exceptions.php');  // exception classes used by portfolio code
require_once($CFG->libdir . '/portfolio/formats.php');     // the export format hierarchy
require_once($CFG->libdir . '/portfolio/forms.php');       // the form classes that subclass moodleform
require_once($CFG->libdir . '/portfolio/exporter.php');    // the exporter class
require_once($CFG->libdir . '/portfolio/plugin.php');      // the base classes for plugins
require_once($CFG->libdir . '/portfolio/caller.php');      // the base classes for calling code

/**
* use this to add a portfolio button or icon or form to a page
*
* These class methods do not check permissions. the caller must check permissions first.
* Later, during the export process, the caller class is instantiated and the check_permissions method is called
*
* This class can be used like this:
* $button = new portfolio_add_button();
* $button->set_callback_options('name_of_caller_class', array('id' => 6), '/your/mod/lib.php');
* $button->render(PORTFOLIO_ADD_FULL_FORM, get_string('addeverythingtoportfolio', 'yourmodule'));
*
* or like this:
* $button = new portfolio_add_button(array('callbackclass' => 'name_of_caller_class', 'callbackargs' => array('id' => 6), 'callbackfile' => '/your/mod/lib.php'));
* $somehtml .= $button->to_html(PORTFOLIO_ADD_TEXT_LINK);
*
* See http://docs.moodle.org/en/Development:Adding_a_Portfolio_Button_to_a_page for more information
*/
class portfolio_add_button {

    private $callbackclass;
    private $callbackargs;
    private $callbackfile;
    private $formats;
    private $instances;

    /**
    * constructor. either pass the options here or set them using the helper methods.
    * generally the code will be clearer if you use the helper methods.
    *
    * @param array $options keyed array of options:
    *                       key 'callbackclass': name of the caller class (eg forum_portfolio_caller')
    *                       key 'callbackargs': the array of callback arguments your caller class wants passed to it in the constructor
    *                       key 'callbackfile': the file containing the class definition of your caller class.
    *                       See set_callback_options for more information on these three.
    *                       key 'formats': an array of PORTFOLIO_FORMATS this caller will support
    *                       See set_formats for more information on this.
    */
    public function __construct($options=null) {
        global $SESSION;
        if (isset($SESSION->portfolioexport)) {
            $a = new StdClass;
            $a->cancel = $CFG->wwwroot . '/portfolio/add.php?cancel=1';
            $a->finish = $CFG->wwwroot . '/portfolio/add.php?id=' . $SESSION->portfolioexport;
            throw new portfolio_button_exception('alreadyexporting', 'portfolio', null, $a);
        }
        if (empty($options)) {
            return true;
        }
        foreach ((array)$options as $key => $value) {
            if (!in_array($key, $constructoroptions)) {
                throw new portfolio_button_exception('invalidbuttonproperty', 'portfolio', $key);
            }
            $this->{$key} = $value;
        }
        $this->instances = portfolio_instances();
    }

    /*
    * @param string $class   name of the class containing the callback functions
    *                        activity modules should ALWAYS use their name_portfolio_caller
    *                        other locations must use something unique
    * @param mixed $argarray this can be an array or hash of arguments to pass
    *                        back to the callback functions (passed by reference)
    *                        these MUST be primatives to be added as hidden form fields.
    *                        and the values get cleaned to PARAM_ALPHAEXT or PARAM_NUMBER or PARAM_PATH
    * @param string $file    this can be autodetected if it's in the same file as your caller,
    *                        but often, the caller is a script.php and the class in a lib.php
    *                        so you can pass it here if necessary.
    *                        this path should be relative (ie, not include) dirroot, eg '/mod/forum/lib.php'
    */
    public function set_callback_options($class, array $argarray, $file=null) {
        global $CFG;
        if (empty($file)) {
            $backtrace = debug_backtrace();
            if (!array_key_exists(0, $backtrace) || !array_key_exists('file', $backtrace[0]) || !is_readable($backtrace[0]['file'])) {
                throw new portfolio_button_exception('nocallbackfile', 'portfolio');
            }

            $file = substr($backtrace[0]['file'], strlen($CFG->dirroot));
        } else if (!is_readable($CFG->dirroot . $file)) {
            throw new portfolio_button_exception('nocallbackfile', 'portfolio', $file);
        }
        $this->callbackfile = $file;
        require_once($CFG->dirroot . $file);
        if (!class_exists($class)) {
            throw new portfolio_button_exception('nocallbackclass', 'portfolio', $class);
        }
        $this->callbackclass = $class;
        $this->callbackargs = $argarray;
    }

    /*
    * @param array $formats if the calling code knows better than the static method on the calling class (supported_formats)
    *                       eg, if it's going to be a single file, or if you know it's HTML, you can pass it here instead
    *                       this is almost always the case so you should always use this.
    *                       {@see portfolio_format_from_file} for how to get the appropriate formats to pass here for uploaded files.
    */
    public function set_formats($formats=null) {
        if (is_string($formats)) {
            $formats = array($formats);
        }
        if (empty($formats)) {
            if (empty($this->callbackclass)) {
                throw new portfolio_button_exception('noformatsorclass', 'portfolio');
            }
            $formats = call_user_func(array($this->callbackclass, 'supported_formats'));
        }
        $this->formats = $formats;
    }

    /*
    * echo the form/button/icon/text link to the page
    *
    * @param int $format format to display the button or form or icon or link.
    *                    See constants PORTFOLIO_ADD_XXX for more info.
    *                    optional, defaults to PORTFOLI_ADD_FULL_FORM
    * @param str $addstr string to use for the button or icon alt text or link text.
    *                    this is whole string, not key.  optional, defaults to 'Add to portfolio';
    */
    public function render($format=null, $addstr=null) {
        echo $this->tohtml($format, $addstr);
    }

    /*
    * returns the form/button/icon/text link as html
    *
    * @param int $format format to display the button or form or icon or link.
    *                    See constants PORTFOLIO_ADD_XXX for more info.
    *                    optional, defaults to PORTFOLI_ADD_FULL_FORM
    * @param str $addstr string to use for the button or icon alt text or link text.
    *                    this is whole string, not key.  optional, defaults to 'Add to portfolio';
    */
    public function to_html($format=null, $addstr=null) {
        global $CFG, $COURSE;
        if (!$this->is_renderable()) {
            return;
        }
        if (empty($this->callbackclass) || $this->callbackfile) {
            throw new portfolio_button_exception('mustcallsetcallbackoptions', 'portfolio');
        }
        if (empty($this->formats)) {
            // use the caller defaults
            $this->set_formats();
        }
        $formoutput = '<form method="post" action="' . $CFG->wwwroot . '/portfolio/add.php" id="portfolio-add-button">' . "\n";
        $linkoutput = '<a href="' . $CFG->wwwroot . '/portfolio/add.php?';
        foreach ($this->callbackargs as $key => $value) {
            if (!empty($value) && !is_string($value) && !is_numeric($value)) {
                $a->key = $key;
                $a->value = print_r($value, true);
                debugging(get_string('nonprimative', 'portfolio', $a));
                return;
            }
            $linkoutput .= 'ca_' . $key . '=' . $value . '&amp;';
            $formoutput .= "\n" . '<input type="hidden" name="ca_' . $key . '" value="' . $value . '" />';
        }
        $formoutput .= "\n" . '<input type="hidden" name="callbackfile" value="' . $this->callbackfile . '" />';
        $formoutput .= "\n" . '<input type="hidden" name="callbackclass" value="' . $this->callbackclass . '" />';
        $formoutput .= "\n" . '<input type="hidden" name="course" value="' . (!empty($COURSE) ? $COURSE->id : 0) . '" />';
        $linkoutput .= 'callbackfile=' . $this->callbackfile . '&amp;callbackclass='
            . $this->callbackclass . '&amp;course=' . (!empty($COURSE) ? $COURSE->id : 0);
        $selectoutput = '';
        if (count($this->instances) == 1) {
            $instance = array_shift($this->instances);
            $formats = portfolio_supported_formats_intersect($this->formats, $instance->supported_formats());
            if (count($formats) == 0) {
                // bail. no common formats.
                debugging(get_string('nocommonformats', 'portfolio', $this->callbackclass));
                return;
            }
            if ($error = portfolio_instance_sanity_check($instance)) {
                // bail, plugin is misconfigured
                debugging(get_string('instancemisconfigured', 'portfolio', get_string($error[$instance->get('id')], 'portfolio_' . $instance->get('plugin'))));
                return;
            }
            $formoutput .= "\n" . '<input type="hidden" name="instance" value="' . $instance->get('id') . '" />';
            $linkoutput .= '&amp;instance=' . $instance->get('id');
        }
        else {
            $selectoutput = portfolio_instance_select($this->instances, $this->formats, $this->callbackclass, 'instance', true);
        }

        if (empty($addstr)) {
            $addstr = get_string('addtoportfolio', 'portfolio');
        }
        if (empty($format)) {
            $format = PORTFOLIO_ADD_FULL_FORM;
        }
        switch ($format) {
            case PORTFOLIO_ADD_FULL_FORM:
                $formoutput .= $selectoutput;
                $formoutput .= "\n" . '<input type="submit" value="' . $addstr .'" />';
                $formoutput .= "\n" . '</form>';
            break;
            case PORTFOLIO_ADD_ICON_FORM:
                $formoutput .= $selectoutput;
                $formoutput .= "\n" . '<input type="image" src="' . $CFG->pixpath . '/t/portfolio.gif" alt=' . $addstr .'" />';
                $formoutput .= "\n" . '</form>';
            break;
            case PORTFOLIO_ADD_ICON_LINK:
                $linkoutput .= '"><img src="' . $CFG->pixpath . '/t/portfolio.gif" alt=' . $addstr .'" /></a>';
            break;
            case PORTFOLIO_ADD_TEXT_LINK:
                $linkoutput .= '">' . $addstr .'</a>';
            break;
            default:
                debugging(get_string('invalidaddformat', 'portfolio', $format));
        }
        $output = (in_array($format, array(PORTFOLIO_ADD_FULL_FORM, PORTFOLIO_ADD_ICON_FORM)) ? $formoutput : $linkoutput);
        return $output;
    }

    /**
    * does some internal checks
    * these are not errors, just situations
    * where it's not appropriate to add the button
    */
    private function is_renderable() {
        global $CFG;
        if (empty($CFG->enableportfolios)) {
            return false;
        }
        if (defined('PORTFOLIO_INTERNAL')) {
            // something somewhere has detected a risk of this being called during inside the preparation
            // eg forum_print_attachments
            return false;
        }
        if (!$this->instances) {
            return false;
        }
        return true;
    }
}


function portfolio_add_button($callbackclass, $callbackargs, $callbackfile=null, $format=PORTFOLIO_ADD_FULL_FORM, $addstr=null, $return=false, $callersupports=null) {
    $button = new portfolio_add_button();
    $button->set_callback_options($callbackclass, $callbackargs, $callbackfile);
    $button->set_formats($callersupports);
    if ($return) {
        return $button->to_html($format, $addstr);
    }
    $button->render();
}

/**
* returns a drop menu with a list of available instances.
*
* @param array    $instances     array of portfolio plugin instance objects - the instances to put in the menu
* @param array    $callerformats array of PORTFOLIO_FORMAT_XXX constants - the formats the caller supports (this is used to filter plugins)
* @param array    $callbackclass the callback class name - used for debugging only for when there are no common formats
* @param string   $selectname    the name of the select element. Optional, defaults to instance.
* @param boolean  $return        whether to print or return the output. Optional, defaults to print.
* @param booealn  $returnarray   if returning, whether to return the HTML or the array of options. Optional, defaults to HTML.
*
* @return string the html, from <select> to </select> inclusive.
*/
function portfolio_instance_select($instances, $callerformats, $callbackclass, $selectname='instance', $return=false, $returnarray=false) {
    global $CFG;

    if (empty($CFG->enableportfolios)) {
        return;
    }

    $insane = portfolio_instance_sanity_check();
    $count = 0;
    $selectoutput = "\n" . '<select name="' . $selectname . '">' . "\n";
    foreach ($instances as $instance) {
        $formats = portfolio_supported_formats_intersect($callerformats, $instance->supported_formats());
        if (count($formats) == 0) {
            // bail. no common formats.
            continue;
        }
        if (array_key_exists($instance->get('id'), $insane)) {
            // bail, plugin is misconfigured
            debugging(get_string('instancemisconfigured', 'portfolio', get_string($insane[$instance->get('id')], 'portfolio_' . $instance->get('plugin'))));
            continue;
        }
        $count++;
        $selectoutput .= "\n" . '<option value="' . $instance->get('id') . '">' . $instance->get('name') . '</option>' . "\n";
        $options[$instance->get('id')] = $instance->get('name');
    }
    if (empty($count)) {
        // bail. no common formats.
        debugging(get_string('nocommonformats', 'portfolio', $callbackclass));
        return;
    }
    $selectoutput .= "\n" . "</select>\n";
    if (!empty($returnarray)) {
        return $options;
    }
    if (!empty($return)) {
        return $selectoutput;
    }
    echo $selectoutput;
}

/**
* return all portfolio instances
*
* @todo check capabilities here - see MDL-15768
*
* @param boolean visibleonly Don't include hidden instances. Defaults to true and will be overridden to true if the next parameter is true
* @param boolean useronly    Check the visibility preferences and permissions of the logged in user. Defaults to true.
*
* @return array of portfolio instances (full objects, not just database records)
*/
function portfolio_instances($visibleonly=true, $useronly=true) {

    global $DB, $USER;

    $values = array();
    $sql = 'SELECT * FROM {portfolio_instance}';

    if ($visibleonly || $useronly) {
        $values[] = 1;
        $sql .= ' WHERE visible = ?';
    }
    if ($useronly) {
        $sql .= ' AND id NOT IN (
                SELECT instance FROM {portfolio_instance_user}
                WHERE userid = ? AND name = ? AND value = ?
            )';
        $values = array_merge($values, array($USER->id, 'visible', 0));
    }
    $sql .= ' ORDER BY name';

    $instances = array();
    foreach ($DB->get_records_sql($sql, $values) as $instance) {
        $instances[$instance->id] = portfolio_instance($instance->id, $instance);
    }
    return $instances;
}

/**
* Supported formats currently in use.
*
* Canonical place for a list of all formats
* that portfolio plugins and callers
* can use for exporting content
*
* @return keyed array of all the available export formats (constant => classname)
*/
function portfolio_supported_formats() {
    return array(
        PORTFOLIO_FORMAT_FILE  => 'portfolio_format_file',
        PORTFOLIO_FORMAT_IMAGE => 'portfolio_format_image',
        PORTFOLIO_FORMAT_HTML  => 'portfolio_format_html',
        PORTFOLIO_FORMAT_TEXT  => 'portfolio_format_text',
        PORTFOLIO_FORMAT_VIDEO => 'portfolio_format_video',
        /*PORTFOLIO_FORMAT_MBKP, */ // later
        /*PORTFOLIO_FORMAT_PIOP, */ // also later
    );
}

/**
* Deduce export format from file mimetype
*
* This function returns the revelant portfolio export format
* which is used to determine which portfolio plugins can be used
* for exporting this content
* according to the mime type of the given file
* this only works when exporting exactly <b>one</b> file
*
* @param stored_file $file file to check mime type for
*
* @return string the format constant (see PORTFOLIO_FORMAT_XXX constants)
*/
function portfolio_format_from_file(stored_file $file) {
    static $alreadymatched;
    if (empty($alreadymatched)) {
        $alreadymatched = array();
    }
    if (!($file instanceof stored_file)) {
        throw new portfolio_exception('invalidfileargument', 'portfolio');
    }
    $mimetype = $file->get_mimetype();
    if (array_key_exists($mimetype, $alreadymatched)) {
        return $alreadymatched[$mimetype];
    }
    $allformats = portfolio_supported_formats();
    foreach ($allformats as $format => $classname) {
        $supportedmimetypes = call_user_func(array($classname, 'mimetypes'));
        if (!is_array($supportedmimetypes)) {
            debugging("one of the portfolio format classes, $classname, said it supported something funny for mimetypes, should have been array...");
            debugging(print_r($supportedmimetypes, true));
            continue;
        }
        if (in_array($mimetype, $supportedmimetypes)) {
            $alreadymatched[$mimetype] = $format;
            return $format;
        }
    }
    return PORTFOLIO_FORMAT_FILE; // base case for files...
}

/**
* Intersection of plugin formats and caller formats
*
* Walks both the caller formats and portfolio plugin formats
* and looks for matches (walking the hierarchy as well)
* and returns the intersection
*
* @param array $callerformats formats the caller supports
* @param array $pluginformats formats the portfolio plugin supports
*/
function portfolio_supported_formats_intersect($callerformats, $pluginformats) {
    $allformats = portfolio_supported_formats();
    $intersection = array();
    foreach ($callerformats as $cf) {
        if (!array_key_exists($cf, $allformats)) {
            debugging(get_string('invalidformat', 'portfolio', $cf));
            continue;
        }
        $cfobj = new $allformats[$cf]();
        foreach ($pluginformats as $p => $pf) {
            if (!array_key_exists($pf, $allformats)) {
                debugging(get_string('invalidformat', 'portfolio', $pf));
                unset($pluginformats[$p]); // to avoid the same warning over and over
                continue;
            }
            if ($cfobj instanceof $allformats[$pf]) {
                $intersection[] = $cf;
            }
        }
    }
    return $intersection;
}

/**
* helper function to return an instance of a plugin (with config loaded)
*
* @param int   $instance id of instance
* @param array $record   database row that corresponds to this instance
*                        this is passed to avoid unnecessary lookups
*                        Optional, and the record will be retrieved if null.
*
* @return subclass of portfolio_plugin_base
*/
function portfolio_instance($instanceid, $record=null) {
    global $DB, $CFG;

    if ($record) {
        $instance  = $record;
    } else {
        if (!$instance = $DB->get_record('portfolio_instance', array('id' => $instanceid))) {
            throw new portfolio_exception('invalidinstance', 'portfolio');
        }
    }
    require_once($CFG->dirroot . '/portfolio/type/'. $instance->plugin . '/lib.php');
    $classname = 'portfolio_plugin_' . $instance->plugin;
    return new $classname($instanceid, $instance);
}

/**
* Helper function to call a static function on a portfolio plugin class
*
* This will figure out the classname and require the right file and call the function.
* you can send a variable number of arguments to this function after the first two
* and they will be passed on to the function you wish to call.
*
* @param string $plugin   name of plugin
* @param string $function function to call
*/
function portfolio_static_function($plugin, $function) {
    global $CFG;

    $pname = null;
    if (is_object($plugin) || is_array($plugin)) {
        $plugin = (object)$plugin;
        $pname = $plugin->name;
    } else {
        $pname = $plugin;
    }

    $args = func_get_args();
    if (count($args) <= 2) {
        $args = array();
    }
    else {
        array_shift($args);
        array_shift($args);
    }

    require_once($CFG->dirroot . '/portfolio/type/' . $plugin .  '/lib.php');
    return call_user_func_array(array('portfolio_plugin_' . $plugin, $function), $args);
}

/**
* helper function to check all the plugins for sanity and set any insane ones to invisible.
*
* @param array $plugins to check (if null, defaults to all)
*               one string will work too for a single plugin.
*
* @return array array of insane instances (keys= id, values = reasons (keys for plugin lang)
*/
function portfolio_plugin_sanity_check($plugins=null) {
    global $DB;
    if (is_string($plugins)) {
       $plugins = array($plugins);
    } else if (empty($plugins)) {
        $plugins = get_list_of_plugins('portfolio/type');
    }

    $insane = array();
    foreach ($plugins as $plugin) {
        if ($result = portfolio_static_function($plugin, 'plugin_sanity_check')) {
            $insane[$plugin] = $result;
        }
    }
    if (empty($insane)) {
        return array();
    }
    list($where, $params) = $DB->get_in_or_equal(array_keys($insane));
    $where = ' plugin ' . $where;
    $DB->set_field_select('portfolio_instance', 'visible', 0, $where, $params);
    return $insane;
}

/**
* helper function to check all the instances for sanity and set any insane ones to invisible.
*
* @param array $instances to check (if null, defaults to all)
*              one instance or id will work too
*
* @return array array of insane instances (keys= id, values = reasons (keys for plugin lang)
*/
function portfolio_instance_sanity_check($instances=null) {
    global $DB;
    if (empty($instances)) {
        $instances = portfolio_instances(false);
    } else if (!is_array($instances)) {
        $instances = array($instances);
    }

    $insane = array();
    foreach ($instances as $instance) {
        if (is_object($instance) && !($instance instanceof portfolio_plugin_base)) {
            $instance = portfolio_instance($instance->id, $instance);
        } else if (is_numeric($instance)) {
            $instance = portfolio_instance($instance);
        }
        if (!($instance instanceof portfolio_plugin_base)) {
            debugging('something weird passed to portfolio_instance_sanity_check, not subclass or id');
            continue;
        }
        if ($result = $instance->instance_sanity_check()) {
            $insane[$instance->get('id')] = $result;
        }
    }
    if (empty($insane)) {
        return array();
    }
    list ($where, $params) = $DB->get_in_or_equal(array_keys($insane));
    $where = ' id ' . $where;
    $DB->set_field_select('portfolio_instance', 'visible', 0, $where, $params);
    return $insane;
}

/**
* helper function to display a table of plugins (or instances) and reasons for disabling
*
* @param array $insane array of insane plugins (key = plugin (or instance id), value = reason)
* @param array $instances if reporting instances rather than whole plugins, pass the array (key = id, value = object) here
*
*/
function portfolio_report_insane($insane, $instances=false, $return=false) {
    if (empty($insane)) {
        return;
    }

    static $pluginstr;
    if (empty($pluginstr)) {
        $pluginstr = get_string('plugin', 'portfolio');
    }
    if ($instances) {
        $headerstr = get_string('someinstancesdisabled', 'portfolio');
    } else {
        $headerstr = get_string('somepluginsdisabled', 'portfolio');
    }

    $output = notify($headerstr, 'notifyproblem', 'center', true);
    $table = new StdClass;
    $table->head = array($pluginstr, '');
    $table->data = array();
    foreach ($insane as $plugin => $reason) {
        if ($instances) {
            $instance = $instances[$plugin];
            $plugin   = $instance->get('plugin');
            $name     = $instance->get('name');
        } else {
            $name = $plugin;
        }
        $table->data[] = array($name, get_string($reason, 'portfolio_' . $plugin));
    }
    $output .= print_table($table, true);
    $output .= '<br /><br /><br />';

    if ($return) {
        return $output;
    }
    echo $output;
}

/**
* fake the url to portfolio/add.php from data from somewhere else
* you should use portfolio_add_button instead 99% of the time
*
* @param int    $instanceid   instanceid (optional, will force a new screen if not specified)
* @param string $classname    callback classname
* @param string $classfile    file containing the callback class definition
* @param array  $callbackargs arguments to pass to the callback class
*/
function portfolio_fake_add_url($instanceid, $classname, $classfile, $callbackargs) {
    global $CFG;
    $url = $CFG->wwwroot . '/portfolio/add.php?instance=' . $instanceid . '&amp;callbackclass=' . $classname . '&amp;callbackfile=' . $classfile;

    if (is_object($callbackargs)) {
        $callbackargs = (array)$callbackargs;
    }
    if (!is_array($callbackargs) || empty($callbackargs)) {
        return $url;
    }
    foreach ($callbackargs as $key => $value) {
        $url .= '&amp;ca_' . $key . '=' . urlencode($value);
    }
    return $url;
}



/**
* event handler for the portfolio_send event
*/
function portfolio_handle_event($eventdata) {
    global $CFG;
    $exporter = portfolio_exporter::rewaken_object($eventdata);
    $exporter->process_stage_package();
    $exporter->process_stage_send();
    $exporter->save();
    $exporter->process_stage_cleanup();
    return true;
}

/**
* main portfolio cronjob
* currently just cleans up expired transfer records.
*
* @todo add hooks in the plugins - either per instance or per plugin
*/
function portfolio_cron() {
    global $DB;

    if ($expired = $DB->get_records_select('portfolio_tempdata', 'expirytime < ?', array(time()), '', 'id')) {
        foreach ($expired as $d) {
            $e = portfolio_exporter::rewaken_object($d);
            $e->process_stage_cleanup(true);
        }
    }
}

/**
* helper function to rethrow a caught portfolio_exception as an export exception
*
* used because when a portfolio_export exception is thrown the export is cancelled
*
* @param portfolio_exporter $exporter  current exporter object
* @param exception          $exception exception to rethrow
*
* @return void
* @throws portfolio_export_exceptiog
*/
function portfolio_export_rethrow_exception($exporter, $exception) {
    throw new portfolio_export_exception($exporter, $exception->errorcode, $exception->module, $exception->link, $exception->a);
}

/**
* try and determine expected_time for purely file based exports
* or exports that might include large file attachments.
*
* @param mixed $totest - either an array of stored_file objects or a single stored_file object
*
* @return constant PORTFOLIO_TIME_XXX
*/
function portfolio_expected_time_file($totest) {
    global $CFG;
    if ($totest instanceof stored_file) {
        $totest = array($totest);
    }
    $size = 0;
    foreach ($totest as $file) {
        if (!($file instanceof stored_file)) {
            debugging('something weird passed to portfolio_expected_time_file - not stored_file object');
            debugging(print_r($file, true));
            continue;
        }
        $size += $file->get_filesize();
    }

    $fileinfo = portfolio_filesize_info();

    $moderate = $high = 0; // avoid warnings

    foreach (array('moderate', 'high') as $setting) {
        $settingname = 'portfolio_' . $setting . '_filesize_threshold';
        if (empty($CFG->{$settingname}) || !array_key_exists($CFG->{$settingname}, $fileinfo['options'])) {
            debugging("weird or unset admin value for $settingname, using default instead");
            $$setting = $fileinfo[$setting];
        } else {
            $$setting = $CFG->{$settingname};
        }
    }

    if ($size < $moderate) {
        return PORTFOLIO_TIME_LOW;
    } else if ($size < $high) {
        return PORTFOLIO_TIME_MODERATE;
    }
    return PORTFOLIO_TIME_HIGH;
}


/**
* the default filesizes and threshold information for file based transfers
* this shouldn't need to be used outside the admin pages and the portfolio code
*/
function portfolio_filesize_info() {
    $filesizes = array();
    $sizelist = array(10240, 51200, 102400, 512000, 1048576, 2097152, 5242880, 10485760, 20971520, 52428800);
    foreach ($sizelist as $size) {
        $filesizes[$size] = display_size($size);
    }
    return array(
        'options' => $filesizes,
        'moderate' => 1048576,
        'high'     => 5242880,
    );
}

/**
* try and determine expected_time for purely database based exports
* or exports that might include large parts of a database
*
* @param integer $recordcount - number of records trying to export
*
* @return constant PORTFOLIO_TIME_XXX
*/
function portfolio_expected_time_db($recordcount) {
    global $CFG;

    if (empty($CFG->portfolio_moderate_dbsize_threshold)) {
        set_config('portfolio_moderate_dbsize_threshold', 10);
    }
    if (empty($CFG->portfolio_high_dbsize_threshold)) {
        set_config('portfolio_high_dbsize_threshold', 50);
    }
    if ($recordcount < $CFG->portfolio_moderate_dbsize_threshold) {
        return PORTFOLIO_TIME_LOW;
    } else if ($recordcount < $CFG->portfolio_high_dbsize_threshold) {
        return PORTFOLIO_TIME_MODERATE;
    }
    return PORTFOLIO_TIME_HIGH;
}

?>