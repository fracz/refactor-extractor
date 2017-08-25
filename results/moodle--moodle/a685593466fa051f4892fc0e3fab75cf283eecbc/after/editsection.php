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
 * Edit the introduction of a section
 *
 * @copyright 1999 Martin Dougiamas  http://dougiamas.com
 * @license http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @package course
 */

require_once("../config.php");
require_once("lib.php");
require_once($CFG->libdir.'/filelib.php');
require_once('editsection_form.php');

$id = required_param('id',PARAM_INT);    // Week/topic ID

$PAGE->set_url('/course/editsection.php', array('id'=>$id));

if (! $section = $DB->get_record("course_sections", array("id"=>$id))) {
    print_error("sectionnotexist");
}

if (! $course = $DB->get_record("course", array("id"=>$section->course))) {
    print_error("invalidcourseid");
}

require_login($course);
$context = get_context_instance(CONTEXT_COURSE, $course->id);
require_capability('moodle/course:update', $context);

$draftitemid = file_get_submitted_draft_itemid('summary');
$currenttext = file_prepare_draft_area($draftitemid, $context->id, 'course_section', $section->id, array('subdirs'=>true), $section->summary);

$mform = new editsection_form(null, $course);
$data = array('id'=>$section->id, 'summary'=>array('text'=>$currenttext, 'format'=>FORMAT_HTML, 'itemid'=>$draftitemid));
$mform->set_data($data); // set defaults

/// If data submitted, then process and store.
if ($mform->is_cancelled()){
    redirect($CFG->wwwroot.'/course/view.php?id='.$course->id);

} else if ($data = $mform->get_data()) {

    $text = file_save_draft_area_files($data->summary['itemid'], $context->id, 'course_section', $section->id, array('subdirs'=>true), $data->summary['text']);
    $DB->set_field("course_sections", "summary", $text, array("id"=>$section->id));
    add_to_log($course->id, "course", "editsection", "editsection.php?id=$section->id", "$section->section");
    redirect("view.php?id=$course->id");
}

/// Inelegant hack for bug 3408
if ($course->format == 'site') {
    $sectionname  = get_string('site');
    $stredit      = get_string('edit', '', " $sectionname");
    $strsummaryof = get_string('summaryof', '', " $sectionname");
} else {
    $sectionname  = get_section_name($course->format);
    $stredit      = get_string('edit', '', " $sectionname $section->section");
    $strsummaryof = get_string('summaryof', '', " $sectionname $section->section");
}

$PAGE->set_title($stredit);
$PAGE->navbar->add($stredit);
$PAGE->set_focuscontrol('theform.summary');
echo $OUTPUT->header();

echo $OUTPUT->heading_with_help($strsummaryof, 'summaries');

$mform->display();
echo $OUTPUT->footer();