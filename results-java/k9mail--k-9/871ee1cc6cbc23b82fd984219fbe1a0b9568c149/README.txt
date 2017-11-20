commit 871ee1cc6cbc23b82fd984219fbe1a0b9568c149
Author: Joe Steele <joe@madewell.net>
Date:   Sun Feb 9 22:02:23 2014 -0500

    IMAP authentication improvements

    Changes:

    Implement the PLAIN SASL mechanism.  IMAPv4rev1 assures its availability
    so long as the connection is encrypted.  The big advantage of PLAIN over
    IMAP "LOGIN" is that PLAIN uses UTF-8 encoding for the user name and
    password, whereas "LOGIN" is only safe for 7-bit US-ASCII -- the encoding
    of 8-bit data is undefined.

    (Note that RFC 6855 says that IMAP "LOGIN" does not support UTF-8, and
    clients must use IMAP "AUTHENTICATE" to pass UTF-8 user names and
    passwords.)

    Honor the "LOGINDISABLED" CAPABILITY (RFC 2595) when the server declares
    it.  There's no sense transmitting a password in the clear when it is
    known that it will be rejected.

    No attempt is made to try CRAM-MD5 if the server doesn't profess to
    support it in its CAPABILITY response. (This is the same behavior as
    Thunderbird.)

    Extract code from ImapConnection.open into new method
    ImapConnection.login.

    Extract code from ImapConnection.executeSimpleCommand into new method
    ImapConnection.readStatusResponse.

    Related issues:  6015, 6016