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

/**
 * Handles the archiving process for a day.
 * The class provides generic helper methods to manipulate data from the DB,
 * easily create Piwik_DataTable objects from running SELECT ... GROUP BY on the log_visit table.
 *
 * All the logic of the archiving is done inside the plugins listening to the event 'ArchiveProcessing_Day.compute'
 *
 * @package Piwik
 * @subpackage Piwik_ArchiveProcessor
 */
class Piwik_ArchiveProcessor_Day extends Piwik_ArchiveProcessor
{
    protected $logAggregator = null;

    function __construct(Piwik_Period $period, Piwik_Site $site, Piwik_Segment $segment) {
        parent::__construct($period, $site, $segment);
        $this->logAggregator = new Piwik_DataAccess_LogAggregator($period->getDateStart(), $period->getDateEnd(), $site, $segment);
    }
    /**
     * @return Piwik_DataAccess_LogAggregator
     */
    public function getLogAggregator()
    {
        return $this->logAggregator;
    }

    /**
     * Converts the given array to a datatable
     * @param Piwik_DataArray $array
     * @return Piwik_DataTable
     */
    static public function getDataTableFromDataArray(Piwik_DataArray $array)
    {
        $dataArray = $array->getDataArray();
        $dataArrayTwoLevels = $array->getDataArrayWithTwoLevels();

        $subtableByLabel = null;
        if (!empty($dataArrayTwoLevels)) {
            $subtableByLabel = array();
            foreach ($dataArrayTwoLevels as $label => $subTable) {
                $subtableByLabel[$label] = Piwik_DataTable::makeFromIndexedArray($subTable);
            }
        }
        return Piwik_DataTable::makeFromIndexedArray($dataArray, $subtableByLabel);
    }

    /**
     * Output:
     *        array(
     *            LABEL => array(
     *                        Piwik_Archive::INDEX_NB_UNIQ_VISITORS    => 0,
     *                        Piwik_Archive::INDEX_NB_VISITS            => 0
     *                    ),
     *            LABEL2 => array(
     *                    [...]
     *                    )
     *        )
     *
     * Helper function that returns an array with common statistics for a given database field distinct values.
     *
     * The statistics returned are:
     *  - number of unique visitors
     *  - number of visits
     *  - number of actions
     *  - maximum number of action for a visit
     *  - sum of the visits' length in sec
     *  - count of bouncing visits (visits with one page view)
     *
     * For example if $dimension = 'config_os' it will return the statistics for every distinct Operating systems
     * The returned array will have a row per distinct operating systems,
     * and a column per stat (nb of visits, max  actions, etc)
     *
     * 'label'    Piwik_Archive::INDEX_NB_UNIQ_VISITORS    Piwik_Archive::INDEX_NB_VISITS    etc.
     * Linux    27    66    ...
     * Windows XP    12    ...
     * Mac OS    15    36    ...
     *
     * @param string $dimension  Table log_visit field name to be use to compute common stats
     * @return Piwik_DataArray
     */
    public function getMetricsForDimension($dimension)
    {
        if (!is_array($dimension)) {
            $dimension = array($dimension);
        }
        if (count($dimension) == 1) {
            $dimension = array("label" => reset($dimension));
        }
        $query = $this->getLogAggregator()->queryVisitsByDimension($dimension);
        $metrics = new Piwik_DataArray();
        while ($row = $query->fetch()) {
            $metrics->sumMetricsVisits($row["label"], $row);
        }
        return $metrics;
    }

    protected function aggregateCoreVisitsMetrics()
    {
        $query = $this->getLogAggregator()->queryVisitsByDimension();
        $data = $query->fetch();

        $metrics = $this->convertMetricsIdToName($data);
        $this->insertNumericRecords($metrics);
        return $metrics;
    }


    /**
     * @param $data
     * @return array
     */
    protected function convertMetricsIdToName($data)
    {
        $metrics = array();
        foreach ($data as $metricId => $value) {
            $readableMetric = Piwik_Archive::$mappingFromIdToName[$metricId];
            $metrics[$readableMetric] = $value;
        }
        return $metrics;
    }

    protected function compute()
    {
        Piwik_PostEvent('ArchiveProcessing_Day.compute', $this);
    }
}