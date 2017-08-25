commit 0149e6c713ead8ac1f0ce552c5fd477cd4fb5d6b
Author: Eloy Lafuente (stronk7) <stronk7@moodle.org>
Date:   Wed Feb 16 19:13:56 2011 +0100

    MDL-26405 restore - dispatch able to skip branches

    after this change any restore_structure_step processor
    method is able to instruct the dispatcher about to skip
    any path below it. Until now, we were doing the checks on
    each child processor method, but that was inneficient and
    prone to errors (easy to miss the check in a child so some
    orphaned piezes of restore may be causing mess here and there).
    Once implemented, it's simlpy a matter of the parent deciding if
    all its children must be processed or no. Easier for developers
    and also small speed improvement because avoids unnecesary
    dispatching/processing to happen.

    Surely only will be used in parts of core, like in question_categories,
    saving 50-60 sub processors (sub-paths) to be dispatched.