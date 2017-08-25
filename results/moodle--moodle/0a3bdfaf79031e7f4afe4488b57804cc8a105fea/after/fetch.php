<?php  //$Id$

$nomoodlecookie = true; // session not used here
require '../../../config.php';

$id = required_param('id', PARAM_INT); // course id
if (!$course = get_record('course', 'id', $id)) {
    print_error('nocourseid');
}

require_user_key_login('grade/import', $id); // we want different keys for each course

if (empty($CFG->gradepublishing)) {
    error('Grade publishing disabled');
}

$context = get_context_instance(CONTEXT_COURSE, $id);
require_capability('gradeimport/xml:pusblish', $context);

// use the same page parameters as import.php and append &key=sdhakjsahdksahdkjsahksadjksahdkjsadhksa
require 'import.php';

?>