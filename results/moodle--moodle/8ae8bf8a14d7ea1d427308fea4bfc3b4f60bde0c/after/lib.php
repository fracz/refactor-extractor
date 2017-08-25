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
 * File in which the grader_report class is defined.
 * @package gradebook
 */

require_once($CFG->dirroot . '/grade/report/lib.php');
require_once($CFG->libdir.'/tablelib.php');

/**
 * Class providing an API for the grader report building and displaying.
 * @uses grade_report
 * @package gradebook
 */
class grade_report_grader extends grade_report {
    /**
     * The final grades.
     * @var array $grades
     */
    public $grades;

    /**
     * Array of errors for bulk grades updating.
     * @var array $gradeserror
     */
    public $gradeserror = array();

//// SQL-RELATED

    /**
     * The id of the grade_item by which this report will be sorted.
     * @var int $sortitemid
     */
    public $sortitemid;

    /**
     * Sortorder used in the SQL selections.
     * @var int $sortorder
     */
    public $sortorder;

    /**
     * An SQL fragment affecting the search for users.
     * @var string $userselect
     */
    public $userselect;

    /**
     * The bound params for $userselect
     * @var array $userselectparams
     */
    public $userselectparams = array();

    /**
     * List of collapsed categories from user preference
     * @var array $collapsed
     */
    public $collapsed;

    /**
     * A count of the rows, used for css classes.
     * @var int $rowcount
     */
    public $rowcount = 0;

    /**
     * Capability check caching
     * */
    public $canviewhidden;

    var $preferencespage=false;

    /**
     * Constructor. Sets local copies of user preferences and initialises grade_tree.
     * @param int $courseid
     * @param object $gpr grade plugin return tracking object
     * @param string $context
     * @param int $page The current page being viewed (when report is paged)
     * @param int $sortitemid The id of the grade_item by which to sort the table
     */
    public function __construct($courseid, $gpr, $context, $page=null, $sortitemid=null) {
        global $CFG;
        parent::__construct($courseid, $gpr, $context, $page);

        $this->canviewhidden = has_capability('moodle/grade:viewhidden', get_context_instance(CONTEXT_COURSE, $this->course->id));

        // load collapsed settings for this report
        if ($collapsed = get_user_preferences('grade_report_grader_collapsed_categories')) {
            $this->collapsed = unserialize($collapsed);
        } else {
            $this->collapsed = array('aggregatesonly' => array(), 'gradesonly' => array());
        }

        if (empty($CFG->enableoutcomes)) {
            $nooutcomes = false;
        } else {
            $nooutcomes = get_user_preferences('grade_report_shownooutcomes');
        }

        // if user report preference set or site report setting set use it, otherwise use course or site setting
        $switch = $this->get_pref('aggregationposition');
        if ($switch == '') {
            $switch = grade_get_setting($this->courseid, 'aggregationposition', $CFG->grade_aggregationposition);
        }

        // Grab the grade_tree for this course
        $this->gtree = new grade_tree($this->courseid, true, $switch, $this->collapsed, $nooutcomes);

        $this->sortitemid = $sortitemid;

        // base url for sorting by first/last name

        $this->baseurl = new moodle_url('index.php', array('id' => $this->courseid));

        $studentsperpage = $this->get_pref('studentsperpage');
        if (!empty($studentsperpage)) {
            $this->baseurl->params(array('perpage' => $studentsperpage, 'page' => $this->page));
        }

        $this->pbarurl = new moodle_url($CFG->wwwroot.'/grade/report/grader/index.php', array('id' => $this->courseid, 'perpage' => $studentsperpage));

        $this->setup_groups();

        $this->setup_sortitemid();
    }

    /**
     * Processes the data sent by the form (grades and feedbacks).
     * Caller is reposible for all access control checks
     * @param array $data form submission (with magic quotes)
     * @return array empty array if success, array of warnings if something fails.
     */
    public function process_data($data) {
        global $DB;
        $warnings = array();

        $separategroups = false;
        $mygroups       = array();
        if ($this->groupmode == SEPARATEGROUPS and !has_capability('moodle/site:accessallgroups', $this->context)) {
            $separategroups = true;
            $mygroups = groups_get_user_groups($this->course->id);
            $mygroups = $mygroups[0]; // ignore groupings
            // reorder the groups fro better perf bellow
            $current = array_search($this->currentgroup, $mygroups);
            if ($current !== false) {
                unset($mygroups[$current]);
                array_unshift($mygroups, $this->currentgroup);
            }
        }

        // always initialize all arrays
        $queue = array();
        foreach ($data as $varname => $postedvalue) {

            $needsupdate = false;

            // skip, not a grade nor feedback
            if (strpos($varname, 'grade') === 0) {
                $datatype = 'grade';
            } else if (strpos($varname, 'feedback') === 0) {
                $datatype = 'feedback';
            } else {
                continue;
            }

            $gradeinfo = explode("_", $varname);
            $userid = clean_param($gradeinfo[1], PARAM_INT);
            $itemid = clean_param($gradeinfo[2], PARAM_INT);

            $oldvalue = $data->{'old'.$varname};

            // was change requested?
            if ($oldvalue == $postedvalue) { // string comparison
                continue;
            }

            if (!$gradeitem = grade_item::fetch(array('id'=>$itemid, 'courseid'=>$this->courseid))) { // we must verify course id here!
                print_error('invalidgradeitmeid');
            }

            // Pre-process grade
            if ($datatype == 'grade') {
                $feedback = false;
                $feedbackformat = false;
                if ($gradeitem->gradetype == GRADE_TYPE_SCALE) {
                    if ($postedvalue == -1) { // -1 means no grade
                        $finalgrade = null;
                    } else {
                        $finalgrade = $postedvalue;
                    }
                } else {
                    $finalgrade = unformat_float($postedvalue);
                }

                $errorstr = '';
                // Warn if the grade is out of bounds.
                if (is_null($finalgrade)) {
                    // ok
                } else {
                    $bounded = $gradeitem->bounded_grade($finalgrade);
                    if ($bounded > $finalgrade) {
                    $errorstr = 'lessthanmin';
                    } else if ($bounded < $finalgrade) {
                        $errorstr = 'morethanmax';
                    }
                }
                if ($errorstr) {
                    $user = $DB->get_record('user', array('id' => $userid), 'id, firstname, lastname');
                    $gradestr = new object();
                    $gradestr->username = fullname($user);
                    $gradestr->itemname = $gradeitem->get_name();
                    $warnings[] = get_string($errorstr, 'grades', $gradestr);
                }

            } else if ($datatype == 'feedback') {
                $finalgrade = false;
                $trimmed = trim($postedvalue);
                if (empty($trimmed)) {
                     $feedback = NULL;
                } else {
                     $feedback = stripslashes($postedvalue);
                }
            }

            // group access control
            if ($separategroups) {
                // note: we can not use $this->currentgroup because it would fail badly
                //       when having two browser windows each with different group
                $sharinggroup = false;
                foreach($mygroups as $groupid) {
                    if (groups_is_member($groupid, $userid)) {
                        $sharinggroup = true;
                        break;
                    }
                }
                if (!$sharinggroup) {
                    // either group membership changed or somebedy is hacking grades of other group
                    $warnings[] = get_string('errorsavegrade', 'grades');
                    continue;
                }
            }

            $gradeitem->update_final_grade($userid, $finalgrade, 'gradebook', $feedback, FORMAT_MOODLE);
        }

        return $warnings;
    }


    /**
     * Setting the sort order, this depends on last state
     * all this should be in the new table class that we might need to use
     * for displaying grades.
     */
    private function setup_sortitemid() {

        global $SESSION;

        if ($this->sortitemid) {
            if (!isset($SESSION->gradeuserreport->sort)) {
                if ($this->sortitemid == 'firstname' || $this->sortitemid == 'lastname') {
                    $this->sortorder = $SESSION->gradeuserreport->sort = 'ASC';
                } else {
                    $this->sortorder = $SESSION->gradeuserreport->sort = 'DESC';
                }
            } else {
                // this is the first sort, i.e. by last name
                if (!isset($SESSION->gradeuserreport->sortitemid)) {
                    if ($this->sortitemid == 'firstname' || $this->sortitemid == 'lastname') {
                        $this->sortorder = $SESSION->gradeuserreport->sort = 'ASC';
                    } else {
                        $this->sortorder = $SESSION->gradeuserreport->sort = 'DESC';
                    }
                } else if ($SESSION->gradeuserreport->sortitemid == $this->sortitemid) {
                    // same as last sort
                    if ($SESSION->gradeuserreport->sort == 'ASC') {
                        $this->sortorder = $SESSION->gradeuserreport->sort = 'DESC';
                    } else {
                        $this->sortorder = $SESSION->gradeuserreport->sort = 'ASC';
                    }
                } else {
                    if ($this->sortitemid == 'firstname' || $this->sortitemid == 'lastname') {
                        $this->sortorder = $SESSION->gradeuserreport->sort = 'ASC';
                    } else {
                        $this->sortorder = $SESSION->gradeuserreport->sort = 'DESC';
                    }
                }
            }
            $SESSION->gradeuserreport->sortitemid = $this->sortitemid;
        } else {
            // not requesting sort, use last setting (for paging)

            if (isset($SESSION->gradeuserreport->sortitemid)) {
                $this->sortitemid = $SESSION->gradeuserreport->sortitemid;
            }else{
                $this->sortitemid = 'lastname';
            }

            if (isset($SESSION->gradeuserreport->sort)) {
                $this->sortorder = $SESSION->gradeuserreport->sort;
            } else {
                $this->sortorder = 'ASC';
            }
        }
    }

    /**
     * pulls out the userids of the users to be display, and sorts them
     */
    public function load_users() {
        global $CFG, $DB;

        list($usql, $gbrparams) = $DB->get_in_or_equal(explode(',', $this->gradebookroles), SQL_PARAMS_NAMED, 'grbr0');

        if (is_numeric($this->sortitemid)) {
            $params = array_merge(array('gitemid'=>$this->sortitemid), $gbrparams, $this->groupwheresql_params);
            // the MAX() magic is required in order to please PG
            $sort = "MAX(g.finalgrade) $this->sortorder";

            $sql = "SELECT u.id, u.firstname, u.lastname, u.imagealt, u.picture, u.idnumber
                      FROM {user} u
                           JOIN {role_assignments} ra ON ra.userid = u.id
                           $this->groupsql
                           LEFT JOIN {grade_grades} g ON (g.userid = u.id AND g.itemid = :gitemid)
                     WHERE ra.roleid $usql AND u.deleted = 0
                           $this->groupwheresql
                           AND ra.contextid ".get_related_contexts_string($this->context)."
                  GROUP BY u.id, u.firstname, u.lastname, u.imagealt, u.picture, u.idnumber
                  ORDER BY $sort";

        } else {
            switch($this->sortitemid) {
                case 'lastname':
                    $sort = "u.lastname $this->sortorder, u.firstname $this->sortorder"; break;
                case 'firstname':
                    $sort = "u.firstname $this->sortorder, u.lastname $this->sortorder"; break;
                case 'idnumber':
                default:
                    $sort = "u.idnumber $this->sortorder"; break;
            }

            $params = array_merge($gbrparams, $this->groupwheresql_params);
            $sql = "SELECT DISTINCT u.id, u.firstname, u.lastname, u.imagealt, u.picture, u.idnumber
                      FROM {user} u
                           JOIN {role_assignments} ra ON u.id = ra.userid
                           $this->groupsql
                     WHERE ra.roleid $usql AND u.deleted = 0
                           $this->groupwheresql
                           AND ra.contextid ".get_related_contexts_string($this->context)."
                  ORDER BY $sort";
        }


        $this->users = $DB->get_records_sql($sql, $params, $this->get_pref('studentsperpage') * $this->page, $this->get_pref('studentsperpage'));

        if (empty($this->users)) {
            $this->userselect = '';
            $this->users = array();
            $this->userselect_params = array();
        } else {
            list($usql, $params) = $DB->get_in_or_equal(array_keys($this->users), SQL_PARAMS_NAMED, 'usid0');
            $this->userselect = "AND g.userid $usql";
            $this->userselect_params = $params;
        }

        return $this->users;
    }

    /**
     * we supply the userids in this query, and get all the grades
     * pulls out all the grades, this does not need to worry about paging
     */
    public function load_final_grades() {
        global $CFG, $DB;

        // please note that we must fetch all grade_grades fields if we want to contruct grade_grade object from it!
        $params = array_merge(array('courseid'=>$this->courseid), $this->userselect_params);
        $sql = "SELECT g.*
                  FROM {grade_items} gi,
                       {grade_grades} g
                 WHERE g.itemid = gi.id AND gi.courseid = :courseid {$this->userselect}";

        $userids = array_keys($this->users);


        if ($grades = $DB->get_records_sql($sql, $params)) {
            foreach ($grades as $graderec) {
                if (in_array($graderec->userid, $userids) and array_key_exists($graderec->itemid, $this->gtree->get_items())) { // some items may not be present!!
                    $this->grades[$graderec->userid][$graderec->itemid] = new grade_grade($graderec, false);
                    $this->grades[$graderec->userid][$graderec->itemid]->grade_item =& $this->gtree->get_item($graderec->itemid); // db caching
                }
            }
        }

        // prefil grades that do not exist yet
        foreach ($userids as $userid) {
            foreach ($this->gtree->get_items() as $itemid=>$unused) {
                if (!isset($this->grades[$userid][$itemid])) {
                    $this->grades[$userid][$itemid] = new grade_grade();
                    $this->grades[$userid][$itemid]->itemid = $itemid;
                    $this->grades[$userid][$itemid]->userid = $userid;
                    $this->grades[$userid][$itemid]->grade_item =& $this->gtree->get_item($itemid); // db caching
                }
            }
        }
    }

    /**
     * Builds and returns a div with on/off toggles.
     * @return string HTML code
     */
    public function get_toggles_html() {
        global $CFG, $USER, $COURSE, $OUTPUT;

        $html = '';
        if ($USER->gradeediting[$this->courseid]) {
            if (has_capability('moodle/grade:manage', $this->context) or has_capability('moodle/grade:hide', $this->context)) {
                $html .= $this->print_toggle('eyecons');
            }
            if (has_capability('moodle/grade:manage', $this->context)
             or has_capability('moodle/grade:lock', $this->context)
             or has_capability('moodle/grade:unlock', $this->context)) {
                $html .= $this->print_toggle('locks');
            }
            if (has_capability('moodle/grade:manage', $this->context)) {
                $html .= $this->print_toggle('quickfeedback');
            }

            if (has_capability('moodle/grade:manage', $this->context)) {
                $html .= $this->print_toggle('calculations');
            }
        }

        if ($this->canviewhidden) {
            $html .= $this->print_toggle('averages');
        }

        $html .= $this->print_toggle('ranges');
        if (!empty($CFG->enableoutcomes)) {
            $html .= $this->print_toggle('nooutcomes');
        }

        return $OUTPUT->container($html, 'grade-report-toggles');
    }

    /**
    * Shortcut function for printing the grader report toggles.
    * @param string $type The type of toggle
    * @param bool $return Whether to return the HTML string rather than printing it
    * @return void
    */
    public function print_toggle($type) {
        global $CFG, $OUTPUT;

        $icons = array('eyecons' => 't/hide',
                       'calculations' => 't/calc',
                       'locks' => 't/lock',
                       'averages' => 't/mean',
                       'quickfeedback' => 't/feedback',
                       'nooutcomes' => 't/outcomes');

        $prefname = 'grade_report_show' . $type;

        if (array_key_exists($prefname, $CFG)) {
            $showpref = get_user_preferences($prefname, $CFG->$prefname);
        } else {
            $showpref = get_user_preferences($prefname);
        }

        $strshow = $this->get_lang_string('show' . $type, 'grades');
        $strhide = $this->get_lang_string('hide' . $type, 'grades');

        $showhide = 'show';
        $toggleaction = 1;

        if ($showpref) {
            $showhide = 'hide';
            $toggleaction = 0;
        }

        if (array_key_exists($type, $icons)) {
            $imagename = $icons[$type];
        } else {
            $imagename = "t/$type.gif";
        }

        $string = ${'str' . $showhide};

        $aurl = clone($this->baseurl);
        $url->params(array('toggle' => $toggleaction, 'toggle_type' => $type));

        $retval = $OUTPUT->container($OUTPUT->action_icon($url, $string, $imagename, array('class'=>'iconsmall'))); // TODO: this container looks wrong here

        return $retval;
    }

    /**
     * Builds and returns the rows that will make up the left part of the grader report
     * This consists of student names and icons, links to user reports and id numbers, as well
     * as header cells for these columns. It also includes the fillers required for the
     * categories displayed on the right side of the report.
     * @return array Array of html_table_row objects
     */
    public function get_left_rows() {
        global $CFG, $USER, $OUTPUT;

        $rows = array();

        $showuserimage = $this->get_pref('showuserimage');
        $showuseridnumber = $this->get_pref('showuseridnumber');
        $fixedstudents = $this->is_fixed_students();

        $strfeedback  = $this->get_lang_string("feedback");
        $strgrade     = $this->get_lang_string('grade');

        $arrows = $this->get_sort_arrows();

        $colspan = 1;

        if (has_capability('gradereport/'.$CFG->grade_profilereport.':view', $this->context)) {
            $colspan++;
        }

        if ($showuseridnumber) {
            $colspan++;
        }

        $levels = count($this->gtree->levels) - 1;

        for ($i = 0; $i < $levels; $i++) {
            $fillercell = new html_table_cell();
            $fillercell->add_classes(array('fixedcolumn', 'cell', 'c0', 'topleft'));
            $fillercell->text = ' ';
            $fillercell->colspan = $colspan;
            $row = html_table_row::make(array($fillercell));
            $rows[] = $row;
        }

        $headerrow = new html_table_row();
        $headerrow->add_class('heading');

        $studentheader = new html_table_cell();
        $studentheader->add_classes(array('header', 'c0'));
        $studentheader->scope = 'col';
        $studentheader->header = true;
        $studentheader->id = 'studentheader';
        if (has_capability('gradereport/'.$CFG->grade_profilereport.':view', $this->context)) {
            $studentheader->colspan = 2;
        }
        $studentheader->text = $arrows['studentname'];

        $headerrow->cells[] = $studentheader;

        if ($showuseridnumber) {
            $sortidnumberlink = html_link::make(clone($this->baseurl), get_string('idnumber'));
            $sortidnumberlink->url->param('sortitemid', 'idnumber');

            $idnumberheader = new html_table_cell();
            $idnumberheader->add_classes(array('header', 'c0', 'useridnumber'));
            $idnumberheader->scope = 'col';
            $idnumberheader->header = true;
            $idnumberheader->text = $arrows['idnumber'];

            $headerrow->cells[] = $idnumberheader;
        }

        $rows[] = $headerrow;

        $rows = $this->get_left_icons_row($rows, $colspan);

        $rowclasses = array('even', 'odd');

        foreach ($this->users as $userid => $user) {
            $userrow = new html_table_row();
            $userrow->add_classes(array('r'.$this->rowcount++, $rowclasses[$this->rowcount % 2]));

            $usercell = new html_table_cell();
            $usercell->add_classes(array('c0', 'user'));
            $usercell->header = true;
            $usercell->scope = 'row';
            $usercell->add_action('click', 'yui_set_row');

            if ($showuserimage) {
                $usercell->text = $OUTPUT->container($OUTPUT->user_picture($user), 'userpic');
            }

            $usercell->text .= $OUTPUT->link(html_link::make(new moodle_url($CFG->wwwroot.'/user/view.php', array('id' => $user->id, 'course' => $this->course->id)), fullname($user)));

            $userrow->cells[] = $usercell;

            if (has_capability('gradereport/'.$CFG->grade_profilereport.':view', $this->context)) {
                $userreportcell = new html_table_cell();
                $userreportcell->add_class('userreport');
                $userreportcell->header = true;
                $a->user = fullname($user);
                $strgradesforuser = get_string('gradesforuser', 'grades', $a);
                $url = new moodle_url($CFG->wwwroot.'/grade/report/'.$CFG->grade_profilereport.'/index.php', array('userid' => $user->id, 'id' => $this->course->id));
                $userreportcell->text = $OUTPUT->action_icon($url, $strgradesforuser, 't/grades', array('class'=>'iconsmall'));
                $userrow->cells[] = $userreportcell;
            }

            if ($showuseridnumber) {
                $idnumbercell = new html_table_cell();
                $idnumbercell->add_classes(array('header', 'c0', 'useridnumber'));
                $idnumbercell->header = true;
                $idnumbercell->scope = 'row';
                $idnumbercell->add_action('click', 'yui_set_row');
                $userrow->cells[] = $idnumbercell;
            }

            $rows[] = $userrow;
        }

        $rows = $this->get_left_range_row($rows, $colspan);
        $rows = $this->get_left_avg_row($rows, $colspan, true);
        $rows = $this->get_left_avg_row($rows, $colspan);

        return $rows;
    }

    /**
     * Builds and returns the rows that will make up the right part of the grader report
     * @return array Array of html_table_row objects
     */
    public function get_right_rows() {
        global $CFG, $USER, $OUTPUT, $DB;

        $rows = array();
        $this->rowcount = 0;
        $numrows = count($this->gtree->get_levels());
        $numusers = count($this->users);
        $gradetabindex = 1;
        $columnstounset = array();
        $strgrade = $this->get_lang_string('grade');
        $arrows = $this->get_sort_arrows();

        foreach ($this->gtree->get_levels() as $key=>$row) {
            $columncount = 0;
            if ($key == 0) {
                // do not display course grade category
                // continue;
            }

            $headingrow = new html_table_row();
            $headingrow->add_class('heading_name_row');

            foreach ($row as $columnkey => $element) {
                $sortlink = clone($this->baseurl);
                if (isset($element['object']->id)) {
                    $sortlink->param('sortitemid', $element['object']->id);
                }

                $eid    = $element['eid'];
                $object = $element['object'];
                $type   = $element['type'];
                $categorystate = @$element['categorystate'];
                $itemmodule = null;
                $iteminstance = null;

                $columnclass = 'c' . $columncount++;
                if (!empty($element['colspan'])) {
                    $colspan = $element['colspan'];
                    $columnclass = '';
                } else {
                    $colspan = 1;
                }

                if (!empty($element['depth'])) {
                    $catlevel = 'catlevel'.$element['depth'];
                } else {
                    $catlevel = '';
                }

// Element is a filler
                if ($type == 'filler' or $type == 'fillerfirst' or $type == 'fillerlast') {
                    $fillercell = new html_table_cell();
                    $fillercell->add_classes(array($columnclass, $type, $catlevel));
                    $fillercell->colspan = $colspan;
                    $fillercell->text = '&nbsp;';
                    $fillercell->header = true;
                    $fillercell->scope = 'col';
                    $headingrow->cells[] = $fillercell;
                }
// Element is a category
                else if ($type == 'category') {
                    $categorycell = new html_table_cell();
                    $categorycell->add_classes(array($columnclass, 'category', $catlevel));
                    $categorycell->colspan = $colspan;
                    $categorycell->text = shorten_text($element['object']->get_name());
                    $categorycell->text .= $this->get_collapsing_icon($element);
                    $categorycell->header = true;
                    $categorycell->scope = 'col';

                    // Print icons
                    if ($USER->gradeediting[$this->courseid]) {
                        $categorycell->text .= $this->get_icons($element);
                    }

                    $headingrow->cells[] = $categorycell;
                }
// Element is a grade_item
                else {
                    $itemmodule = $element['object']->itemmodule;
                    $iteminstance = $element['object']->iteminstance;

                    if ($element['object']->id == $this->sortitemid) {
                        if ($this->sortorder == 'ASC') {
                            $arrow = $this->get_sort_arrow('up', $sortlink);
                        } else {
                            $arrow = $this->get_sort_arrow('down', $sortlink);
                        }
                    } else {
                        $arrow = $this->get_sort_arrow('move', $sortlink);
                    }

                    $headerlink = $this->gtree->get_element_header($element, true, $this->get_pref('showactivityicons'), false);

                    $itemcell = new html_table_cell();
                    $itemcell->add_classes(array($columnclass, $type, $catlevel));

                    if ($element['object']->is_hidden()) {
                        $itemcell->add_class('hidden');
                    }

                    $itemcell->colspan = $colspan;
                    $itemcell->text = shorten_text($headerlink) . $arrow;
                    $itemcell->header = true;
                    $itemcell->scope = 'col';
                    $itemcell->onclick = 'set_col(this.cellIndex);';

                    $headingrow->cells[] = $itemcell;
                }

            }
            $rows[] = $headingrow;
        }

        $rows = $this->get_right_icons_row($rows);

        // Preload scale objects for items with a scaleid
        $scaleslist = array();
        $tabindices = array();

        foreach ($this->gtree->get_items() as $item) {
            if (!empty($item->scaleid)) {
                $scaleslist[] = $item->scaleid;
            }

            $tabindices[$item->id]['grade'] = $gradetabindex;
            $tabindices[$item->id]['feedback'] = $gradetabindex + $numusers;
            $gradetabindex += $numusers * 2;
        }
        $scalesarray = array();

        if (!empty($scaleslist)) {
            $scalesarray = $DB->get_records_list('scale', 'id', $scaleslist);
        }

        $rowclasses = array('even', 'odd');

        foreach ($this->users as $userid => $user) {

            if ($this->canviewhidden) {
                $altered = array();
                $unknown = array();
            } else {
                $hidingaffected = grade_grade::get_hiding_affected($this->grades[$userid], $this->gtree->get_items());
                $altered = $hidingaffected['altered'];
                $unknown = $hidingaffected['unknown'];
                unset($hidingaffected);
            }

            $columncount = 0;

            $itemrow = new html_table_row();
            $itemrow->add_class($rowclasses[$this->rowcount % 2]);

            foreach ($this->gtree->items as $itemid=>$unused) {
                $item =& $this->gtree->items[$itemid];
                $grade = $this->grades[$userid][$item->id];

                $itemcell = new html_table_cell();

                // Get the decimal points preference for this item
                $decimalpoints = $item->get_decimals();

                if (in_array($itemid, $unknown)) {
                    $gradeval = null;
                } else if (array_key_exists($itemid, $altered)) {
                    $gradeval = $altered[$itemid];
                } else {
                    $gradeval = $grade->finalgrade;
                }

                // MDL-11274
                // Hide grades in the grader report if the current grader doesn't have 'moodle/grade:viewhidden'
                if (!$this->canviewhidden and $grade->is_hidden()) {
                    if (!empty($CFG->grade_hiddenasdate) and $grade->get_datesubmitted() and !$item->is_category_item() and !$item->is_course_item()) {
                        // the problem here is that we do not have the time when grade value was modified, 'timemodified' is general modification date for grade_grades records
                        $itemcell->text = $OUTPUT->span(userdate($grade->get_datesubmitted(),get_string('strftimedatetimeshort')), 'datesubmitted');
                    } else {
                        $itemcell->text = '-';
                    }
                    $itemrow->cells[] = $itemcell;
                    continue;
                }

                // emulate grade element
                $eid = $this->gtree->get_grade_eid($grade);
                $element = array('eid'=>$eid, 'object'=>$grade, 'type'=>'grade');

                $itemcell->add_class('grade');
                if ($item->is_category_item()) {
                    $itemcell->add_class('cat');
                }
                if ($item->is_course_item()) {
                    $itemcell->add_class('course');
                }
                if ($grade->is_overridden()) {
                    $itemcell->add_class('overridden');
                }

                if ($grade->is_excluded()) {
                    // $itemcell->add_class('excluded');
                }

                $gradetitle = $OUTPUT->container(fullname($user), 'fullname');
                $gradetitle .= $OUTPUT->container($item->get_name(true), 'itemname');

                if (!empty($grade->feedback) && !$USER->gradeediting[$this->courseid]) {
                    $gradetitle .= $OUTPUT->container(wordwrap(trim(format_string($grade->feedback, $grade->feedbackformat)), 34, '<br/ >'), 'feedback');
                }

                $itemcell->title = s($gradetitle);

                if ($grade->is_excluded()) {
                    $itemcell->text .= $OUTPUT->span(get_string('excluded', 'grades'), 'excludedfloater');
                }

                // Do not show any icons if no grade (no record in DB to match)
                if (!$item->needsupdate and $USER->gradeediting[$this->courseid]) {
                    $itemcell->text .= $this->get_icons($element);
                }

                $hidden = '';
                if ($grade->is_hidden()) {
                    $hidden = ' hidden ';
                }

                $gradepass = ' gradefail ';
                if ($grade->is_passed($item)) {
                    $gradepass = ' gradepass ';
                } elseif (is_null($grade->is_passed($item))) {
                    $gradepass = '';
                }

                // if in editting mode, we need to print either a text box
                // or a drop down (for scales)
                // grades in item of type grade category or course are not directly editable
                if ($item->needsupdate) {
                    $itemcell->text .= $OUTPUT->span(get_string('error'), "gradingerror$hidden");

                } else if ($USER->gradeediting[$this->courseid]) {

                    if ($item->scaleid && !empty($scalesarray[$item->scaleid])) {
                        $scale = $scalesarray[$item->scaleid];
                        $gradeval = (int)$gradeval; // scales use only integers
                        $scales = explode(",", $scale->scale);
                        // reindex because scale is off 1

                        // MDL-12104 some previous scales might have taken up part of the array
                        // so this needs to be reset
                        $scaleopt = array();
                        $i = 0;
                        foreach ($scales as $scaleoption) {
                            $i++;
                            $scaleopt[$i] = $scaleoption;
                        }

                        if ($this->get_pref('quickgrading') and $grade->is_editable()) {
                            $oldval = empty($gradeval) ? -1 : $gradeval;
                            if (empty($item->outcomeid)) {
                                $nogradestr = $this->get_lang_string('nograde');
                            } else {
                                $nogradestr = $this->get_lang_string('nooutcome', 'grades');
                            }
                            $itemcell->text .= '<input type="hidden" name="oldgrade_'.$userid.'_'
                                          .$item->id.'" value="'.$oldval.'"/>';
                            $select = html_select::make($scaleopt, 'grade_'.$userid.'_'.$item->id,$gradeval, $nogradestr);
                            $select->nothingvalue = '-1';
                            $select->tabindex = $tabindices[$item->id]['grade'];
                            $itemcell->text .= $OUTPUT->select($select);
                        } elseif(!empty($scale)) {
                            $scales = explode(",", $scale->scale);

                            // invalid grade if gradeval < 1
                            if ($gradeval < 1) {
                                $itemcell->text .= $OUTPUT->span('-', "gradevalue$hidden$gradepass");
                            } else {
                                $gradeval = $grade->grade_item->bounded_grade($gradeval); //just in case somebody changes scale
                                $itemcell->text .= $OUTPUT->span($scales[$gradeval-1], "gradevalue$hidden$gradepass");
                            }
                        } else {
                            // no such scale, throw error?
                        }

                    } else if ($item->gradetype != GRADE_TYPE_TEXT) { // Value type
                        if ($this->get_pref('quickgrading') and $grade->is_editable()) {
                            $value = format_float($gradeval, $decimalpoints);
                            $itemcell->text .= '<input type="hidden" name="oldgrade_'.$userid.'_'.$item->id.'" value="'.$value.'" />';
                            $itemcell->text .= '<input size="6" tabindex="' . $tabindices[$item->id]['grade']
                                          . '" type="text" class="text" title="'. $strgrade .'" name="grade_'
                                          .$userid.'_' .$item->id.'" value="'.$value.'" />';
                        } else {
                            $itemcell->text .= $OUTPUT->span(format_float($gradeval, $decimalpoints), "gradevalue$hidden$gradepass");
                        }
                    }


                    // If quickfeedback is on, print an input element
                    if ($this->get_pref('showquickfeedback') and $grade->is_editable()) {

                        $itemcell->text .= '<input type="hidden" name="oldfeedback_'
                                      .$userid.'_'.$item->id.'" value="' . s($grade->feedback) . '" />';
                        $itemcell->text .= '<input class="quickfeedback" tabindex="' . $tabindices[$item->id]['feedback']
                                      . '" size="6" title="' . $strfeedback . '" type="text" name="feedback_'
                                      .$userid.'_'.$item->id.'" value="' . s($grade->feedback) . '" />';
                    }

                } else { // Not editing
                    $gradedisplaytype = $item->get_displaytype();

                    // If feedback present, surround grade with feedback tooltip: Open span here

                    if ($item->needsupdate) {
                        $itemcell->text .= $OUTPUT->span(get_string('error'), "gradingerror$hidden$gradepass");
                    } else {
                        $itemcell->text .= $OUTPUT->span(grade_format_gradevalue($gradeval, $item, true, $gradedisplaytype, null), "gradevalue$hidden$gradepass");
                    }
                }

                if (!empty($this->gradeserror[$item->id][$userid])) {
                    $itemcell->text .= $this->gradeserror[$item->id][$userid];
                }

                $itemrow->cells[] = $itemcell;
            }
            $rows[] = $itemrow;
        }

        $rows = $this->get_right_range_row($rows);
        $rows = $this->get_right_avg_row($rows, true);
        $rows = $this->get_right_avg_row($rows);

        return $rows;
    }

    /**
     * Depending on the style of report (fixedstudents vs traditional one-table),
     * arranges the rows of data in one or two tables, and returns the output of
     * these tables in HTML
     * @return string HTML
     */
    public function get_grade_table() {
        global $OUTPUT;
        $fixedstudents = $this->is_fixed_students();

        $leftrows = $this->get_left_rows();
        $rightrows = $this->get_right_rows();

        $html = '';

        if ($fixedstudents) {
            $fixedcolumntable = new html_table();
            $fixedcolumntable->id = 'fixed_column';
            $fixedcolumntable->add_class('fixed_grades_column');
            $fixedcolumntable->bodyclasses = 'leftbody';
            $fixedcolumntable->data = $leftrows;
            $html .= $OUTPUT->container($OUTPUT->table($fixedcolumntable), 'left_scroller');

            $righttable = new html_table();
            $righttable->id = 'user-grades';
            $righttable->bodyclasses = array('righttest');
            $righttable->data = $rightrows;

            $html .= $OUTPUT->container($OUTPUT->table($righttable), 'right_scroller');
        } else {
            $fulltable = new html_table();
            $fulltable->add_classes(array('gradestable', 'flexible', 'boxaligncenter', 'generaltable'));
            $fulltable->id = 'user-grades';

            // Extract rows from each side (left and right) and collate them into one row each
            foreach ($leftrows as $key => $row) {
                $row->cells = array_merge($row->cells, $rightrows[$key]->cells);
                $fulltable->data[] = $row;
            }
            $html .= $OUTPUT->table($fulltable);
        }
        return $OUTPUT->container($html, 'gradeparent');
    }

    /**
     * Builds and return the row of icons for the left side of the report.
     * It only has one cell that says "Controls"
     * @param array $rows The Array of rows for the left part of the report
     * @param int $colspan The number of columns this cell has to span
     * @return array Array of rows for the left part of the report
     */
    public function get_left_icons_row($rows=array(), $colspan=1) {
        global $USER;

        if ($USER->gradeediting[$this->courseid]) {
            $controlsrow = new html_table_row();
            $controlsrow->add_class('controls');
            $controlscell = new html_table_cell();
            $controlscell->add_classes(array('header', 'c0', 'controls'));
            $controlscell->colspan = $colspan;
            $controlscell->text = $this->get_lang_string('controls','grades');

            $controlsrow->cells[] = $controlscell;
            $rows[] = $controlsrow;
        }
        return $rows;
    }

    /**
     * Builds and return the header for the row of ranges, for the left part of the grader report.
     * @param array $rows The Array of rows for the left part of the report
     * @param int $colspan The number of columns this cell has to span
     * @return array Array of rows for the left part of the report
     */
    public function get_left_range_row($rows=array(), $colspan=1) {
        global $CFG, $USER;

        if ($this->get_pref('showranges')) {
            $rangerow = new html_table_row();
            $rangerow->add_classes(array('range', 'r'.$this->rowcount++));
            $rangecell = new html_table_cell();
            $rangecell->add_classes(array('header', 'c0', 'range'));
            $rangecell->colspan = $colspan;
            $rangecell->header = true;
            $rangecell->scope = 'row';
            $rangecell->text = $this->get_lang_string('range','grades');
            $rangerow->cells[] = $rangecell;
            $rows[] = $rangerow;
        }

        return $rows;
    }

    /**
     * Builds and return the headers for the rows of averages, for the left part of the grader report.
     * @param array $rows The Array of rows for the left part of the report
     * @param int $colspan The number of columns this cell has to span
     * @param bool $groupavg If true, returns the row for group averages, otherwise for overall averages
     * @return array Array of rows for the left part of the report
     */
    public function get_left_avg_row($rows=array(), $colspan=1, $groupavg=false) {
        if (!$this->canviewhidden) {
            // totals might be affected by hiding, if user can not see hidden grades the aggregations might be altered
            // better not show them at all if user can not see all hideen grades
            return $rows;
        }

        $showaverages = $this->get_pref('showaverages');
        $showaveragesgroup = $this->currentgroup && $showaverages;
        $straveragegroup = get_string('groupavg', 'grades');

        if ($groupavg) {
            if ($showaveragesgroup) {
                $groupavgrow = new html_table_row();
                $groupavgrow->add_classes(array('groupavg', 'r'.$this->rowcount++));
                $groupavgcell = new html_table_cell();
                $groupavgcell->add_classes(array('header', 'c0', 'range'));
                $groupavgcell->colspan = $colspan;
                $groupavgcell->header = true;
                $groupavgcell->scope = 'row';
                $groupavgcell->text = $straveragegroup;
                $groupavgrow->cells[] = $groupavgcell;
                $rows[] = $groupavgrow;
            }
        } else {
            $straverage = get_string('overallaverage', 'grades');

            if ($showaverages) {
                $avgrow = new html_table_row();
                $avgrow->add_classes(array('avg', 'r'.$this->rowcount++));
                $avgcell = new html_table_cell();
                $avgcell->add_classes(array('header', 'c0', 'range'));
                $avgcell->colspan = $colspan;
                $avgcell->header = true;
                $avgcell->scope = 'row';
                $avgcell->text = $straverage;
                $avgrow->cells[] = $avgcell;
                $rows[] = $avgrow;
            }
        }

        return $rows;
    }

    /**
     * Builds and return the row of icons when editing is on, for the right part of the grader report.
     * @param array $rows The Array of rows for the right part of the report
     * @return array Array of rows for the right part of the report
     */
    public function get_right_icons_row($rows=array()) {
        global $USER;
        if ($USER->gradeediting[$this->courseid]) {
            $iconsrow = new html_table_row();
            $iconsrow->add_class('controls');

            $showuseridnumber = $this->get_pref('showuseridnumber');

            $columncount = 0;
            foreach ($this->gtree->items as $itemid=>$unused) {
                // emulate grade element
                $item =& $this->gtree->get_item($itemid);

                $eid = $this->gtree->get_item_eid($item);
                $element = $this->gtree->locate_element($eid);
                $itemcell = new html_table_cell();
                $itemcell->add_classes(array('controls', 'icons'));
                $itemcell->text = $this->get_icons($element);
                $iconsrow->cells[] = $itemcell;
            }
            $rows[] = $iconsrow;
        }
        return $rows;
    }

    /**
     * Builds and return the row of ranges for the right part of the grader report.
     * @param array $rows The Array of rows for the right part of the report
     * @return array Array of rows for the right part of the report
     */
    public function get_right_range_row($rows=array()) {
        global $OUTPUT;

        if ($this->get_pref('showranges')) {
            $rangesdisplaytype   = $this->get_pref('rangesdisplaytype');
            $rangesdecimalpoints = $this->get_pref('rangesdecimalpoints');
            $rangerow = new html_table_row();
            $rangerow->add_classes(array('heading', 'range'));

            foreach ($this->gtree->items as $itemid=>$unused) {
                $item =& $this->gtree->items[$itemid];
                $itemcell = new html_table_cell();
                $itemcell->header = true;
                $itemcell->add_classes(array('header', 'range'));

                $hidden = '';
                if ($item->is_hidden()) {
                    $hidden = ' hidden ';
                }

                $formattedrange = $item->get_formatted_range($rangesdisplaytype, $rangesdecimalpoints);

                $itemcell->text = $OUTPUT->container($formattedrange, 'rangevalues'.$hidden);
                $rangerow->cells[] = $itemcell;
            }
            $rows[] = $rangerow;
        }
        return $rows;
    }

    /**
     * Builds and return the row of averages for the right part of the grader report.
     * @param array $rows Whether to return only group averages or all averages.
     * @param bool $grouponly Whether to return only group averages or all averages.
     * @return array Array of rows for the right part of the report
     */
    public function get_right_avg_row($rows=array(), $grouponly=false) {
        global $CFG, $USER, $DB, $OUTPUT;

        if (!$this->canviewhidden) {
            // totals might be affected by hiding, if user can not see hidden grades the aggregations might be altered
            // better not show them at all if user can not see all hideen grades
            return $rows;
        }

        $showaverages = $this->get_pref('showaverages');
        $showaveragesgroup = $this->currentgroup && $showaverages;

        $averagesdisplaytype   = $this->get_pref('averagesdisplaytype');
        $averagesdecimalpoints = $this->get_pref('averagesdecimalpoints');
        $meanselection         = $this->get_pref('meanselection');
        $shownumberofgrades    = $this->get_pref('shownumberofgrades');

        $avghtml = '';
        $avgcssclass = 'avg';

        if ($grouponly) {
            $straverage = get_string('groupavg', 'grades');
            $showaverages = $this->currentgroup && $this->get_pref('showaverages');
            $groupsql = $this->groupsql;
            $groupwheresql = $this->groupwheresql;
            $groupwheresqlparams = $this->groupwheresql_params;
            $avgcssclass = 'groupavg';
        } else {
            $straverage = get_string('overallaverage', 'grades');
            $showaverages = $this->get_pref('showaverages');
            $groupsql = "";
            $groupwheresql = "";
            $groupwheresqlparams = array();
        }

        if ($shownumberofgrades) {
            $straverage .= ' (' . get_string('submissions', 'grades') . ') ';
        }

        $totalcount = $this->get_numusers($grouponly);

        list($usql, $rolesparams) = $DB->get_in_or_equal(explode(',', $this->gradebookroles), SQL_PARAMS_NAMED, 'grbr0');

        if ($showaverages) {
            $params = array_merge(array('courseid'=>$this->courseid), $rolesparams, $groupwheresqlparams);

            // find sums of all grade items in course
            $SQL = "SELECT g.itemid, SUM(g.finalgrade) AS sum
                      FROM {grade_items} gi
                           JOIN {grade_grades} g      ON g.itemid = gi.id
                           JOIN {user} u              ON u.id = g.userid
                           JOIN {role_assignments} ra ON ra.userid = u.id
                           $groupsql
                     WHERE gi.courseid = :courseid
                           AND ra.roleid $usql
                           AND ra.contextid ".get_related_contexts_string($this->context)."
                           AND g.finalgrade IS NOT NULL
                           $groupwheresql
                  GROUP BY g.itemid";
            $sumarray = array();
            if ($sums = $DB->get_records_sql($SQL, $params)) {
                foreach ($sums as $itemid => $csum) {
                    $sumarray[$itemid] = $csum->sum;
                }
            }

            // MDL-10875 Empty grades must be evaluated as grademin, NOT always 0
            // This query returns a count of ungraded grades (NULL finalgrade OR no matching record in grade_grades table)
            $params = array_merge(array('courseid'=>$this->courseid), $rolesparams, $groupwheresqlparams);
            $SQL = "SELECT gi.id, COUNT(u.id) AS count
                      FROM {grade_items} gi
                           CROSS JOIN {user} u
                           JOIN {role_assignments} ra        ON ra.userid = u.id
                           LEFT OUTER JOIN  {grade_grades} g ON (g.itemid = gi.id AND g.userid = u.id AND g.finalgrade IS NOT NULL)
                           $groupsql
                     WHERE gi.courseid = :courseid
                           AND ra.roleid $usql
                           AND ra.contextid ".get_related_contexts_string($this->context)."
                           AND g.id IS NULL
                           $groupwheresql
                  GROUP BY gi.id";

            $ungradedcounts = $DB->get_records_sql($SQL, $params);

            $avgrow = new html_table_row();
            $avgrow->add_class('avg');

            foreach ($this->gtree->items as $itemid=>$unused) {
                $item =& $this->gtree->items[$itemid];

                if ($item->needsupdate) {
                    $avgcell = new html_table_cell();
                    $avgcell->text = $OUTPUT->container(get_string('error'), 'gradingerror');
                    $avgrow->cells[] = $avgcell;
                    continue;
                }

                if (!isset($sumarray[$item->id])) {
                    $sumarray[$item->id] = 0;
                }

                if (empty($ungradedcounts[$itemid])) {
                    $ungradedcount = 0;
                } else {
                    $ungradedcount = $ungradedcounts[$itemid]->count;
                }

                if ($meanselection == GRADE_REPORT_MEAN_GRADED) {
                    $meancount = $totalcount - $ungradedcount;
                } else { // Bump up the sum by the number of ungraded items * grademin
                    $sumarray[$item->id] += $ungradedcount * $item->grademin;
                    $meancount = $totalcount;
                }

                $decimalpoints = $item->get_decimals();

                // Determine which display type to use for this average
                if ($USER->gradeediting[$this->courseid]) {
                    $displaytype = GRADE_DISPLAY_TYPE_REAL;

                } else if ($averagesdisplaytype == GRADE_REPORT_PREFERENCE_INHERIT) { // no ==0 here, please resave the report and user preferences
                    $displaytype = $item->get_displaytype();

                } else {
                    $displaytype = $averagesdisplaytype;
                }

                // Override grade_item setting if a display preference (not inherit) was set for the averages
                if ($averagesdecimalpoints == GRADE_REPORT_PREFERENCE_INHERIT) {
                    $decimalpoints = $item->get_decimals();

                } else {
                    $decimalpoints = $averagesdecimalpoints;
                }

                if (!isset($sumarray[$item->id]) || $meancount == 0) {
                    $avgcell = new html_table_cell();
                    $avgcell->text = '-';
                    $avgrow->cells[] = $avgcell;

                } else {
                    $sum = $sumarray[$item->id];
                    $avgradeval = $sum/$meancount;
                    $gradehtml = grade_format_gradevalue($avgradeval, $item, true, $displaytype, $decimalpoints);

                    $numberofgrades = '';
                    if ($shownumberofgrades) {
                        $numberofgrades = " ($meancount)";
                    }

                    $avgcell = new html_table_cell();
                    $avgcell->text = $gradehtml.$numberofgrades;
                    $avgrow->cells[] = $avgcell;
                }
            }
            $rows[] = $avgrow;
        }
        return $rows;
    }

    /**
     * Given a grade_category, grade_item or grade_grade, this function
     * figures out the state of the object and builds then returns a div
     * with the icons needed for the grader report.
     *
     * @param object $object
     * @return string HTML
     */
    protected function get_icons($element) {
        global $CFG, $USER, $OUTPUT;

        if (!$USER->gradeediting[$this->courseid]) {
            return '<div class="grade_icons" />';
        }

        // Init all icons
        $editicon = '';

        if ($element['type'] != 'categoryitem' && $element['type'] != 'courseitem') {
            $editicon = $this->gtree->get_edit_icon($element, $this->gpr);
        }

        $editcalculationicon = '';
        $showhideicon        = '';
        $lockunlockicon      = '';

        if (has_capability('moodle/grade:manage', $this->context)) {

            if ($this->get_pref('showcalculations')) {
                $editcalculationicon = $this->gtree->get_calculation_icon($element, $this->gpr);
            }

            if ($this->get_pref('showeyecons')) {
               $showhideicon = $this->gtree->get_hiding_icon($element, $this->gpr);
            }

            if ($this->get_pref('showlocks')) {
                $lockunlockicon = $this->gtree->get_locking_icon($element, $this->gpr);
            }
        }

        return $OUTPUT->container($editicon.$editcalculationicon.$showhideicon.$lockunlockicon, 'grade_icons');
    }

    /**
     * Given a category element returns collapsing +/- icon if available
     * @param object $object
     * @return string HTML
     */
    protected function get_collapsing_icon($element) {
        global $OUTPUT;

        $icon = '';
        // If object is a category, display expand/contract icon
        if ($element['type'] == 'category') {
            // Load language strings
            $strswitchminus = $this->get_lang_string('aggregatesonly', 'grades');
            $strswitchplus  = $this->get_lang_string('gradesonly', 'grades');
            $strswitchwhole = $this->get_lang_string('fullmode', 'grades');

            $url = new moodle_url($this->gpr->get_return_url(null, array('target'=>$element['eid'], 'sesskey'=>sesskey())));

            if (in_array($element['object']->id, $this->collapsed['aggregatesonly'])) {
                $url->param('action', 'switch_plus');
                $icon = $OUTPUT->action_icon($url, $strswitchplus, 't/switch_plus', array('class'=>'iconsmall'));

            } else if (in_array($element['object']->id, $this->collapsed['gradesonly'])) {
                $url->param('action', 'switch_whole');
                $icon = $OUTPUT->action_icon($url, $strswitchwhole, 't/switch_whole', array('class'=>'iconsmall'));
                $contractexpandicon->image->src = $OUTPUT->pix_url('t/switch_whole');

            } else {
                $url->param('action', 'switch_minus');
                $icon = $OUTPUT->action_icon($url, $strswitchminus, 't/switch_minus', array('class'=>'iconsmall'));
            }
        }
        return $icon;
    }

    /**
     * Processes a single action against a category, grade_item or grade.
     * @param string $target eid ({type}{id}, e.g. c4 for category4)
     * @param string $action Which action to take (edit, delete etc...)
     * @return
     */
    public function process_action($target, $action) {
        // TODO: this code should be in some grade_tree static method
        $targettype = substr($target, 0, 1);
        $targetid = substr($target, 1);
        // TODO: end

        if ($collapsed = get_user_preferences('grade_report_grader_collapsed_categories')) {
            $collapsed = unserialize($collapsed);
        } else {
            $collapsed = array('aggregatesonly' => array(), 'gradesonly' => array());
        }

        switch ($action) {
            case 'switch_minus': // Add category to array of aggregatesonly
                if (!in_array($targetid, $collapsed['aggregatesonly'])) {
                    $collapsed['aggregatesonly'][] = $targetid;
                    set_user_preference('grade_report_grader_collapsed_categories', serialize($collapsed));
                }
                break;

            case 'switch_plus': // Remove category from array of aggregatesonly, and add it to array of gradesonly
                $key = array_search($targetid, $collapsed['aggregatesonly']);
                if ($key !== false) {
                    unset($collapsed['aggregatesonly'][$key]);
                }
                if (!in_array($targetid, $collapsed['gradesonly'])) {
                    $collapsed['gradesonly'][] = $targetid;
                }
                set_user_preference('grade_report_grader_collapsed_categories', serialize($collapsed));
                break;
            case 'switch_whole': // Remove the category from the array of collapsed cats
                $key = array_search($targetid, $collapsed['gradesonly']);
                if ($key !== false) {
                    unset($collapsed['gradesonly'][$key]);
                    set_user_preference('grade_report_grader_collapsed_categories', serialize($collapsed));
                }

                break;
            default:
                break;
        }

        return true;
    }

    /**
     * Returns whether or not to display fixed students column.
     * Includes a browser check, because IE6 doesn't support the scrollbar.
     *
     * @return bool
     */
    public function is_fixed_students() {
        global $USER, $CFG;
        return empty($USER->screenreader) && $CFG->grade_report_fixedstudents &&
            (check_browser_version('MSIE', '7.0') ||
             check_browser_version('Firefox', '2.0') ||
             check_browser_version('Gecko', '2006010100') ||
             check_browser_version('Camino', '1.0') ||
             check_browser_version('Opera', '6.0') ||
             check_browser_version('Safari', '2.0'));
    }

    /**
     * Refactored function for generating HTML of sorting links with matching arrows.
     * Returns an array with 'studentname' and 'idnumber' as keys, with HTML ready
     * to inject into a table header cell.
     * @return array An associative array of HTML sorting links+arrows
     */
    public function get_sort_arrows() {
        global $OUTPUT;
        $arrows = array();

        $strsortasc   = $this->get_lang_string('sortasc', 'grades');
        $strsortdesc  = $this->get_lang_string('sortdesc', 'grades');
        $strfirstname = $this->get_lang_string('firstname');
        $strlastname  = $this->get_lang_string('lastname');

        $firstlink = html_link::make(clone($this->baseurl), $strfirstname);
        $firstlink->url->param('sortitemid', 'firstname');
        $lastlink = html_link::make(clone($this->baseurl), $strlastname);
        $lastlink->url->param('sortitemid', 'lastname');
        $idnumberlink = html_link::make(clone($this->baseurl), get_string('idnumber'));
        $idnumberlink->url->param('sortitemid', 'idnumber');

        $arrows['studentname'] = $OUTPUT->link($lastlink);

        if ($this->sortitemid === 'lastname') {
            if ($this->sortorder == 'ASC') {
                $arrows['studentname'] .= print_arrow('up', $strsortasc, true);
            } else {
                $arrows['studentname'] .= print_arrow('down', $strsortdesc, true);
            }
        }

        $arrows['studentname'] .= ' ' . $OUTPUT->link($firstlink);

        if ($this->sortitemid === 'firstname') {
            if ($this->sortorder == 'ASC') {
                $arrows['studentname'] .= print_arrow('up', $strsortasc, true);
            } else {
                $arrows['studentname'] .= print_arrow('down', $strsortdesc, true);
            }
        }

        $arrows['idnumber'] = $OUTPUT->link($idnumberlink);

        if ('idnumber' == $this->sortitemid) {
            if ($this->sortorder == 'ASC') {
                $arrows['idnumber'] .= print_arrow('up', $strsortasc, true);
            } else {
                $arrows['idnumber'] .= print_arrow('down', $strsortdesc, true);
            }
        }

        return $arrows;
    }
}
