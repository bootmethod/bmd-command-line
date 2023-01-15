package io.github.bootmethod.commandline.util

import groovy.transform.CompileStatic

/**
 * 
 * 
 * @created : 11/9/2022
 * */
@CompileStatic
class SimpleTableData implements TableData {

    Object[] columns = []

    List<Object[]> data = new ArrayList<>()

    SimpleTableData(Object[] columns, Collection<Object> data) {
        this(columns, data as Object[][])
    }

    SimpleTableData(Object[] columns, Object[][] data) {
        this.columns = columns
        for (Object[] row : data) {
            this.data.add(row)
        }
    }

    @Override
    Object[] getColumns() {
        return this.columns
    }

    @Override
    Object[][] getBodyAsArrayArray() {
        return this.data as Object[][]
    }
}
