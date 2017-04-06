commit 91ce4830b71fc6008b5c9bd69ee0a950e1aed206
Author: Lee Hinman <lee@writequit.org>
Date:   Thu Mar 5 14:16:21 2015 -0700

    [ENGINE] Inc store reference before reading segments info

    If a tragic even happens while we are reading the segments info from the
    store the store might have been closed concurrently. We had this
    behavior before and was lost in a refactoring.