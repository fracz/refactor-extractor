commit cec6441755c8a273afc36800a7a60dfb7b517214
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Nov 17 17:03:08 2011 +0100

    GRADLE-947 tarTree area. In order to make the dsl symmetric with Tar and also more natural I added a getter for compression. This will also be helpful with next steps of the refactoring. At the moment, when custom decompressor is used we return Compression.NONE - it will get fixed later.