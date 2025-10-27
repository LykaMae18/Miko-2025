package com.indracompany.acrsal.api.general;

import com.indracompany.acrsal.models.AuthorizedUser;
import com.indracompany.acrsal.models.Branch;
import com.indracompany.acrsal.models.Business;
import java.util.List;
import java.util.Map;

public interface LoginService {
  AuthorizedUser getAuthorizedUserByLogin(Map<String, Object> paramMap);
  
  List<Branch> getBranchListOfUser(AuthorizedUser paramAuthorizedUser);
  
  Business getBusinessByUsernameAndLoginType(String paramString1, String paramString2, String paramString3);
  
  Branch getBranchByUsernameAndSelectedBranch(String paramString1, String paramString2, String paramString3);
  
  void updateLastLoginDate(String paramString);
}


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\api\general\LoginService.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */