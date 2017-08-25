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
 * Blog entry edit page
 *
 * @package    moodlecore
 * @subpackage blog
 * @copyright  2009 Nicolas Connault
 * @license    http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */
require_once('../config.php');
include_once('lib.php');
include_once('locallib.php');
include_once($CFG->dirroot.'/tag/lib.php');

$action   = required_param('action', PARAM_ALPHA);
$id       = optional_param('id', 0, PARAM_INT);
$confirm  = optional_param('confirm', 0, PARAM_BOOL);
$modid    = optional_param('modid', 0, PARAM_INT);
$courseid = optional_param('courseid', 0, PARAM_INT); // needed for user tab - does nothing here

$PAGE->set_url('blog/edit.php', compact('id', 'action', 'confirm', 'modid', 'courseid'));

require_login($courseid);

if (empty($CFG->bloglevel)) {
    print_error('blogdisable', 'blog');
}

if (isguest()) {
    print_error('noguestentry', 'blog');
}

$sitecontext = get_context_instance(CONTEXT_SYSTEM);
if (!has_capability('moodle/blog:create', $sitecontext) && !has_capability('moodle/blog:manageentries', $sitecontext)) {
    print_error('cannoteditentryorblog');
}

$returnurl = new moodle_url($CFG->wwwroot . '/blog/index.php');

// Make sure that the person trying to edit have access right
if ($id) {
    if (!$existing = new blog_entry($id)) {
        print_error('wrongentryid', 'blog');
    }

    if (!blog_user_can_edit_entry($existing)) {
        print_error('notallowedtoedit', 'blog');
    }
    $userid    = $existing->userid;
    $returnurl->param('userid', $existing->userid);
} else {
    if (!has_capability('moodle/blog:create', $sitecontext)) {
        print_error('noentry', 'blog'); // manageentries is not enough for adding
    }
    $existing  = false;
    $userid    = $USER->id;
    $returnurl->param('userid', $userid);
}

if (!empty($courseid) && empty($modid)) {
    $returnurl->param('courseid', $courseid);
    $PAGE->set_context(get_context_instance(CONTEXT_COURSE, $courseid));
}

// If a modid is given, guess courseid
if (!empty($modid)) {
    $returnurl->param('modid', $modid);
    $courseid = $DB->get_field('course_modules', 'course', array('id' => $modid));
    $returnurl->param('courseid', $courseid);
    $PAGE->set_context(get_context_instance(CONTEXT_MODULE, $modid));
}

$strblogs = get_string('blogs','blog');

if ($action === 'delete'){
    if (!$existing) {
        print_error('wrongentryid', 'blog');
    }
    if (data_submitted() && $confirm && confirm_sesskey()) {
        $existing->delete();
        redirect($returnurl);
    } else {
        $optionsyes = array('id'=>$id, 'action'=>'delete', 'confirm'=>1, 'sesskey'=>sesskey(), 'courseid'=>$courseid);
        $optionsno = array('userid'=>$existing->userid, 'courseid'=>$courseid);
        print_header("$SITE->shortname: $strblogs", $SITE->fullname);
        //blog_print_entry($existing);
        $existing->print_html();
        echo '<br />';
        echo $OUTPUT->confirm(get_string('blogdeleteconfirm', 'blog'), new moodle_url('edit.php', $optionsyes),new moodle_url( 'index.php', $optionsno));
        echo $OUTPUT->footer();
        die;
    }
}

require_once('edit_form.php');

if (!empty($existing)) {
    $assignmentdata = $DB->get_record_sql('SELECT a.timedue, a.preventlate, a.emailteachers, a.var2, asub.grade
                                           FROM {assignment} a, {assignment_submissions} as asub WHERE
                                           a.id = asub.assignment AND userid = '.$USER->id.' AND a.assignmenttype = \'blog\'
                                           AND asub.data1 = \''.$existing->id.'\'');

    if ($blogassociations = $DB->get_records('blog_association', array('blogid' => $existing->id))) {

        foreach ($blogassociations as $assocrec) {
            $contextrec = $DB->get_record('context', array('id' => $assocrec->contextid));

            switch ($contextrec->contextlevel) {
                case CONTEXT_COURSE:
                    $existing->courseassoc = $assocrec->contextid;
                    break;
                case CONTEXT_MODULE:
                    $existing->modassoc[] = $assocrec->contextid;
                    break;
            }
        }
    }
}

$textfieldoptions = array('trusttext'=>true, 'subdirs'=>true);
$blogeditform = new blog_edit_form(null, compact('existing', 'sitecontext', 'assignmentdata', 'textfieldoptions'));

$existing = file_prepare_standard_editor($existing, 'summary', $textfieldoptions, $PAGE->get_context());

if ($blogeditform->is_cancelled()){
    redirect($returnurl);
} else if ($fromform = $blogeditform->get_data()){
    $fromform = file_postupdate_standard_editor($fromform, 'summary', $textfieldoptions, $PAGE->get_context());

    //save stuff in db
    switch ($action) {
        case 'add':
            $blog_entry = new blog_entry($fromform, $blogeditform);
            $blog_entry->add();
        break;

        case 'edit':
            if (!$existing) {
                print_error('wrongentryid', 'blog');
            }
            $existing->edit($fromform, $blogeditform);
        break;
        default :
            print_error('invalidaction');
    }
    redirect($returnurl);
}


// gui setup
switch ($action) {
    case 'add':
        // prepare new empty form
        $entry->publishstate = 'site';
        $strformheading = get_string('addnewentry', 'blog');
        $entry->action       = $action;

        if ($courseid) {  //pre-select the course for associations
            $context = get_context_instance(CONTEXT_COURSE, $courseid);
            $entry->courseassoc = $context->id;
        }

        if ($modid) { //pre-select the mod for associations
            $context = get_context_instance(CONTEXT_MODULE, $modid);
            $entry->modassoc = array($context->id);
        }
        break;

    case 'edit':
        if (!$existing) {
            print_error('wrongentryid', 'blog');
        }

        $entry->id           = $existing->id;
        $entry->subject      = $existing->subject;
        $entry->fakesubject  = $existing->subject;
        $entry->summary      = $existing->summary;
        $entry->fakesummary  = $existing->summary;
        $entry->publishstate = $existing->publishstate;
        $entry->format       = $existing->format;
        $entry->tags         = tag_get_tags_array('post', $entry->id);
        $entry->action       = $action;

        if (!empty($existing->courseassoc)) {
            $entry->courseassoc = $existing->courseassoc;
        }

        if (!empty($existing->modassoc)) {
            $entry->modassoc = $existing->modassoc;
        }

        $strformheading = get_string('updateentrywithid', 'blog');

        break;
    default :
        print_error('unknowaction');
}

// Add filter params for return url
$entry->modid = $modid;
$entry->courseid = $courseid;

$PAGE->requires->data_for_js('blog_edit_existing', $entry);

// done here in order to allow deleting of entries with wrong user id above
if (!$user = $DB->get_record('user', array('id'=>$userid))) {
    print_error('invaliduserid');
}

$blog_headers = blog_get_headers();

$navigation = build_navigation($blog_headers['navlinks'], $blog_headers['cm']);
/*
$navlinks = array();
$navlinks[] = array('name' => fullname($user), 'link' => "$CFG->wwwroot/user/view.php?id=$userid", 'type' => 'misc');
$navlinks[] = array('name' => $strblogs, 'link' => "$CFG->wwwroot/blog/index.php?userid=$userid", 'type' => 'misc');
$navlinks[] = array('name' => $strformheading, 'link' => null, 'type' => 'misc');
$navigation = build_navigation($navlinks);
*/
$PAGE->requires->js('blog/edit_form.js');
$PAGE->set_title($blog_headers['title']);
$PAGE->set_heading($blog_headers['title']);

echo $OUTPUT->header($navigation);

$blogeditform->set_data($entry);
$blogeditform->display();

$PAGE->requires->js_function_call('select_initial_course');

echo $OUTPUT->footer();

die;