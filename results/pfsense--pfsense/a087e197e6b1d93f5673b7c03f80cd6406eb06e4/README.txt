commit a087e197e6b1d93f5673b7c03f80cd6406eb06e4
Author: Phil Davis <phil.davis@inf.org>
Date:   Mon Feb 23 22:16:12 2015 +0545

    OpenVPN server improve handling of authmode

    Currently if the user is clicking around while they are setting up an OpenVPN server, they can do stuff like this:
    a) Select Server Mode - Remote Access (SSL/TLS + User Auth)
    b) Select something in Backend for authentication
    c) Change their mind and select Server Mode - Peer to Peer (SSL/TLS)
    d) Enter the other settings and Save
    Now the OpenVPN server config has an 'authmode' key in it.
    Probably does no harm, I suspect it will simply not be used when building the server.conf for Peer to Peer, but it looks a bit odd when analysing/diagnosing a config for problems.
    Other fields that are mode-specific have tests to only save the values at the end if the appropriate mode is the one finally selected.

    While I am here, I also constantly forget to click on "Local Database" authmode when setting up a new server. It gives the validation error message, then I click on "Local Database" again and save. Seems unnecessary - when defining a new OpenVPN server why not default this to have the first entry in the list be the one selected? So I did that. What do you think? 1 place less for many users to need to click.