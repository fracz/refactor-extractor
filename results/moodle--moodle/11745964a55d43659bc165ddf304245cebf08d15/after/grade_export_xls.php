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

class grade_export_xls extends grade_export {

    /**
     * To be implemented by child classes
     */
    function print_grades($feedback = false) {
        global $CFG;

        $this->load_grades();

        /// Whether this plugin is entitled to update export time
        if ($expplugins = explode(",", $CFG->gradeexport)) {
            if (in_array('xls', $expplugins)) {
                $export = true;
            } else {
                $export = false;
          }
        } else {
            $export = false;
        }

        require_once($CFG->dirroot.'/lib/excellib.class.php');

    /// Calculate file name
        $downloadfilename = clean_filename("{$this->course->shortname} $this->strgrades.xls");
    /// Creating a workbook
        $workbook = new MoodleExcelWorkbook("-");
    /// Sending HTTP headers
        $workbook->send($downloadfilename);
    /// Adding the worksheet
        $myxls =& $workbook->add_worksheet($this->strgrades);

    /// Print names of all the fields
        $myxls->write_string(0,0,get_string("firstname"));
        $myxls->write_string(0,1,get_string("lastname"));
        $myxls->write_string(0,2,get_string("idnumber"));
        $myxls->write_string(0,3,get_string("institution"));
        $myxls->write_string(0,4,get_string("department"));
        $myxls->write_string(0,5,get_string("email"));
        $pos=6;
        foreach ($this->columns as $column) {
            $myxls->write_string(0,$pos++,strip_tags($column));
            /// add a column_feedback column
            if ($feedback) {
                $myxls->write_string(0,$pos++,strip_tags($column."_feedback"));
            }
        }

    /// Print all the lines of data.
        $i = 0;
        if (!empty($this->grades)) {
            foreach ($this->grades as $studentid => $studentgrades) {
                $i++;
                $student = $this->students[$studentid];
                if (empty($this->totals[$student->id])) {
                    $this->totals[$student->id] = '';
                }

                $myxls->write_string($i,0,$student->firstname);
                $myxls->write_string($i,1,$student->lastname);
                $myxls->write_string($i,2,$student->idnumber);
                $myxls->write_string($i,3,$student->institution);
                $myxls->write_string($i,4,$student->department);
                $myxls->write_string($i,5,$student->email);
                $j=6;
                foreach ($studentgrades as $gradeitemid => $grade) {
                    if (is_numeric($grade)) {
                        $myxls->write_number($i,$j++,$grade);
                    }
                    else {
                        $myxls->write_string($i,$j++,strip_tags($grade));
                    }

                    // writing comment if requested
                    if ($feedback) {
                        $myxls->write_string($i,$j++,$this->comments[$student->id][$gradeitemid]);
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
            }
        }

    /// Close the workbook
        $workbook->close();
        exit;
    }
}

?>