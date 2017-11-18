commit 92cd7a6f0ef94b74e96d587121baf48428457968
Author: Eric Wendelin <eric@gradle.com>
Date:   Thu Oct 20 16:33:39 2016 -0700

    Stabilize ZincScalaCompilerFactory and improve testing

    - Moved ZincCompilerServices to be private inner class
    - Fixed failures in ScalaCompileParallelIntegrationTest
    - Fixed typo "coping" => "copying"
    - Update Zinc compiler interface JAR name. File name started
      with compiler-interface-compiler-interface-
    - Avoid leaking files when generating Zinc interface JAR
    - Acquire lock when trying to get pre-existing zinc interface JAR.
    This prevents returning a partially-copied JAR.
    - Create temp file under zinc cache dir so we can verify that no
    files are left behind after creating compiler interface JAR.
    - Wrap Zinc cache closure in try/finally to avoid leaving it open
    if an exception is thrown in operations before it's closed.
    - Print warning if compiler interface JAR takes over 30 seconds to
    generate.

    Issue: #707, #733, #734