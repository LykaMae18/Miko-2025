/*     */ package com.indracompany.sysad.main.usermanagement;
/*     */ 
/*     */ import com.indracompany.acrsal.api.general.UserService;
/*     */ import com.indracompany.acrsal.dao.business.UserDao;
/*     */ import com.indracompany.acrsal.model.sysad.BIRUser;
/*     */ import com.indracompany.acrsal.model.sysad.UserAccess;
/*     */ import com.indracompany.acrsal.models.AuditTrail;
/*     */ import com.indracompany.acrsal.models.ParameterizedObject;
/*     */ import com.indracompany.acrsal.models.SecurityProfile;
/*     */ import com.indracompany.core.reporting.ReportGenerator;
/*     */ import com.indracompany.core.reporting.model.ReportContainer;
/*     */ import com.indracompany.core.security.model.User;
/*     */ import com.indracompany.sysad.api.usermanagement.UserManagementService;
/*     */ import com.indracompany.sysad.dao.UserManagementDAO;
/*     */ import com.indracompany.sysad.dao.role.RoleAccessDao;
/*     */ import com.indracompany.sysad.forms.ResetPasswordForm;
/*     */ import com.indracompany.sysad.forms.UserListInquiryForm;
/*     */ import java.text.ParseException;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import net.sf.jasperreports.engine.JRDataSource;
/*     */ import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.springframework.security.GrantedAuthority;
/*     */ import org.springframework.security.GrantedAuthorityImpl;
/*     */ 
/*     */ 
/*     */ 
/*     */ public class UserManagementServiceImpl
/*     */   implements UserManagementService
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*  36 */   private static final Logger log = Logger.getLogger(UserManagementServiceImpl.class);
/*     */   
/*     */   private UserManagementDAO userManagementDAO;
/*     */   private ReportGenerator birUserDetailExportGenerator;
/*     */   private ReportGenerator passwordResetExportGenerator;
/*     */   private UserService userService;
/*     */   private UserDao userDao;
/*     */   private RoleAccessDao roleAccessDao;
/*     */   public static final String MAX_PASSWORD_LENGTH = "8";
/*     */   
/*     */   public UserDao getUserDao() {
/*  47 */     return this.userDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setUserDao(UserDao userDao) {
/*  52 */     this.userDao = userDao;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*  57 */   public static final char[] SIMPLE_CHARACTERS = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  63 */   public static final char[] NUMERIC_CHARACTERS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
/*     */ 
/*     */   
/*  66 */   public static final char[] SPECIAL_CHARACTERS = new char[] { '!', '@', '#', '$', '%', '^', '&', '*' };
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void setUserManagementDAO(UserManagementDAO userManagementDAO) {
/*  72 */     this.userManagementDAO = userManagementDAO;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getRoleList(String divisionCode) {
/*  78 */     return this.userManagementDAO.getRoleList(divisionCode);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getRoleList(String systemType, String divisionCode) {
/*  84 */     return this.userManagementDAO.getRoleList(systemType, divisionCode);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<List<String>> getUserList() {
/*  90 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public UserManagementDAO getUserManagementDAO() {
/*  95 */     return this.userManagementDAO;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getOfficeList() {
/* 101 */     return this.userManagementDAO.getOfficeList();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getDivisionList(String officeCode) {
/* 107 */     return this.userManagementDAO.getDivisionList(officeCode);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<UserListInquiryForm> getUserListForManagement(Map<String, Object> paramMap) {
/* 113 */     return this.userManagementDAO.getUserListForManagement(paramMap);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<UserAccess> getUserAccessList(Map<String, Object> paramMap, boolean disabled) {
/* 119 */     return this.userManagementDAO.getUserAccessList(paramMap, disabled);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void updateAccess(Map<String, Object> parameterMap) {
/* 125 */     this.userManagementDAO.updateAccess(parameterMap);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void clearModifiedAccess(String username) {
/* 131 */     this.userManagementDAO.clearModifiedAccess(username);
/*     */   }
/*     */ 
/*     */   
/*     */   public ReportContainer exportBIRUserDetails(Map<String, Object> paramMap, List<BIRUser> userList) {
/* 136 */     return this.birUserDetailExportGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(userList));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public ReportContainer passwordResetExport(Map<String, Object> paramMap, List<ResetPasswordForm> userList) {
/* 143 */     return this.passwordResetExportGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(userList));
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public BIRUser getBIRUSerDetails(String userName, String loginType) {
/* 150 */     return this.userManagementDAO.getBIRUSerDetails(userName, loginType);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getSystemReference(String referenceCode) {
/* 156 */     return this.userManagementDAO.getSystemReference(referenceCode);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void modifyBIRUser(BIRUser user, AuditTrail auditTrail) {
/* 169 */     log.info("user.getLoginType().getKey(): " + user.getLoginType().getKey());
/*     */     
/* 171 */     BIRUser userBir = this.userManagementDAO.getBIRUSerDetails(user.getSecurityProfile().getUsername(), user.getLoginType().getKey());
/*     */     
/* 173 */     if (userBir.getStatus().getKey().equals(user.getStatus().getKey())) {
/*     */       
/* 175 */       log.info("Reseting pass failed attempts...");
/* 176 */       user.getSecurityProfile().setFailedAttempts(0);
/*     */     } 
/*     */     
/* 179 */     log.info(" Status : " + user.getStatus().getKey());
/*     */     
/* 181 */     if (user.getStatus().getKey().equals("142")) {
/*     */       
/* 183 */       log.info("ACTIVE");
/* 184 */       user.getSecurityProfile().setAccountNonExpired(true);
/* 185 */       user.getSecurityProfile().setAccountNonLocked(true);
/* 186 */       user.getSecurityProfile().setCredentialsNonExpired(true);
/* 187 */       user.getSecurityProfile().setEnabled(true);
/*     */     }
/* 189 */     else if (user.getStatus().getKey().equals("145")) {
/*     */       
/* 191 */       log.info("EXPIRED");
/* 192 */       user.getSecurityProfile().setEnabled(true);
/* 193 */       user.getSecurityProfile().setAccountNonExpired(true);
/* 194 */       user.getSecurityProfile().setAccountNonLocked(true);
/* 195 */       user.getSecurityProfile().setCredentialsNonExpired(false);
/*     */     }
/* 197 */     else if (user.getStatus().getKey().equals("143")) {
/*     */       
/* 199 */       log.info("INACTIVE");
/* 200 */       user.getSecurityProfile().setEnabled(true);
/* 201 */       user.getSecurityProfile().setAccountNonExpired(false);
/* 202 */       user.getSecurityProfile().setAccountNonLocked(true);
/* 203 */       user.getSecurityProfile().setCredentialsNonExpired(true);
/*     */     }
/* 205 */     else if (user.getStatus().getKey().equals("144")) {
/*     */       
/* 207 */       log.info("LOCKED");
/* 208 */       user.getSecurityProfile().setEnabled(true);
/* 209 */       user.getSecurityProfile().setAccountNonExpired(true);
/* 210 */       user.getSecurityProfile().setAccountNonLocked(false);
/* 211 */       user.getSecurityProfile().setCredentialsNonExpired(true);
/*     */     }
/* 213 */     else if (user.getStatus().getKey().equals("146")) {
/*     */       
/* 215 */       log.info("RESET");
/* 216 */       user.getSecurityProfile().setEnabled(true);
/* 217 */       user.getSecurityProfile().setAccountNonExpired(true);
/* 218 */       user.getSecurityProfile().setAccountNonLocked(true);
/*     */       
/* 220 */       user.getSecurityProfile().setCredentialsNonExpired(false);
/*     */     } 
/*     */     
/* 223 */     this.userManagementDAO.modifyBIRUser(user, auditTrail);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<List<Object>> getUserAccessDetails(Map<String, Object> param) {
/* 229 */     return this.userManagementDAO.getUserAccessDetails(param);
/*     */   }
/*     */ 
/*     */   
/*     */   public void setBirUserDetailExportGenerator(ReportGenerator birUserDetailExportGenerator) {
/* 234 */     this.birUserDetailExportGenerator = birUserDetailExportGenerator;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<String[]> resetPasswordOfUser(Map<String, Object> paramMap, List<String[]> resetUsersList) {
/* 240 */     for (String[] userDetails : resetUsersList) {
/*     */       
/* 242 */       SecurityProfile spEncrypter = new SecurityProfile(userDetails[1], getRandomPassword(), (GrantedAuthority[])new GrantedAuthorityImpl[] { new GrantedAuthorityImpl("ROLE_DUMMY") });
/* 243 */       userDetails[2] = spEncrypter.getPassword();
/* 244 */       this.userService.encryptPassword((User)spEncrypter);
/* 245 */       paramMap.put("USER_NAME", userDetails[1]);
/* 246 */       paramMap.put("PASSWORD", spEncrypter.getPassword());
/* 247 */       this.userManagementDAO.resetPasswordOfUser(paramMap);
/*     */     } 
/* 249 */     return resetUsersList;
/*     */   }
/*     */ 
/*     */   
/*     */   public String getRandomPassword() {
/* 254 */     StringBuffer sb = new StringBuffer();
/* 255 */     int j = 0;
/*     */     
/* 257 */     j = (int)(Math.random() * SIMPLE_CHARACTERS.length - 1.0D);
/* 258 */     sb.append((SIMPLE_CHARACTERS[j] + "").toLowerCase());
/*     */     
/* 260 */     j = (int)(Math.random() * NUMERIC_CHARACTERS.length - 1.0D);
/* 261 */     sb.append(NUMERIC_CHARACTERS[j]);
/*     */     
/* 263 */     j = (int)(Math.random() * SIMPLE_CHARACTERS.length - 1.0D);
/* 264 */     sb.append((SIMPLE_CHARACTERS[j] + "").toUpperCase());
/*     */     
/* 266 */     j = (int)(Math.random() * SPECIAL_CHARACTERS.length - 1.0D);
/* 267 */     sb.append(SPECIAL_CHARACTERS[j]);
/*     */     
/* 269 */     sb.trimToSize();
/*     */     
/* 271 */     while (sb.length() < Integer.parseInt("8")) {
/*     */       
/* 273 */       j = (int)(Math.random() * SIMPLE_CHARACTERS.length - 1.0D);
/* 274 */       sb.append(SIMPLE_CHARACTERS[j]);
/* 275 */       sb.trimToSize();
/* 276 */       log.info("sb : " + sb + " -: size : " + sb.length());
/*     */     } 
/*     */     
/* 279 */     String randomString = sb.toString();
/*     */     
/* 281 */     return randomString;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setUserService(UserService userService) {
/* 286 */     this.userService = userService;
/*     */   }
/*     */ 
/*     */   
/*     */   public void insertBIRUserDetailsHist(Map<String, Object> param) {
/* 291 */     this.userManagementDAO.insertBIRUserDetailsHist(param);
/*     */   }
/*     */ 
/*     */   
/*     */   public void insertIntoUserRoleHist(Map<String, Object> param) {
/* 296 */     this.userManagementDAO.insertIntoUserRoleHist(param);
/*     */   }
/*     */ 
/*     */   
/*     */   public void setPasswordResetExportGenerator(ReportGenerator passwordResetExportGenerator) {
/* 301 */     this.passwordResetExportGenerator = passwordResetExportGenerator;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getProfileList() {
/* 307 */     return this.userManagementDAO.getProfileList();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void insertRecordUserAssignmentHist(Map<String, Object> param) {
/* 313 */     this.userManagementDAO.insertRecordUserAssignmentHist(param);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<UserAccess> getUseSysUsersAssignments(Map<String, Object> param) {
/* 319 */     return this.userManagementDAO.getUseSysUsersAssignments(param);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void modifyUserAssignment(Map<String, Object> paramMap) {
/* 325 */     Date VALIDITY_DATE_FROM = null, VALIDITY_DATE_TO = null;
/*     */     
/*     */     try {
/* 328 */       VALIDITY_DATE_FROM = (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).parse("1900-01-01 00:00:00");
/* 329 */       VALIDITY_DATE_TO = (new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")).parse("2100-12-31 00:00:00");
/*     */     }
/* 331 */     catch (ParseException e) {
/*     */       
/* 333 */       log.info(e);
/*     */     } 
/*     */     
/* 336 */     BIRUser birUser = (BIRUser)paramMap.get("USER");
/* 337 */     AuditTrail auditTrail = (AuditTrail)paramMap.get("AUDIT_TRAIL");
/*     */     
/* 339 */     Map<String, Object> newParam = new HashMap<String, Object>();
/*     */     
/* 341 */     newParam.put("USER_NAME", birUser.getSecurityProfile().getUsername());
/* 342 */     newParam.put("LOGIN_TYPE", birUser.getLoginType().getKey());
/* 343 */     newParam.put("MENU_ACCESS", "1");
/* 344 */     newParam.put("CREATE_USER", auditTrail.getUserSecurity().getUsername());
/* 345 */     newParam.put("AUDIT_TRAIL", auditTrail);
/* 346 */     newParam.put("VALIDITY_DATE_FROM", VALIDITY_DATE_FROM);
/* 347 */     newParam.put("VALIDITY_DATE_TO", VALIDITY_DATE_TO);
/* 348 */     newParam.put("TRANS_TYPE", "041");
/*     */ 
/*     */     
/* 351 */     List<String> existingFunctionList = this.userManagementDAO.selectExistingFunction(newParam);
/* 352 */     this.userManagementDAO.deleteExistingFunction(newParam);
/*     */     
/* 354 */     List<String> functionList = this.userDao.getMenuFunctionsOfRole(birUser.getRole().getRoleCode());
/*     */     
/* 356 */     for (String function : functionList) {
/*     */       
/* 358 */       newParam.put("MENU_FUNCTION_CD", function);
/* 359 */       this.roleAccessDao.insertUserAssignments(newParam);
/*     */     } 
/*     */     
/* 362 */     newParam.remove("TRANS_TYPE");
/* 363 */     newParam.put("TRANS_TYPE", "042");
/* 364 */     for (String function : existingFunctionList) {
/*     */       
/* 366 */       newParam.put("MENU_FUNCTION_CD", function);
/* 367 */       this.roleAccessDao.insertModifyHist(newParam);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public RoleAccessDao getRoleAccessDao() {
/* 374 */     return this.roleAccessDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setRoleAccessDao(RoleAccessDao roleAccessDao) {
/* 379 */     this.roleAccessDao = roleAccessDao;
/*     */   }
/*     */ }


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\sysad\mai\\usermanagement\UserManagementServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */