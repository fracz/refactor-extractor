commit 6aff7f4f2bf0c6418fb88e967653f2feeb83819e
Author: Lukacs Berki <lberki@google.com>
Date:   Wed Dec 9 08:57:08 2015 +0000

    Refactor external repository support significantly to solve a number of issues.

    In particular:

    - Separate the implementation of maven_server into a RepositoryFunction and one that creates the MavenServerValue (ideally, maven_server wouldn't exist but we'll have to make to for the time being)
    - Refactor the logic of determining whether an external repository needs to be re-fetched to RepositoryDelegatorFunction
    - Make RepositoryFunctions not be SkyFunctions anymore (they are called from RepositoryDelegatorFunction, though, who *is* a SkyFunction)
    - Add a Skyframe dirtiness checker that makes --nofetch RepositoryValues not be cached
    - Add a bunch of test cases and javadoc

    There is only one wart that I know of that remains: changes to BUILD files of new_* repository rules that weren't refetched when their RepositoryValue was initiall created on server restart won't take effect. This is because we don't add those BUILD files to the created RepositoryValue. This will fix itself once the ExternalFilesHelper refactoring is submitted.

    --
    MOS_MIGRATED_REVID=109768345