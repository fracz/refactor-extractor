<?php // $Id$

///////////////////////////////////////////////////////////////////////////
//                                                                       //
// NOTICE OF COPYRIGHT                                                   //
//                                                                       //
// Moodle - Modular Object-Oriented Dynamic Learning Environment         //
//          http://moodle.com                                            //
//                                                                       //
// Copyright (C) 2001-2003  Martin Dougiamas  http://dougiamas.com       //
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

require_once('grade_object.php');

/**
 * A text string used to compute the value displayed by a grade_item.
 * There can be only one grade_text per grade_item (one-to-one).
 */
class grade_grades_text extends grade_object {
    /**
     * DB Table (used by grade_object).
     * @var string $table
     */
    var $table = 'grade_grades_text';

    /**
     * Array of class variables that are not part of the DB table fields
     * @var array $nonfields
     */
    var $nonfields = array('table', 'nonfields', 'grade_item');

    /**
     * The grade_item.id this text refers to.
     * @var int $itemid
     */
    var $itemid;

    /**
     * The grade_item object referenced by $this->itemid.
     * @var object $grade_item
     */
    var $grade_item;

    /**
     * The user.id this text refers to.
     * @var int $userid
     */
    var $userid;

    /**
     * Further information like forum rating distribution 4/5/7/0/1
     * @var string $information
     */
    var $information;

    /**
     * Text format for information (FORMAT_PLAIN, FORMAT_HTML etc...).
     * @var int $informationformat
     */
    var $informationformat = FORMAT_MOODLE;

    /**
     * Manual feedback from the teacher. This could be a code like 'mi'.
     * @var string $feedback
     */
    var $feedback;

    /**
     * Text format for feedback (FORMAT_PLAIN, FORMAT_HTML etc...).
     * @var int $feedbackformat
     */
    var $feedbackformat = FORMAT_MOODLE;

    /**
     * The userid of the person who last modified this text.
     * @var int $usermodified
     */
    var $usermodified;

    /**
     * Finds and returns a grade_grades_text instance based on params.
     * @static
     *
     * @param array $params associative arrays varname=>value
     * @return object grade_grades_text instance or false if none found.
     */
    function fetch($params) {
        return grade_object::fetch_helper('grade_grades_text', 'grade_grades_text', $params);
    }

    /**
     * Finds and returns all grade_grades_text instances based on params.
     * @static
     *
     * @param array $params associative arrays varname=>value
     * @return array array of grade_grades_text insatnces or false if none found.
     */
    function fetch_all($params) {
        return grade_object::fetch_all_helper('grade_grades_text', 'grade_grades_text', $params);
    }

    /**
     * Loads the grade_item object referenced by $this->itemid and saves it as $this->grade_item for easy access.
     * @return object grade_item.
     */
    function load_grade_item() {
        if (empty($this->grade_item) && !empty($this->itemid)) {
            $this->grade_item = grade_item::fetch(array('id'=>$this->itemid));
        }
        return $this->grade_item;
    }
}
?>