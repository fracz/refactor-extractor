commit 6d731921f84136f83a08810f1295ff44bb1661ac
Author: andrepereiradasilva <andrepereiradasilva@users.noreply.github.com>
Date:   Sun Oct 9 08:40:47 2016 +0100

    Add attribs to the script tag, add version to JHtml script methods and deprecate MD5SUM (alternative to 8540) (#11289)

    * new method to add attrbibs to the script tag

    * corrections

    * defaultJsMimes

    * add deprecated comment

    * travis

    * further simplify

    * remove extra space

    * forgot to convert mime to type

    * add UTF-8, ENT_COMPAT to htmlspecialchars (like previous done in all other places)

    * cs line size fix

    * better coding

    * small typo

    * elseif

    * deprecate MD5SUM and don't allow two version hashes

    * add JLog deprected warnings for new method signatures

    * cs line size

    * fix conflicts 1

    * fix conflicts 2

    * refactor JHtml script method signature

    * deprecate addScriptVersion add options for version and conditional statements

    * Update head.php

    * cs

    * cs fix

    * Change all self::getMd5Version to static::getMd5Version

    * has changed

    * has changed

    * fix conflicts - part one

    * fix conflicts

    * correct unit test