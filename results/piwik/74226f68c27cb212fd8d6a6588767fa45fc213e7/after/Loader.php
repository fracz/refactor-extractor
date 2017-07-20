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
use Piwik\Archive;
use Piwik\ArchiveProcessor;
use Piwik\Config;
use Piwik\DataAccess\ArchiveSelector;
use Piwik\DataAccess\ArchiveWriter;
use Piwik\Date;
use Piwik\Metrics;
use Piwik\Period;

/**
 * This class manages the ArchiveProcessor and
 */
class Loader
{
    /**
     * Is the current archive temporary. ie.
     * - today
     * - current week / month / year
     */
    protected $temporaryArchive;

    /**
     * Idarchive in the DB for the requested archive
     *
     * @var int
     */
    protected $idArchive;

    /**
     * @var Parameters
     */
    protected $params;

    public function __construct(Parameters $params)
    {
        $this->params = $params;
    }

    /**
     * @return bool
     */
    protected function isThereSomeVisits($visits)
    {
        return $visits > 0;
    }

    /**
     * @return bool
     */
    protected function isVisitsCountAlreadyProcessed($visits)
    {
        return $visits !== false;
    }

    public function prepareArchive()
    {
        list($idArchive, $visits) = $this->loadExistingArchiveIdFromDb();
        if (!empty($idArchive)) {
            return $idArchive;
        }

        // Archive was not found, let's see if there's any visit in this period
        $this->prepareCoreMetricsArchive($visits);

        list($idArchive, $visits) = $this->computeNewArchive($enforceProcessCoreMetricsOnly = false, $visits);

        if ($this->isThereSomeVisits($visits)) {
            return $idArchive;
        }
        return false;
    }

    protected function prepareCoreMetricsArchive($visits)
    {
        $createSeparateArchiveForCoreMetrics =
            !$this->doesRequestedPluginIncludeVisitsSummary()
            && !$this->isVisitsCountAlreadyProcessed($visits);

        if ($createSeparateArchiveForCoreMetrics) {
            $requestedPlugin = $this->params->getRequestedPlugin();

            $this->params->setRequestedPlugin('VisitsSummary');

            $this->computeNewArchive($enforceProcessCoreMetricsOnly = true, $visits);

            $this->params->setRequestedPlugin($requestedPlugin);
        }
    }

    protected function computeNewArchive($enforceProcessCoreMetricsOnly, $visits)
    {
        $isArchiveDay = $this->params->isDayArchive();

        $archiveWriter = new ArchiveWriter($this->params->getSite()->getId(), $this->params->getSegment(), $this->params->getPeriod(), $this->params->getRequestedPlugin(), $this->isArchiveTemporary());
        $archiveWriter->initNewArchive();

        $pluginsArchiver = new PluginsArchiver($archiveWriter, $this->params);

        if (!$this->isVisitsCountAlreadyProcessed($visits)
            || $this->doesRequestedPluginIncludeVisitsSummary()
            || $enforceProcessCoreMetricsOnly
        ) {
            $metrics = $pluginsArchiver->callAggregateCoreMetrics();

            if (!empty($metrics)) {
                $pluginsArchiver->archiveProcessor->setNumberOfVisits($metrics['nb_visits'], $metrics['nb_visits_converted']);
                $visits = $metrics['nb_visits'];
            }
        }

        if ($this->isThereSomeVisits($visits)
            && !$enforceProcessCoreMetricsOnly
        ) {
            $pluginsArchiver->callAggregateAllPlugins();
        }

        $this->params->logStatusDebug( $this->isArchiveTemporary() );

        $archiveWriter->finalizeArchive();

        if ($this->isThereSomeVisits($visits) && !$isArchiveDay) {
            ArchiveSelector::purgeOutdatedArchives($this->params->getPeriod()->getDateStart());
        }

        return array($archiveWriter->getIdArchive(), $visits);
    }

    protected function doesRequestedPluginIncludeVisitsSummary()
    {
        $processAllReportsIncludingVisitsSummary =
                Rules::shouldProcessReportsAllPlugins($this->params->getSegment(), $this->params->getPeriod()->getLabel());
        $doesRequestedPluginIncludeVisitsSummary =
                $processAllReportsIncludingVisitsSummary || $this->params->getRequestedPlugin() == 'VisitsSummary';
        return $doesRequestedPluginIncludeVisitsSummary;
    }

    protected function isArchivingForcedToTrigger()
    {
        $period = $this->params->getPeriod()->getLabel();
        $debugSetting = 'always_archive_data_period'; // default
        if ($period == 'day') {
            $debugSetting = 'always_archive_data_day';
        } elseif ($period == 'range') {
            $debugSetting = 'always_archive_data_range';
        }
        return Config::getInstance()->Debug[$debugSetting];
    }

    /**
     * Returns the idArchive if the archive is available in the database for the requested plugin.
     * Returns false if the archive needs to be processed.
     *
     * @return int or false
     */
    protected function loadExistingArchiveIdFromDb()
    {
        if ($this->isArchivingForcedToTrigger()) {
            return array(false, false);
        }

        $minDatetimeArchiveProcessedUTC = $this->getMinTimeArchiveProcessed();
        $site = $this->params->getSite();
        $period = $this->params->getPeriod();
        $segment = $this->params->getSegment();
        $requestedPlugin = $this->params->getRequestedPlugin();

        $idAndVisits = ArchiveSelector::getArchiveIdAndVisits($site, $period, $segment, $minDatetimeArchiveProcessedUTC, $requestedPlugin);
        if (!$idAndVisits) {
            return array(false, false);
        }
        return $idAndVisits;
    }

    /**
     * Returns the minimum archive processed datetime to look at. Only public for tests.
     *
     * @return int|bool  Datetime timestamp, or false if must look at any archive available
     */
    protected function getMinTimeArchiveProcessed()
    {
        $endDateTimestamp = self::determineIfArchivePermanent($this->params->getDateEnd());
        $isArchiveTemporary = ($endDateTimestamp === false);
        $this->temporaryArchive = $isArchiveTemporary;

        if ($endDateTimestamp) {
            // Permanent archive
            return $endDateTimestamp;
        }
        // Temporary archive
        return Rules::getMinTimeProcessedForTemporaryArchive($this->params->getDateStart(), $this->params->getPeriod(), $this->params->getSegment(), $this->params->getSite());
    }

    protected static function determineIfArchivePermanent(Date $dateEnd)
    {
        $now = time();
        $endTimestampUTC = strtotime($dateEnd->getDateEndUTC());
        if ($endTimestampUTC <= $now) {
            // - if the period we are looking for is finished, we look for a ts_archived that
            //   is greater than the last day of the archive
            return $endTimestampUTC;
        }
        return false;
    }

    protected function isArchiveTemporary()
    {
        if (is_null($this->temporaryArchive)) {
            throw new \Exception("getMinTimeArchiveProcessed() should be called prior to isArchiveTemporary()");
        }
        return $this->temporaryArchive;
    }
}
