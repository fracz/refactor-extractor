commit bcc618a8f221d05abc745e60f5098786d84bfc72
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri Dec 14 14:32:14 2007 +0000

    refactored static Exceptions class into proper Reporter class

    --HG--
    rename : src/org/mockito/exceptions/Exceptions.java => src/org/mockito/exceptions/Reporter.java
    rename : test/org/mockito/exceptions/ExceptionsTest.java => test/org/mockito/exceptions/ReporterTest.java
    extra : convert_revision : svn%3Aaa2aecf3-ea3e-0410-9d70-716747e7c967/trunk%40185