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
     * @param array $params an array with required parameters for this grade object.
     * @param boolean $fetch Whether to fetch corresponding row from DB or not.
     */
    function grade_object($params=NULL, $fetch = true) {
        if (!empty($params) and (is_array($params) or is_object($params))) {
            if ($fetch and $data = $this->fetch($params)) {
                grade_object::set_properties($this, $data);

            } else {
                grade_object::set_properties($this, $params);
            }
        }
    }

    /**
     * Finds and returns a grade_object instance based on params.
     * @static abstract
     *
     * @param array $params associative arrays varname=>value
     * @return object grade_object instance or false if none found.
     */
    function fetch($params) {
        error('Abstract method fetch() not overrided in '.get_class($this));
    }

    /**
     * Finds and returns all grade_object instances based on params.
     * @static abstract
     *
     * @param array $params associative arrays varname=>value
     * @return array array of grade_object insatnces or false if none found.
     */
    function fetch_all($params) {
        error('Abstract method fetch_all() not overrided in '.get_class($this));
    }

    /**
     * Factory method - uses the parameters to retrieve matching instance from the DB.
     * @static final protected
     * @return mixed object insatnce or false if not found
     */
    function fetch_helper($table, $classname, $params) {
        // we have to do use this hack because of the incomplete OOP implementation in PHP4 :-(
        // in PHP5 we could do it much better
        if ($instances = grade_object::fetch_all_helper($table, $classname, $params)) {
            if (count($instances) > 1) {
                // we should not tolerate any errors here - proplems might appear later
                error('Found more than one record in fetch() !');
            }
            return reset($instances);
        } else {
            return false;
        }
    }

    /**
     * Factory method - uses the parameters to retrieve all matching instances from the DB.
     * @static final protected
     * @return mixed array of object instances or false if not found
     */
    function fetch_all_helper($table, $classname, $params) {
        // we have to do use this hack because of the incomplete OOP implementation in PHP4 :-(
        // in PHP5 we could do it much better
        $instance = new $classname();

        $classvars = (array)$instance;
        $params    = (array)$params;

        $wheresql = array();

        // remove incorrect params - warn developer if needed
        foreach ($params as $var=>$value) {
            if (!array_key_exists($var, $classvars) or in_array($var, $instance->nonfields)) {
                debugging("Incorrect property name $var for class $classname");
                continue;
            }
            if (is_null($value)) {
                $wheresql[] = " $var IS NULL ";
            } else {
                $value = addslashes($value);
                $wheresql[] = " $var = '$value' ";
            }
        }

        if (empty($wheresql)) {
            $wheresql = '';
        } else {
            $wheresql = implode("AND", $wheresql);
        }

        if ($datas = get_records_select($table, $wheresql, 'id')) {
            $result = array();
            foreach($datas as $data) {
                $instance = new $classname();
                grade_object::set_properties($instance, $data);
                $result[$instance->id] = $instance;
            }
            return $result;

        } else {
            return false;
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

        if (array_key_exists('usermodified', $this)) {
            $this->usermodified = $USER->id;
        }

        // we need to do this to prevent infinite loops in addslashes_recursive - grade_item -> category ->grade_item
        $data = new object();
        foreach ($this as $var=>$value) {
            if (!in_array($var, $this->nonfields)) {
                $data->$var = addslashes_recursive($value);
            }
        }

        return update_record($this->table, $data);
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

        if (array_key_exists('usermodified', $this)) {
            $this->usermodified = $USER->id;
        }

        // we need to do this to prevent infinite loops in addslashes_recursive - grade_item -> category ->grade_item
        $data = new object();
        foreach ($this as $var=>$value) {
            if (!in_array($var, $this->nonfields)) {
                $data->$var = addslashes_recursive($value);
            }
        }

        if (!$this->id = insert_record($this->table, addslashes_recursive($data))) {
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
            debugging("Object with this id:{$this->id} does not exist in table:{$this->table}, can not update from db!");
            return false;
        }

        grade_object::set_properties($this, $params);

        return true;
    }

    /**
     * Given an associated array or object, cycles through each key/variable
     * and assigns the value to the corresponding variable in this object.
     * @static final
     */
    function set_properties(&$instance, $params) {
        $classvars = (array)$instance;
        foreach ($params as $var => $value) {
            if (array_key_exists($var, $classvars)) {
                $instance->$var = $value;
            }
        }
    }

}
?>