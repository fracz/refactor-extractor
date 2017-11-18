commit f781a490d32977cc1c2b622722419174212ec4bb
Author: Martin Traverso <martint@fb.com>
Date:   Mon Mar 16 11:59:36 2015 -0700

    Various improvements to partition-aware planning

    - Relax partitioning criteria: P{k1, k2, ..., kn} => P{k1, k2, ..., kn+1}
    - Pass preferences down the tree. This can help produce subplans that
      benefit multiple operators and avoid unnecessary shuffles
    - Perform partitioned joins if both inputs are hash-partitioned on the same fields