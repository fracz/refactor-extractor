<?php  // $Id$

/// This page prints a particular instance of quiz

    require_once("../../config.php");
    require_once($CFG->libdir.'/blocklib.php');
    require_once($CFG->libdir.'/gradelib.php');
    require_once($CFG->dirroot.'/mod/quiz/locallib.php');
    require_once($CFG->dirroot.'/mod/quiz/pagelib.php');

    $id = optional_param('id', 0, PARAM_INT); // Course Module ID, or
    $q = optional_param('q',  0, PARAM_INT);  // quiz ID

    if ($id) {
        if (! $cm = get_coursemodule_from_id('quiz', $id)) {
            print_error("There is no coursemodule with id $id");
        }
        if (! $course = get_record("course", "id", $cm->course)) {
            print_error("Course is misconfigured");
        }
        if (! $quiz = get_record("quiz", "id", $cm->instance)) {
            print_error("The quiz with id $cm->instance corresponding to this coursemodule $id is missing");
        }
    } else {
        if (! $quiz = get_record("quiz", "id", $q)) {
            print_error("There is no quiz with id $q");
        }
        if (! $course = get_record("course", "id", $quiz->course)) {
            print_error("The course with id $quiz->course that the quiz with id $q belongs to is missing");
        }
        if (! $cm = get_coursemodule_from_instance("quiz", $quiz->id, $course->id)) {
            print_error("The course module for the quiz with id $q is missing");
        }
    }

/// Check login and get context.
    require_login($course->id, false, $cm);
    $context = get_context_instance(CONTEXT_MODULE, $cm->id);
    require_capability('mod/quiz:view', $context);

/// Cache some other capabilites we use several times.
    $canattempt = has_capability('mod/quiz:attempt', $context);
    $canpreview = has_capability('mod/quiz:preview', $context);

/// Create an object to manage all the other (non-roles) access rules.
    $timenow = time();
    $accessmanager = new quiz_access_manager($quiz, $timenow,
            has_capability('mod/quiz:ignoretimelimits', $context, NULL, false));

/// If no questions have been set up yet redirect to edit.php
    if (!$quiz->questions && has_capability('mod/quiz:manage', $context)) {
        redirect($CFG->wwwroot . '/mod/quiz/edit.php?cmid=' . $cm->id);
    }

/// Log this request.
    add_to_log($course->id, "quiz", "view", "view.php?id=$cm->id", $quiz->id, $cm->id);

/// Initialize $PAGE, compute blocks
    $PAGE       = page_create_instance($quiz->id);
    $pageblocks = blocks_setup($PAGE);
    $blocks_preferred_width = bounded_number(180, blocks_preferred_width($pageblocks[BLOCK_POS_LEFT]), 210);

    $edit = optional_param('edit', -1, PARAM_BOOL);
    if ($edit != -1 && $PAGE->user_allowed_editing()) {
        $USER->editing = $edit;
    }

/// Print the page header
    $bodytags = '';
    if ($accessmanager->securewindow_required($canpreview)) {
        $bodytags = 'onload="popupchecker(\'' . get_string('popupblockerwarning', 'quiz') . '\');"';
    }
    require_js('yui_yahoo');
    require_js('yui_event');
    $PAGE->print_header($course->shortname.': %fullname%','',$bodytags);

/// Print any blocks on the left of the page.
    echo '<table id="layout-table"><tr>';
    if(!empty($CFG->showblocksonmodpages) && (blocks_have_content($pageblocks, BLOCK_POS_LEFT) || $PAGE->user_is_editing())) {
        echo '<td style="width: '.$blocks_preferred_width.'px;" id="left-column">';
        print_container_start();
        blocks_print_group($PAGE, $pageblocks, BLOCK_POS_LEFT);
        print_container_end();
        echo "</td>\n";
    }

/// Start the main part of the page
    echo '<td id="middle-column">';
    print_container_start();

/// Print heading and tabs (if there is more than one).
    $currenttab = 'info';
    include('tabs.php');

/// Print quiz name and description
    print_heading(format_string($quiz->name));
    if (trim(strip_tags($quiz->intro))) {
        $formatoptions->noclean = true;
        print_box(format_text($quiz->intro, FORMAT_MOODLE, $formatoptions), 'generalbox', 'intro');
    }

/// Display information about this quiz.
    $messages = $accessmanager->describe_rules();
    if ($quiz->attempts != 1) {
        $messages[] = get_string('gradingmethod', 'quiz', quiz_get_grading_option_name($quiz->grademethod));
    }
    print_box_start('quizinfo');
    $accessmanager->print_messages($messages);
    print_box_end();

/// Show number of attempts summary to those who can view reports.
    if (has_capability('mod/quiz:viewreports', $context)) {
        if ($strattemptnum = quiz_num_attempt_summary($quiz, $cm)) {
            echo '<div class="quizattemptcounts"><a href="report.php?mode=overview&amp;id=' .
                    $cm->id . '">' . $strattemptnum . "</a></div>\n";
        }
    }

/// Guests can't do a quiz, so offer them a choice of logging in or going back.
    if (isguestuser()) {
        $loginurl = $CFG->wwwroot.'/login/index.php';
        if (!empty($CFG->loginhttps)) {
            $loginurl = str_replace('http:','https:', $loginurl);
        }

        notice_yesno('<p>' . get_string('guestsno', 'quiz') . "</p>\n\n<p>" .
                get_string('liketologin') . "</p>\n", $loginurl, get_referer(false));
        finish_page($course);
    }

/// If they are not using guest access, and they can't do the quiz, tell them that.
    if (!($canattempt || $canpreview)) {
        print_box('<p>' . get_string('youneedtoenrol', 'quiz') . "</p>\n\n<p>" .
                print_continue($CFG->wwwroot . '/course/view.php?id=' . $course->id, true) .
                "</p>\n", 'generalbox', 'notice');
        finish_page($course);
    }

/// Get this user's attempts.
    $attempts = quiz_get_user_attempts($quiz->id, $USER->id);
    $lastfinishedattempt = end($attempts);
    $unfinished = false;
    if ($unfinishedattempt = quiz_get_user_attempt_unfinished($quiz->id, $USER->id)) {
        $attempts[] = $unfinishedattempt;
        $unfinished = true;
    }
    $numattempts = count($attempts);

/// Work out the final grade, checking whether it was overridden in the gradebook.
    $mygrade = quiz_get_best_grade($quiz, $USER->id);
    $mygradeoverridden = false;
    $gradebookfeedback = '';

    $grading_info = grade_get_grades($course->id, 'mod', 'quiz', $quiz->id, $USER->id);
    if (!empty($grading_info->items)) {
        $item = $grading_info->items[0];
        if (isset($item->grades[$USER->id])) {
            $grade = $item->grades[$USER->id];

            if ($grade->overridden) {
                $mygrade = $grade->grade + 0; // Convert to number.
                $mygradeoverridden = true;
            }
            if (!empty($grade->str_feedback)) {
                $gradebookfeedback = $grade->str_feedback;
            }
        }
    }

/// Print table with existing attempts
    if ($attempts) {

        print_heading(get_string('summaryofattempts', 'quiz'));

        // Get some strings.
        $strattempt       = get_string("attempt", "quiz");
        $strtimetaken     = get_string("timetaken", "quiz");
        $strtimecompleted = get_string("timecompleted", "quiz");
        $strgrade         = get_string("grade");
        $strmarks         = get_string('marks', 'quiz');
        $strfeedback      = get_string('feedback', 'quiz');

        // Work out which columns we need, taking account what data is available in each attempt.
        list($someoptions, $alloptions) = quiz_get_combined_reviewoptions($quiz, $attempts, $context);

        $gradecolumn = $someoptions->scores && $quiz->grade && $quiz->sumgrades;
        $markcolumn = $gradecolumn && ($quiz->grade != $quiz->sumgrades);
        $overallstats = $alloptions->scores;

        $feedbackcolumn = quiz_has_feedback($quiz->id);
        $overallfeedback = $feedbackcolumn && $alloptions->overallfeedback;

        // Prepare table header
        $table->class = 'generaltable quizattemptsummary';
        $table->head = array($strattempt, $strtimecompleted);
        $table->align = array("center", "left");
        $table->size = array("", "");
        if ($markcolumn) {
            $table->head[] = "$strmarks / $quiz->sumgrades";
            $table->align[] = 'center';
            $table->size[] = '';
        }
        if ($gradecolumn) {
            $table->head[] = "$strgrade / $quiz->grade";
            $table->align[] = 'center';
            $table->size[] = '';
        }
        if ($feedbackcolumn) {
            $table->head[] = $strfeedback;
            $table->align[] = 'left';
            $table->size[] = '';
        }
        if (isset($quiz->showtimetaken)) {
            $table->head[] = $strtimetaken;
            $table->align[] = 'left';
            $table->size[] = '';
        }

        // One row for each attempt
        foreach ($attempts as $attempt) {
            $attemptoptions = quiz_get_reviewoptions($quiz, $attempt, $context);
            $row = array();

            // Add the attempt number, making it a link, if appropriate.
            if ($attempt->preview) {
                $row[] = $accessmanager->make_review_link(get_string('preview', 'quiz'), $attempt, $canpreview, $attemptoptions);
            } else {
                $row[] = $accessmanager->make_review_link($attempt->attempt, $attempt, $canpreview, $attemptoptions);
            }

            // prepare strings for time taken and date completed
            $timetaken = '';
            $datecompleted = '';
            if ($attempt->timefinish > 0) {
                // attempt has finished
                $timetaken = format_time($attempt->timefinish - $attempt->timestart);
                $datecompleted = userdate($attempt->timefinish);
            } else if (!$quiz->timeclose || $timenow < $quiz->timeclose) {
                // The attempt is still in progress.
                $timetaken = format_time($timenow - $attempt->timestart);
                $datecompleted = '';
            } else {
                $timetaken = format_time($quiz->timeclose - $attempt->timestart);
                $datecompleted = userdate($quiz->timeclose);
            }
            $row[] = $datecompleted;

            if ($markcolumn && $attempt->timefinish > 0) {
                if ($attemptoptions->scores) {
                    $row[] = $accessmanager->make_review_link(round($attempt->sumgrades, $quiz->decimalpoints),
                            $attempt, $canpreview, $attemptoptions);
                } else {
                    $row[] = '';
                }
            }

            // Ouside the if because we may be showing feedback but not grades.
            $attemptgrade = quiz_rescale_grade($attempt->sumgrades, $quiz);

            if ($gradecolumn) {
                if ($attemptoptions->scores && $attempt->timefinish > 0) {
                    $formattedgrade = $attemptgrade;
                    // highlight the highest grade if appropriate
                    if ($overallstats && !$attempt->preview && $numattempts > 1 && !is_null($mygrade) &&
                            $attemptgrade == $mygrade && $quiz->grademethod == QUIZ_GRADEHIGHEST) {
                        $table->rowclass[$attempt->attempt] = 'bestrow';
                    }

                    $row[] = $accessmanager->make_review_link($formattedgrade, $attempt, $canpreview, $attemptoptions);
                } else {
                    $row[] = '';
                }
            }

            if ($feedbackcolumn && $attempt->timefinish > 0) {
                if ($attemptoptions->overallfeedback) {
                    $row[] = quiz_feedback_for_grade($attemptgrade, $quiz->id);
                } else {
                    $row[] = '';
                }
            }

            if (isset($quiz->showtimetaken)) {
                $row[] = $timetaken;
            }

            if ($attempt->preview) {
                $table->data['preview'] = $row;
            } else {
                $table->data[$attempt->attempt] = $row;
            }
        } // End of loop over attempts.
        print_table($table);
    }

/// Print information about the student's best score for this quiz if possible.
    $moreattempts = $unfinished || !$accessmanager->is_finished($numattempts, $lastfinishedattempt);
    if (!$moreattempts) {
        print_heading(get_string("nomoreattempts", "quiz"));
    }

    if ($numattempts && $quiz->sumgrades && !is_null($mygrade)) {
        $resultinfo = '';

        if ($overallstats) {
            if ($moreattempts) {
                $a = new stdClass;
                $a->method = quiz_get_grading_option_name($quiz->grademethod);
                $a->mygrade = $mygrade;
                $a->quizgrade = $quiz->grade;
                $resultinfo .= print_heading(get_string('gradesofar', 'quiz', $a), '', 2, 'main', true);
            } else {
                $resultinfo .= print_heading(get_string('yourfinalgradeis', 'quiz', "$mygrade / $quiz->grade"), '', 2, 'main', true);
                if ($mygradeoverridden) {
                    $resultinfo .= '<p class="overriddennotice">'.get_string('overriddennotice', 'grades')."</p>\n";
                }
            }
        }

        if ($gradebookfeedback) {
            $resultinfo .= print_heading(get_string('comment', 'quiz'), '', 3, 'main', true);
            $resultinfo .= '<p class="quizteacherfeedback">'.$gradebookfeedback."</p>\n";
        }
        if ($overallfeedback) {
            $resultinfo .= print_heading(get_string('overallfeedback', 'quiz'), '', 3, 'main', true);
            $resultinfo .= '<p class="quizgradefeedback">'.quiz_feedback_for_grade($mygrade, $quiz->id)."</p>\n";
        }

        if ($resultinfo) {
            print_box($resultinfo, 'generalbox', 'feedback');
        }
    }

/// Determine if we should be showing a start/continue attempt button,
/// or a button to go back to the course page.
    print_box_start('quizattempt');
    $buttontext = ''; // This will be set something if as start/continue attempt button should appear.
    if (!$quiz->questions) {
        print_heading(get_string("noquestions", "quiz"));
    } else {
        if ($unfinished) {
            if ($canpreview) {
                $buttontext = get_string('continuepreview', 'quiz');
            } else {
                $buttontext = get_string('continueattemptquiz', 'quiz');
            }
        } else {
            $messages = $accessmanager->prevent_new_attempt($numattempts, $lastfinishedattempt);
            if (!$canpreview && $messages) {
                $accessmanager->print_messages($messages);
            } else {
                if ($canpreview) {
                    $buttontext = get_string('previewquiznow', 'quiz');
                } else if ($numattempts == 0) {
                    $buttontext = get_string('attemptquiznow', 'quiz');
                } else {
                    $buttontext = get_string('reattemptquiz', 'quiz');
                }
            }
        }

        // If, so far, we think a button should be printed, so check if they will be allowed to access it.
        if ($buttontext) {
            if (!$moreattempts) {
                $buttontext = '';
            } else if (!$canpreview && $messages = $accessmanager->prevent_access()) {
                $accessmanager->print_messages($messages);
                $buttontext = '';
            }
        }
    }

/// Now actually print the appropriate button.
    if ($buttontext) {
        $accessmanager->print_start_attempt_button($canpreview, $buttontext, $unfinished);
    } else {
        print_continue($CFG->wwwroot . '/course/view.php?id=' . $course->id);
    }
    print_box_end();

    // Should we not be seeing if we need to print right-hand-side blocks?

    finish_page($course);

// Utility functions =================================================================

function finish_page($course) {
    global $THEME;
    print_container_end();
    echo '</td></tr></table>';
    print_footer($course);
    exit;
}
?>