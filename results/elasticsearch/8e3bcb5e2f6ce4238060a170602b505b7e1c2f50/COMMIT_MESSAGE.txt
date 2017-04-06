commit 8e3bcb5e2f6ce4238060a170602b505b7e1c2f50
Author: Britta Weber <britta.weber@elasticsearch.com>
Date:   Mon May 12 23:39:24 2014 +0200

    refactor: unify terms and significant_terms parsing

    Both need the requiredSize, shardSize, minDocCount and shardMinDocCount.
    Parsing should not be duplicated.