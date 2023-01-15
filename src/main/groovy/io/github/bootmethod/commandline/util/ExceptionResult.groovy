package io.github.bootmethod.commandline.util

import io.github.bootmethod.commandline.Result


/**
 * 
 * @
 *  
 * */

class ExceptionResult implements Result {

    public static String PRINT_STACK = ".result.printStack"

    Throwable exception

    Boolean printStack

    ExceptionResult(Throwable exception) {
        this.exception = exception
    }

    protected boolean isPrintStack(PrintStream context) {
        if (this.printStack != null) {
            return this.printStack.booleanValue()
        }
        return true
    }

    @Override
    void print(PrintStream context) {

        if (this.isPrintStack(context)) {
            this.exception.printStackTrace(context)
        } else {
            context.print(this.exception.getClass().getSimpleName())
            context.print(": ")
            context.println(this.exception.getMessage())
        }
    }
}
