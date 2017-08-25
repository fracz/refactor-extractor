<?php // $Id$

///////////////////////////////////////////////////////////////////////////
//                                                                       //
// NOTICE OF COPYRIGHT                                                   //
//                                                                       //
// Moodle - Modular Object-Oriented Dynamic Learning Environment         //
//          http://moodle.org                                            //
//                                                                       //
// Copyright (C) 1999 onwards Martin Dougiamas, Moodle  http://moodle.com//
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
 * deprecatedlib.php - Old functions retained only for backward compatibility
 *
 * Old functions retained only for backward compatibility.  New code should not
 * use any of these functions.
 *
 * @author Martin Dougiamas
 * @version $Id$
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package moodlecore
 */


/**
 * Determines if a user an admin
 *
 * @uses $USER
 * @param int $userid The id of the user as is found in the 'user' table
 * @staticvar array $admins List of users who have been found to be admins by user id
 * @staticvar array $nonadmins List of users who have been found not to be admins by user id
 * @return bool
 */
function isadmin($userid=0) {
    global $USER, $CFG;

    if (empty($CFG->rolesactive)) {    // Then the user is likely to be upgrading NOW
        if (!$userid) {
            if (empty($USER->id)) {
                return false;
            }
            if (!empty($USER->admin)) {
                return true;
            }
            $userid = $USER->id;
        }

        return record_exists('user_admins', 'userid', $userid);
    }

    $context = get_context_instance(CONTEXT_SYSTEM);

    return has_capability('moodle/legacy:admin', $context, $userid, false);
}

/**
 * Determines if a user is a teacher (or better)
 *
 * @uses $CFG
 * @param int $courseid The id of the course that is being viewed, if any
 * @param int $userid The id of the user that is being tested against. Set this to 0 if you would just like to test against the currently logged in user.
 * @param bool $obsolete_includeadmin Not used any more
 * @return bool
 */

function isteacher($courseid=0, $userid=0, $obsolete_includeadmin=true) {
/// Is the user able to access this course as a teacher?
    global $CFG;

    if (empty($CFG->rolesactive)) {     // Teachers are locked out during an upgrade to 1.7
        return false;
    }

    if ($courseid) {
        $context = get_context_instance(CONTEXT_COURSE, $courseid);
    } else {
        $context = get_context_instance(CONTEXT_SYSTEM);
    }

    return (has_capability('moodle/legacy:teacher', $context, $userid, false)
         or has_capability('moodle/legacy:editingteacher', $context, $userid, false)
         or has_capability('moodle/legacy:admin', $context, $userid, false));
}

/**
 * Determines if a user is a teacher in any course, or an admin
 *
 * @uses $USER
 * @param int $userid The id of the user that is being tested against. Set this to 0 if you would just like to test against the currently logged in user.
 * @param bool $includeadmin Include anyone wo is an admin as well
 * @return bool
 */
function isteacherinanycourse($userid=0, $includeadmin=true) {
    global $USER, $CFG;

    if (empty($CFG->rolesactive)) {     // Teachers are locked out during an upgrade to 1.7
        return false;
    }

    if (!$userid) {
        if (empty($USER->id)) {
            return false;
        }
        $userid = $USER->id;
    }

    if (!record_exists('role_assignments', 'userid', $userid)) {    // Has no roles anywhere
        return false;
    }

/// If this user is assigned as an editing teacher anywhere then return true
    if ($roles = get_roles_with_capability('moodle/legacy:editingteacher', CAP_ALLOW)) {
        foreach ($roles as $role) {
            if (record_exists('role_assignments', 'roleid', $role->id, 'userid', $userid)) {
                return true;
            }
        }
    }

/// If this user is assigned as a non-editing teacher anywhere then return true
    if ($roles = get_roles_with_capability('moodle/legacy:teacher', CAP_ALLOW)) {
        foreach ($roles as $role) {
            if (record_exists('role_assignments', 'roleid', $role->id, 'userid', $userid)) {
                return true;
            }
        }
    }

/// Include admins if required
    if ($includeadmin) {
        $context = get_context_instance(CONTEXT_SYSTEM);
        if (has_capability('moodle/legacy:admin', $context, $userid, false)) {
            return true;
        }
    }

    return false;
}

/**
 * Determines if a user can create new courses
 *
 * @param int $userid The user being tested. You can set this to 0 or leave it blank to test the currently logged in user.
 * @return bool
 */
function iscreator ($userid=0) {
    global $CFG;

    if (empty($CFG->rolesactive)) {
        return false;
    }

    $context = get_context_instance(CONTEXT_SYSTEM);

    return (has_capability('moodle/legacy:coursecreator', $context, $userid, false)
         or has_capability('moodle/legacy:admin', $context, $userid, false));
}


/**
 * Determines if the specified user is logged in as guest.
 *
 * @param int $userid The user being tested. You can set this to 0 or leave it blank to test the currently logged in user.
 * @return bool
 */
function isguest($userid=0) {
    global $CFG;

    if (empty($CFG->rolesactive)) {
        return false;
    }

    $context = get_context_instance(CONTEXT_SYSTEM);

    return has_capability('moodle/legacy:guest', $context, $userid, false);
}


/**
 * Get the guest user information from the database
 *
 * @return object(user) An associative array with the details of the guest user account.
 * @todo Is object(user) a correct return type? Or is array the proper return type with a note that the contents include all details for a user.
 */
function get_guest() {
    return get_complete_user_data('username', 'guest');
}

/**
 * Returns $user object of the main teacher for a course
 *
 * @uses $CFG
 * @param int $courseid The course in question.
 * @return user|false  A {@link $USER} record of the main teacher for the specified course or false if error.
 * @todo Finish documenting this function
 */
function get_teacher($courseid) {

    global $CFG;

    $context = get_context_instance(CONTEXT_COURSE, $courseid);

    // Pass $view=true to filter hidden caps if the user cannot see them
    if ($users = get_users_by_capability($context, 'moodle/course:update', 'u.*', 'u.id ASC',
                                         '', '', '', '', false, true)) {
        $users = sort_by_roleassignment_authority($users, $context);
        return array_shift($users);
    }

    return false;
}

/**
 * Searches logs to find all enrolments since a certain date
 *
 * used to print recent activity
 *
 * @uses $CFG
 * @param int $courseid The course in question.
 * @return object|false  {@link $USER} records or false if error.
 * @todo Finish documenting this function
 */
function get_recent_enrolments($courseid, $timestart) {

    global $CFG;

    $context = get_context_instance(CONTEXT_COURSE, $courseid);

    return get_records_sql("SELECT DISTINCT u.id, u.firstname, u.lastname, l.time
                            FROM {$CFG->prefix}user u,
                                 {$CFG->prefix}role_assignments ra,
                                 {$CFG->prefix}log l
                            WHERE l.time > '$timestart'
                              AND l.course = '$courseid'
                              AND l.module = 'course'
                              AND l.action = 'enrol'
                              AND l.info = u.id
                              AND u.id = ra.userid
                              AND ra.contextid ".get_related_contexts_string($context)."
                              ORDER BY l.time ASC");
}

/**
 * Returns array of userinfo of all students in this course
 * or on this site if courseid is id of site
 *
 * @uses $CFG
 * @uses SITEID
 * @param int $courseid The course in question.
 * @param string $sort ?
 * @param string $dir ?
 * @param int $page ?
 * @param int $recordsperpage ?
 * @param string $firstinitial ?
 * @param string $lastinitial ?
 * @param ? $group ?
 * @param string $search ?
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @param string $exceptions ?
 * @return object
 * @todo Finish documenting this function
 */
function get_course_students($courseid, $sort='ul.timeaccess', $dir='', $page='', $recordsperpage='',
                             $firstinitial='', $lastinitial='', $group=NULL, $search='', $fields='', $exceptions='') {

    global $CFG;

    // make sure it works on the site course
    $context = get_context_instance(CONTEXT_COURSE, $courseid);

    /// For the site course, old way was to check if $CFG->allusersaresitestudents was set to true.
    /// The closest comparible method using roles is if the $CFG->defaultuserroleid is set to the legacy
    /// student role. This function should be replaced where it is used with something more meaningful.
    if (($courseid == SITEID) && !empty($CFG->defaultuserroleid) && empty($CFG->nodefaultuserrolelists)) {
        if ($roles = get_roles_with_capability('moodle/legacy:student', CAP_ALLOW, $context)) {
            $hascap = false;
            foreach ($roles as $role) {
                if ($role->id == $CFG->defaultuserroleid) {
                    $hascap = true;
                    break;
                }
            }
            if ($hascap) {
                // return users with confirmed, undeleted accounts who are not site teachers
                // the following is a mess because of different conventions in the different user functions
                $sort = str_replace('s.timeaccess', 'lastaccess', $sort); // site users can't be sorted by timeaccess
                $sort = str_replace('timeaccess', 'lastaccess', $sort); // site users can't be sorted by timeaccess
                $sort = str_replace('u.', '', $sort); // the get_user function doesn't use the u. prefix to fields
                $fields = str_replace('u.', '', $fields);
                if ($sort) {
                    $sort = $sort .' '. $dir;
                }
                // Now we have to make sure site teachers are excluded

                if ($teachers = get_course_teachers(SITEID)) {
                    foreach ($teachers as $teacher) {
                        $exceptions .= ','. $teacher->userid;
                    }
                    $exceptions = ltrim($exceptions, ',');

                }

                return get_users(true, $search, true, $exceptions, $sort, $firstinitial, $lastinitial,
                                  $page, $recordsperpage, $fields ? $fields : '*');
            }
        }
    }

    $LIKE      = sql_ilike();
    $fullname  = sql_fullname('u.firstname','u.lastname');

    $groupmembers = '';

    $select = "c.contextlevel=".CONTEXT_COURSE." AND "; // Must be on a course
    if ($courseid != SITEID) {
        // If not site, require specific course
        $select.= "c.instanceid=$courseid AND ";
    }
    $select.="rc.capability='moodle/legacy:student' AND rc.permission=".CAP_ALLOW." AND ";

    $select .= ' u.deleted = \'0\' ';

    if (!$fields) {
        $fields = 'u.id, u.confirmed, u.username, u.firstname, u.lastname, '.
                  'u.maildisplay, u.mailformat, u.maildigest, u.email, u.city, '.
                  'u.country, u.picture, u.idnumber, u.department, u.institution, '.
                  'u.emailstop, u.lang, u.timezone, ul.timeaccess as lastaccess';
    }

    if ($search) {
        $search = ' AND ('. $fullname .' '. $LIKE .'\'%'. $search .'%\' OR email '. $LIKE .'\'%'. $search .'%\') ';
    }

    if ($firstinitial) {
        $select .= ' AND u.firstname '. $LIKE .'\''. $firstinitial .'%\' ';
    }

    if ($lastinitial) {
        $select .= ' AND u.lastname '. $LIKE .'\''. $lastinitial .'%\' ';
    }

    if ($group === 0) {   /// Need something here to get all students not in a group
        return array();

    } else if ($group !== NULL) {
        $groupmembers = "INNER JOIN {$CFG->prefix}groups_members gm on u.id=gm.userid";
        $select .= ' AND gm.groupid = \''. $group .'\'';
    }

    if (!empty($exceptions)) {
        $select .= ' AND u.id NOT IN ('. $exceptions .')';
    }

    if ($sort) {
        $sort = ' ORDER BY '. $sort .' ';
    }

    $students = get_records_sql("SELECT $fields
                                FROM {$CFG->prefix}user u INNER JOIN
                                     {$CFG->prefix}role_assignments ra on u.id=ra.userid INNER JOIN
                                     {$CFG->prefix}role_capabilities rc ON ra.roleid=rc.roleid INNER JOIN
                                     {$CFG->prefix}context c ON c.id=ra.contextid LEFT OUTER JOIN
                                     {$CFG->prefix}user_lastaccess ul on ul.userid=ra.userid
                                     $groupmembers
                                WHERE $select $search $sort $dir", $page, $recordsperpage);

    return $students;
}


/**
 * Returns list of all teachers in this course
 *
 * If $courseid matches the site id then this function
 * returns a list of all teachers for the site.
 *
 * @uses $CFG
 * @param int $courseid The course in question.
 * @param string $sort ?
 * @param string $exceptions ?
 * @return object
 * @todo Finish documenting this function
 */
function get_course_teachers($courseid, $sort='t.authority ASC', $exceptions='') {

    global $CFG;

    $sort = 'ul.timeaccess DESC';

    $context = get_context_instance(CONTEXT_COURSE, $courseid);

    /// For the site course, if the $CFG->defaultuserroleid is set to the legacy teacher role, then all
    /// users are teachers. This function should be replaced where it is used with something more
    /// meaningful.
    if (($courseid == SITEID) && !empty($CFG->defaultuserroleid) && empty($CFG->nodefaultuserrolelists)) {
        if ($roles = get_roles_with_capability('moodle/legacy:teacher', CAP_ALLOW, $context)) {
            $hascap = false;
            foreach ($roles as $role) {
                if ($role->id == $CFG->defaultuserroleid) {
                    $hascap = true;
                    break;
                }
            }
            if ($hascap) {
                if (empty($fields)) {
                    $fields = '*';
                }
                return get_users(true, '', true, $exceptions, 'lastname ASC', '', '', '', '', $fields);
            }
        }
    }

    $users = get_users_by_capability($context, 'moodle/course:update',
                                     'u.*, ul.timeaccess as lastaccess',
                                     $sort, '','','',$exceptions, false);
    return sort_by_roleassignment_authority($users, $context);

    /// some fields will be missing, like authority, editall
    /*
    return get_records_sql("SELECT u.id, u.username, u.firstname, u.lastname, u.maildisplay, u.mailformat, u.maildigest,
                                   u.email, u.city, u.country, u.lastlogin, u.picture, u.lang, u.timezone,
                                   u.emailstop, t.authority,t.role,t.editall,t.timeaccess as lastaccess
                            FROM {$CFG->prefix}user u,
                                 {$CFG->prefix}user_teachers t
                            WHERE t.course = '$courseid' AND t.userid = u.id
                              AND u.deleted = '0' AND u.confirmed = '1' $exceptions $sort");
    */
}

/**
 * Returns all the users of a course: students and teachers
 *
 * @param int $courseid The course in question.
 * @param string $sort ?
 * @param string $exceptions ?
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @return object
 * @todo Finish documenting this function
 */
function get_course_users($courseid, $sort='ul.timeaccess DESC', $exceptions='', $fields='u.*, ul.timeaccess as lastaccess') {
    global $CFG;

    $context = get_context_instance(CONTEXT_COURSE, $courseid);

    /// If the course id is the SITEID, we need to return all the users if the "defaultuserroleid"
    /// has the capbility of accessing the site course. $CFG->nodefaultuserrolelists set to true can
    /// over-rule using this.
    if (($courseid == SITEID) && !empty($CFG->defaultuserroleid) && empty($CFG->nodefaultuserrolelists)) {
        if ($roles = get_roles_with_capability('moodle/course:view', CAP_ALLOW, $context)) {
            $hascap = false;
            foreach ($roles as $role) {
                if ($role->id == $CFG->defaultuserroleid) {
                    $hascap = true;
                    break;
                }
            }
            if ($hascap) {
                if (empty($fields)) {
                    $fields = '*';
                }
                return get_users(true, '', true, $exceptions, 'lastname ASC', '', '', '', '', $fields);
            }
        }
    }
    return get_users_by_capability($context, 'moodle/course:view', $fields, $sort, '','','',$exceptions, false);

}

/**
 * Returns a list of all site users
 * Obsolete, just calls get_course_users(SITEID)
 *
 * @uses SITEID
 * @deprecated Use {@link get_course_users()} instead.
 * @param string $fields A comma separated list of fields to be returned from the chosen table.
 * @return object|false  {@link $USER} records or false if error.
 */
function get_site_users($sort='u.lastaccess DESC', $fields='*', $exceptions='') {

    return get_course_users(SITEID, $sort, $exceptions, $fields);
}

/**
 * Returns an array of user objects
 *
 * @uses $CFG
 * @param int $groupid The group(s) in question.
 * @param string $sort How to sort the results
 * @return object (changed to groupids)
 */
function get_group_students($groupids, $sort='ul.timeaccess DESC') {

    if (is_array($groupids)){
        $groups = $groupids;
        // all groups must be from one course anyway...
        $group = groups_get_group(array_shift($groups));
    } else {
        $group = groups_get_group($groupids);
    }
    if (!$group) {
        return NULL;
    }

    $context = get_context_instance(CONTEXT_COURSE, $group->courseid);
    return get_users_by_capability($context, 'moodle/legacy:student', 'u.*, ul.timeaccess as lastaccess', $sort, '','',$groupids, '', false);
}

/**
 * Determines if the HTML editor is enabled. This function has
 * been deprecated, but needs to remain available because it is
 * used in language packs for Moodle 1.6 to 1.9.
 *
 * @deprecated Use {@link can_use_html_editor()} instead.
 */
function can_use_richtext_editor() {
    return can_use_html_editor();
}


########### FROM weblib.php ##########################################################################

/**
 * Creates a nicely formatted table and returns it.
 *
 * @param array $table is an object with several properties.
 *     <ul<li>$table->head - An array of heading names.
 *     <li>$table->align - An array of column alignments
 *     <li>$table->size  - An array of column sizes
 *     <li>$table->wrap - An array of "nowrap"s or nothing
 *     <li>$table->data[] - An array of arrays containing the data.
 *     <li>$table->class -  A css class name
 *     <li>$table->fontsize - Is the size of all the text
 *     <li>$table->tablealign  - Align the whole table
 *     <li>$table->width  - A percentage of the page
 *     <li>$table->cellpadding  - Padding on each cell
 *     <li>$table->cellspacing  - Spacing between cells
 * </ul>
 * @return string
 * @todo Finish documenting this function
 */
function make_table($table) {
    return print_table($table, true);
}


/**
 * Print a message in a standard themed box.
 * This old function used to implement boxes using tables.  Now it uses a DIV, but the old
 * parameters remain.  If possible, $align, $width and $color should not be defined at all.
 * Preferably just use print_box() in weblib.php
 *
 * @param string $align, alignment of the box, not the text (default center, left, right).
 * @param string $width, width of the box, including units %, for example '100%'.
 * @param string $color, background colour of the box, for example '#eee'.
 * @param int $padding, padding in pixels, specified without units.
 * @param string $class, space-separated class names.
 * @param string $id, space-separated id names.
 * @param boolean $return, return as string or just print it
 */
function print_simple_box($message, $align='', $width='', $color='', $padding=5, $class='generalbox', $id='', $return=false) {
    $output = '';
    $output .= print_simple_box_start($align, $width, $color, $padding, $class, $id, true);
    $output .= stripslashes_safe($message);
    $output .= print_simple_box_end(true);

    if ($return) {
        return $output;
    } else {
        echo $output;
    }
}



/**
 * This old function used to implement boxes using tables.  Now it uses a DIV, but the old
 * parameters remain.  If possible, $align, $width and $color should not be defined at all.
 * Even better, please use print_box_start() in weblib.php
 *
 * @param string $align, alignment of the box, not the text (default center, left, right).   DEPRECATED
 * @param string $width, width of the box, including % units, for example '100%'.            DEPRECATED
 * @param string $color, background colour of the box, for example '#eee'.                   DEPRECATED
 * @param int $padding, padding in pixels, specified without units.                          OBSOLETE
 * @param string $class, space-separated class names.
 * @param string $id, space-separated id names.
 * @param boolean $return, return as string or just print it
 */
function print_simple_box_start($align='', $width='', $color='', $padding=5, $class='generalbox', $id='', $return=false) {

    $output = '';

    $divclasses = 'box '.$class.' '.$class.'content';
    $divstyles  = '';

    if ($align) {
        $divclasses .= ' boxalign'.$align;    // Implement alignment using a class
    }
    if ($width) {    // Hopefully we can eliminate these in calls to this function (inline styles are bad)
        if (substr($width, -1, 1) == '%') {    // Width is a % value
            $width = (int) substr($width, 0, -1);    // Extract just the number
            if ($width < 40) {
                $divclasses .= ' boxwidthnarrow';    // Approx 30% depending on theme
            } else if ($width > 60) {
                $divclasses .= ' boxwidthwide';      // Approx 80% depending on theme
            } else {
                $divclasses .= ' boxwidthnormal';    // Approx 50% depending on theme
            }
        } else {
            $divstyles  .= ' width:'.$width.';';     // Last resort
        }
    }
    if ($color) {    // Hopefully we can eliminate these in calls to this function (inline styles are bad)
        $divstyles  .= ' background:'.$color.';';
    }
    if ($divstyles) {
        $divstyles = ' style="'.$divstyles.'"';
    }

    if ($id) {
        $id = ' id="'.$id.'"';
    }

    $output .= '<div'.$id.$divstyles.' class="'.$divclasses.'">';

    if ($return) {
        return $output;
    } else {
        echo $output;
    }
}


/**
 * Print the end portion of a standard themed box.
 * Preferably just use print_box_end() in weblib.php
 */
function print_simple_box_end($return=false) {
    $output = '</div>';
    if ($return) {
        return $output;
    } else {
        echo $output;
    }
}

/**
 * deprecated - use clean_param($string, PARAM_FILE); instead
 * Check for bad characters ?
 *
 * @param string $string ?
 * @param int $allowdots ?
 * @todo Finish documenting this function - more detail needed in description as well as details on arguments
 */
function detect_munged_arguments($string, $allowdots=1) {
    if (substr_count($string, '..') > $allowdots) {   // Sometimes we allow dots in references
        return true;
    }
    if (ereg('[\|\`]', $string)) {  // check for other bad characters
        return true;
    }
    if (empty($string) or $string == '/') {
        return true;
    }

    return false;
}


/////////////////////////////////////////////////////////////
/// Old functions not used anymore - candidates for removal
/////////////////////////////////////////////////////////////


/** various deprecated groups function **/


/**
 * Get the IDs for the user's groups in the given course.
 *
 * @uses $USER
 * @param int $courseid The course being examined - the 'course' table id field.
 * @return array An _array_ of groupids.
 * (Was return $groupids[0] - consequences!)
 */
function mygroupid($courseid) {
    global $USER;
    if ($groups = groups_get_all_groups($courseid, $USER->id)) {
        return array_keys($groups);
    } else {
        return false;
    }
}


/**
 * Returns an array of user objects
 *
 * @uses $CFG
 * @param int $groupid The group in question.
 * @param string $sort ?
 * @param string $exceptions ?
 * @return object
 * @todo Finish documenting this function
 */
function get_group_users($groupid, $sort='u.lastaccess DESC', $exceptions='',
                         $fields='u.*') {
    global $CFG;
    if (!empty($exceptions)) {
        $except = ' AND u.id NOT IN ('. $exceptions .') ';
    } else {
        $except = '';
    }
    // in postgres, you can't have things in sort that aren't in the select, so...
    $extrafield = str_replace('ASC','',$sort);
    $extrafield = str_replace('DESC','',$extrafield);
    $extrafield = trim($extrafield);
    if (!empty($extrafield)) {
        $extrafield = ','.$extrafield;
    }
    return get_records_sql("SELECT DISTINCT $fields $extrafield
                              FROM {$CFG->prefix}user u,
                                   {$CFG->prefix}groups_members m
                             WHERE m.groupid = '$groupid'
                               AND m.userid = u.id $except
                          ORDER BY $sort");
}

/**
 * Returns the current group mode for a given course or activity module
 *
 * Could be false, SEPARATEGROUPS or VISIBLEGROUPS    (<-- Martin)
 */
function groupmode($course, $cm=null) {

    if (isset($cm->groupmode) && empty($course->groupmodeforce)) {
        return $cm->groupmode;
    }
    return $course->groupmode;
}

/**
 * Sets the current group in the session variable
 * When $SESSION->currentgroup[$courseid] is set to 0 it means, show all groups.
 * Sets currentgroup[$courseid] in the session variable appropriately.
 * Does not do any permission checking.
 * @uses $SESSION
 * @param int $courseid The course being examined - relates to id field in
 * 'course' table.
 * @param int $groupid The group being examined.
 * @return int Current group id which was set by this function
 */
function set_current_group($courseid, $groupid) {
    global $SESSION;
    return $SESSION->currentgroup[$courseid] = $groupid;
}


/**
 * Gets the current group - either from the session variable or from the database.
 *
 * @uses $USER
 * @uses $SESSION
 * @param int $courseid The course being examined - relates to id field in
 * 'course' table.
 * @param bool $full If true, the return value is a full record object.
 * If false, just the id of the record.
 */
function get_current_group($courseid, $full = false) {
    global $SESSION;

    if (isset($SESSION->currentgroup[$courseid])) {
        if ($full) {
            return groups_get_group($SESSION->currentgroup[$courseid]);
        } else {
            return $SESSION->currentgroup[$courseid];
        }
    }

    $mygroupid = mygroupid($courseid);
    if (is_array($mygroupid)) {
        $mygroupid = array_shift($mygroupid);
        set_current_group($courseid, $mygroupid);
        if ($full) {
            return groups_get_group($mygroupid);
        } else {
            return $mygroupid;
        }
    }

    if ($full) {
        return false;
    } else {
        return 0;
    }
}










/**
 * Print an error page displaying an error message.
 * Old method, don't call directly in new code - use print_error instead.
 *
 *
 * @uses $SESSION
 * @uses $CFG
 * @param string $message The message to display to the user about the error.
 * @param string $link The url where the user will be prompted to continue. If no url is provided the user will be directed to the site index page.
 */
function error ($message, $link='') {

    global $CFG, $SESSION, $THEME;
    debugging('error() is a deprecated function, please call print_error() instead of error()', DEBUG_DEVELOPER);
    $message = clean_text($message);   // In case nasties are in here

    /**
     * TODO VERY DIRTY HACK USED FOR UNIT TESTING UNTIL PROPER EXCEPTION HANDLING IS IMPLEMENTED
     */
    if (defined('UNITTEST')) {
        // Errors in unit test become exceptions, so you can unit test
        // code that might call error().
        throw new Exception('error() call: '.  $message.($link!=='' ? ' ['.$link.']' : ''));
    }

    if (defined('FULLME') && FULLME == 'cron') {
        // Errors in cron should be mtrace'd.
        mtrace($message);
        die;
    }

    if (! defined('HEADER_PRINTED')) {
        //header not yet printed
        @header('HTTP/1.0 404 Not Found');
        print_header(get_string('error'));
    } else {
        print_container_end_all(false, $THEME->open_header_containers);
    }

    echo '<br />';
    print_simple_box($message, '', '', '', '', 'errorbox');

    debugging('Stack trace:', DEBUG_DEVELOPER);

    // in case we are logging upgrade in admin/index.php stop it
    if (function_exists('upgrade_log_finish')) {
        upgrade_log_finish();
    }

    if (empty($link) and !defined('ADMIN_EXT_HEADER_PRINTED')) {
        if ( !empty($SESSION->fromurl) ) {
            $link = $SESSION->fromurl;
            unset($SESSION->fromurl);
        } else {
            $link = $CFG->wwwroot .'/';
        }
    }

    if (!empty($link)) {
        print_continue($link);
    }

    print_footer();

    for ($i=0;$i<512;$i++) {  // Padding to help IE work with 404
        echo ' ';
    }

    die;
}
?>