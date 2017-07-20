commit ba489f9d85f351b7844c8922dd4caff78e44daca
Author: epriestley <git@epriestley.com>
Date:   Sun Dec 30 06:16:15 2012 -0800

    Add a local configuration source and a non-environmental ENV config source

    Summary:
    See discussion in T2221. Before we can move configuration to the database, we have a bootstrapping problem: we need database credentials to live //somewhere// if we can't guess them (and we can only really guess localhost / root / no password).

    Some options for this are:

      - Have them live in ENV variables.
        - These are often somewhat unfamiliar to users.
        - Scripts would become a huge pain -- you'd have to dump a bunch of stuff into ENV.
        - Some environments have limited ability to set ENV vars.
        - SSH is also a pain.
      - Have them live in a normal config file.
        - This probably isn't really too awful, but:
        - Since we deploy/upgrade with git, we can't currently let them edit a file which already exists, or their working copy will become dirty.
        - So they have to copy or create a file, then edit it.
        - The biggest issue I have with this is that it will be difficult to give specific, easily-followed directions from Setup. The instructions need to be like "Copy template.conf.php to real.conf.php, then edit these keys: x, y, z". This isn't as easy to follow as "run script Y".
      - Have them live in an abnormal config file with script access (this diff).
        - I think this is a little better than a normal config file, because we can tell users 'run phabricator/bin/config set mysql.user phabricator' and such, which is easier to follow than editing a config file.

    I think this is only a marginal improvement over a normal config file and am open to arguments against this approach, but I think it will be a little easier for users to deal with than a normal config file. In most cases they should only need to store three values in this file -- db user/host/pass -- since once we have those we can bootstrap everything else. Normal config files also aren't going away for more advanced users, we're just offering a simple alternative for most users.

    This also adds an ENVIRONMENT file as an alternative to PHABRICATOR_ENV. This is just a simple way to specify the environment if you don't have convenient access to env vars.

    Test Plan: Ran `config set x y`, verified writes. Wrote to ENVIRONMENT, ran `PHABRICATOR_ENV= ./bin/repository`.

    Reviewers: btrahan, vrana, codeblock

    Reviewed By: codeblock

    CC: aran

    Maniphest Tasks: T2221

    Differential Revision: https://secure.phabricator.com/D4294