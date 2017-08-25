commit 7462076c1d38bbe429ef40d5abacd6b15f19071c
Author: Tom Klingenberg <tklingenberg@lastflood.net>
Date:   Sat May 30 21:44:28 2015 +0200

    fixed and improved build

    Build dependencies were not under configuration management. This has been
    fixed by putting those under build/composer.lock.

    Additionally a build.sh with rudimentary build instructions has been
    created and tested under git bash.

    Another fix for the patched phar package task has been done. It didn't
    reflect the configured compression method. this has been fixed.

    Additionally as the phar compressin bug is not always existent, now the
    patched task tries the much faster method first and uses
    single-file-compression only as a fall-back in case of a RuntimeException.

    Next to that, phpunit.xml has been renamed to phpunit.xml.dist. It is
    equally accepted as default and allows to create a local configuration as
    phpunit.xml - which has been also added to .gitignore.