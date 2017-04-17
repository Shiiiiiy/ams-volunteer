package com.uws.volunteer.util;

import com.uws.sys.model.Dic;
import com.uws.sys.service.DicUtil;
import com.uws.sys.service.impl.DicFactory;

public class VolunteerConstants {
	/**
	 * 数据字典工具类
	 */
	private static DicUtil dicUtil=DicFactory.getDicUtil();
	


	public static final String NAMESPACE = "/volunteer";
	
	/**
	 * 志愿者报名【学生申请】
	 */
	public static final String NAMESPACE_APPLLY_REQUEST = "/volunteer/applyRequest";
	
	/**
	 * 志愿者报名【院青协审核】
	 */
	public static final String NAMESPACE_APPLLY_APPROVE = "/volunteer/applyApprove";
	
	/**
	 * 志愿者维护【院青协】
	 */
	public static final String NAMESPACE_MAINTAIN = "/volunteer/maintain";
	
	/**
	 * 志愿者查询【院青协】
	 */
	public static final String NAMESPACE_VIEW = "/volunteer/view";

	/**
	 * 志愿者基地维护【院青协】
	 */
	public static final String NAMESPACE_OFFICE = "/volunteer/office";
	
	/**
	 * 监管分析【校团委】
	 */
	public static final String NAMESPACE_STATISTIC = "/volunteer/statistic";
	/**
	 * 【系统数据字典_保存状态_保存】
	 */
	public static final Dic STATUS_SAVE_DICS=dicUtil.getDicInfo("STATUS","SAVE");
	
	/**
	 * 【系统数据字典_保存状态_提交】
	 */
	public static final Dic STATUS_SUBMIT_DICS=dicUtil.getDicInfo("STATUS","SUBMIT");
	/**
	 * 【审核通过】
	 */
	public static final Dic STATUS_PASS=dicUtil.getDicInfo("APPLY_APPROVE","PASS");
	/**
	 * 【审核拒绝】
	 */
	public static final Dic STATUS_REJECT=dicUtil.getDicInfo("APPLY_APPROVE","REJECT");
	/**
	 * 【未审核】
	 */
	public static final Dic STATUS_NOT_APPROVE=dicUtil.getDicInfo("APPLY_APPROVE","NOT_APPROVE");
	
	
}
