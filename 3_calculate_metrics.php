<?php
require 'vendor/autoload.php';

const SKIP_EXISTING = true;
const PROJECTS_DIR = __DIR__ . '/results/';

$command = 'sh -c "../../../../vendor/bin/phpmetrics --report-json=../metrics.REPORT.json -q --ignore-errors -n --excluded-dirs ___NONE___ ."';
//$command = 'jsmeter -o ../metrics-REPORT .'; // npm install node-jsmeter -g

$projects = array_diff(scandir(PROJECTS_DIR), ['.', '..', 'README.txt']);
$progress = new \ProgressBar\Manager(0, count($projects));

foreach ($projects as $project) {
    $projectDir = PROJECTS_DIR . $project;
    if (!is_dir($projectDir)) {
        $progress->advance();
        continue;
    }
    $commits = $files = array_diff(scandir($projectDir), ['.', '..', 'README.txt']);
    foreach ($commits as $commit) {
        chdir($projectDir . "/$commit/after");
        if (!SKIP_EXISTING || !file_exists('../metrics.after.json')) {
            $commandForCommit = str_replace('REPORT', 'after', $command);
            exec($commandForCommit);
        }
        chdir($projectDir . "/$commit/before");
        if (!SKIP_EXISTING || !file_exists('../metrics.before.json')) {
            $commandForCommit = str_replace('REPORT', "before", $command);
            exec($commandForCommit);
        }
    }
    $progress->advance();
}



