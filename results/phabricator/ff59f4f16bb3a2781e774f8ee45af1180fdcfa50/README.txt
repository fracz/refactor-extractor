commit ff59f4f16bb3a2781e774f8ee45af1180fdcfa50
Author: Chad Little <chad@chadsdomain.com>
Date:   Wed Apr 8 09:41:56 2015 -0700

    Send Markdown in Diffusion through SourceSans

    Summary: This moves Markdown rendering from normal fonts to PHUIDocumentView with Source Sans improving readability of this longer form text.

    Test Plan:
    Test libphutil and Phabricator readmes in my sandbox.

    {F363483}

    Reviewers: btrahan, epriestley

    Reviewed By: epriestley

    Subscribers: Korvin, epriestley

    Differential Revision: https://secure.phabricator.com/D12330