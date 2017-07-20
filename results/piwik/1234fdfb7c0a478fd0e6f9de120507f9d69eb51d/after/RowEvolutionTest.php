<?php
/**
 * Piwik - Open source web analytics
 *
 * @link    http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 * @version $Id$
 */

/**
 * Tests the method API.getRowEvolution
 */
class Test_Piwik_Integration_RowEvolution extends IntegrationTestCase
{
    protected static $today = '2010-03-06 11:22:33';
    protected static $idSite = 1;
    protected static $keywords = array(
        'free > proprietary', // BUG! testing a keyword containing >
        'peace "," not war', // testing a keyword containing ,
        'justice )(&^#%$ NOT corruption!',
    );

    public static function setUpBeforeClass()
    {
        parent::setUpBeforeClass();
        self::setUpWebsitesAndGoals();
        self::trackVisits();
    }

    /**
     * @dataProvider getApiForTesting
     * @group        Integration
     * @group        RowEvolution
     */
    public function testApi($api, $params)
    {
        $this->runApiTests($api, $params);
    }

    public function getApiForTesting()
    {
        $return = array();

        $config = array(
            'testSuffix'             => '_referrer1',
            'idSite'                 => self::$idSite,
            'date'                   => self::$today,
            'otherRequestParameters' => array(
                'date'      => '2010-02-06,2010-03-06',
                'period'    => 'day',
                'apiModule' => 'Referers',
                'apiAction' => 'getWebsites',
                'label'     => 'www.referrer2.com',
                'expanded'  => 0
            )
        );

        $return[] = array('API.getRowEvolution', $config);

        // Websites, hierarchical
        $config['testSuffix']                      = '_referrer2';
        $referrerLabel                             = urlencode('www.referrer0.com') . '>' . urlencode('theReferrerPage1.html');
        $config['otherRequestParameters']['label'] = $referrerLabel;
        $return[]                                  = array('API.getRowEvolution', $config);

        // Websites, multiple labels including one hierarchical
        $config['testSuffix']                      = '_referrerMulti1';
        $referrerLabel                             = urlencode($referrerLabel) . ',' . urlencode('www.referrer2.com');
        $config['otherRequestParameters']['label'] = $referrerLabel;
        $return[]                                  = array('API.getRowEvolution', $config);

        // Keywords, label containing > and ,
        $config['otherRequestParameters']['apiAction'] = 'getKeywords';
        $config['testSuffix']                          = '_LabelReservedCharacters';
        $keywords                                      = urlencode(self::$keywords[0]) . ',' . urlencode(self::$keywords[1]);
        $config['otherRequestParameters']['label']     = $keywords;
        $return[]                                      = array('API.getRowEvolution', $config);

        // Keywords, hierarchical
        $config['otherRequestParameters']['apiAction'] = 'getSearchEngines';
        $config['testSuffix']                          = '_LabelReservedCharactersHierarchical';
        $keywords                                      = "Google>" . urlencode(strtolower(self::$keywords[0]))
            . ',Google>' . urlencode(strtolower(self::$keywords[1]))
            . ',Google>' . urlencode(strtolower(self::$keywords[2]));
        // Test multiple labels search engines, Google should also have a 'logo' entry
        $config['otherRequestParameters']['label'] = $keywords . ",Google";
        $return[]                                  = array('API.getRowEvolution', $config);

        // Actions > Pages titles, standard label
        $config['testSuffix']                          = '_pageTitles';
        $config['periods']                             = array('day', 'week');
        $config['otherRequestParameters']['apiModule'] = 'Actions';
        $config['otherRequestParameters']['apiAction'] = 'getPageTitles';
        $config['otherRequestParameters']['label']     = 'incredible title 0';
        $return[]                                      = array('API.getRowEvolution', $config);

        // Actions > Page titles, multiple labels
        $config['testSuffix']                      = '_pageTitlesMulti';
        $label                                     = urlencode('incredible title 0') . ',' . urlencode('incredible title 2');
        $config['otherRequestParameters']['label'] = $label;
        $return[]                                  = array('API.getRowEvolution', $config);

        // Actions > Page URLS, hierarchical label
        $config['testSuffix']                          = '_pageUrls';
        $config['periods']                             = array('range');
        $config['otherRequestParameters']['date']      = '2010-03-01,2010-03-06';
        $config['otherRequestParameters']['apiModule'] = 'Actions';
        $config['otherRequestParameters']['apiAction'] = 'getPageUrls';
        $config['otherRequestParameters']['label']     = 'my>dir>' . urlencode('/page3?foo=bar&baz=bar');
        $return[]                                      = array('API.getRowEvolution', $config);

        return $return;
    }

    public function getOutputPrefix()
    {
        return 'RowEvolution';
    }

    protected static function setUpWebsitesAndGoals()
    {
        self::createWebsite('2010-02-01 11:22:33');
    }

    protected static function trackVisits()
    {
        $dateTime = self::$today;
        $idSite   = self::$idSite;

        for ($daysIntoPast = 30; $daysIntoPast >= 0; $daysIntoPast--) {
            // Visit 1: referrer website + test page views
            $visitDateTime = Piwik_Date::factory($dateTime)->subDay($daysIntoPast)->getDatetime();
            $t             = self::getTracker($idSite, $visitDateTime, $defaultInit = true);
            $t->setUrlReferrer('http://www.referrer' . ($daysIntoPast % 5) . '.com/theReferrerPage' . ($daysIntoPast % 2) . '.html');
            $t->setUrl('http://example.org/my/dir/page' . ($daysIntoPast % 4) . '?foo=bar&baz=bar');
            $t->setForceVisitDateTime($visitDateTime);
            self::checkResponse($t->doTrackPageView('incredible title ' . ($daysIntoPast % 3)));

            // VISIT 2: search engine
            $t->setForceVisitDateTime(Piwik_Date::factory($visitDateTime)->addHour(3)->getDatetime());
            $t->setUrlReferrer('http://google.com/search?q=' . urlencode(self::$keywords[$daysIntoPast % 3]));
            self::checkResponse($t->doTrackPageView('not an incredible title '));
        }
    }
}
