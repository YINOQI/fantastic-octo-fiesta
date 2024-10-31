package com.lwl.social_media_platform.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lwl.social_media_platform.common.BaseContext;
import com.lwl.social_media_platform.common.Result;
import com.lwl.social_media_platform.common.exception.ServiceException;
import com.lwl.social_media_platform.domain.dto.PageDTO;
import com.lwl.social_media_platform.domain.dto.TreadsDTO;
import com.lwl.social_media_platform.domain.pojo.*;
import com.lwl.social_media_platform.domain.query.TreadsPageQuery;
import com.lwl.social_media_platform.domain.vo.TreadsVo;
import com.lwl.social_media_platform.mapper.TreadsMapper;
import com.lwl.social_media_platform.mq.TreadsProducer;
import com.lwl.social_media_platform.service.*;
import com.lwl.social_media_platform.utils.BeanUtils;
import com.lwl.social_media_platform.utils.ESClientUtil;
import com.lwl.social_media_platform.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.lwl.social_media_platform.utils.RedisConstant.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class TreadsServiceImpl extends ServiceImpl<TreadsMapper, Treads> implements TreadsService {
    private final TagService tagService;
    private final TreadsTagService treadsTagService;
    private final ImageService imageService;
    private final ConcentrationService concentrationService;
    private final SupportService supportService;
    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ESClientUtil esClientUtil;
    private final TreadsProducer treadsProducer;
    private final RestHighLevelClient restHighLevelClient;
    private final RedissonClient redissonClient;

    @Override
    @Transactional
    public Result<String> publish(TreadsDTO treadsDTO) {
        Long userId = BaseContext.getCurrentId();
//        RLock lock = redissonClient.getLock(TREADS_ADD_KEY + userId.toString());
//        try {
//            boolean isLock = lock.tryLock(1, TimeUnit.SECONDS);
//            if (!isLock) {
//                throw new ServiceException("请勿多次点击");
//            }
            treadsDTO.setContent(
                    treadsDTO.getContent()
                            .replace("\n", "<br/>")
                            .replace("\r", "")
            );

            treadsDTO.setUserId(userId);
            treadsDTO.setCreateTime(LocalDateTime.now());
            treadsDTO.setSupportNum(0L);

            // 保存动态
            this.save(treadsDTO);

            Long treadsId = treadsDTO.getId();

            // 为 tag 设置动态id
            List<TreadsTag> treadsTagList = treadsDTO.getTreadsTagList();
            if (CollUtil.isNotEmpty(treadsTagList)) {
                treadsTagList.forEach(item -> item.setTreadsId(treadsId));
                // 保存标签
                treadsTagService.saveBatch(treadsTagList);
            }

            // 为 图片列表 设置动态id
            List<Image> imageList = treadsDTO.getImageList();
            if (CollUtil.isNotEmpty(imageList)) {
                imageList.forEach(item -> item.setTreadsId(treadsId));
                // 保存图片
                imageService.saveBatch(imageList);
            }

            treadsProducer.sendTreadMessage(JSONUtil.toJsonStr(treadsDTO));
//            treadsProducer.sendTreadsToFollowMessage(JSONUtil.toJsonStr(treadsDTO));

            return Result.success("发布成功");
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        } finally {
//            lock.unlock();
//        }
    }

    @Override
    @Transactional
    public Result<String> deleteTread(Long id) {
        // 删除动态
        this.removeById(id);

        // 删除动态相关标签
        treadsTagService.lambdaUpdate().eq(TreadsTag::getTreadsId, id).remove();
        // 删除动态相关图片
        imageService.lambdaUpdate().eq(Image::getTreadsId, id).remove();
        // 从es中删除该动态的文档
        esClientUtil.deleteDoc("treads-vo", id.toString());

        return Result.success("删除成功");
    }

    @Override
    public Result<TreadsVo> getTread(Long id) {
        Long userId = BaseContext.getCurrentId();

        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(TREADS_VO_KEY + id);

        if (!entries.isEmpty()) {
            if (entries.containsKey("nullTread")) {
                return Result.error("该动态不存在");
            }
            TreadsVo treadsVo = BeanUtils.fillBeanWithMap(entries, new TreadsVo(), false,
                    CopyOptions.create().setIgnoreError(true));

            List<Image> imageList = JSONUtil.toList((String)entries.get("imageList"),Image.class);
            List<Tag> tagList = JSONUtil.toList((String)entries.get("tagList"),Tag.class);
            treadsVo.setImageList(imageList).setTagList(tagList);

            return Result.success(treadsVo);
        }

        RLock lock = redissonClient.getLock(TREADS_LOCK_KEY + userId);
        boolean isLock = lock.tryLock();

        if (!isLock) {
            throw new ServiceException("请勿重复点击");
        }

        try {
            Treads treads = this.getById(id);
            if (treads == null) {
                stringRedisTemplate.opsForHash().put(TREADS_VO_KEY + id, "nullTread", "-");
                stringRedisTemplate.expire(TREADS_VO_KEY + id, 1, TimeUnit.MINUTES);
                return Result.error("该动态不存在");
            }

            TreadsVo treadsVo = getTreadsVo(treads);


            Map<String, Object> stringObjectMap = BeanUtils.beanToMap(treadsVo, new HashMap<>(),
                    CopyOptions.create()
                            .setIgnoreNullValue(true)
                            .setFieldValueEditor((name, value) -> {
                                if (name.equals("imageList") || name.equals("tagList")){
                                    return JSONUtil.toJsonStr(value);
                                }else {
                                    return value.toString();
                                }
                            }));

            stringRedisTemplate.opsForHash().putAll(TREADS_VO_KEY + treadsVo.getId(), stringObjectMap);

            return Result.success(treadsVo);// 调用 getTreadsVo 方法 返回 TreadsVo
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Result<PageDTO<TreadsVo>> getTreadByUserId(TreadsPageQuery treadsPageQuery) throws IOException {
        Long userId = BaseContext.getCurrentId();

        // 构造查询条件
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                // 起始页
                .from(treadsPageQuery.getPageNo())
                // 每页数量
                .size(treadsPageQuery.getPageSize())
                // 指定查询用户id字段
                .query(QueryBuilders.matchQuery("userId", treadsPageQuery.getUserId().toString()))
                // 排序字段
                .sort("createTime", SortOrder.DESC);
        SearchRequest searchRequest = new SearchRequest("treads-vo").source(searchSourceBuilder);

        // 聚合查询
        SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);

        // 转化为treadsVo
        List<TreadsVo> treadsVoList = new ArrayList<>();
        for (SearchHit hit :
                searchResponse.getHits().getHits()) {
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();

            TreadsVo treadsVo = BeanUtils.mapToBean(sourceAsMap, TreadsVo.class, true, CopyOptions.create());
            // 获取动态作者id
            long toUserId = treadsVo.getUserId();
            // 是否关注
            boolean concentration;
            if (stringRedisTemplate.opsForZSet().score(FOLLOW_LIST_KEY + toUserId, userId.toString()) != null) {
                concentration = true;
            } else {
                concentration = concentrationService.lambdaQuery()
                        .eq(Concentration::getUserId, userId)
                        .eq(Concentration::getToUserId, toUserId)
                        .exists();
            }

            // 动态id
            Long treadsVoId = treadsVo.getId();

            LambdaQueryWrapper<Support> supportLambdaQueryWrapper = new LambdaQueryWrapper<>();
            // 获取点赞数
            Long score = stringRedisTemplate.opsForZSet().size(SUPPORT_KEY + treadsVoId);
            long supportNum;
            supportNum = Objects.requireNonNullElseGet(score, () -> supportService.count(supportLambdaQueryWrapper.eq(Support::getTreadsId, treadsVoId)));
            // 是否点赞
            boolean isSupport = supportService.exists(supportLambdaQueryWrapper.eq(Support::getTreadsId, treadsVoId).eq(Support::getUserId, userId));

            // 封装
            treadsVo.setIsFollow(concentration)
                    .setIsSupport(isSupport)
                    .setSupportNum(supportNum);

            treadsVoList.add(treadsVo);
        }

        PageDTO<TreadsVo> treadsVoPageDTO = new PageDTO<>();
        treadsVoPageDTO.setList(treadsVoList)
                .setPages(Integer.toUnsignedLong(treadsPageQuery.getPageSize()))
                .setTotal(searchResponse.getHits().getTotalHits().value);

        return Result.success(treadsVoPageDTO);
    }

    @Override
    @Deprecated
    public Result<List<TreadsVo>> getTreadsList(Long userId) {
        return Result.error("该接口已过期");
    }


    @Override
    public Result<PageDTO<TreadsVo>> getTreadsPage(TreadsPageQuery treadsPageQuery) {
        Page<Treads> treadsPage = this.lambdaQuery()
                .like(StrUtil.isNotEmpty(treadsPageQuery.getKey()), Treads::getContent, treadsPageQuery.getKey())
                .page(treadsPageQuery.toMpPageDefaultSortByCreateTimeDesc());

        List<Treads> records = treadsPage.getRecords();
        List<TreadsVo> treadsVos = records.stream()
                .map(this::getTreadsVo)
                .toList();

        return Result.success(PageUtils.of(treadsPage, treadsVos));
    }

    @Override
    @Transactional
    public Result<String> updateTread(TreadsDTO treadsDTO) {

        Long treadsId = treadsDTO.getId();

        // 更新动态内容
        this.lambdaUpdate().eq(Treads::getId, treadsId).update(treadsDTO);

        // 删除该动态的标签
        treadsTagService.lambdaUpdate().eq(TreadsTag::getTreadsId, treadsId).remove();

        // 获取该动态的新标签
        List<TreadsTag> treadsTagList = treadsDTO.getTreadsTagList();
        if (CollUtil.isNotEmpty(treadsTagList)) {
            // 设置动态id
            treadsTagList.forEach(item -> item.setTreadsId(treadsId));

            // 保存新标签
            treadsTagService.saveBatch(treadsTagList);
        }
        treadsProducer.sendTreadUpdateMessage(treadsDTO);
        return Result.success("更新成功");
    }


    @Override
    @Transactional
    public Result<String> support(Support support) {
        Double isSupport = stringRedisTemplate.opsForZSet().score(SUPPORT_SCHEDULER_KEY + support.getTreadsId(), support.getUserId().toString());

        if (isSupport != null) {
            boolean exists = supportService.lambdaQuery()
                    .eq(Support::getUserId, support.getUserId())
                    .eq(Support::getTreadsId, support.getTreadsId())
                    .exists();
            if (exists) {
                throw new ServiceException("已经点过赞啦!");
            }
        }

        support.setIsCancel(1);

        stringRedisTemplate.opsForHash().put(SUPPORT_KEY + support.getTreadsId(), support.getUserId().toString(), JSONUtil.toJsonStr(support));
        stringRedisTemplate.opsForZSet().add(SUPPORT_SCHEDULER_KEY + support.getTreadsId(), support.getUserId().toString(), System.currentTimeMillis());
        Long supportNum = stringRedisTemplate.opsForZSet().zCard(SUPPORT_SCHEDULER_KEY + support.getTreadsId());
        if (supportNum != null) {
            stringRedisTemplate.opsForZSet().add(SUPPORT_SCHEDULER_TREAD_KEY, support.getTreadsId().toString(), supportNum);
        }

        return Result.success("点赞成功");
    }

    @Override
    @Transactional
    public Result<String> cancelSupport(Support support) {
        support.setIsCancel(0);

        stringRedisTemplate.opsForHash().put(SUPPORT_KEY + support.getTreadsId(), support.getUserId().toString(), JSONUtil.toJsonStr(support));
        stringRedisTemplate.opsForZSet().remove(SUPPORT_SCHEDULER_KEY + support.getTreadsId(), support.getUserId().toString());
        stringRedisTemplate.opsForZSet().incrementScore(SUPPORT_SCHEDULER_TREAD_KEY, support.getTreadsId().toString(), -1);

        return Result.success("取消点赞成功");
    }

    @Override
    public List<TreadsVo> getCurrentHotTreads() {
        long timestamp = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000;
        LocalDateTime localDateTimeBefore = Instant.ofEpochMilli(timestamp).atZone(ZoneOffset.ofHours(8)).toLocalDateTime();

        List<Treads> treadsList = this.lambdaQuery()
                .le(Treads::getCreateTime, LocalDateTime.now())
                .ge(Treads::getCreateTime, localDateTimeBefore)
                .list();

        Map<Long, Long> currentMaxSupportNum = supportService.getCurrentMaxSupportNum(treadsList.stream().map(Treads::getId).toList());

        List<TreadsVo> treadsVos = new ArrayList<>();

        treadsList.forEach(item -> {
            Long supportNum = currentMaxSupportNum.get(item.getId());
            if (supportNum != null) {
                TreadsVo treadsVo = BeanUtils.copyProperties(item, TreadsVo.class);
                treadsVo.setSupportNum(supportNum);
                treadsVos.add(treadsVo);
            }
        });
        return treadsVos;
    }

    /**
     * 将 组合 TreadsVo 抽象出为一个方法
     *
     * @param treads 动态
     * @return treadsVo
     */
    private TreadsVo getTreadsVo(Treads treads) {
        Long userId = BaseContext.getCurrentId();

        // 获取该动态的标签id
        Long id = treads.getId();
        List<TreadsTag> treadsTags = treadsTagService.lambdaQuery().eq(TreadsTag::getTreadsId, id).list();
        // 取出标签id
        List<Long> tagsId = treadsTags.stream().map(TreadsTag::getTagId).toList();
        List<Tag> tags;
        // 根据id获取标签内容
        if (CollUtil.isNotEmpty(tagsId)) {
            tags = tagService.listByIds(tagsId);
        } else {
            tags = Collections.emptyList();
        }

        // 获取图片url
        List<Image> imageList = imageService.lambdaQuery().eq(Image::getTreadsId, id).list();

        // 获取动态作者id
        long toUserId = treads.getUserId();
        // 是否关注
        boolean concentration = concentrationService.lambdaQuery()
                .eq(userId != null, Concentration::getUserId, userId)
                .eq(userId != null, Concentration::getToUserId, toUserId)
                .exists();

        // 获取动态作者
        User user = userService.getById(toUserId);

        LambdaQueryWrapper<Support> supportLambdaQueryWrapper = new LambdaQueryWrapper<>();
        // 获取点赞数
        long supportNum = supportService.count(supportLambdaQueryWrapper.eq(Support::getTreadsId, id));
        // 是否点赞
        boolean isSupport = supportService.exists(supportLambdaQueryWrapper.eq(Support::getTreadsId, id).eq(Support::getUserId, userId));

        // 转换为vo
        TreadsVo treadsVo = BeanUtil.copyProperties(treads, TreadsVo.class);
        // 设置标签 图片url 是否关注 点赞数 已点赞
        treadsVo.setTagList(tags)
                .setImageList(imageList)
                .setIsFollow(concentration)
                .setNickName(user.getUsername())
                .setPic(user.getPic())
                .setIsSupport(isSupport)
                .setSupportNum(supportNum);

        return treadsVo;
    }

}
