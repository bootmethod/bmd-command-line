package io.github.bootmethod.commandline

import groovy.transform.CompileStatic
import io.github.bootmethod.commandline.impl.LinesReaderImpl
import io.github.bootmethod.commandline.util.Results
import org.jline.reader.LineReader
import org.jline.reader.LineReaderBuilder
import org.jline.reader.UserInterruptException
import org.jline.terminal.Terminal
import org.jline.utils.OSUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.nio.file.Path
import java.util.concurrent.BlockingQueue
import java.util.concurrent.LinkedBlockingQueue
import java.util.function.Function
import java.util.function.Supplier

/**
 * 
 * @
 *  
 * */
@CompileStatic
class ShellRunner<T> {

    static Logger LOG = LoggerFactory.getLogger(ShellRunner.class.getName())

    Terminal terminal
    Function<String, Object> lineConsumer = { 0 }
    Supplier<T> resultSupplier = { 0 }
    Supplier<Boolean> runningMonitor = { true }
    Map<String, Object> variables = [:]
    String prompt = "# "

    ShellRunner<T> variables(Map<String, Object> variables) {
        this.variables.putAll(variables ?: [:])
        return this
    }

    ShellRunner<T> terminal(Terminal terminal) {
        this.terminal = terminal
        return this
    }

    ShellRunner<T> runningMonitor(Supplier<Boolean> runningMonitor) {
        this.runningMonitor = runningMonitor
        return this
    }

    ShellRunner<T> lineConsumer(Function<String, Object> lineConsumer) {
        this.lineConsumer = lineConsumer
        return this
    }

    ShellRunner<T> resultSupplier(Supplier<T> resultSupplier) {
        this.resultSupplier = resultSupplier
        return this
    }

    T run() {
        ShellContext shellContext = ShellContext.create()
        try {
            return doRun(shellContext)
        } finally {
            shellContext.close()
        }

    }

    T doRun(ShellContext shellContext) {
        if (!this.terminal) {
            this.terminal = variables.get("terminal") as Terminal

        }
        if (!this.terminal) {
            throw new RuntimeException("no terminal provided.")
        }
        LineReader lineReader = buildLineReader(this.terminal)
        PrintStream output = new PrintStream(this.terminal.output())
        LinesReader linesReader = new LinesReaderImpl(lineReader)
        BlockingQueue<String> subLines = new LinkedBlockingQueue<>()

        while (shellContext.running) {
            if (!runningMonitor.get()) {
                shellContext.running = false
                break
            }
            LineContext lineContext = LineContext.create()
            lineContext.subLines = subLines
            try {
                boolean interrupted
                try {

                    String line = subLines.poll()

                    if (line == null) {
                        line = linesReader.nextLine(prompt)
                    }
                    Object result = lineConsumer.apply(line)
                    lineContext.rawResult = result

                } catch (Throwable e) {
                    if (e instanceof UserInterruptException) {
                        //ignore
                        continue
                    } else {
                        lineContext.rawResult = e
                    }
                }
                
                Result printResult = Results.asResult(lineContext.rawResult)
                logResultOutput(printResult, lineContext)
                printResult.print(output)


            } finally {
                lineContext.close()
            }
        }
        return resultSupplier?.get()
    }

    protected void logResultOutput(Result printResult, LineContext lineContext) {

        if (LOG.isInfoEnabled()) {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
            PrintStream ps = new PrintStream(outputStream, true, "utf-8")

            ps.println(lineContext.rawResult ? lineContext.rawResult.getClass().getName() : "null")

            printResult.print(ps)
            ps.flush()

            LOG.info(outputStream.toString("utf-8"))
        }
    }

    LineReader buildLineReader(Terminal terminal) {

        Path historyFile = null
        LineReader lineReader = LineReaderBuilder.builder()
                .terminal(terminal)
                .completer(null)
                .parser(null)
                .highlighter(null)
                .variable(LineReader.SECONDARY_PROMPT_PATTERN, "%M%P > ")
                .variable(LineReader.INDENTATION, 2)
                .variable(LineReader.LIST_MAX, 100)
                .variable(LineReader.HISTORY_FILE, historyFile)
                .option(LineReader.Option.INSERT_BRACKET, true)
                .option(LineReader.Option.EMPTY_WORD_OPTIONS, false)
                .option(LineReader.Option.USE_FORWARD_SLASH, true)             // use forward slash in directory separator
                .option(LineReader.Option.DISABLE_EVENT_EXPANSION, true)
                .build()
        if (OSUtils.IS_WINDOWS) {
            lineReader.setVariable(LineReader.BLINK_MATCHING_PAREN, 0)
            // if enabled cursor remains in begin parenthesis (gitbash)
        }
        return lineReader
    }

}
