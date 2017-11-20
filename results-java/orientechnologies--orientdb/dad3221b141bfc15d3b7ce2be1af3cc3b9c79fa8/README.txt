commit dad3221b141bfc15d3b7ce2be1af3cc3b9c79fa8
Author: l.garulli <l.garulli@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Thu Nov 4 14:24:09 2010 +0000

    new ORecordTracked* collection and map classes that keep track of changes and propagate them to the owner document if any. Lazy collections and maps have been refactored to extend them