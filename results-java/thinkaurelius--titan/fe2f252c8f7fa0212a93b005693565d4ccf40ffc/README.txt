commit fe2f252c8f7fa0212a93b005693565d4ccf40ffc
Author: Dan LaRocque <dalaro@hopcount.org>
Date:   Fri Jul 25 06:06:08 2014 -0400

    Hack support for HBase short CF names in Faunus

    Prior to this commit, Faunus's HBase input format would fail to read
    from a Titan database written with short-cf-names=true (currently the
    default), since it just assumed that long CF names were in use.

    This commit suggests future refactoring.  Hiding the mapping between
    short and long names inside HBaseStoreManager a private datastructure
    prevents TitanHBaseInputFormat from reading it.  Either the mapping
    needs to become public, or we would need to change the type of CF
    names from String to an enum or some interface that can supply both
    the short and long name through a pair of methods.