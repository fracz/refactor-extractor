commit e0b86cc81b39e73bb227643125b909f52f12abdc
Author: epriestley <git@epriestley.com>
Date:   Fri Sep 16 03:56:23 2011 -0700

    Add a Mercurial commit discovery daemon

    Summary:
    Repository import has three major steps:

      - Commit discovery (serial)
      - Message parsing (parallel, mostly VCS independent)
      - Change parsing (parallel, highly VCS dependent)

    This implements commit discovery for Mercurial, similar to git's parsing:

      - List the heads of all the branches.
      - If we haven't already discovered them, follow them back to their roots (or
    the first commit we have discovered).
      - Import all the newly discovered commits, oldest first.

    This is a little complicated but it ensures we discover commits in depth order,
    so the discovery process is robust against interruption/failure. If we just
    inserted commits as we went, we might read the tip, insert it, and then crash.
    When we ran again, we'd think we had already discovered commits older than HEAD.

    This also allows later stages to rely on being able to find Phabricator commit
    IDs which correspond to parent commits.

    NOTE: This importer is fairly slow because "hg" has a large startup time
    (compare "hg --version" to "git --version" and "svn --version"; on my machine,
    hg has 60ms of overhead for any command) and we need to run many commands (see
    the whole "hg id" mess). You can expect something like 10,000 per hour, which
    means you may need to run overnight to discover a large repository (IIRC, the
    svn/git discovery processes are both about an order of magnitude faster). We
    could improve this with batching, but I want to keep it as simple as possible
    for now.

    Test Plan: Discovered all the commits in the main Mercurial repository,
    http://selenic.com/repo/hg.

    Reviewers: Makinde, jungejason, nh, tuomaspelkonen, aran

    Reviewed By: Makinde

    CC: aran, Makinde

    Differential Revision: 943