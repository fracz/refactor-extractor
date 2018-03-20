<?php
require 'vendor/autoload.php';
require 'class.Diff.php';

ini_set('memory_limit', '2G');

$maxLength = 200;
$astDir = __DIR__ . '/input/ast-java-strict/';

$tokensByNumber = array_map('trim', file(__DIR__ . '/tokens.txt'));
$tokens = array_flip($tokensByNumber);

require __DIR__ . '/scoreboard.php';
$used = [];
foreach ($scoreboard as $scoreboardEntry) {
    list($score, $count, $filename) =  array_values($scoreboardEntry);
    if (abs($score) < 3) {
        continue;
    }
    $used[intval($filename) - 1] = true;
}

$fileBefore = 'changed-before.txt';
$fileAfter = 'changed-after.txt';

$rowsBefore = explode(PHP_EOL, file_get_contents($astDir . $fileBefore));
$rowsAfter = explode(PHP_EOL, file_get_contents($astDir . $fileAfter));

$count = count($rowsBefore);
$usedCount = 0;
for ($i = 0; $i < $count; $i++) {

    $tokensBefore = substr_count($rowsBefore[$i], ',') + 1;
    $tokensAfter = substr_count($rowsAfter[$i], ',') + 1;
    $shortes = max($tokensBefore, $tokensAfter);
    if ($shortes > $maxLength) {
        unset($rowsBefore[$i]);
        unset($rowsAfter[$i]);
    } else {
        $beforeForDiff = str_replace(',', PHP_EOL, $rowsBefore[$i]);
        $afterForDiff = str_replace(',', PHP_EOL, $rowsAfter[$i]);
//            $beforeForDiff = implode(PHP_EOL, array_map(function ($int) use ($tokensByNumber) {
//                return $tokensByNumber[$int];
//            }, explode(',', $rowsBefore[$i])));
//            $afterForDiff = implode(PHP_EOL, array_map(function ($int) use ($tokensByNumber) {
//                return $tokensByNumber[$int];
//            }, explode(',', $rowsAfter[$i])));
        $diff = Diff::compare($beforeForDiff, $afterForDiff);
        $changed = array_filter($diff, function ($d) {
            return $d[1] != Diff::UNMODIFIED;
        });
        $added = count(array_filter($changed, function ($d) {
            return $d[1] == Diff::INSERTED;
        }));
        $deleted = count($changed) - $added;
        if (isset($used[$i])) {
            $usedCount++;
        }
        if (count($changed) < 5
            || count($changed) > 100
            || !$added
            || !$deleted
            || isset($used[$i])
        ) {
            unset($rowsBefore[$i]);
            unset($rowsAfter[$i]);
        } else {
//                $change = implode(' ', array_map(function($c) { return $c[0]; }, $changed));
//                time();
        }
    }
}

echo "Methods skipped: " . $usedCount . PHP_EOL;
echo "Methods that should be skipped: " . count($used) . PHP_EOL;
echo "Methods count in dataset: " . count($rowsBefore) . PHP_EOL;

$rowsBefore = array_values($rowsBefore);
$rowsAfter = array_values($rowsAfter);

$result = ['after' => [], 'before' => []];

for ($i = 0; $i < count($rowsBefore); $i++) {
    $result['before'][] = [
        'X' => implode(',', array_pad(explode(',', $rowsBefore[$i]), $maxLength, 0)),
        'Y' => implode(',', [0, 1]),
        'len' => substr_count($rowsBefore[$i], ',') + 1,
    ];
    $result['after'][] = [
        'X' => implode(',', array_pad(explode(',', $rowsAfter[$i]), $maxLength, 0)),
        'Y' => implode(',', [1, 0]),
        'len' => substr_count($rowsAfter[$i], ',') + 1,
    ];
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



