commit 270ab5e85fa0d46e6d7069cf36d2be33c2bdb052
Author: Pavel Dolgov <pavel.dolgov@jetbrains.com>
Date:   Thu Oct 27 13:22:35 2016 +0300

    Javafx: "Wrap field with JavaFx property" intention - don't use TransactionGuard, because it delays the write action which may cause inconsistencies when performing refactoring (IDEA-102430)