commit 87ff461470dea6b73e3fde74bcacee4ed3f568db
Author: vrana <jakubv@fb.com>
Date:   Thu May 3 14:46:11 2012 -0700

    Add /F123 shortcut

    Summary:
    I wanted to point someone on a file uploaded to Phabricator and the normal link is just too long.

    I guess that this also improves security. Because pointing someone to the file directly reveals the secret key used in /data/ and it can be served without auth?

    We already use `{F123}` so there will be no conflicts in future because we wouldn't want to reuse it for something else.

    I promote the link on /file/ - it adds one redirect but I think it's worth it. I also considered making the link from the File ID column but there are already too many links (with some duplicity).

    Test Plan:
    /file/
    /F123 (redirect)
    /F9999999999 (404)

    Reviewers: epriestley

    Reviewed By: epriestley

    CC: aran, Koolvin

    Differential Revision: https://secure.phabricator.com/D2380