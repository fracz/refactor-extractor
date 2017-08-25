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
 * repository_local class is used to browse moodle files
 *
 * @since 2.0
 * @package moodlecore
 * @subpackage repository
 * @copyright 2009 Dongsheng Cai <dongsheng@moodle.com>
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */

class repository_local extends repository {

    /**
     * initialize local plugin
     * @param int $repositoryid
     * @param int $context
     * @param array $options
     */
    public function __construct($repositoryid, $context = SITEID, $options = array()) {
        parent::__construct($repositoryid, $context, $options);
    }

    /**
     * local plugin don't need login, so list all files
     * @return mixed
     */
    public function print_login() {
        return $this->get_listing();
    }

    /**
     * Not supported by File API yet
     * @param string $search_text
     * @return mixed
     */
    public function search($search_text) {
        return array();
    }

    /**
     * Get file listing
     *
     * @param string $encodedpath
     * @param string $path not used by this plugin
     * @return mixed
     */
    public function get_listing($encodedpath = '', $page = '') {
        global $CFG, $USER, $OUTPUT;
        $ret = array();
        $ret['dynload'] = true;
        $ret['nosearch'] = true;
        $list = array();

        if (!empty($encodedpath)) {
            $params = unserialize(base64_decode($encodedpath));
            if (is_array($params)) {
                $itemid   = $params['itemid'];
                $filename = $params['filename'];
                $filearea = $params['filearea'];
                $filepath = $params['filepath'];
                $context  = get_context_instance_by_id($params['contextid']);
            }
        } else {
            $itemid   = null;
            $filename = null;
            $filearea = null;
            $filepath = null;
            $context  = get_system_context();
        }

        try {
            $browser = get_file_browser();

            if ($fileinfo = $browser->get_file_info($context, $filearea, $itemid, $filepath, $filename)) {
                $level = $fileinfo->get_parent();
                while ($level) {
                    $params = base64_encode(serialize($level->get_params()));
                    $path[] = array('name'=>$level->get_visible_name(), 'path'=>$params);
                    $level = $level->get_parent();
                }
                if (!empty($path) && is_array($path)) {
                    $path = array_reverse($path);
                    $ret['path'] = $path;
                }
                $children = $fileinfo->get_children();
                foreach ($children as $child) {
                    if ($child->is_directory()) {
                        $params = base64_encode(serialize($child->get_params()));
                        $node = array(
                            'title' => $child->get_visible_name(),
                            'size' => 0,
                            'date' => '',
                            'path' => $params,
                            'children'=>array(),
                            'thumbnail' => $OUTPUT->old_icon_url('f/folder-32') . ''
                        );
                        $list[] = $node;
                    } else {
                        $params = base64_encode(serialize($child->get_params()));
                        $icon = 'f/'.str_replace('.gif', '', mimeinfo('icon', $child->get_visible_name())).'-32';
                        $node = array(
                            'title' => $child->get_visible_name(),
                            'size' => 0,
                            'date' => '',
                            'source'=> $params,
                            'thumbnail' => $OUTPUT->old_icon_url($icon)
                        );
                        $list[] = $node;
                    }
                }
            }
        } catch (Exception $e) {
            throw new repository_exception('emptyfilelist', 'repository_local');
        }
        $ret['list'] = $list;
        return $ret;
    }

     /**
     * Move a file to draft area
     *
     * @global object $USER
     * @global object $DB
     * @param string $encoded The metainfo of file, it is base64 encoded php seriablized data
     * @param string $title The intended name of file
     * @param string $itemid itemid
     * @param string $save_path the new path in draft area
     * @return array The information of file
     */
    public function move_to_draft($encoded, $title = '', $itemid = '', $save_path = '/') {
        global $USER, $DB;
        $ret = array();

        $browser = get_file_browser();
        $params = unserialize(base64_decode($encoded));
        $user_context = get_context_instance(CONTEXT_USER, $USER->id);
        // the final file
        $contextid  = $params['contextid'];
        $filearea   = $params['filearea'];
        $filepath   = $params['filepath'];
        $filename   = $params['filename'];
        $fileitemid = $params['itemid'];
        $context    = get_context_instance_by_id($contextid);
        $file_info = $browser->get_file_info($context, $filearea, $fileitemid, $filepath, $filename);
        $file_info->copy_to_storage($user_context->id, 'user_draft', $itemid, $save_path, $title);

        $ret['itemid'] = $itemid;
        $ret['title']  = $title;
        $ret['contextid'] = $user_context->id;
        $ret['filesize'] = $file_info->get_filesize();

        return $ret;
    }

    /**
     * Set repository name
     *
     * @return string repository name
     */
    public function get_name(){
        return get_string('repositoryname', 'repository_local');;
    }

    /**
     * Local file don't support to link to external links
     *
     * @return int
     */
    public function supported_returntypes() {
        return FILE_INTERNAL;
    }
}
