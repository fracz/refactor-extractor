commit 6fdaf8c181d4273f4a2f2c86a77ced1a79991887
Author: Johannes Edmeier <johannes.edmeier@gmail.com>
Date:   Mon Dec 26 19:40:28 2016 +0100

    Aggregate info on the server

    The info is now fetched by the server on status change. This way the
    info can be used in the notifications and also improves performance when
    listing a huge amount of applications in the ui.

    closes #349