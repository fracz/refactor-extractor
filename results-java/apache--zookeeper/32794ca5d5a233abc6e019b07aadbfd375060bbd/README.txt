commit 32794ca5d5a233abc6e019b07aadbfd375060bbd
Author: Michael Han <hanm@apache.org>
Date:   Thu Jul 27 14:09:10 2017 -0700

    ZOOKEEPER-2577: Fix flaky testDuringLeaderSync test.

    testDuringLeaderSync uses the presence of intermediate zoo.cfg.dynamic file to decide if the reconfig operation was succeeded or not. This is not a problem and is logically correct, however in tests that provisions QuorumPeer directly through MainThread, the configFile will be null and the resulted intermediate dynamic reconfig file will has a name of "null.cfg.dynamic". This causes problem because multiple test cases use MainThread to provision QuorumPeer so these tests will share a single "null.cfg.dynamic" file, as opposed to the zoo.cfg.dynamic file in their specific test folder when configFile was not null. Since we support running concurrent ant unit tests in apache build infrastructure, it is highly likely that multiple tests that rely on this shared null.cfg.dynamic file will execute simultaneously which create all sorts of failure cases (this also explains why if one tries to reproduce the test failures in a standalone fashion the test will always pass, with the absence of the file sharing.).

    This is problematic in multiple test cases and in particular cause this testDuringLeaderSync fail fairly frequently. There are multiple solutions to this problem:

    * Implement retry with timeout logic when detecting the presence of intermediate config files in testDuringLeaderSync. This will fix this specific test but the fix is non-deterministic and other tests might still fail because of sharing of null.cfg.dynamic file.

    * Always make sure to to feed a real config file when provision QuorumPeer. This however is an overkill as some cases a pure artificial QuorumPeer w/o real config file fit the use case well.

    The approach taking here is to for this specific test, making sure we always have a configured confFileName, and it is pretty trivial to do so. For other tests, where the intermediate config file name is not important or irrelevant (e.g. FLE related tests), they will still have null confFileName because they directly provision QuorumPeer during test.

    Testing done: running this patch on apache jenkins for the past week with 500 runs. Not only this test is fixed but overall stability of entirely unit tests are improved.

    Author: Michael Han <hanm@apache.org>

    Reviewers: Alexander Shraer <shralex@gmail.com>

    Closes #315 from hanm/working