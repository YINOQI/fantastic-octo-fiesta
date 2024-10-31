package com.lwl.social_media_platform;

import cn.hutool.json.JSONUtil;
import com.lwl.social_media_platform.common.Result;
import com.lwl.social_media_platform.domain.dto.PageDTO;
import com.lwl.social_media_platform.domain.pojo.ChatMessage;
import com.lwl.social_media_platform.domain.query.TreadsPageQuery;
import com.lwl.social_media_platform.domain.vo.TreadsVo;
import com.lwl.social_media_platform.service.ChatMessageService;
import com.lwl.social_media_platform.service.TreadsService;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.List;

@SpringBootTest
class AppTests {
    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Autowired
    private TreadsService treadsService;

    @Autowired
    private ChatMessageService chatMessageService;
    public static final String MAPPING_TEMPLATE = """
             {
                 "mappings": {
                     "properties": {
                         "id": {
                             "type": "long"
                         },
                         "userId": {
                             "type": "long"
                         },
                         "content": {
                             "type": "text",
                             "analyzer": "ik_max_word"
                         },
                         "state": {
                             "type": "keyword",
                             "index": false
                         },
                         "createTime": {
                             "type": "date",
                             "index": false
                         },
                         "tagList": {
                             "type": "nested",
                             "properties": {
                                 "id": {
                                     "type": "keyword",
                                     "index": false
                                 },
                                 "name": {
                                     "type": "keyword"
                                 }
                             }
                         },
                         "imageList": {
                             "type": "nested",
                             "properties": {
                                 "id": {
                                     "type": "long",
                                     "index": false
                                 },
                                 "url": {
                                     "type": "text",
                                     "index": false
                                 },
                                 "treadsId": {
                                     "type": "long",
                                     "index": false
                                 }
                             }
                         },
                         "supportNum": {
                             "type": "long"
                         },
                         "pic": {
                             "type": "text",
                             "index": false
                         },
                         "nickName": {
                             "type": "text",
                             "analyzer": "ik_max_word"
                         }
                     }
                 }
             }
            """;

    @Test
    void contextLoads() {
        CreateIndexRequest createIndexRequest = new CreateIndexRequest("treads-vo");
        createIndexRequest.source(MAPPING_TEMPLATE, XContentType.JSON);
        try {
            restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void addTreadsToES() throws IOException {
        TreadsPageQuery treadsPageQuery = new TreadsPageQuery();
        treadsPageQuery.setPageSize(100);
        Result<PageDTO<TreadsVo>> treadsPage = treadsService.getTreadsPage(treadsPageQuery);
        List<TreadsVo> list = treadsPage.getData().getList();
        BulkRequest request = new BulkRequest();
        list.forEach(treads -> request.add(new IndexRequest("treads-vo")
                .id(treads.getId().toString())
                .source(JSONUtil.toJsonStr(treads),XContentType.JSON)));
        restHighLevelClient.bulk(request,RequestOptions.DEFAULT);
    }

    @Test
    void addDocTest(){

    }

}
