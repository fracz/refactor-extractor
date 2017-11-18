commit e4a4bc9c1442aadc7d4774fb90811ae45246ca1c
Author: Eugene Kudelevsky <Eugene.Kudelevsky@jetbrains.com>
Date:   Mon Jul 22 13:42:18 2013 +0400

    IDEA-110808 support 'rename application package' refactoring which doesn't change java package names, but updates relative component names in manifest; reference from package attribute now only provides completion and is not updated after rename