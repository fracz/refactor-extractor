commit 0dee00b16d4210b0e4e66673d51b443472a026b8
Author: Chris Gioran <chris.gioran@neotechnology.com>
Date:   Mon Jun 2 11:55:30 2014 +0200

    IndexCommand now accepts NeoCommandVisitor

    IndexCommands are now written and read with NeoCommandVisitors
     as all other NeoCommands are.
    Slight refactorings all around