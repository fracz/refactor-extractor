commit ca3597482787257206872f3b476e810d6d757aae
Author: jamiesensei <jamiesensei>
Date:   Thu May 15 13:59:52 2008 +0000

    MDL-14852 "improve sql in overview report for fetching question grades" moved all querying for question grades into main attempts sql query. AND MDL-14200 "Add group and course averages" some fixes for functionality to display question grades averages. Grade averages are not displayed when grading method is set to Average - as this cannot be done in sql and would be expensive to do in memory.

    merged from 1.9 branch