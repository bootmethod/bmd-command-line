package io.github.bootmethod.commandline


import groovy.transform.CompileStatic

import java.util.concurrent.BlockingQueue

/**
 * 
 * @
 *
 * */
@CompileStatic
class LineContext {

    static interface CloseListener {
        void beforeLineContextClose(LineContext lineContext)
    }

    static ThreadLocal<LineContext> threadLocalLineContext = new ThreadLocal<>()

    List<CloseListener> closeListeners = new ArrayList<>()

    LineContext parent

    Object rawResult

    BlockingQueue<String> subLines

    private LineContext(LineContext parent) {
        this.parent = parent
    }

    static LineContext create() {
        LineContext previous = threadLocalLineContext.get()
        LineContext lineContext = new LineContext(previous)
        threadLocalLineContext.set(lineContext)
        return lineContext
    }

    static LineContext get() {
        return threadLocalLineContext.get()
    }

    void close() {
        for (CloseListener closeListener : this.closeListeners) {
            closeListener.beforeLineContextClose(this)
        }
        threadLocalLineContext.set(parent)
    }
}
