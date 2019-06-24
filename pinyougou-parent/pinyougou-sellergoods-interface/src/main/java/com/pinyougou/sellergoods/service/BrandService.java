package com.pinyougou.sellergoods.service;
/**
 * 品牌接口
 * @author Administrator
 *
 */

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;

import entity.PageResult;

public interface BrandService {

	public List<TbBrand> findAll();
	
	public PageResult findPage(Integer pageNum,Integer pageSize);
	
	public void add(TbBrand brand);
	
	public TbBrand findOne(Long id);
	
	public void update(TbBrand brand);
	
	public void delete(Long[] ids);
	
	public PageResult findPage(TbBrand brand,Integer pageNum,Integer pageSize);
	
	public List<Map> selectOptionList();
}
