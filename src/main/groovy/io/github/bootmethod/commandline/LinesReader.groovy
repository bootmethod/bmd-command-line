package io.github.bootmethod.commandline

interface LinesReader {
    String nextLine(String prompt)

    String nextLine(String prompt, Character mask)

}