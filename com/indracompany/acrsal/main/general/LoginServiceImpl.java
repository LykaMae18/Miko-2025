/*    */ package com.indracompany.acrsal.main.general;
/*    */ 
/*    */ import com.indracompany.acrsal.api.general.LoginService;
/*    */ import com.indracompany.acrsal.dao.business.BusinessDao;
/*    */ import com.indracompany.acrsal.dao.business.UserDao;
/*    */ import com.indracompany.acrsal.models.AuthorizedUser;
/*    */ import com.indracompany.acrsal.models.Branch;
/*    */ import com.indracompany.acrsal.models.Business;
/*    */ import java.util.HashMap;
/*    */ import java.util.List;
/*    */ import java.util.Map;
/*    */ import org.apache.log4j.Logger;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class LoginServiceImpl
/*    */   implements LoginService
/*    */ {
/*    */   private UserDao userDao;
/*    */   private BusinessDao businessDao;
/* 23 */   public final String username = "USER_NAME";
/* 24 */   public final String loginType = "LOGIN_TYPE";
/* 25 */   public final String branchCode = "BRANCH_CODE";
/*    */   
/* 27 */   private final Logger logger = Logger.getLogger(LoginServiceImpl.class);
/*    */ 
/*    */ 
/*    */   
/*    */   public AuthorizedUser getAuthorizedUserByLogin(Map<String, Object> userMap) {
/* 32 */     return this.userDao.getAuthorizedUserByLogin(userMap);
/*    */   }
/*    */ 
/*    */   
/*    */   public List<Branch> getBranchListOfUser(AuthorizedUser user) {
/* 37 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/* 38 */     paramMap.put("USER", user);
/* 39 */     return this.userDao.getBranchListOfUser(paramMap);
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public Business getBusinessByUsernameAndLoginType(String username, String loginType, String selectedBranch) {
/* 45 */     Map<String, String> param = new HashMap<String, String>();
/* 46 */     getClass(); param.put("USER_NAME", username);
/* 47 */     getClass(); param.put("LOGIN_TYPE", loginType);
/* 48 */     getClass(); param.put("BRANCH_CODE", selectedBranch);
/*    */     
/* 50 */     return this.businessDao.getBusinessByUsernameAndLoginType(param);
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public Branch getBranchByUsernameAndSelectedBranch(String username, String loginType, String branchCode) {
/* 56 */     Map<String, String> param = new HashMap<String, String>();
/* 57 */     getClass(); param.put("USER_NAME", username);
/* 58 */     getClass(); param.put("LOGIN_TYPE", loginType);
/* 59 */     getClass(); param.put("BRANCH_CODE", branchCode);
/*    */     
/* 61 */     return this.businessDao.getBranchByUsernameAndSelectedBranch(param);
/*    */   }
/*    */ 
/*    */   
/*    */   public void setUserDao(UserDao userDao) {
/* 66 */     this.userDao = userDao;
/*    */   }
/*    */ 
/*    */   
/*    */   public void setBusinessDao(BusinessDao businessDao) {
/* 71 */     this.businessDao = businessDao;
/*    */   }
/*    */ 
/*    */ 
/*    */   
/*    */   public void updateLastLoginDate(String username) {
/* 77 */     this.logger.info("updating last login date by user: " + username);
/* 78 */     this.userDao.updateLastLoginDate(username);
/*    */   }
/*    */ }


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\main\general\LoginServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */