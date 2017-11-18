commit 8d51b4548f2554de694ba45b6f3f34b9829643c1
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Wed Dec 7 21:12:11 2011 +0100

    Moved some complexity from ResourceHandler to FileResolver.

    More refactorings after the pairing session driven by tarTree changes. Now the fileResolver deals with resolving resources which should be more flexible down the road.