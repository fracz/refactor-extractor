commit 5f66d46e220d54660ce5aa78a366ed0e8c103b9b
Author: Eloy Lafuente (stronk7) <stronk7@moodle.org>
Date:   Thu Jan 16 02:38:24 2014 +0100

    MDL-43713 behat: improve multi-select support

    This patch implements:

    1) Normalization of options. Before the patch options
    in a select were being returned as "op1 op2 op3" by selenium
    and "op1 op2 op3" by goutte. With the patch, those lists
    are always returned like "op1, op2, op3". If real commas are
    needed when handling multiple selects they should
    be escaped with backslash in feature files.

    2) Support for selecting multiple options. Before the patch
    only one option was selected and a new selection was cleaning the
    previous one. With the patch it's possible to pass "op1, op2" in
    these steps:
      - I fill the moodle form with (table)
      - I select "OPTION_STRING" from "SELECT_STRING"

    3) Ability to match multiple options in this steps. Before the
    patch matching of multiple was really random, now every every
    passed option ("opt1, opt2") is individually verified. It applies
    to these 2 steps:
      - the "ELEMENT" select box should contain "OPTIONS"
      - the "ELEMENT" select box should not contain "OPTIONS"

    4) Two new steps able to verify if a form have some options selected or no:
      - the "ELEMENT" select box should contain "OPTIONS" selected
      - the "ELEMENT" select box should contain "OPTIONS" not selected

    5) Change get_value from xpath search to Mink's getValue() that is immediate
    (does not need form submission) and works for all browsers but Safari, that
    fails because of the extra ->click() issued.

    Note all the changes 1-4 only affect to multi-select fields. Single
    selects should continue working 100% the same.

    The change 5) causes Safari to fail. The problem has been traced down to
    the extra ->click() present there. Anyway there are not test cases
    requiring that "immediate" evaluation right now. Only the special feature
    file attached verifies it.