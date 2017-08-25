commit 8d53b1da305d26b27054135281f4739d3ed945d6
Author: Steve Clay <steve@mrclay.org>
Date:   Wed Jan 15 15:51:18 2014 -0500

    security(tokens): adds HMAC API and improves token-based security

    Uses HMAC and constant-time string comparison for tokens in actions,
    friend invites, password resets, and email validation.