commit 14f5dd0e69e31c4f5763cc1ab9c6d4e7c706e47e
Author: Steve Clay <steve@mrclay.org>
Date:   Wed Aug 2 21:14:55 2017 -0400

    chore(core): Overhauls installer, boot, and configuration

    Application composes the service provider, configuration, and the request object
    more cleanly, allowing more control over each.

    The boot process loads all config values from the DB, so reads no longer trigger
    a read. Config names are no longer auto-trimmed.

    Moves all global constants to `engine/lib/constants.php`.
    Exceptions are handled in Application (available earlier in boot)
    Removes unneeded /rewrite.php endpoint.
    Removes all core usage of $CONFIG.
    Config setup occurs in Application.
    Sensitive values like DB credentials/site secret are moved to services early.
    DB credentials *can* be accessed from Application.
    No more confusing aliases.
    Keys can be made read-only.
    The site key is set up early and not kept in config.
    CLI-server gets URLs right again.
    Quieter tests.
    Re-implements UI for settings set in DB.
    Cleans up session boot.
    Removes unneeded code after `last_action` update.
    Migrates cache server to Request/Response.
    Batch installer no longer silently uses non-matching existing settings file values.
    Allows booting with unsaved `ElggSite`.
    Sets initial subtypes in schema file.
    All installer controller method names start with "run".

    **chore(events): events are consolidated into the plugin hook service**

    Having these two very similar services constructed and traveling separately
    made it more likely that an API would end up with inconsistent instances shared
    between them. This should ease future refactoring.

    **chore(notifications): lower log level for notification event results**

    **fix(cache): 304 responses work on nginx**

    Instead of appending `-gzip`, nginx prepends `W/` to the ETag. This change
    allows the cache handler to implement conditional GET on nginx.

    **chore(core): big cleanup of core services**

    Removed globals `$_ELGG` and `$_PAM_HANDLERS`.
    State moved to services where possible.
    More parameterized queries in entities.

    BREAKING CHANGES:
    Config functions like `elgg_get_config` no longer trim keys.

    Some `$CONFIG` modifications will fail. See `docs/guides/upgrading.rst`.

    The config value `entity_types` is no longer present or used.

    If you override the view `navigation/menu/user_hover/placeholder`, you must
    change the config key `lazy_hover:menus` to `elgg_lazy_hover_menus`.