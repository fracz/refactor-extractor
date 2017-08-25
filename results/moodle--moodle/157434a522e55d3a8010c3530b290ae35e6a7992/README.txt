commit 157434a522e55d3a8010c3530b290ae35e6a7992
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Mon Nov 1 17:44:59 2010 +0000

    quiz/question javascript MDL-24170 Enter in a shortanswer question preview should not flag the question.
    That bit of JavaScript from mod/quiz/attempt.php is needed in question preview too, so refactor a bit.

    I was disappointed to find that the
    $PAGE->requires->js_module('core_question_engine');
    line was needed in quiz_get_js_module, but it seems to be.

    Also change non-Moodle-y string "End test..." to "Finish attempt..."