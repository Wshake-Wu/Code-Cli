package com.wshake.cli.make.utils

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2023-12-30
 */
inline fun Boolean.isTrue(func: () -> Unit) {
    if (this) func()
}

inline fun Boolean.isFalse(func: () -> Unit) {
    if (!this) func()
}
inline fun <T> T.isNull(func: () -> Unit){
    if (this==null) func()
}
inline fun <T> T.isNotNull(func: () -> Unit){
    if (this!=null) func()
}