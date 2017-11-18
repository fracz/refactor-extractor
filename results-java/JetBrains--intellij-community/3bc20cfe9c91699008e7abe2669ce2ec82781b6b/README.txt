commit 3bc20cfe9c91699008e7abe2669ce2ec82781b6b
Author: Anna.Kozlova <anna.kozlova@jetbrains.com>
Date:   Mon Jul 10 11:19:20 2017 +0200

    callback for change signature with conflict or preview

    otherwise additional initialization don't work when refactoring was not finished synchronously (IDEA-175566)