commit d93063bd7322597b80d73b06b835aa466f74da18
Author: mattpiwik <matthieu.aubry@gmail.com>
Date:   Thu Oct 27 03:42:04 2011 +0000

    Fixes #584, #536, #2031 - Kuddos to Benaka akka capedfuzz for this great patch!!! I did a few minor modifications in wording and metadata output
     * Add Report "Visits by visit number" under Visitors > Engagement
     * Add Report for all Goals (including ecommerce): "Visit until Conversion": number of visits until the conversion occured
     * Add Report for all Goals (including ecommerce): "Days until Conversion": days since the first visit

    Notes
     * These new reports are also in the Metadata API so should be displayed in Piwik Mobile, and can be exported in the Scheduled reports.
     * filter_only_idgoal now renamed as idGoal for consistency
     * refactored the "Beautify labels" for ranges in generic filters
     * refactored archiving code to process multiple reports in one generic SQL query


    git-svn-id: http://dev.piwik.org/svn/trunk@5378 59fd770c-687e-43c8-a1e3-f5a4ff64c105