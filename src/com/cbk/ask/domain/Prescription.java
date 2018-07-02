package com.cbk.ask.domain;

import java.io.Serializable;


public class Prescription implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String hospital;//医院
	private String no;//问题ID
	private String date;//开票日期
	private String name;// 姓名
	private String sex;// 性别
	private String age;// 年龄
	private String keshi;// 科室
	private String phone;// 电话
	private String yaopin ;// 药品信息
	private String yishi;//医师
	private String yaoshi;//药师
	private String preNo;//处方编号
	public String getHospital() {
		return hospital;
	}
	public void setHospital(String hospital) {
		this.hospital = hospital;
	}
	public String getNo() {
		return no;
	}
	public void setNo(String no) {
		this.no = no;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSex() {
		return sex;
	}
	public void setSex(String sex) {
		this.sex = sex;
	}
	public String getAge() {
		return age;
	}
	public void setAge(String age) {
		this.age = age;
	}
	public String getKeshi() {
		return keshi;
	}
	public void setKeshi(String keshi) {
		this.keshi = keshi;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getYaopin() {
		return yaopin;
	}
	public void setYaopin(String yaopin) {
		this.yaopin = yaopin;
	}
	public String getYishi() {
		return yishi;
	}
	public void setYishi(String yishi) {
		this.yishi = yishi;
	}
	public String getYaoshi() {
		return yaoshi;
	}
	public void setYaoshi(String yaoshi) {
		this.yaoshi = yaoshi;
	}
	public String getPreNo() {
		return preNo;
	}
	public void setPreNo(String preNo) {
		this.preNo = preNo;
	}
	
}
