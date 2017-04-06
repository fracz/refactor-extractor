commit 48414000bb1342c01b729df7d9dcc6f6c66cc144
Merge: 2ae1f90 cb6fdb1
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sat Feb 11 23:58:28 2012 +0100

    merged branch drak/session_refactor (PR #2853)

    Commits
    -------

    cb6fdb1 [HttpFoundation] removed Session::close()
    c59d880 Docblocks.
    b8df162 Correct instanceof condition.
    8a01dd5 renamed getFlashes() to getFlashBag() to avoid clashes
    282d3ae updated CHANGELOG for 2.1
    0f6c50a [HttpFoundation] added some method for a better BC
    146a502 [FrameworkBundle] added some service aliases to avoid some BC breaks
    93d81a1 [HttpFoundation] removed configuration for session storages in session.xml as we cannot provide a way to configure them (like before this PR anyway)
    74ccf70 reverted 5b7ef116507fa05e1042065f61a50733c19116cb (Simplify session storage class names now we have a separate namespace for sessions)
    91f4f8a [HttpFoundation] changed default flash bag to auto-expires to keep BC
    0494250 removed unused use statements
    7878a0a [HttpFoundation] renamed pop() to all() and getAll() to all()
    0d2745f [HttpFoundation] Remove constants from FlashBagInterface
    dad60ef [HttpFoundation] Add back get defaults and small clean-up.
    5b7ef11 [HttpFoundation] Simplify session storage class names now we have a separate namespace for sessions.
    27530cb [HttpFoundation] Moved session related classes to own sub-namespace.
    4683915 [HttpFoundation] Free bags from session storage and move classes to their own namespaces.
    d64939a [DoctrineBridge] Refactored driver for changed interface.
    f9951a3 Fixed formatting.
    398acc9 [HttpFoundation] Reworked flashes to maintain same behaviour as in Symfony 2.0
    f98f9ae [HttpFoundation] Refactor for DRY code.
    9dd4dbe Documentation, changelogs and coding standards.
    1ed6ee3 [DoctribeBridge][SecurityBundle][WebProfiler] Refactor code for HttpFoundation changes.
    7aaf024 [FrameworkBundle] Refactored code for changes to HttpFoundation component.
    669bc96 [HttpFoundation] Added pure Memcache, Memcached and Null storage drivers.
    e185c8d [HttpFoundation] Refactored component for session workflow.
    85b5c43 [HttpFoundation] Added drivers for PHP native session save handlers, files, sqlite, memcache and memcached.
    57ef984 [HttpFoundation] Added unit and functional testing session storage objects.
    3a263dc [HttpFoundation] Introduced session storage base class and interfaces.
    c969423 [HttpFoundation] Added FlashBagInterface and concrete implementation.
    39288bc [HttpFoundation] Added AttributesInterface and AttributesBagInterface and concrete implementations.

    Discussion
    ----------

    [2.1][HttpFoundation] Refactor session handling and flash messages

    Bug fix: yes
    Feature addition: yes
    Backwards compatibility break: yes
    Symfony2 tests pass: yes
    Fixes the following tickets: #2607, #2591, #2717, #2773
    References the following tickets: #2592, #2543, #2541, #2510, #2714, #2684
    Todo: -

    __Introduction__

    This extensive PR is a refactor with minimal BC breaks of the `[HttpFoundation]` component's session management which fixes several issues in the current implementation.  This PR includes all necessary changes to other bundles and components is documented in the `CHANGELOG-2.1` and `UPGRADING-2.1`.

    __Summary of Changes__

    __Session:__
      - Session object now implements `SessionInterface`

    __Attributes:__
      - Attributes now handled by `AttributeBagInterface`
      - Added two AttributeBag implementations: `AttributeBag` replicates the current Symfony2 attributes behaviour, and the second, `NamespacedAttributeBag` introduces structured namespaced representation using '/' in the key.  Both are BC.  `FrameworkBundle` defaults to the old behaviour.

    __Flash messages:__
      - Flash messages now handled by `FlashBagInterface`
      - Introduced `FlashBag` which changes the way flash messages expire, they now expire on use rather than automatically, useful for ESI.
      - Introduced `AutoExpireFlashBag` (default) which replicates the old automatic expiry behaviour of flash messages.

    __Session Storage:__
      - Introduced a base object, `AbstractSessionStorage` for session storage drivers
      - Introduced a `SessionSaveHandlerInterface` when using custom session save handlers
      - Introduced a `NullSessionStorage` driver which allows for unsaved sessions
      - Introduced new session storage drivers for Memcache and Memcached
      - Introduced new session storage drivers for PHP's native SQLite, Memcache and Memcached support

    __General:__
      - Fixed bugs where session attributes are not saved and all cases where flash messages would get lost
      - Wrote new tests, refactored related existing tests and increased test coverage extensively.

    __Rationale/Details__

    I'll explain more detail in the following sections.

    __Unit Tests__

    All unit and functional tests pass.

    __Note on Functional Testing__

    I've introduced `MockFileSessionStorage` which replaces `FilesystemSessionStorage` to emulate a PHP session for functional testing.  Essentially the same methodology of functional testing has been maintained but without interrupting the other session storage drivers interaction with real PHP sessions.  The service is now called `session.storage.mock_file`.

    __Session Workflow__

    PHP sessions follow a specific workflow which is not being followed by the current session management implementation and is responsible for some unpredictable bugs and behaviours.

    Basically, PHP session workflow is as follows: `open`, `read`, `write`, `close`.  In between these can occur, `destroy` and `garbage collection`.  These actions are handled by `session save handlers` and one is always registered in all cases.  By default, the `files` save handler (internally to PHP) is registered by PHP at the start of code execution.

    PHP offers the possibility to change the save handler to another internal type, for example one provided by a PHP extension (think SQLite, Memcache etc), or you can register a user type and set your own handlers.  However __in all cases__ PHP requires the handlers.

    The handlers are called when the following things occur:

      - `open` and `read` when `session_start()` or the session autostarts when PHP outputs some display
      - `destroy` when `session_regenerate_id(true)` is called
      - `write` and `close` when PHP shuts down or when `session_write_close()` is called
      - `garbage collection` is called randomly according to configurable probability

    The next very important aspect of this PR is that `$_SESSION` plays an important part in this workflow because the contents of the $_SESSION is populated __after__ the `read` handler returns the previously saved serialised session data.  The `write` handler is sent the serialised `$_SESSION` contents for save.  Please note the serialisation is a different format to `serialize()`.

    For this reason, any session implementation cannot get rid of using `$_SESSION`.

    I wrote more details on this issue [here](https://github.com/symfony/symfony/issues/2607#issuecomment-2858300)

    In order to make writing session storage drivers simple, I created a light base class `AbstractSessionStorage` and the `SessionSaveHandlerInterface` which allows you to quickly write native and custom save handler drivers.

    __Flash Messages [BC BREAK]__

    Flash messages currently allow representation of a single message per `$name`.  Fabien designed the original system so that `$name` was equivalent to flash message type.  The current PR changes the fact that Flash messages expire explicitly when retrieved for display to the user as opposed to immediately on the next page load.

    The last issue fixes potential cases when flash messages are lost due to an unexpected intervening page-load (an error for example).  The API `get()` has a flag which allows you to override the `clear()` action.

    __Flash message translation__

    This PR does not cover translation of flash messages because  messages should be translated before calling the flash message API.  This is because flash messages are used to present messages to the user after a specific action, and in any case, immediately on the next page load.  Since we know the locale of the request in every case we can translate the message before storing.  Secondly, translation is simply a string manipulation.  Translation API calls should always have the raw untranslated string present because it allows for extraction of translation catalogs.  For a complete answer see my answer [here](https://github.com/symfony/symfony/pull/2543#issuecomment-2858707)

    __Session attribute and structured namespacing__

    __This has been implemented without changing the current default behaviour__ but details are below for the alternative:

    Attributes are currently stored in a flat array which limits the potential of session attributes:

    Here are some examples to see why this 'structured namespace' methodology is extremely convenient over using a flat system.  Let's look at an example with csrf tokens.  Let's say we have multiple csrftokens stored by form ID (allowing multiple forms on the page and tabbed browsing).

    If we're using a flat system, you might have

        'tokens' => array('a' => 'a6c1e0b6',
                          'b' => 'f4a7b1f3')

    With a flat system when you get the key `tokens`, you will get back an array, so now you have to analyse the array.  So if you simply want to add another token, you have to follow three steps: get the session attribute `tokens`, have to add to the array, and lastly set the entire array back to the session.

        $tokens = $session->get('tokens');
        $tokens['c'] = $value;
        $session->set('tokens', $tokens);

    Doable, but you can see it's pretty long winded.

    With structured namespacing you can simply do:

        $session->set('c', $value, '/tokens');

    There are several ways to implement this, either with an additional `$namespace` argument, or by treating a character in the `$key` as a namespacer.  `NamespacedAttributeBag` treats `/` as a namespacer so you can represent `user.tokens/a` for example.  The namespace character is configurable in `NamespacedAttributeBag`.

    ---------------------------------------------------------------------------

    by marijn at 2011-12-18T15:43:17Z

    I haven't read the code yet but the description from this PR and your line of thought seem very well structured.
    Seems like a big +1 for me.

    ---------------------------------------------------------------------------

    by lsmith77 at 2011-12-19T16:01:19Z

    @deviantintegral could you look over this to see if it really addresses everything you wanted with PR #2510 ?

    ---------------------------------------------------------------------------

    by deviantintegral at 2011-12-24T20:12:03Z

    I've read through the documentation and upgrade notes, and I can't see anything that's obviously missing from #2510. Being able to support multiple flashes per type is the most important, and the API looks reasonable to me. Drupal does support supressing repeat messages, but that can easily be implemented in our code unless there's a compelling case for it to be a part of Symfony.

    I wonder if PHP memcache support is required in Symfony given the availability of memcached. I'm not familiar with how other parts of Symfony handle it, but there is often quite a bit of confusion between the two PHP extensions. It could be simpler to remove one, or add a bit of info describing or linking to why there are two nearly identical classes.

    Is it possible to make one class inherit from the other (memcached is a child of memcache)?

    ---------------------------------------------------------------------------

    by Fristi at 2011-12-24T20:29:46Z

    Interesting, maybe add: session events as I did with the current impl: https://github.com/Fristi/SessionBundle

    ---------------------------------------------------------------------------

    by drak at 2011-12-25T00:50:03Z

    @deviantintegral - I agree about the confusion between memcache and memcached but actually, it is necessary to support both because `memcached` is not available everywhere.  For example on Debian Lenny and RHEL/CentOS 5, only memcache is available by default.  This would preclude a massive amount of shared hosting environments.  Also, it is not possible to inherit one from the other, they are completely different drivers.

    @Fristi - I also thought about the events, but they do not belong as part of the standalone component as this would create a coupling to the event dispatcher.  The way you have done it, ie, in a bundle is the right way to achieve it.

    ---------------------------------------------------------------------------

    by matheo at 2011-12-25T01:12:00Z

    Impressive work, looks like a big improvement and deserves a big +1

    ---------------------------------------------------------------------------

    by datiecher at 2011-12-26T11:57:12Z

    Took some time to grok all the changes in this PR but all in all it is a keeper. Specially the new flash message API, it's really nicer to work with it then the previous one.

    Nicely done @drak!

    ---------------------------------------------------------------------------

    by lsmith77 at 2012-01-02T15:00:00Z

    @fabpot did you have time to review this yet? with all the work @drak has done its important that he gets some feedback soon. its clear this PR breaks BC in ways we never wanted to allow. but i think this PR also clearly explains why its necessary none the less.

    ---------------------------------------------------------------------------

    by drak at 2012-01-02T15:41:53Z

    @fabpot - I have removed the WIP status from this PR now and rebased against the current master branch.

    ---------------------------------------------------------------------------

    by Tobion at 2012-01-07T07:13:38Z

    From what I read from the IRC chat logs, the main concern of @fabpot is whether we really need multiple flash messages per type. I'm in favor of this PR and just want to add one point to this discussion.
    At the moment you can add multiple flash messages of different type/category/identifier. For example you can specify one error message and one info message after an operation. I think most agree that this can be usefull.
    But then it makes semantically no sense that you currently cannot add 2 info messages. This approach feels a bit half-done.
    So I think this PR eliminates this paradox.

    ---------------------------------------------------------------------------

    by drak at 2012-01-07T09:11:07Z

    For reference there is also a discussion started by @lsmith77 on the mailing list at https://groups.google.com/forum/#!topic/symfony-devs/cy4wokD0mQI

    ---------------------------------------------------------------------------

    by dlsniper at 2012-01-07T16:02:15Z

    @drak I could also add the next scenario that I currently have to live with, in addition to @lsmith77 ones.

    I had this issue while working on our shopping cart implementation for a customer where the customer wanted to show the unavailability of the items as different lines in the 'flash-error' section of the cart. We had to set an array as the 'flash message' in order to display that information.

    So in this case for example having the flash messages types as array would actually make more sense that sending an array to the flasher. Plus the the other issue we had was that we also wanted to add another error in the message but we had to do a check to see if the flash message is an array already or we need to make it an array.

    I think it's better not to impose a limit of this sort and let the users be able to handle every scenario, even if some are rare, rather that forcing users to overcome limitations such as these.

    I really hope this PR gets approved faster and thanks everyone for their hard work :)

    ---------------------------------------------------------------------------

    by Tobion at 2012-01-07T21:01:07Z

    @dlsniper I think you misinterpreted my point.

    ---------------------------------------------------------------------------

    by dlsniper at 2012-01-07T21:04:04Z

    @Tobion I'm sorry I did that, I'll edit the message asap. Seems no sleep in 26 hours can cause brain not to function as intended :)

    ---------------------------------------------------------------------------

    by lsmith77 at 2012-02-01T14:38:52Z

    FYI the drupal guys are liking this PR (including the flash changes):
    http://drupal.org/node/335411

    ---------------------------------------------------------------------------

    by drak at 2012-02-01T14:51:33Z

    @lsmith77 Fabien asked me to remove the changes to the flash messages so that they are as before - i.e. only one flash per name/type /cc @fabpot

    ---------------------------------------------------------------------------

    by fabpot at 2012-02-01T14:58:23Z

    To be clear, I've asked to split this PR in two parts:

     * one about the session refactoring (which is non-controversial and should be merged ASAP)
     * this one with only the flash refactoring

    ---------------------------------------------------------------------------

    by drak at 2012-02-02T11:29:26Z

    @fabpot this is ready to be merged now.  I will open a separate PR later today for the flash messages as a bucket.

    ---------------------------------------------------------------------------

    by fabpot at 2012-02-02T11:34:39Z

    I must have missed something, but I still see a lot of changes related to the flash messages.

    ---------------------------------------------------------------------------

    by drak at 2012-02-02T11:39:10Z

    When I spoke to you you said you wanted to make the commit with flash messages with one message per name/type rather than multiple.  The old flash messages behaviour is 100% maintained in `AutoExpireFlashBag` which can be the default in the framework if you wish.  The `FlashBag` implementation makes Symfony2 ESI compatible.

    ---------------------------------------------------------------------------

    by stof at 2012-02-02T11:47:38Z

    @drak splitting into 2 PRs means you should not refactor the flash messages in this one but in the dedicated one.

    ---------------------------------------------------------------------------

    by drak at 2012-02-02T12:29:43Z

    @stof Yes. I discussed with Fabien over chat there are basically no changes
    to flashes in `FlashBag` and `AutoExpireFlashBag` maintains the exact
    behaviour as before.  The FlashBag just introduces ESI compatible flashes.
     There is no way to refactor the sessions without moving the flash messages
    to their own bag.  The next PR will propose the changes to flashes that
    allow multiple messages per name/type.  I can size the PR down a little
    more removing the new storage drivers and so on to make the PR smaller but
    that's really as far as I can go.  To be clear, while the API has changed a
    little for flashes, the behaviour is the same.