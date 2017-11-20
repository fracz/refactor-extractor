commit e55204433928e47e9ff1450f05d9031db1c2cd9a
Author: LELEU Jérôme <leleuj@gmail.com>
Date:   Mon Aug 22 13:32:11 2016 +0200

    Fixing OAuth / OIDC support (#1961)

    * fix discovery url metadata

    * root url for OIDC is /oidc

    * add missing @Controller + token_type

    * fix Checkstyle issues + doc

    * refactoring controllers declaration + idtokensigningalgs naming