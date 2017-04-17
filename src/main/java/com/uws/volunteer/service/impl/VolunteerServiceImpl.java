package com.uws.volunteer.service.impl;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.uws.common.dao.ICommonRoleDao;
import com.uws.common.service.IBaseDataService;
import com.uws.common.util.Constants;
import com.uws.core.base.BaseModel;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.volunteer.VolunteerBaseinfoModel;
import com.uws.domain.volunteer.VolunteerHonorModel;
import com.uws.domain.volunteer.VolunteerHonorUserModel;
import com.uws.domain.volunteer.VolunteerOfficeModel;
import com.uws.domain.volunteer.VolunteerServiceModel;
import com.uws.domain.volunteer.VolunteerServiceUserModel;
import com.uws.domain.volunteer.VolunteerSummaryInfoModel;
import com.uws.log.Logger;
import com.uws.log.LoggerFactory;
import com.uws.sys.model.Dic;
import com.uws.sys.model.UploadFileRef;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.FileUtil;
import com.uws.sys.service.IDicService;
import com.uws.sys.service.impl.DicFactory;
import com.uws.sys.service.impl.FileFactory;
import com.uws.volunteer.dao.IVolunteerDao;
import com.uws.volunteer.service.IVolunteerService;
import com.uws.volunteer.util.VolunteerConstants;

@Service("com.uws.volunteer.service.impl.VolunteerServiceImpl")
@SuppressWarnings("all")
public class VolunteerServiceImpl implements IVolunteerService {
	
	@Autowired
	private IVolunteerDao volunteerDao;
	
	@Autowired
	private ICommonRoleDao commonRoleDao;
	@Autowired
	private IDicService dicService;
	@Autowired
	private IBaseDataService baseDataService;
	//附件工具类
  	private FileUtil fileUtil=FileFactory.getFileUtil();
    //数据字典工具类
  	private DicUtil dicUtil = DicFactory.getDicUtil();
  	
	//日志工具类
	private static Logger logger = new LoggerFactory(VolunteerServiceImpl.class);

	
	/**
	 * 分页获取志愿者申请列表
	 * @param vbm			志愿者基本信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	@Override
	public Page pageQueryVolunteerApplyInfo(VolunteerBaseinfoModel vbm,int pageNo, int pageSize,String userId) {
		Page page=this.volunteerDao.pageQueryVolunteerApplyInfo(vbm, pageNo, pageSize, userId);
		return page;
	}
	
	/**
	 * 分页获取志愿者审核列表
	 * @param vbm			志愿者基本信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	@Override
	public Page pageQuerySubmitApplyInfo(VolunteerBaseinfoModel vbm,int pageNo, int pageSize,String teacherOrgId) {
		Page page=this.volunteerDao.pageQuerySubmitApplyInfo(vbm, pageNo, pageSize,teacherOrgId);
		return page;
	}
	
	/**
	 * 分页获取志愿者信息
	 * @param vbm			志愿者基本信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	@Override
	public Page pageQueryVolunteerInfo(VolunteerBaseinfoModel vbm, int pageNo,int pageSize,String teacherOrgId) {
		Page page=this.volunteerDao.pageQueryVolunteerInfo(vbm, pageNo, pageSize,teacherOrgId);
		return page;
	}
	
	/**
	 * 统计【待定】
	 */
	@Override
	public Page summaryVolunteerInfo() {
		
		return new Page();
	}
	
	/**
	 * 增加志愿者申请
	 */
	@Override
	public void addVounteerApply(VolunteerBaseinfoModel vbm) {
		vbm.setDeleteStatus(Constants.STATUS_NORMAL);
		if(VolunteerConstants.STATUS_SUBMIT_DICS.getId().equals(vbm.getStatus().getId())){
			vbm.setApplyTime(new Date());//第一次提交：设置申请时间
		}
		this.volunteerDao.save(vbm);
	}
	
	/**
	 * 更新志愿者申请
	 */
	@Override
	public void updateVounteerApply(VolunteerBaseinfoModel vbm) {
		VolunteerBaseinfoModel vbmPo=this.queryVolunteerApplyById(vbm.getId());
		if(vbmPo.getApplyTime()==null ||"".equals(vbmPo.getApplyTime())){
			vbmPo.setApplyTime(new Date());//第一次提交：是通过修改提交的：设置申请时间
		}
		if(vbm.getStatus()!=null&&!"".equals(vbm.getStatus())){
			if(VolunteerConstants.STATUS_SUBMIT_DICS.getId().equals(vbm.getStatus().getId())){
				BeanUtils.copyProperties(vbm, vbmPo, new String[]{"id","deleteStatus","applyTime","createTime"});
			}else{
				BeanUtils.copyProperties(vbm, vbmPo, new String[]{"id","deleteStatus","applyTime","createTime","approveResult"});
			}
		}
		this.volunteerDao.update(vbmPo);
	}
	
	/**
	 * 根据id查找志愿者申请
	 */
	@Override
	public VolunteerBaseinfoModel queryVolunteerApplyById(String id) {
		VolunteerBaseinfoModel vbm=(VolunteerBaseinfoModel)this.volunteerDao.queryUnique("from VolunteerBaseinfoModel where id=? and deleteStatus.id=?", 
				new Object[]{id,Constants.STATUS_NORMAL.getId()});
		return vbm;
	}
	
	/**
	 * 根据学号查找志愿者
	 */
	@Override
	public VolunteerBaseinfoModel queryVolunteerByStuNumber(String stuNumber) {
		return (VolunteerBaseinfoModel)this.volunteerDao.queryUnique("from VolunteerBaseinfoModel where  stuInfo.stuNumber=? ", stuNumber);
	}
	
	/**
	 * 分页获取志愿者基地信息
	 * @param vom			志愿者基地信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	@Override
	public Page pageQueryVolunteerOfficeInfo(VolunteerOfficeModel vom,int pageNo, int pageSize,String teacherOrgId) {
		Page page=this.volunteerDao.pageQueryVolunteerOfficeInfo(vom, pageNo, pageSize,teacherOrgId);
		return page;
	}
	
	/**
	 * 保存志愿者基地信息
	 */
	@Override
	public void saveVolunteerOffice(VolunteerOfficeModel vom) {
		vom.setDeleteStatus(Constants.STATUS_NORMAL);
		this.volunteerDao.save(vom);
		
	}
	
	/**
	 * 更新志愿者基地信息
	 */
	@Override
	public void updateVolunteerOffice(VolunteerOfficeModel vom) {
		VolunteerOfficeModel vomPo=this.queryVolunteerOfficeById(vom.getId());
		BeanUtils.copyProperties(vom, vomPo, new String[]{"id","createTime","deleteStatus"});
		this.volunteerDao.update(vomPo);
	}
	
	/**
	 * 查找愿者基地信息
	 */
	@Override
	public VolunteerOfficeModel queryVolunteerOfficeById(String officeId) {
		return (VolunteerOfficeModel)this.volunteerDao.get(VolunteerOfficeModel.class, officeId);
	}
	
	/**
	 * 逻辑删除志愿者基地信息
	 */
	@Override
	public void  deleteOffice(VolunteerOfficeModel office){
		office.setDeleteStatus(Constants.STATUS_DELETED);
		this.volunteerDao.update(office);
	}
	
	/**
	 * 删除志愿者申请
	 */
	@Override
	public void delVolunteerApply(VolunteerBaseinfoModel vbm) {
		this.volunteerDao.delete(vbm);
	}
	
	/**
	 * 更新对象【公共方法】
	 * @param baseModel
	 */
	@Override
	public void update(BaseModel baseModel) {
		this.volunteerDao.update(baseModel);
	}
	
	/**
	 * 保存对象
	 * @param baseModel【公共方法】
	 */
	@Override
	public void save(BaseModel baseModel) {
		this.volunteerDao.save(baseModel);
	}
	
	/**
	 * 保存荣誉	
	 */
	@Override
	public void saveHonor(VolunteerHonorModel volunteerhonor,String[] fileId) {
		this.volunteerDao.save(volunteerhonor);
		//上传的附件进行处理
		 if (ArrayUtils.isEmpty(fileId)) {
		       return;
		    }
		 for (String id : fileId)
			 this.fileUtil.updateFormalFileTempTag(id, volunteerhonor.getId());
	}
	
	
	/**
	 * 志愿者荣誉维护
	 * @param vhm				志愿者荣誉实体
	 * @param pageNo			当前页码
	 * @param pageSize		分页大小
	 * @param orgId				组织机构id
	 */
	@Override
	public Page pageQueryVolunteerHonor(VolunteerHonorModel vhm, int pageNo,
			int pageSize, String orgId) {
		Page page=this.volunteerDao.pageQueryVolunteerHonor( vhm,  pageNo,
				 pageSize,  orgId);
		return page;
	}
	
	/**
	 * 根据id 查找 荣誉
	 */
	@Override
	public VolunteerHonorModel queryHonorById(String id) {
		VolunteerHonorModel vhm=(VolunteerHonorModel)this.volunteerDao.get(VolunteerHonorModel.class, id);
		return vhm;
	}
	
	/**
	 * 更新志愿者荣誉
	 */
	@Override
	public void updateHonor(VolunteerHonorModel volunteerHonor,String[] fileId) {
		
		this.volunteerDao.update(volunteerHonor);
		
		 //上传的附件进行处理
		 if (ArrayUtils.isEmpty(fileId))
			 fileId = new String[0];
		     List<UploadFileRef> list = this.fileUtil.getFileRefsByObjectId(volunteerHonor.getId());
		     for (UploadFileRef ufr : list) {
		       if (!ArrayUtils.contains(fileId, ufr.getUploadFile().getId())){
		    	   this.fileUtil.deleteFormalFile(ufr);
		       }
		     }
		     for (String id : fileId){
		       this.fileUtil.updateFormalFileTempTag(id, volunteerHonor.getId());
		     }
	}
	
	/**
	 * 根据荣誉id 查找荣誉user 集合
	 */
	@Override
	public List<VolunteerHonorUserModel> findHonorUserListByHonorId(
			String honorId) {
		return  (List<VolunteerHonorUserModel>)this.volunteerDao.query("from VolunteerHonorUserModel where volunteerHonorPo.id=? ", honorId);
	}
	
	/**
	 * 删除该荣誉的所有用户
	 */
	@Override
	public void delHonorUserList(
			List<VolunteerHonorUserModel> honorUserList) {
		for(VolunteerHonorUserModel honorUser:honorUserList){
			//查找出该志愿者所有的荣誉list
			List<VolunteerHonorModel> volunteerHonorList=this.queryHonorListByVolunteerId(honorUser.getVolunteerPo().getId());
			if(volunteerHonorList==null||"".equals(volunteerHonorList)||volunteerHonorList.size()<=1){
				honorUser.getVolunteerPo().setIsHavehonor(Constants.STATUS_NO);
				this.save(honorUser.getVolunteerPo());
			}
			this.volunteerDao.delete(honorUser);
		}
	}
	
	/**
	 * 删除志愿者荣誉
	 */
	@Override
	public void deleteHonor(VolunteerHonorModel honor) {
		//级联删除
		List<VolunteerHonorUserModel> honorUserList=this.findHonorUserListByHonorId(honor.getId());
		for(VolunteerHonorUserModel honorUser:honorUserList){
			this.volunteerDao.delete(honorUser);

		}
		this.volunteerDao.delete(honor);
	}
	
	/**
	 * 根据志愿者id查找志愿者荣誉列表
	 */
	@Override
	public List<VolunteerHonorModel> queryHonorListByVolunteerId(
			String volunteerId) {
		return (List<VolunteerHonorModel>)this.volunteerDao.query("select h from VolunteerHonorModel h,VolunteerHonorUserModel u " +
				"where u.volunteerHonorPo.id=h.id and u.volunteerPo.id=?", volunteerId);
	}

	/**
	 * 保存志愿者服务
	 * @param volunteerService 志愿者服务实体类对象
	 */
	@Override
	public void saveService(VolunteerServiceModel volunteerService,String[] fileId) {
		this.volunteerDao.save(volunteerService);
		//上传的附件进行处理
		 if (ArrayUtils.isEmpty(fileId)) {
		       return;
		    }
		 for (String id : fileId)
			 this.fileUtil.updateFormalFileTempTag(id, volunteerService.getId());
	}
	
	/**
	 * 志愿者服务维护
	 * @param vsm			志愿者服务实体
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @param orgId			组织机构id
	 */
	@Override
	public Page pageQueryVolunteerService(VolunteerServiceModel vsm, int pageNo,
			int pageSize, String orgId) {
		Page page=this.volunteerDao. pageQueryVolunteerService( vsm,  pageNo,
				 pageSize,  orgId);
		return page;
	}
	
	/**
	 * 根据id 查找 服务
	 */
	@Override
	public VolunteerServiceModel queryServiceById(String id) {
		VolunteerServiceModel vsm=(VolunteerServiceModel)this.volunteerDao.get(VolunteerServiceModel.class, id);
		return vsm;
	}
	
	/**
	 * 更新志愿者服务
	 * @param volunteerService 志愿者服务实体类对象
	 */
	@Override
	public void updateService(VolunteerServiceModel volunteerService,String[] fileId) {
		this.volunteerDao.update(volunteerService);
		 //上传的附件进行处理
		 if (ArrayUtils.isEmpty(fileId))
			 fileId = new String[0];
		     List<UploadFileRef> list = this.fileUtil.getFileRefsByObjectId(volunteerService.getId());
		     for (UploadFileRef ufr : list) {
		       if (!ArrayUtils.contains(fileId, ufr.getUploadFile().getId())){
		    	   this.fileUtil.deleteFormalFile(ufr);
		       }
		     }
		     for (String id : fileId){
		       this.fileUtil.updateFormalFileTempTag(id, volunteerService.getId());
		     }
	}
	
	/**
	 * 根据服务id 查找服务user 集合
	 */
	@Override
	public List<VolunteerServiceUserModel> findServiceUserListByServiceId(
			String serviceId) {
		return  (List<VolunteerServiceUserModel>)this.volunteerDao.query("from VolunteerServiceUserModel where volunteerServicePo.id=? ", serviceId);
	}
	
	/**
	 * 删除serviceUserList
	 */
	@Override
	public void delServiceUserList(
			List<VolunteerServiceUserModel> serviceUserList) {
		for(VolunteerServiceUserModel serviceUser:serviceUserList){
			this.volunteerDao.delete(serviceUser);
		}
	}
	
	/**
	 * 删除志愿者服务
	 */
	@Override
	public void deleteService(VolunteerServiceModel service) {
		//级联删除
		List<VolunteerServiceUserModel> serviceUserList=this.findServiceUserListByServiceId(service.getId());
		for(VolunteerServiceUserModel serviceUser:serviceUserList){
			this.volunteerDao.delete(serviceUser);

		}
		this.volunteerDao.delete(service);
	}
	
	/**
	 * 根据志愿者id查找志愿者服务列表
	 */
	@Override
	public List<VolunteerServiceModel> queryServiceListByVolunteerId(
			String volunteerId) {
		return (List<VolunteerServiceModel>)this.volunteerDao.query("select s from VolunteerServiceModel s,VolunteerServiceUserModel u " +
				"where u.volunteerServicePo.id=s.id and u.volunteerPo.id=?", volunteerId);
	}
	@Override
	public boolean isRightRole(String userId, String roleCode) {
		return this.commonRoleDao.checkUserIsExist(userId, roleCode);
	}
	
	/**
	 * 查询学院、学年参加服务的志愿者
	 * @throws ParseException 
	 */
	@Override
	public List<VolunteerServiceUserModel> queryVolunteerService(String college,String yearId) {
		
		List<VolunteerServiceUserModel> list=null;
		if(yearId!=null&&!"".equals(yearId)){
			 List<Date> dateList=this.changeYear(yearId);
			 Date yaerDateMin=dateList.get(0);
			 Date yaerDateMax=dateList.get(1);
			 list = this.volunteerDao.query("select distinct v.volunteerPo.id from VolunteerServiceUserModel v where v.volunteerPo.stuInfo.college.id=? and v.volunteerServicePo.serviceDate>=? and v.volunteerServicePo.serviceDate<? and v.volunteerPo.approveResult.id=? and v.volunteerPo.deleteStatus.id=?",college,yaerDateMin,yaerDateMax,VolunteerConstants.STATUS_PASS.getId(),Constants.STATUS_NORMAL.getId());
		}else{
			 list = this.volunteerDao.query("select distinct v.volunteerPo.id from VolunteerServiceUserModel v where v.volunteerPo.stuInfo.college.id=? and v.volunteerPo.approveResult.id=? and v.volunteerPo.deleteStatus.id=?",college,VolunteerConstants.STATUS_PASS.getId(),Constants.STATUS_NORMAL.getId());
		}
		return list;
	}
	/**
	 * 查询学院下志愿者服务的总时长
	 * @param college	当前学院
	 * @return List<VolunteerServiceUserModel>
	 */
	@Override
	public List<VolunteerServiceModel> queryVolunteerServiceHour( String college,String yearId){
		List<VolunteerServiceModel> list=null;
		if(yearId!=null&&!"".equals(yearId)){
			 List<Date> dateList=this.changeYear(yearId);
			 Date yaerDateMin=dateList.get(0);
			 Date yaerDateMax=dateList.get(1);
			 list = this.volunteerDao.query("select distinct v.volunteerServicePo from VolunteerServiceUserModel v where v.volunteerPo.stuInfo.college.id=? and v.volunteerServicePo.serviceDate>=? and v.volunteerServicePo.serviceDate<? ",college,yaerDateMin,yaerDateMax);
		}else{
			 list = this.volunteerDao.query("select distinct v.volunteerServicePo from VolunteerServiceUserModel v where v.volunteerPo.stuInfo.college.id=? ",college);
		}
		return list;
	}
	/**
	 * 查询学院下获得荣誉的志愿者
	 */
	@Override
	public List<VolunteerHonorUserModel> queryVolunteerHonor(String college, String honorYear){
		Map<String,Object> values = new HashMap<String,Object>();
	    StringBuffer hql = new StringBuffer("select v.volunteerPo.id from VolunteerHonorUserModel v where 1=1 and v.volunteerPo.deleteStatus.id=:volunteerPoDeleteStatus and v.volunteerPo.approveResult.id=:approveResult and v.volunteerPo.stuInfo.college.id=:college and v.volunteerPo.isHavehonor.id=:isHavehonor ");
	    values.put("volunteerPoDeleteStatus", Constants.STATUS_NORMAL.getId());
	    values.put("approveResult", VolunteerConstants.STATUS_PASS.getId());
	    values.put("college", college);
        values.put("isHavehonor",Constants.STATUS_YES.getId());
	    //values.put("deleteStatus", Constants.STATUS_NORMAL.getId());
	    if(!StringUtils.isEmpty(honorYear)){
        	hql.append(" and v.volunteerHonorPo.honorYear.id = :honorYear");
            values.put("honorYear", honorYear);
        }
//      if(!StringUtils.isEmpty(honorTerm)){
//        	hql.append(" and v.volunteerHonorPo.honorTerm.id = :honorTerm");
//            values.put("honorTerm", honorTerm);
//      }
    	hql.append(" group by v.volunteerPo.id");
        return this.volunteerDao.query(hql.toString(), values);
	}
	
	/**
	 * 查询学院下参加培训志愿者
	 */
	@Override
	public List<VolunteerBaseinfoModel> queryVolunteerTraining(String college){

		 Map<String,Object> values = new HashMap<String,Object>();
	     StringBuffer hql = new StringBuffer("from VolunteerBaseinfoModel v  where 1=1 ");
	     if(college!=null){
	    	 hql.append("and v.stuInfo.college.id= (:collegeId )");
	    	 values.put("collegeId", college);
	     }
	     hql.append(" and  v.approveResult.id=(:approveResultId) and v.deleteStatus.id=(:deleteStatusId)");
	     values.put("approveResultId", VolunteerConstants.STATUS_PASS.getId());
	     values.put("deleteStatusId", Constants.STATUS_NORMAL.getId());
	     hql.append(" and v.isTraining.id=(:isTrainingId)");
	     values.put("isTrainingId", Constants.STATUS_YES.getId());
	     List<BaseClassModel> listBaseClass = baseDataService.listBaseClass(null, null, college);
	     List<String> classIds=new ArrayList<String>();
	     for(int i=0;i<listBaseClass.size();i++){
	    	 if(Constants.STATUS_NO.getId().equals(listBaseClass.get(i).getIsGraduatedDic())){
	    		 classIds.add(listBaseClass.get(i).getId());
	    	 }
	    	 
	     }
	     if(classIds!=null&& classIds.size()>0){
	    		hql.append(" and v.stuInfo.classId.id  in (:classIds)");
	    		values.put("classIds", classIds);
	     }
	    
	     return this.volunteerDao.query(hql.toString(), values);
	}
	
	/**
	 * 查询学院下审核通过的志愿者
	 */
	@Override
	public List<VolunteerBaseinfoModel> queryVolunteerPass(String college){
		 Map<String,Object> values = new HashMap<String,Object>();
	     StringBuffer hql = new StringBuffer("from VolunteerBaseinfoModel v  where 1=1 ");
	     if(college!=null){
	    	 hql.append("and v.stuInfo.college.id= (:collegeId )");
	    	 values.put("collegeId", college);
	     }
	     hql.append(" and  v.approveResult.id=(:approveResultId) and v.deleteStatus.id=(:deleteStatusId)");
	     values.put("approveResultId", VolunteerConstants.STATUS_PASS.getId());
	     values.put("deleteStatusId", Constants.STATUS_NORMAL.getId());
	     List<BaseClassModel> listBaseClass = baseDataService.listBaseClass(null, null, college);
	     List<String> classIds=new ArrayList<String>();
	     for(int i=0;i<listBaseClass.size();i++){
	    	 if(Constants.STATUS_NO.getId().equals(listBaseClass.get(i).getIsGraduatedDic())){
	    		 classIds.add(listBaseClass.get(i).getId());
	    	 }
	    	 
	     }
	     if(classIds!=null&& classIds.size()>0){
	    		hql.append(" and v.stuInfo.classId.id  in (:classIds)");
	    		values.put("classIds", classIds);
	     }
	    
	     return this.volunteerDao.query(hql.toString(), values);
		
	}

	/**
	 * 控件   ：分页获取志愿者信息
	 * @param vbm			志愿者基本信息对象
	 * @param pageNo		当前页码
	 * @param pageSize	分页大小
	 * @return						分页对象
	 */
	@Override
	public Page queryRadioVolunteer(VolunteerBaseinfoModel vbm,int pageNo,int pageSize,String teacherOrgId){
		return this.volunteerDao.queryRadioVolunteer(vbm, pageNo, pageSize, teacherOrgId);
	}
	
	/**
	 * 监管分析列表
	 */
	@Override
	public Page pageQueryVolunteerSummary(VolunteerSummaryInfoModel volunteerSummaryInfoModel, int pageNo, int pageSize){
		return this.volunteerDao.pageQueryVolunteerSummary(volunteerSummaryInfoModel, pageNo, pageSize);
		
	}
	
	/**
	 * 根据志愿者注册号查找志愿者
	 * @param registerNum
	 * @return
	 */
	@Override
	public VolunteerBaseinfoModel queryVolunteerByRegisterNum(
			String registerNum) {
		return (VolunteerBaseinfoModel)this.volunteerDao.queryUnique("from VolunteerBaseinfoModel where registerNum=?", registerNum);
	}
	/**
	 * 根据学年id转化为时间
	 * @param yearCode
	 * @return
	 * @throws ParseException 
	 */
	@Override
	public List<Date> changeYear(String yearId) {
		 Dic yearDic=dicService.getDic(yearId);
		 String yearStr=yearDic.getCode();
		 int yearMin=Integer.parseInt(yearStr);
		 int yearMax=yearMin+1;
	
		 String yearStrMin=yearMin+"-09-01";
		 String yearStrMax=yearMax+"-09-01";
		 SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		 List<Date> list=new ArrayList<Date>();
		try {
			Date yaerDateMin = dateFormat.parse(yearStrMin);
			list.add(yaerDateMin);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		try {
			Date yaerDateMax= dateFormat.parse(yearStrMax);
			list.add(yaerDateMax);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return list;
	}
}
