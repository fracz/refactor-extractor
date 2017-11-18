commit a645a5dc7ae56d41f26c8918ffd00decf2a8884a
Author: Eric Hwang <ehwang@fb.com>
Date:   Tue Nov 3 00:58:31 2015 -0800

    Propagate local properties that guarantee the window pre-partitioned inputs

    This improves the property derivation of WindowNode such that if a WindowNode
    is able to take advantage of the local properties of its input to do less work
    in sorting the input, then the specific underlying local properties that guarantee
    this optimization should be propagated up to subsequent operators.

    For example:

    SELECT SUM(x) OVER (PARTITION BY a ORDER BY b) FROM (
      SELECT SUM(x) OVER (PARTITION BY a) FROM (
        SELECT SUM(X) OVER (ORDER BY a, b) FROM table
      )
    )

    The inner most query produces data ordered by (a, b). The middle query can take
    advantage of that fact that 'a' is already sorted to avoid partitioning on 'a'
    again. Now, with this improvement, the outermost query can also take advantage
    of the fact that the underlying data stream is sorted on (a, b) to avoid
    partitioning on 'a' and sorting on 'b'.