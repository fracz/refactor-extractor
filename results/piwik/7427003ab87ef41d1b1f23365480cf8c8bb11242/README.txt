commit 7427003ab87ef41d1b1f23365480cf8c8bb11242
Author: JulienMoumne <julien@piwik.org>
Date:   Mon Aug 13 20:26:46 2012 +0000

    refs #2708
     * fixing a bug in ''Piwik::getPrettyValue()'' where a revenue evolution would be  prettified like so: ''$ 100%''
     * fixing a bug happening when no data is available for the selected day
     * when MultiSites plugin is deactivated, invite user via SMS to activate it back
     * SMS API accounts are now managed using an API key instead of a username and a password
     * adding ''[too long]'' at the end of the SMS content when it reaches the maximum length allowed by the SMS API
     * support for UCS-2 characters
     * Mediaburst rebranded to Clockwork
     * various UI improvements based on comment:31:ticket:2708
     * PDFReport.generateReport now supports $outputType=3 : output report in browser
     * removing non-libre select-to-autocomplete jQuery plugin
     * tracking count of phone number validation requests and SMS sent
     * SMS content now contains CoreHome_ThereIsNoDataForThisReport when applicable
     * setting SMS From with General_Reports when configured report is MultiSites_getAll
     * adding Clockwork description

    TODO
     * using POST instead of GET to send SMS to go around a Clockwork limitation

    git-svn-id: http://dev.piwik.org/svn/trunk@6727 59fd770c-687e-43c8-a1e3-f5a4ff64c105