<?php
/**
 * This script lists student attempts
 *
 * @version $Id$
 * @author Martin Dougiamas, Tim Hunt and others.
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package quiz
 *//** */

require_once($CFG->libdir.'/tablelib.php');
require_once($CFG->dirroot.'/mod/quiz/report/overview/overviewsettings_form.php');
require_once($CFG->dirroot.'/mod/quiz/report/overview/overview_table.php');

class quiz_overview_report extends quiz_default_report {

    /**
     * Display the report.
     */
    function display($quiz, $cm, $course) {
        global $CFG, $COURSE, $DB;

        $context = get_context_instance(CONTEXT_MODULE, $cm->id);

        // Work out some display options - whether there is feedback, and whether scores should be shown.
        $hasfeedback = quiz_has_feedback($quiz->id) && $quiz->grade > 1.e-7 && $quiz->sumgrades > 1.e-7;
        $fakeattempt = new stdClass();
        $fakeattempt->preview = false;
        $fakeattempt->timefinish = $quiz->timeopen;
        $reviewoptions = quiz_get_reviewoptions($quiz, $fakeattempt, $context);
        $showgrades = $quiz->grade && $quiz->sumgrades && $reviewoptions->scores;

        $download = optional_param('download', '', PARAM_ALPHA);

        if($attemptids = optional_param('attemptid', array(), PARAM_INT)) {
            //attempts need to be deleted
            require_capability('mod/quiz:deleteattempts', $context);
            $attemptids = optional_param('attemptid', array(), PARAM_INT);
            foreach($attemptids as $attemptid) {
                add_to_log($course->id, 'quiz', 'delete attempt', 'report.php?id=' . $cm->id,
                        $attemptid, $cm->id);
                quiz_delete_attempt($attemptid, $quiz);
            }
            //No need for a redirect, any attemptids that do not exist are ignored.
            //So no problem if the user refreshes and tries to delete the same attempts
            //twice.
        }


        $pageoptions = array();
        $pageoptions['id'] = $cm->id;
        $pageoptions['q'] = $quiz->id;
        $pageoptions['mode'] = 'overview';

        $reporturl = new moodle_url($CFG->wwwroot.'/mod/quiz/report.php', $pageoptions);
        $qmsubselect = quiz_report_qm_filter_select($quiz);



        /// find out current groups mode
        $currentgroup = groups_get_activity_group($cm, true);

        $mform = new mod_quiz_report_overview_settings($reporturl, array('qmsubselect'=> $qmsubselect, 'quiz'=>$quiz, 'currentgroup'=>$currentgroup));
        if ($fromform = $mform->get_data()){
            $attemptsmode = $fromform->attemptsmode;
            if ($qmsubselect){
                //control is not on the form if
                //the grading method is not set
                //to grade one attempt per user eg. for average attempt grade.
                $qmfilter = $fromform->qmfilter;
            } else {
                $qmfilter = 0;
            }
            set_user_preference('quiz_report_overview_detailedmarks', $fromform->detailedmarks);
            set_user_preference('quiz_report_pagesize', $fromform->pagesize);
            $detailedmarks = $fromform->detailedmarks;
            $pagesize = $fromform->pagesize;
        } else {
            $qmfilter = optional_param('qmfilter', 0, PARAM_INT);
            $attemptsmode = optional_param('attemptsmode', null, PARAM_INT);
            if ($attemptsmode === null){
                //default
                $attemptsmode = QUIZ_REPORT_ATTEMPTS_ALL;
            } else if ($currentgroup){
                //default for when a group is selected
                if ($attemptsmode === null  || $attemptsmode == QUIZ_REPORT_ATTEMPTS_ALL){
                    $attemptsmode = QUIZ_REPORT_ATTEMPTS_STUDENTS_WITH;
                }
            } else if (!$currentgroup && $course->id == SITEID) {
                //force report on front page to show all, unless a group is selected.
                $attemptsmode = QUIZ_REPORT_ATTEMPTS_ALL;
            }
            $detailedmarks = get_user_preferences('quiz_report_overview_detailedmarks', 1);
            $pagesize = get_user_preferences('quiz_report_pagesize', 0);
        }
        if (!$reviewoptions->scores) {
            $detailedmarks = 0;
        }
        if ($pagesize < 1) {
            $pagesize = QUIZ_REPORT_DEFAULT_PAGE_SIZE;
        }
        // We only want to show the checkbox to delete attempts
        // if the user has permissions and if the report mode is showing attempts.
        $candelete = has_capability('mod/quiz:deleteattempts', $context)
                && ($attemptsmode!= QUIZ_REPORT_ATTEMPTS_STUDENTS_WITH_NO);


        $displayoptions = array();
        $displayoptions['attemptsmode'] = $attemptsmode;
        $displayoptions['qmfilter'] = $qmfilter;

        //work out the sql for this table.
        if (!$students = get_users_by_capability($context, 'mod/quiz:attempt','','','','','','',false)){
            $students = array();
        } else {
            $students = array_keys($students);
        }

        if (empty($currentgroup)) {
            // all users who can attempt quizzes
            $allowed = $students;
            $groupstudents = array();
        } else {
            // all users who can attempt quizzes and who are in the currently selected group
            if (!$groupstudents = get_users_by_capability($context, 'mod/quiz:attempt','','','','',$currentgroup,'',false)){
                $groupstudents = array();
            } else {
                $groupstudents = array_keys($groupstudents);
            }
            $allowed = $groupstudents;
        }

        if ($detailedmarks) {
            $questions = quiz_report_load_questions($quiz);
        } else {
            $questions = array();
        }
        $table = new quiz_report_overview_table($quiz , $qmsubselect, $groupstudents,
                $students, $detailedmarks, $questions, $candelete, $reporturl, $displayoptions);
        $table->is_downloading($download, get_string('reportoverview','quiz'),
                    "$COURSE->shortname ".format_string($quiz->name,true));
        if (!$table->is_downloading()) {
            // Only print headers if not asked to download data
            $this->print_header_and_tabs($cm, $course, $quiz, "overview");
        }

        if ($groupmode = groups_get_activity_groupmode($cm)) {   // Groups are being used
            if (!$table->is_downloading()) {
                groups_print_activity_menu($cm, $reporturl->out(false, $displayoptions));
            }
        }
        // Print information on the number of existing attempts
        if (!$table->is_downloading()) { //do not print notices when downloading
            if ($strattemptnum = quiz_num_attempt_summary($quiz, $cm, true, $currentgroup)) {
                echo '<div class="quizattemptcounts">' . $strattemptnum . '</div>';
            }
        }
        $nostudents = false;
        if (!$students){
            notify(get_string('nostudentsyet'));
            $nostudents = true;
        }else if ($currentgroup && !$groupstudents){
            notify(get_string('nostudentsingroup'));
            $nostudents = true;
        }
        if (!$table->is_downloading()) {
            // Print display options
            $mform->set_data($displayoptions +compact('detailedmarks', 'pagesize'));
            $mform->display();
        }

        if (!$nostudents || ($attemptsmode == QUIZ_REPORT_ATTEMPTS_ALL)){
            // Print information on the grading method and whether we are displaying
            //
            if (!$table->is_downloading()) { //do not print notices when downloading
                if ($strattempthighlight = quiz_report_highlighting_grading_method($quiz, $qmsubselect, $qmfilter)) {
                    echo '<div class="quizattemptcounts">' . $strattempthighlight . '</div>';
                }
            }


            $showgrades = $quiz->grade && $quiz->sumgrades && $reviewoptions->scores;
            $hasfeedback = quiz_has_feedback($quiz->id) && $quiz->grade > 1.e-7 && $quiz->sumgrades > 1.e-7;


            // Construct the SQL
            $fields = $DB->sql_concat('u.id', '\'#\'', 'COALESCE(qa.attempt, \'0\')').' AS uniqueid, '.
                ($qmsubselect?"($qmsubselect) AS gradedattempt, ":'').
                'qa.uniqueid AS attemptuniqueid, qa.id AS attempt, u.id AS userid, u.idnumber, u.firstname, u.lastname, u.picture, '.
                'qa.sumgrades, qa.timefinish, qa.timestart, qa.timefinish - qa.timestart AS duration ';

            // This part is the same for all cases - join users and quiz_attempts tables
            $from = '{user} u ';
            $from .= 'LEFT JOIN {quiz_attempts} qa ON qa.userid = u.id AND qa.quiz = :quizid';
            $params = array('quizid' => $quiz->id);

            if ($qmsubselect && $qmfilter){
                $from .= ' AND '.$qmsubselect;
            }
             switch ($attemptsmode){
                 case QUIZ_REPORT_ATTEMPTS_ALL:
                     // Show all attempts, including students who are no longer in the course
                    $where = 'qa.id IS NOT NULL AND qa.preview = 0';
                     break;
                 case QUIZ_REPORT_ATTEMPTS_STUDENTS_WITH:
                     // Show only students with attempts
                     list($allowed_usql, $allowed_params) = $DB->get_in_or_equal($allowed, SQL_PARAMS_NAMED, 'u0000');
                     $params += $allowed_params;
                    $where = "u.id $allowed_usql AND qa.preview = 0 AND qa.id IS NOT NULL";
                     break;
                 case QUIZ_REPORT_ATTEMPTS_STUDENTS_WITH_NO:
                     // Show only students without attempts
                     list($allowed_usql, $allowed_params) = $DB->get_in_or_equal($allowed, SQL_PARAMS_NAMED, 'u0000');
                     $params += $allowed_params;
                    $where = "u.id $allowed_usql AND qa.id IS NULL";
                     break;
                 case QUIZ_REPORT_ATTEMPTS_ALL_STUDENTS:
                     // Show all students with or without attempts
                     list($allowed_usql, $allowed_params) = $DB->get_in_or_equal($allowed, SQL_PARAMS_NAMED, 'u0000');
                     $params += $allowed_params;
                    $where = "u.id $allowed_usql AND (qa.preview = 0 OR qa.preview IS NULL)";
                     break;
             }

            $table->set_count_sql("SELECT COUNT(1) FROM $from WHERE $where", $params);



            $table->set_sql($fields, $from, $where, $params);

            // Define table columns
            $columns = array();
            $headers = array();


            if (!$table->is_downloading() && $candelete) {
                $columns[]= 'checkbox';
                $headers[]= NULL;
            }

            if (!$table->is_downloading() && $CFG->grade_report_showuserimage) {
                $columns[]= 'picture';
                $headers[]= '';
            }
            if (!$table->is_downloading()){
                $columns[]= 'fullname';
                $headers[]= get_string('name');
             } else {
                $columns[]= 'lastname';
                $headers[]= get_string('lastname');
                $columns[]= 'firstname';
                $headers[]= get_string('firstname');
             }

            if ($CFG->grade_report_showuseridnumber) {
                $columns[]= 'idnumber';
                $headers[]= get_string('idnumber');
            }

            $columns[]= 'timestart';
            $headers[]= get_string('startedon', 'quiz');

            $columns[]= 'timefinish';
            $headers[]= get_string('timecompleted','quiz');

            $columns[]= 'duration';
            $headers[]= get_string('attemptduration', 'quiz');

            if ($detailedmarks) {
                foreach ($questions as $id => $question) {
                    // Ignore questions of zero length
                    $columns[] = 'qsgrade'.$id;
                    $headers[] = '#'.$question->number;
                 }
            }

            if ($showgrades) {
                $columns[] = 'sumgrades';
                $headers[] = get_string('grade', 'quiz').'/'.$quiz->grade;
             }

            if ($hasfeedback) {
                $columns[] = 'feedbacktext';
                $headers[] = get_string('feedback', 'quiz');
             }

            $table->define_columns($columns);
            $table->define_headers($headers);
            $table->sortable(true, 'uniqueid');

            // Set up the table
            $table->define_baseurl($reporturl->out(false, $displayoptions));

            $table->collapsible(true);

            $table->column_suppress('picture');
            $table->column_suppress('fullname');
            $table->column_suppress('idnumber');

            $table->no_sorting('feedbacktext');

            $table->column_class('picture', 'picture');
            $table->column_class('lastname', 'bold');
            $table->column_class('firstname', 'bold');
            $table->column_class('fullname', 'bold');
            $table->column_class('sumgrades', 'bold');

            $table->set_attribute('id', 'attempts');

            $table->out($pagesize, true);
        }
        if (!$table->is_downloading()) {
            //should be quicker than a COUNT to test if there is at least one record :
            if ($DB->get_records('quiz_grades', array('quiz'=> $quiz->id), '', '*', 0, 1)){
                 $imageurl = $CFG->wwwroot.'/mod/quiz/report/overview/overviewgraph.php?id='.$quiz->id;
                 print_heading(get_string('overviewreportgraph', 'quiz_overview'));
                 echo '<div class="mdl-align"><img src="'.$imageurl.'" alt="'.get_string('overviewreportgraph', 'quiz_overview').'" /></div>';
            }
        }
        return true;
    }

}


?>