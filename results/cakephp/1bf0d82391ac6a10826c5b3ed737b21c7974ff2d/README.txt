commit 1bf0d82391ac6a10826c5b3ed737b21c7974ff2d
Author: Jorge González <steinkel@gmail.com>
Date:   Mon Mar 14 13:25:59 2016 +0000

    code improvements fixing PR comments from @markstory
      * fix sprintf not required and extra exception when unexpected debug token found
      * sort unlocked fields
      * fix docblocks and sprintf usage
      * ensure key 0 exists in array
      * throw exception on _validatePost instead of return false
      * fix no need to restore the debug value
      * test what if the tampered field is mutated into an array, improve expected fields output to display correctly the name of the field