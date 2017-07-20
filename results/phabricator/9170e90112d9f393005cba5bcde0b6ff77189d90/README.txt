commit 9170e90112d9f393005cba5bcde0b6ff77189d90
Author: Chad Little <chad@phacility.com>
Date:   Sat Jun 13 20:32:45 2015 +0100

    [Redesign] Move basefont to Lato, remove Source Sans Pro

    Summary: Working towards a more unified look and feel. This brings in Lato as a complete base font over Helvetica Neue, as well as removing Source Sans Pro from DocumentView and Conpherence. Design-wise Lato provides the nice readability at larger font sizes that Source Sans Pro did, with the ability to scale down to tables and UI widgets with ease. This gives us one font instead of two, and now Object descriptions and Timeline posts all can benefit from a consistent, readable font.

    Test Plan:
    Test main UI, smaller elements like tables, menus, DocumentViews, Previews, Conpherence.

    {F498135}

    {F498136}

    Reviewers: btrahan, epriestley

    Reviewed By: epriestley

    Subscribers: epriestley, Korvin

    Differential Revision: https://secure.phabricator.com/D13276