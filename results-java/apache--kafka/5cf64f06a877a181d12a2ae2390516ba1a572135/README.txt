commit 5cf64f06a877a181d12a2ae2390516ba1a572135
Author: Ismael Juma <ismael@juma.me.uk>
Date:   Fri Apr 7 18:29:55 2017 +0100

    MINOR: Log append validation improvements

    - Consistent validation across different code paths in LogValidator
    - Validate baseOffset for message format V2
    - Flesh out LogValidatorTest to check producerId, baseSequence, producerEpoch and partitionLeaderEpoch.

    Author: Ismael Juma <ismael@juma.me.uk>

    Reviewers: Jun Rao <junrao@gmail.com>

    Closes #2802 from ijuma/validate-base-offset