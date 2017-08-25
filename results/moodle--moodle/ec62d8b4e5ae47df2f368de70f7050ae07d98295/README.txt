commit ec62d8b4e5ae47df2f368de70f7050ae07d98295
Author: Marina Glancy <marina@moodle.com>
Date:   Thu Jan 24 12:39:26 2013 +1100

    MDL-37455 Performance improvement in mod_folder

    We cache and store additional data in cm_info::customdata and do not
    query DB every time the folder is displayed on a course page