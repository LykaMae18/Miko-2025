/*     */ package com.indracompany.sysad.main.enrollment;
/*     */ 
/*     */ import com.indracompany.acrsal.api.general.UserService;
/*     */ import com.indracompany.acrsal.dao.business.UserDao;
/*     */ import com.indracompany.acrsal.model.sysad.BIRUser;
/*     */ import com.indracompany.acrsal.model.sysad.RoleAccess;
/*     */ import com.indracompany.acrsal.models.AuditTrail;
/*     */ import com.indracompany.acrsal.models.ContactInformation;
/*     */ import com.indracompany.acrsal.models.FullName;
/*     */ import com.indracompany.acrsal.models.ParameterizedObject;
/*     */ import com.indracompany.acrsal.models.SecurityProfile;
/*     */ import com.indracompany.core.mail.mailer.TemplateMailer;
/*     */ import com.indracompany.core.security.model.User;
/*     */ import com.indracompany.sysad.api.enrollment.AccountEnrollmentService;
/*     */ import com.indracompany.sysad.dao.UserManagementDAO;
/*     */ import com.indracompany.sysad.dao.rdo.RdoAccessesDao;
/*     */ import com.indracompany.sysad.dao.rdo.RdoAccessesHistDao;
/*     */ import com.indracompany.sysad.dao.role.RoleAccessDao;
/*     */ import com.indracompany.sysad.forms.UserAccountForm;
/*     */ import java.text.ParseException;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.apache.log4j.Logger;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class AccountEnrollmentServiceImpl
/*     */   implements AccountEnrollmentService
/*     */ {
/*  33 */   private static final Logger log = Logger.getLogger(AccountEnrollmentServiceImpl.class);
/*     */   
/*     */   private static final long serialVersionUID = 1L;
/*     */   
/*     */   private TemplateMailer sysadUserDetailsMailer;
/*     */   
/*     */   private UserDao userDao;
/*     */   
/*     */   private UserManagementDAO userManagementDAO;
/*     */   
/*     */   private RoleAccessDao roleAccessDao;
/*     */   private RdoAccessesDao rdoAccessesDao;
/*     */   private RdoAccessesHistDao rdoAccessHistDao;
/*     */   private UserService userService;
/*     */   
/*     */   public String addAccountEnrollment(Map<String, Object> param) {
/*  49 */     Date VALIDITY_DATE_FROM = null, VALIDITY_DATE_TO = null;
/*     */     
/*     */     try {
/*  52 */       VALIDITY_DATE_FROM = (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).parse("1900-01-01 00:00:00");
/*  53 */       VALIDITY_DATE_TO = (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).parse("2100-12-31 00:00:00");
/*     */     }
/*  55 */     catch (ParseException e) {
/*     */       
/*  57 */       log.info(e);
/*     */     } 
/*     */     
/*  60 */     UserAccountForm userAccountForm = (UserAccountForm)param.get("USER_ACCOUNT_FORM");
/*  61 */     AuditTrail auditTrail = (AuditTrail)param.get("AUDIT_TRAIL");
/*  62 */     FullName fullName = userAccountForm.getFullName();
/*  63 */     String userFullName = (!fullName.getLastName().equals("") ? fullName.getLastName() : " , ") + (!fullName.getFirstName().equals("") ? fullName.getFirstName() : " ") + (!fullName.getMiddleName().equals("") ? fullName.getMiddleName() : " ");
/*     */ 
/*     */     
/*  66 */     String originalPassword = userAccountForm.getPassword();
/*  67 */     String sysType = userAccountForm.getSystemType().getValue();
/*  68 */     String loginType = "140".equals(sysType) ? "002" : ("141".equals(sysType) ? "003" : "001");
/*     */ 
/*     */     
/*  71 */     userAccountForm.getOffice().setKey(userAccountForm.getOffice().getValue());
/*  72 */     userAccountForm.getDivision().setKey(userAccountForm.getDivision().getValue());
/*     */ 
/*     */     
/*  75 */     param.put("USER_NAME", userAccountForm.getUsername());
/*  76 */     param.put("USER_PASSWORD", userAccountForm.getPassword());
/*  77 */     param.put("LOGIN_TYPE", loginType);
/*  78 */     param.put("MENU_ACCESS", "1");
/*  79 */     param.put("VALIDITY_DATE_FROM", VALIDITY_DATE_FROM);
/*  80 */     param.put("VALIDITY_DATE_TO", VALIDITY_DATE_TO);
/*  81 */     param.put("TRANS_TYPE", "040");
/*     */     
/*  83 */     SecurityProfile spEncrypter = new SecurityProfile(auditTrail.getUserSecurity().getUsername(), userAccountForm.getPassword(), auditTrail.getUserSecurity().getAuthorities());
/*     */     
/*  85 */     this.userService.encryptPassword((User)spEncrypter);
/*     */ 
/*     */     
/*  88 */     BIRUser birUser = new BIRUser();
/*  89 */     ContactInformation contactInfo = new ContactInformation();
/*  90 */     contactInfo.setEmailAddress(userAccountForm.getEmail());
/*  91 */     birUser.setContactDetail(contactInfo);
/*     */     
/*  93 */     ParameterizedObject paramLoginType = new ParameterizedObject();
/*  94 */     paramLoginType.setKey(loginType);
/*  95 */     birUser.setLoginType(paramLoginType);
/*  96 */     birUser.setDateCreated(new Date());
/*  97 */     birUser.setFullName(userAccountForm.getFullName());
/*  98 */     birUser.setSecurityProfile(spEncrypter);
/*  99 */     birUser.getSecurityProfile().setAccountNonExpired(true);
/* 100 */     birUser.getSecurityProfile().setAccountNonLocked(true);
/*     */ 
/*     */ 
/*     */     
/* 104 */     birUser.getSecurityProfile().setCredentialsNonExpired(false);
/*     */ 
/*     */     
/* 107 */     birUser.getSecurityProfile().setEnabled(true);
/* 108 */     birUser.getSecurityProfile().setUsername(userAccountForm.getUsername());
/* 109 */     birUser.getSecurityProfile().setPassword(spEncrypter.getPassword());
/* 110 */     birUser.getSecurityProfile().setLoginName(userAccountForm.getUsername());
/* 111 */     birUser.getSecurityProfile().setProfile(auditTrail.getUserSecurity().getProfile());
/*     */ 
/*     */ 
/*     */     
/* 115 */     SecurityProfile spEncrypterSecAns = new SecurityProfile(userAccountForm.getUsername(), userAccountForm.getUsername(), auditTrail.getUserSecurity().getAuthorities());
/*     */     
/* 117 */     this.userService.encryptPassword((User)spEncrypterSecAns);
/*     */     
/* 119 */     birUser.getSecurityProfile().setSecurityQuestion(new ParameterizedObject("", "008", ""));
/* 120 */     birUser.getSecurityProfile().setSecurityAnswer(spEncrypterSecAns.getPassword());
/*     */     
/* 122 */     birUser.setAuditrail(auditTrail);
/*     */     
/* 124 */     param.put("USER", birUser);
/* 125 */     param.put("REMARKS", userAccountForm.getRemarks());
/* 126 */     param.put("PROFILE_CODE", userAccountForm.getProfile().getValue());
/*     */     
/* 128 */     param.put("TRANS_TYPE", "040");
/*     */     
/* 130 */     this.userDao.insertIntoUserMain(param);
/*     */ 
/*     */ 
/*     */     
/* 134 */     this.userDao.insertIntoUserHist(param);
/* 135 */     this.userDao.insertIntoUserProfile(param);
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 141 */     log.info("role code : " + userAccountForm.getRole().getValue());
/*     */     
/* 143 */     birUser.setOffice(userAccountForm.getOffice());
/* 144 */     birUser.setDivision(userAccountForm.getDivision());
/* 145 */     RoleAccess roleAccess = new RoleAccess();
/* 146 */     roleAccess.setRoleCode(userAccountForm.getRole().getValue());
/* 147 */     birUser.setRole(roleAccess);
/* 148 */     ParameterizedObject paramObject = new ParameterizedObject();
/*     */ 
/*     */     
/* 151 */     if (loginType.equals("001")) {
/*     */       
/* 153 */       paramObject.setKey("142");
/*     */     }
/*     */     else {
/*     */       
/* 157 */       paramObject.setKey("145");
/*     */     } 
/*     */     
/* 160 */     birUser.setStatus(paramObject);
/* 161 */     paramObject = new ParameterizedObject();
/* 162 */     paramObject.setKey(userAccountForm.getSystemType().getValue());
/* 163 */     birUser.setSystemType(paramObject);
/*     */     
/* 165 */     Map<String, Object> birParam = param;
/* 166 */     birParam.put("USER", birUser);
/*     */     
/* 168 */     this.userManagementDAO.insertIntoUserRole(birParam);
/* 169 */     this.userManagementDAO.insertIntoUserRoleHist(param);
/* 170 */     this.userDao.insertIntoUserPasswordRecord(birUser);
/*     */     
/* 172 */     List<String> functionList = this.userDao.getMenuFunctionsOfRole(userAccountForm.getRole().getValue());
/* 173 */     for (String function : functionList) {
/*     */       
/* 175 */       if (function.length() > 3) {
/*     */         
/* 177 */         param.put("MENU_FUNCTION_CD", function);
/* 178 */         this.roleAccessDao.insertUserAssignments(param);
/*     */         
/*     */         continue;
/*     */       } 
/* 182 */       birParam.put("USER_NAME", birUser.getSecurityProfile().getUsername());
/* 183 */       birParam.put("LOGIN_TYPE", birUser.getLoginType().getKey());
/* 184 */       birParam.put("REPORT_CODE", function);
/* 185 */       this.roleAccessDao.insertUserReportAccess(birParam);
/*     */     } 
/*     */ 
/*     */     
/* 189 */     HashMap<String, Object> paramList = null;
/*     */     
/* 191 */     List<ParameterizedObject> rdoCodes = (List<ParameterizedObject>)param.get("RDO_CODE");
/* 192 */     for (int i = 0; i < rdoCodes.size(); i++) {
/*     */       
/* 194 */       paramList = new HashMap<String, Object>();
/* 195 */       paramList.put("USER_NAME", userAccountForm.getUsername());
/* 196 */       paramList.put("LOGIN_TYPE", loginType);
/* 197 */       paramList.put("RDO_CODE", ((ParameterizedObject)rdoCodes.get(i)).getKey().trim());
/* 198 */       paramList.put("AUDIT_TRAIL", auditTrail);
/* 199 */       this.rdoAccessesDao.insertRdoCodes(paramList);
/*     */       
/* 201 */       paramList.put("TRANS_TYPE", "040");
/* 202 */       this.rdoAccessHistDao.insertRecordRdoAccessesHist(paramList);
/*     */     } 
/*     */ 
/*     */     
/* 206 */     if (userAccountForm.getSendEmail().equals("true")) {
/*     */       
/* 208 */       Map<String, Object> mailMap = new HashMap<String, Object>();
/*     */ 
/*     */       
/* 211 */       mailMap.put("userName", userAccountForm.getUsername());
/* 212 */       mailMap.put("password", originalPassword);
/* 213 */       String sysTypeDe = userAccountForm.getSystemType().getValue();
/* 214 */       mailMap.put("systemType", "140".equals(sysTypeDe) ? "eAccreg" : ("141".equals(sysTypeDe) ? "eSales" : "System Administration"));
/*     */ 
/*     */       
/* 217 */       this.sysadUserDetailsMailer.send(mailMap, new String[] { userAccountForm.getEmail() });
/*     */     } 
/*     */     
/* 220 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setSysadUserDetailsMailer(TemplateMailer sysadUserDetailsMailer) {
/* 225 */     this.sysadUserDetailsMailer = sysadUserDetailsMailer;
/*     */   }
/*     */ 
/*     */   
/*     */   public UserDao getUserDao() {
/* 230 */     return this.userDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setUserDao(UserDao userDao) {
/* 235 */     this.userDao = userDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public UserManagementDAO getUserManagementDAO() {
/* 240 */     return this.userManagementDAO;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setUserManagementDAO(UserManagementDAO userManagementDAO) {
/* 245 */     this.userManagementDAO = userManagementDAO;
/*     */   }
/*     */ 
/*     */   
/*     */   public RoleAccessDao getRoleAccessDao() {
/* 250 */     return this.roleAccessDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setRoleAccessDao(RoleAccessDao roleAccessDao) {
/* 255 */     this.roleAccessDao = roleAccessDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public TemplateMailer getSysadUserDetailsMailer() {
/* 260 */     return this.sysadUserDetailsMailer;
/*     */   }
/*     */ 
/*     */   
/*     */   public RdoAccessesDao getRdoAccessesDao() {
/* 265 */     return this.rdoAccessesDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setRdoAccessesDao(RdoAccessesDao rdoAccessesDao) {
/* 270 */     this.rdoAccessesDao = rdoAccessesDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public RdoAccessesHistDao getRdoAccessHistDao() {
/* 275 */     return this.rdoAccessHistDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setRdoAccessHistDao(RdoAccessesHistDao rdoAccessHistDao) {
/* 280 */     this.rdoAccessHistDao = rdoAccessHistDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setUserService(UserService userService) {
/* 285 */     this.userService = userService;
/*     */   }
/*     */ }


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\sysad\main\enrollment\AccountEnrollmentServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */