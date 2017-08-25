commit 30e891e08fcdf0bca4982e037bedc4672484f19f
Author: David Mudrák <david@moodle.com>
Date:   Thu Jun 15 20:51:58 2017 +0200

    MDL-46418 repositories: Fix how enabled repositories are populated

    Repository instances are stored in the 'repository' table. Repositories
    in the table are either 'Enabled and visible' or 'Enabled but hidden'.
    Hidden repositories still serve their files, but are not visible in the
    filepicker UI. Disabling a repository instance removes its record from
    the table.

    In the original implementation of the plugin manager (see b9934a17), the
    method plugintype_repository::get_enabled_repositories() correctly
    returned all records from the repository table. Then as a part of the
    bigger refactoring in MDL-41437, the commit bde002b8 replaced the
    original method with the new get_enabled_plugins() one which started to
    return visible repositories only.

    As a consequence, the admin tree stopped populating setting page nodes
    for hidden repository instances. So attempting to visit their setting
    page threw a section error. Credit goes to Ike Quigley for debugging and
    tracing this down.

    This patch fixes the way how the list of enabled repositories is
    populated by the plugin manager so that both visible and hidden
    repositories are returned again. This does not affect the filepicker
    itself as it is using its own methods for obtaining the list.