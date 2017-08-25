<?php

// This file is part of Moodle - http://moodle.org/
//
// Moodle is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Moodle is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Moodle.  If not, see <http://www.gnu.org/licenses/>.

/**
 * Library of internal classes and functions for module workshop
 *
 * All the workshop specific functions, needed to implement the module
 * logic, should go to here. Instead of having bunch of function named
 * workshop_something() taking the workshop instance as the first
 * parameter, we use a class workshop that provides all methods.
 *
 * @package   mod-workshop
 * @copyright 2009 David Mudrak <david.mudrak@gmail.com>
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */

defined('MOODLE_INTERNAL') || die();

require_once(dirname(__FILE__).'/lib.php');     // we extend this library here
require_once($CFG->libdir . '/gradelib.php');   // we use some rounding and comparing routines here

/**
 * Full-featured workshop API
 *
 * This wraps the workshop database record with a set of methods that are called
 * from the module itself. The class should be initialized right after you get
 * $workshop, $cm and $course records at the begining of the script.
 */
class workshop {

    /** return statuses of {@link add_allocation} to be passed to a workshop renderer method */
    const ALLOCATION_EXISTS             = -1;
    const ALLOCATION_ERROR              = -2;

    /** the internal code of the workshop phases as are stored in the database */
    const PHASE_SETUP                   = 10;
    const PHASE_SUBMISSION              = 20;
    const PHASE_ASSESSMENT              = 30;
    const PHASE_EVALUATION              = 40;
    const PHASE_CLOSED                  = 50;

    /** the internal code of the examples modes as are stored in the database */
    const EXAMPLES_VOLUNTARY            = 0;
    const EXAMPLES_BEFORE_SUBMISSION    = 1;
    const EXAMPLES_BEFORE_ASSESSMENT    = 2;

    /** @var stdClass course module record */
    public $cm = null;

    /** @var stdClass course record */
    public $course = null;

    /** @var stdClass context object */
    public $context = null;

    /**
     * @var workshop_strategy grading strategy instance
     * Do not use directly, get the instance using {@link workshop::grading_strategy_instance()}
     */
    protected $strategyinstance = null;

    /**
     * @var workshop_evaluation grading evaluation instance
     * Do not use directly, get the instance using {@link workshop::grading_evaluation_instance()}
     */
    protected $evaluationinstance = null;

    /**
     * Initializes the workshop API instance using the data from DB
     *
     * Makes deep copy of all passed records properties. Replaces integer $course attribute
     * with a full database record (course should not be stored in instances table anyway).
     *
     * @param stdClass $dbrecord Workshop instance data from {workshop} table
     * @param stdClass $cm       Course module record as returned by {@link get_coursemodule_from_id()}
     * @param stdClass $course   Course record from {course} table
     * @param stdClass $context  The context of the workshop instance
     */
    public function __construct(stdClass $dbrecord, stdClass $cm, stdClass $course, stdClass $context=null) {
        foreach ($dbrecord as $field => $value) {
            $this->{$field} = $value;
        }
        $this->cm           = $cm;
        $this->course       = $course;  // beware - this replaces the standard course field in the instance table
                                        // this is intentional - IMO there should be no such field as it violates
                                        // 3rd normal form with no real performance gain
        if (is_null($context)) {
            $this->context = get_context_instance(CONTEXT_MODULE, $this->cm->id);
        } else {
            $this->context = $context;
        }
        $this->evaluation   = 'best';   // todo make this configurable although we have no alternatives yet
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Static methods                                                             //
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Return list of available allocation methods
     *
     * @return array Array ['string' => 'string'] of localized allocation method names
     */
    public static function installed_allocators() {
        $installed = get_plugin_list('workshopallocation');
        $forms = array();
        foreach ($installed as $allocation => $allocationpath) {
            if (file_exists($allocationpath . '/lib.php')) {
                $forms[$allocation] = get_string('pluginname', 'workshopallocation_' . $allocation);
            }
        }
        // usability - make sure that manual allocation appears the first
        if (isset($forms['manual'])) {
            $m = array('manual' => $forms['manual']);
            unset($forms['manual']);
            $forms = array_merge($m, $forms);
        }
        return $forms;
    }

    /**
     * Returns an array of options for the editors that are used for submitting and assessing instructions
     *
     * @param stdClass $context
     * @return array
     */
    public static function instruction_editors_options(stdClass $context) {
        return array('subdirs' => 1, 'maxbytes' => 0, 'maxfiles' => EDITOR_UNLIMITED_FILES,
                     'changeformat' => 1, 'context' => $context, 'noclean' => 1, 'trusttext' => 0);
    }

    /**
     * Given the percent and the total, returns the number
     *
     * @param float $percent from 0 to 100
     * @param float $total   the 100% value
     * @return float
     */
    public static function percent_to_value($percent, $total) {
        if ($percent < 0 or $percent > 100) {
            throw new coding_exception('The percent can not be less than 0 or higher than 100');
        }

        return $total * $percent / 100;
    }

    /**
     * Returns an array of numeric values that can be used as maximum grades
     *
     * @return array Array of integers
     */
    public static function available_maxgrades_list() {
        $grades = array();
        for ($i=100; $i>=0; $i--) {
            $grades[$i] = $i;
        }
        return $grades;
    }

    /**
     * Returns the localized list of supported examples modes
     *
     * @return array
     */
    public static function available_example_modes_list() {
        $options = array();
        $options[self::EXAMPLES_VOLUNTARY]         = get_string('examplesvoluntary', 'workshop');
        $options[self::EXAMPLES_BEFORE_SUBMISSION] = get_string('examplesbeforesubmission', 'workshop');
        $options[self::EXAMPLES_BEFORE_ASSESSMENT] = get_string('examplesbeforeassessment', 'workshop');
        return $options;
    }

    /**
     * Returns the list of available grading strategy methods
     *
     * @return array ['string' => 'string']
     */
    public static function available_strategies_list() {
        $installed = get_plugin_list('workshopform');
        $forms = array();
        foreach ($installed as $strategy => $strategypath) {
            if (file_exists($strategypath . '/lib.php')) {
                $forms[$strategy] = get_string('pluginname', 'workshopform_' . $strategy);
            }
        }
        return $forms;
    }

    /**
     * Return an array of possible values of assessment dimension weight
     *
     * @return array of integers 0, 1, 2, ..., 16
     */
    public static function available_dimension_weights_list() {
        $weights = array();
        for ($i=16; $i>=0; $i--) {
            $weights[$i] = $i;
        }
        return $weights;
    }

    /**
     * Helper function returning the greatest common divisor
     *
     * @param int $a
     * @param int $b
     * @return int
     */
    public static function gcd($a, $b) {
        return ($b == 0) ? ($a):(self::gcd($b, $a % $b));
    }

    /**
     * Helper function returning the least common multiple
     *
     * @param int $a
     * @param int $b
     * @return int
     */
    public static function lcm($a, $b) {
        return ($a / self::gcd($a,$b)) * $b;
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Workshop API                                                               //
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Fetches all users with the capability mod/workshop:submit in the current context
     *
     * The returned objects contain id, lastname and firstname properties and are ordered by lastname,firstname
     *
     * @todo handle with limits and groups
     * @param bool $musthavesubmission If true, return only users who have already submitted. All possible authors otherwise.
     * @return array array[userid] => stdClass{->id ->lastname ->firstname}
     */
    public function get_potential_authors($musthavesubmission=true) {
        $users = get_users_by_capability($this->context, 'mod/workshop:submit',
                    'u.id,u.lastname,u.firstname', 'u.lastname,u.firstname,u.id', '', '', '', '', false, false, true);
        if ($musthavesubmission) {
            $users = array_intersect_key($users, $this->users_with_submission(array_keys($users)));
        }
        return $users;
    }

    /**
     * Fetches all users with the capability mod/workshop:peerassess in the current context
     *
     * The returned objects contain id, lastname and firstname properties and are ordered by lastname,firstname
     *
     * @todo handle with limits and groups
     * @param bool $musthavesubmission If true, return only users who have already submitted. All possible users otherwise.
     * @return array array[userid] => stdClass{->id ->lastname ->firstname}
     */
    public function get_potential_reviewers($musthavesubmission=false) {
        $users = get_users_by_capability($this->context, 'mod/workshop:peerassess',
                    'u.id, u.lastname, u.firstname', 'u.lastname,u.firstname,u.id', '', '', '', '', false, false, true);
        if ($musthavesubmission) {
            // users without their own submission can not be reviewers
            $users = array_intersect_key($users, $this->users_with_submission(array_keys($users)));
        }
        return $users;
    }

    /**
     * Groups the given users by the group membership
     *
     * This takes the module grouping settings into account. If "Available for group members only"
     * is set, returns only groups withing the course module grouping. Always returns group [0] with
     * all the given users.
     *
     * @param array $users array[userid] => stdClass{->id ->lastname ->firstname}
     * @return array array[groupid][userid] => stdClass{->id ->lastname ->firstname}
     */
    public function get_grouped($users) {
        global $DB;
        global $CFG;

        $grouped = array();  // grouped users to be returned
        if (empty($users)) {
            return $grouped;
        }
        if (!empty($CFG->enablegroupings) and $this->cm->groupmembersonly) {
            // Available for group members only - the workshop is available only
            // to users assigned to groups within the selected grouping, or to
            // any group if no grouping is selected.
            $groupingid = $this->cm->groupingid;
            // All users that are members of at least one group will be
            // added into a virtual group id 0
            $grouped[0] = array();
        } else {
            $groupingid = 0;
            // there is no need to be member of a group so $grouped[0] will contain
            // all users
            $grouped[0] = $users;
        }
        $gmemberships = groups_get_all_groups($this->cm->course, array_keys($users), $groupingid,
                            'gm.id,gm.groupid,gm.userid');
        foreach ($gmemberships as $gmembership) {
            if (!isset($grouped[$gmembership->groupid])) {
                $grouped[$gmembership->groupid] = array();
            }
            $grouped[$gmembership->groupid][$gmembership->userid] = $users[$gmembership->userid];
            $grouped[0][$gmembership->userid] = $users[$gmembership->userid];
        }
        return $grouped;
    }

    /**
     * Returns the list of all allocations (it est assigned assessments) in the workshop
     *
     * Assessments of example submissions are ignored
     *
     * @return array
     */
    public function get_allocations() {
        global $DB;

        $sql = 'SELECT a.id, a.submissionid, a.reviewerid, s.authorid
                  FROM {workshop_assessments} a
            INNER JOIN {workshop_submissions} s ON (a.submissionid = s.id)
                 WHERE s.example = 0 AND s.workshopid = :workshopid';
        $params = array('workshopid' => $this->id);

        return $DB->get_records_sql($sql, $params);
    }

    /**
     * Returns submissions from this workshop
     *
     * Fetches data from {workshop_submissions} and adds some useful information from other
     * tables. Does not return textual fields to prevent possible memory lack issues.
     *
     * @param mixed $authorid int|array|'all' If set to [array of] integer, return submission[s] of the given user[s] only
     * @return array of records or an empty array
     */
    public function get_submissions($authorid='all') {
        global $DB;

        $sql = 'SELECT s.id, s.workshopid, s.example, s.authorid, s.timecreated, s.timemodified,
                       s.title, s.grade, s.gradeover, s.gradeoverby,
                       u.lastname AS authorlastname, u.firstname AS authorfirstname,
                       u.picture AS authorpicture, u.imagealt AS authorimagealt,
                       t.lastname AS overlastname, t.firstname AS overfirstname,
                       t.picture AS overpicture, t.imagealt AS overimagealt
                  FROM {workshop_submissions} s
            INNER JOIN {user} u ON (s.authorid = u.id)
             LEFT JOIN {user} t ON (s.gradeoverby = t.id)
                 WHERE s.example = 0 AND s.workshopid = :workshopid';
        $params = array('workshopid' => $this->id);

        if ('all' === $authorid) {
            // no additional conditions
        } elseif (!empty($authorid)) {
            list($usql, $uparams) = $DB->get_in_or_equal($authorid, SQL_PARAMS_NAMED);
            $sql .= " AND authorid $usql";
            $params = array_merge($params, $uparams);
        } else {
            // $authorid is empty
            return array();
        }
        $sql .= ' ORDER BY u.lastname, u.firstname';

        return $DB->get_records_sql($sql, $params);
    }

    /**
     * Returns a submission record with the author's data
     *
     * @param int $id submission id
     * @return stdClass
     */
    public function get_submission_by_id($id) {
        global $DB;

        // we intentionally check the workshopid here, too, so the workshop can't touch submissions
        // from other instances
        $sql = 'SELECT s.*,
                       u.lastname AS authorlastname, u.firstname AS authorfirstname, u.id AS authorid,
                       u.picture AS authorpicture, u.imagealt AS authorimagealt
                  FROM {workshop_submissions} s
            INNER JOIN {user} u ON (s.authorid = u.id)
                 WHERE s.workshopid = :workshopid AND s.id = :id';
        $params = array('workshopid' => $this->id, 'id' => $id);
        return $DB->get_record_sql($sql, $params, MUST_EXIST);
    }

    /**
     * Returns a submission submitted by the given author
     *
     * @param int $id author id
     * @return stdClass|false
     */
    public function get_submission_by_author($authorid) {
        global $DB;

        if (empty($authorid)) {
            return false;
        }
        $sql = 'SELECT s.*,
                       u.lastname AS authorlastname, u.firstname AS authorfirstname, u.id AS authorid,
                       u.picture AS authorpicture, u.imagealt AS authorimagealt
                  FROM {workshop_submissions} s
            INNER JOIN {user} u ON (s.authorid = u.id)
                 WHERE s.example = 0 AND s.workshopid = :workshopid AND s.authorid = :authorid';
        $params = array('workshopid' => $this->id, 'authorid' => $authorid);
        return $DB->get_record_sql($sql, $params);
    }

    /**
     * Returns the list of all assessments in the workshop with some data added
     *
     * Fetches data from {workshop_assessments} and adds some useful information from other
     * tables. The returned object does not contain textual fields (ie comments) to prevent memory
     * lack issues.
     *
     * @return array [assessmentid] => assessment stdClass
     */
    public function get_all_assessments() {
        global $DB;

        $sql = 'SELECT a.id, a.submissionid, a.reviewerid, a.timecreated, a.timemodified,
                       a.grade, a.gradinggrade, a.gradinggradeover, a.gradinggradeoverby,
                       reviewer.id AS reviewerid,reviewer.firstname AS reviewerfirstname,reviewer.lastname as reviewerlastname,
                       s.title,
                       author.id AS authorid, author.firstname AS authorfirstname,author.lastname AS authorlastname
                  FROM {workshop_assessments} a
            INNER JOIN {user} reviewer ON (a.reviewerid = reviewer.id)
            INNER JOIN {workshop_submissions} s ON (a.submissionid = s.id)
            INNER JOIN {user} author ON (s.authorid = author.id)
                 WHERE s.workshopid = :workshopid AND s.example = 0
              ORDER BY reviewer.lastname, reviewer.firstname';
        $params = array('workshopid' => $this->id);

        return $DB->get_records_sql($sql, $params);
    }

    /**
     * Get the complete information about the given assessment
     *
     * @param int $id Assessment ID
     * @return mixed false if not found, stdClass otherwise
     */
    public function get_assessment_by_id($id) {
        global $DB;

        $sql = 'SELECT a.*,
                       reviewer.id AS reviewerid,reviewer.firstname AS reviewerfirstname,reviewer.lastname as reviewerlastname,
                       s.title,
                       author.id AS authorid, author.firstname AS authorfirstname,author.lastname as authorlastname
                  FROM {workshop_assessments} a
            INNER JOIN {user} reviewer ON (a.reviewerid = reviewer.id)
            INNER JOIN {workshop_submissions} s ON (a.submissionid = s.id)
            INNER JOIN {user} author ON (s.authorid = author.id)
                 WHERE a.id = :id AND s.workshopid = :workshopid';
        $params = array('id' => $id, 'workshopid' => $this->id);

        return $DB->get_record_sql($sql, $params, MUST_EXIST);
    }

    /**
     * Get the complete information about all assessments allocated to the given reviewer
     *
     * @param int $reviewerid
     * @return array
     */
    public function get_assessments_by_reviewer($reviewerid) {
        global $DB;

        $sql = 'SELECT a.*,
                       reviewer.id AS reviewerid,reviewer.firstname AS reviewerfirstname,reviewer.lastname AS reviewerlastname,
                       s.id AS submissionid, s.title AS submissiontitle, s.timecreated AS submissioncreated,
                       s.timemodified AS submissionmodified,
                       author.id AS authorid, author.firstname AS authorfirstname,author.lastname AS authorlastname,
                       author.picture AS authorpicture, author.imagealt AS authorimagealt
                  FROM {workshop_assessments} a
            INNER JOIN {user} reviewer ON (a.reviewerid = reviewer.id)
            INNER JOIN {workshop_submissions} s ON (a.submissionid = s.id)
            INNER JOIN {user} author ON (s.authorid = author.id)
                 WHERE s.example = 0 AND reviewer.id = :reviewerid AND s.workshopid = :workshopid';
        $params = array('reviewerid' => $reviewerid, 'workshopid' => $this->id);

        return $DB->get_records_sql($sql, $params);
    }

    /**
     * Allocate a submission to a user for review
     *
     * @param stdClass $submission Submission record
     * @param int $reviewerid User ID
     * @param bool $bulk repeated inserts into DB expected
     * @return int ID of the new assessment or an error code
     */
    public function add_allocation(stdClass $submission, $reviewerid, $bulk=false) {
        global $DB;

        if ($DB->record_exists('workshop_assessments', array('submissionid' => $submission->id, 'reviewerid' => $reviewerid))) {
            return self::ALLOCATION_EXISTS;
        }

        $now = time();
        $assessment = new stdClass();
        $assessment->submissionid           = $submission->id;
        $assessment->reviewerid             = $reviewerid;
        $assessment->timecreated            = $now;
        $assessment->timemodified           = $now;
        $assessment->weight                 = 1;
        $assessment->generalcommentformat   = FORMAT_HTML;  // todo better default handling
        $assessment->feedbackreviewerformat = FORMAT_HTML;  // todo better default handling

        return $DB->insert_record('workshop_assessments', $assessment, true, $bulk);
    }

    /**
     * Delete assessment record or records
     *
     * @param mixed $id int|array assessment id or array of assessments ids
     * @return bool false if $id not a valid parameter, true otherwise
     */
    public function delete_assessment($id) {
        global $DB;

        // todo remove all given grades from workshop_grades;

        if (is_array($id)) {
            return $DB->delete_records_list('workshop_assessments', 'id', $id);
        } else {
            return $DB->delete_records('workshop_assessments', array('id' => $id));
        }
    }

    /**
     * Returns instance of grading strategy class
     *
     * @return stdClass Instance of a grading strategy
     */
    public function grading_strategy_instance() {
        global $CFG;    // because we require other libs here

        if (is_null($this->strategyinstance)) {
            $strategylib = dirname(__FILE__) . '/form/' . $this->strategy . '/lib.php';
            if (is_readable($strategylib)) {
                require_once($strategylib);
            } else {
                throw new coding_exception('the grading forms subplugin must contain library ' . $strategylib);
            }
            $classname = 'workshop_' . $this->strategy . '_strategy';
            $this->strategyinstance = new $classname($this);
            if (!in_array('workshop_strategy', class_implements($this->strategyinstance))) {
                throw new coding_exception($classname . ' does not implement workshop_strategy interface');
            }
        }
        return $this->strategyinstance;
    }

    /**
     * Returns instance of grading evaluation class
     *
     * @return stdClass Instance of a grading evaluation
     */
    public function grading_evaluation_instance() {
        global $CFG;    // because we require other libs here

        if (is_null($this->evaluationinstance)) {
            $evaluationlib = dirname(__FILE__) . '/eval/' . $this->evaluation . '/lib.php';
            if (is_readable($evaluationlib)) {
                require_once($evaluationlib);
            } else {
                throw new coding_exception('the grading evaluation subplugin must contain library ' . $evaluationlib);
            }
            $classname = 'workshop_' . $this->evaluation . '_evaluation';
            $this->evaluationinstance = new $classname($this);
            if (!in_array('workshop_evaluation', class_implements($this->evaluationinstance))) {
                throw new coding_exception($classname . ' does not implement workshop_evaluation interface');
            }
        }
        return $this->evaluationinstance;
    }

    /**
     * Returns instance of submissions allocator
     *
     * @param string $method The name of the allocation method, must be PARAM_ALPHA
     * @return stdClass Instance of submissions allocator
     */
    public function allocator_instance($method) {
        global $CFG;    // because we require other libs here

        $allocationlib = dirname(__FILE__) . '/allocation/' . $method . '/lib.php';
        if (is_readable($allocationlib)) {
            require_once($allocationlib);
        } else {
            throw new coding_exception('Unable to find the allocation library ' . $allocationlib);
        }
        $classname = 'workshop_' . $method . '_allocator';
        return new $classname($this);
    }

    /**
     * @return moodle_url of this workshop's view page
     */
    public function view_url() {
        global $CFG;
        return new moodle_url($CFG->wwwroot . '/mod/workshop/view.php', array('id' => $this->cm->id));
    }

    /**
     * @return moodle_url of the page for editing this workshop's grading form
     */
    public function editform_url() {
        global $CFG;
        return new moodle_url($CFG->wwwroot . '/mod/workshop/editform.php', array('cmid' => $this->cm->id));
    }

    /**
     * @return moodle_url of the page for previewing this workshop's grading form
     */
    public function previewform_url() {
        global $CFG;
        return new moodle_url($CFG->wwwroot . '/mod/workshop/editformpreview.php', array('cmid' => $this->cm->id));
    }

    /**
     * @param int $assessmentid The ID of assessment record
     * @return moodle_url of the assessment page
     */
    public function assess_url($assessmentid) {
        global $CFG;
        $assessmentid = clean_param($assessmentid, PARAM_INT);
        return new moodle_url($CFG->wwwroot . '/mod/workshop/assessment.php', array('asid' => $assessmentid));
    }

    /**
     * @return moodle_url of the page to view own submission
     */
    public function submission_url() {
        global $CFG;
        return new moodle_url($CFG->wwwroot . '/mod/workshop/submission.php', array('cmid' => $this->cm->id));
    }

    /**
     * @return moodle_url of the mod_edit form
     */
    public function updatemod_url() {
        global $CFG;
        return new moodle_url($CFG->wwwroot . '/course/modedit.php', array('update' => $this->cm->id, 'return' => 1));
    }

    /**
     * @return moodle_url to the allocation page
     */
    public function allocation_url() {
        global $CFG;
        return new moodle_url($CFG->wwwroot . '/mod/workshop/allocation.php', array('cmid' => $this->cm->id));
    }

    /**
     * @param int $phasecode The internal phase code
     * @return moodle_url of the script to change the current phase to $phasecode
     */
    public function switchphase_url($phasecode) {
        global $CFG;
        $phasecode = clean_param($phasecode, PARAM_INT);
        return new moodle_url($CFG->wwwroot . '/mod/workshop/switchphase.php', array('cmid' => $this->cm->id, 'phase' => $phasecode));
    }

    /**
     * @return moodle_url to the aggregation page
     */
    public function aggregate_url() {
        global $CFG;
        return new moodle_url($CFG->wwwroot . '/mod/workshop/aggregate.php', array('cmid' => $this->cm->id));
    }

    /**
     * Are users allowed to create/edit their submissions?
     *
     * TODO: this depends on the workshop phase, phase deadlines, submitting after deadlines possibility
     *
     * @return bool
     */
    public function submitting_allowed() {
        return true;
    }

    /**
     * Are reviewers allowed to create/edit their assessments?
     *
     * TODO: this depends on the workshop phase, phase deadlines
     *
     * @return bool
     */
    public function assessing_allowed() {
        return true;
    }


    /**
     * Are the peer-reviews available to the authors?
     *
     * TODO: this depends on the workshop phase
     *
     * @return bool
     */
    public function assessments_available() {
        return true;
    }

    /**
     * Can the given grades be displayed to the authors?
     *
     * Grades are not displayed if {@link self::assessments_available()} return false. The returned
     * value may be true (if yes, display grades) or false (no, hide grades yet)
     *
     * @return bool
     */
    public function grades_available() {
        return true;
    }

    /**
     * Prepare an individual workshop plan for the given user.
     *
     * @param int $userid whom the plan is prepared for
     * @param stdClass context of the planned workshop
     * @return stdClass data object to be passed to the renderer
     */
    public function prepare_user_plan($userid) {
        global $DB;

        $phases = array();

        // Prepare tasks for the setup phase
        $phase = new stdClass();
        $phase->title = get_string('phasesetup', 'workshop');
        $phase->tasks = array();
        if (has_capability('moodle/course:manageactivities', $this->context, $userid)) {
            $task = new stdClass();
            $task->title = get_string('taskintro', 'workshop');
            $task->link = $this->updatemod_url();
            $task->completed = !(trim(strip_tags($this->intro)) == '');
            $phase->tasks['intro'] = $task;
        }
        if (has_capability('moodle/course:manageactivities', $this->context, $userid)) {
            $task = new stdClass();
            $task->title = get_string('taskinstructauthors', 'workshop');
            $task->link = $this->updatemod_url();
            $task->completed = !(trim(strip_tags($this->instructauthors)) == '');
            $phase->tasks['instructauthors'] = $task;
        }
        if (has_capability('mod/workshop:editdimensions', $this->context, $userid)) {
            $task = new stdClass();
            $task->title = get_string('editassessmentform', 'workshop');
            $task->link = $this->editform_url();
            if ($this->grading_strategy_instance()->form_ready()) {
                $task->completed = true;
            } elseif ($this->phase > self::PHASE_SETUP) {
                $task->completed = false;
            }
            $phase->tasks['editform'] = $task;
        }
        if (empty($phase->tasks) and $this->phase == self::PHASE_SETUP) {
            // if we are in the setup phase and there is no task (typical for students), let us
            // display some explanation what is going on
            $task = new stdClass();
            $task->title = get_string('undersetup', 'workshop');
            $task->completed = 'info';
            $phase->tasks['setupinfo'] = $task;
        }
        $phases[self::PHASE_SETUP] = $phase;

        // Prepare tasks for the submission phase
        $phase = new stdClass();
        $phase->title = get_string('phasesubmission', 'workshop');
        $phase->tasks = array();
        if (has_capability('moodle/course:manageactivities', $this->context, $userid)) {
            $task = new stdClass();
            $task->title = get_string('taskinstructreviewers', 'workshop');
            $task->link = $this->updatemod_url();
            if (trim(strip_tags($this->instructreviewers))) {
                $task->completed = true;
            } elseif ($this->phase >= self::PHASE_ASSESSMENT) {
                $task->completed = false;
            }
            $phase->tasks['instructreviewers'] = $task;
        }
        if (has_capability('mod/workshop:submit', $this->context, $userid, false)) {
            $task = new stdClass();
            $task->title = get_string('tasksubmit', 'workshop');
            $task->link = $this->submission_url();
            if ($DB->record_exists('workshop_submissions', array('workshopid'=>$this->id, 'example'=>0, 'authorid'=>$userid))) {
                $task->completed = true;
            } elseif ($this->phase >= self::PHASE_ASSESSMENT) {
                $task->completed = false;
            } else {
                $task->completed = null;    // still has a chance to submit
            }
            $phase->tasks['submit'] = $task;
        }
        $phases[self::PHASE_SUBMISSION] = $phase;
        if (has_capability('mod/workshop:allocate', $this->context, $userid)) {
            $task = new stdClass();
            $task->title = get_string('allocate', 'workshop');
            $task->link = $this->allocation_url();
            $numofauthors = count(get_users_by_capability($this->context, 'mod/workshop:submit', 'u.id', '', '', '',
                    '', '', false, true));
            $numofsubmissions = $DB->count_records('workshop_submissions', array('workshopid'=>$this->id, 'example'=>0));
            $sql = 'SELECT COUNT(s.id) AS nonallocated
                      FROM {workshop_submissions} s
                 LEFT JOIN {workshop_assessments} a ON (a.submissionid=s.id)
                     WHERE s.workshopid = :workshopid AND s.example=0 AND a.submissionid IS NULL';
            $params['workshopid'] = $this->id;
            $numnonallocated = $DB->count_records_sql($sql, $params);
            if ($numofsubmissions == 0) {
                $task->completed = null;
            } elseif ($numnonallocated == 0) {
                $task->completed = true;
            } elseif ($this->phase > self::PHASE_SUBMISSION) {
                $task->completed = false;
            } else {
                $task->completed = null;    // still has a chance to allocate
            }
            $a = new stdClass();
            $a->expected    = $numofauthors;
            $a->submitted   = $numofsubmissions;
            $a->allocate    = $numnonallocated;
            $task->details  = get_string('allocatedetails', 'workshop', $a);
            unset($a);
            $phase->tasks['allocate'] = $task;

            if ($numofsubmissions < $numofauthors and $this->phase >= self::PHASE_SUBMISSION) {
                $task = new stdClass();
                $task->title = get_string('someuserswosubmission', 'workshop');
                $task->completed = 'info';
                $phase->tasks['allocateinfo'] = $task;
            }
        }

        // Prepare tasks for the peer-assessment phase (includes eventual self-assessments)
        $phase = new stdClass();
        $phase->title = get_string('phaseassessment', 'workshop');
        $phase->tasks = array();
        $phase->isreviewer = has_capability('mod/workshop:peerassess', $this->context, $userid);
        $phase->assessments = $this->get_assessments_by_reviewer($userid);
        $numofpeers     = 0;    // number of allocated peer-assessments
        $numofpeerstodo = 0;    // number of peer-assessments to do
        $numofself      = 0;    // number of allocated self-assessments - should be 0 or 1
        $numofselftodo  = 0;    // number of self-assessments to do - should be 0 or 1
        foreach ($phase->assessments as $a) {
            if ($a->authorid == $userid) {
                $numofself++;
                if (is_null($a->grade)) {
                    $numofselftodo++;
                }
            } else {
                $numofpeers++;
                if (is_null($a->grade)) {
                    $numofpeerstodo++;
                }
            }
        }
        unset($a);
        if ($numofpeers) {
            $task = new stdClass();
            if ($numofpeerstodo == 0) {
                $task->completed = true;
            } elseif ($this->phase > self::PHASE_ASSESSMENT) {
                $task->completed = false;
            }
            $a = new stdClass();
            $a->total = $numofpeers;
            $a->todo  = $numofpeerstodo;
            $task->title = get_string('taskassesspeers', 'workshop');
            $task->details = get_string('taskassesspeersdetails', 'workshop', $a);
            unset($a);
            $phase->tasks['assesspeers'] = $task;
        }
        if ($numofself) {
            $task = new stdClass();
            if ($numofselftodo == 0) {
                $task->completed = true;
            } elseif ($this->phase > self::PHASE_ASSESSMENT) {
                $task->completed = false;
            }
            $task->title = get_string('taskassessself', 'workshop');
            $phase->tasks['assessself'] = $task;
        }
        $phases[self::PHASE_ASSESSMENT] = $phase;

        // Prepare tasks for the grading evaluation phase
        $phase = new stdClass();
        $phase->title = get_string('phaseevaluation', 'workshop');
        $phase->tasks = array();
        if (has_capability('mod/workshop:overridegrades', $this->context)) {
            $authors = $this->get_potential_authors(false);
            $reviewers = $this->get_potential_reviewers(false);
            $expected = count($authors + $reviewers);
            unset($authors);
            unset($reviewers);
            $known = $DB->count_records_select('workshop_aggregations', 'workshopid = ? AND totalgrade IS NOT NULL',
                    array($this->id));
            $task = new stdClass();
            $task->title = get_string('calculatetotalgrades', 'workshop');
            $a = new stdClass();
            $a->expected    = $expected;
            $a->known       = $known;
            $task->details  = get_string('calculatetotalgradesdetails', 'workshop', $a);
            if ($known >= $expected) {
                $task->completed = true;
            } elseif ($this->phase > self::PHASE_EVALUATION) {
                $task->completed = false;
            }
            $phase->tasks['calculatetotalgrade'] = $task;
            if ($known > 0 and $known < $expected) {
                $task = new stdClass();
                $task->title = get_string('totalgradesmissing', 'workshop');
                $task->completed = 'info';
                $phase->tasks['totalgradesmissinginfo'] = $task;
            }
        } elseif ($this->phase == self::PHASE_EVALUATION) {
            $task = new stdClass();
            $task->title = get_string('evaluategradeswait', 'workshop');
            $task->completed = 'info';
            $phase->tasks['evaluateinfo'] = $task;
        }
        $phases[self::PHASE_EVALUATION] = $phase;

        // Prepare tasks for the "workshop closed" phase - todo
        $phase = new stdClass();
        $phase->title = get_string('phaseclosed', 'workshop');
        $phase->tasks = array();
        $phases[self::PHASE_CLOSED] = $phase;

        // Polish data, set default values if not done explicitly
        foreach ($phases as $phasecode => $phase) {
            $phase->title       = isset($phase->title)      ? $phase->title     : '';
            $phase->tasks       = isset($phase->tasks)      ? $phase->tasks     : array();
            if ($phasecode == $this->phase) {
                $phase->active = true;
            } else {
                $phase->active = false;
            }
            if (!isset($phase->actions)) {
                $phase->actions = array();
            }

            foreach ($phase->tasks as $taskcode => $task) {
                $task->title        = isset($task->title)       ? $task->title      : '';
                $task->link         = isset($task->link)        ? $task->link       : null;
                $task->details      = isset($task->details)     ? $task->details    : '';
                $task->completed    = isset($task->completed)   ? $task->completed  : null;
            }
        }

        // Add phase swithing actions
        if (has_capability('mod/workshop:switchphase', $this->context, $userid)) {
            foreach ($phases as $phasecode => $phase) {
                if (! $phase->active) {
                    $action = new stdClass();
                    $action->type = 'switchphase';
                    $action->url  = $this->switchphase_url($phasecode);
                    $phase->actions[] = $action;
                }
            }
        }

        return $phases;
    }

    /**
     * Switch to a new workshop phase
     *
     * Modifies the underlying database record. You should terminate the script shortly after calling this.
     *
     * @param int $newphase new phase code
     * @return bool true if success, false otherwise
     */
    public function switch_phase($newphase) {
        global $DB;

        $known = $this->available_phases();
        if (!isset($known[$newphase])) {
            return false;
        }

        if (self::PHASE_CLOSED == $newphase) {
            // push the total grades into the gradebook

        }

        $DB->set_field('workshop', 'phase', $newphase, array('id' => $this->id));
        return true;
    }

    /**
     * Saves a raw grade for submission as calculated from the assessment form fields
     *
     * @param array $assessmentid assessment record id, must exists
     * @param mixed $grade        raw percentual grade from 0.00000 to 100.00000
     * @return false|float        the saved grade
     */
    public function set_peer_grade($assessmentid, $grade) {
        global $DB;

        if (is_null($grade)) {
            return false;
        }
        $data = new stdClass();
        $data->id = $assessmentid;
        $data->grade = $grade;
        $DB->update_record('workshop_assessments', $data);
        return $grade;
    }

    /**
     * Prepares data object with all workshop grades to be rendered
     *
     * @param int $userid the user we are preparing the report for
     * @param mixed $groups single group or array of groups - only show users who are in one of these group(s). Defaults to all
     * @param int $page the current page (for the pagination)
     * @param int $perpage participants per page (for the pagination)
     * @param string $sortby lastname|firstname|submissiontitle|submissiongrade|gradinggrade|totalgrade
     * @param string $sorthow ASC|DESC
     * @return stdClass data for the renderer
     */
    public function prepare_grading_report($userid, $groups, $page, $perpage, $sortby, $sorthow) {
        global $DB;

        $canviewall     = has_capability('mod/workshop:viewallassessments', $this->context, $userid);
        $isparticipant  = has_any_capability(array('mod/workshop:submit', 'mod/workshop:peerassess'), $this->context, $userid);

        if (!$canviewall and !$isparticipant) {
            // who the hell is this?
            return array();
        }

        if (!in_array($sortby, array('lastname','firstname','submissiontitle','submissiongrade','gradinggrade','totalgrade'))) {
            $sortby = 'lastname';
        }

        if (!($sorthow === 'ASC' or $sorthow === 'DESC')) {
            $sorthow = 'ASC';
        }

        // get the list of user ids to be displayed
        if ($canviewall) {
            // fetch the list of ids of all workshop participants - this may get really long so fetch just id
            $participants = get_users_by_capability($this->context, array('mod/workshop:submit', 'mod/workshop:peerassess'),
                    'u.id', '', '', '', $groups, '', false, false, true);
        } else {
            // this is an ordinary workshop participant (aka student) - display the report just for him/her
            $participants = array($userid => (object)array('id' => $userid));
        }

        // we will need to know the number of all records later for the pagination purposes
        $numofparticipants = count($participants);

        // load all fields which can be used for sorting and paginate the records
        list($participantids, $params) = $DB->get_in_or_equal(array_keys($participants), SQL_PARAMS_NAMED);
        $params['workshopid1'] = $this->id;
        $params['workshopid2'] = $this->id;
        $sqlsort = $sortby . ' ' . $sorthow . ',u.lastname,u.firstname,u.id';
        $sql = "SELECT u.id AS userid,u.firstname,u.lastname,u.picture,u.imagealt,
                       s.title AS submissiontitle, s.grade AS submissiongrade, ag.gradinggrade, ag.totalgrade
                  FROM {user} u
             LEFT JOIN {workshop_submissions} s ON (s.authorid = u.id AND s.workshopid = :workshopid1 AND s.example = 0)
             LEFT JOIN {workshop_aggregations} ag ON (ag.userid = u.id AND ag.workshopid = :workshopid2)
                 WHERE u.id $participantids
              ORDER BY $sqlsort";
        $participants = $DB->get_records_sql($sql, $params, $page * $perpage, $perpage);

        // this will hold the information needed to display user names and pictures
        $userinfo = array();

        // get the user details for all participants to display
        foreach ($participants as $participant) {
            if (!isset($userinfo[$participant->userid])) {
                $userinfo[$participant->userid]            = new stdClass();
                $userinfo[$participant->userid]->id        = $participant->userid;
                $userinfo[$participant->userid]->firstname = $participant->firstname;
                $userinfo[$participant->userid]->lastname  = $participant->lastname;
                $userinfo[$participant->userid]->picture   = $participant->picture;
                $userinfo[$participant->userid]->imagealt  = $participant->imagealt;
            }
        }

        // load the submissions details
        $submissions = $this->get_submissions(array_keys($participants));

        // get the user details for all moderators (teachers) that have overridden a submission grade
        foreach ($submissions as $submission) {
            if (!isset($userinfo[$submission->gradeoverby])) {
                $userinfo[$submission->gradeoverby]            = new stdClass();
                $userinfo[$submission->gradeoverby]->id        = $submission->gradeoverby;
                $userinfo[$submission->gradeoverby]->firstname = $submission->overfirstname;
                $userinfo[$submission->gradeoverby]->lastname  = $submission->overlastname;
                $userinfo[$submission->gradeoverby]->picture   = $submission->overpicture;
                $userinfo[$submission->gradeoverby]->imagealt  = $submission->overimagealt;
            }
        }

        // get the user details for all reviewers of the displayed participants
        $reviewers = array();
        if ($submissions) {
            list($submissionids, $params) = $DB->get_in_or_equal(array_keys($submissions), SQL_PARAMS_NAMED);
            $sql = "SELECT a.id AS assessmentid, a.submissionid, a.grade, a.gradinggrade, a.gradinggradeover,
                           r.id AS reviewerid, r.lastname, r.firstname, r.picture, r.imagealt,
                           s.id AS submissionid, s.authorid
                      FROM {workshop_assessments} a
                      JOIN {user} r ON (a.reviewerid = r.id)
                      JOIN {workshop_submissions} s ON (a.submissionid = s.id)
                     WHERE a.submissionid $submissionids";
            $reviewers = $DB->get_records_sql($sql, $params);
            foreach ($reviewers as $reviewer) {
                if (!isset($userinfo[$reviewer->reviewerid])) {
                    $userinfo[$reviewer->reviewerid]            = new stdClass();
                    $userinfo[$reviewer->reviewerid]->id        = $reviewer->reviewerid;
                    $userinfo[$reviewer->reviewerid]->firstname = $reviewer->firstname;
                    $userinfo[$reviewer->reviewerid]->lastname  = $reviewer->lastname;
                    $userinfo[$reviewer->reviewerid]->picture   = $reviewer->picture;
                    $userinfo[$reviewer->reviewerid]->imagealt  = $reviewer->imagealt;
                }
            }
        }

        // get the user details for all reviewees of the displayed participants
        $reviewees = array();
        if ($participants) {
            list($participantids, $params) = $DB->get_in_or_equal(array_keys($participants), SQL_PARAMS_NAMED);
            $params['workshopid'] = $this->id;
            $sql = "SELECT a.id AS assessmentid, a.submissionid, a.grade, a.gradinggrade, a.gradinggradeover, a.reviewerid,
                           s.id AS submissionid,
                           e.id AS authorid, e.lastname, e.firstname, e.picture, e.imagealt
                      FROM {user} u
                      JOIN {workshop_assessments} a ON (a.reviewerid = u.id)
                      JOIN {workshop_submissions} s ON (a.submissionid = s.id)
                      JOIN {user} e ON (s.authorid = e.id)
                     WHERE u.id $participantids AND s.workshopid = :workshopid";
            $reviewees = $DB->get_records_sql($sql, $params);
            foreach ($reviewees as $reviewee) {
                if (!isset($userinfo[$reviewee->authorid])) {
                    $userinfo[$reviewee->authorid]            = new stdClass();
                    $userinfo[$reviewee->authorid]->id        = $reviewee->authorid;
                    $userinfo[$reviewee->authorid]->firstname = $reviewee->firstname;
                    $userinfo[$reviewee->authorid]->lastname  = $reviewee->lastname;
                    $userinfo[$reviewee->authorid]->picture   = $reviewee->picture;
                    $userinfo[$reviewee->authorid]->imagealt  = $reviewee->imagealt;
                }
            }
        }

        // finally populate the object to be rendered
        $grades = $participants;

        foreach ($participants as $participant) {
            // set up default (null) values
            $grades[$participant->userid]->submissionid = null;
            $grades[$participant->userid]->submissiontitle = null;
            $grades[$participant->userid]->submissiongrade = null;
            $grades[$participant->userid]->submissiongradeover = null;
            $grades[$participant->userid]->submissiongradeoverby = null;
            $grades[$participant->userid]->reviewedby = array();
            $grades[$participant->userid]->reviewerof = array();
        }
        unset($participants);
        unset($participant);

        foreach ($submissions as $submission) {
            $grades[$submission->authorid]->submissionid = $submission->id;
            $grades[$submission->authorid]->submissiontitle = $submission->title;
            $grades[$submission->authorid]->submissiongrade = $this->real_grade($submission->grade);
            $grades[$submission->authorid]->submissiongradeover = $this->real_grade($submission->gradeover);
            $grades[$submission->authorid]->submissiongradeoverby = $submission->gradeoverby;
        }
        unset($submissions);
        unset($submission);

        foreach($reviewers as $reviewer) {
            $info = new stdClass();
            $info->userid = $reviewer->reviewerid;
            $info->assessmentid = $reviewer->assessmentid;
            $info->submissionid = $reviewer->submissionid;
            $info->grade = $this->real_grade($reviewer->grade);
            $info->gradinggrade = $this->real_grading_grade($reviewer->gradinggrade);
            $info->gradinggradeover = $this->real_grading_grade($reviewer->gradinggradeover);
            $grades[$reviewer->authorid]->reviewedby[$reviewer->reviewerid] = $info;
        }
        unset($reviewers);
        unset($reviewer);

        foreach($reviewees as $reviewee) {
            $info = new stdClass();
            $info->userid = $reviewee->authorid;
            $info->assessmentid = $reviewee->assessmentid;
            $info->submissionid = $reviewee->submissionid;
            $info->grade = $this->real_grade($reviewee->grade);
            $info->gradinggrade = $this->real_grading_grade($reviewee->gradinggrade);
            $info->gradinggradeover = $this->real_grading_grade($reviewee->gradinggradeover);
            $grades[$reviewee->reviewerid]->reviewerof[$reviewee->authorid] = $info;
        }
        unset($reviewees);
        unset($reviewee);

        foreach ($grades as $grade) {
            $grade->gradinggrade = $this->real_grading_grade($grade->gradinggrade);
            $grade->totalgrade = $this->format_total_grade($grade->totalgrade);
        }

        $data = new stdClass();
        $data->grades = $grades;
        $data->userinfo = $userinfo;
        $data->totalcount = $numofparticipants;
        $data->maxgrade = $this->real_grade(100);
        $data->maxgradinggrade = $this->real_grading_grade(100);
        $data->maxtotalgrade = $this->format_total_grade($data->maxgrade + $data->maxgradinggrade);
        return $data;
    }

    /**
     * Calculates the real value of a grade
     *
     * @param float $value percentual value from 0 to 100
     * @param float $max   the maximal grade
     * @return string
     */
    public function real_grade_value($value, $max) {
        $localized = true;
        if (is_null($value)) {
            return null;
        } elseif ($max == 0) {
            return 0;
        } else {
            return format_float($max * $value / 100, $this->gradedecimals, $localized);
        }
    }

    /**
     * Calculates the raw (percentual) value from a real grade
     *
     * This is used in cases when a user wants to give a grade such as 12 of 20 and we need to save
     * this value in a raw percentual form into DB
     * @param float $value given grade
     * @param float $max   the maximal grade
     * @return float       suitable to be stored as numeric(10,5)
     */
    public function raw_grade_value($value, $max) {
        if (is_null($value)) {
            return null;
        }
        if ($max == 0 or $value < 0) {
            return 0;
        }
        $p = $value / $max * 100;
        if ($p > 100) {
            return $max;
        }
        return grade_floatval($p);
    }

    /**
     * Rounds the value from DB to be displayed
     *
     * @param float $raw value from {workshop_aggregations}
     * @return string
     */
    public function format_total_grade($raw) {
        if (is_null($raw)) {
            return null;
        }
        return format_float($raw, $this->gradedecimals, true);
    }

    /**
     * Calculates the real value of grade for submission
     *
     * @param float $value percentual value from 0 to 100
     * @return string
     */
    public function real_grade($value) {
        return $this->real_grade_value($value, $this->grade);
    }

    /**
     * Calculates the real value of grade for assessment
     *
     * @param float $value percentual value from 0 to 100
     * @return string
     */
    public function real_grading_grade($value) {
        return $this->real_grade_value($value, $this->gradinggrade);
    }

    /**
     * Calculates grades for submission for the given participant(s) and updates it in the database
     *
     * @param null|int|array $restrict If null, update all authors, otherwise update just grades for the given author(s)
     * @return void
     */
    public function aggregate_submission_grades($restrict=null) {
        global $DB;

        // fetch a recordset with all assessments to process
        $sql = 'SELECT s.id AS submissionid, s.grade AS submissiongrade,
                       a.weight, a.grade
                  FROM {workshop_submissions} s
             LEFT JOIN {workshop_assessments} a ON (a.submissionid = s.id)
                 WHERE s.example=0 AND s.workshopid=:workshopid'; // to be cont.
        $params = array('workshopid' => $this->id);

        if (is_null($restrict)) {
            // update all users - no more conditions
        } elseif (!empty($restrict)) {
            list($usql, $uparams) = $DB->get_in_or_equal($restrict, SQL_PARAMS_NAMED);
            $sql .= " AND s.authorid $usql";
            $params = array_merge($params, $uparams);
        } else {
            throw new coding_exception('Empty value is not a valid parameter here');
        }

        $sql .= ' ORDER BY s.id'; // this is important for bulk processing

        $rs         = $DB->get_recordset_sql($sql, $params);
        $batch      = array();    // will contain a set of all assessments of a single submission
        $previous   = null;       // a previous record in the recordset

        foreach ($rs as $current) {
            if (is_null($previous)) {
                // we are processing the very first record in the recordset
                $previous   = $current;
            }
            if ($current->submissionid == $previous->submissionid) {
                // we are still processing the current submission
                $batch[] = $current;
            } else {
                // process all the assessments of a sigle submission
                $this->aggregate_submission_grades_process($batch);
                // and then start to process another submission
                $batch      = array($current);
                $previous   = $current;
            }
        }
        // do not forget to process the last batch!
        $this->aggregate_submission_grades_process($batch);
        $rs->close();
    }

    /**
     * Calculates grades for assessment for the given participant(s)
     *
     * Grade for assessment is calculated as a simple mean of all grading grades calculated by the grading evaluator.
     * The assessment weight is not taken into account here.
     *
     * @param null|int|array $restrict If null, update all reviewers, otherwise update just grades for the given reviewer(s)
     * @return void
     */
    public function aggregate_grading_grades($restrict=null) {
        global $DB;

        // fetch a recordset with all assessments to process
        $sql = 'SELECT a.reviewerid, a.gradinggrade, a.gradinggradeover,
                       ag.id AS aggregationid, ag.gradinggrade AS aggregatedgrade
                  FROM {workshop_assessments} a
            INNER JOIN {workshop_submissions} s ON (a.submissionid = s.id)
             LEFT JOIN {workshop_aggregations} ag ON (ag.userid = a.reviewerid AND ag.workshopid = s.workshopid)
                 WHERE s.example=0 AND s.workshopid=:workshopid'; // to be cont.
        $params = array('workshopid' => $this->id);

        if (is_null($restrict)) {
            // update all users - no more conditions
        } elseif (!empty($restrict)) {
            list($usql, $uparams) = $DB->get_in_or_equal($restrict, SQL_PARAMS_NAMED);
            $sql .= " AND a.reviewerid $usql";
            $params = array_merge($params, $uparams);
        } else {
            throw new coding_exception('Empty value is not a valid parameter here');
        }

        $sql .= ' ORDER BY a.reviewerid'; // this is important for bulk processing

        $rs         = $DB->get_recordset_sql($sql, $params);
        $batch      = array();    // will contain a set of all assessments of a single submission
        $previous   = null;       // a previous record in the recordset

        foreach ($rs as $current) {
            if (is_null($previous)) {
                // we are processing the very first record in the recordset
                $previous   = $current;
            }
            if ($current->reviewerid == $previous->reviewerid) {
                // we are still processing the current reviewer
                $batch[] = $current;
            } else {
                // process all the assessments of a sigle submission
                $this->aggregate_grading_grades_process($batch);
                // and then start to process another reviewer
                $batch      = array($current);
                $previous   = $current;
            }
        }
        // do not forget to process the last batch!
        $this->aggregate_grading_grades_process($batch);
        $rs->close();
    }

    /**
     * Calculates the workshop total grades for the given participant(s)
     *
     * @param null|int|array $restrict If null, update all reviewers, otherwise update just grades for the given reviewer(s)
     * @return void
     */
    public function aggregate_total_grades($restrict=null) {
        global $DB;

        // fetch a recordset with all assessments to process
        $sql = 'SELECT s.grade, s.gradeover, s.authorid AS userid,
                       ag.id AS agid, ag.gradinggrade, ag.totalgrade
                  FROM {workshop_submissions} s
            INNER JOIN {workshop_aggregations} ag ON (ag.userid = s.authorid)
                 WHERE s.example=0 AND s.workshopid=:workshopid'; // to be cont.
        $params = array('workshopid' => $this->id);

        if (is_null($restrict)) {
            // update all users - no more conditions
        } elseif (!empty($restrict)) {
            list($usql, $uparams) = $DB->get_in_or_equal($restrict, SQL_PARAMS_NAMED);
            $sql .= " AND ag.userid $usql";
            $params = array_merge($params, $uparams);
        } else {
            throw new coding_exception('Empty value is not a valid parameter here');
        }

        $sql .= ' ORDER BY ag.userid'; // this is important for bulk processing

        $rs         = $DB->get_recordset_sql($sql, $params);

        foreach ($rs as $current) {
            $this->aggregate_total_grades_process($current);
        }
        $rs->close();
    }

    /**
     * Returns the mform the teachers use to put a feedback for the reviewer
     *
     * @return workshop_feedbackreviewer_form
     */
    public function get_feedbackreviewer_form(moodle_url $actionurl, stdClass $assessment, $editable=true) {
        global $CFG;
        require_once(dirname(__FILE__) . '/feedbackreviewer_form.php');

        $current = new stdClass();
        $current->asid                      = $assessment->id;
        $current->gradinggrade              = $this->real_grading_grade($assessment->gradinggrade);
        $current->gradinggradeover          = $this->real_grading_grade($assessment->gradinggradeover);
        $current->feedbackreviewer          = $assessment->feedbackreviewer;
        $current->feedbackreviewerformat    = $assessment->feedbackreviewerformat;
        if (is_null($current->gradinggrade)) {
            $current->gradinggrade = get_string('nullgrade', 'workshop');
        }

        // prepare wysiwyg editor
        $current = file_prepare_standard_editor($current, 'feedbackreviewer', array());

        return new workshop_feedbackreviewer_form($actionurl,
                array('workshop' => $this, 'current' => $current, 'feedbackopts' => array()),
                'post', '', null, $editable);
    }

    ////////////////////////////////////////////////////////////////////////////////
    // Internal methods (implementation details)                                  //
    ////////////////////////////////////////////////////////////////////////////////

    /**
     * Given an array of all assessments of a single submission, calculates the final grade for this submission
     *
     * This calculates the weighted mean of the passed assessment grades. If, however, the submission grade
     * was overridden by a teacher, the gradeover value is returned and the rest of grades are ignored.
     *
     * @param array $assessments of stdClass(->submissionid ->submissiongrade ->gradeover ->weight ->grade)
     * @return void
     */
    protected function aggregate_submission_grades_process(array $assessments) {
        global $DB;

        $submissionid   = null; // the id of the submission being processed
        $current        = null; // the grade currently saved in database
        $finalgrade     = null; // the new grade to be calculated
        $sumgrades      = 0;
        $sumweights     = 0;

        foreach ($assessments as $assessment) {
            if (is_null($submissionid)) {
                // the id is the same in all records, fetch it during the first loop cycle
                $submissionid = $assessment->submissionid;
            }
            if (is_null($current)) {
                // the currently saved grade is the same in all records, fetch it during the first loop cycle
                $current = $assessment->submissiongrade;
            }
            if (is_null($assessment->grade)) {
                // this was not assessed yet
                continue;
            }
            if ($assessment->weight == 0) {
                // this does not influence the calculation
                continue;
            }
            $sumgrades  += $assessment->grade * $assessment->weight;
            $sumweights += $assessment->weight;
        }
        if ($sumweights > 0 and is_null($finalgrade)) {
            $finalgrade = grade_floatval($sumgrades / $sumweights);
        }
        // check if the new final grade differs from the one stored in the database
        if (grade_floats_different($finalgrade, $current)) {
            // we need to save new calculation into the database
            $DB->set_field('workshop_submissions', 'grade', $finalgrade, array('id' => $submissionid));
        }
    }

    /**
     * Given an array of all assessments done by a single reviewer, calculates the final grading grade
     *
     * This calculates the simple mean of the passed grading grades. If, however, the grading grade
     * was overridden by a teacher, the gradinggradeover value is returned and the rest of grades are ignored.
     *
     * @param array $assessments of stdClass(->reviewerid ->gradinggrade ->gradinggradeover ->aggregationid ->aggregatedgrade)
     * @return void
     */
    protected function aggregate_grading_grades_process(array $assessments) {
        global $DB;

        $reviewerid = null; // the id of the reviewer being processed
        $current    = null; // the gradinggrade currently saved in database
        $finalgrade = null; // the new grade to be calculated
        $agid       = null; // aggregation id
        $sumgrades  = 0;
        $count      = 0;

        foreach ($assessments as $assessment) {
            if (is_null($reviewerid)) {
                // the id is the same in all records, fetch it during the first loop cycle
                $reviewerid = $assessment->reviewerid;
            }
            if (is_null($agid)) {
                // the id is the same in all records, fetch it during the first loop cycle
                $agid = $assessment->aggregationid;
            }
            if (is_null($current)) {
                // the currently saved grade is the same in all records, fetch it during the first loop cycle
                $current = $assessment->aggregatedgrade;
            }
            if (!is_null($assessment->gradinggradeover)) {
                // the grading grade for this assessment is overriden by a teacher
                $sumgrades += $assessment->gradinggradeover;
                $count++;
            } else {
                if (!is_null($assessment->gradinggrade)) {
                    $sumgrades += $assessment->gradinggrade;
                    $count++;
                }
            }
        }
        if ($count > 0) {
            $finalgrade = grade_floatval($sumgrades / $count);
        }
        // check if the new final grade differs from the one stored in the database
        if (grade_floats_different($finalgrade, $current)) {
            // we need to save new calculation into the database
            if (is_null($agid)) {
                // no aggregation record yet
                $record = new stdClass();
                $record->workshopid = $this->id;
                $record->userid = $reviewerid;
                $record->gradinggrade = $finalgrade;
                $DB->insert_record('workshop_aggregations', $record);
            } else {
                $DB->set_field('workshop_aggregations', 'gradinggrade', $finalgrade, array('id' => $agid));
            }
        }
    }

    /**
     * Given an object with final grade for submission and final grade for assessment, updates the total grade in DB
     *
     * @param stdClass $data
     * @return void
     */
    protected function aggregate_total_grades_process(stdClass $data) {
        global $DB;

        if (!is_null($data->gradeover)) {
            $submissiongrade = $data->gradeover;
        } else {
            $submissiongrade = $data->grade;
        }

        // If we do not have enough information to update totalgrade, do not do
        // anything. Please note there may be a lot of reasons why the workshop
        // participant does not have one of these grades - maybe she was ill or
        // just did not reach the deadlines. Teacher has to fix grades in
        // gradebook manually.

        if (is_null($submissiongrade) or (!empty($this->gradinggrade) and is_null($this->gradinggrade))) {
            return;
        }

        $totalgrade = $this->grade * $submissiongrade / 100 + $this->gradinggrade * $data->gradinggrade / 100;

        // check if the new total grade differs from the one stored in the database
        if (grade_floats_different($totalgrade, $data->totalgrade)) {
            // we need to save new calculation into the database
            if (is_null($data->agid)) {
                // no aggregation record yet
                $record = new stdClass();
                $record->workshopid = $this->id;
                $record->userid = $data->userid;
                $record->totalgrade = $totalgrade;
                $DB->insert_record('workshop_aggregations', $record);
            } else {
                $DB->set_field('workshop_aggregations', 'totalgrade', $totalgrade, array('id' => $data->agid));
            }
        }
    }

    /**
     * Given a list of user ids, returns the filtered one containing just ids of users with own submission
     *
     * Example submissions are ignored.
     *
     * @param array $userids
     * @return array
     */
    protected function users_with_submission(array $userids) {
        global $DB;

        if (empty($userids)) {
            return array();
        }
        $userswithsubmission = array();
        list($usql, $uparams) = $DB->get_in_or_equal($userids, SQL_PARAMS_NAMED);
        $sql = "SELECT id,authorid
                  FROM {workshop_submissions}
                 WHERE example = 0 AND workshopid = :workshopid AND authorid $usql";
        $params = array('workshopid' => $this->id);
        $params = array_merge($params, $uparams);
        $submissions = $DB->get_records_sql($sql, $params);
        foreach ($submissions as $submission) {
            $userswithsubmission[$submission->authorid] = true;
        }

        return $userswithsubmission;
    }

    /**
     * @return array of available workshop phases
     */
    protected function available_phases() {
        return array(
            self::PHASE_SETUP       => true,
            self::PHASE_SUBMISSION  => true,
            self::PHASE_ASSESSMENT  => true,
            self::PHASE_EVALUATION  => true,
            self::PHASE_CLOSED      => true,
        );
    }

}