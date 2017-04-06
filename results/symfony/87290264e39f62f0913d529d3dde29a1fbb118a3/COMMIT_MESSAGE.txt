commit 87290264e39f62f0913d529d3dde29a1fbb118a3
Merge: b378964 5be0042
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Mon Jan 7 23:10:31 2013 +0100

    merged branch ricardclau/improve-creditcard-regexp (PR #6583)

    This PR was merged into the master branch.

    Commits
    -------

    5be0042 better regexp, more test cases, added comments about each credit card
    cc278af [Validator] Fix `CardSchemeValidator` double violation when value is non-numeric. Making scheme option accept strings in addition to arrays.

    Discussion
    ----------

    [Validator] Improve regexp for Credit Cards and some more tests

    Bug fix: yes
    Feature addition: no
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets:
    Todo: Ensure these regexps are proper (credit card validation is always a pain)
    License of the code: MIT
    Documentation PR:

    Regarding Cases excluded from new Regular Expressions:

    - Credit card lengths should be respected, these regexp cover lengths in http://en.wikipedia.org/wiki/Bank_card_number
    - Visa length can only be 16 and 13 (older ones)
    - Diners Cards starting by 5 come from a joint venture between Diners Club and MasterCard, and should be processed like a MasterCard (according to http://www.regular-expressions.info/creditcard.html).
    - There seems to be JCB cards starting by 2131 and 1800, I could find them is some places, also found these numbers being tested in Credit Card generators, but some people don't cover them. I don't know their story either

    Any comments will be much appreciated!

    ---------------------------------------------------------------------------

    by fabpot at 2013-01-06T19:33:27Z

    Thanks for working on this. It would be very valuable if you can add information about these regexes as comments (with links to relevant sources -- like what you've done in the PR description). Thanks.

    ---------------------------------------------------------------------------

    by ricardclau at 2013-01-06T21:01:52Z

    Always glad to be able to contribute a little bit

    @fabpot you mean @link / @see PHPDoc inside CardSchemeValidator.php? Or further comments in this discussion before adding them?

    ---------------------------------------------------------------------------

    by fabpot at 2013-01-06T21:16:48Z

    The more information we can add in the class, the better it is.

    ---------------------------------------------------------------------------

    by ricardclau at 2013-01-07T20:56:05Z

    I've added comments and included code from #6603 as I've said there. If you need something else, please let me know, once this is merged, #6603 can also be closed

    ---------------------------------------------------------------------------

    by fabpot at 2013-01-07T21:41:40Z

    Can you keep the commit from #6603 to keep ownership?

    ---------------------------------------------------------------------------

    by ricardclau at 2013-01-07T21:44:16Z

    I actually have thought about that... let me try my git skills :)

    ---------------------------------------------------------------------------

    by ricardclau at 2013-01-07T21:59:16Z

    There you go!