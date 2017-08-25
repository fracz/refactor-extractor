<?php

/**
* prints the form to confirm the deleting of a completed
*
* @author Andreas Grabs
* @license http://www.gnu.org/copyleft/gpl.html GNU Public License
* @package feedback
*/

require_once("../../config.php");
require_once("lib.php");
require_once('delete_completed_form.php');

$id = required_param('id', PARAM_INT);
$completedid = optional_param('completedid', 0, PARAM_INT);

if($completedid == 0){
    print_error('no_complete_to_delete', 'feedback', 'show_entries.php?id='.$id.'&do_show=showentries');
}

$PAGE->set_url(new moodle_url($CFG->wwwroot.'/mod/feedback/delete_completed.php', array('id'=>$id, 'completed'=>$completedid)));

if ($id) {
    if (! $cm = get_coursemodule_from_id('feedback', $id)) {
        print_error('invalidcoursemodule');
    }

    if (! $course = $DB->get_record("course", array("id"=>$cm->course))) {
        print_error('coursemisconf');
    }

    if (! $feedback = $DB->get_record("feedback", array("id"=>$cm->instance))) {
        print_error('invalidcoursemodule');
    }
}
$capabilities = feedback_load_capabilities($cm->id);

require_login($course->id, true, $cm);

if(!$capabilities->deletesubmissions){
    print_error('error');
}

$mform = new mod_feedback_delete_completed_form();
$newformdata = array('id'=>$id,
                    'completedid'=>$completedid,
                    'confirmdelete'=>'1',
                    'do_show'=>'edit');
$mform->set_data($newformdata);
$formdata = $mform->get_data();

if ($mform->is_cancelled()) {
    redirect('show_entries.php?id='.$id.'&do_show=showentries');
}

if(isset($formdata->confirmdelete) AND $formdata->confirmdelete == 1){
    if($completed = $DB->get_record('feedback_completed', array('id'=>$completedid))) {
        feedback_delete_completed($completedid);
        add_to_log($course->id, 'feedback', 'delete', 'view.php?id='.$cm->id, $feedback->id,$cm->id);
        redirect('show_entries.php?id='.$id.'&do_show=showentries');
    }
}

/// Print the page header
$strfeedbacks = get_string("modulenameplural", "feedback");
$strfeedback  = get_string("modulename", "feedback");

$PAGE->navbar->add($strfeedbacks, new moodle_url($CFG->wwwroot.'/mod/feedback/index.php', array('id'=>$course->id)));
$PAGE->navbar->add(format_string($feedback->name));

$PAGE->set_title(format_string($feedback->name));
echo $OUTPUT->header();

/// Print the main part of the page
///////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////
echo $OUTPUT->heading(format_text($feedback->name));
echo $OUTPUT->box_start('generalbox errorboxcontent boxaligncenter boxwidthnormal');
echo $OUTPUT->heading(get_string('confirmdeleteentry', 'feedback'));
$mform->display();
echo $OUTPUT->box_end();


echo $OUTPUT->footer();

