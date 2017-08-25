<?PHP // $Id$

//
// moodlelib.php
//
// Large collection of useful functions used by many parts of Moodle.
//
// Martin Dougiamas, 2000
//


/// STANDARD WEB PAGE PARTS ///////////////////////////////////////////////////

function print_header ($title="", $heading="", $navigation="", $focus="", $meta="", $cache=true, $button="") {
// $title - appears top of window
// $heading - appears top of page
// $navigation - premade navigation string
// $focus - indicates form element eg  inputform.password
// $meta - meta tags in the header
// $cache - should this page be cacheable?
// $button - code for a button in the top-right
    global $USER, $CFG, $THEME;

    if (file_exists("$CFG->dirroot/theme/$CFG->theme/styles.css")) {
        $styles = "$CFG->wwwroot/theme/$CFG->theme/styles.css";
    } else {
        $styles = "$CFG->wwwroot/theme/standard/styles.css";
    }

    if (!$button and $navigation) {
        if (isset($USER->id)) {
            $button = "<FONT SIZE=2><A HREF=\"$CFG->wwwroot/login/logout.php\">".get_string("logout")."</A></FONT>";
        } else {
            $button = "<FONT SIZE=2><A HREF=\"$CFG->wwwroot/login\">".get_string("login")."</A></FONT>";
        }
    }

    if (!$cache) {   // Do everything we can to prevent clients and proxies caching
        @header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
        @header("Pragma: no-cache");
        $meta .= "\n<META HTTP-EQUIV=\"Pragma\" CONTENT=\"no-cache\">";
        $meta .= "\n<META HTTP-EQUIV=\"Expires\" CONTENT=\"0\">";
    }

    include ("$CFG->dirroot/theme/$CFG->theme/header.html");
}

function print_footer ($course=NULL) {
// Can provide a course object to make the footer contain a link to
// to the course home page, otherwise the link will go to the site home
    global $USER, $CFG, $THEME;

    if ($course) {
        if ($course == "home") {   // special case for site home page - please do not remove
            if (!$dversion = get_field("config", "value", "name", "version")) {
                $dversion = "unknown!";
            }
            $homelink  = "<P ALIGN=center><A TITLE=\"Version $dversion: Click to visit moodle.com\" HREF=\"http://moodle.com/\">";
            $homelink .= "<IMG WIDTH=85 HEIGHT=25 SRC=\"pix/madewithmoodle.gif\" BORDER=0></A></P>";
        } else {
            $homelink = "<A TARGET=_top HREF=\"$CFG->wwwroot/course/view.php?id=$course->id\">$course->shortname</A>";
        }
    } else {
        $homelink = "<A TARGET=_top HREF=\"$CFG->wwwroot\">".get_string("home")."</A>";
    }
    if ($USER->realuser) {
        if ($realuser = get_record("user", "id", $USER->realuser)) {
            $realuserinfo = " [$realuser->firstname $realuser->lastname] ";
        }
    }
    if ($USER->id) {
        $loggedinas = $realuserinfo.get_string("loggedinas", "moodle", "$USER->firstname $USER->lastname").
                      " (<A HREF=\"$CFG->wwwroot/login/logout.php\">".get_string("logout")."</A>)";
    } else {
        $loggedinas = get_string("loggedinnot", "moodle").
                      " (<A HREF=\"$CFG->wwwroot/login/\">".get_string("login")."</A>)";
    }

    include ("$CFG->dirroot/theme/$CFG->theme/footer.html");
}

function print_navigation ($navigation) {
   global $CFG;

   if ($navigation) {
       if (! $site = get_site()) {
           $site->shortname = get_string("home");;
       }
       echo "<A TARGET=_top HREF=\"$CFG->wwwroot/\">$site->shortname</A> -> $navigation";
   }
}

function print_heading($text, $align="CENTER", $size=3) {
    echo "<P ALIGN=\"$align\"><FONT SIZE=\"$size\"><B>$text</B></FONT></P>";
}

function print_continue($link) {
    global $HTTP_REFERER;

    if (!$link) {
        $link = $HTTP_REFERER;
    }

    print_heading("<A HREF=\"$link\">".get_string("continue")."</A>");
}


function print_simple_box($message, $align="", $width="", $color="#FFFFFF", $padding=5, $border=1) {
    print_simple_box_start($align, $width, $color, $padding, $border);
    echo "<P>$message</P>";
    print_simple_box_end();
}

function print_simple_box_start($align="", $width="", $color="#FFFFFF", $padding=5, $border=1) {
    global $THEME;

    if ($align) {
        $tablealign = "ALIGN=\"$align\"";
    }
    if ($width) {
        $tablewidth = "WIDTH=\"$width\"";
        $innertablewidth = "WIDTH=\"100%\"";
    }
    echo "<TABLE $tablealign $tablewidth BORDER=0 CELLPADDING=\"$border\" CELLSPACING=0>";
    echo "<TR><TD BGCOLOR=\"$THEME->borders\">\n";
    echo "<TABLE $innertablewidth BORDER=0 CELLPADDING=\"$padding\" CELLSPACING=0><TR><TD BGCOLOR=\"$color\">";
}

function print_simple_box_end() {
    echo "</TD></TR></TABLE>";
    echo "</TD></TR></TABLE>";
}

function print_single_button($link, $options, $label="OK") {
    echo "<FORM ACTION=\"$link\" METHOD=GET>";
    if ($options) {
        foreach ($options as $name => $value) {
            echo "<INPUT TYPE=hidden NAME=\"$name\" VALUE=\"$value\">";
        }
    }
    echo "<INPUT TYPE=submit VALUE=\"$label\"></FORM>";
}

function print_user_picture($userid, $courseid, $picture, $large=false, $returnstring=false) {
    global $CFG;

    $output = "<A HREF=\"$CFG->wwwroot/user/view.php?id=$userid&course=$courseid\">";
    if ($large) {
        $file = "f1.jpg";
        $size = 100;
    } else {
        $file = "f2.jpg";
        $size = 35;
    }
    if ($picture) {
        if (iswindows()) { // Workaround for a PATH_INFO problem on Windows PHP
            $output .= "<IMG SRC=\"$CFG->wwwroot/user/pix.php?file=/$userid/$file\" BORDER=0 WIDTH=$size HEIGHT=$size ALT=\"\">";
        } else {           // Use this method if possible for better caching
            $output .= "<IMG SRC=\"$CFG->wwwroot/user/pix.php/$userid/$file\" BORDER=0 WIDTH=$size HEIGHT=$size ALT=\"\">";
        }
    } else {
        $output .= "<IMG SRC=\"$CFG->wwwroot/user/default/$file\" BORDER=0 WIDTH=$size HEIGHT=$size ALT=\"\">";
    }
    $output .= "</A>";

    if ($returnstring) {
        return $output;
    } else {
        echo $output;
    }
}

function print_table($table) {
// Prints a nicely formatted table.
// $table is an object with three properties.
//     $table->head      is an array of heading names.
//     $table->align     is an array of column alignments
//     $table->data[]    is an array of arrays containing the data.
//     $table->width     is an percentage of the page

    if ($table->align) {
        foreach ($table->align as $key => $aa) {
            if ($aa) {
                $align[$key] = "ALIGN=\"$aa\"";
            } else {
                $align[$key] = "";
            }
        }
    }

    if (!$table->width) {
        $table->width = "80%";
    }

    print_simple_box_start("CENTER", "$table->width", "#FFFFFF", 0);
    echo "<TABLE WIDTH=100% BORDER=0 valign=top align=center cellpadding=10 cellspacing=1>\n";

    if ($table->head) {
        echo "<TR>";
        foreach ($table->head as $key => $heading) {
            echo "<TH ".$align[$key].">$heading</TH>";
        }
        echo "</TR>\n";
    }

    foreach ($table->data as $row) {
        echo "<TR VALIGN=TOP>";
        foreach ($row as $key => $item) {
            echo "<TD ".$align[$key].">$item</TD>";
        }
        echo "</TR>\n";
    }
    echo "</TABLE>\n";
    print_simple_box_end();

    return true;
}

function print_editing_switch($courseid) {
    global $CFG, $USER;

    if (isteacher($courseid)) {
        if ($USER->editing) {
            echo "<A HREF=\"$CFG->wwwroot/course/view.php?id=$courseid&edit=off\">Turn editing off</A>";
        } else {
            echo "<A HREF=\"$CFG->wwwroot/course/view.php?id=$courseid&edit=on\">Turn editing on</A>";
        }
    }
}

function update_course_icon($courseid) {
    global $CFG, $USER;

    if (isteacher($courseid)) {
        if ($USER->editing) {
            return "<A TITLE=\"".get_string("turneditingoff")."\"
                    HREF=\"$CFG->wwwroot/course/view.php?id=$courseid&edit=off\"
                    TARGET=_top><IMG SRC=\"$CFG->wwwroot/pix/i/edit.gif\" ALIGN=right BORDER=0></A>";
        } else {
            return "<A TITLE=\"".get_string("turneditingon")."\"
                    HREF=\"$CFG->wwwroot/course/view.php?id=$courseid&edit=on\"
                    TARGET=_top><IMG SRC=\"$CFG->wwwroot/pix/i/edit.gif\" ALIGN=right BORDER=0></A>";
        }
    }
}

function update_module_icon($moduleid, $courseid) {
    global $CFG;

    if (isteacher($courseid)) {
        return "<A TITLE=\"".get_string("editthisactivity")."\" HREF=\"$CFG->wwwroot/course/mod.php?update=$moduleid&return=true\" TARGET=_top><IMG SRC=\"$CFG->wwwroot/pix/i/edit.gif\" ALIGN=right BORDER=0></A>";
    }
}


function print_date_selector($day, $month, $year, $currenttime=0) {
// Currenttime is a default timestamp in GMT
// Prints form items with the names $day, $month and $year

    if (!$currenttime) {
        $currenttime = time();
    }
    $currentdate = usergetdate($currenttime);

    for ($i=1; $i<=31; $i++) {
        $days[$i] = "$i";
    }
    for ($i=1; $i<=12; $i++) {
        $months[$i] = date("F", mktime(0,0,0,$i,1,2000));
    }
    for ($i=2000; $i<=2010; $i++) {
        $years[$i] = $i;
    }
    choose_from_menu($days,   $day,   $currentdate[mday], "");
    choose_from_menu($months, $month, $currentdate[mon],  "");
    choose_from_menu($years,  $year,  $currentdate[year], "");
}

function print_time_selector($hour, $minute, $currenttime=0) {
// Currenttime is a default timestamp in GMT
// Prints form items with the names $hour and $minute

    if (!$currenttime) {
        $currenttime = time();
    }
    $currentdate = usergetdate($currenttime);
    for ($i=0; $i<=23; $i++) {
        $hours[$i] = sprintf("%02d",$i);
    }
    for ($i=0; $i<=59; $i++) {
        $minutes[$i] = sprintf("%02d",$i);
    }
    choose_from_menu($hours,   $hour,   $currentdate[hours],   "");
    choose_from_menu($minutes, $minute, $currentdate[minutes], "");
}

function make_timestamp($year, $month=1, $day=1, $hour=0, $minute=0, $second=0) {
// Given date parts in user time, produce a GMT timestamp

   return mktime((int)$hour,(int)$minute,(int)$second,(int)$month,(int)$day,(int)$year);
}

function format_time($totalsecs) {
// Given an amount of time in seconds, prints it
// nicely as months, days, hours etc as needed

    $totalsecs = abs($totalsecs);

    $days  = floor($totalsecs/86400);
    $remainder = $totalsecs - ($days*86400);
    $hours = floor($remainder/3600);
    $remainder = $remainder - ($hours*3600);
    $mins  = floor($remainder/60);
    $secs = $remainder - ($mins*60);

    if ($secs  != 1) $ss = "s";
    if ($mins  != 1) $ms = "s";
    if ($hours != 1) $hs = "s";
    if ($days  != 1) $ds = "s";

    if ($days)  $odays  = "$days day$ds";
    if ($hours) $ohours = "$hours hr$hs";
    if ($mins)  $omins  = "$mins min$ms";
    if ($secs)  $osecs  = "$secs sec$ss";

    if ($days)  return "$odays $ohours";
    if ($hours) return "$ohours $omins";
    if ($mins)  return "$omins $osecs";
    if ($secs)  return "$osecs";
    return get_string("now");
}

function userdate($date, $format="", $timezone=99) {
// Returns a formatted string that represents a date in user time
// WARNING: note that the format is for strftime(), not date().

    global $USER;

    if ($format == "") {
        $format = "%A, %e %B %Y, %I:%M %p";
    }
    if ($timezone == 99) {
        if (isset($USER->timezone)) {
            $timezone = (float)$USER->timezone;
        }
    }
    if (abs($timezone) > 12) {
        return strftime("$format", $date);
    }
    return gmstrftime($format, $date + (int)($timezone * 3600));
}

function usergetdate($date, $timezone=99) {
// Given a $date timestamp in GMT, returns an array
// that represents the date in user time

    global $USER;

    if ($timezone == 99) {
        $timezone = (float)$USER->timezone;
    }
    if (abs($timezone) > 12) {
        return getdate($date);
    }
    //There is no gmgetdate so I have to fake it...
    $date = $date + (int)($timezone * 3600);
    $getdate["seconds"] = gmstrftime("%S", $date);
    $getdate["minutes"] = gmstrftime("%M", $date);
    $getdate["hours"]   = gmstrftime("%H", $date);
    $getdate["mday"]    = gmstrftime("%d", $date);
    $getdate["wday"]    = gmstrftime("%u", $date);
    $getdate["mon"]     = gmstrftime("%m", $date);
    $getdate["year"]    = gmstrftime("%Y", $date);
    $getdate["yday"]    = gmstrftime("%j", $date);
    $getdate["weekday"] = gmstrftime("%A", $date);
    $getdate["month"]   = gmstrftime("%B", $date);
    return $getdate;
}

function usertime($date, $timezone=99) {
// Given a GMT timestamp (seconds since epoch), offsets it by
// the timezone.  eg 3pm in India is 3pm GMT - 7 * 3600 seconds
    global $USER;

    if ($timezone == 99) {
        $timezone = (float)$USER->timezone;
    }
    if (abs($timezone) > 12) {
        return $date;
    }
    return $date - (int)($timezone * 3600);
}

function usergetmidnight($date, $timezone=99) {
// Given a time, return the GMT timestamp of the most recent midnight
// for the current user.
    global $USER;

    if ($timezone == 99) {
        $timezone = (float)$USER->timezone;
    }

    $userdate = usergetdate($date, $timezone);

    if (abs($timezone) > 12) {
        return mktime(0, 0, 0, $userdate["mon"], $userdate["mday"], $userdate["year"]);
    }

    $timemidnight = gmmktime (0, 0, 0, $userdate["mon"], $userdate["mday"], $userdate["year"]);
    return usertime($timemidnight, $timezone); // Time of midnight of this user's day, in GMT

}

function usertimezone($timezone=99) {
// returns a string that prints the user's timezone
    global $USER;

    if ($timezone == 99) {
        $timezone = (float)$USER->timezone;
    }
    if (abs($timezone) > 12) {
        return "server time";
    }
    if (abs($timezone) < 0.5) {
        return "GMT";
    }
    if ($timezone > 0) {
        return "GMT+$timezone";
    } else {
        return "GMT$timezone";
    }
}


function error ($message, $link="") {
    global $CFG, $SESSION;

    print_header(get_string("error"));
    echo "<BR>";
    print_simple_box($message, "center", "", "#FFBBBB");

    if (!$link) {
        if ( !empty($SESSION->fromurl) ) {
            $link = "$SESSION->fromurl";
            unset($SESSION->fromurl);
            save_session("SESSION");
        } else {
            $link = "$CFG->wwwroot";
        }
    }
    print_continue($link);
    print_footer();
    die;
}

function helpbutton ($page, $title="", $module="moodle", $image=true, $text="") {
    // $page = the keyword that defines a help page
    // $title = the title of links, rollover tips, alt tags etc
    // $module = which module is the page defined in
    // $image = use a help image for the link?  (otherwise uses text)
    // $text = if defined then this text is used in the page, and
    //         the $page variable is ignored.
    global $CFG;
    if ($module == "") {
        $module = "moodle";
    }
    if ($image) {
        $linkobject = "<IMG BORDER=0 ALT=\"$title\" SRC=\"$CFG->wwwroot/pix/help.gif\">";
    } else {
        $linkobject = $title;
    }
    if ($text) {
        $url = "/help.php?module=$module&text=$text";
    } else {
        $url = "/help.php?module=$module&file=$page.html";
    }
    link_to_popup_window ($url, "popup", $linkobject, 400, 500, $title);
}

function notice ($message, $link="") {
    global $THEME, $HTTP_REFERER;

    if (!$link) {
        $link = $HTTP_REFERER;
    }

    echo "<BR>";
    print_simple_box($message, "center", "", "$THEME->cellheading");
    print_heading("<A HREF=\"$link\">".get_string("continue")."</A>");
    print_footer();
    die;
}

function notice_yesno ($message, $linkyes, $linkno) {
    global $THEME;

    print_simple_box_start("center", "", "$THEME->cellheading");
    echo "<P ALIGN=CENTER><FONT SIZE=3>$message</FONT></P>";
    echo "<P ALIGN=CENTER><FONT SIZE=3><B>";
    echo "<A HREF=\"$linkyes\">".get_string("yes")."</A>";
    echo "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
    echo "<A HREF=\"$linkno\">".get_string("no")."</A>";
    echo "</B></FONT></P>";
    print_simple_box_end();
}

function redirect($url, $message="", $delay=0) {
// Uses META tags to redirect the user, after printing a notice

    echo "<META HTTP-EQUIV='Refresh' CONTENT='$delay; URL=$url'>";

    if (!empty($message)) {
        print_header();
        echo "<CENTER>";
        echo "<P>$message</P>";
        echo "<P>( <A HREF=\"$url\">".get_string("continue")."</A> )</P>";
        echo "</CENTER>";
    }
    die;
}

function notify ($message) {
    echo "<P align=center><B><FONT COLOR=#FF0000>$message</FONT></B></P>\n";
}



/// PARAMETER HANDLING ////////////////////////////////////////////////////

function require_variable($var) {
    if (! isset($var)) {
        error("A required parameter was missing");
    }
}

function optional_variable(&$var, $default=0) {
    if (! isset($var)) {
        $var = $default;
    }
}




/// DATABASE HANDLING ////////////////////////////////////////////////

function execute_sql($command) {
// Completely general

    global $db;

    $result = $db->Execute("$command");

    if ($result) {
        echo "<P><FONT COLOR=green><B>".get_string("success")."</B></FONT></P>";
        return true;
    } else {
        echo "<P><FONT COLOR=red><B>".get_string("error")."</B></FONT></P>";
        return false;
    }
}

function modify_database($sqlfile) {
// Assumes that the input text file consists of a number
// of SQL statements ENDING WITH SEMICOLONS.  The semicolons
// MUST be the last character in a line.
// Lines that are blank or that start with "#" are ignored.
// Only tested with mysql dump files (mysqldump -p -d moodle)


    if (file_exists($sqlfile)) {
        $success = true;
        $lines = file($sqlfile);
        $command = "";

        while ( list($i, $line) = each($lines) ) {
            $line = chop($line);
            $length = strlen($line);

            if ($length  &&  substr($line, 0, 1) <> "#") {
                if (substr($line, $length-1, 1) == ";") {
                    $line = substr($line, 0, $length-1);   // strip ;
                    $command .= $line;
                    if (! execute_sql($command)) {
                        $success = false;
                    }
                    $command = "";
                } else {
                    $command .= $line;
                }
            }
        }

    } else {
        $success = false;
        echo "<P>Tried to modify database, but \"$sqlfile\" doesn't exist!</P>";
    }

    return $success;
}


function record_exists($table, $field, $value) {
    global $db;

    $rs = $db->Execute("SELECT * FROM $table WHERE $field = '$value' LIMIT 1");
    if (!$rs) return false;

    if ( $rs->RecordCount() ) {
        return true;
    } else {
        return false;
    }
}

function record_exists_sql($sql) {
    global $db;

    $rs = $db->Execute($sql);
    if (!$rs) return false;

    if ( $rs->RecordCount() ) {
        return true;
    } else {
        return false;
    }
}


function count_records($table, $selector, $value) {
// Get all the records and count them
    global $db;

    $rs = $db->Execute("SELECT COUNT(*) FROM $table WHERE $selector = '$value'");
    if (!$rs) return 0;

    return $rs->fields[0];
}

function count_records_sql($sql) {
// Get all the records and count them
    global $db;

    $rs = $db->Execute("$sql");
    if (!$rs) return 0;

    return $rs->fields[0];
}

function get_record($table, $selector, $value) {
// Get a single record as an object
    global $db;

    $rs = $db->Execute("SELECT * FROM $table WHERE $selector = '$value'");
    if (!$rs) return false;

    if ( $rs->RecordCount() == 1 ) {
        return (object)$rs->fields;
    } else {
        return false;
    }
}

function get_record_sql($sql) {
// Get a single record as an object
// The sql statement is provided as a string.

    global $db;

    $rs = $db->Execute("$sql");
    if (!$rs) return false;

    if ( $rs->RecordCount() == 1 ) {
        return (object)$rs->fields;
    } else {
        return false;
    }
}

function get_records($table, $selector, $value, $sort="") {
// Get a number of records as an array of objects
// Can optionally be sorted eg "time ASC" or "time DESC"
// The "key" is the first column returned, eg usually "id"
    global $db;

    if ($sort) {
        $sortorder = "ORDER BY $sort";
    }
    $sql = "SELECT * FROM $table WHERE $selector = '$value' $sortorder";

    return get_records_sql($sql);
}

function get_records_sql($sql) {
// Get a number of records as an array of objects
// The "key" is the first column returned, eg usually "id"
// The sql statement is provided as a string.

    global $db;

    $rs = $db->Execute("$sql");
    if (!$rs) return false;

    if ( $rs->RecordCount() > 0 ) {
        if ($records = $rs->GetAssoc(true)) {
            foreach ($records as $key => $record) {
                $objects[$key] = (object) $record;
            }
            return $objects;
        } else {
            return false;
        }
    } else {
        return false;
    }
}

function get_records_sql_menu($sql) {
// Given an SQL select, this function returns an associative
// array of the first two columns.  This is most useful in
// combination with the choose_from_menu function to create
// a form menu.

    global $db;

    $rs = $db->Execute("$sql");
    if (!$rs) return false;

    if ( $rs->RecordCount() > 0 ) {
        while (!$rs->EOF) {
            $menu[$rs->fields[0]] = $rs->fields[1];
            $rs->MoveNext();
        }
        return $menu;

    } else {
        return false;
    }
}

function get_field($table, $field, $selector, $value) {
    global $db;

    $rs = $db->Execute("SELECT $field FROM $table WHERE $selector = '$value'");
    if (!$rs) return false;

    if ( $rs->RecordCount() == 1 ) {
        return $rs->fields["$field"];
    } else {
        return false;
    }
}

function set_field($table, $field, $newvalue, $selector, $value) {
    global $db;

    return $db->Execute("UPDATE $table SET $field = '$newvalue' WHERE $selector = '$value'");
}


function delete_records($table, $selector, $value) {
// Delete one or more records from a table
    global $db;

    return $db->Execute("DELETE FROM $table WHERE $selector = '$value'");
}

function insert_record($table, $dataobject) {
// Insert a record into a table and return the "id" field
// $dataobject is an object containing needed data

    global $db;

    // Determine all the fields needed
    if (! $columns = $db->MetaColumns("$table")) {
        return false;
    }

    $data = (array)$dataobject;

    // Pull out data matching these fields
    foreach ($columns as $column) {
        if ($column->name <> "id" && isset($data[$column->name]) ) {
            $ddd[$column->name] = $data[$column->name];
        }
    }

    // Construct SQL queries
    if (! $numddd = count($ddd)) {
        return 0;
    }

    $count = 0;
    $insert = "";
    $select = "";

    foreach ($ddd as $key => $value) {
        $count++;
        $insert .= "$key = '$value'";
        $select .= "$key = '$value'";
        if ($count < $numddd) {
            $insert .= ", ";
            $select .= " AND ";
        }
    }

    if (! $rs = $db->Execute("INSERT INTO $table SET $insert")) {
        return false;
    }

    // Pull it out again to find the id.  This is the most cross-platform method.
    if ($rs = $db->Execute("SELECT id FROM $table WHERE $select")) {
        return $rs->fields[0];
    } else {
        return false;
    }
}


function update_record($table, $dataobject) {
// Update a record in a table
// $dataobject is an object containing needed data

    global $db;

    if (! isset($dataobject->id) ) {
        return false;
    }

    // Determine all the fields in the table
    if (!$columns = $db->MetaColumns($table)) {
        return false;
    }
    $data = (array)$dataobject;

    // Pull out data matching these fields
    foreach ($columns as $column) {
        if ($column->name <> "id" && isset($data[$column->name]) ) {
            $ddd[$column->name] = $data[$column->name];
        }
    }

    // Construct SQL queries
    $numddd = count($ddd);
    $count = 0;
    $update = "";

    foreach ($ddd as $key => $value) {
        $count++;
        $update .= "$key = '$value'";
        if ($count < $numddd) {
            $update .= ", ";
        }
    }

    if ($rs = $db->Execute("UPDATE $table SET $update WHERE id = '$dataobject->id'")) {
        return true;
    } else {
        return false;
    }
}


function print_object($object) {
// Mostly just for debugging

    $array = (array)$object;
    foreach ($array as $key => $item) {
        echo "$key -> $item <BR>";
    }
}



/// USER DATABASE ////////////////////////////////////////////////

function get_user_info_from_db($field, $value) {

    global $db;

    if (!$field || !$value)
        return false;

    $result = $db->Execute("SELECT * FROM user WHERE $field = '$value'");

    if ( $result->RecordCount() == 1 ) {
        $user = (object)$result->fields;

        $rs = $db->Execute("SELECT course FROM user_students WHERE user = '$user->id' ");
        while (!$rs->EOF) {
            $course = $rs->fields["course"];
            $user->student["$course"] = true;
            $rs->MoveNext();
        }

        $rs = $db->Execute("SELECT course FROM user_teachers WHERE user = '$user->id' ");
        while (!$rs->EOF) {
            $course = $rs->fields["course"];
            $user->teacher["$course"] = true;
            $rs->MoveNext();
        }

        $rs = $db->Execute("SELECT * FROM user_admins WHERE user = '$user->id' ");
        while (!$rs->EOF) {
            $user->admin = true;
            $rs->MoveNext();
        }

        if ($course = get_site()) {
            // Everyone is always a member of the top course
            $user->student["$course->id"] = true;
        }

        return $user;

    } else {
        return false;
    }
}

function update_user_in_db() {

   global $db, $USER, $REMOTE_ADDR;

   if (!isset($USER->id))
       return false;

   $timenow = time();
   if ($db->Execute("UPDATE LOW_PRIORITY user SET lastIP='$REMOTE_ADDR', lastaccess='$timenow'
                                               WHERE id = '$USER->id' ")) {
       return true;
   } else {
       return false;
   }
}

function require_login($courseid=0) {
// This function checks that the current user is logged in, and optionally
// whether they are "logged in" or allowed to be in a particular course.
// If not, then it redirects them to the site login or course enrolment.

    global $CFG, $SESSION, $USER, $FULLME, $HTTP_REFERER, $PHPSESSID;

    // First check that the user is logged in to the site.

    if (! (isset($USER->loggedin) and $USER->confirmed) ) { // They're not
        $SESSION->wantsurl = $FULLME;
        $SESSION->fromurl  = $HTTP_REFERER;
        save_session("SESSION");
        if ($PHPSESSID) { // Cookies not enabled.
            redirect("$CFG->wwwroot/login/?PHPSESSID=$PHPSESSID");
        } else {
            redirect("$CFG->wwwroot/login/");
        }
        die;
    }

    // Next, check if the user can be in a particular course
    if ($courseid) {
        if ($USER->student[$courseid] || $USER->teacher[$courseid] || $USER->admin) {
            if (!isset($USER->realuser)) {  // Don't update if this isn't a realuser
                update_user_in_db();
            }
            return;   // user is a member of this course.
        }
        if (! $course = get_record("course", "id", $courseid)) {
            error("That course doesn't exist");
        }
        if ($USER->username == "guest") {
            switch ($course->guest) {
                case 0: // Guests not allowed
                    print_header();
                    notice(get_string("guestsnotallowed", "", $course->fullname));
                    break;
                case 1: // Guests allowed
                    update_user_in_db();
                    return;
                case 2: // Guests allowed with key (drop through)
                    break;
            }
        }

        // Currently not enrolled in the course, so see if they want to enrol
        $SESSION->wantsurl = $FULLME;
        save_session("SESSION");
        redirect("$CFG->wwwroot/course/enrol.php?id=$courseid");
        die;
    }
}



function update_login_count() {
    global $SESSION;

    $max_logins = 10;

    if (empty($SESSION->logincount)) {
        $SESSION->logincount = 1;
    } else {
        $SESSION->logincount++;
    }
    save_session("SESSION");

    if ($SESSION->logincount > $max_logins) {
        unset($SESSION->wantsurl);
        save_session("SESSION");
        error("Sorry, you have exceeded the allowed number of login attempts. Restart your browser.");
    }
}


function isadmin($userid=0) {
    global $USER;

    if (!$userid) {
        return $USER->admin;
    }

    return record_exists_sql("SELECT * FROM user_admins WHERE user='$userid'");
}

function isteacher($courseid, $userid=0) {
    global $USER;

    if (isadmin($userid)) {  // admins can do anything the teacher can
        return true;
    }

    if (!$userid) {
        return $USER->teacher[$courseid];
    }

    return record_exists_sql("SELECT * FROM user_teachers WHERE user='$userid' AND course='$courseid'");
}


function isstudent($courseid, $userid=0) {
    global $USER;

    if (!$userid) {
        return $USER->student[$courseid];
    }

    $timenow = time();   // todo:  add time check below

    return record_exists_sql("SELECT * FROM user_students WHERE user='$userid' AND course='$courseid'");
}

function isguest($userid=0) {
    global $USER;

    if (!$userid) {
        return ($USER->username == "guest");
    }

    return record_exists_sql("SELECT * FROM user WHERE id='$userid' AND username = 'guest' ");
}

function isediting($courseid, $user=NULL) {
    global $USER;
    if (!$user){
        $user = $USER;
    }
    return ($user->editing and isteacher($courseid, $user->id));
}

function reset_login_count() {
    global $SESSION;

    $SESSION->logincount = 0;
    save_session("SESSION");
}


function set_moodle_cookie($thing) {

    $days = 60;
    $seconds = 60*60*24*$days;

    setCookie ('MOODLEID', "", time() - 3600, "/");
    setCookie ('MOODLEID', rc4encrypt($thing), time()+$seconds, "/");
}


function get_moodle_cookie() {
    global $MOODLEID;
    return rc4decrypt($MOODLEID);
}


function save_session($VAR) {
// Copies temporary session variable to permanent sesson variable
// eg $_SESSION["USER"] = $USER;
    global $$VAR;
    $_SESSION[$VAR] = $$VAR;
}


function verify_login($username, $password) {

    $user = get_user_info_from_db("username", $username);

    if (! $user) {
        return false;
    } else if ( $user->password == md5($password) ) {
        return $user;
    } else {
        return false;
    }
}

function get_site () {
// Returns $course object of the top-level site.
    if ( $course = get_record("course", "category", 0)) {
        return $course;
    } else {
        return false;
    }
}

function get_admin () {
// Returns $user object of the main admin user

    if ( $admins = get_records_sql("SELECT u.* FROM user u, user_admins a WHERE a.user = u.id ORDER BY u.id ASC")) {
        foreach ($admins as $admin) {
            return $admin;   // ie the first one (yeah I know it's bodgy)
        }
    } else {
        return false;
    }
}

function get_teacher($courseid) {
// Returns $user object of the main teacher for a course
    if ( $teachers = get_records_sql("SELECT u.* FROM user u, user_teachers t
                                      WHERE t.user = u.id AND t.course = '$courseid'
                                      ORDER BY t.authority ASC")) {
        foreach ($teachers as $teacher) {
            return $teacher;   // ie the first one (yeah I know it's bodgy)
        }
    } else {
        return false;
    }
}

function get_course_students($courseid, $sort="u.lastaccess DESC") {
    return get_records_sql("SELECT u.* FROM user u, user_students s
                            WHERE s.course = '$courseid' AND s.user = u.id
                            ORDER BY $sort");
}

function get_course_teachers($courseid, $sort="t.authority ASC") {
    return get_records_sql("SELECT u.* FROM user u, user_teachers t
                            WHERE t.course = '$courseid' AND t.user = u.id
                            ORDER BY $sort");
}

function get_course_users($courseid, $sort="u.lastaccess DESC") {
// Using this method because the direct SQL just would not always work!

    $teachers = get_course_teachers($courseid, $sort);
    $students = get_course_students($courseid, $sort);

    if ($teachers and $students) {
        return array_merge($teachers, $students);
    } else if ($teachers) {
        return $teachers;
    } else {
        return $students;
    }

//    return get_records_sql("SELECT u.* FROM user u, user_students s, user_teachers t
//                            WHERE (s.course = '$courseid' AND s.user = u.id) OR
//                                  (t.course = '$courseid' AND t.user = u.id)
//                            ORDER BY $sort");
}



/// MODULE FUNCTIONS /////////////////////////////////////////////////

function get_coursemodule_from_instance($modulename, $instance, $courseid) {
// Given an instance of a module, finds the coursemodule description

    return get_record_sql("SELECT cm.*, m.name
                           FROM course_modules cm, modules md, $modulename m
                           WHERE cm.course = '$courseid' AND
                                 cm.deleted = '0' AND
                                 cm.instance = m.id AND
                                 md.name = '$modulename' AND
                                 md.id = cm.module AND
                                 m.id = '$instance'");

}

function get_all_instances_in_course($modulename, $courseid, $sort="cw.section") {
// Returns an array of all the active instances of a particular
// module in a given course.   Returns false on any errors.

    return get_records_sql("SELECT m.*,cw.section,cm.id as coursemodule
                            FROM course_modules cm, course_sections cw, modules md, $modulename m
                            WHERE cm.course = '$courseid' AND
                                  cm.instance = m.id AND
                                  cm.deleted = '0' AND
                                  cm.section = cw.id AND
                                  md.name = '$modulename' AND
                                  md.id = cm.module
                            ORDER BY $sort");

}




/// CORRESPONDENCE  ////////////////////////////////////////////////

function email_to_user($user, $from, $subject, $messagetext, $messagehtml="", $attachment="", $attachname="") {
//  user        - a user record as an object
//  from        - a user record as an object
//  subject     - plain text subject line of the email
//  messagetext - plain text version of the message
//  messagehtml - complete html version of the message (optional)
//  attachment  - a file on the filesystem, relative to $CFG->dataroot
//  attachname  - the name of the file (extension indicates MIME)

    global $CFG, $_SERVER;

    include_once("$CFG->libdir/phpmailer/class.phpmailer.php");

    if (!$user) {
        return false;
    }

    $mail = new phpmailer;

    $mail->Version = "Moodle $CFG->moodleversion";     // mailer version
    $mail->PluginDir = "$CFG->libdir/phpmailer/";      // plugin directory (eg smtp plugin)
    if ($CFG->smtphosts) {
        $mail->IsSMTP();                               // use SMTP directly
        $mail->Host = "$CFG->smtphosts";               // specify main and backup servers
    } else {
        $mail->IsMail();                               // use PHP mail() = sendmail
    }

    $mail->From     = "$from->email";
    $mail->FromName = "$from->firstname $from->lastname";
    $mail->Subject  =  stripslashes($subject);

    $mail->AddAddress("$user->email", "$user->firstname $user->lastname");

    $mail->WordWrap = 70;                               // set word wrap

    if ($messagehtml) {
        $mail->IsHTML(true);
        $mail->Body    =  $messagehtml;
        $mail->AltBody =  "\n$messagetext\n";
    } else {
        $mail->IsHTML(false);
        $mail->Body =  "\n$messagetext\n";
    }

    if ($attachment && $attachname) {
        if (ereg( "\\.\\." ,$attachment )) {    // Security check for ".." in dir path
            $adminuser = get_admin();
            $mail->AddAddress("$adminuser->email", "$adminuser->firstname $adminuser->lastname");
            $mail->AddStringAttachment("Error in attachment.  User attempted to attach a filename with a unsafe name.", "error.txt", "8bit", "text/plain");
        } else {
            include_once("$CFG->dirroot/files/mimetypes.php");
            $mimetype = mimeinfo("type", $attachname);
            $mail->AddAttachment("$CFG->dataroot/$attachment", "$attachname", "base64", "$mimetype");
        }
    }

    if ($mail->Send()) {
        return true;
    } else {
        echo "ERROR: $mail->ErrorInfo\n";
        $site = get_site();
        add_to_log($site->id, "library", "mailer", $_SERVER["REQUEST_URI"], "ERROR: $mail->ErrorInfo");
        return false;
    }
}


/// FILE HANDLING  /////////////////////////////////////////////

function make_upload_directory($directory) {
// $directory = a string of directory names under $CFG->dataroot
// eg  stuff/assignment/1
// Returns full directory if successful, false if not

    global $CFG;

    $currdir = $CFG->dataroot;
    if (!file_exists($currdir)) {
        if (! mkdir($currdir, 0750)) {
            notify("ERROR: You need to create the directory $currdir with web server write access");
            return false;
        }
    }

    $dirarray = explode("/", $directory);

    foreach ($dirarray as $dir) {
        $currdir = "$currdir/$dir";
        if (! file_exists($currdir)) {
            if (! mkdir($currdir, 0750)) {
                notify("ERROR: Could not find or create a directory ($currdir)");
                return false;
            }
        }
    }

    return $currdir;
}


function get_directory_list( $rootdir ) {
// Returns an array with all the filenames in
// all subdirectories, relative to the given rootdir.

    $dirs = array();

    $dir = opendir( $rootdir );

    while( $file = readdir( $dir ) ) {
        $fullfile = $rootdir."/".$file;
        if ($file != "." and $file != "..") {
            if (filetype($fullfile) == "dir") {
                $subdirs = get_directory_list($fullfile);
                foreach ($subdirs as $subdir) {
                    $dirs[] = $file."/".$subdir;
                }
            } else {
                $dirs[] = $file;
            }
        }
    }

    return $dirs;
}

function get_real_size($size=0) {
// Converts numbers like 10M into bytes
    if (!$size) {
        return 0;
    }
    $scan['MB'] = 1048576;
    $scan['M'] = 1048576;
    $scan['KB'] = 1024;
    $scan['K'] = 1024;

    while (list($key) = each($scan)) {
        if ((strlen($size)>strlen($key))&&(substr($size, strlen($size) - strlen($key))==$key)) {
            $size = substr($size, 0, strlen($size) - strlen($key)) * $scan[$key];
            break;
        }
    }
    return $size;
}

function clean_filename($string) {
    $string = eregi_replace("\.\.", "", $string);
    $string = eregi_replace("[^([:alnum:]|\.)]", "_", $string);
    return    eregi_replace("_+", "_", $string);
}


/// STRING TRANSLATION  ////////////////////////////////////////

function print_string($identifier, $module="", $a=NULL) {
    echo get_string($identifier, $module, $a);
}


function get_string($identifier, $module="", $a=NULL) {
// Return the translated string specified by $identifier as
// for $module.  Uses the same format files as STphp.
// $a is an object, string or number that can be used
// within translation strings
//
// eg "hello \$a->firstname \$a->lastname"
// or "hello \$a"

    global $CFG, $USER;

    if (isset($USER->lang)) {    // User language can override site language
        $lang = $USER->lang;
    } else {
        $lang = $CFG->lang;
    }

    if ($module == "") {
        $module = "moodle";
    }

    $langpath = "$CFG->dirroot/lang";
    $langfile = "$langpath/$lang/$module.php";

    if (!file_exists($langfile)) {                // try English instead
        $langfile = "$langpath/en/$module.php";
        if (!file_exists($langfile)) {
            return "ERROR: No lang file ($langpath/en/$module.php)!";
        }
    }

    if ($result = get_string_from_file($identifier, $langfile, "\$resultstring")) {

        eval($result);
        return $resultstring;

    } else {
        if ($lang == "en") {
            return "ERROR: '$identifier' is missing!";

        } else {   // Try looking in the english file.
            $langfile = "$langpath/en/$module.php";
            if (!file_exists($langfile)) {
                return "ERROR: No lang file ($langpath/en/$module.php)!";
            }
            if ($result = get_string_from_file($identifier, $langfile, "\$resultstring")) {
                eval($result);
                return $resultstring;
            } else {
                return "ERROR: '$identifier' is missing!";
            }
        }
    }
}


function get_string_from_file($identifier, $langfile, $destination) {
// This function is only used from get_string().
    include ($langfile);

    if (!isset ($string[$identifier])) {
        return false;
    }

    return "$destination = sprintf(\"".$string[$identifier]."\");";
}


/// ENCRYPTION  ////////////////////////////////////////////////

function rc4encrypt($data) {
    $password = "nfgjeingjk";
    return endecrypt($password, $data, "");
}

function rc4decrypt($data) {
    $password = "nfgjeingjk";
    return endecrypt($password, $data, "de");
}

function endecrypt ($pwd, $data, $case) {
// Based on a class by Mukul Sabharwal [mukulsabharwal@yahoo.com]

    if ($case == 'de') {
        $data = urldecode($data);
    }

    $key[] = "";
    $box[] = "";
    $temp_swap = "";
    $pwd_length = 0;

    $pwd_length = strlen($pwd);

    for ($i = 0; $i <= 255; $i++) {
        $key[$i] = ord(substr($pwd, ($i % $pwd_length), 1));
        $box[$i] = $i;
    }

    $x = 0;

    for ($i = 0; $i <= 255; $i++) {
        $x = ($x + $box[$i] + $key[$i]) % 256;
        $temp_swap = $box[$i];
        $box[$i] = $box[$x];
        $box[$x] = $temp_swap;
    }

    $temp = "";
    $k = "";

    $cipherby = "";
    $cipher = "";

    $a = 0;
    $j = 0;

    for ($i = 0; $i < strlen($data); $i++) {
        $a = ($a + 1) % 256;
        $j = ($j + $box[$a]) % 256;
        $temp = $box[$a];
        $box[$a] = $box[$j];
        $box[$j] = $temp;
        $k = $box[(($box[$a] + $box[$j]) % 256)];
        $cipherby = ord(substr($data, $i, 1)) ^ $k;
        $cipher .= chr($cipherby);
    }

    if ($case == 'de') {
        $cipher = urldecode(urlencode($cipher));
    } else {
        $cipher = urlencode($cipher);
    }

    return $cipher;
}


/// MISCELLANEOUS ////////////////////////////////////////////////////////////////////

function count_words($string) {
    $string = strip_tags($string);
    return count(preg_split("/\w\b/", $string)) - 1;
}

function getweek ($startdate, $thedate) {
// Given dates in seconds, how many weeks is the date from startdate
// The first week is 1, the second 2 etc ...

    if ($thedate < $startdate) {   // error
        return 0;
    }

    return floor(($thedate - $startdate) / 604800.0) + 1;
}

function add_to_log($course, $module, $action, $url="", $info="") {
// Add an entry to the log table.  These are "action" focussed rather
// than web server hits, and provide a way to easily reconstruct what
// any particular student has been doing.
//
// course = the course id
// module = forum, journal, reading, course, user etc
// action = view, edit, post (often but not always the same as the file.php)
// url    = the file and parameters used to see the results of the action
// info   = additional description information


    global $db, $USER, $REMOTE_ADDR;

    if (isset($USER->realuser)) {  // Don't log
        return;
    }

    $timenow = time();
    $info = addslashes($info);

    $result = $db->Execute("INSERT INTO log
                            SET time = '$timenow',
                                user = '$USER->id',
                                course = '$course',
                                ip = '$REMOTE_ADDR',
                                module = '$module',
                                action = '$action',
                                url = '$url',
                                info = '$info'");
    if (!$result) {
        echo "<P>Error: Could not insert a new entry to the Moodle log</P>";  // Don't throw an error
    }
}

function generate_password($maxlen=10) {
// returns a randomly generated password of length $maxlen.  inspired by
// http://www.phpbuilder.com/columns/jesus19990502.php3

    global $CFG;

    $fillers = "1234567890!$-+";
    $wordlist = file($CFG->wordlist);

    srand((double) microtime() * 1000000);
    $word1 = trim($wordlist[rand(0, count($wordlist) - 1)]);
    $word2 = trim($wordlist[rand(0, count($wordlist) - 1)]);
    $filler1 = $fillers[rand(0, strlen($fillers) - 1)];

    return substr($word1 . $filler1 . $word2, 0, $maxlen);
}

function moodle_needs_upgrading() {
// Checks version numbers of Main code and all modules to see
// if there are any mismatches ... returns true or false
    global $CFG;

    include_once("$CFG->dirroot/version.php");  # defines $version and upgrades
    if ($dversion = get_field("config", "value", "name", "version")) {
        if ($version > $dversion) {
            return true;
        }
        if ($mods = get_list_of_plugins("mod")) {
            foreach ($mods as $mod) {
                $fullmod = "$CFG->dirroot/mod/$mod";
                unset($module);
                include_once("$fullmod/version.php");  # defines $module with version etc
                if ($currmodule = get_record("modules", "name", $mod)) {
                    if ($module->version > $currmodule->version) {
                        return true;
                    }
                }
            }
        }
    } else {
        return true;
    }
    return false;
}


function get_list_of_plugins($plugin="mod") {
// Lists plugin directories within some directory

    global $CFG;

    $basedir = opendir("$CFG->dirroot/$plugin");
    while ($dir = readdir($basedir)) {
        if ($dir == "." || $dir == ".." || $dir == "CVS") {
            continue;
        }
        if (filetype("$CFG->dirroot/$plugin/$dir") != "dir") {
            continue;
        }
        $plugins[] = $dir;
    }
    if ($plugins) {
        asort($plugins);
    }
    return $plugins;
}


function iswindows() {
// True if this is Windows, False if not.

   global $WINDIR;

   return isset($WINDIR);
}

function check_php_version($version="4.1.0") {
// Returns true is the current version of PHP is greater that the specified one
    $minversion = intval(str_replace(".", "", $version));
    $curversion = intval(str_replace(".", "", phpversion()));
    return ($curversion >= $minversion);
}



?>