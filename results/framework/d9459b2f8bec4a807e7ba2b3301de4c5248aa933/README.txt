commit d9459b2f8bec4a807e7ba2b3301de4c5248aa933
Author: Peter van Westen <info@regularlabs.com>
Date:   Tue May 2 11:04:50 2017 +0200

    [5.4] Makes cache() throw exceoption when incorrect first argument

    The cache() function will return nothing when the first argument is something else than a string or array.

    This PR fixes that by throwing an exception when that is the case.
    It also improves code styling by removing the nested ifs (so less indentation) and follows the 'golden path' guideline.