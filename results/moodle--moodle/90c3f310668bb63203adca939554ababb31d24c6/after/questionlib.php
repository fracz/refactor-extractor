<?php  // $Id$
/**
 * Code for handling and processing questions
 *
 * This is code that is module independent, i.e., can be used by any module that
 * uses questions, like quiz, lesson, ..
 * This script also loads the questiontype classes
 * Code for handling the editing of questions is in {@link question/editlib.php}
 *
 * TODO: separate those functions which form part of the API
 *       from the helper functions.
 *
 * @version $Id$
 * @author Martin Dougiamas and many others. This has recently been completely
 *         rewritten by Alex Smith, Julian Sedding and Gustav Delius as part of
 *         the Serving Mathematics project
 *         {@link http://maths.york.ac.uk/serving_maths}
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package question
 */

/// CONSTANTS ///////////////////////////////////

/**#@+
 * The different types of events that can create question states
 */
define('QUESTION_EVENTOPEN', '0');      // The state was created by Moodle
define('QUESTION_EVENTNAVIGATE', '1');  // The responses were saved because the student navigated to another page (this is not currently used)
define('QUESTION_EVENTSAVE', '2');      // The student has requested that the responses should be saved but not submitted or validated
define('QUESTION_EVENTGRADE', '3');     // Moodle has graded the responses. A SUBMIT event can be changed to a GRADE event by Moodle.
define('QUESTION_EVENTDUPLICATE', '4'); // The responses submitted were the same as previously
define('QUESTION_EVENTVALIDATE', '5');  // The student has requested a validation. This causes the responses to be saved as well, but not graded.
define('QUESTION_EVENTCLOSEANDGRADE', '6'); // Moodle has graded the responses. A CLOSE event can be changed to a CLOSEANDGRADE event by Moodle.
define('QUESTION_EVENTSUBMIT', '7');    // The student response has been submitted but it has not yet been marked
define('QUESTION_EVENTCLOSE', '8');     // The response has been submitted and the session has been closed, either because the student requested it or because Moodle did it (e.g. because of a timelimit). The responses have not been graded.
/**#@-*/

/**#@+
 * The core question types
 */
define("SHORTANSWER",   "1");
define("TRUEFALSE",     "2");
define("MULTICHOICE",   "3");
define("RANDOM",        "4");
define("MATCH",         "5");
define("RANDOMSAMATCH", "6");
define("DESCRIPTION",   "7");
define("NUMERICAL",     "8");
define("MULTIANSWER",   "9");
define("CALCULATED",   "10");
define("RQP",          "11");
define("ESSAY",        "12");
/**#@-*/

/**
 * Constant determines the number of answer boxes supplied in the editing
 * form for multiple choice and similar question types.
 */
define("QUESTION_NUMANS", "10");


/**#@+
 * Option flags for ->optionflags
 * The options are read out via bitwise operation using these constants
 */
/**
 * Whether the questions is to be run in adaptive mode. If this is not set then
 * a question closes immediately after the first submission of responses. This
 * is how question is Moodle always worked before version 1.5
 */
define('QUESTION_ADAPTIVE', 1);

/** When processing responses the code checks that the new responses at
 * a question differ from those given on the previous submission. If
 * furthermore this flag is set to true
 * then the code goes through the whole history of responses and checks if
 * ANY of them are identical to the current response in which case the
 * current response is ignored.
 */
define('QUESTION_IGNORE_DUPRESP', 2);

/**#@-*/

/// QTYPES INITIATION //////////////////

/**
 * Array holding question type objects
 */
global $QTYPES;
$QTYPES = array(); // This array will be populated when the questiontype.php files are loaded below

/**
 * Array of question types names translated to the user's language
 *
 * The $QTYPE_MENU array holds the names of all the question types that the user should
 * be able to create directly. Some internal question types like random questions are excluded.
 * The complete list of question types can be found in {@link $QTYPES}.
 */
$QTYPE_MENU = array(); // This array will be populated when the questiontype.php files are loaded

require_once("$CFG->dirroot/question/questiontypes/questiontype.php");

/*
* Load the questiontype.php file for each question type
* These files in turn instantiate the corresponding question type class
* and add them to the $QTYPES array
*/
$qtypenames= get_list_of_plugins('question/questiontypes');
foreach($qtypenames as $qtypename) {
    // Instanciates all plug-in question types
    $qtypefilepath= "$CFG->dirroot/question/questiontypes/$qtypename/questiontype.php";

    // echo "Loading $qtypename<br/>"; // Uncomment for debugging
    if (is_readable($qtypefilepath)) {
        require_once($qtypefilepath);
    }
}

/// OTHER CLASSES /////////////////////////////////////////////////////////

/**
 * This holds the options that are set by the course module
 */
class cmoptions {
    /**
    * Whether a new attempt should be based on the previous one. If true
    * then a new attempt will start in a state where all responses are set
    * to the last responses from the previous attempt.
    */
    var $attemptonlast = false;

    /**
    * Various option flags. The flags are accessed via bitwise operations
    * using the constants defined in the CONSTANTS section above.
    */
    var $optionflags = QUESTION_ADAPTIVE;

    /**
    * Determines whether in the calculation of the score for a question
    * penalties for earlier wrong responses within the same attempt will
    * be subtracted.
    */
    var $penaltyscheme = true;

    /**
    * The maximum time the user is allowed to answer the questions withing
    * an attempt. This is measured in minutes so needs to be multiplied by
    * 60 before compared to timestamps. If set to 0 no timelimit will be applied
    */
    var $timelimit = 0;

    /**
    * Timestamp for the closing time. Responses submitted after this time will
    * be saved but no credit will be given for them.
    */
    var $timeclose = 9999999999;

    /**
    * The id of the course from withing which the question is currently being used
    */
    var $course = SITEID;

    /**
    * Whether the answers in a multiple choice question should be randomly
    * shuffled when a new attempt is started.
    */
    var $shuffleanswers = false;

    /**
    * The number of decimals to be shown when scores are printed
    */
    var $decimalpoints = 2;
}


/// FUNCTIONS //////////////////////////////////////////////////////

/**
 * Returns an array with all the course modules that use this question
 *
 * @param object $questionid
 */
function question_whereused($questionid) {
    $instances = array();
    $modules = get_records('modules');
    foreach ($modules as $module) {
        $fn = $module->name.'_question_whereused';
        if (function_exists($fn)) {
            $instances[] = $fn($questionid);
        }
    }
    return $instances;
}

/**
 * Deletes question and all associated data from the database
 *
 * It will not delete a question if it is used by an activity module
 * @param object $question  The question being deleted
 */
function delete_question($questionid) {
    global $QTYPES;

    // Do not delete a question if it is used by an activity module
    if (count(question_whereused($questionid))) {
        return;
    }

    // delete questiontype-specific data
    if (isset($QTYPES[$question->qtype])) {
        $QTYPES[$question->qtype]->delete_question($questionid);
    }

    // delete entries from all other question tables
    // It is important that this is done only after calling the questiontype functions
    delete_records("question_answers", "question", $questionid);
    delete_records("question_states", "question", $questionid);
    delete_records("question_sessions", "questionid", $questionid);

    // Now recursively delete all child questions
    if ($children = get_records('question', 'parent', $questionid)) {
        foreach ($children as $child) {
            delete_question($child->id);
        }
    }

    // Finally delete the question record itself
    delete_records('question', 'id', $questionid);

    return;
}

/**
* Updates the question objects with question type specific
* information by calling {@link get_question_options()}
*
* Can be called either with an array of question objects or with a single
* question object.
* @return bool            Indicates success or failure.
* @param mixed $questions Either an array of question objects to be updated
*                         or just a single question object
*/
function get_question_options(&$questions) {
    global $QTYPES;

    if (is_array($questions)) { // deal with an array of questions
        // get the keys of the input array
        $keys = array_keys($questions);
        // update each question object
        foreach ($keys as $i) {
            // set name prefix
            $questions[$i]->name_prefix = question_make_name_prefix($i);

            if (!$QTYPES[$questions[$i]->qtype]->get_question_options($questions[$i]))
                return false;
        }
        return true;
    } else { // deal with single question
        $questions->name_prefix = question_make_name_prefix($questions->id);
        return $QTYPES[$questions->qtype]->get_question_options($questions);
    }
}

/**
* Loads the most recent state of each question session from the database
* or create new one.
*
* For each question the most recent session state for the current attempt
* is loaded from the question_states table and the question type specific data and
* responses are added by calling {@link restore_question_state()} which in turn
* calls {@link restore_session_and_responses()} for each question.
* If no states exist for the question instance an empty state object is
* created representing the start of a session and empty question
* type specific information and responses are created by calling
* {@link create_session_and_responses()}.
*
* @return array           An array of state objects representing the most recent
*                         states of the question sessions.
* @param array $questions The questions for which sessions are to be restored or
*                         created.
* @param object $cmoptions
* @param object $attempt  The attempt for which the question sessions are
*                         to be restored or created.
*/
function get_question_states(&$questions, $cmoptions, $attempt) {
    global $CFG, $QTYPES;

    // get the question ids
    $ids = array_keys($questions);
    $questionlist = implode(',', $ids);

    // The question field must be listed first so that it is used as the
    // array index in the array returned by get_records_sql
    $statefields = 'n.questionid as question, s.*, n.sumpenalty';
    // Load the newest states for the questions
    $sql = "SELECT $statefields".
           "  FROM {$CFG->prefix}question_states s,".
           "       {$CFG->prefix}question_sessions n".
           " WHERE s.id = n.newest".
           "   AND n.attemptid = '$attempt->uniqueid'".
           "   AND n.questionid IN ($questionlist)";
    $states = get_records_sql($sql);

    // Load the newest graded states for the questions
    $sql = "SELECT $statefields".
           "  FROM {$CFG->prefix}question_states s,".
           "       {$CFG->prefix}question_sessions n".
           " WHERE s.id = n.newgraded".
           "   AND n.attemptid = '$attempt->uniqueid'".
           "   AND n.questionid IN ($questionlist)";
    $gradedstates = get_records_sql($sql);

    // loop through all questions and set the last_graded states
    foreach ($ids as $i) {
        if (isset($states[$i])) {
            restore_question_state($questions[$i], $states[$i]);
            if (isset($gradedstates[$i])) {
                restore_question_state($questions[$i], $gradedstates[$i]);
                $states[$i]->last_graded = $gradedstates[$i];
            } else {
                $states[$i]->last_graded = clone($states[$i]);
            }
        } else {
            // Create a new state object
            if ($cmoptions->attemptonlast and $attempt->attempt > 1 and !$attempt->preview) {
                // build on states from last attempt
                if (!$lastattemptid = get_field('quiz_attempts', 'uniqueid', 'quiz', $attempt->quiz, 'userid', $attempt->userid, 'attempt', $attempt->attempt-1)) {
                    error('Could not find previous attempt to build on');
                }
                // Load the last graded state for the question
                $sql = "SELECT $statefields".
                       "  FROM {$CFG->prefix}question_states s,".
                       "       {$CFG->prefix}question_sessions n".
                       " WHERE s.id = n.newgraded".
                       "   AND n.attemptid = '$lastattemptid'".
                       "   AND n.questionid = '$i'";
                if (!$states[$i] = get_record_sql($sql)) {
                    error('Could not find state for previous attempt to build on');
                }
                restore_question_state($questions[$i], $states[$i]);
                $states[$i]->attempt = $attempt->uniqueid;
                $states[$i]->question = (int) $i;
                $states[$i]->seq_number = 0;
                $states[$i]->timestamp = $attempt->timestart;
                $states[$i]->event = ($attempt->timefinish) ? QUESTION_EVENTCLOSE : QUESTION_EVENTOPEN;
                $states[$i]->grade = 0;
                $states[$i]->raw_grade = 0;
                $states[$i]->penalty = 0;
                $states[$i]->sumpenalty = 0;
                $states[$i]->changed = true;
                $states[$i]->last_graded = clone($states[$i]);
                $states[$i]->last_graded->responses = array('' => '');

            } else {
                // create a new empty state
                $states[$i] = new object;
                $states[$i]->attempt = $attempt->uniqueid;
                $states[$i]->question = (int) $i;
                $states[$i]->seq_number = 0;
                $states[$i]->timestamp = $attempt->timestart;
                $states[$i]->event = ($attempt->timefinish) ? QUESTION_EVENTCLOSE : QUESTION_EVENTOPEN;
                $states[$i]->grade = 0;
                $states[$i]->raw_grade = 0;
                $states[$i]->penalty = 0;
                $states[$i]->sumpenalty = 0;
                $states[$i]->responses = array('' => '');
                // Prevent further changes to the session from incrementing the
                // sequence number
                $states[$i]->changed = true;

                // Create the empty question type specific information
                if (!$QTYPES[$questions[$i]->qtype]
                 ->create_session_and_responses($questions[$i], $states[$i], $cmoptions, $attempt)) {
                    return false;
                }
                $states[$i]->last_graded = clone($states[$i]);
            }
        }
    }
    return $states;
}


/**
* Creates the run-time fields for the states
*
* Extends the state objects for a question by calling
* {@link restore_session_and_responses()}
* @return boolean         Represents success or failure
* @param object $question The question for which the state is needed
* @param object $state   The state as loaded from the database
*/
function restore_question_state(&$question, &$state) {
    global $QTYPES;

    // initialise response to the value in the answer field
    $state->responses = array('' => $state->answer);
    unset($state->answer);

    // Set the changed field to false; any code which changes the
    // question session must set this to true and must increment
    // ->seq_number. The save_question_session
    // function will save the new state object to the database if the field is
    // set to true.
    $state->changed = false;

    // Load the question type specific data
    return $QTYPES[$question->qtype]
     ->restore_session_and_responses($question, $state);

}

/**
* Saves the current state of the question session to the database
*
* The state object representing the current state of the session for the
* question is saved to the question_states table with ->responses[''] saved
* to the answer field of the database table. The information in the
* question_sessions table is updated.
* The question type specific data is then saved.
* @return boolean         Indicates success or failure.
* @param object $question The question for which session is to be saved.
* @param object $state    The state information to be saved. In particular the
*                         most recent responses are in ->responses. The object
*                         is updated to hold the new ->id.
*/
function save_question_session(&$question, &$state) {
    global $QTYPES;
    // Check if the state has changed
    if (!$state->changed && isset($state->id)) {
        return true;
    }
    // Set the legacy answer field
    $state->answer = isset($state->responses['']) ? $state->responses[''] : '';

    // Save the state
    if (isset($state->update)) { // this ->update field is only used by the
        // regrading function to force the old state record to be overwritten
        update_record('question_states', $state);
    } else {
        if (!$state->id = insert_record('question_states', $state)) {
            unset($state->id);
            unset($state->answer);
            return false;
        }

        // this is the most recent state
        if (!record_exists('question_sessions', 'attemptid',
         $state->attempt, 'questionid', $question->id)) {
            $new->attemptid = $state->attempt;
            $new->questionid = $question->id;
            $new->newest = $state->id;
            $new->sumpenalty = $state->sumpenalty;
            if (!insert_record('question_sessions', $new)) {
                error('Could not insert entry in question_sessions');
            }
        } else {
            set_field('question_sessions', 'newest', $state->id, 'attemptid',
             $state->attempt, 'questionid', $question->id);
        }
        if (question_state_is_graded($state)) {
            // this is also the most recent graded state
            if ($newest = get_record('question_sessions', 'attemptid',
             $state->attempt, 'questionid', $question->id)) {
                $newest->newgraded = $state->id;
                $newest->sumpenalty = $state->sumpenalty;
                update_record('question_sessions', $newest);
            }
        }
    }

    unset($state->answer);

    // Save the question type specific state information and responses
    if (!$QTYPES[$question->qtype]->save_session_and_responses(
     $question, $state)) {
        return false;
    }
    // Reset the changed flag
    $state->changed = false;
    return true;
}

/**
* Determines whether a state has been graded by looking at the event field
*
* @return boolean         true if the state has been graded
* @param object $state
*/
function question_state_is_graded($state) {
    return ($state->event == QUESTION_EVENTGRADE or $state->event == QUESTION_EVENTCLOSEANDGRADE);
}

/**
* Determines whether a state has been closed by looking at the event field
*
* @return boolean         true if the state has been closed
* @param object $state
*/
function question_state_is_closed($state) {
    return ($state->event == QUESTION_EVENTCLOSE or $state->event == QUESTION_EVENTCLOSEANDGRADE);
}


/**
* Extracts responses from submitted form
*
* TODO: Finish documenting this
* @return array            array of action objects, indexed by question ids.
* @param array $questions  an array containing at least all questions that are used on the form
* @param array $responses
* @param integer $defaultevent
*/
function question_extract_responses($questions, $responses, $defaultevent) {

    $actions = array();
    foreach ($responses as $key => $response) {
        // Get the question id from the response name
        if (false !== ($quid = question_get_id_from_name_prefix($key))) {
            // check if this is a valid id
            if (!isset($questions[$quid])) {
                error('Form contained question that is not in questionids');
            }

            // Remove the name prefix from the name
            //decrypt trying
            $key = substr($key, strlen($questions[$quid]->name_prefix));
            if (false === $key) {
                $key = '';
            }
            // Check for question validate and mark buttons & set events
            if ($key === 'validate') {
                $actions[$quid]->event = QUESTION_EVENTVALIDATE;
            } else if ($key === 'mark') {
                $actions[$quid]->event = QUESTION_EVENTSUBMIT;
            } else {
                $actions[$quid]->event = $defaultevent;
            }

            // Update the state with the new response
            $actions[$quid]->responses[$key] = $response;
        }
    }
    return $actions;
}



/**
* For a given question in an attempt we walk the complete history of states
* and recalculate the grades as we go along.
*
* This is used when a question is changed and old student
* responses need to be marked with the new version of a question.
*
* TODO: Make sure this is not quiz-specific
*
* @return boolean            Indicates success/failure
* @param object  $question   A question object
* @param object  $attempt    The attempt, in which the question needs to be regraded.
* @param object  $cmoptions
* @param boolean $verbose    Optional. Whether to print progress information or not.
*/
function regrade_question_in_attempt($question, $attempt, $cmoptions, $verbose=false) {

    // load all states for this question in this attempt, ordered in sequence
    if ($states = get_records_select('question_states',
     "attempt = '{$attempt->uniqueid}' AND question = '{$question->id}'", 'seq_number ASC')) {
        $states = array_values($states);

        // Subtract the grade for the latest state from $attempt->sumgrades to get the
        // sumgrades for the attempt without this question.
        $attempt->sumgrades -= $states[count($states)-1]->grade;

        // Initialise the replaystate
        $state = clone($states[0]);
        restore_question_state($question, $state);
        $state->sumpenalty = 0.0;
        $replaystate = clone($state);
        $replaystate->last_graded = $state;

        $changed = 0;
        for($j = 1; $j < count($states); $j++) {
            restore_question_state($question, $states[$j]);
            $action = new stdClass;
            $action->responses = $states[$j]->responses;
            $action->timestamp = $states[$j]->timestamp;

            // Close the last state of a finished attempt
            if (((count($states) - 1) === $j) && ($attempt->timefinish > 0)) {
                $action->event = QUESTION_EVENTCLOSE;

            // Change event to submit so that it will be reprocessed
            } else if (QUESTION_EVENTCLOSE == $states[$j]->event
                       or QUESTION_EVENTGRADE == $states[$j]->event
                       or QUESTION_EVENTCLOSEANDGRADE == $states[$j]->event) {
                $action->event = QUESTION_EVENTSUBMIT;

            // By default take the event that was saved in the database
            } else {
                $action->event = $states[$j]->event;
            }
            // Reprocess (regrade) responses
            if (!question_process_responses($question, $replaystate, $action, $cmoptions,
             $attempt)) {
                $verbose && notify("Couldn't regrade state #{$state->id}!");
            }

            // We need rounding here because grades in the DB get truncated
            // e.g. 0.33333 != 0.3333333, but we want them to be equal here
            if (round((float)$replaystate->grade, 5) != round((float)$states[$j]->grade, 5)) {
                $changed++;
            }

            $replaystate->id = $states[$j]->id;
            $replaystate->update = true;
            save_question_session($question, $replaystate);
        }
        if ($verbose) {
            if ($changed) {
                link_to_popup_window ('/question/reviewquestion.php?attempt='.$attempt->id.'&amp;question='.$question->id,
                 'reviewquestion', ' #'.$attempt->id, 450, 550, get_string('reviewresponse', 'quiz'));
                update_record('quiz_attempts', $attempt);
            } else {
                echo ' #'.$attempt->id;
            }
            echo "\n"; @flush(); @ob_flush();
        }

        return true;
    }
    return true;
}

/**
* Processes an array of student responses, grading and saving them as appropriate
*
* @return boolean         Indicates success/failure
* @param object $question Full question object, passed by reference
* @param object $state    Full state object, passed by reference
* @param object $action   object with the fields ->responses which
*                         is an array holding the student responses,
*                         ->action which specifies the action, e.g., QUESTION_EVENTGRADE,
*                         and ->timestamp which is a timestamp from when the responses
*                         were submitted by the student.
* @param object $cmoptions
* @param object $attempt  The attempt is passed by reference so that
*                         during grading its ->sumgrades field can be updated
*/
function question_process_responses(&$question, &$state, $action, $cmoptions, &$attempt) {
    global $QTYPES;

    // if no responses are set initialise to empty response
    if (!isset($action->responses)) {
        $action->responses = array('' => '');
    }

    // make sure these are gone!
    unset($action->responses['mark'], $action->responses['validate']);

    // Check the question session is still open
    if (question_state_is_closed($state)) {
        return true;
    }

    // If $action->event is not set that implies saving
    if (! isset($action->event)) {
        $action->event = QUESTION_EVENTSAVE;
    }
    // If submitted then compare against last graded
    // responses, not last given responses in this case
    if (question_isgradingevent($action->event)) {
        $state->responses = $state->last_graded->responses;
    }
    // Check for unchanged responses (exactly unchanged, not equivalent).
    // We also have to catch questions that the student has not yet attempted
    $sameresponses = (($state->responses == $action->responses) or
     ($state->responses == array(''=>'') && array_keys(array_count_values($action->responses))===array('')));

    // If the response has not been changed then we do not have to process it again
    // unless the attempt is closing or validation is requested
    if ($sameresponses and QUESTION_EVENTCLOSE != $action->event
     and QUESTION_EVENTVALIDATE != $action->event) {
        return true;
    }

    // Roll back grading information to last graded state and set the new
    // responses
    $newstate = clone($state->last_graded);
    $newstate->responses = $action->responses;
    $newstate->seq_number = $state->seq_number + 1;
    $newstate->changed = true; // will assure that it gets saved to the database
    $newstate->last_graded = $state->last_graded;
    $newstate->timestamp = $action->timestamp;
    $state = $newstate;

    // Set the event to the action we will perform. The question type specific
    // grading code may override this by setting it to QUESTION_EVENTCLOSE if the
    // attempt at the question causes the session to close
    $state->event = $action->event;

    if (!question_isgradingevent($action->event)) {
        // Grade the response but don't update the overall grade
        $QTYPES[$question->qtype]->grade_responses(
         $question, $state, $cmoptions);
        // Don't allow the processing to change the event type
        $state->event = $action->event;

    } else if (QUESTION_EVENTSUBMIT == $action->event) {

        // Work out if the current responses (or equivalent responses) were
        // already given in
        // a. the last graded attempt
        // b. any other graded attempt
        if($QTYPES[$question->qtype]->compare_responses(
         $question, $state, $state->last_graded)) {
            $state->event = QUESTION_EVENTDUPLICATE;
        } else {
            if ($cmoptions->optionflags & QUESTION_IGNORE_DUPRESP) {
                /* Walk back through the previous graded states looking for
                one where the responses are equivalent to the current
                responses. If such a state is found, set the current grading
                details to those of that state and set the event to
                QUESTION_EVENTDUPLICATE */
                question_search_for_duplicate_responses($question, $state);
            }
        }

        // If we did not find a duplicate, perform grading
        if (QUESTION_EVENTDUPLICATE != $state->event) {
            // Decrease sumgrades by previous grade and then later add new grade
            $attempt->sumgrades -= (float)$state->last_graded->grade;

            $QTYPES[$question->qtype]->grade_responses(
             $question, $state, $cmoptions);
            // Calculate overall grade using correct penalty method
            question_apply_penalty_and_timelimit($question, $state, $attempt, $cmoptions);
            // Update the last graded state (don't simplify!)
            unset($state->last_graded);
            $state->last_graded = clone($state);
            unset($state->last_graded->changed);

            $attempt->sumgrades += (float)$state->last_graded->grade;
        }

    } else if (QUESTION_EVENTCLOSE == $action->event) {
        // decrease sumgrades by previous grade and then later add new grade
        $attempt->sumgrades -= (float)$state->last_graded->grade;

        // Only mark if they haven't been marked already
        if (!$sameresponses) {
            $QTYPES[$question->qtype]->grade_responses(
             $question, $state, $cmoptions);
            // Calculate overall grade using correct penalty method
            question_apply_penalty_and_timelimit($question, $state, $attempt, $cmoptions);
        }

        // Update the last graded state (don't simplify!)
        unset($state->last_graded);
        $state->last_graded = clone($state);
        unset($state->last_graded->changed);

        $attempt->sumgrades += (float)$state->last_graded->grade;
    }
    $attempt->timemodified = $action->timestamp;

    return true;
}

/**
* Determine if event requires grading
*/
function question_isgradingevent($event) {
    return (QUESTION_EVENTSUBMIT == $event || QUESTION_EVENTCLOSE == $event);
}

/**
* Compare current responses to all previous graded responses
*
* This is used by {@link question_process_responses()} to determine whether
* to ignore the marking request for the current response. However this
* check against all previous graded responses is only performed if
* the QUESTION_IGNORE_DUPRESP bit in $cmoptions->optionflags is set
* If the current response is a duplicate of a previously graded response then
* $STATE->event is set to QUESTION_EVENTDUPLICATE.
* @return boolean         Indicates if a state with duplicate responses was
*                         found.
* @param object $question
* @param object $state
*/
function question_search_for_duplicate_responses(&$question, &$state) {
    // get all previously graded question states
    global $QTYPES;
    if (!$oldstates = get_records('question_states', "event = '" .
     QUESTION_EVENTGRADE . "' AND " . "question = '" . $question->id .
     "'", 'seq_number DESC')) {
        return false;
    }
    foreach ($oldstates as $oldstate) {
        if ($QTYPES[$question->qtype]->restore_session_and_responses(
         $question, $oldstate)) {
            if(!$QTYPES[$question->qtype]->compare_responses(
             $question, $state, $oldstate)) {
                $state->event = QUESTION_EVENTDUPLICATE;
                break;
            }
        }
    }
    return (QUESTION_EVENTDUPLICATE == $state->event);
}

/**
* Applies the penalty from the previous graded responses to the raw grade
* for the current responses
*
* The grade for the question in the current state is computed by subtracting the
* penalty accumulated over the previous graded responses at the question from the
* raw grade. If the timestamp is more than 1 minute beyond the end of the attempt
* the grade is set to zero. The ->grade field of the state object is modified to
* reflect the new grade but is never allowed to decrease.
* @param object $question The question for which the penalty is to be applied.
* @param object $state    The state for which the grade is to be set from the
*                         raw grade and the cumulative penalty from the last
*                         graded state. The ->grade field is updated by applying
*                         the penalty scheme determined in $cmoptions to the ->raw_grade and
*                         ->last_graded->penalty fields.
* @param object $cmoptions  The options set by the course module.
*                           The ->penaltyscheme field determines whether penalties
*                           for incorrect earlier responses are subtracted.
*/
function question_apply_penalty_and_timelimit(&$question, &$state, $attempt, $cmoptions) {
    // deal with penalty
    if ($cmoptions->penaltyscheme) {
            $state->grade = $state->raw_grade - $state->sumpenalty;
            $state->sumpenalty += (float) $state->penalty;
    } else {
        $state->grade = $state->raw_grade;
    }

    // deal with timelimit
    if ($cmoptions->timelimit) {
        // We allow for 5% uncertainty in the following test
        if (($state->timestamp - $attempt->timestart) > ($cmoptions->timelimit * 63)) {
            $state->grade = 0;
        }
    }

    // deal with closing time
    if ($cmoptions->timeclose and $state->timestamp > ($cmoptions->timeclose + 60) // allowing 1 minute lateness
             and !$attempt->preview) { // ignore closing time for previews
        $state->grade = 0;
    }

    // Ensure that the grade does not go down
    $state->grade = max($state->grade, $state->last_graded->grade);
}

/**
* Print the icon for the question type
*
* @param object $question  The question object for which the icon is required
* @param boolean $editlink If true then the icon is a link to the question
*                          edit page.
* @param boolean $return   If true the functions returns the link as a string
*/
function print_question_icon($question, $editlink=true, $return = false) {
// returns a question icon

    global $QTYPES, $CFG;

    $html = '<img border="0" height="16" width="16" src="'.$CFG->wwwroot.'/question/questiontypes/'.
            $QTYPES[$question->qtype]->name().'/icon.gif" alt="'.
            get_string($QTYPES[$question->qtype]->name(), 'quiz').'" />';

    if ($editlink) {
        $html =  "<a href=\"$CFG->wwwroot/question/question.php?id=$question->id\" title=\""
                .$QTYPES[$question->qtype]->name()."\">".
                $html."</a>\n";
    }
    if ($return) {
        return $html;
    } else {
        echo $html;
    }
}

/**
* Returns a html link to the question image if there is one
*
* @return string The html image tag or the empy string if there is no image.
* @param object $question The question object
*/
function get_question_image($question, $courseid) {

    global $CFG;
    $img = '';

    if ($question->image) {

        if (substr(strtolower($question->image), 0, 7) == 'http://') {
            $img .= $question->image;

        } else if ($CFG->slasharguments) {        // Use this method if possible for better caching
            $img .= "$CFG->wwwroot/file.php/$courseid/$question->image";

        } else {
            $img .= "$CFG->wwwroot/file.php?file=$courseid/$question->image";
        }
    }
    return $img;
}
/**
* Construct name prefixes for question form element names
*
* Construct the name prefix that should be used for example in the
* names of form elements created by questions.
* This is called by {@link get_question_options()}
* to set $question->name_prefix.
* This name prefix includes the question id which can be
* extracted from it with {@link question_get_id_from_name_prefix()}.
*
* @return string
* @param integer $id  The question id
*/
function question_make_name_prefix($id) {
    return 'resp' . $id . '_';
}

/**
* Extract question id from the prefix of form element names
*
* @return integer      The question id
* @param string $name  The name that contains a prefix that was
*                      constructed with {@link question_make_name_prefix()}
*/
function question_get_id_from_name_prefix($name) {
    if (!preg_match('/^resp([0-9]+)_/', $name, $matches))
        return false;
    return (integer) $matches[1];
}

/**
 * TODO: document this
 */
function question_new_attempt_uniqueid() {
    global $CFG;
    set_config('attemptuniqueid', $CFG->attemptuniqueid + 1);
    return $CFG->attemptuniqueid;
}

/**
* Array of names of course modules a question appears in
*
* TODO: Currently this works with quiz only
*
* @return array   Array of quiz names
* @param integer $id Question id
*/
function question_used($id) {

    $quizlist = array();
    if ($instances = get_records('quiz_question_instances', 'question', $id)) {
        foreach($instances as $instance) {
            $quizlist[$instance->quiz] = get_field('quiz', 'name', 'id', $instance->quiz);
        }
    }

    return $quizlist;
}

/// FUNCTIONS THAT SIMPLY WRAP QUESTIONTYPE METHODS //////////////////////////////////

/**
* Prints a question
*
* Simply calls the question type specific print_question() method.
*/
function print_question(&$question, &$state, $number, $cmoptions, $options=null) {
    global $QTYPES;

    $QTYPES[$question->qtype]->print_question($question, $state, $number,
     $cmoptions, $options);
}
/**
* Saves question options
*
* Simply calls the question type specific save_question_options() method.
*/
function save_question_options($question) {
    global $QTYPES;

    $QTYPES[$question->qtype]->save_question_options($question);
}

/**
* Gets all teacher stored answers for a given question
*
* Simply calls the question type specific get_all_responses() method.
*/
// ULPGC ecastro
function get_question_responses($question, $state) {
    global $QTYPES;
    $r = $QTYPES[$question->qtype]->get_all_responses($question, $state);
    return $r;
}


/**
* Gets the response given by the user in a particular state
*
* Simply calls the question type specific get_actual_response() method.
*/
// ULPGC ecastro
function get_question_actual_response($question, $state) {
    global $QTYPES;

    $r = $QTYPES[$question->qtype]->get_actual_response($question, $state);
    return $r;
}

/**
* TODO: document this
*/
// ULPGc ecastro
function get_question_fraction_grade($question, $state) {
    global $QTYPES;

    $r = $QTYPES[$question->qtype]->get_fractional_grade($question, $state);
    return $r;
}


/// CATEGORY FUNCTIONS /////////////////////////////////////////////////////////////////

/**
* Gets the default category in a course
*
* It returns the first category with no parent category. If no categories
* exist yet then one is created.
* @return object The default category
* @param integer $courseid  The id of the course whose default category is wanted
*/
function get_default_question_category($courseid) {
/// Returns the current category

    if ($categories = get_records_select("question_categories", "course = '$courseid' AND parent = '0'", "id")) {
        foreach ($categories as $category) {
            return $category;   // Return the first one (lowest id)
        }
    }

    // Otherwise, we need to make one
    $category->name = get_string("default", "quiz");
    $category->info = get_string("defaultinfo", "quiz");
    $category->course = $courseid;
    $category->parent = 0;
    // TODO: Figure out why we use 999 below
    $category->sortorder = 999;
    $category->publish = 0;
    $category->stamp = make_unique_id_code();

    if (!$category->id = insert_record("question_categories", $category)) {
        notify("Error creating a default category!");
        return false;
    }
    return $category;
}

function question_category_menu($courseid, $published=false) {
/// Returns the list of categories
    $publish = "";
    if ($published) {
        $publish = "OR publish = '1'";
    }

    if (!isadmin()) {
        $categories = get_records_select("question_categories", "course = '$courseid' $publish", 'parent, sortorder, name ASC');
    } else {
        $categories = get_records_select("question_categories", '', 'parent, sortorder, name ASC');
    }
    if (!$categories) {
        return false;
    }
    $categories = add_indented_names($categories);

    foreach ($categories as $category) {
       if ($catcourse = get_record("course", "id", $category->course)) {
           if ($category->publish && ($category->course != $courseid)) {
               $category->indentedname .= " ($catcourse->shortname)";
           }
           $catmenu[$category->id] = $category->indentedname;
       }
    }
    return $catmenu;
}

function sort_categories_by_tree(&$categories, $id = 0, $level = 1) {
// returns the categories with their names ordered following parent-child relationships
// finally it tries to return pending categories (those being orphaned, whose parent is
// incorrect) to avoid missing any category from original array.
    $children = array();
    $keys = array_keys($categories);

    foreach ($keys as $key) {
        if (!isset($categories[$key]->processed) && $categories[$key]->parent == $id) {
            $children[$key] = $categories[$key];
            $categories[$key]->processed = true;
            $children = $children + sort_categories_by_tree($categories, $children[$key]->id, $level+1);
        }
    }
    //If level = 1, we have finished, try to look for non processed categories (bad parent) and sort them too
    if ($level == 1) {
        foreach ($keys as $key) {
            //If not processed and it's a good candidate to start (because its parent doesn't exist in the course)
            if (!isset($categories[$key]->processed) && !record_exists('question_categories', 'course', $categories[$key]->course, 'id', $categories[$key]->parent)) {
                $children[$key] = $categories[$key];
                $categories[$key]->processed = true;
                $children = $children + sort_categories_by_tree($categories, $children[$key]->id, $level+1);
            }
        }
    }
    return $children;
}

function flatten_category_tree( $cats, $depth=0 ) {
    // flattens tree structure created by add_indented_named
    // (adding the names)
    $newcats = array();
    $fillstr = '&nbsp;&nbsp;&nbsp;';

    foreach ($cats as $key => $cat) {
        $newcats[$key] = $cat;
        $newcats[$key]->indentedname = str_repeat($fillstr,$depth) . $cat->name;
        // recurse if the category has children
        if (!empty($cat->children)) {
            $newcats += flatten_category_tree( $cat->children, $depth+1 );
        }
    }

    return $newcats;
}

function add_indented_names( $categories ) {

    // iterate through categories adding new fields
    // and creating references
    foreach ($categories as $key => $category) {
        $categories[$key]->children = array();
        $categories[$key]->link = &$categories[$key];
    }

    // create tree structure of children
    // link field is used to track 'new' place of category in tree
    foreach ($categories as $key => $category) {
        if (!empty($category->parent)) {
            $categories[$category->parent]->link->children[$key] = $categories[$key];
            $categories[$key]->link = &$categories[$category->parent]->link->children[$key];
        }
    }

    // remove top level categories with parents
    $newcats = array();
    foreach ($categories as $key => $category) {
        unset( $category->link );
        if (empty($category->parent)) {
            $newcats[$key] = $category;
        }
    }

    // walk the tree to flatten revised structure
    $categories = flatten_category_tree( $newcats );

    return $categories;
}


/**
* Displays a select menu of categories with appended course names
*
* Optionaly non editable categories may be excluded.
* @author Howard Miller June '04
*/
function question_category_select_menu($courseid,$published=false,$only_editable=false,$selected="") {

    // get sql fragment for published
    $publishsql="";
    if ($published) {
        $publishsql = "or publish=1";
    }

    $categories = get_records_select("question_categories","course=$courseid $publishsql", 'parent, sortorder, name ASC');

    $categories = add_indented_names($categories);

    echo "<select name=\"category\">\n";
    foreach ($categories as $category) {
        $cid = $category->id;
        $cname = question_category_coursename($category, $courseid);
        $seltxt = "";
        if ($cid==$selected) {
            $seltxt = "selected=\"selected\"";
        }
        if ((!$only_editable) || isteacheredit($category->course)) {
            echo "    <option value=\"$cid\" $seltxt>$cname</option>\n";
        }
    }
    echo "</select>\n";
}

function question_category_coursename($category, $courseid = 0) {
/// if the category is not from this course and is published , adds on the course
/// name
    $cname = (isset($category->indentedname)) ? $category->indentedname : $category->name;
    if ($category->course != $courseid && $category->publish) {
        if ($catcourse=get_record("course","id",$category->course)) {
            $cname .= " ($catcourse->shortname) ";
        }
    }
    return $cname;
}


/**
* Returns a comma separated list of ids of the category and all subcategories
*/
function question_categorylist($categoryid) {
    // returns a comma separated list of ids of the category and all subcategories
    $categorylist = $categoryid;
    if ($subcategories = get_records('question_categories', 'parent', $categoryid, 'sortorder ASC', 'id, id')) {
        foreach ($subcategories as $subcategory) {
            $categorylist .= ','. question_categorylist($subcategory->id);
        }
    }
    return $categorylist;
}


/**
* Function to read all questions for category into big array
*
* @param int $category category number
* @param bool @noparent if true only questions with NO parent will be selected
* @author added by Howard Miller June 2004
*/
function get_questions_category( $category, $noparent=false ) {

    global $QTYPES;

    // questions will be added to an array
    $qresults = array();

    // build sql bit for $noparent
    $npsql = '';
    if ($noparent) {
      $npsql = " and parent='0' ";
    }

    // get the list of questions for the category
    if ($questions = get_records_select("question","category={$category->id} $npsql", "qtype, name ASC")) {

        // iterate through questions, getting stuff we need
        foreach($questions as $question) {
            $questiontype = $QTYPES[$question->qtype];
            $questiontype->get_question_options( $question );
            $qresults[] = $question;
        }
    }

    return $qresults;
}

/**
 * Get list of available import or export formats
 * @param string $type 'import' if import list, otherwise export list assumed
 * @return array sorted list of import/export formats available
**/
function get_import_export_formats( $type ) {

    global $CFG;
    $fileformats = get_list_of_plugins("question/format");

    $fileformatname=array();
    require_once( "format.php" );
    foreach ($fileformats as $key => $fileformat) {
        $format_file = $CFG->dirroot . "/question/format/$fileformat/format.php";
        if (file_exists( $format_file ) ) {
            require_once( $format_file );
        }
        else {
            continue;
        }
        $classname = "qformat_$fileformat";
        $format_class = new $classname();
        if ($type=='import') {
            $provided = $format_class->provide_import();
        }
        else {
            $provided = $format_class->provide_export();
        }
        if ($provided) {
            $formatname = get_string($fileformat, 'quiz');
            if ($formatname == "[[$fileformat]]") {
                $formatname = $fileformat;  // Just use the raw folder name
            }
            $fileformatnames[$fileformat] = $formatname;
        }
    }
    natcasesort($fileformatnames);

    return $fileformatnames;
}


/**
* Create default export filename
*
* @return string   default export filename
* @param object $course
* @param object $category
*/
function default_export_filename($course,$category) {
    //Take off some characters in the filename !!
    $takeoff = array(" ", ":", "/", "\\", "|");
    $export_word = str_replace($takeoff,"_",strtolower(get_string("exportfilename","quiz")));
    //If non-translated, use "export"
    if (substr($export_word,0,1) == "[") {
        $export_word= "export";
    }

    //Calculate the date format string
    $export_date_format = str_replace(" ","_",get_string("exportnameformat","quiz"));
    //If non-translated, use "%Y%m%d-%H%M"
    if (substr($export_date_format,0,1) == "[") {
        $export_date_format = "%%Y%%m%%d-%%H%%M";
    }

    //Calculate the shortname
    $export_shortname = clean_filename($course->shortname);
    if (empty($export_shortname) or $export_shortname == '_' ) {
        $export_shortname = $course->id;
    }

    //Calculate the category name
    $export_categoryname = clean_filename($category->name);

    //Calculate the final export filename
    //The export word
    $export_name = $export_word."-";
    //The shortname
    $export_name .= strtolower($export_shortname)."-";
    //The category name
    $export_name .= strtolower($export_categoryname)."-";
    //The date format
    $export_name .= userdate(time(),$export_date_format,99,false);
    //The extension - no extension, supplied by format
    // $export_name .= ".txt";

    return $export_name;
}

?>