commit e7c76d92d546b2b331a3aa1b10fb182b4320f4f9
Author: epriestley <git@epriestley.com>
Date:   Tue Mar 28 12:47:58 2017 -0700

    Make `bin/search init` messaging a little more consistent

    Summary:
    Ref T12450. This mostly just smooths out the text a little to improve consistency. Also:

      - Use `isWritable()`.
      - Make the "skipping because not writable" message more clear and tailored.
      - Try not to use the word "index" too much to avoid confusion with `bin/search index` -- instead, talk about "initialize a service".

    Test Plan: Ran `bin/search init` with a couple of different (writable / not writable) configs, saw slightly clearer messaging.

    Reviewers: chad, 20after4

    Reviewed By: 20after4

    Maniphest Tasks: T12450

    Differential Revision: https://secure.phabricator.com/D17572