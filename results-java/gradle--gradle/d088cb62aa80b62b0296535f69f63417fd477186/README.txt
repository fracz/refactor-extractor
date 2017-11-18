commit d088cb62aa80b62b0296535f69f63417fd477186
Author: Peter Niederwieser <pniederw@gmail.com>
Date:   Wed Aug 22 09:09:23 2012 +0200

    FindBugs: improved error messages, streamlined validation

    - if we want more/better validation, we should model allowed values using enums, do the validation in the task rather than the builder, and do it at configuration time