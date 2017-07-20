<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 * @category Piwik_Plugins
 * @package Piwik_API
 */


class Piwik_API_ProcessedReport
{

    /**
     * Loads reports metadata, then return the requested one,
     * matching optional API parameters.
     */
    public function getMetadata($idSite, $apiModule, $apiAction, $apiParameters = array(), $language = false,
                                $period = false, $date = false, $hideMetricsDoc = false, $showSubtableReports = false)
    {
        $reportsMetadata = $this->getReportMetadata($idSite, $period, $date, $hideMetricsDoc, $showSubtableReports);

        foreach ($reportsMetadata as $report) {
            // See ArchiveProcessor/Period.php - unique visitors are not processed for period != day
            if (($period && $period != 'day') && !($apiModule == 'VisitsSummary' && $apiAction == 'get')) {
                unset($report['metrics']['nb_uniq_visitors']);
            }
            if ($report['module'] == $apiModule
                && $report['action'] == $apiAction
            ) {
                // No custom parameters
                if (empty($apiParameters)
                    && empty($report['parameters'])
                ) {
                    return array($report);
                }
                if (empty($report['parameters'])) {
                    continue;
                }
                $diff = array_diff($report['parameters'], $apiParameters);
                if (empty($diff)) {
                    return array($report);
                }
            }
        }
        return false;
    }

    /**
     * Triggers a hook to ask plugins for available Reports.
     * Returns metadata information about each report (category, name, dimension, metrics, etc.)
     *
     * @param string $idSites Comma separated list of website Ids
     * @return array
     */
    public function getReportMetadata($idSites, $period = false, $date = false, $hideMetricsDoc = false, $showSubtableReports = false)
    {
        $idSites = Piwik_Site::getIdSitesFromIdSitesString($idSites);
        if (!empty($idSites)) {
            Piwik::checkUserHasViewAccess($idSites);
        }

        $parameters = array('idSites' => $idSites, 'period' => $period, 'date' => $date);

        $availableReports = array();
        Piwik_PostEvent('API.getReportMetadata', $availableReports, $parameters);
        foreach ($availableReports as &$availableReport) {
            if (!isset($availableReport['metrics'])) {
                $availableReport['metrics'] = Piwik_Metrics::getDefaultMetrics();
            }
            if (!isset($availableReport['processedMetrics'])) {
                $availableReport['processedMetrics'] = Piwik_Metrics::getDefaultProcessedMetrics();
            }

            if ($hideMetricsDoc) // remove metric documentation if it's not wanted
            {
                unset($availableReport['metricsDocumentation']);
            } else if (!isset($availableReport['metricsDocumentation'])) {
                // set metric documentation to default if it's not set
                $availableReport['metricsDocumentation'] = Piwik_Metrics::getDefaultMetricsDocumentation();
            }
        }

        // Some plugins need to add custom metrics after all plugins hooked in
        Piwik_PostEvent('API.getReportMetadata.end', $availableReports, $parameters);
        // Oh this is not pretty! Until we have event listeners order parameter...
        Piwik_PostEvent('API.getReportMetadata.end.end', $availableReports, $parameters);

        // Sort results to ensure consistent order
        usort($availableReports, array($this, 'sort'));

        // Add the magic API.get report metadata aggregating all plugins API.get API calls automatically
        $this->addApiGetMetdata($availableReports);

        $knownMetrics = array_merge(Piwik_Metrics::getDefaultMetrics(), Piwik_Metrics::getDefaultProcessedMetrics());
        foreach ($availableReports as &$availableReport) {
            // Ensure all metrics have a translation
            $metrics = $availableReport['metrics'];
            $cleanedMetrics = array();
            foreach ($metrics as $metricId => $metricTranslation) {
                // When simply the column name was given, ie 'metric' => array( 'nb_visits' )
                // $metricTranslation is in this case nb_visits. We look for a known translation.
                if (is_numeric($metricId)
                    && isset($knownMetrics[$metricTranslation])
                ) {
                    $metricId = $metricTranslation;
                    $metricTranslation = $knownMetrics[$metricTranslation];
                }
                $cleanedMetrics[$metricId] = $metricTranslation;
            }
            $availableReport['metrics'] = $cleanedMetrics;

            // if hide/show columns specified, hide/show metrics & docs
            $availableReport['metrics'] = $this->hideShowMetrics($availableReport['metrics']);
            if (isset($availableReport['processedMetrics'])) {
                $availableReport['processedMetrics'] = $this->hideShowMetrics($availableReport['processedMetrics']);
            }
            if (isset($availableReport['metricsDocumentation'])) {
                $availableReport['metricsDocumentation'] =
                    $this->hideShowMetrics($availableReport['metricsDocumentation']);
            }

            // Remove array elements that are false (to clean up API output)
            foreach ($availableReport as $attributeName => $attributeValue) {
                if (empty($attributeValue)) {
                    unset($availableReport[$attributeName]);
                }
            }
            // when there are per goal metrics, don't display conversion_rate since it can differ from per goal sum
            if (isset($availableReport['metricsGoal'])) {
                unset($availableReport['processedMetrics']['conversion_rate']);
                unset($availableReport['metricsGoal']['conversion_rate']);
            }

            // Processing a uniqueId for each report,
            // can be used by UIs as a key to match a given report
            $uniqueId = $availableReport['module'] . '_' . $availableReport['action'];
            if (!empty($availableReport['parameters'])) {
                foreach ($availableReport['parameters'] as $key => $value) {
                    $uniqueId .= '_' . $key . '--' . $value;
                }
            }
            $availableReport['uniqueId'] = $uniqueId;

            // Order is used to order reports internally, but not meant to be used outside
            unset($availableReport['order']);
        }

        // remove subtable reports
        if (!$showSubtableReports) {
            foreach ($availableReports as $idx => $report) {
                if (isset($report['isSubtableReport']) && $report['isSubtableReport']) {
                    unset($availableReports[$idx]);
                }
            }
        }

        return array_values($availableReports); // make sure array has contiguous key values
    }


    /**
     * API metadata are sorted by category/name,
     * with a little tweak to replicate the standard Piwik category ordering
     *
     * @param string $a
     * @param string $b
     * @return int
     */
    private function sort($a, $b)
    {
        static $order = null;
        if (is_null($order)) {
            $order = array(
                Piwik_Translate('General_MultiSitesSummary'),
                Piwik_Translate('VisitsSummary_VisitsSummary'),
                Piwik_Translate('Goals_Ecommerce'),
                Piwik_Translate('Actions_Actions'),
                Piwik_Translate('Actions_SubmenuSitesearch'),
                Piwik_Translate('Referers_Referers'),
                Piwik_Translate('Goals_Goals'),
                Piwik_Translate('General_Visitors'),
                Piwik_Translate('DevicesDetection_DevicesDetection'),
                Piwik_Translate('UserSettings_VisitorSettings'),
            );
        }
        return ($category = strcmp(array_search($a['category'], $order), array_search($b['category'], $order))) == 0
            ? (@$a['order'] < @$b['order'] ? -1 : 1)
            : $category;
    }

    /**
     * Add the metadata for the API.get report
     * In other plugins, this would hook on 'API.getReportMetadata'
     */
    private function addApiGetMetdata(&$availableReports)
    {
        $metadata = array(
            'category'             => Piwik_Translate('General_API'),
            'name'                 => Piwik_Translate('General_MainMetrics'),
            'module'               => 'API',
            'action'               => 'get',
            'metrics'              => array(),
            'processedMetrics'     => array(),
            'metricsDocumentation' => array(),
            'order'                => 1
        );

        $indexesToMerge = array('metrics', 'processedMetrics', 'metricsDocumentation');

        foreach ($availableReports as $report) {
            if ($report['action'] == 'get') {
                foreach ($indexesToMerge as $index) {
                    if (isset($report[$index])
                        && is_array($report[$index])
                    ) {
                        $metadata[$index] = array_merge($metadata[$index], $report[$index]);
                    }
                }
            }
        }

        $availableReports[] = $metadata;
    }



    public function getProcessedReport($idSite, $period, $date, $apiModule, $apiAction, $segment = false,
                                       $apiParameters = false, $idGoal = false, $language = false,
                                       $showTimer = true, $hideMetricsDoc = false, $idSubtable = false, $showRawMetrics = false)
    {
        $timer = new Piwik_Timer();
        if (empty($apiParameters)) {
            $apiParameters = array();
        }
        if (!empty($idGoal)
            && empty($apiParameters['idGoal'])
        ) {
            $apiParameters['idGoal'] = $idGoal;
        }
        // Is this report found in the Metadata available reports?
        $reportMetadata = $this->getMetadata($idSite, $apiModule, $apiAction, $apiParameters, $language,
            $period, $date, $hideMetricsDoc, $showSubtableReports = true);
        if (empty($reportMetadata)) {
            throw new Exception("Requested report $apiModule.$apiAction for Website id=$idSite not found in the list of available reports. \n");
        }
        $reportMetadata = reset($reportMetadata);

        // Generate Api call URL passing custom parameters
        $parameters = array_merge($apiParameters, array(
                                                       'method'     => $apiModule . '.' . $apiAction,
                                                       'idSite'     => $idSite,
                                                       'period'     => $period,
                                                       'date'       => $date,
                                                       'format'     => 'original',
                                                       'serialize'  => '0',
                                                       'language'   => $language,
                                                       'idSubtable' => $idSubtable,
                                                  ));
        if (!empty($segment)) $parameters['segment'] = $segment;

        $url = Piwik_Url::getQueryStringFromParameters($parameters);
        $request = new Piwik_API_Request($url);
        try {
            /** @var Piwik_DataTable */
            $dataTable = $request->process();
        } catch (Exception $e) {
            throw new Exception("API returned an error: " . $e->getMessage() . " at " . basename($e->getFile()) . ":" . $e->getLine() . "\n");
        }

        list($newReport, $columns, $rowsMetadata) = $this->handleTableReport($idSite, $dataTable, $reportMetadata, $showRawMetrics);
        foreach ($columns as $columnId => &$name) {
            $name = ucfirst($name);
        }
        $website = new Piwik_Site($idSite);

        $period = Piwik_Period::advancedFactory($period, $date);
        $period = $period->getLocalizedLongString();

        $return = array(
            'website'        => $website->getName(),
            'prettyDate'     => $period,
            'metadata'       => $reportMetadata,
            'columns'        => $columns,
            'reportData'     => $newReport,
            'reportMetadata' => $rowsMetadata,
        );
        if ($showTimer) {
            $return['timerMillis'] = $timer->getTimeMs(0);
        }
        return $return;
    }

    /**
     * Enhance a $dataTable using metadata :
     *
     * - remove metrics based on $reportMetadata['metrics']
     * - add 0 valued metrics if $dataTable doesn't provide all $reportMetadata['metrics']
     * - format metric values to a 'human readable' format
     * - extract row metadata to a separate Piwik_DataTable_Simple|Piwik_DataTable_Array : $rowsMetadata
     * - translate metric names to a separate array : $columns
     *
     * @param int $idSite enables monetary value formatting based on site currency
     * @param Piwik_DataTable|Piwik_DataTable_Array $dataTable
     * @param array $reportMetadata
     * @param boolean $hasDimension
     * @return array Piwik_DataTable_Simple|Piwik_DataTable_Array $newReport with human readable format & array $columns list of translated column names & Piwik_DataTable_Simple|Piwik_DataTable_Array $rowsMetadata
     **/
    private function handleTableReport($idSite, $dataTable, &$reportMetadata, $showRawMetrics = false)
    {
        $hasDimension = isset($reportMetadata['dimension']);
        $columns = $reportMetadata['metrics'];

        if ($hasDimension) {
            $columns = array_merge(
                array('label' => $reportMetadata['dimension']),
                $columns
            );

            if (isset($reportMetadata['processedMetrics'])) {
                $processedMetricsAdded = Piwik_Metrics::getDefaultProcessedMetrics();
                foreach ($processedMetricsAdded as $processedMetricId => $processedMetricTranslation) {
                    // this processed metric can be displayed for this report
                    if (isset($reportMetadata['processedMetrics'][$processedMetricId])) {
                        $columns[$processedMetricId] = $processedMetricTranslation;
                    }
                }
            }

            // Display the global Goal metrics
            if (isset($reportMetadata['metricsGoal'])) {
                $metricsGoalDisplay = array('revenue');
                // Add processed metrics to be displayed for this report
                foreach ($metricsGoalDisplay as $goalMetricId) {
                    if (isset($reportMetadata['metricsGoal'][$goalMetricId])) {
                        $columns[$goalMetricId] = $reportMetadata['metricsGoal'][$goalMetricId];
                    }
                }
            }

            if (isset($reportMetadata['processedMetrics'])) {
                // Add processed metrics
                $dataTable->filter('AddColumnsProcessedMetrics', array($deleteRowsWithNoVisit = false));
            }
        }

        $columns = $this->hideShowMetrics($columns);

        // $dataTable is an instance of Piwik_DataTable_Array when multiple periods requested
        if ($dataTable instanceof Piwik_DataTable_Array) {
            // Need a new Piwik_DataTable_Array to store the 'human readable' values
            $newReport = new Piwik_DataTable_Array();
            $newReport->setKeyName("prettyDate");

            // Need a new Piwik_DataTable_Array to store report metadata
            $rowsMetadata = new Piwik_DataTable_Array();
            $rowsMetadata->setKeyName("prettyDate");

            // Process each Piwik_DataTable_Simple entry
            foreach ($dataTable->getArray() as $label => $simpleDataTable) {
                $this->removeEmptyColumns($columns, $reportMetadata, $simpleDataTable);

                list($enhancedSimpleDataTable, $rowMetadata) = $this->handleSimpleDataTable($idSite, $simpleDataTable, $columns, $hasDimension, $showRawMetrics);
                $enhancedSimpleDataTable->metadata = $simpleDataTable->metadata;

                $period = $simpleDataTable->metadata['period']->getLocalizedLongString();
                $newReport->addTable($enhancedSimpleDataTable, $period);
                $rowsMetadata->addTable($rowMetadata, $period);
            }
        } else {
            $this->removeEmptyColumns($columns, $reportMetadata, $dataTable);
            list($newReport, $rowsMetadata) = $this->handleSimpleDataTable($idSite, $dataTable, $columns, $hasDimension, $showRawMetrics);
        }

        return array(
            $newReport,
            $columns,
            $rowsMetadata
        );
    }

    /**
     * Removes metrics from the list of columns and the report meta data if they are marked empty
     * in the data table meta data.
     */
    private function removeEmptyColumns(&$columns, &$reportMetadata, $dataTable)
    {
        $emptyColumns = $dataTable->getMetadata(Piwik_DataTable::EMPTY_COLUMNS_METADATA_NAME);

        if (!is_array($emptyColumns)) {
            return;
        }

        $columns = $this->hideShowMetrics($columns, $emptyColumns);

        if (isset($reportMetadata['metrics'])) {
            $reportMetadata['metrics'] = $this->hideShowMetrics($reportMetadata['metrics'], $emptyColumns);
        }

        if (isset($reportMetadata['metricsDocumentation'])) {
            $reportMetadata['metricsDocumentation'] = $this->hideShowMetrics($reportMetadata['metricsDocumentation'], $emptyColumns);
        }
    }

    /**
     * Removes column names from an array based on the values in the hideColumns,
     * showColumns query parameters. This is a hack that provides the ColumnDelete
     * filter functionality in processed reports.
     *
     * @param array $columns List of metrics shown in a processed report.
     * @param array $emptyColumns Empty columns from the data table meta data.
     * @return array Filtered list of metrics.
     */
    private function hideShowMetrics($columns, $emptyColumns = array())
    {
        if (!is_array($columns)) {
            return $columns;
        }

        // remove columns if hideColumns query parameters exist
        $columnsToRemove = Piwik_Common::getRequestVar('hideColumns', '');
        if ($columnsToRemove != '') {
            $columnsToRemove = explode(',', $columnsToRemove);
            foreach ($columnsToRemove as $name) {
                // if a column to remove is in the column list, remove it
                if (isset($columns[$name])) {
                    unset($columns[$name]);
                }
            }
        }

        // remove columns if showColumns query parameters exist
        $columnsToKeep = Piwik_Common::getRequestVar('showColumns', '');
        if ($columnsToKeep != '') {
            $columnsToKeep = explode(',', $columnsToKeep);
            $columnsToKeep[] = 'label';

            foreach ($columns as $name => $ignore) {
                // if the current column should not be kept, remove it
                $idx = array_search($name, $columnsToKeep);
                if ($idx === false) // if $name is not in $columnsToKeep
                {
                    unset($columns[$name]);
                }
            }
        }

        // remove empty columns
        if (is_array($emptyColumns)) {
            foreach ($emptyColumns as $column) {
                if (isset($columns[$column])) {
                    unset($columns[$column]);
                }
            }
        }

        return $columns;
    }

    /**
     * Enhance $simpleDataTable using metadata :
     *
     * - remove metrics based on $reportMetadata['metrics']
     * - add 0 valued metrics if $simpleDataTable doesn't provide all $reportMetadata['metrics']
     * - format metric values to a 'human readable' format
     * - extract row metadata to a separate Piwik_DataTable_Simple $rowsMetadata
     *
     * @param int $idSite enables monetary value formatting based on site currency
     * @param Piwik_DataTable_Simple $simpleDataTable
     * @param array $metadataColumns
     * @param boolean $hasDimension
     * @param bool $returnRawMetrics If set to true, the original metrics will be returned
     *
     * @return array Piwik_DataTable $enhancedDataTable filtered metrics with human readable format & Piwik_DataTable_Simple $rowsMetadata
     */
    private function handleSimpleDataTable($idSite, $simpleDataTable, $metadataColumns, $hasDimension, $returnRawMetrics = false)
    {
        // new DataTable to store metadata
        $rowsMetadata = new Piwik_DataTable();

        // new DataTable to store 'human readable' values
        if ($hasDimension) {
            $enhancedDataTable = new Piwik_DataTable();
        } else {
            $enhancedDataTable = new Piwik_DataTable_Simple();
        }

        // add missing metrics
        foreach ($simpleDataTable->getRows() as $row) {
            $rowMetrics = $row->getColumns();
            foreach ($metadataColumns as $id => $name) {
                if (!isset($rowMetrics[$id])) {
                    $row->addColumn($id, 0);
                }
            }
        }

        foreach ($simpleDataTable->getRows() as $row) {
            $enhancedRow = new Piwik_DataTable_Row();
            $enhancedDataTable->addRow($enhancedRow);
            $rowMetrics = $row->getColumns();
            foreach ($rowMetrics as $columnName => $columnValue) {
                // filter metrics according to metadata definition
                if (isset($metadataColumns[$columnName])) {
                    // generate 'human readable' metric values
                    $prettyValue = Piwik::getPrettyValue($idSite, $columnName, $columnValue, $htmlAllowed = false);
                    $enhancedRow->addColumn($columnName, $prettyValue);
                } // For example the Maps Widget requires the raw metrics to do advanced datavis
                elseif ($returnRawMetrics) {
                    $enhancedRow->addColumn($columnName, $columnValue);
                }
            }

            // If report has a dimension, extract metadata into a distinct DataTable
            if ($hasDimension) {
                $rowMetadata = $row->getMetadata();
                $idSubDataTable = $row->getIdSubDataTable();

                // Create a row metadata only if there are metadata to insert
                if (count($rowMetadata) > 0 || !is_null($idSubDataTable)) {
                    $metadataRow = new Piwik_DataTable_Row();
                    $rowsMetadata->addRow($metadataRow);

                    foreach ($rowMetadata as $metadataKey => $metadataValue) {
                        $metadataRow->addColumn($metadataKey, $metadataValue);
                    }

                    if (!is_null($idSubDataTable)) {
                        $metadataRow->addColumn('idsubdatatable', $idSubDataTable);
                    }
                }
            }
        }

        return array(
            $enhancedDataTable,
            $rowsMetadata
        );
    }

}