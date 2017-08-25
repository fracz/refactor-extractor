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
 * Class representing a grade scale. It is responsible for handling its DB representation,
 * modifying and returning its metadata.
 */
class grade_scale extends grade_object {
    /**
     * DB Table (used by grade_object).
     * @var string $table
     */
    var $table = 'scale';

    /**
     * Array of class variables that are not part of the DB table fields
     * @var array $nonfields
     */
    var $nonfields = array('table', 'nonfields', 'required_fields', 'scale_items');

    /**
     * The course this scale belongs to.
     * @var int $courseid
     */
    var $courseid;

    var $userid;

    /**
     * The name of the scale.
     * @var string $name
     */
    var $name;

    /**
     * The items in this scale.
     * @var array $scale_items
     */
    var $scale_items = array();

    /**
     * A string representatin of the scale items (a comma-separated list).
     * @var string $scale
     */
    var $scale;

    /**
     * A description for this scale.
     * @var string $description
     */
    var $description;

    /**
     * Finds and returns a grade_scale instance based on params.
     * @static
     *
     * @param array $params associative arrays varname=>value
     * @return object grade_scale instance or false if none found.
     */
    function fetch($params) {
        return grade_object::fetch_helper('scale', 'grade_scale', $params);
    }

    /**
     * Finds and returns all grade_scale instances based on params.
     * @static
     *
     * @param array $params associative arrays varname=>value
     * @return array array of grade_scale insatnces or false if none found.
     */
    function fetch_all($params) {
        return grade_object::fetch_all_helper('scale', 'grade_scale', $params);
    }

    /**
     * Loads the scale's items into the $scale_items array.
     * There are three ways to achieve this:
     * 1. No argument given: The $scale string is already loaded and exploded to an array of items.
     * 2. A string is given: A comma-separated list of items is exploded into an array of items.
     * 3. An array of items is given and saved directly as the array of items for this scale.
     *
     * @param mixed $items Could be null, a string or an array. The method behaves differently for each case.
     * @return array The resulting array of scale items or null if the method failed to produce one.
     */
    function load_items($items=NULL) {
        if (empty($items)) {
            $this->scale_items = explode(',', $this->scale);
        } elseif (is_array($items)) {
            $this->scale_items = $items;
        } else {
            $this->scale_items = explode(',', $items);
        }

        // Trim whitespace around each value
        foreach ($this->scale_items as $key => $val) {
            $this->scale_items[$key] = trim($val);
        }

        return $this->scale_items;
    }

    /**
     * Compacts (implodes) the array of items in $scale_items into a comma-separated string, $scale.
     * There are three ways to achieve this:
     * 1. No argument given: The $scale_items array is already loaded and imploded to a string of items.
     * 2. An array is given and is imploded into a string of items.
     * 3. A string of items is given and saved directly as the $scale variable.
     * NOTE: This method is the exact reverse of load_items, and their input/output should be interchangeable. However,
     * because load_items() trims the whitespace around the items, when the string is reconstructed these whitespaces will
     * be missing. This is not an issue, but should be kept in mind when comparing the two strings.
     *
     * @param mixed $items Could be null, a string or an array. The method behaves differently for each case.
     * @return array The resulting string of scale items or null if the method failed to produce one.
     */
    function compact_items($items=NULL) {
        if (empty($items)) {
            $this->scale = implode(',', $this->scale_items);
        } elseif (is_array($items)) {
            $this->scale = implode(',', $items);
        } else {
            $this->scale = $items;
        }

        return $this->scale;
    }

    /**
     * When called on a loaded scale object (with a valid id) and given a float grade between
     * the grademin and grademax, this method returns the scale item that falls closest to the
     * float given (which is usually an average of several grades on a scale). If the float falls
     * below 1 but above 0, it will be rounded up to 1.
     * @param float $grade
     * @return string
     */
    function get_nearest_item($grade) {
        // Obtain nearest scale item from average
        $scales_array = get_records_list('scale', 'id', $this->id);
        $scale = $scales_array[$this->id];
        $scales = explode(",", $scale->scale);

        // this could be a 0 when summed and rounded, e.g, 1, no grade, no grade, no grade
        if ($grade < 1) {
            $grade = 1;
        }

        return $scales[$grade-1];
    }

    /**
     * Determines if scale can be deleted.
     * @return boolean
     */
    function can_delete() {
        $count = $this->get_uses_count();
        return empty($count);
    }

    /**
     * Returns the number of places where scale is used - activities, grade items, outcomes, etc.
     * @return int
     */
    function get_uses_count() {
        global $CFG;

        $count = 0;
        if (!empty($this->courseid)) {
            if ($scales_uses = course_scale_used($this->courseid,$this->id)) {
                $count += count($scales_uses);
            }
        } else {
            $courses = array();
            if ($scales_uses = site_scale_used($this->id,$courses)) {
                $count += count($scales_uses);
            }
        }

        // count grade items
        $sql = "SELECT COUNT(id) FROM {$CFG->prefix}grade_items WHERE scaleid = {$this->id} AND outcomeid IS NULL";
        if ($scales_uses = count_records_sql($sql)) {
            $count += $scales_uses;
        }

        // count outcomes
        $sql = "SELECT COUNT(id) FROM {$CFG->prefix}grade_outcomes WHERE scaleid = {$this->id}";
        if ($scales_uses = count_records_sql($sql)) {
            $count += $scales_uses;
        }

        return $count;
    }
}
?>