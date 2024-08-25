package com.lwl.social_media_platform.mq;

import cn.hutool.json.JSONUtil;
import com.lwl.social_media_platform.domain.dto.TreadsDTO;
import com.lwl.social_media_platform.domain.pojo.Tag;
import com.lwl.social_media_platform.domain.pojo.TreadsTag;
import com.lwl.social_media_platform.domain.vo.TreadsVo;
import com.lwl.social_media_platform.service.TagService;
import com.lwl.social_media_platform.utils.BeanUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(consumerGroup = "${rocketmq.consumer_update.group}",
        topic = "${rocketmq.producer.topic}",
        selectorExpression = "${rocketmq.consumer_update.selectorExpression}")
public class TreadsUpdateConsumer implements RocketMQListener<Map<String, String>> {
    private final TagService tagService;
    private final RestHighLevelClient restHighLevelClient;

    @Override
    public void onMessage(Map<String, String> treadsMap) {
        String treadsDtoJSON = treadsMap.get("treadsDtoJSON");
        TreadsDTO treadsDTO = JSONUtil.toBean(treadsDtoJSON, TreadsDTO.class);

        List<TreadsTag> treadsTagList = treadsDTO.getTreadsTagList();
        List<Tag> tagList;
        if (treadsTagList != null) {
            tagList = tagService.listByIds(treadsTagList.stream().map(TreadsTag::getTagId).toList());
        } else {
            tagList = Collections.emptyList();
        }

        TreadsVo treadsVo = BeanUtils.copyProperties(treadsDTO, TreadsVo.class);
        treadsVo.setTagList(tagList);

        Map<String, Object> stringObjectMap = JSONUtil.parseObj(treadsVo).toBean(Map.class);

//        Map<String, Object> stringObjectMap = BeanUtils.beanToMap(treadsVo, new HashMap<>(),
//                CopyOptions.create()
//                        .setIgnoreNullValue(true)
//                        .setFieldValueEditor((field, value) -> String.valueOf(value))
//        );

        UpdateRequest updateRequest = new UpdateRequest("treads-vo", treadsDTO.getId().toString());
        updateRequest.doc(stringObjectMap, XContentType.JSON);

        try {
            UpdateResponse update = restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
            log.info("es更新文档结果为{}", update.toString());
        } catch (
                IOException e) {
            throw new RuntimeException(e);
        }
    }
}
