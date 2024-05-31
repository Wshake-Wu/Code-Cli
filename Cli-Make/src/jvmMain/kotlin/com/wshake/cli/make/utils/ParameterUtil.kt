package com.wshake.cli.make.utils

import com.wshake.common.exception.BusinessException

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2024-01-11
 */
object ParameterUtil {
    fun loopParameter(loop: Any, str: String, func: (Any) -> Unit) {
        val split = str.split(".")
        var temp: Any = loop
        for (i in split.indices) {
            when (temp) {
                is Map<*, *> -> {
                    temp.containsKey(split[i]).isFalse {
                        throw BusinessException("foreach参数${split[i]}不存在")
                    }
                    temp = temp[split[i]]!!
                    if (i == split.size - 1) {
                        func(temp)
                        return
                    }
                }
                else -> {
                    throw BusinessException("foreach参数${split[i]}最终必须指向Map")
                }
            }
        }
    }
}