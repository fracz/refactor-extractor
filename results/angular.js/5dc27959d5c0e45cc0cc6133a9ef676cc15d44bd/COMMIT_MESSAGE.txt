commit 5dc27959d5c0e45cc0cc6133a9ef676cc15d44bd
Author: Tobias Bosch <tbosch1009@gmail.com>
Date:   Mon Jan 6 12:19:51 2014 -0800

    chore(build): refactor build scripts in prepare/publish phase

    Refactored all scripts so that they are divided into a `prepare`
    and a `publish` phase. By this we can build, test, tag, commit
    everything first. Only if all of this is ok we start pushing
    to Github. By this we keep Github consistent even in error cases.

    Extracted include script `/scripts/utils.inc`:
    - parse and validate named arguments in the style
      `--name=value`
    - proxy git command and deactivate `git push` based on
      command option `--git_push_dry_run=true`
      (will be inherited to child scripts)
    - enable/disable bash debug mode by command option
      `--verbose=true`
    - dispatch to functions based on command option
      `--action=...`
    - helper functions for dealing with json files