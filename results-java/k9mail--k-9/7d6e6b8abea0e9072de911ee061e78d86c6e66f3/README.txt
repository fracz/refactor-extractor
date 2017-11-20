commit 7d6e6b8abea0e9072de911ee061e78d86c6e66f3
Author: Jan Berkel <jan.berkel@gmail.com>
Date:   Mon Dec 15 12:05:21 2014 +0100

    MimeUtility / Message refactor

     * break MimeUtility class into manageable pieces (MessageExtractor/CharsetSupport)
     * move HTML related code out of the mail package