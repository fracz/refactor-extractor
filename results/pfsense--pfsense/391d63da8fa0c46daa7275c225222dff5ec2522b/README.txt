commit 391d63da8fa0c46daa7275c225222dff5ec2522b
Author: Renato Botelho <renato@netgate.com>
Date:   Wed Feb 17 09:57:55 2016 -0200

    Fix #4675

    Following bugs and improvements on DHCPv6 DDNS area, obtained from
    PR #1638 from @Robert-Nelson:

    - Use correct domain (ddnsdomain) instead of (domain)
    - The option "deny client-updates" wasn't being set so forward entries
    weren't being added. Allow user to chose between allow, deny or ignore
    - Implement reverse DNZ zone information (PTR)