<?php  // $Id$
/**
 * This page prints a summary of a quiz attempt before it is submitted.
 *
 * @author Tim Hunt others.
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package quiz
 */

require_once("../../config.php");
require_once("locallib.php");
require_once("attemptlib.php");

$attemptid = required_param('attempt', PARAM_INT); // The attempt to summarise.
$attemptobj = new quiz_attempt($attemptid);

/// Check login and get contexts.
require_login($attemptobj->get_courseid(), false, $attemptobj->get_cm());

/// If this is not our own attempt, display an error.
if ($attemptobj->get_userid() != $USER->id) {
    print_error('notyourattempt', 'quiz', $attemptobj->view_url());
}

/// If the attempt is already closed, redirect them to the review page.
if ($attemptobj->is_finished()) {
    redirect($attemptobj->review_url());
}

/// Check access.
$accessmanager = $attemptobj->get_access_manager(time());
$messages = $accessmanager->prevent_access();
if (!$attemptobj->is_preview_user() && $messages) {
    print_error('attempterror', 'quiz', $attemptobj->view_url(),
            $accessmanager->print_messages($messages, true));
}
$accessmanager->do_password_check($attemptobj->is_preview_user());

/// Log this page view.
add_to_log($attemptobj->get_courseid(), 'quiz', 'view summary', 'summary.php?attempt=' . $attemptobj->get_attemptid(),
        $attemptobj->get_quizid(), $attemptobj->get_cmid());

/// Load the questions and states.
$attemptobj->load_questions();
$attemptobj->load_question_states();

/// Print the page header
require_js($CFG->wwwroot . '/mod/quiz/quiz.js');
$title = get_string('summaryofattempt', 'quiz');
if ($accessmanager->securewindow_required($attemptobj->is_preview_user())) {
    $accessmanager->setup_secure_page($course->shortname.': '.format_string($quiz->name), $headtags);
} else {
    print_header_simple(format_string($attemptobj->get_quiz_name()), '',
            $attemptobj->navigation($title), '', '', true, $attemptobj->update_module_button());
}

/// Print tabs if they should be there.
if ($attemptobj->is_preview_user()) {
    $currenttab = 'preview';
    include('tabs.php');
}

/// Print heading.
print_heading(format_string($attemptobj->get_quiz_name()));
if ($attemptobj->is_preview_user()) {
    print_restart_preview_button($quiz);
}
print_heading($title);

/// Prepare the summary table header
$table->class = 'generaltable quizsummaryofattempt';
$table->head = array(get_string('question', 'quiz'), get_string('status', 'quiz'));
$table->align = array('left', 'left');
$table->size = array('', '');
$scorescolumn = $attemptobj->get_review_options()->scores;
if ($scorescolumn) {
    $table->head[] = get_string('marks', 'quiz');
    $table->align[] = 'left';
    $table->size[] = '';
}
$table->data = array();

/// Get the summary info for each question.
$questionids = $attemptobj->get_question_ids();
foreach ($attemptobj->get_question_iterator() as $number => $question) {
    $row = array($number, $attemptobj->get_question_status($question->id));
    if ($scorescolumn) {
        $row[] = $attemptobj->get_question_score($question->id);
    }
    $table->data[] = $row;
}

/// Print the summary table.
print_table($table);

/// Finish attempt button.
echo "<div class=\"submitbtns mdl-align\">\n";
$options = array(
    'finishattempt' => 1,
    'timeup' => 0,
    'questionids' => '',
    'sesskey' => sesskey()
);
print_single_button($attemptobj->attempt_url(), $options, get_string('finishattempt', 'quiz'),
        'post', '', false, '', false, get_string('confirmclose', 'quiz'));
echo "</div>\n";

/// Finish the page
$accessmanager->show_attempt_timer_if_needed($attemptobj->get_attempt(), time());
if ($accessmanager->securewindow_required($attemptobj->is_preview_user())) {
    print_footer('empty');
} else {
    print_footer($course);
}

?>