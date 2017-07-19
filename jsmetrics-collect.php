<?php
require 'vendor/autoload.php';

$project = $project = $argc == 2 ? $argv[1] : 'freeCodeCamp';
//$command = 'sh -c "../../../../vendor/bin/phpmetrics --report-json=../metrics.REPORT.json ."';
$command = 'jsmeter -o ../metrics-REPORT .'; // npm install node-jsmeter -g

require 'vendor/autoload.php';

$path = __DIR__ . '/results/' . basename($project);

$changes = array_diff(scandir($path), ['.', '..', 'README.txt']);

$progress = new \ProgressBar\Manager(0, count($changes));

foreach ($changes as $commitId) {
    $changePath = $path . '/' . $commitId;
    $filename = current(array_diff(scandir($changePath . '/metrics-after'), ['.', '..']));
    if ($filename && file_exists($changePath . '/metrics-before/' . $filename)) {
        $metricsAfter = json_decode(file_get_contents($changePath . '/metrics-after/' . $filename), true)[0];
        $metricsBefore = json_decode(file_get_contents($changePath . '/metrics-before/' . $filename), true)[0];
        foreach (['operators', 'operands', 'shortName', 'name'] as $toUnset) {
            unset($metricsBefore[$toUnset]);
            unset($metricsAfter[$toUnset]);
        }

    } else {

    }
    $progress->advance();
}
