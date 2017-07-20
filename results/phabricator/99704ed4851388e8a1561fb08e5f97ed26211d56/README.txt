commit 99704ed4851388e8a1561fb08e5f97ed26211d56
Author: vrana <jakubv@fb.com>
Date:   Fri Mar 30 16:49:06 2012 -0700

    Support operators in Phabricator search

    Summary:
    Boolean search supports operators, such as phrase search.
    It can be further improved by setting [[http://dev.mysql.com/doc/mysql/en/server-system-variables.html#sysvar_ft_boolean_syntax | ft_boolean_syntax]] to `' |-><()~*:""&^'` (note the leading space):
    Default value uses no operator for "optional word" and `+` for "mandatory word".
    This value uses no operator for "mandatory word" and `|` for "optional word".

    Test Plan: Search for "Enter the name" (with quotes).

    Reviewers: epriestley

    Reviewed By: epriestley

    CC: aran

    Differential Revision: https://secure.phabricator.com/D2064