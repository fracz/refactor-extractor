<?php
require 'vendor/autoload.php';

const POSITIVE_VECTORS_FILE = __DIR__ . '/results/positive.csv';
const NEGATIVE_VECTORS_FILE = __DIR__ . '/results/negative.csv';
const DELTA_VECTORS_FILE = __DIR__ . '/results/delta.csv';

@unlink(POSITIVE_VECTORS_FILE);
@unlink(NEGATIVE_VECTORS_FILE);
@unlink(DELTA_VECTORS_FILE);

$projectsDir = __DIR__ . '/results/';
$projects = array_diff(scandir($projectsDir), ['.', '..', 'README.txt']);

$progress = new \ProgressBar\Manager(0, count($projects));

$changesCount = 0;

foreach ($projects as $project) {
    $projectDir = $projectsDir . $project;
    $changes = array_diff(scandir($projectDir), ['.', '..', 'README.txt']);
    foreach ($changes as $commitId) {
        $changeDir = $projectDir . '/' . $commitId;
        $metricsBeforePath = $changeDir . '/metrics.before.json';
        $metricsAfterPath = $changeDir . '/metrics.after.json';
        if (file_exists($metricsBeforePath) && file_exists($metricsAfterPath)) {
            ++$changesCount;
            $metricsAfter = createAssocArrayFromMetrics(json_decode(file_get_contents($metricsAfterPath), true));
            $metricsBefore = createAssocArrayFromMetrics(json_decode(file_get_contents($metricsBeforePath), true));
            foreach ($metricsBefore as $filename => $fileMetricsBefore) {
                if (isset($metricsAfter[$filename])) {
                    $fileMetricsAfter = $metricsAfter[$filename];
                    ksort($fileMetricsBefore);
                    ksort($fileMetricsAfter);
                    if (!file_exists(POSITIVE_VECTORS_FILE)) {
                        file_put_contents(POSITIVE_VECTORS_FILE, implode(',', array_keys($fileMetricsAfter)) . PHP_EOL);
                        file_put_contents(NEGATIVE_VECTORS_FILE, implode(',', array_keys($fileMetricsAfter)) . PHP_EOL);
                        file_put_contents(DELTA_VECTORS_FILE, implode(',', array_keys($fileMetricsAfter)) . PHP_EOL);
                    }
                    file_put_contents(POSITIVE_VECTORS_FILE, implode(',', $fileMetricsAfter) . PHP_EOL, FILE_APPEND);
                    file_put_contents(NEGATIVE_VECTORS_FILE, implode(',', $fileMetricsBefore). PHP_EOL, FILE_APPEND);
                    $delta = [];
                    foreach ($fileMetricsBefore as $metric => $value) {
                        $delta[$metric] = floatval($fileMetricsAfter[$metric]) - floatval($value);
                    }
                    file_put_contents(DELTA_VECTORS_FILE, implode(',', $delta). PHP_EOL, FILE_APPEND);
                }
            }
        }
    }
    $progress->advance();
}

echo "Refactoring commits: " . $changesCount;

function createAssocArrayFromMetrics(array $metrics) {
    $assocMetrics = [];
    foreach ($metrics as $metric) {
        $assocMetrics[basename($metric['name'])] = array_diff_key($metric, ['filename' => '', 'name' => '', 'myerInterval' => '']);
    }
    return $assocMetrics;
}

//$changes = array_diff(scandir($path), ['.', '..', 'README.txt']);
//
//
//
//foreach ($changes as $commitId) {
//    $changePath = $path . '/' . $commitId;
//    $filename = current(array_diff(scandir($changePath . '/metrics-after'), ['.', '..']));
//    if ($filename && file_exists($changePath . '/metrics-before/' . $filename)) {
//        $metricsAfter = json_decode(file_get_contents($changePath . '/metrics-after/' . $filename), true)[0];
//        $metricsBefore = json_decode(file_get_contents($changePath . '/metrics-before/' . $filename), true)[0];
//        foreach (['operators', 'operands', 'shortName', 'name'] as $toUnset) {
//            unset($metricsBefore[$toUnset]);
//            unset($metricsAfter[$toUnset]);
//        }
//
//    } else {
//
//    }
//    $progress->advance();
//}
