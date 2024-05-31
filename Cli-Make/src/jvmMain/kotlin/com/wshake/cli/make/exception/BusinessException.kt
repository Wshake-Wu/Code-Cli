package com.wshake.common.exception

import com.wshake.cli.make.exception.HttpEnum

/**
 * Description:</p>
 *
 * @author Wshake-Wu
 * @since 2023-12-26
 */
data class BusinessException(
    val code: Int?=0,
    val msg: String?="BusinessException",
    val data: Any?=null
): RuntimeException(msg){
    constructor(errorEnum: HttpEnum, data: Any?=null):this(errorEnum.code,errorEnum.msg,data)
    constructor(msg: String?, data: Any?=null):this(0,msg,data)
    constructor(data: Any?=null):this(0,"BusinessException",data)
}