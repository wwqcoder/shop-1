package com.pinyougou.sellergoods.service.impl;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbBrandMapper;
import com.pinyougou.pojo.TbBrand;
import com.pinyougou.pojo.TbBrandExample;
import com.pinyougou.pojo.TbBrandExample.Criteria;
import com.pinyougou.sellergoods.service.BrandService;

import entity.PageResult;

@Service
@Transactional
public class BrandServiceImpl implements BrandService{

	@Autowired
	private TbBrandMapper brandMapper;
	
	/**
	 * 查询品牌列表
	 */
	public List<TbBrand> findAll() {
		return brandMapper.selectByExample(null);
	}
	/**
	 * 品牌分页
	 */
	public PageResult findPage(Integer pageNum, Integer pageSize) {
		//分页
		PageHelper.startPage(pageNum, pageSize);
		
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(null);
		
		return new PageResult(page.getTotal(),page.getResult());
	}
	/**
	 * 增加品牌
	 */
	public void add(TbBrand brand) {
		brandMapper.insert(brand);
	}
	/**
	 * 通过id查一个品牌数据，用于修改显示
	 */
	public TbBrand findOne(Long id) {
		return brandMapper.selectByPrimaryKey(id);
	}
	/**
	 * 品牌修改
	 */
	public void update(TbBrand brand) {
		brandMapper.updateByPrimaryKey(brand);
		
	}
	/**
	 * 品牌删除
	 */
	public void delete(Long[] ids) {
		for (Long id : ids) {
			brandMapper.deleteByPrimaryKey(id);
		}
	}
	@Override
	public PageResult findPage(TbBrand brand, Integer pageNum, Integer pageSize) {
		//分页
		PageHelper.startPage(pageNum, pageSize);
		
		TbBrandExample example = new TbBrandExample();
		Criteria criteria = example.createCriteria(); 
		
		if (brand != null) {
			if (brand.getName() != null && brand.getName().length()>0) {
				criteria.andNameLike("%"+brand.getName()+"%");
			}
			if (brand.getFirstChar() != null && brand.getFirstChar().length()>0) {
				criteria.andFirstCharLike("%"+brand.getFirstChar()+"%");
			}
			
		}
		
		Page<TbBrand> page = (Page<TbBrand>) brandMapper.selectByExample(example);
		
		return new PageResult(page.getTotal(),page.getResult());
	}
	
	/**
	 * 返回下拉列表数据
	 */
	public List<Map> selectOptionList() {
		return brandMapper.selectOptionList();
	}

}













