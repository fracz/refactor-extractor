<?php
require 'vendor/autoload.php';

ini_set('memory_limit', '2G');

$maxLength = 100;
$tokensTreatedAsBad = ['AST_IF', 'ZEND_AST_IF', 'AST_IF_ELEM'];
$astDir = __DIR__ . '/input/ast/';

$tokens = array_flip(array_map('trim', file(__DIR__ . '/tokens.txt')));
$tokensTreatedAsBad = array_map(function ($tokenName) use ($tokens) {
    return $tokens[$tokenName];
}, $tokensTreatedAsBad);

$readDataset = function ($file) use ($maxLength, $astDir, $tokensTreatedAsBad) {
    $rows = explode(PHP_EOL, file_get_contents($astDir . $file));
    $notTooLongRows = array_filter($rows, function ($row) use ($maxLength) {
        $tokenCount = substr_count($row, ',') + 1;
        return $tokenCount > 3 && $tokenCount <= $maxLength;
    });
    unset($rows);

    return array_map(function ($row) use ($tokensTreatedAsBad, $maxLength) {
        $tokens = explode(',', $row);
        $length = count($tokens);
        $isBadCode = count(array_intersect($tokensTreatedAsBad, $tokens)) > 0;
        $label = $isBadCode ? [0, 1] : [1, 0];
        $padded = array_pad($tokens, $maxLength, 0);
        return [
            'X' => implode(',', $padded),
            'Y' => implode(',', $label),
            'len' => $length,
        ];
    }, $notTooLongRows);
};

$fakeDataset = $readDataset('changed-before.txt');

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

