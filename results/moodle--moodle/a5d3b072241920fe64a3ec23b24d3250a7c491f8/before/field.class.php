<?php //$Id$

class profile_field_text extends profile_field_base {

    function display_field_add(&$form) {
        /// Param 1 for text type is the size of the field
        $size = (empty($this->field->param1)) ? '30' : $this->field->param1;

        /// Param 2 for text type is the maxlength of the field
        $maxlength = (empty($this->field->param2)) ? '2048' : $this->field->param2;

        /// Create the form field
        $form->addElement('text', $this->inputname, format_string($this->field->name), 'maxlength="'.$maxlength.'" size="'.$size.'" ');
        $form->setType($this->inputname, PARAM_MULTILANG);
    }

}

?>