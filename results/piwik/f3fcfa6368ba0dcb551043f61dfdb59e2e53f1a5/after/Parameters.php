<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 * @category Piwik
 * @package Piwik
 */

namespace Piwik\ArchiveProcessor;

use Piwik\Date;
use Piwik\Period;
use Piwik\Segment;
use Piwik\Site;

/**
 * An ArchiveProcessor processes data for an Archive determined by these Parameters: website, period and segment.
 *
 * @api
 */
class Parameters
{
    /**
     * @var Site
     */
    private $site = null;

    /**
     * @var Period
     */
    private $period = null;

    /**
     * @var Segment
     */
    private $segment = null;

    /**
     * @var string Plugin name which triggered this archive processor
     */
    private $requestedPlugin = false;

    public function __construct(Site $site, Period $period, Segment $segment)
    {
        $this->site = $site;
        $this->period = $period;
        $this->segment = $segment;
    }

    public function setRequestedPlugin($plugin)
    {
        $this->requestedPlugin = $plugin;
    }

    public function getRequestedPlugin()
    {
        return $this->requestedPlugin;
    }

    /**
     * Returns the period we computing statistics for.
     *
     * @return Period
     * @api
     */
    public function getPeriod()
    {
        return $this->period;
    }

    /**
     * Returns the site we are computing statistics for.
     *
     * @return Site
     * @api
     */
    public function getSite()
    {
        return $this->site;
    }

    /**
     * The Segment used to limit the set of visits that are being aggregated.
     *
     * @return Segment
     * @api
     */
    public function getSegment()
    {
        return $this->segment;
    }

    /**
     * Returns the Date end of this period.
     *
     * @return Date
     */
    public function getDateEnd()
    {
        return $this->getPeriod()->getDateEnd()->setTimezone($this->getSite()->getTimezone());
    }

    /**
     * Returns the Date start of this period.
     *
     * @return Date
     */
    public function getDateStart()
    {
        return $this->getPeriod()->getDateStart()->setTimezone($this->getSite()->getTimezone());
    }

    /**
     * @return bool
     */
    public function isDayArchive()
    {
        return $this->getPeriod()->getLabel() == 'day';
    }
}