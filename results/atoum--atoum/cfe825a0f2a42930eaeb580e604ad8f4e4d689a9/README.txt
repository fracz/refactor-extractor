commit cfe825a0f2a42930eaeb580e604ad8f4e4d689a9
Author: mageekguy <frederic.hardy@mageekbox.net>
Date:   Mon Dec 1 17:09:51 2014 +0100

    Improve dataset key reporting in case of failure.

    Currently, if a test using a dataset provider fail, atoum display in cli
    the following message ($ is the CLI prompt):

    $ In file 547c9209a296c on line 547 in case '547c9209a276c', 547c9209a29d7 failed for data set #3404808712286633985 of data provider 547c9209a27e1: 547c9209a2a00

    To be clear, it display the key of the dataset which fail as # following by a string if a named key is used, by a number if the key is not named.
    And if it's easily readable if key are not named, it's not the case if
    the key is named:

    $ In file 547c9209a296c on line 547 in case '547c9209a276c', 547c9209a29d7 failed for data set #integer should be greater than 0 of data provider 547c9209a27e1: 547c9209a2a00

    So, to improve readability, this commit use the [] notation for the two kind of key:

    $ In file 547c9209a296c on line 547 in case '547c9209a276c', 547c9209a29d7 failed for data set [integer should be greater than 0] of data provider 547c9209a27e1: 547c9209a2a00