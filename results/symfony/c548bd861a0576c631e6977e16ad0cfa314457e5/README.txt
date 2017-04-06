commit c548bd861a0576c631e6977e16ad0cfa314457e5
Merge: ea45769 5939d34
Author: Romain Neutron <imprec@gmail.com>
Date:   Fri Jul 25 11:23:56 2014 +0200

    bug #11436 fix signal handling in wait() on calls to stop() (xabbuh, romainneutron)

    This PR was merged into the 2.3 branch.

    Discussion
    ----------

    fix signal handling in wait() on calls to stop()

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | #11286
    | License       | MIT
    | Doc PR        |

    ``wait()`` throws an exception when the process was terminated by a signal. This should not happen when the termination was requested by calling the ``stop()`` method (for example, inside a callback which is passed to ``wait()``).

    Commits
    -------

    5939d34 [Process] Fix unit tests in sigchild environment
    eb68662 [Process] fix signal handling in wait()
    94ffc4f bug #11469  [BrowserKit] Fixed server HTTP_HOST port uri conversion (bcremer, fabpot)
    103fd88 [BrowserKit] refactor code and fix unquoted regex
    f401ab9 Fixed server HTTP_HOST port uri conversion
    045cbc5 bug #11425 Fix issue described in #11421 (Ben, ben-rosio)
    f5bfa9b bug #11423 Pass a Scope instance instead of a scope name when cloning a container in the GrahpvizDumper (jakzal)
    3177be5 minor #11464 [Translator] Use quote to surround invalid locale (lyrixx)
    c9742ef [Translator] Use quote to surround invalid locale
    4dbe0e1 bug #11120 [2.3][Process] Reduce I/O load on Windows platform (romainneutron)
    797d814 bug #11342 [2.3][Form] Check if IntlDateFormatter constructor returned a valid object before using it (romainneutron)
    0b5348e minor #11441 [Translator] Optimize assertLocale regexp (Jérémy Derussé)
    537c39b Optimize assertLocale regexp
    4cf50e8 Bring code into standard
    9f4313c [Process] Add test to verify fix for issue #11421
    02eb765 [Process] Fixes issue #11421
    6787669 [DependencyInjection] Pass a Scope instance instead of a scope name.
    9572918 bug #11411 [Validator] Backported #11410 to 2.3: Object initializers are called only once per object (webmozart)
    291cbf9 [Validator] Backported #11410 to 2.3: Object initializers are called only once per object
    efab884 bug #11403 [Translator][FrameworkBundle] Added @ to the list of allowed chars in Translator (takeit)
    3176f8b [Translator][FrameworkBundle] Added @ to the list of allowed chars in Translator
    91e32f8 bug #11381 [2.3] [Process] Use correct test for empty string in UnixPipes (whs, romainneutron)
    45df2f3 minor #11397 [2.3][Process] Fix unit tests on Windows platform (romainneutron)
    cec0a45 [Process] Adjust PR #11264, make it Windows compatible and fix CS
    d418935 [Process] Fix unit tests on Windows platform
    ff0bb01 [Process] Reduce I/O load on Windows platform
    ace5a29 bumped Symfony version to 2.3.19
    75e07e6 updated VERSION for 2.3.18
    4a12f4d update CONTRIBUTORS for 2.3.18
    98b891d updated CHANGELOG for 2.3.18
    06a80fb Validate locales sets intos translator
    06fc97e feature #11367 [HttpFoundation] Fix to prevent magic bytes injection in JSONP responses... (CVE-2014-4671) (Andrew Moore)
    3c54659 minor #11387 [2.3] [Validator] Fix UserPassword validator translation (redstar504)
    73d50ed Fix UserPassword validator translation
    93a970c bug #11386 Remove Spaceless Blocks from Twig Form Templates (chrisguitarguy)
    8f9ed3e Remove Spaceless Blocks from Twig Form Templates
    9e1ea4a [Process] Use correct test for empty string in UnixPipes
    6af3d05 [HttpFoundation] Fix to prevent magic bytes injection in JSONP responses (Prevents CVE-2014-4671)
    ebf967d [Form] Check if IntlDateFormatter constructor returned a valid object before using it