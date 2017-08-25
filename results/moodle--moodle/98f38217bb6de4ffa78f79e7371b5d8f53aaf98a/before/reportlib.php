<?php
define('QUIZ_REPORT_DEFAULT_PAGE_SIZE', 30);
define('QUIZ_REPORT_DEFAULT_GRADING_PAGE_SIZE', 10);

define('QUIZ_REPORT_ATTEMPTS_ALL', 0);
define('QUIZ_REPORT_ATTEMPTS_STUDENTS_WITH_NO', 1);
define('QUIZ_REPORT_ATTEMPTS_STUDENTS_WITH', 2);
define('QUIZ_REPORT_ATTEMPTS_ALL_STUDENTS', 3);
/**
 * Get newest graded state or newest state for a number of attempts. Pass in the
 * uniqueid field from quiz_attempt table not the id. Use question_state_is_graded
 * function to check that the question is actually graded.
 * @param array attemptidssql either an array of attemptids with numerical keys
 * or an object with properties from, where and params.
 * @param boolean idxattemptq true if a multidimensional array should be
 * constructed with keys indexing array first by attempt and then by question
 * id.
 */
function quiz_get_newgraded_states($attemptidssql, $idxattemptq = true, $fields='qs.*'){
    global $CFG, $DB;
    if ($attemptidssql && is_array($attemptidssql)){
        list($usql, $params) = $DB->get_in_or_equal($attemptidssql);
        $gradedstatesql = "SELECT $fields FROM " .
                "{question_sessions} qns, " .
                "{question_states} qs " .
                "WHERE qns.attemptid $usql AND " .
                "qns.newgraded = qs.id";
        $gradedstates = $DB->get_records_sql($gradedstatesql, $params);
    } else if ($attemptidssql && is_object($attemptidssql)){
        $gradedstatesql = "SELECT $fields FROM " .
                $attemptidssql->from.",".
                "{question_sessions} qns, " .
                "{question_states} qs " .
                "WHERE qns.attemptid = qa.uniqueid AND " .
                $attemptidssql->where." AND ".
                "qns.newgraded = qs.id";
        $gradedstates = $DB->get_records_sql($gradedstatesql, $attemptidssql->params);
    } else {
        return array();
    }
    if ($idxattemptq){
        $gradedstatesbyattempt = array();
        foreach ($gradedstates as $gradedstate){
            if (!isset($gradedstatesbyattempt[$gradedstate->attempt])){
                $gradedstatesbyattempt[$gradedstate->attempt] = array();
            }
            $gradedstatesbyattempt[$gradedstate->attempt][$gradedstate->question] = $gradedstate;
        }
        return $gradedstatesbyattempt;
    } else {
        return $gradedstates;
    }
}

function quiz_get_average_grade_for_questions($quiz, $userids){
    global $CFG, $DB;
    $qmfilter = quiz_report_qm_filter_select($quiz);
    list($usql, $params) = $DB->get_in_or_equal($userids);
    $params[] = $quiz->id;
    $questionavgssql = "SELECT qns.questionid, AVG(qs.grade) FROM
                        {quiz_attempts} qa
                        LEFT JOIN {question_sessions} qns ON (qns.attemptid = qa.uniqueid)
                        LEFT JOIN {question_states} qs ON (qns.newgraded = qs.id AND qs.event IN (".QUESTION_EVENTS_GRADED."))
                        WHERE " .
                        "($qmfilter) AND " .
                        "qa.userid $usql AND " .
                        "qa.quiz = ? ".
                        "GROUP BY qs.question";
    return $DB->get_records_sql_menu($questionavgssql, $params);
}

function quiz_get_total_qas_graded_and_ungraded($quiz, $questionids, $userids){
    global $CFG, $DB;
    $params = array($quiz->id);
    list($u_sql, $u_params) = $DB->get_in_or_equal($userids);
    list($q_sql, $q_params) = $DB->get_in_or_equal($questionids);

    $params = array_merge($params, $u_params, $q_params);
    $sql = "SELECT qs.question, COUNT(1) AS totalattempts,
            SUM(qs.event IN (".QUESTION_EVENTS_GRADED.")) AS gradedattempts
            FROM
            {quiz_attempts} qa,
            {question_sessions} qns,
            {question_states} qs
            WHERE
            qa.quiz = ? AND
            qa.userid $u_sql AND
            qns.attemptid = qa.uniqueid AND
            qns.newgraded = qs.id AND
            qs.question $q_sql
            GROUP BY qs.question";
    return $DB->get_records_sql($sql, $params);
}

function quiz_format_average_grade_for_questions($avggradebyq, $questions, $quiz, $download){
    $row = array();
    if (!$avggradebyq){
        $avggradebyq = array();
    }
    foreach(array_keys($questions) as $questionid) {
        if (isset($avggradebyq[$questionid])){
            $grade = $avggradebyq[$questionid];
            $grade = quiz_rescale_grade($grade, $quiz);
        } else {
            $grade = '--';
        }
        if (!$download) {
            $grade = $grade.'/'.quiz_rescale_grade($questions[$questionid]->grade, $quiz);
        }
        $row['qsgrade'.$questionid]= $grade;
    }
    return $row;
}
/**
 * Load the question data necessary in the reports.
 * - Remove description questions.
 * - Order questions in order that they are in the quiz
 * - Add question numbers.
 * - Add grade from quiz_questions_instance
 */
function quiz_report_load_questions($quiz){
    global $CFG, $DB;
    $questionlist = quiz_questions_in_quiz($quiz->questions);
    //In fact in most cases the id IN $questionlist below is redundant
    //since we are also doing a JOIN on the qqi table. But will leave it in
    //since this double check will probably do no harm.
    list($usql, $params) = $DB->get_in_or_equal(explode(',', $questionlist));
    $params[] = $quiz->id;
    if (!$questions = $DB->get_records_sql("SELECT q.*, qqi.grade
            FROM {question} q,
            {quiz_question_instances} qqi
            WHERE q.id $usql AND
            qqi.question = q.id AND
            qqi.quiz = ?", $params)) {
        print_error('noquestionsfound', 'quiz');
    }
    //Now we have an array of questions from a quiz we work out there question nos and remove
    //questions with zero length ie. description questions etc.
    //also put questions in order.
    $number = 1;
    $realquestions = array();
    $questionids = explode(',', $questionlist);
    foreach ($questionids as $id) {
        if ($questions[$id]->length) {
            // Ignore questions of zero length
            $realquestions[$id] = $questions[$id];
            $realquestions[$id]->number = $number;
            $number += $questions[$id]->length;
        }
    }
    return $realquestions;
}
/**
 * Given the quiz grading method return sub select sql to find the id of the
 * one attempt that will be graded for each user. Or return
 * empty string if all attempts contribute to final grade.
 */
function quiz_report_qm_filter_select($quiz){
    if ($quiz->attempts == 1) {//only one attempt allowed on this quiz
        return '';
    }
    $useridsql = 'qa.userid';
    $quizidsql = 'qa.quiz';
    $qmfilterattempts = true;
    switch ($quiz->grademethod) {
    case QUIZ_GRADEHIGHEST :
        $field1 = 'sumgrades';
        $field2 = 'timestart';
        $aggregator1 = 'MAX';
        $aggregator2 = 'MIN';
        $qmselectpossible = true;
        break;
    case QUIZ_GRADEAVERAGE :
        $qmselectpossible = false;
        break;
    case QUIZ_ATTEMPTFIRST :
        $field1 = 'timestart';
        $field2 = 'id';
        $aggregator1 = 'MIN';
        $aggregator2 = 'MIN';
        $qmselectpossible = true;
        break;
    case QUIZ_ATTEMPTLAST :
        $field1 = 'timestart';
        $field2 = 'id';
        $aggregator1 = 'MAX';
        $aggregator2 = 'MAX';
        $qmselectpossible = true;
        break;
    }
    if ($qmselectpossible){
        $qmselect = "qa.$field1 = (SELECT $aggregator1(qa2.$field1) FROM {quiz_attempts} qa2 WHERE qa2.quiz = $quizidsql AND qa2.userid = $useridsql) AND " .
                    "qa.$field2 = (SELECT $aggregator2(qa3.$field2) FROM {quiz_attempts} qa3 WHERE qa3.quiz = $quizidsql AND qa3.userid = $useridsql AND qa3.$field1 = qa.$field1)";
    } else {
        $qmselect = '';
    }

    return $qmselect;
}

function quiz_report_grade_bands($bandwidth, $bands, $quizid, $userids=array()){
    global $CFG, $DB;
    if ($userids){
        list($usql, $params) = $DB->get_in_or_equal($userids);
    } else {
        $usql ='';
        $params = array();
    }
    $sql = "SELECT
        FLOOR(qg.grade/$bandwidth) AS band,
        COUNT(1) AS num
    FROM
        {quiz_grades} qg,  {quiz} q
    WHERE qg.quiz = q.id " .
            ($usql?"AND qg.userid $usql ":'') .
            "AND qg.quiz = ?
    GROUP BY band
    ORDER BY band";
    $params[] = $quizid;
    $data = $DB->get_records_sql_menu($sql, $params);
    //need to create array elements with values 0 at indexes where there is no element
    $data =  $data + array_fill(0, $bands+1, 0);
    ksort($data);
    //place the maximum (prefect grade) into the last band i.e. make last
    //band for example 9 <= g <=10 (where 10 is the perfect grade) rather than
    //just 9 <= g <10.
    $data[$bands-1] += $data[$bands];
    unset($data[$bands]);
    return $data;
}
function quiz_report_highlighting_grading_method($quiz, $qmsubselect, $qmfilter){
    if ($quiz->attempts == 1) {//only one attempt allowed on this quiz
        return "<p>".get_string('onlyoneattemptallowed', "quiz_overview")."</p>";
    } else if (!$qmsubselect){
        return "<p>".get_string('allattemptscontributetograde', "quiz_overview")."</p>";
    } else if ($qmfilter){
        return "<p>".get_string('showinggraded', "quiz_overview")."</p>";
    }else {
        return "<p>".get_string('showinggradedandungraded', "quiz_overview",
                ('<span class="highlight">'.quiz_get_grading_option_name($quiz->grademethod).'</span>'))."</p>";
    }
}


/**
 * Get the feedback text for a grade on this quiz. The feedback is
 * processed ready for display.
 *
 * @param float $grade a grade on this quiz.
 * @param integer $quizid the id of the quiz object.
 * @return string the comment that corresponds to this grade (empty string if there is not one.
 */
function quiz_report_feedback_for_grade($grade, $quizid) {
    global $DB;
    static $feedbackcache = array();
    if (!isset($feedbackcache[$quizid])){
        $feedbackcache[$quizid] = $DB->get_records('quiz_feedback', array('quizid' => $quizid));
    }
    $feedbacks = $feedbackcache[$quizid];
    $feedbacktext = '';
    foreach ($feedbacks as $feedback) {
        if ($feedback->mingrade <= $grade && $grade < $feedback->maxgrade){
            $feedbacktext = $feedback->feedbacktext;
            break;
        }
    }

    // Clean the text, ready for display.
    $formatoptions = new stdClass;
    $formatoptions->noclean = true;
    $feedbacktext = format_text($feedbacktext, FORMAT_MOODLE, $formatoptions);

    return $feedbacktext;
}

function quiz_report_scale_sumgrades_as_percentage($rawgrade, $quiz, $round = true) {
    if ($quiz->sumgrades) {
        $grade = $rawgrade * 100 / $quiz->sumgrades;
        if ($round) {
            $grade = number_format($grade, $quiz->decimalpoints);
        }
    } else {
        $grade = 0;
    }
    return $grade.' %';
}
?>