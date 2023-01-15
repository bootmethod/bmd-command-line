package io.github.bootmethod.commandline.impl


import groovy.transform.CompileStatic
import org.jline.reader.Candidate
import org.jline.reader.Completer
import org.jline.reader.LineReader
import org.jline.reader.ParsedLine

/**
 * 
 * 
 * @created : 11/14/2022
 * */
@CompileStatic
class CommandsCompleter<T> implements Completer {

    Map<String, Completer> map = new HashMap<>()

    CommandsCompleter add(String command, Completer completer) {
        map.put(command, completer)
        return this
    }

    @Override
    void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        doComplete(reader, line, candidates)
    }

    void doComplete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
        String command = ""
        List<String> words = line.words()
        if (words.size() > 0) {
            command = words.get(0)
        }

        if (words.size() < 2) {

            map.entrySet().forEach({
                if (it.key.startsWith(command)) {
                    candidates.add(new Candidate(it.key))
                }
            })

        } else {
            map.entrySet().forEach({
                if (command == it.key) {
                    it.value.complete(reader, line, candidates)
                }
            })

        }
    }

}
