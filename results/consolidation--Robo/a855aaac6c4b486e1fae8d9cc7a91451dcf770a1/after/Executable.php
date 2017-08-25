<?php
namespace Robo\Task\Shared;
use Robo\Result;
use \Symfony\Component\Process\Process as SymfonyProcess;

trait Executable
{
    use CommandExecutable;

    protected $arguments = '';
    protected $workingDirectory;

    /**
     * changes working directory of command
     * @param $dir
     * @return $this
     */
    public function dir($dir)
    {
        $this->workingDirectory = $dir;
        return $this;
    }

    /**
     * Pass argument to executable
     *
     * @param $arg
     * @return $this
     */
    public function arg($arg)
    {
        return $this->args($arg);
    }

    /**
     * Pass methods parameters as arguments to executable
     *
     * @param $args
     * @return $this
     */
    public function args($args)
    {
        if (!is_array($args)) {
            $args = func_get_args();
        }
        $this->arguments .= " ".implode(' ', $args);
        return $this;
    }

    /**
     * Pass option to executable. Options are prefixed with `--` , value can be provided in second parameter
     *
     * @param $option
     * @param null $value
     * @return $this
     */
    public function option($option, $value = null)
    {
        if ($option !== null and strpos($option, '-') !== 0) {
            $option = "--$option";
        }
        $this->arguments .= null == $option ? '' : " " . $option;
        $this->arguments .= null == $value ? '' : " " . $value;
        return $this;
    }


}