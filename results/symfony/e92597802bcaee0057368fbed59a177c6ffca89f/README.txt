commit e92597802bcaee0057368fbed59a177c6ffca89f
Merge: 559fa8c 5e8d401
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Wed Dec 5 15:02:12 2012 +0100

    merged branch lolautruche/configNormalizeKeysFlag (PR #6086)

    This PR was submitted for the 2.1 branch but it was merged into the master branch instead (closes #6086).

    Commits
    -------

    d4a70e8 Implemented possibility to skip key normalization in config processing

    Discussion
    ----------

    Implemented possibility to skip key normalization in config processing

    Bug fix: no
    Feature addition: yes
    Backwards compatibility break: no
    Symfony2 tests pass: yes
    Fixes the following tickets: -
    Todo: -
    License of the code: MIT
    Documentation PR: -

    ## Description

    This PR implements the possibility to deactivate explicitly config keys normalization as it's sometimes annoying and unexpected to have `-` transformed in `_`. The default behavior is kept and deactivation is possible at the DI extension level (not possible to do it at the node level since the config processor does key normalization globally).

    ---------------------------------------------------------------------------

    by lsmith77 at 2012-11-22T09:52:54Z

    this is tricky since you might break some app config formats.

    Semi related: I assume few people test their Bundles with anything but Yaml, but ATM the chances are quite good that it would also work with XML. then again many people are already not including the fix keys call so maybe we should also enable people to explicitly say which formats they support.

    ---------------------------------------------------------------------------

    by stof at 2012-11-22T10:02:54Z

    @lsmith77 you won't break anything as this PR is BC. You would brak it only if an existing bundle starts using it (and you are using XML)

    ---------------------------------------------------------------------------

    by lsmith77 at 2012-11-22T10:14:55Z

    I wasn't trying to imply it breaks BC but it would likely mean that Bundles using this would break the assumption that all formats are supported.

    ---------------------------------------------------------------------------

    by stof at 2012-11-22T10:39:24Z

    @lsmith77 The only difference is that a bundle using that would have an XML config using underscores instead of dashes (which are the XML convention).
    And btw, as long as you don't provide an XSD, people can already use the underscored tags in their XML config...

    ---------------------------------------------------------------------------

    by lsmith77 at 2012-11-22T11:49:50Z

    right again. my point is that this feature breaks current assumptions. note I am not saying this should not be done either. just adding something to consider.

    ---------------------------------------------------------------------------

    by lolautruche at 2012-11-22T16:30:20Z

    Well, the real issue behind that is we currently don't know which format is used for application configuration, leading sometimes to unexpected issues. The problem is that the current *fix* is a bit brutal and magical, leading to headaches while debugging.
    While a real way of dealing with config format should be the best way to fix this, allowing to throw an exception if the user format is inappropriate, this patch at least gives the opportunity to bypass this magical key normalization. A real solution should come with 2.2 or 2.3

    ---------------------------------------------------------------------------

    by stof at 2012-11-22T17:07:17Z

    Actually, this renaming of keys from dashes to underscores should probably be refactored to be aware of the tree. Because the only case where it causes some issues is for prototyped array nodes (using an associative array), as this is the only case where a key is defined by the user.

    ---------------------------------------------------------------------------

    by lolautruche at 2012-11-23T07:30:57Z

    @stof Exactly, and this is precisely where we have a problem, with prototyped array nodes. Having this key normalization aware of the tree is a nice option as well for a proper fix, but I think with this patch at least you can have *some* control :smiley:

    ---------------------------------------------------------------------------

    by lolautruche at 2012-11-26T11:16:06Z

    ping @fabpot

    ---------------------------------------------------------------------------

    by lolautruche at 2012-12-05T09:41:49Z

    Hello, any news on this ? Thanks

    ---------------------------------------------------------------------------

    by fabpot at 2012-12-05T13:45:30Z

    That can only be merged in master.

    ---------------------------------------------------------------------------

    by lolautruche at 2012-12-05T13:47:01Z

    @fabpot OK, should I open a new PR on master then ?

    ---------------------------------------------------------------------------

    by fabpot at 2012-12-05T13:48:07Z

    No, I'm going to switch the branch when merging, it was just to warn you about this.

    ---------------------------------------------------------------------------

    by lolautruche at 2012-12-05T13:49:40Z

    OK thanks :smiley: