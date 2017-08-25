<?php
// This file is part of Moodle - http://moodle.org/
//
// Moodle is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// Moodle is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Moodle.  If not, see <http://www.gnu.org/licenses/>.

/**
 * @package    core
 * @subpackage lib
 * @copyright  (C) 2001-3001 Eloy Lafuente (stronk7) {@link http://contiento.com}
 * @license    http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */

defined('MOODLE_INTERNAL') || die();

/**
 * Original singleton helper function, please use static methods instead,
 * ex: textlib::convert()
 *
 * @deprecated
 * @return textlib instance
 */
function textlib_get_instance() {
    return new textlib();
}


/**
 * This class is used to manipulate strings under Moodle 1.6 an later. As
 * utf-8 text become mandatory a pool of safe functions under this encoding
 * become necessary. The name of the methods is exactly the
 * same than their PHP originals.
 *
 * A big part of this class acts as a wrapper over the Typo3 charset library,
 * really a cool group of utilities to handle texts and encoding conversion.
 *
 * Take a look to its own copyright and license details.
 *
 * IMPORTANT Note: Typo3 libraries always expect lowercase charsets to use 100%
 * its capabilities so, don't forget to make the conversion
 * from every wrapper function!
 *
 * @package    core
 * @subpackage lib
 * @copyright 1999 onwards Martin Dougiamas  {@link http://moodle.com}
 * @license   http://www.gnu.org/copyleft/gpl.html GNU GPL v3 or later
 */
class textlib {

    /**
     * Return t3lib helper class
     * @return t3lib_cs
     */
    protected static function typo3() {
        static $typo3cs = null;

        if (isset($typo3cs)) {
            return $typo3cs;
        }

        global $CFG;

        // Required files
        require_once($CFG->libdir.'/typo3/class.t3lib_cs.php');
        require_once($CFG->libdir.'/typo3/class.t3lib_div.php');

        // If ICONV is available, lets Typo3 library use it for convert
        if (extension_loaded('iconv')) {
            $GLOBALS['TYPO3_CONF_VARS']['SYS']['t3lib_cs_convMethod'] = 'iconv';
        // Else if mbstring is available, lets Typo3 library use it
        } else if (extension_loaded('mbstring')) {
            $GLOBALS['TYPO3_CONF_VARS']['SYS']['t3lib_cs_convMethod'] = 'mbstring';
        // Else if recode is available, lets Typo3 library use it
        } else if (extension_loaded('recode')) {
            $GLOBALS['TYPO3_CONF_VARS']['SYS']['t3lib_cs_convMethod'] = 'recode';
        } else {
            $GLOBALS['TYPO3_CONF_VARS']['SYS']['t3lib_cs_convMethod'] = '';
        }

        // If mbstring is available, lets Typo3 library use it for functions
        if (extension_loaded('mbstring')) {
            $GLOBALS['TYPO3_CONF_VARS']['SYS']['t3lib_cs_utils'] = 'mbstring';
        } else {
            $GLOBALS['TYPO3_CONF_VARS']['SYS']['t3lib_cs_utils'] = '';
        }

        // Tell Typo3 we are curl enabled always (mandatory since 2.0)
        $GLOBALS['TYPO3_CONF_VARS']['SYS']['curlUse'] = '1';

        // And this directory must exist to allow Typo to cache conversion
        // tables when using internal functions
        make_upload_directory('temp/typo3temp/cs');

        // Make sure typo is using our dir permissions
        $GLOBALS['TYPO3_CONF_VARS']['BE']['folderCreateMask'] = decoct($CFG->directorypermissions);

        // Default mask for Typo
        $GLOBALS['TYPO3_CONF_VARS']['BE']['fileCreateMask'] = $CFG->directorypermissions;

        // This full path constants must be defined too, transforming backslashes
        // to forward slashed because Typo3 requires it.
        define ('PATH_t3lib', str_replace('\\','/',$CFG->libdir.'/typo3/'));
        define ('PATH_typo3', str_replace('\\','/',$CFG->libdir.'/typo3/'));
        define ('PATH_site', str_replace('\\','/',$CFG->dataroot.'/temp/'));
        define ('TYPO3_OS', stristr(PHP_OS,'win')&&!stristr(PHP_OS,'darwin')?'WIN':'');

        $typo3cs = new t3lib_cs();

        return $typo3cs;
    }

    /**
     * Standardise charset name
     *
     * Please note it does not mean the returned charset is actually supported.
     *
     * @static
     * @param string $charset raw charset name
     * @return string normalised lowercase charset name
     */
    public static function parse_charset($charset) {
        // shortcuts so that we do not have to load typo on every page
        $charset = strtolower($charset);
        switch ($charset) {
            case 'utf8':
            case 'utf-8':
                return 'utf-8';

            case 'cp1250':
            case 'win-1250':
            case 'windowns-1250':
                return 'windows-1250';

            case 'cp1252':
            case 'win-1252':
            case 'windowns-1252':
                return 'windows-1252';

            case 'l1':
            case 'latin1':
            case 'iso-8859-1':
            case 'iso-8859-1':
                return 'iso-8859-1';

            case 'l2':
            case 'latin2':
            case 'iso-8859-2':
            case 'iso-8859-2':
                return 'iso-8859-2';

            //TODO: add more common charsets here - all win and unix encodings from our lang packs

        }

        // fallback to typo3
        return self::typo3()->parse_charset($charset);
    }

    /**
     * Converts the text between different encodings. It uses iconv extension with //TRANSLIT parameter.
     * Returns false if fails.
     *
     * @param string $text
     * @param string $fromCS source encoding
     * @param string $toCS result encoding
     * @return string|bool
     */
    public static function convert($text, $fromCS, $toCS='utf-8') {
        $fromCS = self::parse_charset($fromCS);
        $toCS   = self::parse_charset($toCS);

        return iconv($fromCS, $toCS.'//TRANSLIT', $text);
    }

    /**
     * Multibyte safe substr() function, uses mbstring if available.
     *
     * @param string $text
     * @param int $start negative value means from end
     * @param int $len
     * @param string $charset encoding of the text
     * @return string
     */
    public static function substr($text, $start, $len=null, $charset='utf-8') {
        $charset = self::parse_charset($charset);

        return iconv_substr($text, $start, $len, $charset);
    }

    /**
     * Multibyte safe strlen() function, uses mbstring if available.
     *
     * @param string $text
     * @param string $charset encoding of the text
     * @return int number of characters
     */
    public static function strlen($text, $charset='utf-8') {
        $charset = self::parse_charset($charset);

        return iconv_strlen($text, $charset);
    }

    /**
     * Multibyte safe strtolower() function, uses mbstring if available.
     *
     * @param string $text
     * @param string $charset encoding of the text (may not work for all encodings)
     * @return string lower case text
     */
    public static function strtolower($text, $charset='utf-8') {
        $charset = self::parse_charset($charset);

        if (function_exists('mb_strtolower')) {
            return mb_strtolower($text, $charset);
        }

        // Avoid some notices from Typo3 code
        $oldlevel = error_reporting(E_PARSE);
        // Call Typo3 conv_case() function. It will do all the work
        $result = self::typo3()->conv_case($charset, $text, 'toLower');
        // Restore original debug level
        error_reporting($oldlevel);
        return $result;
    }

    /**
     * Multibyte safe strtoupper() function, uses mbstring if available.
     *
     * @param string $text
     * @param string $charset encoding of the text (may not work for all encodings)
     * @return string upper case text
     */
    public static function strtoupper($text, $charset='utf-8') {
        $charset = self::parse_charset($charset);

        if (function_exists('mb_strtoupper')) {
            return mb_strtoupper($text, $charset);
        }

        // Avoid some notices from Typo3 code
        $oldlevel = error_reporting(E_PARSE);
        // Call Typo3 conv_case() function. It will do all the work
        $result = self::typo3()->conv_case($charset, $text, 'toUpper');
        // Restore original debug level
        error_reporting($oldlevel);
        return $result;
    }

    /**
     * UTF-8 ONLY safe strpos().
     *
     * @param string $haystack
     * @param string $needle
     * @param int $offset
     * @return string
     */
    public static function strpos($haystack, $needle, $offset=0) {
        return iconv_strpos($haystack, $needle, $offset, 'utf-8');
    }

    /**
     * UTF-8 ONLY safe strrpos().
     *
     * @param string $haystack
     * @param string $needle
     * @return string
     */
    public static function strrpos($haystack, $needle) {
        return iconv_strrpos($haystack, $needle, 'utf-8');
    }

    /**
     * Try to convert upper unicode characters to plain ascii,
     * the returned string may contain unconverted unicode characters.
     *
     * @param string $text
     * @param string $charset encoding of the text
     * @return string
     */
    public static function specialtoascii($text, $charset='utf-8') {
        $charset = self::parse_charset($charset);
        // Avoid some notices from Typo3 code
        $oldlevel = error_reporting(E_PARSE);
        $result = self::typo3()->specCharsToASCII($charset, $text);
        // Restore original debug level
        error_reporting($oldlevel);
        return $result;
    }

    /**
     * Generate a correct base64 encoded header to be used in MIME mail messages.
     * This function seems to be 100% compliant with RFC1342. Credits go to:
     * paravoid (http://www.php.net/manual/en/function.mb-encode-mimeheader.php#60283).
     *
     * @param string $text
     * @param string $charset encoding of the text
     * @return string
     */
    public static function encode_mimeheader($text, $charset='utf-8') {
        if (empty($text)) {
            return (string)$text;
        }
        // Normalize charset
        $charset = self::parse_charset($charset);
        // If the text is pure ASCII, we don't need to encode it
        if (self::convert($text, $charset, 'ascii') == $text) {
            return $text;
        }
        // Although RFC says that line feed should be \r\n, it seems that
        // some mailers double convert \r, so we are going to use \n alone
        $linefeed="\n";
        // Define start and end of every chunk
        $start = "=?$charset?B?";
        $end = "?=";
        // Accumulate results
        $encoded = '';
        // Max line length is 75 (including start and end)
        $length = 75 - strlen($start) - strlen($end);
        // Multi-byte ratio
        $multilength = self::strlen($text, $charset);
        // Detect if strlen and friends supported
        if ($multilength === false) {
            if ($charset == 'GB18030' or $charset == 'gb18030') {
                while (strlen($text)) {
                    // try to encode first 22 chars - we expect most chars are two bytes long
                    if (preg_match('/^(([\x00-\x7f])|([\x81-\xfe][\x40-\x7e])|([\x81-\xfe][\x80-\xfe])|([\x81-\xfe][\x30-\x39]..)){1,22}/m', $text, $matches)) {
                        $chunk = $matches[0];
                        $encchunk = base64_encode($chunk);
                        if (strlen($encchunk) > $length) {
                            // find first 11 chars - each char in 4 bytes - worst case scenario
                            preg_match('/^(([\x00-\x7f])|([\x81-\xfe][\x40-\x7e])|([\x81-\xfe][\x80-\xfe])|([\x81-\xfe][\x30-\x39]..)){1,11}/m', $text, $matches);
                            $chunk = $matches[0];
                            $encchunk = base64_encode($chunk);
                        }
                        $text = substr($text, strlen($chunk));
                        $encoded .= ' '.$start.$encchunk.$end.$linefeed;
                    } else {
                        break;
                    }
                }
                $encoded = trim($encoded);
                return $encoded;
            } else {
                return false;
            }
        }
        $ratio = $multilength / strlen($text);
        // Base64 ratio
        $magic = $avglength = floor(3 * $length * $ratio / 4);
        // basic infinite loop protection
        $maxiterations = strlen($text)*2;
        $iteration = 0;
        // Iterate over the string in magic chunks
        for ($i=0; $i <= $multilength; $i+=$magic) {
            if ($iteration++ > $maxiterations) {
                return false; // probably infinite loop
            }
            $magic = $avglength;
            $offset = 0;
            // Ensure the chunk fits in length, reducing magic if necessary
            do {
                $magic -= $offset;
                $chunk = self::substr($text, $i, $magic, $charset);
                $chunk = base64_encode($chunk);
                $offset++;
            } while (strlen($chunk) > $length);
            // This chunk doesn't break any multi-byte char. Use it.
            if ($chunk)
                $encoded .= ' '.$start.$chunk.$end.$linefeed;
        }
        // Strip the first space and the last linefeed
        $encoded = substr($encoded, 1, -strlen($linefeed));

        return $encoded;
    }

    /**
     * Converts all the numeric entities &#nnnn; or &#xnnn; to UTF-8
     * Original from laurynas dot butkus at gmail at:
     * http://php.net/manual/en/function.html-entity-decode.php#75153
     * with some custom mods to provide more functionality
     *
     * @param    string    $str      input string
     * @param    boolean   $htmlent  convert also html entities (defaults to true)
     * @return   string
     *
     * NOTE: we could have used typo3 entities_to_utf8() here
     *       but the direct alternative used runs 400% quicker
     *       and uses 0.5Mb less memory, so, let's use it
     *       (tested against 10^6 conversions)
     */
    public static function entities_to_utf8($str, $htmlent=true) {
        static $trans_tbl; // Going to use static transliteration table

        // Replace numeric entities
        $result = preg_replace('~&#x([0-9a-f]+);~ei', 'textlib::code2utf8(hexdec("\\1"))', $str);
        $result = preg_replace('~&#([0-9]+);~e', 'textlib::code2utf8(\\1)', $result);

        // Replace literal entities (if desired)
        if ($htmlent) {
            // Generate/create $trans_tbl
            if (!isset($trans_tbl)) {
                $trans_tbl = array();
                foreach (get_html_translation_table(HTML_ENTITIES) as $val=>$key) {
                    $trans_tbl[$key] = utf8_encode($val);
                }
            }
            $result = strtr($result, $trans_tbl);
        }
        // Return utf8-ised string
        return $result;
    }

    /**
     * Converts all Unicode chars > 127 to numeric entities &#nnnn; or &#xnnn;.
     *
     * @param    string   $str      input string
     * @param    boolean  $dec      output decadic only number entities
     * @param    boolean  $nonnum   remove all non-numeric entities
     * @return   string converted string
     */
    public static function utf8_to_entities($str, $dec=false, $nonnum=false) {
        // Avoid some notices from Typo3 code
        $oldlevel = error_reporting(E_PARSE);
        if ($nonnum) {
            $str = self::typo3()->entities_to_utf8($str, true);
        }
        $result = self::typo3()->utf8_to_entities($str);
        if ($dec) {
            $result = preg_replace('/&#x([0-9a-f]+);/ie', "'&#'.hexdec('$1').';'", $result);
        }
        // Restore original debug level
        error_reporting($oldlevel);
        return $result;
    }

    /**
     * Removes the BOM from unicode string - see http://unicode.org/faq/utf_bom.html
     *
     * @param string $str
     * @return string
     */
    public static function trim_utf8_bom($str) {
        $bom = "\xef\xbb\xbf";
        if (strpos($str, $bom) === 0) {
            return substr($str, strlen($bom));
        }
        return $str;
    }

    /**
     * Returns encoding options for select boxes, utf-8 and platform encoding first
     * @return array encodings
     */
    public static function get_encodings() {
        $encodings = array();
        $encodings['UTF-8'] = 'UTF-8';
        $winenc = strtoupper(get_string('localewincharset', 'langconfig'));
        if ($winenc != '') {
            $encodings[$winenc] = $winenc;
        }
        $nixenc = strtoupper(get_string('oldcharset', 'langconfig'));
        $encodings[$nixenc] = $nixenc;

        foreach (self::typo3()->synonyms as $enc) {
            $enc = strtoupper($enc);
            $encodings[$enc] = $enc;
        }
        return $encodings;
    }

    /**
     * Returns the utf8 string corresponding to the unicode value
     * (from php.net, courtesy - romans@void.lv)
     *
     * @param  int    $num one unicode value
     * @return string the UTF-8 char corresponding to the unicode value
     */
    public static function code2utf8($num) {
        if ($num < 128) {
            return chr($num);
        }
        if ($num < 2048) {
            return chr(($num >> 6) + 192) . chr(($num & 63) + 128);
        }
        if ($num < 65536) {
            return chr(($num >> 12) + 224) . chr((($num >> 6) & 63) + 128) . chr(($num & 63) + 128);
        }
        if ($num < 2097152) {
            return chr(($num >> 18) + 240) . chr((($num >> 12) & 63) + 128) . chr((($num >> 6) & 63) + 128) . chr(($num & 63) + 128);
        }
        return '';
    }

    /**
     * Makes first letter of each word capital - words must be separated by spaces.
     * Use with care, this function does not work properly in many locales!!!
     *
     * @param string $text
     * @return string
     */
    public static function strtotitle($text) {
        if (empty($text)) {
            return $text;
        }

        if (function_exists('mb_convert_case')) {
            return mb_convert_case($text, MB_CASE_TITLE, 'UTF-8');
        }

        $text = self::strtolower($text);
        $words = explode(' ', $text);
        foreach ($words as $i=>$word) {
            $length = self::strlen($word);
            if (!$length) {
                continue;

            } else if ($length == 1) {
                $words[$i] = self::strtoupper($word);

            } else {
                $letter = self::substr($word, 0, 1);
                $letter = self::strtoupper($letter);
                $rest   = self::substr($word, 1);
                $words[$i] = $letter.$rest;
            }
        }
        return implode(' ', $words);
    }

    /**
     * Locale aware sorting, the key associations are kept, values are sorted alphabetically.
     *
     * Note: this function is using current moodle locale.
     *
     * @param array $arr array to be sorted
     * @return void, modifies parameter
     */
    public static function asort(array &$arr) {
        if (function_exists('collator_asort')) {
            if ($coll = collator_create(get_string('locale', 'langconfig'))) {
                collator_asort($coll, $arr);
                return;
            }
        }
        asort($arr, SORT_LOCALE_STRING);
    }
}