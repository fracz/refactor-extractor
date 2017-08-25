<?php
/**
 * Export quiz questions into the given category
 *
 * @author Martin Dougiamas, Howard Miller, and many others.
 *         {@link http://moodle.org}
 * @license http://www.gnu.org/copyleft/gpl.html GNU Public License
 * @package questionbank
 * @subpackage importexport
 */

    require_once("../config.php");
    require_once("editlib.php");
    require_once("export_form.php");

    list($thispageurl, $contexts, $cmid, $cm, $module, $pagevars) = question_edit_setup('export');


    // get display strings
    $strexportquestions = get_string('exportquestions', 'quiz');

    // make sure we are using the user's most recent category choice
    if (empty($categoryid)) {
        $categoryid = $pagevars['cat'];
    }

    // ensure the files area exists for this course
    make_upload_directory("$COURSE->id");
    list($catid, $catcontext) = explode(',', $pagevars['cat']);
    if (!$category = $DB->get_record("question_categories", array("id" => $catid, 'contextid' => $catcontext))) {
        print_error('nocategory','quiz');
    }

    /// Header
    $PAGE->set_url($thispageurl->out());
    $PAGE->set_title($strexportquestions);
    if ($cm!==null) {
        $PAGE->navbar->add($strexportquestions);
        echo $OUTPUT->header();
        $currenttab = 'edit';
        $mode = 'export';
        ${$cm->modname} = $module;
        include($CFG->dirroot."/mod/$cm->modname/tabs.php");
    } else {
        // Print basic page layout.
        $PAGE->navbar->add($strexportquestions);
        echo $OUTPUT->header();
        // print tabs
        $currenttab = 'export';
        include('tabs.php');
    }

    $exportfilename = default_export_filename($COURSE, $category);
    $export_form = new question_export_form($thispageurl, array('contexts'=>$contexts->having_one_edit_tab_cap('export'), 'defaultcategory'=>$pagevars['cat'],
                                    'defaultfilename'=>$exportfilename));


    if ($from_form = $export_form->get_data()) {   /// Filename


        if (! is_readable("format/$from_form->format/format.php")) {
            print_error('unknowformat', '', '', $from_form->format);
        }

        // load parent class for import/export
        require_once("format.php");

        // and then the class for the selected format
        require_once("format/$from_form->format/format.php");

        $classname = "qformat_$from_form->format";
        $qformat = new $classname();
        $qformat->setContexts($contexts->having_one_edit_tab_cap('export'));
        $qformat->setCategory($category);
        $qformat->setCourse($COURSE);

        if (empty($from_form->exportfilename)) {
            $from_form->exportfilename = default_export_filename($COURSE, $category);
        }
        $qformat->setFilename($from_form->exportfilename);
        $canaccessbackupdata = has_capability('moodle/site:backup', $contexts->lowest());
        $qformat->set_can_access_backupdata($canaccessbackupdata);
        $qformat->setCattofile(!empty($from_form->cattofile));
        $qformat->setContexttofile(!empty($from_form->contexttofile));

        if (! $qformat->exportpreprocess()) {   // Do anything before that we need to
            print_error('exporterror', 'question', $thispageurl->out());
        }

        if (! $qformat->exportprocess()) {         // Process the export data
            print_error('exporterror', 'question', $thispageurl->out());
        }

        if (! $qformat->exportpostprocess()) {                    // In case anything needs to be done after
            print_error('exporterror', 'question', $thispageurl->out());
        }
        echo "<hr />";

        // link to download the finished file
        $file_ext = $qformat->export_file_extension();
        $filename = $from_form->exportfilename . $file_ext;
        if ($canaccessbackupdata) {
            $efile = get_file_url($qformat->question_get_export_dir() . '/' . $filename,
                    array('forcedownload' => 1));
            echo '<p><div class="boxaligncenter"><a href="' . $efile . '">' .
                    get_string('download', 'quiz') . '</a></div></p>';
            echo '<p><div class="boxaligncenter"><font size="-1">' .
                    get_string('downloadextra', 'quiz') . '</font></div></p>';
        } else {
            $efile = get_file_url($filename, null, 'questionfile');
            echo '<p><div class="boxaligncenter">' .
                    get_string('yourfileshoulddownload', 'question', $efile) . '</div></p>';
            $PAGE->requires->js_function_call('document.location.replace', array($efile))->after_delay(1);
        }

        echo $OUTPUT->continue_button('edit.php?' . $thispageurl->get_query_string());
        echo $OUTPUT->footer();
        exit;
    }

    /// Display export form
    echo $OUTPUT->heading_with_help($strexportquestions, 'export', 'quiz');

    $export_form->display();

    echo $OUTPUT->footer();
