<?php

$webservice_rest_capabilities = array(

    'webservice/rest:use' => array(
        'captype' => 'read', // in fact this may be considered read and write at the same time
        'contextlevel' => CONTEXT_COURSE, // the context level should be probably CONTEXT_MODULE
        'legacy' => array(
        ),
    ),

);