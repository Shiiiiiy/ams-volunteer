package com.uws.volunteer.controller;

import java.text.SimpleDateFormat;
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

import com.uws.common.service.IBaseDataService;
import com.uws.common.service.ICommonApproveService;
import com.uws.common.service.IStudentCommonService;
import com.uws.comp.service.ICompService;
import com.uws.core.base.BaseController;
import com.uws.core.hibernate.dao.support.Page;
import com.uws.core.session.SessionFactory;
import com.uws.core.session.SessionUtil;
import com.uws.core.util.StringUtils;
import com.uws.domain.base.BaseAcademyModel;
import com.uws.domain.base.BaseClassModel;
import com.uws.domain.base.BaseMajorModel;
import com.uws.domain.common.CommonApproveComments;
import com.uws.domain.orientation.StudentInfoModel;
import com.uws.domain.volunteer.VolunteerBaseinfoModel;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.impl.DicFactory;
import com.uws.user.model.User;
import com.uws.user.service.IUserService;
import com.uws.util.ProjectSessionUtils;
import com.uws.volunteer.service.IVolunteerService;
import com.uws.volunteer.util.VolunteerConstants;

/** 
* VolunteerBaseController
* @Description:志愿者申请审核控制类Controller
* @author zhangmx
* @date	   2015-12-02
*/
@Controller
public class VolunteerApplyController extends BaseController{

	@Autowired
	private IVolunteerService volunteerService;
	@Autowired
	private IBaseDataService baseDataService;
	@Autowired
	private IStudentCommonService studentCommonService;
	@Autowired
	private ICompService compService;
  	@Autowired
	private IUserService userService;
  	@Autowired
	private ICommonApproveService commonApproveService;
  	
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
	 * 志愿者申请列表
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vbm			志愿者实体类
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/applyRequest/opt-query/getVolunteerApplyList")
	public String getVolunteerApplyList(ModelMap model,HttpServletRequest request,VolunteerBaseinfoModel vbm){
		
		//根据当前登录人的信息
 		String userId = sessionUtil.getCurrentUserId();
		int pageNo = request.getParameter("pageNo") != null ? Integer.valueOf(request.getParameter("pageNo")).intValue() : 1;
		Page page = this.volunteerService.pageQueryVolunteerApplyInfo(vbm,pageNo,Page.DEFAULT_PAGE_SIZE,userId);
		// 下拉列表 学院
		List<BaseAcademyModel> collegeList = this.baseDataService.listBaseAcademy();
		model.addAttribute("collegeList", collegeList);
		model.addAttribute("page", page);
		model.addAttribute("applyApproveList",dicUtil.getDicInfoList("APPLY_APPROVE"));
		return VolunteerConstants.NAMESPACE+"/apply/volunteerApplyList";
	}
	
	/**
	 * 编辑志愿者申请页面
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vbm			志愿者实体类
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/applyRequest/opt-apply/addVounteer")
	public String addVolunteer(ModelMap model,HttpServletRequest request){
		//根据当前登录人的信息
 		String userId = sessionUtil.getCurrentUserId();
 		VolunteerBaseinfoModel vbm=this.volunteerService.queryVolunteerByStuNumber(userId);
 		if(vbm==null||"".equals(vbm.getId())){
 			StudentInfoModel currentStu = studentCommonService.queryStudentById(userId);
 			model.addAttribute("currentStu", currentStu);
 		}else{
 			model.addAttribute("vbm", vbm);
 		}
 		 //判断当前登录人是否“学生”
	    boolean isStudent=ProjectSessionUtils.checkIsStudent(request);
	    if(isStudent){
	    	model.addAttribute("isStudent",isStudent);
	    }
		
	
		model.addAttribute("isNoList",dicUtil.getDicInfoList("Y&N"));

		return VolunteerConstants.NAMESPACE+"/apply/volunteerApplyEdit";
	}
	
	/**
	 * 保存志愿者申请
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vbm			志愿者实体类
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/applyRequest/opt-save/saveVounteer")
	public String saveVolunteer(ModelMap model,HttpServletRequest request,VolunteerBaseinfoModel vbm){
		 //判断当前登录人是否“学生”
	    boolean isStudent=ProjectSessionUtils.checkIsStudent(request);
	    if(isStudent){
	    	String flags=request.getParameter("flags");
			if("1".equals(flags)){
				//提交
				vbm.setStatus(VolunteerConstants.STATUS_SUBMIT_DICS);
				vbm.setApproveResult(VolunteerConstants.STATUS_NOT_APPROVE);
			}else{
				//保存
				vbm.setStatus(VolunteerConstants.STATUS_SAVE_DICS);
			}
	    }else{
	    	vbm.setApplyTime(new Date());
	    	vbm.setApproveTime(new Date());
	    	User user=new User();
	    	user.setId(this.sessionUtil.getCurrentUserId());
	    	vbm.setApproverPo(user);
	    	vbm.setSuggest("通过");
	    	vbm.setApproveResult(VolunteerConstants.STATUS_PASS);
	    	vbm.setStatus(VolunteerConstants.STATUS_SUBMIT_DICS);
	    }
		
		if(vbm!=null && vbm.getId()!=null && !"".equals(vbm.getId())){
			
			this.volunteerService.updateVounteerApply(vbm);
		}else{
			this.volunteerService.addVounteerApply(vbm);
		}
		if(isStudent){
			return "redirect:/volunteer/applyRequest/opt-apply/addVounteer.do";
		}else{
			return "redirect:/volunteer/maintain/opt-query/getVolunteerList.do";

		}
	}
	
	/**
	 * 删除志愿者申请
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vbm			志愿者实体类
	 */
	@ResponseBody
	@RequestMapping("/volunteer/applyRequest/opt-del/delVounteer")
	public String delVolunteer(ModelMap model,HttpServletRequest request,String id){
		VolunteerBaseinfoModel vbm=this.volunteerService.queryVolunteerApplyById(id);
		this.volunteerService.delVolunteerApply(vbm);
	  
    	return "success";
	}

	/**
	 * 志愿者审核列表
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vbm			志愿者实体类
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/applyApprove/opt-query/getSubmitApplyList")
	public String getSubmitApplyList(ModelMap model,HttpServletRequest request,VolunteerBaseinfoModel vbm,HttpSession session){
		//获取当前用户所在的部门Id
		String teacherOrgId= ProjectSessionUtils.getCurrentTeacherOrgId(request);
		//设置查询条件
		if(vbm==null||vbm.getStuInfo()==null||"".equals(vbm.getStuInfo())){
			StudentInfoModel stu=new StudentInfoModel();
			BaseAcademyModel c=new BaseAcademyModel();
			c.setId(teacherOrgId);
			stu.setCollege(c);
			vbm.setStuInfo(stu);
		}
		if(vbm==null||vbm.getApproveResult()==null){
			vbm.setApproveResult(VolunteerConstants.STATUS_NOT_APPROVE);
		}
		session.setAttribute("vbm",vbm);
		model.addAttribute("vbm", vbm);
		model.addAttribute("teacherOrgId", teacherOrgId);
		int pageNo = request.getParameter("pageNo") != null ? Integer.valueOf(request.getParameter("pageNo")).intValue() : 1;
		Page page = this.volunteerService.pageQuerySubmitApplyInfo(vbm,pageNo,Page.DEFAULT_PAGE_SIZE,teacherOrgId);
		//学院
    	List<BaseAcademyModel> collegeList = baseDataService.listBaseAcademy();
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
		return VolunteerConstants.NAMESPACE+"/apply/volunteerApproveList";
	}
	
	/**
	 * 编辑志愿者审核页面
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vbm			志愿者实体类
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/applyApprove/opt-update/editApprove")
	public String editApprove(ModelMap model,HttpServletRequest request,String id){
 		
		if(id!=null && !"".equals(id)){
			VolunteerBaseinfoModel vbm=this.volunteerService.queryVolunteerApplyById(id);
			model.addAttribute("vbm", vbm);
		}
		model.addAttribute("isNoList",dicUtil.getDicInfoList("Y&N"));

		return VolunteerConstants.NAMESPACE+"/apply/volunteerApproveEdit";
	}
	
	/**
	 * 保存志愿者审核
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vbm			志愿者实体类
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/applyApprove/opt-save/saveApprove")
	public String saveApprove(ModelMap model,HttpServletRequest request){
		//根据当前登录人的信息
 		String userId = sessionUtil.getCurrentUserId();
		String id=request.getParameter("id");
		String suggest=request.getParameter("suggest");
		String registerNum=request.getParameter("registerNum");
		String approveKey=request.getParameter("approveKey");
		VolunteerBaseinfoModel vbmPo=this.volunteerService.queryVolunteerApplyById(id);
		if("PASS".equals(approveKey)){
			vbmPo.setApproveResult(VolunteerConstants.STATUS_PASS);
			vbmPo.setRegisterNum(registerNum);
		}else if("REJECT".equals(approveKey)){
			vbmPo.setApproveResult(VolunteerConstants.STATUS_REJECT);
		}
		vbmPo.setSuggest(suggest);
		vbmPo.setApproveTime(new Date());
		User user=new User();
		user.setId(userId);
		vbmPo.setApproverPo(user);
		this.volunteerService.update(vbmPo);
		// 封装审核信息
		this.saveApproveInfo(vbmPo,userId);
		return "redirect:/volunteer/applyApprove/opt-query/getSubmitApplyList.do";
	}
	
	/**
	 * 志愿者申请批量审核
	 * @param model		页面数据加载器
	 * @param request		页面请求
	 * @param vbm			志愿者实体类
	 * @param ids				待审核志愿者主键集合
	 * @param status			审批结果【PASS、REJECT】
	 * @return						指定视图
	 */
	@RequestMapping("/volunteer/volunteerMutiApprove/opt-approve/volunteerMutiApprove")
	public String volunteerMutiApprove(ModelMap model,HttpServletRequest request,String[] ids,String status){
		//根据当前登录人的信息
 		String userId = sessionUtil.getCurrentUserId();
		
		if(ids!=null &&!"".equals(ids)){
			for(int i=0;i<ids.length;i++){
				VolunteerBaseinfoModel vbmPo=this.volunteerService.queryVolunteerApplyById(ids[i]);
				if("PASS".equals(status)){
					vbmPo.setApproveResult(VolunteerConstants.STATUS_PASS);
					vbmPo.setSuggest("通过");
				}else if("REJECT".equals(status)){
					vbmPo.setApproveResult(VolunteerConstants.STATUS_REJECT);
					vbmPo.setSuggest("拒绝");
				}
				vbmPo.setApproveTime(new Date());
				User user=new User();
				user.setId(userId);
				vbmPo.setApproverPo(user);
				this.volunteerService.update(vbmPo);
				// 封装审核信息
				this.saveApproveInfo(vbmPo,userId);
			}
		}
	
		return "redirect:/volunteer/applyApprove/opt-query/getSubmitApplyList.do";
	}
	
	/**
	 * 封装审核信息
	 * @param vbmPo		志愿者基础信息
	 * @param userId		志愿者用户id
	 */
	public void saveApproveInfo( VolunteerBaseinfoModel vbmPo,String userId){
		// 封装审核信息
		CommonApproveComments ac = new CommonApproveComments();
		// 审核结果
		ac.setApproveOpinion(vbmPo.getApproveResult().getName());
		// 审核人
		ac.setApprover(userService.getUserById(userId));
		// 审核时间
		ac.setApproveTime(new Date());
		// 审核意见
		ac.setApproveComments(vbmPo.getSuggest());
		// 业务主键
		ac.setObjectId(vbmPo.getId());
		commonApproveService.saveApproveComments(ac);
	}
	
}
