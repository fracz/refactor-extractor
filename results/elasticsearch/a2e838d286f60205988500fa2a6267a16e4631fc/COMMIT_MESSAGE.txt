commit a2e838d286f60205988500fa2a6267a16e4631fc
Author: Ryan Ernst <ryan@iernst.net>
Date:   Sat Mar 11 22:12:48 2017 -0800

    Build: Give better output for java version introspection (#23547)

    This commit improves the output when jrunscript fails to include the
    full output of the command. It also makes the quoting that is needed for
    windows only happen on windows (which worked on java 8, but for some
    reason does not work with java 9)