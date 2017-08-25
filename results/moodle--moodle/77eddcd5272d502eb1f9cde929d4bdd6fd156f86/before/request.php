<?php  // $Id$

    /// this allows a student to request a course be created for them.

    require_once('../config.php');
    require_once('request_form.php');

    require_login();

    if (isguest()) {
        print_error("noguest");
    }

    if (empty($CFG->enablecourserequests)) {
        print_error('courserequestdisabled');
    }

    $requestform = new course_request_form();

    $strtitle = get_string('courserequest');
    $navlinks = array();
    $navlinks[] = array('name' => $strtitle, 'link' => null, 'type' => 'misc');
    $navigation = build_navigation($navlinks);

    print_header($strtitle, $strtitle, $navigation, $requestform->focus());

    print_simple_box_start('center');
    print_string('courserequestintro');
    print_simple_box_end();


    if ($requestform->is_cancelled()){

        redirect($CFG->wwwroot);

    }elseif ($data = $requestform->get_data()) {
        $data->requester = $USER->id;

        if ($DB->insert_record('course_request', $data)) {
            notice(get_string('courserequestsuccess'));
        } else {
            notice(get_string('courserequestfailed'));
        }

    } else {

        $requestform->display();
    }

    print_footer();

?>