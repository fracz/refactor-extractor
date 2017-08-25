<?php
/**
 * Main script - SOAP server
 *
 * @author Jerome Mouneyrac <jerome@moodle.com>
 * @version 1.0
 * @package webservices
 */

/*
 * SOAP server
 */
require_once(dirname(__FILE__) . '/../../config.php');

if (empty($CFG->enablewebservices)) {
    die;
}

// retrieve the token from the url
// if the token doesn't exist, set a class containing only get_token()
$token = optional_param('token',null,PARAM_ALPHANUM);
if (empty($token)) {
    $server = new SoapServer($CFG->wwwroot."/webservice/soap/generatewsdl.php");
    $server->setClass("soap_authentication");
    $server->handle();
} else { // if token exist, do the authentication here
    /// TODO: following function will need to be modified
    $user = mock_check_token($token);
    if (empty($user)) {
        throw new moodle_exception('wrongidentification');
    } else {
        /// TODO: probably change this
        global $USER;
        $USER = $user;
    }

    //retrieve the api name
    $classpath = optional_param(classpath,null,PARAM_ALPHA);
    require_once(dirname(__FILE__) . '/../../'.$classpath.'/external.php');

    /// run the server
    $server = new SoapServer($CFG->wwwroot."/webservice/soap/generatewsdl.php?token=".$token); //TODO: need to call the wsdl generation on the fly
    $server->setClass($classpath."_external"); //TODO: pass $user as parameter
    $server->handle();
}


function mock_check_token($token) {
    //fake test
    if ($token == 465465465468468464) {
        ///retrieve the user
        global $DB;
        $user = $DB->get_record('user', array('username'=>'wsuser', 'mnethostid'=>1));

        if (empty($user)) {
            return false;
        }

        return $user;
    } else {
        return false;
    }
}

class soap_authentication {
    function tmp_get_token($params) {
        if ($params['username'] == 'wsuser' && $params['password'] == 'wspassword') {
                return '465465465468468464';
            } else {
                throw new moodle_exception('wrongusernamepassword');
            }
    }
}
?>