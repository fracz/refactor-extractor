commit c80630da1cad6e960dc1173de0d8b437089c141e
Author: Damyon Wiese <damyon@moodle.com>
Date:   Thu Nov 26 14:58:33 2015 +0800

    MDL-51041 cbe: More refactoring of exporters

    * Unit tests for base exporter
    * Use the course_summary_exporter for list_courses_using_competency
    * Rename competency_with_linked_courses_exporter to competency_summary_exporter
    * Split properties_definition in 2 (added read_properties_definition)
    * Improve phpdocs
    * Make some methods final
    * Throw an error when other_properties overlap with properties