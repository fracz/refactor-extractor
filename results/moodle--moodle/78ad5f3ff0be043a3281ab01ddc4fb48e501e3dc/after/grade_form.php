<?php  //$Id$

require_once $CFG->libdir.'/formslib.php';

class edit_grade_form extends moodleform {

    function definition() {
        global $CFG, $COURSE;

        $mform =& $this->_form;

        $grade_item = $this->_customdata['grade_item'];
        $gpr        = $this->_customdata['gpr'];

        /// information fields
        $mform->addElement('static', 'user', get_string('user'));
        $mform->addElement('static', 'itemname', get_string('itemname', 'grades'));

        /// actual grade - numeric or scale
        if ($grade_item->gradetype == GRADE_TYPE_VALUE) {
            // numeric grade
            $mform->addElement('text', 'finalgrade', get_string('finalgrade', 'grades'));

        } else if ($grade_item->gradetype == GRADE_TYPE_SCALE) {
            // scale grade
            $scaleopt = array();
            $scaleopt[-1] = get_string('nograde');

            $i = 1;
            if ($scale = get_record('scale', 'id', $grade_item->scaleid)) {
                foreach (split(",", $scale->scale) as $option) {
                    $scaleopt[$i] = $option;
                    $i++;
                }
            }

            $mform->addElement('select', 'finalgrade', get_string('finalgrade', 'grades'), $scaleopt);
        }

        $mform->addElement('advcheckbox', 'hidden', get_string('hidden', 'grades'));
        $mform->addElement('advcheckbox', 'overridden', get_string('overridden', 'grades'));
        $mform->addElement('advcheckbox', 'excluded', get_string('excluded', 'grades'));

        /// locking
        $mform->addElement('advcheckbox', 'locked', get_string('locked', 'grades'));
        $mform->addElement('date_time_selector', 'locktime', get_string('locktime', 'grades'), array('optional'=>true));
        $mform->disabledIf('locktime', 'gradetype', 'eq', GRADE_TYPE_NONE);

        // Feedback format is automatically converted to html if user has enabled editor
        $mform->addElement('htmleditor', 'feedback', get_string('feedback', 'grades'),
            array('rows'=>'15', 'course'=>$COURSE->id, 'cols'=>'45'));
        $mform->setType('text', PARAM_RAW); // to be cleaned before display, no XSS risk
        $mform->addElement('format', 'feedbackformat', get_string('format'));
        $mform->setHelpButton('feedbackformat', array('textformat', get_string('helpformatting')));

        // hidden params
        $mform->addElement('hidden', 'id', 0);
        $mform->setType('id', PARAM_INT);

        $mform->addElement('hidden', 'itemid', 0);
        $mform->setType('itemid', PARAM_INT);

        $mform->addElement('hidden', 'userid', 0);
        $mform->setType('userid', PARAM_INT);

        $mform->addElement('hidden', 'courseid', $COURSE->id);
        $mform->setType('courseid', PARAM_INT);

/// add return tracking info
        $gpr->add_mform_elements($mform);

//-------------------------------------------------------------------------------
        // buttons
        $this->add_action_buttons();
    }

    function definition_after_data() {
        global $CFG;

        $mform =& $this->_form;
        $grade_item = $this->_customdata['grade_item'];

        if ($userid = $mform->getElementValue('userid')) {
            $user = get_record('user', 'id', $userid);
            $username = '<a href="'.$CFG->wwwroot.'/user/view.php?id='.$userid.'">'.fullname($user).'</a>';
            $user_el =& $mform->getElement('user');
            $user_el->setValue($username);
        }

        if ($grade_item->itemtype == 'mod') {
            $cm = get_coursemodule_from_instance($grade_item->itemmodule, $grade_item->iteminstance, $grade_item->courseid);
            $itemname = '<a href="'.$CFG->wwwroot.'/mod/'.$grade_item->itemmodule.'/view.php?id='.$cm->id.'">'.$grade_item->get_name().'</a>';
        } else {
            $itemname = $grade_item->get_name();
        }
        $itemname_el =& $mform->getElement('itemname');
        $itemname_el->setValue($itemname);
    }
}

?>