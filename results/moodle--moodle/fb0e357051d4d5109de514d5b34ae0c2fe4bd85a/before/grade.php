<?php  //$Id$

require_once '../../../config.php';
require_once $CFG->dirroot.'/grade/lib.php';
require_once 'grade_form.php';

$courseid = required_param('courseid', PARAM_INT);
$id       = optional_param('id', 0, PARAM_INT);
$itemid   = optional_param('itemid', 0, PARAM_INT);
$userid   = optional_param('userid', 0, PARAM_INT);

if (!$course = get_record('course', 'id', $courseid)) {
    print_error('nocourseid');
}

require_login($course);
$context = get_context_instance(CONTEXT_COURSE, $course->id);
if (!has_capability('moodle/grade:manage', $context)) {
    require_capability('moodle/grade:override', $context);
}

// default return url
$gpr = new grade_plugin_return();
$returnurl = $gpr->get_return_url($CFG->wwwroot.'/grade/report.php?id='.$course->id);

// security checks!
if (!empty($id)) {
    if (!$grade = get_record('grade_grades', 'id', $id)) {
        error('Incorrect grade id');
    }

    if (!empty($itemid) and $itemid != $grade->itemid) {
        error('Incorrect itemid');
    }
    $itemid = $grade->itemid;

    if (!empty($userid) and $userid != $grade->userid) {
        error('Incorrect userid');
    }
    $userid = $grade->userid;

    unset($grade);

} else if (empty($userid) or empty($itemid)) {
    error('Missing userid and itemid');
}

if (!$grade_item = grade_item::fetch(array('id'=>$itemid, 'courseid'=>$courseid))) {
    error('Can not find grade_item');
}

// now verify grading user has access to all groups or is member of the same group when separate groups used in course
if (groupmode($COURSE) == SEPARATEGROUPS and !has_capability('moodle/site:accessallgroups', $context)) {
    if ($groups = user_group($COURSE->id, $userid)) {
        $ok = false;
        foreach ($groups as $group) {
            if (groups_is_member($group->id, $USER->id)) {
                $ok = true;
            }
        }
        if (!$ok) {
            error('Can not grade this user');
        }
    } else {
        error('Can not grade this user');
    }
}

$mform = new edit_grade_form(null, array('grade_item'=>$grade_item, 'gpr'=>$gpr));

if ($grade = get_record('grade_grades', 'itemid', $grade_item->id, 'userid', $userid)) {
    if ($grade_text = get_record('grade_grades_text', 'gradeid', $grade->id)) {
        // always clean existing feedback - grading should not have XSS risk
        if (can_use_html_editor()) {
            $options = new object();
            $options->smiley  = false;
            $options->filter  = false;
            $options->noclean = false;
            $grade->feedback       = format_text($grade_text->feedback, $grade_text->feedbackformat, $options);
            $grade->feedbackformat = FORMAT_HTML;
        } else {
            $grade->feedback       = clean_text($grade_text->feedback, $grade_text->feedbackformat);
            $grade->feedbackformat = $grade_text->feedbackformat;
        }
    }

    $grade->locked      = $grade->locked     > 0 ? 1:0;
    $grade->overridden  = $grade->overridden > 0 ? 1:0;
    $grade->excluded    = $grade->excluded   > 0 ? 1:0;

    if ($grade->hidden > 1) {
        $grade->hiddenuntil = $grade->hidden;
        $grade->hidden = 0;
    } else {
        $grade->hiddenuntil = 0;
    }

    $mform->set_data($grade);

} else {
    $mform->set_data(array('itemid'=>$itemid, 'userid'=>$userid, 'locked'=>$grade_item->locked, 'locktime'=>$grade_item->locktime));
}

if ($mform->is_cancelled()) {
    redirect($returnurl);

// form processing
} else if ($data = $mform->get_data(false)) {
    $old_grade_grade = new grade_grade(array('userid'=>$data->userid, 'itemid'=>$grade_item->id), true); //might not exist yet

    // fix no grade for scales
    if ($grade_item->gradetype == GRADE_TYPE_SCALE and $data->finalgrade < 1) {
        $data->finalgrade = NULL;
    }

    // update final grade or feedback
    $grade_item->update_final_grade($data->userid, $data->finalgrade, NULL, 'editgrade', $data->feedback, $data->feedbackformat);

    $grade_grade = grade_grade::fetch(array('userid'=>$data->userid, 'itemid'=>$grade_item->id));

    if (has_capability('moodle/grade:manage', $context) or has_capability('moodle/grade:hide', $context)) {
        if (empty($data->hidden)) {
            if (empty($data->hiddenuntil)) {
                $grade_grade->set_hidden(0);
            } else {
                $grade_grade->set_hidden($data->hiddenuntil);
            }
        } else {
            $grade_grade->set_hidden(1);
        }
    }

    if (has_capability('moodle/grade:manage', $context) or has_capability('moodle/grade:override', $context)) {
        // ignore overridden flag when changing final grade
        if ($old_grade_grade->finalgrade == $grade_grade->finalgrade) {
            if ($grade_grade->set_overridden($data->overridden) and empty($data->overridden)) {
                $grade_item->force_regrading(); // force regrading only when clearing the flag
            }
        }
    }

    if (has_capability('moodle/grade:manage', $context)) {
        if ($grade_grade->set_excluded($data->excluded)) {
            $grade_item->force_regrading();
        }
    }

    if (($old_grade_grade->locked or $old_grade_grade->locktime)
      and (!has_capability('moodle/grade:manage', $context) and !has_capability('moodle/grade:unlock', $context))) {
        //ignore data

    } else if ((!$old_grade_grade->locked and !$old_grade_grade->locktime)
      and (!has_capability('moodle/grade:manage', $context) and !has_capability('moodle/grade:lock', $context))) {
        //ignore data

    } else {
        $grade_grade->set_locked($data->locked);
        $grade_grade->set_locktime($data->locktime);
      }

    redirect($returnurl);
}

$strgrades       = get_string('grades');
$strgraderreport = get_string('graderreport', 'grades');
$strgradeedit    = get_string('editgrade', 'grades');
$struser         = get_string('user');

$navigation = grade_build_nav(__FILE__, $strgradeedit, array('courseid' => $courseid));

/*********** BEGIN OUTPUT *************/

print_header_simple($strgrades . ': ' . $strgraderreport . ': ' . $strgradeedit,
    ': ' . $strgradeedit , $navigation, '', '', true, '', navmenu($course));

print_heading($strgradeedit);

print_simple_box_start("center");

// Form if in edit or add modes
$mform->display();

print_simple_box_end();

print_footer($course);
die;