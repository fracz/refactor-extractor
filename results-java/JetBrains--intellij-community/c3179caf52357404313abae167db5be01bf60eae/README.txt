commit c3179caf52357404313abae167db5be01bf60eae
Author: Nadya Zabrodina <Nadya.Zabrodina@jetbrains.com>
Date:   Thu Jul 23 00:43:19 2015 +0400

    refactoring and bug fix

     * create 2 separated mathod for migration info and resources;
     * by default use only info migration;
     * create sheme name according to other existing shemes instead of directory;
     * fix problem with very long description and impossibility to create file with it;
     * rename patch file in shelved sheme dir to unified short one