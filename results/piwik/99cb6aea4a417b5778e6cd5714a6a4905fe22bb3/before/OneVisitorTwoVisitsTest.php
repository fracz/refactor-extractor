<?php
/**
 * Piwik - Open source web analytics
 *
 * @link    http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 * @version $Id$
 */

/**
 * This use case covers many simple tracking features.
 * - Tracking Goal by manual trigger, and URL matching, with custom revenue
 * - Tracking the same Goal twice only records it once
 * - Tracks 4 page views: 3 clicks and a file download
 * - URLs parameters exclude is tested
 * - In a returning visit, tracks a Goal conversion
 *   URL matching, with custom referer and keyword
 *   NO cookie support
 */
class Test_Piwik_Integration_OneVisitorTwoVisits extends IntegrationTestCase
{
    protected static $idSite   = 1;
    protected static $dateTime = '2010-03-06 11:22:33';

    public static function setUpBeforeClass()
    {
        parent::setUpBeforeClass();
        try {
            self::setUpWebsitesAndGoals();
            self::trackVisits();
        } catch(Exception $e) {
            // Skip whole test suite if an error occurs while setup
            throw new PHPUnit_Framework_SkippedTestSuiteError($e->getMessage());
        }
    }

    /**
     * @dataProvider getApiForTesting
     * @group        Integration
     * @group        OneVisitorTwoVisits
     */
    public function testApi($api, $params)
    {
        $this->runApiTests($api, $params);
    }

    public function getApiForTesting()
    {
        $enExtraParam = array('expanded' => 1, 'flat' => 1, 'include_aggregate_rows' => 0, 'translateColumnNames' => 1);
        $bulkUrls     = array(
            "idSite=".self::$idSite."&date=2010-03-06&format=json&expanded=1&period=day&method=VisitsSummary.get",
            "idSite=".self::$idSite."&date=2010-03-06&format=xml&expanded=1&period=day&method=VisitsSummary.get",
            "idSite=".self::$idSite."&date=2010-03-06&format=json&expanded=1&period=day&method="
                . "VisitorInterest.getNumberOfVisitsPerVisitDuration"
        );
        return array(
            array('all', array('idSite' => self::$idSite, 'date' => self::$dateTime)),

            // test API.get (for bug that incorrectly reorders columns of CSV output)
            //   note: bug only affects rows after first
            array('API.get', array('idSite'                 => self::$idSite,
                                   'date'                   => '2009-10-01',
                                   'format'                 => 'csv',
                                   'periods'                => array('month'),
                                   'setDateLastN'           => true,
                                   'otherRequestParameters' => $enExtraParam,
                                   'language'               => 'en',
                                   'testSuffix'             => '_csv')),

            array('API.getBulkRequest', array('otherRequestParameters' => array('urls' => $bulkUrls))),

            // test API.getProcessedReport w/ report that is its own 'actionToLoadSubTables'
            array('API.getProcessedReport', array('idSite'		  => self::$idSite,
            									  'date'		  => self::$dateTime,
            									  'periods'		  => array('week'),
            									  'apiModule'	  => 'Actions',
            									  'apiAction'	  => 'getPageUrls',
            									  'supertableApi' => 'Actions.getPageUrls',
            									  'testSuffix'	  => '__subtable')),
        );
    }

    protected static function setUpWebsitesAndGoals()
    {
        // tests run in UTC, the Tracker in UTC
        self::createWebsite(self::$dateTime);
    }

    protected static function trackVisits()
    {
        $t = self::getTracker(self::$idSite, self::$dateTime, $defaultInit = true);

        $dateTime = self::$dateTime;
        $idSite   = self::$idSite;

        $t->disableCookieSupport();

        $t->setUrlReferrer('http://referer.com/page.htm?param=valuewith some spaces');

        // testing URL excluded parameters
        $parameterToExclude = 'excluded_parameter';
        Piwik_SitesManager_API::getInstance()->updateSite($idSite, 'new name', $url = array('http://site.com'), $ecommerce = 0, $excludedIps = null, $parameterToExclude . ',anotherParameter');

        // Record 1st page view
        $urlPage1 = 'http://example.org/index.htm?excluded_Parameter=SHOULD_NOT_DISPLAY&parameter=Should display';
        $t->setUrl($urlPage1);
        self::checkResponse($t->doTrackPageView('incredible title!'));

        // testing that / and index.htm above record with different URLs
        // Recording the 2nd page after 3 minutes
        $t->setForceVisitDateTime(Piwik_Date::factory($dateTime)->addHour(0.05)->getDatetime());
        $urlPage2 = 'http://example.org/';
        $t->setUrl($urlPage2);
//		$t->setUrlReferrer($urlPage1);
        self::checkResponse($t->doTrackPageView('Second page view - should be registered as URL /'));

//		$t->setUrlReferrer($urlPage2);
        // Click on external link after 6 minutes (3rd action)
        $t->setForceVisitDateTime(Piwik_Date::factory($dateTime)->addHour(0.1)->getDatetime());
        self::checkResponse($t->doTrackAction('http://dev.piwik.org/svn', 'link'));

        // Click on file download after 12 minutes (4th action)
        $t->setForceVisitDateTime(Piwik_Date::factory($dateTime)->addHour(0.2)->getDatetime());
        self::checkResponse($t->doTrackAction('http://piwik.org/path/again/latest.zip', 'download'));

        // Click on two more external links, one the same as before (5th & 6th actions)
        $t->setForceVisitDateTime(Piwik_Date::factory($dateTime)->addHour(0.22)->getDateTime());
        self::checkResponse($t->doTrackAction('http://outlinks.org/other_outlink', 'link'));
        $t->setForceVisitDateTime(Piwik_Date::factory($dateTime)->addHour(0.25)->getDateTime());
        self::checkResponse($t->doTrackAction('http://dev.piwik.org/svn', 'link'));

        // Create Goal 1: Triggered by JS, after 18 minutes
        $idGoal = Piwik_Goals_API::getInstance()->addGoal($idSite, 'triggered js', 'manually', '', '');
        $t->setForceVisitDateTime(Piwik_Date::factory($dateTime)->addHour(0.3)->getDatetime());

        // Change to Thai  browser to ensure the conversion is credited to FR instead (the visitor initial country)
        $t->setBrowserLanguage('th');
        self::checkResponse($t->doTrackGoal($idGoal, $revenue = 42));

        // Track same Goal twice (after 24 minutes), should only be tracked once
        $t->setForceVisitDateTime(Piwik_Date::factory($dateTime)->addHour(0.4)->getDatetime());
        self::checkResponse($t->doTrackGoal($idGoal, $revenue = 42));

        $t->setBrowserLanguage('fr');
        // Final page view (after 27 min)
        $t->setForceVisitDateTime(Piwik_Date::factory($dateTime)->addHour(0.45)->getDatetime());
        $t->setUrl('http://example.org/index.htm');
        self::checkResponse($t->doTrackPageView('Looking at homepage (again)...'));

        // -
        // End of first visit: 24min

        // Create Goal 2: Matching on URL
        Piwik_Goals_API::getInstance()->addGoal($idSite, 'matching purchase.htm', 'url', '(.*)store\/purchase\.(.*)', 'regex', false, $revenue = 1);

        // -
        // Start of returning visit, 1 hour after first page view
        $t->setForceVisitDateTime(Piwik_Date::factory($dateTime)->addHour(1)->getDatetime());
        $t->setUrl('http://example.org/store/purchase.htm');
        $t->setUrlReferrer('http://search.yahoo.com/search?p=purchase');
        // Temporary, until we implement 1st party cookies in PiwikTracker
        $t->DEBUG_APPEND_URL = '&_idvc=2';

        // Goal Tracking URL matching, testing custom referer including keyword
        self::checkResponse($t->doTrackPageView('Checkout/Purchasing...'));
        // -
        // End of second visit
    }
}