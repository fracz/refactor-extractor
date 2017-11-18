commit 100cbee7937203225ae3c701d2e6fa218f6c3ce1
Author: Jim Webber <jim@neotechnology.com>
Date:   Wed Nov 3 23:43:18 2010 +0000

    Did some refactoring work to simplify how we deploy onto underlying Web servers.

    At the moment, we still can't deploy static content to / without other things breaking. This means the webadmin needs to be mounted somewhere like /webadmin

    git-svn-id: https://svn.neo4j.org/components/server/trunk@6653 0b971d98-bb2f-0410-8247-b05b2b5feb2a