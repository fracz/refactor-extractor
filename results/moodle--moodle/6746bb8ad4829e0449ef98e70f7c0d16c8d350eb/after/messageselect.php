<?php
{
    require_once('../config.php');
    require_once($CFG->dirroot.'/message/lib.php');

    $id = required_param('id');
    $messagebody = optional_param('messagebody',null,false);
    $send = optional_param('send',null);
    $returnto = optional_param('returnto',null);
    $preview = optional_param('preview',null);
    $format = optional_param('format',FORMAT_MOODLE);
    $edit = optional_param('edit',null);
    $deluser = optional_param('deluser',null);

    if (!$course = get_record('course','id',$id)) {
        error("Invalid course id");
    }

    if (!isteacher($course->id)) {
        error("Only teachers can use this page");
    }

    if (empty($SESSION->emailto)) {
        $SESSION->emailto = array();
    }
    if (!array_key_exists($id,$SESSION->emailto)) {
        $SESSION->emailto[$id] = array();
    }

    if ($deluser) {
        if (array_key_exists($id,$SESSION->emailto) && array_key_exists($deluser,$SESSION->emailto[$id])) {
            unset($SESSION->emailto[$id][$deluser]);
        }
    }

    $count = 0;

    foreach ($_GET as $k => $v) {
        if (preg_match('/^user(\d+)$/',$k,$m)) {
            if (!array_key_exists($m[1],$SESSION->emailto[$id])) {
                if ($user = get_record_select('user','id = '.$m[1],'id,firstname,lastname,idnumber,email,emailstop,mailformat')) {
                    $SESSION->emailto[$id][$m[1]] = $user;
                    $count++;
                }
            }
        }
    }

    $strtitle = get_string('coursemessage');

    if (empty($messagebody)) {
        $formstart = "theform.messagebody";
    } else {
        $formstart = "";
    }

    print_header($strtitle,$strtitle,"<A HREF=\"$CFG->wwwroot/course/view.php?id=$course->id\">$course->shortname</A> -> <a href=\"index.php?id=$course->id\">".get_string("participants")."</a> -> ".$strtitle,$formstart);


    if ($count) {
        if ($count == 1) {
            $heading =  get_string('addedrecip','moodle',$count);
        } else {
            $heading = get_string('addedrecips','moodle',$count);
        }
        print_heading($heading);
    }

    if (!empty($messagebody) && !$edit && !$deluser) {
        if (count($SESSION->emailto[$id])) {
            if ($preview) {
                echo '<form method="post" action="messageselect.php" style="margin: 0 20px;">
<input type="hidden" name="returnto" value="'.stripslashes($returnto).'">
<input type="hidden" name="id" value="'.$id.'">
<input type="hidden" name="format" value="'.$format.'">
<input type="hidden" name="messagebody" value="'.htmlentities(stripslashes($messagebody)).'">';
                echo "<br/><h3>".get_string('previewhtml')."</h3><div class=\"messagepreview\">\n".format_text(stripslashes($messagebody),$format)."\n</div>";
                echo "\n<p align=\"center\"><input type=\"submit\" name=\"send\" value=\"Send\" /> <input type=\"submit\" name=\"edit\" value=\"Edit\" /></p>\n</form>";
            } elseif ($send) {
                $good = 1;
                foreach ($SESSION->emailto[$id] as $user) {
                    $good = $good && message_post_message($USER,$user,$messagebody,$format,'direct');
                }
                if ($good) {
                    print_heading(get_string('messagedselectedusers'));
                    unset($SESSION->emailto[$id]);
                } else {
                    print_heading(get_string('messagedselectedusersfailed'));
                }
                echo '<p align="center"><a href="index.php?id='.$id.'">'.get_string('backtoparticipants').'</a></p>';
            }
            print_footer();
            exit;
        } else {
            notify(get_string('nousersyet'));
        }
    }

    echo '<p align="center"><a href="'.$returnto.'">'.get_string("keepsearching").'</a>'.((count($SESSION->emailto[$id])) ? ', '.get_string('usemessageform') : '').'</p>';

    if ((!empty($send) || !empty($preview) || !empty($edit)) && (empty($messagebody))) {
        notify(get_string('allfieldsrequired'));
    }

    if (count($SESSION->emailto[$id])) {
        $usehtmleditor = can_use_richtext_editor();
        require("message.html");
        if ($usehtmleditor) {
            use_html_editor("messagebody");
        }
    }

    print_footer();

}
?>