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

define('GRADE_AGGREGATE_MEAN_ALL', 0);
define('GRADE_AGGREGATE_MEDIAN', 1);
define('GRADE_AGGREGATE_MEAN_GRADED', 2);
define('GRADE_AGGREGATE_MIN', 3);
define('GRADE_AGGREGATE_MAX', 4);
define('GRADE_AGGREGATE_MODE', 5);

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
define('GRADE_UPDATE_ITEM_LOCKED', 4);


require_once($CFG->libdir . '/grade/grade_category.php');
require_once($CFG->libdir . '/grade/grade_item.php');
require_once($CFG->libdir . '/grade/grade_grades.php');
require_once($CFG->libdir . '/grade/grade_scale.php');
require_once($CFG->libdir . '/grade/grade_outcome.php');
require_once($CFG->libdir . '/grade/grade_history.php');
require_once($CFG->libdir . '/grade/grade_grades_text.php');
require_once($CFG->libdir . '/grade/grade_tree.php');

/***** PUBLIC GRADE API *****/

/**
 * Submit new or update grade; update/create grade_item definition. Grade must have userid specified,
 * rawgrade and feedback with format are optional. rawgrade NULL means 'Not graded', missing property
 * or key means do not change existing.
 *
 * Only following grade item properties can be changed 'itemname', 'idnumber', 'gradetype', 'grademax',
 * 'grademin', 'scaleid', 'multfactor', 'plusfactor', 'deleted'.
 *
 * @param string $source source of the grade such as 'mod/assignment', often used to prevent infinite loops when processing grade_updated events
 * @param int $courseid id of course
 * @param string $itemtype type of grade item - mod, block, gradecategory, calculated
 * @param string $itemmodule more specific then $itemtype - assignment, forum, etc.; maybe NULL for some item types
 * @param int $iteminstance instance it of graded subject
 * @param int $itemnumber most probably 0, modules can use other numbers when having more than one grades for each user
 * @param mixed $grades grade (object, array) or several grades (arrays of arrays or objects), NULL if updating rgade_item definition only\
 * @param mixed $itemdetails object or array describing the grading item, NULL if no change
 */
function grade_update($source, $courseid, $itemtype, $itemmodule, $iteminstance, $itemnumber, $grades=NULL, $itemdetails=NULL) {

    // only following grade_item properties can be changed in this function
    $allowed = array('itemname', 'idnumber', 'gradetype', 'grademax', 'grademin', 'scaleid', 'multfactor', 'plusfactor', 'deleted');

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

        debugging('Found more than one grade item');
        return GRADE_UPDATE_MULTIPLE;
    }

/// Create or update the grade_item if needed
    if (!$grade_item) {
        $params = compact('courseid', 'itemtype', 'itemmodule', 'iteminstance', 'itemnumber');
        if ($itemdetails) {
            $itemdetails = (array)$itemdetails;

            // grademin and grademax ignored when scale specified
            if (array_key_exists('scaleid', $itemdetails)) {
                if ($itemdetails['scaleid']) {
                    unset($itemdetails['grademin']);
                    unset($itemdetails['grademax']);
                }
            }

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
        $grade_item = new grade_item($params);
        $grade_item->insert();

    } else {
        if ($grade_item->is_locked()) {
            debugging('Grading item is locked!');
            return GRADE_UPDATE_ITEM_LOCKED;
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
    if (empty($grades)) {
        return GRADE_UPDATE_OK;
    }

    // no grading in deleted items
    if ($grade_item->deleted) {
        debugging('Grade item was already deleted!');
        return GRADE_UPDATE_ITEM_DELETED;
    }

/// Finally start processing of grades
    if (is_object($grades)) {
        $grades = array($grades);
    } else {
        if (array_key_exists('userid', $grades)) {
            $grades = array($grades);
        }
    }

    $failed = false;
    foreach ($grades as $grade) {
        $grade = (array)$grade;
        if (empty($grade['userid'])) {
            $failed = true;
            debugging('Invalid userid in grade submitted');
            continue;
        } else {
            $userid = $grade['userid'];
        }

        $rawgrade       = false;
        $feedback       = false;
        $feedbackformat = FORMAT_MOODLE;

        if (array_key_exists('rawgrade', $grade)) {
            $rawgrade = $grade['rawgrade'];
        }

        if (array_key_exists('feedback', $grade)) {
            $feedback = $grade['feedback'];
        }

        if (array_key_exists('feedbackformat', $grade)) {
            $feedbackformat = $grade['feedbackformat'];
        }

        // update or insert the grade
        $grade = $grade_item->update_raw_grade($userid, $rawgrade, $source, null, $feedback, $feedbackformat);

        if (!$grade) {
            $failed = true;
            continue;
        }

        // trigger grade_updated event notification
        $eventdata = new object();

        $eventdata->source            = $source;
        $eventdata->itemid            = $grade_item->id;
        $eventdata->courseid          = $grade_item->courseid;
        $eventdata->itemtype          = $grade_item->itemtype;
        $eventdata->itemmodule        = $grade_item->itemmodule;
        $eventdata->iteminstance      = $grade_item->iteminstance;
        $eventdata->itemnumber        = $grade_item->itemnumber;
        $eventdata->idnumber          = $grade_item->idnumber;
        $eventdata->userid            = $grade->userid;
        $eventdata->rawgrade        = $grade->rawgrade;

        // load existing text annotation
        if ($grade_text = $grade->load_text()) {
            $eventdata->feedback          = $grade_text->feedback;
            $eventdata->feedbackformat    = $grade_text->feedbackformat;
            $eventdata->information       = $grade_text->information;
            $eventdata->informationformat = $grade_text->informationformat;
        }

        events_trigger('grade_updated', $eventdata);
    }

    if (!$failed) {
        return GRADE_UPDATE_OK;
    } else {
        return GRADE_UPDATE_FAILED;
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
* @param int $courseid id of course
* @param string $itemtype 'mod', 'blocks', 'import', 'calculated' etc
* @param string $itemmodule 'forum, 'quiz', 'csv' etc
* @param int $iteminstance id of the item module
* @param int $itemnumber most probably 0, modules can use other numbers when having more than one grades for each user
* @param int $userid ID of the graded user
* @return boolean Whether the grade is locked or not
*/
function grade_is_locked($courseid, $itemtype, $itemmodule, $iteminstance, $itemnumber, $userid=NULL) {

    $grade_item = new grade_item(compact('courseid', 'itemtype', 'itemmodule', 'iteminstance', 'itemnumber'), false);
    if (!$grade_items = $grade_item->fetch_all_using_this()) {
        return false;

    } else if (count($grade_items) == 1){
        $grade_item = reset($grade_items);
        return $grade_item->is_locked($userid);

    } else {
        debugging('Found more than one grade item');
        foreach ($grade_items as $grade_item) {
            if ($grade_item->is_locked($userid)) {
                return true;
            }
        }
        return false;
    }
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
* @param int $itemnumber Can be used to distinguish multiple grades for an activity
* @param string $itemname The name of the grade item
* @param int $idnumber grade item Primary Key
* @return array An array of grade items
*/
function grade_get_items($courseid, $itemtype=NULL, $itemmodule=NULL, $iteminstance=NULL, $itemnumber=NULL, $itemname=NULL, $idnumber=NULL) {
    $grade_item = new grade_item(compact('courseid', 'itemtype', 'itemmodule', 'iteminstance', 'itemname', 'itemnumber', 'idnumber'), false);
    $grade_items = $grade_item->fetch_all_using_this();
    return $grade_items;
}

/**
 * Updates all final grades in course.
 *
 * @param int $courseid
 * @param boolean $regradeall force regrading of all items
 *
 * @return boolean true if ok, array of errors if problems found
 */
function grade_update_final_grades($courseid, $regradeall=false) {

    if ($regradeall) {
        set_field('grade_items', 'needsupdate', 1, 'courseid', $courseid);
    }

    $grade_item = new grade_item(array('courseid'=>$courseid), false);

    if (!$grade_items = $grade_item->fetch_all_using_this()) {
        return true;
    }

    if (!$regradeall) {
        $needsupdate = false;
        $calculated = false;
        foreach ($grade_items as $gid=>$gitem) {
            $grade_item =& $grade_items[$gid];
            if ($grade_item->needsupdate) {
                $needsupdate = true;
            }
            if ($grade_item->is_calculated()) {
                $calculated = true;
            }
        }

        if (!$needsupdate) {
            // no update needed
            return true;

        } else if ($calculated) {
            // flag all calculated grade items with needsupdate
            // we want to make sure all are ok, this can be improved later with proper dependency calculation
            foreach ($grade_items as $gid=>$gitem) {
                $grade_item =& $grade_items[$gid];
                if (!$grade_item->is_calculated()) {
                    continue;
                }
                $grade_item->update_from_db(); // make sure we have current data, it might have been updated in this loop already
                if (!$grade_item->needsupdate) {
                    //force recalculation and forced update of all parents
                    $grade_item->force_regrading();
                }
            }

            // again make sure all date is up-to-date - the needsupdate flag might have changed
            foreach ($grade_items as $gid=>$gitem) {
                $grade_item =& $grade_items[$gid];
                $grade_item->update_from_db();
                unset($grade_item->category);
            }
        }
    }


    $errors = array();

    // now the hard way with calculated grade_items or categories
    $finalitems = array();
    $finalids = array();
    while (count($grade_items) > 0) {
        $count = 0;
        foreach ($grade_items as $gid=>$gitem) {
            $grade_item =& $grade_items[$gid];
            if (!$grade_item->needsupdate) {
                $finalitems[$gid] = $grade_item;
                $finalids[] = $gid;
                unset($grade_items[$gid]);
                continue;
            }

            //do we have all data for finalizing of this item?
            $depends_on = $grade_item->depends_on();

            $doupdate = true;
            foreach ($depends_on as $did) {
                if (!in_array($did, $finalids)) {
                    $doupdate = false;
                }
            }

            //oki - let's update, calculate or aggregate :-)
            if ($doupdate) {
                $result = $grade_item->update_final_grades();
                if ($result !== true) {
                    $errors = array_merge($errors, $result);
                } else {
                    $finalitems[$gid] = $grade_item;
                    $finalids[] = $gid;
                    unset($grade_items[$gid]);
                }
            }
        }

        if ($count == 0) {
            foreach($grade_items as $grade_item) {
                $errors[] = 'Probably circular reference or broken calculation formula in grade_item id:'.$grade_item->id; // TODO: localize
            }
            break;
        }
    }

    if (count($errors) == 0) {
        return true;
    } else {
        return $errors;
    }
}

/**
 * For backwards compatibility with old third-party modules, this function can
 * be used to import all grades from activities with legacy grading.
 */
function grade_grab_legacy_grades() {

    global $CFG;

    if (!$mods = get_list_of_plugins('mod') ) {
        error('No modules installed!');
    }

    foreach ($mods as $mod) {

        if ($mod == 'NEWMODULE') {   // Someone has unzipped the template, ignore it
            continue;
        }

        if (!$module = get_record('modules', 'name', $mod)) {
            //not installed
            continue;
        }

        if (!$module->visible) {
            //disabled module
            continue;
        }

        $fullmod = $CFG->dirroot.'/mod/'.$mod;

        // include the module lib once
        if (file_exists($fullmod.'/lib.php')) {
            include_once($fullmod.'/lib.php');
            // look for modname_grades() function - old gradebook pulling function
            // if present sync the grades with new grading system
            $gradefunc = $mod.'_grades';
            if (function_exists($gradefunc)) {

                // get all instance of the activity
                $sql = "SELECT a.*, cm.idnumber as cmidnumber, m.name as modname
                          FROM {$CFG->prefix}$mod a, {$CFG->prefix}course_modules cm, {$CFG->prefix}modules m
                         WHERE m.name='$mod' AND m.id=cm.module AND cm.instance=a.id";

                if ($modinstances = get_records_sql($sql)) {
                    foreach ($modinstances as $modinstance) {
                        grade_update_mod_grades($modinstance);
                    }
                }
            }
        }
    }
}

/**
 * For testing purposes mainly, reloads grades from all non legacy modules into gradebook.
 */
function grade_grab_grades() {

    global $CFG;

    if (!$mods = get_list_of_plugins('mod') ) {
        error('No modules installed!');
    }

    foreach ($mods as $mod) {

        if ($mod == 'NEWMODULE') {   // Someone has unzipped the template, ignore it
            continue;
        }

        if (!$module = get_record('modules', 'name', $mod)) {
            //not installed
            continue;
        }

        if (!$module->visible) {
            //disabled module
            continue;
        }

        $fullmod = $CFG->dirroot.'/mod/'.$mod;

        // include the module lib once
        if (file_exists($fullmod.'/lib.php')) {
            include_once($fullmod.'/lib.php');
            // look for modname_grades() function - old gradebook pulling function
            // if present sync the grades with new grading system
            $gradefunc = $mod.'_update_grades';
            if (function_exists($gradefunc)) {
                $gradefunc();
            }
        }
    }
}

/**
 * Force full update of module grades in central gradebook - works for both legacy and converted activities.
 * @param object $modinstance object with extra cmidnumber and modname property
 * @return boolean success
 */
function grade_update_mod_grades($modinstance) {
    global $CFG;

    $fullmod = $CFG->dirroot.'/mod/'.$modinstance->modname;
    if (!file_exists($fullmod.'/lib.php')) {
        debugging('missing lib.php file in module');
        return false;
    }
    include_once($fullmod.'/lib.php');

    // does it use legacy grading?
    $gradefunc        = $modinstance->modname.'_grades';
    $updategradesfunc = $modinstance->modname.'_update_grades';
    $updateitemfunc   = $modinstance->modname.'_grade_item_update';

    if (function_exists($gradefunc)) {
        if ($oldgrades = $gradefunc($modinstance->id)) {

            $grademax = $oldgrades->maxgrade;
            $scaleid = NULL;
            if (!is_numeric($grademax)) {
                // scale name is provided as a string, try to find it
                if (!$scale = get_record('scale', 'name', $grademax)) {
                    debugging('Incorrect scale name! name:'.$grademax);
                    return false;
                }
                $scaleid = $scale->id;
            }

            if (!$grade_item = grade_get_legacy_grade_item($modinstance, $grademax, $scaleid)) {
                debugging('Can not get/create legacy grade item!');
                return false;
            }

            $grades = array();
            foreach ($oldgrades->grades as $userid=>$usergrade) {
                $grade = new object();
                $grade->userid = $userid;

                if ($usergrade == '-') {
                    // no grade
                    $grade->rawgrade = null;

                } else if ($scaleid) {
                    // scale in use, words used
                    $gradescale = explode(",", $scale->scale);
                    $grade->rawgrade = array_search($usergrade, $gradescale) + 1;

                } else {
                    // good old numeric value
                    $grade->rawgrade = $usergrade;
                }
                $grades[] = $grade;
            }

            grade_update('legacygrab', $grade_item->courseid, $grade_item->itemtype, $grade_item->itemmodule,
                         $grade_item->iteminstance, $grade_item->itemnumber, $grades);
        }

    } else if (function_exists($updategradesfunc) and function_exists($updateitemfunc)) {
        //new grading supported, force updating of grades
        $updateitemfunc($modinstance);
        $updategradesfunc($modinstance);

    } else {
        // mudule does not support grading
    }

    return true;
}

/**
 * Get and update/create grade item for legacy modules.
 */
function grade_get_legacy_grade_item($modinstance, $grademax, $scaleid) {

    // does it already exist?
    if ($grade_items = grade_get_items($modinstance->course, 'mod', $modinstance->modname, $modinstance->id, 0)) {
        if (count($grade_items) > 1) {
            debugging('Multiple legacy grade_items found.');
            return false;
        }

        $grade_item = reset($grade_items);

        if (is_null($grademax) and is_null($scaleid)) {
           $grade_item->gradetype  = GRADE_TYPE_NONE;

        } else if ($scaleid) {
            $grade_item->gradetype = GRADE_TYPE_SCALE;
            $grade_item->scaleid   = $scaleid;

        } else {
           $grade_item->gradetype  = GRADE_TYPE_VALUE;
           $grade_item->grademax   = $grademax;
           $grade_item->grademin   = 0;
        }

        $grade_item->itemname = $modinstance->name;
        $grade_item->idnumber = $modinstance->cmidnumber;

        $grade_item->update();

        return $grade_item;
    }

    // create new one
    $params = array('courseid'    =>$modinstance->course,
                    'itemtype'    =>'mod',
                    'itemmodule'  =>$modinstance->modname,
                    'iteminstance'=>$modinstance->id,
                    'itemnumber'  =>0,
                    'itemname'    =>$modinstance->name,
                    'idnumber'    =>$modinstance->cmidnumber);

    if (is_null($grademax) and is_null($scaleid)) {
        $params['gradetype'] = GRADE_TYPE_NONE;

    } else if ($scaleid) {
        $params['gradetype'] = GRADE_TYPE_SCALE;
        $params['scaleid']   = $scaleid;

    } else {
        $params['gradetype'] = GRADE_TYPE_VALUE;
        $params['grademax']  = $grademax;
        $params['grademin']  = 0;
    }

    $grade_item = new grade_item($params);
    $grade_item->insert();

    return $grade_item;
}

/**
 * This function is used to migrade old date and settings from old gradebook into new grading system.
 *
 * TODO:
 *   - category weight not used - we would have to create extra top course grade calculated category
 *   - exta_credit item flag not used - does not fit all our aggregation types, could be used in SUM only
 */
function grade_oldgradebook_upgrade($courseid) {
    global $CFG;

    $categories = array();
    if ($oldcats = get_records('grade_category', 'courseid', $courseid)) {
        foreach ($oldcats as $oldcat) {
            $newcat = new grade_category(array('courseid'=>$courseid, 'fullname'=>$oldcat->name));
            $newcat->droplow     = $oldcat->drop_x_lowest;
            $newcat->aggregation = GRADE_AGGREGATE_MEAN_GRADED;

            if (empty($newcat->id)) {
                $newcat->insert();
            } else {
                $newcat->update();
            }

            $categories[$oldcat->id] = $newcat;

            $catitem = $newcat->get_grade_item();
            $catitem->gradetype  = GRADE_TYPE_VALUE;
            $catitem->plusfactor = $oldcat->bonus_points;
            $catitem->hidden     = $oldcat->hidden;
            $catitem->update();
        }
    }

    // get all grade items with mod details
    $sql = "SELECT gi.*, cm.idnumber as cmidnumber, m.name as modname
              FROM {$CFG->prefix}grade_item gi, {$CFG->prefix}course_modules cm, {$CFG->prefix}modules m
             WHERE gi.courseid=$courseid AND m.id=gi.modid AND cm.instance=gi.cminstance
          ORDER BY gi.sortorder ASC";

    if ($olditems = get_records_sql($sql)) {
        foreach ($olditems as $olditem) {
            $newitem = new grade_item(array('courseid'=>$olditem->courseid, 'itemtype'=>'mod', 'itemmodule'=>$olditem->modname, 'iteminstance'=>$olditem->cminstance, 'itemnumber'=>0));
            if (!empty($olditem->category)) {
                // we do this low level stuff to get some speedup during upgrade
                $newitem->set_parent_id($categories[$olditem->category]->id);
            }
            $newitem->gradetype = GRADE_TYPE_NONE;
            $newitem->multfactor = $olditem->scale_grade;
            if (empty($newitem->id)) {
                $newitem->insert();
            } else {
                $newitem->update();
            }
        }
    }
}

?>