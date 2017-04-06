commit 3952408cf141fdaa884e99845ec2f4077cea74d4
Author: Peter Bacon Darwin <pete@bacondarwin.com>
Date:   Thu Sep 4 14:23:04 2014 +0100

    chore(docs): improve searching by member

    The keywords processor now also extracts the members (i.e. method, properties
    and events) into its own search term property. These are then used in the lunr
    search index with higher weighting that normal keywords to push services that
    contain the query term as a member higher up the search results.

    Closes #7661