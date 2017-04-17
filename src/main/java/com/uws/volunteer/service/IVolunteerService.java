package com.uws.volunteer.service;

import java.util.Date;
import java.util.List;

import com.uws.core.base.BaseModel;
import com.uws.core.base.IBaseService;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.volunteer.VolunteerBaseinfoModel;
import com.uws.domain.volunteer.VolunteerHonorModel;
import com.uws.domain.volunteer.VolunteerHonorUserModel;
import com.uws.domain.volunteer.VolunteerOfficeModel;
import com.uws.domain.volunteer.VolunteerServiceModel;
import com.uws.domain.volunteer.VolunteerServiceUserModel;
import com.uws.domain.volunteer.VolunteerSummaryInfoModel;

public interface IVolunteerService extends IBaseService{
	
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
	 * 保存志愿者基地信息
	 * @param vom			志愿者基地实体
	 */
	public void saveVolunteerOffice(VolunteerOfficeModel vom);
	
	/**
	 * 更新志愿者基地信息
	 * @param vom			志愿者基地实体
	 */
	public void updateVolunteerOffice(VolunteerOfficeModel vom);
	
	/**
	 * 根据id查找志愿者基地信息
	 * @param officeId		业务主键
	 */
	public VolunteerOfficeModel queryVolunteerOfficeById(String officeId);
	
	/**
	 * 逻辑删除志愿者基地信息
	 * @param office			志愿者基地实体
	 */
	public void  deleteOffice(VolunteerOfficeModel office);
	
	/**
	 * 志愿者监管分析统计
	 * @return	分页信息
	 */
	public Page summaryVolunteerInfo();
	
	/**
	 * 增加志愿者申请
	 * @param vbm	志愿者基础信息
	 */
	public void addVounteerApply(VolunteerBaseinfoModel vbm);
	
	/**
	 * 更新志愿者申请
	 * @param vbm	志愿者基础信息
	 */
	public void updateVounteerApply(VolunteerBaseinfoModel vbm);

	/**
	 * 根据id查找志愿者申请
	 * @param id			业务主键
	 * @return				志愿者基础信息
	 */
	public VolunteerBaseinfoModel queryVolunteerApplyById(String id);
	
	/**
	 * 根据学号查找志愿者
	 * @param stuNumber	学生学号
	 * @return							志愿者基础信息
	 */
	public VolunteerBaseinfoModel queryVolunteerByStuNumber(String stuNumber);
	
	/**
	 * 删除志愿者申请
	 * @param vbm		志愿者基础信息
	 */
	public void delVolunteerApply(VolunteerBaseinfoModel vbm);
	
	/**
	 * 更新对象【公共方法】
	 * @param baseModel
	 */
	public void update(BaseModel baseModel);
	
	/**
	 * 保存 对象【公共方法】
	 * @param baseModel
	 */
	public void save(BaseModel baseModel);
	
	/**
	 * 保存志愿者荣誉
	 * @param vhm	志愿者荣誉实体
	 */
	public void saveHonor(VolunteerHonorModel vhm,String[] fileId);
	
	/**
	 * 更新志愿者荣誉
	 * @param vhm
	 */
	public void updateHonor(VolunteerHonorModel vhm,String[] fileId);
	
	/**
	 * 查找荣誉志愿者集合
	 * @param id		业务主键
	 * @return			志愿者荣誉集合	
	 */
	public List<VolunteerHonorUserModel> findHonorUserListByHonorId(String honorId);
	
	/**
	 * 删除该荣誉的所有志愿者
	 * @param honorUserList	荣誉志愿者集合
	 */
	public void delHonorUserList(List<VolunteerHonorUserModel> honorUserList);
	
	/**
	 * 志愿者荣誉维护列表
	 * @param vsm			志愿者荣誉实体
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @param orgId			组织结构id
	 */
	public Page pageQueryVolunteerHonor(VolunteerHonorModel vhm,int pageNo,int pageSize,String orgId);

	/**
	 * 根据id 查找荣誉
	 * @param id				业务主键
	 */
	public VolunteerHonorModel queryHonorById(String id);
	
	/**
	 * 删除志愿者荣誉
	 * @param honor	志愿者荣誉实体
	 */
	public void deleteHonor(VolunteerHonorModel honor);
	
	/**
	 * 查找志愿者荣誉列表
	 * @param volunteerId		志愿者id
	 * @return	志愿者荣誉列表
	 */
	public List<VolunteerHonorModel> queryHonorListByVolunteerId(String volunteerId);

	/**
	 * 保存志愿者服务
	 * @param vsm		志愿者服务实体
	 * @param fileId		附件集合
	 */
	public void saveService(VolunteerServiceModel vsm,String[] fileId);
	
	/**
	 * 更新志愿者服务
	 * @param vsm		志愿者服务实体
	 * @param fileId		附件集合
	 */
	public void updateService(VolunteerServiceModel vsm,String[] fileId);
	
	/**
	 * 查找服务user 集合
	 * @param serviceId	志愿服务id
	 * @return	志愿服务列表
	 */
	public List<VolunteerServiceUserModel> findServiceUserListByServiceId(String serviceId);

	/**
	 * 删除志愿者服务
	 * @param serviceUserList		志愿者服务列表
	 */
	public void delServiceUserList(List<VolunteerServiceUserModel> serviceUserList);
	
	/**
	 * 志愿者服务维护列表
	 * @param vsm			志愿者服务列表
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @param orgId			组织机构id
	 */
	public Page pageQueryVolunteerService(VolunteerServiceModel vsm,int pageNo,int pageSize,String orgId);

	/**
	 * 查找志愿者服务
	 * @param id			业务主键
	 * @return				志愿者服务实体
	 */
	public VolunteerServiceModel queryServiceById(String id);
	
	/**
	 * 删除志愿者服务
	 * @param service	志愿者服务
	 */
	public void deleteService(VolunteerServiceModel service);
	
	/**
	 * 查找志愿者服务列表
	 * @param volunteerId		志愿者id
	 * @return 志愿者服务列表
	 */
	public List<VolunteerServiceModel> queryServiceListByVolunteerId(String volunteerId);

	/**
	 * 判断当前用户角色
	 * @param userId		用户id
	 * @param roleCode	用户角色
	 * @return	[true、false]
	 */
	public boolean isRightRole(String userId, String roleCode);
	
	/**
	 * 查询学院下参加服务的志愿者
	 * @param college	当前学院
	 * @return List<VolunteerServiceUserModel>
	 */
	public List<VolunteerServiceUserModel> queryVolunteerService( String college,String yearId);
	/**
	 * 查询学院下志愿者服务的总时长
	 * @param college	当前学院
	 * @return List<VolunteerServiceUserModel>
	 */
	public List<VolunteerServiceModel> queryVolunteerServiceHour( String college,String yearId);
	
	/**
	 * 查询学院下获得荣誉的志愿者(在校)
	 * @param college						当前学院
	 * @param honorYear				当前学年
	 * @param honorTerm				当前学期
	 * @return 志愿者荣誉列表
	 */
	public List<VolunteerHonorUserModel> queryVolunteerHonor(String college, String honorYear);

	/**
	 * 查询学院下参加培训志愿者
	 * @param college						当前学院
	 * @return 志愿者列表
	 */
	public List<VolunteerBaseinfoModel> queryVolunteerTraining(String college);
	
	/**
	 * 查询学院下审核通过的志愿者
	 * @param college	 					当前学院
	 * @return 志愿者列表
	 */
	public List<VolunteerBaseinfoModel> queryVolunteerPass(String college);
	
	/**
	 * 控件   ：分页获取志愿者信息
	 * @param vbm			志愿者基本信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	public Page queryRadioVolunteer(VolunteerBaseinfoModel vbm,int pageNo,int pageSize,String teacherOrgId);

	/**
	 *  监管分析列表
	 * @param vsim			监管分析实体
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	public Page pageQueryVolunteerSummary(VolunteerSummaryInfoModel vsim, int pageNo, int pageSize);

	/**
	 * 根据志愿者注册号查找志愿者
	 * @param registerNum	注册号
	 * @return	志愿者基础实体
	 */
	public VolunteerBaseinfoModel queryVolunteerByRegisterNum(String registerNum);
	/**
	 * 根据学年id转化为时间
	 * @param yearCode
	 * @return
	 */
	public List<Date> changeYear(String yearId);
}
