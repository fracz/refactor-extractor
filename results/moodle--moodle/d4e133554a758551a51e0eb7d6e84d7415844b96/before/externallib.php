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
 * External user API
 *
 * @package    moodlecore
 * @subpackage webservice
 * @copyright  2009 Petr Skoda (http://skodak.org)
 * @license    http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */

require_once("$CFG->libdir/externallib.php");

class moodle_user_external extends external_api {

    public static function create_users_params() {
        $userpreference = array();
        $userpreference->name =  array(PARAM_ALPHANUMEXT, 'The name of the preference to set');
        $userpreference->value =  array(PARAM_RAW, 'The value of the preference');

        $usercustomfields = new object();
        $usercustomfields->name =  array(PARAM_ALPHANUMEXT, 'The name of the custom field (must exist)');
        $usercustomfields->value =  array(PARAM_RAW, 'The value of the custom field');

        $usertocreate = new object();
        $usertocreate->username    = array(PARAM_USERNAME, 'Username policy is defined in Moodle security config', REQUIRED);
        $usertocreate->password    = array(PARAM_RAW, 'Moodle passwords can consist of any character', REQUIRED);
        $usertocreate->firstname   = array(PARAM_NOTAGS, 'The first name(s) of the user', REQUIRED);
        $usertocreate->lastname    = array(PARAM_NOTAGS, 'The family name of the user', REQUIRED);
        $usertocreate->email       = array(PARAM_EMAIL, 'A valid and unique email address', REQUIRED);
        $usertocreate->auth        = array(PARAM_AUTH, 'Auth plugins include manual, ldap, imap, etc');
        $usertocreate->confirmed   = array(PARAM_NUMBER, 'Active user: 1 if confirmed, 0 otherwise');
        $usertocreate->idnumber    = array(PARAM_RAW, 'An arbitrary ID code number perhaps from the institution');
        $usertocreate->emailstop   = array(PARAM_NUMBER, 'Email is blocked: 1 is blocked and 0 otherwise');
        $usertocreate->lang        = array(PARAM_LANG, 'Language code such as "en_utf8", must exist on server');
        $usertocreate->theme       = array(PARAM_THEME, 'Theme name such as "standard", must exist on server');
        $usertocreate->timezone    = array(PARAM_ALPHANUMEXT, 'Timezone code such as Australia/Perth, or 99 for default');
        $usertocreate->mailformat  = array(PARAM_INTEGER, 'Mail format code is 0 for plain text, 1 for HTML etc');
        $usertocreate->description = array(PARAM_TEXT, 'User profile description, as HTML');
        $usertocreate->city        = array(PARAM_NOTAGS, 'Home city of the user');
        $usertocreate->country     = array(PARAM_ALPHA, 'Home country code of the user, such as AU or CZ');
        $usertocreate->preferences = array('multiple' => $userpreference);
        $usertocreate->custom      = array('multiple' => $usercustomfields);

        $createusersparams = new object();
        $createusersparams->users  = array('multiple' => $usertocreate);

        return $createusersparams;
    }

    public static function create_users_return() {
        $createusersreturn = new object();
        $createusersreturn->userids = array('multiple' => PARAM_NUMBER);

        return $createusersreturn;
    }

    /*
     * Create one or more users
     *
     * @param $params  An array of users to create.  Each user is defined by $usertocreate above.
     *
     * @return $return  An array of userids, one for each user that was created
     */
    public static function create_users($params) {
        global $CFG, $DB;

        // Ensure the current user is allowed to run this function
        $context = get_context_instance(CONTEXT_SYSTEM);
        require_capability('moodle/user:create', $context);
        self::validate_context($context);

        // Do basic automatic PARAM checks on incoming data, using params description
        // This checks to make sure that:
        //      1) No extra data was sent
        //      2) All required items were sent
        //      3) All data passes clean_param without changes (yes this is strict)
        // If any problems are found then exceptions are thrown with helpful error messages
        self::validate_params($params, self::create_users_params());


        // Perform further checks and build up a clean array of user data
        // Nothing is actually performed until the whole dataset is checked
        $users = array();
        foreach ($params as $user) {

            // Empty or no auth is assumed to be manual
            if (empty($user['auth'])) {
                $user['auth'] = 'manual';
            }

            // Lang must be a real code, not empty string
            if (isset($user['lang']) && empty($user['lang'])) {
                unset($user['lang']);
            }

            // Make sure that the username doesn't already exist
            if ($DB->get_record('user', array('username'=>$user['username'], 'mnethostid'=>$CFG->mnet_localhost_id))) {
                throw new invalid_parameter_exception($user['username']." username is already taken, sorry");
            }

            // Make sure that incoming data doesn't contain duplicate usernames
            if (isset($users[$user['username']])) {
                throw new invalid_parameter_exception("multiple users with the same username requested");
            }

            // TODO: More checks here?

            $users[$user['username']] = $user;   // Add this data to an array (mem overflows?)
        }

        $result = array();

        foreach ($users as $user) {  // Actually create the user accounts now
            $record = create_user_record($user['username'], $user['password'], $user['auth']);
            unset($user['username']);
            unset($user['password']);
            unset($user['auth']);

            // now override the default (or external) values
            foreach ($user as $key=>$value) {
                $record->$key = $value;
            }
            $DB->update_record('user', $record);

            $result[] = $record->id;

            // TODO: Save all the preferences and custom fields here

        }

        return $result;
    }


    public static function delete_users($params) {
        //TODO
    }


    public static function update_users($params) {
        //TODO
    }

    public static function get_users($params) {
        $context = get_context_instance(CONTEXT_SYSTEM);
        require_capability('moodle/user:viewdetails', $context);
        self::validate_context($context);

        $search = validate_param($params['search'], PARAM_RAW);

        //TODO: this search is probably useless for external systems because it is not exact
        //      1/ we should specify multiple search parameters including the mnet host id
        //      2/ custom profile fileds not inlcuded

        return get_users(true, $search, false, null, 'firstname ASC','', '', '', 1000, 'id, mnethostid, auth, confirmed, username, idnumber, firstname, lastname, email, emailstop, lang, theme, timezone, mailformat, city, description, country');
    }

}