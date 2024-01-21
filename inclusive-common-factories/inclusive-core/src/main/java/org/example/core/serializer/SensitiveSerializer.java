package org.example.core.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.example.core.annotation.Sensitive;
import org.example.core.enums.SensitiveTypeEnum;
import org.example.core.utils.DesensitizedUtils;

import java.io.IOException;
import java.util.Objects;

/**
 * 敏感信息序列化
 *
 * @author wcx
 * @date 2024/1/21
 */
@NoArgsConstructor
@AllArgsConstructor
public class SensitiveSerializer extends JsonSerializer<String> implements ContextualSerializer {

    /**
     * 脱敏类型
     */
    private SensitiveTypeEnum sensitiveTypeEnum;

    /**
     * 前几位不脱敏
     */
    private Integer prefixNoMaskLen;

    /**
     * 最后几位不脱敏
     */
    private Integer suffixNoMaskLen;

    /**
     * 用什么打码
     */
    private String symbol;

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        switch (sensitiveTypeEnum) {
            case CUSTOMER:
                gen.writeString(DesensitizedUtils.desValue(value, prefixNoMaskLen, suffixNoMaskLen, symbol));
                break;
            case NAME:
                gen.writeString(DesensitizedUtils.chineseName(value));
                break;
            case ID_NUM:
                gen.writeString(DesensitizedUtils.idCardNum(value));
                break;
            case PHONE_NUM:
                gen.writeString(DesensitizedUtils.mobilePhone(value));
                break;
            default:
                throw new IllegalArgumentException("unknown sensitive type enum " + sensitiveTypeEnum);
        }
    }

    @Override
    public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) throws JsonMappingException {
        if (property != null) {
            if (Objects.equals(property.getType().getRawClass(), String.class)) {
                Sensitive sensitive = property.getAnnotation(Sensitive.class);
                if (sensitive == null) {
                    sensitive = property.getContextAnnotation(Sensitive.class);
                }
                if (sensitive != null) {
                    return new SensitiveSerializer(sensitive.type(), sensitive.prefixNoMaskLen(),
                            sensitive.suffixNoMaskLen(), sensitive.symbol());
                }
            }
            return prov.findValueSerializer(property.getType(), property);
        }
        return prov.findNullValueSerializer(null);
    }
}
