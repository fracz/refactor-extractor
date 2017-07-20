commit f39a8e103f01ade174f601c3c5a68c4a874d3e6d
Author: JulienMoumne <julien@piwik.org>
Date:   Thu Jun 14 16:19:42 2012 +0000

    fixes #2708

    refs #71
     * PDFReports major refactoring. Any plugin can now add new kinds of reports. Required for #2708 and #3118.
     * test report functionality ($idReport == 0) dropped in Piwik_PDFReports_API->generateReport()
     * All Websites report shows 3 more metrics: Goal Conversions, eCommerce Conversions and eCommerce Revenue. Can be removed if asked to.
     * Piwik_PDFReports_API->sendEmailReport renamed to sendReport
     * All Piwik_PDFReports_API method signatures updated to support generic report parameters
    refs #389
     * new API method to retrieve only one Piwik site : Piwik_MultiSites_API->getOne()
     * per #2708 description, Piwik_MultiSites_API methods now support a new parameter named enhanced. When activated, Goal Conversions, eCommerce Conversions and eCommerce Revenue along with their evolution will be included in the API output.
     * API metrics refactored in (@ignored)Piwik_MultiSites_API->getApiMetrics()
     * Metadata now returns 12 metrics : nb_visits, visits_evolution, nb_actions, actions_evolution, revenu, revenue_evolution, nb_conversions, nb_conversions_evolution, orders, orders_evolution, ecommerce_revenue, ecommerce_revenue_evolution
    refs #3118
     * ReportPublisher plugin could now easily be implemented
    commits merged
     * r6243
     * r6422 (#3012)
    TODO
     * the MobileMessaging settings page may need some embellishment
     * @review annotations need some attention
     * test if the MultiSites API evolutions have some impact on Piwik Mobile and other client code

    git-svn-id: http://dev.piwik.org/svn/trunk@6478 59fd770c-687e-43c8-a1e3-f5a4ff64c105