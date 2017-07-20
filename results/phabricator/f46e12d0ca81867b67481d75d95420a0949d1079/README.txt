commit f46e12d0ca81867b67481d75d95420a0949d1079
Author: Jason Ge <jungejason@fb.com>
Date:   Fri Oct 21 18:34:38 2011 -0700

    Refactor some Herald code

    Summary:
    I was reading herald code for a task and realized that the method was
    really long. So I refactor it to shorter methods.

    Test Plan:
    was still able to create a differential rule and commit rule; and
    verified that dry-run still worked.

    Reviewers: epriestley, tuomaspelkonen

    Reviewed By: epriestley

    CC: aran, epriestley

    Differential Revision: 1077