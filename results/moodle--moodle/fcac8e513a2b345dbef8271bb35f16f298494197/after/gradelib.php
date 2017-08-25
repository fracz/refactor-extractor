<?php // $Id$

///////////////////////////////////////////////////////////////////////////
// NOTICE OF COPYRIGHT                                                   //
//                                                                       //
// Moodle - Modular Object-Oriented Dynamic Learning Environment         //
//          http://moodle.org                                            //
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

/**
 * Library of functions for gradebook
 *
 * @author Moodle HQ developers
 * @version  $Id$
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package moodlecore
 */

require_once($CFG->libdir . '/grade/constants.php');

require_once($CFG->libdir . '/grade/grade_category.php');
require_once($CFG->libdir . '/grade/grade_item.php');
require_once($CFG->libdir . '/grade/grade_grade.php');
require_once($CFG->libdir . '/grade/grade_scale.php');
require_once($CFG->libdir . '/grade/grade_outcome.php');

/***** PUBLIC GRADE API - only these functions should be used in modules *****/

/**
 * Submit new or update grade; update/create grade_item definition. Grade must have userid specified,
 * rawgrade and feedback with format are optional. rawgrade NULL means 'Not graded', missing property
 * or key means do not change existing.
 *
 * Only following grade item properties can be changed 'itemname', 'idnumber', 'gradetype', 'grademax',
 * 'grademin', 'scaleid', 'multfactor', 'plusfactor', 'deleted'.
 *
 * Manual, course or category items can not be updated by this function.

 * @param string $source source of the grade such as 'mod/assignment'
 * @param int $courseid id of course
 * @param string $itemtype type of grade item - mod, block
 * @param string $itemmodule more specific then $itemtype - assignment, forum, etc.; maybe NULL for some item types
 * @param int $iteminstance instance it of graded subject
 * @param int $itemnumber most probably 0, modules can use other numbers when having more than one grades for each user
 * @param mixed $grades grade (object, array) or several grades (arrays of arrays or objects), NULL if updating grade_item definition only
 * @param mixed $itemdetails object or array describing the grading item, NULL if no change
 */
function grade_update($source, $courseid, $itemtype, $itemmodule, $iteminstance, $itemnumber, $grades=NULL, $itemdetails=NULL) {
    global $USER;

    // only following grade_item properties can be changed in this function
    $allowed = array('itemname', 'idnumber', 'gradetype', 'grademax', 'grademin', 'scaleid', 'multfactor', 'plusfactor', 'deleted');

    // grade item identification
    $params = compact('courseid', 'itemtype', 'itemmodule', 'iteminstance', 'itemnumber');

    if (is_null($courseid) or is_null($itemtype)) {
        debugging('Missing courseid or itemtype');
        return GRADE_UPDATE_FAILED;
    }

    if (!$grade_items = grade_item::fetch_all($params)) {
        // create a new one
        $grade_item = false;

    } else if (count($grade_items) == 1){
        $grade_item = reset($grade_items);
        unset($grade_items); //release memory

    } else {
        debugging('Found more than one grade item');
        return GRADE_UPDATE_MULTIPLE;
    }

    if (!empty($itemdetails['deleted'])) {
        if ($grade_item) {
            if ($grade_item->delete($source)) {
                return GRADE_UPDATE_OK;
            } else {
                return GRADE_UPDATE_FAILED;
            }
        }
        return GRADE_UPDATE_OK;
    }

/// Create or update the grade_item if needed

    if (!$grade_item) {
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
            $message = get_string('gradeitemislocked', 'grades', $grade_item->itemname);
            notice($message);
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

        if (array_key_exists('usermodified', $grade)) {
            $usermodified = $grade['usermodified'];
        } else {
            $usermodified = $USER->id;
        }

        // update or insert the grade
        if (!$grade_item->update_raw_grade($userid, $rawgrade, $source, null, $feedback, $feedbackformat, $usermodified)) {
            $failed = true;
        }
    }

    if (!$failed) {
        return GRADE_UPDATE_OK;
    } else {
        return GRADE_UPDATE_FAILED;
    }
}

/**
 * Updates outcomes of user
 * Manual outcomes can not be updated.
 * @param string $source source of the grade such as 'mod/assignment'
 * @param int $courseid id of course
 * @param string $itemtype 'mod', 'block'
 * @param string $itemmodule 'forum, 'quiz', etc.
 * @param int $iteminstance id of the item module
 * @param int $userid ID of the graded user
 * @param array $data array itemnumber=>outcomegrade
 */
function grade_update_outcomes($source, $courseid, $itemtype, $itemmodule, $iteminstance, $userid, $data) {
    if ($items = grade_item::fetch_all(array('itemtype'=>$itemtype, 'itemmodule'=>$itemmodule, 'iteminstance'=>$iteminstance, 'courseid'=>$courseid))) {
        foreach ($items as $item) {
            if (!array_key_exists($item->itemnumber, $data)) {
                continue;
            }
            $grade = $data[$item->itemnumber] < 1 ? null : $data[$item->itemnumber];
            $item->update_final_grade($userid, $grade, $source);
        }
    }
}

/**
 * Returns grading information for given activity - optionally with users grades
 * Manual, course or category items can not be queried.
 * @param int $courseid id of course
 * @param string $itemtype 'mod', 'block'
 * @param string $itemmodule 'forum, 'quiz', etc.
 * @param int $iteminstance id of the item module
 * @param int $userid optional id of the graded user; if userid not used, returns only information about grade_item
 * @return array of grade information objects (scaleid, name, grade and locked status, etc.) indexed with itemnumbers
 */
function grade_get_grades($courseid, $itemtype, $itemmodule, $iteminstance, $userid_or_ids=0) {
    $return = new object();
    $return->items    = array();
    $return->outcomes = array();

    $course_item = grade_item::fetch_course_item($courseid);
    $needsupdate = array();
    if ($course_item->needsupdate) {
        $result = grade_regrade_final_grades($courseid);
        if ($result !== true) {
            $needsupdate = array_keys($result);
        }
    }

    if ($grade_items = grade_item::fetch_all(array('itemtype'=>$itemtype, 'itemmodule'=>$itemmodule, 'iteminstance'=>$iteminstance, 'courseid'=>$courseid))) {
        foreach ($grade_items as $grade_item) {
            if (empty($grade_item->outcomeid)) {
                // prepare information about grade item
                $item = new object();
                $item->itemnumber = $grade_item->itemnumber;
                $item->scaleid    = $grade_item->scaleid;
                $item->name       = $grade_item->get_name();
                $item->grademin   = $grade_item->grademin;
                $item->grademax   = $grade_item->grademax;
                $item->gradepass  = $grade_item->gradepass;
                $item->locked     = $grade_item->is_locked();
                $item->hidden     = $grade_item->is_hidden();
                $item->grades     = array();

                switch ($grade_item->gradetype) {
                    case GRADE_TYPE_NONE:
                        continue;

                    case GRADE_TYPE_VALUE:
                        $item->scaleid = 0;
                        break;

                    case GRADE_TYPE_TEXT:
                        $item->scaleid   = 0;
                        $item->grademin   = 0;
                        $item->grademax   = 0;
                        $item->gradepass  = 0;
                        break;
                }

                if (empty($userid_or_ids)) {
                    $userids = array();

                } else if (is_array($userid_or_ids)) {
                    $userids = $userid_or_ids;

                } else {
                    $userids = array($userid_or_ids);
                }

                if ($userids) {
                    $grade_grades = grade_grade::fetch_users_grades($grade_item, $userids, true);
                    foreach ($userids as $userid) {
                        $grade_grades[$userid]->grade_item =& $grade_item;

                        $grade = new object();
                        $grade->grade          = $grade_grades[$userid]->finalgrade;
                        $grade->locked         = $grade_grades[$userid]->is_locked();
                        $grade->hidden         = $grade_grades[$userid]->is_hidden();
                        $grade->overridden     = $grade_grades[$userid]->overridden;
                        $grade->feedback       = $grade_grades[$userid]->feedback;
                        $grade->feedbackformat = $grade_grades[$userid]->feedbackformat;

                        // create text representation of grade
                        if (in_array($grade_item->id, $needsupdate)) {
                            $grade->grade     = false;
                            $grade->str_grade = get_string('error');

                        } else if (is_null($grade->grade)) {
                            $grade->str_grade = get_string('nograde');

                        } else {
                            switch ($grade_item->gradetype) {
                                case GRADE_TYPE_VALUE:
                                    $grade->str_grade = $grade->grade; //TODO: fix localisation and decimal places
                                    break;

                                case GRADE_TYPE_SCALE:
                                    $scale = $grade_item->load_scale();
                                    $grade->str_grade = format_string($scale->scale_items[$grade->grade-1]);
                                    break;

                                case GRADE_TYPE_TEXT:
                                default:
                                    $grade->str_grade = '';
                            }
                        }

                        // create html representation of feedback
                        if (is_null($grade->feedback)) {
                            $grade->str_feedback = '';
                        } else {
                            $grade->str_feedback = format_text($grade->feedback, $grade->feedbackformat);
                        }

                        $item->grades[$userid] = $grade;
                    }
                }
                $return->items[$grade_item->itemnumber] = $item;

            } else {
                if (!$grade_outcome = grade_outcome::fetch(array('id'=>$grade_item->outcomeid))) {
                    debugging('Incorect outcomeid found');
                    continue;
                }

                // outcome info
                $outcome = new object();
                $outcome->itemnumber = $grade_item->itemnumber;
                $outcome->scaleid    = $grade_outcome->scaleid;
                $outcome->name       = $grade_outcome->get_name();
                $outcome->locked     = $grade_item->is_locked();
                $outcome->hidden     = $grade_item->is_hidden();

                if (empty($userid_or_ids)) {
                    $userids = array();
                } else if (is_array($userid_or_ids)) {
                    $userids = $userid_or_ids;
                } else {
                    $userids = array($userid_or_ids);
                }

                if ($userids) {
                    $grade_grades = grade_grade::fetch_users_grades($grade_item, $userids, true);
                    foreach ($userids as $userid) {
                        $grade_grades[$userid]->grade_item =& $grade_item;

                        $grade = new object();
                        $grade->grade          = $grade_grades[$userid]->finalgrade;
                        $grade->locked         = $grade_grades[$userid]->is_locked();
                        $grade->hidden         = $grade_grades[$userid]->is_hidden();
                        $grade->feedback       = $grade_grades[$userid]->feedback;
                        $grade->feedbackformat = $grade_grades[$userid]->feedbackformat;

                        // create text representation of grade
                        if (in_array($grade_item->id, $needsupdate)) {
                            $grade->grade     = false;
                            $grade->str_grade = get_string('error');

                        } else if (is_null($grade->grade)) {
                            $grade->grade = 0;
                            $grade->str_grade = get_string('nooutcome', 'grades');

                        } else {
                            $grade->grade = (int)$grade->grade;
                            $scale = $grade_item->load_scale();
                            $grade->str_grade = format_string($scale->scale_items[(int)$grade->grade-1]);
                        }

                        // create html representation of feedback
                        if (is_null($grade->feedback)) {
                            $grade->str_feedback = '';
                        } else {
                            $grade->str_feedback = format_text($grade->feedback, $grade->feedbackformat);
                        }

                        $outcome->grades[$userid] = $grade;
                    }
                }
                $return->outcomes[$grade_item->itemnumber] = $outcome;

            }
        }
    }

    // sort results using itemnumbers
    ksort($return->items, SORT_NUMERIC);
    ksort($return->outcomes, SORT_NUMERIC);

    return $return;
}

/***** END OF PUBLIC API *****/


/**
 * Verify new value of idnumber - checks for uniqueness of new idnumbers, old are kept intact
 * @param string idnumber string (with magic quotes)
 * @param object $cm used for course module idnumbers and items attached to modules
 * @param object $gradeitem is item idnumber
 * @return boolean true means idnumber ok
 */
function grade_verify_idnumber($idnumber, $grade_item=null, $cm=null) {
    if ($idnumber == '') {
        //we allow empty idnumbers
        return true;
    }

    // keep existing even when not unique
    if ($cm and $cm->idnumber == $idnumber) {
        return true;
    } else if ($grade_item and $grade_item->idnumber == $idnumber) {
        return true;
    }

    if (get_records('course_modules', 'idnumber', $idnumber)) {
        return false;
    }

    if (get_records('grade_items', 'idnumber', $idnumber)) {
        return false;
    }

    return true;
}

/**
 * Force final grade recalculation in all course items
 * @param int $courseid
 */
function grade_force_full_regrading($courseid) {
    set_field('grade_items', 'needsupdate', 1, 'courseid', $courseid);
}

/**
 * Updates all final grades in course.
 *
 * @param int $courseid
 * @param int $userid if specified, try to do a quick regrading of grades of this user only
 * @param object $updated_item the item in which
 * @return boolean true if ok, array of errors if problems found (item id is used as key)
 */
function grade_regrade_final_grades($courseid, $userid=null, $updated_item=null) {

    $course_item = grade_item::fetch_course_item($courseid);

    if ($userid) {
        // one raw grade updated for one user
        if (empty($updated_item)) {
            error("updated_item_id can not be null!");
        }
        if ($course_item->needsupdate) {
            $updated_item->force_regrading();
            return 'Can not do fast regrading after updating of raw grades';
        }

    } else {
        if (!$course_item->needsupdate) {
            // nothing to do :-)
            return true;
        }
    }

    $grade_items = grade_item::fetch_all(array('courseid'=>$courseid));
    $depends_on = array();

    // first mark all category and calculated items as needing regrading
    // this is slower, but 100% accurate
    foreach ($grade_items as $gid=>$gitem) {
        if (!empty($updated_item) and $updated_item->id == $gid) {
            $grade_items[$gid]->needsupdate = 1;

        } else if ($gitem->is_course_item() or $gitem->is_category_item() or $gitem->is_calculated()) {
            $grade_items[$gid]->needsupdate = 1;
        }

        // construct depends_on lookup array
        $depends_on[$gid] = $grade_items[$gid]->depends_on();
    }

    $errors = array();
    $finalids = array();
    $gids     = array_keys($grade_items);
    $failed = 0;

    while (count($finalids) < count($gids)) { // work until all grades are final or error found
        $count = 0;
        foreach ($gids as $gid) {
            if (in_array($gid, $finalids)) {
                continue; // already final
            }

            if (!$grade_items[$gid]->needsupdate) {
                $finalids[] = $gid; // we can make it final - does not need update
                continue;
            }

            $doupdate = true;
            foreach ($depends_on[$gid] as $did) {
                if (!in_array($did, $finalids)) {
                    $doupdate = false;
                    continue; // this item depends on something that is not yet in finals array
                }
            }

            //oki - let's update, calculate or aggregate :-)
            if ($doupdate) {
                $result = $grade_items[$gid]->regrade_final_grades($userid);

                if ($result === true) {
                    $grade_items[$gid]->regrading_finished();
                    $grade_items[$gid]->check_locktime(); // do the locktime item locking
                    $count++;
                    $finalids[] = $gid;

                } else {
                    $grade_items[$gid]->force_regrading();
                    $errors[$gid] = $result;
                }
            }
        }

        if ($count == 0) {
            $failed++;
        } else {
            $failed = 0;
        }

        if ($failed > 1) {
            foreach($gids as $gid) {
                if (in_array($gid, $finalids)) {
                    continue; // this one is ok
                }
                $grade_items[$gid]->force_regrading();
                $errors[$grade_items[$gid]->id] = 'Probably circular reference or broken calculation formula'; // TODO: localize
            }
            break; // oki, found error
        }
    }

    if (count($errors) == 0) {
        if (empty($userid)) {
            // do the locktime locking of grades, but only when doing full regrading
            grade_grade::check_locktime_all($gids);
        }
        return true;
    } else {
        return $errors;
    }
}

/**
 * For backwards compatibility with old third-party modules, this function can
 * be used to import all grades from activities with legacy grading.
 * @param int $courseid or null if all courses
 */
function grade_grab_legacy_grades($courseid=null) {

    global $CFG;

    if (!$mods = get_list_of_plugins('mod') ) {
        error('No modules installed!');
    }

    if ($courseid) {
        $course_sql = " AND cm.course=$courseid";
    } else {
        $course_sql = "";
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
                         WHERE m.name='$mod' AND m.id=cm.module AND cm.instance=a.id $course_sql";

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
function grade_update_mod_grades($modinstance, $userid=0) {
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

        // legacy module - not yet converted
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
            foreach ($oldgrades->grades as $uid=>$usergrade) {
                if ($userid and $uid != $userid) {
                    continue;
                }
                $grade = new object();
                $grade->userid = $uid;

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
        $updategradesfunc($modinstance, $userid);

    } else {
        // mudule does not support grading??
    }

    return true;
}

/**
 * Get and update/create grade item for legacy modules.
 */
function grade_get_legacy_grade_item($modinstance, $grademax, $scaleid) {

    // does it already exist?
    if ($grade_items = grade_item::fetch_all(array('courseid'=>$modinstance->course, 'itemtype'=>'mod', 'itemmodule'=>$modinstance->modname, 'iteminstance'=>$modinstance->id, 'itemnumber'=>0))) {
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
            $grade_item->grademin  = 1;

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
        $grade_item->grademin  = 1;
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
 * Remove all grade related course data - history is kept
 * @param int $courseid
 * @param bool @showfeedback print feedback
 */
function remove_course_grades($courseid, $showfeedback) {
    $strdeleted = get_string('deleted');

    $course_category = grade_category::fetch_course_category($courseid);
    $course_category->delete('coursedelete');
    if ($showfeedback) {
        notify($strdeleted.' - '.get_string('grades', 'grades').', '.get_string('items', 'grades').', '.get_string('categories', 'grades'));
    }

    if ($outcomes = grade_outcome::fetch_all(array('courseid'=>$courseid))) {
        foreach ($outcomes as $outcome) {
            $outcome->delete('coursedelete');
        }
    }
    delete_records('grade_outcomes_courses', 'courseid', $courseid);
    if ($showfeedback) {
        notify($strdeleted.' - '.get_string('outcomes', 'grades'));
    }

    if ($scales = grade_scale::fetch_all(array('courseid'=>$courseid))) {
        foreach ($scales as $scale) {
            $scale->delete('coursedelete');
        }
    }
    if ($showfeedback) {
        notify($strdeleted.' - '.get_string('scales'));
    }
}

/**
 * Builds an array of percentages indexed by integers for the purpose of building a select drop-down element.
 * @param int $steps The value between each level.
 * @param string $order 'asc' for 0-100 and 'desc' for 100-0
 * @param int $lowest The lowest value to include
 * @param int $highest The highest value to include
 */
function build_percentages_array($steps=1, $order='desc', $lowest=0, $highest=100) {
    // TODO reject or implement
}

/**
 * Grading cron job
 */
function grade_cron() {
    global $CFG;

    $now = time();

    $sql = "SELECT i.*
              FROM {$CFG->prefix}grade_items i
             WHERE i.locked = 0 AND i.locktime > 0 AND i.locktime < $now AND EXISTS (
                SELECT 'x' FROM {$CFG->prefix}grade_items c WHERE c.itemtype='course' AND c.needsupdate=0 AND c.courseid=i.courseid)";

    // go through all courses that have proper final grades and lock them if needed
    if ($rs = get_recordset_sql($sql)) {
        if ($rs->RecordCount() > 0) {
            while ($item = rs_fetch_next_record($rs)) {
                $grade_item = new grade_item($item, false);
                $grade_item->locked = $now;
                $grade_item->update('locktime');
            }
        }
        rs_close($rs);
    }

    $grade_inst = new grade_grade();
    $fields = 'g.'.implode(',g.', $grade_inst->required_fields);

    $sql = "SELECT $fields
              FROM {$CFG->prefix}grade_grades g, {$CFG->prefix}grade_items i
             WHERE g.locked = 0 AND g.locktime > 0 AND g.locktime < $now AND g.itemid=i.id AND EXISTS (
                SELECT 'x' FROM {$CFG->prefix}grade_items c WHERE c.itemtype='course' AND c.needsupdate=0 AND c.courseid=i.courseid)";

    // go through all courses that have proper final grades and lock them if needed
    if ($rs = get_recordset_sql($sql)) {
        if ($rs->RecordCount() > 0) {
            while ($grade = rs_fetch_next_record($rs)) {
                $grade_grade = new grade_grade($grade, false);
                $grade_grade->locked = $now;
                $grade_grade->update('locktime');
            }
        }
        rs_close($rs);
    }

}

?>