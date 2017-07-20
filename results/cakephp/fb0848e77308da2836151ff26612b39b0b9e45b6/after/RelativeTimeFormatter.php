<?php
/**
 * CakePHP(tm) : Rapid Development Framework (http://cakephp.org)
 * Copyright (c) Cake Software Foundation, Inc. (http://cakefoundation.org)
 *
 * Licensed under The MIT License
 * For full copyright and license information, please see the LICENSE.txt
 * Redistributions of files must retain the above copyright notice.
 *
 * @copyright     Copyright (c) Cake Software Foundation, Inc. (http://cakefoundation.org)
 * @link          http://cakephp.org CakePHP(tm) Project
 * @since         3.2.0
 * @license       http://www.opensource.org/licenses/mit-license.php MIT License
 */
namespace Cake\I18n;

use DatetimeInterface;

/**
 * Helper class for formatting relative dates & times.
 *
 * @internal
 */
class RelativeTimeFormatter
{
    /**
     * Format a into a relative timestring.
     *
     * @param \DateTimeInterface $time The time instance to format.
     * @param array $options Array of options.
     * @return string Relative time string.
     * @see Cake\I18n\Time::timeAgoInWords()
     */
    public static function timeAgoInWords(DatetimeInterface $time, array $options = [])
    {
        $options = static::_options($options, FrozenTime::class);
        if ($options['timezone']) {
            $time = $time->timezone($options['timezone']);
        }

        $now = $options['from']->format('U');
        $inSeconds = $time->format('U');
        $backwards = ($inSeconds > $now);

        $futureTime = $now;
        $pastTime = $inSeconds;
        if ($backwards) {
            $futureTime = $inSeconds;
            $pastTime = $now;
        }
        $diff = $futureTime - $pastTime;

        if (!$diff) {
            return __d('cake', 'just now', 'just now');
        }

        if ($diff > abs($now - (new FrozenTime($options['end']))->format('U'))) {
            return sprintf($options['absoluteString'], $time->i18nFormat($options['format']));
        }

        $diffData = static::_diffData($futureTime, $pastTime, $backwards, $options);
        list($fNum, $fWord, $years, $months, $weeks, $days, $hours, $minutes, $seconds) = array_values($diffData);

        $relativeDate = [];
        if ($fNum >= 1 && $years > 0) {
            $relativeDate[] = __dn('cake', '{0} year', '{0} years', $years, $years);
        }
        if ($fNum >= 2 && $months > 0) {
            $relativeDate[] = __dn('cake', '{0} month', '{0} months', $months, $months);
        }
        if ($fNum >= 3 && $weeks > 0) {
            $relativeDate[] = __dn('cake', '{0} week', '{0} weeks', $weeks, $weeks);
        }
        if ($fNum >= 4 && $days > 0) {
            $relativeDate[] = __dn('cake', '{0} day', '{0} days', $days, $days);
        }
        if ($fNum >= 5 && $hours > 0) {
            $relativeDate[] = __dn('cake', '{0} hour', '{0} hours', $hours, $hours);
        }
        if ($fNum >= 6 && $minutes > 0) {
            $relativeDate[] = __dn('cake', '{0} minute', '{0} minutes', $minutes, $minutes);
        }
        if ($fNum >= 7 && $seconds > 0) {
            $relativeDate[] = __dn('cake', '{0} second', '{0} seconds', $seconds, $seconds);
        }
        $relativeDate = implode(', ', $relativeDate);

        // When time has passed
        if (!$backwards) {
            $aboutAgo = [
                'second' => __d('cake', 'about a second ago'),
                'minute' => __d('cake', 'about a minute ago'),
                'hour' => __d('cake', 'about an hour ago'),
                'day' => __d('cake', 'about a day ago'),
                'week' => __d('cake', 'about a week ago'),
                'year' => __d('cake', 'about a year ago')
            ];
            return $relativeDate ? sprintf($options['relativeString'], $relativeDate) : $aboutAgo[$fWord];
        }

        // When time is to come
        if ($relativeDate) {
            return $relativeDate;
        }
        $aboutIn = [
            'second' => __d('cake', 'in about a second'),
            'minute' => __d('cake', 'in about a minute'),
            'hour' => __d('cake', 'in about an hour'),
            'day' => __d('cake', 'in about a day'),
            'week' => __d('cake', 'in about a week'),
            'year' => __d('cake', 'in about a year')
        ];
        return $aboutIn[$fWord];
    }

    /**
     * Calculate the data needed to format a relative difference string.
     *
     * @param \DateTime $futureTime The time from the future.
     * @param \DateTime $pastTime The time from the past.
     * @param bool $backwards Whether or not the difference was backwards.
     * @param array $options An array of options.
     * @return array An array of values.
     */
    protected static function _diffData($futureTime, $pastTime, $backwards, $options)
    {
        $diff = $futureTime - $pastTime;

        // If more than a week, then take into account the length of months
        if ($diff >= 604800) {
            list($future['H'], $future['i'], $future['s'], $future['d'], $future['m'], $future['Y']) = explode('/', date('H/i/s/d/m/Y', $futureTime));

            list($past['H'], $past['i'], $past['s'], $past['d'], $past['m'], $past['Y']) = explode('/', date('H/i/s/d/m/Y', $pastTime));
            $weeks = $days = $hours = $minutes = $seconds = 0;

            $years = $future['Y'] - $past['Y'];
            $months = $future['m'] + ((12 * $years) - $past['m']);

            if ($months >= 12) {
                $years = floor($months / 12);
                $months = $months - ($years * 12);
            }
            if ($future['m'] < $past['m'] && $future['Y'] - $past['Y'] === 1) {
                $years--;
            }

            if ($future['d'] >= $past['d']) {
                $days = $future['d'] - $past['d'];
            } else {
                $daysInPastMonth = date('t', $pastTime);
                $daysInFutureMonth = date('t', mktime(0, 0, 0, $future['m'] - 1, 1, $future['Y']));

                if (!$backwards) {
                    $days = ($daysInPastMonth - $past['d']) + $future['d'];
                } else {
                    $days = ($daysInFutureMonth - $past['d']) + $future['d'];
                }

                if ($future['m'] != $past['m']) {
                    $months--;
                }
            }

            if (!$months && $years >= 1 && $diff < ($years * 31536000)) {
                $months = 11;
                $years--;
            }

            if ($months >= 12) {
                $years = $years + 1;
                $months = $months - 12;
            }

            if ($days >= 7) {
                $weeks = floor($days / 7);
                $days = $days - ($weeks * 7);
            }
        } else {
            $years = $months = $weeks = 0;
            $days = floor($diff / 86400);

            $diff = $diff - ($days * 86400);

            $hours = floor($diff / 3600);
            $diff = $diff - ($hours * 3600);

            $minutes = floor($diff / 60);
            $diff = $diff - ($minutes * 60);
            $seconds = $diff;
        }

        $fWord = $options['accuracy']['second'];
        if ($years > 0) {
            $fWord = $options['accuracy']['year'];
        } elseif (abs($months) > 0) {
            $fWord = $options['accuracy']['month'];
        } elseif (abs($weeks) > 0) {
            $fWord = $options['accuracy']['week'];
        } elseif (abs($days) > 0) {
            $fWord = $options['accuracy']['day'];
        } elseif (abs($hours) > 0) {
            $fWord = $options['accuracy']['hour'];
        } elseif (abs($minutes) > 0) {
            $fWord = $options['accuracy']['minute'];
        }

        $fNum = str_replace(['year', 'month', 'week', 'day', 'hour', 'minute', 'second'], [1, 2, 3, 4, 5, 6, 7], $fWord);
        return [$fNum, $fWord, $years, $months, $weeks, $days, $hours, $minutes, $seconds];
    }

    /**
     * Format a into a relative date string.
     *
     * @param \DatetimeInterface $date The date to format.
     * @param array $options Array of options.
     * @return string Relative date string.
     * @see Cake\I18n\Date::timeAgoInWords()
     */
    public static function dateAgoInWords(DatetimeInterface $date, array $options = [])
    {
        $options = static::_options($options, FrozenDate::class);
        if ($options['timezone']) {
            $date = $date->timezone($options['timezone']);
        }

        $now = $options['from']->format('U');
        $inSeconds = $date->format('U');
        $backwards = ($inSeconds > $now);

        $futureTime = $now;
        $pastTime = $inSeconds;
        if ($backwards) {
            $futureTime = $inSeconds;
            $pastTime = $now;
        }
        $diff = $futureTime - $pastTime;

        if (!$diff) {
            return __d('cake', 'today');
        }

        if ($diff > abs($now - (new FrozenDate($options['end']))->format('U'))) {
            return sprintf($options['absoluteString'], $date->i18nFormat($options['format']));
        }

        $diffData = static::_diffData($futureTime, $pastTime, $backwards, $options);
        list($fNum, $fWord, $years, $months, $weeks, $days, $hours, $minutes, $seconds) = array_values($diffData);

        $relativeDate = [];
        if ($fNum >= 1 && $years > 0) {
            $relativeDate[] = __dn('cake', '{0} year', '{0} years', $years, $years);
        }
        if ($fNum >= 2 && $months > 0) {
            $relativeDate[] = __dn('cake', '{0} month', '{0} months', $months, $months);
        }
        if ($fNum >= 3 && $weeks > 0) {
            $relativeDate[] = __dn('cake', '{0} week', '{0} weeks', $weeks, $weeks);
        }
        if ($fNum >= 4 && $days > 0) {
            $relativeDate[] = __dn('cake', '{0} day', '{0} days', $days, $days);
        }
        $relativeDate = implode(', ', $relativeDate);

        // When time has passed
        if (!$backwards) {
            $aboutAgo = [
                'day' => __d('cake', 'about a day ago'),
                'week' => __d('cake', 'about a week ago'),
                'month' => __d('cake', 'about a month ago'),
                'year' => __d('cake', 'about a year ago')
            ];
            return $relativeDate ? sprintf($options['relativeString'], $relativeDate) : $aboutAgo[$fWord];
        }

        // When time is to come
        if ($relativeDate) {
            return $relativeDate;
        }
        $aboutIn = [
            'day' => __d('cake', 'in about a day'),
            'week' => __d('cake', 'in about a week'),
            'month' => __d('cake', 'in about a month'),
            'year' => __d('cake', 'in about a year')
        ];
        return $aboutIn[$fWord];
    }

    /**
     * Build the options for relative date formatting.
     *
     * @param array $options The options provided by the user.
     * @param string $class The class name to use for defaults.
     * @return array Options with defaults applied.
     */
    protected static function _options($options, $class)
    {
        $options += [
            'from' => $class::now(),
            'timezone' => null,
            'format' => $class::$wordFormat,
            'accuracy' => $class::$wordAccuracy,
            'end' => $class::$wordEnd,
            'relativeString' => __d('cake', '%s ago'),
            'absoluteString' => __d('cake', 'on %s'),
        ];
        if (is_string($options['accuracy'])) {
            $accuracy = $options['accuracy'];
            $options['accuracy'] = [];
            foreach ($class::$wordAccuracy as $key => $level) {
                $options['accuracy'][$key] = $accuracy;
            }
        } else {
            $options['accuracy'] += $class::$wordAccuracy;
        }
        return $options;
    }
}