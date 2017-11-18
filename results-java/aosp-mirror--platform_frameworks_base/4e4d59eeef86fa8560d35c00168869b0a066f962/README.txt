commit 4e4d59eeef86fa8560d35c00168869b0a066f962
Author: Lorenzo Colitti <lorenzo@google.com>
Date:   Thu Sep 10 18:12:18 2015 +0900

    Connect the DHCP UDP socket to the server.

    This makes it so that the socket cannot receive datagrams from
    anybody except the DHCP server. This does not improve security,
    because we never read from the UDP socket anyway, but it does
    make ListeningPortsTest pass.

    Bug: 23906864
    Bug: 23933386
    Change-Id: Ib090273a417f7eb2ac1ee3309260249b72fb8345