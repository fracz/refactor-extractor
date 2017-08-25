<?php

// Init
error_reporting(NULL);
session_start();
$TAB = 'USER';

// Inlcude functions
include($_SERVER['DOCUMENT_ROOT']."/inc/main.php");

// Header
include($_SERVER['DOCUMENT_ROOT'].'/templates/header.html');

// Panel
top_panel($user,$TAB);

// Data
if ($_SESSION['user'] == 'admin') {
    $cmd = "v_list_user '".$user."' json";
    if ($user == 'admin') $cmd = "v_list_users json";
    exec (VESTA_CMD.$cmd, $output, $return_var);
    $data = json_decode(implode('', $output), true);
    $data = array_reverse($data);
    display_error_block;
    include($_SERVER['DOCUMENT_ROOT'].'/templates/admin/list_user.html');
} else {
    exec (VESTA_CMD."v_list_user ".$user." json", $output, $return_var);
    $data = json_decode(implode('', $output), true);
    $data = array_reverse($data);
    display_error_block;
    include($_SERVER['DOCUMENT_ROOT'].'/templates/user/list_user.html');
}

// Footer
include($_SERVER['DOCUMENT_ROOT'].'/templates/footer.html');