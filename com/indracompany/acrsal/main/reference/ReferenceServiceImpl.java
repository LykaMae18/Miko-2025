/*     */ package com.indracompany.acrsal.main.reference;
/*     */ 
/*     */ import com.indracompany.acrsal.api.reference.ReferenceService;
/*     */ import com.indracompany.acrsal.dao.business.ReferenceDao;
/*     */ import com.indracompany.acrsal.models.ParameterizedObject;
/*     */ import com.indracompany.sysad.forms.ListOfValuesDisplayForm;
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import org.apache.log4j.Logger;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class ReferenceServiceImpl
/*     */   implements ReferenceService
/*     */ {
/*     */   private static final long serialVersionUID = 1L;
/*  22 */   private static final Logger log = Logger.getLogger(ReferenceServiceImpl.class);
/*     */ 
/*     */   
/*     */   private ReferenceDao referenceDao;
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getReferenceObjects(String referenceType) {
/*  29 */     if (referenceType.equals("AST"))
/*     */     {
/*  31 */       return sortAccStatus(this.referenceDao.getReference(referenceType));
/*     */     }
/*  33 */     if (referenceType.equals("RST"))
/*     */     {
/*  35 */       return sortRegStatus(this.referenceDao.getReference(referenceType));
/*     */     }
/*  37 */     if (referenceType.equals("STA"))
/*     */     {
/*  39 */       return sortEnrollStatus(this.referenceDao.getReference(referenceType));
/*     */     }
/*     */ 
/*     */ 
/*     */     
/*  44 */     return this.referenceDao.getReference(referenceType);
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private List<ParameterizedObject> sortEnrollStatus(List<ParameterizedObject> reference) {
/*  51 */     List<String> statusSorting = new ArrayList<String>();
/*     */     
/*  53 */     statusSorting.add("NEW");
/*  54 */     statusSorting.add("ON-HOLD");
/*  55 */     statusSorting.add("FOR EVALUATION");
/*  56 */     statusSorting.add("ACTIVATED");
/*  57 */     statusSorting.add("DEACTIVATED");
/*  58 */     statusSorting.add("REJECTED");
/*     */     
/*  60 */     List<ParameterizedObject> newList = new ArrayList<ParameterizedObject>();
/*     */     
/*  62 */     for (String outList : statusSorting) {
/*     */       
/*  64 */       for (ParameterizedObject checkParam : reference) {
/*     */         
/*  66 */         if (checkParam.getValue().equalsIgnoreCase(outList))
/*     */         {
/*  68 */           newList.add(checkParam);
/*     */         }
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/*  74 */     return newList;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private List<ParameterizedObject> sortAccStatus(List<ParameterizedObject> stats) {
/*  81 */     List<String> statusSorting = new ArrayList<String>();
/*     */     
/*  83 */     statusSorting.add("NEW");
/*  84 */     statusSorting.add("FOR ASSIGNMENT");
/*  85 */     statusSorting.add("FOR EVALUATION");
/*  86 */     statusSorting.add("RECOMMENDED");
/*  87 */     statusSorting.add("APPROVED");
/*  88 */     statusSorting.add("REVOKED");
/*  89 */     statusSorting.add("DENIED");
/*     */     
/*  91 */     List<ParameterizedObject> newList = new ArrayList<ParameterizedObject>();
/*     */     
/*  93 */     for (String outList : statusSorting) {
/*     */       
/*  95 */       for (ParameterizedObject checkParam : stats) {
/*     */         
/*  97 */         if (checkParam.getValue().equalsIgnoreCase(outList))
/*     */         {
/*  99 */           newList.add(checkParam);
/*     */         }
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 105 */     return newList;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private List<ParameterizedObject> sortRegStatus(List<ParameterizedObject> stats) {
/* 112 */     List<String> statusSorting = new ArrayList<String>();
/*     */     
/* 114 */     statusSorting.add("NEW");
/* 115 */     statusSorting.add("APPROVED FOR PRINTING");
/* 116 */     statusSorting.add("PRINTED");
/* 117 */     statusSorting.add("DENIED");
/* 118 */     statusSorting.add("CANCELLED");
/*     */     
/* 120 */     List<ParameterizedObject> newList = new ArrayList<ParameterizedObject>();
/*     */     
/* 122 */     for (String outList : statusSorting) {
/*     */       
/* 124 */       for (ParameterizedObject checkParam : stats) {
/*     */         
/* 126 */         if (checkParam.getValue().replaceAll(" ", "").equalsIgnoreCase(outList.replace(" ", "")))
/*     */         {
/* 128 */           newList.add(checkParam);
/*     */         }
/*     */       } 
/*     */     } 
/*     */ 
/*     */     
/* 134 */     return newList;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getStatusListForApproval(String status) {
/* 141 */     return this.referenceDao.getStatusListForApproval(status);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ParameterizedObject> getListOfValues(Map<String, Object> initParam) {
/* 147 */     return this.referenceDao.getListOfValues(initParam);
/*     */   }
/*     */ 
/*     */   
/*     */   public void setReferenceDao(ReferenceDao referenceDao) {
/* 152 */     this.referenceDao = referenceDao;
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void insertReference(Map<String, Object> paramMap) {
/* 159 */     this.referenceDao.insertRefence(paramMap);
/*     */ 
/*     */     
/* 162 */     Map<String, Object> pMap = new HashMap<String, Object>();
/* 163 */     pMap.put("REF_TYPE_CD", paramMap.get("REF_TYPE_CD"));
/* 164 */     pMap.put("REF_DESC", paramMap.get("REF_DESC"));
/*     */     
/* 166 */     String newRefCd = this.referenceDao.getLOVRefCD(paramMap);
/*     */     
/* 168 */     log.debug("newRefCd  : " + newRefCd);
/* 169 */     paramMap.put("REF_CD", newRefCd);
/* 170 */     paramMap.put("TRANS_TYPE", "040");
/* 171 */     log.debug("paramMap.put(REF_CD) : " + paramMap.get("REF_CD"));
/* 172 */     log.debug("FOR HISTORY: " + paramMap);
/*     */     
/* 174 */     this.referenceDao.insertReferenceHistory(paramMap);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void updateReference(Map<String, Object> paramMap) {
/* 180 */     this.referenceDao.updateRefence(paramMap);
/*     */ 
/*     */     
/* 183 */     paramMap.put("TRANS_TYPE", "041");
/* 184 */     log.debug("FOR HISTORY: " + paramMap);
/*     */     
/* 186 */     this.referenceDao.insertReferenceHistory(paramMap);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public void deleteReference(String referenceCode) {
/* 192 */     this.referenceDao.deleteRefence(referenceCode);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public Map<String, Object> getLOV(Map<String, Object> paramMap) {
/* 198 */     return this.referenceDao.getLOV(paramMap);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public List<ListOfValuesDisplayForm> getReferenceForAddDisplay(String referenceType) {
/* 204 */     return this.referenceDao.getReferenceForAddDisplay(referenceType);
/*     */   }
/*     */ 
/*     */   
/*     */   public ParameterizedObject getReferenceObject(String refCode) {
/* 209 */     return this.referenceDao.getReferenceObject(refCode);
/*     */   }
/*     */ }


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\main\reference\ReferenceServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */