package com.fn.game.controller;

import com.fn.common.dispatcher.annotation.MessageController;
import com.fn.common.dispatcher.annotation.MessageMapping;
import com.fn.common.proto.http.PBIntValue;
import com.fn.common.proto.http.PBMessageType;
import com.fn.common.proto.http.PBStrValue;

/**
 * @author fomin
 * @date 2020-02-23
 */
@MessageController
public class HttpController {

    @MessageMapping(messageType = PBMessageType.DEFAULT_TYPE_VALUE)
    public PBStrValue getString(PBIntValue req) {
        return PBStrValue.newBuilder().setValue(("Hello world！" + req.getValue())).build();
    }
}
