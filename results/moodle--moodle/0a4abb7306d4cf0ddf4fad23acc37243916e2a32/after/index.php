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
 * This page lists all the instances of lesson in a particular course
 *
 * @package   lesson
 * @copyright 1999 onwards Martin Dougiamas  {@link http://moodle.com}
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 **/

/** Include required files */
require_once("../../config.php");
require_once($CFG->dirroot.'/mod/lesson/locallib.php');

$id = required_param('id', PARAM_INT);   // course

$PAGE->set_url(new moodle_url($CFG->wwwroot.'/mod/lesson/index.php', array('id'=>$id)));

if (!$course = $DB->get_record("course", array("id" => $id))) {
    print_error('invalidcourseid');
}

require_login($course);

add_to_log($course->id, "lesson", "view all", "index.php?id=$course->id", "");


/// Get all required strings

$strlessons = get_string("modulenameplural", "lesson");
$strlesson  = get_string("modulename", "lesson");


/// Print the header
$PAGE->navbar->add($strlessons);
$PAGE->set_title("$course->shortname: $strlessons");
$PAGE->set_heading($course->fullname);
echo $OUTPUT->header();

/// Get all the appropriate data

if (! $lessons = get_all_instances_in_course("lesson", $course)) {
    notice(get_string('thereareno', 'moodle', $strlessons), "../../course/view.php?id=$course->id");
    die;
}

/// Print the list of instances (your module will probably extend this)

$timenow = time();
$strname  = get_string("name");
$strgrade  = get_string("grade");
$strdeadline  = get_string("deadline", "lesson");
$strweek  = get_string("week");
$strtopic  = get_string("topic");
$strnodeadline = get_string("nodeadline", "lesson");
$table = new html_table();

if ($course->format == "weeks") {
    $table->head  = array ($strweek, $strname, $strgrade, $strdeadline);
    $table->align = array ("center", "left", "center", "center");
} else if ($course->format == "topics") {
    $table->head  = array ($strtopic, $strname, $strgrade, $strdeadline);
    $table->align = array ("center", "left", "center", "center");
} else {
    $table->head  = array ($strname, $strgrade, $strdeadline);
    $table->align = array ("left", "center", "center");
}

foreach ($lessons as $lesson) {
    if (!$lesson->visible) {
        //Show dimmed if the mod is hidden
        $link = "<a class=\"dimmed\" href=\"view.php?id=$lesson->coursemodule\">".format_string($lesson->name,true)."</a>";
    } else {
        //Show normal if the mod is visible
        $link = "<a href=\"view.php?id=$lesson->coursemodule\">".format_string($lesson->name,true)."</a>";
    }
    $cm = get_coursemodule_from_instance('lesson', $lesson->id);
    $context = get_context_instance(CONTEXT_MODULE, $cm->id);

    if ($lesson->deadline == 0) {
        $due = $strnodeadline;
    } else if ($lesson->deadline > $timenow) {
        $due = userdate($lesson->deadline);
    } else {
        $due = "<font color=\"red\">".userdate($lesson->deadline)."</font>";
    }

    if ($course->format == "weeks" or $course->format == "topics") {
        if (has_capability('mod/lesson:manage', $context)) {
            $grade_value = $lesson->grade;
        } else {
            // it's a student, show their grade
            $grade_value = 0;
            if ($return = lesson_get_user_grades($lesson, $USER->id)) {
                $grade_value = $return[$USER->id]->rawgrade;
            }
        }
        $table->data[] = array ($lesson->section, $link, $grade_value, $due);
    } else {
        $table->data[] = array ($link, $lesson->grade, $due);
    }
}
echo $OUTPUT->table($table);
echo $OUTPUT->footer();