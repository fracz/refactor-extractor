<?php
/**
 * Piwik - Open source web analytics
 *
 * @link http://piwik.org
 * @license http://www.gnu.org/licenses/gpl-3.0.html GPL v3 or later
 *
 * @category Piwik_Plugins
 * @package Piwik_AnonymizeIP
 */
use Piwik\Core\Config;
use Piwik\Core\Piwik_Common;

/**
 * Anonymize visitor IP addresses to comply with the privacy laws/guidelines in countries, such as Germany.
 *
 * @package Piwik_AnonymizeIP
 */
class Piwik_AnonymizeIP extends Piwik_Plugin
{
    /**
     * @see Piwik_Plugin::getInformation
     */
    public function getInformation()
    {
        return array(
            'description'     => Piwik_Translate('AnonymizeIP_PluginDescription'),
            'author'          => 'Piwik',
            'author_homepage' => 'http://piwik.org/',
            'version'         => Piwik_Version::VERSION,
        );
    }

    /**
     * @see Piwik_Plugin::getListHooksRegistered
     */
    public function getListHooksRegistered()
    {
        return array(
            'Tracker.Visit.setVisitorIp' => 'setVisitorIpAddress',
        );
    }

    /**
     * Internal function to mask portions of the visitor IP address
     *
     * @param string $ip IP address in network address format
     * @param int $maskLength Number of octets to reset
     * @return string
     */
    static public function applyIPMask($ip, $maskLength)
    {
        $i = Piwik_Common::strlen($ip);
        if ($maskLength > $i) {
            $maskLength = $i;
        }

        while ($maskLength-- > 0) {
            $ip[--$i] = chr(0);
        }

        return $ip;
    }

    /**
     * Hook on Tracker.Visit.setVisitorIp to anonymize visitor IP addresses
     */
    public function setVisitorIpAddress(&$ip)
    {
        $ip = self::applyIPMask($ip, Config::getInstance()->Tracker['ip_address_mask_length']);
    }
}