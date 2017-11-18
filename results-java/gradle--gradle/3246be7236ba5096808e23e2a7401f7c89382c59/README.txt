commit 3246be7236ba5096808e23e2a7401f7c89382c59
Author: adammurdoch <a@rubygrapefruit.net>
Date:   Mon Dec 14 17:09:02 2009 +1100

    - Much improved detection of source script file and line number for exceptions.
    - Extracted ExceptionAnalyser from GradleScriptException.
    - Added some specific exception classes for some common failures.
    - Added more int test coverage for exception handling