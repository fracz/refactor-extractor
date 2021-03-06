<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 * @version $Id$
 */

/**
 * This tests the output of the API plugin API
 * It will return metadata about all API reports from all plugins
 * as well as the data itself, pre-processed and ready to be displayed
 */
class Test_Piwik_Integration_ApiGetReportMetadata extends IntegrationTestCase
{
    protected static $dateTime = '2009-01-04 00:11:42';
    protected static $idSite   = 1;
    protected static $idGoal   = 1;
    protected static $idGoal2  = 2;
    protected static $idGoal3  = 3;

    protected function setUpWebsitesAndGoals()
    {
        $this->createWebsite(self::$dateTime, $ecommerce = 1);
        Piwik_Goals_API::getInstance()->addGoal(self::$idSite, 'Goal 1 - Thank you', 'title', 'Thank you', 'contains', $caseSensitive = false, $revenue = 10, $allowMultipleConversions = 1);
        Piwik_Goals_API::getInstance()->addGoal(self::$idSite, 'Goal 2 - Hello', 'url', 'hellow', 'contains', $caseSensitive = false, $revenue = 10, $allowMultipleConversions = 0);
        Piwik_Goals_API::getInstance()->addGoal(self::$idSite, 'triggered js', 'manually', '', '');
    }

    public function setUp()
    {
        parent::setUp();

        // From Piwik 1.5, we hide Goals.getConversions and other get* methods via @ignore, but we ensure that they still work
        // This hack allows the API proxy to let us generate example URLs for the ignored functions
        Piwik_API_Proxy::getInstance()->hideIgnoredFunctions = false;
    }

    public function getOutputPrefix()
    {
        return 'apiGetReportMetadata';
    }

    public function getApiForTesting()
    {
        return array(
            array('API', array('idSite' => self::$idSite, 'date' => self::$dateTime))
        );
    }

    /**
     * @dataProvider getApiForTesting
     * @group        Integration
     * @group        ApiGetReportMetadata
     */
    public function testApi($api, $params)
    {
        $this->runApiTests($api, $params);
    }

    protected function trackVisits()
    {
        $idSite   = self::$idSite;
        $dateTime = self::$dateTime;

        $t = $this->getTracker($idSite, $dateTime, $defaultInit = true);

        // Record 1st page view
        $t->setUrl('http://example.org/index.htm');
        $this->checkResponse($t->doTrackPageView('incredible title!'));

        $t->setForceVisitDateTime(Piwik_Date::factory($dateTime)->addHour(0.3)->getDatetime());
        $this->checkResponse($t->doTrackGoal(self::$idGoal3, $revenue = 42.256));
    }
}
