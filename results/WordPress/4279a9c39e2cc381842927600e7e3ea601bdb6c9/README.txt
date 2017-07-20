commit 4279a9c39e2cc381842927600e7e3ea601bdb6c9
Author: Drew Jaynes <info@drewapicture.com>
Date:   Wed Sep 16 10:03:26 2015 +0000

    Docs: Improve documentation for the `WP_Object_Cache` class.

    Spaces out parameter documentation for readability, fixes some minor syntactical issues, and adds some missing `@access` tags or reorders tags according to the PHP docs standards.

    Also, documents `&$found`, the fourth parameter for the `get()` method, and adds missing parameter and return descriptions for the `_exists()` utility method.

    See #32246.

    Built from https://develop.svn.wordpress.org/trunk@34227


    git-svn-id: http://core.svn.wordpress.org/trunk@34191 1a063a9b-81f0-0310-95a4-ce76da25c4cd