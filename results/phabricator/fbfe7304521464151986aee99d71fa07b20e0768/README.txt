commit fbfe7304521464151986aee99d71fa07b20e0768
Author: epriestley <git@epriestley.com>
Date:   Sat Apr 16 18:17:13 2016 -0700

    Support more transactions types in RepositoryEditEngine

    Summary:
    Ref T10748. This supports more transaction types in the modern editor and improves validation so Conduit benefits.

    You can technically create repositories via `diffusion.repository.edit` now, although they aren't very useful.

    Test Plan:
      - Used `diffusion.repository.edit` to create and edit repositories.
      - Used `/editpro/` to edit repositories.

    Reviewers: chad

    Reviewed By: chad

    Maniphest Tasks: T10748

    Differential Revision: https://secure.phabricator.com/D15740