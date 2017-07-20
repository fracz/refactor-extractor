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
 * @subpackage Piwik_ArchiveProcessing
 */
class Piwik_ArchiveProcessing_Day extends Piwik_ArchiveProcessing
{
    const FIELDS_SEPARATOR = ", \n\t\t\t";
    const LOG_CONVERSION_TABLE = "log_conversion";
    const REVENUE_SUBTOTAL_FIELD = 'revenue_subtotal';
    const REVENUE_TAX_FIELD = 'revenue_tax';
    const REVENUE_SHIPPING_FIELD = 'revenue_shipping';
    const REVENUE_DISCOUNT_FIELD = 'revenue_discount';
    const TOTAL_REVENUE_FIELD = 'revenue';
    const ITEMS_COUNT_FIELD = "items";

    const IDGOAL_FIELD = 'idgoal';

    const CONVERSION_DATETIME_FIELD = "server_time";
    const ACTION_DATETIME_FIELD = "server_time";

    const VISIT_DATETIME_FIELD = 'visit_last_action_time';

    const LOG_ACTIONS_TABLE = 'log_link_visit_action';

    const LOG_VISIT_TABLE = 'log_visit';

    public function queryActionsByDimension($dimensions, $where = '', $additionalSelects = array(), $metrics = false, $rankingQuery = null, $joinLogActionOnColumn = false)
    {
        $tableName = self::LOG_ACTIONS_TABLE;
        $availableMetrics = $this->getActionsMetricFields();

        $select = $this->getSelectStatement($dimensions, $tableName, $additionalSelects, $availableMetrics, $metrics);
        $from = array($tableName);
        $where = $this->getWhereStatement($tableName, self::ACTION_DATETIME_FIELD, $where);
        $groupBy = $this->getGroupByStatement($dimensions, $tableName);
        $orderBy = false;

        if ($joinLogActionOnColumn !== false) {
            $multiJoin = is_array($joinLogActionOnColumn);
            if (!$multiJoin) {
                $joinLogActionOnColumn = array($joinLogActionOnColumn);
            }

            foreach ($joinLogActionOnColumn as $i => $joinColumn) {
                $tableAlias = 'log_action' . ($multiJoin ? $i + 1 : '');
                if (strpos($joinColumn, ' ') === false) {
                    $joinOn = $tableAlias . '.idaction = ' . $tableName . '.' . $joinColumn;
                } else {
                    // more complex join column like IF(...)
                    $joinOn = $tableAlias . '.idaction = ' . $joinColumn;
                }
                $from[] = array(
                    'table'      => 'log_action',
                    'tableAlias' => $tableAlias,
                    'joinOn'     => $joinOn
                );
            }
        }

        if ($rankingQuery) {
            $orderBy = '`' . Piwik_Archive::INDEX_NB_ACTIONS . '` DESC';
        }

        $query = $this->query($select, $from, $where, $groupBy, $orderBy);

        if ($rankingQuery !== null) {
            $sumColumns = array_keys($availableMetrics);
            if ($metrics) {
                $sumColumns = array_intersect($sumColumns, $metrics);
            }
            $rankingQuery->addColumn($sumColumns, 'sum');
            return $rankingQuery->execute($query['sql'], $query['bind']);
        }

        return $this->getDb()->query($query['sql'], $query['bind']);
    }

    protected function getActionsMetricFields()
    {
        return $availableMetrics = array(
            Piwik_Archive::INDEX_NB_VISITS        => "count(distinct " . self::LOG_ACTIONS_TABLE . ".idvisit)",
            Piwik_Archive::INDEX_NB_UNIQ_VISITORS => "count(distinct " . self::LOG_ACTIONS_TABLE . ".idvisitor)",
            Piwik_Archive::INDEX_NB_ACTIONS       => "count(*)",
        );
    }

    protected function query($select, $from, $where, $groupBy, $orderBy)
    {
        $bind = array($this->getStartDatetimeUTC(), $this->getEndDatetimeUTC(), $this->getSite()->getId());

        $query = $this->getSegment()->getSelectQuery($select, $from, $where, $bind, $orderBy, $groupBy);
        return $query;
    }

    /**
     * @see queryVisitsByDimension() Similar to this function,
     * but queries metrics for the requested dimensionRecord,
     * for each Goal conversion
     *
     * @param string|array $dimensions
     * @param string $where
     * @param array $additionalSelects
     * @return PDOStatement
     */
    public function queryConversionsByDimension($dimensions = array(), $where = false, $additionalSelects = array())
    {
        $dimensions = array_merge( array(self::IDGOAL_FIELD), $dimensions );
        $availableMetrics = $this->getConversionsMetricFields();
        $tableName = self::LOG_CONVERSION_TABLE;

        $select = $this->getSelectStatement($dimensions, $tableName, $additionalSelects, $availableMetrics);

        $from = array($tableName);
        $where = $this->getWhereStatement($tableName, self::CONVERSION_DATETIME_FIELD, $where);
        $groupBy = $this->getGroupByStatement($dimensions, $tableName);
        $orderBy =  false;
        $query = $this->query($select, $from, $where, $groupBy, $orderBy);
        return $this->getDb()->query($query['sql'], $query['bind']);
    }

    protected function getSelectStatement($dimensions, $tableName, $additionalSelects, $availableMetrics, $requestedMetrics = false)
    {
        $selects = array_merge(
            $this->getSelectDimensions($dimensions, $tableName),
            $this->getSelectsMetrics($availableMetrics, $requestedMetrics),
            !empty($additionalSelects) ? $additionalSelects : array()
        );
        $select = implode(self::FIELDS_SEPARATOR, $selects);
        return $select;
    }

    static public function getConversionsMetricFields()
    {
        return array(
            Piwik_Archive::INDEX_GOAL_NB_CONVERSIONS             => "count(*)",
            Piwik_Archive::INDEX_GOAL_NB_VISITS_CONVERTED        => "count(distinct " . self::LOG_CONVERSION_TABLE . ".idvisit)",
            Piwik_Archive::INDEX_GOAL_REVENUE                    => self::getSqlConversionRevenueSum(self::TOTAL_REVENUE_FIELD),
            Piwik_Archive::INDEX_GOAL_ECOMMERCE_REVENUE_SUBTOTAL => self::getSqlConversionRevenueSum(self::REVENUE_SUBTOTAL_FIELD),
            Piwik_Archive::INDEX_GOAL_ECOMMERCE_REVENUE_TAX      => self::getSqlConversionRevenueSum(self::REVENUE_TAX_FIELD),
            Piwik_Archive::INDEX_GOAL_ECOMMERCE_REVENUE_SHIPPING => self::getSqlConversionRevenueSum(self::REVENUE_SHIPPING_FIELD),
            Piwik_Archive::INDEX_GOAL_ECOMMERCE_REVENUE_DISCOUNT => self::getSqlConversionRevenueSum(self::REVENUE_DISCOUNT_FIELD),
            Piwik_Archive::INDEX_GOAL_ECOMMERCE_ITEMS            => "SUM(" . self::LOG_CONVERSION_TABLE . "." . self::ITEMS_COUNT_FIELD . ")",
        );
    }

    static public function getSqlConversionRevenueSum($field)
    {
        return self::getSqlRevenue('SUM(' . self::LOG_CONVERSION_TABLE . '.' . $field . ')');
    }

    /**
     * Returns the ecommerce items
     *
     * @param string $field
     * @return string
     */
    public function queryEcommerceItems($field)
    {
        $query = "SELECT
						name as label,
						" . self::getSqlRevenue('SUM(quantity * price)') . " as `" . Piwik_Archive::INDEX_ECOMMERCE_ITEM_REVENUE . "`,
						" . self::getSqlRevenue('SUM(quantity)') . " as `" . Piwik_Archive::INDEX_ECOMMERCE_ITEM_QUANTITY . "`,
						" . self::getSqlRevenue('SUM(price)') . " as `" . Piwik_Archive::INDEX_ECOMMERCE_ITEM_PRICE . "`,
						count(distinct idorder) as `" . Piwik_Archive::INDEX_ECOMMERCE_ORDERS . "`,
						count(idvisit) as `" . Piwik_Archive::INDEX_NB_VISITS . "`,
						case idorder when '0' then " . Piwik_Tracker_GoalManager::IDGOAL_CART . " else " . Piwik_Tracker_GoalManager::IDGOAL_ORDER . " end as ecommerceType
			 	FROM " . Piwik_Common::prefixTable('log_conversion_item') . "
			 		LEFT JOIN " . Piwik_Common::prefixTable('log_action') . "
			 		ON $field = idaction
			 	WHERE server_time >= ?
						AND server_time <= ?
			 			AND idsite = ?
			 			AND deleted = 0
			 	GROUP BY ecommerceType, $field
				ORDER BY null";
        // Segment not supported yet
        // $query = $this->query($select, $from, $where, $groupBy, $orderBy);

        $bind = array($this->getStartDatetimeUTC(),
                      $this->getEndDatetimeUTC(),
                      $this->getSite()->getId()
        );
        $query = $this->getDb()->query($query, $bind);
        return $query;
    }

    /**
     * @param string $field
     * @return string
     */
    static public function getSqlRevenue($field)
    {
        return "ROUND(" . $field . "," . Piwik_Tracker_GoalManager::REVENUE_PRECISION . ")";
    }

    /**
     * Converts the given array to a datatable
     * @param array $array
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
        if(!is_array($dimension)) {
            $dimension = array($dimension);
        }
        if(count($dimension) == 1) {
            $dimension = array("label" => reset($dimension));
        }
        $query = $this->queryVisitsByDimension($dimension);
        $metrics = new Piwik_DataArray();
        while ($row = $query->fetch()) {
            $metrics->sumMetricsVisits($row["label"], $row);
        }
        return $metrics;
    }

    /**
     * Returns true if there are logs for the current archive.
     *
     * If the current archive is for a specific plugin (for example, Referers),
     *   (for example when a Segment is defined and the Keywords report is requested)
     * Then the function will create the Archive for the Core metrics 'VisitsSummary' which will in turn process the number of visits
     *
     * If there is no specified segment, the SQL query will always run.
     *
     * @return bool|null
     */
    public function isThereSomeVisits()
    {
        if (!is_null($this->isThereSomeVisits)) {
            return $this->isThereSomeVisits;
        }

        if (!$this->isProcessingEnabled()) {
            return $this->makeArchiveToCheckForVisits();
        }

        $query = $this->queryVisitsByDimension();
        $data = $query->fetch();

        // no visits found
        if (!is_array($data) || $data[Piwik_Archive::INDEX_NB_VISITS] == 0) {
            return $this->isThereSomeVisits = false;
        }
        $metrics = array();
        foreach($data as $metricId => $value) {
            $readableMetric = Piwik_Archive::$mappingFromIdToName[$metricId];
            $metrics[$readableMetric] = $value;
        }
        $this->insertNumericRecords($metrics);

        $this->setNumberOfVisits($data[Piwik_Archive::INDEX_NB_VISITS]);
        $this->setNumberOfVisitsConverted($data[Piwik_Archive::INDEX_NB_VISITS_CONVERTED]);
        return $this->isThereSomeVisits = true;
    }

    /**
     * Query visits by dimension
     *
     * @param array|string $dimensions     Can be a string, eg. "referer_name", will be aliased as 'label' in the returned rows
     *                                      Can also be an array of strings, when the dimension spans multiple fields,
     *                                      eg. array("referer_name", "referer_keyword")
     * @param bool|string $where Additional condition for WHERE clause
     * @param bool|string $additionalSelects Additional SELECT clause
     * @param bool|array $metrics   Set this if you want to limit the columns that are returned.
     *                                      The possible values in the array are Piwik_Archive::INDEX_*.
     * @param bool|\Piwik_RankingQuery $rankingQuery
     *                                      A pre-configured ranking query instance that is used to limit the result.
     *                                      If set, the return value is the array returned by Piwik_RankingQuery::execute().
     *
     * @return mixed
     */
    public function queryVisitsByDimension(array $dimensions = array(), $where = false, array $additionalSelects = array(), $metrics = false, $rankingQuery = false)
    {
        $tableName = self::LOG_VISIT_TABLE;
        $availableMetrics = $this->getVisitsMetricFields();

        $select = $this->getSelectStatement($dimensions, $tableName, $additionalSelects, $availableMetrics, $metrics);
        $from = array($tableName);
        $where = $this->getWhereStatement($tableName, self::VISIT_DATETIME_FIELD, $where);
        $groupBy = $this->getGroupByStatement($dimensions, $tableName);
        $orderBy = false;

        if ($rankingQuery) {
            $orderBy = '`' . Piwik_Archive::INDEX_NB_VISITS . '` DESC';
        }
        $query = $this->query($select, $from, $where, $groupBy, $orderBy);

        if ($rankingQuery) {
            unset($availableMetrics[Piwik_Archive::INDEX_MAX_ACTIONS]);
            $sumColumns = array_keys($availableMetrics);
            if ($metrics) {
                $sumColumns = array_intersect($sumColumns, $metrics);
            }
            $rankingQuery->addColumn($sumColumns, 'sum');
            if ($this->isMetricRequested(Piwik_Archive::INDEX_MAX_ACTIONS, $metrics)) {
                $rankingQuery->addColumn(Piwik_Archive::INDEX_MAX_ACTIONS, 'max');
            }
            return $rankingQuery->execute($query['sql'], $query['bind']);
        }

        return $this->getDb()->query($query['sql'], $query['bind']);
    }

    protected function getVisitsMetricFields()
    {
        return array(
            Piwik_Archive::INDEX_NB_UNIQ_VISITORS    => "count(distinct " . self::LOG_VISIT_TABLE . ".idvisitor)",
            Piwik_Archive::INDEX_NB_VISITS           => "count(*)",
            Piwik_Archive::INDEX_NB_ACTIONS          => "sum(" . self::LOG_VISIT_TABLE . ".visit_total_actions)",
            Piwik_Archive::INDEX_MAX_ACTIONS         => "max(" . self::LOG_VISIT_TABLE . ".visit_total_actions)",
            Piwik_Archive::INDEX_SUM_VISIT_LENGTH    => "sum(" . self::LOG_VISIT_TABLE . ".visit_total_time)",
            Piwik_Archive::INDEX_BOUNCE_COUNT        => "sum(case " . self::LOG_VISIT_TABLE . ".visit_total_actions when 1 then 1 when 0 then 1 else 0 end)",
            Piwik_Archive::INDEX_NB_VISITS_CONVERTED => "sum(case " . self::LOG_VISIT_TABLE . ".visit_goal_converted when 1 then 1 else 0 end)",
        );
    }

    protected function getWhereStatement($tableName, $datetimeField, $extraWhere = false)
    {
        $where = "$tableName.$datetimeField >= ?
				AND $tableName.$datetimeField <= ?
				AND $tableName.idsite = ?";
        if (!empty($extraWhere)) {
            $extraWhere = sprintf($extraWhere, $tableName, $tableName);
            $where .= ' AND ' . $extraWhere;
        }
        return $where;
    }


    protected function getSelectsMetrics($metricsAvailable, $metricsRequested = false)
    {
        $selects = array();
        foreach ($metricsAvailable as $metricId => $statement) {
            if ($this->isMetricRequested($metricId, $metricsRequested)) {
                $selects[] = $statement . " as `" . $metricId . "`";
            }
        }
        return $selects;
    }

    /**
     * @param $metricId
     * @param $metricsRequested
     * @return bool
     */
    protected function isMetricRequested($metricId, $metricsRequested)
    {
        return $metricsRequested === false
            || in_array($metricId, $metricsRequested);
    }

    /**
     * Returns the actions by the given dimension
     *
     * - The basic use case is to use $dimensionRecord and optionally $where.
     * - If you want to apply a limit and group the others, use $orderBy to sort the way you
     *   want the limit to be applied and pass a pre-configured instance of Piwik_RankingQuery.
     *   The ranking query instance has to have a limit and at least one label column.
     *   See Piwik_RankingQuery::setLimit() and Piwik_RankingQuery::addLabelColumn().
     *   If $rankingQuery is set, the return value is the array returned by
     *   Piwik_RankingQuery::execute().
     * - By default, the method only queries log_link_visit_action. If you need data from
     *   log_action (e.g. to partition the result from the ranking query into the different
     *   action types), use $joinLogActionOnColumn and $additionalSelects to join log_action and select
     *   the column you need from log_action.
     *
     *
     * @param array|string $dimensions      the dimensionRecord(s) you're interested in
     * @param string $where      where clause
     * @param bool|string $additionalSelects  additional select clause
     * @param bool|array $metrics    Set this if you want to limit the columns that are returned.
     *                                  The possible values in the array are Piwik_Archive::INDEX_*.
     * @param Piwik_RankingQuery $rankingQuery     pre-configured ranking query instance
     * @param bool|string $joinLogActionOnColumn  column from log_link_visit_action that
     *                                              log_action should be joined on.
     *                                                can be an array to join multiple times.
     * @internal param bool|string $orderBy order by clause
     * @return mixed
     */
    protected function getGroupByStatement($dimensions, $tableName)
    {
        $dimensions = $this->getSelectDimensions($dimensions, $tableName, $appendSelectAs = false);
        $groupBy = implode(", ", $dimensions);
        return $groupBy;
    }

    protected function getSelectDimensions($dimensions, $tableName, $appendSelectAs = true)
    {
        foreach ($dimensions as $selectAs => &$field) {
            $selectAsString = $field;
            if(!is_numeric($selectAs)) {
                $selectAsString = $selectAs;
            }
            if($selectAsString == $field) {
                $field = "$tableName.$field";
            }
            if($appendSelectAs) {
                $field = "$field AS $selectAsString";
            }
        }
        return $dimensions;
    }

    /**
     * Main method to process logs for a day. The only logic done here is computing the number of visits, actions, etc.
     * All the other reports are computed inside plugins listening to the event 'ArchiveProcessing_Day.compute'.
     * See some of the plugins for an example eg. 'Provider'
     */
    protected function compute()
    {
        if (!$this->isThereSomeVisits()) {
            return;
        }
        Piwik_PostEvent('ArchiveProcessing_Day.compute', $this);
    }

    /**
     * If a segment is specified but a plugin other than 'VisitsSummary' is being requested,
     * we create an archive for processing VisitsSummary Core Metrics, which will in turn
     * execute the query above (in isThereSomeVisits)
     *
     * @return bool|null
     */
    private function makeArchiveToCheckForVisits()
    {
        $archive = $this->makeNewArchive();
        $nbVisits = $archive->getNumeric('nb_visits');
        $this->isThereSomeVisits = $nbVisits > 0;

        if ($this->isThereSomeVisits) {
            $nbVisitsConverted = $archive->getNumeric('nb_visits_converted');
            $this->setNumberOfVisits($nbVisits);
            $this->setNumberOfVisitsConverted($nbVisitsConverted);
        }

        return $this->isThereSomeVisits;
    }

    public function getDb()
    {
        return Zend_Registry::get('db');
    }
}