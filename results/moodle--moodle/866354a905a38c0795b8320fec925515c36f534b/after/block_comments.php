<?php

require_once($CFG->dirroot . '/comment/lib.php');

class block_comments extends block_base {

    function init() {
        $this->title = get_string('comments');
        $this->version = 2009072000;
    }

    function specialization() {
        // require js for commenting
        comment::init();
    }
    function applicable_formats() {
        return array('all' => true);
    }

    function instance_allow_multiple() {
        return false;
    }

    function get_content() {
        global $CFG;
        if (!$CFG->usecomments) {
            $this->content->text = '';
            if ($this->page->user_is_editing()) {
                $this->content->text = get_string('disabledcomments');
            }
            return $this->content;
        }
        if ($this->content !== NULL) {
            return $this->content;
        }
        if (empty($this->instance)) {
            return null;
        }
        $this->content->footer = '';
        $this->content->text = '';
        //TODO: guest and not-logged-in shoudl be able to read comments, right?
        if (isloggedin() && !isguestuser()) {   // Show the block
            list($context, $course, $cm) = get_context_info_array($this->context->id);
            $cmt = new stdclass;
            $cmt->context   = $context;
            $cmt->course    = $course;
            $cmt->area      = 'block_comments';
            $cmt->itemid    = $this->instance->id;
            // this is a hack to adjust commenting UI in block_comments
            $cmt->env       = 'block_comments';
            $cmt->linktext  = get_string('showcomments');
            $comment = new comment($cmt);

            $this->content = new stdClass;
            $this->content->text = $comment->output(true);
            $this->content->footer = '';
        }
        return $this->content;
    }
}