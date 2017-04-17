package com.uws.volunteer.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.uws.common.service.IBaseDataService;
import com.uws.common.util.CYLeagueUtil;
import com.uws.common.util.Constants;
import com.uws.core.hibernate.dao.impl.BaseDaoImpl;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.session.SessionFactory;
import com.uws.core.session.SessionUtil;
import com.uws.core.util.HqlEscapeUtil;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.volunteer.VolunteerBaseinfoModel;
import com.uws.domain.volunteer.VolunteerHonorModel;
import com.uws.domain.volunteer.VolunteerOfficeModel;
import com.uws.domain.volunteer.VolunteerServiceModel;
import com.uws.domain.volunteer.VolunteerSummaryInfoModel;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.impl.DicFactory;
import com.uws.volunteer.dao.IVolunteerDao;
import com.uws.volunteer.service.IVolunteerService;
import com.uws.volunteer.util.VolunteerConstants;

@Repository("volunteerDao")
@SuppressWarnings("all")
public class VolunteerDaoImpl  extends BaseDaoImpl implements IVolunteerDao {

	@Autowired
	private IVolunteerService volunteerService;
	
	@Autowired
	private IBaseDataService baseDataService;
	
	// 数据字典工具类
	private static DicUtil dicUtil=DicFactory.getDicUtil();
	
	 //session工具类
	private SessionUtil sessionUtil = SessionFactory.getSession(VolunteerConstants.NAMESPACE);
	
	/**
	 * 分页获取志愿者申请列表
	 * @param vbm			志愿者基本信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	@Override
	public Page pageQueryVolunteerApplyInfo(VolunteerBaseinfoModel vbm,
			int pageNo, int pageSize, String userId) {
		 List<Object> values = new ArrayList<Object>();
	     StringBuffer hql = new StringBuffer("from VolunteerBaseinfoModel w  where 1=1 ");
	     hql.append(" and w.stuInfo.id=? ");
  		 values.add(userId);
	     if(vbm!=null && vbm.getStuInfo()!=null && !"".equals(vbm.getStuInfo())){
	    	  hql.append(" and w.stuInfo.id=? ");
	  		 values.add(vbm.getStuInfo().getId());
	     }
 		 
	     if (values.size() == 0) {
		      return pagedQuery(hql.toString(), pageNo, pageSize, new Object[0]);
		 }else{
			 return pagedQuery(hql.toString(), pageNo, pageSize, values.toArray());
		 }
		      
	}
	
	/**
	 * 分页获取志愿者审核列表
	 * @param vbm			志愿者基本信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	@Override
	public Page pageQuerySubmitApplyInfo(VolunteerBaseinfoModel vbm,
			int pageNo, int pageSize,String teacherOrgId) {
		 List<Object> values = new ArrayList<Object>();
	     StringBuffer hql = new StringBuffer("from VolunteerBaseinfoModel w  where 1=1 ");
	     //1.判断当前登录人的机构是不是在学院中
	     String isCollegeTeacher="false";
	     //学院
    	 List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
	     for(BaseAcademyModel co:collegeList){
	    		if(teacherOrgId!=null&& teacherOrgId.equals(co.getId())){
	    			isCollegeTeacher="true";
	    			break ;
	    		}
	     }
	     if("false".equals(isCollegeTeacher)){
	    	 hql.append(" and 1=2 ");
	     }
	    
	     if(vbm!=null && vbm.getStuInfo()!=null && !"".equals(vbm.getStuInfo())){
	    	 //学院
			 if (vbm.getStuInfo().getCollege()!=null&&StringUtils.isNotBlank(vbm.getStuInfo().getCollege().getId())) {
		         hql.append(" and w.stuInfo.college.id = ? ");
		         values.add(vbm.getStuInfo().getCollege().getId());
			 }
			 //专业
			 if ( vbm.getStuInfo().getMajor()!= null && StringUtils.isNotBlank(vbm.getStuInfo().getMajor().getId())) {
		         hql.append(" and w.stuInfo.major.id =? ");
		         values.add(vbm.getStuInfo().getMajor().getId());
			 }
			 //班级
			 if ( vbm.getStuInfo().getClassId()!= null && StringUtils.isNotBlank(vbm.getStuInfo().getClassId().getId())) {
		         hql.append(" and w.stuInfo.classId.id= ?");
		         values.add(vbm.getStuInfo().getClassId().getId());
			 }
			 //学号
	    	 if( vbm.getStuInfo()!=null && StringUtils.isNotBlank( vbm.getStuInfo().getStuNumber())){
	    		   hql.append(" and w.stuInfo.stuNumber = ? ");
			         values.add(vbm.getStuInfo().getStuNumber());
	    		
	    	 }
	    	//姓名
	    	 if( vbm.getStuInfo()!=null && StringUtils.isNotBlank( vbm.getStuInfo().getName())){
	    		 hql.append(" and w.stuInfo.name like ? ");
		         values.add("%" +  HqlEscapeUtil.escape(vbm.getStuInfo().getName()) + "%");
	    	 }
	     }
	     if(vbm!=null && vbm.getApproveResult()!=null && !"".equals(vbm.getApproveResult())&&!"".equals(vbm.getApproveResult().getId())){
	    		   hql.append(" and w.approveResult.id=? ");
		    	   values.add(vbm.getApproveResult().getId());
	     }
	     //已经提交
	     hql.append(" and w.status.id=? ");
  		 values.add(VolunteerConstants.STATUS_SUBMIT_DICS.getId());
  		 hql.append(" order by  w.approveResult,w.stuInfo.college,w.stuInfo.major,w.stuInfo.classId,w.stuInfo.stuNumber");
	     if (values.size() == 0) {
		      return pagedQuery(hql.toString(), pageNo, pageSize, new Object[0]);
		 }else{
			 return pagedQuery(hql.toString(), pageNo, pageSize, values.toArray());
		 }
		      
	}

	/**
	 * 分页获取志愿者信息
	 * @param vbm			志愿者基本信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	@Override
	public Page pageQueryVolunteerInfo(VolunteerBaseinfoModel vbm, int pageNo,
			int pageSize,String teacherOrgId) {
		 List<Object> values = new ArrayList<Object>();
	     StringBuffer hql = new StringBuffer("from VolunteerBaseinfoModel w  where 1=1 ");
	     //1.判断当前登录人的机构是不是在学院中
	     String isCollegeTeacher="false";
	     //1.判断校青协？？--设置查询条件
		 boolean isSchoolYouthClub=this.volunteerService.isRightRole(sessionUtil.getCurrentUserId(), "HKY_SCHOLL_YOUTHCLUB_LEADER");
	     if(!isSchoolYouthClub){
	    	 //学院
	    	 List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
		     for(BaseAcademyModel co:collegeList){
		    		if(teacherOrgId!=null&& teacherOrgId.equals(co.getId())){
		    			isCollegeTeacher="true";
		    			break ;
		    		}
		     }
		     if("false".equals(isCollegeTeacher)){
		    	 hql.append(" and 1=2 ");//组织机构不是学院的== 将看不到数据
		     }
	     }
	     hql.append(" and w.approveResult.id=? ");
  		 values.add(VolunteerConstants.STATUS_PASS.getId());
	     if(vbm!=null && vbm.getStuInfo()!=null && !"".equals(vbm.getStuInfo())){

	  		 //学院
			 if (vbm.getStuInfo().getCollege()!=null&&StringUtils.isNotBlank(vbm.getStuInfo().getCollege().getId())) {
		         hql.append(" and w.stuInfo.college.id = ? ");
		         values.add(vbm.getStuInfo().getCollege().getId());
			 }
			 //专业
			 if ( vbm.getStuInfo().getMajor()!= null && StringUtils.isNotBlank(vbm.getStuInfo().getMajor().getId())) {
		         hql.append(" and w.stuInfo.major.id =? ");
		         values.add(vbm.getStuInfo().getMajor().getId());
			 }
			 //班级
			 if ( vbm.getStuInfo().getClassId()!= null && StringUtils.isNotBlank(vbm.getStuInfo().getClassId().getId())) {
		         hql.append(" and w.stuInfo.classId.id= ?");
		         values.add(vbm.getStuInfo().getClassId().getId());
			 }
			 //学号
	    	 if( vbm.getStuInfo()!=null && StringUtils.isNotBlank( vbm.getStuInfo().getStuNumber())){
	    		   hql.append(" and w.stuInfo.stuNumber = ? ");
			         values.add(vbm.getStuInfo().getStuNumber());
	    		
	    	 }
	    	//姓名
	    	 if( vbm.getStuInfo()!=null && StringUtils.isNotBlank( vbm.getStuInfo().getName())){
	    		 hql.append(" and w.stuInfo.name like ? ");
		         values.add("%" +  HqlEscapeUtil.escape(vbm.getStuInfo().getName()) + "%");
	    	 }
	     }
		 hql.append("and w.deleteStatus.id=?");
    	 values.add(Constants.STATUS_NORMAL.getId());
  		 hql.append(" order by  w.stuInfo.college,w.stuInfo.major,w.stuInfo.classId,w.stuInfo.stuNumber");

	     if (values.size() == 0) {
		      return pagedQuery(hql.toString(), pageNo, pageSize, new Object[0]);
		 }else{
			 return pagedQuery(hql.toString(), pageNo, pageSize, values.toArray());
		 }
	}
	
	/**
	 * 分页获取志愿者基地信息
	 * @param vom			志愿者基地信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	@Override
	public Page pageQueryVolunteerOfficeInfo(VolunteerOfficeModel vom,
			int pageNo, int pageSize,String teacherOrgId) {
		 List<Object> values = new ArrayList<Object>();
	     StringBuffer hql = new StringBuffer("from VolunteerOfficeModel w  where 1=1 ");
	     hql.append(" and w.deleteStatus.id=? ");
  		 values.add(Constants.STATUS_NORMAL.getId());
  		 //判断是院团委
		 boolean isCollegeLeague=this.volunteerService.isRightRole(this.sessionUtil.getCurrentUserId(),CYLeagueUtil.CYL_ROLES.HKY_COLLEGE_LEAGUE_LEADER.toString());
		 if(isCollegeLeague && teacherOrgId!=null){
			 hql.append(" and w.college.id=? ");
	  		 values.add(teacherOrgId);
		 }else{
			 if (vom!=null && vom.getCollege()!=null&&StringUtils.isNotBlank(vom.getCollege().getId())) {
				 hql.append(" and w.college.id=? ");
		  		 values.add(vom.getCollege().getId());
		     }
		 }
	     if(vom!=null && vom.getOfficeAddress()!=null && !"".equals(vom.getOfficeAddress())){
	    	  hql.append(" and w.officeAddress like ? ");
	  		  values.add("%"+ HqlEscapeUtil.escape(vom.getOfficeAddress())+"%");
	     }
	     if(vom!=null && vom.getManager()!=null && !"".equals(vom.getManager())){
	    	  hql.append(" and w.manager like ? ");
	  		  values.add("%"+ HqlEscapeUtil.escape(vom.getManager())+"%");
	     }
  		 hql.append(" order by  w.college,w.officeAddress,w.manager,w.foundedTime");

	     if (values.size() == 0) {
		      return pagedQuery(hql.toString(), pageNo, pageSize, new Object[0]);
		 }else{
			  return pagedQuery(hql.toString(), pageNo, pageSize, values.toArray());
		 }
	}
	
	/**
	 * 志愿者服务维护列表
	 * @param vsm
	 * @param pageNo
	 * @param pageSize
	 * @param orgId
	 */
	@Override
	public Page pageQueryVolunteerService(VolunteerServiceModel vsm,
			int pageNo, int pageSize, String orgId) {
		 List<Object> values = new ArrayList<Object>();
		 StringBuffer hql = new StringBuffer("select vsu.volunteerServicePo from VolunteerServiceUserModel vsu  where 1=1 ");
	     if(orgId!=null &&!"".equals(orgId)){
	    	 hql.append(" and vsu.volunteerPo.stuInfo.college.id=? ");
	  		 values.add(orgId);
	     }
	     if(vsm!=null ){
	    	 if(vsm.getServiceDate()!=null&&!"".equals(vsm.getServiceDate())){
	    		 //服务日期
	    		 hql.append(" and vsu.volunteerServicePo.serviceDate=? ");
		  		 values.add(vsm.getServiceDate());
	    	 }
	    	 if(vsm.getServiceHourStr()!=null &&!"".equals(vsm.getServiceHourStr())){
	    		 //服务时长
	    		 hql.append(" and vsu.volunteerServicePo.serviceHour=? ");
	    		 Double serviceHour=Double.parseDouble(vsm.getServiceHourStr());
		  		 values.add(serviceHour);
	    	 }
	    	 if(vsm.getServiceRecord()!=null&&!"".equals(vsm.getServiceRecord())){
	    		 //服务时间
	    		 hql.append(" and vsu.volunteerServicePo.serviceRecord like ? ");
		  		 values.add("%"+ HqlEscapeUtil.escape(vsm.getServiceRecord())+"%");
	    	 }
	     }
  		 hql.append(" order by  vsu.volunteerServicePo.serviceDate,vsu.volunteerServicePo.serviceHour");

	     if (values.size() == 0) {
		      return pagedQuery(hql.toString(), pageNo, pageSize, new Object[0]);
		 }else{
			 return pagedQuery(hql.toString(), pageNo, pageSize, values.toArray());
		 }
	}
	
	/**
	 * 志愿者荣誉维护列表
	 * @param vhm
	 * @param pageNo
	 * @param pageSize
	 * @param orgId
	 */
	@Override
	public Page pageQueryVolunteerHonor(VolunteerHonorModel vhm,
			int pageNo, int pageSize, String orgId) {
		 List<Object> values = new ArrayList<Object>();
		 StringBuffer hql = new StringBuffer(" select  vhu.volunteerHonorPo from VolunteerHonorUserModel vhu  where 1=1 ");
	     if(orgId!=null &&!"".equals(orgId)){
	    	 hql.append(" and vhu.volunteerPo.stuInfo.college.id=? ");
	  		 values.add(orgId);
	     }
	   
	     if(vhm!=null){
	    	 //学年
	    	 if(vhm.getHonorYear()!=null && StringUtils.isNotBlank(vhm.getHonorYear().getId())){
	    		 hql.append(" and vhu.volunteerHonorPo.honorYear.id=? ");
		  		 values.add(vhm.getHonorYear().getId());
	    	 }
	    	 //学期
	    	 if(vhm.getHonorTerm()!=null && StringUtils.isNotBlank(vhm.getHonorTerm().getId())){
	    		 hql.append(" and vhu.volunteerHonorPo.honorTerm.id=? ");
		  		 values.add(vhm.getHonorTerm().getId());
	    	 }
	    	 //级别
	    	 if(vhm.getHonorLevel()!=null && StringUtils.isNotBlank(vhm.getHonorLevel().getId())){
	    		 hql.append(" and vhu.volunteerHonorPo.honorLevel.id=? ");
		  		 values.add(vhm.getHonorLevel().getId());
	    	 }
	    	 //名称
	    	 if(vhm.getHonorName()!=null && StringUtils.isNotBlank(vhm.getHonorName())){
	    		 hql.append(" and vhu.volunteerHonorPo.honorName like ?");
		  		 values.add("%"+ HqlEscapeUtil.escape(vhm.getHonorName())+"%");
	    	 }
	     }
  		 hql.append(" order by  vhu.volunteerHonorPo.honorYear desc,vhu.volunteerHonorPo.honorTerm desc,"
  		 		+ "vhu.volunteerHonorPo.honorLevel,vhu.volunteerHonorPo.honorName");
	     if (values.size() == 0) {
		      return pagedQuery(hql.toString(), pageNo, pageSize, new Object[0]);
		 }else{
			 return pagedQuery(hql.toString(), pageNo, pageSize, values.toArray());
		 }
	}
	

	/**
	 * 控件   ：分页获取志愿者信息
	 * @param vbm			志愿者基本信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	@Override
	public Page queryRadioVolunteer(VolunteerBaseinfoModel vbm, int pageNo,
			int pageSize,String teacherOrgId) {
		 List<Object> values = new ArrayList<Object>();
	     StringBuffer hql = new StringBuffer("from VolunteerBaseinfoModel w  where 1=1 ");
	  
	     hql.append(" and w.approveResult.id=? ");
  		 values.add(VolunteerConstants.STATUS_PASS.getId());
  		 if(teacherOrgId!=null &&!"".equals(teacherOrgId)){
  			  hql.append(" and w.stuInfo.college.id = ? ");
		      values.add(teacherOrgId);
  		 }
	     if(vbm!=null && vbm.getStuInfo()!=null && !"".equals(vbm.getStuInfo())){
	  		
			 //专业
			 if ( vbm.getStuInfo().getMajor()!= null && StringUtils.isNotBlank(vbm.getStuInfo().getMajor().getId())) {
		         hql.append(" and w.stuInfo.major.id =? ");
		         values.add(vbm.getStuInfo().getMajor().getId());
			 }
			 //班级
			 if ( vbm.getStuInfo().getClassId()!= null && StringUtils.isNotBlank(vbm.getStuInfo().getClassId().getId())) {
		         hql.append(" and w.stuInfo.classId.id= ?");
		         values.add(vbm.getStuInfo().getClassId().getId());
			 }
			 //学号
	    	 if( vbm.getStuInfo()!=null && StringUtils.isNotBlank( vbm.getStuInfo().getStuNumber())){
	    		   hql.append(" and w.stuInfo.stuNumber = ? ");
			         values.add(vbm.getStuInfo().getStuNumber());
	    		
	    	 }
	    	//姓名
	    	 if( vbm.getStuInfo()!=null && StringUtils.isNotBlank( vbm.getStuInfo().getName())){
	    		 hql.append(" and w.stuInfo.name like ? ");
		         values.add("%" +  HqlEscapeUtil.escape(vbm.getStuInfo().getName()) + "%");
	    	 }
	     }
		 hql.append("and w.deleteStatus.id=?");
    	 values.add(Constants.STATUS_NORMAL.getId());

	     if (values.size() == 0) {
		      return pagedQuery(hql.toString(), pageNo, pageSize, new Object[0]);
		 }else{
			 return pagedQuery(hql.toString(), pageNo, pageSize, values.toArray());
		 }
	}
	
	/**
	 * 监管分析列表
	 * @param 
	 * @param pageNo
	 * @param pageSize
	 */
	@Override
	public Page pageQueryVolunteerSummary(VolunteerSummaryInfoModel volunteerSummaryInfoModel, int pageNo, int pageSize) {
		 List<Object> values = new ArrayList<Object>();
		 StringBuffer hql = new StringBuffer("from VolunteerSummaryInfoModel v where 1=1 ");
	     if(volunteerSummaryInfoModel!=null && volunteerSummaryInfoModel.getCollege()!=null && !StringUtils.isEmpty(volunteerSummaryInfoModel.getCollege().getId())){
	    	 hql.append(" and v.college.id=?");
	         values.add(volunteerSummaryInfoModel.getCollege().getId()); 
	     }
	     if (values.size() == 0) {
		      return pagedQuery(hql.toString(), pageNo, pageSize, new Object[0]);
		 }else{
			 return pagedQuery(hql.toString(), pageNo, pageSize, values.toArray());
		 }
	}
}
