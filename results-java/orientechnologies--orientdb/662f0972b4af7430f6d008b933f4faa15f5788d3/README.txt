commit 662f0972b4af7430f6d008b933f4faa15f5788d3
Author: l.garulli <l.garulli@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Thu Jul 15 18:36:51 2010 +0000

    Huge refactoring on GraphDB:
    - changed class names in vertex and edge
    - Optimized memory consumption by removing nested records
    - Optimized speed in ORecord.equals() and hashCode(): now avoid field checks (experimental)