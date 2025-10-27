/*      */ package com.indracompany.acrsal.main.enrollment;
/*      */ 
/*      */ import com.indracompany.acrsal.api.enrollment.AccountEnrollmentService;
/*      */ import com.indracompany.acrsal.dao.business.BusinessDao;
/*      */ import com.indracompany.acrsal.dao.business.SequenceDao;
/*      */ import com.indracompany.acrsal.dao.business.UserDao;
/*      */ import com.indracompany.acrsal.exception.DuplicateAccountEnrollmentException;
/*      */ import com.indracompany.acrsal.exception.DuplicateUsernameException;
/*      */ import com.indracompany.acrsal.exception.ITSValidationException;
/*      */ import com.indracompany.acrsal.exception.NoMINException;
/*      */ import com.indracompany.acrsal.forms.LineOfBusinessForm;
/*      */ import com.indracompany.acrsal.models.AuditTrail;
/*      */ import com.indracompany.acrsal.models.AuthorizedUser;
/*      */ import com.indracompany.acrsal.models.Branch;
/*      */ import com.indracompany.acrsal.models.Business;
/*      */ import com.indracompany.acrsal.models.FullName;
/*      */ import com.indracompany.acrsal.models.ParameterizedObject;
/*      */ import com.indracompany.core.mail.mailer.TemplateMailer;
/*      */ import java.text.DateFormat;
/*      */ import java.text.ParseException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Collections;
/*      */ import java.util.Comparator;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import org.apache.commons.lang.StringUtils;
/*      */ import org.apache.log4j.Logger;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class AccountEnrollmentServiceImpl
/*      */   implements AccountEnrollmentService
/*      */ {
/*      */   private static final long serialVersionUID = 1L;
/*   40 */   private final Logger log = Logger.getLogger(AccountEnrollmentService.class);
/*      */   
/*      */   private static final String USER = "USER";
/*      */   
/*      */   private static final String TRANS_TYPE = "TRANS_TYPE";
/*      */   
/*      */   private static final String BUSINESS_TIN = "BUSINESS_TIN";
/*      */   
/*      */   private static final String BRANCH_CODE = "BRANCH_CODE";
/*      */   
/*      */   private static final String BUSINESS_TYPE = "BUSINESS_TYPE";
/*      */   private TemplateMailer enrollmentMailer;
/*      */   private TemplateMailer accountApprovalMailer;
/*      */   private TemplateMailer accountOnholdMailer;
/*      */   private TemplateMailer accountRejectedMailer;
/*      */   private TemplateMailer accountActivateDeactivate;
/*      */   private BusinessDao businessDao;
/*      */   private UserDao userDao;
/*      */   private SequenceDao sequenceDao;
/*      */   
/*      */   public List<Business> getAcctEnrollmentList(Map<String, Object> param, String loginType) {
/*   61 */     List<Business> businessList = new ArrayList<Business>();
/*      */     
/*   63 */     if (param != null) {
/*      */ 
/*      */       
/*   66 */       AuthorizedUser user = (AuthorizedUser)param.get("USER");
/*   67 */       String status = user.getStatus().getKey();
/*      */       
/*   69 */       this.log.info("Status : " + status);
/*      */       
/*   71 */       if (status.equals("004") || status.equals("005") || status.equals("006")) {
/*      */ 
/*      */ 
/*      */         
/*   75 */         this.log.info("Complete Business In Temp");
/*   76 */         return this.businessDao.getCompleteBusinessInTemp(param, loginType);
/*      */       } 
/*   78 */       if (status.equals("007")) {
/*      */         
/*   80 */         this.log.info("Complete Business In Main");
/*   81 */         return this.businessDao.getCompleteBusinessInMain(param, loginType);
/*      */       } 
/*      */ 
/*      */       
/*   85 */       Comparator<Business> comparator = new Comparator<Business>()
/*      */         {
/*      */           public int compare(Business b1, Business b2)
/*      */           {
/*      */             double transNoB1;
/*      */             
/*      */             double transNoB2;
/*   92 */             if (((AuthorizedUser)((Branch)b1.getBranchList().get(0)).getAuthorizedUserList().get(0)).getTransactionNo().startsWith("ENR")) {
/*      */               
/*   94 */               transNoB1 = Double.parseDouble(((AuthorizedUser)((Branch)b1.getBranchList().get(0)).getAuthorizedUserList().get(0)).getTransactionNo().replace("ENR", ""));
/*      */             }
/*      */             else {
/*      */               
/*   98 */               transNoB1 = Double.parseDouble(((AuthorizedUser)((Branch)b1.getBranchList().get(0)).getAuthorizedUserList().get(0)).getTransactionNo().replace("TRN", ""));
/*      */             } 
/*      */             
/*  101 */             if (((AuthorizedUser)((Branch)b2.getBranchList().get(0)).getAuthorizedUserList().get(0)).getTransactionNo().startsWith("ENR")) {
/*      */               
/*  103 */               transNoB2 = Double.parseDouble(((AuthorizedUser)((Branch)b2.getBranchList().get(0)).getAuthorizedUserList().get(0)).getTransactionNo().replace("ENR", ""));
/*      */             }
/*      */             else {
/*      */               
/*  107 */               transNoB2 = Double.parseDouble(((AuthorizedUser)((Branch)b2.getBranchList().get(0)).getAuthorizedUserList().get(0)).getTransactionNo().replace("TRN", ""));
/*      */             } 
/*      */             
/*  110 */             return (transNoB1 > transNoB2) ? -1 : 1;
/*      */           }
/*      */         };
/*      */ 
/*      */       
/*  115 */       this.log.info("Complete Business both in Temp and Main");
/*  116 */       businessList.addAll(this.businessDao.getCompleteBusinessInTemp(param, loginType));
/*  117 */       businessList.addAll(this.businessDao.getCompleteBusinessInMain(param, loginType));
/*  118 */       Collections.sort(businessList, comparator);
/*  119 */       return businessList;
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/*  124 */     return businessList;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public List<Business> getAcctEnrollmentListForTaskManager(Map<String, Object> param, String loginType) {
/*  133 */     this.log.info("Complete Business In Temp for task manager.");
/*  134 */     return this.businessDao.getCompleteBusinessInTemp(param, loginType);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void modifyUserContactDetails(Map<String, Object> userMap) {
/*  181 */     AuthorizedUser user = (AuthorizedUser)userMap.get("USER");
/*      */     
/*  183 */     String status = user.getStatus().getKey();
/*      */     
/*  185 */     if (status.equals("004") || status.equals("005") || status.equals("006")) {
/*      */ 
/*      */ 
/*      */       
/*  189 */       this.userDao.updateUserContactsTemp(userMap);
/*      */ 
/*      */ 
/*      */     
/*      */     }
/*      */     else {
/*      */ 
/*      */ 
/*      */       
/*  198 */       userMap.put("TRANS_TYPE", "041");
/*      */       
/*  200 */       this.userDao.updateUserContactsMain(userMap);
/*  201 */       this.userDao.insertIntoUserHist(userMap);
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void modifyDeactivateActivate(Map<String, Object> userMap) {
/*  208 */     AuthorizedUser user = (AuthorizedUser)userMap.get("USER");
/*  209 */     Branch branch = (Branch)userMap.get("BRANCH");
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  216 */     List<Branch> branchList = this.userDao.getBranchListOfUser(userMap);
/*      */     
/*  218 */     if (branchList.size() == 1 && branch.getBranchCode().equals(((Branch)branchList.get(0)).getBranchCode())) {
/*      */       
/*  220 */       userMap.put("ENABLED", "0");
/*  221 */       user.getSecurityProfile().setEnabled(false);
/*      */     }
/*      */     else {
/*      */       
/*  225 */       userMap.put("ENABLED", "1");
/*  226 */       user.getSecurityProfile().setEnabled(true);
/*      */     } 
/*      */     
/*  229 */     userMap.put("USER", user);
/*      */ 
/*      */     
/*  232 */     userMap.put("TRANS_TYPE", "041");
/*  233 */     this.userDao.activateDeactivateUser(userMap);
/*  234 */     this.userDao.insertIntoUserHist(userMap);
/*      */     
/*  236 */     this.userDao.updateUseSysBusBranchStatus(userMap);
/*  237 */     this.userDao.insertIntoUserBusinessBranchHist(userMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public Business getAuthorizedUserByTransactionNo(String transactionNumber) {
/*  242 */     return null;
/*      */   }
/*      */ 
/*      */   
/*      */   private String generateTransactionNo() {
/*  247 */     SimpleDateFormat sdfDate = new SimpleDateFormat("yyMMdd");
/*  248 */     SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");
/*  249 */     Date date = new Date();
/*  250 */     String strDate = sdfDate.format(date);
/*  251 */     String strTime = sdfTime.format(date);
/*      */     
/*  253 */     Map<String, Object> transTemp = this.sequenceDao.getTransactionNo("ENR");
/*      */     
/*  255 */     String prefix = transTemp.get("SEQUENCE_PREFIX").toString().trim();
/*  256 */     int trans_number = Integer.parseInt(transTemp.get("SEQUENCE_NUMBER").toString().trim());
/*      */ 
/*      */     
/*  259 */     trans_number = (trans_number + 1 > 99999) ? 1 : (trans_number + 1);
/*      */     
/*  261 */     this.sequenceDao.updateTransactionNo(String.valueOf(trans_number), "ENR");
/*      */ 
/*      */     
/*  264 */     return prefix + strDate + strTime + sequenceNumberFormatter(trans_number);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private String sequenceNumberFormatter(int sequence_number) {
/*  270 */     String cont = String.valueOf(sequence_number);
/*  271 */     String padValue = "0";
/*  272 */     StringBuilder resultContainer = new StringBuilder("");
/*      */     
/*  274 */     for (int x = cont.length(); x < 5; x++)
/*      */     {
/*  276 */       resultContainer.append(padValue);
/*      */     }
/*      */     
/*  279 */     resultContainer.append(cont);
/*      */     
/*  281 */     return resultContainer.toString();
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public String addAccountEnrollmentToTemp(Map<String, Object> param) {
/*  287 */     Business business = (Business)param.get("BUSINESS");
/*  288 */     Branch branch = (Branch)param.get("BRANCH");
/*      */ 
/*      */     
/*  291 */     String rdoCode = this.businessDao.getRDOCodeFromITS(business.getBusinessTIN(), branch.getBranchCode());
/*      */ 
/*      */     
/*  294 */     ParameterizedObject rdo = new ParameterizedObject();
/*  295 */     rdo.setKey(rdoCode);
/*  296 */     branch.setRdoCode(rdo);
/*      */ 
/*      */     
/*  299 */     String transNo = generateTransactionNo();
/*      */     
/*  301 */     AuthorizedUser user = (AuthorizedUser)param.get("USER");
/*  302 */     user.setTransactionNo(transNo);
/*  303 */     param.put("USER", user);
/*      */     
/*  305 */     this.log.info("the rdo : " + branch.getRdoCode());
/*      */     
/*  307 */     this.businessDao.addEnrollmentTransactionNo(param);
/*      */ 
/*      */     
/*  310 */     this.businessDao.addAccountEnrollmentToTemp(param);
/*  311 */     AuditTrail auditTrail = (AuditTrail)param.get("AUDITRAIL");
/*      */     
/*  313 */     String profileCode = (String)param.get("PROFILE_CODE");
/*  314 */     String securityStatus = (String)param.get("SECURITY_STATUS");
/*      */     
/*  316 */     String statusKeyChange = user.getStatus().getKey();
/*      */     
/*  318 */     String remarks = (String)param.get("REMARKS");
/*      */     
/*  320 */     Business newBusiness = this.businessDao.getBusinessFromTempByTransNo(transNo);
/*  321 */     Branch newBranch = this.businessDao.getBranchFromTempByTransNo(transNo);
/*  322 */     AuthorizedUser newUser = this.userDao.getUserFromTempByTransNo(transNo);
/*      */     
/*  324 */     ParameterizedObject pStatus = new ParameterizedObject();
/*  325 */     pStatus.setKey(statusKeyChange);
/*  326 */     newUser.setStatus(pStatus);
/*  327 */     String uname = newUser.getSecurityProfile().getUsername();
/*      */     
/*  329 */     auditTrail.getUserSecurity().setUsername(uname);
/*  330 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/*  331 */     paramMap.put("BUSINESS", newBusiness);
/*  332 */     paramMap.put("BRANCH", newBranch);
/*  333 */     paramMap.put("USER", newUser);
/*  334 */     paramMap.put("AUDIT_TRAIL", auditTrail);
/*  335 */     paramMap.put("REMARKS", remarks);
/*  336 */     paramMap.put("PROFILE_CODE", profileCode);
/*  337 */     paramMap.put("SECURITY_STATUS", securityStatus);
/*      */     
/*  339 */     newBusiness.setBusinessDesc(business.getBusinessDesc());
/*  340 */     newBusiness.setBusinessName(business.getBusinessName());
/*  341 */     newBusiness.setIncDate(business.getIncDate());
/*  342 */     newBusiness.setBirthDate(business.getIncDate());
/*      */     
/*  344 */     if (this.businessDao.getTINCountFromBusinessMain(paramMap) == 0) {
/*      */       
/*  346 */       paramMap.put("TRANS_TYPE", "040");
/*      */     }
/*      */     else {
/*      */       
/*  350 */       paramMap.put("TRANS_TYPE", "041");
/*      */     } 
/*      */     
/*  353 */     this.businessDao.insertIntoSysBusBranchUserHist(paramMap);
/*  354 */     if (user.getLoginType() != null)
/*      */     {
/*      */ 
/*      */       
/*  358 */       this.businessDao.insertIntoEnrollmentBusinessNames(param);
/*      */     }
/*      */     
/*  361 */     Map<String, Object> mailMap = new HashMap<String, Object>();
/*  362 */     mailMap.put("transNo", transNo);
/*  363 */     mailMap.put("authorizedName", newUser.getFullName().getLastName());
/*  364 */     mailMap.put("loginType", param.get("loginType"));
/*  365 */     mailMap.put("userName", param.get("mapKey"));
/*      */     
/*  367 */     if (!StringUtils.isEmpty(user.getContactDetail().getAlternateEmailAddress())) {
/*      */       
/*  369 */       String[] send = new String[2];
/*  370 */       send[0] = user.getContactDetail().getEmailAddress();
/*  371 */       send[1] = user.getContactDetail().getAlternateEmailAddress();
/*  372 */       this.enrollmentMailer.send(mailMap, send);
/*      */     }
/*      */     else {
/*      */       
/*  376 */       String[] send = new String[1];
/*  377 */       send[0] = user.getContactDetail().getEmailAddress();
/*  378 */       this.enrollmentMailer.send(mailMap, send);
/*      */     } 
/*      */     
/*  381 */     return transNo;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean isBusinessBranchCodeValid(String businessTin, String branchCode) throws ITSValidationException {
/*  388 */     Map<String, Object> param = new HashMap<String, Object>();
/*  389 */     param.put("BUSINESS_TIN", businessTin);
/*  390 */     param.put("BRANCH_CODE", branchCode);
/*      */     
/*  392 */     int branchCount = this.businessDao.checkBusinessBranchFromITS(param);
/*      */     
/*  394 */     if (branchCount > 0)
/*      */     {
/*  396 */       return true;
/*      */     }
/*      */ 
/*      */     
/*  400 */     throw new ITSValidationException(branchCode);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean isTinBrancCodeBusinessTypeValid(String businessTin, String branchCode, String businessType) throws ITSValidationException {
/*  406 */     Map<String, Object> param = new HashMap<String, Object>();
/*  407 */     param.put("BUSINESS_TIN", businessTin);
/*  408 */     if (null != branchCode && !branchCode.equals(""))
/*      */     {
/*  410 */       param.put("BRANCH_CODE", branchCode);
/*      */     }
/*  412 */     if (null != businessType)
/*      */     {
/*  414 */       param.put("BUSINESS_TYPE", businessType.equals("028") ? "I" : "N");
/*      */     }
/*      */ 
/*      */     
/*  418 */     int branchCount = this.businessDao.checkBusinessBranchFromITS(param);
/*      */     
/*  420 */     if (branchCount > 0)
/*      */     {
/*  422 */       return true;
/*      */     }
/*      */ 
/*      */     
/*  426 */     throw new ITSValidationException(branchCode);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public Business getBusinessInMain(AuthorizedUser user, String loginType) {
/*  432 */     Map<String, Object> businessMap = new HashMap<String, Object>();
/*  433 */     businessMap.put("USER", user);
/*      */     
/*  435 */     return this.businessDao.getBusinessInMain(businessMap, loginType);
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean isUserExists(String userName, String loginType) throws DuplicateUsernameException {
/*  440 */     Map<String, Object> userMap = new HashMap<String, Object>();
/*  441 */     userMap.put("USER_NAME", userName);
/*  442 */     userMap.put("LOGIN_TYPE", loginType);
/*      */     
/*  444 */     if (this.userDao.checkIfUserExist(userMap).booleanValue())
/*      */     {
/*  446 */       throw new DuplicateUsernameException(userName);
/*      */     }
/*      */ 
/*      */     
/*  450 */     return false;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Business getAcctEnrollmentByTransactionNo(String transactionNo, String statusCode) {
/*  460 */     Business business = new Business();
/*  461 */     Branch branch = new Branch();
/*  462 */     List<Branch> branchList = new ArrayList<Branch>();
/*  463 */     AuthorizedUser user = new AuthorizedUser();
/*  464 */     List<AuthorizedUser> userList = new ArrayList<AuthorizedUser>();
/*      */     
/*  466 */     if (statusCode.equals("004") || statusCode.equals("005") || statusCode.equals("006")) {
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  471 */       business = this.businessDao.getBusinessFromTempByTransNo(transactionNo);
/*  472 */       branch = this.businessDao.getBranchFromTempByTransNo(transactionNo);
/*  473 */       user = this.userDao.getUserFromTempByTransNo(transactionNo);
/*      */     
/*      */     }
/*      */     else {
/*      */       
/*  478 */       business = this.businessDao.getBusinessFromMainByTransNo(transactionNo);
/*  479 */       branch = this.businessDao.getBranchFromMainByTransNo(transactionNo);
/*  480 */       user = this.userDao.getUserFromMainByTransNo(transactionNo);
/*      */     } 
/*      */     
/*  483 */     userList.add(user);
/*  484 */     branch.setAuthorizedUserList(userList);
/*  485 */     branchList.add(branch);
/*  486 */     if (business != null) {
/*  487 */       business.setBranchList(branchList);
/*      */     }
/*  489 */     return business;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public Business getAcctDetailsByUserNameAndLoginType(Map<String, Object> param) {
/*  495 */     Business business = new Business();
/*      */     
/*  497 */     String loginType = (String)param.get("LOGIN_TYPE");
/*  498 */     business = this.businessDao.getBusinessInMain(param, loginType);
/*  499 */     if (null == business)
/*  500 */       return null; 
/*  501 */     List<Branch> branchList = new ArrayList<Branch>();
/*  502 */     String businessTIN = business.getBusinessTIN();
/*  503 */     param.put("BUSINESS_TIN", businessTIN);
/*  504 */     branchList = this.businessDao.getBranchCodesFromMainByUserNameAndLoginType(param);
/*      */     
/*  506 */     List<AuthorizedUser> userList = new ArrayList<AuthorizedUser>();
/*  507 */     AuthorizedUser user = this.userDao.getUserFromMainByUserNameAndLoginType(param);
/*  508 */     userList.add(user);
/*      */     
/*  510 */     for (int y = 0; y < branchList.size(); y++)
/*      */     {
/*  512 */       ((Branch)branchList.get(y)).setAuthorizedUserList(userList);
/*      */     }
/*  514 */     business.setBranchList(branchList);
/*      */     
/*  516 */     return business;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public Business getITSBusinessInfo(Map<String, Object> paramMap) {
/*  522 */     return this.businessDao.getITSBusinessInfo(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean isBusinessAddBranchCodeExist(String businessTin, String branchCode, Map<String, Object> userDetails) throws DuplicateAccountEnrollmentException {
/*  530 */     int branchCount = this.businessDao.checkBusinessBranchCodeFromMain(userDetails);
/*      */     
/*  532 */     if (branchCount > 0)
/*      */     {
/*  534 */       throw new DuplicateAccountEnrollmentException("Account Enrollment is already existing.");
/*      */     }
/*      */ 
/*      */     
/*  538 */     return false;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Business getBusinessDetailBranch(Map<String, Object> param) {
/*  545 */     Business business = new Business();
/*  546 */     business = this.businessDao.getBusinessDetailBranch(param);
/*      */     
/*  548 */     return business;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void approveAccount(Map<String, Object> param) {
/*  555 */     this.log.info("Updating Account...");
/*      */     
/*  557 */     Business business = (Business)param.get("BUSINESS");
/*      */     
/*  559 */     AuthorizedUser user = (AuthorizedUser)param.get("USER");
/*  560 */     AuditTrail auditTrail = (AuditTrail)param.get("AUDIT_TRAIL");
/*  561 */     String profileCode = (String)param.get("PROFILE_CODE");
/*  562 */     String securityStatus = (String)param.get("SECURITY_STATUS");
/*      */     
/*  564 */     String statusKeyChange = user.getStatus().getKey();
/*      */     
/*  566 */     String transactionNo = ((AuthorizedUser)((Branch)business.getBranchList().get(0)).getAuthorizedUserList().get(0)).getTransactionNo();
/*  567 */     String loginType = ((AuthorizedUser)((Branch)business.getBranchList().get(0)).getAuthorizedUserList().get(0)).getLoginType().getKey();
/*  568 */     String remarks = (String)param.get("REMARKS");
/*      */     
/*  570 */     Business newBusiness = this.businessDao.getBusinessFromTempByTransNo(transactionNo);
/*  571 */     Branch newBranch = this.businessDao.getBranchFromTempByTransNo(transactionNo);
/*  572 */     AuthorizedUser newUser = this.userDao.getUserFromTempByTransNo(transactionNo);
/*      */ 
/*      */     
/*  575 */     param.put("TRANS_NO", transactionNo);
/*      */     
/*  577 */     DateFormat formatter = new SimpleDateFormat("MMMM dd, yyyy");
/*      */ 
/*      */     
/*  580 */     if (business.getBusinessType().equals("029")) {
/*      */       
/*  582 */       String incDate = (String)param.get("INC_DATE");
/*      */       
/*  584 */       this.log.info("incDate : " + incDate);
/*      */ 
/*      */       
/*      */       try {
/*  588 */         if (null != incDate && !incDate.isEmpty())
/*      */         {
/*  590 */           Date date = formatter.parse(incDate);
/*  591 */           newBusiness.setIncDate(date);
/*  592 */           business.setIncDate(date);
/*      */         }
/*      */       
/*  595 */       } catch (ParseException e) {
/*      */         
/*  597 */         this.log.error(e.getMessage());
/*      */       }
/*      */     
/*      */     }
/*  601 */     else if (business.getBusinessType().equals("028")) {
/*      */       
/*  603 */       String birthDate = (String)param.get("BIRTH_DATE");
/*      */       
/*      */       try {
/*  606 */         if (null == birthDate || birthDate.isEmpty())
/*      */         {
/*  608 */           newBusiness.setBirthDate(null);
/*  609 */           business.setBirthDate(null);
/*      */         }
/*      */         else
/*      */         {
/*  613 */           Date date = formatter.parse(birthDate);
/*  614 */           newBusiness.setBirthDate(date);
/*  615 */           business.setBirthDate(date);
/*      */         }
/*      */       
/*  618 */       } catch (ParseException e) {
/*      */         
/*  620 */         this.log.error(e.getMessage());
/*      */       } 
/*      */     } 
/*      */     
/*  624 */     newBranch.setAddress(((Branch)business.getBranchList().get(0)).getAddress());
/*      */     
/*  626 */     ParameterizedObject pStatus = new ParameterizedObject();
/*  627 */     pStatus.setKey(statusKeyChange);
/*  628 */     newUser.setStatus(pStatus);
/*      */ 
/*      */     
/*  631 */     Map<String, Object> mailMap = new HashMap<String, Object>();
/*  632 */     mailMap.put("transNo", transactionNo);
/*  633 */     mailMap.put("loginType", user.getLoginType().getKey().equals("002") ? "eAccReg" : (user.getLoginType().getKey().equals("003") ? "eSales" : ""));
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  639 */     mailMap.put("loginTypeLink", param.get("loginTypeLink"));
/*  640 */     mailMap.put("loginTypeMsg", user.getLoginType().getKey().equals("002") ? " to file applications for Accreditation and Registration of Software and/or Sales Machines." : (user.getLoginType().getKey().equals("003") ? " to report sales for your registered sales machines." : ""));
/*      */ 
/*      */     
/*  643 */     FullName userFullName = this.userDao.getCompleteNameOfUserInTemp(transactionNo, user.getLoginType().getKey());
/*  644 */     mailMap.put("userName", userFullName.getLastName().trim());
/*      */     
/*  646 */     if (statusKeyChange.equals("005")) {
/*      */ 
/*      */       
/*  649 */       newBusiness.setBusinessDesc(business.getBusinessDesc());
/*  650 */       newBusiness.setBusinessName(business.getBusinessName());
/*      */       
/*  652 */       Map<String, Object> paramMap = new HashMap<String, Object>();
/*  653 */       paramMap.put("BUSINESS", newBusiness);
/*  654 */       paramMap.put("BRANCH", newBranch);
/*  655 */       paramMap.put("USER", newUser);
/*  656 */       paramMap.put("AUDIT_TRAIL", auditTrail);
/*  657 */       paramMap.put("REMARKS", remarks);
/*  658 */       paramMap.put("PROFILE_CODE", profileCode);
/*  659 */       paramMap.put("SECURITY_STATUS", securityStatus);
/*      */       
/*  661 */       if ((loginType.equals("003") || loginType.equals("002")) && business.getBusinessType().equals("028"))
/*      */       {
/*      */         
/*  664 */         newBusiness.setOwner(business.getOwner());
/*      */       }
/*      */       
/*  667 */       if (loginType.equals("002") || loginType.equals("003")) {
/*      */         
/*  669 */         ParameterizedObject pLOB = new ParameterizedObject();
/*  670 */         pLOB.setKey(business.getLineOfBusiness().getKey());
/*  671 */         pLOB.setCode(business.getLineOfBusiness().getCode());
/*  672 */         newBusiness.setLineOfBusiness(pLOB);
/*      */       } 
/*      */       
/*  675 */       if (this.businessDao.getTINCountFromBusinessMain(paramMap) == 0)
/*      */       {
/*  677 */         paramMap.put("TRANS_TYPE", "040");
/*      */       }
/*      */ 
/*      */ 
/*      */       
/*  682 */       if (loginType.equals("002") || loginType.equals("003"))
/*      */       {
/*  684 */         paramMap.put("TRANS_TYPE", "041");
/*      */       }
/*      */       
/*  687 */       if (null == this.userDao.getAuthorizedUserInMain(user.getSecurityProfile().getUsername(), loginType)) {
/*      */         
/*  689 */         newUser.getSecurityProfile().setAccountNonExpired(true);
/*  690 */         newUser.getSecurityProfile().setAccountNonLocked(true);
/*  691 */         newUser.getSecurityProfile().setCredentialsNonExpired(true);
/*  692 */         newUser.getSecurityProfile().setEnabled(true);
/*  693 */         newUser.setAuditrail(auditTrail);
/*  694 */         paramMap.put("TRANS_TYPE", "041");
/*      */       } 
/*  696 */       this.businessDao.updateBusinessDetailsTemp(param);
/*  697 */       this.businessDao.insertIntoSysBusBranchUserHist(paramMap);
/*      */       
/*  699 */       if (!StringUtils.isEmpty(user.getContactDetail().getAlternateEmailAddress()))
/*      */       {
/*  701 */         String[] send = new String[2];
/*  702 */         send[0] = user.getContactDetail().getEmailAddress();
/*  703 */         send[1] = user.getContactDetail().getAlternateEmailAddress();
/*  704 */         this.accountOnholdMailer.send(mailMap, send);
/*      */       }
/*      */       else
/*      */       {
/*  708 */         String[] send = new String[1];
/*  709 */         send[0] = user.getContactDetail().getEmailAddress();
/*  710 */         this.accountOnholdMailer.send(mailMap, send);
/*      */       }
/*      */     
/*      */     }
/*  714 */     else if (statusKeyChange.equals("006")) {
/*      */       
/*  716 */       newBusiness.setBusinessDesc(business.getBusinessDesc());
/*  717 */       newBusiness.setBusinessName(business.getBusinessName());
/*      */ 
/*      */ 
/*      */       
/*  721 */       Map<String, Object> paramMap = new HashMap<String, Object>();
/*  722 */       paramMap.put("BUSINESS", newBusiness);
/*  723 */       paramMap.put("BRANCH", newBranch);
/*  724 */       paramMap.put("USER", newUser);
/*  725 */       paramMap.put("AUDIT_TRAIL", auditTrail);
/*  726 */       paramMap.put("REMARKS", remarks);
/*  727 */       paramMap.put("PROFILE_CODE", profileCode);
/*  728 */       paramMap.put("SECURITY_STATUS", securityStatus);
/*      */       
/*  730 */       if ((loginType.equals("003") || loginType.equals("002")) && business.getBusinessType().equals("028"))
/*      */       {
/*      */         
/*  733 */         newBusiness.setOwner(business.getOwner());
/*      */       }
/*      */       
/*  736 */       if (loginType.equals("002") || loginType.equals("003")) {
/*      */         
/*  738 */         ParameterizedObject pLOB = new ParameterizedObject();
/*  739 */         pLOB.setKey(business.getLineOfBusiness().getKey());
/*  740 */         pLOB.setCode(business.getLineOfBusiness().getCode());
/*  741 */         newBusiness.setLineOfBusiness(pLOB);
/*      */       } 
/*      */       
/*  744 */       if (this.businessDao.getTINCountFromBusinessMain(paramMap) == 0)
/*      */       {
/*  746 */         paramMap.put("TRANS_TYPE", "040");
/*      */       }
/*      */ 
/*      */ 
/*      */       
/*  751 */       if (loginType.equals("002") || loginType.equals("003"))
/*      */       {
/*  753 */         paramMap.put("TRANS_TYPE", "041");
/*      */       }
/*      */       
/*  756 */       if (null == this.userDao.getAuthorizedUserInMain(user.getSecurityProfile().getUsername(), loginType)) {
/*      */         
/*  758 */         newUser.getSecurityProfile().setAccountNonExpired(true);
/*  759 */         newUser.getSecurityProfile().setAccountNonLocked(true);
/*  760 */         newUser.getSecurityProfile().setCredentialsNonExpired(true);
/*  761 */         newUser.getSecurityProfile().setEnabled(true);
/*  762 */         newUser.setAuditrail(auditTrail);
/*  763 */         paramMap.put("TRANS_TYPE", "041");
/*      */       } 
/*  765 */       this.businessDao.updateBusinessDetailsTemp(param);
/*  766 */       this.businessDao.insertIntoSysBusBranchUserHist(paramMap);
/*      */       
/*  768 */       if (!StringUtils.isEmpty(user.getContactDetail().getAlternateEmailAddress()))
/*      */       {
/*  770 */         String[] send = new String[2];
/*  771 */         send[0] = user.getContactDetail().getEmailAddress();
/*  772 */         send[1] = user.getContactDetail().getAlternateEmailAddress();
/*  773 */         this.accountRejectedMailer.send(mailMap, send);
/*      */       }
/*      */       else
/*      */       {
/*  777 */         String[] send = new String[1];
/*  778 */         send[0] = user.getContactDetail().getEmailAddress();
/*  779 */         this.accountRejectedMailer.send(mailMap, send);
/*      */       }
/*      */     
/*  782 */     } else if (statusKeyChange.equals("007")) {
/*      */       
/*  784 */       Map<String, Object> paramMap = new HashMap<String, Object>();
/*  785 */       paramMap.put("BUSINESS", newBusiness);
/*  786 */       paramMap.put("BRANCH", newBranch);
/*  787 */       paramMap.put("USER", newUser);
/*  788 */       paramMap.put("AUDIT_TRAIL", auditTrail);
/*  789 */       paramMap.put("REMARKS", remarks);
/*  790 */       paramMap.put("PROFILE_CODE", profileCode);
/*  791 */       paramMap.put("SECURITY_STATUS", securityStatus);
/*  792 */       newBusiness.setBusinessDesc(business.getBusinessDesc());
/*  793 */       newBusiness.setBusinessName(business.getBusinessName());
/*  794 */       newBranch.setAddress(((Branch)business.getBranchList().get(0)).getAddress());
/*      */       
/*  796 */       if ((loginType.equals("003") || loginType.equals("002")) && business.getBusinessType().equals("028"))
/*      */       {
/*      */         
/*  799 */         newBusiness.setOwner(business.getOwner());
/*      */       }
/*      */       
/*  802 */       if (loginType.equals("002") || loginType.equals("003")) {
/*      */         
/*  804 */         ParameterizedObject pLOB = new ParameterizedObject();
/*  805 */         pLOB.setKey(business.getLineOfBusiness().getKey());
/*  806 */         pLOB.setCode(business.getLineOfBusiness().getCode());
/*  807 */         newBusiness.setLineOfBusiness(pLOB);
/*      */       } 
/*      */       
/*  810 */       if (this.businessDao.getTINCountFromBusinessMain(paramMap) == 0) {
/*      */         
/*  812 */         paramMap.put("TRANS_TYPE", "040");
/*  813 */         this.businessDao.insertIntoBusinessMain(paramMap);
/*  814 */         this.businessDao.insertIntoBusinessHist(paramMap);
/*      */       } 
/*      */ 
/*      */       
/*  818 */       if (this.businessDao.getTINCountFromBusinessBranch(paramMap) == 0) {
/*      */         
/*  820 */         paramMap.put("TRANS_TYPE", "041");
/*  821 */         this.businessDao.insertIntoBusinessBranchMain(paramMap);
/*      */         
/*  823 */         if (loginType.equals("002"))
/*      */         {
/*  825 */           this.businessDao.updateBusinessMain(paramMap);
/*      */           
/*  827 */           this.businessDao.insertIntoBusinessHist(paramMap);
/*      */         
/*      */         }
/*      */ 
/*      */       
/*      */       }
/*  833 */       else if (loginType.equals("002") || loginType.equals("003")) {
/*      */         
/*  835 */         this.businessDao.updateBusinessMain(paramMap);
/*  836 */         paramMap.put("TRANS_TYPE", "041");
/*  837 */         this.businessDao.insertIntoBusinessHist(paramMap);
/*  838 */         this.businessDao.updateBusinessBranch(paramMap);
/*      */       } 
/*      */ 
/*      */       
/*  842 */       if (null == this.userDao.getAuthorizedUserInMain(user.getSecurityProfile().getUsername(), loginType)) {
/*      */ 
/*      */         
/*  845 */         newUser.getSecurityProfile().setAccountNonExpired(true);
/*  846 */         newUser.getSecurityProfile().setAccountNonLocked(true);
/*  847 */         newUser.getSecurityProfile().setCredentialsNonExpired(true);
/*  848 */         newUser.getSecurityProfile().setEnabled(true);
/*  849 */         newUser.setAuditrail(auditTrail);
/*      */         
/*  851 */         this.userDao.insertIntoUserMain(paramMap);
/*  852 */         this.userDao.insertIntoUserPasswordRecord(newUser);
/*  853 */         paramMap.put("TRANS_TYPE", "041");
/*  854 */         this.userDao.insertIntoUserHist(paramMap);
/*  855 */         this.userDao.insertIntoUserProfile(paramMap);
/*      */         
/*  857 */         insertRoleAccessOfUser(loginType, auditTrail, newUser, profileCode);
/*      */       } 
/*      */       
/*  860 */       this.businessDao.insertIntoSysBusBranchUser(paramMap);
/*  861 */       this.businessDao.insertIntoSysBusBranchUserHist(paramMap);
/*      */       
/*  863 */       this.businessDao.deleteBusinessFromTemp(user.getSecurityProfile().getUsername(), transactionNo);
/*      */       
/*  865 */       if (!StringUtils.isEmpty(user.getContactDetail().getAlternateEmailAddress())) {
/*      */         
/*  867 */         String[] send = new String[2];
/*  868 */         send[0] = user.getContactDetail().getEmailAddress();
/*  869 */         send[1] = user.getContactDetail().getAlternateEmailAddress();
/*  870 */         this.accountApprovalMailer.send(mailMap, send);
/*      */       }
/*      */       else {
/*      */         
/*  874 */         String[] send = new String[1];
/*  875 */         send[0] = user.getContactDetail().getEmailAddress();
/*  876 */         this.accountApprovalMailer.send(mailMap, send);
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void setBusinessDao(BusinessDao businessDao) {
/*  884 */     this.businessDao = businessDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setUserDao(UserDao userDao) {
/*  889 */     this.userDao = userDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSequenceDao(SequenceDao sequenceDao) {
/*  894 */     this.sequenceDao = sequenceDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setEnrollmentMailer(TemplateMailer enrollmentMailer) {
/*  899 */     this.enrollmentMailer = enrollmentMailer;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setAccountApprovalMailer(TemplateMailer accountApprovalMailer) {
/*  904 */     this.accountApprovalMailer = accountApprovalMailer;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setAccountOnholdMailer(TemplateMailer accountOnholdMailer) {
/*  909 */     this.accountOnholdMailer = accountOnholdMailer;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setAccountRejectedMailer(TemplateMailer accountRejectedMailer) {
/*  914 */     this.accountRejectedMailer = accountRejectedMailer;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public String getBusinessDetailsOfTINAndBranch(String businessTIN, String branchCode) {
/*  920 */     return this.businessDao.getBusinessDetailsOfTINAndBranch(businessTIN, branchCode);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean isBusinessExistingInMain(String businessTIN, String branchCode) {
/*  926 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/*  927 */     paramMap.put("BUSINESS_TIN", businessTIN);
/*  928 */     paramMap.put("BRANCH_CODE", branchCode);
/*      */     
/*  930 */     return (this.businessDao.checkBusinessBranchFromMain(paramMap) > 0);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public String getRDOCodeByTINAndBranch(String businessTIN, String branchCode) {
/*  936 */     return this.businessDao.getRDOCodeByTINAndBranch(businessTIN, branchCode);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public Business getAcctDetailsByUserLoginTypeAndTransNum(Map<String, Object> param) {
/*  942 */     Business business = new Business();
/*      */     
/*  944 */     String loginType = (String)param.get("LOGIN_TYPE");
/*  945 */     business = this.businessDao.getBusinessInMain(param, loginType);
/*      */     
/*  947 */     if (null == business) {
/*  948 */       return null;
/*      */     }
/*  950 */     List<Branch> branchList = new ArrayList<Branch>();
/*  951 */     String businessTIN = business.getBusinessTIN();
/*  952 */     param.put("BUSINESS_TIN", businessTIN);
/*  953 */     branchList = this.businessDao.getBranchCodesFromMainByUserNameAndLoginType(param);
/*      */     
/*  955 */     List<AuthorizedUser> userList = new ArrayList<AuthorizedUser>();
/*  956 */     AuthorizedUser user = this.userDao.getUserFromMainByUserNameAndLoginType(param);
/*  957 */     userList.add(user);
/*      */     
/*  959 */     for (int y = 0; y < branchList.size(); y++)
/*      */     {
/*  961 */       ((Branch)branchList.get(y)).setAuthorizedUserList(userList);
/*      */     }
/*  963 */     business.setBranchList(branchList);
/*      */     
/*  965 */     business.setBusinessName(this.businessDao.getBusinessByTransNum(param));
/*      */     
/*  967 */     return business;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public List<Map<String, Object>> getUserEnrollmentHistory(String transactionNo) {
/*  974 */     return this.businessDao.getUserEnrollmentHistory(transactionNo);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public Business getBusinessDetailsOfTINBranchAccredCd(String tin, String branchCode, String accredCd) {
/*  980 */     return this.businessDao.getBusinessDetailsOfTINBranchAccredCd(tin, branchCode, accredCd);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public Business getBusinessDetailsByBusTIN(Map<String, Object> param) {
/*  986 */     Business business = new Business();
/*  987 */     business = this.businessDao.getBusinessDetailsByBusTIN(param);
/*      */     
/*  989 */     List<Branch> branchList = new ArrayList<Branch>();
/*  990 */     String businessTIN = business.getBusinessTIN();
/*  991 */     param.put("BUSINESS_TIN", businessTIN);
/*      */     
/*  993 */     branchList = this.businessDao.getBranchCodesFromMainByUserNameAndLoginType(param);
/*      */     
/*  995 */     List<AuthorizedUser> userList = new ArrayList<AuthorizedUser>();
/*  996 */     AuthorizedUser user = this.userDao.getAuthorizedUserByLogin(param);
/*  997 */     userList.add(user);
/*      */     
/*  999 */     for (int y = 0; y < branchList.size(); y++)
/*      */     {
/* 1001 */       ((Branch)branchList.get(y)).setAuthorizedUserList(userList);
/*      */     }
/* 1003 */     business.setBranchList(branchList);
/*      */     
/* 1005 */     return business;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean isBusinessNameValidFromITS(Map<String, Object> paramMap) {
/* 1011 */     int recordCount = this.businessDao.checkBusinessNameFromITS(paramMap);
/* 1012 */     if (recordCount > 0)
/*      */     {
/* 1014 */       return true;
/*      */     }
/*      */ 
/*      */     
/* 1018 */     return false;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private void insertRoleAccessOfUser(String loginType, AuditTrail auditTrail, AuthorizedUser user, String profileCode) {
/* 1025 */     String roleCode = "";
/* 1026 */     if (loginType.equals("002")) {
/*      */       
/* 1028 */       roleCode = "ROL0000005";
/* 1029 */       if (profileCode.equals("PRF0000013"))
/*      */       {
/* 1031 */         roleCode = "ROL0000047";
/*      */       }
/*      */     }
/*      */     else {
/*      */       
/* 1036 */       roleCode = "ROL0000006";
/*      */     } 
/* 1038 */     List<String> functionList = this.userDao.getMenuFunctionsOfRole(roleCode);
/*      */     
/* 1040 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/* 1041 */     paramMap.put("USER", user);
/* 1042 */     paramMap.put("AUDIT_TRAIL", auditTrail);
/*      */     
/* 1044 */     for (String function : functionList) {
/*      */       
/* 1046 */       if (function.length() > 3) {
/*      */         
/* 1048 */         paramMap.put("FUNCTION_CD", function);
/* 1049 */         this.userDao.insertUserAssignments(paramMap);
/*      */         
/*      */         continue;
/*      */       } 
/* 1053 */       paramMap.put("USER_NAME", user.getSecurityProfile().getUsername());
/* 1054 */       paramMap.put("LOGIN_TYPE", user.getLoginType().getKey());
/* 1055 */       paramMap.put("REPORT_CODE", function);
/* 1056 */       this.userDao.insertUserReportAccess(paramMap);
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public List<LineOfBusinessForm> getLOBList() {
/* 1065 */     return this.userDao.getLOBList();
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public String getLOBByMITandCode(Map<Object, String> lobMap) {
/* 1071 */     return this.userDao.getLOBByMITandCode(lobMap);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public int getMachinePermitCount(Map<Object, String> paramMap) throws NoMINException {
/* 1079 */     int minCount = this.businessDao.getMachinePermitCount(paramMap);
/*      */     
/* 1081 */     if (minCount > 0)
/*      */     {
/* 1083 */       return minCount;
/*      */     }
/*      */ 
/*      */     
/* 1087 */     throw new NoMINException("No registered MIN for the specified TIN and Branch.");
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void activateDeactivateUser(Map<String, Object> param) {
/* 1094 */     AuthorizedUser user = (AuthorizedUser)param.get("USER");
/* 1095 */     Business business = (Business)param.get("BUSINESS");
/* 1096 */     Branch branch = business.getBranchList().get(0);
/*      */     
/* 1098 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/* 1099 */     AuditTrail auditTrail = (AuditTrail)param.get("AUDIT_TRAIL");
/* 1100 */     user.setLoginType(auditTrail.getLoginType());
/* 1101 */     paramMap.put("USER", user);
/* 1102 */     List<Branch> branchList = this.userDao.getBranchListOfUser(param);
/*      */     
/* 1104 */     boolean statusChange = Boolean.valueOf(param.get("STATUS_CHANGE").toString()).booleanValue();
/* 1105 */     param.put("ENABLED", null);
/*      */     
/* 1107 */     if (statusChange) {
/*      */       
/* 1109 */       String isEnabled = "1";
/* 1110 */       if (user.getStatus().getKey().equals("007")) {
/*      */         
/* 1112 */         isEnabled = "1";
/*      */ 
/*      */       
/*      */       }
/* 1116 */       else if (branchList.size() == 1 && branch.getBranchCode().equals(((Branch)branchList.get(0)).getBranchCode())) {
/*      */         
/* 1118 */         isEnabled = "0";
/*      */       } 
/*      */       
/* 1121 */       param.put("ENABLED", isEnabled);
/*      */     } 
/* 1123 */     param.put("USER", user);
/* 1124 */     param.put("BUSINESS", business);
/* 1125 */     param.put("BRANCH", branch);
/*      */     
/* 1127 */     this.userDao.activateDeactivateUser(param);
/*      */     
/* 1129 */     if (statusChange) {
/*      */       
/* 1131 */       this.userDao.insertIntoUserBusinessBranchHist(param);
/* 1132 */       this.userDao.updateUseSysBusBranchStatus(param);
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setAccountActivateDeactivate(TemplateMailer accountActivateDeactivate) {
/* 1178 */     this.accountActivateDeactivate = accountActivateDeactivate;
/*      */   }
/*      */ }


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\main\enrollment\AccountEnrollmentServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */