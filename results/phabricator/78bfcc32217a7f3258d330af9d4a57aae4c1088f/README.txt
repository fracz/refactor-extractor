commit 78bfcc32217a7f3258d330af9d4a57aae4c1088f
Author: Bob Trahan <btrahan@phacility.com>
Date:   Mon Apr 13 11:31:34 2015 -0700

    Conpherence - change "A, B, C..." subtitle to "A: what most recent person said" when we can

    Summary:
    For the price of loading transactions more consistently, we get a better subtitle. We do this in all cases EXCEPT for when we're grabbing handles, because that makes the handles pretty heavy weight and I could even feel the perf hit on my development machine and we don't use subtitle there anyway. We may want to cache the latest message on the conpherence thread object to improve performance here as well as consider falling back to "A, B, C..." more often. Code is written such that no transactions means an automagical fallback.

    Fixes T7795. (Technically, there's still a note about handle code conversion work on T7795 but we'll get that generally later.)

    Test Plan:
    played around with conpherence in both views and things seemed to work nicely.
    made sure to try the original repro in T7795 and couldn't get that to go either
    posted a long comment and verified that the CSS / string truncation both make it display nicely. Note that without the CSS the chosen glyph value can be too high to fit nicely at times.

    Reviewers: chad, epriestley

    Reviewed By: epriestley

    Subscribers: Korvin, epriestley

    Maniphest Tasks: T7795

    Differential Revision: https://secure.phabricator.com/D12347