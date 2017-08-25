commit 5e468d30e17ddebd6eb9e600151c07474638d533
Author: gustav_delius <gustav_delius>
Date:   Sun Feb 5 22:51:32 2006 +0000

    Fixed a potential bug that would have hit if one quiz would have had two random questions for the same category, one of which was set to recurse into subcategories and the other not.
    At the same time this improves performance because no longer all the potential questions are loaded from the database but only their ids.