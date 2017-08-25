commit 5355c285fc5dfce3daf945fa6d5080f6c381d1ee
Author: Arthur Schiwon <blizzz@owncloud.com>
Date:   Tue Mar 3 11:56:03 2015 +0100

    LDAP Wizard Overhaul

    wizard refactor

    reimplement save spinners and cursor

    implement Port detector

    introduced detector queue, added base dn detector

    disable input fields when detectors are running

    introduce spinners for fields that are being updated by detector

    cache jq element objects

    consolidate processing of detector results in generic / abstract base class

    display notification if a detector discovered a problem

    don't run base dn detector if a base is configured

    reset detector queue on configuration switch

    implement functionality check and update of status indicator

    document ConfigModel

    jsdoc for controller and main view

    more documentation

    implement the user filter tab view

    so far the multiselects get initialized (not filled yet) and the mode can be switched.

    mode is also restored.

    reintroduce filter switch confirmation in admin XP mode

    new detector for user object classes. so we also load user object classes if necessary and are able to save and show the setting.

    multiselect trigger save actions now on close only

    show spinners automatically, when a detector is running

    20k limit for object classes preselection test

    adjust wordings, fix grammar

    add group (for users tab) detector

    also includes wording fixes

    error presentation moved from detectors to view, where it belongs

    add info label to users page

    missing wording changes

    show effective LDAP filter in Assisted Mode

    add user filter detector

    implement count button for users and limit all count actions to 1001 for performance reasons

    make port field a bit bigger. not perfect though.

    do not detect port automatically

    implement login filter tab view

    only load features in assisted mode and don't enable assisted fields while in raw mode

    add tooltips on login filter checkbox options for better understanding

    permanently show filter on login tab

    and also compile login filter in assisted mode

    test/verify button on login attributes tab, with backend changes.

    only run wizard requests if your an active tab. also run compile filter requests when switching to assisted mode

    underline toggle filter links to stress that they are clickable

    unity user and group tab functionality in common abstract class, add group filter tab view. only detectors and template adjustments left to have group tab implementation complete

    add object class and group detector for groups as well as filter composer

    show ldap filter permanently on groups tab

    introduce input element that can deal better with many groups, will be used with > 40

    fix disabling complex group chooser while detection is running

    hide complex group chooser on config switch

    fix few more issues with complex chooser

    make complex group chooser available on Users tab as well

    detect base dn improvements/changes:

    - do not look for Base DN automatically, offer a button instead
    - fix for alternative way to detect a base dn (if agent dn is not given)
    - do not trigger filter composers on config switch

    Changes with configuration chooser controls

    - "New" was removed out of the configuration list
    - and split into buttons "add" and "copy"
    - delete button is also now an icon

    add test button for Base DN

    reimplement advanced tab. The save button is gone.

    reimplement expert tab

    remove unused methods

    implement mail attribute detector

    implement user display name attribute detection

    implement member group association detector

    replace text input with textarea for raw filter input

    finish functionality check

    auto-enable good configurations, as it was before

    cleanup

    move save confirmation handling to base class, reduces code duplication

    enable tabs only if no running save processes are left.

    move onConfigLoaded to base class, avoids code duplication

    simplify, save LOCs

    Test Configuration button to be dealt with in main view as it is a cross-tab element

    require detectorQueue in constructor

    cleanup

    put bootstrap into a function and thus make it testable

    get rid of old stuff