commit 01b4040a27980dfa6d68218f238a61a746c2ba15
Author: Petr Škoda <commits@skodak.org>
Date:   Wed Jan 9 15:54:11 2013 +0100

    MDL-37429 zipping improvements

    Changes:
    * zip_packer can create empty zip archives
    * new option to ignore problematic files when creating archive
    * detection of non-existent files
    * debugging messages for opening of faulty zip archives
    * coding style improvements
    * no PHP 5.2 hacks
    * more unit tests