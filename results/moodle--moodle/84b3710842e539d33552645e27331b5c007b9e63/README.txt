commit 84b3710842e539d33552645e27331b5c007b9e63
Author: Tim Hunt <T.J.Hunt@open.ac.uk>
Date:   Wed Jan 8 18:22:20 2014 +0000

    MDL-43246 question engine: avoid order-by id.

    This was breaking with oracle master/master replication. Fortunately all
    the places that needed to be changed were private to datalib.php. There
    is still some ordering by id there, but only places where we want a
    consitent, rather than meaningful, order, so that is OK.

    The queries changed by this patch all have subqueries in aggregate
    queries that pull out the latest step for a question_attempt. Those
    queries used to look for MAX(id) but now they look for
    MAX(sequencenumber). This is equivalent (for databases where ids always
    increase with time, except for auto-saved steps. In the past,  an
    auto-saved step might have been considered latest. Now the latest step
    will always be one that has been properly processed. You can aruge that
    this change is an improvement. Anyway, it is a moot point. All these
    queries are only used in reports which are run on completed attempts,
    where there will not be any autosaved data.