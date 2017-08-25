<?php
///////////////////////////////////////////////////////////////////////////
//                                                                       //
// This file is part of Moodle - http://moodle.org/                      //
// Moodle - Modular Object-Oriented Dynamic Learning Environment         //
//                                                                       //
// Moodle is free software: you can redistribute it and/or modify        //
// it under the terms of the GNU General Public License as published by  //
// the Free Software Foundation, either version 3 of the License, or     //
// (at your option) any later version.                                   //
//                                                                       //
// Moodle is distributed in the hope that it will be useful,             //
// but WITHOUT ANY WARRANTY; without even the implied warranty of        //
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the         //
// GNU General Public License for more details.                          //
//                                                                       //
// You should have received a copy of the GNU General Public License     //
// along with Moodle.  If not, see <http://www.gnu.org/licenses/>.       //
//                                                                       //
///////////////////////////////////////////////////////////////////////////

/*
 * @package    course
 * @subpackage publish
 * @author     Jerome Mouneyrac <jerome@mouneyrac.com>
 * @license    http://www.gnu.org/copyleft/gpl.html GNU GPL
 * @copyright  (C) 1999 onwards Martin Dougiamas  http://dougiamas.com
 *
 * The forms used for course publication
*/


require_once($CFG->dirroot.'/lib/formslib.php');
require_once($CFG->dirroot."/lib/hublib.php");

/*
 * Hub selector to choose on which hub we want to publish.
*/
class hub_publish_selector_form extends moodleform {

    public function definition() {
        global $CFG;
        $mform =& $this->_form;
        $share = $this->_customdata['share'];

        $mform->addElement('header', 'site', get_string('selecthub', 'hub'));

        $hubmanager = new hub();
        $registeredhubs = $hubmanager->get_registered_on_hubs();

        //Public hub list
        $options = array();
        foreach ($registeredhubs as $hub) {
            $options[$hub->huburl] = $hub->huburl;
            $hubname = $hub->hubname;
            $mform->addElement('hidden', clean_param($hub->huburl, PARAM_ALPHANUMEXT), $hubname);
        }
        $mform->addElement('select', 'huburl', get_string('publichub','hub'),
                $options, array("size" => 15));

        $mform->addElement('hidden', 'id', $this->_customdata['id']);

        if ($share) {
            $buttonlabel = get_string('shareonhub', 'hub');
            $mform->addElement('hidden', 'share', true);
        } else {
            $buttonlabel = get_string('advertiseonhub', 'hub');
            $mform->addElement('hidden', 'advertise', true);
        }

        $this->add_action_buttons(false, $buttonlabel);
    }

}

/*
 * Course publication form
*/
class course_publication_form extends moodleform {

    public function definition() {
        global $CFG, $DB, $USER;

        $strrequired = get_string('required');
        $mform =& $this->_form;
        $huburl = $this->_customdata['huburl'];
        $hubname = $this->_customdata['hubname'];
        $course = $this->_customdata['course'];
        $advertise = $this->_customdata['advertise'];
        $share = $this->_customdata['share'];
        $site = get_site();

        //hidden parameters
        $mform->addElement('hidden', 'huburl', $huburl);
        $mform->addElement('hidden', 'hubname', $hubname);

        //check on the hub if the course has already been published
        $hub = new hub();
        $registeredhub = $hub->get_registeredhub($huburl);
        $publications = $hub->get_publications($registeredhub->id, $course->id, $advertise);

        if (!empty($publications)) {
            //get the last publication of this course
            $publication = array_pop($publications);

            $function = 'hub_get_courses';
            $options = new stdClass();
            $options->ids = array($publication->hubcourseid);
            $options->allsitecourses = 1;
            $params = array('', $share, !$share, $options);
            $serverurl = $huburl."/local/hub/webservice/webservices.php";
            require_once($CFG->dirroot."/webservice/xmlrpc/lib.php");
            $xmlrpcclient = new webservice_xmlrpc_client();
            $publishedcourses = $xmlrpcclient->call($serverurl, $registeredhub->token, $function, $params);
        }


        if (!empty($publishedcourses)) {
            $publishedcourse = $publishedcourses[0];
            $defaultfullname = $publishedcourse['fullname'];
            $defaultshortname = $publishedcourse['shortname'];
            $defaultsummary = $publishedcourse['description'];
            $defaultlanguage = $publishedcourse['language'];
            $defaultpublishername = $publishedcourse['publishername'];
            $defaultcontributornames = $publishedcourse['contributornames'];
            $defaultcoverage = $publishedcourse['coverage'];
            $defaultcreatorname = $publishedcourse['creatorname'];
            $defaultlicenceshortname = $publishedcourse['licenceshortname'];
            $defaultsubject = $publishedcourse['subject'];
            $defaultaudience = $publishedcourse['audience'];
            $defaulteducationallevel = $publishedcourse['educationallevel'];
            $defaultcreatornotes = $publishedcourse['creatornotes'];

        } else {
            $defaultfullname = $course->fullname;
            $defaultshortname = $course->shortname;
            $defaultsummary =  $course->summary;
            if (empty($course->lang)) {
                $language = get_site()->lang;
                if (empty($language)) {
                    $defaultlanguage =  current_language();
                } else {
                    $defaultlanguage =  $language;
                }
            } else {
                $defaultlanguage =  $course->lang;
            }
            $defaultpublishername = $USER->firstname.' '.$USER->lastname;
            $defaultcontributornames = '';
            $defaultcoverage = '';
            $defaultcreatorname = $USER->firstname.' '.$USER->lastname;
            $defaultlicenceshortname = 'cc';
            $defaultsubject = '';
            $defaultaudience = AUDIENCE_EDUCATORS;
            $defaulteducationallevel = EDULEVEL_TERTIARY;
            $defaultcreatornotes = '';
        }


        //the input parameters
        $mform->addElement('header', 'moodle', get_string('publicationinfo', 'hub'));

        $mform->addElement('text','name' , get_string('coursename', 'hub'));
        $mform->addRule('name', $strrequired, 'required', null, 'client');
        $mform->setType('name', PARAM_TEXT);
        $mform->setDefault('name', $defaultfullname);

        $mform->addElement('hidden', 'id', $this->_customdata['id']);

        if ($share) {
            $buttonlabel = get_string('shareon', 'hub', !empty($hubname)?$hubname:$huburl);

            $mform->addElement('hidden', 'share', $share);

            $mform->addElement('text', 'demourl', get_string('demourl', 'hub'));
            $mform->setType('demourl', PARAM_URL);
            $mform->setDefault('demourl', new moodle_url("/course/view.php?id=".$course->id));
            $mform->addHelpButton('demourl', 'demourl', 'hub');
        }

        if ($advertise) {
            if (empty($publications)) {
               $buttonlabel = get_string('advertiseon', 'hub', !empty($hubname)?$hubname:$huburl);
            } else {
               $buttonlabel = get_string('readvertiseon', 'hub', !empty($hubname)?$hubname:$huburl);
            }
            $mform->addElement('hidden', 'advertise', $advertise);

            $mform->addElement('static', 'courseurl', get_string('courseurl', 'hub'));
            $mform->setType('courseurl', PARAM_URL);
            $mform->setDefault('courseurl', new moodle_url("/course/view.php?id=".$course->id));
        }

        $mform->addElement('text', 'courseshortname',get_string('courseshortname', 'hub'));
        $mform->setDefault('courseshortname', $defaultshortname);
        $mform->addHelpButton('courseshortname', 'courseshortname', 'hub');

        $mform->addElement('textarea', 'description', get_string('description'), array('rows'=>10));
        $mform->addRule('description', $strrequired, 'required', null, 'client');
        $mform->setDefault('description', $defaultsummary);
        $mform->setType('description', PARAM_TEXT);

        $languages = get_string_manager()->get_list_of_languages();
        asort($languages, SORT_LOCALE_STRING);
        $mform->addElement('select', 'language',get_string('language'), $languages);
        $mform->setDefault('language', $defaultlanguage);


        $mform->addElement('text', 'publishername',get_string('publishername', 'hub'));
        $mform->setDefault('publishername', $defaultpublishername);
        $mform->addRule('publishername', $strrequired, 'required', null, 'client');
        $mform->addHelpButton('publishername', 'publishername', 'hub');

        $mform->addElement('text', 'contributornames', get_string('contributornames', 'hub'));
        $mform->setDefault('contributornames', $defaultcontributornames);
        $mform->addHelpButton('contributornames', 'contributornames', 'hub');

        $mform->addElement('text','coverage' , get_string('tags', 'hub'));
        $mform->setType('coverage', PARAM_TEXT);
        $mform->setDefault('coverage', $defaultcoverage);
        $mform->addHelpButton('coverage', 'tags', 'hub');

        $mform->addElement('text', 'creatorname', get_string('creatorname', 'hub'));
        $mform->addRule('creatorname', $strrequired, 'required', null, 'client');
        $mform->setType('creatorname', PARAM_TEXT);
        $mform->setDefault('creatorname', $defaultcreatorname);
        $mform->addHelpButton('creatorname', 'creatorname', 'hub');

        require_once($CFG->dirroot."/lib/licenselib.php");
        $licensemanager = new license_manager();
        $licences = $licensemanager->get_licenses();
        $options = array();
        foreach ($licences as $license) {
            $options[$license->shortname] = get_string($license->shortname, 'license');
        }
        $mform->addElement('select', 'licence', get_string('license'), $options);
        $mform->setDefault('licence', $defaultlicenceshortname);
        unset($options);
        $mform->addHelpButton('licence', 'licence', 'hub');

        $options = get_string_manager()->load_component_strings('edufields', current_language());
        foreach ($options as $key => &$option) {
            $keylength = strlen ( $key );
            if ( $keylength == 10) {
                $option = "&nbsp;&nbsp;" . $option;
            } else  if ( $keylength == 12) {
                $option = "&nbsp;&nbsp;&nbsp;&nbsp;" . $option;
            }
        }
        $mform->addElement('select', 'subject', get_string('subject', 'hub'), $options);
        unset($options);
        $mform->addHelpButton('subject', 'subject', 'hub');
        $mform->setDefault('subject', $defaultsubject);

        $options = array();
        $options[AUDIENCE_EDUCATORS] = get_string('audienceeducators', 'hub');
        $options[AUDIENCE_STUDENTS] = get_string('audiencestudents', 'hub');
        $options[AUDIENCE_ADMINS] = get_string('audienceadmins', 'hub');
        $mform->addElement('select', 'audience', get_string('audience', 'hub'), $options);
        $mform->setDefault('audience', $defaultaudience);
        unset($options);
        $mform->addHelpButton('audience', 'audience', 'hub');

        $options = array();
        $options[EDULEVEL_PRIMARY] = get_string('edulevelprimary', 'hub');
        $options[EDULEVEL_SECONDARY] = get_string('edulevelsecondary', 'hub');
        $options[EDULEVEL_TERTIARY] = get_string('eduleveltertiary', 'hub');
        $options[EDULEVEL_GOVERNMENT] = get_string('edulevelgovernment', 'hub');
        $options[EDULEVEL_ASSOCIATION] = get_string('edulevelassociation', 'hub');
        $options[EDULEVEL_CORPORATE] = get_string('edulevelcorporate', 'hub');
        $options[EDULEVEL_OTHER] = get_string('edulevelother', 'hub');
        $mform->addElement('select', 'educationallevel', get_string('educationallevel', 'hub'), $options);
        $mform->setDefault('educationallevel', $defaulteducationallevel);
        unset($options);
        $mform->addHelpButton('educationallevel', 'educationallevel', 'hub');

        $editoroptions = array('maxfiles'=>0, 'maxbytes'=>0, 'trusttext'=>false, 'forcehttps'=>false);
        $mform->addElement('editor', 'creatornotes', get_string('creatornotes', 'hub'), '', $editoroptions);
        $mform->addRule('creatornotes', $strrequired, 'required', null, 'client');
        $mform->setDefault('creatornotes', $defaultcreatornotes);
        $mform->setType('creatornotes', PARAM_CLEANHTML);
        $mform->addHelpButton('creatornotes', 'creatornotes', 'hub');



//        $mform->addElement('filemanager', 'screenshots', get_string('screenshots','hub'), null,
//                array('subdirs'=>0,
//                'maxbytes'=>1000000,
//                'maxfiles'=>3
//        ));
//        $mform->setHelpButton('screenshots', array('screenshots', get_string('screenshots', 'hub'), 'hub'));
//        $mform->addHelpButton('screenshots', 'screenshots', 'hub');

        $this->add_action_buttons(false, $buttonlabel);
    }

    function validation($data, $files) {
        global $CFG;

        $errors = array();

        if (!(strlen($this->_form->_submitValues['subject']) == 12 or $this->_form->_submitValues['subject'] == 'all')) {
            $errors['subject'] = get_string('cannotselecttopsubject', 'block_community');
        }

        return $errors;
    }



}

?>