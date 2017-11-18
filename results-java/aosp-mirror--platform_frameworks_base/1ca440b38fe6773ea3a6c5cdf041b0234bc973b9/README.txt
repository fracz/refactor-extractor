commit 1ca440b38fe6773ea3a6c5cdf041b0234bc973b9
Author: Yorke Lee <yorkelee@google.com>
Date:   Wed Jun 4 18:01:46 2014 -0700

    API changes per API review for contacts

    * Improve documentation for REMOVE_DUPLICATE_ENTRIES
    * Hide android.provider.ContactsContract.PinnedPositions, it will be
    reworked and improved for L.
    * Hide android.provider.ContactsContract.Preferences - we will rework
    the various apps that are relying on this to use their own preferences
    instead
    * Remove inheritance of hidden interfaces in android.provider.Contacts

    Bug: 15430304
    Change-Id: I0f96e8a506083df21023f9b95655f0ce5244bdce