<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html Gpl v3 or later
 * @version $Id$
 *
 * @package Piwik_Visualization
 */
require_once "Visualization/Chart.php";

/**
 *
 * Customize the Pie chart style for the flash graph
 *
 * @package Piwik_Visualization
 */
class Piwik_Visualization_ChartPie extends Piwik_Visualization_Chart
{
	function customizeGraph()
	{
		parent::customizeGraph();

		$this->prepareData();
//		$this->title( 'PIE Chart', '{font-size: 20px;}' );

	    // strip labels
	    for($i = 0, $cnt = count($this->arrayLabel); $i < $cnt; $i++)
	    {
	    	$label = $this->arrayLabel[$i];
			$this->arrayLabel[$i] = (strlen($label) > 20 ? substr($label, 0, 20).'...' : $label);
	    }
	    $this->set_x_label_style( 12, $this->x_axis_colour, 0, 2, $this->bg_colour );

		//
		$this->pie(60,'#505050','{font-size: 12px; color: #142448}', true);
		//
		// pass in two arrays, one of data, the other data labels
		//
		$this->pie_values( $this->arrayData, $this->arrayLabel );
		//
		// Colours for each slice, in this case some of the colours
		// will be re-used (3 colurs for 5 slices means the last two
		// slices will have colours colour[0] and colour[1]):
		//
		$this->pie_slice_colours( array('#3C5A69','#679BB5','#695A3C','#B58E67','#969696') );

		$this->set_tool_tip( '#x_label# <br>#val# ' );

	}

}