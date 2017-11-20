commit 64fd04ece2a44815fe7e48c5a4017f0b658ef270
Author: Joe Steele <joe@madewell.net>
Date:   Sat Feb 22 17:51:18 2014 -0500

    POP3 authentication improvements

    Changes:

    Extract code and create login() and authCramMD5() methods.

    Implement the SASL PLAIN authentication mechanism.  Its primary benefit is
    the explicit support for UTF-8.  If the user has configured "PLAIN"
    authentication, then SASL PLAIN will be used, if available, otherwise
    login() will be used.

    Implement POP3 APOP authentication (issue 3218).  If the user has
    configured "CRAM_MD5" authentication (a future commit will change this
    user option to a localized string "Encrypted password"), then SASL
    CRAM-MD5 will be used, if available, otherwise the availability of POP3
    APOP will be checked and used (per RFC 2449, there is no APOP
    "capability").

    Extend getCapabilities() to check for available authentication methods by
    sending the "AUTH" command with no arguments
    (http://tools.ietf.org/html/draft-myers-sasl-pop3-05).  This never became
    a standard, but there are servers that support it, and Thunderbird
    includes this check.

    The SASL PLAIN and CRAM-MD5 authentication methods are not attempted
    unless the server professes to have the appropriate capability.
    (Previously, CRAM-MD5 was tried regardless of capability.)  No check is
    made for the USER capability prior to use of that method.  All this is the
    same behavior as in Thunderbird.

    Eliminate the testing for capabilities in cases where the test results are
    never used (PIPELINING, USER).

    Change when getCapabilities() is called.  It is called once upon
    connection.  If STARTTLS is negotiated (POP3 STLS), then
    getCapabilities() is called again after the connection is encrypted (and
    the server is authenticated), but before user authentication is attempted.