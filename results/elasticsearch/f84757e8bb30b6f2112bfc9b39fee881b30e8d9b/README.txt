commit f84757e8bb30b6f2112bfc9b39fee881b30e8d9b
Author: Clinton Gormley <clint@traveljury.com>
Date:   Tue Jul 21 18:04:52 2015 +0200

    Refactored check_license_and_sha.pl to accept a license dir and package path

    In preparation for the move to building the core zip, tar.gz, rpm, and deb as separate modules, refactored check_license_and_sha.pl to:

    * accept a license dir and path to the package to check on the command line
    * to be able to extract zip, tar.gz, deb, and rpm
    * all packages except rpm will work on Windows