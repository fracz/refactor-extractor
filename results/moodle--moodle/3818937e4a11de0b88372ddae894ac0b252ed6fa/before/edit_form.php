<?php  //$Id$

require_once $CFG->libdir.'/formslib.php';

class edit_outcome_form extends moodleform {
    function definition() {
        global $CFG, $COURSE;
        $mform =& $this->_form;

        // visible elements
        $mform->addElement('header', 'general', get_string('outcomes', 'grades'));

        $mform->addElement('text', 'fullname', get_string('fullname'));
        $mform->addRule('fullname', get_string('required'), 'required');
        $mform->setType('fullname', PARAM_TEXT);

        $mform->addElement('text', 'shortname', get_string('shortname'));
        $mform->addRule('shortname', get_string('required'), 'required');
        $mform->setType('shortname', PARAM_NOTAGS);

        $mform->addElement('advcheckbox', 'standard', get_string('outcomestandard', 'grades'));

        $options = array();

        $mform->addElement('select', 'scaleid', get_string('scale'), $options);
        $mform->addRule('scaleid', get_string('required'), 'required');


        // hidden params
        $mform->addElement('hidden', 'id', 0);
        $mform->setType('id', PARAM_INT);

        $mform->addElement('hidden', 'courseid', 0);
        $mform->setType('courseid', PARAM_INT);

/// add return tracking info
        $gpr = $this->_customdata['gpr'];
        $gpr->add_mform_elements($mform);

//-------------------------------------------------------------------------------
        // buttons
        $this->add_action_buttons();
    }


/// tweak the form - depending on existing data
    function definition_after_data() {
        global $CFG;

        $mform =& $this->_form;

        // first load proper scales
        if ($courseid = $mform->getElementValue('courseid')) {
            $options = array();
            if ($scales = grade_scale::fetch_all_local($courseid)) {
                $options[-1] = '--'.get_string('scalescustom');
                foreach($scales as $scale) {
                    $options[$scale->id] = $scale->get_name();
                }
            }
            if ($scales = grade_scale::fetch_all_global()) {
                $options[-2] = '--'.get_string('scalesstandard');
                foreach($scales as $scale) {
                    $options[$scale->id] = $scale->get_name();
                }
            }
            $scale_el =& $mform->getElement('scaleid');
            $scale_el->load($options);

        } else {
            $options = array();
            if ($scales = grade_scale::fetch_all_global()) {
                foreach($scales as $scale) {
                    $options[$scale->id] = $scale->get_name();
                }
            }
            $scale_el =& $mform->getElement('scaleid');
            $scale_el->load($options);
        }

        if ($id = $mform->getElementValue('id')) {
            $outcome = grade_outcome::fetch(array('id'=>$id));
            $count = $outcome->get_uses_count();

            if ($count) {
                $mform->hardFreeze('scaleid');
            }

            if (empty($courseid)) {
                $mform->hardFreeze('standard');

            } else if (empty($outcome->courseid) and !has_capability('moodle/grade:manage', get_context_instance(CONTEXT_SYSTEM))) {
                $mform->hardFreeze('standard');

            } else if ($count and !empty($outcome->courseid)) {
                $mform->hardFreeze('standard');
            }


        } else {
            if (empty($courseid) or !has_capability('moodle/grade:manage', get_context_instance(CONTEXT_SYSTEM))) {
                $mform->hardFreeze('standard');
            }
        }
    }

/// perform extra validation before submission
    function validation($data){
        $errors = array();

        if ($data['scaleid'] < 1) {
            $errors['scaleid'] = get_string('required');
        }

        if (!empty($data['standard']) and $scale = grade_scale::fetch(array('id'=>$data['scaleid']))) {
            if (!empty($scale->courseid)) {
                //TODO: localize
                $errors['scaleid'] = 'Can not use custom scale in global outcome!';
            }
        }

        if (0 == count($errors)){
            return true;
        } else {
            return $errors;
        }
    }


}

?>