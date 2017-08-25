commit b896b18f87064f523dca82c3fcaa5465ec8328ad
Author: alecpl <alec@alec.pl>
Date:   Fri Jun 3 12:34:48 2011 +0000

    - Call addressbook_get hook only if build-in addressbook doesn't match wanted ID (for better performance), other improvements