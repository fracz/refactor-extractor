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
 * Form page for blog preferences
 *
 * @package    moodlecore
 * @subpackage blog
 * @copyright  2009 Nicolas Connault
 * @license    http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */

require_once('../config.php');
require_once($CFG->dirroot.'/blog/lib.php');
require_once('preferences_form.php');

$courseid = optional_param('courseid', SITEID, PARAM_INT);
$PAGE->set_url('blog/preferences.php', array('courseid' => $courseid));

if ($courseid == SITEID) {
    require_login();
    $context = get_context_instance(CONTEXT_SYSTEM);
} else {
    require_login($courseid);
    $context = get_context_instance(CONTEXT_COURSE, $courseid);
}

if (empty($CFG->bloglevel)) {
    print_error('blogdisable', 'blog');
}

require_capability('moodle/blog:view', $context);

/// If data submitted, then process and store.

$mform = new blog_preferences_form('preferences.php');

if (!$mform->is_cancelled() && $data = $mform->get_data()) {
    $pagesize = $data->pagesize;

    if ($pagesize < 1) {
        print_error('invalidpagesize');
    }
    set_user_preference('blogpagesize', $pagesize);
}

if ($mform->is_cancelled()){
    redirect($CFG->wwwroot . '/blog/index.php');
}

$site = get_site();

$strpreferences = get_string('preferences');
$strblogs       = get_string('blogs', 'blog');
$navlinks = array(array('name' => $strblogs, 'link' => "$CFG->wwwroot/blog/", 'type' => 'misc'));
$navlinks[] = array('name' => $strpreferences, 'link' => null, 'type' => 'misc');
$navigation = build_navigation($navlinks);

$title = "$site->shortname: $strblogs : $strpreferences";
$PAGE->set_title($title);
$PAGE->set_heading($title);

echo $OUTPUT->header($navigation);

echo $OUTPUT->heading("$strblogs : $strpreferences", 2);

$mform->display();

echo $OUTPUT->footer();