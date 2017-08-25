<?php // $Id$

///////////////////////////////////////////////////////////////////////////
//                                                                       //
// NOTICE OF COPYRIGHT                                                   //
//                                                                       //
// Moodle - Modular Object-Oriented Dynamic Learning Environment         //
//          http://moodle.com                                            //
//                                                                       //
// Copyright (C) 2001-2007  Martin Dougiamas  http://dougiamas.com       //
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

/**
 * An abstract object that holds methods and attributes common to all grade_* objects defined here.
 * @abstract
 */
class grade_object {
    /**
     * Array of class variables that are not part of the DB table fields
     * @var array $nonfields
     */
    var $nonfields = array('nonfields', 'required_fields');

    /**
     * Array of required fields (keys) and their default values (values).
     * @var array $required_fields
     */
    var $required_fields = array();

    /**
     * The PK.
     * @var int $id
     */
    var $id;

    /**
     * The first time this grade_calculation was created.
     * @var int $timecreated
     */
    var $timecreated;

    /**
     * The last time this grade_calculation was modified.
     * @var int $timemodified
     */
    var $timemodified;

    /**
     * Constructor. Optionally (and by default) attempts to fetch corresponding row from DB.
     * @param object $params an object with named parameters for this grade item.
     * @param boolean $fetch Whether to fetch corresponding row from DB or not.
     */
    function grade_object($params=NULL, $fetch = true) {
        if (!empty($params) && (is_array($params) || is_object($params))) {
            $this->assign_to_this($params);

            if ($fetch) {
                $records = $this->fetch_all_using_this();
                if ($records && count($records) > 0) {
                    $this->assign_to_this(current($records));
                }
            }
        }
    }

    /**
     * Updates this object in the Database, based on its object variables. ID must be set.
     *
     * @return boolean
     */
    function update() {
        global $USER;

        $this->timemodified = time();

        if (empty($this->usermodified)) {
            $this->usermodified = $USER->id;
        }

        return update_record($this->table, addslashes_recursive($this));
    }

    /**
     * Deletes this object from the database.
     */
    function delete() {
        return delete_records($this->table, 'id', $this->id);
    }

    /**
     * Records this object in the Database, sets its id to the returned value, and returns that value.
     * If successful this function also fetches the new object data from database and stores it
     * in object properties.
     * @return int PK ID if successful, false otherwise
     */
    function insert() {
        global $USER;

        if (!empty($this->id)) {
            debugging("Grade object already exists!");
            return false;
        }

        $this->timecreated = $this->timemodified = time();

        if (empty($this->usermodified)) {
            $this->usermodified = $USER->id;
        }

        $clonethis = fullclone($this);

        // Unset non-set and null fields
        foreach ($clonethis as $var => $val) {
            if (!isset($val)) {
                unset($clonethis->$var);
            }
        }

        if (!$this->id = insert_record($this->table, addslashes_recursive($clonethis), true)) {
            debugging("Could not insert object into db");
            return false;
        }

        // set all object properties from real db data
        $this->update_from_db();

        return $this->id;
    }

    /**
     * Using this object's id field, fetches the matching record in the DB, and looks at
     * each variable in turn. If the DB has different data, the db's data is used to update
     * the object. This is different from the update() function, which acts on the DB record
     * based on the object.
     */
    function update_from_db() {
        if (empty($this->id)) {
            debugging("The object could not be used in its state to retrieve a matching record from the DB, because its id field is not set.");
            return false;
        }

        if (!$params = get_record($this->table, 'id', $this->id)) {
            debugging("Object with this id does not exist, can not update from db!");
            return false;
        }

        $this->assign_to_this($params);

        return true;
    }

    /**
     * Uses the variables of this object to retrieve all matching objects from the DB.
     * @return array $objects
     */
    function fetch_all_using_this() {
        $variables = get_object_vars($this);
        $wheresql = '';

        foreach ($variables as $var => $value) {
            if (!empty($value) && !in_array($var, $this->nonfields)) {
                $value = addslashes($value);
                $wheresql .= " $var = '$value' AND ";
            }
        }

        // Trim trailing AND
        $wheresql = substr($wheresql, 0, strrpos($wheresql, 'AND'));

        $objects = get_records_select($this->table, $wheresql, 'id');

        if (!empty($objects)) {
            $full_objects = array();

            // Convert the stdClass objects returned by the get_records_select method into proper objects
            $classname = get_class($this);
            foreach ($objects as $id => $stdobject) {
                $full_objects[$id] = new $classname($stdobject, false);
            }
            return $full_objects;
        } else {
            return $objects;
        }
    }


    /**
     * Given an associated array or object, cycles through each key/variable
     * and assigns the value to the corresponding variable in this object.
     */
    function assign_to_this($params) {
        foreach ($params as $param => $value) {
            if (in_object_vars($param, $this)) {
                $this->$param = $value;
            }
        }
    }

}
?>