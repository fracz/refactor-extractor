commit 4031f6a27cc270d5abe1aa32bd09164e67d502a5
Author: Petr Skoda <skodak@moodle.org>
Date:   Sun Aug 29 14:33:39 2010 +0000

    MDL-23984 improved check_dir_exists() and make_upload_directory() incorrect permissions throw fatal exceptions by default; it is possible to create dirs outside of dataroot (necessary for custom dir locations); fixed Win32 compatibility in session_exists method