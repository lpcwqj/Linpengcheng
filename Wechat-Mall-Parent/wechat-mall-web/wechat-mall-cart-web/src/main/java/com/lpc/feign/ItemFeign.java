package com.lpc.feign;


import commodity.service.api.ItemService;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author Lin
 * @Date 2019/12/23
 */
@FeignClient(name = "commodity-server",fallback = ItemHystrixFallBack.class, path = "/commodity")
public interface ItemFeign extends ItemService {

}
