<?php // $Id$

///////////////////////////////////////////////////////////////////////////
//                                                                       //
// NOTICE OF COPYRIGHT                                                   //
//                                                                       //
// Moodle - Modular Object-Oriented Dynamic Learning Environment         //
//          http://moodle.com                                            //
//                                                                       //
// Copyright (C) 2008 onwards  Moodle Pty Ltd   http://moodle.com        //
//                                                                       //
// This program is free software; you can redistribute it and/or modify  //
// it under the terms of the GNU General Public License as published by  //
// the Free Software Foundation; either version 2 of the License, or     //
// (at your option) any later version.                                   //
//                                                                       //
// This program is distributed in the hope that it will be useful,       //
// but WITHOUT ANY WARRANTY; without even the implied warranty of        //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         //
// GNU General Public License for more details:                          //
//                                                                       //
//          http://www.gnu.org/copyleft/gpl.html                         //
//                                                                       //
///////////////////////////////////////////////////////////////////////////

/**
 * This is the base class of the repository class
 *
 * To use repository plugin, you need to create a new folder under repository/, named as the remote
 * repository, the subclass must be defined in  the name

 *
 * class repository is an abstract class, some functions must be implemented in subclass.
 *
 * See an example of use of this library in repository/boxnet/repository.class.php
 *
 * A few notes:
 *   // print login page or a link to redirect to another page
 *   $repo->print_login();
 *   // call get_listing, and print result
 *   $repo->print_listing();
 *   // print a search box
 *   $repo->print_search();
 *
 * @version 1.0 dev
 * @package repository
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 */
require_once(dirname(dirname(__FILE__)) . '/config.php');
require_once(dirname(dirname(__FILE__)).'/lib/filelib.php');
require_once(dirname(dirname(__FILE__)).'/lib/formslib.php');

/**
 * A repository_type is a repository plug-in. It can be Box.net, Flick-r, ...
 * A repository type can be edited, sorted and hidden. It is mandatory for an
 * administrator to create a repository type in order to be able to create
 * some instances of this type.
 *
 * Coding note:
 * - a repository_type object is mapped to the "repository" database table
 * - "typename" attibut maps the "type" database field. It is unique.
 * - general "options" for a repository type are saved in the config_plugin table
 * - TODO: when you delete a repository, all instances are deleted
 * - When you create a type for a plugin that can't have multiple instances, a
 *   instance is automatically created.
 */
class repository_type {


    /**
     * Type name (no whitespace) - A type name is unique
     * Note: for a user-friendly type name see get_readablename()
     * @var String
     */
    private $_typename;


    /**
     * Options of this type
     * They are general options that any instance of this type would share
     * e.g. API key
     * These options are saved in config_plugin table
     * @var array
     */
   private $_options;


    /**
     * Is the repository type visible or hidden
     * If false (hidden): no instances can be created, edited, deleted, showned , used...
     * @var boolean
     */
   private $_visible;


    /**
     * 0 => not ordered, 1 => first position, 2 => second position...
     * A not order type would appear in first position (should never happened)
     * @var integer
     */
    private $_sortorder;

    /**
     * repository_type constructor
     * @global <type> $CFG
     * @param integer $typename
     * @param array $typeoptions
     * @param boolean $visible
     * @param integer $sortorder (don't really need set, it will be during create() call)
     */
    public function __construct($typename = '', $typeoptions = array(), $visible = true, $sortorder = 0){
        global $CFG;

        //set type attributs
        $this->_typename = $typename;
        $this->_visible = $visible;
        $this->_sortorder = $sortorder;

        //set options attribut
        $this->_options = array();
        //check that the type can be setup
        if (repository_static_function($typename,"has_admin_config")){
            $options = repository_static_function($typename,'get_admin_option_names');
            //set the type options
            foreach ($options as $config) {
                if (array_key_exists($config,$typeoptions)){
                        $this->_options[$config] = $typeoptions[$config];
                }
            }
        }
    }

    /**
     * Get the type name (no whitespace)
     * For a human readable name, use get_readablename()
     * @return String the type name
     */
    public function get_typename(){
        return $this->_typename;
    }

    /**
     * Return a human readable and user-friendly type name
     * @return string user-friendly type name
     */
    public function get_readablename(){
        return get_string('repositoryname','repository_'.$this->_typename);
    }

    /**
     * Return general options
     * @return array the general options
     */
    public function get_options(){
        return $this->_options;
    }

    /**
     * Return visibility
     * @return boolean
     */
    public function get_visible(){
        return $this->_visible;
    }

    /**
     * Return order / position of display in the file picker
     * @return integer
     */
    public function get_sortorder(){
        return $this->_sortorder;
    }

    /**
     * Create a repository type (the type name must not already exist)
     * @global object $DB
     */
    public function create(){
        global $DB;

        //check that $type has been set
        $timmedtype = trim($this->_typename);
        if (empty($timmedtype)) {
             throw new repository_exception('emptytype', 'repository');
        }

        //set sortorder as the last position in the list
        if (!isset($this->_sortorder) || $this->_sortorder == 0 ){
            $sql = "SELECT MAX(sortorder) FROM {repository}";
            $this->_sortorder = 1 + $DB->get_field_sql($sql);
        }

        //only create a new type if it doesn't already exist
        $existingtype = $DB->get_record('repository', array('type'=>$this->_typename));
        if(!$existingtype){
           //create the type
           $newtype = new stdclass;
           $newtype->type = $this->_typename;
           $newtype->visible = $this->_visible;
           $newtype->sortorder = $this->_sortorder;
           $DB->insert_record('repository', $newtype);

           //save the options in DB
           $this->update_options();
        }
        else {
            throw new repository_exception('existingrepository', 'repository');
        }
    }


    /**
     * Update plugin options into the config_plugin table
     * @param array $options
     * @return boolean
     */
    public function update_options($options = null){
        if (!empty($options)){
            $this->_options = $options;
        }

        foreach ($this->_options as $name => $value) {
            set_config($name,$value,$this->_typename);
        }

        return true;
    }

    /**
     * Update visible database field with the value given as parameter
     * or with the visible value of this object
     * This function is private.
     * For public access, have a look to switch_and_update_visibility()
     * @global object $DB
     * @param boolean $visible
     * @return boolean
     */
    private function update_visible($visible = null){
        global $DB;

        if (!empty($visible)){
            $this->_visible = $visible;
        }
        else if (!isset($this->_visible)){
            throw new repository_exception('updateemptyvisible', 'repository');
        }

        return $DB->set_field('repository', 'visible', $this->_visible, array('type'=>$this->_typename));
    }

    /**
     * Update database sortorder field with the value given as parameter
     * or with the sortorder value of this object
     * This function is private.
     * For public access, have a look to move_order()
     * @global object $DB
     * @param integer $sortorder
     * @return boolean
     */
    private function update_sortorder($sortorder = null){
        global $DB;

        if (!empty($sortorder) && $sortorder!=0){
            $this->_sortorder = $sortorder;
        }
        //if sortorder is not set, we set it as the ;ast position in the list
        else if (!isset($this->_sortorder) || $this->_sortorder == 0 ){
            $sql = "SELECT MAX(sortorder) FROM {repository}";
            $this->_sortorder = 1 + $DB->get_field_sql($sql);
        }

        return $DB->set_field('repository', 'sortorder', $this->_sortorder, array('type'=>$this->_typename));
    }

    /**
     * Change order of the type with its adjacent upper or downer type
     * (database fields are updated)
     * Algorithm details:
     * 1. retrieve all types in an array. This array is sorted by sortorder,
     * and the array keys start from 0 to X (incremented by 1)
     * 2. switch sortorder values of this type and its adjacent type
     * @global object $DB
     * @param string $move "up" or "down"
     */
    public function move_order($move) {
        global $DB;
        //retrieve all types
        $types = repository_get_types();

        //retrieve this type into the returned array
         $i = 0;
        while (!isset($indice) && $i<count($types)){
            if ($types[$i]->get_typename() == $this->_typename){
                $indice = $i;
            }
            $i++;
        }

        //retrieve adjacent indice
        switch ($move) {
            case "up":
                $adjacentindice = $indice - 1;
                break;
            case "down":
                $adjacentindice = $indice + 1;
                break;
            default:
                throw new repository_exception('movenotdefined', 'repository');
        }

        //switch sortorder of this type and the adjacent type
        //TODO: we could reset sortorder for all types. This is not as good in performance term, but
        //that prevent from wrong behaviour on a screwed database. As performance are not important in this particular case
        //it worth to change the algo.
        if ($adjacentindice>=0 && !empty($types[$adjacentindice])){
            $DB->set_field('repository', 'sortorder', $this->_sortorder, array('type'=>$types[$adjacentindice]->get_typename()));
            $this->update_sortorder($types[$adjacentindice]->get_sortorder());
        }
    }

    /**
     * 1. Switch the visibility OFF if it's ON, and ON if it's OFF.
     * 2. Update the type
     * @return <type>
     */
    public function switch_and_update_visibility(){
        $this->_visible = !$this->_visible;
        return $this->update_visible();
    }


    /**
     * Delete a repository_type - DO NOT CALL IT TILL THE TODO IS DONE
     * TODO: delete all instances for this type
     * @global object $DB
     * @return boolean
     */
    public function delete(){
        global $DB;
        return $DB->delete_records('repository', array('type' => $this->_typename));
    }
}

/**
 * Return a type for a given type name.
 * @global object $DB
 * @param string $typename the type name
 * @return integer
 */
function repository_get_type_by_typename($typename){
    global $DB;

    if(!$record = $DB->get_record('repository',array('type' => $typename))) {
        return false;
    }

    return new repository_type($typename, (array)get_config($typename), $record->visible, $record->sortorder);
}

/**
 * Return a type for a given type id.
 * @global object $DB
 * @param string $typename the type name
 * @return integer
 */
function repository_get_type_by_id($id){
    global $DB;

    if(!$record = $DB->get_record('repository',array('id' => $id))) {
        return false;
    }

    return new repository_type($record->type, (array)get_config($record->type), $record->visible, $record->sortorder);
}

/**
 * Return all repository types ordered by sortorder
 * first type in returnedarray[0], second type in returnedarray[1], ...
 * @global object $DB
 * @return array Repository types
 */
function repository_get_types(){
    global $DB;

    $types = array();

    if($records = $DB->get_records('repository',null,'sortorder')) {
        foreach($records as $type) {
            $typename = $type->type;
            $visible = $type->visible;
            $sortorder = $type->sortorder;
            $types[] = new repository_type($typename, (array)get_config($typename), $visible, $sortorder);
        }
    }

    return $types;
}

/**
 *TODO: write comment
 */

abstract class repository {
    public $id;
    public $context;
    public $options;

    /**
     * 1. Initialize context and options
     * 2. Accept necessary parameters
     *
     * @param integer $repositoryid
     * @param integer $contextid
     * @param array $options
     */
    public function __construct($repositoryid, $contextid = SITEID, $options = array()){
        $this->id = $repositoryid;
        $this->context = get_context_instance_by_id($contextid);
        $this->options = array();
        if (is_array($options)) {
            $options = array_merge($this->get_option(), $options);
        } else {
            $options = $this->get_option();
        }
        $this->options = array();
        foreach ($options as $n => $v) {
            $this->options[$n] = $v;
        }
        $this->name = $this->get_name();
    }

    /**
     * set options for repository instance
     *
     * @param string $name
     * @param mixed $value
     */
    public function __set($name, $value) {
        $this->options[$name] = $value;
    }

    /**
     * get options for repository instance
     *
     * @param string $name
     * @return mixed
     */
    public function __get($name) {
        if (array_key_exists($name, $this->options)){
            return $this->options[$name];
        }
        trigger_error('Undefined property: '.$name, E_USER_NOTICE);
        return null;
    }

    /**
     * test option name
     *
     * @param string name
     */
    public function __isset($name) {
        return isset($this->options[$name]);
    }

    /**
     * Return the name of the repository class
     * @return <type>
     */
    public function __toString() {
        return 'Repository class: '.__CLASS__;
    }

    /**
     * Download a file from a given url
     *
     * @global object $CFG
     * @param string $url the url of file
     * @param string $file save location
     * @return string the location of the file
     * @see curl package
     */
    public function get_file($url, $file = '') {
        global $CFG;
        if (!file_exists($CFG->dataroot.'/temp/download')) {
            mkdir($CFG->dataroot.'/temp/download/', 0777, true);
        }
        if(is_dir($CFG->dataroot.'/temp/download')) {
            $dir = $CFG->dataroot.'/temp/download/';
        }
        if(empty($file)) {
            $file = uniqid('repo').'_'.time().'.tmp';
        }
        if(file_exists($dir.$file)){
            $file = uniqid('m').$file;
        }
        $fp = fopen($dir.$file, 'w');
        $c = new curl;
        $c->download(array(
            array('url'=>$url, 'file'=>$fp)
        ));
        return $dir.$file;
    }

    /**
     * Print a list or return string
     *
     * @param string $list
     * @param boolean $print false, return html, otherwise, print it directly
     * @return <type>
     */
    public function print_listing($listing = array(), $print=true) {
        if(empty($listing)){
            $listing = $this->get_listing();
        }
        if (empty($listing)) {
            $str = '';
        } else {
            $count = 0;
            $str = '<table>';
            foreach ($listing as $v){
                $str .= '<tr id="entry_'.$count.'">';
                $str .= '<td><input type="checkbox" /></td>';
                $str .= '<td>'.$v['name'].'</td>';
                $str .= '<td>'.$v['size'].'</td>';
                $str .= '<td>'.$v['date'].'</td>';
                $str .= '</tr>';
                $count++;
            }
            $str .= '</table>';
        }
        if ($print){
            echo $str;
            return null;
        } else {
            return $str;
        }
    }
    public function get_name(){
        global $DB;
        // We always verify instance id from database,
        // so we always know repository name before init
        // a repository, so we don't enquery repository
        // name from database again here.
        if (isset($this->options['name'])) {
            return $this->options['name'];
        } else {
            if ( $repo = $DB->get_record('repository_instances', array('id'=>$this->id)) ) {
                return $repo->name;
            } else {
                return '';
            }
        }
    }

    /**
     * Provide repository instance information for Ajax
     * @global object $CFG
     * @return object
     */
    final public function ajax_info() {
        global $CFG;
        $repo = new stdclass;
        $repo->id   = $this->id;
        $repo->name = $this->get_name();
        $repo->type = $this->options['type'];
        $repo->icon = $CFG->wwwroot.'/repository/'.$repo->type.'/icon.png';
        return $repo;
    }

    /**
     * Create an instance for this plug-in
     * @global object $CFG
     * @global object $DB
     * @param string $type the type of the repository
     * @param integer $userid the user id
     * @param object $context the context
     * @param array $params the options for this instance
     * @return <type>
     */
    final public static function create($type, $userid, $context, $params) {
        global $CFG, $DB;
        $params = (array)$params;
        require_once($CFG->dirroot . '/repository/'. $type . '/repository.class.php');
        $classname = 'repository_' . $type;
        if ($repo = $DB->get_record('repository', array('type'=>$type))) {
            $record = new stdclass;
            $record->name = $params['name'];
            $record->typeid = $repo->id;
            $record->timecreated  = time();
            $record->timemodified = time();
            $record->contextid = $context->id;
            $record->userid    = $userid;
            $id = $DB->insert_record('repository_instances', $record);
            if (call_user_func($classname . '::has_instance_config')) {
                $configs = call_user_func($classname . '::get_instance_option_names');
                $options = array();
                foreach ($configs as $config) {
                    $options[$config] = $params[$config];
                }
            }
            if (!empty($id)) {
                unset($options['name']);
                $instance = repository_get_instance($id);
                $instance->set_option($options);
                return $id;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * delete a repository instance
     * @global object $DB
     * @return <type>
     */
    final public function delete(){
        global $DB;
        $DB->delete_records('repository_instances', array('id'=>$this->id));
        return true;
    }

    /**
     * Hide/Show a repository
     * @global object $DB
     * @param string $hide
     * @return <type>
     */
    final public function hide($hide = 'toggle'){
        global $DB;
        if ($entry = $DB->get_record('repository', array('id'=>$this->id))) {
            if ($hide === 'toggle' ) {
                if (!empty($entry->visible)) {
                    $entry->visible = 0;
                } else {
                    $entry->visible = 1;
                }
            } else {
                if (!empty($hide)) {
                    $entry->visible = 0;
                } else {
                    $entry->visible = 1;
                }
            }
            return $DB->update_record('repository', $entry);
        }
        return false;
    }

    /**
     * Cache login details for repositories
     * @global object $DB
     * @param string $username
     * @param string $password
     * @param integer $userid The id of specific user
     * @return integer Id of the record
     */
    public function store_login($username = '', $password = '', $userid = 1) {
        global $DB;

        $repository = new stdclass;
        if (!empty($this->id)) {
            $repository->id = $this->id;
        } else {
            $repository->userid         = $userid;
            $repository->repositorytype = $this->type;
            $repository->contextid      = $this->context->id;
        }
        if ($entry = $DB->get_record('repository', $repository)) {
            $repository->id = $entry->id;
            $repository->username = $username;
            $repository->password = $password;
            return $DB->update_record('repository', $repository);
        } else {
            $repository->username = $username;
            $repository->password = $password;
            return $DB->insert_record('repository', $repository);
        }
    }

    /**
     * Save settings for repository instance
     * $repo->set_option(array('api_key'=>'f2188bde132', 'name'=>'dongsheng'));
     * @global object $DB
     * @param array $options settings
     * @return int Id of the record
     */
    public function set_option($options = array()){
        global $DB;
        if (!empty($options['name'])) {
            $r = new object();
            $r->id   = $this->id;
            $r->name = $options['name'];
            $DB->update_record('repository_instances', $r);
            unset($options['name']);
        }
        foreach ($options as $name=>$value) {
            if ($id = $DB->get_field('repository_instance_config', 'id', array('name'=>$name, 'instanceid'=>$this->id))) {
                if ($value===null) {
                    return $DB->delete_records('repository_instance_config', array('name'=>$name, 'instanceid'=>$this->id));
                } else {
                    return $DB->set_field('repository_instance_config', 'value', $value, array('id'=>$id));
                }
            } else {
                if ($value===null) {
                    return true;
                }
                $config = new object();
                $config->instanceid = $this->id;
                $config->name   = $name;
                $config->value  = $value;
                return $DB->insert_record('repository_instance_config', $config);
            }
        }
        return true;
    }

    /**
     * Get settings for repository instance
     * @global object $DB
     * @param <type> $config
     * @return array Settings
     */
    public function get_option($config = ''){
        global $DB;
        $entries = $DB->get_records('repository_instance_config', array('instanceid'=>$this->id));
        $ret = array();
        if (empty($entries)) {
            return $ret;
        }
        foreach($entries as $entry){
            $ret[$entry->name] = $entry->value;
        }
        if (!empty($config)) {
            return $ret[$config];
        } else {
            return $ret;
        }
    }

    /**
     * Given a path, and perhaps a search, get a list of files.
     *
     * The format of the returned array must be:
     * array(
     *   'path' => (string) path for the current folder
     *   'dynload' => (bool) use dynamic loading,
     *   'manage' => (string) link to file manager,
     *   'nologin' => (bool) requires login,
     *   'nosearch' => (bool) no search link,
     *   'upload' => array( // upload manager
     *     'name' => (string) label of the form element,
     *     'id' => (string) id of the form element
     *   ),
     *   'list' => array(
     *     array( // file
     *       'title' => (string) file name,
     *       'date' => (string) file last modification time, usually userdate(...),
     *       'size' => (int) file size,
     *       'thumbnail' => (string) url to thumbnail for the file,
     *       'source' => plugin-dependent unique path to the file (id, url, path, etc.),
     *       'url'=> the accessible url of file
     *     ),
     *     array( // folder - same as file, but no 'source'.
     *       'title' => (string) folder name,
     *       'path' => (string) path to this folder
     *       'date' => (string) folder last modification time, usually userdate(...),
     *       'size' => 0,
     *       'thumbnail' => (string) url to thumbnail for the folder,
     *       'children' => array( // an empty folder needs to have 'children' defined, but empty.
     *         // content (files and folders)
     *       )
     *     ),
     *   )
     * )
     *
     * @param string $parent The parent path, this parameter can
     * a folder name, or a identification of folder
     * @param string $search The text will be searched.
     * @return array the list of files, including meta infomation
     */
    abstract public function get_listing($parent = '/', $search = '');


    /**
     * Show the login screen, if required
     * This is an abstract function, it must be overriden.
     */
    abstract public function print_login();

    /**
     * Show the search screen, if required
     * @return null
     */
    abstract public function print_search();

    /**
     * Defines operations that happen occasionally on cron
     * @return <type>
     */
    public function cron() {
        return true;
    }

    /**
     * Return true if the plugin type has at least one general option field
     * By default: false
     * @return boolean
     */
    public static function has_admin_config() {
        return false;
    }

    /**
     * Return true if a plugin instance has at least one config field
     * By default: false
     * @return boolean
     */
    public static function has_instance_config() {
        return false;
    }

    /**
     * Return true if the plugin can have multiple instances
     * By default: false
     * @return boolean
     */
    public static function has_multiple_instances(){
        return false;
    }

    /**
     * Return names of the general options
     * By default: no general option name
     * @return array
     */
    public static function get_admin_option_names(){
        return array();
    }

    /**
     * Return names of the instance options
     * By default: no instance option name
     * @return array
     */
    public static function get_instance_option_names(){
        return array();
    }
}

/**
 * exception class for repository api
 */
class repository_exception extends moodle_exception {
}


/**
 * Return repository instances
 * @global object $DB
 * @global object $CFG
 * @global object $USER
 * @param object $context
 * @param integer $userid
 * @param boolean $visible if visible == true, return visible instances only,
 *                otherwise, return all instances
 * @param string $type a type name to retrieve
 * @return array repository instances
 */
function repository_get_instances($context, $userid = null, $visible = true, $type=null){
    global $DB, $CFG, $USER;
    $params = array();
    $sql = 'SELECT i.*, r.type AS repositorytype, r.visible FROM {repository} r, {repository_instances} i WHERE ';
    $sql .= 'i.typeid = r.id AND ';
    if (!empty($userid) && is_numeric($userid)) {
        $sql .= ' (i.userid = 0 or i.userid = ?) AND ';
        $params[] = $userid;
    }
    if($context->id == SYSCONTEXTID) {
        $sql .= ' (i.contextid = ?)';
        $params[] = SYSCONTEXTID;
    } else {
        $sql .= ' (i.contextid = ? or i.contextid = ?)';
        $params[] = SYSCONTEXTID;
        $params[] = $context->id;
    }
    if($visible == true) {
        $sql .= ' AND (r.visible = 1)';
    }
    if(isset($type)) {
        $sql .= ' AND (r.type = ?)';
        $params[] = $type;
    }
    if(!$repos = $DB->get_records_sql($sql, $params)) {
        $repos = array();
    }
    $ret = array();
    foreach($repos as $repo) {
        require_once($CFG->dirroot . '/repository/'. $repo->repositorytype
            . '/repository.class.php');
        $options['visible'] = $repo->visible;
        $options['name']    = $repo->name;
        $options['type']    = $repo->repositorytype;
        $options['typeid']  = $repo->typeid;
        $classname = 'repository_' . $repo->repositorytype;
        $ret[] = new $classname($repo->id, $repo->contextid, $options);
    }
    return $ret;
}

/**
 * Get single repository instance
 * @global object $DB
 * @global object $CFG
 * @param integer $id repository id
 * @return object repository instance
 */
function repository_get_instance($id){
    global $DB, $CFG;
    $sql = 'SELECT i.*, r.type AS repositorytype, r.visible FROM {repository} r, {repository_instances} i WHERE ';
    $sql .= 'i.typeid = r.id AND ';
    $sql .= 'i.id = '.$id;

    if(!$instance = $DB->get_record_sql($sql)) {
        return false;
    }
    require_once($CFG->dirroot . '/repository/'. $instance->repositorytype
        . '/repository.class.php');
    $classname = 'repository_' . $instance->repositorytype;
    $options['typeid'] = $instance->typeid;
    $options['type']   = $instance->repositorytype;
    $options['name']   = $instance->name;
    return new $classname($instance->id, $instance->contextid, $options);
}

/**
 * TODO: write documentation
 * @global <type> $CFG
 * @param <type> $plugin
 * @param <type> $function
 * @param type $nocallablereturnvalue default value if function not found
 *             it's mostly used when you don't want to display an error but
 *             return a boolean
 * @return <type>
 */
function repository_static_function($plugin, $function) {
    global $CFG;

    //check that the plugin exists
    $typedirectory = $CFG->dirroot . '/repository/'. $plugin . '/repository.class.php';
        if (!file_exists($typedirectory)) {
            throw new repository_exception('invalidplugin', 'repository');
    }

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

    require_once($typedirectory);
    return call_user_func_array(array('repository_' . $plugin, $function), $args);
}

/**
 * Move file from download folder to file pool using FILE API
 * @TODO Need review
 * @global object $DB
 * @global object $CFG
 * @global object $USER
 * @param string $path file path in download folder
 * @param string $name file name
 * @param integer $itemid item id to identify a file in filepool
 * @param string $filearea file area
 * @param string $filepath filepath in file area
 * @return array information of file in file pool
 */
function repository_move_to_filepool($path, $name, $itemid, $filearea = 'user_draft') {
    global $DB, $CFG, $USER;
    $context = get_context_instance(CONTEXT_USER, $USER->id);
    $entry = new object();
    $entry->filearea  = $filearea;
    $entry->contextid = $context->id;
    $entry->filename  = $name;
    $entry->filepath  = '/'.uniqid().'/';
    $entry->timecreated  = time();
    $entry->timemodified = time();
    if(is_numeric($itemid)) {
        $entry->itemid = $itemid;
    } else {
        $entry->itemid = 0;
    }
    $entry->mimetype     = mimeinfo('type', $path);
    $entry->userid       = $USER->id;
    $fs = get_file_storage();
    $browser = get_file_browser();
    if ($file = $fs->create_file_from_pathname($entry, $path)) {
        $delete = unlink($path);
        $ret = $browser->get_file_info($context, $file->get_filearea(), $file->get_itemid(), $file->get_filepath(), $file->get_filename());
        if(!empty($ret)){
            return array('url'=>$ret->get_url(),'id'=>$file->get_itemid(), 'file'=>$file->get_filename());
        } else {
            return null;
        }
    } else {
        return null;
    }
}

/**
 * Save file to local filesystem pool
 * @param string $elname name of element
 * @param int $contextid
 * @param string $filearea
 * @param string $filepath
 * @param string $filename - use specified filename, if not specified name of uploaded file used
 * @param bool $override override file if exists
 * @param int $userid
 * @return mixed stored_file object or false if error; may throw exception if duplicate found
 */
function repository_store_to_filepool($elname, $filearea='user_draft', $filepath='/') {
    global $USER;
    if (!isset($_FILES[$elname])) {
        return false;
    }

    $filename = $_FILES[$elname]['name'];
    $context = get_context_instance(CONTEXT_USER, $USER->id);
    $itemid = (int)substr(hexdec(uniqid()), 0, 9)+rand(1,100);
    $fs = get_file_storage();
    $browser = get_file_browser();

    if ($file = $fs->get_file($context->id, $filearea, $itemid, $filepath, $filename)) {
        if ($override) {
            $file->delete();
        } else {
            return false;
        }
    }

    $file_record = new object();
    $file_record->contextid = $context->id;
    $file_record->filearea  = $filearea;
    $file_record->itemid    = $itemid;
    $file_record->filepath  = $filepath;
    $file_record->filename  = $filename;
    $file_record->userid    = $USER->id;

    $file = $fs->create_file_from_pathname($file_record, $_FILES[$elname]['tmp_name']);
    $info = $browser->get_file_info($context, $file->get_filearea(), $file->get_itemid(), $file->get_filepath(), $file->get_filename());
    $ret = array('url'=>$info->get_url(),'id'=>$itemid, 'file'=>$file->get_filename());
    return $ret;
}

/**
 * Return javascript to create file picker to browse repositories
 * @global object $CFG
 * @global object $USER
 * @param object $context the context
 * @return array
 */
function repository_get_client($context){
    global $CFG, $USER;
    $suffix = uniqid();
    $sesskey = sesskey();
    $strsaveas    = get_string('saveas', 'repository').': ';
    $stradd  = get_string('add', 'repository');
    $strback      = get_string('back', 'repository');
    $strcancel    = get_string('cancel');
    $strclose     = get_string('close', 'repository');
    $strcopying   = get_string('copying', 'repository');
    $strdownbtn   = get_string('getfile', 'repository');
    $strdownload  = get_string('downloadsucc', 'repository');
    $strdate      = get_string('date', 'repository').': ';
    $strerror     = get_string('error', 'repository');
    $strfilenotnull = get_string('filenotnull', 'repository');
    $strinvalidjson = get_string('invalidjson', 'repository');
    $strlistview  = get_string('listview', 'repository');
    $strlogout    = get_string('logout', 'repository');
    $strloading   = get_string('loading', 'repository');
    $strthumbview = get_string('thumbview', 'repository');
    $strtitle     = get_string('title', 'repository');
    $strmgr       = get_string('manageurl', 'repository');
    $strnoenter   = get_string('noenter', 'repository');
    $strsave      = get_string('save', 'repository');
    $strsaved     = get_string('saved', 'repository');
    $strsaving    = get_string('saving', 'repository');
    $strsize      = get_string('size', 'repository').': ';
    $strsync      = get_string('sync', 'repository');
    $strsearch    = get_string('search', 'repository');
    $strsearching = get_string('searching', 'repository');
    $strsubmit    = get_string('submit', 'repository');
    $strpreview   = get_string('preview', 'repository');
    $strupload    = get_string('upload', 'repository');
    $struploading = get_string('uploading', 'repository');
    $css = <<<EOD
<style type="text/css">
#list-$suffix{line-height: 1.5em}
#list-$suffix a{ padding: 3px }
#list-$suffix li a:hover{ background: gray; color:white; }
#repo-list-$suffix ul{list-style-type:none;}
#repo-list-$suffix li{margin-bottom: 1em;}
#paging-$suffix{margin:10px 5px; clear:both;}
#paging-$suffix a{padding: 4px;border: 1px solid #CCC}
#path-$suffix{margin: 4px;border-bottom: 1px dotted gray;}
#path-$suffix a{padding: 4px;}
#panel-$suffix{padding:0;margin:0; text-align:left;}
#rename-form{text-align:center}
#rename-form p{margin: 1em;}
p.upload{text-align:right;margin: 5px}
p.upload a{font-size: 14px;background: #ccc;color:black;padding: 3px}
p.upload a:hover {background: grey;color:white}
.file_name{color:green;}
.file_date{color:blue}
.file_size{color:gray}
.grid{width:80px; float:left;text-align:center;}
.grid div{width: 80px; overflow: hidden}
.grid .label{height: 36px}
.repo-opt{font-size: 10px;}
#file-picker-$suffix{
font-size:12px;
}
</style>
<style type="text/css">
@import "$CFG->wwwroot/lib/yui/resize/assets/skins/sam/resize.css";
@import "$CFG->wwwroot/lib/yui/container/assets/skins/sam/container.css";
@import "$CFG->wwwroot/lib/yui/layout/assets/skins/sam/layout.css";
@import "$CFG->wwwroot/lib/yui/button/assets/skins/sam/button.css";
@import "$CFG->wwwroot/lib/yui/assets/skins/sam/treeview.css";
</style>
EOD;

    $js = <<<EOD
<script type="text/javascript" src="$CFG->wwwroot/lib/yui/yahoo-dom-event/yahoo-dom-event.js"></script>
<script type="text/javascript" src="$CFG->wwwroot/lib/yui/element/element-beta-min.js"></script>
<script type="text/javascript" src="$CFG->wwwroot/lib/yui/treeview/treeview-min.js"></script>
<script type="text/javascript" src="$CFG->wwwroot/lib/yui/dragdrop/dragdrop-min.js"></script>
<script type="text/javascript" src="$CFG->wwwroot/lib/yui/container/container-min.js"></script>
<script type="text/javascript" src="$CFG->wwwroot/lib/yui/resize/resize-beta-min.js"></script>
<script type="text/javascript" src="$CFG->wwwroot/lib/yui/layout/layout-beta-min.js"></script>
<script type="text/javascript" src="$CFG->wwwroot/lib/yui/connection/connection-min.js"></script>
<script type="text/javascript" src="$CFG->wwwroot/lib/yui/json/json-min.js"></script>
<script type="text/javascript" src="$CFG->wwwroot/lib/yui/button/button-min.js"></script>
<script type="text/javascript" src="$CFG->wwwroot/lib/yui/selector/selector-beta-min.js"></script>
<script type="text/javascript">
//<![CDATA[
var repository_client_$suffix = (function() {
// private static field
var dver = '1.0';
// private static methods
function alert_version(){
    alert(dver);
}
function _client(){
    // public varible
    this.name = 'repository_client_$suffix';
    // private varible
    var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, layout = null, resize = null;
    var IE_QUIRKS = (YAHOO.env.ua.ie && document.compatMode == "BackCompat");
    var IE_SYNC = (YAHOO.env.ua.ie == 6 || (YAHOO.env.ua.ie == 7 && IE_QUIRKS));
    var PANEL_BODY_PADDING = (10*2);
    var btn_list = {label: '$strlistview', value: 'l', checked: true, onclick: {fn: _client.viewlist}};
    var btn_thumb = {label: '$strthumbview', value: 't', onclick: {fn: _client.viewthumb}};
    var repo_list = null;
    var resize = null;
    var panel = new YAHOO.widget.Panel('file-picker-$suffix', {
        draggable: true,
        close: true,
        modal: true,
        underlay: 'none',
        width: '510px',
        zindex: 666666,
        xy: [50, Dom.getDocumentScrollTop()+20]
    });
    // construct code section
    {
        panel.setHeader('$strtitle');
        panel.setBody('<div id="layout-$suffix"></div>');
        panel.beforeRenderEvent.subscribe(function() {
            Event.onAvailable('layout-$suffix', function() {
                layout = new YAHOO.widget.Layout('layout-$suffix', {
                    height: 400, width: 490,
                    units: [
                        {position: 'top', height: 32, resize: false,
                        body:'<div class="yui-buttongroup" id="repo-viewbar-$suffix"></div>', gutter: '2'},
                        {position: 'left', width: 150, resize: true,
                        body:'<ul id="repo-list-$suffix"></ul>', gutter: '0 5 0 2', minWidth: 150, maxWidth: 300 },
                        {position: 'center', body: '<div id="panel-$suffix"></div>',
                        scroll: true, gutter: '0 2 0 0' }
                    ]
                });
                layout.render();
            });
        });
        resize = new YAHOO.util.Resize('file-picker-$suffix', {
            handles: ['br'],
            autoRatio: true,
            status: true,
            minWidth: 380,
            minHeight: 400
        });
        resize.on('resize', function(args) {
            var panelHeight = args.height;
            var headerHeight = this.header.offsetHeight; // Content + Padding + Border
            var bodyHeight = (panelHeight - headerHeight);
            var bodyContentHeight = (IE_QUIRKS) ? bodyHeight : bodyHeight - PANEL_BODY_PADDING;
            Dom.setStyle(this.body, 'height', bodyContentHeight + 'px');
            if (IE_SYNC) {
                this.sizeUnderlay();
                this.syncIframe();
            }
            layout.set('height', bodyContentHeight);
            layout.set('width', (args.width - PANEL_BODY_PADDING));
            layout.resize();

        }, panel, true);
        _client.viewbar = new YAHOO.widget.ButtonGroup({
            id: 'btngroup-$suffix',
            name: 'buttons',
            disabled: true,
            container: 'repo-viewbar-$suffix'
            });
    }
    // public method
    this.show = function(){
        panel.show();
    }
    this.hide = function(){
        panel.hide();
    }
    this.create_picker = function(){
        // display UI
        panel.render();
        _client.viewbar.addButtons([btn_list, btn_thumb]);
        // init repository list
        repo_list = new YAHOO.util.Element('repo-list-$suffix');
        repo_list.on('contentReady', function(e){
            for(var i=0; i<_client.repos.length; i++) {
                var repo = _client.repos[i];
                var li = document.createElement('li');
                li.id = 'repo-$suffix-'+repo.id;
                var icon = document.createElement('img');
                icon.src = repo.icon;
                icon.width = '16';
                icon.height = '16';
                li.appendChild(icon);
                var link = document.createElement('a');
                link.href = '###';
                link.id = 'repo-call-$suffix-'+repo.id;
                link.innerHTML = ' '+repo.name;
                link.className = 'repo-name';
                link.onclick = function(){
                    var re = /repo-call-$suffix-(\d+)/i;
                    var id = this.id.match(re);
                    repository_client_$suffix.req(id[1], '', 0);
                }
                li.appendChild(link);
                var opt = document.createElement('div');
                opt.id = 'repo-opt-$suffix-'+repo.id;
                li.appendChild(opt);
                this.appendChild(li);
                repo = null;
            }
            });
    }
}

// public static varible
_client.repos = [];
_client.repositoryid = 0;
// _client.ds save all data received from server side
_client.ds = null;
_client.viewmode = 0;
_client.viewbar =null;

// public static mehtod
_client.postdata = function(obj) {
    var str = '';
    for(k in obj) {
        if(obj[k] instanceof Array) {
            for(i in obj[k]) {
                str += (encodeURIComponent(k) +'[]='+encodeURIComponent(obj[k][i]));
                str += '&';
            }
        } else {
            str += encodeURIComponent(k) +'='+encodeURIComponent(obj[k]);
            str += '&';
        }
    }
    return str;
}
_client.loading = function(type, name){
    var panel = new YAHOO.util.Element('panel-$suffix');
    panel.get('element').innerHTML = '';
    var content = document.createElement('div');
    content.style.textAlign='center';
    var para = document.createElement('P');
    var img = document.createElement('IMG');
    if(type=='load'){
    img.src = '$CFG->pixpath/i/loading.gif';
    para.innerHTML = '$strloading';
    }else{
    img.src = '$CFG->pixpath/i/progressbar.gif';
    para.innerHTML = '$strcopying '+name;
    }
    content.appendChild(para);
    content.appendChild(img);
    //content.innerHTML = '';
    panel.get('element').appendChild(content);
}
_client.rename = function(oldname, url, icon){
    var panel = new YAHOO.util.Element('panel-$suffix');
    var html = '<div id="rename-form">';
    html += '<p><img src="'+icon+'" /></p>';
    html += '<p><label for="newname-$suffix">$strsaveas</label>';
    html += '<input type="text" id="newname-$suffix" value="'+oldname+'" /></p>';
    /**
    html += '<p><label for="syncfile-$suffix">$strsync</label> ';
    html += '<input type="checkbox" id="syncfile-$suffix" /></p>';
    */
    html += '<p><input type="hidden" id="fileurl-$suffix" value="'+url+'" />';
    html += '<a href="###" onclick="repository_client_$suffix.viewfiles()">$strback</a> ';
    html += '<input type="button" onclick="repository_client_$suffix.download()" value="$strdownbtn" />';
    html += '<input type="button" onclick="repository_client_$suffix.hide()" value="$strcancel" /></p>';
    html += '</div>';
    panel.get('element').innerHTML = html;
}
_client.print_login = function(){
    var panel = new YAHOO.util.Element('panel-$suffix');
    var data = _client.ds.login;
    var str = '';
    for(var k in data){
        str += '<p>';
        var lable_id = '';
        var field_id = '';
        var field_value = '';
        if(data[k].id){
            lable_id = ' for="'+data[k].id+'"';
            field_id = ' id="'+data[k].id+'"';
        }
        if (data[k].label) {
            str += '<label'+lable_id+'>'+data[k].label+'</label><br/>';
        }
        if(data[k].value){
            field_value = ' value="'+data[k].value+'"';
        }
        str += '<input type="'+data[k].type+'"'+' name="'+data[k].name+'"'+field_id+field_value+' />';
        str += '</p>';
    }
    str += '<p><input type="button" onclick="repository_client_$suffix.login()" value="$strsubmit" /></p>';
    panel.get('element').innerHTML = str;
}

_client.viewfiles = function(){
    if(_client.viewmode) {
        _client.viewthumb();
    } else {
        _client.viewlist();
    }
}
_client.navbar = function(){
    var panel = new YAHOO.util.Element('panel-$suffix');
    panel.get('element').innerHTML = _client.uploadcontrol();
    panel.get('element').innerHTML += _client.makepage();
    _client.makepath();
}
// TODO
// Improve CSS
_client.viewthumb = function(ds){
    _client.viewmode = 1;
    var panel = new YAHOO.util.Element('panel-$suffix');
    _client.viewbar.check(1);
    var list = null;
    var args = arguments.length;
    if(args == 1){
        list = ds;
    } else {
        // from button
        list = _client.ds.list;
    }
    _client.navbar();
    var count = 0;
    for(k in list){
        var el = document.createElement('div');
        el.className='grid';
        var frame = document.createElement('DIV');
        frame.style.textAlign='center';
        var img = document.createElement('img');
        img.src = list[k].thumbnail;
        var link = document.createElement('A');
        link.href='###';
        link.id = 'img-id-'+String(count);
        link.appendChild(img);
        frame.appendChild(link);
        var title = document.createElement('div');
        if(list[k].children){
            title.innerHTML = '<i><u>'+list[k].title+'</i></u>';
        } else {
            if(list[k].url)
                title.innerHTML = '<p><a target="_blank" href="'+list[k].url+'">$strpreview</a></p>';
            title.innerHTML += list[k].title;
        }
        title.className = 'label';
        el.appendChild(frame);
        el.appendChild(title);
        panel.get('element').appendChild(el);
        if(list[k].children){
            var el = new YAHOO.util.Element(link.id);
            el.ds = list[k].children;
            el.on('click', function(){
                if(_client.ds.dynload){
                    // TODO: get file list dymanically
                }else{
                    _client.viewthumb(this.ds);
                }
            });
        } else {
            var el = new YAHOO.util.Element(link.id);
            el.title = list[k].title;
            el.value = list[k].source;
            el.icon  = list[k].thumbnail;
            el.on('click', function(){
                repository_client_$suffix.rename(this.title, this.value, this.icon);
            });
        }
        count++;
    }
}
_client.buildtree = function(node, level){
    if(node.children){
        node.title = '<i><u>'+node.title+'</u></i>';
    }
    var info = {label:node.title, title:"$strdate"+node.date+' '+'$strsize'+node.size};
    var tmpNode = new YAHOO.widget.TextNode(info, level, false);
    var tooltip = new YAHOO.widget.Tooltip(tmpNode.labelElId, {
        context:tmpNode.labelElId, text:info.title});
    tmpNode.filename = node.title;
    tmpNode.value  = node.source;
    tmpNode.icon = node.thumbnail;
    if(node.children){
        tmpNode.isLeaf = false;
        if (node.path) {
            tmpNode.path = node.path;
        } else {
            tmpNode.path = '';
        }
        for(var c in node.children){
            _client.buildtree(node.children[c], tmpNode);
        }
    } else {
        tmpNode.isLeaf = true;
        tmpNode.onLabelClick = function() {
            repository_client_$suffix.rename(this.filename, this.value, this.icon);
        }
    }
}
_client.dynload = function (node, fnLoadComplete){
    var callback = {
        success: function(o) {
            try {
                var json = YAHOO.lang.JSON.parse(o.responseText);
            } catch(e) {
                alert('$strinvalidjson - '+o.responseText);
            }
            for(k in json.list){
                _client.buildtree(json.list[k], node);
            }
            o.argument.fnLoadComplete();
        },
        failure:function(oResponse){
            alert('$strerror');
            oResponse.argument.fnLoadComplete();
        },
        argument:{"node":node, "fnLoadComplete": fnLoadComplete},
        timeout:600
    }
    var params = [];
    params['p']=node.path;
    params['env']=_client.env;
    params['sesskey']='$sesskey';
    params['ctx_id']=$context->id;
    params['repo_id']=_client.repositoryid;
    var trans = YAHOO.util.Connect.asyncRequest('POST',
        '$CFG->wwwroot/repository/ws.php?action=list', callback, _client.postdata(params));
}
_client.viewlist = function(){
    _client.viewmode = 0;
    var panel = new YAHOO.util.Element('panel-$suffix');
    _client.viewbar.check(0);
    list = _client.ds.list;
    _client.navbar();
    panel.get('element').innerHTML += '<div id="treediv"></div>';
    var tree = new YAHOO.widget.TreeView('treediv');
    if(_client.ds.dynload) {
        tree.setDynamicLoad(_client.dynload, 1);
    } else {
    }
    for(k in list){
        _client.buildtree(list[k], tree.getRoot());
    }
    tree.draw();
}
_client.upload = function(){
    var u = _client.ds.upload;
    var aform = document.getElementById(u.id);
    var parent = document.getElementById(u.id+'_div');
    var d = document.getElementById(_client.ds.upload.id+'-file');
    if(d.value!='' && d.value!=null){
        var container = document.createElement('DIV');
        container.id = u.id+'_loading';
        container.style.textAlign='center';
        var img = document.createElement('IMG');
        img.src = '$CFG->pixpath/i/progressbar.gif';
        var para = document.createElement('p');
        para.innerHTML = '$struploading';
        container.appendChild(para);
        container.appendChild(img);
        parent.appendChild(container);
        YAHOO.util.Connect.setForm(aform, true, true);
        var trans = YAHOO.util.Connect.asyncRequest('POST',
            '$CFG->wwwroot/repository/ws.php?action=upload&sesskey=$sesskey&ctx_id=$context->id&repo_id='
                +_client.repositoryid,
            _client.upload_cb);
    }else{
        alert('$strfilenotnull');
    }
}
_client.upload_cb = {
    upload: function(o){
        try {
            var ret = YAHOO.lang.JSON.parse(o.responseText);
        } catch(e) {
            alert('$strinvalidjson - '+o.responseText);
        }
        if(ret && ret.e){
            var panel = new YAHOO.util.Element('panel-$suffix');
            panel.get('element').innerHTML = ret.e;
            return;
        }
        if(ret){
            alert('$strsaved');
            repository_client_$suffix.end(ret);
        }else{
            alert('$strinvalidjson');
        }
    }
}
_client.uploadcontrol = function() {
    var str = '';
    if(_client.ds.upload){
        str += '<div id="'+_client.ds.upload.id+'_div">';
        str += '<form id="'+_client.ds.upload.id+'" onsubmit="return false">';
        str += '<label for="'+_client.ds.upload.id+'-file">'+_client.ds.upload.label+'</label>';
        str += '<input type="file" id="'+_client.ds.upload.id+'-file" name="repo_upload_file" />';
        str += '<p class="upload"><a href="###" onclick="return repository_client_$suffix.upload();">$strupload</a></p>';
        str += '</form>';
        str += '</div>';
    }
    return str;
}
_client.makepage = function(){
    var str = '';
    if(_client.ds.pages){
        str += '<div id="paging-$suffix">';
        for(var i = 1; i <= _client.ds.pages; i++) {
            str += '<a onclick="repository_client_$suffix.req('+_client.repositoryid+', '+i+', 0)" href="###">';
            str += String(i);
            str += '</a> ';
        }
        str += '</div>';
    }
    return str;
}
_client.makepath = function(){
    if(_client.viewmode == 0) {
        return;
    }
    var panel = new YAHOO.util.Element('panel-$suffix');
    var p = _client.ds.path;
    if(p && p.length!=0){
        var oDiv = document.createElement('DIV');
        oDiv.id = "path-$suffix";
        panel.get('element').appendChild(oDiv);
        for(var i = 0; i < _client.ds.path.length; i++) {
            var link = document.createElement('A');
            link.href = "###";
            link.innerHTML = _client.ds.path[i].name;
            link.id = 'path-'+i+'-el';
            var sep = document.createElement('SPAN');
            sep.innerHTML = '/';
            oDiv.appendChild(link);
            oDiv.appendChild(sep);
            var el = new YAHOO.util.Element(link.id);
            el.id = _client.repositoryid;
            el.path = _client.ds.path[i].path;
            el.on('click', function(){
                repository_client_$suffix.req(this.id, this.path, 0);
            });
        }
    }
}
// send download request
_client.download = function(){
    var title = document.getElementById('newname-$suffix').value;
    var file = document.getElementById('fileurl-$suffix').value;
    _client.loading('download', title);
    var params = [];
    params['env']=_client.env;
    params['file']=file;
    params['title']=title;
    params['sesskey']='$sesskey';
    params['ctx_id']=$context->id;
    params['repo_id']=_client.repositoryid;
    var trans = YAHOO.util.Connect.asyncRequest('POST',
        '$CFG->wwwroot/repository/ws.php?action=download', _client.dlfile, _client.postdata(params));
}
// send login request
_client.login = function(){
    var params = [];
    var data = _client.ds.login;
    for (var k in data) {
        var el = document.getElementsByName(data[k].name)[0];
        params[data[k].name] = '';
        if(el.type == 'checkbox') {
            params[data[k].name] = el.checked;
        } else {
            params[data[k].name] = el.value;
        }
    }
    params['env'] = _client.env;
    params['ctx_id'] = $context->id;
    params['sesskey']= '$sesskey';
    _client.loading('load');
    var trans = YAHOO.util.Connect.asyncRequest('POST',
        '$CFG->wwwroot/repository/ws.php?action=sign', _client.callback, _client.postdata(params));
}
_client.end = function(str){
    if(_client.env=='form'){
        _client.target.value = str['id'];
    }else{
        _client.target.value = str['url'];
    }
    _client.formcallback(str['file']);
    _client.instance.hide();
    _client.viewfiles();
}
_client.hide = function(){
    _client.instance.hide();
    _client.viewfiles();
}
_client.callback = {
    success: function(o) {
        var panel = new YAHOO.util.Element('panel-$suffix');
        try {
            var ret = YAHOO.lang.JSON.parse(o.responseText);
        } catch(e) {
            alert('$strinvalidjson - '+o.responseText);
        };
        if(ret && ret.e){
            panel.get('element').innerHTML = ret.e;
            return;
        }
        _client.ds = ret;
        var oDiv = document.getElementById('repo-opt-$suffix-'
            +_client.repositoryid);
        oDiv.innerHTML = '';
        var search = null;
        var logout = null;
        var mgr = null;
        if(_client.ds && _client.ds.login){
            _client.print_login();
        } else if(_client.ds.list) {
            if(_client.viewmode) {
                _client.viewthumb();
            } else {
                _client.viewlist();
            }
            search = document.createElement('a');
            search.href = '###';
            search.innerHTML = '$strsearch ';
            search.id = 'repo-search-$suffix-'+_client.repositoryid;
            search.onclick = function() {
                var re = /repo-search-$suffix-(\d+)/i;
                var id = this.id.match(re);
                repository_client_$suffix.search(id[1]);
            }
            logout = document.createElement('a');
            logout.href = '###';
            logout.innerHTML = '$strlogout';
            logout.id = 'repo-logout-$suffix-'+_client.repositoryid;
            logout.onclick = function() {
                var re = /repo-logout-$suffix-(\d+)/i;
                var id = this.id.match(re);
                var oDiv = document.getElementById('repo-opt-$suffix-'+id[1]);
                oDiv.innerHTML = '';
                _client.ds = null;
                repository_client_$suffix.req(id[1], 1, 1);
            }
            if(_client.ds.manage){
                mgr = document.createElement('A');
                mgr.innerHTML = '$strmgr ';
                mgr.href = _client.ds.manage;
                mgr.id = 'repo-mgr-$suffix-'+_client.repositoryid;
                mgr.target = "_blank";
            }
            if(_client.ds.nosearch != true){
                oDiv.appendChild(search);
            }
            if(mgr != null) {
                oDiv.appendChild(mgr);
            }
            if(_client.ds.nologin != true){
                oDiv.appendChild(logout);
            }
        }
    }
}
_client.dlfile = {
    success: function(o) {
        var panel = new YAHOO.util.Element('panel-$suffix');
        try {
            var ret = YAHOO.lang.JSON.parse(o.responseText);
        } catch(e) {
            alert('$strinvalidjson - '+o.responseText);
        }
        if(ret && ret.e){
            panel.get('element').innerHTML = ret.e;
            return;
        }
        if(ret){
            repository_client_$suffix.end(ret);
        }else{
            alert('$strinvalidjson');
        }
    }
}
// request file list or login
_client.req = function(id, path, reset) {
    _client.viewbar.set('disabled', false);
    _client.loading('load');
    _client.repositoryid = id;
    if (reset == 1) {
        action = 'logout';
    } else {
        action = 'list';
    }
    var params = [];
    params['p'] = path;
    params['reset']=reset;
    params['env']=_client.env;
    params['action']=action;
    params['sesskey']='$sesskey';
    params['ctx_id']=$context->id;
    params['repo_id']=id;
    var trans = YAHOO.util.Connect.asyncRequest('POST', '$CFG->wwwroot/repository/ws.php?action='+action, _client.callback, _client.postdata(params));
}
_client.search = function(id){
    var data = window.prompt("$strsearching");
    if(data == '') {
        alert('$strnoenter');
        return;
    }
    _client.viewbar.set('disabled', false);
    _client.loading('load');
    var params = [];
    params['s']=data;
    params['env']=_client.env;
    params['sesskey']='$sesskey';
    params['ctx_id']=$context->id;
    params['repo_id']=id;
    var trans = YAHOO.util.Connect.asyncRequest('POST', '$CFG->wwwroot/repository/ws.php?action=search', _client.callback, _client.postdata(params));
}

return _client;
})();
EOD;

    $repos = repository_get_instances($context);
    foreach($repos as $repo) {
        $js .= "\r\n";
        $js .= 'repository_client_'.$suffix.'.repos.push('.json_encode($repo->ajax_info()).');'."\n";
    }
    $js .= "\r\n";

    $js .= <<<EOD
function openpicker_$suffix(params) {
    if(!repository_client_$suffix.instance) {
        repository_client_$suffix.env = params.env;
        repository_client_$suffix.target = params.target;
        if(params.type){
            repository_client_$suffix.filetype = params.filetype;
        } else {
            repository_client_$suffix.filetype = 'all';
        }
        repository_client_$suffix.instance = new repository_client_$suffix();
        repository_client_$suffix.instance.create_picker();
        if(params.callback){
            repository_client_$suffix.formcallback = params.callback;
        } else {
            repository_client_$suffix.formcallback = function(){};
        }
    } else {
        repository_client_$suffix.instance.show();
    }
}
//]]>
</script>
EOD;
    return array('css'=>$css, 'js'=>$js, 'suffix'=>$suffix);
}

/**
 * TODO: write comment
 */
final class repository_instance_form extends moodleform {
    protected $instance;
    protected $plugin;

    /**
     * TODO: write comment
     * @global <type> $CFG
     */
    public function definition() {
        global $CFG;
        // type of plugin, string
        $this->plugin = $this->_customdata['plugin'];
        $this->typeid = $this->_customdata['typeid'];
        $this->instance = (isset($this->_customdata['instance'])
                && is_subclass_of($this->_customdata['instance'], 'repository'))
            ? $this->_customdata['instance'] : null;

        $mform =& $this->_form;
        $strrequired = get_string('required');

        $mform->addElement('hidden', 'edit',  ($this->instance) ? $this->instance->id : 0);
        $mform->addElement('hidden', 'new',   $this->plugin);
        $mform->addElement('hidden', 'plugin', $this->plugin);
        $mform->addElement('hidden', 'typeid', $this->typeid);

        $mform->addElement('text', 'name', get_string('name'), 'maxlength="100" size="30"');
        $mform->addRule('name', $strrequired, 'required', null, 'client');

        // let the plugin add the fields they want (either statically or not)
        if (repository_static_function($this->plugin, 'has_instance_config')) {
            if (!$this->instance) {
                $result = repository_static_function($this->plugin, 'instance_config_form', $mform);
            } else {
                $result = $this->instance->instance_config_form($mform);
            }
        }

        // and set the data if we have some.
        //var_dump($this);
        if ($this->instance) {
            $data = array();
            $data['name'] = $this->instance->name;
            foreach ($this->instance->get_instance_option_names() as $config) {
                if (!empty($this->instance->$config)) {
                    $data[$config] = $this->instance->$config;
                } else {
                    $data[$config] = '';
                }
            }
            $this->set_data($data);
        }
        $this->add_action_buttons(true, get_string('submit'));
    }

    /**
     * TODO: write comment
     * @global <type> $DB
     * @param <type> $data
     * @return <type>
     */
    public function validation($data) {
        global $DB;

        $errors = array();
        if ($DB->count_records('repository_instances', array('name' => $data['name'], 'typeid' => $data['typeid'])) > 1) {
            $errors = array('name' => get_string('err_uniquename', 'repository'));
        }

        $pluginerrors = array();
        if ($this->instance) {
            //$pluginerrors = $this->instance->admin_config_validation($data);
        } else {
            //$pluginerrors = repository_static_function($this->plugin, 'admin_config_validation', $data);
        }
        if (is_array($pluginerrors)) {
            $errors = array_merge($errors, $pluginerrors);
        }
        return $errors;
    }
}


/**
 * Display a form with the general option fields of a type
 */
final class repository_admin_form extends moodleform {
    protected $instance;
    protected $plugin;

    /**
     * Definition of the moodleform
     * @global object $CFG
     */
    public function definition() {
        global $CFG;
        // type of plugin, string
        $this->plugin = $this->_customdata['plugin'];
        $this->instance = (isset($this->_customdata['instance'])
                && is_a($this->_customdata['instance'], 'repository_type'))
            ? $this->_customdata['instance'] : null;

        $mform =& $this->_form;
        $strrequired = get_string('required');

        $mform->addElement('hidden', 'edit',  ($this->instance) ? $this->instance->get_typename() : 0);
        $mform->addElement('hidden', 'new',   $this->plugin);
        $mform->addElement('hidden', 'plugin', $this->plugin);

        // let the plugin add the fields they want (either statically or not)
        if (repository_static_function($this->plugin, 'has_admin_config')) {
            if (!$this->instance) {
                $result = repository_static_function($this->plugin, 'admin_config_form', $mform);
            } else {
                  $classname = 'repository_' . $this->instance->get_typename();
                  $result = call_user_func(array($classname,'admin_config_form'),$mform);
            }
        }

        // and set the data if we have some.
        if ($this->instance) {
            $data = array();
            $option_names = call_user_func(array($classname,'get_admin_option_names'));
            $instanceoptions = $this->instance->get_options();
            foreach ($option_names as $config) {
                if (!empty($instanceoptions[$config])) {
                    $data[$config] = $instanceoptions[$config];
                } else {
                    $data[$config] = '';
                }
            }
            $this->set_data($data);
        }

        $this->add_action_buttons(true, get_string('submit'));
    }

}


/**
 * Display a repository instance list (with edit/delete/create links)
 * @global object $CFG
 * @global object $USER
 * @param object $context the context for which we display the instance
 * @param boolean $admin if true, so the form is been called by an administration
 *                       page, only one type would be displayed
 * @param string $typename if set, we display only one type of instance
 */
function repository_display_instances_list($context, $admin = false, $typename = null){
       global $CFG, $USER;
        if ($admin) {
            $baseurl = $CFG->wwwroot . '/admin/repositoryinstance.php?sesskey=' . sesskey();
            $type = repository_get_type_by_typename($typename);

        }
        $output = print_simple_box_start(true);
        $output .= "<div ><h2 style='text-align: center'>" . get_string('instances', 'repository') . "</h2></div>";

        $namestr = get_string('name');
        $pluginstr = get_string('plugin', 'repository');
        $stropt = get_string('operation', 'repository');
        $updown = get_string('updown', 'repository');
        $plugins = get_list_of_plugins('repository');
        $instances = repository_get_instances($context,null,true,$typename);
        $instancesnumber = count($instances);
        $alreadyplugins = array();
        $table = new StdClass;
        $table->head = array($namestr, $pluginstr, $stropt);
        $table->align = array('left', 'left', 'center');
        $table->data = array();
        $updowncount=1;
        foreach ($instances as $i) {
            $row = '';
            $row .= '<a href="' . $baseurl . '&amp;type='.$typename.'&amp;edit=' . $i->id . '"><img src="' . $CFG->pixpath . '/t/edit.gif" alt="' . get_string('edit') . '" /></a>' . "\n";
            $row .= '<a href="' . $baseurl . '&amp;type='.$typename.'&amp;delete=' .  $i->id . '"><img src="' . $CFG->pixpath . '/t/delete.gif" alt="' . get_string('delete') . '" /></a>' . "\n";
            //$row .= ' <a href="' . $baseurl . '&amp;type='.$typename.'&amp;hide=' . $i->id . '"><img src="' . $CFG->pixpath . '/t/' . ($i->visible ? 'hide' : 'show') . '.gif" alt="' . get_string($i->visible ? 'hide' : 'show') . '" /></a>' . "\n";

            $table->data[] = array($i->name, $type->get_readablename(),$row);
            if (!in_array($i->name, $alreadyplugins)) {
                $alreadyplugins[] = $i->name;
            }
        }
        $output .= print_table($table, true);
        $instancehtml = '<div><h3>';
        $addable = 0;
        $instancehtml .= get_string('createrepository', 'repository');
        $instancehtml .= '</h3><ul>';
        $addable = 0;

        //if no type is set, we can create all type of instance
        if (!$typename) {
            foreach ($plugins as $p) {
                if (!in_array($p, $alreadyplugins)) {
                   if (repository_static_function($p->get_typename(), 'has_multiple_instances')){
                        $instancehtml .= '<li><a href="'.$baseurl.'&amp;new='.$p.'">'.get_string('create', 'repository')
                            .' "'.get_string('repositoryname', 'repository_'.$p).'" '
                            .get_string('instance', 'repository').'</a></li>';
                        $addable++;
                    }
                }
            }
        }
        //create a unique type of instance
        else {
            if (repository_static_function($typename, 'has_multiple_instances')){
                $addable = 1;
                $instancehtml .= '<li><a href="'.$baseurl.'&amp;new='.$typename.'">'.get_string('create', 'repository')
                                  .' "'.get_string('repositoryname', 'repository_'.$typename).'" '
                                  .get_string('instance', 'repository').'</a></li>';
            }
        }

        $instancehtml .= '</ul>';

        if ($addable) {
            $instancehtml .= '</div>';
            $output .= $instancehtml;
        }

        $output .= print_simple_box_end(true);

        //print the list + creation links
        print($output);

}