commit 6232e9676cd4acb67a747d5463ce9a8092eb802f
Author: epriestley <git@epriestley.com>
Date:   Mon Aug 11 12:13:09 2014 -0700

    Don't send reset links to unverified addresses on accounts with verified addresses

    Summary:
    Via HackerOne. If a user adds an email address and typos it, entering `alinculne@gmailo.com`, and it happens to be a valid address which an evil user controls, the evil user can request a password reset and compromise the account.

    This strains the imagination, but we can implement a better behavior cheaply.

      - If an account has any verified addresses, only send to verified addresses.
      - If an account has no verified addresses (e.g., is a new account), send to any address.

    We've also received several reports about reset links not being destroyed as aggressively as researchers expect. While there's no specific scenario where this does any harm, revoke all outstanding reset tokens when a reset link is used to improve the signal/noise ratio of the reporting channel.

    Test Plan:
      - Tried to send a reset link to an unverified address on an account with a verified address (got new error).
      - Tried to send a reset link to a verified adddress on an account with a verified address (got email).
      - Tried to send a reset link to an invalid address (got old error).
      - Tried to send a reset link to an unverified address on an account with only unverified addresses -- a new user (got email).
      - Requested several reset links, used one, verified all the others were revoked.

    Reviewers: btrahan

    Reviewed By: btrahan

    Subscribers: epriestley

    Differential Revision: https://secure.phabricator.com/D10206