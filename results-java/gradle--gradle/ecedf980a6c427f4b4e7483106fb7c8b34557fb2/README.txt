commit ecedf980a6c427f4b4e7483106fb7c8b34557fb2
Author: Szczepan Faber <szczepiq@gmail.com>
Date:   Thu Aug 8 13:12:04 2013 +0200

    Added basic caching for configuration resolve results that are loaded from disk.

    1. Previously, after the results were loaded, they resided in memory till the end of the build. Now, I'm keeping ~20 results during the build.
    2. This improves the memory footprint of builds that need the results. For example, the ide plugins (e.g. 'gradle idea' in a large project).