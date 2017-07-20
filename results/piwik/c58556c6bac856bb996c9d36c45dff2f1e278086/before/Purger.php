<?php
/**
 * Piwik - free/libre analytics platform
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 */
namespace Piwik\Archive;

use Piwik\ArchiveProcessor\Rules;
use Piwik\Config;
use Piwik\DataAccess\ArchiveTableCreator;
use Piwik\DataAccess\InvalidatedReports;
use Piwik\DataAccess\Model;
use Piwik\Date;
use Piwik\Db;
use Piwik\Log;
use Piwik\Piwik;

/**
 *
 * This class purges two types of archives:
 *
 * (1) Deletes invalidated archives (from ArchiveInvalidator)
 *
 * (2) Deletes outdated archives (the temporary or errored archives)
 *
 */
class Purger
{
    /**
     * @var Model
     */
    private $model;

    public function __construct(Model $model = null)
    {
        $this->model = $model ?: new Model();
    }

    public function purgeInvalidatedArchives()
    {
        // TODO: why does it only look in specified sites to purge instead of all sites?
        $store = new InvalidatedReports();
        $idSitesByYearMonth = $store->getSitesByYearMonthArchiveToPurge();
        foreach ($idSitesByYearMonth as $yearMonth => $idSites) {
            if(empty($idSites)) {
                continue;
            }

            $date = Date::factory(str_replace('_', '-', $yearMonth) . '-01');
            $numericTable = ArchiveTableCreator::getNumericTable($date);

            $archiveIds = $this->model->getInvalidatedArchiveIdsSafeToDelete($numericTable, $idSites);

            if (count($archiveIds) == 0) {
                continue;
            }
            $this->deleteArchiveIds($date, $archiveIds);

            $store->markSiteIdsHaveBeenPurged($idSites, $yearMonth);
        }
    }

    /**
     * Removes the outdated archives for the given month.
     * (meaning they are marked with a done flag of ArchiveWriter::DONE_OK_TEMPORARY or ArchiveWriter::DONE_ERROR)
     *
     * @param Date $dateStart Only the month will be used
     */
    public function purgeOutdatedArchives(Date $dateStart)
    {
        $purgeArchivesOlderThan = self::getOldestTemporaryArchiveToKeepThreshold();
        if (!$purgeArchivesOlderThan) {
            return;
        }

        $idArchivesToDelete = $this->getOutdatedArchiveIds($dateStart, $purgeArchivesOlderThan);
        if (!empty($idArchivesToDelete)) {
            $this->deleteArchiveIds($dateStart, $idArchivesToDelete);
        }

        $this->deleteArchivesWithPeriodRange($dateStart);

        Log::debug("Purging temporary archives: done [ purged archives older than %s in %s ] [Deleted IDs: %s]",
                   $purgeArchivesOlderThan,
                   $dateStart->toString("Y-m"),
                   implode(',', $idArchivesToDelete));
    }

    protected function getOutdatedArchiveIds(Date $date, $purgeArchivesOlderThan)
    {
        $archiveTable = ArchiveTableCreator::getNumericTable($date);

        $result = $this->model->getTemporaryArchivesOlderThan($archiveTable, $purgeArchivesOlderThan);

        $idArchivesToDelete = array();
        if (!empty($result)) {
            foreach ($result as $row) {
                $idArchivesToDelete[] = $row['idarchive'];
            }
        }

        return $idArchivesToDelete;
    }

    /**
     * Deleting "Custom Date Range" reports after 1 day, since they can be re-processed and would take up un-necessary space.
     *
     * @param $date Date
     */
    protected  function deleteArchivesWithPeriodRange(Date $date)
    {
        $numericTable = ArchiveTableCreator::getNumericTable($date);
        $blobTable    = ArchiveTableCreator::getBlobTable($date);
        $daysRangesValid = Config::getInstance()->General['purge_date_range_archives_after_X_days'];
        $pastDate    = Date::factory('today')->subDay($daysRangesValid)->getDateTime();

        $this->model->deleteArchivesWithPeriod($numericTable, $blobTable, Piwik::$idPeriods['range'], $pastDate);

        Log::debug("Purging Custom Range archives: done [ purged archives older than %s from %s / blob ]",
            $pastDate, $numericTable);
    }

    /**
     * Deletes by batches Archive IDs in the specified month,
     *
     * @param Date $date
     * @param $idArchivesToDelete
     */
    protected function deleteArchiveIds(Date $date, $idArchivesToDelete)
    {
        $batches      = array_chunk($idArchivesToDelete, 1000);
        $numericTable = ArchiveTableCreator::getNumericTable($date);
        $blobTable    = ArchiveTableCreator::getBlobTable($date);

        foreach ($batches as $idsToDelete) {
            $this->model->deleteArchiveIds($numericTable, $blobTable, $idsToDelete);
        }
    }

    /**
     * Returns a timestamp indicating outdated archives older than this timestamp (processed before) can be purged.
     *
     * @return int|bool  Outdated archives older than this timestamp should be purged
     */
    private static function getOldestTemporaryArchiveToKeepThreshold()
    {
        $temporaryArchivingTimeout = Rules::getTodayArchiveTimeToLive();
        if (Rules::isBrowserTriggerEnabled()) {
            // If Browser Archiving is enabled, it is likely there are many more temporary archives
            // We delete more often which is safe, since reports are re-processed on demand
            return Date::factory(time() - 2 * $temporaryArchivingTimeout)->getDateTime();
        }

        // If cron core:archive command is building the reports, we should keep all temporary reports from today
        return Date::factory('yesterday')->getDateTime();
    }
}