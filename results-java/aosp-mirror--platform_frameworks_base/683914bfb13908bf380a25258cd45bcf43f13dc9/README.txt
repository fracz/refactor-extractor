commit 683914bfb13908bf380a25258cd45bcf43f13dc9
Author: Svetoslav <svetoslavganov@google.com>
Date:   Thu Jan 15 14:22:26 2015 -0800

    Rewrite of the settings provider.

    This change modifies how global, secure, and system settings are
    managed. In particular, we are moving away from the database to
    an in-memory model where the settings are persisted asynchronously
    to XML.

    This simplifies evolution and improves performance, for example,
    changing a setting is down from around 400 ms to 10 ms as we do not
    hit the disk. The trade off is that we may lose data if the system
    dies before persisting the change.

    In practice this is not a problem because 1) this is very rare;
    2) apps changing a setting use the setting itself to know if it
    changed, so next time the app runs (after a reboot that lost data)
    the app will be oblivious that data was lost.

    When persisting the settings we delay the write a bit to batch
    multiple changes. If a change occurs we reschedule the write
    but when a maximal delay occurs after the first non-persisted
    change we write to disk no matter what. This prevents a malicious
    app poking the settings all the time to prevent them being persisted.

    The settings are persisted in separate XML files for each type of
    setting per user. Specifically, they are in the user's system
    directory and the files are named: settings_type_of_settings.xml.

    Data migration is performed after the data base is upgraded to its
    last version after which the global, system, and secure tables are
    dropped.

    The global, secure, and system settings now have the same version
    and are upgraded as a whole per user to allow migration of settings
    between these them. The upgrade steps should be added to the
    SettingsProvider.UpgradeController and not in the DatabaseHelper.

    Setting states are mapped to an integer key derived from the user
    id and the setting type. Therefore, all setting states are in
    a lookup table which makes all opertions very fast.

    The code is a complete rewrite aiming for improved clarity and
    increased maintainability as opposed to using minor optimizations.
    Now setting and getting the changed setting takes around 10 ms. We
    can optimize later if needed.

    Now the code path through the call API and the one through the
    content provider APIs end up being the same which fixes bugs where
    some enterprise cases were not implemented in the content provider
    code path.

    Note that we are keeping the call code path as it is a bit faster
    than the provider APIs with about 2 ms for setting and getting
    a setting. The front-end settings APIs use the call method.

    Further, we are restricting apps writing to the system settings.
    If the app is targeting API higher than Lollipop MR1 we do not
    let them have their settings in the system ones. Otherwise, we
    warn that this will become an error. System apps like GMS core
    can change anything like the system or shell or root.

    Since old apps can add their settings, this can increase the
    system memory footprint with no limit. Therefore, we limit the
    amount of settings data an app can write to the system settings
    before starting to reject new data.

    Another problem with the system settings was that an app with a
    permission to write there can put invalid values for the settings.
    We now have validators for these settings that ensure only valid
    values are accepted.

    Since apps can put their settings in the system table, when the
    app is uninstalled this data is stale in the sytem table without
    ever being used. Now we keep the package that last changed the
    setting and when the package is removed all settings it touched
    that are not in the ones defined in the APIs are dropped.

    Keeping in memory settings means that we cannot handle arbitrary
    SQL operations, rather the supported operations are on a single
    setting by name and all settings (querying). This should not be
    a problem in practice but we have to verify it. For that reason,
    we log unsupported SQL operations to the event log to do some
    crunching and see what if any cases we should additionally support.

    There are also tests for the settings provider in this change.

    Change-Id: I941dc6e567588d9812905b147dbe1a3191c8dd68