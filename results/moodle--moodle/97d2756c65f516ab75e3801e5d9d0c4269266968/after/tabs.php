<?php  // $Id$
/**
* Sets up the tabs used by the lesson pages for teachers.
*
* This file was adapted from the mod/quiz/tabs.php
*
* @version $Id$
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
* @package lesson
*/

/// This file to be included so we can assume config.php has already been included.

    if (empty($lesson)) {
        error('You cannot call this script in that way');
    }
    if (!isset($currenttab)) {
        $currenttab = '';
    }
    if (!isset($cm)) {
        $cm = get_coursemodule_from_instance('lesson', $lesson->id);
    }
    if (!isset($course)) {
        $course = get_record('course', 'id', $lesson->course);
    }

    $tabs     = array();
    $row      = array();
    $inactive = array();


/// user attempt count for reports link hover (completed attempts - much faster)
    $counts           = new stdClass;
    $counts->attempts = count_records('lesson_grades', 'lessonid', $lesson->id);
    $counts->student  = $course->student;

    $row[] = new tabobject('teacherview', "$CFG->wwwroot/mod/lesson/view.php?id=$cm->id", get_string('edit'), get_string('editlesson', 'lesson', format_string($lesson->name)));
    $row[] = new tabobject('navigation', "$CFG->wwwroot/mod/lesson/view.php?id=$cm->id&amp;action=navigation", get_string('preview', 'lesson'), get_string('previewlesson', 'lesson', format_string($lesson->name)));
    $row[] = new tabobject('reports', "$CFG->wwwroot/mod/lesson/report.php?id=$cm->id", get_string('reports', 'lesson'), get_string('viewreports', 'lesson', $counts));
    if (isteacheredit($course->id)) {
        $row[] = new tabobject('essayview', "$CFG->wwwroot/mod/lesson/view.php?id=$cm->id&amp;action=essayview", get_string('manualgrading', 'lesson'));
    }
    if ($lesson->highscores) {
        $row[] = new tabobject('highscores', "$CFG->wwwroot/mod/lesson/view.php?id=$cm->id&amp;action=highscores", get_string('highscores', 'lesson'));
    }

    $tabs[] = $row;

/// sub tabs for reports (overview and detail)
    if ($currenttab == 'reports' and isset($mode)) {
        $inactive[] = 'reports';
        $currenttab = $mode;

        $row    = array();
        $row[]  = new tabobject('view', "$CFG->wwwroot/mod/lesson/report.php?id=$cm->id&amp;action=view", get_string('overview', 'lesson'));
        $row[]  = new tabobject('detail', "$CFG->wwwroot/mod/lesson/report.php?id=$cm->id&amp;action=detail", get_string('detailedstats', 'lesson'));
        $tabs[] = $row;
    }

/// sub tabs for teacher view (collapsed and expanded aka full)
    if ($currenttab == 'teacherview') {
        // use user preferences to remember which edit mode the user has selected
        if (empty($mode)) {
            $mode = get_user_preferences('lesson_view', 'collapsed');
        } else {
            set_user_preference('lesson_view', $mode);
        }

        $inactive[] = 'teacherview';
        $currenttab = $mode;

        $row    = array();
        $row[]  = new tabobject('collapsed', "$CFG->wwwroot/mod/lesson/view.php?id=$cm->id&amp;action=teacherview&amp;mode=collapsed", get_string('collapsed', 'lesson'));
        $row[]  = new tabobject('full', "$CFG->wwwroot/mod/lesson/view.php?id=$cm->id&amp=teacherview&amp;mode=full", get_string('full', 'lesson'));
        $tabs[] = $row;
    }

    print_tabs($tabs, $currenttab, $inactive);

?>