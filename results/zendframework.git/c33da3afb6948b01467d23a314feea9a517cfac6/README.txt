commit c33da3afb6948b01467d23a314feea9a517cfac6
Author: Koen Pieters <koen@ibuildings.nl>
Date:   Fri Sep 14 17:49:21 2012 +0200

    Zend\Form Added extra unit tests and some code improvements
    - Fixed FormCollection::shouldWrap function
    - Removed Duplicate code FormElement::render line 173 - 176 === 178 - 181
    - Moved check for name to static getName FormMultiCheckbox::render
    - Improved readability Zend\Form\Form::setValidationGroup