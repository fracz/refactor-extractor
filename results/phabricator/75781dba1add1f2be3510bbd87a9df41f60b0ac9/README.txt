commit 75781dba1add1f2be3510bbd87a9df41f60b0ac9
Author: epriestley <git@epriestley.com>
Date:   Sat Jan 16 14:59:17 2016 -0800

    Improve autocomplete behavior in lists and with noncompleting results

    Summary:
    Ref T10163. Currently, we don't activate on indented lines, but were too aggressive about this, and would not activate on lines like `  - Hey, @user...`, where we should.

    Instead, don't activate on indented lines if there's only an indent (i.e., `#` probably means enumerated list).

    Also, if results don't have autocompletes (rare but possible with projects missing slugs), improve behavior.

    Test Plan:
      - Typed `  #a`, got no autocomplete.
      - Missing slug thing is a pain to test locallly, `#1 z z z z` reproduces in production. I'll just verify it there.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10163

    Differential Revision: https://secure.phabricator.com/D15040