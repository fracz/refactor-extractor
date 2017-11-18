commit 4921aeafc3f4f621f97f120c0f36c5211272b18c
Author: James Strachan <james.strachan@gmail.com>
Date:   Tue May 29 17:28:15 2012 +0100

    refactored the maven plugin to use the vanilla K2JSCompiler directly and avoid using the K2JVMCompiler when generating JS code; also included a default LibrarySourceConfig which detects JS library code on the classpath which works nicer in maven/ant style worlds where dependencies tend to be specified rather than file paths to zip files