import java.util.function.Consumer

class Process {
    static run(String command) {
        def process = Runtime.runtime.exec(command)

        join(process.inputStream, { System.out.println(it) })
        join(process.errorStream, { System.err.println(it) })

        process.waitFor()
        process
    }

    private static join(InputStream stream, Consumer<String> logger) {
        def bufferedReader = new BufferedReader(new InputStreamReader(stream))
        String line
        while ((line = bufferedReader.readLine()) != null) {
            logger(line)
        }
    }
}
