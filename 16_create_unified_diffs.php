<?php
require 'vendor/autoload.php';
require 'class.Diff.php';

ini_set('memory_limit', '2G');

$maxLength = 100;
$astDir = __DIR__ . '/input/ast-java-strict/';
$diffDir = __DIR__ . '/input/ast-java-strict/unified-diffs/';
const DIFF_SEPARATOR = '||||||||';

@mkdir($diffDir, 0777, true);

$readDataset = function ($fileBefore, $fileAfter) use ($diffDir, $maxLength, $astDir) {
    $rowsBefore = explode(PHP_EOL, file_get_contents($astDir . $fileBefore));
    $rowsAfter = explode(PHP_EOL, file_get_contents($astDir . $fileAfter));


    $count = count($rowsBefore);
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
            if (count($changed) < 10
                || count($changed) > 50
            ) {
                unset($rowsBefore[$i]);
                unset($rowsAfter[$i]);
            } else {
                $id = sprintf("%08d", $i + 1);
                $codes = file_get_contents($astDir . '/changed/' . $id . '.txt');
                $codes = explode(DIFF_SEPARATOR, $codes);
                $tempBefore = $astDir . '/temp-before.java';
                $tempAfter = $astDir . '/temp-after.java';
                file_put_contents($tempBefore, $codes[0]);
                file_put_contents($tempAfter, $codes[1]);
                $hashBefore = exec("git hash-object $tempBefore -w", $gitDiff);
                $hashAfter = exec("git hash-object $tempAfter -w", $gitDiff);
                $command = "git diff $hashBefore $hashAfter -U1000";
                $gitDiff = [];
                exec($command, $gitDiff);
                file_put_contents($diffDir . '/' . $id . '.txt', implode(PHP_EOL, $gitDiff));

//                $change = implode(' ', array_map(function($c) { return $c[0]; }, $changed));
//                time();
            }
        }
    }

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

    return $result;
};

$readDataset('changed-before.txt', 'changed-after.txt');




