package com.lwl.social_media_platform.mq;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.json.JSONUtil;
import com.lwl.social_media_platform.domain.dto.TreadsDTO;
import com.lwl.social_media_platform.domain.pojo.Image;
import com.lwl.social_media_platform.domain.pojo.Tag;
import com.lwl.social_media_platform.domain.pojo.TreadsTag;
import com.lwl.social_media_platform.domain.pojo.User;
import com.lwl.social_media_platform.domain.vo.TreadsVo;
import com.lwl.social_media_platform.service.TagService;
import com.lwl.social_media_platform.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
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
@RocketMQMessageListener(consumerGroup = "${rocketmq.consumer_insert.group}",
        topic = "${rocketmq.producer.topic}",
        selectorExpression = "${rocketmq.consumer_insert.selectorExpression}")
public class TreadsConsumer implements RocketMQListener<Map<String, String>> {
    private final RestHighLevelClient restHighLevelClient;
    private final TagService tagService;
    private final UserService userService;

    @Override
    public void onMessage(Map<String, String> treadsMap) {
        String treadsJSON = treadsMap.get("treadsDtoJSON");
        String treadsVoJson = setTreadsVoJSON(treadsJSON);
        Long id = (Long) JSONUtil.parseObj(treadsJSON).get("id");
        String idStr = id.toString();
        log.info("消费者获取tread为{}", treadsJSON);
        IndexRequest request = new IndexRequest("treads-vo").source(treadsVoJson, XContentType.JSON).id(idStr);
        try {
            log.info("treadsMap为{}",treadsVoJson);
            IndexResponse indexResponse = restHighLevelClient.index(request, RequestOptions.DEFAULT);
            log.info("队列异步添加文档结果为:{}", indexResponse.getShardInfo().status().toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String setTreadsVoJSON(String treadsDTOJSON) {
        TreadsDTO treadsDTO = JSONUtil.toBean(treadsDTOJSON, TreadsDTO.class);

        User user = userService.getById(treadsDTO.getUserId());

        List<Image> imageList = treadsDTO.getImageList();

        // 获取动态的标签的id
        List<TreadsTag> treadsTagList = treadsDTO.getTreadsTagList();
        List<Long> tagIdList = treadsTagList.stream().map(TreadsTag::getTagId).toList();
        List<Tag> tagList;
        // 根据id获取标签内容
        if (CollUtil.isNotEmpty(tagIdList)) {
            tagList = tagService.listByIds(tagIdList);
        } else {
            tagList = Collections.emptyList();
        }
        TreadsVo treadsVo = BeanUtil.copyProperties(treadsDTO, TreadsVo.class);
        treadsVo.setNickName(user.getNickname())
                .setPic(user.getPic())
                .setImageList(imageList)
                .setTagList(tagList)
                .setSupportNum(0L);

        return JSONUtil.toJsonStr(treadsVo);
    }
}
