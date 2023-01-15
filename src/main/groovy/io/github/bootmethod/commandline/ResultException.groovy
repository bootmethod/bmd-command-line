package io.github.bootmethod.commandline

import io.github.bootmethod.commandline.util.StringResult
import io.github.bootmethod.commandline.util.TableData
import io.github.bootmethod.commandline.util.TableResult


/**
 * 
 * 
 * @created : 11/8/2022
 * */
class ResultException extends RuntimeException implements Result {
    Result result

    ResultException(String message) {
        this(new StringResult(message))
    }

    ResultException(Result result) {
        this.result = result
    }

    ResultException(TableData result) {
        this.result = new TableResult(result)
    }

    @Override
    void print(PrintStream context) {
        this.result.print(context)
    }
}
