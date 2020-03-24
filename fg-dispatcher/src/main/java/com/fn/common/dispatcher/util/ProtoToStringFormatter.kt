package com.fn.common.dispatcher.util

import com.google.protobuf.ByteString
import com.google.protobuf.Descriptors
import com.google.protobuf.ExtensionRegistry
import com.google.protobuf.Message
import com.google.protobuf.UnknownFieldSet
import com.googlecode.protobuf.format.AbstractCharBasedFormatter

/**
 * Created by vince on Feb 18, 2019.
 */
open class ProtoToStringFormatter : AbstractCharBasedFormatter() {

    /**
     * 将 proto 对象格式化成美观的字符串，打印到 Appendable 对象中
     */
    override fun print(message: Message?, output: Appendable) {
        if (message == null) {
            output.append("null")

        } else {
            val descriptor = message.descriptorForType
            output.append(descriptor.name).append("{")

            descriptor.fields
                .filterNot { ignoreField(message, it) }
                .forEachIndexed { i, field ->
                    if (i > 0) output.append(", ")
                    output.append(field.name).append("=")
                    printFieldValue(message, field, output)
                }

            if (message.unknownFields.asMap().isNotEmpty()) {
                output.append(", ")
                printUnknownFields(message.unknownFields, output)
            }

            output.append("}")
        }
    }

    /**
     * 输出未知字段
     */
    override fun print(fields: UnknownFieldSet, output: Appendable) {
        output.append("{")
        printUnknownFields(fields, output)
        output.append("}")
    }

    /**
     * 是否忽略 proto 对象中的指定字段，父类实现默认会忽略空字段，子类可覆盖此方法
     */
    protected open fun ignoreField(message: Message, field: Descriptors.FieldDescriptor): Boolean {
        return when {
            field.isRepeated -> message.getRepeatedFieldCount(field) == 0
            else -> !message.hasField(field)
        }
    }
    
    /**
     * 将 proto 对象中指定字段的值格式化成美观的字符串，打印到 Appendable 对象中
     * 子类可覆盖此方法，定制具体字段的格式化方式
     */
    protected open fun printFieldValue(message: Message, field: Descriptors.FieldDescriptor, output: Appendable) {
        printValue(message.getField(field), output)
    }

    /**
     * 输出具体值
     */
    protected fun printValue(value: Any?, output: Appendable) {
        when (value) {
            is ByteString -> {
                output.append("[total ").append(value.size().toString()).append(" bytes]")
            }
            is Message -> {
                print(value, output)
            }
            is List<*> -> {
                output.append("[")
                for ((i, item) in value.withIndex()) {
                    if (i > 0) output.append(", ")
                    printValue(item, output)
                }
                output.append("]")
            }
            is Map<*, *> -> {
                output.append("{")
                value.entries.forEachIndexed { i, (key, value) ->
                    if (i > 0) output.append(", ")
                    output.append(key.toString()).append("=")
                    printValue(value, output)
                }
                output.append("}")
            }
            is String -> {
                output.append(value.replace("\n", "\\n"))
            }
            else -> {
                output.append(value.toString())
            }
        }
    }

    /**
     * 输出未知字段
     */
    protected open fun printUnknownFields(fields: UnknownFieldSet, output: Appendable) {
        fields.asMap().entries.forEachIndexed { i, (key, _) ->
            if (i > 0) output.append(", ")
            output.append(key.toString()).append("=[unknown value]")
        }
    }

    override fun merge(input: CharSequence, extensionRegistry: ExtensionRegistry, builder: Message.Builder) {
        throw UnsupportedOperationException("Unsupported method 'merge', ProtobufToStringFormatter is just designed to format protobuf messages to strings.")
    }
}