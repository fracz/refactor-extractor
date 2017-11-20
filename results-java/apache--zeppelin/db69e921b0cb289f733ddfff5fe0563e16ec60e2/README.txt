commit db69e921b0cb289f733ddfff5fe0563e16ec60e2
Author: Nate Sammons <Nate.Sammons@nasdaq.com>
Date:   Thu May 26 07:55:03 2016 -0700

    [ZEPPELIN-848] Add support for encrypted data stored in Amazon S3

    ### What is this PR for?
    Adds support for using the AWS KMS or a custom encryption materials
    provider class to encrypt data stored in Amazon S3.  Also a minor
    improvement to logic inside the S3 notebook repo when dealing with local files.

    ### What type of PR is it?
    Improvement

    ### What is the Jira issue?
    https://issues.apache.org/jira/browse/ZEPPELIN-848

    ### How should this be tested?
    Running in EMR or another system in AWS is easiest.  Make appropriate changes to the config and use an AWS KMS key

    ### Questions:
    * Does the licenses files need update? -- NO
    * Is there breaking changes for older versions? -- NO
    * Does this needs documentation? -- YES, changes in storage.md and zeppelin-site.xml.template

    Author: Nate Sammons <Nate.Sammons@nasdaq.com>
    Author: Nate Sammons <nate.sammons@nasdaq.com>

    Closes #886 from natesammons-nasdaq/master and squashes the following commits:

    a6e074f [Nate Sammons] Merge remote-tracking branch 'origin/master'
    cdd3107 [Nate Sammons] Merge remote-tracking branch 'apache/master'
    48b89c0 [Nate Sammons] Update install.md
    ff1540b [Nate Sammons] Merge remote-tracking branch 'apache/master'
    84709c4 [Nate Sammons] Merge remote-tracking branch 'apache/master'
    513361f [Nate Sammons] Update line length
    b318c79 [Nate Sammons] Merge remote-tracking branch 'apache/master'
    ceb5847 [Nate Sammons] Merge remote-tracking branch 'apache/master'
    1475aa0 [Nate Sammons] Merge remote-tracking branch 'apache/master'
    84ddd3b [Nate Sammons] Log exception when reloading notebooks
    b55b98c [Nate Sammons] Updated exception handling
    8628d95 [Nate Sammons] ZEPPELIN-848: Add support for encrypted data stored in Amazon S3