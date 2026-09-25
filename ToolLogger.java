package com.atguigo.springaiproject_1.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 工具调用日志的统一封装。
 * 4 个 Function 都用它打印，保证格式一致、方便 grep。
 */
public final class ToolLogger {

//    private static final Logger log = LoggerFactory.getLogger("TOOL");
//
//    private ToolLogger() {}
//
//    /** 工具开始调用 */
//    public static void onEnter(String toolName, Object request) {
//        log.info(">>> [TOOL] {} 调用开始, 参数={}", toolName, request);
//    }
//
//    /** 参数校验失败 */
//    public static void onInvalidArgs(String toolName, String reason) {
//        log.warn(">>> [TOOL] {} 参数不合法: {}", toolName, reason);
//    }
//
//    /** 工具调用成功 */
//    public static void onSuccess(String toolName, Object response, long costMs) {
//        log.info(">>> [TOOL] {} 调用成功, 耗时={}ms, 返回={}", toolName, costMs, response);
//    }
//
//    /** 业务失败（比如"未找到"）*/
//    public static void onBusinessFail(String toolName, String reason) {
//        log.info(">>> [TOOL] {} 业务失败: {}", toolName, reason);
//    }
//
//    /** 系统异常 */
//    public static void onError(String toolName, Throwable e) {
//        log.error(">>> [TOOL] {} 执行异常", toolName, e);
//    }
    private static final Logger log = LoggerFactory.getLogger("Tool");

    private  ToolLogger() {
    }
    public static void onEnter(String toolname,Object request){
        log.info(">>>[TOOL]{}工具调用开始，参数是{}",toolname,request);
    }
    public static void onInvaliArgs(String toolname,Object reason){
        log.warn("[TOOL]{}参数不合法，{}",toolname,reason );
    }
    public static void onSuccess(String toolname,Object request,Long costMs){
        log.info("[TOOL]{}流程正常结束，耗时{}ms，返回{}",toolname,costMs,request);
    }
    public static void onBusinessFail(String toolname,Object request){
        log.info("[TOOL]{}工具业务失败：{}",toolname,request);
    }
    public static void onError(String toolname,Throwable e){
        log.error("[TOOL]{}执行异常：",toolname,e);
    }
}