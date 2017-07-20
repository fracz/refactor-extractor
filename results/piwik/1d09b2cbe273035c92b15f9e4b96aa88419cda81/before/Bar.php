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

namespace Piwik\Plugins\CoreVisualizations\Visualizations\JqplotGraph;

use Piwik\Plugins\CoreVisualizations\Visualizations\JqplotGraph;
use Piwik\Plugins\CoreVisualizations\JqplotDataGenerator;

/**
 * Visualization that renders HTML for a Bar graph using jqPlot.
 */
class Bar extends JqplotGraph
{
    const ID = 'graphVerticalBar';

    public static function getDefaultPropertyValues()
    {
        $result = parent::getDefaultPropertyValues();
        $result['visualization_properties']['max_graph_elements'] = 6;
        return $result;
    }

    protected function makeDataGenerator($properties)
    {
        return JqplotDataGenerator::factory('bar', $properties);
    }
}