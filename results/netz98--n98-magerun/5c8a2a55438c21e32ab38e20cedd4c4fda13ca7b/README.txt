commit 5c8a2a55438c21e32ab38e20cedd4c4fda13ca7b
Author: Alexander Menk <a.menk@imi.de>
Date:   Thu Apr 11 17:38:52 2013 +0200

    dev:console improvement / fix for php 5.4

    * Show a message if Magento installation failed
    * realpath on a phar:// URL fails on php 5.4 (Ubuntu 12.10) - so just removed the realpath call and it works