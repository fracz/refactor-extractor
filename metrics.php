<?php
$project = $argc == 2 ? $argv[1] : 'freeCodeCamp';
$command = 'sh -c "../../../../vendor/bin/phpmetrics --report-json=../metrics.REPORT.json -q --ignore-errors -n ."';
//$command = 'jsmeter -o ../metrics-REPORT .'; // npm install node-jsmeter -g

require 'vendor/autoload.php';

$path = __DIR__ . '/results/' . basename($project);

$commits = $files = array_diff(scandir($path), ['.', '..', 'README.txt']);

$progress = new \ProgressBar\Manager(0, count($commits));

foreach ($commits as $commit) {
    $progress->advance();
    chdir($path . "/$commit/after");
    $commandForCommit = str_replace('REPORT', 'after', $command);
    exec($commandForCommit);
    chdir($path . "/$commit/before");
    $commandForCommit = str_replace('REPORT', "before", $command);
    exec($commandForCommit);
}
