commit e84a85517a43aa5dbc4b6a599aec4b15d8210fca
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Mon Jul 9 21:46:17 2012 +0200

    improved "violations found" message of test and code quality plugins

    - report paths are now displayed as file URLs to get console support
    - print "violations found" message even if task is configured not to fail the build