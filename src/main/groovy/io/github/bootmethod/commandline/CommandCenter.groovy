package io.github.bootmethod.commandline


import org.jline.reader.Completer

import java.util.function.BiFunction

/**
 * 
 * 
 * @created : 11/10/2022
 * */

interface CommandCenter {

    CommandCenter add(Class... commandClass)

    Completer completer()

    void close()

    Object execute(String s)

    BiFunction<String[],String[],Object> getMissingCommandHandler()

    void setMissingCommandHandler(BiFunction<String[],String[],Object> handler)
}
