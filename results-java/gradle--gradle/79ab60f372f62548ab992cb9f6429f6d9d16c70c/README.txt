commit 79ab60f372f62548ab992cb9f6429f6d9d16c70c
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Nov 17 19:01:31 2011 +0100

    GRADLE-947 tarTree area. Made sure that the getCompression() works well for Tar (and also in backwards compatible fashion). I think that down the road we should rid the Compression enum because they are somewhat problematic when DSL needs to be refactored.