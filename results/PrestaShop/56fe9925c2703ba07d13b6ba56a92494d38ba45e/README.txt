commit 56fe9925c2703ba07d13b6ba56a92494d38ba45e
Author: Gregory Roussac <gregory.roussac@prestashop.com>
Date:   Tue Mar 24 18:30:35 2015 +0100

    [-] FO : Fix #PSCSX-4326, Incorrect prices on product page using reduction amount and more currencies, first part

    [-] FO : fix disappearing radio button when hovering.

    Update _uniform._base.scss

    // Add shop activity on addons link

    [-] BO : #PSCSX-4326, currency unit to real currency

    // sprintf

    // Submodules updated

    [-] BO : Fix NaN on price if ecotax isn't set

    [*] CORE: PHPDoc: inline docs for local variables in class files

    [*] CORE: PHPDoc: inline docs for variables  in controller files

    [*] CORE: PHPDocs for object property in admin controller classes

    // Missing uniform.default.css.map

    [-] BO: Fix delivery time string language in carrier wizard summary.

    // Rename AdminMeta Robot file hook

    [-] CORE : follow up fe37ea2c11c004d5f1172cdd82cd290b2abe7b8d add the config var like htmlpurifier do

    // oups

    [-] CORE : fix sass errors

    IfModule directives changes in upload/.htaccess

    Changing <IfModule mime.c> to <IfModule mod_mime.c>, with mime.c RemoveHandler and RemoveType directives are ignored.

    Adding IfModule mod_php5.c around php_flag directive. When PHP is run as CGI or FastCGI, php_flag directive without IfModule envelope causes 500 Internal Server Error.

    [+] TEST : PrestaShop Security (prestafraud) test

    // Small improvement on prestashop security automated test

    // currency rate

    // category

    // bo js part

    // partial revert

    // taxes excluded

    // cart

    // templates clear > global.tpl

    // bad commit