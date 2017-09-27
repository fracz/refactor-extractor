<?php
require 'vendor/autoload.php';

ini_set('memory_limit','1000M');

$maxLength = 2000;
$astDir = __DIR__ . '/input/ast/';

$asts = ['added', 'changed-before', 'changed-after', 'deleted'];

foreach ($asts as $ast) {
    $input = $astDir . $ast . '.txt';

    $rows = explode(PHP_EOL, file_get_contents($input));
    $notTooLongRows = array_filter($rows, function ($row) use ($maxLength) {
        $tokenCount = substr_count($row, ',') + 1;
        return $tokenCount <= $maxLength;
    });
    unset($rows);

    shuffle($notTooLongRows);

    $lenghts = array_map(function ($row) {
        return substr_count($row, ',') + 1;
    }, $notTooLongRows);

    $padded = array_map(function ($row) use ($maxLength) {
        $tokens = explode(',', $row);
        $padded = array_pad($tokens, $maxLength, 0);
        unset($tokens);
        return implode(',', $padded);
    }, $notTooLongRows);

    file_put_contents($astDir . $ast . '-max-' . $maxLength . '.txt', implode(PHP_EOL, $padded));
    file_put_contents($astDir . $ast . '-max-' . $maxLength . '-lengths.txt', implode(',', $lenghts));

    unset($padded);
    unset($lenghts);
    unset($notTooLongRows);
}
