commit 6ce61a8fd6423e5b7cd851ebbfab3486523db9b6
Author: Ermal <eri@pfsense.org>
Date:   Tue Apr 20 00:39:16 2010 +0000

    Add a new option which allows the admin user to configure CP so that it automatically enters an MAC passthru entry. The MAC is taken from login details and has to be removed manually. Also do improvements on rules handling and pipes. Add some optmizations. Teach the GUI/backend on ip/mac passthrough to configure a bw limit for this entries.