commit 167287de1f9f8790b0f73c51362a85e5e901d627
Author: Jared Hancock <jared@osticket.com>
Date:   Thu Aug 13 18:13:42 2015 -0500

    search: Seriously improve keyword searching performance

    No. Seriously. Like from 11k seconds down to 0.3 for a full-text search on
    several thousand tickets.