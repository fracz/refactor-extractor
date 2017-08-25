commit c426ef3aa59ce0e890f5ab2676e8ad32d4cb23b9
Author: Petr Skoda <skodak@moodle.org>
Date:   Sun Aug 29 14:51:09 2010 +0000

    MDL-23984 improvements of check_dir_exists() - replacing by make_upload_directory() in cases where we want to be sure that the dir is writable; removning now default create and recursive params to make code easier to read