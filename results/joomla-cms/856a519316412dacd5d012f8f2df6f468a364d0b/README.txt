commit 856a519316412dacd5d012f8f2df6f468a364d0b
Author: Frank Mayer <frank@frankmayer.net>
Date:   Sun Jun 11 14:33:15 2017 +0300

    Moved some things outside loops, project-wide. (#13249)

    * Moved some things outside loops.

    * Converted  if-else to ternary

    * Missed an optimization

    * Reverted this one to not clash with @laoneo's refactoring efforts

    * CS-change according to @izharaazmi's comment