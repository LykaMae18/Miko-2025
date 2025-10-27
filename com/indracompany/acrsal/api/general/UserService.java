package com.indracompany.acrsal.api.general;

import com.indracompany.acrsal.exception.PasswordInvalidException;
import com.indracompany.acrsal.exception.PasswordRecentlyUsedException;
import com.indracompany.acrsal.exception.UserNullException;
import com.indracompany.acrsal.models.AuthorizedUser;
import com.indracompany.acrsal.models.HelpFile;
import com.indracompany.acrsal.models.SecurityProfile;
import com.indracompany.core.security.service.UserService;
import java.util.List;
import java.util.Map;

public interface UserService extends UserService {
  void changePassword(AuthorizedUser paramAuthorizedUser, String paramString1, String paramString2) throws PasswordRecentlyUsedException, PasswordInvalidException;
  
  void updateNewUserPassword(String paramString1, String paramString2);
  
  AuthorizedUser getAuthorizedUserInMain(String paramString1, String paramString2);
  
  SecurityProfile getUserSecurityDetails(String paramString1, String paramString2) throws UserNullException;
  
  void forgotPasswordUpdate(AuthorizedUser paramAuthorizedUser);
  
  List<HelpFile> getHelpFiles(String paramString);
  
  List<HelpFile> getHelpFiles(Map<String, Object> paramMap);
  
  HelpFile getHelpFileByNameCategoryLocation(String paramString1, String paramString2, String paramString3, String paramString4);
  
  Map<String, Object> getBusinessTINAndBranchByActiveUser(Map<String, Object> paramMap);
  
  AuthorizedUser getAuthorizedUserByLogin(Map<String, Object> paramMap);
}


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\api\general\UserService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */