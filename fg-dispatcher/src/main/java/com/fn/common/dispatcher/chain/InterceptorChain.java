package com.fn.common.dispatcher.chain;

import com.fn.common.dispatcher.bean.MessageRequest;
import com.fn.common.dispatcher.bean.MessageResponse;

/**
 * 拦截器链
 * @author fomin
 * @date 2019-12-07
 */
public interface InterceptorChain {
    /**
     * 执行下一个拦截器，若已到达链的尾部，则执行真正的消息分发或 RPC 调用
     *
     * @param request 消息请求，可以使用 {@link InterceptorContext#request()}，也可以自己构造一个新的请求代替
     * @return 返回下一个拦截器处理完后的响应结果，若已到达链的尾部，则返回真正的消息响应或 RPC 响应
     * @throws Throwable 可抛出拦截过程、消息分发过程、RPC 调用过程中产生的任何异常
     */
    MessageResponse proceed(MessageRequest request) throws Throwable;
}
