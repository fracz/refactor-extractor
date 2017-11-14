<?php
require 'vendor/autoload.php';

ini_set('memory_limit', '2G');

$maxLength = 100;
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

$readDataset = function ($file) use ($maxLength, $astDir) {
    $rows = explode(PHP_EOL, file_get_contents($astDir . $file));
    $notTooLongRows = array_values(array_filter($rows, function ($row) use ($maxLength) {
        $tokenCount = substr_count($row, ',') + 1;
        return $tokenCount > 3 && $tokenCount <= $maxLength;
    }));
    unset($rows);

    echo 'Total: ' . count($notTooLongRows);
    $bad = 0;
    $result = [];

    foreach($notTooLongRows as $row) {
        $tokens = explode(',', $row);
        $length = count($tokens);
        $cost = calculateCost($tokens);
        $isBadCode = $cost / count($tokens) > .15;
        if ($isBadCode) $bad++;
        $label = $isBadCode ? [0, 1] : [1, 0];
        $padded = array_pad($tokens, $maxLength, 0);
        $result[] = [
            'X' => implode(',', $padded),
            'Y' => implode(',', $label),
            'len' => $length,
        ];
    }

    echo PHP_EOL . 'BAD: ' . $bad;

    return $result;
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

