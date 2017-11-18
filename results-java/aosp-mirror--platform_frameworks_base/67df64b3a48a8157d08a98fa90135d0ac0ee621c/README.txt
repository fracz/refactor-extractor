commit 67df64b3a48a8157d08a98fa90135d0ac0ee621c
Author: Amith Yamasani <yamasani@google.com>
Date:   Fri Dec 14 12:09:36 2012 -0800

    Shared accounts and sharing of apps

    API and preliminary implementation for sharing primary user accounts with a secondary user.
    AbstractAccountAuthenticator has new methods to retrieve and apply a bundle of credentials
    to clone an account from the primary to a restricted secondary user. The AccountManagerService
    initiates the account clone when it starts up the user and detects that the user has
    a shared account registered that hasn't been converted to a real account.

    AccountManager also has new hidden APIs to add/remove/get shared accounts. There might be
    further improvements to this API to make shared accounts hidden/visible to select apps.

    AccountManagerService has a new table to store the shared account information.

    Added ability in PackageManager to install and uninstall packages for a secondary user. This
    is required when the primary user selects a few apps to share with a restricted user.

    Remove shared accounts from secondary users when primary user removes the account.

    Change-Id: I9378ed0d8c1cc66baf150a4bec0ede56f6f8b06b