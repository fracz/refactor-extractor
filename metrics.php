<?php
$project = 'symfony';
$command = 'sh -c "./vendor/bin/phpmetrics --report-json=REPORT.json PATH"';

require 'vendor/autoload.php';

$path = './results/' . $project;

$commits = $files = array_diff(scandir($path), ['.', '..', 'README.txt']);

$progress = new \ProgressBar\Manager(0, count($commits));

foreach ($commits as $commit) {
    $progress->advance();
    $commandForCommit = str_replace(['REPORT', 'PATH'], [$path . "/$commit/metrics.after", $path . "/$commit/after"], $command);
    exec($commandForCommit);
    $commandForCommit = str_replace(['REPORT', 'PATH'], [$path . "/$commit/metrics.before", $path . "/$commit/before"], $command);
    exec($commandForCommit);
}
