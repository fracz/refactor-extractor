commit 99b2e086663113108814d0b424747d4f4e872407
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Fri May 25 17:48:42 2012 +0200

    GRADLE-2290, Fixed the problem with ibm jvm + windows.

    Apparently, the ibm vm interprets process' standard input as a text stream. Hence our WorkerProcess and BootstrapSecurityManager fail when try to read data from System.in. The solution we took is applying very simple String encoding to the data we pass on via process' standard input.

    Some minor refactorings and unit test coverage is pending. Also, some changes to the ExecHandle stack are needed to make it fully working in ibm java + windows combo.