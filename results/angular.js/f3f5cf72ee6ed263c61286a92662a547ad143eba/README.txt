commit f3f5cf72ee6ed263c61286a92662a547ad143eba
Author: mirabilos <t.glaser@tarent.de>
Date:   Tue Jun 7 00:07:46 2016 +0200

    fix(input[email]): improve email address validation

    **Limit size of local-part and total path size in eMail addresses**

    RFC 5321 §4.5.3.1.1 ⇒ local-part can have up to 64 octets
    RFC 5321 §4.5.3.1.3 ⇒ path “including the punctuation and
    element separators” can have up to 256 octets
    RFC 5321 §4.1.2 specifies path as ‘<’ + mailbox¹ + ‘>’ in
    the best case, leaving us 254 octets

    The limitation of the total path size to 254 octets leaves
    at most 252 octets (one local-part, one ‘@’) for the domain,
    which means we don’t need to explicitly check the domain
    size any more (removing the assertion after the ‘@’).

    ① RFC 821/5321 “mailbox” is the same as RFC 822 “addr-spec”

    **Optimise eMail address regex for speed**

    There is no need to make it case-insensitive; the local-part
    already catches the entire range, and the host part is easily
    done. Furthermore, this makes the regex locale-independent,
    avoiding issues with e.g. turkish case conversions.

    cf. http://www.mirbsd.org/cvs.cgi/contrib/hosted/tg/mailfrom.php?rev=HEAD

    **Limit eMail address total host part length**

    RFC 1035 §2.3.4 imposes a maximum length for any DNS object;
    RFC 5321 §2.3.5 references this (and requires FQDNs, but there
    have been cases where a TLD had an MX RR and thus eMail addresses
    like “localpart@tld” are valid).

    Credits: Natureshadow <d.george@tarent.de>

    **Limit eMail address individual host part length**

    A “label” (each of the things between the dots (‘.’) after the ‘@’ in
    the eMail address) MUST NOT be longer than 63 characters.

    cf. http://www.mirbsd.org/cvs.cgi/contrib/hosted/tg/mailfrom.php?rev=HEAD
    and RFC 1035 §2.3.4

    **Fix eMail address local-part validation**

    A period (‘.’) may not begin or end a local-part

    cf. http://www.mirbsd.org/cvs.cgi/contrib/hosted/tg/mailfrom.php?rev=HEAD
    and RFC 822 / 5321

    Closes #14719