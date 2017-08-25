commit 08d7babffe1bded4620d0a3624bdd80522283138
Author: Michael Klier <chi@chimeric.de>
Date:   Tue Mar 30 11:15:08 2010 +0200

    added support for plugin unittests

            This patch adds support to include plugin tests in the
            DokuWiki testsuite. Plugin tests are located in a dedicated
            directory _test/within a plugin directory. The naming
            convention of the test files follows the one used in
            DokuWikis testsuite.

                    <plugin>/_test/*.test.php       -> single test
                    <plugin>/_test/*.group.php  -> group test

            The plugin tests are accessible via the web interface
            of the test suite and via the cli interface. It is recommend
            to bundle plugin test in a plugin group test. The webinterface
            also allows to run all plugin tests at once.

            Test files must include:

                    <dokuwiki>/_test/lib/unittest.php

            Example Test:

            require_once(DOKU_INC.'_test/lib/unittest.php');
            class plugin_test extends Doku_UnitTestCase {
                    function test() {
                            $this->assertEqual(1,1);
                    }
            }

            Example Group Test:

            require_once(DOKU_INC.'_test/lib/unittest.php');
            class plugin_group_test extends Doku_GroupTest {
                    function group_test() {
                            $dir = dirname(__FILE__).'/';
                            $this->GroupTest('plugin_grouptest');
                            $this->addTestFile($dir . 'plugin.test1.php');
                            $this->addTestFile($dir . 'plugin.test2.php');
                            $this->addTestFile($dir . 'plugin.test3.php');
                    }
            }

            At the moment unittest.php contains only two
            meta classes so plugins tests don't have to inherit
            from the simpletest classes.

            This patch should be treated as intermediate step to
            allow for plugin tests. The testsuite wasn't designed
            to include plugin tests. It should probably be refactored
            at a later point.