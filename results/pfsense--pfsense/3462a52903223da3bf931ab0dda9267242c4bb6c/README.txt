commit 3462a52903223da3bf931ab0dda9267242c4bb6c
Author: Matthew Grooms <mgrooms@pfsense.org>
Date:   Sun Jul 13 23:28:45 2008 +0000

    Introduce a new and improved version of IPsec mobile client support. The
    mobile client tab is now used to configure user authentication (Xauth) and
    client configuration (mode-cfg) options. User authentication is currently
    limited to system password file entries. This will be extended to support
    external RADIUS and LDAP account DBs in a follow up comiit.