<?php
/**
 * Piwik - free/libre analytics platform
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 */
namespace Piwik\Plugin;

use Piwik\DataTable\Row;
use Piwik\Metrics;

/**
 * Base type of metric metadata classes.
 *
 * A metric metadata class is a class that describes how a metric is described, computed and
 * formatted.
 *
 * There are two types of metrics: aggregated and processed. An aggregated metric is computed
 * in the backend datastore and aggregated in PHP when archiving period reports.
 *
 * Currently, only processed metrics can be defined as metric metadata classes. Support for
 * aggregated metrics will be added at a later date.
 *
 * See {@link Piwik\Plugin\ProcessedMetric} and {@link Piwik\Plugin|AggregatedMetric}.
 *
 * @api
 */
abstract class Metric
{
    /**
     * The sub-namespace name in a plugin where Metric components are stored.
     */
    const COMPONENT_SUBNAMESPACE = 'Metrics';

    /**
     * Returns the column name of this metric, eg, `"nb_visits"` or `"avg_time_on_site"`.
     *
     * This string is what appears in API output.
     *
     * @return string
     */
    abstract public function getName();

    /**
     * Returns the human readable translated name of this metric, eg, `"Visits"` or `"Avg. time on site"`.
     *
     * This string is what appears in the UI.
     *
     * @return string
     */
    abstract public function getTranslatedName();

    /**
     * Returns a string describing what the metric represents. The result will be included in report metadata
     * API output, including processed reports.
     *
     * Implementing this method is optional.
     *
     * @return string
     */
    public function getDocumentation()
    {
        return "";
    }

    /**
     * Returns a formatted metric value. This value is what appears in API output. From within Piwik,
     * (core & plugins) the computed value is used. Only when outputting to the API does a metric
     * get formatted.
     *
     * By default, just returns the value.
     *
     * @return mixed $value
     */
    public function format($value)
    {
        return $value;
    }

    /**
     * Helper method that will access a metric in a {@link Piwik\DataTable\Row} or array either by
     * its name or by its special numerical index value.
     *
     * @param Row|array $row
     * @param string $columnName
     * @param int[]|null $mappingNameToId A custom mapping of metric names to special index values. By
     *                                    default {@link Metrics::getMappingFromNameToId()} is used.
     * @return mixed The metric value or false if none exists.
     */
    public static function getMetric($row, $columnName, $mappingNameToId = null)
    {
        if (empty($mappingIdToName)) {
            $mappingNameToId = Metrics::getMappingFromNameToId();
        }

        if ($row instanceof Row) { // TODO: benchmark w/ array-access (so we don't need this if statement).
            $value = $row->getColumn($columnName);
            if ($value === false
                && isset($mappingNameToId[$columnName])
            ) {
                $value = $row->getColumn($mappingNameToId[$columnName]);
            }
        } else {
            $value = @$row[$columnName];
            if ($value === false
                && isset($mappingNameToId[$columnName])
            ) {
                $value = $row[$mappingNameToId[$columnName]];
            }
            return $value;
        }

        return $value;
    }
}