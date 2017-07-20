commit bea4ec7f8878a1fc982dbf2304a17c2a12adf78d
Author: Stefan Grootscholten <stefan.grootscholten@gmail.com>
Date:   Wed Dec 28 23:04:40 2016 +0100

    Some refactoring after testing hg protocol.

    - Revert deletion of generateSshUrl() as this is needed when falling back on the GitDriver or HgDriver.
    - Implement clean way to fallback from BitbucketDriver to GitDriver or HgDriver after previous changes.
    - Implement fallback in HgBitbucketDriver like in GitBitbucketDriver.