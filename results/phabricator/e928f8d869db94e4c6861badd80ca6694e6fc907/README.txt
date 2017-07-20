commit e928f8d869db94e4c6861badd80ca6694e6fc907
Author: Chad Little <chad@chadsdomain.com>
Date:   Wed Jan 9 12:54:25 2013 -0800

    Have form elements default to the standard font size.

    Summary: Font size globally is 13px, but form elements were 12px and harder to read. This helps readability and consistency with form elements.

    Test Plan: Checked various forms, left comments.

    Reviewers: epriestley, btrahan

    Reviewed By: btrahan

    CC: aran, Korvin

    Differential Revision: https://secure.phabricator.com/D4377