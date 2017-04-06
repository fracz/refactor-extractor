commit bcef85b948123138e73eb2dabf1f1bd340255d2b
Merge: d8541ab 43e0db5
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Jan 24 09:35:39 2012 +0100

    merged branch vicb/issue/1579 (PR #3035)

    Commits
    -------

    43e0db5 [DomCrawler] Add support for multivalued form fields (fix #1579, #3012)

    Discussion
    ----------

    [DomCrawler] Support for multivalued fields

    This is a tentative fix for #1579 by @kriswallsmith, also see #3012 for more info.

    Any feedback is appreciated.

    ---------------------------------------------------------------------------

    by vicb at 2012-01-05T08:44:51Z

    @stof thanks for the valuable feedback I think most of it should be implemented should we use this solution.
    The one thing I don't agree is PSR-0, I don't want this class to be public, that's is just a "private" helper class.

    There are also missing type hints in the helper class, that should be added.

    ---------------------------------------------------------------------------

    by alessandro1997 at 2012-01-05T10:05:15Z

    Well, @vicb, I think it's up to the developer to not use "private" classes. Just write it in the documentation. But declaring two classes in the same file would be a big violation of the standards.

    ---------------------------------------------------------------------------

    by vicb at 2012-01-05T11:28:53Z

    What "standard"s ?
    PSR-0 is about auto-loading, I don't want/need this to be autoloaded.
    Sf coding standards ? Well relying on a developer reading the doc is more error prone than the current implementation. I sometimes favor pragmatism over theory.

    edit: I am not trying to say I am right here but only that I don't see any added value in moving the helper class to a dedicated file. I appreciate any feedback, really.

    ---------------------------------------------------------------------------

    by fabpot at 2012-01-06T11:55:09Z

    FYI, we already have such a "private" class in https://github.com/symfony/symfony/blob/master/src/Symfony/Component/Security/Http/Firewall/DigestAuthenticationListener.php#L135

    ---------------------------------------------------------------------------

    by vicb at 2012-01-06T16:36:04Z

    @alessandro1997 if you need an example on why it is not safe to rely on developers reading comments, see #2892

    ---------------------------------------------------------------------------

    by vicb at 2012-01-09T22:19:52Z

    @fabpot I am waiting for your feedback on the [proposed API](https://github.com/symfony/symfony/pull/3035/files#L1R57) before finishing this PR.

    ---------------------------------------------------------------------------

    by drak at 2012-01-10T05:12:16Z

    @fabpot

    > FYI, we already have such a "private" class in https://github.com/symfony/symfony/blob/master/src/Symfony/Component/Security/Http/Firewall/DigestAuthenticationListener.php#L135

    Why on is that necessary, it could just be another class file in the namespace.  Unless you are making some kind of forward compatibility, e.g. for with a new class in PHP 5.4 then I see no reason to do that.

    ---------------------------------------------------------------------------

    by vicb at 2012-01-10T07:40:32Z

    What would be a good reason not to allow "private" classes ?
    If the Sf coding standards are the only good reason let's change them then.

    [Java](http://stackoverflow.com/questions/968347/can-a-java-file-have-more-than-one-class) and [ActionScript3](http://livedocs.adobe.com/flex/3/html/help.html?content=03_Language_and_Syntax_05.html) allow such construction

    I would no say any better than the above link on Stack Overflow:

    > The purpose of including multiple classes in one source file is to bundle related support functionality (internal data structures, support classes, etc) together with the main public class. Note that it is always ok not to do this -- the only effect is on the readability (or not) of your code.

    ---------------------------------------------------------------------------

    by Tobion at 2012-01-10T09:35:09Z

    There are also many private classes in the test cases.

    ---------------------------------------------------------------------------

    by stof at 2012-01-10T13:29:08Z

    @Tobion for tests, it is logical because there is no autoloader for the test classes.

    ---------------------------------------------------------------------------

    by vicb at 2012-01-10T13:31:53Z

    @stof by definition you do not want a "private" class to be autoloaded anyway.

    ---------------------------------------------------------------------------

    by alessandro1997 at 2012-01-10T14:11:42Z

    Sure, but what you're doing here is just making instantiating the class a bit more difficult. If a stubborn developer wants to use it, then he (or her) can include the file manually or autoload the "main class".

    PHP does NOT have support for private/inner classes, and, until it does, all classes should be istantiable normally.

    ---------------------------------------------------------------------------

    by stof at 2012-01-10T14:23:30Z

    @vicb what about someone wanting to serialize the object ? (well, serializing is not the issue. unserializing is)

    ---------------------------------------------------------------------------

    by vicb at 2012-01-10T14:57:52Z

    @alessandro1997 you are absolutely right, it's not meant to be instantiated from the outside (it's **private**). You could argue the same with private properties & methods (using Reflection). Dead-end.

    @stof Is unserializing really an issue as the file would have been loaded already ?

    ---------------------------------------------------------------------------

    by fabpot at 2012-01-22T09:38:13Z

    @vicb: I'm fine with the proposed API, but I fail to see why it would be more BC than #3012.

    ---------------------------------------------------------------------------

    by vicb at 2012-01-22T10:06:56Z

    For BC I have to check #3012 again but at some point if I remember correctly the public API had changed (not sure about the latest version in your branch)

    By introducing the private helper class, it is quite easy to see that the public API is not modified by this PR.

    Next steps:

      * Stof the code,
      * Add/fix phpdoc,
      * Add tests for the helper class,
      * Add/refactor tests for the `Form` class.

    @fabpot if you agree with the above steps it could be ready sometime next week.

    ---------------------------------------------------------------------------

    by fabpot at 2012-01-22T10:21:16Z

    The API is perhaps not changed but the behavior will certainly changed. I agree with your steps.

    ---------------------------------------------------------------------------

    by vicb at 2012-01-22T10:45:10Z

    Which leads to the question: should we consider this as a change in behavior (2.1) or a bug fix (2.0) ?

    _I am thinking of a form with multiple fields named `field[]`_

    ---------------------------------------------------------------------------

    by fabpot at 2012-01-22T11:32:04Z

    @vicb: this change should be done on master

    ---------------------------------------------------------------------------

    by vicb at 2012-01-24T07:59:40Z

    Should be ready now, let me know when I should squash after review.

    ---------------------------------------------------------------------------

    by fabpot at 2012-01-24T08:18:03Z

    @vicb: yes, can you squash your commits?

    ---------------------------------------------------------------------------

    by vicb at 2012-01-24T08:29:58Z

    @fabpot done