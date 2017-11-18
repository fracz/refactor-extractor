commit 9e6246e322ec620419c437d525c75fd08acf8a1a
Author: Andy Street <andrews@fb.com>
Date:   Wed Jul 26 10:55:37 2017 -0700

    Add PropertyHandle for animations

    Summary:
    Adds PropertyHandle with proper equals and hashCode functions. This is going to be part of a larger cleanup refactor where I try to get rid of a lot of the old animation API code (e.g. ChangingComponent etc).

    This pair of data is important and tracked in a bunch of different ways right now, I want to consolidate to use just this class for it.

    Reviewed By: muraziz

    Differential Revision: D5482151

    fbshipit-source-id: 32e0589fd1bd1f9341bc8507bf19c64f5cd452ea