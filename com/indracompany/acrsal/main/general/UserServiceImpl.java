/*     */ package com.indracompany.acrsal.main.general;
/*     */ 
/*     */ import com.indracompany.acrsal.api.general.UserService;
/*     */ import com.indracompany.acrsal.dao.business.UserDao;
/*     */ import com.indracompany.acrsal.exception.PasswordInvalidException;
/*     */ import com.indracompany.acrsal.exception.PasswordRecentlyUsedException;
/*     */ import com.indracompany.acrsal.exception.UserNullException;
/*     */ import com.indracompany.acrsal.models.AuthorizedUser;
/*     */ import com.indracompany.acrsal.models.FullName;
/*     */ import com.indracompany.acrsal.models.HelpFile;
/*     */ import com.indracompany.acrsal.models.PasswordRecord;
/*     */ import com.indracompany.acrsal.models.SecurityProfile;
/*     */ import com.indracompany.core.mail.mailer.TemplateMailer;
/*     */ import com.indracompany.core.security.dao.UserDao;
/*     */ import com.indracompany.core.security.model.User;
/*     */ import com.indracompany.core.security.service.impl.UserServiceImpl;
/*     */ import java.io.File;
/*     */ import java.text.ParseException;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.apache.commons.lang.StringUtils;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.springframework.dao.DataAccessException;
/*     */ import org.springframework.security.GrantedAuthority;
/*     */ import org.springframework.security.GrantedAuthorityImpl;
/*     */ import org.springframework.security.userdetails.UserDetails;
/*     */ import org.springframework.security.userdetails.UsernameNotFoundException;
/*     */ 
/*     */ 
/*     */ public class UserServiceImpl
/*     */   extends UserServiceImpl
/*     */   implements UserService
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*     */   private static final String passwordRecentlyUsed = "new password is recently used.. throwing PasswordRecentlyUsedException";
/*     */   public static final String MAX_PASSWORD_LENGTH = "8";
/*  40 */   public static final char[] SIMPLE_CHARACTERS = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  46 */   public static final char[] NUMERIC_CHARACTERS = new char[] { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' };
/*     */ 
/*     */   
/*  49 */   public static final char[] SPECIAL_CHARACTERS = new char[] { '!', '@', '#', '$', '%', '^', '&', '*', '(', ')', '_', '+', '|', '~', '-', '\'', '=', '\\', '`', '{', '}', '[', ']', ':', '"', ';', ',', '<', '>', '?', '.', '/' };
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*  58 */   private final Logger logger = Logger.getLogger(UserServiceImpl.class);
/*     */   
/*     */   private UserDao userDao;
/*     */   
/*     */   private TemplateMailer forgotPasswordMailer;
/*     */   
/*     */   private TemplateMailer changePasswordMailer;
/*     */   
/*     */   private String baseFolder;
/*     */ 
/*     */   
/*     */   public void changePassword(AuthorizedUser authorizedUser, String currentPassword, String newPassword) throws PasswordRecentlyUsedException, PasswordInvalidException {
/*  70 */     if (isCurrentPasswordValid(authorizedUser, currentPassword)) {
/*     */       
/*  72 */       this.logger.info("entered password is valid, setting new password");
/*     */       
/*  74 */       SecurityProfile spEncrypter = new SecurityProfile(authorizedUser.getSecurityProfile().getUsername(), newPassword, (GrantedAuthority[])new GrantedAuthorityImpl[] { new GrantedAuthorityImpl("ROLE_DUMMY") });
/*  75 */       encryptPassword((User)spEncrypter);
/*     */       
/*  77 */       PasswordRecord passwordRecord = this.userDao.getUserPasswordRecordHistory(authorizedUser);
/*     */       
/*  79 */       if (passwordRecord == null || isNotRecentlyUsedPassword(spEncrypter.getPassword(), passwordRecord)) {
/*     */ 
/*     */         
/*  82 */         authorizedUser.getSecurityProfile().setPassword(spEncrypter.getPassword());
/*  83 */         this.userDao.updatePassword(authorizedUser);
/*     */ 
/*     */         
/*  86 */         Map<String, Object> userMap = new HashMap<String, Object>();
/*  87 */         userMap.put("STATUS", "142");
/*  88 */         userMap.put("USER", spEncrypter);
/*  89 */         userMap.put("LOGIN_TYPE", authorizedUser.getLoginType());
/*  90 */         this.userDao.updateUserSecurityRole(userMap);
/*     */         
/*  92 */         this.userDao.insertIntoUserHistFromMain(authorizedUser);
/*  93 */         if (passwordRecord != null) {
/*     */           
/*  95 */           this.userDao.updateUserPasswordRecord(authorizedUser);
/*     */         }
/*     */         else {
/*     */           
/*  99 */           this.userDao.insertIntoUserPasswordRecord(authorizedUser);
/*     */         } 
/*     */         
/* 102 */         Map<String, Object> emailList = this.userDao.getEmailAddresses(spEncrypter.getUsername(), authorizedUser.getLoginType().getKey());
/*     */         
/* 104 */         FullName fullName = this.userDao.getCompleteNameOfUser(spEncrypter.getUsername(), authorizedUser.getLoginType().getKey());
/*     */         
/* 106 */         Map<String, Object> forgotParam = new HashMap<String, Object>();
/* 107 */         forgotParam.put("userPassword", newPassword);
/* 108 */         forgotParam.put("userName", spEncrypter.getUsername());
/* 109 */         forgotParam.put("loginType", authorizedUser.getLoginType().getKey().equals("002") ? "eAccReg" : (authorizedUser.getLoginType().getKey().equals("003") ? "eSales" : ""));
/*     */         
/* 111 */         forgotParam.put("authorizedName", fullName.getLastName());
/*     */         
/* 113 */         String emailAddress = (String)emailList.get("EMAIL_ADDRESS");
/* 114 */         String alternateEmailAddress = (String)emailList.get("EMAIL_ALTERNATE");
/*     */         
/* 116 */         if (alternateEmailAddress != null) {
/*     */           
/* 118 */           String[] toAddress = new String[2];
/* 119 */           toAddress[0] = emailAddress;
/* 120 */           toAddress[1] = alternateEmailAddress;
/* 121 */           this.changePasswordMailer.send(forgotParam, toAddress);
/*     */         }
/*     */         else {
/*     */           
/* 125 */           String[] toAddress = new String[1];
/* 126 */           toAddress[0] = emailAddress;
/* 127 */           this.changePasswordMailer.send(forgotParam, toAddress);
/*     */         } 
/*     */       } 
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private boolean isCurrentPasswordValid(AuthorizedUser authorizedUser, String currentPassword) throws PasswordInvalidException {
/* 136 */     SecurityProfile verifySecProfile = new SecurityProfile(authorizedUser.getSecurityProfile().getUsername(), currentPassword, (GrantedAuthority[])new GrantedAuthorityImpl[] { new GrantedAuthorityImpl("ROLE_ANONYMOUS") });
/* 137 */     encryptPassword((User)verifySecProfile);
/* 138 */     this.logger.info("checking current password entered if valid");
/* 139 */     this.logger.info("entered password: " + verifySecProfile.getPassword());
/* 140 */     this.logger.info("current password: " + authorizedUser.getSecurityProfile().getPassword());
/*     */     
/* 142 */     if (!verifySecProfile.getPassword().equals(authorizedUser.getSecurityProfile().getPassword()))
/*     */     {
/* 144 */       throw new PasswordInvalidException("current password is invalid.. throwing PasswordInvalidException");
/*     */     }
/* 146 */     return true;
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean isNotRecentlyUsedPassword(String newPassword, PasswordRecord passwordRecord) throws PasswordRecentlyUsedException {
/* 151 */     this.logger.info("checking new password if recently used.");
/* 152 */     if (passwordRecordValidator(passwordRecord.getPassword(), newPassword))
/*     */     {
/* 154 */       throw new PasswordRecentlyUsedException("new password is recently used.. throwing PasswordRecentlyUsedException");
/*     */     }
/* 156 */     if (passwordRecordValidator(passwordRecord.getPassword2(), newPassword))
/*     */     {
/* 158 */       throw new PasswordRecentlyUsedException("new password is recently used.. throwing PasswordRecentlyUsedException");
/*     */     }
/* 160 */     if (passwordRecordValidator(passwordRecord.getPassword3(), newPassword))
/*     */     {
/* 162 */       throw new PasswordRecentlyUsedException("new password is recently used.. throwing PasswordRecentlyUsedException");
/*     */     }
/* 164 */     if (passwordRecordValidator(passwordRecord.getPassword4(), newPassword))
/*     */     {
/* 166 */       throw new PasswordRecentlyUsedException("new password is recently used.. throwing PasswordRecentlyUsedException");
/*     */     }
/* 168 */     if (passwordRecordValidator(passwordRecord.getPassword5(), newPassword))
/*     */     {
/* 170 */       throw new PasswordRecentlyUsedException("new password is recently used.. throwing PasswordRecentlyUsedException");
/*     */     }
/* 172 */     return true;
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean passwordRecordValidator(String passwordRecord, String newPassword) {
/* 177 */     return (!StringUtils.isEmpty(passwordRecord) && StringUtils.equals(passwordRecord, newPassword));
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException, DataAccessException {
/* 183 */     this.logger.info("loadUserByUsername.username: " + username);
/* 184 */     SecurityProfile securityProfile = (SecurityProfile)super.loadUserByUsername(username);
/*     */     
/* 186 */     this.logger.info("credential : " + securityProfile.isCredentialsNonExpired());
/*     */     
/* 188 */     if (securityProfile.isCredentialsNonExpired()) {
/*     */       
/*     */       try {
/*     */         
/* 192 */         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
/*     */         
/* 194 */         Date userCredentialExpiration = sdf.parse(sdf.format(securityProfile.getExpirationDate()));
/* 195 */         Date currendDate = sdf.parse(sdf.format(new Date()));
/*     */         
/* 197 */         this.logger.info("currendDate : " + currendDate);
/* 198 */         this.logger.info("userCredentialExpiration : " + userCredentialExpiration);
/*     */         
/* 200 */         boolean testBool = (userCredentialExpiration.compareTo(currendDate) <= 0);
/*     */         
/* 202 */         this.logger.info("boolean : " + testBool);
/*     */         
/* 204 */         if (userCredentialExpiration.compareTo(currendDate) <= 0)
/*     */         {
/* 206 */           securityProfile.setCredentialsNonExpired(false);
/* 207 */           this.userDao.update((User)securityProfile);
/*     */         }
/*     */       
/* 210 */       } catch (ParseException e) {
/*     */         
/* 212 */         this.logger.error(e.getMessage());
/*     */       } 
/*     */     }
/*     */     
/* 216 */     return (UserDetails)securityProfile;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void updateNewUserPassword(String transaction_number, String login_type) {
/* 222 */     SecurityProfile securityProfile = this.userDao.getSecurityUserInTempByTransactionNo(transaction_number);
/* 223 */     encryptPassword((User)securityProfile);
/*     */     
/* 225 */     Map<String, Object> updateParam = new HashMap<String, Object>();
/*     */     
/* 227 */     updateParam.put("USER_PASSWORD", securityProfile.getPassword());
/* 228 */     updateParam.put("USER_NAME", securityProfile.getUsername());
/* 229 */     updateParam.put("LOGIN_TYPE", login_type);
/*     */     
/* 231 */     this.userDao.updateUserPasswordInTemp(updateParam);
/*     */   }
/*     */ 
/*     */   
/*     */   public void setUserDao(UserDao userDao) {
/* 236 */     this.userDao = userDao;
/* 237 */     setUserDao((UserDao)userDao);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public AuthorizedUser getAuthorizedUserInMain(String username, String login_type) {
/* 243 */     return this.userDao.getAuthorizedUserInMain(username, login_type);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public SecurityProfile getUserSecurityDetails(String userName, String login_Type) throws UserNullException {
/* 249 */     if (this.userDao.getUserSecurityDetails(userName, login_Type) == null)
/*     */     {
/* 251 */       throw new UserNullException();
/*     */     }
/*     */ 
/*     */     
/* 255 */     return this.userDao.getUserSecurityDetails(userName, login_Type);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void forgotPasswordUpdate(AuthorizedUser authorizedUser) {
/* 264 */     String newPassword = getRandomPassword();
/* 265 */     authorizedUser.getSecurityProfile().setPassword(newPassword);
/* 266 */     SecurityProfile spEncrypter = authorizedUser.getSecurityProfile();
/* 267 */     encryptPassword((User)spEncrypter);
/* 268 */     authorizedUser.setSecurityProfile(spEncrypter);
/*     */     
/* 270 */     this.userDao.updateForgotPassword(authorizedUser);
/* 271 */     this.userDao.insertIntoUserHistFromMain(authorizedUser);
/* 272 */     this.userDao.updateUserPasswordRecord(authorizedUser);
/*     */     
/* 274 */     Map<String, Object> emailList = this.userDao.getEmailAddresses(spEncrypter.getUsername(), authorizedUser.getLoginType().getKey());
/*     */     
/* 276 */     FullName fullName = this.userDao.getCompleteNameOfUser(spEncrypter.getUsername(), authorizedUser.getLoginType().getKey());
/*     */     
/* 278 */     Map<String, Object> forgotParam = new HashMap<String, Object>();
/* 279 */     forgotParam.put("userPassword", newPassword);
/* 280 */     forgotParam.put("userName", spEncrypter.getUsername());
/* 281 */     forgotParam.put("loginType", authorizedUser.getLoginType().getKey().equals("002") ? "eAccReg" : (authorizedUser.getLoginType().getKey().equals("003") ? "eSales" : ""));
/*     */     
/* 283 */     forgotParam.put("authorizedName", fullName.getLastName());
/*     */     
/* 285 */     String emailAddress = (String)emailList.get("EMAIL_ADDRESS");
/* 286 */     String alternateEmailAddress = (String)emailList.get("EMAIL_ALTERNATE");
/*     */     
/* 288 */     if (alternateEmailAddress != null) {
/*     */       
/* 290 */       String[] toAddress = new String[2];
/* 291 */       toAddress[0] = emailAddress;
/* 292 */       toAddress[1] = alternateEmailAddress;
/* 293 */       this.forgotPasswordMailer.send(forgotParam, toAddress);
/*     */     }
/*     */     else {
/*     */       
/* 297 */       String[] toAddress = new String[1];
/* 298 */       toAddress[0] = emailAddress;
/* 299 */       this.forgotPasswordMailer.send(forgotParam, toAddress);
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private static String getRandomPassword() {
/* 306 */     StringBuffer sb = new StringBuffer();
/* 307 */     int j = 0;
/*     */     
/* 309 */     j = (int)(Math.random() * SIMPLE_CHARACTERS.length - 1.0D);
/* 310 */     sb.append((SIMPLE_CHARACTERS[j] + "").toLowerCase());
/*     */     
/* 312 */     j = (int)(Math.random() * NUMERIC_CHARACTERS.length - 1.0D);
/* 313 */     sb.append(NUMERIC_CHARACTERS[j]);
/*     */     
/* 315 */     j = (int)(Math.random() * SIMPLE_CHARACTERS.length - 1.0D);
/* 316 */     sb.append((SIMPLE_CHARACTERS[j] + "").toUpperCase());
/*     */     
/* 318 */     j = (int)(Math.random() * SPECIAL_CHARACTERS.length - 1.0D);
/* 319 */     sb.append(SPECIAL_CHARACTERS[j]);
/*     */     
/* 321 */     while (sb.length() < Integer.parseInt("8")) {
/*     */       
/* 323 */       j = (int)(Math.random() * SIMPLE_CHARACTERS.length - 1.0D);
/* 324 */       sb.append(SIMPLE_CHARACTERS[j]);
/*     */     } 
/* 326 */     String randomString = sb.toString();
/*     */     
/* 328 */     return randomString;
/*     */   }
/*     */ 
/*     */   
/*     */   public TemplateMailer getForgotPasswordMailer() {
/* 333 */     return this.forgotPasswordMailer;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setForgotPasswordMailer(TemplateMailer forgotPasswordMailer) {
/* 338 */     this.forgotPasswordMailer = forgotPasswordMailer;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<HelpFile> getHelpFiles(String fileSystem) {
/* 344 */     List<HelpFile> helpFileList = this.userDao.getHelpFiles(fileSystem);
/* 345 */     String basePathHolder = basePathConfigurer(this.baseFolder);
/*     */     
/* 347 */     for (HelpFile temp : helpFileList) {
/*     */       
/* 349 */       if (isServerUnix()) {
/*     */         
/* 351 */         temp.setBasePath(basePathHolder);
/* 352 */         temp.setAbsolutePath(temp.getFullBasePath());
/*     */         
/*     */         continue;
/*     */       } 
/* 356 */       temp.setBasePath(basePathHolder);
/* 357 */       temp.setAbsolutePath(temp.getFullBasePath().replace("\\", "\\\\"));
/*     */     } 
/*     */ 
/*     */     
/* 361 */     return helpFileList;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public List<HelpFile> getHelpFiles(Map<String, Object> fileSystem) {
/* 368 */     List<HelpFile> helpFileList = this.userDao.getHelpFiles(fileSystem);
/* 369 */     String basePathHolder = basePathConfigurer(this.baseFolder);
/*     */     
/* 371 */     for (HelpFile temp : helpFileList) {
/*     */       
/* 373 */       if (isServerUnix()) {
/*     */         
/* 375 */         temp.setBasePath(basePathHolder);
/* 376 */         temp.setAbsolutePath(temp.getFullBasePath());
/*     */         
/*     */         continue;
/*     */       } 
/* 380 */       temp.setBasePath(basePathHolder);
/* 381 */       temp.setAbsolutePath(temp.getFullBasePath().replace("\\", "\\\\"));
/*     */     } 
/*     */ 
/*     */     
/* 385 */     return helpFileList;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public HelpFile getHelpFileByNameCategoryLocation(String fileName, String fileCategory, String fileLocation, String loginType) {
/* 392 */     HelpFile dbParam = new HelpFile();
/* 393 */     dbParam.setFileName(fileName);
/* 394 */     dbParam.setFileCategory(fileCategory);
/* 395 */     dbParam.setFileLocation(fileLocation);
/*     */     
/* 397 */     HelpFile resultFile = this.userDao.getHelpFileByNameCategoryLocation(dbParam, loginType);
/*     */     
/* 399 */     String basePathHolder = basePathConfigurer(this.baseFolder);
/*     */     
/* 401 */     if (isServerUnix()) {
/*     */       
/* 403 */       resultFile.setBasePath(basePathHolder);
/* 404 */       resultFile.setAbsolutePath(resultFile.getFullBasePath());
/*     */     }
/*     */     else {
/*     */       
/* 408 */       resultFile.setBasePath(basePathHolder);
/* 409 */       resultFile.setAbsolutePath(resultFile.getFullBasePath().replace("\\", "\\\\"));
/*     */     } 
/*     */     
/* 412 */     return resultFile;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setBaseFolder(String baseFolder) {
/* 417 */     this.baseFolder = baseFolder;
/*     */   }
/*     */ 
/*     */   
/*     */   private boolean isServerUnix() {
/* 422 */     return "/".equals(File.separator);
/*     */   }
/*     */ 
/*     */   
/*     */   private String basePathConfigurer(String basePath) {
/* 427 */     StringBuilder pathContainer = new StringBuilder("");
/* 428 */     char[] c = basePath.toCharArray();
/* 429 */     boolean isUnixTag = false;
/*     */     
/* 431 */     for (char temp : c) {
/*     */       
/* 433 */       if (temp == '/')
/*     */       {
/* 435 */         isUnixTag = true;
/*     */       }
/*     */     } 
/*     */     
/* 439 */     if (isUnixTag) {
/*     */       
/* 441 */       pathContainer.append(basePath.replace("/", File.separator));
/*     */     }
/*     */     else {
/*     */       
/* 445 */       pathContainer.append(basePath.replace("\\", "\\\\"));
/*     */     } 
/*     */     
/* 448 */     return pathContainer.toString();
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public Map<String, Object> getBusinessTINAndBranchByActiveUser(Map<String, Object> param) {
/* 454 */     Map<String, Object> activeUserDetails = new HashMap<String, Object>();
/* 455 */     activeUserDetails = this.userDao.getBusinessTINAndBranchByActiveUser(param);
/* 456 */     return activeUserDetails;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public AuthorizedUser getAuthorizedUserByLogin(Map<String, Object> userMap) {
/* 462 */     return this.userDao.getAuthorizedUserByLogin(userMap);
/*     */   }
/*     */ 
/*     */   
/*     */   public void setChangePasswordMailer(TemplateMailer changePasswordMailer) {
/* 467 */     this.changePasswordMailer = changePasswordMailer;
/*     */   }
/*     */ }


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\main\general\UserServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */