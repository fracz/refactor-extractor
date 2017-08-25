commit 855769109030cafa5b38aaf1ad9480b8eb14c004
Author: Nate Good <nate.good@showclix.com>
Date:   Sat Apr 14 14:48:28 2012 -0400

    some refactoring

    Renamed the AbstractMimeHandler to MimeHandlerAdapter seeing as it is more of an adapter pattern
    Adds the newer phar, fixes autoloading for the phar
    Removing building the old single file .php file as the phar is the best option