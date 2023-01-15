package io.github.bootmethod.commandline.util

import io.github.bootmethod.commandline.Result

/**
 * 
 * 
 * @created : 11/8/2022
 * */

class StringResult implements Result {

    String message

    StringResult(String message) {
        this.message = message
    }

    @Override
    void print(PrintStream context) {
        context.println(message)
    }
}
