<?php
/**
 * Piwik - free/libre analytics platform
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 */
namespace Piwik\Plugins\UserSettings\Columns;

use Piwik\Common;
use Piwik\Plugin\Dimension\VisitDimension;
use Piwik\Tracker\Request;
use Piwik\Tracker\Visitor;
use Piwik\Tracker\Action;

class PluginRealPlayer extends VisitDimension
{
    protected $columnName = 'config_realplayer';
    protected $columnType = 'TINYINT(1) NOT NULL';

    public function getName()
    {
        return '';
    }

    /**
     * @param Request $request
     * @param Visitor $visitor
     * @param Action|null $action
     * @return mixed
     */
    public function onNewVisit(Request $request, Visitor $visitor, $action)
    {
        return Common::getRequestVar('realp', 0, 'int', $request->getParams());
    }
}