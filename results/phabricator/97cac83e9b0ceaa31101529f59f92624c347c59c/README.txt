commit 97cac83e9b0ceaa31101529f59f92624c347c59c
Author: epriestley <git@epriestley.com>
Date:   Wed Jan 25 11:05:49 2017 -0800

    Add a "Needs Verification" state to Audit

    Summary:
    Fixes T2393. This allows authors to explicitly say "I think I fixed everything, please accept my commit now thank you".

    Also improves behavior of "re-accept" and "re-reject" after new auditors you have authority over get added.

    Test Plan:
      - Kicked a commit back and forth between an author and auditor by alternately using "Request Verification" and "Raise Concern".
      - Verified it showed up properly in bucketing for both users.
      - Accepted, added a project, accepted again (works now; didn't before).
      - Audited on behalf of projects / packages.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T2393

    Differential Revision: https://secure.phabricator.com/D17252