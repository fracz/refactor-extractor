<?php  // $Id$

$gradeimport_xml_capabilities = array(

    'gradeimport/xml:view' => array(
        'captype' => 'write',
        'contextlevel' => CONTEXT_COURSE,
        'legacy' => array(
            'editingteacher' => CAP_ALLOW,
            'admin' => CAP_ALLOW
        )
    ),

    'gradeimport/xml:publish' => array(
        'captype' => 'write',
        'contextlevel' => CONTEXT_COURSE,
        'legacy' => array(
            'admin' => CAP_ALLOW
        )
    )
);

?>

