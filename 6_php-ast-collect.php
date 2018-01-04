<?php
require 'vendor/autoload.php';

const RESULT_DIR = __DIR__ . '/input/ast-java-strict-no-parenthesis';
const ADDED_METHODS = RESULT_DIR . '/added.txt';
const DELETED_METHODS = RESULT_DIR . '/deleted.txt';
const CHANGED_METHODS_BEFORE = RESULT_DIR . '/changed-before.txt';
const CHANGED_METHODS_AFTER = RESULT_DIR . '/changed-after.txt';
const CHANGED_DIR = RESULT_DIR . '/changed/';
const ADDED_DIR = RESULT_DIR . '/added/';
const DELETED_DIR = RESULT_DIR . '/deleted/';
const DIFF_SEPARATOR = '||||||||';

@mkdir(CHANGED_DIR, 0777, true);
@mkdir(ADDED_DIR, 0777, true);
@mkdir(DELETED_DIR, 0777, true);

file_put_contents(ADDED_METHODS, '');
file_put_contents(DELETED_METHODS, '');
file_put_contents(CHANGED_METHODS_BEFORE, '');
file_put_contents(CHANGED_METHODS_AFTER, '');

$projectsDir = __DIR__ . '/results-java/';
$projects = array_diff(scandir($projectsDir), ['.', '..', 'README.txt']);

$progress = new \ProgressBar\Manager(0, count($projects));

$methodsCount = 0;

$tokens = array_map('trim', file(__DIR__ . '/java-parser/tokens-java.txt'));

$refactorRegex = '#(\brefactor|readability)#i';

$tokenDuplicates = array_filter(array_count_values($tokens), function ($count) {
    return $count > 1;
});
if ($tokenDuplicates) {
    throw new InvalidArgumentException("THERE ARE TOKEN DUPLICATES: " . implode(', ', array_keys($tokenDuplicates)));
}

$tokens = array_map(function ($token) {
    return $token . ',';
}, array_flip($tokens));
$tokenLengths = array_map('strlen', array_keys($tokens));
array_multisort($tokenLengths, SORT_DESC, $tokens);

$changedCounter = 0;
$addedCounter = 0;
$deletedCounter = 0;

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
        $commitMessage = file($changeDir . '/README.txt');
        array_splice($commitMessage, 0, 3);
        $commitMessageFirstLine = current(array_filter(array_map('trim', $commitMessage)));
        if (!preg_match($refactorRegex, $commitMessageFirstLine)) {
            continue;
        }
        if (is_dir($diffsDir)) {
            $asts = array_diff(scandir($diffsDir), ['.', '..', 'README.txt']);
            foreach ($asts as $astFile) {
                $diffFile = file_get_contents($diffsDir . '/' . $astFile);
                list($codeBefore, $codeAfter, $astBefore, $astAfter) = explode(DIFF_SEPARATOR, $diffFile);
                $astBefore = tokenize($astBefore);
                $astAfter = tokenize($astAfter);
                if ($astBefore && $astAfter) {
                    file_put_contents(CHANGED_METHODS_BEFORE, $astBefore . PHP_EOL, FILE_APPEND);
                    file_put_contents(CHANGED_METHODS_AFTER, $astAfter . PHP_EOL, FILE_APPEND);
                    file_put_contents(CHANGED_DIR . sprintf("%08d", ++$changedCounter) . '.txt', $diffFile);
                }
//                elseif ($astBefore) {
//                    file_put_contents(DELETED_METHODS, $astBefore . PHP_EOL, FILE_APPEND);
//                    file_put_contents(DELETED_DIR . sprintf("%08d", ++$deletedCounter) . '.txt', $diffFile);
//                } else {
//                    file_put_contents(ADDED_METHODS, $astAfter . PHP_EOL, FILE_APPEND);
//                    file_put_contents(ADDED_DIR . sprintf("%08d", ++$addedCounter) . '.txt', $diffFile);
//                }
            }
        }
    }
    $progress->advance();
}

function tokenize(string $ast): string
{
    global $tokens;
    $tokenizedAst = preg_replace('#\\s#m', '', $ast);
    $tokenizedAst = str_replace(array_keys($tokens), $tokens, $tokenizedAst);
    $tokenizedAst = preg_replace('#[\(\)]#m', '', $tokenizedAst);
    $allTokensReplaced = preg_match('#^[\d,]*$#', $tokenizedAst, $matches);
    if (!$allTokensReplaced) {
        preg_match_all('#[^\d,]+#', $tokenizedAst, $matches);
        throw new InvalidArgumentException("NO TOKENS FOR: " . implode(', ', $matches[0]) . PHP_EOL . $tokenizedAst . PHP_EOL . $ast);
    }
    return rtrim($tokenizedAst, ',');
}
