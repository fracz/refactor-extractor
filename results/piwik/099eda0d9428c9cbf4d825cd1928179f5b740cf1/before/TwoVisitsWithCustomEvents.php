<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 */
use Piwik\Date;
use Piwik\Plugins\Goals\API as APIGoals;

/**
 * Tracks custom events
 */
class Test_Piwik_Fixture_TwoVisitsWithCustomEvents extends Test_Piwik_BaseFixture
{
    public $dateTime = '2010-01-03 11:22:33';
    public $idSite = 1;
    public $idGoal1 = 1;

    public function setUp()
    {
        $this->setUpWebsitesAndGoals();
        $this->trackVisits();
    }

    private function setUpWebsitesAndGoals()
    {
        // tests run in UTC, the Tracker in UTC
        self::createWebsite($this->dateTime);
        APIGoals::getInstance()->addGoal($this->idSite, 'triggered js', 'manually', '', '');
    }

    public function trackVisits()
    {
        $uselocal = false;
        $vis = self::getTracker($this->idSite, $this->dateTime, $useDefault = true, $uselocal);
        $this->moveTimeForward($vis);

        $this->trackMusicPlaying($vis);
        $this->trackMusicRatings($vis);
        $this->trackMovieWatchingIncludingInterval($vis);

        $this->dateTime = Date::factory($this->dateTime)->addHour(0.5);
        $vis2 = self::getTracker($this->idSite, $this->dateTime, $useDefault = true, $uselocal);
        $vis2->setIp('111.1.1.1');
        $vis2->setPlugins($flash = false, $java = false, $director = true);

        $this->trackMusicPlaying($vis2);
        $this->trackMusicRatings($vis2);
        $this->trackMovieWatchingIncludingInterval($vis2);
    }

    private function moveTimeForward(PiwikTracker $vis, $minutes)
    {
        $hour = $minutes / 60;
        return $vis->setForceVisitDateTime(Date::factory($this->dateTime)->addHour($hour)->getDatetime());
    }

    protected function trackMusicPlaying(PiwikTracker $vis)
    {
        $vis->setUrl('http://example.org/webradio');

        $this->moveTimeForward($vis, 1);
        $this->setMusicEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Music', 'play', 'La fiancée de l\'eau'));

        $this->moveTimeForward($vis, 2);
        $this->setMusicEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Music', 'play25%', 'La fiancée de l\'eau'));
        $this->moveTimeForward($vis, 3);
        $this->setMusicEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Music', 'play50%', 'La fiancée de l\'eau'));
        $this->moveTimeForward($vis, 4);
        $this->setMusicEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Music', 'play75%', 'La fiancée de l\'eau'));

        $this->moveTimeForward($vis, 4.5);
        $this->setMusicEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Music', 'playEnd', 'La fiancée de l\'eau'));
    }

    protected function trackMusicRatings(PiwikTracker $vis)
    {
        $this->moveTimeForward($vis, 5);
        $this->setMusicEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Music', 'rating', 'La fiancée de l\'eau', 9));

        $this->moveTimeForward($vis, 5.02);
        $this->setMusicEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Music', 'rating', 'La fiancée de l\'eau', 10));
    }

    protected function trackMovieWatchingIncludingInterval(PiwikTracker $vis)
    {
        $vis->setUrl('http://example.org/movies');

        $this->moveTimeForward($vis, 30);
        $this->setMovieEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Movie', 'playTrailer', 'Princess Mononoke (もののけ姫)'));
        $this->moveTimeForward($vis, 33);
        $this->setMovieEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Movie', 'playTrailer', 'Ponyo (崖の上のポニョ)'));
        $this->moveTimeForward($vis, 35);
        $this->setMovieEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Movie', 'playTrailer', 'Spirited Away (千と千尋の神隠し)'));
        $this->moveTimeForward($vis, 36);
        $this->setMovieEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Movie', 'clickBuyNow', 'Spirited Away (千と千尋の神隠し)'));
        $this->moveTimeForward($vis, 38);
        $this->setMovieEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Movie', 'playStart', 'Spirited Away (千と千尋の神隠し)'));
        $this->moveTimeForward($vis, 60);
        $this->setMovieEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Movie', 'play25%', 'Spirited Away (千と千尋の神隠し)'));

        // taking 2+ hours break & resuming this epic moment of cinema
        $this->moveTimeForward($vis, 200);

        $this->moveTimeForward($vis, 222);
        $this->setMovieEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Movie', 'play50%', 'Spirited Away (千と千尋の神隠し)'));
        $this->moveTimeForward($vis, 244);
        $this->setMovieEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Movie', 'play75%', 'Spirited Away (千と千尋の神隠し)'));
        $this->moveTimeForward($vis, 266);
        $this->setMovieEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Movie', 'playEnd', 'Spirited Away (千と千尋の神隠し)'));
        $this->moveTimeForward($vis, 268);
        $this->setMovieEventCustomVar($vis);
        self::checkResponse($vis->doTrackEvent('Movie', 'rating', 'Spirited Away (千と千尋の神隠し)', 9.66));
    }

    private function setMusicEventCustomVar(PiwikTracker $vis)
    {
        $vis->setCustomVariable($id = 1, $name = 'Page Scope Custom var', $value = 'should not appear in events report', $scope = 'page');
        $vis->setCustomVariable($id = 1, $name = 'album', $value = 'En attendant les caravanes...', $scope = 'event');
        $vis->setCustomVariable($id = 1, $name = 'genre', $value = 'World music', $scope = 'event');
    }

    private function setMovieEventCustomVar(PiwikTracker $vis)
    {
        $vis->setCustomVariable($id = 1, $name = 'country', $value = '日本', $scope = 'event');
        $vis->setCustomVariable($id = 2, $name = 'genre', $value = 'Greatest animated films', $scope = 'event');
        $vis->setCustomVariable($id = 4, $name = 'genre', $value = 'Adventure', $scope = 'event');
        $vis->setCustomVariable($id = 5, $name = 'genre', $value = 'Family', $scope = 'event');
        $vis->setCustomVariable($id = 5, $name = 'movieid', $value = 15763, $scope = 'event');

        $vis->setCustomVariable($id = 1, $name = 'Visit Scope Custom var', $value = 'should not appear in events report Bis', $scope = 'visit');
    }

    public function tearDown()
    {
    }

}