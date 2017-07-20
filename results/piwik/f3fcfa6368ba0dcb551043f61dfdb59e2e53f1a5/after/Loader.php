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
use Piwik\Log;
use Piwik\Metrics;
use Piwik\Period;
use Piwik\Plugin\Archiver;

/**
 * This class manages the ArchiveProcessor and
 */
class Loader
{
    /**
     * @var int Cached number of visits cached
     */
    protected $visitsMetricCached = false;

    /**
     * @var int Cached number of visits with conversions
     */
    protected $convertedVisitsMetricCached = false;

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
     * A flag mechanism to store whether visits were selected from archive
     *
     * @param $visitsMetricCached
     * @param bool $convertedVisitsMetricCached
     */
    protected function setNumberOfVisits($visitsMetricCached, $convertedVisitsMetricCached = false)
    {
        if ($visitsMetricCached === false) {
            $this->visitsMetricCached = $this->convertedVisitsMetricCached = false;
        } else {
            $this->visitsMetricCached = (int)$visitsMetricCached;
            $this->convertedVisitsMetricCached = (int)$convertedVisitsMetricCached;
        }
    }

    public function getNumberOfVisits()
    {
        return $this->visitsMetricCached;
    }

    public function getNumberOfVisitsConverted()
    {
        return $this->convertedVisitsMetricCached;
    }

    public function preProcessArchive($enforceProcessCoreMetricsOnly = false)
    {
        $this->idArchive = false;

        if (!$enforceProcessCoreMetricsOnly) {
            $this->idArchive = $this->loadExistingArchiveIdFromDb();
            if ($this->isArchivingForcedToTrigger()) {
                $this->idArchive = false;
                $this->setNumberOfVisits(false);
            }
            if (!empty($this->idArchive)) {
                return $this->idArchive;
            }

            $visitsNotKnownYet = $this->getNumberOfVisits() === false;

            $createAnotherArchiveForVisitsSummary = !$this->doesRequestedPluginIncludeVisitsSummary() && $visitsNotKnownYet;

            if ($createAnotherArchiveForVisitsSummary) {
                // recursive archive creation in case we create another separate one, for VisitsSummary core metrics
                // We query VisitsSummary here, as it is needed in the call below ($this->getNumberOfVisits() > 0)
                $requestedPlugin = $this->params->getRequestedPlugin();
                $this->params->setRequestedPlugin('VisitsSummary');

                $this->preProcessArchive($pleaseProcessCoreMetricsOnly = true);

                $this->params->setRequestedPlugin($requestedPlugin);
                if ($this->getNumberOfVisits() === false) {
                    throw new \Exception("preProcessArchive() is expected to set number of visits to a numeric value.");
                }
            }
        }

        return $this->computeNewArchive($enforceProcessCoreMetricsOnly);
    }

    protected function doesRequestedPluginIncludeVisitsSummary()
    {
        $processAllReportsIncludingVisitsSummary = Rules::shouldProcessReportsAllPlugins($this->params->getSegment(), $this->params->getPeriod()->getLabel());
        $doesRequestedPluginIncludeVisitsSummary = $processAllReportsIncludingVisitsSummary || $this->params->getRequestedPlugin() == 'VisitsSummary';
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
        $minDatetimeArchiveProcessedUTC = $this->getMinTimeArchiveProcessed();
        $site = $this->params->getSite();
        $period = $this->params->getPeriod();
        $segment = $this->params->getSegment();
        $requestedPlugin = $this->params->getRequestedPlugin();

        $idAndVisits = ArchiveSelector::getArchiveIdAndVisits($site, $period, $segment, $minDatetimeArchiveProcessedUTC, $requestedPlugin);
        if (!$idAndVisits) {
            return false;
        }
        list($idArchive, $visits, $visitsConverted) = $idAndVisits;
        $this->setNumberOfVisits($visits, $visitsConverted);
        return $idArchive;
    }

    protected function computeNewArchive($enforceProcessCoreMetricsOnly)
    {
        $archiveWriter = new ArchiveWriter($this->params->getSite()->getId(), $this->params->getSegment(), $this->params->getPeriod(), $this->params->getRequestedPlugin(), $this->isArchiveTemporary());
        $archiveWriter->initNewArchive();

        $archiveProcessor = $this->makeArchiveProcessor($archiveWriter);

        $visitsNotKnownYet = $this->getNumberOfVisits() === false;
        if ($visitsNotKnownYet
            || $this->doesRequestedPluginIncludeVisitsSummary()
            || $enforceProcessCoreMetricsOnly
        ) {

            if($this->params->isDayArchive()) {
                $metrics = $this->aggregateDayVisitsMetrics($archiveProcessor);
            } else {
                $metrics = $this->aggregateMultipleVisitMetrics($archiveProcessor);
            }

            if (empty($metrics)) {
                $this->setNumberOfVisits(false);
            } else {
                $this->setNumberOfVisits($metrics['nb_visits'], $metrics['nb_visits_converted']);
            }
        }
        $this->logStatusDebug();

        $archiveProcessor = $this->makeArchiveProcessor($archiveWriter);

        $isVisitsToday = $this->getNumberOfVisits() > 0;
        if ($isVisitsToday
            && !$enforceProcessCoreMetricsOnly
        ) {
            $pluginsArchiver = new PluginsArchiver($archiveProcessor);
            $pluginsArchiver->callPluginsAggregate();
        }

        $archiveWriter->finalizeArchive();

        if ($isVisitsToday && $this->params->getPeriod()->getLabel() != 'day') {
            ArchiveSelector::purgeOutdatedArchives($this->params->getPeriod()->getDateStart());
        }

        return $archiveWriter->getIdArchive();
    }

    protected function aggregateDayVisitsMetrics(ArchiveProcessor $archiveProcessor)
    {
        $query = $archiveProcessor->getLogAggregator()->queryVisitsByDimension();
        $data = $query->fetch();

        $metrics = $this->convertMetricsIdToName($data);
        $archiveProcessor->insertNumericRecords($metrics);
        return $metrics;
    }

    protected function convertMetricsIdToName($data)
    {
        $metrics = array();
        foreach ($data as $metricId => $value) {
            $readableMetric = Metrics::$mappingFromIdToName[$metricId];
            $metrics[$readableMetric] = $value;
        }
        return $metrics;
    }

    protected function aggregateMultipleVisitMetrics(ArchiveProcessor $archiveProcessor)
    {
        $toSum = Metrics::getVisitsMetricNames();
        $metrics = $archiveProcessor->aggregateNumericMetrics($toSum);
        return $metrics;
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

    protected function logStatusDebug()
    {
        $temporary = 'definitive archive';
        if ($this->isArchiveTemporary()) {
            $temporary = 'temporary archive';
        }
        Log::verbose(
            "'%s, idSite = %d (%s), segment '%s', report = '%s', UTC datetime [%s -> %s]",
            $this->params->getPeriod()->getLabel(),
            $this->params->getSite()->getId(),
            $temporary,
            $this->params->getSegment()->getString(),
            $this->params->getRequestedPlugin(),
            $this->params->getDateStart()->getDateStartUTC(),
            $this->params->getDateEnd()->getDateEndUTC()
        );
    }

    /**
     * @param $archiveWriter
     * @return ArchiveProcessor
     */
    protected function makeArchiveProcessor($archiveWriter)
    {
        $archiveProcessor = new ArchiveProcessor($this->params, $archiveWriter, $this->getNumberOfVisits(), $this->getNumberOfVisitsConverted());

        if (!$this->params->isDayArchive()) {
            $subPeriods = $this->params->getPeriod()->getSubperiods();
            $archiveProcessor->archive = Archive::factory($this->params->getSegment(), $subPeriods, array($this->params->getSite()->getId()));
        }
        return $archiveProcessor;
    }
}