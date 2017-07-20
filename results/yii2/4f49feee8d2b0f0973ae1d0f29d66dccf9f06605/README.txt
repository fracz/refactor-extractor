commit 4f49feee8d2b0f0973ae1d0f29d66dccf9f06605
Author: John Was <janek.jan@gmail.com>
Date:   Wed Mar 25 00:04:35 2015 +0100

    fixes #7757: in oci schema fix query results row keys case when PDO::ATTR_CASE is set to PDO::CASE_LOWER
    added test to check support for setting PDO::ATTR_CASE and fetching table schemas
    fixed fetching table schemas for mysql when PDO::ATTR_CASE is set
    added tests for oci
    fixed fetching composite fks for oci
    improvements in oci schema parsing
    removed autoIncrement detection fro oci and added test to verify that
    implement batchInsert for oci
    fix detecting IntegrityException for oci
    fixed creating raw sql by skipping object and resource params
    fix command test failing for sqlite