commit 7831b924276079250c078371502ffefbdf4c43d5
Author: epriestley <git@epriestley.com>
Date:   Thu Dec 29 14:23:54 2011 -0800

    Improve LiskDAO::__call() performance

    Summary:
    This is kind of expensive and can be significant on, e.g., the
    Maniphest task list view. Do a little more caching and some clever nonsense to
    improve performance.

    Test Plan:
    Local cost on Maniphest "all tasks" view for this method dropped from
    ##82,856us## to ##24,607us## on 9,061 calls.

    I wrote some unit test / microbenchmark things:

      public function testGetIDCost() {
        $u = new PhabricatorUser();
        $n = 100000;
        while ($n--) {
          $u->getID();
        }
        $this->assertEqual(1, 1);
      }

      public function testGetCost() {
        $u = new PhabricatorUser();
        $n = 100000;
        while ($n--) {
          $u->getUsername();
        }
        $this->assertEqual(1, 1);
      }

      public function testSetCost() {
        $u = new PhabricatorUser();
        $n = 100000;
        while ($n--) {
          $u->setID(1);
        }
        $this->assertEqual(1, 1);
      }

    Before:

       PASS  598ms   testSetCost
       PASS  584ms   testGetCost
       PASS  272ms   testGetIDCost

    After:

       PASS  170ms   testSetCost
       PASS  207ms   testGetCost
       PASS   29ms   testGetIDCost

    Also, ran unit tests.

    Reviewers: nh, btrahan, jungejason

    Reviewed By: nh

    CC: aran, epriestley, nh

    Differential Revision: https://secure.phabricator.com/D1291