commit 31085cc03ee0595ab4ee05195bc477be3b6a2a88
Author: Khalid Huseynov <khalidhnv@gmail.com>
Date:   Fri Jan 6 20:51:47 2017 -0800

    [ZEPPELIN-1848] add option for S3 KMS key region

    ### What is this PR for?
    When using S3 storage layer with encryption keys, currently only keys created in `us-east-1` region can be used. This PR adds ability to set target region for AWS KMS keys.

    ### What type of PR is it?
    Improvement

    ### Todos
    * [x] - add region to awsClient
    * [x] - add conf for region
    * [x] - tested with aws account `us-west-2` region

    ### What is the Jira issue?
    [ZEPPELIN-1848](https://issues.apache.org/jira/browse/ZEPPELIN-1848)

    ### How should this be tested?
    1. set up S3 storage as in [here](https://zeppelin.apache.org/docs/0.7.0-SNAPSHOT/storage/storage.html#notebook-storage-in-s3)
    2. add region variable with `export ZEPPELIN_NOTEBOOK_S3_KMS_KEY_REGION="us-west-2"` in `conf/zeppelin-env.sh`
    3.  start Zeppelin and read/write S3

    ### Screenshots (if appropriate)
    ![kmc_region](https://cloud.githubusercontent.com/assets/1642088/21712912/0a79ee66-d3ac-11e6-8ba4-1e7f081f213f.gif)

    ### Questions:
    * Does the licenses files need update? no
    * Is there breaking changes for older versions? no
    * Does this needs documentation? updated

    Author: Khalid Huseynov <khalidhnv@gmail.com>

    Closes #1860 from khalidhuseynov/feat/s3-repo-kms-region and squashes the following commits:

    712025f [Khalid Huseynov] add missing vars to .cmd conf
    35c015a [Khalid Huseynov] align # in .sh conf
    40ae2f1 [Khalid Huseynov] refactor and keep backward compatibility
    303f16d [Khalid Huseynov] add documentation
    929d401 [Khalid Huseynov] add property to .site
    d5808cd [Khalid Huseynov] add env vars to .sh
    3110193 [Khalid Huseynov] add crypt conf to s3 repo
    da14298 [Khalid Huseynov] add property to ZeppelinConfiguration