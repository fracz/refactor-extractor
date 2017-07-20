commit 11786fb1cc84f343ed5237b57fdd9c0575333f35
Author: epriestley <git@epriestley.com>
Date:   Fri Jan 24 12:29:03 2014 -0800

    Don't try to set anonymous session cookie on CDN/file domain

    Summary:
    Ref T2380. If an install has a CDN domain configured, but does not list it as an alternate domain (which is standard/correct, but not incredibly common, see T2380), we'll currently try to set anonymous cookies on it. These will correctly fail security rules.

    Instead, don't try to set these cookies.

    I missed this in testing yesterday because I have a file domain, but I also have it configured as an alternate domain, which allows cookies to be set. Generally, domain management is due for some refactoring.

    Test Plan: Set file domain but not as an alternate, logged out, nuked file domain cookies, reloaded page. No error after patch.

    Reviewers: btrahan, csilvers

    Reviewed By: btrahan

    CC: aran

    Maniphest Tasks: T2380

    Differential Revision: https://secure.phabricator.com/D8057