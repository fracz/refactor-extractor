commit 7baf7c774d8f25a60f74bf57ca19a92a0ef1f748
Author: epriestley <git@epriestley.com>
Date:   Mon Jul 25 10:50:34 2011 -0700

    Further simplify SearchAttachController

    Summary: Try to break this apart a little better in preparation for D595. No
    functional changes, just refactored the relatively large processRequest()
    method.
    Test Plan:
      - Attached and detached revisions from tasks.
      - Attached and detached tasks from revisions.
      - Merged tasks.

    Reviewed By: jungejason
    Reviewers: jungejason, tuomaspelkonen, aran
    CC: aran, jungejason
    Differential Revision: 725