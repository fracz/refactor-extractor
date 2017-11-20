commit 93e0251d53c3c5f39680b70f82dc8c19f1bd36e4
Author: l.garulli <l.garulli@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Sat Apr 21 18:26:05 2012 +0000

    Fixed issue 797. This required a quite huge refactoring to the storage API to pull up the version at database level to being updated into the record. Local storage now saves the version as: (version+1)*-1. This allow to know if the record is deleted (negative number) but, and this is the news, with last version number. On recycling the version is computed as version*-1. On remote storage the version now is sent back to the clients.