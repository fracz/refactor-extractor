commit 8bdb31ed54412f9de7f8d84830ac406a4d57c63a
Author: Petr Skoda <commits@skodak.org>
Date:   Sat Feb 26 15:16:51 2011 +0100

    MDL-26564 fix regressions and other problems in csv user upload

    This patch fixes incorrect password creating, updating and resetting, updating of user fields, unsupported auth plugins are correctly identified, modification of mnethostid is prevented, fixed problem with email duplicates, new password is generated for users without email, etc. It also includes coding style improvements, more inline docs, future TODOs and license information.