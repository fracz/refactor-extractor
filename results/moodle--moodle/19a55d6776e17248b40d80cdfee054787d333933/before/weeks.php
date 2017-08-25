<?PHP // $Id$
      // Display the whole course as "weeks" made of of modules
      // Included from "view.php"

    include("../mod/forum/lib.php");

    if (! $sections = get_all_sections($course->id)) {
        $section->course = $course->id;   // Create a default section.
        $section->section = 0;
        $section->id = insert_record("course_sections", $section);
        if (! $sections = get_all_sections($course->id) ) {
            error("Error finding or creating section structures for this course");
        }
    }

    if (isset($week)) {
        if ($week == "all") {
            unset($USER->section);
        } else {
            $USER->section = $week;
        }
        save_session("USER");
    }


    // Layout the whole page as three big columns.
    echo "<TABLE BORDER=0 CELLPADDING=3 CELLSPACING=0 WIDTH=100%>";
    echo "<TR VALIGN=top><TD VALIGN=top WIDTH=180>";

    // Layout the left column


    // Links to people

    print_simple_box(get_string("people"), $align="CENTER", $width="100%", $color="$THEME->cellheading");
    $moddata[]="<A HREF=\"../user/index.php?id=$course->id\">".get_string("listofallpeople")."</A>";
    $modicon[]="<IMG SRC=\"../user/users.gif\" HEIGHT=16 WIDTH=16 ALT=\"\">";
    $editmyprofile = "<A HREF=\"../user/view.php?id=$USER->id&course=$course->id\">".get_string("editmyprofile")."</A>";
    if ($USER->description) {
        $moddata[]= $editmyprofile;
    } else {
        $moddata[]= $editmyprofile.$blinker;
    }
    $modicon[]="<IMG SRC=\"../user/user.gif\" HEIGHT=16 WIDTH=16 ALT=\"\">";
    print_side_block("", $moddata, "", $modicon);


    // Then all the links to module types

    $moddata = array();
    $modicon = array();
    if ($modnamesused) {
        foreach ($modnamesused as $modname => $modfullname) {
            $moddata[] = "<A HREF=\"../mod/$modname/index.php?id=$course->id\">".$modnamesplural[$modname]."</A>";
            $modicon[] = "<IMG SRC=\"../mod/$modname/icon.gif\" HEIGHT=16 WIDTH=16 ALT=\"\">";
        }
    }
    print_simple_box(get_string("activities"), $align="CENTER", $width="100%", $color="$THEME->cellheading");
    print_side_block("", $moddata, "", $modicon);

    // Print a form to search forums
    print_simple_box(get_string("search","forum"), $align="CENTER", $width="100%", $color="$THEME->cellheading");
    echo "<DIV ALIGN=CENTER>";
    forum_print_search_form($course);
    echo "</DIV>";

    // Admin links and controls
    if (isteacher($course->id)) {
        print_course_admin_links($course->id);
    }

    // Start main column
    echo "</TD><TD WIDTH=\"*\">";

    print_simple_box(get_string("weeklyoutline"), $align="CENTER", $width="100%", $color="$THEME->cellheading");

    // Now all the weekly modules
    $timenow = time();
    $weekdate = $course->startdate;    // this should be 0:00 Monday of that week
    $week = 1;
    $weekofseconds = 604800;
    $course->enddate = $course->startdate + ($weekofseconds * $course->numsections);

    $streditsummary = get_string("editsummary");
    $stradd         = get_string("add");

    echo "<TABLE BORDER=0 CELLPADDING=8 CELLSPACING=0 WIDTH=100%>";
    while ($weekdate < $course->enddate) {

        $nextweekdate = $weekdate + ($weekofseconds);

        if (isset($USER->section)) {         // Just display a single week
            if ($USER->section != $week) {
                $week++;
                $weekdate = $nextweekdate;
                continue;
            }
        }

        $thisweek = (($weekdate <= $timenow) && ($timenow < $nextweekdate));

        $weekday = date("j F", $weekdate);
        $endweekday = date("j F", $weekdate+(6*24*3600));

        if ($thisweek) {
            $highlightcolor = $THEME->cellheading2;
        } else {
            $highlightcolor = $THEME->cellheading;
        }

        echo "<TR>";
        echo "<TD NOWRAP BGCOLOR=\"$highlightcolor\" VALIGN=top WIDTH=20>";
        echo "<P ALIGN=CENTER><FONT SIZE=3><B>$week</B></FONT></P>";
        echo "</TD>";

        echo "<TD VALIGN=top BGCOLOR=\"$THEME->cellcontent\" WIDTH=\"100%\">";
        echo "<P><FONT SIZE=3 COLOR=\"$THEME->cellheading2\">$weekday - $endweekday</FONT></P>";

        if (! $thisweek = $sections[$week]) {
            $thisweek->course = $course->id;   // Create a new week structure
            $thisweek->section = $week;
            $thisweek->summary = "";
            if (!$thisweek->id = insert_record("course_sections", $thisweek)) {
                notify("Error inserting new week!");
            }
        }

        if (isediting($course->id)) {
            $thisweek->summary .= "&nbsp;<A TITLE=\"$streditsummary\" HREF=\"editsection.php?id=$thisweek->id\"><IMG SRC=\"../pix/t/edit.gif\" BORDER=0 ALT=\"$streditsummary\"></A></P>";
        }

        echo text_to_html($thisweek->summary);

        print_section($course->id, $thisweek, $mods, $modnamesused);

        if (isediting($course->id)) {
            echo "<DIV ALIGN=right>";
            popup_form("$CFG->wwwroot/course/mod.php?id=$course->id&section=$week&add=",
                        $modnames, "section$week", "", "$stradd...");
            echo "</DIV>";
        }

        echo "</TD>";
        echo "<TD NOWRAP BGCOLOR=\"$highlightcolor\" VALIGN=top ALIGN=CENTER WIDTH=10>";
        echo "<FONT SIZE=1>";
        if (isset($USER->section)) {
            $strshowallweeks = get_string("showallweeks");
            echo "<A HREF=\"view.php?id=$course->id&week=all\" TITLE=\"$strshowallweeks\"><IMG SRC=../pix/i/all.gif BORDER=0></A></FONT>";
        } else {
            $strshowonlyweek = get_string("showonlyweek", "", $week);
            echo "<A HREF=\"view.php?id=$course->id&week=$week\" TITLE=\"$strshowonlyweek\"><IMG SRC=../pix/i/one.gif BORDER=0></A></FONT>";
        }
        echo "</TD>";
        echo "</TR>";
        echo "<TR><TD COLSPAN=3><IMG SRC=\"../pix/spacer.gif\" WIDTH=1 HEIGHT=1></TD></TR>";

        $week++;
        $weekdate = $nextweekdate;
    }
    echo "</TABLE>";


    echo "</TD><TD WIDTH=210>";

    // Print all the news items.

    if ($course->newsitems) {
        if ($news = forum_get_course_forum($course->id, "news")) {
            print_simple_box(get_string("latestnews"), $align="CENTER", $width="100%", $color="$THEME->cellheading");
            print_simple_box_start("CENTER", "100%", "#FFFFFF", 3, 0);
            echo "<FONT SIZE=1>";
            forum_print_latest_discussions($news->id, $course->newsitems, "minimal", "DESC", false);
            echo "</FONT>";
            print_simple_box_end();
        }
        echo "<BR>";
    }

    // Print all the recent activity
    print_simple_box(get_string("recentactivity"), $align="CENTER", $width="100%", $color="$THEME->cellheading");
    print_simple_box_start("CENTER", "100%", "#FFFFFF", 3, 0);
    print_recent_activity($course);
    print_simple_box_end();

    echo "</TD></TR></TABLE>\n";

?>