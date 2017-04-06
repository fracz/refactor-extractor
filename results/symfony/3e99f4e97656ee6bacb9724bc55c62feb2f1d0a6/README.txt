commit 3e99f4e97656ee6bacb9724bc55c62feb2f1d0a6
Merge: b981a6f 73db84f
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Jan 10 14:38:06 2013 +0100

    merged branch asm89/issue-837 (PR #4935)

    This PR was merged into the master branch.

    Commits
    -------

    73db84f [Security] Move translations file to 'security' domain
    324703a [Security] Switch to English messages as message keys
    aa74769 [Security] Fix CS + unreachable code
    2d7a7ba [Security] Fix `AuthenticationException` serialization
    50d5724 [Security] Introduced `UsernameNotFoundException#get/setUsername`
    39da27a [Security] Removed `get/setExtraInformation`, added `get/set(Token|User)`
    837ae15 [Security] Add note about changed constructor to changelog
    d6c57cf [FrameworkBundle] Register security exception translations
    d7129b9 [Security] Fix exception constructors called in `UserChecker`
    0038fbb [Security] Add initial translations for AccountStatusException childs
    50e2cfc [Security] Add custom `getMessageKey` AccountStatusException childs
    1147977 [Security] Fix InsufficientAuthenticationException constructor calls
    79430b8 [Security] Fix AuthenticationServiceException constructor calls
    42cced4 [Security] Fix AuthenticationException constructor calls
    963a1d7 [Security] Add initial translations for the exceptions
    ed6eed4 [Security] Add `getMessageKey` and `getMessageData` to auth exceptions
    694c47c [Security] Change signature of `AuthenticationException` to match `\Exception`

    Discussion
    ----------

    [2.2][Security] AuthenticationException enhancements

    Bug fix: semi
    Feature addition: yes
    Backwards compatibility break: yes
    Symfony2 tests pass: [![Build Status](https://secure.travis-ci.org/asm89/symfony.png?branch=issue-837)](http://travis-ci.org/asm89/symfony)
    Fixes the following tickets: #837
    License of the code: MIT

    This PR adds the functionality discussed in #837 and changes the constructor of the `AuthenticationException` to match that of `\Exception`. This PR will allow developers to show a translated (save) authentication exception message to the user. :)

    *Todo:*
    - Add some functional test to check that the exceptions can indeed be translated?
    - Get feedback on the current English messages

    ---------------------------------------------------------------------------

    by asm89 at 2012-07-15T14:04:11Z

    ping @schmittjoh

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-07-15T14:57:32Z

    Looks good to me.

    While you are at the exceptions, I think we can also get rid of the "extra information" thing and replace it by explicit getters/setters. Mostly that will mean adding set/getToken, set/getUser, set/getUsername. Bundles might add custom exceptions which have other data. This will make it a bit more useful and predictable.

    ---------------------------------------------------------------------------

    by asm89 at 2012-07-15T15:40:45Z

    @schmittjoh I removed the `get/setExtraInformation` and added the more explicit getters/setters as you suggested.

    ---------------------------------------------------------------------------

    by asm89 at 2012-07-15T19:33:15Z

    @fabpot Did you reschedule this for 2.2? Why? It was originally a 2.1 ticket. I think it is an important one because at the moment there is no reliable way to show users the cause of an `AuthenticationException` without the threat of exposing sensitive information. This issue has been around for a while, see the original issue this PR refers to, or for example [this TODO comment in FOSUB](https://github.com/FriendsOfSymfony/FOSUserBundle/blob/master/Controller/SecurityController.php#L37).

    The PR itself is ready to merge now. My only question that remains is about whether the actual translations should be functional tested?

    ---------------------------------------------------------------------------

    by fabpot at 2012-07-15T19:43:19Z

    We need to stop at some point. If not, we never release anything. beta3 was scheduled for today and I don't plan any other one before the first RC and I won't have time to review this PR next week. So, if you, @schmittjoh, @vicb, @stof, and a few other core devs "validate" this PR, I might consider merging it before 2.1.

    ---------------------------------------------------------------------------

    by asm89 at 2012-07-15T19:46:09Z

    @fabpot I totally agree with your point of view. I just have been trying to pickup some security issues that were still open. :)

    ---------------------------------------------------------------------------

    by stof at 2012-07-15T19:50:29Z

    This looks good to me

    ---------------------------------------------------------------------------

    by asm89 at 2012-08-12T09:06:24Z

    Since the beta period is over I assume the window was missed to get this security related PR in 2.1. If I have feedback from @fabpot I'll still try to make it mergeable asap though.

    ---------------------------------------------------------------------------

    by fabpot at 2012-08-13T10:10:32Z

    @asm89 This would indeed be considered for merging in 2.2.

    ---------------------------------------------------------------------------

    by Antek88 at 2012-10-03T10:30:46Z

    +1

    ---------------------------------------------------------------------------

    by stof at 2012-10-04T21:27:15Z

    @asm89 could you rebase this PR ? It conflicts with master

    ---------------------------------------------------------------------------

    by fabpot at 2012-10-05T17:16:44Z

    What's the status of this PR? @asm89 Have you taken all the feedback into account?

    ---------------------------------------------------------------------------

    by stof at 2012-10-13T17:48:48Z

    @asm89 ping

    ---------------------------------------------------------------------------

    by fabpot at 2012-10-29T09:48:40Z

    @asm89 If you don't have time, I can finish the work on this PR, but can you just tell me what's left?

    ---------------------------------------------------------------------------

    by asm89 at 2012-10-29T10:02:22Z

    I can pick this up, but I have two outstanding questions:
    - One about adding `::create()`? https://github.com/symfony/symfony/pull/4935#discussion_r1358297
    - And what is the final verdict on the messages? https://github.com/symfony/symfony/pull/4935#discussion_r1165701 The initial idea was that the exception itself have an exception message which is plain english and informative for the developer. If you want to display the 'safe' user messages you have the optional dependency on the translator. There is a comparison made with the Validator component, but in my opinion that's a different case because the violations always contain the message directed at the user and have no plain english message for the developer. Apart from that the Validator component contains it's own code for replacing `{{ }}` variables in messages (duplication? not as flexible as the translator). Concluding I'd opt for: optional dependency on translator component if you want to show 'safe' user messages + message keys.

    @schmittjoh Any things to add?

    ---------------------------------------------------------------------------

    by schmittjoh at 2012-10-29T10:14:09Z

    Message keys sound good to me. I wouldn't add the ``create`` method for now.

    On Mon, Oct 29, 2012 at 11:02 AM, Alexander <notifications@github.com>wrote:

    > I can pick this up, but I have two outstanding questions:
    >
    >    - One about adding ::create()? symfony/symfony#4935<https://github.com/symfony/symfony/issues/4935#discussion_r1358297>
    >    - And what is the final verdict on the messages? symfony/symfony#4935<https://github.com/symfony/symfony/issues/4935#discussion_r1165701>The initial idea was that the exception itself have an exception message
    >    which is plain english and informative for the developer. If you want to
    >    display the 'safe' user messages you have the optional dependency on the
    >    translator. There is a comparison made with the Validator component, but in
    >    my opinion that's a different case because the violations always contain
    >    the message directed at the user and have no plain english message for the
    >    developer. Apart from that the Validator component contains it's own code
    >    for replacing {{ }} variables in messages (duplication? not as
    >    flexible as the translator). Concluding I'd opt for: optional dependency on
    >    translator component if you want to show 'safe' user messages + message
    >    keys.
    >
    > @schmittjoh <https://github.com/schmittjoh> Any things to add?
    >
    > —
    > Reply to this email directly or view it on GitHub<https://github.com/symfony/symfony/pull/4935#issuecomment-9861016>.
    >
    >

    ---------------------------------------------------------------------------

    by fabpot at 2012-10-29T10:27:37Z

    As I said in the discussion about the translations, I'm -1 for the message keys to be consistent with how we manage translations everywhere else in the framework.

    ---------------------------------------------------------------------------

    by stof at 2012-10-29T10:30:50Z

    @fabpot When we changed the English translation for the validation errors in 2.1, we had to tag the commit as a BC rbeak as it was changing the source for all other translations. And if you look at the state of the files now, you will see that we are *not* using the English as source anymore in some places as some validation errors have a pluralized translation but the source has not been changed.
    So I think using a key is more future-proof.

    ---------------------------------------------------------------------------

    by asm89 at 2012-10-30T19:44:49Z

    Any final decision on this? On one hand I have @stof and @schmittjoh +1 on message keys, on the other @fabpot -1. I guess it's your call @fabpot.

    Edit: also @vicb seemed to be +1 on message keys earlier on.

    ---------------------------------------------------------------------------

    by drak at 2012-11-01T20:19:00Z

    I am also -1, I agree with @fabpot

    ---------------------------------------------------------------------------

    by asm89 at 2012-11-12T09:38:51Z

    @fabpot Can you please give a definite answer on this? I personally think @stof and @vicb have good points to do message keys, but with all these different people +1 and -1'ing the PR I'm lost on what it should actually do.

    ---------------------------------------------------------------------------

    by asm89 at 2012-11-14T09:59:06Z

    ping @fabpot

    ---------------------------------------------------------------------------

    by asm89 at 2012-11-26T10:01:27Z

    ping @fabpot We talked about this in Berlin. Any final thoughts on the PR? :) One idea was to do message keys + opt depend on the translator component if you want to use them, or use your own implementation.

    ---------------------------------------------------------------------------

    by fabpot at 2012-11-26T14:01:37Z

    The conclusion is: keep using plain English.

    On Mon, Nov 26, 2012 at 11:01 AM, Alexander <notifications@github.com>wrote:

    > ping @fabpot <https://github.com/fabpot> We talked about this in Berlin.
    > Any final thoughts on the PR? :) One idea was to do message keys + opt
    > depend on the translator component if you want to use them, or use your own
    > implementation.
    >
    > —
    > Reply to this email directly or view it on GitHub<https://github.com/symfony/symfony/pull/4935#issuecomment-10709997>.
    >
    >

    ---------------------------------------------------------------------------

    by Inori at 2012-11-26T15:00:22Z

    is this final? if not, then +1 for message keys

    ---------------------------------------------------------------------------

    by vicb at 2012-11-27T22:33:47Z

    @fabpot I can't understand why we keep discussing this for months as this implementation use *both* keys and plain Englis, ie using  keys  is optional ( if it was not it would not be an issue according to #6129)

    ---------------------------------------------------------------------------

    by asm89 at 2013-01-02T21:43:46Z

    @fabpot @vicb I'll rebase this PR, fix the comments and refactor the message keys to use plain English + {{ }} syntax for the placeholders.

    ---------------------------------------------------------------------------

    by asm89 at 2013-01-07T15:00:58Z

    @fabpot If I fix this tonight, will it make the beta?

    ---------------------------------------------------------------------------

    by fabpot at 2013-01-07T15:53:00Z

    yes, definitely.

    ---------------------------------------------------------------------------

    by asm89 at 2013-01-07T20:13:38Z

    @fabpot I switched the implementation to English messages instead of message keys and fixed the final comments + rebased. Anything you want me to do after this?

    Still happy with `getMessageKey()`?