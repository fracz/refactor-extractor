commit 572f835eac3ddd387ceb6ae6a8f9dce8f9f442e9
Author: Abyr Valg <valga.github@abyrga.ru>
Date:   Mon May 29 18:55:49 2017 +0300

    Refactor requests, reorder request data (#1273)

    This pull request does the following: Refactors Request multipart form building to much cleaner code to avoid the need for code repetition, adds some more Direct messaging functions, and now always re-orders request params to match the Java order which makes it much easier for developers to compare JSON dumps between the app and this library, without having to care about parameter order.

    The full history can be read in the original pull request:

    https://github.com/mgp25/Instagram-API/pull/1273

    * reorder request data by hashCode()

    * multipart requests builder

    * use multipart builder for changeProfilePicture()

    * use multipart builder for direct->sendPhoto()

    * allow to provide file contents for multipart requests

    * use multipart builder for uploadPhotoData()

    * requestVideoUploadURL() is not a multipart request

    * bonus: direct->sendVideo()

    * remove unused _buildBody() method

    * styleci

    * direct videos must be square

    * move high level methods from Client to Instagram

    * do not debug multipart bodies

    * Fix PHPdoc return type for changeProfilePicture

    * Request.php: Tweak PHPdoc summary lines

    * Direct: Rewrite risky call_user_func_array

    * Request: Fix PHPdoc return type

    * Request: Fix PHPdoc typo

    * Request: Document addHeader replacement behavior

    * Request: More info about addHeader behavior

    * Client: Document custom headers behavior

    * Request: Clearer descriptions for getBody-funcs

    * Request: Rename $body to match api() param name

    * Direct: Tweak comment

    * Utils: We normally put @see at the end of PHPdoc

    * Utils: Grammar improvement in exception msg

    * StyleCI whitespace removal

    * Update Instagram.php

    * Update Story.php

    * Update Timeline.php

    * Update Timeline.php

    * Direct: Use UploadFailedException instead