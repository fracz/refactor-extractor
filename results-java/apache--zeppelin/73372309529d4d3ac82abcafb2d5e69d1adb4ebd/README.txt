commit 73372309529d4d3ac82abcafb2d5e69d1adb4ebd
Author: Nelson Costa <nelson.costa85@gmail.com>
Date:   Sun Jun 11 10:10:14 2017 +0100

    [ZEPPELIN-2592] Ensure open stream is closed

    ### What is this PR for?
    Fix for missing close statement on input streams found on Helium Bundle factory

    ### What type of PR is it?
    Bug Fix

    ### What is the Jira issue?
    * https://issues.apache.org/jira/browse/ZEPPELIN-2592

    ### How should this be tested?
    NA

    ### Screenshots (if appropriate)

    ### Questions:
    * Does the licenses files need update? N
    * Is there breaking changes for older versions? N
    * Does this needs documentation? N

    Author: Nelson Costa <nelson.costa85@gmail.com>

    Closes #2397 from necosta/zeppelin2592 and squashes the following commits:

    003321e [Nelson Costa] Revert "ZEPPELIN-2592] Fixed bug on target directory"
    2c26650 [Nelson Costa] ZEPPELIN-2592] Fixed bug on target directory
    2c8d83c [Nelson Costa] [ZEPPELIN-2592] Ensure open stream is closed + refactoring
    89e8c4c [Nelson Costa] [ZEPPELIN-2592] Ensure open stream is closed