<?php
/**
 * Handles lesson actions
 *
 * ACTIONS handled are:
 *    addbranchtable
 *    addendofbranch
 *    addcluster
 *    addendofcluster
 *    addpage
 *    confirmdelete
 *    continue
 *    delete
 *    editpage
 *    insertpage
 *    move
 *    moveit
 *    updatepage
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package lesson
 **/

    require("../../config.php");
    require("locallib.php");

    $id     = required_param('id', PARAM_INT);         // Course Module ID
    $action = required_param('action', PARAM_ALPHA);   // Action

    list($cm, $course, $lesson) = lesson_get_basics($id);

    require_login($course, false, $cm);

    $url = new moodle_url($CFG->wwwroot.'/mod/lesson/edit.php', array('id'=>$id,'action'=>$action));
    $PAGE->set_url($url);
    $PAGE->navbar->add(get_string($action, 'lesson'));

    $context = get_context_instance(CONTEXT_MODULE, $cm->id);

/// Set up some general variables
    $usehtmleditor = can_use_html_editor();

/// Process the action
    switch ($action) {
        case 'addbranchtable':
        case 'addpage':
        case 'confirmdelete':
        case 'editpage':
        case 'move':
            lesson_print_header($cm, $course, $lesson);
        case 'addcluster':
        case 'addendofbranch':
        case 'addendofcluster':
        case 'delete':
        case 'insertpage':
        case 'updatepage':
        case 'moveit':
            require_capability('mod/lesson:edit', $context);
        case 'continue':
            include($CFG->dirroot.'/mod/lesson/action/'.$action.'.php');
            break;
        default:
            print_error('unknowaction');
    }

    echo $OUTPUT->footer();

