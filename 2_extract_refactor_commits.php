<?php
require 'vendor/autoload.php';

$project = $argc == 2 ? $argv[1] : 'domnikl/DesignPatternsPHP';

$REPO_DIR = __DIR__ . '/repos/' . $project;
const REFACTOR_KEYWORDS = ['refactor', 'improve', 'reorganize', 'readability'];
const EXTENSION_FILTER = ['java']; //['java', 'js', 'ts', 'php', 'cs'];

chdir($REPO_DIR);

echo 'Looking for commits with refactorings...' . PHP_EOL;
$searchCommand = 'git log --pretty=format:%H ' . implode(' ', array_map(function ($keyword) {
        return '--grep=' . $keyword;
    }, REFACTOR_KEYWORDS));
//echo $searchCommand . PHP_EOL;
$commits = executeInRepo($searchCommand);

echo 'Getting refactor changes from found commits...' . PHP_EOL;

$progress = new \ProgressBar\Manager(0, count($commits));

$outputDir = __DIR__ . '/results/' . str_replace('/', '--', $project);
@mkdir($outputDir);

$files = 0;
$commitsWithFiles = 0;
$filesStats = [];

foreach ($commits as $hash) {
    $progress->advance();
    $output = $outputDir . '/' . $hash;
    $outputBefore = $output . '/before';
    $outputAfter = $output . '/after';
    $changedFiles = executeInRepo('git diff-tree --no-commit-id --name-only -r ' . $hash);
    $processableFiles = array_filter($changedFiles, function ($changedFile) {
        $extension = strtolower(@end(explode('.', $changedFile)));
        return in_array($extension, EXTENSION_FILTER);
    });
    $filesInThisCommit = count($processableFiles);
    $files += $filesInThisCommit;
    if ($filesInThisCommit > 0) {
        foreach ($processableFiles as $changedFile) {
            $filename = basename($changedFile);
            @mkdir($output);
            @mkdir($outputBefore);
            @mkdir($outputAfter);
            $cmd = "git show {$hash}^:$changedFile";
            $beforeRefactoring = implode(PHP_EOL, executeInRepo("git show {$hash}~1:$changedFile"));
            $afterRefactoring = implode(PHP_EOL, executeInRepo("git show $hash:$changedFile"));
            file_put_contents($outputBefore . "/$filename", $beforeRefactoring);
            file_put_contents($outputAfter . "/$filename", $afterRefactoring);
        }
    }
    if ($filesInThisCommit > 0) {
        ++$commitsWithFiles;
        if (!isset($filesStats[$filesInThisCommit])) {
            $filesStats[$filesInThisCommit] = 0;
        }
        ++$filesStats[$filesInThisCommit];
        $fullCommitMessage = implode(PHP_EOL, executeInRepo('git show -q ' . $hash));
        file_put_contents($output . '/README.txt', $fullCommitMessage);
    }
}

$fileStatsReadme = PHP_EOL . PHP_EOL . 'Commit stats:' . PHP_EOL;
ksort($filesStats);
foreach ($filesStats as $fileCount => $commitCount) {
    $fileStatsReadme .= $commitCount . ' commits with ' . $fileCount . ' files' . PHP_EOL;
}

$resultText = 'Commits: ' . $commitsWithFiles . PHP_EOL . 'Files: ' . $files . $fileStatsReadme;

file_put_contents($outputDir . '/README.txt', $resultText);

echo $resultText;

function executeInRepo($cmd)
{
    exec($cmd . ' 2>NUL', $output);
    return $output;
}
