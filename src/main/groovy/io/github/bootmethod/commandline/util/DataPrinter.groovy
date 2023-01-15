package io.github.bootmethod.commandline.util

import java.nio.charset.Charset

/**
 * 
 * 
 * @created : 11/9/2022
 * */

interface DataPrinter<T> {

    DataPrinter<T> printHeader(boolean printHeader)

    void print(T data, Appendable out, Charset charset);
}
