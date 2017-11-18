commit ae93bfb01d8a62130bc5904b41d26c038699419d
Author: Alistair Jones <alistair.jones@gmail.com>
Date:   Thu Apr 18 09:48:12 2013 +0100

    REST transactions: documentation and timeout improvements.

    o Re-write most of the English in the documentation.
    o Add note about using REST transactions in HA.
    o Replace StatusCode annotations with constructor parameters.
    o Avoid race conditions when rolling back timed out transactions.