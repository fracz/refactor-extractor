commit ad88ff28a1b557f76cd77bc06e970b2fbe6c5147
Author: epriestley <git@epriestley.com>
Date:   Tue Mar 11 15:53:15 2014 -0700

    Reject Phame domains which include a port number

    Summary: Via HackerOne. This doesn't actually have any security impact as far as we can tell, but a researcher reported it since it seems suspicious. At a minimum, it could be confusing. Also improve some i18n stuff.

    Test Plan: Hit all the error cases, then saved a valid custom domain.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: aran, epriestley

    Differential Revision: https://secure.phabricator.com/D8493