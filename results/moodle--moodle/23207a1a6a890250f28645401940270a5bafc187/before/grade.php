<?php  //$Id$

require_once '../../config.php';
require_once $CFG->dirroot.'/grade/lib.php';
require_once $CFG->libdir.'/gradelib.php';
require_once 'grade_form.php';

$courseid = required_param('courseid', PARAM_INT);
$id       = optional_param('id', 0, PARAM_INT); // grade_grade id
$action   = optional_param('action', 'view', PARAM_ALPHA);

if (!$course = get_record('course', 'id', $courseid)) {
    print_error('nocourseid');
}

// capabilities check
require_login($course);
$context = get_context_instance(CONTEXT_COURSE, $course->id);
require_capability('gradereport/grader:manage', $context);

// default return url
$gpr = new grade_plugin_return();
$returnurl = $gpr->get_return_url($CFG->wwwroot.'/grade/report.php?id='.$course->id);

// TODO: add proper check that grade is editable

$grade_grade = get_record('grade_grades', 'id', $id);
$gradeitem = get_record('grade_items', 'id', $grade_grade->itemid);

$mform = new edit_grade_form(null, array('gradeitem'=>$gradeitem, 'gpr'=>$gpr));
if ($grade_grade = get_record('grade_grades', 'id', $id)) {
    if ($grade_text = get_record('grade_grades_text', 'gradeid', $id)) {
        if (can_use_html_editor()) {
            $options = new object();
            $options->smiley = false;
            $options->filter = false;
            $grade_text->feedback = format_text($grade_text->feedback, $grade_text->feedbackformat, $options);
            $grade_text->feedbackformat = FORMAT_HTML;
        }
        $mform->set_data($grade_text);
    }

    $grade_grade->locked = $grade_grade->locked > 0 ? 1:0;
    $grade_grade->courseid = $courseid;
    $mform->set_data($grade_grade);

} else {
    $mform->set_data(array('courseid'=>$course->id, 'id' => $id));
}

if ($mform->is_cancelled()) {
    redirect($returnurl);
// form processing
} else if ($data = $mform->get_data()) {
    $grade_grade = new grade_grade(array('id'=>$id));
    $grade_item = new grade_item(array('id'=>$grade_grade->itemid));
    $grade_item->update_final_grade($grade_grade->userid, $data->finalgrade, NULL, NULL, $data->feedback, $data->feedbackformat);

    // Assign finalgrade value
    $grade_grade->finalgrade = $data->finalgrade;

    // set locked
    $grade_grade->set_locked($data->locked);

    // set hidden
    $grade_grade->set_hidden($data->hidden);

    // set locktime
    $grade_grade->set_locktime($data->locktime);

    redirect($returnurl);
}

// Get extra data related to this feedback
$query = "SELECT a.id AS userid, a.firstname, a.lastname,
                 b.id AS itemid, b.itemname, b.grademin, b.grademax, b.iteminstance, b.itemmodule, b.scaleid,
                 c.finalgrade
            FROM {$CFG->prefix}user a,
                 {$CFG->prefix}grade_items b,
                 {$CFG->prefix}grade_grades c
           WHERE c.id = $id
             AND b.id = c.itemid
             AND a.id = c.userid";

$extra_info = get_record_sql($query) ;
$extra_info->grademin = round($extra_info->grademin);
$extra_info->grademax = round($extra_info->grademax);
$extra_info->finalgrade = round($extra_info->finalgrade);

if (!empty($extra_info->itemmodule) && !empty($extra_info->iteminstance)) {
    $extra_info->course_module = get_coursemodule_from_instance($extra_info->itemmodule, $extra_info->iteminstance, $courseid);
}

$stronascaleof   = get_string('onascaleof', 'grades', $extra_info);
$strgrades       = get_string('grades');
$strgrade        = get_string('grade');
$strgraderreport = get_string('graderreport', 'grades');
$strgrade     = get_string('grade', 'grades');
$strgradeedit = get_string('gradeedit', 'grades');
$strgradeview = get_string('gradeview', 'grades');
$strstudent      = get_string('student', 'grades');
$strgradeitem    = get_string('gradeitem', 'grades');

$feedback = null;
$heading = ${"strgrade$action"};
if (!empty($action) && $action == 'view' && !empty($grade_text->feedback)) {
    $feedback = "<p><strong>$strgrade</strong>:</p><p>$grade_text->feedback</p>";
}

$nav = array(array('name'=>$strgrades,'link'=>$CFG->wwwroot.'/grade/index.php?id='.$courseid, 'type'=>'misc'),
             array('name'=>$heading, 'link'=>'', 'type'=>'misc'));

$navigation = build_navigation($nav);

/*********** BEGIN OUTPUT *************/

print_header_simple($strgrades . ': ' . $strgraderreport . ': ' . $heading,
    ': ' . $heading , $navigation, '', '', true, '', navmenu($course));

print_heading($heading);

print_simple_box_start("center");

// Student name and link
echo "<p><strong>$strstudent:</strong> <a href=\"" . $CFG->wwwroot . '/user/view.php?id='
     . $extra_info->userid . '">' . fullname($extra_info) . "</a></p>";

// Grade item name and link
if (!empty($extra_info->course_module) && !empty($extra_info->itemmodule)) {
    echo "<p><strong>$strgradeitem:</strong> <a href=\"" . $CFG->wwwroot . '/mod/' . $extra_info->itemmodule
         . '/view.php?id=' . $extra_info->course_module->id . "&amp;courseid=$courseid\">$extra_info->itemname</a></p>";
}

// Form if in edit or add modes
$mform->display();

print_simple_box_end();

print_footer($course);
die;