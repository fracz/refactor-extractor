commit caabd415b13ee347dbccc16b03cff67853c0037a
Merge: 711788b ec42844
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Thu Mar 27 08:46:44 2014 +0100

    feature #10546 [Validator] Improved ISBN validator (sprain)

    This PR was merged into the 2.5-dev branch.

    Discussion
    ----------

    [Validator] Improved ISBN validator

    | Q             | A
    | ------------- | ---
    | Bug fix?      | yes
    | New feature?  | no
    | BC breaks?    | no
    | Deprecations? | yes
    | Tests pass?   | yes
    | Fixed tickets | #10386, #10387, #10542
    | License       | MIT
    | Doc PR        | symfony/symfony-docs/pull/3724

    This is a new PR of #10542, now on master branch.

    Todos:
    - [x] Discuss and determine deprecation versions
    - [x] Update docs

    After following some discussion in the tickets mentioned above I have improved the ISBN validator, which has had some inconsistencies:

    * Use a `type` which can be set to `isbn10` or `isbn13` or `null` (checks if value matches any type) instead of the current boolean `isbn10` and `isbn13` options which cause confusion (e.q. if both are true, does this mean the value must match any or both? You could think it's the latter, but that's actually impossible.).
    * In the IBAN validator we recently agreed to be strict about upper- and lowercase handling (#10489). Therefore this should be also the case with the ISBN validator. Some ISBN10 numbers may end with an uppercase `X` (representing the check-digit 10), while a lowercase `x` is considered wrong (see [here](http://www.goodreads.com/topic/show/1253500-a-question-about-isbn-10-s---ending-in-x) and [here](http://en.wikipedia.org/wiki/Category:Pages_with_ISBN_errors)). I did not have access to the actual specifications as I have only found documentation which costs about $100 (e.q. [here](http://www.iso.org/iso/catalogue_detail?csnumber=36563)).

    To avoid bc breaks I suggest to introduce deprecations for current constraint options. [In the documentation](http://symfony.com/doc/current/contributing/code/conventions.html#deprecations) I haven't found any information about which versions may introduce deprecations, so you might have to help me out here with hints on how to handle it correctly. I'll be happy to provide the code with the deprecated parts removed after that.

    Commits
    -------

    ec42844 Improved ISBN validator