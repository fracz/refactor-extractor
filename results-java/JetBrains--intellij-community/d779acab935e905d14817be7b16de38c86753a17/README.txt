commit d779acab935e905d14817be7b16de38c86753a17
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Mon Mar 28 15:12:08 2016 +0300

    [shelve]: IDEA-153138 improve unshelve strategy

    * provide useProjectDir as best base flag;
    * unshelve to project dir by default despite context matching;
    * add test when several files with the same name exist in the project