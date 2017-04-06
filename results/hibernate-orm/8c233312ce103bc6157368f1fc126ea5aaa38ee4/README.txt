commit 8c233312ce103bc6157368f1fc126ea5aaa38ee4
Author: adamw <adam@warski.org>
Date:   Sat Apr 2 09:03:46 2011 +0200

    HHH-6020: some refactoring on how the JTA tests are done. All hibernate config is in the AbstractEntityTest now, instead of loading an xml file.