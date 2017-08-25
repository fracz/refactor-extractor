commit 111c9f7563f1ac43d8e615a2aa8dc89375a1ffb6
Author: Roeland Jago Douma <roeland@famdouma.nl>
Date:   Wed Mar 29 12:23:46 2017 +0200

    Don't add the Encryption Storage Wrapper if there are no encryption modules

    fixes #4125

    If there is no encryption module enabled it makes no sense to setup the
    encryption wrapper (because we can't do anything anyway).

    This saves reading the header of files.
    Especialy on external storage/objectstore this should improve
    performance

    Signed-off-by: Roeland Jago Douma <roeland@famdouma.nl>