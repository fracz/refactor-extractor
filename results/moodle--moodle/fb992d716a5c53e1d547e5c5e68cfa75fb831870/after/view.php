<?PHP  // $Id$

/// This page prints a particular instance of lesson
/// (Replace lesson with the name of your module)

    require_once('../../config.php');
    require_once('locallib.php');
    require_once('lib.php');

    $id      = required_param('id', PARAM_INT);             // Course Module ID
    $pageid  = optional_param('pageid', NULL, PARAM_INT);   // Lesson Page ID
    $action  = optional_param('action', '', PARAM_ALPHA);

    list($cm, $course, $lesson) = lesson_get_basics($id);

    require_login($course->id, false, $cm);

    $context = get_context_instance(CONTEXT_MODULE, $cm->id);
    require_capability('mod/lesson:view', $context);

/// Check these for students only
///     Check lesson availability
///     Check for password
///     Check for high scores
    if (!has_capability('mod/lesson:manage', $context)) {

        if (time() < $lesson->available) {
            lesson_print_header($cm, $course, $lesson);
            print_simple_box_start('center');
            echo '<div align="center">';
            echo get_string('lessonopen', 'lesson', userdate($lesson->available)).'<br />';
            echo '<div class="lessonbutton standardbutton" style="padding: 5px;"><a href="../../course/view.php?id='. $course->id .'">'. get_string('returnmainmenu', 'lesson') .'</a></div>';
            echo '</div>';
            print_simple_box_end();
            print_footer($course);
            exit();

        } else if (time() > $lesson->deadline) {
            lesson_print_header($cm, $course, $lesson);
            print_simple_box_start('center');
            echo '<div align="center">';
            echo get_string('lessonclosed', 'lesson', userdate($lesson->deadline)) .'<br />';
            echo '<div class="lessonbutton standardbutton" style="padding: 5px;"><a href="../../course/view.php?id='. $course->id. '">'. get_string('returnmainmenu', 'lesson') .'</a></div>';
            echo '</div>';
            print_simple_box_end();
            print_footer($course);
            exit();

        } else if ($lesson->usepassword) { // Password protected lesson code
            $correctpass = false;
            if ($password = optional_param('userpassword', '', PARAM_CLEAN)) {
                if ($lesson->password == md5(trim($password))) {
                    $USER->lessonloggedin[$lesson->id] = true;
                    $correctpass = true;
                    if ($lesson->highscores) {
                        // Logged in, now we can show high scores
                        redirect("$CFG->wwwroot/mod/lesson/highscores.php?id=$cm->id", '', 0);
                    }
                }
            } elseif (isset($USER->lessonloggedin[$lesson->id])) {
                $correctpass = true;
            }

            if (!$correctpass) {
                echo "<div class=\"password-form\">\n";
                print_simple_box_start('center');
                echo '<form name="password" method="post" action="view.php">' . "\n";
                echo '<input type="hidden" name="id" value="'. $cm->id .'" />' . "\n";
                echo '<input type="hidden" name="action" value="navigation" />' . "\n";
                if (optional_param('userpassword', 0, PARAM_CLEAN)) {
                    notify(get_string('loginfail', 'lesson'));
                }

                echo get_string('passwordprotectedlesson', 'lesson', format_string($lesson->name))."<br /><br />\n".
                     get_string('enterpassword', 'lesson')." <input type=\"password\" name=\"userpassword\" /><br /><br />\n".
                     '<span class="lessonbutton standardbutton"><a href="'.$CFG->wwwroot.'/course/view.php?id='. $course->id .'">'. get_string('cancel', 'lesson') .'</a></span> ';

                lesson_print_submit_link(get_string('continue', 'lesson'), 'password', 'center', 'standardbutton submitbutton');
                print_simple_box_end();
                echo "</div>\n";
                exit();
            }
        } else if ($lesson->highscores and !$lesson->practice and !optional_param('viewed', 0)) {
            // Display high scores before starting lesson
            redirect("$CFG->wwwroot/mod/lesson/highscores.php?id=$cm->id");
        }
    }

    lesson_print_header($cm, $course, $lesson, 'view');

    // set up some general variables
    $path = $CFG->wwwroot .'/course';

    // this is called if a student leaves during a lesson
    if($pageid == LESSON_UNSEENBRANCHPAGE) {
        $pageid = lesson_unseen_question_jump($lesson->id, $USER->id, $pageid);
    }

    // display individual pages and their sets of answers
    // if pageid is EOL then the end of the lesson has been reached
           // for flow, changed to simple echo for flow styles, michaelp, moved lesson name and page title down
   $timedflag = false;
   $attemptflag = false;
    if (empty($pageid)) {
        // make sure there are pages to view
        if (!get_field('lesson_pages', 'id', 'lessonid', $lesson->id, 'prevpageid', 0)) {
            if (!has_capability('mod/lesson:manage', $context)) {
                notify(get_string('lessonnotready', 'lesson', $course->teacher)); // a nice message to the student
            } else {
                if (!count_records('lesson_pages', 'lessonid', $lesson->id)) {
                    redirect('view.php?id='.$cm->id); // no pages - redirect to add pages
                } else {
                    notify(get_string('lessonpagelinkingbroken', 'lesson'));  // ok, bad mojo
                }
            }
            print_footer($course);
            exit();
        }

        // check for dependencies
        if ($lesson->dependency and !has_capability('mod/lesson:manage', $context)) {
            if ($dependentlesson = get_record('lesson', 'id', $lesson->dependency)) {
                // lesson exists, so we can proceed
                $conditions = unserialize($lesson->conditions);
                // assume false for all
                $timespent = false;
                $completed = false;
                $gradebetterthan = false;
                // check for the timespent condition
                if ($conditions->timespent) {
                    if ($attempttimes = get_records_select('lesson_timer', "userid = $USER->id AND lessonid = $dependentlesson->id")) {
                        // go through all the times and test to see if any of them satisfy the condition
                        foreach($attempttimes as $attempttime) {
                            $duration = $attempttime->lessontime - $attempttime->starttime;
                            if ($conditions->timespent < $duration/60) {
                                $timespent = true;
                            }
                        }
                    }
                } else {
                    $timespent = true; // there isn't one set
                }

                // check for the gradebetterthan condition
                if($conditions->gradebetterthan) {
                    if ($studentgrades = get_records_select('lesson_grades', "userid = $USER->id AND lessonid = $dependentlesson->id")) {
                        // go through all the grades and test to see if any of them satisfy the condition
                        foreach($studentgrades as $studentgrade) {
                            if ($studentgrade->grade >= $conditions->gradebetterthan) {
                                $gradebetterthan = true;
                            }
                        }
                    }
                } else {
                    $gradebetterthan = true; // there isn't one set
                }

                // check for the completed condition
                if ($conditions->completed) {
                    if (count_records('lesson_grades', 'userid', $USER->id, 'lessonid', $dependentlesson->id)) {
                        $completed = true;
                    }
                } else {
                    $completed = true; // not set
                }

                $errors = array();
                // collect all of our error statements
                if (!$timespent) {
                    $errors[] = get_string('timespenterror', 'lesson', $conditions->timespent);
                }
                if (!$completed) {
                    $errors[] = get_string('completederror', 'lesson');
                }
                if (!$gradebetterthan) {
                    $errors[] = get_string('gradebetterthanerror', 'lesson', $conditions->gradebetterthan);
                }
                if (!empty($errors)) {  // print out the errors if any
                    echo '<p>';
                    print_simple_box_start('center');
                    print_string('completethefollowingconditions', 'lesson', $dependentlesson->name);
                    echo '<p align="center">'.implode('<br />'.get_string('and', 'lesson').'<br />', $errors).'</p>';
                    print_simple_box_end();
                    echo '</p>';
                    print_footer($course);
                    exit();
                }
            }
        }
        add_to_log($course->id, 'lesson', 'start', 'view.php?id='. $cm->id, $lesson->id, $cm->id);
        // if no pageid given see if the lesson has been started
        if ($grades = get_records_select('lesson_grades', 'lessonid = '. $lesson->id .' AND userid = '. $USER->id,
                    'grade DESC')) {
            $retries = count($grades);
        } else {
            $retries = 0;
        }
        if ($retries) {
            $attemptflag = true;
        }

        if (isset($USER->modattempts[$lesson->id])) {
            unset($USER->modattempts[$lesson->id]);  // if no pageid, then student is NOT reviewing
        }

        // if there are any questions have been answered correctly in this attempt
        if ($attempts = get_records_select('lesson_attempts',
                    "lessonid = $lesson->id AND userid = $USER->id AND retry = $retries AND
                    correct = 1", 'timeseen DESC')) {

            foreach ($attempts as $attempt) {
                $jumpto = get_field('lesson_answers', 'jumpto', 'id', $attempt->answerid);
                // convert the jumpto to a proper page id
                if ($jumpto == 0) { // unlikely value!
                    $lastpageseen = $attempt->pageid;
                } elseif ($jumpto == LESSON_NEXTPAGE) {
                    if (!$lastpageseen = get_field('lesson_pages', 'nextpageid', 'id',
                                $attempt->pageid)) {
                        // no nextpage go to end of lesson
                        $lastpageseen = LESSON_EOL;
                    }
                } else {
                    $lastpageseen = $jumpto;
                }
                break; // only look at the latest correct attempt
            }
        } else {
            $attempts = NULL;
        }

        if ($branchtables = get_records_select('lesson_branch',
                            "lessonid = $lesson->id AND userid = $USER->id AND retry = $retries", 'timeseen DESC')) {
            // in here, user has viewed a branch table
            $lastbranchtable = current($branchtables);
            if ($attempts != NULL) {
                foreach($attempts as $attempt) {
                    if ($lastbranchtable->timeseen > $attempt->timeseen) {
                        // branch table was viewed later than the last attempt
                        $lastpageseen = $lastbranchtable->pageid;
                    }
                    break;
                }
            } else {
                // hasnt answered any questions but has viewed a branch table
                $lastpageseen = $lastbranchtable->pageid;
            }
        }
        //if ($lastpageseen != $firstpageid) {
        if (isset($lastpageseen) and count_records('lesson_attempts', 'lessonid', $lesson->id, 'userid', $USER->id, 'retry', $retries) > 0) {
            // get the first page
            if (!$firstpageid = get_field('lesson_pages', 'id', 'lessonid', $lesson->id,
                        'prevpageid', 0)) {
                error('Navigation: first page not found');
            }
            if ($lesson->timed) {
                if ($lesson->retake) {
                    print_simple_box('<p align="center">'. get_string('leftduringtimed', 'lesson') .'</p>', 'center');
                    echo '<div align="center" class="lessonbutton standardbutton">'.
                              '<a href="view.php?id='.$cm->id.'&amp;action=navigation&amp;pageid='.$firstpageid.'&amp;startlastseen=no">'.
                                get_string('continue', 'lesson').'</a></div>';
                } else {
                    print_simple_box_start('center');
                    echo '<div align="center">';
                    echo get_string('leftduringtimednoretake', 'lesson');
                    echo '<br /><br /><div class="lessonbutton standardbutton"><a href="../../course/view.php?id='. $course->id .'">'. get_string('returntocourse', 'lesson') .'</a></div>';
                    echo '</div>';
                    print_simple_box_end();
                }

            } else {
                print_simple_box("<p align=\"center\">".get_string('youhaveseen','lesson').'</p>',
                        "center");

                echo '<div align="center">';
                echo '<span class="lessonbutton standardbutton">'.
                        '<a href="view.php?id='.$cm->id.'&amp;action=navigation&amp;pageid='.$lastpageseen.'&amp;startlastseen=yes">'.
                        get_string('yes').'</a></span>&nbsp;&nbsp;&nbsp;';
                echo '<span class="lessonbutton standardbutton">'.
                        '<a href="view.php?id='.$cm->id.'&amp;action=navigation&amp;pageid='.$firstpageid.'&amp;startlastseen=no">'.
                        get_string('no').'</a></div>';
                echo '</span>';
            }
            print_footer($course);
            exit();
        }

        if ($grades) {
            foreach ($grades as $grade) {
                $bestgrade = $grade->grade;
                break;
            }
            if (!$lesson->retake) {
                print_simple_box_start('center');
                echo "<div align=\"center\">";
                echo get_string("noretake", "lesson");
                echo "<br /><br /><div class=\"lessonbutton standardbutton\"><a href=\"../../course/view.php?id=$course->id\">".get_string('returntocourse', 'lesson').'</a></div>';
                echo "</div>";
                print_simple_box_end();
                print_footer($course);
                exit();
                  //redirect("../../course/view.php?id=$course->id", get_string("alreadytaken", "lesson"));
            // allow student to retake course even if they have the maximum grade
            // } elseif ($bestgrade == 100) {
              //     redirect("../../course/view.php?id=$course->id", get_string("maximumgradeachieved",
            //                 "lesson"));
            }
        }
        // start at the first page
        if (!$pageid = get_field('lesson_pages', 'id', 'lessonid', $lesson->id, 'prevpageid', 0)) {
                error('Navigation: first page not found');
        }
        /// This is the code for starting a timed test
        if(!isset($USER->startlesson[$lesson->id]) && !has_capability('mod/lesson:manage', $context)) {
            $USER->startlesson[$lesson->id] = true;
            $startlesson = new stdClass;
            $startlesson->lessonid = $lesson->id;
            $startlesson->userid = $USER->id;
            $startlesson->starttime = time();
            $startlesson->lessontime = time();

            if (!insert_record('lesson_timer', $startlesson)) {
                error('Error: could not insert row into lesson_timer table');
            }
            if ($lesson->timed) {
                $timedflag = true;
            }
        }

        if (!empty($lesson->mediafile)) {
            // open our pop-up
            $url = '/mod/lesson/mediafile.php?id='.$cm->id;
            $name = 'lessonmediafile';
            $options = 'menubar=0,location=0,left=5,top=5,scrollbars,resizable,width='. $lesson->mediawidth .',height='. $lesson->mediaheight;
            echo "\n<script language=\"javascript\" type=\"text/javascript\">";
            echo "\n<!--\n";
            echo "     openpopup('$url', '$name', '$options', 0);";
            echo "\n-->\n";
            echo '</script>';
        }
    }

    if ($pageid != LESSON_EOL) {
        /// This is the code updates the lessontime for a timed test
        if ($startlastseen = optional_param('startlastseen', '', PARAM_ALPHA)) {  /// this deletes old records  not totally sure if this is necessary anymore
            if ($startlastseen == 'no') {
                if ($grades = get_records_select('lesson_grades', "lessonid = $lesson->id AND userid = $USER->id",
                            'grade DESC')) {
                    $retries = count($grades);
                } else {
                    $retries = 0;
                }
                if (!delete_records('lesson_attempts', 'userid', $USER->id, 'lessonid', $lesson->id, 'retry', $retries)) {
                    error('Error: could not delete old attempts');
                }
                if (!delete_records('lesson_branch', 'userid', $USER->id, 'lessonid', $lesson->id, 'retry', $retries)) {
                    error('Error: could not delete old seen branches');
                }
            }
        }

        add_to_log($course->id, 'lesson', 'view', 'view.php?id='. $cm->id, $pageid, $cm->id);
        if (!$page = get_record('lesson_pages', 'id', $pageid)) {
            error('Navigation: the page record not found');
        }

        if ($page->qtype == LESSON_CLUSTER) {  //this only gets called when a user starts up a new lesson and the first page is a cluster page
            if (!has_capability('mod/lesson:manage', $context)) {
                // get new id
                $pageid = lesson_cluster_jump($lesson->id, $USER->id, $pageid);
                // get new page info
                if (!$page = get_record('lesson_pages', 'id', $pageid)) {
                    error('Navigation: the page record not found');
                }
                add_to_log($course->id, 'lesson', 'view', 'view.php?id='. $cm->id, $pageid, $cm->id);
            } else {
                // get the next page
                $pageid = $page->nextpageid;
                if (!$page = get_record('lesson_pages', 'id', $pageid)) {
                    error('Navigation: the page record not found');
                }
            }
        } elseif ($page->qtype == LESSON_ENDOFCLUSTER) {
            if ($page->nextpageid == 0) {
                $nextpageid = LESSON_EOL;
            } else {
                $nextpageid = $page->nextpageid;
            }
            redirect("view.php?id=$cm->id&amp;action=navigation&amp;pageid=$nextpageid", get_string('endofclustertitle', 'lesson'));
        }


        // check to see if the user can see the left menu
        if (!has_capability('mod/lesson:manage', $context)) {
            $lesson->displayleft = lesson_displayleftif($lesson);
        }

        // start of left menu
        if ($lesson->displayleft) {
           echo '<table><tr valign="top"><td>';
           // skip navigation link
           echo '<a href="#maincontent" class="skip">'.get_string('skip', 'lesson').'</a>';
           if($firstpageid = get_field('lesson_pages', 'id', 'lessonid', $lesson->id, 'prevpageid', 0)) {
                    // print the pages
                    echo "<div class=\"leftmenu_container\">\n";
                        echo '<div class="leftmenu_title">'.get_string('lessonmenu', 'lesson')."</div>\n";
                        echo '<div class="leftmenu_courselink">';
                        echo "<a href=\"$CFG->wwwroot/course/view.php?id=$course->id\">".get_string("mainmenu", "lesson")."</a>\n";
                        echo "</div>\n";
                        echo "<div class=\"leftmenu_links\">\n";
                        lesson_print_tree_menu($lesson->id, $firstpageid, $cm->id);
                        echo "</div>\n";
                    echo "</div>\n";
            }
            if ($page->qtype == LESSON_BRANCHTABLE) {
                $width = '';
            } else {
                $width = ' width="100%" ';
            }
            echo '</td><td align="center" '.$width.'>';
            // skip to anchor
            echo '<a name="maincontent" id="maincontent" title="'.get_string('anchortitle', 'lesson').'"></a>';
        } elseif ($lesson->slideshow && $page->qtype == LESSON_BRANCHTABLE) {
            echo '<table align="center"><tr><td>';  // only want this if no left menu
        }

        // starts the slideshow div
        if($lesson->slideshow && $page->qtype == LESSON_BRANCHTABLE) {
            echo "<table align=\"center\" width=\"100%\" border=\"0\"><tr><td>\n".
                 "<div class=\"slideshow\" style=\"
                    background-color: $lesson->bgcolor;
                    height: ".$lesson->height."px;
                    width: ".$lesson->width."px;
                    \">\n";
        } else {
            echo "<table align=\"center\" width=\"100%\" border=\"0\"><tr><td>\n";
            $lesson->slideshow = false; // turn off slide show for all pages other than LESSON_BRANTCHTABLE
        }

        // This is where several messages (usually warnings) are displayed
        // all of this is displayed above the actual page

        if (!empty($lesson->mediafile)) {
            $url = '/mod/lesson/mediafile.php?id='.$cm->id;
            $options = 'menubar=0,location=0,left=5,top=5,scrollbars,resizable,width='. $lesson->mediawidth .',height='. $lesson->mediaheight;
            $name = 'lessonmediafile';
            echo '<div align="right">';
            link_to_popup_window ($url, $name, get_string('mediafilepopup', 'lesson'), '', '', get_string('mediafilepopup', 'lesson'), $options);
            helpbutton("mediafilestudent", get_string("mediafile", "lesson"), "lesson");
            echo '</div>';
        }
        // clock code
        // get time information for this user
        if(!has_capability('mod/lesson:manage', $context)) {
            if (!$timer = get_records_select('lesson_timer', "lessonid = $lesson->id AND userid = $USER->id", 'starttime')) {
                error('Error: could not find records');
            } else {
                $timer = array_pop($timer); // this will get the latest start time record
            }
        }

        $startlastseen = optional_param('startlastseen', '', PARAM_ALPHA);
        if ($startlastseen == 'yes') {  // continue a previous test, need to update the clock  (think this option is disabled atm)
            $timer->starttime = time() - ($timer->lessontime - $timer->starttime);
            $timer->lessontime = time();
        } else if ($startlastseen == 'no') {  // starting over
            // starting over, so reset the clock
            $timer->starttime = time();
            $timer->lessontime = time();
        }

        // for timed lessons, display clock
        if ($lesson->timed) {
            if(has_capability('mod/lesson:manage', $context)) {
                echo '<p align="center">'. get_string('teachertimerwarning', 'lesson') .'<p>';
            } else {
                if ((($timer->starttime + $lesson->maxtime * 60) - time()) > 0) {
                    // code for the clock
                    echo '<table align="right" width="150px" class="generaltable generalbox" cellspacing="0" cellpadding="5px" border="0" valign="top">'.
                        "<tr><th valign=\"top\" class=\"header\">".get_string("timeremaining", "lesson").
                        "</th></tr><tr><td align=\"center\" class=\"c0\">";
                    echo "<script language=\"javascript\">\n";
                        echo "var starttime = ". $timer->starttime . ";\n";
                        echo "var servertime = ". time() . ";\n";
                        echo "var testlength = ". $lesson->maxtime * 60 .";\n";
                        echo "document.write('<script type=\"text/javascript\" src=\"$CFG->wwwroot/mod/lesson/timer.js\"><\/script>');\n";
                        echo "window.onload = function () { show_clock(); }\n";
                    echo "</script>\n";
                    echo '<noscript>'.lesson_print_time_remaining($timer->starttime, $lesson->maxtime, true)."</noscript>\n";
                    echo "</td></tr></table>";
                    echo "<br /><br /><br />";
                } else {
                    redirect("view.php?id=$cm->id&amp;action=navigation&amp;pageid=".LESSON_EOL."&amp;outoftime=normal", get_string("outoftime", "lesson"));
                }
                // update clock when viewing a new page... no special treatment
                if ((($timer->starttime + $lesson->maxtime * 60) - time()) < 60) {
                    echo "<p align=\"center\">".get_string('studentoneminwarning', 'lesson')."</p>";
                }

                if ($timedflag) {
                    print_simple_box(get_string('maxtimewarning', 'lesson', $lesson->maxtime), 'center');
                }
            }
        }

        // update the clock
        if (!has_capability('mod/lesson:manage', $context)) {
            $timer->lessontime = time();
            if (!update_record('lesson_timer', $timer)) {
                error('Error: could not update lesson_timer table');
            }
        }

        if ($attemptflag) {
            print_heading(get_string('attempt', 'lesson', $retries + 1));
        }

        // before we output everything check to see if the page is a EOB, if so jump directly
        // to it's associated branch table
        if ($page->qtype == LESSON_ENDOFBRANCH) {
            if ($answers = get_records('lesson_answers', 'pageid', $page->id, 'id')) {
                // print_heading(get_string('endofbranch', 'lesson'));
                foreach ($answers as $answer) {
                    // just need the first answer
                    if ($answer->jumpto == LESSON_RANDOMBRANCH) {
                        $answer->jumpto = lesson_unseen_branch_jump($lesson->id, $USER->id);
                    } elseif ($answer->jumpto == LESSON_CLUSTERJUMP) {
                        if (!has_capability('mod/lesson:manage', $context)) {
                            $answer->jumpto = lesson_cluster_jump($lesson->id, $USER->id, $pageid);
                        } else {
                            if ($page->nextpageid == 0) {
                                $answer->jumpto = LESSON_EOL;
                            } else {
                                $answer->jumpto = $page->nextpageid;
                            }
                        }
                    } else if ($answer->jumpto == LESSON_NEXTPAGE) {
                        if ($page->nextpageid == 0) {
                            $answer->jumpto = LESSON_EOL;
                        } else {
                            $answer->jumpto = $page->nextpageid;
                        }
                    } else if ($answer->jumpto == 0) {
                        $answer->jumpto = $page->id;
                    } else if ($answer->jumpto == LESSON_PREVIOUSPAGE) {
                        $answer->jumpto = $page->prevpageid;
                    }
                    redirect("view.php?id=$cm->id&amp;action=navigation&amp;pageid=$answer->jumpto");// REMOVED: , get_string("endofbranch", "lesson")
                    break;
                }
                print_footer($course);
                exit();
            } else {
                error('Navigation: No answers on EOB');
            }
        }

         ///  This is the warning msg for teachers to inform them that cluster and unseen does not work while logged in as a teacher
        if(has_capability('mod/lesson:manage', $context)) {
            if (lesson_display_teacher_warning($lesson->id)) {
                $warningvars->cluster = get_string('clusterjump', 'lesson');
                $warningvars->unseen = get_string('unseenpageinbranch', 'lesson');
                echo '<p align="center">'. get_string('teacherjumpwarning', 'lesson', $warningvars) .'</p>';
            }
        }

        /// This calculates and prints the ongoing score
        if ($lesson->ongoing and !empty($pageid)) {
            lesson_print_ongoing_score($lesson);
        }

        if ($page->qtype == LESSON_BRANCHTABLE) {
            if ($lesson->minquestions and !has_capability('mod/lesson:manage', $context)) {
                // tell student how many questions they have seen, how many are required and their grade
                $ntries = count_records("lesson_grades", "lessonid", $lesson->id, "userid", $USER->id);

                $gradeinfo = lesson_grade($lesson, $ntries);

                if ($gradeinfo->attempts) {
                    echo "<p align=\"center\">".get_string("numberofpagesviewed", "lesson", $gradeinfo->nquestions).
                            "; (".get_string("youshouldview", "lesson", $lesson->minquestions).")<br />";
                    // count the number of distinct correct pages
                    if ($gradeinfo->nquestions < $lesson->minquestions) {
                        $gradeinfo->nquestions = $lesson->minquestions;
                    }
                    echo get_string("numberofcorrectanswers", "lesson", $gradeinfo->earned)."<br />\n";
                    echo get_string("yourcurrentgradeis", "lesson",
                            number_format($gradeinfo->grade * $lesson->grade / 100, 1)).
                        " (".get_string("outof", "lesson", $lesson->grade).")</p>\n";
                }
            }
        }

        // now starting to print the page's contents
        echo "<div align=\"center\">";
        echo "<em><strong>";
        echo format_string($lesson->name) . "</strong></em>";
        if ($page->qtype == LESSON_BRANCHTABLE) {
            echo ":<br />";
            print_heading(format_string($page->title));
        }
        echo "</div><br />";

        if (!$lesson->slideshow) {
            $options = new stdClass;
            $options->noclean = true;
            print_simple_box('<div class="contents">'.
                            format_text($page->contents, FORMAT_MOODLE, $options).
                            '</div>', 'center');
        }
        echo "<br />\n";

        // this is for modattempts option.  Find the users previous answer to this page,
        //   and then display it below in answer processing
        if (isset($USER->modattempts[$lesson->id])) {
            $retries = count_records('lesson_grades', "lessonid", $lesson->id, "userid", $USER->id);
            $retries--;
            if (! $attempts = get_records_select("lesson_attempts", "lessonid = $lesson->id AND userid = $USER->id AND pageid = $page->id AND retry = $retries", "timeseen")) {
                error("Previous attempt record could not be found!");
            }
            $attempt = end($attempts);
        }

        // get the answers in a set order, the id order
        if ($answers = get_records("lesson_answers", "pageid", $page->id, "id")) {
            echo "<form name=\"answerform\" method =\"post\" action=\"lesson.php\">";
            echo "<input type=\"hidden\" name=\"id\" value=\"$cm->id\" />";
            echo "<input type=\"hidden\" name=\"action\" value=\"continue\" />";
            echo "<input type=\"hidden\" name=\"pageid\" value=\"$pageid\" />";
            echo "<input type=\"hidden\" name=\"sesskey\" value=\"".$USER->sesskey."\" />";
            if (!$lesson->slideshow) {
                if ($page->qtype != LESSON_BRANCHTABLE) {
                    print_simple_box_start("center");
                }
                echo '<table width="100%">';
            }
            // default format text options
            $options = new stdClass;
            $options->para = false; // no <p></p>
            $options->noclean = true;
            switch ($page->qtype) {
                case LESSON_SHORTANSWER :
                case LESSON_NUMERICAL :
                    if (isset($USER->modattempts[$lesson->id])) {
                        $value = "value=\"$attempt->useranswer\"";
                    } else {
                        $value = "";
                    }
                    echo '<tr><td align="center"><label for="answer">'.get_string('youranswer', 'lesson').'</label>'.
                        ": <input type=\"text\" id=\"answer\" name=\"answer\" size=\"50\" maxlength=\"200\" $value />\n";
                    echo '</table>';
                    print_simple_box_end();
                    lesson_print_submit_link(get_string('pleaseenteryouranswerinthebox', 'lesson'), 'answerform');
                    break;
                case LESSON_TRUEFALSE :
                    shuffle($answers);
                    $i = 0;
                    foreach ($answers as $answer) {
                        echo '<tr><td valign="top">';
                        if (isset($USER->modattempts[$lesson->id]) && $answer->id == $attempt->answerid) {
                            $checked = 'checked="checked"';
                        } else {
                            $checked = '';
                        }
                        echo "<input type=\"radio\" id=\"answerid$i\" name=\"answerid\" value=\"{$answer->id}\" $checked />";
                        echo "</td><td>";
                        echo "<label for=\"answerid$i\">".format_text(trim($answer->answer), FORMAT_MOODLE, $options).'</label>';
                        echo '</td></tr>';
                        if ($answer != end($answers)) {
                            echo "<tr><td><br></td></tr>";
                        }
                        $i++;
                    }
                    echo '</table>';
                    print_simple_box_end();
                    lesson_print_submit_link(get_string('pleasecheckoneanswer', 'lesson'), 'answerform');
                    break;
                case LESSON_MULTICHOICE :
                    $i = 0;
                    shuffle($answers);

                    foreach ($answers as $answer) {
                        echo '<tr><td valign="top">';
                        if ($page->qoption) {
                            $checked = '';
                            if (isset($USER->modattempts[$lesson->id])) {
                                $answerids = explode(",", $attempt->useranswer);
                                if (in_array($answer->id, $answerids)) {
                                    $checked = ' checked="checked"';
                                } else {
                                    $checked = '';
                                }
                            }
                            // more than one answer allowed
                            echo "<input type=\"checkbox\" id=\"answerid$i\" name=\"answer[$i]\" value=\"{$answer->id}\"$checked />";
                        } else {
                            if (isset($USER->modattempts[$lesson->id]) && $answer->id == $attempt->answerid) {
                                $checked = ' checked="checked"';
                            } else {
                                $checked = '';
                            }
                            // only one answer allowed
                            echo "<input type=\"radio\" id=\"answerid$i\" name=\"answerid\" value=\"{$answer->id}\"$checked />";
                        }
                        echo '</td><td>';
                        echo "<label for=\"answerid$i\" >".format_text(trim($answer->answer), FORMAT_MOODLE, $options).'</label>';
                        echo '</td></tr>';
                        if ($answer != end($answers)) {
                            echo '<tr><td><br></td></tr>';
                        }
                        $i++;
                    }
                    echo '</table>';
                    print_simple_box_end();
                    if ($page->qoption) {
                        $linkname = get_string('pleasecheckoneormoreanswers', 'lesson');
                    } else {
                        $linkname = get_string('pleasecheckoneanswer', 'lesson');
                    }
                    lesson_print_submit_link($linkname, 'answerform');
                    break;

                case LESSON_MATCHING :
                    echo '<tr><td><table width="100%">';
                    // don't suffle answers (could be an option??)
                    foreach ($answers as $answer) {
                        // get all the response
                        if ($answer->response != NULL) {
                            $responses[] = trim($answer->response);
                        }
                    }
                    shuffle($responses);
                    $responses = array_unique($responses);

                    $responseoptions = array();
                    foreach ($responses as $response) {
                        $responseoptions[htmlspecialchars(trim($response))] = $response;
                    }

                    if (isset($USER->modattempts[$lesson->id])) {
                        $useranswers = explode(',', $attempt->useranswer);
                        $t = 0;
                    }
                    foreach ($answers as $answer) {
                        if ($answer->response != NULL) {
                            echo '<tr><td align="right">';
                            echo "<b><label for=\"menuresponse[$answer->id]\">".
                                    format_text($answer->answer,FORMAT_MOODLE,$options).
                                    '</label>: </b></td><td valign="bottom">';

                            if (isset($USER->modattempts[$lesson->id])) {
                                $selected = htmlspecialchars(trim($answers[$useranswers[$t]]->response));  // gets the user's previous answer
                                choose_from_menu ($responseoptions, "response[$answer->id]", $selected);
                                $t++;
                            } else {
                                choose_from_menu ($responseoptions, "response[$answer->id]");
                            }
                            echo '</td></tr>';
                            if ($answer != end($answers)) {
                                echo '<tr><td><br /></td></tr>';
                            }
                        }
                    }
                    echo '</table></table>';
                    print_simple_box_end();
                    lesson_print_submit_link(get_string('pleasematchtheabovepairs', 'lesson'), 'answerform');
                    break;
                case LESSON_BRANCHTABLE :
                    $options = new stdClass;
                    $options->para = false;
                    $buttons = array('next' => array(), 'prev' => array(), 'other' => array());
                /// seperate out next and previous jumps from the other jumps
                    foreach ($answers as $answer) {
                        if ($answer->jumpto == LESSON_NEXTPAGE) {
                            $type = 'next';
                        } else if ($answer->jumpto == LESSON_PREVIOUSPAGE) {
                            $type = 'prev';
                        } else {
                            $type = 'other';
                        }
                        $buttons[$type][] = '<a href="javascript:document.answerform.jumpto.value='.$answer->jumpto.';document.answerform.submit();">'.
                                strip_tags(format_text($answer->answer, FORMAT_MOODLE, $options)).'</a>';
                    }

                /// set the order and orientation (order is very important for the divs to work for horizontal!)
                    if ($page->layout) {
                        $orientation = 'horizontal';
                        $a = 'a';
                        $b = 'b';
                        $c = 'c';
                        $implode = ' ';
                        $implode2 = "\n    ";
                        if (empty($buttons['other'])) {
                            $buttons['other'][] = '&nbsp;';  // very critical! If nothing is in the middle,
                                                             // then the div style float left/right will not
                                                             // render properly with next/previous buttons
                        }
                    } else {
                        $orientation = 'vertical';
                        $a = 'c';
                        $b = 'a';
                        $c = 'b';
                        $implode = '<br /><br />';
                        $implode2 = "<br /><br />\n    ";
                    }
                    $buttonsarranged = array();
                    $buttonsarranged[$a] = '<span class="lessonbutton prevbutton prev'.$orientation.'">'.implode($implode, $buttons['prev']).'</span>';
                    $buttonsarranged[$b] = '<span class="lessonbutton nextbutton next'.$orientation.'">'.implode($implode, $buttons['next']).'</span>';
                    $buttonsarranged[$c] = '<span class="lessonbutton standardbutton standard'.$orientation.'">'.implode($implode, $buttons['other']).'</span>';
                    ksort($buttonsarranged); // sort by key

                    $fullbuttonhtml = "\n<div class=\"branchbuttoncontainer\">\n    " . implode($implode2, $buttonsarranged). "\n</div>\n";

                    if ($lesson->slideshow) {
                        echo '<div class="branchslidetop">' . $fullbuttonhtml . '</div>';
                        $options = new stdClass;
                        $options->noclean = true;
                        echo '<div class="contents">'.format_text($page->contents, FORMAT_MOODLE, $options)."</div>\n";
                        echo '</div><!--end slideshow div-->';
                        echo '<div class="branchslidebottom">' . $fullbuttonhtml . '</div>';
                    } else {
                        echo '<tr><td>';
                        print_simple_box($fullbuttonhtml, 'center');
                        echo '</td></tr></table>'; // ends the answers table
                    }
                    echo '<input type="hidden" name="jumpto" />';

                    break;
                case LESSON_ESSAY :
                    if (isset($USER->modattempts[$lesson->id])) {
                        $essayinfo = unserialize($attempt->useranswer);
                        $value = $essayinfo->answer;
                    } else {
                        $value = "";
                    }
                    echo '<tr><td align="center" valign="top" nowrap><label for="answer">'.get_string("youranswer", "lesson").'</label>:</td><td>'.
                         '<textarea id="answer" name="answer" rows="15" cols="60">'.$value."</textarea>\n";
                    echo '</td></tr></table>';
                    print_simple_box_end();
                    lesson_print_submit_link(get_string('pleaseenteryouranswerinthebox', 'lesson'), 'answerform');
                    break;
            }
            echo "</form>\n";
        } else {
            // a page without answers - find the next (logical) page
            echo "<form name=\"pageform\" method =\"post\" action=\"view.php\">\n";
            echo "<input type=\"hidden\" name=\"id\" value=\"$cm->id\" />\n";
            echo "<input type=\"hidden\" name=\"action\" value=\"navigation\" />\n";
            if ($lesson->nextpagedefault) {
                // in Flash Card mode...
                // ...first get number of retakes
                $nretakes = count_records("lesson_grades", "lessonid", $lesson->id, "userid", $USER->id);
                // ...then get the page ids (lessonid the 5th param is needed to make get_records play)
                $allpages = get_records("lesson_pages", "lessonid", $lesson->id, "id", "id,lessonid");
                shuffle ($allpages);
                $found = false;
                if ($lesson->nextpagedefault == LESSON_UNSEENPAGE) {
                    foreach ($allpages as $thispage) {
                        if (!count_records("lesson_attempts", "pageid", $thispage->id, "userid",
                                    $USER->id, "retry", $nretakes)) {
                            $found = true;
                            break;
                        }
                    }
                } elseif ($lesson->nextpagedefault == LESSON_UNANSWEREDPAGE) {
                    foreach ($allpages as $thispage) {
                        if (!count_records_select("lesson_attempts", "pageid = $thispage->id AND
                                    userid = $USER->id AND correct = 1 AND retry = $nretakes")) {
                            $found = true;
                            break;
                        }
                    }
                }
                if ($found) {
                    $newpageid = $thispage->id;
                    if ($lesson->maxpages) {
                        // check number of pages viewed (in the lesson)
                        if (count_records("lesson_attempts", "lessonid", $lesson->id, "userid", $USER->id,
                                "retry", $nretakes) >= $lesson->maxpages) {
                            $newpageid = LESSON_EOL;
                        }
                    }
                } else {
                    $newpageid = LESSON_EOL;
                }
            } else {
                // in normal lesson mode...
                if (!$newpageid = get_field("lesson_pages", "nextpageid", "id", $pageid)) {
                    // this is the last page - flag end of lesson
                    $newpageid = LESSON_EOL;
                }
            }
            echo "<input type=\"hidden\" name=\"pageid\" value=\"$newpageid\" />\n";
            echo "<p align=\"center\"><input type=\"submit\" name=\"continue\" value=\"".
                 get_string("continue", "lesson")."\" /></p>\n";
            echo "</form>\n";
        }
        lesson_print_progress_bar($lesson, $course);
        echo "</table>\n";
    } else {
        // end of lesson reached work out grade

        // check to see if the student ran out of time
        $outoftime = optional_param('outoftime', '', PARAM_ALPHA);
        if ($lesson->timed && !has_capability('mod/lesson:manage', $context)) {
            if ($outoftime == 'normal') {
                print_simple_box(get_string("eolstudentoutoftime", "lesson"), "center");
            }
        }

        // Update the clock / get time information for this user
        if (!has_capability('mod/lesson:manage', $context)) {
            unset($USER->startlesson[$lesson->id]);
            if (!$timer = get_records_select('lesson_timer', "lessonid = $lesson->id AND userid = $USER->id", 'starttime')) {
                error('Error: could not find records');
            } else {
                $timer = array_pop($timer); // this will get the latest start time record
            }
            $timer->lessontime = time();

            if (!update_record("lesson_timer", $timer)) {
                error("Error: could not update lesson_timer table");
            }
        }

        add_to_log($course->id, "lesson", "end", "view.php?id=$cm->id", "$lesson->id", $cm->id);
        print_heading(get_string("congratulations", "lesson"));
        print_simple_box_start("center");
        $ntries = count_records("lesson_grades", "lessonid", $lesson->id, "userid", $USER->id);
        if (isset($USER->modattempts[$lesson->id])) {
            $ntries--;  // need to look at the old attempts :)
        }
        if (!has_capability('mod/lesson:manage', $context)) {

            $gradeinfo = lesson_grade($lesson, $ntries);

            if ($gradeinfo->attempts) {
                if (!$lesson->custom) {
                    echo "<p align=\"center\">".get_string("numberofpagesviewed", "lesson", $gradeinfo->nquestions).
                        "</p>\n";
                    if ($lesson->minquestions) {
                        if ($gradeinfo->nquestions < $lesson->minquestions) {
                            // print a warning and set nviewed to minquestions
                            echo "<p align=\"center\">".get_string("youshouldview", "lesson",
                                    $lesson->minquestions)."</p>\n";
                        }
                    }
                    echo "<p align=\"center\">".get_string("numberofcorrectanswers", "lesson", $gradeinfo->earned).
                        "</p>\n";
                }
                $a = new stdClass;
                $a->score = $gradeinfo->earned;
                $a->grade = $gradeinfo->total;
                if ($gradeinfo->nmanual) {
                    $a->tempmaxgrade = $gradeinfo->total - $gradeinfo->manualpoints;
                    $a->essayquestions = $gradeinfo->nmanual;
                    echo "<div align=\"center\">".get_string("displayscorewithessays", "lesson", $a)."</div>";
                } else {
                    echo "<div align=\"center\">".get_string("displayscorewithoutessays", "lesson", $a)."</div>";
                }
                echo "<p align=\"center\">".get_string("gradeis", "lesson",
                        number_format($gradeinfo->grade * $lesson->grade / 100, 1)).
                    " (".get_string("outof", "lesson", $lesson->grade).")</p>\n";

                $grade->lessonid = $lesson->id;
                $grade->userid = $USER->id;
                $grade->grade = $gradeinfo->grade;
                $grade->completed = time();
                if (!$lesson->practice) {
                    if (isset($USER->modattempts[$lesson->id])) { // if reviewing, make sure update old grade record
                        if (!$grades = get_records_select("lesson_grades", "lessonid = $lesson->id and userid = $USER->id", "completed")) {
                            error("Could not find Grade Records");
                        }
                        $oldgrade = end($grades);
                        $grade->id = $oldgrade->id;
                        if (!$update = update_record("lesson_grades", $grade)) {
                            error("Navigation: grade not updated");
                        }
                    } else {
                        if (!$newgradeid = insert_record("lesson_grades", $grade)) {
                            error("Navigation: grade not inserted");
                        }
                    }
                } else {
                    if (!delete_records("lesson_attempts", "lessonid", $lesson->id, "userid", $USER->id, "retry", $ntries)) {
                        error("Could not delete lesson attempts");
                    }
                }
            } else {
                if ($lesson->timed) {
                    if ($outoftime == 'normal') {
                        $grade = new stdClass;
                        $grade->lessonid = $lesson->id;
                        $grade->userid = $USER->id;
                        $grade->grade = 0;
                        $grade->completed = time();
                        if (!$lesson->practice) {
                            if (!$newgradeid = insert_record("lesson_grades", $grade)) {
                                error("Navigation: grade not inserted");
                            }
                        }
                        echo get_string("eolstudentoutoftimenoanswers", "lesson");
                    }
                } else {
                    echo get_string("welldone", "lesson");
                }
            }
        } else {
            // display for teacher
            echo "<p align=\"center\">".get_string("displayofgrade", "lesson")."</p>\n";
        }
        print_simple_box_end(); //End of Lesson button to Continue.

        // after all the grade processing, check to see if "Show Grades" is off for the course
        // if yes, redirect to the course page
        if (!$course->showgrades) {
            redirect($CFG->wwwroot.'/course/view.php?id='.$course->id);
        }

        // high scores code
        if ($lesson->highscores && !has_capability('mod/lesson:manage', $context) && !$lesson->practice) {
            echo "<div align=\"center\"><br>";
            if ($grades = get_records_select("lesson_grades", "lessonid = $lesson->id", "completed")) {
                $madeit = false;
                if ($highscores = get_records_select("lesson_high_scores", "lessonid = $lesson->id")) {
                    // get all the high scores into an array
                    foreach ($highscores as $highscore) {
                        $grade = $grades[$highscore->gradeid]->grade;
                        $topscores[] = $grade;
                    }
                    // sort to find the lowest score
                    sort($topscores);
                    $lowscore = $topscores[0];

                    if ($gradeinfo->grade >= $lowscore || count($topscores) <= $lesson->maxhighscores) {
                        $madeit = true;
                    }
                }
                if (!$highscores or $madeit) {
                    echo '<p>'.get_string("youmadehighscore", "lesson", $lesson->maxhighscores).
                         '</p><p>
                          <form method="post" name="highscores" action="'.$CFG->wwwroot.'/mod/lesson/highscores.php">
                          <input type="hidden" name="mode" value="add" />
                          <input type="hidden" name="id" value="'.$cm->id.'" />
                          <input type="hidden" name="sesskey" value="'.sesskey().'" />
                          <p>';
                          lesson_print_submit_link(get_string('clicktopost', 'lesson'), 'highscores');
                    echo '</p>
                          </form>';
                } else {
                    echo get_string("nothighscore", "lesson", $lesson->maxhighscores)."<br>";
                }
            }
            echo "<br /><div style=\"padding: 5px;\" class=\"lessonbutton standardbutton\"><a href=\"$CFG->wwwroot/mod/lesson/highscores.php?id=$cm->id&amp;link=1\">".get_string("viewhighscores", "lesson").'</a></div>';
            echo "</div>";
        }

        if ($lesson->modattempts && !has_capability('mod/lesson:manage', $context)) {
            // make sure if the student is reviewing, that he/she sees the same pages/page path that he/she saw the first time
            // look at the attempt records to find the first QUESTION page that the user answered, then use that page id
            // to pass to view again.  This is slick cause it wont call the empty($pageid) code
            // $ntries is decremented above
            if (!$attempts = get_records_select("lesson_attempts", "lessonid = $lesson->id AND userid = $USER->id AND retry = $ntries", "timeseen")) {
                $attempts = array();
            }
            $firstattempt = current($attempts);
            $pageid = $firstattempt->pageid;
            // IF the student wishes to review, need to know the last question page that the student answered.  This will help to make
            // sure that the student can leave the lesson via pushing the continue button.
            $lastattempt = end($attempts);
            $USER->modattempts[$lesson->id] = $lastattempt->pageid;
            echo "<div align=\"center\" style=\"padding: 5px;\" class=\"lessonbutton standardbutton\"><a href=\"view.php?id=$cm->id&amp;pageid=$pageid\">".get_string("reviewlesson", "lesson")."</a></div>\n";
        } elseif ($lesson->modattempts && has_capability('mod/lesson:manage', $context)) {
            echo "<p align=\"center\">".get_string("modattemptsnoteacher", "lesson")."</p>";
        }

        if ($lesson->activitylink) {
            if ($module = get_record('course_modules', 'id', $lesson->activitylink)) {
                if ($modname = get_field('modules', 'name', 'id', $module->module))
                    if ($instance = get_record($modname, 'id', $module->instance)) {
                        echo "<div align=\"center\" style=\"padding: 5px;\" class=\"lessonbutton standardbutton\">".
                                "<a href=\"$CFG->wwwroot/mod/$modname/view.php?id=$lesson->activitylink\">".
                                get_string('activitylinkname', 'lesson', $instance->name)."</a></div>\n";
                    }
            }
        }

        echo "<div align=\"center\" style=\"padding: 5px;\" class=\"lessonbutton standardbutton\"><a href=\"../../course/view.php?id=$course->id\">".get_string("mainmenu", "lesson")."</a></div>\n"; // Back to the menu (course view).
        echo "<div align=\"center\" style=\"padding: 5px;\" class=\"lessonbutton standardbutton\"><a href=\"../../grade/index.php?id=$course->id\">".get_string("viewgrades", "lesson")."</a></div>\n"; //view grades
    }

    if ($lesson->displayleft || $lesson->slideshow) {  // this ends the table cell and table for the leftmenu or for slideshow
        echo "</td></tr></table>";
    }

/// Finish the page
    print_footer($course);

?>