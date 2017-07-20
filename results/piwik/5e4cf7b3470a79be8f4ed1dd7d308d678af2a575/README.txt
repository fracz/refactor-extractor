commit 5e4cf7b3470a79be8f4ed1dd7d308d678af2a575
Author: JulienMoumne <julien@piwik.org>
Date:   Tue Dec 20 17:50:45 2011 +0000

     * fixes #2706, #2828, #2704, refs #1721, #2637, #2711, #2318, #71 : horizontal static graph implemented
     * fixes #2788, refs #2670, #1721, #2637, #2711 : default graph type logic moved to ImageGraph API, improved date/period logic, new parameter graphs_default_period_to_plot_when_period_range
     * fixes #2704, #2804, refs #1721 : pChart updated to 2.1.3, pChart code removed from Piwik code, OOP refactoring, support for unifont.ttf if present in ImageGraph/fonts, testAllSizes now uses report metadata ImageGraphUrl
     * refs #71 : space between report title and report table reduced to avoid page overflow
     * refs #2829 : TODO display percentages
     * r5544, r5547, r5549 merged

    git-svn-id: http://dev.piwik.org/svn/trunk@5582 59fd770c-687e-43c8-a1e3-f5a4ff64c105