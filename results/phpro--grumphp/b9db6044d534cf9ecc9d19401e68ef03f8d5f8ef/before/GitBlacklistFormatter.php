<?php

namespace GrumPHP\Formatter;

use GrumPHP\IO\IOInterface;
use Symfony\Component\Process\Process;

/**
 * Class GitBlacklistFormatter
 *
 * @package GrumPHP\Formatter
 */
class GitBlacklistFormatter implements ProcessFormatterInterface
{
    /**
     * @var IOInterface
     */
    private $IO;

    /**
     * GitBlacklistFormatter constructor.
     * @param IOInterface $IO
     */
    public function __construct(IOInterface $IO)
    {
        $this->IO = $IO;
    }

    /**
     * @param Process $process
     *
     * @return string
     */
    public function format(Process $process)
    {
        $output = $process->getOutput();
        if (!$output) {
            return $process->getErrorOutput();
        }
        if (!$this->IO->isDecorated()) {
            return $output;
        }
        return $this->formatOutput($output);
    }

    /**
     * @param string $output
     * @return string
     */
    private function formatOutput($output)
    {
        $result = "\033[m";
        foreach (array_filter(explode(PHP_EOL, $output)) as $lineNumber => $line) {
            if (preg_match('/^[0-9]+/', $line)) {
                $result .= $this->trimOutputLine($line, $lineNumber) . PHP_EOL;
            } else {
                $result .= $line . PHP_EOL;
            }
        }
        return trim($result);
    }

    private function trimOutputLine($line, $lineNumber)
    {
        $wordColor = "\033[1;31";
        $resetColor = "\033[m";
        $colonColor = "\033[36m";
        $coloredColon = $colonColor . ':' . $resetColor;

        $before = 20;
        $after = 20;

        if (strlen($line) >= 80) {
            $lastPos = 0;
            $positionsFirst = array();
            $positionsSecond = array();
            $parts = array();

            while (($lastPos = mb_strpos($line, $wordColor, $lastPos)) !== false) {
                $positionsFirst[] = $lastPos;
                $lastPos = $lastPos + mb_strlen($wordColor);
            }
            $lastPos = 0;
            while (($lastPos = mb_strpos($line, $resetColor, $lastPos)) !== false) {
                $positionsSecond[] = $lastPos;
                $lastPos = $lastPos + mb_strlen($resetColor);
            }

            foreach ($positionsFirst as $pos) {
                do {
                    $pos2 = array_shift($positionsSecond);
                } while ($pos2 < $pos);

                $pos -= $before;
                $pos2 += $after;
                $parts[] = '  ' . $lineNumber . $coloredColon
                    . ($pos + $before) . $coloredColon
                    . ' ' . mb_substr($line, $pos, $pos2 - $pos) . $resetColor;
            }

            $line = implode(PHP_EOL, $parts);
        }

        return $line;
    }
}