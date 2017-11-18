commit 2d391918b0bac3e37b979ac0e2dda026f2328615
Author: Tobias Ivarsson <tobias.ivarsson@neotechnology.com>
Date:   Wed Jan 26 21:28:53 2011 +0000

    Refactored the HA test classes:
     * Test cases are now better isolated. Failure in one case should not affect others.
     * All tests that execute code in multiple processes now use the SubProcess abstraction.
       * This provides better tracability for (failing) test cases by giving access to stdout/stderr of all subprocesses.
       * Each line of output is identified by the name of the testcase and the pid of the subprocesses.
     * Removed most Thread.sleep() calls from the test code.
     * Added a log4j.xml file in the test resources to get tracability of failures in ZooKeeper.
    Also refactored HAGDb (in MasterServer) to fail early if unable to bind its communication port.


    git-svn-id: https://svn.neo4j.org/components/ha/trunk/src@8462 0b971d98-bb2f-0410-8247-b05b2b5feb2a