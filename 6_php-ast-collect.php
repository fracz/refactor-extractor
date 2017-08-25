<?php
require 'vendor/autoload.php';

const INDENT_OUTPUT = true;
const DIFF_SEPARATOR = '||||||||';

$projectsDir = __DIR__ . '/results/';
$projects = array_diff(scandir($projectsDir), ['.', '..', 'README.txt']);

$progress = new \ProgressBar\Manager(0, count($projects));

$methodsCount = 0;

foreach ($projects as $project) {
    $projectDir = $projectsDir . $project;
    if (!is_dir($projectDir)) {
        $progress->advance();
        continue;
    }
    $changes = array_diff(scandir($projectDir), ['.', '..', 'README.txt']);
    foreach ($changes as $commitId) {
        $changeDir = $projectDir . '/' . $commitId;
        $diffsDir = $changeDir . '/diffs';
        if (is_dir($diffsDir)) {
            $asts = array_diff(scandir($diffsDir), ['.', '..', 'README.txt']);
            foreach ($asts as $astFile) {
                list($codeBefore, $codeAfter, $astBefore, $astAfter) = explode(DIFF_SEPARATOR, file_get_contents($diffsDir . '/' . $astFile));
                $astBefore = tokenize($astBefore);
                $astAfter = tokenize($astAfter);
            }
        }
    }
    $progress->advance();
}

function tokenize(string $ast): string {
    $ast = preg_replace('#\\s#m', '', $ast);
    return $ast;
}
