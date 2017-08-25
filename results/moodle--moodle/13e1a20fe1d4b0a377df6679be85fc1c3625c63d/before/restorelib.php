<?PHP //$Id$
    //Functions used in restore

    //This function unzips a zip file in the same directory that it is
    //It automatically uses pclzip or command line unzip
    function restore_unzip ($file,$moodle_home) {

        global $CFG;

        $status = true;

        if (empty($CFG->unzip)) {    // Use built-in php-based unzip function
            include_once($moodle_home."/lib/pclzip/pclzip.lib.php");
            $archive = new PclZip($file);
            if (!$list = $archive->extract(dirname($file))) {
                $status = false;
            }
        } else {                     // Use external unzip program
            $command = "cd ".dirname($file)."; $CFG->unzip -o ".basename($file);
            Exec($command);
        }

        return $status;
    }

    //This function checks if moodle.xml seems to be a valid xml file
    //(exists, has an xml header and a course main tag
    function restore_check_moodle_file ($file) {

        $status = true;

        //Check if it exists
        if ($status = is_file($file)) {
            //Open it and read the first 200 bytes (chars)
            $handle = fopen ($file, "r");
            $first_chars = fread($handle,200);
            $status = fclose ($handle);
            //Chek if it has the requires strings
            if ($status) {
                $status = strpos($first_chars,"<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
                if ($status !== false) {
                    $status = strpos($first_chars,"<MOODLE_BACKUP>");
                }
            }
        }

        return $status;
    }

    //This function read the xml file and store it data from the info zone in an object
    function restore_read_xml_info ($xml_file) {

        //We call the main read_xml function, with todo = INFO
        $info = restore_read_xml ($xml_file,"INFO",false);

        return $info;
    }

    //This function read the xml file and store it data from the course header zone in an object
    function restore_read_xml_course_header ($xml_file) {

        //We call the main read_xml function, with todo = COURSE_HEADER
        $info = restore_read_xml ($xml_file,"COURSE_HEADER",false);

        return $info;
    }

    //This function read the xml file and store its data from the sections in a object
    function restore_read_xml_sections ($xml_file) {

        //We call the main read_xml function, with todo = SECTIONS
        $info = restore_read_xml ($xml_file,"SECTIONS",false);

        return $info;
    }

    //This function read the xml file and store its data from the users in
    //backup_ids->info db (and user's id in $info)
    function restore_read_xml_users ($restore,$xml_file) {

        //We call the main read_xml function, with todo = USERS
        $info = restore_read_xml ($xml_file,"USERS",$restore);

        return $info;
    }

    //This function read the xml file and store its data from the modules in
    //backup_ids->info
    function restore_read_xml_modules ($restore,$xml_file) {

        //We call the main read_xml function, with todo = MODULES
        $info = restore_read_xml ($xml_file,"MODULES",$restore);

        return $info;
    }

    //This function prints the contents from the info parammeter passed
    function restore_print_info ($info) {

        $status = true;
        if ($info) {
            //This is tha align to every ingo table
            $table->align = array ("RIGHT","LEFT");
            //This is the nowrap clause
            $table->wrap = array ("","NOWRAP");
            //The width
            $table->width = "70%";
            //Put interesting info in table
            //The backup original name
            $tab[0][0] = "<b>".get_string("backuporiginalname").":</b>";
            $tab[0][1] = $info->backup_name;
            //The moodle version
            $tab[1][0] = "<b>".get_string("moodleversion").":</b>";
            $tab[1][1] = $info->backup_moodle_release." (".$info->backup_moodle_version.")";
            //The backup version
            $tab[2][0] = "<b>".get_string("backupversion").":</b>";
            $tab[2][1] = $info->backup_backup_release." (".$info->backup_backup_version.")";
            //The backup date
            $tab[3][0] = "<b>".get_string("backupdate").":</b>";
            $tab[3][1] = userdate($info->backup_date);
            //Print title
            print_heading(get_string("backup").":");
            $table->data = $tab;
            //Print backup general info
            print_table($table);
            //Now backup contents in another table
            $tab = array();
            //First mods info
            $mods = $info->mods;
            $elem = 0;
            foreach ($mods as $key => $mod) {
                $tab[$elem][0] = "<b>".get_string("modulenameplural",$key).":</b>";
                if ($mod->backup == "false") {
                    $tab[$elem][1] = get_string("notincluded");
                } else {
                    if ($mod->userinfo == "true") {
                        $tab[$elem][1] = get_string("included")." ".get_string("withuserdata");
                    } else {
                        $tab[$elem][1] = get_string("included")." ".get_string("withoutuserdata");
                    }
                }
                $elem++;
            }
            //Users info
            $tab[$elem][0] = "<b>".get_string("users").":</b>";
            $tab[$elem][1] = get_string($info->backup_users);
            $elem++;
            //Logs info
            $tab[$elem][0] = "<b>".get_string("logs").":</b>";
            if ($info->backup_logs == "true") {
                $tab[$elem][1] = get_string("yes");
            } else {
                $tab[$elem][1] = get_string("no");
            }
            $elem++;
            //User Files info
            $tab[$elem][0] = "<b>".get_string("userfiles").":</b>";
            if ($info->backup_user_files == "true") {
                $tab[$elem][1] = get_string("yes");
            } else {
                $tab[$elem][1] = get_string("no");
            }
            $elem++;
            //Course Files info
            $tab[$elem][0] = "<b>".get_string("coursefiles").":</b>";
            if ($info->backup_course_files == "true") {
                $tab[$elem][1] = get_string("yes");
            } else {
                $tab[$elem][1] = get_string("no");
            }
            $elem++;
            $table->data = $tab;
            //Print title
            print_heading(get_string("backupdetails").":");
            //Print backup general info
            print_table($table);
        } else {
            $status = false;
        }

        return $status;
    }

    //This function prints the contents from the course_header parammeter passed
    function restore_print_course_header ($course_header) {

        $status = true;
        if ($course_header) {
            //This is tha align to every ingo table
            $table->align = array ("RIGHT","LEFT");
            //The width
            $table->width = "70%";
            //Put interesting course header in table
            //The course name
            $tab[0][0] = "<b>".get_string("name").":</b>";
            $tab[0][1] = $course_header->course_fullname." (".$course_header->course_shortname.")";
            //The course summary
            $tab[1][0] = "<b>".get_string("summary").":</b>";
            $tab[1][1] = $course_header->course_summary;
            $table->data = $tab;
            //Print title
            print_heading(get_string("course").":");
            //Print backup course header info
            print_table($table);
        } else {
            $status = false;
        }
        return $status;
    }

    //This function create a new course record.
    //When finished, course_header contains the id of the new course
    function restore_create_new_course($restore,&$course_header) {

        global $CFG;

        $status = true;

        $fullname = $course_header->course_fullname;
        $shortname = $course_header->course_shortname;
        $currentfullname = "";
        $currentshortname = "";
        $counter = 0;
        //Iteratere while the name exists
        do {
            if ($counter) {
                $suffixfull = " ".get_string("copy")." ".$counter;
                $suffixshort = "-".$counter;
            } else {
                $suffixfull = "";
                $suffixshort = "";
            }
            $currentfullname = $fullname.$suffixfull;
            $currentshortname = $shortname.$suffixshort;
            $course = get_record("course","fullname",addslashes($currentfullname));
            $counter++;
        } while ($course);

        //New name = currentname
        $course_header->course_fullname = $currentfullname;
        $course_header->course_shortname = $currentshortname;

        //Now calculate the category
        $category = get_record("course_categories","id",$course_header->category->id,
                                                   "name",addslashes($course_header->category->name));
        //If no exists, try by name only
        if (!$category) {
            $category = get_record("course_categories","name",addslashes($course_header->category->name));
        }
        //If no exists, get category id 1
        if (!$category) {
            $category = get_record("course_categories","id","1");
        }
        //If exists, put new category id
        if ($category) {
            $course_header->category->id = $category->id;
            $course_header->category->name = $category->name;
        //Error, cannot locate category
        } else {
            $course_header->category->id = 0;
            $course_header->category->name = getstring("unknowcategory");
            $status = false;
        }

        //Create the course_object
        if ($status) {
            $course->category = addslashes($course_header->category->id);
            $course->password = addslashes($course_header->course_password);
            $course->fullname = addslashes($course_header->course_fullname);
            $course->shortname = addslashes($course_header->course_shortname);
            $course->summary = addslashes($course_header->course_summary);
            $course->format = addslashes($course_header->course_format);
            $course->newsitems = addslashes($course_header->course_newsitems);
            $course->teacher = addslashes($course_header->course_teacher);
            $course->teachers = addslashes($course_header->course_teachers);
            $course->student = addslashes($course_header->course_student);
            $course->students = addslashes($course_header->course_students);
            $course->guest = addslashes($course_header->course_guest);
            $course->startdate = addslashes($course_header->course_startdate);
            $course->numsections = addslashes($course_header->course_numsections);
            $course->showrecent = addslashes($course_header->course_showrecent);
            $course->marker = addslashes($course_header->course_marker);
            $course->timecreated = addslashes($course_header->course_timecreated);
            $course->timemodified = addslashes($course_header->course_timemodified);
            //Now insert the record
            $newid = insert_record("course",$course);
            if ($newid) {
                //save old and new course id
                backup_putid ($restore->backup_unique_code,"course",$course_header->course_id,$newid);
                //Replace old course_id in course_header
                $course_header->course_id = $newid;
            } else {
                $status = false;
            }
        }

        return $status;
    }

    //This function creates all the course_sections and course_modules from xml
    //when restoring in a new course or simply checks sections and create records
    //in backup_ids when restoring in a existing course
    function restore_create_sections($restore,$xml_file) {

        global $CFG,$db;

        $status = true;
        //Check it exists
        if (!file_exists($xml_file)) {
            $status = false;
        }
        //Get info from xml
        if ($status) {
            $info = restore_read_xml_sections($xml_file);
        }
        //Put the info in the DB, recoding ids and saving the in backup tables

        $sequence = "";

        if ($info) {
            //For each, section, save it to db
            foreach ($info->sections as $key => $sect) {
                $sequence = "";
                $section->course = $restore->course_id;
                $section->section = $sect->number;
                $section->summary = addslashes($sect->summary);
                $section->visible = $sect->visible;
                $section->sequence = "";
                //Now calculate the section's newid
                $newid = 0;
                if ($restore->restoreto == 1) {
                //Save it to db (only if restoring to new course)
                    $newid = insert_record("course_sections",$section);
                } else {
                    //Get section id when restoring in existing course
                    $rec = get_record("course_sections","course",$restore->course_id,
                                                        "section",$section->section);
                    //If that section doesn't exist, get section 0 (every mod will be
                    //asigned there
                    if(!$rec) {
                        $rec = get_record("course_sections","course",$restore->course_id,
                                                            "section","0");
                    }
                    $newid = $rec->id;
                    $sequence = $rec->sequence;
                }
                if ($newid) {
                    //save old and new section id
                    backup_putid ($restore->backup_unique_code,"course_sections",$key,$newid);
                } else {
                    $status = false;
                }
                //If all is OK, go with associated mods
                if ($status) {
                    //If we have mods in the section
                    if ($sect->mods) {
                        //For each mod inside section
                        foreach ($sect->mods as $keym => $mod) {
                            //Check if we've to restore this module
                            if ($restore->mods[$mod->type]->restore) {
                                //Get the module id from modules
                                $module = get_record("modules","name",$mod->type);
                                if ($module) {
                                    $course_module->course = $restore->course_id;
                                    $course_module->module = $module->id;
                                    $course_module->section = $newid;
                                    $course_module->added = $mod->added;
                                    $course_module->deleted = $mod->deleted;
                                    $course_module->score = $mod->score;
                                    $course_module->visible = $mod->visible;
                                    $course_module->instance = null;
                                    //NOTE: The instance (new) is calculated and updated in db in the
                                    //      final step of the restore. We don't know it yet.
                                    //print_object($course_module);					//Debug
                                    //Save it to db
                                    $newidmod = insert_record("course_modules",$course_module);
                                    if ($newidmod) {
                                        //save old and new module id
                                        //In the info field, we save the original instance of the module
                                        //to use it later
                                        backup_putid ($restore->backup_unique_code,"course_modules",
                                                                $keym,$newidmod,$mod->instance);
                                    } else {
                                        $status = false;
                                    }
                                    //Now, calculate the sequence field
                                    if ($status) {
                                        if ($sequence) {
                                            $sequence .= ",".$newidmod;
                                        } else {
                                            $sequence = $newidmod;
                                        }
                                    }
                                } else {
                                    $status = false;
                                }
                            }
                        }
                    }
                }
                //If all is OK, update sequence field in course_sections
                if ($status) {
                    $rec->id = $newid;
                    $rec->sequence = $sequence;
                    $status = update_record("course_sections",$rec);
                }
            }
        } else {
            $status = false;
        }
        return $status;
    }

    //This function creates all the user, user_students, user_teachers
    //user_course_creators and user_admins from xml
    function restore_create_users($restore,$xml_file) {

        global $CFG;

        $status = true;
        //Check it exists
        if (!file_exists($xml_file)) {
            $status = false;
        }
        //Get info from xml
        if ($status) {
            //info will contain the old_id of every user
            //in backup_ids->info will be the real info (serialized)
            $info = restore_read_xml_users($restore,$xml_file);
        }

        //Now, get evey user_id from $info and user data from $backup_ids
        //and create the necessary records (users, user_students, user_teachers
        //user_course_creators and user_admins
        if ($info) {
            //For each, user, take its info from backup_ids
            foreach ($info->users as $userid) {
                $rec = backup_getid($restore->backup_unique_code,"user",$userid);
                //First strip slashes
                $temp = stripslashes($rec->info);
                //Now unserialize
                $user = unserialize($temp);
                //Calculate if it is a course user
                //Has role teacher or student or admin or coursecreator
                $is_course_user = ($user->roles[teacher] or $user->roles[student] or
                                   $user->roles[admin] or $user->roles[coursecreator]);
                //Check if it's admin and coursecreator
                $is_admin = ($user->roles[admin]);
                $is_coursecreator = ($user->roles[coursecreator]);
                //Check if it's teacher and student
                $is_teacher = ($user->roles[teacher]);
                $is_student = ($user->roles[student]);
                //To store new ids created
                $newid=null;
                //check if it exists (by username) and get its id
                $user_exists = true;
                $user_data = get_record("user","username",$user->username);
                if (!$user_data) {
                    $user_exists = false;
                } else {
                    $newid = $user_data->id;
                }
                //Flags to see if we have to create the user and roles
                $create_user = true;
                $create_roles = true;

                //If we are restoring course users and it isn't a course user
                if ($restore->users == 1 and !$is_course_user) {
                    //If only restoring course_users and user isn't a course_user, inform to $backup_ids
                    $status = backup_putid($restore->backup_unique_code,"user",$userid,null,'notincourse');
                    $create_user = false;
                    $create_roles = false;
                }

                if ($user_exists and $create_user) {
                    //If user exists mark its newid in backup_ids (the same than old)
                    $status = backup_putid($restore->backup_unique_code,"user",$userid,$newid,'exists');
                    $create_user = false;
                }

                //Here, if create_user, do it
                if ($create_user) {
                    //We are going to create the user
                    //The structure is exactly as we need
                    $newid = insert_record ("user",$user);
                    //Put the new id
                    $status = backup_putid($restore->backup_unique_code,"user",$userid,$newid,"new");
                }

                //Here, if create_roles, do it as necessary
                if ($create_roles) {
                    //Get the newid and currecnt info from backup_ids
                    $data = backup_getid($restore->backup_unique_code,"user",$userid);
                    $newid = $data->new_id;
                    $currinfo = $data->info.",";
                    //Now, depending of the role, create records in user_studentes and user_teacher
                    //and/or mark it in backup_ids
                    if ($is_admin) {
                        //If the record (user_admins) doesn't exists
                        if (!record_exists("user_admins","userid",$newid)) {
                            //Only put status in backup_ids
                            $currinfo = $currinfo."admin,";
                            $status = backup_putid($restore->backup_unique_code,"user",$userid,$newid,$currinfo);
                        }
                    }
                    if ($is_coursecreator) {
                        //If the record (user_coursecreators) doesn't exists
                        if (!record_exists("user_coursecreators","userid",$newid)) {
                            //Only put status in backup_ids
                            $currinfo = $currinfo."coursecreator,";
                            $status = backup_putid($restore->backup_unique_code,"user",$userid,$newid,$currinfo);
                        }
                    }
                    if ($is_teacher) {
                        //If the record (teacher) doesn't exists
                        if (!record_exists("user_teachers","userid",$newid,"course", $restore->course_id)) {
                            //Put status in backup_ids
                            $currinfo = $currinfo."teacher,";
                            $status = backup_putid($restore->backup_unique_code,"user",$userid,$newid,$currinfo);
                            //Set course and user
                            $user->roles[teacher]->course = $restore->course_id;
                            $user->roles[teacher]->userid = $newid;
                            //Insert data in user_teachers
                            //The structure is exactly as we need
                            $status = insert_record("user_teachers",$user->roles[teacher]);
                        }
                    }
                    if ($is_student) {
                        //If the record (student) doesn't exists
                        if (!record_exists("user_students","userid",$newid,"course", $restore->course_id)) {
                            //Put status in backup_ids
                            $currinfo = $currinfo."student,";
                            $status = backup_putid($restore->backup_unique_code,"user",$userid,$newid,$currinfo);
                            //Set course and user
                            $user->roles[student]->course = $restore->course_id;
                            $user->roles[student]->userid = $newid;
                            //Insert data in user_students
                            //The structure is exactly as we need
                            $status = insert_record("user_students",$user->roles[student]);
                        }
                    }
                    if (!$is_course_user) {
                        //If the record (user) doesn't exists
                        if (!record_exists("user","id",$newid)) {
                            //Put status in backup_ids
                            $currinfo = $currinfo."user,";
                            $status = backup_putid($restore->backup_unique_code,"user",$userid,$newid,$currinfo);
                        }
                    }
                }
            }
        } else {
            $status = false;
        }

        return $status;
    }

    //This function restores the userfiles from the temp (user_files) directory to the
    //dataroot/users directory
    function restore_user_files($restore) {

        global $CFG;

        $status = true;

        //First, we check to "users" exists and create is as necessary
        //in CFG->dataroot
        $dest_dir = $CFG->dataroot."/users";
        $status = check_dir_exists($dest_dir,true);

        //Now, we iterate over "user_files" records to check if that user dir must be
        //copied (and renamed) to the "users" dir.
        $rootdir = $CFG->dataroot."/temp/backup/".$restore->backup_unique_code."/user_files";
        //Check if directory exists
        if (is_dir($rootdir)) {
            $list = list_directories ($rootdir);
            if ($list) {
                //Iterate
                $counter = 0;
                foreach ($list as $dir) {
                    //Look for dir like username in backup_ids
                    $data = get_record ("backup_ids","backup_code",$restore->backup_unique_code,
                                                     "table_name","user",
                                                     "old_id",$dir);
                    //If thar user exists in backup_ids
                    if ($data) {
                        //Only it user has been created now
                        if (strpos($data->info,"new") !== false) {
                            //Copy the old_dir to its new location (and name) !!
                            //Only if destination doesn't exists
                            if (!file_exists($dest_dir."/".$data->new_id)) {
                                $status = backup_copy_file($rootdir."/".$dir,
                                              $dest_dir."/".$data->new_id);
                                $counter ++;
                            }
                        }
                    }
                }
            }
        }
        //If status is ok and whe have dirs created, returns counter to inform
        if ($status and $counter) {
            return $counter;
        } else {
            return $status;
        }
    }

    //This function restores the course files from the temp (course_files) directory to the
    //dataroot/course_id directory
    function restore_course_files($restore) {

        global $CFG;

        $status = true;

        //First, we check to "course_id" exists and create is as necessary
        //in CFG->dataroot
        $dest_dir = $CFG->dataroot."/".$restore->course_id;
        $status = check_dir_exists($dest_dir,true);

        //Now, we iterate over "course_files" records to check if that file/dir must be
        //copied to the "dest_dir" dir.
        $rootdir = $CFG->dataroot."/temp/backup/".$restore->backup_unique_code."/course_files";
        //Check if directory exists
        if (is_dir($rootdir)) {
            $list = list_directories_and_files ($rootdir);
            if ($list) {
                //Iterate
                $counter = 0;
                foreach ($list as $dir) {
                    //Copy the dir to its new location
                    //Only if destination file/dir doesn exists
                    if (!file_exists($dest_dir."/".$dir)) {
                        $status = backup_copy_file($rootdir."/".$dir,
                                      $dest_dir."/".$dir);
                        $counter ++;
                    }
                }
            }
        }
        //If status is ok and whe have dirs created, returns counter to inform
        if ($status and $counter) {
            return $counter;
        } else {
            return $status;
        }
    }


    //This function creates all the structures for every module in backup file
    //Depending what has been selected.
    function restore_create_modules($restore,$xml_file) {

        global $CFG;

        $status = true;
        //Check it exists
        if (!file_exists($xml_file)) {
            $status = false;
        }
        //Get info from xml
        if ($status) {
            //info will contain the id and modtype of every module
            //in backup_ids->info will be the real info (serialized)
            $info = restore_read_xml_modules($restore,$xml_file);
        }

        //Now, if we have anything in info, we have to restore that mods
        //from backup_ids (calling every mod restore function)
        if ($info) {
            //Iterate over each module
            foreach ($info as $mod) {
                $modrestore = $mod->modtype."_restore_mods";
                if (function_exists($modrestore)) {
                    $status = $modrestore($mod,$restore);
                } else {
                    //Something was wrong. Function should exist.
                    $status = false;
                }
            }
        } else {
            $status = false;
        }
       return $status;
    }

    //This function adjusts the instance field into course_modules. It's executed after
    //modules restore. There, we KNOW the new instance id !!
    function restore_check_instances($restore) {

        global $CFG;

        $status = true;

        //We are going to iterate over each course_module saved in backup_ids
        $course_modules = get_records_sql("SELECT new_id,info as instance
                                           FROM {$CFG->prefix}backup_ids
                                           WHERE backup_code = '$restore->backup_unique_code' AND
                                                 table_name = 'course_modules'");
        if ($course_modules) {
            foreach($course_modules as $cm) {
                //Now we are going to the REAL course_modules to get its type (field module)
                $module = get_record("course_modules","id",$cm->new_id);
                if ($module) {
                    //We know the module type id. Get the name from modules
                    $type = get_record("modules","id",$module->module);
                    if ($type) {
                        //We know the type name and the old_id. Get its new_id
                        //from backup_ids. It's the instance !!!
                        $instance = get_record("backup_ids","backup_code",$restore->backup_unique_code,
                                                            "table_name",$type->name,
                                                            "old_id",$cm->instance);
                        if ($instance) {
                            //We have the new instance, so update the record in course_modules
                            $module->instance = $instance->new_id;
                            //print_object ($module); 							//Debug
                            $status = update_record("course_modules",$module);
                        } else {
                            $status = false;
                        }
                    } else {
                        $status = false;
                    }
                } else {
                    $status = false;
               }
            }
        } else {
            $status = false;
        }


        return $status;
    }

    //=====================================================================================
    //==                                                                                 ==
    //==                         XML Functions (SAX)                                     ==
    //==                                                                                 ==
    //=====================================================================================

    //This is the class used to do all the xml parse
    class MoodleParser {

        var $level = 0;        //Level we are
        var $tree = array();   //Array of levels we are
        var $content = "";     //Content under current level
        var $todo = "";        //What we hav to do when parsing
        var $info = "";        //Information collected. Temp storage. Used to return data after parsing.
        var $temp = "";        //Temp storage.
        var $preferences = ""; //Preferences about what to load !!
        var $finished = false; //Flag to say xml_parse to stop

        //This function is used to get the current contents property value
        //They are trimed and converted from utf8
        function getContents() {
            return trim(utf8_decode($this->content));
        }

        //This is the startTag handler we use where we are reading the info zone (todo="INFO")
        function startElementInfo($parser, $tagName, $attrs) {
            //Refresh properties
            $this->level++;
            $this->tree[$this->level] = $tagName;

            //Check if we are into INFO zone
            //if ($this->tree[2] == "INFO")                                                             //Debug
            //    echo $this->level.str_repeat("&nbsp;",$this->level*2)."&lt;".$tagName."&gt;<br>\n";   //Debug
        }

        //This is the startTag handler we use where we are reading the course header zone (todo="COURSE_HEADER")
        function startElementCourseHeader($parser, $tagName, $attrs) {
            //Refresh properties
            $this->level++;
            $this->tree[$this->level] = $tagName;

            //Check if we are into COURSE_HEADER zone
            //if ($this->tree[3] == "HEADER")                                                           //Debug
            //    echo $this->level.str_repeat("&nbsp;",$this->level*2)."&lt;".$tagName."&gt;<br>\n";   //Debug
        }

        //This is the startTag handler we use where we are reading the sections zone (todo="SECTIONS")
        function startElementSections($parser, $tagName, $attrs) {
            //Refresh properties
            $this->level++;
            $this->tree[$this->level] = $tagName;

            //Check if we are into SECTIONS zone
            //if ($this->tree[3] == "SECTIONS")                                                         //Debug
            //    echo $this->level.str_repeat("&nbsp;",$this->level*2)."&lt;".$tagName."&gt;<br>\n";   //Debug
        }

        //This is the startTag handler we use where we are reading the user zone (todo="USERS")
        function startElementUsers($parser, $tagName, $attrs) {
            //Refresh properties
            $this->level++;
            $this->tree[$this->level] = $tagName;
            //Check if we are into USERS zone
            //if ($this->tree[3] == "USERS")                                                            //Debug
            //    echo $this->level.str_repeat("&nbsp;",$this->level*2)."&lt;".$tagName."&gt;<br>\n";   //Debug
        }

        //This is the startTag handler we use where we are reading the modules zone (todo="MODULES")
        function startElementModules($parser, $tagName, $attrs) {
            //Refresh properties
            $this->level++;
            $this->tree[$this->level] = $tagName;
            //Check if we are into MODULES zone
            //if ($this->tree[3] == "MODULES")                                                          //Debug
            //    echo $this->level.str_repeat("&nbsp;",$this->level*2)."&lt;".$tagName."&gt;<br>\n";   //Debug
            //If we are under a MOD tag under a MODULES zone, accumule it
            if (($this->tree[4] == "MOD") and ($this->tree[3] == "MODULES")) {
                $this->temp .= "<".$tagName.">";
            }
        }

        //This is the startTag default handler we use when todo is undefined
        function startElement($parser, $tagName, $attrs) {
            $this->level++;
            $this->tree[$this->level] = $tagName;
            echo $this->level.str_repeat("&nbsp;",$this->level*2)."&lt;".$tagName."&gt;<br>\n";   //Debug
        }

        //This is the endTag handler we use where we are reading the info zone (todo="INFO")
        function endElementInfo($parser, $tagName) {
            //Check if we are into INFO zone
            if ($this->tree[2] == "INFO") {
                //if (trim($this->content))                                                                     //Debug
                //    echo "C".str_repeat("&nbsp;",($this->level+2)*2).$this->getContents()."<br>\n";           //Debug
                //echo $this->level.str_repeat("&nbsp;",$this->level*2)."&lt;/".$tagName."&gt;<br>\n";          //Debug
                //Dependig of different combinations, do different things
                if ($this->level == 3) {
                    switch ($tagName) {
                        case "NAME":
                            $this->info->backup_name = $this->getContents();
                            break;
                        case "MOODLE_VERSION":
                            $this->info->backup_moodle_version = $this->getContents();
                            break;
                        case "MOODLE_RELEASE":
                            $this->info->backup_moodle_release = $this->getContents();
                            break;
                        case "BACKUP_VERSION":
                            $this->info->backup_backup_version = $this->getContents();
                            break;
                        case "BACKUP_RELEASE":
                            $this->info->backup_backup_release = $this->getContents();
                            break;
                        case "DATE":
                            $this->info->backup_date = $this->getContents();
                            break;
                    }
                }
                if ($this->tree[3] == "DETAILS") {
                    if ($this->level == 4) {
                        switch ($tagName) {
                            case "USERS":
                                $this->info->backup_users = $this->getContents();
                                break;
                            case "LOGS":
                                $this->info->backup_logs = $this->getContents();
                                break;
                            case "USERFILES":
                                $this->info->backup_user_files = $this->getContents();
                                break;
                            case "COURSEFILES":
                                $this->info->backup_course_files = $this->getContents();
                                break;
                        }
                    }
                    if ($this->level == 5) {
                        switch ($tagName) {
                            case "NAME":
                                $this->info->tempName = $this->getContents();
                                break;
                            case "INCLUDED":
                                $this->info->mods[$this->info->tempName]->backup = $this->getContents();
                                break;
                            case "USERINFO":
                                $this->info->mods[$this->info->tempName]->userinfo = $this->getContents();
                                break;
                        }
                    }
                }
            }


            //Clear things
            $this->tree[$this->level] = "";
            $this->level--;
            $this->content = "";

            //Stop parsing if todo = INFO and tagName = INFO (en of the tag, of course)
            //Speed up a lot (avoid parse all)
            if ($tagName == "INFO") {
                $this->finished = true;
            }
        }

        //This is the endTag handler we use where we are reading the course_header zone (todo="COURSE_HEADER")
        function endElementCourseHeader($parser, $tagName) {
            //Check if we are into COURSE_HEADER zone
            if ($this->tree[3] == "HEADER") {
                //if (trim($this->content))                                                                     //Debug
                //    echo "C".str_repeat("&nbsp;",($this->level+2)*2).$this->getContents()."<br>\n";           //Debug
                //echo $this->level.str_repeat("&nbsp;",$this->level*2)."&lt;/".$tagName."&gt;<br>\n";          //Debug
                //Dependig of different combinations, do different things
                if ($this->level == 4) {
                    switch ($tagName) {
                        case "ID":
                            $this->info->course_id = $this->getContents();
                            break;
                        case "PASSWORD":
                            $this->info->course_password = $this->getContents();
                            break;
                        case "FULLNAME":
                            $this->info->course_fullname = $this->getContents();
                            break;
                        case "SHORTNAME":
                            $this->info->course_shortname = $this->getContents();
                            break;
                        case "SUMMARY":
                            $this->info->course_summary = $this->getContents();
                            break;
                        case "FORMAT":
                            $this->info->course_format = $this->getContents();
                            break;
                        case "NEWSITEMS":
                            $this->info->course_newsitems = $this->getContents();
                            break;
                        case "TEACHER":
                            $this->info->course_teacher = $this->getContents();
                            break;
                        case "TEACHERS":
                            $this->info->course_teachers = $this->getContents();
                            break;
                        case "STUDENT":
                            $this->info->course_student = $this->getContents();
                            break;
                        case "STUDENTS":
                            $this->info->course_students = $this->getContents();
                            break;
                        case "GUEST":
                            $this->info->course_guest = $this->getContents();
                            break;
                        case "STARDATE":
                            $this->info->course_stardate = $this->getContents();
                            break;
                        case "NUMSECTIONS":
                            $this->info->course_numsections = $this->getContents();
                            break;
                        case "SHOWRECENT":
                            $this->info->course_showrecent = $this->getContents();
                            break;
                        case "MARKER":
                            $this->info->course_marker = $this->getContents();
                            break;
                        case "TIMECREATED":
                            $this->info->course_timecreated = $this->getContents();
                            break;
                        case "TIMEMODIFIED":
                            $this->info->course_timemodified = $this->getContents();
                            break;
                    }
                }
                if ($this->tree[4] == "CATEGORY") {
                    if ($this->level == 5) {
                        switch ($tagName) {
                            case "ID":
                                $this->info->category->id = $this->getContents();
                                break;
                            case "NAME":
                                $this->info->category->name = $this->getContents();
                                break;
                        }
                    }
                }

            }
            //Clear things
            $this->tree[$this->level] = "";
            $this->level--;
            $this->content = "";

            //Stop parsing if todo = COURSE_HEADER and tagName = HEADER (en of the tag, of course)
            //Speed up a lot (avoid parse all)
            if ($tagName == "HEADER") {
                $this->finished = true;
            }
        }

        //This is the endTag handler we use where we are reading the sections zone (todo="SECTIONS")
        function endElementSections($parser, $tagName) {
            //Check if we are into SECTIONS zone
            if ($this->tree[3] == "SECTIONS") {
                //if (trim($this->content))                                                                     //Debug
                //    echo "C".str_repeat("&nbsp;",($this->level+2)*2).$this->getContents()."<br>\n";           //Debug
                //echo $this->level.str_repeat("&nbsp;",$this->level*2)."&lt;/".$tagName."&gt;<br>\n";          //Debug
                //Dependig of different combinations, do different things
                if ($this->level == 4) {
                    switch ($tagName) {
                        case "SECTION":
                            //We've finalized a section, get it
                            $this->info->sections[$this->info->tempsection->id]->number = $this->info->tempsection->number;
                            $this->info->sections[$this->info->tempsection->id]->summary = $this->info->tempsection->summary;
                            $this->info->sections[$this->info->tempsection->id]->visible = $this->info->tempsection->visible;
                            $this->info->sections[$this->info->tempsection->id]->mods = $this->info->tempsection->mods;
                            unset($this->info->tempsection);
                    }
                }
                if ($this->level == 5) {
                    switch ($tagName) {
                        case "ID":
                            $this->info->tempsection->id = $this->getContents();
                            break;
                        case "NUMBER":
                            $this->info->tempsection->number = $this->getContents();
                            break;
                        case "SUMMARY":
                            $this->info->tempsection->summary = $this->getContents();
                            break;
                        case "VISIBLE":
                            $this->info->tempsection->visible = $this->getContents();
                            break;
                    }
                }
                if ($this->level == 6) {
                    switch ($tagName) {
                        case "MOD":
                            //We've finalized a mod, get it
                            $this->info->tempsection->mods[$this->info->tempmod->id]->type =
                                $this->info->tempmod->type;
                            $this->info->tempsection->mods[$this->info->tempmod->id]->instance =
                                $this->info->tempmod->instance;
                            $this->info->tempsection->mods[$this->info->tempmod->id]->added =
                                $this->info->tempmod->added;
                            $this->info->tempsection->mods[$this->info->tempmod->id]->deleted =
                                $this->info->tempmod->deleted;
                            $this->info->tempsection->mods[$this->info->tempmod->id]->score =
                                $this->info->tempmod->score;
                            $this->info->tempsection->mods[$this->info->tempmod->id]->visible =
                                $this->info->tempmod->visible;
                            unset($this->info->tempmod);
                    }
                }
                if ($this->level == 7) {
                    switch ($tagName) {
                        case "ID":
                            $this->info->tempmod->id = $this->getContents();
                            break;
                        case "TYPE":
                            $this->info->tempmod->type = $this->getContents();
                            break;
                        case "INSTANCE":
                            $this->info->tempmod->instance = $this->getContents();
                            break;
                        case "ADDED":
                            $this->info->tempmod->added = $this->getContents();
                            break;
                        case "DELETED":
                            $this->info->tempmod->deleted = $this->getContents();
                            break;
                        case "SCORE":
                            $this->info->tempmod->score = $this->getContents();
                            break;
                        case "VISIBLE":
                            $this->info->tempmod->visible = $this->getContents();
                            break;
                    }
                }
            }
            //Clear things
            $this->tree[$this->level] = "";
            $this->level--;
            $this->content = "";

            //Stop parsing if todo = SECTIONS and tagName = SECTIONS (en of the tag, of course)
            //Speed up a lot (avoid parse all)
            if ($tagName == "SECTIONS") {
                $this->finished = true;
            }
        }

        //This is the endTag handler we use where we are reading the users zone (todo="USERS")
        function endElementUsers($parser, $tagName) {
            //Check if we are into USERS zone
            if ($this->tree[3] == "USERS") {
                //if (trim($this->content))                                                                     //Debug
                //    echo "C".str_repeat("&nbsp;",($this->level+2)*2).$this->getContents()."<br>\n";           //Debug
                //echo $this->level.str_repeat("&nbsp;",$this->level*2)."&lt;/".$tagName."&gt;<br>\n";          //Debug
                //Dependig of different combinations, do different things
                if ($this->level == 4) {
                    switch ($tagName) {
                        case "USER":
                            //We've finalized a user, get it and save to db
                            //First serialize
                            $ser_temp = serialize($this->info->tempuser);
                            //Now add slashes
                            $sla_temp = addslashes($ser_temp);
                            //Save to db
                            backup_putid($this->preferences->backup_unique_code,"user",$this->info->tempuser->id,
                                          null,$sla_temp);
                            //Delete temp obejct
                            unset($this->info->tempuser);
                            break;
                    }
                }
                if ($this->level == 5) {
                    switch ($tagName) {
                        case "ID":
                            $this->info->users[$this->getContents()] = $this->getContents();
                            $this->info->tempuser->id = $this->getContents();
                            break;
                        case "CONFIRMED":
                            $this->info->tempuser->confirmed = $this->getContents();
                            break;
                        case "DELETED":
                            $this->info->tempuser->deleted = $this->getContents();
                            break;
                        case "USERNAME":
                            $this->info->tempuser->username = $this->getContents();
                            break;
                        case "PASSWORD":
                            $this->info->tempuser->password = $this->getContents();
                            break;
                        case "IDNUMBER":
                            $this->info->tempuser->idnumber = $this->getContents();
                            break;
                        case "FIRSTNAME":
                            $this->info->tempuser->firstname = $this->getContents();
                            break;
                        case "LASTNAME":
                            $this->info->tempuser->lastname = $this->getContents();
                            break;
                        case "EMAIL":
                            $this->info->tempuser->email = $this->getContents();
                            break;
                        case "ICQ":
                            $this->info->tempuser->icq = $this->getContents();
                            break;
                        case "PHONE1":
                            $this->info->tempuser->phone1 = $this->getContents();
                            break;
                        case "PHONE2":
                            $this->info->tempuser->phone2 = $this->getContents();
                            break;
                        case "INSTITUTION":
                            $this->info->tempuser->institution = $this->getContents();
                            break;
                        case "DEPARTMENT":
                            $this->info->tempuser->department = $this->getContents();
                            break;
                        case "ADDRESS":
                            $this->info->tempuser->address = $this->getContents();
                            break;
                        case "CITY":
                            $this->info->tempuser->city = $this->getContents();
                            break;
                        case "COUNTRY":
                            $this->info->tempuser->country = $this->getContents();
                            break;
                        case "LANG":
                            $this->info->tempuser->lang = $this->getContents();
                            break;
                        case "TIMEZONE":
                            $this->info->tempuser->timezone = $this->getContents();
                            break;
                        case "FIRSTACCESS":
                            $this->info->tempuser->firstaccess = $this->getContents();
                            break;
                        case "LASTACCESS":
                            $this->info->tempuser->lastaccess = $this->getContents();
                            break;
                        case "LASTLOGIN":
                            $this->info->tempuser->lastlogin = $this->getContents();
                            break;
                        case "CURRENTLOGIN":
                            $this->info->tempuser->currentlogin = $this->getContents();
                            break;
                        case "LASTIP":
                            $this->info->tempuser->lastip = $this->getContents();
                            break;
                        case "SECRET":
                            $this->info->tempuser->secret = $this->getContents();
                            break;
                        case "PICTURE":
                            $this->info->tempuser->picture = $this->getContents();
                            break;
                        case "URL":
                            $this->info->tempuser->url = $this->getContents();
                            break;
                        case "DESCRIPTION":
                            $this->info->tempuser->description = $this->getContents();
                            break;
                        case "MAILFORMAT":
                            $this->info->tempuser->mailformat = $this->getContents();
                            break;
                        case "MAILDISPLAY":
                            $this->info->tempuser->maildisplay = $this->getContents();
                            break;
                        case "HTMLEDITOR":
                            $this->info->tempuser->htmleditor = $this->getContents();
                            break;
                        case "AUTOSUBSCRIBE":
                            $this->info->tempuser->autosubscribe = $this->getContents();
                            break;
                        case "TIMEMODIFIED":
                            $this->info->tempuser->timemodified = $this->getContents();
                            break;
                    }
                }
                if ($this->level == 6) {
                    switch ($tagName) {
                        case "ROLE":
                            //We've finalized a role, get it
                            $this->info->tempuser->roles[$this->info->temprole->type]->authority =
                                $this->info->temprole->authority;
                            $this->info->tempuser->roles[$this->info->temprole->type]->tea_role =
                                $this->info->temprole->tea_role;
                            $this->info->tempuser->roles[$this->info->temprole->type]->timestart =
                                $this->info->temprole->timestart;
                            $this->info->tempuser->roles[$this->info->temprole->type]->timeend =
                                $this->info->temprole->timeend;
                            $this->info->tempuser->roles[$this->info->temprole->type]->time =
                                $this->info->temprole->time;
                            unset($this->info->temprole);
                            break;
                    }
                }
                if ($this->level == 7) {
                    switch ($tagName) {
                        case "TYPE":
                            $this->info->temprole->type = $this->getContents();
                            break;
                        case "AUTHORITY":
                            $this->info->temprole->authority = $this->getContents();
                            break;
                        case "TEA_ROLE":
                            $this->info->temprole->tea_role = $this->getContents();
                            break;
                        case "TIMESTART":
                            $this->info->temprole->timestart = $this->getContents();
                            break;
                        case "TIMEEND":
                            $this->info->temprole->timeend = $this->getContents();
                            break;
                        case "TIME":
                            $this->info->temprole->time = $this->getContents();
                            break;
                    }
                }
            }

            //Stop parsing if todo = USERS and tagName = USERS (en of the tag, of course)
            //Speed up a lot (avoid parse all)
            if ($tagName == "USERS" and $this->level == 3) {
                $this->finished = true;
            }

            //Clear things
            $this->tree[$this->level] = "";
            $this->level--;
            $this->content = "";
        }

        //This is the endTag handler we use where we are reading the modules zone (todo="MODULES")
        function endElementModules($parser, $tagName) {
            //Check if we are into MODULES zone
            if ($this->tree[3] == "MODULES") {
                //if (trim($this->content))                                                                     //Debug
                //    echo "C".str_repeat("&nbsp;",($this->level+2)*2).$this->getContents()."<br>\n";           //Debug
                //echo $this->level.str_repeat("&nbsp;",$this->level*2)."&lt;/".$tagName."&gt;<br>\n";          //Debug
                //Acumulate data to info (content + close tag)
                //Reconvert: strip htmlchars again and trim to generate xml data
                $this->temp .= htmlspecialchars(trim($this->content))."</".$tagName.">";
                //If we've finished a mod, xmlize it an save to db
                if (($this->level == 4) and ($tagName == "MOD")) {
                    //Prepend XML standard header to info gathered
                    $xml_data = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n".$this->temp;
                    //Call to xmlize for this portion of xml data (one MOD)
                    $data = xmlize($xml_data);
                    //traverse_xmlize($data);                                                                     //Debug
                    //print_object ($GLOBALS['traverse_array']);                                                  //Debug
                    //$GLOBALS['traverse_array']="";                                                              //Debug
                    //Now, save data to db. We'll use it later
                    //Get id and modtype from data
                    $mod_id = $data["MOD"]["#"]["ID"]["0"]["#"];
                    $mod_type = $data["MOD"]["#"]["MODTYPE"]["0"]["#"];
                    //Only if we've selected to restore it
                    if  ($this->preferences->mods[$mod_type]->restore) {
                        //Serialize it
                        $mod_temp = serialize($data);
                        //Now add slashes
                        $sla_mod_temp = addslashes($mod_temp);
                        //Save to db
                        backup_putid($this->preferences->backup_unique_code,$mod_type,$mod_id,
                                     null,$sla_mod_temp);
                        //Create returning info
                        $ret_info->id = $mod_id;
                        $ret_info->modtype = $mod_type;
                        $this->info[] = $ret_info;
                    }
                    //Reset info to empty
                    $this->temp = "";
                }
            }

            //Stop parsing if todo = MODULES and tagName = MODULES (en of the tag, of course)
            //Speed up a lot (avoid parse all)
            if ($tagName == "MODULES" and $this->level == 3) {
                $this->finished = true;
            }

            //Clear things
            $this->tree[$this->level] = "";
            $this->level--;
            $this->content = "";
        }

        //This is the endTag default handler we use when todo is undefined
        function endElement($parser, $tagName) {
            if (trim($this->content))                                                                     //Debug
                echo "C".str_repeat("&nbsp;",($this->level+2)*2).$this->getContents()."<br>\n";           //Debug
            echo $this->level.str_repeat("&nbsp;",$this->level*2)."&lt;/".$tagName."&gt;<br>\n";          //Debug

            //Clear things
            $this->tree[$this->level] = "";
            $this->level--;
            $this->content = "";
        }

        //This is the handler to read data contents (simple accumule it)
        function characterData($parser, $data) {
            $this->content .= $data;
        }
    }

    //This function executes the MoodleParser
    function restore_read_xml ($xml_file,$todo,$preferences) {

        $status = true;

        $xml_parser = xml_parser_create();
        $moodle_parser = new MoodleParser();
        $moodle_parser->todo = $todo;
        $moodle_parser->preferences = $preferences;
        xml_set_object($xml_parser,&$moodle_parser);
        //Depending of the todo we use some element_handler or another
        if ($todo == "INFO") {
            //Define handlers to that zone
            xml_set_element_handler($xml_parser, "startElementInfo", "endElementInfo");
        } else if ($todo == "COURSE_HEADER") {
            //Define handlers to that zone
            xml_set_element_handler($xml_parser, "startElementCourseHeader", "endElementCourseHeader");
        } else if ($todo == "SECTIONS") {
            //Define handlers to that zone
            xml_set_element_handler($xml_parser, "startElementSections", "endElementSections");
        } else if ($todo == "USERS") {
            //Define handlers to that zone
            xml_set_element_handler($xml_parser, "startElementUsers", "endElementUsers");
        } else if ($todo == "MODULES") {
            //Define handlers to that zone
            xml_set_element_handler($xml_parser, "startElementModules", "endElementModules");
        } else {
            //Define default handlers (must no be invoked when everything become finished)
            xml_set_element_handler($xml_parser, "startElementInfo", "endElementInfo");
        }
        xml_set_character_data_handler($xml_parser, "characterData");
        $fp = fopen($xml_file,"r")
            or $status = false;
        if ($status) {
            while ($data = fread($fp, 4096) and !$moodle_parser->finished)
                    xml_parse($xml_parser, $data, feof($fp))
                            or die(sprintf("XML error: %s at line %d",
                            xml_error_string(xml_get_error_code($xml_parser)),
                                    xml_get_current_line_number($xml_parser)));
            fclose($fp);
        }
        //Get info from parser
        $info = $moodle_parser->info;

        //Clear parser mem
        xml_parser_free($xml_parser);

        if ($status) {
            return $info;
        } else {
            return $status;
        }
    }
?>