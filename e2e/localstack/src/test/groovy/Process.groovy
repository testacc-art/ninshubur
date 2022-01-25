import java.util.function.Consumer

class Process {
    static class Result {
        int exitValue
        String error
    }

    static Result run(String command) {
        def process = Runtime.runtime.exec(command)

        def error = join(process.errorStream, System.err.&println)

        process.waitFor()
        new Result(exitValue: process.exitValue(), error: error)
    }

    private static String join(InputStream stream, Consumer<String> logger) {
        def logs = []
        def bufferedReader = new BufferedReader(new InputStreamReader(stream))
        String line
        while ((line = bufferedReader.readLine()) != null) {
            logger(line)
            logs << line
        }
        logs.join('\n')
    }
}
