commit 87a26cc402419516a4c4bf1ee9c0ecc4fd69fee8
Author: Sam Chaffee <sam@moodlerooms.com>
Date:   Mon Sep 30 15:27:03 2013 -0600

    MDL-42065 core_grade: Modified some grade_item queries for improved performance

    * Modified 2 queries in grade_item::depends_on to improve performance
    * Added additional unit tests to cover those queries better