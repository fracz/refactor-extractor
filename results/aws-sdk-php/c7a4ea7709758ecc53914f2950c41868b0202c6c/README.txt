commit c7a4ea7709758ecc53914f2950c41868b0202c6c
Author: Michael Dowling <mtdowling@gmail.com>
Date:   Mon May 12 17:13:18 2014 -0700

    Adding CloudTrail and updating S3 client

    - Added CloudTrail and CloudTrail tests
    - Updated the ResourceIterator to accept an array path to support S3
    - Fixed a bug in the RulesEndpointProvider to not disregard null values
    - Removed the ability to create custom waiters from AwsClient
    - Added the service name to AwsException messages
    - Starting to refactor the S3 implementation a bit