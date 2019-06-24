package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Criteria;
import org.springframework.data.solr.core.query.FilterQuery;
import org.springframework.data.solr.core.query.GroupOptions;
import org.springframework.data.solr.core.query.HighlightOptions;
import org.springframework.data.solr.core.query.HighlightQuery;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleFilterQuery;
import org.springframework.data.solr.core.query.SimpleHighlightQuery;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.data.solr.core.query.SolrDataQuery;
import org.springframework.data.solr.core.query.result.GroupEntry;
import org.springframework.data.solr.core.query.result.GroupPage;
import org.springframework.data.solr.core.query.result.GroupResult;
import org.springframework.data.solr.core.query.result.HighlightEntry;
import org.springframework.data.solr.core.query.result.HighlightEntry.Highlight;
import org.springframework.data.solr.core.query.result.HighlightPage;
import org.springframework.data.solr.core.query.result.ScoredPage;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;

@Service(timeout=5000)
public class ItemSearchServiceImpl implements ItemSearchService{

	@Autowired
	private SolrTemplate solrTemplate;
	
	public Map search(Map searchMap) {
		Map map = new HashMap<>();
		
		//空格处理
		String keywords = (String) searchMap.get("keywords");
		searchMap.put("keywords", keywords.replace(" ", ""));//关键字去掉空格
		
		//1.查询列表
		map.putAll(searchList(searchMap));
		//2.分组查询商品分类列表
		List<String> categoryList = searchCategoryList(searchMap);
		map.put("categoryList",categoryList);
		//3.查询品牌和规格列表
		String category = (String) searchMap.get("category");
		if (!category.equals("")) {
			map.putAll(searchBrandAndSpecList(category));
		}else {
			if (categoryList.size()>0) {
				map.putAll(searchBrandAndSpecList(categoryList.get(0)));
			}
		}
		return map;
	}

	//查询列表
	private Map searchList(Map searchMap) {
		
		Map map = new HashMap<>();
		//高亮选项初始化
		HighlightQuery query = new SimpleHighlightQuery();
		HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");
		highlightOptions.setSimplePrefix("<em style='color:red'>");
		highlightOptions.setSimplePostfix("</em>");
		query.setHighlightOptions(highlightOptions);//为查询对象设置高亮选项
		
		//1.1关键字查询
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//1.2按照商品分类过滤
		if (!"".equals(searchMap.get("category"))) {
			//如果用户选择了分类
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		//1.3按照品牌过滤
		if (!"".equals(searchMap.get("brand"))) {
			//如果用户选择了分类
			FilterQuery filterQuery = new SimpleFilterQuery();
			Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));
			filterQuery.addCriteria(filterCriteria);
			query.addFilterQuery(filterQuery);
		}
		
		
		//1,4按规格过滤
		if (searchMap.get("spec")!=null) {
			
			Map<String, String> specMap = (Map<String, String>) searchMap.get("spec");
			for(String key:specMap.keySet()) {
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_spec_"+key).is(specMap.get(key));
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
			}
		}
		//1.5按价格过滤
		if (!"".equals(searchMap.get("price"))) {
			String priceStr = (String) searchMap.get("price");
			String[] price = priceStr.split("-");
			//如果最低价格不等于0
			if (!price[0].equals("0")) { // 3000-*
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
				
			}
			//如果最高价格不等于*
			if (!price[1].equals("*")) {
				FilterQuery filterQuery = new SimpleFilterQuery();
				Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
				filterQuery.addCriteria(filterCriteria);
				query.addFilterQuery(filterQuery);
				
			}
			
		}
		
		//1.6分页
		//获取页码
		Integer pageNo = (Integer) searchMap.get("pageNo");
		if (pageNo == null) {
			pageNo = 1;
		}
		//获取页大小
		Integer pageSize = (Integer) searchMap.get("pageSize");
		if (pageSize == null) {
			pageSize = 20;
		} 
		//起始索引 
		query.setOffset((pageNo - 1) * pageSize);
		//每页记录数
		query.setRows(pageSize);
		
		
		//1,7按价格排序
		
		String sortValue = (String) searchMap.get("sort");//升序ASC，降序DESC
		String sortField = (String) searchMap.get("sortField");//排序字段 
		if (sortValue != null && !sortValue.equals("")) {
			if (sortValue.equals("ASC")) {
				Sort sort = new Sort(Sort.Direction.ASC, "item_"+sortField);
				query.addSort(sort);
			}
			if (sortValue.equals("DESC")) {
				Sort sort = new Sort(Sort.Direction.DESC, "item_"+sortField);
				query.addSort(sort);
			}
		}
		
		
		
		
		
		
		
		
		
		//*************获取高亮结果集******************
		//高亮显示 返回高亮页对象
		HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query,TbItem.class);
		//高亮入口集合
		List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
		for (HighlightEntry<TbItem> entry : entryList) {
			//获取高亮列表
			List<Highlight> highlightList = entry.getHighlights();
			/*for (Highlight h : highlightList) {
				//每个域有可能查询多值
				List<String> sns = h.getSnipplets();
				System.out.println(sns);
			}*/
			if (highlightList.size()>0 && highlightList.get(0).getSnipplets().size()>0) {
				TbItem item = entry.getEntity();
				item.setTitle(highlightList.get(0).getSnipplets().get(0));
			}
		}
		map.put("rows", page.getContent());
		//总页数
		map.put("totalPages", page.getTotalPages());
		//总记录数
		map.put("total", page.getTotalElements());
		return map;
	}
	/**
	 * 分组查询，（查询商品分类列表）
	 * @return
	 */
	private List searchCategoryList(Map searchMap) {
		
		List<String> list = new ArrayList<String>();
		
		Query query = new SimpleQuery("*:*");
		//根据关键字查询 sql where
		Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
		query.addCriteria(criteria);
		//group by  设置分组选项 
		GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
		query.setGroupOptions(groupOptions);
		//获取分组页
		GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
		//获取分组结果对象
		GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
		//获取分组入口页
		Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
		//获取分组入口集合
		List<GroupEntry<TbItem>> entryList = groupEntries.getContent();
		for (GroupEntry<TbItem> entry : entryList) {
			String str = entry.getGroupValue();
			//将分组的结果添加到返回值中
			list.add(str);
		}
		
		return list;
	}
	
	@Autowired
	private RedisTemplate redisTemplate;
	/**
	 * 查询品牌和规格列表
	 * @param category 商品分类名称
	 * @return
	 */
	private Map searchBrandAndSpecList(String category) {
		Map map = new HashMap();
		//根据商品分类名称得到模板ID
		Long templateId =  (Long) redisTemplate.boundHashOps("itemCat").get(category);
		if (templateId !=null) {
			//根据模板ID得到品牌列表
			List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
			map.put("brandList", brandList);
			//根据模板ID得到规格列表
			List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
			map.put("specList", specList);
		}
		return map;
	}
	
	/**
	 * 导入列表
	 */
	public void importList(List list) {
		solrTemplate.saveBeans(list);
		solrTemplate.commit();
	}

	/**
	 * 删除商品列表
	 */
	public void deleteByGoodsIds(List goodsIds) {
		
		Query query = new SimpleQuery("*:*");
		Criteria criteria = new Criteria("item_goodsid").in(goodsIds);
		query.addCriteria(criteria);
		solrTemplate.delete(query);
		solrTemplate.commit();
		
	}

}




















