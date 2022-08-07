package cn.hfbin.seckill.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @Date 2022/7/24 19:32
 * @Author zhuzhiwei
 */
@Slf4j
public class JsonUtil {
    private static ObjectMapper objectMapper = new ObjectMapper();

    static{
        //序列化需要配置的

        //对象所有字段全部列入
        objectMapper.setSerializationInclusion(JsonSerialize.Inclusion.ALWAYS);

        //取消默认转换timestamps形式
        objectMapper.configure(SerializationConfig.Feature.WRITE_DATE_KEYS_AS_TIMESTAMPS , false);

        //忽略空Bean转换Json的错误
        objectMapper.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS ,false);

        //所有日期格式都是统一为以下样式，即yyyy-MM-dd HH:mm:ss
        objectMapper.setDateFormat(new SimpleDateFormat(DateTimeUtil.STANDARD_FORMAT));

        //反序列化需要配置的

        //忽视在json字符串中存在，但是在java对象中不存在对应的情况，防止错误
        objectMapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,false) ;
    }


}
