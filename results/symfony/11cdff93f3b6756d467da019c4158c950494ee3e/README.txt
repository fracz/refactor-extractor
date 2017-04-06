commit 11cdff93f3b6756d467da019c4158c950494ee3e
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Sun May 1 17:18:30 2011 +0200

    [FrameworkBundle] refactored container:debug command

     * Use a dumper to serialize the container into the cache (XML)
     * Only keep the "real" services (abstract ones are not displayed anymore)