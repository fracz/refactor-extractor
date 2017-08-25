<?php  // $Id$
/**
 * This page prints a particular instance of quiz
 *
 * @author Martin Dougiamas and many others. This has recently been completely
 *         rewritten by Alex Smith, Julian Sedding and Gustav Delius as part of
 *         the Serving Mathematics project
 *         {@link http://maths.york.ac.uk/serving_maths}
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package quiz
 */

    require_once(dirname(__FILE__) . '/../../config.php');
    require_once($CFG->dirroot . '/mod/quiz/locallib.php');

/// remember the current time as the time any responses were submitted
/// (so as to make sure students don't get penalized for slow processing on this page)
    $timenow = time();

/// Get submitted parameters.
    $attemptid = required_param('attempt', PARAM_INT);
    $page = optional_param('page', 0, PARAM_INT);
    $submittedquestionids = optional_param('questionids', '', PARAM_SEQUENCE);
    $finishattempt = optional_param('finishattempt', 0, PARAM_BOOL);
    $timeup = optional_param('timeup', 0, PARAM_BOOL); // True if form was submitted by timer.

    $attemptobj = new quiz_attempt($attemptid);

/// We treat automatically closed attempts just like normally closed attempts
    if ($timeup) {
        $finishattempt = 1;
    }

/// Check login and get contexts.
    require_login($attemptobj->get_courseid(), false, $attemptobj->get_cm());

/// Check capabilites.
    if (!$attemptobj->is_preview_user()) {
        require_capability('mod/quiz:attempt', $context);
    }

/// Log continuation of the attempt, but only if some time has passed.
    if (($timenow - $attemptobj->get_attempt()->timemodified) > QUIZ_CONTINUE_ATTEMPT_LOG_INTERVAL) {
    /// This action used to be 'continue attempt' but the database field has only 15 characters.
        add_to_log($attemptobj->get_courseid(), 'quiz', 'continue attemp',
                'review.php?attempt=' . $attemptobj->get_attemptid(),
                $attemptobj->get_quizid(), $attemptobj->get_cmid());
    }

/// Work out which questions we need.
    $attemptobj->preload_questions();

/// Get the list of questions needed by this page.
    if ($finishattempt) {
        $questionids = $attemptobj->get_question_ids();
    } else if ($page >= 0) {
        $questionids = $attemptobj->get_question_ids($page);
    } else {
        $questionids = array();
    }

/// Add all questions that are on the submitted form
    if ($submittedquestionids) {
        $submittedquestionids = explode(',', $submittedquestionids);
        $questionids = $questionids + $submittedquestionids;
    } else {
        $submittedquestionids = array();
    }

/// Check.
    if (empty($questionids)) {
        quiz_error($quiz, 'noquestionsfound');
    }

/// Load those questions and the associated states.
    $attemptobj->load_questions($questionids);
    $attemptobj->load_question_states($questionids);


/// Process form data /////////////////////////////////////////////////

    if ($responses = data_submitted() and empty($responses->quizpassword)) {

    /// Set the default event. This can be overruled by individual buttons.
        if (array_key_exists('markall', $responses)) {
            $event = QUESTION_EVENTSUBMIT;
        } else if ($finishattempt) {
            $event = QUESTION_EVENTCLOSE;
        } else {
            $event = QUESTION_EVENTSAVE;
        }

    /// Unset any variables we know are not responses
        unset($responses->id);
        unset($responses->q);
        unset($responses->oldpage);
        unset($responses->newpage);
        unset($responses->review);
        unset($responses->questionids);
        unset($responses->saveattempt); // responses get saved anway
        unset($responses->finishattempt); // same as $finishattempt
        unset($responses->markall);
        unset($responses->forcenewattempt);

    /// Extract the responses. $actions will be an array indexed by the questions ids.
        $actions = question_extract_responses($attemptobj->get_questions(), $responses, $event);

    /// Process each question in turn
        $success = true;
        foreach($submittedquestionids as $id) {
            if (!isset($actions[$id])) {
                $actions[$id]->responses = array('' => '');
                $actions[$id]->event = QUESTION_EVENTOPEN;
            }
            $actions[$id]->timestamp = $timenow;
            if (question_process_responses($attemptobj->get_question($id),
                    $attemptobj->get_question_state($id), $actions[$id],
                    $attemptobj->get_quiz(), $attemptobj->get_attempt())) {
                save_question_session($attemptobj->get_question($id),
                        $attemptobj->get_question_state($id));
            } else {
                $success = false;
            }
        }

        if (!$success) {
            print_error('errorprocessingresponses', 'question', $attemptobj->attempt_url(0, $page));
        }

        $attempt = $attemptobj->get_attempt();
        $attempt->timemodified = $timenow;
        if (!$DB->update_record('quiz_attempts', $attempt)) {
            quiz_error($quiz, 'saveattemptfailed');
        }
    }

/// Finish attempt if requested
    if ($finishattempt) {

    /// Move each question to the closed state.
        $success = true;
        foreach ($attemptobj->get_questions() as $id => $question) {
            $action = new stdClass;
            $action->event = QUESTION_EVENTCLOSE;
            $action->responses = $attemptobj->get_question_state($id)->responses;
            $action->timestamp = $attemptobj->get_question_state($id)->timestamp;
            if (question_process_responses($attemptobj->get_question($id),
                    $attemptobj->get_question_state($id), $action,
                    $attemptobj->get_quiz(), $attemptobj->get_attempt())) {
                save_question_session($attemptobj->get_question($id),
                        $attemptobj->get_question_state($id));
            } else {
                $success = false;
            }
        }

        if (!$success) {
            print_error('errorprocessingresponses', 'question', $attemptobj->attempt_url(0, $page));
        }

    /// Log the end of this attempt.
        add_to_log($attemptobj->get_courseid(), 'quiz', 'close attempt',
                'review.php?attempt=' . $attemptobj->get_attemptid(),
                $attemptobj->get_quizid(), $attemptobj->get_cmid());

    /// Update the quiz attempt record.
        $attempt = $attemptobj->get_attempt();
        $attempt->timemodified = $timenow;
        $attempt->timefinish = $timenow;
        if (!$DB->update_record('quiz_attempts', $attempt)) {
            quiz_error($quiz, 'saveattemptfailed');
        }

        if (!$attempt->preview) {
        /// Record this user's best grade (if this is not a preview).
            quiz_save_best_grade($quiz);

        /// Send any notification emails (if this is not a preview).
            $attemptobj->quiz_send_notification_emails();
        }

    /// Clear the password check flag in the session.
        $accessmanager = $attemptobj->get_access_manager($timenow);
        $accessmanager->clear_password_access();

    /// Send the user to the review page.
        redirect($attemptobj->review_url());
    }

/// Now is the right time to check access.
    $accessmanager = $attemptobj->get_access_manager($timenow);
    $messages = $accessmanager->prevent_access();
    if (!$attemptobj->is_preview_user() && $messages) {
        print_error('attempterror', 'quiz', $quizobj->view_url(),
                $accessmanager->print_messages($messages, true));
    }
    $accessmanager->do_password_check($attemptobj->is_preview_user());

/// Having processed the responses, we want to go to the summary page.
if ($page == -1) {
    redirect($attemptobj->summary_url());
}

/// Print the quiz page ////////////////////////////////////////////////////////

    // Print the page header
    require_js($CFG->wwwroot . '/mod/quiz/quiz.js');
    $title = get_string('attempt', 'quiz', $attemptobj->get_attempt_number());
    $headtags = $attemptobj->get_html_head_contributions($page);
    if ($accessmanager->securewindow_required($attemptobj->is_preview_user())) {
        $accessmanager->setup_secure_page($attemptobj->get_course()->shortname . ': ' .
                format_string($attemptobj->get_quiz_name()), $headtags);
    } else {
        print_header_simple(format_string($attemptobj->get_quiz_name()), '', $attemptobj->navigation($title),
                '', $headtags, true, $attemptobj->update_module_button());
    }
    echo '<div id="overDiv" style="position:absolute; visibility:hidden; z-index:1000;"></div>'; // for overlib

    if ($attemptobj->is_preview_user()) {
    /// Show the tab bar.
        $currenttab = 'preview';
        include('tabs.php');

    /// Heading and tab bar.
        print_heading(get_string('previewquiz', 'quiz', format_string($quiz->name)));
        $attemptobj->print_restart_preview_button();

    /// Inform teachers of any restrictions that would apply to students at this point.
        if ($messages) {
            print_box_start('quizaccessnotices');
            print_heading(get_string('accessnoticesheader', 'quiz'), '', 3);
            $accessmanager->print_messages($messages);
            print_box_end();
        }
    } else {
    /// Just a heading.
        if ($quiz->attempts != 1) {
            print_heading(format_string($quiz->name).' - '.$title);
        } else {
            print_heading(format_string($quiz->name));
        }
    }

    // Start the form
    echo '<form id="responseform" method="post" action="', $attemptobj->attempt_url(0, $page),
            '" enctype="multipart/form-data"' .
            ' onclick="this.autocomplete=\'off\'" onkeypress="return check_enter(event);">', "\n";
    if($attemptobj->get_quiz()->timelimit > 0) {
        // Make sure javascript is enabled for time limited quizzes
        ?>
        <script type="text/javascript">
            // Do nothing, but you have to have a script tag before a noscript tag.
        </script>
        <noscript>
        <div>
        <?php print_heading(get_string('noscript', 'quiz')); ?>
        </div>
        </noscript>
        <?php
    }
    echo '<div>';

/// Print the navigation panel if required
    // TODO!!!
    quiz_print_navigation_panel($page, $attemptobj->get_num_pages());

/// Print all the questions
    foreach ($attemptobj->get_question_ids($page) as $id) {
        $attemptobj->print_question($id);
    }

/// Print a link to the next page.
    echo "<div class=\"submitbtns mdl-align\">\n";
    if ($attemptobj->is_last_page($page)) {
        $nextpage = -1;
    } else {
        $nextpage = $page + 1;
    }
    echo link_arrow_right(get_string('next'), 'javascript:navigate(' . $nextpage . ')');
    echo "</div>";

    // Finish the form
    echo '</div>';
    echo '<input type="hidden" name="timeup" id="timeup" value="0" />';

    // Add a hidden field with questionids. Do this at the end of the form, so
    // if you navigate before the form has finished loading, it does not wipe all
    // the student's answers.
    echo '<input type="hidden" name="questionids" value="' .
            implode(',', $attemptobj->get_question_ids($page)) . "\" />\n";

    echo "</form>\n";

    // Finish the page
    $accessmanager->show_attempt_timer_if_needed($attemptobj->get_attempt(), time());
    if ($accessmanager->securewindow_required($attemptobj->is_preview_user())) {
        print_footer('empty');
    } else {
        print_footer($attemptobj->get_course());
    }
?>