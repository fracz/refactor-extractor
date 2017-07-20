commit dcf909ba56bf4393d493886e63115004e73d1d78
Author: Aviv Eyal <avivey@gmail.com>
Date:   Wed Nov 13 17:25:14 2013 -0800

    Land to GitHub + support stuff

    Summary:
    A usable, Land to GitHub flow.

    Still to do:
    - Refactor all git/hg stratagies to a sane structure.
    - Make the dialogs Workflow + explain why it's disabled.
    - Show button and request Link Account if GH is enabled, but user is not linked.
    - After refreshing token, user ends up in the settings stage.

    Hacked something in LandController to be able to show an arbitrary dialog from a strategy.
    It's not very nice, but I want to make some more refactoring to the controller/strategy/ies anyway.

    Also made PhabricatorRepository::getRemoteURIObject() public, because it was very useful in getting
    the domain and path for the repo.

    Test Plan:
    Went through these flows:
    - load revision in hosted, github-backed, non-github backed repos to see button as needed.
    - hit land with weak token - sent to refresh it with the extra scope.
    - Land to repo I'm not allowed - got proper error message.
    - Successfully landed; Failed to apply patch.

    Reviewers: epriestley, #blessed_reviewers

    Reviewed By: epriestley

    CC: Korvin, epriestley, aran

    Maniphest Tasks: T182

    Differential Revision: https://secure.phabricator.com/D7555