<?PHP // $Id$

    require("../config.php");
    require("lib.php");

    require_variable($id);    // Week ID

    if (! $section = get_record("course_sections", "id", $id)) {
        error("Course section is incorrect");
    }

    if (! $course = get_record("course", "id", $section->course)) {
        error("Could not find the course!");
    }

    require_login($course->id);

    if (!isteacher($course->id)) {
        error("Only teachers can edit this!");
    }


/// If data submitted, then process and store.

    if (match_referer() && isset($HTTP_POST_VARS)) {

        $timenow = time();

        if (! set_field("course_sections", "summary", $summary, "id", $section->id)) {
            error("Could not update the summary!");
        }

        add_to_log($course->id, "course", "editsection", "editsection.php?id=$section->id", "$section->section");

        redirect("view.php?id=$course->id");
        exit;
    }

/// Otherwise fill and print the form.

    if (! $form ) {
        $form = $section;
    }

    $sectionname = $COURSE_SECTION[$course->format];

    print_header("Edit $sectionname $section->section", "Edit $sectionname $section->section", "", "form.summary");

    include("editsection.html");

    print_footer($course);

?>