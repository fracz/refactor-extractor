<?php // $Id$
      // Display the whole course as "weeks" made of of modules
      // Included from "view.php"

    require_once("$CFG->dirroot/mod/forum/lib.php");

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

    if (isset($week)) {
        $displaysection = course_set_display($course->id, $week);
    } else {
        if (isset($USER->display[$course->id])) {
            $displaysection = $USER->display[$course->id];
        } else {
            $displaysection = course_set_display($course->id, 0);
        }
    }

    if ($course->newsitems) {
        $news = forum_get_course_forum($course->id, "news");
    }

    $streditsummary  = get_string("editsummary");
    $stradd          = get_string("add");
    $stractivities   = get_string("activities");
    $strshowallweeks = get_string("showallweeks");
    $strweek         = get_string("week");
    $strgroups       = get_string("groups");
    $strgroupmy      = get_string("groupmy");
    if ($editing) {
        $strstudents = moodle_strtolower($course->students);
        $strweekhide = get_string("weekhide", "", $strstudents);
        $strweekshow = get_string("weekshow", "", $strstudents);
        $strmoveup = get_string("moveup");
        $strmovedown = get_string("movedown");
    }


/// Layout the whole page as three big columns.
    echo '<table id="layout-table"><tr>';

/// The left column ...

    if(blocks_have_content($pageblocks[BLOCK_POS_LEFT]) || $editing) {
        echo '<td style="width: '.$preferred_width_left.'px;" id="left-column">';
        blocks_print_group($PAGE, $pageblocks[BLOCK_POS_LEFT]);
        echo '</td>';
    }

/// Start main column
    echo '<td id="middle-column">';
    print_heading_block(get_string("weeklyoutline"), "100%", "outlineheadingblock");
    print_spacer(8, 1, true);

    echo "<table class=\"weeklyoutline\" border=\"0\" cellpadding=\"8\" cellspacing=\"0\" width=\"100%\">";

/// If currently moving a file then show the current clipboard
    if (ismoving($course->id)) {
        $stractivityclipboard = strip_tags(get_string("activityclipboard", "", addslashes($USER->activitycopyname)));
        $strcancel= get_string("cancel");
        echo "<tr>";
        echo "<td colspan=\"3\" valign=\"top\" class=\"weeklyoutlineclip\" width=\"100%\">";
        echo "<div><font size=\"2\">";
        echo "$stractivityclipboard&nbsp;&nbsp;(<a href=\"mod.php?cancelcopy=true&amp;sesskey=$USER->sesskey\">$strcancel</a>)";
        echo "</font></div>";
        echo "</td>";
        echo "</tr>";
        echo "<tr><td colspan=\"3\"><img src=\"../pix/spacer.gif\" width=\"1\" height=\"1\" alt=\"\" /></td></tr>";
    }

/// Print Section 0 with general activities

    $section = 0;
    $thissection = $sections[$section];

    if ($thissection->summary or $thissection->sequence or isediting($course->id)) {
        echo "<tr>";
        echo "<td nowrap=\"nowrap\" class=\"weeklyoutlineside\" valign=\"top\" width=\"20\">&nbsp;</td>";
        echo "<td valign=\"top\" class=\"weeklyoutlinecontent\" width=\"100%\">";

        $summaryformatoptions->noclean = true;
        echo format_text($thissection->summary, FORMAT_HTML, $summaryformatoptions);

        if (isediting($course->id)) {
            echo " <a title=\"$streditsummary\" ".
                 " href=\"editsection.php?id=$thissection->id\"><img height=\"11\" width=\"11\" src=\"$CFG->pixpath/t/edit.gif\" ".
                 " border=\"0\" alt=\"$streditsummary\" /></a><br /><br />";
        }

        print_section($course, $thissection, $mods, $modnamesused);

        if (isediting($course->id)) {
            print_section_add_menus($course, $section, $modnames);
        }

        echo "</td>";
        echo "<td nowrap=\"nowrap\" class=\"weeklyoutlineside\" valign=\"top\" align=\"center\" width=\"10\">";
        echo "&nbsp;</td></tr>";
        echo "<tr><td colspan=\"3\"><img src=\"../pix/spacer.gif\" width=\"1\" height=\"1\" alt=\"\" /></td></tr>";
    }


/// Now all the weekly sections
    $timenow = time();
    $weekdate = $course->startdate;    // this should be 0:00 Monday of that week
    $weekdate += 7200;                 // Add two hours to avoid possible DST problems
    $section = 1;
    $sectionmenu = array();
    $weekofseconds = 604800;
    $course->enddate = $course->startdate + ($weekofseconds * $course->numsections);

    $strftimedateshort = " ".get_string("strftimedateshort");

    while ($weekdate < $course->enddate) {

        $nextweekdate = $weekdate + ($weekofseconds);
        $weekday = userdate($weekdate, $strftimedateshort);
        $endweekday = userdate($weekdate+518400, $strftimedateshort);

        if (!empty($displaysection) and $displaysection != $section) {  // Check this week is visible
            $sectionmenu["week=$section"] = s("$strweek $section |     $weekday - $endweekday");
            $section++;
            $weekdate = $nextweekdate;
            continue;
        }

        if (!empty($sections[$section])) {
            $thissection = $sections[$section];

        } else {
            unset($thissection);
            $thissection->course = $course->id;   // Create a new week structure
            $thissection->section = $section;
            $thissection->summary = "";
            $thissection->visible = 1;
            if (!$thissection->id = insert_record("course_sections", $thissection)) {
                notify("Error inserting new week!");
            }
        }

        $showsection = (isteacher($course->id) or $thissection->visible or !$course->hiddensections);

        if ($showsection) {

            $currentweek = (($weekdate <= $timenow) && ($timenow < $nextweekdate));

            if (!$thissection->visible) {
                $colorsides = "class=\"weeklyoutlinesidehidden\"";
                $colormain  = "class=\"weeklyoutlinecontenthidden\"";
            } else if ($currentweek) {
                $colorsides = "class=\"weeklyoutlinesidehighlight\"";
                $colormain  = "class=\"weeklyoutlinecontenthighlight\"";
            } else {
                $colorsides = "class=\"weeklyoutlineside\"";
                $colormain  = "class=\"weeklyoutlinecontent\"";
            }

            echo "<tr>";
            echo "<td nowrap=\"nowrap\" $colorsides valign=\"top\" width=\"20\">";
            echo "<div align=\"center\"><font size=\"3\"><a name=\"$section\">$section</a></font></div>";
            echo "</td>";

            echo "<td valign=\"top\" $colormain width=\"100%\">";

            if (!isteacher($course->id) and !$thissection->visible) {   // Hidden for students
                echo "<div class=\"weeklydatetext\">$weekday - $endweekday ";
                echo "(".get_string("notavailable").")";
                echo "</div>";
                echo "</td>";

            } else {

                echo "<div class=\"weeklydatetext\">$weekday - $endweekday</div>";

                $summaryformatoptions->noclean = true;
                echo format_text($thissection->summary, FORMAT_HTML, $summaryformatoptions);

                if (isediting($course->id)) {
                    echo " <a title=\"$streditsummary\" href=\"editsection.php?id=$thissection->id\">".
                         "<img src=\"$CFG->pixpath/t/edit.gif\" height=\"11\" width=\"11\" border=\"0\" /></a><br /><br />";
                }

                print_section($course, $thissection, $mods, $modnamesused);

                if (isediting($course->id)) {
                    print_section_add_menus($course, $section, $modnames);
                }

                echo "</td>";
            }
            echo "<td nowrap=\"nowrap\" $colorsides valign=\"top\" align=\"center\" width=\"10\">";
            echo "<font size=\"1\">";

            if ($displaysection == $section) {
                echo "<a href=\"view.php?id=$course->id&amp;week=all\" title=\"$strshowallweeks\">".
                     "<img src=\"$CFG->pixpath/i/all.gif\" height=\"25\" width=\"16\" border=\"0\" alt=\"\" /></a><br />";
            } else {
                $strshowonlyweek = get_string("showonlyweek", "", $section);
                echo "<a href=\"view.php?id=$course->id&amp;week=$section\" title=\"$strshowonlyweek\">".
                     "<img src=\"$CFG->pixpath/i/one.gif\" height=\"16\" width=\"16\" border=\"0\" alt=\"\" /></a><br />";
            }

            if (isediting($course->id)) {
                if ($thissection->visible) {        // Show the hide/show eye
                    echo "<a href=\"view.php?id=$course->id&amp;hide=$section&amp;sesskey=$USER->sesskey\" title=\"$strweekhide\">".
                         "<img src=\"$CFG->pixpath/i/hide.gif\" vspace=\"3\" height=\"16\" width=\"16\" border=\"0\" alt=\"\" /></a><br />";
                } else {
                    echo "<a href=\"view.php?id=$course->id&amp;show=$section&amp;sesskey=$USER->sesskey\" title=\"$strweekshow\">".
                         "<img src=\"$CFG->pixpath/i/show.gif\" vspace=\"3\" height=\"16\" width=\"16\" border=\"0\" alt=\"\" /></a><br />";
                }

                if ($section > 1) {                       // Add a arrow to move section up
                    echo "<a href=\"view.php?id=$course->id&amp;section=$section&amp;move=-1&amp;sesskey=$USER->sesskey\" title=\"$strmoveup\">".
                         "<img src=\"$CFG->pixpath/t/up.gif\" vspace=\"3\" height=\"11\" width=\"11\" border=\"0\" alt=\"\" /></a><br />";
                }

                if ($section < $course->numsections) {    // Add a arrow to move section down
                    echo "<a href=\"view.php?id=$course->id&amp;section=$section&amp;move=1&amp;sesskey=$USER->sesskey\" title=\"$strmovedown\">".
                         "<img src=\"$CFG->pixpath/t/down.gif\" vspace=\"3\" height=\"11\" width=\"11\" border=\"0\" alt=\"\" /></a><br />";
                }

            }

            echo "</font>";
            echo "</td>";
            echo "</tr>";
            echo "<tr><td colspan=\"3\"><img src=\"../pix/spacer.gif\" width=\"1\" height=\"1\" alt=\"\"></td></tr>";
        }

        $section++;
        $weekdate = $nextweekdate;
    }
    echo "</table>";

    if (!empty($sectionmenu)) {
        echo "<center>";
        echo popup_form("$CFG->wwwroot/course/view.php?id=$course->id&", $sectionmenu,
                   "sectionmenu", "", get_string("jumpto"), "", "", true);
        echo "</center>";
    }


    echo "</td>";

    // The right column
    if(blocks_have_content($pageblocks[BLOCK_POS_RIGHT]) || $editing) {
        echo '<td style="width: '.$preferred_width_right.'px;" id="right-column">';
        blocks_print_group($PAGE, $pageblocks[BLOCK_POS_RIGHT]);
        if ($editing) {
            blocks_print_adminblock($PAGE, $pageblocks);
        }
        echo '</td>';
    }

    echo "</tr>\n";
    echo "</table>\n";
?>