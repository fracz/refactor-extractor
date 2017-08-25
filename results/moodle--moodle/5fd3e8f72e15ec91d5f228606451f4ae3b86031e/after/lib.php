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
 * See an example of use of this library in repository/box/repository.class.php
 *
 * A few notes :
 *   // options are stored as serialized format in database
 *   $options = array('api_key'=>'dmls97d8j3i9tn7av8y71m9eb55vrtj4',
 *                  'auth_token'=>'', 'path_root'=>'/');
 *   $repo    = new repository_xxx($options);
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

abstract class repository {
    public $id;
    public $context;
    public $options;

    /**
     * Take an array as a parameter, which contains necessary information
     * of repository.
     *
     * @param string $parent The parent path, this parameter must
     * not be the folder name, it may be a identification of folder
     * @param string $search The text will be searched.
     * @return array the list of files, including meta infomation
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
    }

    public function __set($name, $value) {
        $this->options[$name] = $value;
    }

    public function __get($name) {
        if (array_key_exists($name, $this->options)){
            return $this->options[$name];
        }
        trigger_error('Undefined property: '.$name, E_USER_NOTICE);
        return null;
    }

    public function __isset($name) {
        return isset($this->options[$name]);
    }

    public function __toString() {
        return 'Repository class: '.__CLASS__;
    }

    /**
     * Given a URL, get a file from there.
     * @param string $url the url of file
     * @param string $file save location
     * @see curl package
     */
    public function get_file($url, $file = '') {
        global $CFG;
        if (!file_exists($CFG->dataroot.'/repository/download')) {
            mkdir($CFG->dataroot.'/repository/download/', 0777, true);
        }
        if(is_dir($CFG->dataroot.'/repository/download')) {
            $dir = $CFG->dataroot.'/repository/download/';
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
     * $list = array(
     *            array('title'=>'moodle.txt', 'size'=>12, 'source'=>'url', 'date'=>''),
     *            array('title'=>'repository.txt', 'size'=>32, 'source'=>'', 'date'=>''),
     *            array('title'=>'forum.txt', 'size'=>82, 'source'=>'', 'date'=>''),
     *         );
     *
     * @param boolean $print if printing the listing directly
     *
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

    /**
     * Return data for creating ajax request
     * @return object
     */
    final public function ajax_info() {
        global $CFG;
        $repo = new stdclass;
        $repo->name = $this->options['name'];
        $repo->type = $this->options['type'];
        $repo->id   = $this->options['id'];
        $repo->icon = $CFG->wwwroot.'/repository/'.$repo->type.'/icon.png';
        return $repo;
    }

    /**
     * Create an instance for this plug-in
     * @param string the type of the repository
     * @param int userid
     * @param object context
     * @param array the options for this instance
     *
     */
    final public static function create($type, $userid, $context, $params) {
        global $CFG, $DB;
        $params = (array)$params;
        require_once($CFG->dirroot . '/repository/'. $type . '/repository.class.php');
        $classname = 'repository_' . $type;
        $record = new stdclass;
        if (call_user_func($classname . '::has_admin_config')) {
            $configs = call_user_func($classname . '::get_option_names');
            $options = array();
            foreach ($configs as $config) {
                $options[$config] = $params[$config];
            }
            $record->data1 = serialize($options);
        }
        $record->repositoryname = $params['name'];
        $record->repositorytype = $type;
        $record->timecreated  = time();
        $record->timemodified = time();
        $record->contextid = $context->id;
        $record->visible   = 1;
        $record->userid    = $userid;
        $id = $DB->insert_record('repository', $record);
        if (!empty($id)) {
            return $id;
        } else {
            return null;
        }
    }
    /**
     * delete a repository instance
     */
    final public function delete(){
        global $DB;
        $DB->delete_records('repository', array('id'=>$this->id));
        return true;
    }
    /**
     * Hide/Show a repository
     * @param boolean
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
     *
     * @param string $username
     * @param string $password
     * @param string $userid The id of specific user
     * @return int Id of the record
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
        return false;
    }

    /**
     * Save settings for repository instance
     * $repo->set_option(array('api_key'=>'f2188bde132',
     *                          'name'=>'dongsheng'));
     *
     * @param array settings
     * @return int Id of the record
     */
    public function set_option($options = array()){
        global $DB;
        if (is_array($options)) {
            $options = array_merge($this->get_option(), $options);
        } else {
            $options = $this->get_option();
        }
        $repository = new stdclass;
        $position = 1;
        $options   = serialize($options);
        if ($entry = $DB->get_record('repository', array('id'=>$this->id))) {
            $field = 'data'.$position;
            $repository->id = $entry->id;
            $repository->$field = $options;
            return $DB->update_record('repository', $repository);
        }
        return false;
    }

    /**
     * Get settings for repository instance
     *
     * @param int repository Id
     * @return array Settings
     */
    public function get_option($config = ''){
        global $DB;
        $entry = $DB->get_record('repository', array('id'=>$this->id));
        if (!empty($entry->visible)) {
            $ret['visible'] = 1;
        } else {
            $ret['visible'] = 0;
        }
        $ret['type'] = $entry->repositorytype;
        $ret['name'] = $entry->repositoryname;
        $ret['id']   = $entry->id;
        for ($i=1;$i<6;$i++) {
            $field = 'data'.$i;
            $data = unserialize($entry->$field);
            if (!empty($data)) {
                if (is_array($data)) {
                    $ret = array_merge($ret, $data);
                }
            }
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
     * @param string $parent The parent path, this parameter can
     * a folder name, or a identification of folder
     * @param string $search The text will be searched.
     * @return array the list of files, including meta infomation
     */
    abstract public function get_listing($parent = '/', $search = '');


    /**
     * Show the login screen, if required
     * This is an abstract function, it must be overriden.
     *
     */
    abstract public function print_login();

    /**
     * Show the search screen, if required
     *
     * @return null
     */
    abstract public function print_search();

    public static function has_admin_config() {
        return false;
    }


    /**
     * Defines operations that happen occasionally on cron
     *
     */
    public function cron() {
        return true;
    }
}

/**
 * exception class for repository api
 *
 */

class repository_exception extends moodle_exception {
}


/**
 * Return repository instances
 *
 * @param object context
 * @param int userid
 * @param boolean if visible == true, return visible instances only,
 *                otherwise, return all instances
 * @return array repository instances
 */
function repository_instances($context, $userid = null, $visible = true){
    global $DB, $CFG, $USER;
    $params = array();
    $sql = 'SELECT * FROM {repository} r WHERE ';
    if (!empty($userid) && is_numeric($userid)) {
        $sql .= ' (r.userid = 0 or r.userid = ?) AND ';
        $params[] = $userid;
    }
    if($context->id == SYSCONTEXTID) {
        $sql .= ' (r.contextid = ?)';
        $params[] = SYSCONTEXTID;
    } else {
        $sql .= ' (r.contextid = ? or r.contextid = ?)';
        $params[] = SYSCONTEXTID;
        $params[] = $context->id;
    }
    if($visible == true) {
        $sql .= ' AND (r.visible = 1)';
    }
    if(!$repos = $DB->get_records_sql($sql, $params)) {
        $repos = array();
    }
    $ret = array();
    foreach($repos as $repo) {
        require_once($CFG->dirroot . '/repository/'. $repo->repositorytype
            . '/repository.class.php');
        $classname = 'repository_' . $repo->repositorytype;
        $ret[] = new $classname($repo->id, $repo->contextid);
    }
    return $ret;
}

/**
 * Get single repository instance
 *
 * @param int repository id
 * @return object repository instance
 */
function repository_instance($id){
    global $DB, $CFG;

    if (!$instance = $DB->get_record('repository', array('id' => $id))) {
        return false;
    }
    require_once($CFG->dirroot . '/repository/'. $instance->repositorytype
        . '/repository.class.php');
    $classname = 'repository_' . $instance->repositorytype;
    return new $classname($instance->id, $instance->contextid);
}

function repository_static_function($plugin, $function) {
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

    require_once($CFG->dirroot . '/repository/' . $plugin .  '/repository.class.php');
    return call_user_func_array(array('repository_' . $plugin, $function), $args);
}

/**
 * Move file from download folder to file pool using FILE API
 * @TODO Need review
 *
 * @param string file path in download folder
 * @param string file name
 * @param int itemid to identify a file in filepool
 * @return array information of file in file pool
 */
function move_to_filepool($path, $name, $itemid) {
    global $DB, $CFG, $USER;
    $context = get_context_instance(CONTEXT_USER, $USER->id);
    $entry = new object();
    $entry->filearea  = 'user_draft';
    $entry->contextid = $context->id;
    $entry->filename  = $name;
    $entry->filepath  = '/';
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
        $ret = $browser->get_file_info($context, $file->get_filearea(), $file->get_itemid(), $file->get_filepath(), $file->get_filename());
        return array('url'=>$ret->get_url(),'id'=>$file->get_itemid());
    } else {
        return null;
    }
}

/**
 * Return javascript to create file picker to browse repositories
 *
 * @param object context
 * @return array
 */
function get_repository_client($context){
    global $CFG, $USER;
    $suffix = uniqid();
    $strsubmit    = get_string('submit', 'repository');
    $strlistview  = get_string('listview', 'repository');
    $strthumbview = get_string('thumbview', 'repository');
    $strsearch    = get_string('search', 'repository');
    $strlogout    = get_string('logout', 'repository');
    $strloading   = get_string('loading', 'repository');
    $strtitle     = get_string('title', 'repository');
    $filename     = get_string('filename', 'repository');
    $strsync      = get_string('sync', 'repository');
    $strdownload  = get_string('download', 'repository');
    $strback      = get_string('back', 'repository');
    $strclose     = get_string('close', 'repository');
    $strdownload  = get_string('downloadsucc', 'repository');
    $strnoenter   = get_string('noenter', 'repository');
    $strsearching = get_string('searching', 'repository');
    $stradd  = get_string('add', 'repository');
    $css = <<<EOD
    <style type="text/css">
    #list-$suffix{line-height: 1.5em}
    #list-$suffix a{ padding: 3px }
    #list-$suffix li a:hover{ background: gray; color:white; }
    #repo-list-$suffix .repo-name{}
    #repo-list-$suffix li{margin-bottom: 1em}
    #paging-$suffix{margin:10px 5px; clear:both}
    #paging-$suffix a{padding: 4px; border: 1px solid gray}
    #panel-$suffix{padding:0;margin:0; text-align:left;}
    .file_name{color:green;}
    .file_date{color:blue}
    .file_size{color:gray}
    .grid{width:80px; float:left;text-align:center;}
    .grid img{display:block}
    .grid div{width: 80px; height: 36px; overflow: hidden}
    .repo-opt{font-size: 10px;}
    </style>
    <style type="text/css">
    @import "$CFG->wwwroot/lib/yui/reset-fonts-grids/reset-fonts-grids.css";
    @import "$CFG->wwwroot/lib/yui/reset/reset-min.css";
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
    <script>
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
                        li = document.createElement('li');
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
                            repository_client_$suffix.req(id[1], 1, 0);
                        }
                        li.appendChild(link);
                        var opt = document.createElement('div');
                        var search = document.createElement('a');
                        search.href = '###';
                        search.innerHTML = '$strsearch';
                        search.id = 'repo-search-$suffix-'+repo.id;
                        search.onclick = function() {
                            var re = /repo-search-$suffix-(\d+)/i;
                            var id = this.id.match(re);
                            repository_client_$suffix.search(id[1]);
                        }
                        var logout = document.createElement('a');
                        logout.href = '###';
                        logout.innerHTML = '$strlogout';
                        logout.id = 'repo-logout-$suffix-'+repo.id;
                        logout.onclick = function() {
                            var re = /repo-logout-$suffix-(\d+)/i;
                            var id = this.id.match(re);
                            repository_client_$suffix.req(id[1], 1, 1);
                        }
                        opt.appendChild(search);
                        opt.appendChild(logout);
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
        _client.loading = function(){
            var panel = new YAHOO.util.Element('panel-$suffix');
            panel.get('element').innerHTML = '';
            var content = document.createElement('div');
            content.innerHTML = '$strloading';
            panel.get('element').appendChild(content);
        }
        _client.rename = function(oldname, url){
            var panel = new YAHOO.util.Element('panel-$suffix');
            var html = '<div>';
            html += '<label for="newname">$filename</label>';
            html += '<input type="text" id="newname-$suffix" value="'+oldname+'" /><br/>';
            html += '<label for="syncfile">$strsync</label>';
            html += '<input type="checkbox" id="syncfile-$suffix" /><br/>';
            html += '<input type="hidden" id="fileurl-$suffix" value="'+url+'" />';
            html += '<a href="###" onclick="repository_client_$suffix.viewfiles()">$strback</a>';
            html += '<input type="button" onclick="repository_client_$suffix.download()" value="$strdownload" />';
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
                    str += '<label'+lable_id+'>'+data[k].label+'</label>';
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
            var str = '';
            str += _client.uploadcontrol();
            str += _client.makepage();
            return str;
        }
        // TODO
        // Improve CSS
        _client.viewthumb = function(){
            var panel = new YAHOO.util.Element('panel-$suffix');
            _client.viewbar.check(1);
            list = _client.ds.list;
            panel.get('element').innerHTML = _client.navbar();
            for(k in list){
                var el = document.createElement('div');
                el.className='grid';
                var img = document.createElement('img');
                img.src = list[k].thumbnail;
                var input = document.createElement('input');
                input.type='radio';
                input.title = list[k].title;
                input.name = 'selected-files';
                input.value = list[k].source;
                input.title = list[k].title;
                if(list[k].children){
                    input.onclick = function(){
                        alert('this is a dir');
                    }
                }else{
                    input.onclick = function(){
                        repository_client_$suffix.rename(this.title, this.value);
                    }
                }
                var title = document.createElement('div');
                title.innerHTML = list[k].title;
                el.appendChild(img);
                el.appendChild(input);
                el.appendChild(title);
                panel.get('element').appendChild(el);
            }
            _client.viewmode = 1;
            return str;
        }
        _client.buildtree = function(node, level){
            var info = {label:node.title, title:"Date: "+node.date+' '+'Size:'+node.size};
            var tmpNode = new YAHOO.widget.TextNode(info, level, false);
            var tooltip = new YAHOO.widget.Tooltip(tmpNode.labelElId, {
                context:tmpNode.labelElId, text:info.title});
            tmpNode.filename = node.title;
            tmpNode.value  = node.source;
            if(node.children){
                tmpNode.isLeaf = false;
                tmpNode.subfolder = node.children;
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
                    repository_client_$suffix.rename(this.filename, this.value);
                }
            }
        }

_client.dynload = function (node, fnLoadComplete){
    var callback = {
        success: function(o) {
            try {
                var json = YAHOO.lang.JSON.parse(o.responseText);
            } catch(e) {
                alert('Invalid JSON String'+o.responseText);
            }
            for(k in json.list){
                _client.buildtree(json.list[k], node);
            }
            o.argument.fnLoadComplete();
        },
        failure:function(oResponse){
            alert('Error!');
            oResponse.argument.fnLoadComplete();
        },
        argument:{"node":node, "fnLoadComplete": fnLoadComplete},
        timeout:600
    }
    // TODO: need to include filepath here
    var trans = YAHOO.util.Connect.asyncRequest('GET',
        '$CFG->wwwroot/repository/ws.php?ctx_id=$context->id&repo_id='
            +_client.repositoryid+'&p='+node.path+'&action=list',
        callback);
}
        _client.viewlist = function(){
            var panel = new YAHOO.util.Element('panel-$suffix');
            _client.viewbar.check(0);
            list = _client.ds.list;
            var str = _client.navbar();
            str += '<div id="treediv"></div>';
            var re = new RegExp();
            re.compile("^[A-Za-z]+://[A-Za-z0-9-_]+\\.[A-Za-z0-9-_%&\?\/.=]+$");
            panel.get('element').innerHTML = str;
            var tree = new YAHOO.widget.TreeView('treediv');
            if(_client.ds.dynload) {
                tree.setDynamicLoad(_client.dynload, 1);
            } else {
            }
            for(k in list){
                _client.buildtree(list[k], tree.getRoot());
            }
            tree.draw();
            _client.viewmode = 0;
            return str;
        }
        _client.upload = function(){
            var u = _client.ds.upload;
            var conn = YAHOO.util.Connect;
            var aform = document.getElementById(u.id);
            var parent = document.getElementById(u.id+'_div');
            var loading = document.createElement('DIV');
            loading.innerHTML = "uploading...";
            loading.id = u.id+'_loading';
            parent.appendChild(loading);
            conn.setForm(aform, true, true);
            conn.asyncRequest('POST', '$CFG->wwwroot/repository/ws.php?ctx_id=$context->id&repo_id='+_client.repositoryid+'&action=upload', _client.upload_cb);
        }
        _client.upload_cb = {
            upload: function(o){
                var u = _client.ds.upload;
                var aform = document.getElementById(u.id);
                aform.reset();
                var loading = document.getElementById(u.id+'_loading');
                loading.innerHTML = 'Saved!';
                _client.req(_client.repositoryid, '', 0);
            }
        }
        _client.uploadcontrol = function() {
            var str = '';
            if(_client.ds.upload){
                str += '<div id="'+_client.ds.upload.id+'_div">';
                str += '<form id="'+_client.ds.upload.id+'" onsubmit="return false">';
                str += '<label for="'+_client.ds.upload.id+'-file">'+_client.ds.upload.name+'</label>';
                str += '<input type="file" id="'+_client.ds.upload.id+'-file"/>';
                str += '<a href="###" onclick="return repository_client_$suffix.upload();">Upload</a>';
                str += '</form>';
                str += '</div>';
                str += '<hr />';
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
        // send download request
        _client.download = function(){
            var title = document.getElementById('newname-$suffix').value;
            var file = document.getElementById('fileurl-$suffix').value;
            _client.loading();
            var trans = YAHOO.util.Connect.asyncRequest('POST',
                '$CFG->wwwroot/repository/ws.php?ctx_id=$context->id&repo_id='
                +_client.repositoryid+'&action=download',
                _client.dlfile, _client.postdata({'env':_client.env, 'file':file, 'title':title}));
        }
        // send login request
        _client.login = function(){
            var params = {};
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
            _client.loading();
            var trans = YAHOO.util.Connect.asyncRequest('POST',
                '$CFG->wwwroot/repository/ws.php?action=sign', _client.callback,
                _client.postdata(params));
        }
        _client.end = function(str){
            _client.target.value = str;
            _client.formcallback();
            _client.instance.hide();
            _client.viewfiles();
        }
        _client.callback = {
            success: function(o) {
                var panel = new YAHOO.util.Element('panel-$suffix');
                try {
                    var ret = YAHOO.lang.JSON.parse(o.responseText);
                } catch(e) {
                    alert('Callback: Invalid JSON String'+o.responseText);
                };
                if(ret.e){
                    panel.get('element').innerHTML = ret.e;
                    return;
                }
                _client.ds = ret;
                if(_client.ds.login){
                    _client.print_login();
                } else if(_client.ds.list) {
                    if(_client.viewmode) {
                        _client.viewthumb();
                    } else {
                        _client.viewlist();
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
                    alert('Invalid JSON String'+o.responseText);
                }
                if(ret.e){
                    panel.get('element').innerHTML = ret.e;
                    return;
                }
                var title = document.createElement('h1');
                title.innerHTML = '$strdownload';
                var btn = document.createElement('button');
                btn.innerHTML = '$stradd';
                btn.onclick = function(){
                    repository_client_$suffix.end(ret);
                }
                panel.get('element').innerHTML = '';
                panel.get('element').appendChild(title);
                panel.get('element').appendChild(btn);
            }
        }
        // request file list or login
        _client.req = function(id, path, reset) {
            _client.viewbar.set('disabled', false);
            _client.loading();
            _client.repositoryid = id;
            var trans = YAHOO.util.Connect.asyncRequest('GET', '$CFG->wwwroot/repository/ws.php?action=list&ctx_id=$context->id&repo_id='+id+'&p='+path+'&reset='+reset+'&env='+_client.env, _client.callback);
        }
        _client.search = function(id){
            var data = window.prompt("$strsearching");
            if(data == '') {
                alert('$strnoenter');
                return;
            }
            _client.viewbar.set('disabled', false);
            _client.loading();
            var trans = YAHOO.util.Connect.asyncRequest('GET', '$CFG->wwwroot/repository/ws.php?action=search&ctx_id=$context->id&repo_id='+id+'&s='+data+'&env='+_client.env, _client.callback);
        }
        return _client;
    })();
EOD;

    $repos = repository_instances($context);
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
    </script>
EOD;
    return array('css'=>$css, 'js'=>$js, 'suffix'=>$suffix);
}

final class repository_admin_form extends moodleform {
    protected $instance;
    protected $plugin;

    public function definition() {
        global $CFG;
        // type of plugin, string
        $this->plugin = $this->_customdata['plugin'];
        $this->instance = (isset($this->_customdata['instance'])
                && is_subclass_of($this->_customdata['instance'], 'repository'))
            ? $this->_customdata['instance'] : null;

        $mform =& $this->_form;
        $strrequired = get_string('required');

        $mform->addElement('hidden', 'edit',  ($this->instance) ? $this->instance->id : 0);
        $mform->addElement('hidden', 'new',   $this->plugin);
        $mform->addElement('hidden', 'plugin', $this->plugin);

        $mform->addElement('text', 'name', get_string('name'), 'maxlength="100" size="30"');
        $mform->addRule('name', $strrequired, 'required', null, 'client');

        // let the plugin add the fields they want (either statically or not)
        if (repository_static_function($this->plugin, 'has_admin_config')) {
            if (!$this->instance) {
                $result = repository_static_function($this->plugin, 'admin_config_form', $mform);
            } else {
                $result = $this->instance->admin_config_form($mform);
            }
        }

        // and set the data if we have some.
        if ($this->instance) {
            $data = array();
            $data['name'] = $this->instance->name;
            foreach ($this->instance->get_option_names() as $config) {
                $data[$config] = $this->instance->$config;
            }
            $this->set_data($data);
        }
        $this->add_action_buttons(true, get_string('submit'));
    }

    public function validation($data) {
        global $DB;

        $errors = array();
        if ($DB->count_records('repository', array('repositoryname' => $data['name'], 'repositorytype' => $data['plugin'])) > 1) {
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