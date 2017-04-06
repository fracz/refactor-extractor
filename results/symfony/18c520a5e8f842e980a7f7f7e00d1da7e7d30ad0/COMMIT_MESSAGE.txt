commit 18c520a5e8f842e980a7f7f7e00d1da7e7d30ad0
Merge: 2578f1e 237bbd0
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Dec 11 18:46:52 2012 +0100

    merged branch Tobion/loaders (PR #6172)

    This PR was merged into the master branch.

    Commits
    -------

    237bbd0 fixed and refactored YamlFileLoader in the same sense as the XmlFileLoader
    392d785 removed covers annotation from loader tests and unneeded use statements
    45987eb added tests for the previous XmlFileLoader fixes
    b20d6a7 ensure id, pattern and resource are specified
    8361b5a refactor the XMlFileLoader
    83fc5ff fix namespace handling in xml loader; it could not handle prefixes
    1a60a3c make resource and key attributes required in xsd
    02e01b9 improve exception messages in xml loader
    51fbffe remove unneeded cast
    358e9c4 fix some phpdoc

    Discussion
    ----------

    [Routing] improve loaders

    BC break: no

    Main points:
    - fixed Xml loader that could not handle namespace prefixes but is valid xml
    - fixed Yaml loader where some nonsesense configs were not validated correctly like an imported resource with a pattern key.

    Added tests for all. Some refactoring + a few tweaks like better exception messages and more consistency between Xml loader and yaml loader. See also commits.

    ---------------------------------------------------------------------------

    by Tobion at 2012-12-07T18:16:08Z

    @fabpot this is ready

    ---------------------------------------------------------------------------

    by Tobion at 2012-12-11T17:30:10Z

    @fabpot rebased. Please don't squash to one big commit where one does not see what changed why.

    ---------------------------------------------------------------------------

    by fabpot at 2012-12-11T17:32:40Z

    I only squash when most commits are CS fixes and feedback.

    ---------------------------------------------------------------------------

    by Tobion at 2012-12-11T17:37:49Z

    Well, you squashed #6022 so it's not possible to revert a specific deprecation.