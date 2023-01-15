package io.github.bootmethod.commandline.impl

import groovy.transform.CompileStatic
import io.github.bootmethod.commandline.LinesReader
import org.jline.reader.LineReader

/**
 * 
 * @
 *  
 * */
@CompileStatic
class LinesReaderImpl implements LinesReader {

    LineReader lineReader

    LinesReaderImpl(LineReader lineReader) {
        this.lineReader = lineReader
    }

    @Override
    String nextLine(String prompt) {
        return nextLine(prompt, null)
    }

    @Override
    String nextLine(String prompt, Character mask) {
        StringBuilder buffer = new StringBuilder()

        while (true) {

            String line = mask ? lineReader.readLine(prompt, mask) : lineReader.readLine(prompt)

            int lastSlash = line.lastIndexOf("\\")
            if (lastSlash > 0 && line.substring(lastSlash + 1).trim() == "") {
                line = line.substring(0, lastSlash)
                prompt = " "
                buffer.append(line).append(prompt)
            } else {
                buffer.append(line)
                break
            }
        }
        return buffer.toString()
    }
}
