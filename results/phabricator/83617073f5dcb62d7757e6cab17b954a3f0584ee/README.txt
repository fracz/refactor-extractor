commit 83617073f5dcb62d7757e6cab17b954a3f0584ee
Author: epriestley <git@epriestley.com>
Date:   Thu Apr 23 08:06:46 2015 -0700

    Rename TypehaeadUserParameterizedDatasource to PeopleUserFunctionDatasource

    Summary: Ref T4100. This improves application scoping (Typeahead -> People) and uses a more consitent and concise name (Parameterized -> Function).

    Test Plan:
      - `grep`
      - Used affected datasource.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Maniphest Tasks: T4100

    Differential Revision: https://secure.phabricator.com/D12532