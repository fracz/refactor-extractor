commit 5b15c449cb1680cbd66c9241309062e47424c321
Author: l.garulli <l.garulli@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Thu Jul 15 18:45:52 2010 +0000

    Huge refactoring on GraphDB:
    - changed class names in vertex and edge
    - Optimized memory consumption by removing nested records
    - Optimized speed in ORecord.equals() and hashCode(): now avoid field checks (experimental)