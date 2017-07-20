commit be861f090a09a6612cbbb33ed1195cf57d0d5ffb
Author: Jordi Boggiano <j.boggiano@seld.be>
Date:   Sun Mar 10 13:32:59 2013 +0100

    Remove filterPackages and add RepositoryInterface::search, refactor all commands to use new methods and remove all usage of the full package list for Composer repositories that support providers, fixes #1646