commit 62e305376945022e0efb91a1f3a98d6eb2538749
Author: Fabien Potencier <fabien.potencier@gmail.com>
Date:   Tue Feb 15 05:58:18 2011 +0100

    refactored previous commit, fixed tests

    How to upgrade?

    For XML configuration files:

     * All extensions should now use the config tag (this is just a convention as
       the YAML configurations files do not use it anymore):

     * The previous change means that the doctrine and security bundles now are
       wrapped under a main "config" tag:

            <doctrine:config>
                <doctrine:orm />
                <doctrine:dbal />
            </doctrine:config>

            <security:config>
                <security:acl />
                ...
            </security:config>

    For YAML configuration files:

     * The main keys have been renamed as follows:

            * assetic:config -> assetic
            * app:config -> framework
            * webprofiler:config -> web_profiler
            * doctrine_odm.mongodb -> doctrine_mongo_db
            * doctrine:orm -> doctrine: { orm: ... }
            * doctrine:dbal -> doctrine: { dbal: ... }
            * security:config -> security
            * security:acl -> security: { acl: ... }
            * twig.config -> twig
            * zend.config -> zend