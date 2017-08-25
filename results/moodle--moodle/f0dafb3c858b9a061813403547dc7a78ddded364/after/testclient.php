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
 * Web service test client.
 *
 * @package   webservice
 * @copyright 2009 Moodle Pty Ltd (http://moodle.com)
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */

require('../config.php');
require_once("$CFG->dirroot/webservice/testclient_forms.php");

$function = optional_param('function', '', PARAM_SAFEDIR);
$protocol = optional_param('protocol', '', PARAM_SAFEDIR);

$PAGE->set_url('webservice/testclient.php');

require_login();
require_capability('moodle/site:config', get_context_instance(CONTEXT_SYSTEM)); // TODO: do we need some new capability?

// list of all available functions for testing - please note there must be explicit
// support for testing of each functions, the parameter conversion and form is hardcoded
$functions = array('moodle_group_get_groups');
$functions = array_combine($functions, $functions);
if (!isset($functions[$function])) { // whitelisting security
    $function = '';
}

// list all enabled webservices
$available_protocols = get_plugin_list('webservice');
$active_protocols = empty($CFG->webserviceprotocols) ? array() : explode(',', $CFG->webserviceprotocols);
$protocols = array();
foreach ($active_protocols as $p) {
    if (empty($available_protocols[$p])) {
        continue;
    }
    $protocols[$p] = get_string('pluginname', 'webservice_'.$p);
}
if (!isset($protocols[$protocol])) { // whitelisting security
    $protocol = '';
}

if (!$function or !$protocol) {
    $mform = new webservice_test_client_form(null, array($functions, $protocols));
    echo $OUTPUT->header();
    echo $OUTPUT->heading(get_string('testclient', 'webservice'));
    $mform->display();
    echo $OUTPUT->footer();
    die;
}

$class = $function.'_form';

$mform = new $class();
$mform->set_data(array('function'=>$function, 'protocol'=>$protocol));

if ($mform->is_cancelled()) {
    redirect('testclient.php');

} else if ($data = $mform->get_data()) {
    // remove unused from form data
    unset($data->submitbutton);
    unset($data->protocol);
    unset($data->function);

    // first load lib of selected protocol
    require_once("$CFG->dirroot/webservice/$protocol/locallib.php");

    $testclientclass = "webservice_{$protocol}_test_client";
    if (!class_exists($testclientclass)) {
        throw new coding_exception('Missing WS test class in protocol '.$protocol);
    }
    $testclient = new $testclientclass();

    $serverurl = "$CFG->wwwroot/webservice/$protocol/simpleserver.php";
    $serverurl .= '?wsusername='.urlencode($data->wsusername);
    unset($data->wsusername);
    $serverurl .= '&wspassword='.urlencode($data->wspassword);
    unset($data->wspassword);

    // now get the function parameters - each functions processing must be hardcoded here
    $params = array();
    if ($function === 'moodle_group_get_groups') {
        $params['groupids'] = array();
        for ($i=0; $i<10; $i++) {
            if (empty($data->groupids[$i])) {
                continue;
            }
            $params['groupids'][] = $data->groupids[$i];
        }

    } else {
        throw new coding_exception('Testing of function '.$function.' not implemented yet!');
    }

    echo $OUTPUT->header();
    echo $OUTPUT->heading(get_string('pluginname', 'webservice_'.$protocol).': '.$function);

    echo 'URL: '.s($serverurl);
    echo $OUTPUT->box_start();
    echo '<code>';

    try {
        $response = $testclient->simpletest($serverurl, $function, $params);
        echo str_replace("\n", '<br />', s(var_export($response, true)));
    } catch (Exception $ex) {
        //TODO: handle exceptions and faults without exposing of the sensitive information such as debug traces!
        echo str_replace("\n", '<br />', s($ex));
    }

    echo '</code>';
    echo $OUTPUT->box_end();
    $mform->display();
    echo $OUTPUT->footer();
    die;

} else {
    echo $OUTPUT->header();
    echo $OUTPUT->heading(get_string('pluginname', 'webservice_'.$protocol).': '.$function);
    $mform->display();
    echo $OUTPUT->footer();
    die;
}