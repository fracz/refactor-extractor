<?php  // $Id$

    require_once('config.php');
    require_once('lib/filelib.php');

    require_login();
    if (isguestuser()) {
        print_error('noguest');
    }

    // disable moodle specific debug messages
    disable_debugging();

    $relativepath = get_file_argument('file.php');
    $forcedownload = optional_param('forcedownload', 0, PARAM_BOOL);

    // relative path must start with '/'
    if (!$relativepath) {
        print_error('invalidargorconf');
    } else if ($relativepath{0} != '/') {
        print_error('pathdoesnotstartslash');
    }

    // extract relative path components
    $args = explode('/', ltrim($relativepath, '/'));

    if (count($args) == 0) { // always at least user id
        print_error('invalidarguments');
    }

    $contextid = (int)array_shift($args);
    $filearea = array_shift($args);

    $context = get_context_instance_by_id($contextid);
    if ($context->contextlevel != CONTEXT_USER) {
        print_error('invalidarguments');
    }

    $userid = $context->instanceid;
    if ($USER->id != $userid) {
        print_error('invaliduserid');
    }

    switch ($filearea) {
        case 'private': $itemid = 0; $forcedownload = true; break;
        case 'draft'  : $itemid = (int)array_shift($args); break;
        default:        not_found();
    }

    $relativepath = '/'.implode('/', $args);


    $fs = get_file_storage();

    $fullpath = $context->id.$filearea.$itemid.$relativepath;

    if (!$file = $fs->get_file_by_hash(sha1($fullpath)) or $file->get_filename() == '.') {
        not_found();
    }

    // ========================================
    // finally send the file
    // ========================================
    session_write_close(); // unlock session during fileserving
    send_stored_file($file, 0, false, $forcedownload);

    function not_found() {
        global $CFG;
        header('HTTP/1.0 404 not found');
        print_error('filenotfound', 'error', $CFG->wwwroot.'/'); //this is not displayed on IIS??
    }