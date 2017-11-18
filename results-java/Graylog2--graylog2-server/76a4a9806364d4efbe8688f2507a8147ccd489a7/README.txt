commit 76a4a9806364d4efbe8688f2507a8147ccd489a7
Author: Kay Roepke <kroepke@googlemail.com>
Date:   Thu May 4 17:33:34 2017 +0200

    Refactor FileInfo to never return null or throw exceptions (#3776)

    * refactor FileInfo to never return null or throw exceptions

    this makes the class easier to use. if a file isn't present or inaccessible, return a null object that allows rechecking for modifications.
    if the size of the file is the same after modification, this class can only detect changes if the modification time has changes. the resolution of that is typically only a second

    * added license header

    * fix forbiddenapis checks