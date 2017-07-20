<?php
/**
 * Piwik - Open source web analytics
 *
 * @link    http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 * @version $Id$
 */

require_once dirname(__FILE__) . '/TwoVisitors_TwoWebsites_DifferentDaysTest.php';
require_once 'Goals/Goals.php';

/**
 * Same as TwoVisitors_twoWebsites_differentDays but with goals that convert
 * on every url.
 */
class Test_Piwik_Integration_TwoVisitors_TwoWebsites_DifferentDays_Conversions extends Test_Piwik_Integration_TwoVisitors_TwoWebsites_DifferentDays
{
    public static function setUpBeforeClass()
    {
        IntegrationTestCase::setUpBeforeClass();
        self::$allowConversions = true;
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
     * @group        TwoVisitors_TwoWebsites_DifferentDays_Conversions
     */
    public function testApi($api, $params)
    {
        $this->runApiTests($api, $params);
    }

    protected function getApiToCall()
    {
        return array('Goals.getDaysToConversion', 'MultiSites.getAll');
    }

    public function getApiForTesting()
    {
        $result = parent::getApiForTesting();

        // Tests that getting a visits summary metric (nb_visits) & a Goal's metric (Goal_revenue)
        // at the same time works.
        $dateTime = '2010-01-03,2010-01-06';
        $columns  = 'nb_visits,' . Piwik_Goals::getRecordName('conversion_rate');

        $result[] = array(
            'VisitsSummary.get', array('idSite'                 => 'all', 'date' => $dateTime, 'periods' => 'range',
                                       'otherRequestParameters' => array('columns' => $columns),
                                       'testSuffix'             => '_getMetricsFromDifferentReports')
        );

        return $result;
    }

    public function getOutputPrefix()
    {
        return 'TwoVisitors_twoWebsites_differentDays_Conversions';
    }
}