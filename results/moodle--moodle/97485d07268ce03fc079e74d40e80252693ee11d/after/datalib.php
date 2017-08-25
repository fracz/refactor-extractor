<?PHP // $Id$

/// FUNCTIONS FOR DATABASE HANDLING  ////////////////////////////////

function execute_sql($command, $feedback=true) {
/// Completely general function - it just runs some SQL and reports success.

    global $db;

    $result = $db->Execute("$command");

    if ($result) {
        if ($feedback) {
            echo "<P><FONT COLOR=green><B>".get_string("success")."</B></FONT></P>";
        }
        return true;
    } else {
        if ($feedback) {
            echo "<P><FONT COLOR=red><B>".get_string("error")."</B></FONT></P>";
        }
        return false;
    }
}

function modify_database($sqlfile) {
/// Assumes that the input text file consists of a number
/// of SQL statements ENDING WITH SEMICOLONS.  The semicolons
/// MUST be the last character in a line.
/// Lines that are blank or that start with "#" are ignored.
/// Only tested with mysql dump files (mysqldump -p -d moodle)

    global $CFG;

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
                    $command = str_replace("prefix_", $CFG->prefix, $command); // Table prefixes
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


function record_exists($table, $field="", $value="", $field2="", $value2="", $field3="", $value3="") {
/// Returns true or false depending on whether the specified record exists

    global $CFG;

    if ($field) {
        $select = "WHERE $field = '$value'";
        if ($field2) {
            $select .= " AND $field2 = '$value2'";
            if ($field3) {
                $select .= " AND $field3 = '$value3'";
            }
        }
    }

    return record_exists_sql("SELECT * FROM $CFG->prefix$table $select LIMIT 1");
}


function record_exists_sql($sql) {
/// Returns true or false depending on whether the specified record exists
/// The sql statement is provided as a string.

    global $db;

    $rs = $db->Execute($sql);
    if (!$rs) return false;

    if ( $rs->RecordCount() ) {
        return true;
    } else {
        return false;
    }
}


function count_records($table, $field="", $value="", $field2="", $value2="", $field3="", $value3="") {
/// Get all the records and count them

    global $CFG;

    if ($field) {
        $select = "WHERE $field = '$value'";
        if ($field2) {
            $select .= " AND $field2 = '$value2'";
            if ($field3) {
                $select .= " AND $field3 = '$value3'";
            }
        }
    }

    return count_records_sql("SELECT COUNT(*) FROM $CFG->prefix$table $select");
}

function count_records_select($table, $select="") {
/// Get all the records and count them

    global $CFG;

    if ($select) {
        $select = "WHERE $select";
    }

    return count_records_sql("SELECT COUNT(*) FROM $CFG->prefix$table $select");
}


function count_records_sql($sql) {
/// Get all the records and count them
/// The sql statement is provided as a string.

    global $db;

    $rs = $db->Execute("$sql");
    if (!$rs) return 0;

    return $rs->fields[0];
}

function get_record($table, $field, $value, $field2="", $value2="", $field3="", $value3="") {
/// Get a single record as an object

    global $CFG;

    $select = "WHERE $field = '$value'";

    if ($field2) {
        $select .= " AND $field2 = '$value2'";
        if ($field3) {
            $select .= " AND $field3 = '$value3'";
        }
    }

    return get_record_sql("SELECT * FROM $CFG->prefix$table $select");
}

function get_record_sql($sql) {
/// Get a single record as an object
/// The sql statement is provided as a string.

    global $db;

    $rs = $db->Execute("$sql");
    if (!$rs) return false;

    if ( $rs->RecordCount() == 1 ) {
        return (object)$rs->fields;
    } else {
        return false;
    }
}

function get_records($table, $field="", $value="", $sort="", $fields="*") {
/// Get a number of records as an array of objects
/// Can optionally be sorted eg "time ASC" or "time DESC"
/// If "fields" is specified, only those fields are returned
/// The "key" is the first column returned, eg usually "id"

    global $CFG;

    if ($field) {
        $select = "WHERE $field = '$value'";
    }
    if ($sort) {
        $sortorder = "ORDER BY $sort";
    }

    return get_records_sql("SELECT $fields FROM $CFG->prefix$table $select $sortorder");
}

function get_records_select($table, $select="", $sort="", $fields="*") {
/// Get a number of records as an array of objects
/// Can optionally be sorted eg "time ASC" or "time DESC"
/// "select" is a fragment of SQL to define the selection criteria
/// The "key" is the first column returned, eg usually "id"

    global $CFG;

    if ($sort) {
        $sortorder = "ORDER BY $sort";
    }

    if ($select) {
        $select = "WHERE $select";
    }

    return get_records_sql("SELECT $fields FROM $CFG->prefix$table $select $sortorder");
}


function get_records_list($table, $field="", $values="", $sort="", $fields="*") {
/// Get a number of records as an array of objects
/// Differs from get_records() in that the values variable
/// can be a comma-separated list of values eg  "4,5,6,10"
/// Can optionally be sorted eg "time ASC" or "time DESC"
/// The "key" is the first column returned, eg usually "id"

    global $CFG;

    if ($field) {
        $select = "WHERE $field in ($values)";
    }
    if ($sort) {
        $sortorder = "ORDER BY $sort";
    }

    return get_records_sql("SELECT $fields FROM $CFG->prefix$table $select $sortorder");
}



function get_records_sql($sql) {
/// Get a number of records as an array of objects
/// The "key" is the first column returned, eg usually "id"
/// The sql statement is provided as a string.

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

function get_records_menu($table, $field="", $value="", $sort="", $fields="*") {
/// Get a number of records as an array of objects
/// Can optionally be sorted eg "time ASC" or "time DESC"
/// If "fields" is specified, only those fields are returned
/// The "key" is the first column returned, eg usually "id"

    global $CFG;

    if ($field) {
        $select = "WHERE $field = '$value'";
    }
    if ($sort) {
        $sortorder = "ORDER BY $sort";
    }

    return get_records_sql_menu("SELECT $fields FROM $CFG->prefix$table $select $sortorder");
}

function get_records_select_menu($table, $select="", $sort="", $fields="*") {
/// Get a number of records as an array of objects
/// Can optionally be sorted eg "time ASC" or "time DESC"
/// "select" is a fragment of SQL to define the selection criteria
/// Returns associative array of first two fields

    global $CFG;

    if ($sort) {
        $sortorder = "ORDER BY $sort";
    }

    if ($select) {
        $select = "WHERE $select";
    }

    return get_records_sql_menu("SELECT $fields FROM $CFG->prefix$table $select $sortorder");
}


function get_records_sql_menu($sql) {
/// Given an SQL select, this function returns an associative
/// array of the first two columns.  This is most useful in
/// combination with the choose_from_menu function to create
/// a form menu.

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

function get_field($table, $return, $field, $value) {
/// Get a single field from a database record

    global $db, $CFG;

    $rs = $db->Execute("SELECT $return FROM $CFG->prefix$table WHERE $field = '$value'");
    if (!$rs) return false;

    if ( $rs->RecordCount() == 1 ) {
        return $rs->fields["$return"];
    } else {
        return false;
    }
}

function set_field($table, $newfield, $newvalue, $field, $value) {
/// Set a single field in a database record

    global $db, $CFG;

    return $db->Execute("UPDATE $CFG->prefix$table SET $newfield = '$newvalue' WHERE $field = '$value'");
}


function delete_records($table, $field="", $value="", $field2="", $value2="", $field3="", $value3="") {
/// Delete one or more records from a table

    global $db, $CFG;

    if ($field) {
        $select = "WHERE $field = '$value'";
        if ($field2) {
            $select .= " AND $field2 = '$value2'";
            if ($field3) {
                $select .= " AND $field3 = '$value3'";
            }
        }
    }

    return $db->Execute("DELETE FROM $CFG->prefix$table $select");
}


function insert_record($table, $dataobject, $returnid=true) {
/// Insert a record into a table and return the "id" field if required
/// If the return ID isn't required, then this just reports success as true/false.
/// $dataobject is an object containing needed data

    global $db, $CFG;

    // Determine all the fields needed
    if (! $columns = $db->MetaColumns("$CFG->prefix$table")) {
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
        return false;
    }

    $count = 0;
    $inscolumns = "";
    $insvalues = "";
    $select = "";

    foreach ($ddd as $key => $value) {
        $count++;
        $inscolumns .= "$key";
        $insvalues .= "'$value'";
        $select .= "$key = '$value'";
        if ($count < $numddd) {
            $inscolumns .= ", ";
            $insvalues .= ", ";
            $select .= " AND ";
        }
    }

    if (! $rs = $db->Execute("INSERT INTO $CFG->prefix$table ($inscolumns) VALUES ($insvalues)")) {
        return false;
    }

    if ($returnid) {
        if ($CFG->dbtype == "mysql") {
            return $db->Insert_ID();   // ADOdb has stored the ID for us, but it isn't reliable
        }

        // Try to pull the record out again to find the id.  This is the most cross-platform method.
        if ($rs = $db->Execute("SELECT id FROM $CFG->prefix$table WHERE $select")) {
            if ($rs->RecordCount() == 1) {
                return $rs->fields[0];
            }
        }

        return false;

    } else {
        return true;
    }
}


function update_record($table, $dataobject) {
/// Update a record in a table
/// $dataobject is an object containing needed data
/// Relies on $dataobject having a variable "id" to
/// specify the record to update

    global $db, $CFG;

    if (! isset($dataobject->id) ) {
        return false;
    }

    // Determine all the fields in the table
    if (!$columns = $db->MetaColumns("$CFG->prefix$table")) {
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

    if ($rs = $db->Execute("UPDATE $CFG->prefix$table SET $update WHERE id = '$dataobject->id'")) {
        return true;
    } else {
        return false;
    }
}


function print_object($object) {
/// Mostly just for debugging

    $array = (array)$object;
    foreach ($array as $key => $item) {
        echo "$key -> $item <BR>";
    }
}




/// USER DATABASE ////////////////////////////////////////////////

function get_user_info_from_db($field, $value) {
/// Get a complete user record, which includes all the info
/// in the user record, as well as membership information
/// Suitable for setting as $USER session cookie.

    global $db, $CFG;

    if (!$field || !$value)
        return false;

    if (! $result = $db->Execute("SELECT * FROM {$CFG->prefix}user WHERE $field = '$value' AND deleted <> '1'")) {
        error("Could not find any active users!");
    }

    if ( $result->RecordCount() == 1 ) {
        $user = (object)$result->fields;

        $rs = $db->Execute("SELECT course FROM {$CFG->prefix}user_students WHERE userid = '$user->id' ");
        while (!$rs->EOF) {
            $course = $rs->fields["course"];
            $user->student["$course"] = true;
            $rs->MoveNext();
        }

        $rs = $db->Execute("SELECT course FROM {$CFG->prefix}user_teachers WHERE userid = '$user->id' ");
        while (!$rs->EOF) {
            $course = $rs->fields["course"];
            $user->teacher["$course"] = true;
            $rs->MoveNext();
        }

        $rs = $db->Execute("SELECT * FROM {$CFG->prefix}user_admins WHERE userid = '$user->id' ");
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
/// Updates user record to record their last access

   global $db, $USER, $REMOTE_ADDR, $CFG;

   if (!isset($USER->id))
       return false;

   $timenow = time();
   if ($db->Execute("UPDATE {$CFG->prefix}user SET lastIP='$REMOTE_ADDR', lastaccess='$timenow'
                     WHERE id = '$USER->id' ")) {
       return true;
   } else {
       return false;
   }
}


function adminlogin($username, $md5password) {
/// Does this username and password specify a valid admin user?

    global $CFG;

    return record_exists_sql("SELECT u.id
                                FROM {$CFG->prefix}user u,
                                     {$CFG->prefix}user_admins a
                              WHERE u.id = a.userid
                                AND u.username = '$username'
                                AND u.password = '$md5password'");
}


function get_site () {
/// Returns $course object of the top-level site.

    if ( $course = get_record("course", "category", 0)) {
        return $course;
    } else {
        return false;
    }
}


function get_courses($category=0, $sort="fullname ASC") {
/// Returns list of courses

    if ($category > 0) {          // Return all courses in one category
        return get_records("course", "category", $category, $sort);

    } else if ($category < 0) {   // Return all courses, even the site
        return get_records("course", "", "", $sort);

    } else {                      // Return all courses, except site
        return get_records_select("course", "category > 0", $sort);
    }
}

function get_categories() {
    return get_records("course_categories", "", "", "name");
}


function get_guest() {
    return get_user_info_from_db("username", "guest");
}


function get_admin () {
/// Returns $user object of the main admin user

    global $CFG;

    if ( $admins = get_admins() ) {
        foreach ($admins as $admin) {
            return $admin;   // ie the first one
        }
    } else {
        return false;
    }
}

function get_admins() {
/// Returns list of all admins

    global $CFG;

    return get_records_sql("SELECT u.*
                              FROM {$CFG->prefix}user u,
                                   {$CFG->prefix}user_admins a
                             WHERE a.userid = u.id
                             ORDER BY u.id ASC");
}


function get_teacher($courseid) {
/// Returns $user object of the main teacher for a course

    global $CFG;

    if ( $teachers = get_course_teachers($courseid, "t.authority ASC")) {
        foreach ($teachers as $teacher) {
            if ($teacher->authority) {
                return $teacher;   // the highest authority teacher
            }
        }
    } else {
        return false;
    }
}

function get_course_students($courseid, $sort="u.lastaccess DESC") {
/// Returns list of all students in this course

    global $CFG;

    return get_records_sql("SELECT u.* FROM {$CFG->prefix}user u, {$CFG->prefix}user_students s
                            WHERE s.course = '$courseid' AND s.userid = u.id AND u.deleted = '0'
                            ORDER BY $sort");
}

function get_course_teachers($courseid, $sort="t.authority ASC") {
/// Returns list of all teachers in this course

    global $CFG;

    return get_records_sql("SELECT u.*,t.authority,t.role FROM {$CFG->prefix}user u, {$CFG->prefix}user_teachers t
                            WHERE t.course = '$courseid' AND t.userid = u.id AND u.deleted = '0'
                            ORDER BY $sort");
}

function get_course_users($courseid, $sort="u.lastaccess DESC") {
/// Using this method because the direct SQL just would not always work!

    $teachers = get_course_teachers($courseid, $sort);
    $students = get_course_students($courseid, $sort);

    if ($teachers and $students) {
        return array_merge($teachers, $students);
    } else if ($teachers) {
        return $teachers;
    } else {
        return $students;
    }

///    return get_records_sql("SELECT u.* FROM user u, user_students s, user_teachers t
///                            WHERE (s.course = '$courseid' AND s.userid = u.id) OR
///                                  (t.course = '$courseid' AND t.userid = u.id)
///                            ORDER BY $sort");
}


function get_users_search($search, $sort="u.firstname ASC") {
    global $CFG;

    return get_records_sql("SELECT * from {$CFG->prefix}user
                             WHERE confirmed = 1
                               AND deleted = 0
                               AND (firstname LIKE '%$search%' OR
                                    lastname LIKE '%$search%' OR
                                    email LIKE '%$search%')
                               AND username <> 'guest'
                               AND username <> 'changeme'");
}


function get_users_count() {
    return count_records_select("user", "username <> 'guest' AND deleted <> 1");
}

function get_users_listing($sort, $dir="ASC", $page=1, $recordsperpage=20) {
    global $CFG;

    if ($CFG->dbtype == "mysql") {
        $limit = "LIMIT $page,$recordsperpage";
    } else {
        $limit = "LIMIT $recordsperpage,$page";
    }
    return get_records_sql("SELECT id, username, email, firstname, lastname, city, country, lastaccess
                              FROM {$CFG->prefix}user
                             WHERE username <> 'guest'
                               AND deleted <> '1'
                          ORDER BY $sort $dir $limit");

}

function get_users_confirmed() {
    global $CFG;
    return get_records_sql("SELECT *
                              FROM {$CFG->prefix}user
                             WHERE confirmed = 1
                               AND deleted = 0
                               AND username <> 'guest'
                               AND username <> 'changeme'");
}


function get_users_unconfirmed($cutofftime=2000000000) {
    global $CFG;
    return get_records_sql("SELECT *
                             FROM {$CFG->prefix}user
                            WHERE confirmed = 0
                              AND firstaccess > 0
                              AND firstaccess < '$cutofftime'");
}


function get_users_longtimenosee($cutofftime) {
    global $CFG;

    $db->debug = true;
    return get_records_sql("SELECT u.*
                              FROM {$CFG->prefix}user u,
                                   {$CFG->prefix}user_students s
                             WHERE u.lastaccess > '0'
                               AND u.lastaccess < '$cutofftime'
                               AND u.id = s.userid
                          GROUP BY u.id");
}



/// MODULE FUNCTIONS /////////////////////////////////////////////////

function get_course_mods($courseid) {
/// Just gets a raw list of all modules in a course
    global $CFG;

    return get_records_sql("SELECT cm.*, m.name as modname
                            FROM {$CFG->prefix}modules m, {$CFG->prefix}course_modules cm
                            WHERE cm.course = '$courseid'
                            AND cm.deleted = '0'
                            AND cm.module = m.id ");
}

function get_coursemodule_from_instance($modulename, $instance, $courseid) {
/// Given an instance of a module, finds the coursemodule description

    global $CFG;

    return get_record_sql("SELECT cm.*, m.name
                           FROM {$CFG->prefix}course_modules cm, {$CFG->prefix}modules md, {$CFG->prefix}$modulename m
                           WHERE cm.course = '$courseid' AND
                                 cm.deleted = '0' AND
                                 cm.instance = m.id AND
                                 md.name = '$modulename' AND
                                 md.id = cm.module AND
                                 m.id = '$instance'");

}

function get_all_instances_in_course($modulename, $courseid, $sort="cw.section") {
/// Returns an array of all the active instances of a particular
/// module in a given course.   Returns false on any errors.

    global $CFG;

    return get_records_sql("SELECT m.*,cw.section,cm.id as coursemodule
                            FROM {$CFG->prefix}course_modules cm, {$CFG->prefix}course_sections cw,
                                 {$CFG->prefix}modules md, {$CFG->prefix}$modulename m
                            WHERE cm.course = '$courseid' AND
                                  cm.instance = m.id AND
                                  cm.deleted = '0' AND
                                  cm.section = cw.id AND
                                  md.name = '$modulename' AND
                                  md.id = cm.module
                            ORDER BY $sort");

}


/// LOG FUNCTIONS /////////////////////////////////////////////////////


function add_to_log($course, $module, $action, $url="", $info="") {
/// Add an entry to the log table.  These are "action" focussed rather
/// than web server hits, and provide a way to easily reconstruct what
/// any particular student has been doing.
///
/// course = the course id
/// module = forum, journal, resource, course, user etc
/// action = view, edit, post (often but not always the same as the file.php)
/// url    = the file and parameters used to see the results of the action
/// info   = additional description information

    global $db, $CFG, $USER, $REMOTE_ADDR;

    if (isset($USER->realuser)) {  // Don't log
        return;
    }

    $timenow = time();
    $info = addslashes($info);

    $result = $db->Execute("INSERT INTO {$CFG->prefix}log (time,
                                        userid,
                                        course,
                                        ip,
                                        module,
                                        action,
                                        url,
                                        info)
                             VALUES ('$timenow',
                                        '$USER->id',
                                        '$course',
                                        '$REMOTE_ADDR',
                                        '$module',
                                        '$action',
                                        '$url',
                                        '$info')");

    if (!$result) {
        echo "<P>Error: Could not insert a new entry to the Moodle log</P>";  // Don't throw an error
    }
}


function get_logs($select, $order) {
    global $CFG;

    return get_records_sql("SELECT l.*, u.firstname, u.lastname, u.picture
                              FROM {$CFG->prefix}log l,
                                   {$CFG->prefix}user u
                                   $select $order");
}

function get_logs_usercourse($userid, $courseid, $coursestart) {
    global $CFG;

    return get_records_sql("SELECT floor((`time` - $coursestart)/86400) as day, count(*) as num
                            FROM {$CFG->prefix}log
                           WHERE userid = '$userid'
                             AND course = '$courseid'
                             AND `time` > '$coursestart'
                        GROUP BY day ");
}

function get_logs_userday($userid, $courseid, $daystart) {
    global $CFG;

    return get_records_sql("SELECT floor((`time` - $daystart)/3600) as hour, count(*) as num
                            FROM {$CFG->prefix}log
                           WHERE userid = '$userid'
                             AND course = '$courseid'
                             AND `time` > '$daystart'
                        GROUP BY hour ");
}


?>