<?php
require 'vendor/autoload.php';

ini_set('memory_limit', '2G');

$maxLength = 40;
$astDir = __DIR__ . '/input/ast/';

$asts = ['added', 'changed-before', 'changed-after', 'deleted'];

$readDataset = function ($file, $Y) use ($maxLength, $astDir) {
    $rows = explode(PHP_EOL, file_get_contents($astDir . $file));
    $notTooLongRows = array_filter($rows, function ($row) use ($maxLength) {
        $tokenCount = substr_count($row, ',') + 1;
        return $tokenCount > 3 && $tokenCount <= $maxLength;
    });
    unset($rows);

    return array_map(function ($row) use ($Y, $maxLength) {
        $tokens = explode(',', $row);
        $length = count($tokens);
        $label = $Y;
        $padded = array_pad($tokens, $maxLength, 0);
        return [
            'X' => implode(',', $padded),
            'Y' => implode(',', $label),
            'len' => $length,
        ];
    }, $notTooLongRows);
};

$datasetBefore = $readDataset('changed-before.txt', [0, 1]);
$datasetAfter = $readDataset('changed-after.txt', [1, 0]);

$dataset = array_merge($datasetBefore, $datasetAfter);

unset($datasetBefore);
unset($datasetAfter);

shuffle($dataset);

file_put_contents(__DIR__ . '/input/rnn/input.csv', implode(PHP_EOL, array_map(function ($row) {
    return $row['X'];
}, $dataset)));
file_put_contents(__DIR__ . '/input/rnn/labels.csv', implode(PHP_EOL, array_map(function ($row) {
    return $row['Y'];
}, $dataset)));
file_put_contents(__DIR__ . '/input/rnn/lengths.csv', implode(',', array_map(function ($row) {
    return $row['len'];
}, $dataset)));
//    file_put_contents($astDir . $ast . '-max-' . $maxLength . '-lengths.txt', implode(',', $lenghts));

//    unset($padded);
//    unset($lenghts);
//    unset($notTooLongRows);
