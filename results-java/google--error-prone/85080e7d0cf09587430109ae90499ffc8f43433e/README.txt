commit 85080e7d0cf09587430109ae90499ffc8f43433e
Author: Alex Eagle <alexeagle@google.com>
Date:   Tue May 8 10:19:06 2012 -0700

    Improve the UI slightly, by moving the suggested fix to a new line. It seems that the javac error printer somehow puts the code snippet on a line in between:

    refactors/empty_if_statement/PositiveCases.java:27: error: [EmptyIf] Empty statement after if
        if (i == 10); {
        ^
      did you mean 'if (i == 10) {'?