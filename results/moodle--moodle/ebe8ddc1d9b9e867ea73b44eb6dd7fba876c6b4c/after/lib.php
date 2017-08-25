<?php  // $Id$
   // Library of useful functions


if (defined('COURSE_MAX_LOG_DISPLAY')) {  // Being included again - should never happen!!
    return;
}

define('COURSE_MAX_LOG_DISPLAY', 150);       // days

define('COURSE_MAX_LOGS_PER_PAGE', 1000);    // records

define('COURSE_LIVELOG_REFRESH', 60);        // Seconds

define('COURSE_MAX_RECENT_PERIOD', 604800);  // A week, in seconds

define('COURSE_MAX_SUMMARIES_PER_PAGE', 10); // courses

define("FRONTPAGENEWS",           0);
define("FRONTPAGECOURSELIST",     1);
define("FRONTPAGECATEGORYNAMES",  2);


function print_log_selector_form($course, $selecteduser=0, $selecteddate="today") {

    global $USER, $CFG;

    // Get all the possible users
    $users = array();

    if ($course->category) {
        $courseusers = get_course_users($course->id, "u.lastaccess DESC");
    } else {
        $courseusers = get_site_users("u.lastaccess DESC", "u.id, u.firstname, u.lastname");
    }

    if ($courseusers) {
        foreach ($courseusers as $courseuser) {
            $users[$courseuser->id] = "$courseuser->firstname $courseuser->lastname";
        }
    }
    if ($guest = get_guest()) {
        $users[$guest->id] = "$guest->firstname $guest->lastname";
    }

    if (isadmin()) {
        if ($ccc = get_records("course", "", "", "fullname")) {
            foreach ($ccc as $cc) {
                if ($cc->category) {
                    $courses["$cc->id"] = "$cc->fullname";
                } else {
                    $courses["$cc->id"] = " $cc->fullname (Site)";
                }
            }
        }
        asort($courses);
    }


    $strftimedate = get_string("strftimedate");
    $strftimedaydate = get_string("strftimedaydate");

    asort($users);

    // Get all the possible dates
    // Note that we are keeping track of real (GMT) time and user time
    // User time is only used in displays - all calcs and passing is GMT

    $timenow = time(); // GMT

    // What day is it now for the user, and when is midnight that day (in GMT).
    $timemidnight = $today = usergetmidnight($timenow);

    // Put today up the top of the list
    $dates = array("$timemidnight" => get_string("today").", ".userdate($timenow, $strftimedate) );

    if (!$course->startdate or ($course->startdate > $timenow)) {
        $course->startdate = $course->timecreated;
    }

    $numdates = 1;
    while ($timemidnight > $course->startdate and $numdates < 365) {
        $timemidnight = $timemidnight - 86400;
        $timenow = $timenow - 86400;
        $dates["$timemidnight"] = userdate($timenow, $strftimedaydate);
        $numdates++;
    }

    if ($selecteddate == "today") {
        $selecteddate = $today;
    }

    echo "<CENTER>";
    echo "<FORM ACTION=log.php METHOD=get>";
    echo "<INPUT TYPE=hidden NAME=chooselog VALUE=\"1\">";
    if (isadmin()) {
        choose_from_menu ($courses, "id", $course->id, "");
    } else {
        echo "<INPUT TYPE=hidden NAME=id VALUE=\"$course->id\">";
    }
    choose_from_menu ($users, "user", $selecteduser, get_string("allparticipants") );
    choose_from_menu ($dates, "date", $selecteddate, get_string("alldays"));
    echo "<INPUT TYPE=submit VALUE=\"".get_string("showtheselogs")."\">";
    echo "</FORM>";
    echo "</CENTER>";
}

function make_log_url($module, $url) {
    switch ($module) {
        case "course":
        case "user":
        case "file":
        case "login":
        case "lib":
        case "admin":
            return "/$module/$url";
            break;
        default:
            return "/mod/$module/$url";
            break;
    }
}


function print_log($course, $user=0, $date=0, $order="l.time ASC", $page=0, $perpage=100, $url="") {
// It is assumed that $date is the GMT time of midnight for that day,
// and so the next 86400 seconds worth of logs are printed.

    global $CFG;

    if ($course->category) {
        $selector = "l.course='$course->id' AND l.userid = u.id";

    } else {
        $selector = "l.userid = u.id";  // Show all courses
        if ($ccc = get_courses("all", "c.id ASC", "c.id,c.shortname")) {
            foreach ($ccc as $cc) {
                $courses[$cc->id] = "$cc->shortname";
            }
        }
    }

    if ($user) {
        $selector .= " AND l.userid = '$user'";
    }

    if ($date) {
        $enddate = $date + 86400;
        $selector .= " AND l.time > '$date' AND l.time < '$enddate'";
    }

    $totalcount = 0;  // Initialise

    if (!$logs = get_logs($selector, $order, $page*$perpage, $perpage, $totalcount)) {
        notify("No logs found!");
        print_footer($course);
        exit;
    }

    $count=0;
    $tt = getdate(time());
    $today = mktime (0, 0, 0, $tt["mon"], $tt["mday"], $tt["year"]);

    $strftimedatetime = get_string("strftimedatetime");

    echo "<p align=center>";
    print_string("displayingrecords", "", $totalcount);
    echo "</p>";

    print_paging_bar($totalcount, $page, $perpage, "$url&perpage=$perpage&");

    echo "<table border=0 align=center cellpadding=3 cellspacing=3>";
    foreach ($logs as $log) {

        if ($ld = get_record("log_display", "module", "$log->module", "action", "$log->action")) {
            $log->info = get_field($ld->mtable, $ld->field, "id", $log->info);
        }

        echo "<tr nowrap>";
        if (! $course->category) {
            echo "<td nowrap><font size=2><a href=\"view.php?id=$log->course\">".$courses[$log->course]."</a></td>";
        }
        echo "<td nowrap align=right><font size=2>".userdate($log->time, "%a")."</td>";
        echo "<td nowrap><font size=2>".userdate($log->time, $strftimedatetime)."</td>";
        echo "<td nowrap><font size=2>";
        link_to_popup_window("/lib/ipatlas/plot.php?address=$log->ip&user=$log->userid", "ipatlas","$log->ip", 400, 700);
        echo "</td>";
        echo "<td nowrap><font size=2><a href=\"../user/view.php?id=$log->userid&course=$log->course\"><b>$log->firstname $log->lastname</b></td>";
        echo "<td nowrap><font size=2>";
        link_to_popup_window( make_log_url($log->module,$log->url), "fromloglive","$log->module $log->action", 400, 600);
        echo "</td>";
        echo "<td nowrap><font size=2>$log->info</td>";
        echo "</tr>";
    }
    echo "</table>";

    print_paging_bar($totalcount, $page, $perpage, "$url&perpage=$perpage&");
}


function print_log_graph($course, $userid=0, $type="course.png", $date=0) {
    global $CFG;
    if (empty($CFG->gdversion)) {
        echo "(".get_string("gdneed").")";
    } else {
        echo "<IMG BORDER=0 SRC=\"$CFG->wwwroot/course/loggraph.php?id=$course->id&user=$userid&type=$type&date=$date\">";
    }
}



function print_recent_activity($course) {
    // $course is an object
    // This function trawls through the logs looking for
    // anything new since the user's last login

    global $CFG, $USER, $THEME, $SESSION;

    if (! $USER->lastlogin ) {
        echo "<p align=center><font size=1>";
        print_string("welcometocourse", "", $course->shortname);
        echo "</font></p>";
        return;
    } else {
        echo "<p align=center><font size=1>";
        echo get_string("yourlastlogin").":<BR>";
        echo userdate($USER->lastlogin, get_string("strftimerecentfull"));
        echo "</font></p>";
    }

    $timestart = $USER->lastlogin;
    $timemaxrecent = time() - COURSE_MAX_RECENT_PERIOD;
    if ($timestart < $timemaxrecent) {
        $timestart = $timemaxrecent;
    }


    // Firstly, have there been any new enrolments?

    $heading = false;
    $content = false;

    $users = get_recent_enrolments($course->id, $timestart);

    if ($users) {
        foreach ($users as $user) {
            if (! $heading) {
                print_headline(get_string("newusers").":");
                $heading = true;
                $content = true;
            }
            echo "<p><font size=1><a href=\"../user/view.php?id=$user->id&course=$course->id\">$user->firstname $user->lastname</a></font></p>";
        }
    }

    // Next, have there been any modifications to the course structure?

    $logs = get_records_select("log", "time > '$timestart' AND course = '$course->id' AND
                                       module = 'course' AND action LIKE '% mod'", "time ASC");

    if ($logs) {
        foreach ($logs as $key => $log) {
            $info = split(" ", $log->info);
            $modname = get_field($info[0], "name", "id", $info[1]);
            //Create a temp valid module structure (course,id)
            $tempmod->course = $log->course;
            $tempmod->id = $info[1];
            //Obtain the visible property from the instance
            $modvisible = instance_is_visible($info[0],$tempmod);

            //Only if the mod is visible
            if ($modvisible) {
                switch ($log->action) {
                    case "add mod":
                        $stradded = get_string("added", "moodle", get_string("modulename", $info[0]));
                        $changelist["$log->info"] = array ("operation" => "add", "text" => "$stradded:<BR><A HREF=\"$CFG->wwwroot/course/$log->url\">$modname</A>");
                    break;
                    case "update mod":
                       $strupdated = get_string("updated", "moodle", get_string("modulename", $info[0]));
                       if (empty($changelist["$log->info"])) {
                           $changelist["$log->info"] = array ("operation" => "update", "text" => "$strupdated:<BR><A HREF=\"$CFG->wwwroot/course/$log->url\">$modname</A>");
                       }
                    break;
                    case "delete mod":
                       if (!empty($changelist["$log->info"]["operation"]) and
                                  $changelist["$log->info"]["operation"] == "add") {
                           $changelist["$log->info"] = NULL;
                       } else {
                           $strdeleted = get_string("deletedactivity", "moodle", get_string("modulename", $info[0]));
                           $changelist["$log->info"] = array ("operation" => "delete", "text" => $strdeleted);
                       }
                    break;
                }
            }
        }
    }

    if (!empty($changelist)) {
        foreach ($changelist as $changeinfo => $change) {
            if ($change) {
                $changes[$changeinfo] = $change;
            }
        }
        if (count($changes) > 0) {
            print_headline(get_string("courseupdates").":");
            $content = true;
            foreach ($changes as $changeinfo => $change) {
                echo "<p><font size=1>".$change["text"]."</font></p>";
            }
        }
    }


    // If this site uses Library module, then print recent items
    if (!empty($CFG->librarypath)) {
        if (file_exists("$CFG->dirroot/$CFG->librarypath/librarylib.php")) {
            include_once("$CFG->dirroot/$CFG->librarypath/librarylib.php");
            if (librarysummarize(5, '', date('YmdHis',$USER->lastlogin))) {
                $content = true;
            }
        }
    }

    // Now display new things from each module

    $mods = get_list_of_plugins("mod");

    $isteacher = isteacher($course->id);

    foreach ($mods as $mod) {      // Each module gets it's own logs and prints them
        include_once("$CFG->dirroot/mod/$mod/lib.php");
        $print_recent_activity = $mod."_print_recent_activity";
        if (function_exists($print_recent_activity)) {
            $modcontent = $print_recent_activity($course, $isteacher, $timestart);
            if ($modcontent) {
                $content = true;
            }
        }
    }

    if (! $content) {
        echo "<font size=2>".get_string("nothingnew")."</font>";
    }
}


function get_array_of_activities($courseid) {
// For a given course, returns an array of course activity objects
// Each item in the array contains he following properties:
//  cm - course module id
//  mod - name of the module (eg forum)
//  section - the number of the section (eg week or topic)
//  name - the name of the instance
//  visible - is the instance visible or not
//  extra - contains extra string to include in any link

    $mod = array();

    if (!$rawmods = get_course_mods($courseid)) {
        return NULL;
    }

    if ($sections = get_records("course_sections", "course", $courseid, "section ASC")) {
       foreach ($sections as $section) {
           if (!empty($section->sequence)) {
               $sequence = explode(",", $section->sequence);
               foreach ($sequence as $seq) {
                   if (empty($rawmods[$seq])) {
                       continue;
                   }
                   $mod[$seq]->cm = $rawmods[$seq]->id;
                   $mod[$seq]->mod = $rawmods[$seq]->modname;
                   $mod[$seq]->section = $section->section;
                   $mod[$seq]->name = urlencode(get_field($rawmods[$seq]->modname, "name", "id", $rawmods[$seq]->instance));
                   $mod[$seq]->visible = $rawmods[$seq]->visible;
                   $mod[$seq]->extra = "";

                   // This part is an ugly hack that doesn't belong here//
                   if ($mod[$seq]->mod == "resource") {
                       if ($resource = get_record("resource", "id", $rawmods[$seq]->instance)) {
                           if ($resource->type == 5 and $resource->alltext) {
                               $mod[$seq]->extra = urlencode("target=\"resource$resource->id\" onClick=\"return ".
                                                   "openpopup('/mod/resource/view.php?id=".
                                                   $mod[$seq]->cm.
                                                   "','resource$resource->id','$resource->alltext');\"");
                           }
                       }
                   }
               }
            }
        }
    }
    return $mod;
}




function get_all_mods($courseid, &$mods, &$modnames, &$modnamesplural, &$modnamesused) {
// Returns a number of useful structures for course displays

    $mods          = NULL;    // course modules indexed by id
    $modnames      = NULL;    // all course module names
    $modnamesplural= NULL;    // all course module names (plural form)
    $modnamesused  = NULL;    // course module names used

    if ($allmods = get_records("modules")) {
        foreach ($allmods as $mod) {
            if ($mod->visible) {
                $modnames[$mod->name] = get_string("modulename", "$mod->name");
                $modnamesplural[$mod->name] = get_string("modulenameplural", "$mod->name");
            }
        }
        asort($modnames);
    } else {
        error("No modules are installed!");
    }

    if ($rawmods = get_course_mods($courseid)) {
        foreach($rawmods as $mod) {    // Index the mods
            $mods[$mod->id] = $mod;
            $mods[$mod->id]->modfullname = $modnames[$mod->modname];
            if ($mod->visible or isteacher($courseid)) {
                $modnamesused[$mod->modname] = $modnames[$mod->modname];
            }
        }
        if ($modnamesused) {
            asort($modnamesused);
        }
    }
}


function get_all_sections($courseid) {

    return get_records("course_sections", "course", "$courseid", "section",
                       "section, id, course, summary, sequence, visible");
}

function course_set_display($courseid, $display=0) {
    global $USER;

    if (empty($USER)) {
        return false;
    }

    if ($display == "all" or empty($display)) {
        $display = 0;
    }

    if (record_exists("course_display", "userid", $USER->id, "course", $courseid)) {
        set_field("course_display", "display", $display, "userid", $USER->id, "course", $courseid);
    } else {
        $record->userid = $USER->id;
        $record->course = $courseid;
        $record->display = $display;
        if (!insert_record("course_display", $record)) {
            notify("Could not save your course display!");
        }
    }

    return $USER->display[$courseid] = $display;  // Note: = not ==
}

function set_section_visible($courseid, $sectionnumber, $visibility) {
/// For a given course section, markes it visible or hidden,
/// and does the same for every activity in that section

    if ($section = get_record("course_sections", "course", $courseid, "section", $sectionnumber)) {
        set_field("course_sections", "visible", "$visibility", "id", $section->id);
        if (!empty($section->sequence)) {
            $modules = explode(",", $section->sequence);
            foreach ($modules as $moduleid) {
                set_field("course_modules", "visible", "$visibility", "id", $moduleid);
            }
        }
        rebuild_course_cache($courseid);
    }
}

function print_section_block($heading, $course, $section, $mods, $modnames, $modnamesused,
                             $absolute=true, $width="100%") {

    global $CFG, $USER, $THEME;
    static $isteacher;
    static $isediting;
    static $ismoving;
    static $strmovehere;
    static $strmovefull;

    if (!isset($isteacher)) {
        $isteacher = isteacher($course->id);
    }
    if (!isset($isediting)) {
        $isediting = isediting($course->id);
    }
    if (!isset($ismoving)) {
        $ismoving = ismoving($course->id);
    }

    $modinfo = unserialize($course->modinfo);
    $moddata = array();
    $modicon = array();
    $editbuttons = "";

    if (!empty($section->sequence)) {

        $sectionmods = explode(",", $section->sequence);

        if ($ismoving) {
            $strmovehere = get_string("movehere");
            $strmovefull = get_string("movefull", "", "'$USER->activitycopyname'");
            $stractivityclipboard = $USER->activitycopyname;
            $strcancel= get_string("cancel");
            if (empty($THEME->custompix)) {
                $pixpath = "$CFG->wwwroot/pix";
            } else {
                $pixpath = "$CFG->wwwroot/theme/$CFG->theme/pix";
            }
            $modicon[] = "&nbsp;<img align=bottom src=\"$pixpath/t/move.gif\" height=\"11\" width=\"11\">";
            $moddata[] = "$USER->activitycopyname&nbsp;(<a href=\"$CFG->wwwroot/course/mod.php?cancelcopy=true\">$strcancel</a>)";
        }

        foreach ($sectionmods as $modnumber) {
            if (empty($mods[$modnumber])) {
                continue;
            }
            $mod = $mods[$modnumber];
            if ($isediting and !$ismoving) {
                $editbuttons = "<br />".make_editing_buttons($mod->id, $absolute, $mod->visible, true);
            } else {
                $editbuttons = "";
            }
            if ($mod->visible or $isteacher) {
                if ($ismoving) {
                    if ($mod->id == $USER->activitycopy) {
                        continue;
                    }
                    $modicon[] = "";
                    $moddata[] = "<font size=\"2\"> -> <a title=\"$strmovefull\"".
                                 " href=\"$CFG->wwwroot/course/mod.php?moveto=$mod->id\"><b>$strmovehere</b></a></font>";
                }
                $instancename = urldecode($modinfo[$modnumber]->name);
                $linkcss = $mod->visible ? "" : " class=\"dimmed\" ";
                if (!empty($modinfo[$modnumber]->extra)) {
                    $extra = urldecode($modinfo[$modnumber]->extra);
                } else {
                    $extra = "";
                }

                $modicon[] = "<img src=\"$CFG->wwwroot/mod/$mod->modname/icon.gif\"".
                             " height=\"16\" width=\"16\" alt=\"$mod->modfullname\">";
                $moddata[] = "<a title=\"$mod->modfullname\" $linkcss $extra".
                             "href=\"$CFG->wwwroot/mod/$mod->modname/view.php?id=$mod->id\">$instancename</a>".
                             "$editbuttons";
            }
        }
    }
    if ($ismoving) {
        $modicon[] = "";
        $moddata[] = "<font size=\"2\"> -> <a title=\"$strmovefull\"".
                     " href=\"$CFG->wwwroot/course/mod.php?movetosection=$section->id\"><b>$strmovehere</b></a></font>";
    }
    if ($isediting) {
        $editmenu = popup_form("$CFG->wwwroot/course/mod.php?id=$course->id&amp;section=$section->section&add=",
                   $modnames, "section0", "", get_string("add")."...", "mods", get_string("activities"), true);
        $editmenu = "<div align=right>$editmenu</div>";
    } else {
        $editmenu = "";
    }

    print_side_block($heading, "", $moddata, $modicon, $editmenu, $width);
}


function print_section($course, $section, $mods, $modnamesused, $absolute=false, $width="100%") {
/// Prints a section full of activity modules
    global $CFG, $USER;

    static $isteacher;
    static $isediting;
    static $ismoving;
    static $strmovehere;
    static $strmovefull;

    if (!isset($isteacher)) {
        $isteacher = isteacher($course->id);
    }
    if (!isset($isediting)) {
        $isediting = isediting($course->id);
    }
    if (!isset($ismoving)) {
        $ismoving = ismoving($course->id);
    }
    if ($ismoving) {
        $strmovehere = get_string("movehere");
        $strmovefull = get_string("movefull", "", "'$USER->activitycopyname'");
    }

    $modinfo = unserialize($course->modinfo);

    echo "<table width=\"$width\"><tr><td>\n";
    if (!empty($section->sequence)) {

        $sectionmods = explode(",", $section->sequence);

        foreach ($sectionmods as $modnumber) {
            if (empty($mods[$modnumber])) {
                continue;
            }
            $mod = $mods[$modnumber];
            if ($mod->visible or $isteacher) {
                if ($ismoving) {
                    if ($mod->id == $USER->activitycopy) {
                        continue;
                    }
                    echo "<font size=\"2\"> -> <a title=\"$strmovefull\"".
                         " href=\"mod.php?moveto=$mod->id\">$strmovehere</a></font><br />\n";
                }
                $instancename = urldecode($modinfo[$modnumber]->name);
                if (!empty($modinfo[$modnumber]->extra)) {
                    $extra = urldecode($modinfo[$modnumber]->extra);
                } else {
                    $extra = "";
                }
                $linkcss = $mod->visible ? "" : " class=\"dimmed\" ";
                echo "<img src=\"$CFG->wwwroot/mod/$mod->modname/icon.gif\"".
                     " height=16 width=16 alt=\"$mod->modfullname\">".
                     " <font size=2><a title=\"$mod->modfullname\" $linkcss $extra".
                     " href=\"$CFG->wwwroot/mod/$mod->modname/view.php?id=$mod->id\">$instancename</a></font>";
            }
            if (isediting($course->id)) {
                echo "&nbsp;&nbsp;";
                echo make_editing_buttons($mod->id, $absolute, $mod->visible);
            }
            if ($mod->visible or $isteacher) {
                echo "<br />\n";
            }
        }
    }
    if ($ismoving) {
        echo "<font size=\"2\"> -> <a title=\"$strmovefull\"".
             " href=\"mod.php?movetosection=$section->id\">$strmovehere</a></font><br />\n";
    }
    echo "</td></tr></table><br />\n\n";
}


function rebuild_course_cache($courseid=0) {
// Rebuilds the cached list of course activities stored in the database
// If a courseid is not specified, then all are rebuilt

    if ($courseid) {
        $select = "id = '$courseid'";
    } else {
        $select = "";
    }

    if ($courses = get_records_select("course", $select)) {
        foreach ($courses as $course) {
            $modinfo = serialize(get_array_of_activities($course->id));
            if (!set_field("course", "modinfo", $modinfo, "id", $course->id)) {
                notify("Could not cache module information for course '$course->fullname'!");
            }
        }
    }
}


function print_heading_block($heading, $width="100%", $class="headingblock") {
    global $THEME;

    echo "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"5\">";
    echo "<tr><td bgcolor=\"$THEME->cellheading\" class=\"$class\">";
    echo stripslashes($heading);
    echo "</td></tr></table>";
}

function print_side_block($heading="", $content="", $list=NULL, $icons=NULL, $footer="", $width=180) {
// Prints a nice side block with an optional header.  The content can either
// be a block of HTML or a list of text with optional icons.

    global $THEME;

    print_side_block_start($heading, $width);

    if ($content) {
        echo "$content";
    } else {
        echo "<table width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"2\">";
        foreach ($list as $key => $string) {
            echo "<tr bgcolor=\"$THEME->cellcontent2\">";
            if ($icons) {
                echo "<td class=\"sideblocklinks\" valign=\"top\" width=\"16\">".$icons[$key]."</td>";
            }
            echo "<td class=\"sideblocklinks\" valign=\"top\" width=\"*\"><font size=\"-1\">$string</font></td>";
            echo "</tr>";
        }
        if ($footer) {
            echo "<tr bgcolor=\"$THEME->cellcontent2\">";
            if ($icons) {
                echo "<td class=\"sideblocklinks\" valign=\"top\" width=\"16\">&nbsp;</td>";
            }
            echo "<td class=\"sideblocklinks\"><font size=\"-1\">$footer</td>";
            echo "</tr>";
        }
        echo "</table>";
    }

    print_side_block_end();
}

function print_side_block_start($heading="", $width=180, $class="sideblockmain") {
// Starts a nice side block with an optional header.

    global $THEME;

    echo "<table class=\"sideblock\" width=\"100%\" border=\"0\" cellspacing=\"0\" cellpadding=\"5\">";
    if ($heading) {
        echo "<tr>";
        echo "<td class=\"sideblockheading\" bgcolor=\"$THEME->cellheading\">$heading</td>";
        echo "</tr>";
    }
    echo "<tr>";
    echo "<td class=\"$class\" bgcolor=\"$THEME->cellcontent2\">";
}

function print_side_block_end() {
    echo "</td></tr>";
    echo "</table><br />";
}


function print_admin_links ($siteid, $width=180) {
    global $CFG, $THEME;

    if (empty($THEME->custompix)) {
        $pixpath = "$CFG->wwwroot/pix";
    } else {
        $pixpath = "$CFG->wwwroot/theme/$CFG->theme/pix";
    }

    if (isadmin()) {
	    $moddata[]="<a href=\"$CFG->wwwroot/$CFG->admin/configure.php\">".get_string("configuration")."</a>...";
		$modicon[]="<img src=\"$pixpath/i/admin.gif\" height=16 width=16 alt=\"\" />";

	    $moddata[]="<a href=\"$CFG->wwwroot/$CFG->admin/users.php\">".get_string("users")."</a>...";
		$modicon[]="<img src=\"$pixpath/i/users.gif\" height=16 width=16 alt=\"\" />";
    }

    if (iscreator()) {
	    $moddata[]="<a href=\"$CFG->wwwroot/course/index.php?edit=on\">".get_string("courses")."</a>";
		$modicon[]="<img src=\"$pixpath/i/course.gif\" height=16 width=16 alt=\"\" />";
        $fulladmin = "";
    }

    if (isadmin()) {
		$moddata[]="<a href=\"$CFG->wwwroot/course/log.php?id=$siteid\">".get_string("logs")."</a>";
		$modicon[]="<img src=\"$pixpath/i/log.gif\" height=16 width=16 alt=\"\" />";

		$moddata[]="<a href=\"$CFG->wwwroot/files/index.php?id=$siteid\">".get_string("sitefiles")."</a>";
		$modicon[]="<img src=\"$pixpath/i/files.gif\" height=16 width=16 alt=\"\" />";

		if (file_exists("$CFG->dirroot/$CFG->admin/$CFG->dbtype")) {
            $moddata[]="<a href=\"$CFG->wwwroot/$CFG->admin/$CFG->dbtype/frame.php\">".
                        get_string("managedatabase")."</a>";
		    $modicon[]="<img src=\"$pixpath/i/settings.gif\" height=16 width=16 alt=\"\" />";
		}
        $fulladmin = "<p><a href=\"$CFG->wwwroot/$CFG->admin/\">".get_string("admin")."</a>...";
    }

    print_side_block(get_string("administration"), "", $moddata, $modicon, $fulladmin, $width);

    echo "<img src=\"$CFG->wwwroot/pix/spacer.gif\" width=\"$width\" height=1><br>";
}

function print_course_admin_links($course, $width=180) {
    global $USER, $CFG, $THEME;

    if (isguest()) {
        return true;
    }
    if (empty($THEME->custompix)) {
        $pixpath = "$CFG->wwwroot/pix";
        $modpixpath = "$CFG->wwwroot/mod";
    } else {
        $pixpath = "$CFG->wwwroot/theme/$CFG->theme/pix";
        $modpixpath = "$CFG->wwwroot/theme/$CFG->theme/pix/mod";
    }
    if (isteacher($course->id)) {
        if (isteacheredit($course->id)) {
            $adminicon[]="<img src=\"$pixpath/i/edit.gif\" height=16 width=16 alt=\"\">";
            if (isediting($course->id)) {
                $admindata[]="<a href=\"view.php?id=$course->id&edit=off\">".get_string("turneditingoff")."</a>";
            } else {
                $admindata[]="<a href=\"view.php?id=$course->id&edit=on\">".get_string("turneditingon")."</a>";
            }
            $admindata[]="<a href=\"edit.php?id=$course->id\">".get_string("settings")."...</a>";
            $adminicon[]="<img src=\"$pixpath/i/settings.gif\" height=16 width=16 alt=\"\">";

            if (iscreator() or !empty($CFG->teacherassignteachers)) {
                if (!$course->teachers) {
                    $course->teachers = get_string("defaultcourseteachers");
                }
                $admindata[]="<a href=\"teacher.php?id=$course->id\">$course->teachers...</a>";
                $adminicon[]="<img src=\"$pixpath/i/users.gif\" height=16 width=16 alt=\"\">";
            }
        }

        if (!$course->students) {
            $course->students = get_string("defaultcoursestudents");
        }
        $admindata[]="<a href=\"student.php?id=$course->id\">$course->students...</a>";
        $adminicon[]="<img src=\"$pixpath/i/users.gif\" height=16 width=16 alt=\"\">";

        if (isteacheredit($course->id)) {
            $admindata[]="<a href=\"$CFG->wwwroot/backup/backup.php?id=$course->id\">".get_string("backup")."...</a>";
            $adminicon[]="<img src=\"$pixpath/i/backup.gif\" height=16 width=16 alt=\"\">";

            //Only showed if "backupdata" dir exists
            if (is_dir("$CFG->dataroot/$course->id/backupdata")) {
                $admindata[]="<a href=\"$CFG->wwwroot/files/index.php?id=$course->id&wdir=/backupdata\">".get_string("restore")."...</a>";
                $adminicon[]="<img src=\"$pixpath/i/restore.gif\" height=16 width=16 alt=\"\">";
            }
            $admindata[]="<a href=\"scales.php?id=$course->id\">".get_string("scales")."...</a>";
            $adminicon[]="<img src=\"$pixpath/i/scales.gif\" height=16 width=16 alt=\"\">";
        }

        $admindata[]="<a href=\"grades.php?id=$course->id\">".get_string("grades")."...</a>";
        $adminicon[]="<img src=\"$pixpath/i/grades.gif\" height=16 width=16 alt=\"\">";

        $admindata[]="<a href=\"log.php?id=$course->id\">".get_string("logs")."...</a>";
        $adminicon[]="<img src=\"$pixpath/i/log.gif\" height=16 width=16 alt=\"\">";

        $admindata[]="<a href=\"$CFG->wwwroot/files/index.php?id=$course->id\">".get_string("files")."...</a>";
        $adminicon[]="<img src=\"$pixpath/i/files.gif\" height=16 width=16 alt=\"\">";

        $admindata[]="<a href=\"$CFG->wwwroot/doc/view.php?id=$course->id&file=teacher.html\">".get_string("help")."...</a>";
        $adminicon[]="<img src=\"$modpixpath/resource/icon.gif\" height=16 width=16 alt=\"\">";

        if ($teacherforum = forum_get_course_forum($course->id, "teacher")) {
            $admindata[]="<a href=\"$CFG->wwwroot/mod/forum/view.php?f=$teacherforum->id\">$teacherforum->name</a>";
            $adminicon[]="<img src=\"$modpixpath/forum/icon.gif\" height=16 width=16 alt=\"\">";
        }

    } else if (!isguest()) {  // Students menu
        if ($course->showgrades) {
            $admindata[]="<a href=\"grade.php?id=$course->id\">".get_string("grades")."...</a>";
            $adminicon[]="<img src=\"$pixpath/i/grades.gif\" height=16 width=16 alt=\"\">";
        }
        if ($CFG->auth == "email" or $CFG->auth == "none" or $CFG->auth == "manual") {
            $admindata[]="<a href=\"$CFG->wwwroot/login/change_password.php?id=$course->id\">".
                          get_string("changepassword")."...</a>";
            $adminicon[]="<img src=\"$pixpath/i/user.gif\" height=16 width=16 alt=\"\">";
        } else if ($CFG->changepassword) {
            $admindata[]="<a href=\"$CFG->changepassword\">".get_string("changepassword")."...</a>";
            $adminicon[]="<img src=\"$pixpath/i/user.gif\" height=16 width=16 alt=\"\">";
        }
    }

    if (!empty($admindata)) {
        print_side_block(get_string("administration"), "", $admindata, $adminicon, "", $width);
    }
}


function make_categories_list(&$list, &$parents, $category=NULL, $path="") {
/// Given an empty array, this function recursively travels the
/// categories, building up a nice list for display.  It also makes
/// an array that list all the parents for each category.

    if ($category) {
        if ($path) {
            $path = "$path / $category->name";
        } else {
            $path = "$category->name";
        }
        $list[$category->id] = $path;
    } else {
        $category->id = 0;
    }

    if ($categories = get_categories("$category->id")) {   // Print all the children recursively
        foreach ($categories as $cat) {
            if (!empty($category->id)) {
                $parents[$cat->id]   = $parents[$category->id];
                $parents[$cat->id][] = $category->id;
            }
            make_categories_list($list, $parents, $cat, $path);
        }
    }
}


function print_whole_category_list($category=NULL, $displaylist=NULL, $parentslist=NULL, $depth=-1) {
/// Recursive function to print out all the categories in a nice format
/// with or without courses included

    if (!$displaylist) {
        make_categories_list($displaylist, $parentslist);
    }

    if ($category) {
        if ($category->visible or iscreator()) {
            print_category_info($category, $depth);
        } else {
            return;  // Don't bother printing children of invisible categories
        }

    } else {
        $category->id = "0";
    }

    if ($categories = get_categories($category->id)) {   // Print all the children recursively
        $countcats = count($categories);
        $count = 0;
        $first = true;
        $last = false;
        foreach ($categories as $cat) {
            $count++;
            if ($count == $countcats) {
                $last = true;
            }
            $up = $first ? false : true;
            $down = $last ? false : true;
            $first = false;

            print_whole_category_list($cat, $displaylist, $parentslist, $depth + 1);
        }
    }
}


function print_category_info($category, $depth) {
/// Prints the category info in indented fashion
/// This function is only used by print_whole_category_list() above

    global $CFG;
    static $strallowguests, $strrequireskey, $strsummary;

    if (empty($strsummary)) {
        $strallowguests = get_string("allowguests");
        $strrequireskey = get_string("requireskey");
        $strsummary = get_string("summary");
    }

    if (empty($THEME->custompix)) {
        $pixpath = "$CFG->wwwroot/pix";
    } else {
        $pixpath = "$CFG->wwwroot/theme/$CFG->theme/pix";
    }
    $catlinkcss = $category->visible ? "" : " class=\"dimmed\" ";

    if ($CFG->frontpage == FRONTPAGECOURSELIST) {
        $catimage = "<img src=\"$pixpath/i/course.gif\" width=16 height=16 border=0>";
    } else {
        $catimage = "&nbsp";
    }

    echo "\n\n<table border=0 cellpadding=3 cellspacing=0 width=\"100%\"><tr>";

    if ($CFG->frontpage == FRONTPAGECOURSELIST) {
        $courses = get_courses($category->id);

        echo "<tr>";

        if ($depth) {
            $indent = $depth*30;
            $rows = count($courses) + 1;
            echo "<td rowspan=\"$rows\" valign=\"top\" width=\"$indent\">";
            print_spacer(10, $indent);
            echo "</td>";
        }

        echo "<td valign=\"top\">$catimage</td>";
        echo "<td valign=\"top\" width=\"100%\" class=\"categoryname\">";
        echo "<a $catlinkcss href=\"$CFG->wwwroot/course/category.php?id=$category->id\">$category->name</a>";
        echo "</td>";
        echo "<td class=\"categoryname\">&nbsp;</td>";
        echo "</tr>\n";

        if ($courses) {
            foreach ($courses as $course) {
                $linkcss = $course->visible ? "" : " class=\"dimmed\" ";
                echo "<tr><td valign=\"top\" width=\"30\">&nbsp;";
                echo "</td>\n<td valign=\"top\" width=\"100%\" class=\"coursename\">";
                echo "<a $linkcss href=\"$CFG->wwwroot/course/view.php?id=$course->id\">$course->fullname</a>";
                echo "</td>\n<td align=\"right\" valign=\"top\" nowrap class=\"coursename\">";
                if ($course->guest ) {
                    echo "<a title=\"$strallowguests\" href=\"$CFG->wwwroot/course/view.php?id=$course->id\">";
                    echo "<img hspace=1 alt=\"\" height=16 width=16 border=0 src=\"$pixpath/i/user.gif\"></a>";
                } else {
                    echo "<img alt=\"\" height=16 width=18 border=0 src=\"$pixpath/spacer.gif\">";
                }
                if ($course->password) {
                    echo "<a title=\"$strrequireskey\" href=\"$CFG->wwwroot/course/view.php?id=$course->id\">";
                    echo "<img hspace=1 alt=\"\" height=16 width=16 border=0 src=\"$pixpath/i/key.gif\"></a>";
                } else {
                    echo "<img alt=\"\" height=16 width=18 border=0 src=\"$pixpath/spacer.gif\">";
                }
                if ($course->summary) {
                    link_to_popup_window ("/course/info.php?id=$course->id", "courseinfo",
                                          "<img hspace=1 alt=\"info\" height=16 width=16 border=0 src=\"$pixpath/i/info.gif\">",
                                           400, 500, $strsummary);
                } else {
                    echo "<img alt=\"\" height=16 width=18 border=0 src=\"$pixpath/spacer.gif\">";
                }
                echo "</td></tr>\n";
            }
        }
    } else {

        if ($depth) {
            $indent = $depth*20;
            echo "<td valign=\"top\" width=\"$indent\">";
            print_spacer(10, $indent);
            echo "</td>";
        }

        echo "<td valign=\"top\" width=\"100%\" class=\"categoryname\">";
        echo "<a $catlinkcss href=\"$CFG->wwwroot/course/category.php?id=$category->id\">$category->name</a>";
        echo "</td>";
        echo "<td valign=\"top\" class=\"categoryname\">$category->coursecount</td></tr>";
    }
    echo "\n</table>\n";
}

function print_courses_sideblock($category=0, $width="100%") {
    global $CFG, $THEME, $USER;

    if (empty($THEME->custompix)) {
        $icon  = "<img src=\"$CFG->wwwroot/pix/i/course.gif\"".
                 " height=\"16\" width=\"16\" alt=\"".get_string("course")."\">";
    } else {
        $icon  = "<img src=\"$CFG->wwwroot/theme/$CFG->theme/pix/i/course.gif\"".
                 " height=\"16\" width=\"16\" alt=\"".get_string("course")."\">";
    }

    if (isset($USER->id) and !isadmin()) {    // Just print My Courses
        if ($courses = get_my_courses($USER->id)) {
            foreach ($courses as $course) {
                if (!$course->category) {
                    continue;
                }
                $linkcss = $course->visible ? "" : " class=\"dimmed\" ";
                $moddata[]="<a $linkcss title=\"$course->shortname\" ".
                           "href=\"$CFG->wwwroot/course/view.php?id=$course->id\">$course->fullname</a>";
                $modicon[]=$icon;
            }
            $fulllist = "<p><a href=\"$CFG->wwwroot/course/index.php\">".get_string("fulllistofcourses")."</a>...";
            print_side_block( get_string("mycourses"), "", $moddata, $modicon, $fulllist, $width);
            return;
        }
    }

    $categories = get_categories("0");  // Parent = 0   ie top-level categories only
    if (count($categories) > 1) {     // Just print top level category links
        foreach ($categories as $category) {
            $linkcss = $category->visible ? "" : " class=\"dimmed\" ";
            $moddata[]="<a $linkcss href=\"$CFG->wwwroot/course/category.php?id=$category->id\">$category->name</a>";
            $modicon[]=$icon;
        }
        $fulllist = "<p><a href=\"$CFG->wwwroot/course/search.php\">".get_string("searchcourses")."</a>...";
    } else {                          // Just print course names of single category
        $category = array_shift($categories);
        $courses = get_courses($category->id);

        if ($courses) {
            foreach ($courses as $course) {
                $linkcss = $course->visible ? "" : " class=\"dimmed\" ";
                $moddata[]="<a $linkcss title=\"$course->shortname\" ".
                           "href=\"$CFG->wwwroot/course/view.php?id=$course->id\">$course->fullname</a>";
                $modicon[]=$icon;
            }
            $fulllist = "<p><a href=\"$CFG->wwwroot/course/index.php\">".get_string("fulllistofcourses")."</a>...";
        } else {
            $moddata = array();
            $modicon = array();
            $fulllist = get_string("nocoursesyet");
        }
    }

    print_side_block( get_string("courses"), "", $moddata, $modicon, $fulllist, $width);
}


function print_courses($category, $width="100%") {
/// Category is 0 (for all courses) or an object

    global $CFG, $THEME;

    if (empty($category)) {
        $categories = get_categories(0);  // Parent = 0   ie top-level categories only
        if (count($categories) == 1) {
            $category   = array_shift($categories);
            $courses    = get_courses($category->id);
        } else {
            $courses    = get_courses("all");
        }
        unset($categories);
    } else {
        $categories = get_categories($category->id);  // sub categories
        $courses    = get_courses($category->id);
    }

    if ($courses) {
        foreach ($courses as $course) {
            print_course($course, $width);
            echo "<br />\n";
        }
    } else {
        print_heading(get_string("nocoursesyet"));
    }

}


function print_course($course, $width="100%") {

    global $CFG, $THEME;

    if (! $site = get_site()) {
        error("Could not find a site!");
    }

    if (empty($THEME->custompix)) {
        $pixpath = "$CFG->wwwroot/pix";
    } else {
        $pixpath = "$CFG->wwwroot/theme/$CFG->theme/pix";
    }

    print_simple_box_start("center", "$width");

    $linkcss = $course->visible ? "" : " class=\"dimmed\" ";

    echo "<table width=\"100%\">";
    echo "<tr valign=top>";
    echo "<td valign=top width=50%>";
    echo "<p><font size=3><b><a title=\"".get_string("entercourse")."\"
              $linkcss href=\"$CFG->wwwroot/course/view.php?id=$course->id\">$course->fullname</a></b></font></p>";
    if ($teachers = get_course_teachers($course->id)) {
        echo "<p><font size=\"1\">\n";
        foreach ($teachers as $teacher) {
            if ($teacher->authority > 0) {
                if (!$teacher->role) {
                    $teacher->role = $course->teacher;
                }
                echo "$teacher->role: <a href=\"$CFG->wwwroot/user/view.php?id=$teacher->id&course=$site->id\">$teacher->firstname $teacher->lastname</a><br />";
            }
        }
        echo "</font></p>";
    }
    if ($course->guest) {
        $strallowguests = get_string("allowguests");
        echo "<a title=\"$strallowguests\" href=\"$CFG->wwwroot/course/view.php?id=$course->id\">";
        echo "<img vspace=4 alt=\"$strallowguests\" height=16 width=16 border=0 src=\"$pixpath/i/user.gif\"></a>&nbsp;&nbsp;";
    }
    if ($course->password) {
        $strrequireskey = get_string("requireskey");
        echo "<a title=\"$strrequireskey\" href=\"$CFG->wwwroot/course/view.php?id=$course->id\">";
        echo "<img vspace=4 alt=\"$strrequireskey\" height=16 width=16 border=0 src=\"$pixpath/i/key.gif\"></a>";
    }


    echo "</td><td valign=top width=50%>";
    echo "<p><font size=2>".text_to_html($course->summary)."</font></p>";
    echo "</td></tr>";
    echo "</table>";

    print_simple_box_end();
}


function print_my_moodle() {
/// Prints custom user information on the home page.
/// Over time this can include all sorts of information

    global $USER, $CFG;

    if (!isset($USER->id)) {
        error("It shouldn't be possible to see My Moodle without being logged in.");
    }

    if ($courses = get_my_courses($USER->id)) {
        foreach ($courses as $course) {
            if (!$course->category) {
                continue;
            }
            print_course($course, "100%");
            echo "<br />\n";
        }

        echo "<table width=\"100%\"><tr><td align=\"center\">";
        print_course_search("", false, "short");
        echo "</td><td align=\"center\">";
        print_single_button("$CFG->wwwroot/course/index.php", NULL, get_string("fulllistofcourses"), "get");
        echo "</td></tr></table>\n";
    } else {
        if (count_records("course_categories") > 1) {
            print_simple_box_start("center", "100%", "#FFFFFF", 5, "categorybox");
            print_whole_category_list();
            print_simple_box_end();
        } else {
            print_courses(0, "100%");
        }
    }
}


function print_course_search($value="", $return=false, $format="plain") {

    global $CFG;

    $strsearchcourses= get_string("searchcourses");

    if ($format == "plain") {
        $output  = "<center><p align=\"center\" class=\"coursesearchbox\">";
        $output .= "<form name=\"coursesearch\" action=\"$CFG->wwwroot/course/search.php\" method=\"get\">";
        $output .= "<input type=\"text\" size=30 name=\"search\" value=\"$value\">";
        $output .= "<input type=\"submit\" value=\"$strsearchcourses\">";
        $output .= "</form></p></center>";
    } else if ($format == "short") {
        $output  = "<center><p align=\"center\" class=\"coursesearchbox\">";
        $output .= "<form name=\"coursesearch\" action=\"$CFG->wwwroot/course/search.php\" method=\"get\">";
        $output .= "<input type=\"text\" size=12 name=\"search\" value=\"$value\">";
        $output .= "<input type=\"submit\" value=\"$strsearchcourses\">";
        $output .= "</form></p></center>";
    } else if ($format == "navbar") {
        $output = "<table border=0 cellpadding=0 cellspacing=0><tr><td nowrap>";
        $output .= "<form name=\"coursesearch\" action=\"$CFG->wwwroot/course/search.php\" method=\"get\">";
        $output .= "<input type=\"text\" size=20 name=\"search\" value=\"$value\">";
        $output .= "<input type=\"submit\" value=\"$strsearchcourses\">";
        $output .= "</form>";
        $output .= "</td></tr></table>";
    }

    if ($return) {
        return $output;
    }
    echo $output;
}

/// MODULE FUNCTIONS /////////////////////////////////////////////////////////////////

function add_course_module($mod) {

    $mod->added = time();

    return insert_record("course_modules", $mod);
}

function add_mod_to_section($mod, $beforemod=NULL) {
/// Given a full mod object with section and course already defined
/// If $before is specified, then this is an existing ID which we
/// will insert the new module before
///
/// Returns the course_sections ID where the mod is inserted

    if ($section = get_record("course_sections", "course", "$mod->course", "section", "$mod->section")) {

        $section->sequence = trim($section->sequence);

        if (empty($section->sequence)) {
            $newsequence = "$mod->coursemodule";

        } else if ($beforemod) {
            $modarray = explode(",", $section->sequence);

            if ($key = array_keys ($modarray, $beforemod->id)) {
                $insertarray = array($mod->id, $beforemod->id);
                array_splice($modarray, $key[0], 1, $insertarray);
                $newsequence = implode(",", $modarray);

            } else {  // Just tack it on the end anyway
                $newsequence = "$section->sequence,$mod->coursemodule";
            }

        } else {
            $newsequence = "$section->sequence,$mod->coursemodule";
        }

        if (set_field("course_sections", "sequence", $newsequence, "id", $section->id)) {
            return $section->id;     // Return course_sections ID that was used.
        } else {
            return 0;
        }

    } else {  // Insert a new record
        $section->course = $mod->course;
        $section->section = $mod->section;
        $section->summary = "";
        $section->sequence = $mod->coursemodule;
        return insert_record("course_sections", $section);
    }
}

function hide_course_module($mod) {
    return set_field("course_modules", "visible", 0, "id", $mod);
}

function show_course_module($mod) {
    return set_field("course_modules", "visible", 1, "id", $mod);
}

function delete_course_module($mod) {
    return set_field("course_modules", "deleted", 1, "id", $mod);
}

function delete_mod_from_section($mod, $section) {

    if ($section = get_record("course_sections", "id", "$section") ) {

        $modarray = explode(",", $section->sequence);

        if ($key = array_keys ($modarray, $mod)) {
            array_splice($modarray, $key[0], 1);
            $newsequence = implode(",", $modarray);
            return set_field("course_sections", "sequence", $newsequence, "id", $section->id);
        } else {
            return false;
        }

    }
    return false;
}

function move_section($course, $section, $move) {
/// Moves a whole course section up and down within the course

    if (!$move) {
        return true;
    }

    $sectiondest = $section + $move;

    if ($sectiondest > $course->numsections or $sectiondest < 1) {
        return false;
    }

    if (!$sectionrecord = get_record("course_sections", "course", $course->id, "section", $section)) {
        return false;
    }

    if (!$sectiondestrecord = get_record("course_sections", "course", $course->id, "section", $sectiondest)) {
        return false;
    }

    if (!set_field("course_sections", "section", $sectiondest, "id", $sectionrecord->id)) {
        return false;
    }
    if (!set_field("course_sections", "section", $section, "id", $sectiondestrecord->id)) {
        return false;
    }
    return true;
}


function moveto_module($mod, $section, $beforemod=NULL) {
/// All parameters are objects
/// Move the module object $mod to the specified $section
/// If $beforemod exists then that is the module
/// before which $modid should be inserted

/// Remove original module from original section

    if (! delete_mod_from_section($mod->id, $mod->section)) {
        notify("Could not delete module from existing section");
    }

/// Update module itself if necessary

    if ($mod->section != $section->id) {
        $mod->section = $section->id;

        if (!update_record("course_modules", $mod)) {
            return false;
        }
    }

/// Add the module into the new section

    $mod->course       = $section->course;
    $mod->section      = $section->section;  // need relative reference
    $mod->coursemodule = $mod->id;

    if (! add_mod_to_section($mod, $beforemod)) {
        return false;
    }

    return true;

}



function move_module($cm, $move) {
/// Moves an activity module up and down within the course

    if (!$move) {
        return true;
    }

    if (! $thissection = get_record("course_sections", "id", $cm->section)) {
        error("This course section doesn't exist");
    }

    $mods = explode(",", $thissection->sequence);

    $len = count($mods);
    $pos = array_keys($mods, $cm->id);
    $thepos = $pos[0];

    if ($len == 0 || count($pos) == 0 ) {
        error("Very strange. Could not find the required module in this section.");
    }

    if ($len == 1) {
        $first = true;
        $last = true;
    } else {
        $first = ($thepos == 0);
        $last  = ($thepos == $len - 1);
    }

    if ($move < 0) {    // Moving the module up

        if ($first) {
            if ($thissection->section == 0) {  // First section, do nothing
                return true;

            } else {               // Push onto end of previous section
                $prevsectionnumber = $thissection->section - 1;
                if (! $prevsection = get_record("course_sections", "course", "$thissection->course",
                                                                   "section", "$prevsectionnumber")) {
                    error("Previous section ($prevsection->id) doesn't exist");
                }

                if (!empty($prevsection->sequence)) {
                    $newsequence = "$prevsection->sequence,$cm->id";
                } else {
                    $newsequence = "$cm->id";
                }

                if (! set_field("course_sections", "sequence", $newsequence, "id", $prevsection->id)) {
                    error("Previous section could not be updated");
                }

                if (! set_field("course_modules", "section", $prevsection->id, "id", $cm->id)) {
                    error("Module could not be updated");
                }

                array_splice($mods, 0, 1);
                $newsequence = implode(",", $mods);
                if (! set_field("course_sections", "sequence", $newsequence, "id", $thissection->id)) {
                    error("Module could not be updated");
                }

                return true;

            }
        } else {        // move up within this section
            $swap = $mods[$thepos-1];
            $mods[$thepos-1] = $mods[$thepos];
            $mods[$thepos] = $swap;

            $newsequence = implode(",", $mods);
            if (! set_field("course_sections", "sequence", $newsequence, "id", $thissection->id)) {
                error("This section could not be updated");
            }
            return true;
        }

    } else {            // Moving the module down

        if ($last) {
            $nextsectionnumber = $thissection->section + 1;
            if ($nextsection = get_record("course_sections", "course", "$thissection->course",
                                                               "section", "$nextsectionnumber")) {

                if (!empty($nextsection->sequence)) {
                    $newsequence = "$cm->id,$nextsection->sequence";
                } else {
                    $newsequence = "$cm->id";
                }

                if (! set_field("course_sections", "sequence", $newsequence, "id", $nextsection->id)) {
                    error("Next section could not be updated");
                }

                if (! set_field("course_modules", "section", $nextsection->id, "id", $cm->id)) {
                    error("Module could not be updated");
                }

                array_splice($mods, $thepos, 1);
                $newsequence = implode(",", $mods);
                if (! set_field("course_sections", "sequence", $newsequence, "id", $thissection->id)) {
                    error("This section could not be updated");
                }
                return true;

            } else {        // There is no next section, so just return
                return true;

            }
        } else {      // move down within this section
            $swap = $mods[$thepos+1];
            $mods[$thepos+1] = $mods[$thepos];
            $mods[$thepos] = $swap;

            $newsequence = implode(",", $mods);
            if (! set_field("course_sections", "sequence", $newsequence, "id", $thissection->id)) {
                error("This section could not be updated");
            }
            return true;
        }
    }
}

function make_editing_buttons($moduleid, $absolute=false, $visible=true, $moveselect=true) {
    global $CFG, $THEME;

    static $str = '';
    if (empty($str)) {
        $str->delete   = get_string("delete");
        $str->move     = get_string("move");
        $str->moveup   = get_string("moveup");
        $str->movedown = get_string("movedown");
        $str->update   = get_string("update");
        $str->hide     = get_string("hide");
        $str->show     = get_string("show");
    }

    if ($absolute) {
        $path = "$CFG->wwwroot/course";
    } else {
        $path = ".";
    }

    if (empty($THEME->custompix)) {
        $pixpath = "$path/../pix";
    } else {
        $pixpath = "$path/../theme/$CFG->theme/pix";
    }

    if ($visible) {
        $hideshow = "<a title=\"$str->hide\" href=\"$path/mod.php?hide=$moduleid\"><img".
                    " src=\"$pixpath/t/hide.gif\" hspace=2 height=11 width=11 border=0></a> ";
    } else {
        $hideshow = "<a title=\"$str->show\" href=\"$path/mod.php?show=$moduleid\"><img".
                    " src=\"$pixpath/t/show.gif\" hspace=2 height=11 width=11 border=0></a> ";
    }

    if ($moveselect) {
        $move =     "<a title=\"$str->move\" href=\"$path/mod.php?copy=$moduleid\"><img".
                    " src=\"$pixpath/t/move.gif\" height=\"11\" width=\"11\" border=\"0\"></a> ";
    } else {
        $move =     "<a title=\"$str->moveup\" href=\"$path/mod.php?id=$moduleid&move=-1\"><img".
                    " src=\"$pixpath/t/up.gif\" height=11 width=11 border=0></a> ".
                    "<a title=\"$str->movedown\" href=\"$path/mod.php?id=$moduleid&move=1\"><img".
                    " src=\"$pixpath/t/down.gif\" height=11 width=11 border=0></a> ";
    }

    return "<a title=\"$str->delete\" href=\"$path/mod.php?delete=$moduleid\"><img".
           " src=\"$pixpath/t/delete.gif\" height=11 width=11 border=0></a> $move".
           "<a title=\"$str->update\" href=\"$path/mod.php?update=$moduleid\"><img".
           " src=\"$pixpath/t/edit.gif\" height=11 width=11 border=0></a> $hideshow";
}

?>