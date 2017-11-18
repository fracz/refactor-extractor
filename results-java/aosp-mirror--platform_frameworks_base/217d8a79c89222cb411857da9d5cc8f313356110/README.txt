commit 217d8a79c89222cb411857da9d5cc8f313356110
Author: Yorke Lee <yorkelee@google.com>
Date:   Wed Nov 27 10:14:04 2013 -0800

    ContactsContract api changes

    Make the following fields in ContactsContract public API:
    STREQUENT_PHONE_ONLY
    REMOVE_DUPLICATE_ENTRIES
    CommonDataKinds.Callable.CONTENT_FILTER_URI
    CommonDataKinds.Callable.CONTENT_URI
    ContactOptionsColumns.FULL_NAME_STYLE
    ContactOptionsColumns.PINNED
    Contacts.MULTI_VCARD_URI (and improve documentation)
    PhoneLookup.QUERY_PARAMETER_SIP_ADDRESS (and improve documentation)
    Preferences.SORT_ORDER_PRIMARY
    Preferences.SORT_ORDER_ALTERNATIVE
    Preferences.SORT_ORDER
    Preferences.DISPLAY_ORDER_PRIMARY
    Preferences.DISPLAY_ORDER_ALTERNATIVE
    Preferences.DISPLAY_ORDER
    PinnedPositions.DEMOTED
    PinnedPositions.STAR_WHEN_PINNING
    PinnedPositions.UNDEMOTE
    PinnedPositions.UNPINNED
    PinnedPositions.UPDATE_URI

    Change-Id: I1d70654b4a931b88fff7a3a4b5ffc364978c7406