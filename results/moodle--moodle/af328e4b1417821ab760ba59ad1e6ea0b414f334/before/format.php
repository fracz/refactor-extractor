<?php // $Id$
      // format.php - course format featuring social forum
      //              included from view.php

    require_once("$CFG->dirroot/mod/forum/lib.php");
    require_once("$CFG->dirroot/mod/resource/lib.php");

    // Bounds for block widths
    define('BLOCK_L_MIN_WIDTH', 100);
    define('BLOCK_L_MAX_WIDTH', 210);
    define('BLOCK_R_MIN_WIDTH', 100);
    define('BLOCK_R_MAX_WIDTH', 210);

    optional_variable($preferred_width_left,  blocks_preferred_width($pageblocks[BLOCK_POS_LEFT]));
    optional_variable($preferred_width_right, blocks_preferred_width($pageblocks[BLOCK_POS_RIGHT]));
    $preferred_width_left = min($preferred_width_left, BLOCK_L_MAX_WIDTH);
    $preferred_width_left = max($preferred_width_left, BLOCK_L_MIN_WIDTH);
    $preferred_width_right = min($preferred_width_right, BLOCK_R_MAX_WIDTH);
    $preferred_width_right = max($preferred_width_right, BLOCK_R_MIN_WIDTH);

    $strgroups  = get_string('groups');
    $strgroupmy = get_string('groupmy');
    $editing    = $PAGE->user_is_editing();

    echo '<table id="layout-table">';
    echo '<tr>';

    if(blocks_have_content($pageblocks, BLOCK_POS_LEFT) || $editing) {
        echo '<td style="width: '.$preferred_width_left.'px;" id="left-column">';
        blocks_print_group($PAGE, $pageblocks, BLOCK_POS_LEFT);
        echo '</td>';
    }

    echo '<td id="middle-column">';
    if ($social = forum_get_course_forum($course->id, "social")) {
        if (forum_is_subscribed($USER->id, $social->id)) {
            $subtext = get_string("unsubscribe", "forum");
        } else {
            $subtext = get_string("subscribe", "forum");
        }
        $headertext = "<table border=\"0\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" class=\"headingblockcontent\"><tr><td>".
                       get_string("socialheadline").
                       "</td><td align=\"right\"><font size=\"1\">".
                       "<a href=\"../mod/forum/subscribe.php?id=$social->id\">$subtext</a></td>".
                       "</tr></table>";
        print_heading_block($headertext);
        echo "<img alt=\"\" height=\"7\" src=\"../pix/spacer.gif\" /><br />";

        forum_print_latest_discussions($social->id, 10, "plain", "", false);

    } else {
        notify("Could not find or create a social forum here");
    }
    echo '</td>';

    // The right column
    if(blocks_have_content($pageblocks, BLOCK_POS_RIGHT) || $editing) {
        echo '<td style="width: '.$preferred_width_right.'px;" id="right-column">';
        blocks_print_group($PAGE, $pageblocks, BLOCK_POS_RIGHT);
        echo '</td>';
    }

    echo '</tr>';
    echo '</table>';

?>