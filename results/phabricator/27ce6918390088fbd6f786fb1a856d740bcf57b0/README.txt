commit 27ce6918390088fbd6f786fb1a856d740bcf57b0
Author: epriestley <git@epriestley.com>
Date:   Wed Mar 9 10:17:40 2016 -0800

    Fix an issue with the Herald engine field value cache

    Summary:
    To improve the performance of Herald, we attempt to generate the value for each field (e.g., a task title) only once.

    For most field values this is cheap, but for some (like a commit's branches) it can be quite expensive. We only want to pay this cost once, so we cache field values.

    However, D12957 accidentally added a check where we bypass the cache and generate the value for every field, before reading the cache. This causes us to generate each field for every rule that uses it, plus one extra time.

    Instead, use the cache for this check, too. Also allow the cache to cache `null`, since it can be expensive to generate `null` even though the value isn't too interesting.

    The value of this early hit isn't even used (we only care if it throws or not).

    Test Plan:
      - Wrote a rule like "if any condition matches: branches contain a, branches contain b, branches contain c".
      - Put `phlog(new Exception())` in `DiffusionCommitBranchesHeraldField`.
      - Before patch, saw `bin/repository reparse --herald <any commit>` compute branches three times.
      - After patch, saw only one computation.
      - Verified field values in the transcript view

    Reviewers: chad

    Reviewed By: chad

    Differential Revision: https://secure.phabricator.com/D15451