<?php

namespace Robo\Task;

use Robo\Result;
use Robo\Task\Shared\TaskInterface;

trait Concat
{
    protected function taskConcat($files)
    {
        return new ConcatTask($files);
    }
}

/**
 * Merges files into one. Used for preparing assets.
 *
 * ``` php
 * <?php
 * $this->taskConcat([
 *      'web/assets/screen.css',
 *      'web/assets/print.css',
 *      'web/assets/theme.css'
 *  ])
 *  ->to('web/assets/style.css')
 *  ->run()
 * ?>
 * ```
 */
class ConcatTask implements TaskInterface
{
    use \Robo\Output;

    /**
     * @var array|Iterator files
     */
    protected $files;

    /**
     * @var string dst
     */
    protected $dst;

    /**
     * Constructor.
     */
    public function __construct($files)
    {
        $this->files = $files;
    }

    /**
     * set the destination file
     *
     * @param string $dst
     *
     * @return ConcatTask The current instance
     */
    public function to($dst)
    {
        $this->dst = $dst;

        return $this;
    }

    /**
     * {@inheritdoc}
     */
    public function run()
    {
        if (is_null($this->dst) || "" === $this->dst) {
            return Result::error($this, 'You must specify a destination file with to() method.');
        }

        $dump = '';

        foreach ($this->files as $path) {
		    foreach(glob($path) as $file) {
			    if(!file_exists($file)) {
				    return Result::error($this, sprintf('File %s not found', $file));
			    }

			    $dump .= file_get_contents($file);
		    }
	    }

        $this->printTaskInfo(sprintf('Writing <info>%s</info>', $this->dst));

        $dst = $this->dst.'.part';
        file_put_contents($dst, $dump);
        rename($dst, $this->dst);

        return Result::success($this);
    }
}