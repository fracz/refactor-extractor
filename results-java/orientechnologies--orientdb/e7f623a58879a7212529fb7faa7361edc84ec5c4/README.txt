commit e7f623a58879a7212529fb7faa7361edc84ec5c4
Author: l.garulli@gmail.com <l.garulli@gmail.com@3625ad7b-9c83-922f-a72b-73d79161f2ea>
Date:   Fri Jan 27 02:28:59 2012 +0000

    Huge refactoring on remote connections:
    - used template method pattern to organize better binary protocol listener
    - split distributed protocol in 2: the old binary and the new non-more-as-subclass cluster