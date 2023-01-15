package io.github.bootmethod.commandline.util

/**
 * 
 * 
 * @created : 11/9/2022
 * */

interface TableData {

    /**
     * @return Nullable
     */
    Object[] getColumns()

    Object[][] getBodyAsArrayArray()
}
