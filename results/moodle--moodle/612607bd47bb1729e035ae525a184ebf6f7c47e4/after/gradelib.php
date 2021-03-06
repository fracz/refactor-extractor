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

/**
 * Library of functions for gradebook
 *
 * @author Moodle HQ developers
 * @version  $Id$
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package moodlecore
 */

define('GRADE_AGGREGATE_MEAN', 0);
define('GRADE_AGGREGATE_MEDIAN', 1);
define('GRADE_AGGREGATE_SUM', 2);
define('GRADE_AGGREGATE_MODE', 3);

define('GRADE_CHILDTYPE_ITEM', 0);
define('GRADE_CHILDTYPE_CAT', 1);

define('GRADE_ITEM', 0); // Used to compare class names with CHILDTYPE values
define('GRADE_CATEGORY', 1); // Used to compare class names with CHILDTYPE values

define('GRADE_TYPE_NONE', 0);
define('GRADE_TYPE_VALUE', 1);
define('GRADE_TYPE_SCALE', 2);
define('GRADE_TYPE_TEXT', 3);

define('GRADE_UPDATE_OK', 0);
define('GRADE_UPDATE_FAILED', 1);
define('GRADE_UPDATE_MULTIPLE', 2);
define('GRADE_UPDATE_ITEM_DELETED', 3);
define('GRADE_UPDATE_INVALID_GRADE', 4);
define('GRADE_UPDATE_LOCKED', 5);


require_once($CFG->libdir . '/grade/grade_category.php');
require_once($CFG->libdir . '/grade/grade_item.php');
require_once($CFG->libdir . '/grade/grade_calculation.php');
require_once($CFG->libdir . '/grade/grade_grades_raw.php');
require_once($CFG->libdir . '/grade/grade_grades_final.php');
require_once($CFG->libdir . '/grade/grade_scale.php');
require_once($CFG->libdir . '/grade/grade_outcome.php');
require_once($CFG->libdir . '/grade/grade_history.php');
require_once($CFG->libdir . '/grade/grade_grades_text.php');
require_once($CFG->libdir . '/grade/grade_tree.php');

/***** PUBLIC GRADE API *****/

function grade_update($courseid, $itemtype, $itemmodule, $iteminstance, $itemnumber, $grade=NULL, $itemdetails=NULL) {

    // only following grade_item properties can be changed/used in this function
    $allowed = array('itemname', 'idnumber', 'gradetype', 'grademax', 'grademin', 'scaleid', 'deleted');

    if (is_null($courseid) or is_null($itemtype)) {
        debugging('Missing courseid or itemtype');
        return GRADE_UPDATE_FAILED;
    }

    $grade_item = new grade_item(compact('courseid', 'itemtype', 'itemmodule', 'iteminstance', 'itemnumber'), false);
    if (!$grade_items = $grade_item->fetch_all_using_this()) {
        // create a new one
        $grade_item = false;

    } else if (count($grade_items) == 1){
        $grade_item = reset($grade_items);
        unset($grade_items); //release memory

    } else {

        debugging('Found more than one grading item');
        return GRADE_UPDATE_MULTIPLE;
    }

/// Create or update the grade_item if needed
    if (!$grade_item) {
        $params = compact('courseid', 'itemtype', 'itemmodule', 'iteminstance', 'itemnumber');
        if ($itemdetails) {
            $itemdetails = (array)$itemdetails;
            foreach ($itemdetails as $k=>$v) {
                if (!in_array($k, $allowed)) {
                    // ignore it
                    continue;
                }
                if ($k == 'gradetype' and $v == GRADE_TYPE_NONE) {
                    // no grade item needed!
                    return GRADE_UPDATE_OK;
                }
                $params[$k] = $v;
            }
        }
        $itemid = grade_create_item($params);
        $grade_item = grade_item::fetch('id', $itemid);

    } else {
        if ($grade_item->locked) {
            debugging('Grading item is locked!');
            return GRADE_UPDATE_LOCKED;
        }

        if ($itemdetails) {
            $itemdetails = (array)$itemdetails;
            $update = false;
            foreach ($itemdetails as $k=>$v) {
                if (!in_array($k, $allowed)) {
                    // ignore it
                    continue;
                }
                if ($grade_item->{$k} != $v) {
                    $grade_item->{$k} = $v;
                    $update = true;
                }
            }
            if ($update) {
                $grade_item->update();
            }
        }
    }

/// Some extra checks
    // do we use grading?
    if ($grade_item->gradetype == GRADE_TYPE_NONE) {
        return GRADE_UPDATE_OK;
    }

    // no grade submitted
    if (empty($grade)) {
        return GRADE_UPDATE_OK;
    }

    // no grading in deleted items
    if ($grade_item->deleted) {
        debugging('Grade item was already deleted!');
        return GRADE_UPDATE_ITEM_DELETED;
    }

/// Finally start processing of grades
    if (is_object($grade)) {
        $grades = array($grade);
    } else {
        if (array_key_exists('userid', $grade)) {
            $grades = array($grade);
        } else {
            $grades = $grade;
        }
    }

    unset($grade);

    foreach ($grades as $grade) {
        $grade = (array)$grade;
        if (empty($grade['userid'])) {
            debugging('Invalid grade submitted');
            return GRADE_UPDATE_INVALID_GRADE;
        }

        // get the raw grade if it exist
        $rawgrade = new grade_grades_raw(array('itemid'=>$grade_item->id, 'userid'=>$grade['userid']));
        $rawgrade->grade_item = &$grade_item; // we already have it, so let's use it

        // store these to keep track of original grade item settings
        $rawgrade->grademax = $grade_item->grademax;
        $rawgrade->grademin = $grade_item->grademin;
        $rawgrade->scaleid  = $grade_item->scaleid;

        if (isset($grade['feedback'])) {
            $rawgrade->feedback = $grade['feedback'];
            if (isset($grade['feedbackformat'])) {
                $rawgrade->feedbackformat = $grade['feedbackformat'];
            } else {
                $rawgrade->feedbackformat = FORMAT_PLAIN;
            }
        }

        if (!isset($grade['gradevalue'])) {
            $grade['gradevalue'] = null; // means no grade yet
        }

        if ($rawgrade->id) {
            $rawgrade->update($grade['gradevalue'], 'event');
        } else {
            $rawgrade->gradevalue = $grade['gradevalue'];
            $rawgrade->insert();
        }

        //trigger grade_updated event notification
        $eventdata = new object();
        $eventdata->itemid = $grade_item->id;
        $eventdata->grade  = $grade;
        events_trigger('grade_updated', $eventdata);
    }

    return GRADE_UPDATE_OK;
}

/***** END OF PUBLIC API *****/

/**
* Extracts from the gradebook all the grade items attached to the calling object.
* For example, an assignment may want to retrieve all the grade_items for itself,
* and get three outcome scales in return. This will affect the grading interface.
*
* Note: Each parameter refines the search. So if you only give the courseid,
*       all the grade_items for this course will be returned. If you add the
*       itemtype 'mod', all grade_items for this courseif AND for the 'mod'
*       type will be returned, etc...
*
* @param int $courseid The id of the course to which the grade items belong
* @param string $itemtype 'mod', 'blocks', 'import', 'calculated' etc
* @param string $itemmodule 'forum, 'quiz', 'csv' etc
* @param int $iteminstance id of the item module
* @param string $itemname The name of the grade item
* @param int $itemnumber Can be used to distinguish multiple grades for an activity
* @param int $idnumber grade item Primary Key
* @return array An array of grade items
*/
function grade_get_items($courseid, $itemtype=NULL, $itemmodule=NULL, $iteminstance=NULL, $itemname=NULL, $itemnumber=NULL, $idnumber=NULL) {
    $grade_item = new grade_item(compact('courseid', 'itemtype', 'itemmodule', 'iteminstance', 'itemname', 'itemnumber', 'idnumber'), false);
    $grade_items = $grade_item->fetch_all_using_this();
    return $grade_items;
}


/**
* Creates a new grade_item in case it doesn't exist.
* This function is called when a new module is created.
*
* @param mixed $params array or object
* @return mixed New grade_item id if successful
*/
function grade_create_item($params) {
    $grade_item = new grade_item($params);

    if (empty($grade_item->id)) {
        return $grade_item->insert();
    } else {
        debugging('Grade item already exists - id:'.$grade_item->id);
        return $grade_item->id;
    }
}

/**
* For a given set of items, create a category to group them together (if one doesn't yet exist).
* Modules may want to do this when they are created. However, the ultimate control is in the gradebook interface itself.
*
* @param int $courseid
* @param string $fullname The name of the new category
* @param array $items An array of grade_items to group under the new category
* @param string $aggregation
* @return mixed New grade_category id if successful
*/
function grade_create_category($courseid, $fullname, $items, $aggregation=GRADE_AGGREGATE_MEAN) {
    $grade_category = new grade_category(compact('courseid', 'fullname', 'items', 'aggregation'));

    if (empty($grade_category->id)) {
        return $grade_category->insert();
    } else {
        return $grade_category->id;
    }
}


/**
* Tells a module whether a grade (or grade_item if $userid is not given) is currently locked or not.
* This is a combination of the actual settings in the grade tables and a check on moodle/course:editgradeswhenlocked.
* If it's locked to the current use then the module can print a nice message or prevent editing in the module.
* If no $userid is given, the method will always return the grade_item's locked state.
* If a $userid is given, the method will first check the grade_item's locked state (the column). If it is locked,
* the method will return true no matter the locked state of the specific grade being checked. If unlocked, it will
* return the locked state of the specific grade.
*
* @param string $itemtype 'mod', 'blocks', 'import', 'calculated' etc
* @param string $itemmodule 'forum, 'quiz', 'csv' etc
* @param int $iteminstance id of the item module
* @param int $itemnumber Optional number of the item to check
* @param int $userid ID of the user who owns the grade
* @return boolean Whether the grade is locked or not
*/
function grade_is_locked($itemtype, $itemmodule, $iteminstance, $itemnumber=NULL, $userid=NULL) {
    $grade_item = new grade_item(compact('itemtype', 'itemmodule', 'iteminstance', 'itemnumber'));
    return $grade_item->is_locked($userid);
}

/**
 * Updates all grade_grades_final for each grade_item matching the given attributes.
 * The search is further restricted, so that only grade_items that have needs_update == TRUE
 * or that use calculation are retrieved.
 *
 * @param int $courseid
 * @param int $gradeitemid
 * @return int Number of grade_items updated
 */
function grade_update_final_grades($courseid=NULL, $gradeitemid=NULL) {
    $grade_item = new grade_item();
    $grade_item->courseid = $courseid;
    $grade_item->id = $gradeitemid;
    $grade_items = $grade_item->fetch_all_using_this();

    $count = 0;

    foreach ($grade_items as $gi) {
        $calculation = $gi->get_calculation();
        if (!empty($calculation) || $gi->needsupdate) {
            if ($gi->update_final_grade()) {
                $count++;
            }
        }
    }

    return $count;
}

/**
 * For backward compatibility with old third-party modules, this function is called
 * via to admin/cron.php to search all mod/xxx/lib.php files for functions named xxx_grades(),
 * if the current modules does not have grade events registered with the grade book.
 * Once the data is extracted, the events_trigger() function can be called to initiate
 * an event as usual and copy/ *upgrade the data in the gradebook tables.
 */
function grade_grab_legacy_grades() {

    global $CFG, $db;

    if (!$mods = get_list_of_plugins('mod') ) {
        error('No modules installed!');
    }

    foreach ($mods as $mod) {

        if ($mod == 'NEWMODULE') {   // Someone has unzipped the template, ignore it
            continue;
        }

        $fullmod = $CFG->dirroot .'/mod/'. $mod;

        // include the module lib once
        if (file_exists($fullmod.'/lib.php')) {
            include_once($fullmod.'/lib.php');
            // look for modname_grades() function - old gradebook pulling function
            // if present sync the grades with new grading system
            $gradefunc = $mod.'_grades';
            if (function_exists($gradefunc)) {

                // get all instance of the activity
                $sql = "SELECT a.*, cm.idnumber as cmidnumber, a.course as courseid, m.name as modname FROM {$CFG->prefix}$mod a, {$CFG->prefix}course_modules cm, {$CFG->prefix}modules m
                        WHERE m.name='$mod' AND m.id=cm.module AND cm.instance=a.id";

                if ($modinstances = get_records_sql($sql)) {
                    foreach ($modinstances as $modinstance) {
                        // for each instance, call the xxx_grades() function
                        if ($grades = $gradefunc($modinstance->id)) {

                            $grademax = $grades->maxgrade;
                            $scaleid = 0;
                            if (!is_numeric($grademax)) {
                                // scale name is provided as a string, try to find it
                                if (!$scale = get_record('scale', 'name', $grademax)) {
                                    debugging('Incorrect scale name! name:'.$grademax);
                                    continue;
                                }
                                $scaleid = $scale->id;
                            }

                            if (!$grade_item = grade_get_legacy_grade_item($modinstance, $grademax, $scaleid)) {
                                debugging('Can not get/create legacy grade item!');
                                continue;
                            }

                            foreach ($grades->grades as $userid=>$usergrade) {
                                // make the grade_added eventdata
                                $eventdata = new object();
                                $eventdata->itemid = $grade_item->id;
                                $eventdata->userid = $userid;

                                if ($usergrade == '-') {
                                    // no grade
                                    $eventdata->gradevalue = null;

                                } else if ($scaleid) {
                                    // scale in use, words used
                                    $gradescale = explode(",", $scale->scale);
                                    $eventdata->gradevalue = array_search($usergrade, $gradescale) + 1;

                                } else {
                                    // good old numeric value
                                    $eventdata->gradevalue = $usergrade;
                                }

                                events_trigger('grade_updated', $eventdata);
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * Get (create if needed) grade item for legacy modules.
 */
function grade_get_legacy_grade_item($modinstance, $grademax, $scaleid) {

    // does it already exist?
    if ($grade_items = grade_get_items($modinstances->courseid, 'mod', $modinstance->modname, $modinstances->id)) {
        if (count($grade_items) > 1) {
            return false;
        }

        $grade_item = reset($grade_items);
        $updated = false;

        if ($scaleid) {
            if ($grade_item->scaleid != $scaleid) {
                $grade_item->gradetype = GRADE_TYPE_SCALE;
                $grade_item->scaleid   = $scaleid;
                $updated = true;;
            }

        } else if ($grade_item->scaleid != $scaleid or $grade_item->grademax != $grademax) {
           $grade_item->gradetype = GRADE_TYPE_VALUE;
           $grade_item->scaleid   = 0;
           $grade_item->grademax  = $grademax;
           $grade_item->grademin  = 0;
           $updated = true;;
        }

        if ($grade_item->itemname != $modinstance->name) {
           $grade_item->itemname = $modinstance->name;
           $updated = true;;
        }

        if ($grade_item->idnumber != $modinstance->cmidnumber) {
           $grade_item->idnumber = $modinstance->cmidnumber;
           $updated = true;;
        }

        if ($updated) {
            $grade_item->update();
        }

        return $grade_item;
    }

    // create new one
    $params = array('courseid'    =>$modinstance->courseid,
                    'itemtype'    =>'mod',
                    'itemmodule'  =>$modinstance->modname,
                    'iteminstance'=>$modinstance->id,
                    'itemname'    =>$modinstance->name,
                    'idnumber'    =>$modinstance->cmidnumber);

    if ($scaleid) {
        $params['gradetype'] = GRADE_TYPE_SCALE;
        $params['scaleid']   = $scaleid;

    } else {
        $params['gradetype'] = GRADE_TYPE_VALUE;
        $params['grademax']  = $grademax;
        $params['grademin']  = 0;
    }

    if (!$itemid = grade_create_item($params)) {
        return false;
    }

    return grade_item::fetch('id', $itemid);
}

/**
 * Given a float value situated between a source minimum and a source maximum, converts it to the
 * corresponding value situated between a target minimum and a target maximum. Thanks to Darlene
 * for the formula :-)
 * @param float $gradevalue
 * @param float $source_min
 * @param float $source_max
 * @param float $target_min
 * @param float $target_max
 * @return float Converted value
 */
function standardise_score($gradevalue, $source_min, $source_max, $target_min, $target_max, $debug=false) {
    $factor = ($gradevalue - $source_min) / ($source_max - $source_min);
    $diff = $target_max - $target_min;
    $standardised_value = $factor * $diff + $target_min;
    if ($debug) {
        echo 'standardise_score debug info: (lib/gradelib.php)';
        print_object(array('gradevalue' => $gradevalue,
                           'source_min' => $source_min,
                           'source_max' => $source_max,
                           'target_min' => $target_min,
                           'target_max' => $target_max,
                           'result'     => $standardised_value));
    }
    return $standardised_value;
}


/**
 * Handles some specific grade_update_request events,
 * see lib/db/events.php for description of $eventdata format.
 *
 * @param object $eventdata contains all the data for the event
 * @return boolean success
 *
 */
function grade_handler($eventdata) {
    $eventdata = (array)$eventdata;

    // each grade must belong to some user
    if (empty($eventdata['userid'])) {
        debugging('Missing user id in event data!');
        return true;
    }

    // grade item idnumber must be specified or else it could be accidentally duplicated,
    if (empty($eventdata['idnumber'])) {
        debugging('Missing grade item idnumber in event!');
        return true;
    }

    // get the grade item from db
    $grade_item = new grade_item(array('idnumber'=>$eventdata['idnumber']), false);
    if (!$grade_items = $grade_item->fetch_all_using_this()) {
        // TODO: create a new one - tricky, watch out for duplicates !!
        //       use only special type 'import'(y) or 'manual'(?)
        debugging('Can not create new grade items yet :-(');
        return true;

    } else if (count($grade_items) == 1) {
        $grade_item = reset($grade_items);
        unset($grade_items); //release memory

    } else {
        debugging('More than one grade item matched, grade update request failed');
        return true;
    }

    // !! TODO: whitelist only some types such as 'import'(?) 'manual'(?) and ignore the rest!!
    if ($grade_item->itemtype == 'mnod') {
        // modules must have own handlers for grade update requests
        return true;
    }

    $grade = new object();
    $grade->userid = $eventdata['userid'];
    if (isset($eventdata['gradevalue'])) {
        $grade->feedback = $eventdata['gradevalue'];
    }
    if (isset($eventdata['feedback'])) {
        $grade->feedback = $eventdata['feedback'];
    }
    if (isset($eventdata['feedbackformat'])) {
        $grade->feedbackformat = $eventdata['feedbackformat'];
    }

    grade_update($grade_item->courseid, $grade_item->itemtype, $grade_item->itemmodule,
                 $grade_item->iteminstance, $grade_item->itemnumber, $grade, $eventdata);

    // everything ok :-)
    return true;

}


?>