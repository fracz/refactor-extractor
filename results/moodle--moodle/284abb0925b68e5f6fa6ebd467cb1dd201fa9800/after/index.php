<?php // $Id$

require_once '../../../config.php';
require_once $CFG->dirroot.'/grade/lib.php';
require_once $CFG->libdir.'/gradelib.php';

$courseid  = optional_param('id', SITEID, PARAM_INT);
$action   = optional_param('action', '', PARAM_ALPHA);

if (!$course = get_record('course', 'id', $courseid)) {
    print_error('nocourseid');
}
require_login($course);
$context = get_context_instance(CONTEXT_COURSE, $course->id);
require_capability('moodle/grade:manage', $context);

$gpr = new grade_plugin_return(array('type'=>'edit', 'plugin'=>'letter', 'courseid'=>$courseid));

$strgrades = get_string('grades');
$pagename  = get_string('letters', 'grades');

$navigation = grade_build_nav(__FILE__, $pagename, $courseid);

/// Print header
print_header_simple($strgrades.': '.$pagename, ': '.$strgrades, $navigation, '', '', true, '', navmenu($course));
/// Print the plugin selector at the top
print_grade_plugin_selector($courseid, 'edit', 'letter');

$currenttab = 'lettersview';
require('tabs.php');

$letters = grade_get_letters($context);

$data = array();

$max = 100;
foreach($letters as $boundary=>$letter) {
    $line = array();
    $line[] = format_float($max,2).' %';
    $line[] = format_float($boundary,2).' %';
    $line[] = format_string($letter);
    $data[] = $line;
    $max = $boundary - 0.01;
}

$table = new object();
$table->head  = array(get_string('max', 'grades'), get_string('min', 'grades'), get_string('letter', 'grades'));
$table->size  = array('30%', '30%', '40%');
$table->align = array('left', 'left', 'left');
$table->width = '30%';
$table->data  = $data;
print_table($table);

print_footer($course);

?>
