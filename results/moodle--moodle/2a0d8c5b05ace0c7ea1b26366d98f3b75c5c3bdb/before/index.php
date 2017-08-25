<?PHP // $Id$

    require_once("../../config.php");
    require_once("lib.php");

    require_variable($id);   // course

    if (! $course = get_record("course", "id", $id)) {
        error("Course ID is incorrect");
    }

    require_login($course->id);
    add_to_log($course->id, "journal", "view all", "index.php?id=$course->id", "");

    if ($course->category) {
        $navigation = "<A HREF=\"../../course/view.php?id=$course->id\">$course->shortname</A> ->";
    }

    $strjournal = get_string("modulename", "journal");
    $strjournals = get_string("modulenameplural", "journal");
    $stredit = get_string("edit");
    $strview = get_string("view");
    $strweek = get_string("week");
    $strtopic = get_string("topic");
    $strquestion = get_string("question");
    $stranswer = get_string("answer");

    print_header("$course->shortname: $strjournals", "$course->fullname", "$navigation $strjournals",
                 "", "", true, "", navmenu($course));


    if (! $journals = get_all_instances_in_course("journal", $course)) {
        notice("There are no journals", "../../course/view.php?id=$course->id");
        die;
    }

    $timenow = time();

    if ($course->format == "weeks") {
        $table->head  = array ($strweek, $strquestion, $stranswer);
        $table->align = array ("CENTER", "LEFT", "LEFT");
    } else if ($course->format == "topics") {
        $table->head  = array ($strtopic, $strquestion, $stranswer);
        $table->align = array ("CENTER", "LEFT", "LEFT");
    } else {
        $table->head  = array ($strquestion, $stranswer);
        $table->align = array ("LEFT", "LEFT");
    }

    foreach ($journals as $journal) {

        $journal->timestart  = $course->startdate + (($journal->section - 1) * 608400);
        if (!empty($journal->daysopen)) {
            $journal->timefinish = $journal->timestart + (3600 * 24 * $journal->daysopen);
        } else {
            $journal->timefinish = 9999999999;
        }
        $journalopen = ($journal->timestart < $timenow && $timenow < $journal->timefinish);

        $entrytext = get_field("journal_entries", "text", "userid", $USER->id, "journal", $journal->id);

        $text = text_to_html($entrytext)."<p align=right><a href=\"view.php?id=$journal->coursemodule\">";

        if ($journalopen) {
            $text .= "$stredit</a></p>";
        } else {
            $text .= "$strview</a></p>";
        }
        if (!empty($journal->section)) {
            $section = "$journal->section";
        } else {
            $section = "";
        }
        if ($course->format == "weeks" or $course->format == "topics") {
            $table->data[] = array ($section,
                                    text_to_html($journal->intro),
                                    $text);
        } else {
            $table->data[] = array (text_to_html($journal->intro),
                                    $text);
        }
    }

    echo "<br />";

    print_table($table);

    print_footer($course);

?>
