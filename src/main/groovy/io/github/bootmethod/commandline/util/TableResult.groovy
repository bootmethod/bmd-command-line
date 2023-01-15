package io.github.bootmethod.commandline.util

import groovy.transform.CompileStatic

import java.nio.charset.StandardCharsets

/**
 * 
 * 
 * @created : 11/8/2022
 * */
@CompileStatic
class TableResult implements io.github.bootmethod.commandline.Result {
    TableData tableData

    TableResult(TableData tableData) {
        this.tableData = tableData
    }

    @Override
    void print(PrintStream context) {
        Printers.print(this.tableData, context, StandardCharsets.UTF_8)
    }
}
