commit 7b1bd02814e4aaa6bfaf2b3d4fefac6da4b2af9d
Author: Nate Abele <nate.abele@gmail.com>
Date:   Sat Feb 27 14:25:27 2010 -0500

    Small refactorings to `\action\Request`. Using `Media` to automatically decode non-urlencoded POST data and assign to `Request::$data`, if content-type is a registered type.