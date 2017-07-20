commit 98bd694e2bd47b0c4ed8247546b1903c762ffdde
Author: Francois Ferrand <thetypz@gmail.com>
Date:   Mon Jun 30 17:49:32 2014 +0200

    Implement LDAP user lookup.

    This is required to improve compatibility when the DN cannot be easily
    computed from the user name. Additionally, this allows automatically
    getting the full name and email address from LDAP.