package com.lwl.social_media_platform.controller;

import com.lwl.social_media_platform.common.BaseContext;
import com.lwl.social_media_platform.common.Result;
import com.lwl.social_media_platform.domain.dto.PageDTO;
import com.lwl.social_media_platform.domain.dto.TreadsDTO;
import com.lwl.social_media_platform.domain.pojo.Support;
import com.lwl.social_media_platform.domain.query.TreadsPageQuery;
import com.lwl.social_media_platform.domain.vo.TreadsVo;
import com.lwl.social_media_platform.service.TreadsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/tread")
public class TreadsController {
    private final TreadsService treadsService;

    @PostMapping
    public Result<String> publish(@RequestBody TreadsDTO treadsDTO){
        return treadsService.publish(treadsDTO);
    }

    @DeleteMapping("/delete")
    public Result<String> deleteTread(@RequestParam("id") Long id){
        return treadsService.deleteTread(id);
    }

    @GetMapping("/get")
    public Result<TreadsVo> getTread(@RequestParam("id") Long id){
        return treadsService.getTread(id);
    }
    @GetMapping("/getByUser")
    public Result<PageDTO<TreadsVo>> getTreadsByUser(TreadsPageQuery treadsPageQuery) throws IOException {
        return treadsService.getTreadByUserId(treadsPageQuery);
    }
    @GetMapping("/get-by-tag")
    public Result<PageDTO<TreadsVo>> getTreadsByTag(TreadsPageQuery treadsPageQuery) throws IOException {
        return treadsService.getTreadByUserTag(treadsPageQuery);
    }

    @PostMapping("/update")
    public Result<String> updateTread(@RequestBody TreadsDTO treadsDTO){
        return treadsService.updateTread(treadsDTO);
    }

    @GetMapping("/list")
    public Result<List<TreadsVo>> getTreadsList(@RequestParam(name="id",required = false) Long userId){
        return Result.error("该接口已过期");
    }

    /**
     * 返回 动态 分页
     * @param treadsPageQuery 分页数据
     * @return TreadsVo 分页
     */
    @GetMapping("/page")
    public Result<PageDTO<TreadsVo>> getTreadsPage(TreadsPageQuery treadsPageQuery){
        return treadsService.getTreadsPage(treadsPageQuery);
    }

    @GetMapping("/my-list")
    public Result<List<TreadsVo>> getMyTreadsList(){
        Long userId = BaseContext.getCurrentId();
        return getTreadsList(userId);
    }

    @PostMapping("/support")
    public Result<String> support(@RequestBody Support support){
        return treadsService.support(support);
    }

    @PostMapping("/cancel")
    public Result<String> cancelSupport(@RequestBody Support support){
        return treadsService.cancelSupport(support);
    }

    @GetMapping("/current-hot-treads")
    public Result<List<TreadsVo>> getCurrentHotTreads(){
        return Result.success(treadsService.getCurrentHotTreads());
    }

    @GetMapping("/get-concentration-list")
    public Result<PageDTO<TreadsVo>> getConcentrationTreads(TreadsPageQuery treadsPageQuery){
        Long userId = BaseContext.getCurrentId();
        treadsPageQuery.setUserId(userId);
        return treadsService.getConcentrationTreads(treadsPageQuery);
    }
}
