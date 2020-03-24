package com.fn.common.dispatcher.chain;

import com.fn.common.dispatcher.bean.ArgumentData;
import com.fn.common.dispatcher.bean.MessageData;
import com.fn.common.dispatcher.bean.MessageRequest;
import com.fn.common.dispatcher.bean.MessageResponse;
import com.fn.common.dispatcher.core.MessageDispatcherContext;
import com.fn.common.dispatcher.core.MessageHandler;

import java.lang.reflect.Parameter;
import java.util.Arrays;

/**
 * 拦截器链实现
 *
 * @author fomin
 * @date 2019-12-07
 */
public final class InterceptorChainImp implements InterceptorChain {
    private final MessageHandler handler;
    private final MessageData data;
    private final ArgumentData argumentData;

    public InterceptorChainImp(MessageHandler handler, ArgumentData argumentData) {
        this.handler = handler;
        this.data = handler.getData();
        this.argumentData = argumentData;
    }

    @Override
    public MessageResponse proceed(MessageRequest request) throws Throwable {
        try {
            MessageDispatcherContext.set(request, handler);
            Object[] args = parseArguments(request);
            Object result = handler.invoke(args);
            return createResponse(request, result);
        } finally {
            MessageDispatcherContext.clear();
        }
    }

    /**
     * 为 handler 方法的每个参数赋值，具体算法如下：
     * <p>
     * 1. 先使用传入的 argumentData 由用户推断参数值
     * 2. 若 argumentData 返回 null，用户无法推断，再判断参数类型是否为 MessageRequest 或者 rawRequestClass，若是，传入相应值
     * 3. 最后，如果还是无法推断出参数值，则使用 MessageRequest.parseArguments 从请求体中解析
     */
    private Object[] parseArguments(MessageRequest request) {
        Object[] args = new Object[data.getParameterCount()];

        int[] parsingIndices = new int[data.getParameterCount()];
        int parsingCount = 0;

        for (int i = 0; i < args.length; i++) {
            args[i] = argumentData == null ? null : argumentData.argument(i, data);
            if (args[i] != null) {
                continue;
            }

            Parameter param = data.getParameter(i);
            if (MessageRequest.class.isAssignableFrom(param.getType())) {
                args[i] = request;
            } else {
                parsingIndices[parsingCount++] = i;
            }
        }

        Object[] parsedArguments = request.parseArguments(Arrays.copyOf(parsingIndices, parsingCount), data);

        for (int i = 0; i < parsingCount; i++) {
            args[parsingIndices[i]] = parsedArguments[i];
        }

        return args;
    }

    private MessageResponse createResponse(MessageRequest request, Object result) {
        MessageResponse response;

        if (result instanceof MessageResponse) {
            response = (MessageResponse) result;
        } else {
            response = request.createResponse();

            if (data.getMethod().getReturnType() != void.class) {
                // Important: null can also be a legal result, don't forget to call setResult here.
                response.setResult(result, data);
            }
        }

        return response;
    }
}
