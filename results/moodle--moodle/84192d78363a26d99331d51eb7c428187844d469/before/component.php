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
 * Components (core subsystems + plugins) related code.
 *
 * @package    core
 * @copyright  2013 Petr Skoda {@link http://skodak.org}
 * @license    http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */

/**
 * Collection of components related methods.
 */
class core_component {
    /** @var array list of ignored directories - watch out for auth/db exception */
    protected static $ignoreddirs = array('CVS'=>true, '_vti_cnf'=>true, 'simpletest'=>true, 'db'=>true, 'yui'=>true, 'tests'=>true, 'classes'=>true);
    /** @var array list plugin types that support subplugins, do not add more here unless absolutely necessary */
    protected static $supportsubplugins = array('mod', 'editor', 'local');

    /** @var null cache of plugin types */
    protected static $plugintypes = null;
    /** @var null cache of plugin locations */
    protected static $plugins = null;
    /** @var null cache of core subsystems */
    protected static $subsystems = null;
    /** @var null list of all known classes that can be autoloaded */
    protected static $classmap = null;

    /**
     * Class loader for Frankenstyle named classes in standard locations.
     * Frankenstyle namespaces are supported.
     *
     * The expected location for core classes is:
     *    1/ core_xx_yy_zz ---> lib/classes/xx_yy_zz.php
     *    2/ \core\xx_yy_zz ---> lib/classes/xx_yy_zz.php
     *    3/ \core\xx\yy_zz ---> lib/classes/xx/yy_zz.php
     *
     * The expected location for plugin classes is:
     *    1/ mod_name_xx_yy_zz ---> mod/name/classes/xx_yy_zz.php
     *    2/ \mod_name\xx_yy_zz ---> mod/name/classes/xx_yy_zz.php
     *    3/ \mod_name\xx\yy_zz ---> mod/name/classes/xx/yy_zz.php
     *
     * @param string $classname
     */
    public static function classloader($classname) {
        self::init();

        if (isset(self::$classmap[$classname])) {
            // Global $CFG is expected in included scripts.
            global $CFG;
            // Function include would be faster, but for BC it is better to include only once.
            include_once(self::$classmap[$classname]);
            return;
        }
    }

    /**
     * Initialise caches, always call before accessing self:: caches.
     */
    protected static function init() {
        global $CFG;

        // Init only once per request/CLI execution, we ignore changes done afterwards.
        if (isset(self::$plugintypes)) {
            return;
        }

        if (PHPUNIT_TEST or !empty($CFG->early_install_lang) or
                (defined('BEHAT_UTIL') and BEHAT_UTIL) or
                (defined('BEHAT_TEST') and BEHAT_TEST)) {
            // 1/ Do not bother storing the file for unit tests,
            //    we need fresh copy for each execution and
            //    later we keep it in memory.
            // 2/ We can not write to dataroot in installer yet.
            self::fill_all_caches();
            return;
        }

        // Note: cachedir MUST be shared by all servers in a cluster, sorry guys...
        //       MUC should use classloading, we can not depend on it here.
        $cachefile = "$CFG->cachedir/core_component.php";

        if (!CACHE_DISABLE_ALL and !self::is_developer()) {
            // 1/ Use the cache only outside of install and upgrade.
            // 2/ Let developers add/remove classes in developer mode.
            if (is_readable($cachefile)) {
                $cache = false;
                include($cachefile);
                if (!is_array($cache)) {
                    // Something is very wrong.
                } else if (!isset($cache['plugintypes']) or !isset($cache['plugins']) or !isset($cache['subsystems']) or !isset($cache['classmap'])) {
                    // Something is very wrong.
                } else if ($cache['plugintypes']['mod'] !== "$CFG->dirroot/mod") {
                    // Dirroot was changed.
                } else {
                    // The cache looks ok, let's use it.
                    self::$plugintypes = $cache['plugintypes'];
                    self::$plugins     = $cache['plugins'];
                    self::$subsystems  = $cache['subsystems'];
                    self::$classmap    = $cache['classmap'];
                    return;
                }
            }
        }

        $cachedir = dirname($cachefile);
        if (!is_dir($cachedir)) {
            mkdir($cachedir, $CFG->directorypermissions, true);
        }

        if (!isset(self::$plugintypes)) {
            self::fill_all_caches();

            // This needs to be atomic and self-fixing as much as possible.

            $content = self::get_cache_content();
            if (file_exists($cachefile)) {
                if (sha1_file($cachefile) === sha1($content)) {
                    return;
                }
                unlink($cachefile);
            }

            if ($fp = @fopen($cachefile.'.tmp', 'xb')) {
                fwrite($fp, $content);
                fclose($fp);
                @rename($cachefile.'.tmp', $cachefile);
                @chmod($cachefile, $CFG->filepermissions);
            }
            @unlink($cachefile.'.tmp'); // Just in case anything fails (race condition).
            self::invalidate_opcode_php_cache($cachefile);
        }
    }

    /**
     * Are we in developer debug mode?
     *
     * Note: You need to set "$CFG->debug = (E_ALL | E_STRICT);" in config.php,
     *       the reason is we need to use this before we setup DB connection or caches for CFG.
     *
     * @return bool
     */
    protected static function is_developer() {
        global $CFG;

        if (!isset($CFG->config_php_settings['debug'])) {
            return false;
        }

        $debug = (int)$CFG->config_php_settings['debug'];
        if ($debug & E_ALL and $debug & E_STRICT) {
            return true;
        }

        return false;
    }

    /**
     * Create cache file content.
     *
     * @return string
     */
    protected static function get_cache_content() {
        $cache = array(
            'subsystems'  => self::$subsystems,
            'plugintypes' => self::$plugintypes,
            'plugins'     => self::$plugins,
            'classmap'    => self::$classmap,
        );

        return '<?php
$cache = '.var_export($cache, true).';
';
    }

    /**
     * Fill all caches.
     */
    protected static function fill_all_caches() {
        self::$subsystems = self::fetch_subsystems();

        self::$plugintypes = self::fetch_plugintypes();

        self::$plugins = array();
        foreach (self::$plugintypes as $type => $fulldir) {
            self::$plugins[$type] = self::fetch_plugins($type, $fulldir);
        }

        self::fill_classmap_cache();
    }

    /**
     * Returns list of core subsystems.
     * @return array
     */
    protected static function fetch_subsystems() {
        global $CFG;

        // NOTE: Any additions here must be verified to not collide with existing add-on modules and subplugins!!!

        $info = array(
            'access'      => null,
            'admin'       => $CFG->dirroot.'/'.$CFG->admin,
            'auth'        => $CFG->dirroot.'/auth',
            'backup'      => $CFG->dirroot.'/backup/util/ui',
            'badges'      => $CFG->dirroot.'/badges',
            'block'       => $CFG->dirroot.'/blocks',
            'blog'        => $CFG->dirroot.'/blog',
            'bulkusers'   => null,
            'cache'       => $CFG->dirroot.'/cache',
            'calendar'    => $CFG->dirroot.'/calendar',
            'cohort'      => $CFG->dirroot.'/cohort',
            'condition'   => null,
            'completion'  => null,
            'countries'   => null,
            'course'      => $CFG->dirroot.'/course',
            'currencies'  => null,
            'dbtransfer'  => null,
            'debug'       => null,
            'dock'        => null,
            'editor'      => $CFG->dirroot.'/lib/editor',
            'edufields'   => null,
            'enrol'       => $CFG->dirroot.'/enrol',
            'error'       => null,
            'filepicker'  => null,
            'files'       => $CFG->dirroot.'/files',
            'filters'     => null,
            //'fonts'       => null, // Bogus.
            'form'        => $CFG->dirroot.'/lib/form',
            'grades'      => $CFG->dirroot.'/grade',
            'grading'     => $CFG->dirroot.'/grade/grading',
            'group'       => $CFG->dirroot.'/group',
            'help'        => null,
            'hub'         => null,
            'imscc'       => null,
            'install'     => null,
            'iso6392'     => null,
            'langconfig'  => null,
            'license'     => null,
            'mathslib'    => null,
            'media'       => null,
            'message'     => $CFG->dirroot.'/message',
            'mimetypes'   => null,
            'mnet'        => $CFG->dirroot.'/mnet',
            //'moodle.org'  => null, // Not used any more.
            'my'          => $CFG->dirroot.'/my',
            'notes'       => $CFG->dirroot.'/notes',
            'pagetype'    => null,
            'pix'         => null,
            'plagiarism'  => $CFG->dirroot.'/plagiarism',
            'plugin'      => null,
            'portfolio'   => $CFG->dirroot.'/portfolio',
            'publish'     => $CFG->dirroot.'/course/publish',
            'question'    => $CFG->dirroot.'/question',
            'rating'      => $CFG->dirroot.'/rating',
            'register'    => $CFG->dirroot.'/'.$CFG->admin.'/registration', // Broken badly if $CFG->admin changed.
            'repository'  => $CFG->dirroot.'/repository',
            'rss'         => $CFG->dirroot.'/rss',
            'role'        => $CFG->dirroot.'/'.$CFG->admin.'/roles',
            'search'      => null,
            'table'       => null,
            'tag'         => $CFG->dirroot.'/tag',
            'timezones'   => null,
            'user'        => $CFG->dirroot.'/user',
            'userkey'     => null,
            'webservice'  => $CFG->dirroot.'/webservice',
        );

        return $info;
    }

    /**
     * Returns list of known plugin types.
     * @return array
     */
    protected static function fetch_plugintypes() {
        global $CFG;

        $types = array(
            'qtype'         => $CFG->dirroot.'/question/type',
            'mod'           => $CFG->dirroot.'/mod',
            'auth'          => $CFG->dirroot.'/auth',
            'enrol'         => $CFG->dirroot.'/enrol',
            'message'       => $CFG->dirroot.'/message/output',
            'block'         => $CFG->dirroot.'/blocks',
            'filter'        => $CFG->dirroot.'/filter',
            'editor'        => $CFG->dirroot.'/lib/editor',
            'format'        => $CFG->dirroot.'/course/format',
            'profilefield'  => $CFG->dirroot.'/user/profile/field',
            'report'        => $CFG->dirroot.'/report',
            'coursereport'  => $CFG->dirroot.'/course/report', // Must be after system reports.
            'gradeexport'   => $CFG->dirroot.'/grade/export',
            'gradeimport'   => $CFG->dirroot.'/grade/import',
            'gradereport'   => $CFG->dirroot.'/grade/report',
            'gradingform'   => $CFG->dirroot.'/grade/grading/form',
            'mnetservice'   => $CFG->dirroot.'/mnet/service',
            'webservice'    => $CFG->dirroot.'/webservice',
            'repository'    => $CFG->dirroot.'/repository',
            'portfolio'     => $CFG->dirroot.'/portfolio',
            'qbehaviour'    => $CFG->dirroot.'/question/behaviour',
            'qformat'       => $CFG->dirroot.'/question/format',
            'plagiarism'    => $CFG->dirroot.'/plagiarism',
            'tool'          => $CFG->dirroot.'/'.$CFG->admin.'/tool',
            'cachestore'    => $CFG->dirroot.'/cache/stores',
            'cachelock'     => $CFG->dirroot.'/cache/locks',

        );

        if (!empty($CFG->themedir) and is_dir($CFG->themedir) ) {
            $types['theme'] = $CFG->themedir;
        } else {
            $types['theme'] = $CFG->dirroot.'/theme';
        }

        foreach (self::$supportsubplugins as $type) {
            if ($type === 'local') {
                // Local subplugins must be after local plugins.
                continue;
            }
            $subplugins = self::fetch_subplugins($type, $types[$type]);
            foreach($subplugins as $subtype => $subplugin) {
                if (isset($types[$subtype])) {
                    error_log("Invalid subtype '$subtype', duplicate detected.");
                    continue;
                }
                $types[$subtype] = $subplugin;
            }
        }

        // Local is always last!
        $types['local'] = $CFG->dirroot.'/local';

        if (in_array('local', self::$supportsubplugins)) {
            $subplugins = self::fetch_subplugins('local', $types['local']);
            foreach($subplugins as $subtype => $subplugin) {
                if (isset($types[$subtype])) {
                    error_log("Invalid subtype '$subtype', duplicate detected.");
                    continue;
                }
                $types[$subtype] = $subplugin;
            }
        }

        return $types;
    }

    /**
     * Returns list of subtypes defined in given plugin type.
     * @param string $type
     * @param string $fulldir
     * @return array
     */
    protected static function fetch_subplugins($type, $fulldir) {
        global $CFG;

        $types = array();
        $subpluginowners = self::fetch_plugins($type, $fulldir);
        foreach ($subpluginowners as $ownerdir) {
            if (file_exists("$ownerdir/db/subplugins.php")) {
                $subplugins = array();
                include("$ownerdir/db/subplugins.php");
                foreach ($subplugins as $subtype => $dir) {
                    if (!preg_match('/^[a-z][a-z0-9]*$/', $subtype)) {
                        error_log("Invalid subtype '$subtype'' detected in '$ownerdir', invalid characters present.");
                        continue;
                    }
                    if (isset(self::$subsystems[$subtype])) {
                        error_log("Invalid subtype '$subtype'' detected in '$ownerdir', duplicates core subsystem.");
                        continue;
                    }
                    if (!is_dir("$CFG->dirroot/$dir")) {
                        error_log("Invalid subtype directory '$dir' detected in '$ownerdir'.");
                        continue;
                    }
                    $types[$subtype] = "$CFG->dirroot/$dir";
                }
            }
        }
        return $types;
    }

    /**
     * Returns list of plugins of given type in given directory.
     * @param string $plugintype
     * @param string $fulldir
     * @return array
     */
    protected static function fetch_plugins($plugintype, $fulldir) {
        global $CFG;

        $fulldirs = (array)$fulldir;
        if ($plugintype === 'theme') {
            if (realpath($fulldir) !== realpath($CFG->dirroot.'/theme')) {
                // Include themes in standard location too.
                array_unshift($fulldirs, $CFG->dirroot.'/theme');
            }
        }

        $result = array();

        foreach ($fulldirs as $fulldir) {
            if (!is_dir($fulldir)) {
                continue;
            }
            $items = new \DirectoryIterator($fulldir);
            foreach ($items as $item) {
                if ($item->isDot() or !$item->isDir()) {
                    continue;
                }
                $pluginname = $item->getFilename();
                if ($plugintype === 'auth' and $pluginname === 'db') {
                    // Special exception for this wrong plugin name.
                } else if (isset(self::$ignoreddirs[$pluginname])) {
                    continue;
                }
                if (!self::is_valid_plugin_name($plugintype, $pluginname)) {
                    // Always ignore plugins with problematic names here.
                    continue;
                }
                $result[$pluginname] = $fulldir.'/'.$pluginname;
                unset($item);
            }
            unset($items);
        }

        ksort($result);
        return $result;
    }

    /**
     * Find all classes that can be autoloaded including frankenstyle namespaces.
     */
    protected static function fill_classmap_cache() {
        global $CFG;

        self::$classmap = array();

        self::load_classes('core', "$CFG->dirroot/lib/classes");

        foreach (self::$subsystems as $subsystem => $fulldir) {
            self::load_classes('core_'.$subsystem, "$fulldir/classes");
        }

        foreach (self::$plugins as $plugintype => $plugins) {
            foreach ($plugins as $pluginname => $fulldir) {
                self::load_classes($plugintype.'_'.$pluginname, "$fulldir/classes");
            }
        }

        // Note: Add extra deprecated legacy classes here as necessary.
        self::$classmap['textlib'] = "$CFG->dirroot/lib/classes/text.php";
        self::$classmap['collatorlib'] = "$CFG->dirroot/lib/classes/collator.php";
    }

    /**
     * Find classes in directory and recurse to subdirs.
     * @param string $component
     * @param string $fulldir
     * @param string $namespace
     */
    protected static function load_classes($component, $fulldir, $namespace = '') {
        if (!is_dir($fulldir)) {
            return;
        }

        $items = new \DirectoryIterator($fulldir);
        foreach ($items as $item) {
            if ($item->isDot()) {
                continue;
            }
            if ($item->isDir()) {
                $dirname = $item->getFilename();
                self::load_classes($component, "$fulldir/$dirname", $namespace.'\\'.$dirname);
                continue;
            }

            $filename = $item->getFilename();
            $classname = preg_replace('/\.php$/', '', $filename);

            if ($filename === $classname) {
                // Not a php file.
                continue;
            }
            if ($namespace === '') {
                // Legacy long frankenstyle class name.
                self::$classmap[$component.'_'.$classname] = "$fulldir/$filename";
            }
            // New namespaced classes.
            self::$classmap[$component.$namespace.'\\'.$classname] = "$fulldir/$filename";
        }
        unset($item);
        unset($items);
    }

    /**
     * List all core subsystems and their location
     *
     * This is a whitelist of components that are part of the core and their
     * language strings are defined in /lang/en/<<subsystem>>.php. If a given
     * plugin is not listed here and it does not have proper plugintype prefix,
     * then it is considered as course activity module.
     *
     * The location is absolute file path to dir. NULL means there is no special
     * directory for this subsystem. If the location is set, the subsystem's
     * renderer.php is expected to be there.
     *
     * @return array of (string)name => (string|null)full dir location
     */
    public static function get_core_subsystems() {
        self::init();
        return self::$subsystems;
    }

    /**
     * Get list of available plugin types together with their location.
     *
     * @return array as (string)plugintype => (string)fulldir
     */
    public static function get_plugin_types() {
        self::init();
        return self::$plugintypes;
    }

    /**
     * Get list of plugins of given type.
     *
     * @param string $plugintype
     * @return array as (string)pluginname => (string)fulldir
     */
    public static function get_plugin_list($plugintype) {
        self::init();

        if (!isset(self::$plugins[$plugintype])) {
            return array();
        }
        return self::$plugins[$plugintype];
    }

    /**
     * Get a list of all the plugins of a given type that define a certain class
     * in a certain file. The plugin component names and class names are returned.
     *
     * @param string $plugintype the type of plugin, e.g. 'mod' or 'report'.
     * @param string $class the part of the name of the class after the
     *      frankenstyle prefix. e.g 'thing' if you are looking for classes with
     *      names like report_courselist_thing. If you are looking for classes with
     *      the same name as the plugin name (e.g. qtype_multichoice) then pass ''.
     *      Frankenstyle namespaces are also supported.
     * @param string $file the name of file within the plugin that defines the class.
     * @return array with frankenstyle plugin names as keys (e.g. 'report_courselist', 'mod_forum')
     *      and the class names as values (e.g. 'report_courselist_thing', 'qtype_multichoice').
     */
    public static function get_plugin_list_with_class($plugintype, $class, $file = null) {
        global $CFG; // Necessary in case it is referenced by included PHP scripts.

        if ($class) {
            $suffix = '_' . $class;
        } else {
            $suffix = '';
        }

        $pluginclasses = array();
        $plugins = self::get_plugin_list($plugintype);
        foreach ($plugins as $plugin => $fulldir) {
            // Try class in frankenstyle namespace.
            if ($class) {
                $classname = '\\' . $plugintype . '_' . $plugin . '\\' . $class;
                if (class_exists($classname, true)) {
                    $pluginclasses[$plugintype . '_' . $plugin] = $classname;
                    continue;
                }
            }

            // Try autoloading of class with frankenstyle prefix.
            $classname = $plugintype . '_' . $plugin . $suffix;
            if (class_exists($classname, true)) {
                $pluginclasses[$plugintype . '_' . $plugin] = $classname;
                continue;
            }

            // Fall back to old file location and class name.
            if ($file and file_exists("$fulldir/$file")) {
                include_once("$fulldir/$file");
                if (class_exists($classname, false)) {
                    $pluginclasses[$plugintype . '_' . $plugin] = $classname;
                    continue;
                }
            }
        }

        return $pluginclasses;
    }

    /**
     * Returns the exact absolute path to plugin directory.
     *
     * @param string $plugintype type of plugin
     * @param string $pluginname name of the plugin
     * @return string full path to plugin directory; null if not found
     */
    public static function get_plugin_directory($plugintype, $pluginname) {
        if (empty($pluginname)) {
            // Invalid plugin name, sorry.
            return null;
        }

        self::init();

        if (!isset(self::$plugins[$plugintype][$pluginname])) {
            return null;
        }
        return self::$plugins[$plugintype][$pluginname];
    }

    /**
     * Returns the exact absolute path to plugin directory.
     *
     * @param string $subsystem type of core subsystem
     * @return string full path to subsystem directory; null if not found
     */
    public static function get_subsystem_directory($subsystem) {
        self::init();

        if (!isset(self::$subsystems[$subsystem])) {
            return null;
        }
        return self::$subsystems[$subsystem];
    }

    /**
     * This method validates a plug name. It is much faster than calling clean_param.
     *
     * @param string $plugintype type of plugin
     * @param string $pluginname a string that might be a plugin name.
     * @return bool if this string is a valid plugin name.
     */
    public static function is_valid_plugin_name($plugintype, $pluginname) {
        if ($plugintype === 'mod') {
            // Modules must not have the same name as core subsystems.
            if (!isset(self::$subsystems)) {
                // Watch out, this is called from init!
                self::init();
            }
            if (isset(self::$subsystems[$pluginname])) {
                return false;
            }
            // Modules MUST NOT have any underscores,
            // component normalisation would break very badly otherwise!
            return (bool)preg_match('/^[a-z][a-z0-9]*$/', $pluginname);

        } else {
            return (bool)preg_match('/^[a-z](?:[a-z0-9_](?!__))*[a-z0-9]$/', $pluginname);
        }
    }

    /**
     * Normalize the component name using the "frankenstyle" rules.
     *
     * Note: this does not verify the validity of plugin or type names.
     *
     * @param string $component
     * @return array as (string)$type => (string)$plugin
     */
    public static function normalize_component($component) {
        if ($component === 'moodle' or $component === 'core' or $component === '') {
            return array('core', null);
        }

        if (strpos($component, '_') === false) {
            self::init();
            if (array_key_exists($component, self::$subsystems)) {
                $type   = 'core';
                $plugin = $component;
            } else {
                // Everything else without underscore is a module.
                $type   = 'mod';
                $plugin = $component;
            }

        } else {
            list($type, $plugin) = explode('_', $component, 2);
            if ($type === 'moodle') {
                $type = 'core';
            }
            // Any unknown type must be a subplugin.
        }

        return array($type, $plugin);
    }

    /**
     * Return exact absolute path to a plugin directory.
     *
     * @param string $component name such as 'moodle', 'mod_forum'
     * @return string full path to component directory; NULL if not found
     */
    public static function get_component_directory($component) {
        global $CFG;

        list($type, $plugin) = self::normalize_component($component);

        if ($type === 'core') {
            if ($plugin === null) {
                return $path = $CFG->libdir;
            }
            return self::get_subsystem_directory($plugin);
        }

        return self::get_plugin_directory($type, $plugin);
    }

    /**
     * Returns list of plugin types that allow subplugins.
     * @return array as (string)plugintype => (string)fulldir
     */
    public static function get_plugin_types_with_subplugins() {
        self::init();

        $return = array();
        foreach (self::$supportsubplugins as $type) {
            $return[$type] = self::$plugintypes[$type];
        }
        return $return;
    }

    /**
     * Invalidate opcode cache for given file, this is intended for
     * php files that are stored in dataroot.
     *
     * Note: we need it here because this class must be self-contained.
     *
     * @param string $file
     */
    public static function invalidate_opcode_php_cache($file) {
        if (function_exists('opcache_invalidate')) {
            if (!file_exists($file)) {
                return;
            }
            opcache_invalidate($file, true);
        }
    }
}