commit e98541030c5e0aadfdb194dbb55254f404219600
Author: l.garulli <l.garulli@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Thu Jul 15 18:37:55 2010 +0000

    Huge refactoring on GraphDB:
    - changed class names in vertex and edge
    - Optimized memory consumption by removing nested records
    - Optimized speed in ORecord.equals() and hashCode(): now avoid field checks (experimental)