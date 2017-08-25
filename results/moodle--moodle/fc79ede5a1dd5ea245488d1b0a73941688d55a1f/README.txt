commit fc79ede5a1dd5ea245488d1b0a73941688d55a1f
Author: Marina Glancy <marina@moodle.com>
Date:   Thu Oct 11 13:31:55 2012 +0800

    MDL-35768 Added format-specific options to edit course and section forms

    - Course format may define additional fields (format options) to store for course and each section
    - Edit course form allows to edit format-specific options and refreshes their list on format change
    - Course format may provide it's own form for editing a section
    - Default form for editing section allows to edit all format-specific fields
    - Class section_info refactored, it defines magic methods such as __get() to access basic section
      information and format-specific options (retrieved only on the first request)
    - format_base::update_course_format_options() allows to watch pre-update state of the course,
      format_legacy automatically copies the options with the same names between formats