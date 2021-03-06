<?php
/*
 * This file is part of the DebugBar package.
 *
 * (c) 2013 Maxime Bouroumeau-Fuseau
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

namespace DebugBar\DataCollector;

/**
 * Collects info about the request duration as well as providing
 * a way to log duration of any operations
 */
class TimeDataCollector extends DataCollector implements Renderable
{
    protected $requestStartTime;

    protected $requestEndTime;

    protected $measures = array();

    /**
     * @param float $requestStartTime
     */
    public function __construct($requestStartTime = null)
    {
        if ($requestStartTime === null) {
            if (isset($_SERVER['REQUEST_TIME_FLOAT'])) {
                $requestStartTime = $_SERVER['REQUEST_TIME_FLOAT'];
            } else {
                $requestStartTime = microtime(true);
            }
        }
        $this->requestStartTime = $requestStartTime;
    }

    /**
     * Starts a measure
     *
     * @param string $name Internal name, used to stop the measure
     * @param string $label Public name
     */
    public function startMeasure($name, $label = null)
    {
        $start = microtime(true);
        $this->measures[$name] = array(
            'label' => $label ?: $name,
            'start' => $start,
            'relative_start' => $start - $this->requestStartTime
        );
    }

    /**
     * Stops a measure
     *
     * @param string $name
     */
    public function stopMeasure($name)
    {
        $end = microtime(true);
        $this->measures[$name]['end'] = $end;
        $this->measures[$name]['relative_end'] = $end - $this->requestEndTime;
        $this->measures[$name]['duration'] = $end - $this->measures[$name]['start'];
        $this->measures[$name]['duration_str'] = $this->toReadableString($this->measures[$name]['duration']);
    }

    /**
     * Utility function to measure the execution of a Closure
     *
     * @param Closure $closure
     */
    public function measure(\Closure $closure)
    {
        $name = spl_object_hash($closure);
        $this->startMeasure($name, $closure);
        $closure();
        $this->stopMeasure($name);
    }

    /**
     * Returns an array of all measures
     *
     * @return array
     */
    public function getMeasures()
    {
        return $this->measures;
    }

    /**
     * Returns the request start time
     *
     * @return float
     */
    public function getRequestStartTime()
    {
        return $this->requestStartTime;
    }

    /**
     * Returns the request end time
     *
     * @return float
     */
    public function getRequestEndTime()
    {
        return $this->requestEndTime;
    }

    /**
     * Returns the duration of a request
     *
     * @return float
     */
    public function getRequestDuration()
    {
        if ($this->requestEndTime !== null) {
            return $this->requestEndTime - $this->requestStartTime;
        }
        return microtime(true) - $this->requestStartTime;
    }

    /**
     * Transforms a duration in seconds in a readable string
     *
     * @param float $value
     * @return string
     */
    public function toReadableString($value)
    {
        return round($value * 1000) . 'ms';
    }

    /**
     * {@inheritDoc}
     */
    public function collect()
    {
        $this->requestEndTime = microtime(true);
        foreach ($this->measures as $name => $data) {
            if (!isset($data['end'])) {
                $this->stopMeasure($name);
            }
        }

        return array(
            'start' => $this->requestStartTime,
            'end' => $this->requestEndTime,
            'duration' => $this->getRequestDuration(),
            'duration_str' => $this->toReadableString($this->getRequestDuration()),
            'measures' => array_values($this->measures)
        );
    }

    /**
     * {@inheritDoc}
     */
    public function getName()
    {
        return 'time';
    }

    /**
     * {@inheritDoc}
     */
    public function getWidgets()
    {
        return array(
            "time" => array(
                "icon" => "time",
                "tooltip" => "Request Duration",
                "map" => "time.duration_str",
                "default" => "'0ms'"
            ),
            "timeline" => array(
                "widget" => "PhpDebugBar.Widgets.TimelineWidget",
                "map" => "time",
                "default" => "{}"
            )
        );
    }
}