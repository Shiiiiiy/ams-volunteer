package com.uws.volunteer.dao;

import com.uws.core.hibernate.dao.IBaseDao;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.volunteer.VolunteerBaseinfoModel;
import com.uws.domain.volunteer.VolunteerHonorModel;
import com.uws.domain.volunteer.VolunteerOfficeModel;
import com.uws.domain.volunteer.VolunteerServiceModel;
import com.uws.domain.volunteer.VolunteerSummaryInfoModel;

/** 
* IVolunteerDao
* @Description:志愿者数据层接口 Dao
* @author zhangmx
* @date	   2015-12-02
*/
public interface IVolunteerDao extends IBaseDao{
	
	/**
	 * 分页获取志愿者申请列表
	 * @param vbm			志愿者基本信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	public Page pageQueryVolunteerApplyInfo(VolunteerBaseinfoModel vbm,int pageNo,int pageSize,String userId);
	
	/**
	 * 分页获取志愿者审核列表
	 * @param vbm			志愿者基本信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	public Page pageQuerySubmitApplyInfo(VolunteerBaseinfoModel vbm,int pageNo,int pageSize,String teacherOrgId);
	
	/**
	 * 分页获取志愿者信息
	 * @param vbm			志愿者基本信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	public Page pageQueryVolunteerInfo(VolunteerBaseinfoModel vbm,int pageNo,int pageSize,String teacherOrgId);
	
	/**
	 * 分页获取志愿者基地信息
	 * @param vom			志愿者基地信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	public Page pageQueryVolunteerOfficeInfo(VolunteerOfficeModel vom,int pageNo,int pageSize,String teacherOrgId);
	
	/**
	 * 志愿者服务维护列表
	 * @param vsm			志愿者服务实体
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @param orgId			组织机构id
	 */
	public Page pageQueryVolunteerService(VolunteerServiceModel vsm,int pageNo,int pageSize,String orgId);
	
	/**
	 * 志愿者荣誉维护列表
	 * @param vhm			志愿者荣誉实体
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @param orgId			组织机构id
	 */
	public Page pageQueryVolunteerHonor(VolunteerHonorModel vhm,int pageNo,int pageSize,String orgId);
	
	/**
	 * 控件   ：分页获取志愿者信息
	 * @param vbm			志愿者基本信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	public Page queryRadioVolunteer(VolunteerBaseinfoModel vbm,int pageNo,int pageSize,String teacherOrgId);

	/**
	 * 监管分析列表
	 * @param vsim			志愿者统计实体
	 * @param pageNo		当前页码
	 * @param pageSize	分页大侠
	 * @return						分页对象
	 */
	public Page pageQueryVolunteerSummary(VolunteerSummaryInfoModel volunteerSummaryInfoModel, int pageNo, int pageSize);
	
}
