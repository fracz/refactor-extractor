commit 794dbe5ad51b345ecc1712e78bbe859e6de64fa1
Author: Alan Post <alan.post@teradata.com>
Date:   Mon Jun 5 10:59:38 2017 -0400

    Add PruneIndexSourceColumns optimization rule

    Add PruneIndexSourceColumns and its associated tooling:
    * a builder function for IndexSourceNode
    * a matcher for IndexSourceNode
    * a bulider function for the matcher
    * the rule itself
    * a unit test for the rule
    * enabling the rule in presto's rule list
    Also, refactor some TableScanNode matching code so it can be shared
    between TableScanNode and IndexSourceNode, because they are similar.