commit d55fe8475a1983a593bf4a1f096c66705f19edc6
Merge: 4a93d7f 1701447
Author: Nicolas Grekas <nicolas.grekas@gmail.com>
Date:   Mon Jun 16 15:59:08 2014 +0200

    feature #10921 [Debug] generic ErrorHandler (nicolas-grekas)

    This PR was merged into the 2.6-dev branch.

    Discussion
    ----------

    [Debug] generic ErrorHandler

    | Q             | A
    | ------------- | ---
    | Bug fix?      | no
    | New feature?  | yes
    | BC breaks?    | minor, see updated tests
    | Deprecations? | yes
    | Tests pass?   | yes
    | Fixed tickets | none
    | License       | MIT
    | Doc PR        | none

    The proposed goal of this PR is to build a class that can serve as a foundation for a standard error handler, shared with other projects and not only used by the FrameworkBundle.

    This is a merge of my previous work on the subject (https://github.com/nicolas-grekas/Patchwork-Error-Logger/blob/master/class/Patchwork/PHP/InDepthErrorHandler.php) and recent improvements of error handling in Symfony's Debug\ErrorHandler.

    ExceptionHandler has a new AbstractExceptionHandler base class that factors out the handling of fatal errors casted to exceptions.

    ErrorHandler is introduced, which provides five bit fields that control how errors are handled:

    - thrownErrors: errors thrown as ContextErrorException
    - loggedErrors: logged errors, when not @-silenced
    - scopedErrors: errors thrown or logged with their local context
    - tracedErrors: errors logged with their trace, only once for repeated errors
    - screamedErrors: never @-silenced errors

    Each error level can be logged by a dedicated PSR-3 logger object.
    Screaming only applies to logging.
    Throwing takes precedence over logging.
    Uncaught exceptions are logged as E_ERROR.
    E_DEPRECATED and E_USER_DEPRECATED levels never throw.
    E_RECOVERABLE_ERROR and E_USER_ERROR levels always throw.
    Non catchable errors that can be detected at shutdown time are logged when the scream bit field allows so.
    As errors have a performance cost, repeated errors are all logged, so that the developer
    can see them and weight them as more important to fix than others of the same level.

    - [x] build a more generic ErrorHandler
    - [x] update service/listeners definitions to take advantage of the new interface of ErrorHandler
    - [x] add phpdocs
    - [x] add tests

    Commits
    -------

    1701447 [Debug] update FrameworkBundle and HttpKernel for new ErrorHandler
    839e9ac [Debug] generalized ErrorHandler