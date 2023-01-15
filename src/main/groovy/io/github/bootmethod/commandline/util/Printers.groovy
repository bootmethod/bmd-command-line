package io.github.bootmethod.commandline.util


import groovy.transform.CompileStatic

import java.nio.charset.Charset
import java.util.function.Supplier

/**
 * 
 * 
 * @created : 11/9/2022
 * */

@CompileStatic
class Printers {
    static Map<Class, Supplier<DataPrinter>> drivers = new HashMap<>()
    static {

    }

    static void addProvider(Class dataType, Supplier<DataPrinter> provider) {
        this.drivers.put(dataType, provider)
    }

    static void print(TableData data, Appendable out, Charset charset) {
        newPrinter(TableData.class).print(data, out, charset)
    }

    static <T> DataPrinter<T> newPrinter(Class<T> type) {
        return drivers.get(type).get()
    }
}
