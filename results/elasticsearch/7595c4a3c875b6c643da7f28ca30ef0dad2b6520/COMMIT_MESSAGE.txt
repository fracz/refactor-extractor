commit 7595c4a3c875b6c643da7f28ca30ef0dad2b6520
Author: Robert Muir <rmuir@apache.org>
Date:   Wed Dec 9 13:22:14 2015 -0500

    Improve network docs

    This makes some minor improvements (does not fix all problems!)

    It reorders unicast disco in elasticsearch.yml to be right after the network host,
    for better locality.

    It removes the warning (unreleased) about publish addresses, lets try to really discourage setting
    that unless you need to (behind a proxy server). Most people should be fine with `network.host`

    Finally it reorganizes the network docs page a bit:

    We add a table of 4 "basic" settings at the very beginning:

    * network.host
    * discovery.zen.ping.unicast.hosts
    * http.port
    * transport.tcp.port

    The first two being the most important, which addresses to bind and talk to, and the other two
    being the port numbers.

    The rest of the stuff I tried to simplify and reorder under "advanced" headers.

    This is just a quick stab, I still think we need more effort into this thing, but we gotta start somewhere.