<?php

///////////////////////////////////////////////////////////////////////////
//                                                                       //
// NOTICE OF COPYRIGHT                                                   //
//                                                                       //
// Moodle - Modular Object-Oriented Dynamic Learning Environment         //
//          http://moodle.com                                            //
//                                                                       //
// Copyright (C) 1999 onwards  Martin Dougiamas  http://moodle.com       //
//                                                                       //
// This program is free software; you can redistribute it and/or modify  //
// it under the terms of the GNU General Public License as published by  //
// the Free Software Foundation; either version 2 of the License, or     //
// (at your option) any later version.                                   //
//                                                                       //
// This program is distributed in the hope that it will be useful,       //
// but WITHOUT ANY WARRANTY; without even the implied warranty of        //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         //
// GNU General Public License for more details:                          //
//                                                                       //
//          http://www.gnu.org/copyleft/gpl.html                         //
//                                                                       //
///////////////////////////////////////////////////////////////////////////

require_once($CFG->dirroot.'/lib/gradelib.php');
require_once($CFG->dirroot.'/grade/lib.php');
require_once($CFG->dirroot.'/grade/export/grade_export_form.php');

/**
 * Base export class
 */
class grade_export {

    var $format = ''; // export format
    var $id; // course id
    var $itemids; // array of grade_item ids;
    var $grades = array();    // Collect all grades in this array
    var $gradeshtml= array(); // Collect all grades html formatted in this array
    var $comments = array(); // Collect all comments for each grade
    var $totals = array();    // Collect all totals in this array
    var $columns = array();     // Accumulate column names in this array.
    var $columnhtml = array();  // Accumulate column html in this array.
    var $columnidnumbers = array(); // Collect all gradeitem id numbers
    var $students = array();
    var $course; // course
    var $publish; // Whether to publish this data via URL, or dump it to browser as usual

    // common strings
    var $strgrades;
    var $strgrade;

    /**
     * Constructor should set up all the private variables ready to be pulled
     * @param int $id course id
     * @param string $itemids array of item ids
     * @param boolean $export_letters Whether to export letter grade_items as literal letters, or as numerical values
     * @note Exporting as letters will lead to data loss if that exported set it re-imported.
     */
    function grade_export($id, $itemids = null, $export_letters=false, $publish=false) {
        global $CFG, $COURSE;

        if ($export_letters) {
            require_once($CFG->dirroot . '/grade/report/lib.php');
            $report = new grade_report($COURSE->id, null, null);
            $letters = $report->get_grade_letters();
        }

        $this->publish = $publish;
        $this->strgrades = get_string("grades");
        $this->strgrade = get_string("grade");
        $this->itemids = $itemids;

        $strmax = get_string("maximumshort");

        if (! $course = get_record("course", "id", $id)) {
            error("Course ID was incorrect");
        }
        $context = get_context_instance(CONTEXT_COURSE, $id);
        require_capability('moodle/grade:view', $context);

        $this->id = $id;
        $this->course = $course;

        // first make sure we have all final grades
        // TODO: check that no grade_item has needsupdate set
        grade_regrade_final_grades($id);

        /// Check to see if groups are being used in this course
        if ($groupmode = groupmode($course)) {   // Groups are being used

            if (isset($_GET['group'])) {
                $changegroup = $_GET['group'];  /// 0 or higher
            } else {
                $changegroup = -1;              /// This means no group change was specified
            }

            $currentgroup = get_and_set_current_group($course, $groupmode, $changegroup);

        } else {
            $currentgroup = false;
        }

        if ($currentgroup) {
            $this->students = get_group_students($currentgroup, "u.lastname ASC");
        } else {
            $this->students = get_role_users(@implode(',', $CFG->gradebookroles), $context);
        }

        if (!empty($this->students)) {
            foreach ($this->students as $student) {
                $this->grades[$student->id] = array();    // Collect all grades in this array
                $this->gradeshtml[$student->id] = array(); // Collect all grades html formatted in this array
                $this->totals[$student->id] = array();    // Collect all totals in this array
                $this->comments[$student->id] = array(); // Collect all comments in tihs array
            }
        }

        // if grade_item ids are specified
        if ($itemids) {
            $gradeitems = array();
            foreach ($itemids as $iid) {
                $gradeitems[] = grade_item::fetch(array('id'=>(int)$iid, 'courseid'=>$this->id));
            }
        } else {
            // else we get all items for this course
            $gradeitems = grade_item::fetch_all(array('courseid'=>$this->id));
        }

        if ($gradeitems) {
            foreach ($gradeitems as $gradeitem) {
                // load as an array of grade_final objects
                if ($itemgrades = $gradeitem->get_final()) {

                    $this->columns[$gradeitem->id] = "$gradeitem->itemmodule: ".$gradeitem->get_name()." - $gradeitem->grademax";

                    $this->columnidnumbers[$gradeitem->id] = $gradeitem->idnumber; // this might be needed for some export plugins

                    if (!empty($gradeitem->grademax)) {
                        $maxgrade = "$strmax: $gradeitem->grademax";
                    } else {
                        $maxgrade = "";
                    }

                    if (!empty($this->students)) {
                        foreach ($this->students as $student) {
                            unset($studentgrade);
                            // TODO add support for comment here MDL-9634

                            if (!empty($itemgrades[$student->id])) {
                                $studentgrade = $itemgrades[$student->id];
                            }

                            if ($export_letters) {
                                $grade_item_displaytype = $report->get_pref('gradedisplaytype', $gradeitem->id);
                                // TODO Convert final grade to letter if export option is on, and grade_item is set to letter type MDL-10490
                                if ($grade_item_displaytype == GRADE_REPORT_GRADE_DISPLAY_TYPE_LETTER && !empty($studentgrade)) {
                                    $studentgrade->finalgrade = grade_grade::get_letter($letters, $studentgrade->finalgrade,
                                                $gradeitem->grademin, $gradeitem->grademax);
                                }
                            }

                            if (!empty($studentgrade->finalgrade)) {
                                $this->grades[$student->id][$gradeitem->id] = $currentstudentgrade = $studentgrade->finalgrade;
                            } else {
                                $this->grades[$student->id][$gradeitem->id] = $currentstudentgrade = "";
                                $this->gradeshtml[$student->id][$gradeitem->id] = "";
                            }
                            if (!empty($maxgrade)) {
                                $total = (float)($this->totals[$student->id]) + (float)($currentstudentgrade);
                            } else {
                                $total = (float)($this->totals[$student->id]) + 0;
                            }

                            if ($export_letters) {
                                $total = grade_grade::get_letter($letters, $total, $gradeitem->grademin, $gradeitem->grademax);
                            }
                            $this->totals[$student->id] = $total;

                            if (!empty($comment)) {
                                // load comments here
                                if ($studentgrade) {
                                    $studentgrade->load_text();
                                    // get the actual comment
                                    $comment = $studentgrade->grade_grade_text->feedback;
                                    $this->comments[$student->id][$gradeitem->id] = $comment;
                                }
                            } else {
                                $this->comments[$student->id][$gradeitem->id] = '';
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * To be implemented by child classe
     * TODO finish PHPdocs
     */
    function print_grades() { }

    /**
     * Displays all the grades on screen as a feedback mechanism
     * TODO finish PHPdoc
     */
    function display_grades($feedback=false, $rows=10) {
        echo '<table>';
        echo '<tr>';
        echo '<th>'.get_string("firstname")."</th>".
             '<th>'.get_string("lastname")."</th>".
             '<th>'.get_string("idnumber")."</th>".
             '<th>'.get_string("institution")."</th>".
             '<th>'.get_string("department")."</th>".
             '<th>'.get_string("email")."</th>";
        foreach ($this->columns as $column) {
            $column = strip_tags($column);
            echo "<th>$column</th>";

            /// add a column_feedback column
            if ($feedback) {
                echo "<th>{$column}_feedback</th>";
            }
        }
        echo '<th>'.get_string("total")."</th>";
        echo '</tr>';
        /// Print all the lines of data.

        $i = 0;
        foreach ($this->grades as $studentid => $studentgrades) {

            // number of preview rows
            if ($i++ == $rows) {
                break;
            }
            echo '<tr>';
            $student = $this->students[$studentid];
            if (empty($this->totals[$student->id])) {
                $this->totals[$student->id] = '';
            }


            echo "<td>$student->firstname</td><td>$student->lastname</td><td>$student->idnumber</td><td>$student->institution</td><td>$student->department</td><td>$student->email</td>";
            foreach ($studentgrades as $grade) {
                $grade = strip_tags($grade);
                echo "<td>$grade</td>";

                if ($feedback) {
                    echo '<td>'.array_shift($this->comments[$student->id]).'</td>';
                }
            }
            echo '<td>'.$this->totals[$student->id].'</td>';
            echo "</tr>";
        }
        echo '</table>';
    }
}

?>