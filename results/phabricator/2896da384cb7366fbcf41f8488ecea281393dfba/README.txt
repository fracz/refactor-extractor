commit 2896da384cb7366fbcf41f8488ecea281393dfba
Author: epriestley <git@epriestley.com>
Date:   Tue Apr 4 09:06:00 2017 -0700

    Only require POST to fetch file data if the viewer is logged in

    Summary:
    Ref T11357. In D17611, I added `file.search`, which includes a `"dataURI"`. Partly, this is building toward resolving T8348.

    However, in some cases you can't GET this URI because of a security measure:

      - You have not configured `security.alternate-file-domain`.
      - The file isn't web-viewable.
      - (The request isn't an LFS request.)

    The goal of this security mechanism is just to protect against session hijacking, so it's also safe to disable it if the viewer didn't present any credentials (since that means there's nothing to hijack). Add that exception, and reorganize the code a little bit.

    Test Plan:
      - From the browser (with a session), tried to GET a binary data file. Got redirected.
      - Got a download with POST.
      - From the CLI (without a session), tried to GET a binary data file. Go a download.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T11357

    Differential Revision: https://secure.phabricator.com/D17613