<?php
require_once(dirname(dirname(__FILE__)) . '/config.php');

if (empty($CFG->enableportfolios)) {
    print_error('disabled', 'portfolio');
}

require_once($CFG->libdir . '/portfoliolib.php');
require_once($CFG->libdir . '/formslib.php');

$cancel = optional_param('cancel', 0, PARAM_RAW);

$exporter = null;
$dataid = 0;

if (!$dataid = optional_param('id', '', PARAM_INT) ) {
    if (isset($SESSION->portfolioexport)) {
        $dataid = $SESSION->portfolioexport;
    }
}

if ($dataid) {
    try {
        $exporter = portfolio_exporter::rewaken_object($dataid);
    } catch (portfolio_exception $e) {
        // this can happen in some cases, a cancel request is sent when something is already broken
        // so process it elegantly and move on.
        if ($cancel) {
            unset($SESSION->portfolioexport);
            redirect($CFG->wwwroot);
        } else {
            throw $e;
        }
    }
    if ($cancel) {
        $exporter->cancel_request();
    }
    // verify we still belong to the correct user and session
    $exporter->verify_rewaken();
    if (!$exporter->get('instance')) {
        if ($instance = optional_param('instance', '', PARAM_INT)) {
            try {
                $instance = portfolio_instance($instance);
            } catch (portfolio_exception $e) {
                portfolio_export_rethrow_exception($exporter, $e);
            }
            if ($broken = portfolio_instance_sanity_check($instance)) {
                throw new portfolio_export_exception($exporter, $broken[$instance->get('id')], 'portfolio_' . $instance->get('plugin'));
            }
            $instance->set('user', $USER);
            $exporter->set('instance', $instance);
            $exporter->save();
        }
    }
} else {
    // we'e just posted here for the first time and have might the instance already
    if ($instance = optional_param('instance', 0, PARAM_INT)) {
        // this can throw exceptions but there's no point catching and rethrowing here
        // as the exporter isn't created yet.
        $instance = portfolio_instance($instance);
        if ($broken = portfolio_instance_sanity_check($instance)) {
            throw new portfolio_exception($broken[$instance->get('id')], 'portfolio_' . $instance->get('plugin'));
        }
        $instance->set('user', $USER);
    } else {
        $instance = null;
    }

    $callbackfile = required_param('callbackfile', PARAM_PATH);
    $callbackclass = required_param('callbackclass', PARAM_ALPHAEXT);

    $callbackargs = array();
    foreach (array_keys(array_merge($_GET, $_POST)) as $key) {
        if (strpos($key, 'ca_') === 0) {
            if (!$value =  optional_param($key, false, PARAM_ALPHAEXT)) {
                if (!$value = optional_param($key, false, PARAM_NUMBER)) {
                    $value = optional_param($key, false, PARAM_PATH);
                }
            }
            $callbackargs[substr($key, 3)] = $value;
        }
    }
    require_once($CFG->dirroot . $callbackfile);
    $caller = new $callbackclass($callbackargs);
    $caller->set('user', $USER);
    $caller->load_data();
    if (!$caller->check_permissions()) {
        throw new portfolio_caller_exception('nopermissions', 'portfolio', $caller->get_return_url());
    }

    // for build navigation
    if (!$course = $caller->get('course')) {
        $course = optional_param('course', 0, PARAM_INT);
    }

    if (!empty($course) && is_numeric($course)) {
        $course = $DB->get_record('course', array('id' => $course), 'id,shortname,fullname');
    }

    // this is yuk but used in build_navigation
    $COURSE = $course;

    list($extranav, $cm) = $caller->get_navigation();
    $extranav[] = array('type' => 'title', 'name' => get_string('exporting', 'portfolio'));
    $navigation = build_navigation($extranav, $cm);

    $exporter = new portfolio_exporter($instance, $caller, $callbackfile, $navigation);
    $exporter->set('user', $USER);
    $exporter->set('sesskey', sesskey());
    $exporter->save();
    $SESSION->portfolioexport = $exporter->get('id');
}

if (!$exporter->get('instance')) {
    // we've just arrived but have no instance
    // so retrieve everything from the request,
    // add them as hidden fields in a new form
    // to select the instance and post back here again
    // for the next block to catch
    $mform = new portfolio_instance_select('', array('caller' => $exporter->get('caller')));
    if ($mform->is_cancelled()) {
        $exporter->cancel_request();
    } else if ($fromform = $mform->get_data()){
        redirect($CFG->wwwroot . '/portfolio/add.php?instance=' . $fromform->instance . '&amp;id=' . $exporter->get('id'));
        exit;
    }
    else {
        $exporter->print_header('selectplugin');
        print_simple_box_start();
        $mform->display();
        print_simple_box_end();
        print_footer();
        exit;
    }
}

if (!$stage = optional_param('stage', PORTFOLIO_STAGE_CONFIG)) {
    $stage = $exporter->get('stage');
}

$alreadystolen = false;
// for places returning control to pass (rather than PORTFOLIO_STAGE_PACKAGE
// which is unstable if they can't get to the constant (eg external system)
if ($postcontrol = optional_param('postcontrol', 0, PARAM_INT)) {
    try {
        $exporter->instance()->post_control($stage, array_merge($_GET, $_POST));
    } catch (portfolio_plugin_exception $e) {
        portfolio_export_rethrow_exception($exporter, $e);
    }
    $alreadystolen = true;
}

// actually do the work now..
$exporter->process_stage($stage, $alreadystolen);

?>