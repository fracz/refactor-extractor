<?php
require 'vendor/autoload.php';

const REPO_DIR = 'C:/Users/Wojciech/Desktop/symfony';
const REFACTOR_KEYWORDS = ['refactor', 'improve', 'reorganize', 'readability'];
const EXTENSION_FILTER = ['java', 'js', 'ts', 'php', 'cs'];

chdir(REPO_DIR);

echo 'Looking for commits with refactorings...' . PHP_EOL;
$searchCommand = 'git log --pretty=format:%H ' . implode(' ', array_map(function ($keyword) {
        return '--grep=' . $keyword;
    }, REFACTOR_KEYWORDS));
//echo $searchCommand . PHP_EOL;
$commits = executeInRepo($searchCommand);

echo 'Getting refactor changes from found commits...' . PHP_EOL;

$progress = new \ProgressBar\Manager(0, count($commits));

$outputDir = __DIR__ . '/results/' . basename(REPO_DIR);
@mkdir($outputDir);

$files = 0;
$commitsWithFiles = 0;

foreach ($commits as $hash) {
    $progress->advance();
    $output = $outputDir . '/' . $hash;
    $outputBefore = $output . '/before';
    $outputAfter = $output . '/after';
    $changedFiles = executeInRepo('git diff-tree --no-commit-id --name-only -r ' . $hash);
    $filesInThisCommit = 0;
    foreach ($changedFiles as $changedFile) {
        $filename = basename($changedFile);
        $extension = strtolower(@end(explode('.', $filename)));
        if (in_array($extension, EXTENSION_FILTER)) {
            @mkdir($output);
            @mkdir($outputBefore);
            @mkdir($outputAfter);
            $cmd = "git show {$hash}^:$changedFile";
            $beforeRefactoring = implode(PHP_EOL, executeInRepo("git show {$hash}~1:$changedFile"));
            $afterRefactoring = implode(PHP_EOL, executeInRepo("git show $hash:$changedFile"));
            file_put_contents($outputBefore . "/$filename", $beforeRefactoring);
            file_put_contents($outputAfter . "/$filename", $afterRefactoring);
            ++$files;
            ++$filesInThisCommit;
        }
    }
    if ($filesInThisCommit > 0) {
        ++$commitsWithFiles;
        $fullCommitMessage = implode(PHP_EOL, executeInRepo('git show -q ' . $hash));
        file_put_contents($output . '/README.txt', $fullCommitMessage);
    }
}

file_put_contents($outputDir . '/README.txt', 'Commits: ' . $commitsWithFiles . PHP_EOL . 'Files: ' . $files);

echo "Found $files files with refactor changes in $commitsWithFiles commits.";

function executeInRepo($cmd)
{
    exec($cmd . ' 2>NUL', $output);
    return $output;
}
