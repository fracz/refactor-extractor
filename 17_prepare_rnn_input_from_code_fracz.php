<?php
require 'vendor/autoload.php';
require 'class.Diff.php';

ini_set('memory_limit', '2G');

$maxLength = 200;
$astDir = __DIR__ . '/input/ast-java-strict/';

$tokensByNumber = array_map('trim', file(__DIR__ . '/tokens.txt'));
$tokens = array_flip($tokensByNumber);

require __DIR__ . '/scoreboard.php';

$result = ['after' => [], 'before' => []];
$rowsBefore = explode(PHP_EOL, file_get_contents($astDir . 'changed-before.txt'));
$rowsAfter = explode(PHP_EOL, file_get_contents($astDir . 'changed-after.txt'));
foreach ($scoreboard as $scoreboardEntry) {
    list($score, $count, $filename) =  array_values($scoreboardEntry);
    if (abs($score) < 3) {
        continue;
    }
    $fileIndex = intval($filename) - 1;
    $afterBetter = $score > 0;
    $codeBefore = $rowsBefore[$fileIndex];
    $codeBeforePadded = implode(',', array_pad(explode(',', $codeBefore), $maxLength, 0));
    $codeAfter = $rowsAfter[$fileIndex];
    $codeAfterPadded = implode(',', array_pad(explode(',', $codeAfter), $maxLength, 0));
    $beforeEntry = [
        'X' => $codeBeforePadded,
        'Y' => '0,1',
        'len' => substr_count($codeBefore, ',') + 1,
    ];
    $afterEntry = [
        'X' => $codeAfterPadded,
        'Y' => '1,0',
        'len' => substr_count($codeAfter, ',') + 1,
    ];
    if ($afterBetter) {
        $result['before'][] = $beforeEntry;
        $result['after'][] = $afterEntry;
    } else {
        $beforeEntry['Y'] = '1,0';
        $result['after'][] = $beforeEntry;
        $afterEntry['Y'] = '0,1';
        $result['before'][] = $afterEntry;
    }
}

$fakeDataset = $result;

file_put_contents(__DIR__ . '/input/rnn/input-before.csv', implode(PHP_EOL, array_map(function ($row) {
    return $row['X'];
}, $fakeDataset['before'])));
file_put_contents(__DIR__ . '/input/rnn/lengths-before.csv', implode(',', array_map(function ($row) {
    return $row['len'];
}, $fakeDataset['before'])));
file_put_contents(__DIR__ . '/input/rnn/input-after.csv', implode(PHP_EOL, array_map(function ($row) {
    return $row['X'];
}, $fakeDataset['after'])));
file_put_contents(__DIR__ . '/input/rnn/lengths-after.csv', implode(',', array_map(function ($row) {
    return $row['len'];
}, $fakeDataset['after'])));

$fakeDataset = array_merge($fakeDataset['after'], $fakeDataset['before']);

shuffle($fakeDataset);

file_put_contents(__DIR__ . '/input/rnn/input.csv', implode(PHP_EOL, array_map(function ($row) {
    return $row['X'];
}, $fakeDataset)));
file_put_contents(__DIR__ . '/input/rnn/labels.csv', implode(PHP_EOL, array_map(function ($row) {
    return $row['Y'];
}, $fakeDataset)));
file_put_contents(__DIR__ . '/input/rnn/lengths.csv', implode(',', array_map(function ($row) {
    return $row['len'];
}, $fakeDataset)));



