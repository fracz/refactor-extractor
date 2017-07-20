<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 * @category Piwik_Plugins
 * @package UserCountry
 */
namespace Piwik\Plugins\UserCountry;

use Piwik\ArchiveProcessor;
use Piwik\Common;
use Piwik\Piwik;
use Piwik\WidgetsList;
use Piwik\Url;
use Piwik\Plugins\UserCountry\Archiver;
use Piwik\Plugins\UserCountry\GeoIPAutoUpdater;
use Piwik\Plugins\UserCountry\LocationProvider;
use Piwik\Plugins\UserCountry\LocationProvider\DefaultProvider;
use Piwik\Plugins\UserCountry\LocationProvider\GeoIp;

/**
 * @see plugins/UserCountry/GeoIPAutoUpdater.php
 */
require_once PIWIK_INCLUDE_PATH . '/plugins/UserCountry/GeoIPAutoUpdater.php';

/**
 *
 * @package UserCountry
 */
class UserCountry extends \Piwik\Plugin
{
    /**
     * @see Piwik_Plugin::getListHooksRegistered
     */
    public function getListHooksRegistered()
    {
        $hooks = array(
            'ArchiveProcessing_Day.compute'            => 'archiveDay',
            'ArchiveProcessing_Period.compute'         => 'archivePeriod',
            'WidgetsList.add'                          => 'addWidgets',
            'Menu.add'                                 => 'addMenu',
            'AdminMenu.add'                            => 'addAdminMenu',
            'Goals.getReportsWithGoalMetrics'          => 'getReportsWithGoalMetrics',
            'API.getReportMetadata'                    => 'getReportMetadata',
            'API.getSegmentsMetadata'                  => 'getSegmentsMetadata',
            'AssetManager.getStylesheetFiles'          => 'getStylesheetFiles',
            'AssetManager.getJsFiles'                  => 'getJsFiles',
            'Tracker.getVisitorLocation'               => 'getVisitorLocation',
            'TaskScheduler.getScheduledTasks'          => 'getScheduledTasks',
            'ViewDataTable.getReportDisplayProperties' => 'getReportDisplayProperties',
        );
        return $hooks;
    }

    public function getScheduledTasks(&$tasks)
    {
        // add the auto updater task
        $tasks[] = GeoIPAutoUpdater::makeScheduledTask();
    }

    public function getStylesheetFiles(&$stylesheets)
    {
        $stylesheets[] = "plugins/UserCountry/stylesheets/userCountry.less";
    }

    public function getJsFiles(&$jsFiles)
    {
        $jsFiles[] = "plugins/UserCountry/javascripts/userCountry.js";
    }

    public function getVisitorLocation(&$location, $visitorInfo)
    {
        require_once PIWIK_INCLUDE_PATH . "/plugins/UserCountry/LocationProvider.php";

        $id = Common::getCurrentLocationProviderId();
        $provider = LocationProvider::getProviderById($id);
        if ($provider === false) {
            $id = DefaultProvider::ID;
            $provider = LocationProvider::getProviderById($id);
            Common::printDebug("GEO: no current location provider sent, falling back to default '$id' one.");
        }

        $location = $provider->getLocation($visitorInfo);

        // if we can't find a location, use default provider
        if ($location === false) {
            $defaultId = DefaultProvider::ID;
            $provider = LocationProvider::getProviderById($defaultId);
            $location = $provider->getLocation($visitorInfo);
            Common::printDebug("GEO: couldn't find a location with Geo Module '$id', using Default '$defaultId' provider as fallback...");
            $id = $defaultId;
        }
        Common::printDebug("GEO: Found IP location (provider '" . $id . "'): " . var_export($location, true));
    }

    public function addWidgets()
    {
        $widgetContinentLabel = Piwik_Translate('UserCountry_WidgetLocation')
            . ' (' . Piwik_Translate('UserCountry_Continent') . ')';
        $widgetCountryLabel = Piwik_Translate('UserCountry_WidgetLocation')
            . ' (' . Piwik_Translate('UserCountry_Country') . ')';
        $widgetRegionLabel = Piwik_Translate('UserCountry_WidgetLocation')
            . ' (' . Piwik_Translate('UserCountry_Region') . ')';
        $widgetCityLabel = Piwik_Translate('UserCountry_WidgetLocation')
            . ' (' . Piwik_Translate('UserCountry_City') . ')';

        WidgetsList::add('General_Visitors', $widgetContinentLabel, 'UserCountry', 'getContinent');
        WidgetsList::add('General_Visitors', $widgetCountryLabel, 'UserCountry', 'getCountry');
        WidgetsList::add('General_Visitors', $widgetRegionLabel, 'UserCountry', 'getRegion');
        WidgetsList::add('General_Visitors', $widgetCityLabel, 'UserCountry', 'getCity');
    }

    public function addMenu()
    {
        Piwik_AddMenu('General_Visitors', 'UserCountry_SubmenuLocations', array('module' => 'UserCountry', 'action' => 'index'));
    }

    /**
     * Event handler. Adds menu items to the Admin menu.
     */
    public function addAdminMenu()
    {
        Piwik_AddAdminSubMenu('General_Settings', 'UserCountry_Geolocation',
            array('module' => 'UserCountry', 'action' => 'adminIndex'),
            Piwik::isUserIsSuperUser(),
            $order = 8);
    }

    public function getSegmentsMetadata(&$segments)
    {
        $segments[] = array(
            'type'           => 'dimension',
            'category'       => 'Visit Location',
            'name'           => Piwik_Translate('UserCountry_Country'),
            'segment'        => 'countryCode',
            'sqlSegment'     => 'log_visit.location_country',
            'acceptedValues' => 'de, us, fr, in, es, etc.',
        );
        $segments[] = array(
            'type'           => 'dimension',
            'category'       => 'Visit Location',
            'name'           => Piwik_Translate('UserCountry_Continent'),
            'segment'        => 'continentCode',
            'sqlSegment'     => 'log_visit.location_country',
            'acceptedValues' => 'eur, asi, amc, amn, ams, afr, ant, oce',
            'sqlFilter'      => __NAMESPACE__ . '\UserCountry::getCountriesForContinent',
        );
        $segments[] = array(
            'type'           => 'dimension',
            'category'       => 'Visit Location',
            'name'           => Piwik_Translate('UserCountry_Region'),
            'segment'        => 'regionCode',
            'sqlSegment'     => 'log_visit.location_region',
            'acceptedValues' => '01 02, OR, P8, etc.<br/>eg. region=A1;country=fr',
        );
        $segments[] = array(
            'type'           => 'dimension',
            'category'       => 'Visit Location',
            'name'           => Piwik_Translate('UserCountry_City'),
            'segment'        => 'city',
            'sqlSegment'     => 'log_visit.location_city',
            'acceptedValues' => 'Sydney, Sao Paolo, Rome, etc.',
        );
        $segments[] = array(
            'type'           => 'dimension',
            'category'       => 'Visit Location',
            'name'           => Piwik_Translate('UserCountry_Latitude'),
            'segment'        => 'latitude',
            'sqlSegment'     => 'log_visit.location_latitude',
            'acceptedValues' => '-33.578, 40.830, etc.<br/>You can select visitors within a lat/long range using &segment=lat&gt;X;lat&lt;Y;long&gt;M;long&lt;N.',
        );
        $segments[] = array(
            'type'           => 'dimension',
            'category'       => 'Visit Location',
            'name'           => Piwik_Translate('UserCountry_Longitude'),
            'segment'        => 'longitude',
            'sqlSegment'     => 'log_visit.location_longitude',
            'acceptedValues' => '-70.664, 14.326, etc.',
        );
    }

    public function getReportMetadata(&$reports)
    {
        $metrics = array(
            'nb_visits'        => Piwik_Translate('General_ColumnNbVisits'),
            'nb_uniq_visitors' => Piwik_Translate('General_ColumnNbUniqVisitors'),
            'nb_actions'       => Piwik_Translate('General_ColumnNbActions'),
        );

        $reports[] = array(
            'category'  => Piwik_Translate('General_Visitors'),
            'name'      => Piwik_Translate('UserCountry_Country'),
            'module'    => 'UserCountry',
            'action'    => 'getCountry',
            'dimension' => Piwik_Translate('UserCountry_Country'),
            'metrics'   => $metrics,
            'order'     => 5,
        );

        $reports[] = array(
            'category'  => Piwik_Translate('General_Visitors'),
            'name'      => Piwik_Translate('UserCountry_Continent'),
            'module'    => 'UserCountry',
            'action'    => 'getContinent',
            'dimension' => Piwik_Translate('UserCountry_Continent'),
            'metrics'   => $metrics,
            'order'     => 6,
        );

        $reports[] = array(
            'category'  => Piwik_Translate('General_Visitors'),
            'name'      => Piwik_Translate('UserCountry_Region'),
            'module'    => 'UserCountry',
            'action'    => 'getRegion',
            'dimension' => Piwik_Translate('UserCountry_Region'),
            'metrics'   => $metrics,
            'order'     => 7,
        );

        $reports[] = array(
            'category'  => Piwik_Translate('General_Visitors'),
            'name'      => Piwik_Translate('UserCountry_City'),
            'module'    => 'UserCountry',
            'action'    => 'getCity',
            'dimension' => Piwik_Translate('UserCountry_City'),
            'metrics'   => $metrics,
            'order'     => 8,
        );
    }

    public function getReportsWithGoalMetrics(&$dimensions)
    {
        $dimensions = array_merge($dimensions, array(
                                                    array('category' => Piwik_Translate('General_Visit'),
                                                          'name'     => Piwik_Translate('UserCountry_Country'),
                                                          'module'   => 'UserCountry',
                                                          'action'   => 'getCountry',
                                                    ),
                                                    array('category' => Piwik_Translate('General_Visit'),
                                                          'name'     => Piwik_Translate('UserCountry_Continent'),
                                                          'module'   => 'UserCountry',
                                                          'action'   => 'getContinent',
                                                    ),
                                                    array('category' => Piwik_Translate('General_Visit'),
                                                          'name'     => Piwik_Translate('UserCountry_Region'),
                                                          'module'   => 'UserCountry',
                                                          'action'   => 'getRegion'),
                                                    array('category' => Piwik_Translate('General_Visit'),
                                                          'name'     => Piwik_Translate('UserCountry_City'),
                                                          'module'   => 'UserCountry',
                                                          'action'   => 'getCity'),
                                               ));
    }

    public function archivePeriod(ArchiveProcessor\Period $archiveProcessor)
    {
        $archiving = new Archiver($archiveProcessor);
        if ($archiving->shouldArchive()) {
            $archiving->archivePeriod();
        }
    }

    public function archiveDay(ArchiveProcessor\Day $archiveProcessor)
    {
        $archiving = new Archiver($archiveProcessor);
        if ($archiving->shouldArchive()) {
            $archiving->archiveDay();
        }
    }

    /**
     * Returns a list of country codes for a given continent code.
     *
     * @param string $continent The continent code.
     * @return array
     */
    public static function getCountriesForContinent($continent)
    {
        $result = array();
        $continent = strtolower($continent);
        foreach (Common::getCountriesList() as $countryCode => $continentCode) {
            if ($continent == $continentCode) {
                $result[] = $countryCode;
            }
        }
        return array('SQL'  => "'" . implode("', '", $result) . "', ?",
                     'bind' => '-'); // HACK: SegmentExpression requires a $bind, even if there's nothing to bind
    }

    public function getReportDisplayProperties(&$properties)
    {
        $properties['UserCountry.getCountry'] = $this->getDisplayPropertiesForGetCountry();
        $properties['UserCountry.getContinent'] = $this->getDisplayPropertiesForGetContinent();
        $properties['UserCountry.getRegion'] = $this->getDisplayPropertiesForGetRegion();
        $properties['UserCountry.getCity'] = $this->getDisplayPropertiesForGetCity();
    }

    private function getDisplayPropertiesForGetCountry()
    {
        return array(
            'show_exclude_low_population' => false,
            'show_goals'                  => true,
            'filter_limit'                => 5,
            'translations'                => array('label' => Piwik_Translate('UserCountry_Country')),
            'documentation'               => Piwik_Translate('UserCountry_getCountryDocumentation')
        );
    }

    private function getDisplayPropertiesForGetContinent()
    {
        return array(
            'show_exclude_low_population' => false,
            'show_goals'                  => true,
            'show_search'                 => false,
            'show_offset_information'     => false,
            'show_pagination_control'     => false,
            'translations'                => array('label' => Piwik_Translate('UserCountry_Continent')),
            'documentation'               => Piwik_Translate('UserCountry_getContinentDocumentation')
        );
    }

    private function getDisplayPropertiesForGetRegion()
    {
        $result = array(
            'show_exclude_low_population' => false,
            'show_goals'                  => true,
            'filter_limit'                => 5,
            'translations'                => array('label' => Piwik_Translate('UserCountry_Region')),
            'documentation'               => Piwik_Translate('UserCountry_getRegionDocumentation') . '<br/>'
                . $this->getGeoIPReportDocSuffix()
        );
        $this->checkIfNoDataForGeoIpReport($result);
        return $result;
    }

    private function getDisplayPropertiesForGetCity()
    {
        $result = array(
            'show_exclude_low_population' => false,
            'show_goals'                  => true,
            'filter_limit'                => 5,
            'translations'                => array('label' => Piwik_Translate('UserCountry_City')),
            'documentation'               => Piwik_Translate('UserCountry_getCityDocumentation') . '<br/>'
                . $this->getGeoIPReportDocSuffix()
        );
        $this->checkIfNoDataForGeoIpReport($result);
        return $result;
    }

    private function getGeoIPReportDocSuffix()
    {
        return Piwik_Translate('UserCountry_GeoIPDocumentationSuffix',
            array('<a target="_blank" href="http://www.maxmind.com/?rId=piwik">',
                  '</a>',
                  '<a target="_blank" href="http://www.maxmind.com/en/city_accuracy?rId=piwik">',
                  '</a>')
        );
    }

    /**
     * Checks if a datatable for a view is empty and if so, displays a message in the footer
     * telling users to configure GeoIP.
     */
    private function checkIfNoDataForGeoIpReport(&$properties)
    {
        $self = $this;
        $properties['filters'][] = function ($dataTable, $view) use ($self) {
            // if there's only one row whose label is 'Unknown', display a message saying there's no data
            if ($dataTable->getRowsCount() == 1
                && $dataTable->getFirstRow()->getColumn('label') == Piwik_Translate('General_Unknown')
            ) {
                $footerMessage = Piwik_Translate('UserCountry_NoDataForGeoIPReport1');

                // if GeoIP is working, don't display this part of the message
                if (!$self->isGeoIPWorking()) {
                    $params = array('module' => 'UserCountry', 'action' => 'adminIndex');
                    $footerMessage .= ' ' . Piwik_Translate('UserCountry_NoDataForGeoIPReport2',
                        array('<a target="_blank" href="' . Url::getCurrentQueryStringWithParametersModified($params) . '">',
                              '</a>',
                              '<a target="_blank" href="http://dev.maxmind.com/geoip/geolite?rId=piwik">',
                              '</a>'));
                } else {
                    $footerMessage .= ' ' . Piwik_Translate('UserCountry_ToGeolocateOldVisits',
                        array('<a target="_blank" href="http://piwik.org/faq/how-to/#faq_167">', '</a>'));
                }

                $view->show_footer_message = $footerMessage;
            }
        };
    }

    /**
     * Returns true if a GeoIP provider is installed & working, false if otherwise.
     *
     * @return bool
     */
    public function isGeoIPWorking()
    {
        $provider = LocationProvider::getCurrentProvider();
        return $provider instanceof GeoIp
            && $provider->isAvailable() === true
            && $provider->isWorking() === true;
    }
}