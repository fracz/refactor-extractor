commit 48197be15f754cefa55eefb8794f01f202d58fa0
Author: Makoto Yamazaki <makoto1975@gmail.com>
Date:   Fri Sep 16 21:57:12 2016 +0900

    Allow to specify default value of the field in model's constructor (#3397)

    * Allow to call its accessors, and replace its field accesses with accessor calls in model's constructor.

    fixes #777
    fixes #2536

    * use field instead of checking transaction

    * fix a bug that acceptDefaultValue is not set correctly

    * reject default values when the getter of a model creates other model object

    * add simple test for default value

    * supports default value of model field

    * supports default value of RealmList fields

    * add tests for assignment in constructor and setter in constructor

    * update javadoc comments of createObject

    * always ignores the default value of primary key if the object is managed

    * update javadoc

    * add a test for default values handling in copyToRealm(). the last assertion of RealmTests.copyToRealm_defaultValuesAreIgnored() is failing now.

    * refactor tests

    * use isPrimaryKey()

    * fix a bug that unexpected realm object is created by default value of RealmModel/RealmList fields

    * remove extra ';' from generated code

    * add more tests for default value

    * fix tests

    * fix a bug that creates unexpected objects

    * rename internal methods

    * update changelog

    * update CHANGELOG

    * review comments

    * update CHANGELOG

    * added a description of how proxy object should be created in the Javadoc comment of RealmProcessor