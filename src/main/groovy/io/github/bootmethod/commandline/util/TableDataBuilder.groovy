package io.github.bootmethod.commandline.util

import groovy.transform.CompileStatic

import java.util.function.Function

/**
 * 
 * 
 * @created : 11/9/2022
 * */
@CompileStatic
class TableDataBuilder {

    static TableData createTableData(Object col1, Object col2, Set<Map.Entry<Object, Object>> set) {
        List<Object[]> data = set.collect { new Object[]{it.key, it.value} } as List<Object[]>
        return createTableData(new Object[]{col1, col2}, data, null)
    }

    static TableData createTableData(Object col, Collection<Object> data, String title) {
        return createTableData([col] as Object[], data.collect({ [it] }) as Object[][], title)
    }

    static TableData createTableData(Object[] cols, Collection<Object[]> data, String title) {
        return new SimpleTableData(cols, data as Object[][])
    }

    static TableData createTableData(Object[] cols, Object[][] data, String title) {
        return new SimpleTableData(cols, data)
    }

    static TableData createTableData(Object keyName, Object valueName, Properties properties) {
        return createTableData(keyName, valueName, properties.entrySet())
    }

    static <T> TableData createTableData(Object[] cols, Collection<T> list, Function<T, Object[]> rowGenerator, String title) {
        return createTableData(cols, list.collect({
            rowGenerator.apply(it)
        }) as Object[][], title)
    }
}
