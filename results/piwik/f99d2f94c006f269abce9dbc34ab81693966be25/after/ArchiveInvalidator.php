<?php
/**
 * Piwik - free/libre analytics platform
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 */

namespace Piwik\DataAccess;


use Piwik\Date;
use Piwik\Db;
use Piwik\Plugins\CoreAdminHome\InvalidatedReports;
use Piwik\Plugins\PrivacyManager\PrivacyManager;
use Piwik\Period;
use Piwik\Period\Week;

class ArchiveInvalidator {

    private $warningDates = array();
    private $processedDates = array();
    private $minimumDateWithLogs = false;
    private $invalidDates = array();

    /**
     * @param $idSites array
     * @param $dates string
     * @param $period string
     * @return array
     * @throws \Exception
     */
    public function markArchivesAsInvalidated($idSites, $dates, $period)
    {
        $this->findOlderDateWithLogs();
        $datesToInvalidate = $this->getDatesToInvalidateFromString($dates);
        $minDate = $this->getMinimumDateToInvalidate($datesToInvalidate);

        \Piwik\Plugins\SitesManager\API::getInstance()->updateSiteCreatedTime($idSites, $minDate);

        $this->markArchivesInvalidatedFor($idSites, $period, $datesToInvalidate);


        $store = new InvalidatedReports();
        $store->setSiteIdsToBeInvalidated($idSites);

        return $this->makeOutputLogs();
    }

    /**
     * @param $toInvalidate
     * @return bool|Date
     * @throws \Exception
     */
    private function getMinimumDateToInvalidate($toInvalidate)
    {
        /* @var $date Date */
        $minDate = false;
        foreach ($toInvalidate as $date) {
            // Keep track of the minimum date for each website
            if ($minDate === false
                || $date->isEarlier($minDate)
            ) {
                $minDate = $date;
            }
        }
        if (empty($minDate)) {
            throw new \Exception("Check the 'dates' parameter is a valid date.");
        }
        return $minDate;
    }

    /**
     * @param $idSites
     * @param $period string
     * @param $datesToInvalidate Date[]
     * @throws \Exception
     */
    private function markArchivesInvalidatedFor($idSites, $period, $datesToInvalidate)
    {
        $invalidateForPeriodId = $this->getPeriodId($period);

        $datesByMonth = $this->getDatesByYearMonth($datesToInvalidate);

        // In each table, invalidate day/week/month/year containing this date
        $archiveTables = ArchiveTableCreator::getTablesArchivesInstalled();
        foreach ($archiveTables as $table) {
            // Extract Y_m from table name
            $suffix = ArchiveTableCreator::getDateFromTableName($table);
            if (!isset($datesByMonth[$suffix])) {
                continue;
            }
            // Dates which are to be deleted from this table
            $datesToDelete = $datesByMonth[$suffix];
            self::getModel()->updateArchiveAsInvalidated($table, $idSites, $invalidateForPeriodId, $datesToDelete);
        }
    }

    /**
     * Ensure the specified dates are valid.
     * Store invalid date so we can log them
     * @param $dates string
     * @return Date[]
     */
    private function getDatesToInvalidateFromString($dates)
    {
        $toInvalidate = array();

        $dates = explode(',', trim($dates));
        $dates = array_unique($dates);

        foreach ($dates as $theDate) {
            $theDate = trim($theDate);
            try {
                $date = Date::factory($theDate);
            } catch (\Exception $e) {
                $this->invalidDates[] = $theDate;
                continue;
            }
            if ($date->toString() == $theDate) {
                $toInvalidate[] = $date;
            } else {
                $this->invalidDates[] = $theDate;
            }
        }
        return $toInvalidate;
    }

    private function findOlderDateWithLogs()
    {
// If using the feature "Delete logs older than N days"...
        $purgeDataSettings = PrivacyManager::getPurgeDataSettings();
        $logsAreDeletedBeforeThisDate = $purgeDataSettings['delete_logs_schedule_lowest_interval'];
        $logsDeleteEnabled = $purgeDataSettings['delete_logs_enable'];

        if ($logsDeleteEnabled
            && $logsAreDeletedBeforeThisDate
        ) {
            $this->minimumDateWithLogs = Date::factory('today')->subDay($logsAreDeletedBeforeThisDate);
        }
    }

    /**
     * Given the list of dates, process which tables YYYY_MM we should delete from
     *
     * @param $datesToInvalidate Date[]
     * @return array
     */
    private function getDatesByYearMonth($datesToInvalidate)
    {
        $datesByMonth = array();
        foreach ($datesToInvalidate as $date) {
            // we should only delete reports for dates that are more recent than N days
            if ($this->minimumDateWithLogs
                && $date->isEarlier($this->minimumDateWithLogs)
            ) {
                $this->warningDates[] = $date->toString();
            } else {
                $this->processedDates[] = $date->toString();
            }

            $month = $date->toString('Y_m');
            // For a given date, we must invalidate in the monthly archive table
            $datesByMonth[$month][] = $date->toString();

            // But also the year stored in January
            $year = $date->toString('Y_01');
            $datesByMonth[$year][] = $date->toString();

            // but also weeks overlapping several months stored in the month where the week is starting
            /* @var $week Week */
            $week = Period\Factory::build('week', $date);
            $weekAsString = $week->getDateStart()->toString('Y_m');
            $datesByMonth[$weekAsString][] = $date->toString();

        }
        return $datesByMonth;
    }

    /**
     * @return array
     */
    private function makeOutputLogs()
    {
        $output = array();
        if ($this->warningDates) {
            $output[] = 'Warning: the following Dates have not been invalidated, because they are earlier than your Log Deletion limit: ' .
                implode(", ", $this->warningDates) .
                "\n The last day with logs is " . $this->minimumDateWithLogs . ". " .
                "\n Please disable 'Delete old Logs' or set it to a higher deletion threshold (eg. 180 days or 365 years).'.";
        }
        $output[] = "Success. The following dates were invalidated successfully: " .
            implode(", ", $this->processedDates);
        return $output;
    }

    /**
     * @param $period
     * @return bool|int
     */
    private function getPeriodId($period)
    {
        if (!empty($period)) {
            $period = Period\Factory::build($period, Date::today());
        }
        $invalidateForPeriod = $period ? $period->getId() : false;
        return $invalidateForPeriod;
    }

    private static function getModel()
    {
        return new Model();
    }
}