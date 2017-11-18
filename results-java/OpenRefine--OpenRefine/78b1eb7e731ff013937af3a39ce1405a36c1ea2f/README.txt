commit 78b1eb7e731ff013937af3a39ce1405a36c1ea2f
Author: David Huynh <dfhuynh@gmail.com>
Date:   Sat Mar 6 07:43:45 2010 +0000

    Major refactoring:
    - Made all Change classes save to and load from .zip files.
    - Changed Column.headerLabel to Column.name.
    - Save project's raw data to "raw-data" file for now. We'll make it save to a zip file next.


    git-svn-id: http://google-refine.googlecode.com/svn/trunk@217 7d457c2a-affb-35e4-300a-418c747d4874