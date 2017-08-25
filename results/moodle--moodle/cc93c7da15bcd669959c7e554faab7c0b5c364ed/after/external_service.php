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
 * Web services admin UI
 *
 * @package   webservice
 * @copyright 2009 Moodle Pty Ltd (http://moodle.com)
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */

require_once('../config.php');
require_once($CFG->libdir.'/adminlib.php');
require_once('external_forms.php');

$id      = required_param('id', PARAM_INT);
$action  = optional_param('action', '', PARAM_ACTION);
$confirm = optional_param('confirm', 0, PARAM_BOOL);

$PAGE->set_url('admin/external_service.php', array('id'=>$id));

admin_externalpage_setup('externalservice');

$returnurl = "$CFG->wwwroot/$CFG->admin/settings.php?section=externalservices";

if ($id) {
    $service = $DB->get_record('external_services', array('id'=>$id), '*', MUST_EXIST);
} else {
    $service = null;
}

if ($action == 'delete' and confirm_sesskey() and $service and empty($service->component)) {
    if (!$confirm) {
        admin_externalpage_print_header();
        $optionsyes = array('id'=>$id, 'action'=>'delete', 'confirm'=>1, 'sesskey'=>sesskey());
        $optionsno  = array('section'=>'externalservices');
        $formcontinue = html_form::make_button('external_service.php', $optionsyes, get_string('delete'), 'post');
        $formcancel = html_form::make_button('settings.php', $optionsno, get_string('cancel'), 'get');
        echo $OUTPUT->confirm(get_string('deleteserviceconfirm', 'webservice', $service->name), $formcontinue, $formcancel);
        echo $OUTPUT->footer();
        die;
    }
    $DB->delete_records('external_services_users', array('externalserviceid'=>$service->id));
    $DB->delete_records('external_services_functions', array('externalserviceid'=>$service->id));
    $DB->delete_records('external_services', array('id'=>$service->id));
    redirect($returnurl);
}

$mform = new external_service_form(null, $service);

if ($mform->is_cancelled()) {
    redirect($returnurl);

} else if ($data = $mform->get_data()) {
    $data = (object)$data;

    //TODO: add timecreated+modified and maybe logging too
    if (empty($data->id)) {
        $DB->insert_record('external_services', $data);
    } else {
        $DB->update_record('external_services', $data);
    }

    redirect($returnurl);
}

admin_externalpage_print_header();
$mform->display();
echo $OUTPUT->footer();
