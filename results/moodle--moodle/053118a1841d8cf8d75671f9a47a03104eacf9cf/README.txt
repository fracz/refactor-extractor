commit 053118a1841d8cf8d75671f9a47a03104eacf9cf
Author: Eric Merrill <merrill@oakland.edu>
Date:   Sun Apr 17 21:59:25 2016 -0400

    MDL-53758 search: Better results with low hit rates, improve performance

    Ensures that Solr will return available results, even if there are many
    misses when using check_access(), by asking Solr for more results if the
    counter says there should be more.

    Improves performance by ending processing as soon as the requested page
    of results is processed. Remaining number of pages is an "estimate"
    based on the total result count from Solr and how many items we have
    rejected up to this point.