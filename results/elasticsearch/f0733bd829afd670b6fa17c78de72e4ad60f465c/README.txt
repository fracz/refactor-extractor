commit f0733bd829afd670b6fa17c78de72e4ad60f465c
Author: Robert Muir <rmuir@apache.org>
Date:   Mon Sep 21 15:13:17 2015 -0400

    Get lang-javascript, lang-python, securemock ready for script refactoring.

    I want to refactor scripting engines so we can contain dangerous "God-like" permissions
    like createClassloader/sun.reflect. These are used for dynamic class generation (scripts, mocks).
    This will mean some refactoring to ES core.

    But first lets get the plugins in order first. I removed those permissions globally, and
    fixed grants for lang-javascript, lang-python, securemock so that everything works.

    lang-javascript needs no code changes, because rhino is properly written :)
    lang-python needs accesscontroller blocks. securemock was already working as of 1.1

    This is just a baby step, to try to do some of this incrementally! It doesn't yet provide
    us anything.