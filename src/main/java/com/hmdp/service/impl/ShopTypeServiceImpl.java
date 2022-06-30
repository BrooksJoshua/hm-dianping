package com.hmdp.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.hmdp.dto.Result;
import com.hmdp.entity.ShopType;
import com.hmdp.mapper.ShopTypeMapper;
import com.hmdp.service.IShopTypeService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.utils.RedisConstants;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Service
public class ShopTypeServiceImpl extends ServiceImpl<ShopTypeMapper, ShopType> implements IShopTypeService {
    @Resource
    ShopTypeMapper shopTypeMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public Result queryTypeList() {
        List<String> range = stringRedisTemplate.opsForList().range(RedisConstants.SHOP_TYPE_LIST_PREFIX, 0, -1);
        List<ShopType> typeList = JSONObject.parseArray(range.toString(),ShopType.class);
        if(range.size()!=0){
            System.out.println("Shop type Cache Hit");
            return Result.ok(typeList);
        }
        System.out.println("Shop type DB Query");
        QueryWrapper wrapper = new QueryWrapper();
        List<ShopType> list = shopTypeMapper.selectList(wrapper);
        String s = JSONArray.toJSONString(list);
        stringRedisTemplate.opsForList().leftPushAll(RedisConstants.SHOP_TYPE_LIST_PREFIX,s.substring(1,s.length()-1));
        return Result.ok(list);
    }
}
