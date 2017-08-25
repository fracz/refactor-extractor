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
 * Assess a submission or preview the assessment form
 *
 * Displays an assessment form and saves the grades given by current user (reviewer)
 * for the dimensions.
 *
 * @package   mod-workshop
 * @copyright 2009 David Mudrak <david.mudrak@gmail.com>
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */

require_once(dirname(dirname(dirname(__FILE__))).'/config.php');
require_once(dirname(__FILE__).'/locallib.php');

if ($preview = optional_param('preview', 0, PARAM_INT)) {
    $mode       = 'preview';
    $cm         = get_coursemodule_from_id('workshop', $preview, 0, false, MUST_EXIST);
    $course     = $DB->get_record('course', array('id' => $cm->course), '*', MUST_EXIST);
    $workshop   = $DB->get_record('workshop', array('id' => $cm->instance), '*', MUST_EXIST);
    $submission = new stdClass();
    $assessment = new stdClass();

} else {
    $mode       = 'assessment';
    $asid       = required_param('asid', PARAM_INT);  // assessment id
    $assessment = $DB->get_record('workshop_assessments', array('id' => $asid), '*', MUST_EXIST);
    $submission = $DB->get_record('workshop_submissions', array('id' => $assessment->submissionid), '*', MUST_EXIST);
    $workshop   = $DB->get_record('workshop', array('id' => $submission->workshopid), '*', MUST_EXIST);
    $course     = $DB->get_record('course', array('id' => $workshop->course), '*', MUST_EXIST);
    $cm         = get_coursemodule_from_instance('workshop', $workshop->id, $course->id, false, MUST_EXIST);
}

require_login($course, false, $cm);
$workshop = new workshop_api($workshop, $cm, $course);

if ('preview' == $mode) {
    require_capability('mod/workshop:editdimensions', $PAGE->context);
    // TODO logging add_to_log($course->id, "workshop", "view", "view.php?id=$cm->id", "$workshop->id");
    $PAGE->set_url($workshop->previewform_url());
    $PAGE->set_title($workshop->name);
    $PAGE->set_heading($course->fullname);

} elseif ('assessment' == $mode) {
    if (!(has_capability('mod/workshop:peerassess', $PAGE->context) || has_capability('mod/workshop:peerassess', $PAGE->context))) {
        print_error('nopermissions', '', $workshop->view_url());
    }
    // TODO logging add_to_log($course->id, "workshop", "view", "view.php?id=$cm->id", "$workshop->id");
    $PAGE->set_url($workshop->assess_url($assessment->id));
    $PAGE->set_title($workshop->name);
    $PAGE->set_heading($course->fullname);
}

// load the grading strategy logic
$strategy = $workshop->grading_strategy_instance();

// load the assessment form definition from the database
// this must be called before get_assessment_form() where we have to know
// the number of repeating fieldsets

//todo $formdata = $strategy->load_assessment($assessment);

// load the form to edit the grading strategy dimensions
$mform = $strategy->get_assessment_form($PAGE->url, $mode);

// initialize form data
//todo $mform->set_data($formdata);

if ($mform->is_cancelled()) {
    redirect($returnurl);

} elseif ($data = $mform->get_data()) {
    if (isset($data->backtoeditform)) {
        redirect($workshop->editform_url());
    }
    $strategy->save_assessment($data);
    if (isset($data->saveandclose)) {
        redirect($workshop->view_url());
    } else {
        // save and continue - redirect to self to prevent data being re-posted by pressing "Reload"
        redirect($PAGE->url->out());
    }
}

// build the navigation and the header
$navlinks = array();
$navlinks[] = array('name' => get_string('modulenameplural', 'workshop'),
                    'link' => "index.php?id=$course->id",
                    'type' => 'activity');
$navlinks[] = array('name' => format_string($workshop->name),
                    'link' => "view.php?id=$cm->id",
                    'type' => 'activityinstance');
if ($mode == 'preview') {
    $navlinks[] = array('name' => get_string('editingassessmentform', 'workshop'),
                        'link' => $workshop->editform_url(),
                        'type' => 'title');
    $navlinks[] = array('name' => get_string('previewassessmentform', 'workshop'),
                        'link' => '',
                        'type' => 'title');
} elseif ($mode == 'assessment') {
    $navlinks[] = array('name' => get_string('assessingsubmission', 'workshop'),
                        'link' => '',
                        'type' => 'title');
}
$navigation = build_navigation($navlinks);

// Output starts here

$wsoutput = $THEME->get_renderer('mod_workshop', $PAGE);    // workshop renderer
echo $OUTPUT->header($navigation);
echo $OUTPUT->heading(get_string('assessmentform', 'workshop'), 2);
if (has_capability('mod/workshop:viewauthornames', $PAGE->context)) {
    $showname   = true;
    $author     = $workshop->user_info($submission->userid);
} else {
    $showname   = false;
    $author     = null;
}
echo $wsoutput->submission_full($submission, $showname, $author);
$mform->display();
echo $OUTPUT->footer();