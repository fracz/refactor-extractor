<?php
require 'vendor/autoload.php';

$projectsDir = __DIR__ . '/results/';
$projects = array_diff(scandir($projectsDir), ['.', '..', 'README.txt']);

$projectsCount = 0;
$commitsCount = 0;
$commitsByFilesCount = [];
$filesCount = 0;
$methodsCount = 0;

$progress = new \ProgressBar\Manager(0, count($projects));

foreach ($projects as $project) {
    $projectDir = $projectsDir . $project;
    if (!is_dir($projectDir)) {
        $progress->advance();
        continue;
    }
    $projectsCount++;
    $changes = array_diff(scandir($projectDir), ['.', '..', 'README.txt']);
    $commitsCount += count($changes);
    foreach ($changes as $commitId) {
        $changeDir = $projectDir . '/' . $commitId;
        $currentFilesCount = count(array_diff(scandir($changeDir . '/after'), ['.', '..', 'README.txt']));
        $filesCount += $currentFilesCount;
        if (!isset($commitsByFilesCount[$currentFilesCount])) {
            $commitsByFilesCount[$currentFilesCount] = 0;
        }
        $commitsByFilesCount[$currentFilesCount]++;
        $currentMethodsCount = is_dir($changeDir . '/diffs') ? count(array_diff(scandir($changeDir . '/diffs'), ['.', '..', 'README.txt'])) : 0;
        $methodsCount += $currentMethodsCount;
    }
    $progress->advance();
}

$commitsByFilesCountText = 'Commits by file count:' . PHP_EOL;
ksort($commitsByFilesCount);
foreach ($commitsByFilesCount as $count => $commits) {
    $commitsByFilesCountText .= $commits . ' commits with ' . $count . ' files' . PHP_EOL;
}

echo <<<STATS

PROJECTS: $projectsCount
COMMITS: $commitsCount
FILES: $filesCount
METHODS: $methodsCount

STATS;
