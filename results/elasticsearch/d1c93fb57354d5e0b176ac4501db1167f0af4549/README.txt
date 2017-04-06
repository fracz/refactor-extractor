commit d1c93fb57354d5e0b176ac4501db1167f0af4549
Author: Alexander Reelsen <alexander@reelsen.net>
Date:   Mon Aug 17 11:43:47 2015 +0200

    Release: Remove aws-maven plugin/improve release docs

    In order to have consistent deploys across several repositories,
    we should deploy to sonatype first, then mirror those contents,
    and then upload to s3.

    This means, the aws wagon is not needed anymore.