commit 927e70ca2128598e069189d219306614bdfba77f
Author: jrfnl <github_nospam@adviesenzo.nl>
Date:   Tue Jan 5 03:48:19 2016 +0100

    Variable admin notices based on user level.

    By default the following logic will be used:
    User < `'publish_posts'` (=Author): no admin notices
    User < install/update/activate: "contact administrator notice" without disclosing information about the plugins involved and only if there are *required* plugins which require action.
    User = install/update/activate: "normal" notices

    The minimum user level for which admin notices are shown at all can be adjusted by using the newly introduced `tgmpa_show_admin_notice_capability` filter. The default capability is set to `'publish_posts'`.

    Example:
    add_filter( 'tgmpa_show_admin_notice_capability', create_function( '$cap', 'return \'edit_pages\';' ) );

    Another example:
    To only show the admin notices to network admins on multisite, set it to a super admin capability like `'manage_network_plugins'`.

    Note: the `notices()` function is ugly and in desperate need of refactoring, that is not handled in this PR (which only makes it worse).

    Fixes #190, #414
    Supersedes: https://github.com/INN/Largo/pull/740/files
    Partially fixes #479, #489 - notice will now only show for required updates for non-admin users with level author or editor.
    Possibly fixes #492, though more information is needed on the actual case.