commit 6169cf7a3eaaef7222f6ac51b4a7fc0261dceb00
Author: Denis.Zhdanov <Denis.Zhdanov@jetbrains.com>
Date:   Mon May 14 10:43:53 2012 +0400

    IDEA-19061 Integrate the Rearranger-plugin into core-IDEA

    Initial bundling iteration. The changes to the initial version available at http://java.net/projects/rearranger/sources:
    * switched from log4j to IJ logging;
    * 'Rearranger' component is service now;
    * plugin descriptor is adapted to the common IJ scheme (id, vendor etc);
    * IJ project is configured to have new 'rearranger' module;
    * build script is adjusted to process 'rearranger' plugin as well;
    * almost all tests are commented and their refactoring is started;