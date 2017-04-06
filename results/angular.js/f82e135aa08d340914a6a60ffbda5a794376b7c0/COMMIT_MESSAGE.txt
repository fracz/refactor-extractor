commit f82e135aa08d340914a6a60ffbda5a794376b7c0
Author: Martin Staffa <mjstaffa@gmail.com>
Date:   Wed Jun 8 18:46:50 2016 +0200

    refactor(ngList): access ngList attribute directly

    The original way is not necessary since $compile doesn't remove
    white-space anymore