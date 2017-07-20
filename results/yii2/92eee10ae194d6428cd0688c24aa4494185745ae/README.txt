commit 92eee10ae194d6428cd0688c24aa4494185745ae
Author: Boudewijn Vahrmeijer <info@dynasource.eu>
Date:   Thu Dec 8 21:22:18 2016 +0100

    Change the name of method getQueryTableName and remove its $query argument (#12893)

    * refactores getQueryTableName:
    * replaces the $query argument with a $this implementation

    * exposes getQueryTableName to be public instead of private. Fixes #12878

    * added unit tests for exposed method

    * updated changelog

    * - methodname changed to 'getTableNameAndAlias'
    - scope back to private
    - added @internal tag to emphasize that the method is used purely for the internal workings of this piece of software.
    - removed changelog (as the API has not changed)

    * update tests