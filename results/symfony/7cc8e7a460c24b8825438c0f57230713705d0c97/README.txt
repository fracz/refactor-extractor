commit 7cc8e7a460c24b8825438c0f57230713705d0c97
Merge: 82cbf69 61773de
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun Mar 30 09:42:25 2014 +0200

    minor #10571 [Form] Fixed infinite tests when ICU is available (webmozart)

    This PR was submitted for the master branch but it was merged into the 2.4 branch instead (closes #10571).

    Discussion
    ----------

    [Form] Fixed infinite tests when ICU is available

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | no
    | Tests pass?   | yes
    | Fixed tickets | -
    | License       | MIT
    | Doc PR        | -

    Commits
    -------

    61773de [Form] Fixed infinite tests when ICU is available
    3cd1c9c bug #10565 fixed typos (fabpot)
    584b5c0 fixed typos
    4a0382b fixed CS
    9e78a06 Merge branch '2.4'
    b78d174 minor #10554 framework_bundle -> framework (mvrhov)
    85cf7a7 framework_bundle -> framework
    e505ecd feature #10370 [FrameworkBundle][Console] Add parameter descriptors (inalgnu)
    6aa1050 Add parameter descriptors
    4a06daf feature #9818 [TwigBundle] Add command to list twig functions, filters, globals and tests (Seldaek)
    7d61154 Add command to list twig functions, filters, globals and tests
    caabd41 feature #10546 [Validator] Improved ISBN validator (sprain)
    ec42844 Improved ISBN validator
    711788b fixed CS
    cb147ec feature #10457 [Serializer] Unify usage of normalizer cache (Berdir)
    e7389aa Move normalizer cache to getNormalier()/getDenormalizer(), use those in normalizeObject()/denormalizeObject()
    5a4885e feature #9140 [Validator][Email] - Strict validation and soft dependency (egulias)
    3368630 #1581 - Strict in Email constraint and use of Egulias\EmailValidator
    e0de958 Merge branch '2.4'
    830ab24 minor #10527 [HttpKernel] [Exception] Add UnprocessableEntityHttpException to HttpKernel Exceptions (steveYeah)
    304cbe9 Add UnprocessableEntityHttpException to HttpKernel Exceptions
    3ab2dd7 feature #10291 [Validator] New validator for UUIDs (colinodell)
    19931c9 Added new validator for UUIDs
    790ba4c feature #10534 [SwiftMailer] [MonologBundle] send error log mails from CLI (arodiss)
    9bb602f added explicit swiftmailer flush after ConsoleEvents::TERMINATE
    9dc14a5 feature #10513 [Bridge][Propel1][Form] Model choice accept custom unique column (cedriclombardot)
    81e94d0 Model choice accept custom unique column
    3b95d09 bug #10535 [Form] Fixed tests after merging pattern deprecation (stefanosala)
    e2d8944 Fixed tests after merging pattern deprecation
    58bdf84 Merge branch '2.4'
    3baa43b Merge branch '2.4'
    9e13cc0 feature #9178 made HttpFoundationRequestHandler a service (kor3k)
    43451e9 made HttpFoundationRequestHandler a service
    feea36d feature #10001 [Form] Deprecated max_length and pattern options (stefanosala)
    52c07c7 Deprecated max_length and pattern options
    37d484c bug #10530 [Process] Do not show output in FailedException if it was disabled (Taluu)
    849703a When a process fails, check if the output is enabled
    d6fccdd feature #9690 Using Oracle Database as ACL storage (skolodyazhnyy)
    5f3be0e Fix Exception messages for ObjectIdentity ObjectIdentityInterface doesn't require implementing __toString method, so we need to make sure that object can be converted to string.
    f66bed7 feature #10506 [Debug] sync with deprecation in DebugClassLoader (nicolas-grekas)
    ad88cdd feature #10509 [FrameworkBundle] add scheme option to router:match command (Tobion)
    e3f17f9 add scheme option to router:match command
    c70a468 [Debug] sync with deprecation in DebugClassLoader
    6586eaa feature #10194 [Console] Added standalone PSR-3 compliant logger (dunglas)
    e40b34d [Console] Added standalone PSR-3 compliant logger
    53fec31 feature #9097 [Validator] Added hasser support for entity method validation (bicpi)
    e8b6978 [Validator] Added hasser support for entity method validation
    b14fa26 feature #10476 [Console] Fixed unsetting of setted attributes on OutputFormatterStyle (Badkill)
    ce0c4b4 [Console] Fixed unsetting of setted attributes on OutputFormatterStyle
    8170db8 feature #10473 [WebProfilerBundle] enhance logs display (nicolas-grekas)
    6deb4cc minor #10474 [Console] Rename Command::setProcessName to Command::setProcessTitle (lyrixx)
    3e6c940 [WebProfilerBundle] enhance logs display
    6786f6d [Console] Rename Command::setProcessName to Command::setProcessTitle
    71dc07c feature #10466 [Debug] add a screaming mode to ErrorHandler (nicolas-grekas)
    c152ccb minor #10469 fix doc block and namespace by @insekticide (cordoval)
    5cc817d [Debug] add a screaming mode to ErrorHandler
    89bde6e fix class namespace by @insekticid and doc block fix
    48c9985 feature #10451 [TwigBundle] Add possibility to generate absolute assets urls (romainneutron)
    a635c4f bug #10456 [Process] Handle idle timeout and disabled output conflict (romainneutron)
    ae84810 [Process] Increase tests speed
    40c08c6 [Process] Handle idle timeout and disable output conflict
    76b8851 [TwigBundle] Add possibility to generate absolute assets urls
    1e973b2 feature #10404 [Security] Match request based on HTTP methods in firewall config (danez)
    a8e9ed5 Make it possible to match the request based on HTTP methods in the firewall configuration
    f0c0c2c tweaked sentence
    120a7e9 bug #10443 [FrameworkBundle] Use DIC parameter as default host value if available (romainneutron)
    85a2fbf [FrameworkBundle] Use DIC parameter as default host value if available
    b7c158a feature #10439 [FrameworkBundle] Add posibility to specify method and host in router:match command (romainneutron)
    acc66b9 [FrameworkBundle] Add posibility to specify method and host in router:match command
    c14d67c [SecurityBundle] changed a hardcoded string to its constant equivalent
    ea0598a minor #10390 [Security] Add constants for access decision strategies (c960657)
    1e0fea6 minor #10432 clean up framework bundle commands (cordoval)
    0984313 clean up commands from framework bundle
    5e0bb71 feature #10425 [Process] Add Process::disableOutput and Process::enableOutput methods (romainneutron)
    a891e14 [Process] Add Process::disableOutput and Process::enableOutput methods
    c2d4be1 feature #10418 [Form] Removed "magic" from FormErrorIterator (webmozart)
    daac66e [Form] Removed "magic" from FormErrorIterator
    5b07e0a feature #10414 [Validator] Checked the constraint class in constraint validators (webmozart)
    ce81199 feature #9918 [Form] Changed Form::getErrors() to return an iterator and added two optional parameters $deep and $flatten (webmozart)
    df56c23 [Validator] Checked the constraint class in constraint validators
    5d6ef00 Add class constants for access decision strategies.
    f15ea50 minor #10376 [Component][Serializer] Add fluent interface to GetSetMethodNormalizer (alexsegura)
    2d42533 [Component][Serializer] Add fluent interface to GetSetMethodNormalizer
    bc38d76 minor #10366 [FrameworkBundle] set a default value for gc_probability (fabpot)
    1948d36 Merge branch '2.4'
    f6bc83f minor #10373 added the BC docs to the contributing file (fabpot)
    2f9432a added the BC docs to the contributing file
    69d2c8e Merge branch '2.4'
    e778cf1 fixed previous merge
    0aeb394 feature #9739 [FrameworkBundle] Extract KernelTestCase from WebTestCase (johnkary)
    4d31d2f fixed CS
    c4b8e03 feature #9852 [Translation] Added template for relative file paths in FileDumper (florianv)
    786c956 feature #10368 [FrameworkBundle] Added a translation:debug command (fabpot)
    f039bde [FrameworkBundle] fixed edge cases for translation:debug and tweaked the output
    5ea6437 [FrameworkBundle] refactored the built-in web server
    a04175e Changed placeholders
    623d149 Added a ConcreteDumper
    84f0902 [Translation] Added template for relative file paths
    887e6ff feature #10017 [FrameworkBundle] Add HHVM support for built-in web server (RickySu)
    66798ba9 [FrameworkBundle] Add HHVM support for built-in web server
    597a310 Added a translation:debug command
    2a15923 feature #10100 [ClassLoader] A PSR-4 compatible class loader (derrabus)
    6837df3 [ClassLoader] A PSR-4 compatible class loader
    725f7ab bug #10367 [HttpKernel] fixed serialization of the request data collector (fabpot)
    4a1639a feature #10314 [Serializer] added support for is.* methods in GetSetMethodNormalizer (tiraeth)
    480219f [Serializer] added support for is.* methods in GetSetMethodNormalizer
    6102f99 [HttpKernel] fixed serialization of the request data collector
    98c3fe7 feature #10365 [Console] deprecated TableHelper in favor of Table (fabpot)
    7e1bdd7 [FrameworkBundle] set a default value for gc_probability
    21784ce [Console] make it possible to pass a style directly to Table::setStyle()
    14caaec [Console] added the possibility to insert a table separator anywhere in a table output
    39c495f [Console] deprecated TableHelper in favor of Table
    77bfac7 Merge branch '2.4'
    aed7eab [Console] fixed some initializations in the ProgressBar class
    554b28d feature #10356 [Console] A better progress bar (fabpot)
    0d1a58c [Console] made formats even more flexible
    8c0022b [Console] fixed progress bar when using ANSI colors and Emojis
    38f7a6f [Console] fixed PHP comptability
    244d3b8 [Console] added a way to globally add a progress bar format or modify a built-in one
    a9d47eb [Console] added a way to add a custom message on a progress bar
    7a30e50 [Console] added support for multiline formats in ProgressBar
    1aa7b8c [Console] added more default placeholder formatters for the progress bar
    2a78a09 [Console] refactored the progress bar to allow placeholder to be extensible
    4e76aa3 [Console] added ProgressBar (to replace the stateful ProgressHelper class)
    65c9aca feature #10352 [DataCollector] Improves the readability of the collected arrays in the profiler (fabpot)
    dce66c9 removed double-stringification of values in the profiler
    eede330 feature #10354 removed as many usage of the request service as possible without breaking BC (fabpot)
    d638369 removed as many usage of the request service as possible without breaking BC
    681f14b feature #10353 [Debug] ExceptionHandlerInterface to allow third party exception handlers to handle fatal errors caught by ErrorHandler (FineWolf)
    15d063b Create ExceptionHandlerInterface to allow third party exception handlers' to handle fatal errors
    1cda2d4 [HttpKernel] tweaked value exporter
    3f297ea Improves the readability of the collected arrays in the profiler.
    7baeaa2 Merge branch '2.4'
    a820930 bug #10308 [Debug] enhance non-PSR-0 compatibility for case mismatch test (nicolas-grekas)
    ca4736b [Console] fixed missing abstract keyword
    537f1fa minor #10315 Fix typo in method name (fixe)
    9c582d9 minor #10320 Fix typo in UPGRADE-3.0.md (hice3000)
    53c8189 Fix typo in UPGRADE-3.0.md
    120e197 [Debug] enhance non-PSR-0 compatibility for case mismatch test
    01858d3 Fixed typo in method name
    872647a [Security] simplified code
    6d926c8 minor #10311 use core StringUtils to compare hashes (steelywing)
    9fc01d2 use core StringUtils to compare hashes
    79baf8d feature #10165 [FrameworkBundle] config:dump-reference command can now dump current configuration (lyrixx)
    aca3271 feature #9862 [FrameworkBundle] Added configuration for additionnal request formats (gquemener)
    f90ba11 [FrameworkBundle] Added configuration for additionnal request formats
    6e9358a feature #10257 [FrameworkBundle][Console] Load command from DIC after command from bundles. (lyrixx)
    3e8f33a feature #10201 [Debug] error stacking + fatal screaming + case testing (nicolas-grekas)
    838dc7e Merge branch '2.4'
    5a5eb50 Merge branch '2.4'
    6de362b [Debug] error stacking+fatal screaming+case testing
    19a368e [FramworkBundle] Added config:debug command
    34f4ef5 [FrameworkBundle][Console] Load command from DIC after command from bundles.
    f828aee Merge branch '2.4'
    d0386e4 minor #10214 [3.0][Console] Added isVerbosity* to OutputInterface (lyrixx)
    816b295 [3.0][Console] Added isVerbosity* to OutputInterface
    fe86efd feature #10200 [EventDispatcher] simplified code for TraceableEventDispatcher (fabpot)
    6dfdb97 feature #10198 [Stopwatch] Allow getting duration of events without calling stop() (jochenvdv)
    42e4c7b [EventDispatcher] simplified code for TraceableEventDispatcher
    bcb5239 bug #10199 fix ProcessPipes (nicolas-grekas)
    076d417 fix ProcessPipes
    22970e0 Merge branch '2.4'
    2efe461 Allow retrieving unstopped stopwatch events
    d3d097d Include running periods in duration
    bea1537 minor #10186 Made some HHVM-related fixes (fabpot)
    4c9e307 Merge branch '2.4'
    1e89880 Revert "minor #10160 [Translation] [Loader] Add INI_SCANNER_RAW to parse ini files (TeLiXj)"
    1240758 [Routing] fixed CS
    e223395 [Debug] fixed case differences between PHP and HHVM (classes are case-insensitive anyway in PHP)
    23acc24 [Debug] made order of suggestions predictable in error messages
    10d4d56 removed unneded test groups
    51d3d62 feature #8655 Adds PTY mode & convenience method mustRun() (schmittjoh)
    7affb71 minor #10172 [WebProfilerBundle] Use inline images instead of asset() in form-panel (Danez)
    12eabd8 remove unused icons
    7c3a3e1 minor #10160 [Translation] [Loader] Add INI_SCANNER_RAW to parse ini files (TeLiXj)
    f259157 Further compress icon
    eb6d02c Use inline images instead of asset() function
    5ef60f1 [Translation] [Loader] Add INI_SCANNER_RAW to parse ini files
    d61f492 minor #10149 Fixed grammar in Hungarian translations (r1pp3rj4ck)
    7f74049 Fixed grammar in Hungarian translations
    6a0de7f Merge branch '2.4'
    774674e feature #10112 [Routing] Add createRoute method for AnnotationClassLoader (henrikbjorn)
    4e137cc feature #10064 [TwigBridge] Added support for json format in twig:lint command (lyrixx)
    2e2a65c Merge branch '2.4'
    78d49fb minor #10081 [FrameworkBundle] Pretty Ppint json ouput of yaml:lint command (lyrixx)
    97404b3 Add createRoute method for AnnotationClassLoader
    689e9bf [FrameworkBundle] Pretty Ppint json ouput of yaml:lint command
    4d2f94a [TwigBridge] Added support for json format in twig:lint command
    621f991 [TwigBridge] Cleaned documentation of twig:lint command
    4ad343b feature #10005 [Security] Added named encoders to EncoderFactory (tamirvs)
    c69e2ca [Security] Added named encoders to EncoderFactory
    a207006 minor #9996 [Routing] Added an extension point for globals in AnnotationClassLoader (lyrixx)
    e1b85db feature #9405 [FrameworkBundle] Added a helper method to create AccessDeniedException (klaussilveira)
    183d0ec [FrameworkBundle] Added a helper method to create AccessDeniedException
    7da803f minor #10021 [WebProfilerBundle] Simplified session storage implementation (bschussek)
    cec05bf [WebProfilerBundle] Simplified session storage implementation
    fff29a3 feature #9967 Form debugger storage (WouterJ)
    744da7f Form debugger storage
    916420f feature #9980 [Routing][FrameworkBundle] Deprecated the apache dumper (jakzal)
    6258cfa [Routing][FrameworkBundle] Deprecated the apache dumper
    6b3fbb5 [Form] Changed the default value of $flatten in Form::getErrors() to true
    a9268c4 [Form] Changed Form::getErrors() to return an iterator and added two optional parameters $deep and $flatten
    8ea3a43 feature #9993 [Form] Errors now reference the field they were added to and the violation/exception that caused them (bschussek)
    8f7524e [Routing] Added an extension point for globals in AnnotationClassLoader
    c8a0ee6 [Form] Errors now reference the field they were added to and the violation/exception that caused them
    147c82b minor #9972 Upgrade File for 2.5 (Danez)
    fefcf41 Added upgrade info for #9601
    c833518 feature #9776 [Console] Added the possibility to set a different default command (danielcsgomes)
    418de05 [Console] Added the possibility to set a different default command
    79bea0a feature #9966 added feedback to the cache:clear command (fabpot)
    f2261da [FrameworkBundle] simplified code
    a1f6411 [FrameworkBundle] added feedback in cache:clear
    0af3ca3 Merge branch '2.4'
    ef12af9 feature #9963 [HttpFoundation] JsonResponse::setEncodingOptions accepts also integer (stloyd)
    f8bc3b2 [HttpFoundation] JsonResponse::setEncodingOptions accepts also integer
    74fb207 feature #9915 [HttpFoundation] Add ability to change JSON encoding options (stloyd)
    89f4784 [HttpFoundation] Add ability to change JSON encoding options
    a596ba3 feature #8375 [OptionsResolver] Allow giving a callback as an allowedValue to OptionsResolver (marekkalnik)
    07d1d30 Allow giving a callback as an allowedValue to OptionsResolver
    f3670b4 feature #9666 [FrameworkBundle] Added a yaml:lint command (lyrixx)
    9c06b27 [FrameworkBundle] Added yaml:lint command
    8cd8ec0 Remove usage of deprecated _scheme in Routing Component
    6063b49 Merge branch '2.4'
    d3b28dc minor #9944 [FrameworkBundle] Update composer.json to account for #9792 (realityking)
    f1efd16 [FrameworkBundle] Update composer.json to account for #9792
    f499094 minor #9880 test for class route annotation (ewgRa)
    60c2140 minor #9931 Removed all codeCoverageIgnore annotations from the code (stof)
    ac94ddb test for class route annotation
    4248169 Removed all codeCoverageIgnore annotations from the code
    2c059ee feature #9926 [Finder] Added GLOB_BRACE support in Finder::in() method (jakzal)
    a12db9b Merge branch '2.4'
    e2698fc [Finder] Included GLOB_BRACE support in the CHANGELOG.
    30814d3 [Finder] Added a test case for the GLOB_BRACE in Finder:in().
    da67f5d [Finder] Added GLOB_BRACE support in Finder::in() method
    64c7095 removed unneeded use statements
    18d69a8 Merge branch '2.4'
    df6b0b8 bug #9917 [HttpFoundation] fixed PHP warnings (fabpot)
    cf71e22 [HttpFoundation] fixed PHP warnings
    410d399 fixed PSR0
    0defad9 Merge branch '2.4'
    8850456 Merge branch '2.4'
    702e2a4 feature #9855 [Twig] Decouple Twig commands from the Famework (GromNaN)
    907748d [Twig] Decouple Twig commands from the Famework
    3203793 added a missing namespace use statement
    28a8400 feature #9251 [WIP] [FrameworkBundle] removed some more dependencies on the request service (fabpot)
    9eaed35 feature #9857 Form Debugger JavaScript improvements (WouterJ)
    d9bb4ff Reverted Sfjs.toggle change
    6aaefd8 Reverted new image
    ec2496f Fixed asset function
    624a09f Enlarged the clickable area of the toggle button in the form tree
    0ff2632 Moved toggle icon behind the headlines in the form debugger
    8ba8db2 Changed toggle color back to blue and made headlines in the form debugger clickable
    b8358e3 Added "use strict" statements
    0936694 Inverted toggler images and improved button coloring
    64a3442 Improved JavaScript of the form debugger
    0908155 Vertically centered the icons in the form tree
    9dc2cde Fixed CS
    6eb1e49 Added error badge
    b02c227 Made sections collapsable
    b223527 Improved form tree
    c19ff6f Expand tree
    96c4486 minor #9374 Change of scope (djoos)
    335bee2 Change of scope
    5079f34 feature #9892 [Validator] Added Doctrine cache (florianv)
    3c4de45 [Validator] Added Doctrine cache
    c15175a Merge branch '2.4'
    9fbe148 feature #9590 WebTestCase: Assume relative KERNEL_DIR is relative to phpunit.xml[.dist]? (mpdude)
    4f3d502 [FrameworkBundle] removed some more dependencies on the request service
    fd5a2d0 Merge branch '2.4'
    7d80045 Merge branch '2.4'
    f063108 feature #9814 [EventDispatcher] Added TraceableEventDispatcher from HttpKernel (florianv)
    9a90e06 [EventDispatcher] Added TraceableEventDispatcher from HttpKernel
    0b0c431 feature #9833 [Bridge] [DoctrineExtension] Allow cache drivers that are not an EM's child (FabioBatSilva)
    f0d9af0 feature #9876 [Serializer] error handling inconsistencies fixed in the serializer decoders (fabpot)
    a1ab939 [Serializer] fixed CS
    6d9f0be Json encoder classes now throws UnexpectedValueException as XML classes
    f9dff06 Merge branch '2.4'
    f132197 feature #9360 [Finder] Fix finder date constraints and tests (ruian)
    c6b1c74 feature #9837 [Form] added getter to transformer chain (cordoval)
    7a9ab2c feature #8305 Added MutableAclProvider::deleteSecurityIdentity (lavoiesl)
    a4d423e minor #8423 Update LocaleTest.php (mikemeier)
    572126b Update LocaleTest.php
    694bd72 Merge branch '2.4'
    6a51831 feature #9846 [Console] hide output of ProgressHelper when isDecorated is false (kbond)
    006cb81 [Console] show no output in ProgressHelper when isDecorated is false (fixes #9511)
    8d39213 feature #8650 [Security][Acl] Add MutableAclProvider::updateUserSecurityIdentity (lemoinem)
    2b7af12 feature #9843 [PropertyAccess] Allowed non alphanumeric chars in object properties (florianv)
    20d4eb6 [PropertyAccess] Allowed non alphanumeric chars in object properties
    da53d92 [Security][Acl] Fix #5787 : Add MutableAclProvider::updateUserSecurityIdentity
    3565d96 added getter to transformer chain
    c4f14fb Extract new base test class KernelTestClass
    7528e4c Allow cache drivers that are not an EM's child
    c0e4c4a bug #9816 [DependencyInjection] fixes #9815 Syntax error in PHP dumper (realityking)
    e00b0f3 [DependencyInjection] fixes #9815 Syntax error in PHP dumper
    baaf9b6 feature #9792 [EventDispatcher][HttpKernel] Move RegisterListenersPass from HttpKernel to EventDispatcher. (realityking)
    89b8e0a [EventDispatcher][HttpKernel] Move RegisterListenersPass from HttpKernel to EventDispatcher.
    ad4d6f7 feature #9668 [DepdencyInjection] forgot to add definition of dumped container member variable parameters (cordoval)
    5b02d3f [DepdencyInjection] forgot to add definition of dumped container member variable parameters
    8b08888 bug #9812 [DependencyInjection] fix a regression introduced in #9807 (realityking)
    0d78776 [DependencyInjection] fix a regression introduced in #9807
    11434de minor #9802 [HttpKernel] Remove FrameworkBundle dependency in BundleTest (florianv)
    0604220 feature #9780 [Console] Added a way to set the process title (lyrixx)
    20a064f [HttpKernel] Remove FrameworkBundle dependency in BundleTest
    375a2c7 minor #9807 [DependencyInjection] Avoid call_user_func in dumped containers. (realityking)
    be1eaaa [DependencyInjection] Avoid call_user_func in dumped containers.
    204a25e [Console] Added a way to set the process title
    67ae8fa feature #8224 [Form][2.4] added an option for multiple files upload (closes #1400) (bamarni)
    c8c6448 [Form][2.4] added an option for multiple files upload (closes #1400)
    c1051d5 fixed CS
    e660bc9 feature #9773 [Form] Added delete_empty option to allow proper emptyData handling of collections (peterrehm)
    8bdb7a0 [Form] Added delete_empty option to allow proper emptyData handling of collections
    21ecad1 minor #9723 [Security] [Acl] [MaskBuilder] Refactor common code and reduce nesting (djlambert)
    c0a7e1b feature #9791 [DependencyInjection] added support for inlining Configurators (realityking)
    4e9aa07 [DependencyInjection] added support for inlining Configurators
    8e1f854 feature #9779 [Debug] Added UndefinedMethodFatalErrorHandler (lyrixx)
    74d13e3 [Debug] Added UndefinedMethodFatalErrorHandler
    6764f91 Merge branch '2.4'
    de57903 Merge branch '2.4'
    7d85809 Refactor common code and reduce nesting
    5e37fc8 Revert "encourage the running of coverage"
    bb73852 encourage the running of coverage
    e5362c1 Merge branch '2.4'
    db4f551 Merge branch '2.4'
    ce64435 minor #9594 [Security] Fixed typos/CS/PHPDoc (pborreli)
    1fcc7c5 Merge branch '2.4'
    05dc0e1 Consider KERNEL_DIR setting as relative to the PhpUnit XML file if it does not point to a directory (relative to the current cwd)
    4aab341 updated version to 2.5
    e1110de Fixed typos/CS/PHPDoc
    4ccafa6 Fix finder date constraints and tests
    dbd264a adds cache for isPtySupported()
    6c11207 attempts to fix tests on Travis
    2ff1870 adds convenience method mustRun
    53441aa adds support for PTY mode
    bdbbe58 [Security][Acl] Issue #5787 : Added MutableAclProvider::deleteSecurityIdentity