<?php
require 'vendor/autoload.php';
require 'class.Diff.php';

ini_set('memory_limit', '2G');

$maxLength = 10;
$tokensTreatedAsBad = ['AST_IF', 'ZEND_AST_IF', 'AST_IF_ELEM'];
$astDir = __DIR__ . '/input/ast/';

$tokens = array_flip(array_map('trim', file(__DIR__ . '/tokens.txt')));

$badCostsByName = [
    'AST_IF' => 2,
    'AST_STATIC_CALL' => 3,
    'AST_CONDITIONAL' => 2,
    'SCALAR' => 1,
    'NO_RETURN_TYPE' => 1,
    'NO_DOC_COMMENT' => 1,
    'AST_FOREACH' => 2,
    'AST_FOR' => 3,
    'AST_WHILE' => 3,
    'AST_SWITCH' => 4,
    'AST_UNARY_OP' => 1,
    'AST_CLOSURE' => 2,
    'NO_PARAM_TYPE' => 1,
    'AST_DIM' => 1, // array read
    'AST_CALL' => 1, // call global method
];

global $badCosts;
$badCosts = [];
foreach ($badCostsByName as $tokenName => $tokenValue) {
    $badCosts[$tokens[$tokenName]] = $tokenValue;
}

function calculateCost(array $tokens): int
{
    global $badCosts;
    return array_sum(array_map(function ($token) use ($badCosts) {
        return $badCosts[(int)$token] ?? 0;
    }, $tokens));
}

$readDataset = function ($fileBefore, $fileAfter) use ($maxLength, $astDir, $tokensTreatedAsBad) {


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
            $diff = Diff::compare(str_replace(',', PHP_EOL, $rowsBefore[$i]), str_replace(',', PHP_EOL, $rowsAfter[$i]));
            $changed = array_filter($diff, function ($d) {
                return $d[1] != Diff::UNMODIFIED;
            });
            if (count($changed) < 3) {
                unset($rowsBefore[$i]);
                unset($rowsAfter[$i]);
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

$fakeDataset = $readDataset('changed-before.txt', 'changed-after.txt');

if (false) { // one file with whole dataset, model2
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
} else {
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
}

