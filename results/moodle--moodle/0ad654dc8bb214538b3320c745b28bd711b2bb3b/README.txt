commit 0ad654dc8bb214538b3320c745b28bd711b2bb3b
Author: David Mudrak <david@moodle.com>
Date:   Thu May 31 14:18:13 2012 +0200

    MDL-33330 Use the referencehash when searching for references

    Together with this, refactoring and cleanup of search_references() and
    similar methods was done. SQL was improved to use the INNER JOIN instead
    of the LEFT JOIN as we are really interested in records that have a
    reference. Also, joining the {repository_instances} table assures that
    only references with valid repository_instance are returned (the check
    against is_external_file() can't be applied to the methods that return
    the count of references so I dropped it completely).