package com.lwl.social_media_platform.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lwl.social_media_platform.common.Result;
import com.lwl.social_media_platform.domain.dto.PageDTO;
import com.lwl.social_media_platform.domain.pojo.Support;
import com.lwl.social_media_platform.domain.pojo.Treads;
import com.lwl.social_media_platform.domain.dto.TreadsDTO;
import com.lwl.social_media_platform.domain.query.TreadsPageQuery;
import com.lwl.social_media_platform.domain.vo.TreadsVo;

import java.io.IOException;
import java.util.List;

public interface TreadsService extends IService<Treads> {
    Result<String> publish(TreadsDTO treadsDTO);

    Result<String> deleteTread(Long id);

    Result<TreadsVo> getTread(Long id);

    Result<PageDTO<TreadsVo>> getTreadByUserId(TreadsPageQuery treadsPageQuery) throws IOException;

    @Deprecated
    Result<List<TreadsVo>> getTreadsList(Long userId);

    Result<PageDTO<TreadsVo>> getTreadsPage(TreadsPageQuery treadsPageQuery);

    Result<String> updateTread(TreadsDTO treadsDTO);

    Result<String> support(Support support);

    Result<String> cancelSupport(Support support);

    List<TreadsVo> getCurrentHotTreads();

    Result<PageDTO<TreadsVo>> getConcentrationTreads(TreadsPageQuery treadsPageQuery);

    Result<PageDTO<TreadsVo>> getTreadByUserTag(TreadsPageQuery treadsPageQuery);
}
