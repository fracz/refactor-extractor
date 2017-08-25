commit 4ff284433a56bc03d9dc0fa34f0b76cf58950b28
Author: chris <chris@jalakai.co.uk>
Date:   Fri Sep 8 14:27:44 2006 +0200

    clientIP() update, data cleaning improvements

    as per recent security warning, clientIP() could
    return other arbitrary data along with an IP
    address. This fix ensures only IP addresses can
    be returned by this function.

    darcs-hash:20060908122744-9b6ab-8c90ca361b038a47b65f3f3dbf7228ae569f8c08.gz