import java.util.function.Consumer

class Process {
    static run(String command) {
        def process = Runtime.runtime.exec(command)

        def out = join(process.inputStream, System.out.&println)
        def error = join(process.errorStream, System.err.&println)

        process.waitFor()
        [exitValue: process.exitValue(), out: out, error: error]
    }

    private static join(InputStream stream, Consumer<String> logger) {
        def logs = []
        def bufferedReader = new BufferedReader(new InputStreamReader(stream))
        String line
        while ((line = bufferedReader.readLine()) != null) {
            logger(line)
            logs << line
        }
        logs
    }
}
