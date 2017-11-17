<?php

const PER_PAGE = 100;
const QUERY = 'https://api.github.com/search/repositories?q=language:java%20stars:%3E=3000&sort=stars&order=desc&per_page=' . PER_PAGE . '&page=';

function fetchPage($page = 1) {
    $ch = curl_init(QUERY . $page);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch,CURLOPT_USERAGENT,'Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US; rv:1.8.1.13) Gecko/20080311 Firefox/2.0.0.13');
    $response = curl_exec($ch);
    curl_close($ch);
    return json_decode($response);
}

$pages = 100;
$projects = [];

for ($page = 1; $page <= $pages; $page++) {
    $response = fetchPage($page);
    $pages = ceil($response->total_count / PER_PAGE);
    $projects = array_merge($projects, array_map(function($project) {
        return $project->full_name;
    }, $response->items));
}

echo 'Projects: ' . count($projects) . PHP_EOL;
echo implode(' ', $projects);
