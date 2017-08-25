<?php
/**
 * Moodle - Modular Object-Oriented Dynamic Learning Environment
 *          http://moodle.org
 * Copyright (C) 1999 onwards Martin Dougiamas  http://dougiamas.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @package    moodle
 * @subpackage portfolio
 * @author     Penny Leach <penny@catalyst.net.nz>
 * @license    http://www.gnu.org/copyleft/gpl.html GNU GPL
 * @copyright  (C) 1999 onwards Martin Dougiamas  http://dougiamas.com
 *
 * This file contains the class definition for the exporter object.
 */

/**
* The class that handles the various stages of the actual export
* and the communication between the caller and the portfolio plugin.
* this is stored in the database between page requests in serialized base64 encoded form
* also contains helper methods for the plugin and caller to use (at the end of the file)
* {@see get_base_filearea} - where to write files to
* {@see write_new_file} - write some content to a file in the export filearea
* {@see copy_existing_file} - copy an existing file into the export filearea
* {@see get_tempfiles} - return list of all files in the export filearea
*/
class portfolio_exporter {

    /**
    * the caller object used during the export
    */
    private $caller;

    /** the portfolio plugin instanced used during the export
    */
    private $instance;

    /**
    * if there has been no config form displayed to the user
    */
    private $noconfig;

    /**
    * the navigation to display on the wizard screens
    * built from build_navigation
    */
    private $navigation;

    /**
    * the user currently exporting content
    * always $USER, but more conveniently placed here
    */
    private $user;

    /** the file to include that contains the class defintion
    * of the portfolio instance plugin
    * used to re-waken the object after sleep
    */
    public $instancefile;

    /**
    * the file to include that contains the class definition
    * of the caller object
    * used to re-waken the object after sleep
    */
    public $callerfile;

    /**
    * the current stage of the export
    */
    private $stage;

    /**
    * whether something (usually the portfolio plugin)
    * has forced queuing
    */
    private $forcequeue;

    /**
    * id of this export
    * matches record in portfolio_tempdata table
    * and used for itemid for file storage.
    */
    private $id;

    /**
    * the session key during the export
    * used to avoid hijacking transfers
    */
    private $sesskey;

    /**
    * array of stages that have had the portfolio plugin already steal control from them
    */
    private $alreadystolen;

    /**
    * construct a new exporter for use
    *
    * @param portfolio_plugin_base subclass $instance portfolio instance (passed by reference)
    * @param portfolio_caller_base subclass $caller portfolio caller (passed by reference)
    * @param string $callerfile path to callerfile (relative to dataroot)
    * @param string $navigation result of build_navigation (passed to print_header)
    */
    public function __construct(&$instance, &$caller, $callerfile, $navigation) {
        $this->instance =& $instance;
        $this->caller =& $caller;
        if ($instance) {
            $this->instancefile = 'portfolio/type/' . $instance->get('plugin') . '/lib.php';
            $this->instance->set('exporter', $this);
        }
        $this->callerfile = $callerfile;
        $this->stage = PORTFOLIO_STAGE_CONFIG;
        $this->navigation = $navigation;
        $this->caller->set('exporter', $this);
        $this->alreadystolen = array();
    }

    /*
    * generic getter for properties belonging to this instance
    * <b>outside</b> the subclasses
    * like name, visible etc.
    */
    public function get($field) {
        if (property_exists($this, $field)) {
            return $this->{$field};
        }
        $a = (object)array('property' => $field, 'class' => get_class($this));
        throw new portfolio_export_exception($this, 'invalidproperty', 'portfolio', null, $a);
    }

    /**
    * generic setter for properties belonging to this instance
    * <b>outside</b> the subclass
    * like name, visible, etc.
    */
    public function set($field, &$value) {
        if (property_exists($this, $field)) {
            $this->{$field} =& $value;
            if ($field == 'instance') {
                $this->instancefile = 'portfolio/type/' . $this->instance->get('plugin') . '/lib.php';
                $this->instance->set('exporter', $this);
            }
            $this->dirty = true;
            return true;
        }
        $a = (object)array('property' => $field, 'class' => get_class($this));
        throw new portfolio_export_exception($this, 'invalidproperty', 'portfolio', null, $a);

    }

    /**
    * process the given stage calling whatever functions are necessary
    *
    * @param int $stage (see PORTFOLIO_STAGE_* constants)
    * @param boolean $alreadystolen used to avoid letting plugins steal control twice.
    *
    * @return boolean whether or not to process the next stage. this is important as the function is called recursively.
    */
    public function process_stage($stage, $alreadystolen=false) {
        $this->set('stage', $stage);
        if ($alreadystolen) {
            $this->alreadystolen[$stage] = true;
        } else {
            if (!array_key_exists($stage, $this->alreadystolen)) {
                $this->alreadystolen[$stage] = false;
            }
        }
        $this->save();
        if (!$this->alreadystolen[$stage] && $url = $this->instance->steal_control($stage)) {
            redirect($url);
            break;
        }

        $waiting = $this->instance->get_export_config('wait');
        if ($stage > PORTFOLIO_STAGE_QUEUEORWAIT && empty($waiting)) {
            $stage = PORTFOLIO_STAGE_FINISHED;
        }
        $functionmap = array(
            PORTFOLIO_STAGE_CONFIG        => 'config',
            PORTFOLIO_STAGE_CONFIRM       => 'confirm',
            PORTFOLIO_STAGE_QUEUEORWAIT   => 'queueorwait',
            PORTFOLIO_STAGE_PACKAGE       => 'package',
            PORTFOLIO_STAGE_CLEANUP       => 'cleanup',
            PORTFOLIO_STAGE_SEND          => 'send',
            PORTFOLIO_STAGE_FINISHED      => 'finished'
        );

        $function = 'process_stage_' . $functionmap[$stage];
        try {
            if ($this->$function()) {
                // if we get through here it means control was returned
                // as opposed to wanting to stop processing
                // eg to wait for user input.
                $this->save();
                $stage++;
                return $this->process_stage($stage);
            } else {
                $this->save();
                return false;
            }
        } catch (portfolio_caller_exception $e) {
            portfolio_export_rethrow_exception($this, $e);
        } catch (portfolio_plugin_exception $e) {
            portfolio_export_rethrow_exception($this, $e);
        } catch (portfolio_export_exception $e) {
            throw $e;
        } catch (Exception $e) {
            debugging(get_string('thirdpartyexception', 'portfolio', get_class($e)));
            portfolio_export_rethrow_exception($this, $e);
        }
    }

    /**
    * helper function to return the portfolio instance
    *
    * @return  portfolio_plugin_base subclass
    */
    public function instance() {
        return $this->instance;
    }

    /**
    * helper function to return the caller object
    *
    * @return portfolio_caller_base subclass
    */
    public function caller() {
        return $this->caller;
    }

    /**
    * processes the 'config' stage of the export
    *
    * @return boolean whether or not to process the next stage. this is important as the control function is called recursively.
    */
    public function process_stage_config() {

        $pluginobj = $callerobj = null;
        if ($this->instance->has_export_config()) {
            $pluginobj = $this->instance;
        }
        if ($this->caller->has_export_config()) {
            $callerobj = $this->caller;
        }
        $formats = portfolio_supported_formats_intersect($this->caller->supported_formats($this->caller), $this->instance->supported_formats());
        $expectedtime = $this->instance->expected_time($this->caller->expected_time());
        if (count($formats) == 0) {
            // something went wrong, we should not have gotten this far.
            throw new portfolio_export_exception($this, 'nocommonformats', 'portfolio', null, get_class($this->caller));
        }
        // even if neither plugin or caller wants any config, we have to let the user choose their format, and decide to wait.
        if ($pluginobj || $callerobj || count($formats) > 1 || ($expectedtime != PORTFOLIO_TIME_LOW && $expectedtime != PORTFOLIO_TIME_FORCEQUEUE)) {
            $customdata = array(
                'instance' => $this->instance,
                'plugin' => $pluginobj,
                'caller' => $callerobj,
                'userid' => $this->user->id,
                'formats' => $formats,
                'expectedtime' => $expectedtime,
            );
            $mform = new portfolio_export_form('', $customdata);
            if ($mform->is_cancelled()){
                $this->cancel_request();
            } else if ($fromform = $mform->get_data()){
                if (!confirm_sesskey()) {
                    throw new portfolio_export_exception($this, 'confirmsesskeybad');
                }
                $pluginbits = array();
                $callerbits = array();
                foreach ($fromform as $key => $value) {
                    if (strpos($key, 'plugin_') === 0) {
                        $pluginbits[substr($key, 7)]  = $value;
                    } else if (strpos($key, 'caller_') === 0) {
                        $callerbits[substr($key, 7)] = $value;
                    }
                }
                $callerbits['format'] = $pluginbits['format'] = $fromform->format;
                $pluginbits['wait'] = $fromform->wait;
                if ($expectedtime == PORTFOLIO_TIME_LOW) {
                    $pluginbits['wait'] = 1;
                    $pluginbits['hidewait'] = 1;
                } else if ($expectedtime == PORTFOLIO_TIME_FORCEQUEUE) {
                    $pluginbits['wait'] = 0;
                    $pluginbits['hidewait'] = 1;
                    $this->forcequeue = true;
                }
                $callerbits['hideformat'] = $pluginbits['hideformat'] = (count($formats) == 1);
                $this->caller->set_export_config($callerbits);
                $this->instance->set_export_config($pluginbits);
                return true;
            } else {
                $this->print_header('configexport');
                print_simple_box_start();
                $mform->display();
                print_simple_box_end();
                print_footer();
                return false;;
            }
        } else {
            $this->noexportconfig = true;
            $format = array_shift($formats);
            $config = array(
                'hidewait' => 1,
                'wait' => (($expectedtime == PORTFOLIO_TIME_LOW) ? 1 : 0),
                'format' => $format,
                'hideformat' => 1
            );
            $this->instance->set_export_config($config);
            $this->caller->set_export_config(array('format' => $format, 'hideformat' => 1));
            if ($expectedtime == PORTFOLIO_TIME_FORCEQUEUE) {
                $this->forcequeue = true;
            }
            return true;
            // do not break - fall through to confirm
        }
    }

    /**
    * processes the 'confirm' stage of the export
    *
    * @return boolean whether or not to process the next stage. this is important as the control function is called recursively.
    */
    public function process_stage_confirm() {
        global $CFG, $DB;

        $previous = $DB->get_records(
            'portfolio_log',
            array(
                'userid'      => $this->user->id,
                'portfolio'   => $this->instance->get('id'),
                'caller_sha1' => $this->caller->get_sha1(),
            )
        );
        if (isset($this->noexportconfig) && empty($previous)) {
            return true;
        }
        $strconfirm = get_string('confirmexport', 'portfolio');
        $yesurl = $CFG->wwwroot . '/portfolio/add.php?stage=' . PORTFOLIO_STAGE_QUEUEORWAIT;
        $nourl  = $CFG->wwwroot . '/portfolio/add.php?cancel=1';
        $this->print_header('confirmexport');
        print_simple_box_start();
        print_heading(get_string('confirmsummary', 'portfolio'), '', 4);
        $mainsummary = array();
        if (!$this->instance->get_export_config('hideformat')) {
            $mainsummary[get_string('selectedformat', 'portfolio')] = get_string('format_' . $this->instance->get_export_config('format'), 'portfolio');
        }
        if (!$this->instance->get_export_config('hidewait')) {
            $mainsummary[get_string('selectedwait', 'portfolio')] = get_string(($this->instance->get_export_config('wait') ? 'yes' : 'no'));
        }
        if ($previous) {
            $previousstr = '';
            foreach ($previous as $row) {
                $previousstr .= userdate($row->time);
                if ($row->caller_class != get_class($this->caller)) {
                    require_once($CFG->dirroot . '/' . $row->caller_file);
                    $previousstr .= ' (' . call_user_func(array($row->caller_class, 'display_name')) . ')';
                }
                $previousstr .= '<br />';
            }
            $mainsummary[get_string('exportedpreviously', 'portfolio')] = $previousstr;
        }
        if (!$csummary = $this->caller->get_export_summary()) {
            $csummary = array();
        }
        if (!$isummary = $this->instance->get_export_summary()) {
            $isummary = array();
        }
        $mainsummary = array_merge($mainsummary, $csummary, $isummary);
        $table = new StdClass;
        $table->data = array();
        foreach ($mainsummary as $string => $value) {
            $table->data[] = array($string, $value);
        }
        print_table($table);
        notice_yesno($strconfirm, $yesurl, $nourl);
        print_simple_box_end();
        print_footer();
        return false;
    }

    /**
    * processes the 'queueornext' stage of the export
    *
    * @return boolean whether or not to process the next stage. this is important as the control function is called recursively.
    */
    public function process_stage_queueorwait() {
        global $SESSION;
        $wait = $this->instance->get_export_config('wait');
        if (empty($wait)) {
            events_trigger('portfolio_send', $this->id);
            unset($SESSION->portfolioexport);
            return $this->process_stage_finished(true);
        }
        return true;
    }

    /**
    * processes the 'package' stage of the export
    *
    * @return boolean whether or not to process the next stage. this is important as the control function is called recursively.
    */
    public function process_stage_package() {
        // now we've agreed on a format,
        // the caller is given control to package it up however it wants
        // and then the portfolio plugin is given control to do whatever it wants.
        if (!$this->caller->prepare_package()) {
            throw new portfolio_export_exception($this, 'callercouldnotpackage', 'portfolio');
        }
        if (!$package = $this->instance->prepare_package()) {
            throw new portfolio_export_exception($this, 'plugincouldnotpackage', 'portfolio');
        }
        return true;
    }

    /**
    * processes the 'cleanup' stage of the export
    *
    * @return boolean whether or not to process the next stage. this is important as the control function is called recursively.
    */
    public function process_stage_cleanup($pullok=false) {
        global $CFG, $DB, $SESSION;

        if (!$pullok && $this->get('instance') && !$this->get('instance')->is_push()) {
            unset($SESSION->portfolioexport);
            return true;
        }
        if ($this->get('instance')) {
            // might not be set - before export really starts
            $this->get('instance')->cleanup();
        }
        $DB->delete_records('portfolio_tempdata', array('id' => $this->id));
        $fs = get_file_storage();
        $fs->delete_area_files(SYSCONTEXTID, 'portfolio_exporter', $this->id);
        unset($SESSION->portfolioexport);
        return true;
    }

    /**
    * processes the 'send' stage of the export
    *
    * @return boolean whether or not to process the next stage. this is important as the control function is called recursively.
    */
    public function process_stage_send() {
        // send the file
        if (!$this->instance->send_package()) {
            throw new portfolio_export_exception($this, 'failedtosendpackage', 'portfolio');
        }
        // log the transfer
        global $DB;
        $l = array(
            'userid'         => $this->user->id,
            'portfolio'      => $this->instance->get('id'),
            'caller_file'    => $this->callerfile,
            'caller_sha1'    => $this->caller->get_sha1(),
            'caller_class'   => get_class($this->caller),
            'time'           => time(),
        );
        $DB->insert_record('portfolio_log', $l);
        return true;
    }

    /**
    * processes the 'finish' stage of the export
    *
    * @return boolean whether or not to process the next stage. this is important as the control function is called recursively.
    */
    public function process_stage_finished($queued=false) {
        $returnurl = $this->caller->get_return_url();
        $continueurl = $this->instance->get_continue_url();
        $extras = $this->instance->get_extra_finish_options();

        $key = 'exportcomplete';
        if ($queued) {
            $key = 'exportqueued';
            if ($this->forcequeue) {
                $key = 'exportqueuedforced';
            }
        }
        $this->print_header($key, false);
        if ($returnurl) {
            echo '<a href="' . $returnurl . '">' . get_string('returntowhereyouwere', 'portfolio') . '</a><br />';
        }
        if ($continueurl) {
            echo '<a href="' . $continueurl . '">' . get_string('continuetoportfolio', 'portfolio') . '</a><br />';
        }
        if (is_array($extras)) {
            foreach ($extras as $link => $string) {
                echo '<a href="' . $link . '">' . $string . '</a><br />';
            }
        }
        print_footer();
        return false;
    }


    /**
    * local print header function to be reused across the export
    *
    * @param string $titlestring key for a portfolio language string
    * @param string $headerstring key for a portfolio language string
    */
    public function print_header($headingstr, $summary=true) {
        $titlestr = get_string('exporting', 'portfolio');
        $headerstr = get_string('exporting', 'portfolio');

        print_header($titlestr, $headerstr, $this->navigation);
        print_heading(get_string($headingstr, 'portfolio'));

        if (!$summary) {
            return;
        }

        print_simple_box_start();
        echo $this->caller->heading_summary();
        print_simple_box_end();
    }

    /**
    * cancels a potfolio request and cleans up the tempdata
    * and redirects the user back to where they started
    */
    public function cancel_request() {
        if (!isset($this)) {
            return;
        }
        $this->process_stage_cleanup(true);
        redirect($this->caller->get_return_url());
        exit;
    }

    /**
    * writes out the contents of this object and all its data to the portfolio_tempdata table and sets the 'id' field.
    */
    public function save() {
        global $DB;
        if (empty($this->id)) {
            $r = (object)array(
                'data' => base64_encode(serialize($this)),
                'expirytime' => time() + (60*60*24),
                'userid' => $this->user->id,
            );
            $this->id = $DB->insert_record('portfolio_tempdata', $r);
            $this->save(); // call again so that id gets added to the save data.
        } else {
            $DB->set_field('portfolio_tempdata', 'data', base64_encode(serialize($this)), array('id' => $this->id));
        }
    }

    /**
    * rewakens the data from the database given the id
    * makes sure to load the required files with the class definitions
    *
    * @param int $id id of data
    *
    * @return portfolio_exporter
    */
    public static function rewaken_object($id) {
        global $DB, $CFG;
        require_once($CFG->libdir . '/filelib.php');
        if (!$data = $DB->get_record('portfolio_tempdata', array('id' => $id))) {
            throw new portfolio_exception('invalidtempid', 'portfolio');
        }
        $exporter = unserialize(base64_decode($data->data));
        if ($exporter->instancefile) {
            require_once($CFG->dirroot . '/' . $exporter->instancefile);
        }
        require_once($CFG->dirroot . '/' . $exporter->callerfile);
        $exporter = unserialize(serialize($exporter));
        return $exporter;
    }

    /**
    * helper function to create the beginnings of a file_record object
    * to create a new file in the portfolio_temporary working directory
    * use {@see write_new_file} or {@see copy_existing_file} externally
    *
    * @param string $name filename of new record
    */
    private function new_file_record_base($name) {
        return (object)array_merge($this->get_base_filearea(), array(
            'filepath' => '/',
            'filename' => $name,
        ));
    }

    /**
    * verifies a rewoken object
    *
    * checks to make sure it belongs to the same user and session as is currently in use.
    *
    * @throws portfolio_exception
    */
    public function verify_rewaken() {
        global $USER;
        if ($this->get('user')->id != $USER->id) {
            throw new portfolio_exception('notyours', 'portfolio');
        }
        if (!confirm_sesskey($this->get('sesskey'))) {
            throw new portfolio_exception('confirmsesskeybad');
        }
    }
    /**
    * copies a file from somewhere else in moodle
    * to the portfolio temporary working directory
    * associated with this export
    *
    * @param $oldfile stored_file object
    * @return new stored_file object
    */
    public function copy_existing_file($oldfile) {
        $fs = get_file_storage();
        $file_record = $this->new_file_record_base($oldfile->get_filename());
        try {
            return $fs->create_file_from_storedfile($file_record, $oldfile->get_id());
        } catch (file_exception $e) {
            return false;
        }
    }

    /**
    * writes out some content to a file in the
    * portfolio temporary working directory
    * associated with this export
    *
    * @param string $content content to write
    * @param string $name filename to use
    * @return new stored_file object
    */
    public function write_new_file($content, $name) {
        $fs = get_file_storage();
        $file_record = $this->new_file_record_base($name);
        return $fs->create_file_from_string($file_record, $content);
    }

    /**
    * returns an arary of files in the temporary working directory
    * for this export
    * always use this instead of the files api directly
    *
    * @return array of stored_file objects keyed by name
    */
    public function get_tempfiles() {
        $fs = get_file_storage();
        $files = $fs->get_area_files(SYSCONTEXTID, 'portfolio_exporter', $this->id, '', false);
        if (empty($files)) {
            return array();
        }
        $returnfiles = array();
        foreach ($files as $f) {
            $returnfiles[$f->get_filename()] = $f;
        }
        return $returnfiles;
    }

    /**
    * returns the context, filearea, and itemid
    * parts of a filearea (not filepath) to be used by
    * plugins if they want to do things like zip up the contents of
    * the temp area to here, or something that can't be done just using
    * write_new_file,  copy_existing_file or get_tempfiles
    *
    * @return array contextid, filearea, itemid are the keys.
    */
    public function get_base_filearea() {
        return array(
            'contextid' => SYSCONTEXTID,
            'filearea' => 'portfolio_exporter',
            'itemid'   => $this->id,
        );
    }

}

?>