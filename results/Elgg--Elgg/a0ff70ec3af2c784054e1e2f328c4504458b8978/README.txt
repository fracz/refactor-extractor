commit a0ff70ec3af2c784054e1e2f328c4504458b8978
Author: Ismayil Khayredinov <ismayil.khayredinov@gmail.com>
Date:   Thu Apr 14 11:17:36 2016 +0200

    feature(ckeditor): improved elgg/ckeditor AMD module

    elgg/ckeditor/insert has been deprecated and the hook registration was moved
    to elgg/ckeditor module.

    elgg/ckeditor module now triggers "prepare, ckeditor" hook, which can be used
    to decorate the CKEDITOR global, as well as to register new plugins and event handlers.

    Adds elgg/ckeditor#bind method to simplify bootstrapping.