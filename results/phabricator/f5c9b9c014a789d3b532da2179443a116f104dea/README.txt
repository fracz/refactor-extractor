commit f5c9b9c014a789d3b532da2179443a116f104dea
Author: Joshua Spence <josh@joshuaspence.com>
Date:   Mon May 25 19:02:49 2015 +1000

    Make Herald rules more resilient

    Summary:
    Make Herald conditions and actions more resilient (see discussion in D12896). This protects against invalid rules, which may have been valid in the past but are no longer valid. Specifically:

      - If a rule has an invalid field, the conditions fail and the actions do not execute.
      - The transcript shows that the rule failed because of an invalid field, and points at the issue.
      - If a rule has an invalid action, that action fails but other actions execute.
      - The transcript shows that the action failed.
      - Everything else (particularly, other rules) continues normally in both cases.
      - The edit interface is somewhat working when editing an invalid rule, but it could use some further improvements.

    Test Plan:
      # Ran this rule on a differential revision and saw the rule fail in the transcript.
      # Was able to submit a differential without receiving an `ERR-CONDUIT-CORE`.
      # Edited the Herald rule using the UI and was able to save the rule succesfully.
      # Ran this rule on a differential revision and saw one success and one failure in the transcript.
      # Was able to submit a differential without receiving an `ERR-CONDUIT-CORE`.
      # Edited the Herald rule using the UI. Clicking save caused a `HeraldInvalidActionException` to be thrown, but maybe this is okay.

    Differential Revision: http://phabricator.local/D41