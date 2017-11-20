commit f470fc70bceb0e85828153484bafa1fccc1884f5
Author: Ketan Padegaonkar <KetanPadegaonkar@gmail.com>
Date:   Mon Aug 28 19:07:32 2017 +0530

    More logging improvements

    * rename `go.agent.console.stdout` to `go.console.stdout` so it can be
      re-used on server as well.
    * redirect plugin logs to STDOUT when system property is set. When
      printing plugin logs to console, add the plugin id in the log
      message.
    * agent console output is prefixed with `stdout/stderr` to indicate
      where the output is coming from