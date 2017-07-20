<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 */
namespace Piwik\Plugins\Actions\Reports;

use Piwik\Common;
use Piwik\Piwik;
use Piwik\Plugin\ViewDataTable;
use Piwik\Plugins\Actions\API;
use Piwik\API\Request;
use Piwik\Plugins\Actions\Columns\EntryPageUrl;
use Piwik\Plugins\Actions\Columns\PageUrl;

class GetEntryPageUrls extends Base
{
    protected function init()
    {
        parent::init();

        $this->dimension     = new EntryPageUrl();
        $this->name          = Piwik::translate('Actions_SubmenuPagesEntry');
        $this->documentation = Piwik::translate('Actions_EntryPagesReportDocumentation', '<br />')
                             . '<br />' . Piwik::translate('General_UsePlusMinusIconsDocumentation');

        $this->metrics = array('entry_nb_visits', 'entry_bounce_count', 'bounce_rate');
        $this->order   = 3;

        $this->actionToLoadSubTables = $this->action;

        $this->menuTitle   = 'Actions_SubmenuPagesEntry';
        $this->widgetTitle = 'Actions_WidgetPagesEntry';
    }

    public function configureView(ViewDataTable $view)
    {
        // link to the page, not just the report, but only if not a widget
        $widget = Common::getRequestVar('widget', false);

        $view->config->self_url = Request::getCurrentUrlWithoutGenericFilters(array(
            'module' => 'Actions',
            'action' => $widget === false ? 'indexEntryPageUrls' : 'getEntryPageUrls'
        ));

        $view->config->addTranslations(array(
            'label'              => $this->dimension->getName(),
            'entry_bounce_count' => Piwik::translate('General_ColumnBounces'),
            'entry_nb_visits'    => Piwik::translate('General_ColumnEntrances'))
        );

       // $view->config->title = $this->name;
        $view->config->addRelatedReport('Actions.getEntryPageTitles', Piwik::translate('Actions_EntryPageTitles'));
        $view->config->columns_to_display = array('label', 'entry_nb_visits', 'entry_bounce_count', 'bounce_rate');
        $view->requestConfig->filter_sort_column = 'entry_nb_visits';
        $view->requestConfig->filter_sort_order  = 'desc';

        $this->addPageDisplayProperties($view);
        $this->addBaseDisplayProperties($view);
    }

    public function getRelatedReports()
    {
        return array(
            new GetEntryPageTitles()
        );
    }
}