<?php

// This file is part of Moodle - http://moodle.org/
//
// Moodle is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Moodle is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Moodle.  If not, see <http://www.gnu.org/licenses/>.

/**
 * Allows you to edit a users profile
 *
 * @copyright 1999 Martin Dougiamas  http://dougiamas.com
 * @license http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 * @package user
 */

require_once('../config.php');
require_once($CFG->libdir.'/gdlib.php');
require_once($CFG->libdir.'/adminlib.php');
require_once($CFG->dirroot.'/user/editadvanced_form.php');
require_once($CFG->dirroot.'/user/editlib.php');
require_once($CFG->dirroot.'/user/profile/lib.php');

httpsrequired();

$id     = optional_param('id', $USER->id, PARAM_INT);    // user id; -1 if creating new user
$course = optional_param('course', SITEID, PARAM_INT);   // course id (defaults to Site)

$url = new moodle_url('/user/editadvanced.php', array('course'=>$course));
if ($id !== $USER->id) {
    $url->param('id', $id);
}
$PAGE->set_url($url);

if (!$course = $DB->get_record('course', array('id'=>$course))) {
    print_error('invalidcourseid');
}
if (!empty($USER->newadminuser)) {
    $PAGE->set_course($SITE);
    $PAGE->set_pagelayout('maintenance');
} else {
    require_login($course);
}

if ($course->id == SITEID) {
    $coursecontext = get_context_instance(CONTEXT_SYSTEM);   // SYSTEM context
} else {
    $coursecontext = get_context_instance(CONTEXT_COURSE, $course->id);   // Course context
}
$systemcontext = get_context_instance(CONTEXT_SYSTEM);

if ($id == -1) {
    // creating new user
    require_capability('moodle/user:create', $systemcontext);
    $user = new object();
    $user->id = -1;
    $user->auth = 'manual';
    $user->confirmed = 1;
    $user->deleted = 0;
} else {
    // editing existing user
    require_capability('moodle/user:update', $systemcontext);
    if (!$user = $DB->get_record('user', array('id'=>$id))) {
        print_error('invaliduserid');
    }
}

// remote users cannot be edited
if ($user->id != -1 and is_mnet_remote_user($user)) {
    redirect($CFG->wwwroot . "/user/view.php?id=$id&course={$course->id}");
}

if ($user->id != $USER->id and is_primary_admin($user->id)) {  // Can't edit primary admin
    print_error('adminprimarynoedit');
}

if (isguestuser($user->id)) { // the real guest user can not be edited
    print_error('guestnoeditprofileother');
}

if ($user->deleted) {
    echo $OUTPUT->header();
    echo $OUTPUT->heading(get_string('userdeleted'));
    echo $OUTPUT->footer();
    die;
}

if ($user->id == -1) {
    admin_externalpage_setup('addnewuser', '', array('id' => -1));
} else if ($user->id != $USER->id) {
    admin_externalpage_setup('editusers', '', array('id' => $user->id, 'course' => SITEID), $CFG->wwwroot . '/user/editadvanced.php');
}

//load user preferences
useredit_load_preferences($user);

//Load custom profile fields data
profile_load_data($user);

//User interests
if (!empty($CFG->usetags)) {
    require_once($CFG->dirroot.'/tag/lib.php');
    $user->interests = tag_get_tags_array('user', $id);
}

if ($user->id !== -1) {
    $user->context = get_context_instance(CONTEXT_USER, $user->id);
    $editoroptions = array('maxfiles'=>EDITOR_UNLIMITED_FILES, 'maxbytes'=>$CFG->maxbytes, 'trusttext'=>false, 'forcehttps'=>false);
    $user = file_prepare_standard_editor($user, 'description', $editoroptions, $user->context, 'user_profile', $user->id);
} else {
    // This is a new user, we don't want to add files here
    $editoroptions = array('maxfiles'=>0, 'maxbytes'=>0, 'trusttext'=>false, 'forcehttps'=>false);
}

//create form
$userform = new user_editadvanced_form(null, array('editoroptions'=>$editoroptions));
$userform->set_data($user);

if ($usernew = $userform->get_data()) {
    add_to_log($course->id, 'user', 'update', "view.php?id=$user->id&course=$course->id", '');

    if (empty($usernew->auth)) {
        //user editing self
        $authplugin = get_auth_plugin($user->auth);
        unset($usernew->auth); //can not change/remove
    } else {
        $authplugin = get_auth_plugin($usernew->auth);
    }

    $usernew->username = clean_param($usernew->username, PARAM_USERNAME);
    $usernew->timemodified = time();

    if ($usernew->id == -1) {
        //TODO check out if it makes sense to create account with this auth plugin and what to do with the password
        unset($usernew->id);
        $usernew = file_postupdate_standard_editor($usernew, 'description', $editoroptions, null, 'user_profile', null);
        $usernew->mnethostid = $CFG->mnet_localhost_id; // always local user
        $usernew->confirmed  = 1;
        $usernew->timecreated = time();
        $usernew->password = hash_internal_user_password($usernew->newpassword);
        $usernew->id = $DB->insert_record('user', $usernew);
        $usercreated = true;

    } else {
        $usernew = file_postupdate_standard_editor($usernew, 'description', $editoroptions, $user->context, 'user_profile', $usernew->id);
        $DB->update_record('user', $usernew);
        // pass a true $userold here
        if (! $authplugin->user_update($user, $userform->get_data())) {
            // auth update failed, rollback for moodle
            $DB->update_record('user', $user);
            print_error('cannotupdateuseronexauth', '', '', $user->auth);
        }

        //set new password if specified
        if (!empty($usernew->newpassword)) {
            if ($authplugin->can_change_password()) {
                if (!$authplugin->user_update_password($usernew, $usernew->newpassword)){
                    print_error('cannotupdatepasswordonextauth', '', '', $usernew->auth);
                }
            }
        }
        $usercreated = false;
    }

    $usercontext = get_context_instance(CONTEXT_USER, $usernew->id);

    //update preferences
    useredit_update_user_preference($usernew);

    // update tags
    if (!empty($CFG->usetags)) {
        useredit_update_interests($usernew, $usernew->interests);
    }

    //update user picture
    if (!empty($CFG->gdversion)) {
        useredit_update_picture($usernew, $userform);
    }

    // update mail bounces
    useredit_update_bounces($user, $usernew);

    // update forum track preference
    useredit_update_trackforums($user, $usernew);

    // save custom profile fields data
    profile_save_data($usernew);

    // reload from db
    $usernew = $DB->get_record('user', array('id'=>$usernew->id));

    // trigger events
    if ($usercreated) {
        //set default message preferences
        if (!message_set_default_message_preferences( $usernew )){
            print_error('cannotsavemessageprefs', 'message');
        }
        events_trigger('user_created', $usernew);
    } else {
        events_trigger('user_updated', $usernew);
    }

    if ($user->id == $USER->id) {
        // Override old $USER session variable
        foreach ((array)$usernew as $variable => $value) {
            $USER->$variable = $value;
        }
        if (!empty($USER->newadminuser)) {
            unset($USER->newadminuser);
            // apply defaults again - some of them might depend on admin user info, backup, roles, etc.
            admin_apply_default_settings(NULL , false);
            // redirect to admin/ to continue with installation
            redirect("$CFG->wwwroot/$CFG->admin/");
        } else {
            redirect("$CFG->wwwroot/user/view.php?id=$USER->id&course=$course->id");
        }
    } else {
        session_gc(); // remove stale sessions
        redirect("$CFG->wwwroot/$CFG->admin/user.php");
    }
    //never reached
}


/// Display page header
if ($user->id == -1 or ($user->id != $USER->id)) {
    if ($user->id == -1) {
        admin_externalpage_print_header();
    } else {
        admin_externalpage_print_header();
        $userfullname = fullname($user, true);
        echo $OUTPUT->heading($userfullname);
    }
} else if (!empty($USER->newadminuser)) {
    $strinstallation = get_string('installation', 'install');
    $strprimaryadminsetup = get_string('primaryadminsetup');

    $PAGE->navbar->add($strprimaryadminsetup);
    $PAGE->set_title($strinstallation);
    $PAGE->set_heading($strinstallation);
    $PAGE->set_cacheable(false);

    echo $OUTPUT->header();
    echo $OUTPUT->box(get_string('configintroadmin', 'admin'), 'generalbox boxwidthnormal boxaligncenter');
    echo '<br />';
} else {
    $streditmyprofile = get_string('editmyprofile');
    $strparticipants  = get_string('participants');
    $strnewuser       = get_string('newuser');
    $userfullname     = fullname($user, true);

    $link = null;
    if (has_capability('moodle/course:viewparticipants', $coursecontext) || has_capability('moodle/site:viewparticipants', $systemcontext)) {
        $link = new moodle_url("/user/index.php", array('id'=>$course->id));
    }
    $PAGE->navbar->add($strparticipants, $link);
    $link = new moodle_url('/user/view.php', array('id'=>$user->id, 'course'=>$course->id));
    $PAGE->navbar->add($userfullname, $link);
    $PAGE->navbar->add($streditmyprofile);

    $PAGE->set_title("$course->shortname: $streditmyprofile");
    $PAGE->set_heading($course->fullname);

    echo $OUTPUT->header();
    /// Print tabs at the top
    $showroles = 1;
    $currenttab = 'editprofile';
    require('tabs.php');
}

/// Finally display THE form
$userform->display();

/// and proper footer
echo $OUTPUT->footer();
