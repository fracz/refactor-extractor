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
 * Toggles the manual completion flag for a particular activity and the current user.
 *
 * @license http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @package course
 */

require_once('../config.php');
require_once($CFG->libdir.'/completionlib.php');

// Parameters
$cmid        = required_param('id', PARAM_INT);
$targetstate = required_param('completionstate', PARAM_INT);
$fromajax    = optional_param('fromajax', 0, PARAM_INT);

$PAGE->set_url('/course/togglecompletion.php', array('id'=>$cmid, 'completionstate'=>$targetstate));

switch($targetstate) {
    case COMPLETION_COMPLETE:
    case COMPLETION_INCOMPLETE:
        break;
    default:
        print_error('unsupportedstate');
}

// Get course-modules entry
$cm = get_coursemodule_from_id(null, $cmid, null, false, MUST_EXIST);
$course = $DB->get_record('course', array('id'=>$cm->course), '*', MUST_EXIST);

// Check user is logged in
require_login($course, false, $cm);

if (isguestuser() or !confirm_sesskey()) {
    print_error('error');
}

// Now change state
$completion = new completion_info($course);
if (!$completion->is_enabled()) {
    die;
}

// Check completion state is manual
if($cm->completion != COMPLETION_TRACKING_MANUAL) {
    error_or_ajax('cannotmanualctrack', $fromajax);
}

$completion->update_state($cm, $targetstate);

// And redirect back to course
if ($fromajax) {
    print 'OK';
} else {
    // In case of use in other areas of code we allow a 'backto' parameter,
    // otherwise go back to course page
    $backto = optional_param('backto', 'view.php?id='.$course->id, PARAM_URL);
    redirect($backto);
}

// utility functions

function error_or_ajax($message, $fromajax) {
    if ($fromajax) {
        print get_string($message, 'error');
        exit;
    } else {
        print_error($message);
    }
}
