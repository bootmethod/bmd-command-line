package io.github.bootmethod.commandline

import groovy.transform.CompileStatic

/**
 * 
 * @
 *  
 * */
@CompileStatic
class ShellContext {

    static ThreadLocal<ShellContext> CONTEXT = new ThreadLocal<>()

    boolean running = true

    ShellContext parent

    static ShellContext get() {
        return CONTEXT.get()
    }

    static ShellContext create() {
        ShellContext parent = get()
        ShellContext shellContext = new ShellContext(parent: parent)
        CONTEXT.set(shellContext)
        return shellContext
    }

    void close() {
        if (get() != this) {
            throw new RuntimeException("cannot close the shell context which is not the current one.")
        }
        this.running = false
        CONTEXT.set(parent)
    }
}
