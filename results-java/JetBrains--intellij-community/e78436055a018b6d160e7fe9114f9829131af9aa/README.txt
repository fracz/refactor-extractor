commit e78436055a018b6d160e7fe9114f9829131af9aa
Author: Nadya.Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Fri Nov 16 20:03:25 2012 +0400

    IDEA-68122 Mercurial: ShowHistory for directory: implement CompareWithLocal and Show Diff.
    * Compare and Compare with local actions added (without any merge conflict and multiparent cases)
    * Should be improved if file was moved or renamed (maybe need to change fileStatus)