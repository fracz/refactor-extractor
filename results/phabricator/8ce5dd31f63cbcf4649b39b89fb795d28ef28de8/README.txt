commit 8ce5dd31f63cbcf4649b39b89fb795d28ef28de8
Author: epriestley <git@epriestley.com>
Date:   Sun Oct 2 12:52:54 2011 -0700

    Show open Differential revisions in Diffusion browse views

    Summary:
    Still some rough edges, but this adds a table of open revisions to Diffusion.
    See T262.

    I'll make this a little better (e.g., "see all.." instead of arbitrary 10 cap,
    or maybe move to top-level nav?) but I think I have to refactor some other stuff
    first. This should let us root out any major issues, at least.

    NOTE: You must associate Arcanist Projects with Repositories (in Repositories ->
    Arcanist Projects -> Edit) for this to work!

    Also made paths include all parent paths so that browse views of directories
    will work.

    Test Plan: Uploaded a diff which affected "/blah", it appeared when browsing "/"
    and "/blah".

    Reviewers: jungejason, nh, tuomaspelkonen, aran

    Reviewed By: jungejason

    CC: aran, jungejason

    Differential Revision: 979