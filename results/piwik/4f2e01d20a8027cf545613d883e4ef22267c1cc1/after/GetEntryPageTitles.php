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
use Piwik\Plugins\Actions\API;
use Piwik\API\Request;
use Piwik\Common;
use Piwik\Plugins\Actions\Columns\EntryPageTitle;
use Piwik\Plugins\Actions\Columns\PageTitle;

class GetEntryPageTitles extends Base
{
    protected function init()
    {
        parent::init();

        $this->dimension     = new EntryPageTitle();
        $this->name          = Piwik::translate('Actions_EntryPageTitles');
        $this->documentation = Piwik::translate('Actions_ExitPageTitlesReportDocumentation', '<br />')
                             . ' ' . Piwik::translate('General_UsePlusMinusIconsDocumentation');
        $this->metrics = array('entry_nb_visits', 'entry_bounce_count', 'bounce_rate');
        $this->order   = 6;
        $this->actionToLoadSubTables = $this->action;

        $this->widgetTitle = 'Actions_WidgetEntryPageTitles';
    }

    public function configureView(ViewDataTable $view)
    {
        $entryPageUrlAction =
            Common::getRequestVar('widget', false) === false ? 'indexEntryPageUrls' : 'getEntryPageUrls';

        $view->config->addTranslations(array(
            'label'              => $this->dimension->getName(),
            'entry_bounce_count' => Piwik::translate('General_ColumnBounces'),
            'entry_nb_visits'    => Piwik::translate('General_ColumnEntrances'),
        ));
        $view->config->addRelatedReports(array(
            'Actions.getPageTitles'       => Piwik::translate('Actions_SubmenuPageTitles'),
            "Actions.$entryPageUrlAction" => Piwik::translate('Actions_SubmenuPagesEntry')
        ));

        $view->config->columns_to_display = array('label', 'entry_nb_visits', 'entry_bounce_count', 'bounce_rate');
        $view->config->title = $this->name;

        $view->requestConfig->filter_sort_column = 'entry_nb_visits';

        $this->addPageDisplayProperties($view);
        $this->addBaseDisplayProperties($view);
    }

    public function getRelatedReports()
    {
        return array(
            new GetPageTitles(),
            new GetEntryPageUrls()
        );
    }
}