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

require_once($CFG->dirroot.'/grade/export/lib.php');

class grade_export_txt extends grade_export {

    var $format = 'txt'; // export format
    var $separator = "\t"; // default separator

    function set_separator($separator) {
        if ($separator == 'comma') {
            $this->separator = ",";
        } else if ($separator == 'tab') {
            $this->separator = "\t";
        }
    }

    /**
     * To be implemented by child classes
     */
    function print_grades($feedback = false) {

        global $CFG;

        $retval = '';

        /// Whether this plugin is entitled to update export time
        if ($expplugins = explode(",", $CFG->gradeexport)) {
            if (in_array($this->format, $expplugins)) {
                $export = true;
            } else {
            $export = false;
          }
        } else {
            $export = false;
        }

        /// Print header to force download
        header("Content-Type: application/download\n");
        $downloadfilename = clean_filename("{$this->course->shortname} $this->strgrades");
        header("Content-Disposition: attachment; filename=\"$downloadfilename.txt\"");

/// Print names of all the fields

        $retval .= get_string("firstname")."$this->separator".
             get_string("lastname")."{$this->separator}".
             get_string("idnumber")."{$this->separator}".
             get_string("institution")."{$this->separator}".
             get_string("department")."{$this->separator}".
             get_string("email");
        foreach ($this->columns as $column) {
            $column = strip_tags($column);
            $retval .= "{$this->separator}$column";

            /// add a column_feedback column
            if ($feedback) {
                $retval .= "{$this->separator}{$column}_feedback";
            }
        }
        $retval .= "{$this->separator}".get_string("total")."\n";

/// Print all the lines of data.
        foreach ($this->grades as $studentid => $studentgrades) {

            $student = $this->students[$studentid];
            if (empty($this->totals[$student->id])) {
                $this->totals[$student->id] = '';
            }
            $retval .= "$student->firstname{$this->separator}$student->lastname{$this->separator}$student->idnumber{$this->separator}$student->institution{$this->separator}$student->department{$this->separator}$student->email";

            foreach ($studentgrades as $gradeitemid => $grade) {
                $grade = strip_tags($grade);
                $retval .= "{$this->separator}$grade";

                if ($feedback) {
                    $retval .= "{$this->separator}".array_shift($this->comments[$student->id]);
                }

                /// if export flag needs to be set
                /// construct the grade_grade object and update timestamp if CFG flag is set

                if ($export) {
                    $params = new object();
                    $params->itemid = $gradeitemid;
                    $params->userid = $studentid;

                    $grade_grade = new grade_grade($params);
                    $grade_grade->exported = time();
                    // update the time stamp;
                    $grade_grade->update();
                }
            }
            $retval .= "{$this->separator}".$this->totals[$student->id];
            $retval .= "\n";
        }

        echo $retval;

        exit;
    }
}

?>