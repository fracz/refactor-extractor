<?php
require 'vendor/autoload.php';

$astDir = __DIR__ . '/input/ast/';
$input = $astDir . 'deleted.txt';

$rows = explode(PHP_EOL, file_get_contents($input));

$histogram = [];

foreach ($rows as $row) {
    $tokenCount = substr_count($row, ',') + 1;
    if (!isset($histogram[$tokenCount])) {
        $histogram[$tokenCount] = 1;
    } else {
        ++$histogram[$tokenCount];
    }
}

ksort($histogram);

$results = '';

foreach ($histogram as $tokenCount => $freq) {
    $results .= $tokenCount . "\t" . $freq . PHP_EOL;
}

file_put_contents($astDir . '/histogram.txt', $results);
