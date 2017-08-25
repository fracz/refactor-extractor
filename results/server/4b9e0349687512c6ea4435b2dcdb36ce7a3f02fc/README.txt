commit 4b9e0349687512c6ea4435b2dcdb36ce7a3f02fc
Author: Lukas Reschke <lukas@owncloud.com>
Date:   Mon May 4 14:15:15 2015 +0200

    Remove hard-dependency on disabled output_buffering

    This removes the hard-dependency on output buffering as requested at https://github.com/owncloud/core/issues/16013 since a lot of distributions such as Debian and Ubuntu decided to use `4096` instead of the PHP recommended and documented default value of `off`.

    However, we still should encourage disabling this setting for improved performance and reliability thus the setting switches in `.user.ini` and `.htaccess` are remaining there. It is very likely that we in other cases also should disable the output buffering but aren't doing it everywhere and thus causing memory problems.

    Fixes https://github.com/owncloud/core/issues/16013