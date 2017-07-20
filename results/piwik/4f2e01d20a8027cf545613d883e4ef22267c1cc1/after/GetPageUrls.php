<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 */
namespace Piwik\Plugins\Actions\Reports;

use Piwik\Piwik;
use Piwik\Plugin\ViewDataTable;
use Piwik\Plugins\Actions\Columns\PageUrl;

class GetPageUrls extends Base
{
    protected function init()
    {
        parent::init();

        $this->dimension     = new PageUrl();
        $this->name          = Piwik::translate('Actions_PageUrls');
        $this->title         = Piwik::translate('General_Pages');
        $this->documentation = Piwik::translate('Actions_PagesReportDocumentation', '<br />')
                             . '<br />' . Piwik::translate('General_UsePlusMinusIconsDocumentation');

        $this->actionToLoadSubTables = $this->action;
        $this->order   = 1;
        $this->metrics = array(
            'nb_hits',
            'nb_visits',
            'bounce_rate',
            'avg_time_on_page',
            'exit_rate',
            'avg_time_generation'
        );

        $this->segmentSql = 'log_visit.visit_entry_idaction_url';

        $this->menuTitle   = 'General_Pages';
        $this->widgetTitle = 'General_Pages';
    }

    public function configureView(ViewDataTable $view)
    {
        $view->config->addTranslation('label', $this->dimension->getName());
        $view->config->columns_to_display = array('label', 'nb_hits', 'nb_visits', 'bounce_rate',
                                                  'avg_time_on_page', 'exit_rate', 'avg_time_generation');

        $this->addPageDisplayProperties($view);
        $this->addBaseDisplayProperties($view);
    }
}