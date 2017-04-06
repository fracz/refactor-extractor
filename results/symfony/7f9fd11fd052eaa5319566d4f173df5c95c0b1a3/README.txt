commit 7f9fd11fd052eaa5319566d4f173df5c95c0b1a3
Merge: d100ffa bb138da
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jul 9 15:29:00 2012 +0200

    merged branch asm89/refactor-authentication-success-handling (PR #4599)

    Commits
    -------

    bb138da [Security] Fix regression after rebase. Target url should be firewall dependent
    eb19f2c [Security] Add note to CHANGELOG about refactored authentication failure/success handling [Security] Various CS + doc fixes [Security] Exception when authentication failure/success handlers do not return a response [Security] Add authors + fix docblock
    f9d5606 [Security] Update AuthenticationFailureHandlerInterface docblock. Never return null
    915704c [Security] Move default authentication failure handling strategy to seperate class [Security] Update configuration for changes regarding default failure handler [Security] Fixes + add AbstractFactory test for failure handler
    c6aa392 [Security] Move default authentication success handling strategy to seperate class [Security] Update configuration for changes regarding default success handler [Security] Fix + add AbstractFactory test

    Discussion
    ----------

    [Security] Refactor authentication success handling

    Bug fix: no
    Feature addition: no
    Backwards compatibility break: yes
    Symfony2 tests pass: [![Build Status](https://secure.travis-ci.org/asm89/symfony.png?branch=refactor-authentication-success-handling)](http://travis-ci.org/asm89/symfony)
    License of the code: MIT

    This PR extracts the default authentication success handling to its own class as discussed in #4553. In the end the PR will basically revert #3183 (as suggested by @schmittjoh) and fix point one of #838.

    There are a few noticeable changes in this PR:
    - This implementation changes the constructor signature of the `AbstractAuthentictionListener` and `UsernamePasswordFormAuthenticationListener` by making the `AuthenticationSuccessHandler` mandatory (BC break). If this WIP is approved I will refactor the failure handling logic too and then this will also move one place in the constructor
    - This PR reverts the change of making the returning of a `Response` optional in the `AuthenticationSuccessHandlerInterface`. Developers can now extend the default behavior themselves

    @schmittjoh Any suggestions? Or a +1 to do the failure logic too?

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-06-17T23:53:07Z

    +1 from me

    @fabpot, what so you think?

    ---------------------------------------------------------------------------

    by fabpot at 2012-06-19T08:15:48Z

    Can you add a note in the CHANGELOG? Thanks.

    ---------------------------------------------------------------------------

    by asm89 at 2012-06-19T10:22:20Z

    I will, but I'll first do the same for the failure logic.

    ---------------------------------------------------------------------------

    by travisbot at 2012-06-21T08:03:14Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1671555) (merged 17c8f66f into 55c6df99).

    ---------------------------------------------------------------------------

    by asm89 at 2012-06-21T08:45:38Z

    :+1: thank you @stof. I think this is good to go now.

    ---------------------------------------------------------------------------

    by travisbot at 2012-06-21T08:50:28Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1671817) (merged 8982c769 into 55c6df99).

    ---------------------------------------------------------------------------

    by asm89 at 2012-06-21T14:23:58Z

    @schmittjoh @fabpot The `LogoutListener` currently throws an exception when the successhandler doesn't return a `Response` ([link](https://github.com/symfony/symfony/blob/9e9519913d2c5e2bef96070bcb9106e1e389c3bd/src/Symfony/Component/Security/Http/Firewall/LogoutListener.php#L101)). Should this code check for this too?

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-06-21T14:26:49Z

    Yes, this code was removed, but needs to be re-added here as well.

    ---------------------------------------------------------------------------

    by travisbot at 2012-06-21T15:08:59Z

    This pull request [passes](http://travis-ci.org/symfony/symfony/builds/1674437) (merged 5afa240d into 55c6df99).

    ---------------------------------------------------------------------------

    by asm89 at 2012-06-26T06:01:02Z

    @fabpot Can you make a final decision on this? If you decide on point 3, this code can be merged.  I agree with the arguments of @stof about the option handling and it 'only' being a BC break for direct users of the security component. I even think these direct users should be really careful anyway, since the behavior of the success and failurehandlers now change back to how they acted in 2.0.

    Now I am thinking about it, can't the optional parameters of this class move to setters anyway? That will make it cleaner to extend.

    ---------------------------------------------------------------------------

    by asm89 at 2012-06-28T10:29:50Z

    ping @fabpot

    ---------------------------------------------------------------------------

    by fabpot at 2012-06-28T17:23:02Z

    I'm ok with option 1 (the BC break). After doing the last changes, can you squash your commits before I merge? Thanks.

    ---------------------------------------------------------------------------

    by asm89 at 2012-07-06T21:59:54Z

    @fabpot I rebased the PR, added the authors and also ported the fix that was done in 8ffaafa86741a03ecb2f91e3d67802f4c6baf36b to be contained in the default success handler. I also squashed all the CS and 'small blabla fix' commits. Is it ok now?

    Edit: travisbot will probably say that the tests in this PR fail, but that is because current master fails on form things

    ---------------------------------------------------------------------------

    by asm89 at 2012-07-08T18:53:05Z

    I rebased the PR, tests are green now: [![Build Status](https://secure.travis-ci.org/asm89/symfony.png?branch=refactor-authentication-success-handling)](http://travis-ci.org/asm89/symfony).