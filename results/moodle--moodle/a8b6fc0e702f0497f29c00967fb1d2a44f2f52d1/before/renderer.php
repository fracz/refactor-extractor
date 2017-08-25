<?php

// This file is part of Moodle - http://moodle.org/
//
// Moodle is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Moodle is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Moodle.  If not, see <http://www.gnu.org/licenses/>.

/**
 * All workshop module renderers are defined here
 *
 * @package   mod-workshop
 * @copyright 2009 David Mudrak <david.mudrak@gmail.com>
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */

defined('MOODLE_INTERNAL') || die();

/**
 * Workshop module renderer class
 *
 * @copyright 2009 David Mudrak <david.mudrak@gmail.com>
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */
class mod_workshop_renderer extends plugin_renderer_base {

    /**
     * Returns html code for a status message
     *
     * This should be replaced by a core system of displaying messages, as for example Mahara has.
     *
     * @param string $message to display
     * @return string html
     */
    public function status_message(stdClass $message) {
        if (empty($message->text)) {
            return '';
        }
        $sty = empty($message->sty) ? 'info' : $message->sty;

        $o = $this->output->output_tag('span', array(), $message->text);
        $closer = $this->output->output_tag('a', array('href' => $this->page->url->out()),
                    get_string('messageclose', 'workshop'));
        $o .= $this->output->container($closer, 'status-message-closer');
        if (isset($message->extra)) {
            $o .= $message->extra;
        }
        return $this->output->container($o, array('status-message', $sty));
    }

    /**
     * Wraps html code returned by the allocator init() method
     *
     * Supplied argument can be either integer status code or an array of string messages. Messages
     * in a array can have optional prefix or prefixes, using '::' as delimiter. Prefixes determine
     * the type of the message and may influence its visualisation.
     *
     * @param mixed $result int|array returned by init()
     * @return string html to be echoed
     */
    public function allocation_init_result($result='') {
        $msg = new stdClass();
        if ($result === workshop::ALLOCATION_ERROR) {
            $msg = (object)array('text' => get_string('allocationerror', 'workshop'), 'sty' => 'error');
        } else {
            $msg = (object)array('text' => get_string('allocationdone', 'workshop'), 'sty' => 'ok');
        }
        $o = $this->status_message($msg);
        if (is_array($result)) {
            $o .= $this->output->output_start_tag('ul', array('class' => 'allocation-init-results'));
            foreach ($result as $message) {
                $parts  = explode('::', $message);
                $text   = array_pop($parts);
                $class  = implode(' ', $parts);
                if (in_array('debug', $parts) && !debugging('', DEBUG_DEVELOPER)) {
                    // do not display allocation debugging messages
                    continue;
                }
                $o .= $this->output->output_tag('li', array('class' => $class), $text) . "\n";
            }
            $o .= $this->output->output_end_tag('ul');
            $o .= $this->output->continue_button($this->page->url->out());
        }
        return $o;
    }

    /**
     * Display a short summary of the submission
     *
     * The passed submission object must define at least: id, title, timecreated, timemodified,
     * authorid, authorfirstname, authorlastname, authorpicture and authorimagealt
     *
     * @param stdClass $submission     The submission record
     * @param bool     $showauthorname Should the author name be displayed
     * @return string html to be echoed
     */
    public function submission_summary(stdClass $submission, $showauthorname=false) {
        global $CFG;

        $o  = '';    // output HTML code
        $classes = 'submission-summary';
        if (!$showauthorname) {
            $classes .= ' anonymous';
        }
        $o .= $this->output->container_start($classes);  // main wrapper
        $link = new html_link();
        $link->url = new moodle_url($CFG->wwwroot . '/mod/workshop/submission.php',
                                    array('cmid' => $this->page->context->instanceid, 'id' => $submission->id));
        $link->text = format_string($submission->title);
        $link->set_classes('title');
        $o .= $this->output->link($link);
        if ($showauthorname) {
            $author             = new stdClass();
            $author->id         = $submission->authorid;
            $author->firstname  = $submission->authorfirstname;
            $author->lastname   = $submission->authorlastname;
            $author->picture    = $submission->authorpicture;
            $author->imagealt   = $submission->authorimagealt;
            $userpic            = new moodle_user_picture();
            $userpic->user      = $author;
            $userpic->courseid  = $this->page->course->id;
            $userpic->url       = true;
            $userpic->size      = 35;
            $userpic            = $this->output->user_picture($userpic);
            $userurl            = new moodle_url($CFG->wwwroot . '/user/view.php',
                                            array('id' => $author->id, 'course' => $this->page->course->id));
            $a                  = new stdClass();
            $a->name            = fullname($author);
            $a->url             = $userurl->out();
            $byfullname         = get_string('byfullname', 'workshop', $a);

            $oo  = $this->output->container($userpic, 'picture');
            $oo .= $this->output->container($byfullname, 'fullname');
            $o .= $this->output->container($oo, 'author');
        }
        $created = get_string('userdatecreated', 'workshop', userdate($submission->timecreated));
        $o .= $this->output->container($created, 'userdate created');
        if ($submission->timemodified > $submission->timecreated) {
            $modified = get_string('userdatemodified', 'workshop', userdate($submission->timemodified));
            $o .= $this->output->container($modified, 'userdate modified');
        }
        $o .= $this->output->container_end(); // end of the main wrapper

        return $o;
    }

    /**
     * Displays the submission fulltext
     *
     * By default, this looks similar to a forum post.
     *
     * @param stdClass $submission     The submission data
     * @param bool     $showauthorname Should the author name be displayed
     * @return string html to be echoed
     */
    public function submission_full(stdClass $submission, $showauthorname=false) {
        global $CFG;

        $o  = '';    // output HTML code
        $classes = 'submission-full';
        if (!$showauthorname) {
            $classes .= ' anonymous';
        }
        $o .= $this->output->container_start($classes);
        $o .= $this->output->container_start('header');
        $o .= $this->output->heading(format_string($submission->title), 3, 'title');
        if ($showauthorname) {
            $author             = new stdClass();
            $author->id         = $submission->authorid;
            $author->firstname  = $submission->authorfirstname;
            $author->lastname   = $submission->authorlastname;
            $author->picture    = $submission->authorpicture;
            $author->imagealt   = $submission->authorimagealt;
            $userpic            = new moodle_user_picture();
            $userpic->user      = $author;
            $userpic->courseid  = $this->page->course->id;
            $userpic->url       = true;
            $userpic->size      = 64;
            $userpic            = $this->output->user_picture($userpic);
            $userurl            = new moodle_url($CFG->wwwroot . '/user/view.php',
                                            array('id' => $author->id, 'course' => $this->page->course->id));
            $a                  = new stdClass();
            $a->name            = fullname($author);
            $a->url             = $userurl->out();
            $byfullname         = get_string('byfullname', 'workshop', $a);
            $oo  = $this->output->container($userpic, 'picture');
            $oo .= $this->output->container($byfullname, 'fullname');

            $o .= $this->output->container($oo, 'author');
        }
        $created = get_string('userdatecreated', 'workshop', userdate($submission->timecreated));
        $o .= $this->output->container($created, 'userdate created');
        if ($submission->timemodified > $submission->timecreated) {
            $modified = get_string('userdatemodified', 'workshop', userdate($submission->timemodified));
            $o .= $this->output->container($modified, 'userdate modified');
        }
        $o .= $this->output->container_end(); // end of header

        $content = format_text($submission->content, $submission->contentformat);
        $content = file_rewrite_pluginfile_urls($content, 'pluginfile.php', $this->page->context->id,
                                                        'workshop_submission_content', $submission->id);
        $o .= $this->output->container($content, 'content');

        $o .= $this->submission_attachments($submission);

        $o .= $this->output->container_end(); // end of submission-full

        return $o;
    }

    /**
     * Renders a list of files attached to the submission
     *
     * If format==html, then format a html string. If format==text, then format a text-only string.
     * Otherwise, returns html for non-images and html to display the image inline.
     *
     * @param stdClass $submission Submission record
     * @param string format        The format of the returned string
     * @return string              HTML code to be echoed
     */
    public function submission_attachments(stdClass $submission, $format=null) {
        global $CFG;
        require_once($CFG->libdir.'/filelib.php');

        $fs     = get_file_storage();
        $ctx    = $this->page->context;
        $files  = $fs->get_area_files($ctx->id, 'workshop_submission_attachment', $submission->id);

        $outputimgs     = "";   // images to be displayed inline
        $outputfiles    = "";   // list of attachment files

        foreach ($files as $file) {
            if ($file->is_directory()) {
                continue;
            }

            $filepath   = $file->get_filepath();
            $filename   = $file->get_filename();
            $fileurl    = file_encode_url($CFG->wwwroot . '/pluginfile.php',
                                '/' . $ctx->id . '/workshop_submission_attachment/' . $submission->id . $filepath . $filename, true);
            $type       = $file->get_mimetype();
            $type       = mimeinfo_from_type("type", $type);
            $icon       = new html_image();
            $icon->src  = $this->output->pix_url(file_mimetype_icon($type));
            $icon->set_classes('icon');
            $icon->alt  = $type;
            $image      = $this->output->image($icon);

            $linkhtml   = $this->output->link($fileurl, $image) . $this->output->link($fileurl, $filename);
            $linktxt    = "$filename [$fileurl]";

            if ($format == "html") {
                // this is the same as the code in the last else-branch
                $outputfiles .= $this->output->output_tag('li', array('class' => $type), $linkhtml);

            } else if ($format == "text") {
                $outputfiles .= $linktxt . "\n";

            } else {
                if (in_array($type, array('image/gif', 'image/jpeg', 'image/png'))) {
                    $preview        = new html_image();
                    $preview->src   = $fileurl;
                    $preview->set_classes('preview');
                    $preview        = $this->output->image($preview);
                    $preview        = $this->output->link($fileurl, $preview);
                    $outputimgs    .= $this->output->container($preview);
                } else {
                    // this is the same as the code in html if-branch
                    $outputfiles .= $this->output->output_tag('li', array('class' => $type), $linkhtml);
                }
            }
        }

        if ($outputimgs) {
            $outputimgs = $this->output->container($outputimgs, 'images');
        }
        if ($format !== "text") {
            $outputfiles = $this->output->output_tag('ul', array('class' => 'files'), $outputfiles);
        }
        return $this->output->container($outputimgs . $outputfiles, 'attachments');
    }

    /**
     * Display a short summary of the example submission
     *
     * The passed submission object must define at least: id and title
     *
     * @param stdClass $data prepared by workshop::prepare_example_summary()
     * @return string html to be echoed
     */
    public function example_summary(stdClass $summary) {
        global $CFG;

        $o  = '';    // output HTML code

        // wrapping box
        $o .= $this->output->box_start('generalbox example-summary ' . $summary->status);

        // title
        $o .= $this->output->container_start('example-title');
        $link = new html_link();
        $link->url = new moodle_url($CFG->wwwroot . '/mod/workshop/exsubmission.php',
                                    array('cmid' => $this->page->context->instanceid, 'id' => $summary->example->id));
        $link->text = format_string($summary->example->title);
        $link->set_classes('title');
        $o .= $this->output->link($link);

        if ($summary->example->weight == 1) {
            // dirty hack to guess if the current user is example manager or not
            $icon = new moodle_action_icon();
            $icon->image->src = $this->pix_url('i/edit');
            $icon->image->alt = get_string('edit');
            $icon->link->url = new moodle_url($CFG->wwwroot . '/mod/workshop/exsubmission.php',
                                        array('cmid' => $this->page->context->instanceid, 'id' => $summary->example->id, 'edit' => 'on'));
            $o .= $this->output->action_icon($icon);
        }
        $o .= $this->output->container_end();

        // additional info
        if ($summary->status == 'notgraded') {
            $o .= $this->output->container(get_string('nogradeyet', 'workshop'), 'example-info nograde');
        } else {
            $o .= $this->output->container(get_string('examplegrade', 'workshop' , $summary->gradeinfo), 'example-info grade');
        }

        // button to assess
        $o .= $this->output->container($this->output->button($summary->btnform), 'example-actions');

        // end of wrapping box
        $o .= $this->output->box_end();

        return $o;
    }

    /**
     * Displays the example submission fulltext
     *
     * By default, this looks similar to a forum post.
     *
     * @param stdClass $example        The example submission data
     * @return string html to be echoed
     */
    public function example_full(stdClass $example) {
        global $CFG;

        $o  = '';    // output HTML code
        $classes = 'submission-full example';
        $o .= $this->output->container_start($classes);
        $o .= $this->output->container_start('header');
        $o .= $this->output->heading(format_string($example->title), 3, 'title');
        $created = get_string('userdatecreated', 'workshop', userdate($example->timecreated));
        $o .= $this->output->container($created, 'userdate created');
        if ($example->timemodified > $example->timecreated) {
            $modified = get_string('userdatemodified', 'workshop', userdate($example->timemodified));
            $o .= $this->output->container($modified, 'userdate modified');
        }
        $o .= $this->output->container_end(); // end of header

        $content = format_text($example->content, $example->contentformat);
        $content = file_rewrite_pluginfile_urls($content, 'pluginfile.php', $this->page->context->id,
                                                        'workshop_submission_content', $example->id);
        $o .= $this->output->container($content, 'content');

        $o .= $this->submission_attachments($example);

        $o .= $this->output->container_end(); // end of example-full

        return $o;
    }

    /**
     * Renders the user plannner tool
     *
     * @param array $plan as returned by {@link workshop::prepare_user_plan()}
     * @return string html code to be displayed
     */
    public function user_plan(array $plan) {
        if (empty($plan)) {
            throw new coding_exception('you must provide the prepared user plan to be rendered');
        }
        $table = new html_table();
        $table->set_classes('userplan');
        $table->head = array();
        $table->colclasses = array();
        $row = new html_table_row();
        $row->set_classes('phasetasks');
        foreach ($plan as $phasecode => $phase) {
            $title = $this->output->output_tag('span', array(), $phase->title);
            $actions = '';
            foreach ($phase->actions as $action) {
                switch ($action->type) {
                case 'switchphase':
                    $icon = new moodle_action_icon();
                    $icon->image->src = $this->pix_url('i/marker');
                    $icon->image->alt = get_string('switchphase', 'workshop');
                    $icon->link->url = $action->url;
                    $actions .= $this->output->action_icon($icon);
                    break;
                }
            }
            if (!empty($actions)) {
                $actions = $this->output->container($actions, 'actions');
            }
            $table->head[] = $this->output->container($title . $actions);
            $classes = 'phase' . $phasecode;
            if ($phase->active) {
                $classes .= ' active';
            } else {
                $classes .= ' nonactive';
            }
            $table->colclasses[] = $classes;
            $cell = new html_table_cell();
            $cell->text = $this->user_plan_tasks($phase->tasks);
            $row->cells[] = $cell;
        }
        $table->data = array($row);

        return $this->output->table($table);
    }

    /**
     * Renders the tasks for the single phase in the user plan
     *
     * @param stdClass $tasks
     * @return string html code
     */
    protected function user_plan_tasks(array $tasks) {
        $out = '';
        foreach ($tasks as $taskcode => $task) {
            $classes = '';
            $icon = null;
            if ($task->completed === true) {
                $classes .= ' completed';
            } elseif ($task->completed === false) {
                $classes .= ' fail';
            } elseif ($task->completed === 'info') {
                $classes .= ' info';
            }
            if (is_null($task->link)) {
                $title = $task->title;
            } else {
                $link = new html_link();
                $link->url  = $task->link;
                $link->text = $task->title;
                $title = $this->output->link($link);
            }
            $title = $this->output->container($title, 'title');
            $details = $this->output->container($task->details, 'details');
            $out .= $this->output->output_tag('li', array('class' => $classes), $title . $details);
        }
        if ($out) {
            $out = $this->output->output_tag('ul', array('class' => 'tasks'), $out);
        }
        return $out;
    }

    /**
     * Renders the workshop grading report
     *
     * Grades must be already rounded to the set number of decimals or must be null (in which later case,
     * the [[nullgrade]] string shall be displayed).
     *
     * @param stdClass $data prepared by {@link workshop::prepare_grading_report()}
     * @param stdClass $options display options object with properties ->showauthornames ->showreviewernames ->sortby ->sorthow
     *          ->showsubmissiongrade ->showgradinggrade
     * @return string html code
     */
    public function grading_report(stdClass $data, stdClass $options) {
        $grades             = $data->grades;
        $userinfo           = $data->userinfo;

        if (empty($grades)) {
            return '';
        }

        $table = new html_table();
        $table->set_classes('grading-report');

        $sortbyfirstname = $this->sortable_heading(get_string('firstname'), 'firstname', $options->sortby, $options->sorthow);
        $sortbylastname = $this->sortable_heading(get_string('lastname'), 'lastname', $options->sortby, $options->sorthow);
        if (self::fullname_format() == 'lf') {
            $sortbyname = $sortbylastname . ' / ' . $sortbyfirstname;
        } else {
            $sortbyname = $sortbyfirstname . ' / ' . $sortbylastname;
        }

        $table->head = array();
        $table->head[] = $sortbyname;
        $table->head[] = $this->sortable_heading(get_string('submission', 'workshop'), 'submissiontitle',
                $options->sortby, $options->sorthow);
        $table->head[] = $this->sortable_heading(get_string('receivedgrades', 'workshop'));
        if ($options->showsubmissiongrade) {
            $table->head[] = $this->sortable_heading(get_string('submissiongradeof', 'workshop', $data->maxgrade),
                    'submissiongrade', $options->sortby, $options->sorthow);
        }
        $table->head[] = $this->sortable_heading(get_string('givengrades', 'workshop'));
        if ($options->showgradinggrade) {
            $table->head[] = $this->sortable_heading(get_string('gradinggradeof', 'workshop', $data->maxgradinggrade),
                    'gradinggrade', $options->sortby, $options->sorthow);
        }

        $table->rowclasses  = array();
        $table->colclasses  = array();
        $table->data        = array();

        foreach ($grades as $participant) {
            $numofreceived  = count($participant->reviewedby);
            $numofgiven     = count($participant->reviewerof);

            // compute the number of <tr> table rows needed to display this participant
            if ($numofreceived > 0 and $numofgiven > 0) {
                $numoftrs       = workshop::lcm($numofreceived, $numofgiven);
                $spanreceived   = $numoftrs / $numofreceived;
                $spangiven      = $numoftrs / $numofgiven;
            } elseif ($numofreceived == 0 and $numofgiven > 0) {
                $numoftrs       = $numofgiven;
                $spanreceived   = $numoftrs;
                $spangiven      = $numoftrs / $numofgiven;
            } elseif ($numofreceived > 0 and $numofgiven == 0) {
                $numoftrs       = $numofreceived;
                $spanreceived   = $numoftrs / $numofreceived;
                $spangiven      = $numoftrs;
            } else {
                $numoftrs       = 1;
                $spanreceived   = 1;
                $spangiven      = 1;
            }

            for ($tr = 0; $tr < $numoftrs; $tr++) {
                $row = new html_table_row();
                // column #1 - participant - spans over all rows
                if ($tr == 0) {
                    $cell = new html_table_cell();
                    $cell->text = $this->grading_report_participant($participant, $userinfo);
                    $cell->rowspan = $numoftrs;
                    $cell->add_class('participant');
                    $row->cells[] = $cell;
                }
                // column #2 - submission - spans over all rows
                if ($tr == 0) {
                    $cell = new html_table_cell();
                    $cell->text = $this->grading_report_submission($participant);
                    $cell->rowspan = $numoftrs;
                    $cell->add_class('submission');
                    $row->cells[] = $cell;
                }
                // column #3 - received grades
                if ($tr % $spanreceived == 0) {
                    $idx = intval($tr / $spanreceived);
                    $assessment = self::array_nth($participant->reviewedby, $idx);
                    $cell = new html_table_cell();
                    $cell->text = $this->grading_report_assessment($assessment, $options->showreviewernames, $userinfo,
                            get_string('gradereceivedfrom', 'workshop'));
                    $cell->rowspan = $spanreceived;
                    $cell->add_class('receivedgrade');
                    if (is_null($assessment) or is_null($assessment->grade)) {
                        $cell->add_class('null');
                    } else {
                        $cell->add_class('notnull');
                    }
                    $row->cells[] = $cell;
                }
                // column #4 - total grade for submission
                if ($options->showsubmissiongrade and $tr == 0) {
                    $cell = new html_table_cell();
                    $cell->text = $this->grading_report_grade($participant->submissiongrade, $participant->submissiongradeover);
                    $cell->rowspan = $numoftrs;
                    $cell->add_class('submissiongrade');
                    $row->cells[] = $cell;
                }
                // column #5 - given grades
                if ($tr % $spangiven == 0) {
                    $idx = intval($tr / $spangiven);
                    $assessment = self::array_nth($participant->reviewerof, $idx);
                    $cell = new html_table_cell();
                    $cell->text = $this->grading_report_assessment($assessment, $options->showauthornames, $userinfo,
                            get_string('gradegivento', 'workshop'));
                    $cell->rowspan = $spangiven;
                    $cell->add_class('givengrade');
                    if (is_null($assessment) or is_null($assessment->grade)) {
                        $cell->add_class('null');
                    } else {
                        $cell->add_class('notnull');
                    }
                    $row->cells[] = $cell;
                }
                // column #6 - total grade for assessment
                if ($options->showgradinggrade and $tr == 0) {
                    $cell = new html_table_cell();
                    $cell->text = $this->grading_report_grade($participant->gradinggrade);
                    $cell->rowspan = $numoftrs;
                    $cell->add_class('gradinggrade');
                    $row->cells[] = $cell;
                }

                $table->data[] = $row;
            }
        }

        return $this->output->table($table);
    }

    /**
     * Renders a text with icons to sort by the given column
     *
     * This is intended for table headings.
     *
     * @param string $text    The heading text
     * @param string $sortid  The column id used for sorting
     * @param string $sortby  Currently sorted by (column id)
     * @param string $sorthow Currently sorted how (ASC|DESC)
     *
     * @return string
     */
    protected function sortable_heading($text, $sortid=null, $sortby=null, $sorthow=null) {
        global $PAGE;

        $out = $this->output->output_tag('span', array('class'=>'text'), $text);

        if (!is_null($sortid)) {
            $iconasc = new moodle_action_icon();
            $iconasc->image->src = $this->pix_url('t/up');
            $iconasc->image->alt = get_string('sortasc', 'workshop');
            $iconasc->image->set_classes('sort asc');
            $newurl = clone($PAGE->url);
            $newurl->params(array('sortby' => $sortid, 'sorthow' => 'ASC'));
            $iconasc->link->url = new moodle_url($newurl);

            $icondesc = new moodle_action_icon();
            $icondesc->image->src = $this->pix_url('t/down');
            $icondesc->image->alt = get_string('sortdesc', 'workshop');
            $icondesc->image->set_classes('sort desc');
            $newurl = clone($PAGE->url);
            $newurl->params(array('sortby' => $sortid, 'sorthow' => 'DESC'));
            $icondesc->link->url = new moodle_url($newurl);

            if ($sortby !== $sortid or $sorthow !== 'ASC') {
                $out .= $this->output->action_icon($iconasc);
            }
            if ($sortby !== $sortid or $sorthow !== 'DESC') {
                $out .= $this->output->action_icon($icondesc);
            }
        }
        return $out;
}

    /**
     * @param stdClass $participant
     * @param array $userinfo
     * @return string
     */
    protected function grading_report_participant(stdClass $participant, array $userinfo) {
        $userid = $participant->userid;
        $pic = new moodle_user_picture();
        $pic->user = $userinfo[$userid];
        $pic->courseid = $this->page->course->id;
        $pic->url = true;
        $pic->size = 35;

        $out  = $this->output->user_picture($pic);
        $out .= $this->output->output_tag('span', '', fullname($userinfo[$userid]));

        return $out;
    }

    /**
     * @param stdClass $participant
     * @return string
     */
    protected function grading_report_submission(stdClass $participant) {
        global $CFG;

        if (is_null($participant->submissionid)) {
            $out = $this->output->container(get_string('nosubmissionfound', 'workshop'), 'info');
        } else {
            $link = new html_link();
            $link->url = new moodle_url($CFG->wwwroot . '/mod/workshop/submission.php',
                                        array('cmid' => $this->page->context->instanceid, 'id' => $participant->submissionid));
            $link->text = format_string($participant->submissiontitle);
            $link->set_classes('title');
            $out = $this->output->link($link);
        }

        return $out;
    }

    /**
     * @todo Highlight the nulls
     * @param stdClass|null $assessment
     * @param bool $shownames
     * @param string $separator between the grade and the reviewer/author
     * @return string
     */
    protected function grading_report_assessment($assessment, $shownames, array $userinfo, $separator) {
        global $CFG;

        if (is_null($assessment)) {
            return get_string('nullgrade', 'workshop');
        }
        $a = new stdClass();
        $a->grade = is_null($assessment->grade) ? get_string('nullgrade', 'workshop') : $assessment->grade;
        $a->gradinggrade = is_null($assessment->gradinggrade) ? get_string('nullgrade', 'workshop') : $assessment->gradinggrade;
        $a->weight = $assessment->weight;
        if (is_null($assessment->gradinggradeover)) {
            $grade = get_string('formatpeergrade', 'workshop', $a);
        } else {
            $a->gradinggradeover = $assessment->gradinggradeover;
            $grade = get_string('formatpeergradeover', 'workshop', $a);
        }
        $link = new html_link();
        $link->text = $grade;
        $link->url = new moodle_url($CFG->wwwroot . '/mod/workshop/assessment.php',
                            array('asid' => $assessment->assessmentid));
        $link->set_classes('grade');
        $grade = $this->output->link($link);

        if ($shownames) {
            $userid = $assessment->userid;
            $pic = new moodle_user_picture();
            $pic->user = $userinfo[$userid];
            $pic->courseid = $this->page->course->id;
            $pic->url = true;
            $pic->size = 16;

            $name  = $this->output->user_picture($pic);
            $name .= $this->output->output_tag('span', array('class' => 'fullname'), fullname($userinfo[$userid]));
            $name  = $separator . $this->output->output_tag('span', array('class' => 'user'), $name);
        } else {
            $name = '';
        }

        return $this->output->container($grade . $name, 'assessmentdetails');
    }

    /**
     * Formats the aggreagated grades
     */
    protected function grading_report_grade($grade, $over=null) {
        $a = new stdClass();
        $a->grade = is_null($grade) ? get_string('nullgrade', 'workshop') : $grade;
        if (is_null($over)) {
            $text = get_string('formataggregatedgrade', 'workshop', $a);
        } else {
            $a->over = is_null($over) ? get_string('nullgrade', 'workshop') : $over;
            $text = get_string('formataggregatedgradeover', 'workshop', $a);
        }
        return $text;
    }

    ////////////////////////////////////////////////////////////////////////////
    // Helper methods                                                         //
    ////////////////////////////////////////////////////////////////////////////

    /**
     * Helper function returning the n-th item of the array
     *
     * @param array $a
     * @param int   $n from 0 to m, where m is th number of items in the array
     * @return mixed the $n-th element of $a
     */
    protected static function array_nth(array $a, $n) {
        $keys = array_keys($a);
        if ($n < 0 or $n > count($keys) - 1) {
            return null;
        }
        $key = $keys[$n];
        return $a[$key];
    }

    /**
     * Tries to guess the fullname format set at the site
     *
     * @return string fl|lf
     */
    protected static function fullname_format() {
        $fake = new stdClass(); // fake user
        $fake->lastname = 'LLLL';
        $fake->firstname = 'FFFF';
        $fullname = get_string('fullnamedisplay', '', $fake);
        if (strpos($fullname, 'LLLL') < strpos($fullname, 'FFFF')) {
            return 'lf';
        } else {
            return 'fl';
        }
    }

}