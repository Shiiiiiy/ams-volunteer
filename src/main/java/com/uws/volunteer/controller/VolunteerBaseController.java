package com.uws.volunteer.controller;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.uws.common.service.IActivityService;
import com.uws.common.service.IBaseDataService;
import com.uws.common.util.AmsDateUtil;
import com.uws.common.util.CYLeagueUtil;
import com.uws.common.util.Constants;
import com.uws.comp.service.ICompService;
import com.uws.core.base.BaseController;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.session.SessionFactory;
import com.uws.core.session.SessionUtil;
import com.uws.core.util.StringUtils;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.base.BaseMajorModel;
import com.uws.domain.base.BaseTeacherModel;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.domain.volunteer.VolunteerBaseinfoModel;
import com.uws.domain.volunteer.VolunteerHonorModel;
import com.uws.domain.volunteer.VolunteerHonorUserModel;
import com.uws.domain.volunteer.VolunteerOfficeModel;
import com.uws.domain.volunteer.VolunteerServiceModel;
import com.uws.domain.volunteer.VolunteerServiceUserModel;
import com.uws.domain.volunteer.VolunteerSummaryInfoModel;
import com.uws.sys.model.UploadFileRef;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.FileUtil;
import com.uws.sys.service.impl.DicFactory;
import com.uws.sys.service.impl.FileFactory;
import com.uws.user.model.User;
import com.uws.util.ProjectSessionUtils;
import com.uws.volunteer.service.IVolunteerService;
import com.uws.volunteer.util.VolunteerConstants;

/** 
* VolunteerBaseController
* @Description:志愿者基础业务控制类Controller
* @author zhangmx
* @date	   2015-12-02
*/
@Controller
public class VolunteerBaseController extends BaseController{
	
	@Autowired
	private IVolunteerService volunteerService;
	
	@Autowired
	private IBaseDataService baseDataService;
	
	@Autowired
	private ICompService compService;
	@Autowired
	private IActivityService activityService;//活动管理
	
	 //附件工具类
  	private FileUtil fileUtil=FileFactory.getFileUtil();

	//数据字典工具类
	private DicUtil dicUtil = DicFactory.getDicUtil();
	
	//session工具类
	private SessionUtil sessionUtil = SessionFactory.getSession(VolunteerConstants.NAMESPACE);
  	
	@InitBinder
    protected void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, true));
    }

	/**
	 * 志愿者个人信息维护列表
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vbm			志愿者实体类
	 * @param session		当前会话
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/maintain/opt-query/getVolunteerList")
	public String pageQueryMaintainVolunteer(ModelMap model,HttpServletRequest request,VolunteerBaseinfoModel vbm,HttpSession session){
		
		//获取当前用户所在的部门Id
		String teacherOrgId= ProjectSessionUtils.getCurrentTeacherOrgId(request);
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
	    model.addAttribute("isCollegeTeacher", isCollegeTeacher);
		if(vbm==null||vbm.getStuInfo()==null||"".equals(vbm.getStuInfo())){
			StudentInfoModel stu=new StudentInfoModel();
			BaseAcademyModel c=new BaseAcademyModel();
			c.setId(teacherOrgId);
			stu.setCollege(c);
			vbm.setStuInfo(stu);
		}
		
		session.setAttribute("vbm",vbm);
		model.addAttribute("vbm", vbm);
		model.addAttribute("teacherOrgId", teacherOrgId);
		int pageNo = request.getParameter("pageNo") != null ? Integer.valueOf(request.getParameter("pageNo")).intValue() : 1;
		Page page = this.volunteerService.pageQueryVolunteerInfo(vbm,pageNo,Page.DEFAULT_PAGE_SIZE,teacherOrgId);
	
		//专业
    	List<BaseMajorModel> majorList = null;
    	//班级
    	List<BaseClassModel> classList = null;
    	
    	if(vbm!= null&& !"".equals(vbm) && vbm.getStuInfo()!=null && !"".equals(vbm.getStuInfo())){
    		if(vbm.getStuInfo().getCollege()!=null &&
        		StringUtils.hasText(vbm.getStuInfo().getCollege().getId())){
        		majorList = compService.queryMajorByCollage(teacherOrgId);
        	}
        	if(vbm.getStuInfo().getMajor()!=null &&
        			StringUtils.hasText(vbm.getStuInfo().getMajor().getId())){
        		classList = compService.queryClassByMajor(vbm.getStuInfo().getMajor().getId());
        	}
    	}
    	model.addAttribute("page", page);
    	model.addAttribute("collegeList", collegeList);
		model.addAttribute("majorList", majorList);
		model.addAttribute("classList", classList);
		model.addAttribute("applyApproveList",dicUtil.getDicInfoList("APPLY_APPROVE"));
		return VolunteerConstants.NAMESPACE+"/maintain/volunteerMaintainList";
	}
	/**
	 * 志愿者个人信息维护编辑页面
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vbm			志愿者实体类
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/maintain/opt-edit/editVounteerManage")
	public String addVolunteer(ModelMap model,HttpServletRequest request,String id){
		//根据当前登录人的信息
		if(id!=null && !"".equals(id)){
			VolunteerBaseinfoModel vbm=this.volunteerService.queryVolunteerApplyById(id);
			model.addAttribute("vbm", vbm);
		}
		model.addAttribute("isNoList",dicUtil.getDicInfoList("Y&N"));

		return VolunteerConstants.NAMESPACE+"/maintain/volunteerManageEdit";
	}
	/**
	 * 保存志愿者个人信息维护
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vbm			志愿者实体类
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/maintain/opt-save/saveVounteerMaintain")
	public String saveVolunteer(ModelMap model,HttpServletRequest request,VolunteerBaseinfoModel vbm){
    	vbm.setApplyTime(new Date());
    	vbm.setApproveTime(new Date());
    	User user=new User();
    	user.setId(this.sessionUtil.getCurrentUserId());
    	vbm.setApproverPo(user);
    	vbm.setSuggest("通过");
    	vbm.setApproveResult(VolunteerConstants.STATUS_PASS);
    	vbm.setStatus(VolunteerConstants.STATUS_SUBMIT_DICS);
	
		if(vbm!=null && vbm.getId()!=null && !"".equals(vbm.getId())){
			
			this.volunteerService.updateVounteerApply(vbm);
		}else{
			this.volunteerService.addVounteerApply(vbm);
		}
		
		return "redirect:/volunteer/maintain/opt-query/getVolunteerList.do";

	}
	/**
	 * 志愿者荣誉列表
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vhm			志愿者荣誉实体
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/maintainHonor/opt-query/pageVolunteerHonorList")
	public String pageQueryMaintainHonorVolunteer(ModelMap model,HttpServletRequest request,VolunteerHonorModel vhm,HttpSession session){
		//获取当前用户所在的部门Id
		session.setAttribute("vhm", vhm);
		model.addAttribute("vhm", vhm);

		String teacherOrgId= ProjectSessionUtils.getCurrentTeacherOrgId(request);
		int pageNo = request.getParameter("pageNo") != null ? Integer.valueOf(request.getParameter("pageNo")).intValue() : 1;
		Page page = this.volunteerService.pageQueryVolunteerHonor(vhm,pageNo,Page.DEFAULT_PAGE_SIZE,teacherOrgId);
		// 下拉列表 学院
		List<BaseAcademyModel> collegeList = this.baseDataService.listBaseAcademy();
		model.addAttribute("collegeList", collegeList);
		model.addAttribute("page", page);
		model.addAttribute("yearList",dicUtil.getDicInfoList("YEAR"));
		model.addAttribute("termList",dicUtil.getDicInfoList("TERM"));
		model.addAttribute("honorTypeList",dicUtil.getDicInfoList("HONOR_TYPE"));

		return VolunteerConstants.NAMESPACE+"/maintain/volunteerHonorList";
	}
	
	/**
	 * 志愿者荣誉编辑页面
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param id					业务主键
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/maintainHonor/opt-edit/editHonor")
	public String editMaintainHonor(ModelMap model,HttpServletRequest request,String id){
		String teacherOrgId= ProjectSessionUtils.getCurrentTeacherOrgId(request);
		model.addAttribute("teacherOrgId", teacherOrgId);
		if(id!=null && !"".equals(id)){
			VolunteerHonorModel vhm=this.volunteerService.queryHonorById(id);
			model.addAttribute("vhm",vhm);
			List<VolunteerHonorUserModel> honorUserList=this.volunteerService.findHonorUserListByHonorId(id);
			String stuIds="";
			String stuNames="";
			for(int i=0;i<honorUserList.size();i++){
				if(null!=honorUserList.get(i).getVolunteerPo().getStuInfo()){
					if(i==0){
						
						stuIds=stuIds+honorUserList.get(0).getVolunteerPo().getStuInfo().getId();
						stuNames=stuNames+honorUserList.get(0).getVolunteerPo().getStuInfo().getName();
					}else{
						stuIds=stuIds+","+honorUserList.get(i).getVolunteerPo().getStuInfo().getId();
						stuNames=stuNames+","+honorUserList.get(i).getVolunteerPo().getStuInfo().getName();
					}
				}
				
			}
			model.addAttribute("stuIds",stuIds);
			model.addAttribute("stuNames",stuNames);
			List<UploadFileRef> uploadFileRefList=this.fileUtil.getFileRefsByObjectId(vhm.getId());
			model.addAttribute("uploadFileRefList",uploadFileRefList);
		}

		model.addAttribute("yearList",dicUtil.getDicInfoList("YEAR"));
		model.addAttribute("termList",dicUtil.getDicInfoList("TERM"));
		model.addAttribute("honorTypeList",dicUtil.getDicInfoList("HONOR_TYPE"));
		//学院
    	List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
    	for(BaseAcademyModel c:collegeList){
    		if(c.getId().equals(teacherOrgId)){
    			model.addAttribute("orgName", c.getName());
    		}
    	}
		return VolunteerConstants.NAMESPACE+"/maintain/volunteerHonorEdit";
	}
	
	/**
	 * 志愿者荣誉保存
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vhm			志愿者荣誉实体
	 * @param fileId			当前附件
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/maintainHonor/opt-save/saveHonor")
	public String saveMaintainHonor(ModelMap model,HttpServletRequest request,VolunteerHonorModel vhm,String[] fileId){
		//保存荣誉对象
		if(vhm!=null &&vhm.getId()!=null &&!"".equals(vhm.getId())){
			//更新荣誉对象
			this.volunteerService.updateHonor(vhm,fileId);
			//删除以前所有user
			List<VolunteerHonorUserModel> honorUserList=this.volunteerService.findHonorUserListByHonorId(vhm.getId());
			this.volunteerService.delHonorUserList(honorUserList);
		}else{
			//保存荣誉对象
			this.volunteerService.saveHonor(vhm,fileId);
		}
		String stuIds=request.getParameter("stuIds");
		String[] studentIds=stuIds.split(",");
		for(String stuNumber:studentIds){
			VolunteerBaseinfoModel volunteerPo=this.volunteerService.queryVolunteerByStuNumber(stuNumber);
			if(volunteerPo!=null){
				//保存现在所有user
				VolunteerHonorUserModel volunteerHonorUser=new VolunteerHonorUserModel();
				//将志愿者是否有荣誉字段改为是
				if(volunteerPo!=null &&( volunteerPo.getIsHavehonor()==null ||Constants.STATUS_NO.getId().equals(volunteerPo.getIsHavehonor().getId()))){
					volunteerPo.setIsHavehonor(Constants.STATUS_YES);
					this.volunteerService.save(volunteerPo);
				}
				volunteerHonorUser.setVolunteerPo(volunteerPo);
				volunteerHonorUser.setVolunteerHonorPo(vhm);
				this.volunteerService.save(volunteerHonorUser);
			}
		}
		return "redirect:/volunteer/maintainHonor/opt-query/pageVolunteerHonorList.do";
	}
	
	 /**
	  * 删除志愿者荣誉
	 * @param model		页面数据加载器
	 * @param request		页面请求
	  * @param id				业务主键
	  */
    @ResponseBody
    @RequestMapping(value={"/volunteer/maintainHonor/opt-del/deleteHonor"}, produces={"text/plain;charset=UTF-8"})
    public String deleteHonor(ModelMap model,HttpServletRequest request,String id){
    	VolunteerHonorModel honor=this.volunteerService.queryHonorById(id);
    	this.volunteerService.deleteHonor(honor);
    	return "success";
    }
    
	/**
	 * 志愿者服务维护列表
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vsm			志愿者服务实体类
	 * @param session		当前会话
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/maintainService/opt-query/pageVolunteerServiceList")
	public String pageQueryMaintainServiceVolunteer(ModelMap model,HttpServletRequest request,VolunteerServiceModel vsm,HttpSession session){
		//获取当前用户所在的部门Id
		String teacherOrgId= ProjectSessionUtils.getCurrentTeacherOrgId(request);
		String serviceDateStr=request.getParameter("serviceDateStr");
		String serviceHourStr=request.getParameter("serviceHourStr");
		
		if(serviceDateStr!=null &&!"".equals(serviceDateStr)){
			Date serviceDate=AmsDateUtil.toTime(serviceDateStr);
			vsm.setServiceDate(serviceDate);
		}
		if(serviceHourStr!=null &&!"".equals(serviceHourStr)){
			vsm.setServiceHourStr(serviceHourStr);
		}

		model.addAttribute("vsm", vsm);
		session.setAttribute("vsm", vsm);
		int pageNo = request.getParameter("pageNo") != null ? Integer.valueOf(request.getParameter("pageNo")).intValue() : 1;
		Page page = this.volunteerService.pageQueryVolunteerService(vsm,pageNo,Page.DEFAULT_PAGE_SIZE,teacherOrgId);
		// 下拉列表 学院
		List<BaseAcademyModel> collegeList = this.baseDataService.listBaseAcademy();
		model.addAttribute("collegeList", collegeList);
		model.addAttribute("page", page);
		return VolunteerConstants.NAMESPACE+"/maintain/volunteerServiceList";
	}
	
	/**
	 * 志愿者服务记录编辑页面
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param id					业务主键
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/maintainService/opt-edit/editService")
	public String editMaintainService(ModelMap model,HttpServletRequest request,String id){
		String teacherOrgId= ProjectSessionUtils.getCurrentTeacherOrgId(request);
		model.addAttribute("teacherOrgId", teacherOrgId);
		if(id!=null && !"".equals(id)){
			
			VolunteerServiceModel vsm=this.volunteerService.queryServiceById(id);
			model.addAttribute("vsm",vsm);
			List<VolunteerServiceUserModel> serviceUserList=this.volunteerService.findServiceUserListByServiceId(id);
			String stuIds="";
			String stuNames="";
			for(int i=0;i<serviceUserList.size();i++){
				if(i==0){
					stuIds=stuIds+serviceUserList.get(0).getVolunteerPo().getStuInfo().getId();
					stuNames=stuNames+serviceUserList.get(0).getVolunteerPo().getStuInfo().getName();
				}else{
					stuIds=stuIds+","+serviceUserList.get(i).getVolunteerPo().getStuInfo().getId();
					stuNames=stuNames+","+serviceUserList.get(i).getVolunteerPo().getStuInfo().getName();
				}
			}
			
			model.addAttribute("stuIds",stuIds);
			model.addAttribute("stuNames",stuNames);
			List<UploadFileRef> uploadFileRefList=this.fileUtil.getFileRefsByObjectId(vsm.getId());
			model.addAttribute("uploadFileRefList",uploadFileRefList);
		}
		//学院
    	List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
    	for(BaseAcademyModel c:collegeList){
    		if(c.getId().equals(teacherOrgId)){
    			model.addAttribute("orgName", c.getName());
    		}
    	}
		return VolunteerConstants.NAMESPACE+"/maintain/volunteerServiceEdit";
	}
	
	/**
	 * 志愿者服务记录保存
	 * @param model			页面数据加载器
	 * @param request			页面请求
	 * @param vsm				志愿服务实体类
	 * @param fileId				附件数组
	 * @return							指定视图
	 */
	@RequestMapping("/volunteer/maintainService/opt-save/saveService")
	public String saveMaintainService(ModelMap model,HttpServletRequest request,VolunteerServiceModel vsm,String[] fileId){
		//保存服务对象
		if(vsm!=null &&vsm.getId()!=null &&!"".equals(vsm.getId())){
			//更新服务对象
			this.volunteerService.updateService(vsm,fileId);
			//删除以前所有user
			List<VolunteerServiceUserModel> serviceUserList=this.volunteerService.findServiceUserListByServiceId(vsm.getId());
			this.volunteerService.delServiceUserList(serviceUserList);
		}else{
			//保存服务对象
			this.volunteerService.saveService(vsm,fileId);
		}
		String stuIds=request.getParameter("stuIds");
		String[] studentIds=stuIds.split(",");
		for(String stuNumber:studentIds){
			VolunteerBaseinfoModel volunteerPo=this.volunteerService.queryVolunteerByStuNumber(stuNumber);
			if(volunteerPo!=null){
				//保存现在所有user
				VolunteerServiceUserModel volunteerServiceUser=new VolunteerServiceUserModel();
				volunteerServiceUser.setVolunteerPo(volunteerPo);
				volunteerServiceUser.setVolunteerServicePo(vsm);
				this.volunteerService.save(volunteerServiceUser);
			}
		}
		return "redirect:/volunteer/maintainService/opt-query/pageVolunteerServiceList.do";
	}
	
	 /**
     * 删除志愿者服务
	 * @param model		页面数据加载器
	 * @param request		页面请求
     * @param id					业务主键
     */
    @ResponseBody
    @RequestMapping(value={"/volunteer/maintainService/opt-del/deleteService"}, produces={"text/plain;charset=UTF-8"})
    public String deleteService(ModelMap model,HttpServletRequest request,String id){
    	VolunteerServiceModel service=this.volunteerService.queryServiceById(id);
    	this.volunteerService.deleteService(service);
    	return "success";
    }
    
	/**
	 * 志愿者信息查询列表页面
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vbm			志愿者基础信息实体
	 * @param session		当前会话
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/volunteerSelect/opt-query/queryVolunteerList")
	public String selectVolunteerList(ModelMap model,HttpServletRequest request,VolunteerBaseinfoModel vbm,HttpSession session){
		//根据当前登录人的信息
 		String userId = sessionUtil.getCurrentUserId();
		//获取当前用户所在的部门Id
		String teacherOrgId= ProjectSessionUtils.getCurrentTeacherOrgId(request);
		//1.判断校青协？？--设置查询条件
		boolean isSchoolYouthClub=this.volunteerService.isRightRole(userId, "HKY_SCHOLL_YOUTHCLUB_LEADER");
		if(isSchoolYouthClub){
			model.addAttribute("isSchoolYouthClub", "true");
		}else{
			if(vbm==null||vbm.getStuInfo()==null||"".equals(vbm.getStuInfo())){
				StudentInfoModel stu=new StudentInfoModel();
				BaseAcademyModel c=new BaseAcademyModel();
				c.setId(teacherOrgId);
				stu.setCollege(c);
				vbm.setStuInfo(stu);
			}
			model.addAttribute("isSchoolYouthClub", "false");
		}
		session.setAttribute("vbm",vbm);
		model.addAttribute("vbm", vbm);
		model.addAttribute("teacherOrgId", teacherOrgId);
		int pageNo = request.getParameter("pageNo") != null ? Integer.valueOf(request.getParameter("pageNo")).intValue() : 1;
		Page page = this.volunteerService.pageQueryVolunteerInfo(vbm,pageNo,Page.DEFAULT_PAGE_SIZE,teacherOrgId);
		//学院
    	List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
    	//专业
    	List<BaseMajorModel> majorList = null;
    	//班级
    	List<BaseClassModel> classList = null;
    	
    	if(vbm!= null&& !"".equals(vbm) && vbm.getStuInfo()!=null && !"".equals(vbm.getStuInfo())){
    		if(vbm.getStuInfo().getCollege()!=null &&
        		StringUtils.hasText(vbm.getStuInfo().getCollege().getId())){
        		majorList = compService.queryMajorByCollage(vbm.getStuInfo().getCollege().getId());
        	}
        	if(vbm.getStuInfo().getMajor()!=null &&
        			StringUtils.hasText(vbm.getStuInfo().getMajor().getId())){
        		classList = compService.queryClassByMajor(vbm.getStuInfo().getMajor().getId());
        	}
    	}
    	model.addAttribute("page", page);
    	model.addAttribute("collegeList", collegeList);
		model.addAttribute("majorList", majorList);
		model.addAttribute("classList", classList);
		model.addAttribute("applyApproveList",dicUtil.getDicInfoList("APPLY_APPROVE"));
		return VolunteerConstants.NAMESPACE+"/maintain/volunteerSelectList";
	}
	
	/**
	 * 志愿者个人信息查看
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param id					业务主键
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/view/opt-view/volunteerView")
	public String volunteerView(ModelMap model,HttpServletRequest request,String id){
		if(id!=null){
			VolunteerBaseinfoModel vbm=this.volunteerService.queryVolunteerApplyById(id);
			model.addAttribute("vbm", vbm);
			
			//根据志愿者id 查找 荣誉List
			List<VolunteerHonorModel> honorList=this.volunteerService.queryHonorListByVolunteerId(id);
			List<List<UploadFileRef>> honorFilelist=new ArrayList<List<UploadFileRef>>();
			for(VolunteerHonorModel honor:honorList){
				//志愿者荣誉附件
				List<UploadFileRef> hfileList=this.fileUtil.getFileRefsByObjectId(honor.getId());
				honorFilelist.add(hfileList);
			}
			model.addAttribute("honorFilelist",honorFilelist);
			//根据志愿者id 查找 服务List
			List<List<UploadFileRef>> serviceFilelist=new ArrayList<List<UploadFileRef>>();
			List<VolunteerServiceModel> serviceList=this.volunteerService.queryServiceListByVolunteerId(id);
			for(VolunteerServiceModel service:serviceList){
				//志愿者服务附件
				List<UploadFileRef> sFileList=this.fileUtil.getFileRefsByObjectId(service.getId());
				serviceFilelist.add(sFileList);
			}
			model.addAttribute("serviceFilelist",serviceFilelist);
			model.addAttribute("honorList", honorList);
			model.addAttribute("serviceList", serviceList);
		}
		model.addAttribute("isNoList",dicUtil.getDicInfoList("Y&N"));
		return VolunteerConstants.NAMESPACE+"/maintain/volunteerView";
	}
	
	/**
	 *  志愿者基地信息列表页面
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vom			志愿者基地实体类
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/office/opt-query/getVolunteerOfficeList")
	public String getVolunteerOfficeList(ModelMap model,HttpServletRequest request,VolunteerOfficeModel vom,HttpSession session){
		
		String teacherOrgId= ProjectSessionUtils.getCurrentTeacherOrgId(request);
		model.addAttribute("teacherOrgId", teacherOrgId);
		//学院
	    List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
		//判断是院团委
		boolean isCollegeLeague=this.volunteerService.isRightRole(this.sessionUtil.getCurrentUserId(),CYLeagueUtil.CYL_ROLES.HKY_COLLEGE_LEAGUE_LEADER.toString());
		if(isCollegeLeague){
			BaseAcademyModel college=new BaseAcademyModel();
			college.setId(teacherOrgId);
			vom.setCollege(college);
			model.addAttribute("isCollegeLeague", "true");
			 //1.判断当前登录人的机构是不是在学院中
		     String isCollegeTeacher="false";
		     for(BaseAcademyModel co:collegeList){
		    		if(teacherOrgId!=null&& teacherOrgId.equals(co.getId())){
		    			isCollegeTeacher="true";
		    			break ;
		    		}
		     }
		    model.addAttribute("isCollegeTeacher", isCollegeTeacher);
		}
		session.setAttribute("vom", vom);
		model.addAttribute("vom", vom);
		int pageNo = request.getParameter("pageNo") != null ? Integer.valueOf(request.getParameter("pageNo")).intValue() : 1;
		Page page = this.volunteerService.pageQueryVolunteerOfficeInfo(vom,pageNo,Page.DEFAULT_PAGE_SIZE,teacherOrgId);
		//获取在活动中被使用的基地信息--只能被修改 不能删除
		List<String> activityOfficeIdList=this.activityService.queryActivityVolunteerOfficeList();
		List<VolunteerOfficeModel> newResultList = new ArrayList<VolunteerOfficeModel>();
 		List<VolunteerOfficeModel> resultList = (List<VolunteerOfficeModel>)page.getResult();
 		
		for(VolunteerOfficeModel v:resultList){
			if(activityOfficeIdList.size()>0){
 				for(String s:activityOfficeIdList){
 					v.setComments("canDel");
 					if(v.getId().equals(s)){
 						v.setComments("notDel");
 						break;
 			 		}
 				}
			}else{
				v.setComments("canDel");
			}
				
 				newResultList.add(v);
		}
 		
		page.setResult(newResultList);
		model.addAttribute("activityOfficeIdList", activityOfficeIdList);
		// 下拉列表 学院
		model.addAttribute("collegeList", collegeList);
		model.addAttribute("page", page);
		return VolunteerConstants.NAMESPACE_OFFICE+"/volunteerOfficeList";
	}
	
	/**
	 * 编辑基地页面
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param id					业务主键
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/office/opt-query/editVolunteerOffice")
	public String editVolunteerOffice(ModelMap model,HttpServletRequest request,String id){
		//根据当前登录人的信息
 		String userId = sessionUtil.getCurrentUserId();
 		String teacherOrgId= ProjectSessionUtils.getCurrentTeacherOrgId(request);
 		
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
	    model.addAttribute("isCollegeTeacher", isCollegeTeacher);
	    if("true".equals(isCollegeTeacher)){
	    	 model.addAttribute("teacherOrgId", teacherOrgId);
	    }
		//根据当前登录人获得对象
		BaseTeacherModel currentUser=this.baseDataService.findTeacherById(userId);
		model.addAttribute("currentUser", currentUser);
		if(id!=null && !"".equals(id)){
			VolunteerOfficeModel vom=this.volunteerService.queryVolunteerOfficeById(id);
			model.addAttribute("vom", vom);
		}
		//学院
    	model.addAttribute("collegeList", collegeList);
		return VolunteerConstants.NAMESPACE_OFFICE+"/volunteerOfficeEdit";
	}
	
	/**
	 * 保存志愿者基地office页面
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vom			志愿者基地实体
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/office/opt-save/saveOffice")
	public String saveOffice(ModelMap model,HttpServletRequest request,VolunteerOfficeModel vom){
		if(vom!=null && vom.getId()!=null && !"".equals(vom.getId())){
			this.volunteerService.updateVolunteerOffice(vom);
		}else{
			this.volunteerService.saveVolunteerOffice(vom);
		}
		return "redirect:/volunteer/office/opt-query/getVolunteerOfficeList.do";
	}
	
	 /**
     * 删除志愿者基地
	 * @param model		页面数据加载器
	 * @param request		页面请求
     * @param id					业务主键
     */
    @ResponseBody
    @RequestMapping(value={"/volunteer/office/opt-del/deleteOffice"}, produces={"text/plain;charset=UTF-8"})
    public String deleteOffice(ModelMap model,HttpServletRequest request,String id){
    	//获取在活动中被使用的基地信息
		List<String> activityOfficeIdList=this.activityService.queryActivityVolunteerOfficeList();
		for(String vomId:activityOfficeIdList){
			if(id.equals(vomId)){
				return "notDelete";
			}
		}
    	VolunteerOfficeModel office=this.volunteerService.queryVolunteerOfficeById(id);
    	this.volunteerService.deleteOffice(office);//逻辑删除
    	return "success";
    }
    
    /**
     * 志愿者监管分析
	 * @param model				页面数据加载器
	 * @param request				页面请求
     * @param vsim					志愿者监管分析实体
     * @param honorYear		学年
     * @param honorTerm		学期
	 * @return								指定视图
     */
	@RequestMapping(VolunteerConstants.NAMESPACE_STATISTIC+"/opt-query/summaryVolunteerInfo")
	public String summaryVolunteerInf(ModelMap model,HttpServletRequest request,VolunteerSummaryInfoModel vsim,String yearId){
		
		int pageNo = request.getParameter("pageNo") != null ? Integer.valueOf(request.getParameter("pageNo")).intValue() : 1;
		Page page = this.volunteerService.pageQueryVolunteerSummary(vsim,pageNo,Page.DEFAULT_PAGE_SIZE);
		Collection<VolunteerSummaryInfoModel> volunteerSummarylist = page.getResult();
		
		for(VolunteerSummaryInfoModel volunteerSummary: volunteerSummarylist){
			volunteerSummary.setServiceNums(this.volunteerService.queryVolunteerService(volunteerSummary.getCollege().getId(),yearId).size());
			volunteerSummary.setTrainingNums(this.volunteerService.queryVolunteerTraining(volunteerSummary.getCollege().getId()).size());
			volunteerSummary.setHonorNums(this.volunteerService.queryVolunteerHonor(volunteerSummary.getCollege().getId(), yearId).size());
			volunteerSummary.setVolunteerNums(this.volunteerService.queryVolunteerPass(volunteerSummary.getCollege().getId()).size());
			List<VolunteerServiceModel> serviceList=this.volunteerService.queryVolunteerServiceHour(volunteerSummary.getCollege().getId(),yearId);
			Double sumHour=0.0;
			for(VolunteerServiceModel v:serviceList){
				sumHour=sumHour+v.getServiceHour()*v.getServiceStuNums();
			}
			volunteerSummary.setServiceHourNums(sumHour);

		}
		model.addAttribute("yearId",yearId);
		model.addAttribute("yearList",dicUtil.getDicInfoList("YEAR"));
		List<BaseAcademyModel> collegeList=baseDataService.listBaseAcademy();
		model.addAttribute("collegeList", collegeList);
		model.addAttribute("page", page);
		return VolunteerConstants.NAMESPACE_STATISTIC+"/volunteerSummaryList";
	}

	/**
	 * 志愿者查询， 组件查询
	 * @param model				页面数据加载器
	 * @param request				页面请求
	 * @param volunteer			志愿者基础信息实体
	 * @param selectedStuId 选择的学生id
	 * @param formId				表单id
	 * @param queryFlag		查询标识
	 * @return								指定视图
	 */
	@SuppressWarnings("unchecked")
    @RequestMapping(value={"/volunteer/volunteerRadio/nsm/queryRadioVolunteer"})
	public String queryVolunteer(ModelMap model, HttpServletRequest request,VolunteerBaseinfoModel volunteer,String selectedStuId,String formId,String queryFlag) 
	{
		//获取当前用户所在的部门Id
		String teacherOrgId= ProjectSessionUtils.getCurrentTeacherOrgId(request);
		
		int pageNo = request.getParameter("pageNo") != null ? Integer.valueOf(request.getParameter("pageNo")).intValue() : 1;
		Page page=this.volunteerService.queryRadioVolunteer(volunteer, pageNo, 5, teacherOrgId);
		model.addAttribute("page", page);
		Collection<VolunteerBaseinfoModel> list = page.getResult();
		for( VolunteerBaseinfoModel vbm : list )
		{
			vbm.setVolunteerInfo(
					new StringBuffer()
					.append("{id:'").append(vbm.getStuInfo().getId()).append("',")
					.append("name:'").append(vbm.getStuInfo().getName()).append("',")
					.append("bankCode:'").append(vbm.getStuInfo().getBankCode()).append("',")
					.append("className:'").append(vbm.getStuInfo().getClassId().getClassName()).append("',")
					.append("classId:'").append(vbm.getStuInfo().getClassId().getId()).append("',")
					.append("majorId:'").append(vbm.getStuInfo().getMajor().getId()).append("',")
					.append("majorName:'").append(vbm.getStuInfo().getMajor().getMajorName()).append("',")
					.append("genderId:'").append(vbm.getStuInfo().getGenderDic().getId()).append("',")
					.append("genderName:'").append(vbm.getStuInfo().getGenderDic().getName()).append("',")
					.append("sourceLandId:'").append(vbm.getStuInfo().getSourceLand()).append("',")
					.append("sourceLandName:'").append(vbm.getStuInfo().getSourceLand()).append("',")
					.append("nativeId:'").append(null == vbm.getStuInfo().getNativeDic() ? "" : vbm.getStuInfo().getNativeDic().getId()).append("',")
					.append("nativeName:'").append(null == vbm.getStuInfo().getNativeDic() ? "" : vbm.getStuInfo().getNativeDic().getName()).append("',")
					.append("birthDay:'").append(vbm.getStuInfo().getBrithDate()).append("',")
					.append("collegeId:'").append(vbm.getStuInfo().getCollege().getId()).append("',")
					.append("collegeName:'").append(vbm.getStuInfo().getCollege().getName()).append("'}")
					.toString()
			);
			
		}
		model.addAttribute("selectedId", selectedStuId);
		model.addAttribute("formId", formId);
		model.addAttribute("queryFlag", queryFlag);
		model.addAttribute("volunteer", volunteer);
		
		if("radio".equalsIgnoreCase(queryFlag))
			return "/comp/student/studentRadioTable";
		else
			return "/volunteer/maintain/volunteerCheckTable";
					
	}
	
	/**
	 * 注册号是否重复
	 * @param id							业务主键
	 * @param registerNum	注册号
	 */
	 @ResponseBody
	 @RequestMapping(value={"/volunteer/applyApprove/opt-approve/isRepeatRegisterNum"},produces = { "text/plain;charset=UTF-8" })
	 public String isRepeatRegisterNum(String registerNum,String id){
		VolunteerBaseinfoModel volunteer=this.volunteerService.queryVolunteerByRegisterNum(registerNum);
		if(volunteer!=null && id!=null && !id.equals(volunteer.getId())){
			//重复
			return "false";
		}else{
			return "success";
		}
		
	 }
}
