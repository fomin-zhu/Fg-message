### 前言
对于Spring的IoC(控制反转) 、DI(依赖注入)这两个概念，使用过spring的都很熟悉，使用容器来控制相关的对象的生命周期和对象间的关系。扩散下思维，可以使用一个容器来存储相关的http的controller类，根据http的请求参数来遍历这个容器执行哪个类哪个方法。但是如何是http调用这个容器呢？spring还提供了事件监听(事件监听概念这里不做说明)的实现，实现一个监听，利用spring-mvc的手动注册控制器来监听相关的http请求，调用容器执行相关的类和方法。

### 目标
先看下spring web的普通的http请求

```
@RestController
public class HttpController {
    @GetMapping("/http/welcome")
    @ResponseBody
    public string getLeagueById() {
        return "Hello word!";
    }
}
```
而目标需要改造成这样
```
@MessageController
public class HttpController {
    @MessageMapping(messageType = PBMessageType.HTTP_WELCOME_VALUE)
    public PBString adminLogin(PBWelcome req) {
        return PBWelcome.newBuilder().setValue("Hello word!").build();
    }
}
```
其实可以看出两者是基本相同的，但下面这个自定义了MessageController和MessageMapping使其支持Protocol Buffer（PB)格式。为什么要使用PB数据呢？因为它很适合做数据存储或 RPC 数据交换格式。可用于通讯协议、数据存储等领域的语言无关、平台无关、可扩展的序列化结构数据格式，用于将这些数据结构产生或解析数据流，并且性能比json/xml好。
### 核心设计
* **MessageController**
```
/**
 * controller注解类，用来spring扫描生成handler消息处理
 * 跟 MessageMapping 一起使用
 *
 * @see MessageMapping
 * @see MessageDispatcher
 * @author fomin
 * @date 2019-12-07
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Component
public @interface MessageController {
}
```
* **MessageMapping**
```
/**
 * 消息处理器，支持处理指定的消息，需要注解到public方法
 *
 * @author fomin
 * @date 2019-12-07
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@MetaAnnotation(MessageResolverImp.class)
public @interface MessageMapping {
    /**
     * 消息类型
     */
    int messageType();
}
```
* **MessageDispatcher**
```
/**
 * 消息调度，扫描初始化MessageController注解类
 *
 * @author fomin
 * @date 2019-12-07
 */
public class MessageDispatcher implements ApplicationListener<ContextRefreshedEvent> {
    @Override
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        if (initialized.compareAndSet(false, true)) {
            init(event.getApplicationContext());
            registerMapping(event.getApplicationContext());
        }
    }
    
    /**
     * 初始化 MessageDispatcher, 从 ApplicationContext 中扫描消息处理器并注册
   */
    private void init(ApplicationContext context) {
        // 扫描标注MessageController的类
        for (Object bean : context.getBeansWithAnnotation(MessageController.class).values()) {
            Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
    
            for (Method method : targetClass.getMethods()) {
                MessageData data = resolveMessageData(method);
                if (data == null) continue;
    
                if (handlers.containsKey(data)) {
                    throw new IllegalStateException("Duplicate handler data: " + data);
                } else {
                    handlers.put(data, new MessageHandler(data, bean, objectStringifier));
                    log.debug("Registered handler: {}", data);
                }
            }
        }
    }
    
    /**
     * 注册方法映射
     */
    private void registerMapping(ApplicationContext context) {
        try {
    
            RequestMappingInfo mapping = RequestMappingInfo.paths(path).build();
            Method method = this.getClass().getMethod("handleApi", HttpServletRequest.class, HttpServletResponse.class);
    
            RequestMappingHandlerMapping handlerMapping = context.getBean(RequestMappingHandlerMapping.class);
            handlerMapping.registerMapping(mapping, this, method);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }
    
    // 执行相关的api请求
    @SuppressWarnings("WeakerAccess")
    public void handleApi(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        try {
            MessageRequest request = messageFactory.parseMessageRequest(httpRequest);
            log.info("[API] Request: {}", request);
    
            ArgumentData argumentData = createArgumentData(httpRequest, httpResponse);
            MessageResponse response = dispatchMessage(request, argumentData);
            log.info("[API] Response: {}", response);
    
            byte[] rawData = response.toByteArray();
            byte[] sentData = gzip(httpRequest, httpResponse, rawData);
            log.info("[API] Total " + sentData.length + " bytes sent, raw " + rawData.length + " bytes.");
    
            httpResponse.setContentLength(sentData.length);
            httpResponse.setContentType(response.getContentType());
            httpResponse.getOutputStream().write(sentData);
            httpResponse.getOutputStream().flush();
    
        } catch (Throwable e) {
            unexpectedException(httpResponse, e);
        }
    }

}
```
上面只展示部分核心代码，具体代码文末会说明，该框架的设计思路其实就是利用注解和反射方式，在spring容器初始化的时候扫描相关注解类，并把该类的相关注解方法扫描保存到Map中，最后定义handlerApi把利用spring-webmvc注册到registerMapping中。
### 时序图
![图片](https://uploader.shimo.im/f/FKMX02bShLAQZ2WY.jpg!thumbnail)

### 使用
需要先定义一个PB的请求类型enum文件,并使用pb工具生产响应PB的java文件。

```
//定义额pb文件
enum PBMessageType {
   DEFAULT_TYPE = 0;
}
// 生成的pb文件
/**
 * Protobuf enum {@code fgame.PBMessageType}
 */
public enum PBMessageType
    implements com.google.protobuf.ProtocolMessageEnum {
  /**
   * <code>DEFAULT_TYPE = 0;</code>
   */
  DEFAULT_TYPE(0),
}
```
创建一个api-server的model，存放相关的controller文件
```
@MessageController
public class HttpController {
    @MessageMapping(messageType = PBMessageType.DEFAULT_TYPE_VALUE)
    public PBString adminLogin(PBWelcome req) {
        return PBWelcome.newBuilder().setValue("Hello word!").build();
    }
}
```
这样就可以打造一个支持pb的http请求了。github地址：[https://github.com/fomin-zhu/Fg-message](https://github.com/fomin-zhu/Fg-message)
