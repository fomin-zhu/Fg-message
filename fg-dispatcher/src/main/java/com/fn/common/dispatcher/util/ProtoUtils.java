package com.fn.common.dispatcher.util;

import com.fn.common.code.Charsets;
import com.fn.common.proto.http.PBDoubleValue;
import com.fn.common.proto.http.PBFloatValue;
import com.fn.common.proto.http.PBSIntValue;
import com.fn.common.proto.http.PBSLongValue;
import com.fn.common.proto.http.PBStrValue;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * @author fomin
 * @date 2019-12-07
 */
@Slf4j
public class ProtoUtils {

    private static final byte[] NULL_DATA = "com.fn.common.null-data".getBytes(Charsets.UTF_8);
    private static final WeakHashMap<Class<?>, Message> defaultInstancesCache = new WeakHashMap<>();


    @SuppressWarnings("unchecked")
    public static <T> Optional<T> parseFrom(byte[] bytes, Class<T> messageClass) {
        Message message = defaultInstancesCache.computeIfAbsent(messageClass, aClass -> {
            try {
                return (Message) messageClass.getDeclaredMethod("getDefaultInstance").invoke(null);
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                e.printStackTrace();
            }
            return null;
        });
        if (message == null) return Optional.empty();
        try {
            return Optional.of(messageClass.cast(message.getParserForType().parseFrom(bytes)));
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    public static <T extends ProtocolMessageEnum> Optional<T> parseEnum(int value, Class<T> enumClass) {
        try {
            Method method = enumClass.getMethod("forNumber", Integer.class);
            return Optional.of(enumClass.cast(method.invoke(null, value)));
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private static Type resolveGenericType(AnnotatedElement annotatedElement) {
        // 获取方法参数类型
        if (annotatedElement instanceof Parameter) {
            return ((Parameter) annotatedElement).getParameterizedType();
        }
        // 获取方法返回值类型
        if (annotatedElement instanceof Method) {
            return ((Method) annotatedElement).getGenericReturnType();
        }
        throw new AssertionError("Unexpected element: " + annotatedElement);
    }

    /**
     * 从字节数组中解析出对象
     */
    public static Object deserialize(byte[] data, AnnotatedElement annotatedElement) {
        if (Arrays.equals(data, NULL_DATA)) return null;

        Type genericType = resolveGenericType(annotatedElement);

        if (genericType instanceof Class) {
            Class<?> cls = (Class<?>) genericType;

            // 纯字节数据，不作处理，直接返回
            if (cls == byte[].class) {
                return data;
            }
            if (cls == ByteString.class) {
                return ByteString.copyFrom(data);
            }

            // pb 类型，直接解析
            if (Message.class.isAssignableFrom(cls)) {
                return parseFrom(data, cls.asSubclass(Message.class)).orElse(null);
            }
            // pb 枚举，解析为 PBSIntValue，再转为枚举对象
            if (Enum.class.isAssignableFrom(cls) && ProtocolMessageEnum.class.isAssignableFrom(cls)) {
                int value = parseFrom(data, PBSIntValue.class).orElse(PBSIntValue.getDefaultInstance()).getValue();
                return parseEnum(value, cls.asSubclass(Enum.class));
            }

            // 基本类型，解析为 PBSIntValue, PBStrValue 等 pb 包装对象，再从中取值
            if (cls == int.class || cls == Integer.class) {
                return parseFrom(data, PBSIntValue.class).orElse(PBSIntValue.getDefaultInstance()).getValue();
            }
            if (cls == long.class || cls == Long.class) {
                return parseFrom(data, PBSLongValue.class).orElse(PBSLongValue.getDefaultInstance()).getValue();
            }
            if (cls == float.class || cls == Float.class) {
                return parseFrom(data, PBFloatValue.class).orElse(PBFloatValue.getDefaultInstance()).getValue();
            }
            if (cls == double.class || cls == Double.class) {
                return parseFrom(data, PBDoubleValue.class).orElse(PBDoubleValue.getDefaultInstance()).getValue();
            }
            if (cls == String.class) {
                return parseFrom(data, PBStrValue.class).orElse(PBStrValue.getDefaultInstance()).getValue();
            }
        }
        return new Object();
    }

    /**
     * 将对象序列化为字节数组
     */
    public static byte[] serialize(Object o) {
        if (o == null) return NULL_DATA.clone();

        // 纯字节数据，不作处理，直接返回
        if (o instanceof byte[]) {
            return (byte[]) o;
        }
        if (o instanceof ByteString) {
            return ((ByteString) o).toByteArray();
        }

        // pb 类型，使用 pb 序列化
        if (o instanceof Message) {
            Message message = ((Message) o);
            log.info("serialize:{} ", ProtoUtils.toString(message));
            return message.toByteArray();
        }
        // pb 枚举，包装成 PBSIntValue，再使用 pb 序列化
        if (o instanceof Enum && o instanceof ProtocolMessageEnum) {
            return PBSIntValue.newBuilder().setValue(((ProtocolMessageEnum) o).getNumber()).build().toByteArray();
        }

        // 基本类型，包装为 PBSIntValue, PBStrValue 等 pb 对象，再使用 pb 序列化
        if (o instanceof Integer) {
            return PBSIntValue.newBuilder().setValue((Integer) o).build().toByteArray();
        }
        if (o instanceof Long) {
            return PBSLongValue.newBuilder().setValue((Long) o).build().toByteArray();
        }
        if (o instanceof Float) {
            return PBFloatValue.newBuilder().setValue((Float) o).build().toByteArray();
        }
        if (o instanceof Double) {
            return PBDoubleValue.newBuilder().setValue((Double) o).build().toByteArray();
        }
        if (o instanceof String) {
            return PBStrValue.newBuilder().setValue(o.toString()).build().toByteArray();
        }
        return new byte[0];
    }

    public static String toString(Message msg) {
        StringBuilder builder = new StringBuilder();
        ProtoToStringFormatter formatter = new ProtoToStringFormatter();
        formatter.setDefaultCharset(Charsets.UTF_8);
        formatter.print(msg, builder);
        return builder.toString();
    }
}
