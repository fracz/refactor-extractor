commit 07c304a5593e4fc7d574964bbf3641f92c16d8fa
Author: marjohn56 <martin@queens-park.com>
Date:   Sat Mar 11 09:59:44 2017 +0000

    WAN flap loss of IPv6

    Some hardware is taking too long to set ACCEPT_RTADV on the Interface,
    this results in RTSOLD exiting and this not sending RS to start the
    process. Apart from adding a delay to the start of RTSOLD which did
    improve but not totally fix the issue the other change is to prevent the
    call to -ACCEPT_RTADV if the interface is using DHCP6.

    -ACCEPT_RTADV in the case of wancfg['dhcp6usev4iface'] || $wancfg['ipaddr']==='ppp'

    Cleaning up dhcp6c kill calls.

    ppp-ipv6
    Changed to call kill_dhcp6client_process() to make
    sure the lock files are also cleared.

    Interfaces.php
    Changed to call kill_dhcp6client_process() to make
    sure the lock files are also cleared.