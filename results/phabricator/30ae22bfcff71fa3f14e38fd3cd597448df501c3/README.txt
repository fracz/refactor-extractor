commit 30ae22bfcff71fa3f14e38fd3cd597448df501c3
Author: epriestley <git@epriestley.com>
Date:   Mon Mar 19 19:52:14 2012 -0700

    Fix many encoding and architecture problems in Diffusion request and URI handling

    Summary:
    Diffusion request/uri handling is currently a big, hastily ported mess. In particular, it has:

      - Tons and tons of duplicated code.
      - Bugs with handling unusual branch and file names.
      - An excessively large (and yet insufficiently expressive) API on DiffusionRequest, including a nonsensical concrete base class.
      - Other tools were doing hacky things like passing ":" branch names.

    This diff attempts to fix these issues.

      - Make the base class abstract (it was concrete ONLY for "/diffusion/").
      - Move all URI generation to DiffusionRequest. Make the core static. Add unit tests.
      - Delete the 300 copies of URI generation code throughout Diffusion.
      - Move all URI parsing to DiffusionRequest. Make the core static. Add unit tests.
      - Add an appropriate static initializer for other callers.
      - Convert all code calling `newFromAphrontRequestDictionary` outside of Diffusion to the new `newFromDictionary` API.
      - Refactor static initializers to be sensibly-sized.
      - Refactor derived DiffusionRequest classes to remove duplicated code.
      - Properly encode branch names (fixes branches with "/", see <https://github.com/facebook/phabricator/issues/100>).
      - Properly encode path names (fixes issues in D1742).
      - Properly escape delimiter characters ";" and "$" in path names so files like "$100" are not interpreted as "line 100".
      - Fix a couple warnings.
      - Fix a couple lint issues.
      - Fix a bug where we would not parse filenames with spaces in them correctly in the Git browse query.
      - Fix a bug where Git change queries would fail unnecessarily.
      - Provide or improve some documentation.

    This thing is pretty gigantic but also kind of hard to split up. If it's unreasonably difficult to review, let me know and I can take a stab at it though.

    This supplants D1742.

    Test Plan:
      - Used home, repository, branch, browse, change, history, diff (ajax), lastmodified (ajax) views of Diffusion.
      - Used Owners typeaheads and search.
      - Used diffusion.getrecentcommitsbypath method.
      - Pushed a change to an absurdly-named file on an absurdly-named branch, everything worked properly.

    {F9185}

    Reviewers: nh, vrana, btrahan

    Reviewed By: btrahan

    CC: aran, epriestley

    Differential Revision: https://secure.phabricator.com/D1921