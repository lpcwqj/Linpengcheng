package com.lpc.controller;

import com.lpc.feign.ItemFeign;
import com.lpc.utils.ControllerUtils;
import commodity.entity.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
public class IndexController{
	private static final String INDEX = "index";
	private static final String ERROR = "error";
	private static final String SHOWMORE = "showmore";
	private static final String SEARCHRESULT = "searchResult";
	@Autowired
	private ItemFeign itemFeign;

	/**
	 * 首页
	 */
	@RequestMapping("/index")
	public String index(HttpServletRequest request, @RequestParam(value = "more",defaultValue = "0")String more) {
		if ("1".equals(more)){
			//说明是点击查看更多
			Map<String, Object> allItems = itemFeign.getAllItems();
			return ControllerUtils.get(request,allItems,SHOWMORE,ERROR);
		}
		// 商城首页 只显示数据库中前两种类型的商品 封装成map 返回给页面
		Map<String, Object> resultMapItem = itemFeign.getItems();
		return ControllerUtils.get(request,resultMapItem,INDEX,ERROR);
	}

	/**
	 * 查看配件
	 */
	@RequestMapping("/moreParts")
	public String moreParts(HttpServletRequest request){
		Map<String, Object> allParts = itemFeign.getAllParts();
		return ControllerUtils.get(request,allParts,SHOWMORE,ERROR);
	}

	/**
	 * 按关键字搜索
	 */
	@RequestMapping("/search")
	public String search(HttpServletRequest request,@RequestParam("keyword")String keywords){
		List<Item> result = itemFeign.search(keywords);
		request.setAttribute("searchList",result);
		return SEARCHRESULT;
	}
}
