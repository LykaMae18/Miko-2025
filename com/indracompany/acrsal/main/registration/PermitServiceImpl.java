/*     */ package com.indracompany.acrsal.main.registration;
/*     */ 
/*     */ import com.indracompany.acrsal.api.registration.PermitService;
/*     */ import com.indracompany.acrsal.dao.business.SequenceDao;
/*     */ import com.indracompany.acrsal.dao.registration.PermitDao;
/*     */ import com.indracompany.acrsal.dao.registration.RegistrationDao;
/*     */ import com.indracompany.acrsal.forms.MachineForm;
/*     */ import com.indracompany.acrsal.forms.RegistrationViewForm;
/*     */ import com.indracompany.acrsal.models.AuditTrail;
/*     */ import com.indracompany.acrsal.models.ParameterizedObject;
/*     */ import com.indracompany.acrsal.models.reports.PermitToUse;
/*     */ import com.indracompany.core.reporting.ReportGenerator;
/*     */ import com.indracompany.core.reporting.model.ReportContainer;
/*     */ import com.lowagie.text.pdf.BarcodePDF417;
/*     */ import java.awt.Color;
/*     */ import java.awt.Image;
/*     */ import java.text.SimpleDateFormat;
/*     */ import java.util.ArrayList;
/*     */ import java.util.Date;
/*     */ import java.util.HashMap;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import net.sf.jasperreports.engine.JRDataSource;
/*     */ import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
/*     */ import org.apache.commons.lang.StringUtils;
/*     */ import org.apache.log4j.Logger;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ public class PermitServiceImpl
/*     */   implements PermitService
/*     */ {
/*     */   private static final long serialVersionUID = 8339907360869577038L;
/*  35 */   private static final Logger log = Logger.getLogger(PermitServiceImpl.class);
/*     */   
/*     */   private PermitDao permitDao;
/*     */   
/*     */   private SequenceDao sequenceDao;
/*     */   
/*     */   private RegistrationDao registrationDao;
/*     */   
/*     */   private ReportGenerator permitFinalCRMGenerator;
/*     */   private ReportGenerator permitProvisionalCRMGenerator;
/*     */   private ReportGenerator permitFinalOSMGenerator;
/*     */   private ReportGenerator permitProvisionalOSMGenerator;
/*     */   private ReportGenerator permitSPMGenerator;
/*     */   private ReportGenerator permitFinalPOSDecentralized;
/*     */   private ReportGenerator permitFinalPOSGlobal;
/*     */   private ReportGenerator permitFinalPOSServerCons;
/*     */   private ReportGenerator permitFinalPOSStandAlone;
/*     */   private ReportGenerator permitProvisionalPOSDecentralized;
/*     */   private ReportGenerator permitProvisionalPOSGlobal;
/*     */   private ReportGenerator permitProvisionalPOSServerCons;
/*     */   private ReportGenerator permitProvisionalPOSStandAlone;
/*     */   private static final String HASH_CODE = "HASH_CODE";
/*     */   
/*     */   public void setPermitDao(PermitDao permitDao) {
/*  59 */     this.permitDao = permitDao;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public PermitToUse getPermitToUseByPermitCode(String permitCode, String machineMin, String machineType) {
/*  65 */     log.info("fetching records based on permitCode: " + permitCode);
/*  66 */     log.info("Machine MIN: " + machineMin);
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  71 */     PermitToUse permitToUse = this.permitDao.getPermitToUseDetailsByPermitCodeAndMIN(permitCode, machineMin);
/*  72 */     log.info("permitToUse: " + permitToUse);
/*     */ 
/*     */ 
/*     */ 
/*     */     
/*  77 */     List<MachineForm> machines = this.permitDao.getPermitToUseMachineListByPermitCode(permitCode);
/*     */     
/*  79 */     log.info("fetched machines: " + machines.size());
/*     */     
/*  81 */     log.info("configuring machine details:");
/*     */     
/*  83 */     log.info("configuring server detail..");
/*  84 */     log.info("configuring server detail..: machines : " + machines);
/*     */ 
/*     */ 
/*     */     
/*  88 */     log.info("configuring server detail..: getMachineFromListByMachineTitle : " + getMachineFromListByMachineTitle(machines, "113"));
/*  89 */     if (null != getMachineFromListByMachineTitle(machines, "113"))
/*     */     {
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */       
/*  96 */       permitToUse.setServer(getMachineFromListByMachineTitle(machines, "113"));
/*     */     }
/*     */     
/*  99 */     log.info("configuring server consolidator detail..");
/* 100 */     permitToUse.setServerConsolidator(getMachineFromListByMachineTitle(machines, "114"));
/*     */ 
/*     */     
/* 103 */     log.info("configuring terminal detail..");
/* 104 */     if (!machineType.equals("POS_055_WTG")) {
/*     */       
/* 106 */       permitToUse.setTerminal(getMachineFromListByMIN(machines, permitToUse.getMachineMin()));
/*     */     }
/*     */     else {
/*     */       
/* 110 */       permitToUse.setTerminal(getMachineFromListByMIN(machines, machineMin));
/*     */     } 
/*     */     
/* 113 */     log.info("machine details configured.. setting up machine type.");
/* 114 */     permitToUse = setUpMachineType(machineType, permitToUse);
/*     */     
/* 116 */     log.info("setting up taxpayer name.");
/* 117 */     log.info("setting up taxpayer name.: permitToUse : " + permitToUse);
/* 118 */     setupTaxPayerName(permitToUse);
/*     */     
/* 120 */     log.info("setting up Tin & Branch.");
/* 121 */     setupBusinessTin(permitToUse);
/*     */     
/* 123 */     log.info("successfully retrieve permit & machine details");
/*     */     
/* 125 */     return permitToUse;
/*     */   }
/*     */ 
/*     */   
/*     */   private void setupBusinessTin(PermitToUse permitToUse) {
/* 130 */     String tin = permitToUse.getBusiness().getBusinessTIN();
/* 131 */     String branch = permitToUse.getBranch().getBranchCode();
/*     */     
/* 133 */     String formatted = tin.substring(0, 3) + "-" + tin.substring(3, 6) + "-" + tin.substring(6, tin.length()) + "-" + branch;
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */     
/* 142 */     permitToUse.setFormattedTinBranch(formatted);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   private void setupTaxPayerName(PermitToUse permitToUse) {
/* 148 */     if (permitToUse.getBusiness().getBusinessType().equals("028")) {
/*     */       
/* 150 */       permitToUse.setTaxpayerName("" + permitToUse.getBusiness().getOwner().getFirstName() + " " + permitToUse.getBusiness().getOwner().getMiddleName().substring(0, 1) + "." + " " + permitToUse.getBusiness().getOwner().getLastName() + " with business name " + permitToUse.getBusiness().getBusinessName());
/*     */ 
/*     */ 
/*     */     
/*     */     }
/*     */     else {
/*     */ 
/*     */ 
/*     */ 
/*     */       
/* 160 */       permitToUse.setTaxpayerName("" + permitToUse.getBusiness().getBusinessDesc() + " with business name " + permitToUse.getBusiness().getBusinessName());
/*     */     } 
/*     */   }
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   private MachineForm getMachineFromListByMachineTitle(List<MachineForm> machineList, String machineTitle) {
/* 170 */     for (MachineForm machine : machineList) {
/*     */       
/* 172 */       if (!StringUtils.isEmpty(machine.getMachineTitle().getKey()) && machine.getMachineTitle().getKey().equals(machineTitle))
/*     */       {
/* 174 */         return machine;
/*     */       }
/*     */     } 
/* 177 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private MachineForm getMachineFromListByMIN(List<MachineForm> machineList, String machineMin) {
/* 182 */     for (MachineForm machine : machineList) {
/*     */       
/* 184 */       if (!StringUtils.isEmpty(machine.getMachineId()) && machine.getMachineId().equals(machineMin))
/*     */       {
/* 186 */         return machine;
/*     */       }
/*     */     } 
/* 189 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   private PermitToUse setUpMachineType(String machineType, PermitToUse permitToUse) {
/* 194 */     PermitToUse result = permitToUse;
/*     */     
/* 196 */     String machine_type = machineType.substring(0, 3);
/* 197 */     if (machine_type.equals("POS")) {
/*     */       
/* 199 */       result = setUpPOS(machineType, permitToUse);
/*     */     }
/*     */     else {
/*     */       
/* 203 */       log.debug("permitToUse.getMachineGroup(): " + permitToUse.getMachineGroup());
/* 204 */       if (machineType.equals("SPM_111") || machineType.equals("CRM_050")) {
/*     */ 
/*     */         
/* 207 */         result.getTerminal().setMachineType(permitToUse.getMachineGroup().getMachineType());
/*     */       }
/* 209 */       else if (machineType.equals("OSM_053")) {
/*     */         
/* 211 */         result.getTerminal().setMachineType(permitToUse.getMachineGroup().getOsmType());
/*     */       } 
/*     */     } 
/* 214 */     return result;
/*     */   }
/*     */ 
/*     */   
/*     */   private PermitToUse setUpPOS(String machineType, PermitToUse permitToUse) {
/* 219 */     String With_Server_Consolidator = "Connected To A Server - With Server Consolidator";
/* 220 */     String With_Terminal_Global = "Connected To A Server - With Terminal >> Global";
/* 221 */     String With_Terminal_Decentralized = "Connected To A Server - With Terminal >> Decentralized";
/* 222 */     PermitToUse result = permitToUse;
/*     */     
/* 224 */     log.info("permitToUse.getMachineGroup().getPosType(): " + permitToUse.getMachineGroup().getPosType());
/*     */     
/* 226 */     result.getTerminal().setMachineType(permitToUse.getMachineGroup().getPosType());
/*     */     
/* 228 */     if (machineType.equals("POS_055_WSC")) {
/*     */       
/* 230 */       ParameterizedObject machineTypeParam = permitToUse.getMachineGroup().getPosType();
/*     */       
/* 232 */       machineTypeParam.setValue("Connected To A Server - With Server Consolidator");
/* 233 */       result.getTerminal().setMachineType(machineTypeParam);
/*     */ 
/*     */       
/*     */       try {
/* 237 */         result.getServer().setMachineType(machineTypeParam);
/*     */       }
/* 239 */       catch (Exception e) {
/*     */         
/* 241 */         log.info("No server");
/*     */       } 
/* 243 */       result.getServerConsolidator().setMachineType(machineTypeParam);
/*     */     }
/* 245 */     else if (machineType.equals("POS_055_WTD")) {
/*     */       
/* 247 */       ParameterizedObject machineTypeParam = permitToUse.getMachineGroup().getPosType();
/*     */       
/* 249 */       machineTypeParam.setValue("Connected To A Server - With Terminal >> Decentralized");
/* 250 */       result.getTerminal().setMachineType(machineTypeParam);
/*     */       
/* 252 */       result.getServer().setMachineType(machineTypeParam);
/*     */     }
/* 254 */     else if (machineType.equals("POS_055_WTG")) {
/*     */       
/* 256 */       ParameterizedObject machineTypeParam = permitToUse.getMachineGroup().getPosType();
/*     */       
/* 258 */       machineTypeParam.setValue("Connected To A Server - With Terminal >> Global");
/* 259 */       result.getTerminal().setMachineType(machineTypeParam);
/*     */       
/* 261 */       result.getServer().setMachineType(machineTypeParam);
/*     */     } 
/* 263 */     return result;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public ReportContainer printPermitToUse(String machineType, PermitToUse permitToUse, String barCode, String printAsOriginal, AuditTrail auditTrail) {
/* 269 */     BarcodePDF417 barPdf = new BarcodePDF417();
/* 270 */     barPdf.setText(barCode);
/* 271 */     barPdf.setYHeight(2.0F);
/* 272 */     Image barcodeImage = barPdf.createAwtImage(Color.BLACK, Color.WHITE);
/*     */     
/* 274 */     Map<String, Object> permitMap = new HashMap<String, Object>();
/*     */     
/* 276 */     String hashCode = "";
/* 277 */     Boolean reprinted = Boolean.valueOf(false);
/*     */     
/* 279 */     List<PermitToUse> permitToUseList = new ArrayList<PermitToUse>();
/* 280 */     permitToUseList.add(permitToUse);
/*     */     
/* 282 */     Map<String, Object> hashCodeMap = new HashMap<String, Object>();
/* 283 */     hashCodeMap.put("AUDIT_TRAIL", auditTrail);
/* 284 */     hashCodeMap.put("REPORT_NO", permitToUse.getPermit().getPermitCode());
/*     */     
/* 286 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/* 287 */     RegistrationViewForm regFormContainer = new RegistrationViewForm();
/* 288 */     ParameterizedObject permitStat = new ParameterizedObject("RST", "106", null);
/* 289 */     permitToUse.getPermit().setPermitStatus(permitStat);
/* 290 */     regFormContainer.setPermit(permitToUse.getPermit());
/* 291 */     regFormContainer.setMin(permitToUse.getTerminal().getMachineId());
/* 292 */     paramMap.put("REGFORM", regFormContainer);
/* 293 */     paramMap.put("AUDIT_TRAIL", auditTrail);
/*     */     
/* 295 */     if (!auditTrail.getUserSecurity().getProfile().getProfileCode().equals("PRF0000008") || !auditTrail.getUserSecurity().getProfile().getProfileCode().equals("PRF0000013")) {
/*     */ 
/*     */ 
/*     */       
/* 299 */       if (printAsOriginal.equals("true") || permitToUse.getPermit().getPermitStatus().getKey().equals("105"))
/*     */       {
/*     */ 
/*     */         
/* 303 */         hashCodeMap.put("IS_REPRINT", "N");
/*     */         
/* 305 */         hashCode = this.permitDao.getPermitHashCodeFromDB(permitToUse.getPermit().getPermitCode());
/*     */         
/* 307 */         if (null == hashCode) {
/*     */           
/* 309 */           hashCode = generateHashCode(machineType);
/*     */           
/* 311 */           hashCodeMap.put("REPORT_CD", "093");
/* 312 */           hashCodeMap.put("PRINT_COUNTER", "1");
/* 313 */           hashCodeMap.put("PRINT_AS_ORIG_CTR", "1");
/* 314 */           hashCodeMap.put("REPORT_HASH_CD", hashCode);
/*     */           
/* 316 */           hashCodeMap.put("TRANS_TYPE", "040");
/* 317 */           this.permitDao.insertPermitHashCodeToDB(hashCodeMap);
/*     */         }
/*     */         else {
/*     */           
/* 321 */           hashCode = generateHashCode(machineType);
/* 322 */           hashCodeMap.put("REPORT_HASH_CD", hashCode);
/* 323 */           hashCodeMap.put("TRANS_TYPE", "041");
/* 324 */           this.permitDao.updatePermitHashCodeInDB(hashCodeMap);
/*     */         } 
/*     */         
/* 327 */         updatePermitStatus(permitToUse.getPermit().getPermitCode(), "106", auditTrail);
/* 328 */         this.registrationDao.updateSLSMachineStatus(paramMap);
/*     */         
/* 330 */         reprinted = Boolean.valueOf(false);
/*     */       }
/*     */       else
/*     */       {
/* 334 */         hashCode = this.permitDao.getPermitHashCodeFromDB(permitToUse.getPermit().getPermitCode());
/* 335 */         hashCodeMap.put("IS_REPRINT", "Y");
/* 336 */         hashCodeMap.put("TRANS_TYPE", "041");
/* 337 */         this.permitDao.updatePermitHashCodeInDB(hashCodeMap);
/* 338 */         reprinted = Boolean.valueOf(true);
/*     */       }
/*     */     
/*     */     } else {
/*     */       
/* 343 */       hashCode = this.permitDao.getPermitHashCodeFromDB(permitToUse.getPermit().getPermitCode());
/*     */       
/* 345 */       if (permitToUse.getPermit().getPermitStatus().getKey().equals("105") && null == hashCode) {
/*     */         
/* 347 */         hashCode = generateHashCode(machineType);
/* 348 */         hashCodeMap.put("REPORT_CD", "093");
/* 349 */         hashCodeMap.put("PRINT_COUNTER", "1");
/* 350 */         hashCodeMap.put("PRINT_AS_ORIG_CTR", "1");
/* 351 */         hashCodeMap.put("REPORT_HASH_CD", hashCode);
/* 352 */         hashCodeMap.put("IS_REPRINT", "N");
/* 353 */         hashCodeMap.put("TRANS_TYPE", "040");
/* 354 */         this.permitDao.insertPermitHashCodeToDB(hashCodeMap);
/*     */         
/* 356 */         updatePermitStatus(permitToUse.getPermit().getPermitCode(), "106", auditTrail);
/*     */         
/* 358 */         this.registrationDao.updateSLSMachineStatus(paramMap);
/*     */         
/* 360 */         reprinted = Boolean.valueOf(false);
/*     */       }
/*     */       else {
/*     */         
/* 364 */         hashCodeMap.put("IS_REPRINT", "Y");
/* 365 */         hashCodeMap.put("TRANS_TYPE", "041");
/* 366 */         this.permitDao.updatePermitHashCodeInDB(hashCodeMap);
/* 367 */         hashCode = this.permitDao.getPermitHashCodeFromDB(permitToUse.getPermit().getPermitCode());
/* 368 */         reprinted = Boolean.valueOf(true);
/*     */       } 
/*     */     } 
/*     */     
/* 372 */     permitMap.put("OFFICE", permitToUse.getBranch().getBranchCode().equals("000") ? "HEAD OFFICE" : "BRANCH");
/* 373 */     permitMap.put("BAR_CODE", barcodeImage);
/* 374 */     permitMap.put("HASH_CODE", hashCode);
/* 375 */     permitMap.put("REPRINTED", reprinted);
/* 376 */     permitMap.put("PRINT_STATUS", reprinted.booleanValue() ? "Reprint" : "Original Copy");
/* 377 */     permitMap.put("SIGNATORY_POS", permitToUse.getSignatory_pos());
/*     */     
/* 379 */     String rrkey = permitToUse.getRdo_code();
/* 380 */     if (rrkey.equals("116") || rrkey.equals("121") || rrkey.equals("122") || rrkey.equals("123") || rrkey.equals("124") || rrkey.equals("125") || rrkey.equals("126")) {
/*     */       
/* 382 */       permitMap.put("RR_CODE", "LARGE TAXPAYERS SERVICE ");
/*     */     }
/*     */     else {
/*     */       
/* 386 */       permitMap.put("RR_CODE", permitToUse.getRr_code() + "-" + permitToUse.getRr_desc());
/*     */     } 
/*     */     
/* 389 */     log.debug("permitToUse.getSignatory_pos(): " + permitToUse.getSignatory_pos());
/*     */     
/* 391 */     this.permitDao.insertPermitHashCodeToHistory(hashCodeMap);
/*     */     
/* 393 */     if (machineType.equals("SPM_111"))
/*     */     {
/* 395 */       return this.permitSPMGenerator.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */     }
/* 397 */     if (machineType.equals("CRM_050")) {
/*     */       
/* 399 */       if (permitToUse.getPermit().getPermitType().getKey().equals("109"))
/*     */       {
/* 401 */         return this.permitFinalCRMGenerator.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */       }
/* 403 */       if (permitToUse.getPermit().getPermitType().getKey().equals("110"))
/*     */       {
/* 405 */         return this.permitProvisionalCRMGenerator.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */       }
/*     */     }
/* 408 */     else if (machineType.equals("OSM_053")) {
/*     */       
/* 410 */       if (permitToUse.getPermit().getPermitType().getKey().equals("109"))
/*     */       {
/* 412 */         return this.permitFinalOSMGenerator.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */       }
/* 414 */       if (permitToUse.getPermit().getPermitType().getKey().equals("110"))
/*     */       {
/* 416 */         return this.permitProvisionalOSMGenerator.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */       }
/*     */     }
/* 419 */     else if (machineType.equals("POS_054")) {
/*     */       
/* 421 */       log.info("terminal machine ID : " + permitToUse.getTerminal().getMachineId());
/*     */       
/* 423 */       if (permitToUse.getPermit().getPermitType().getKey().equals("109"))
/*     */       {
/* 425 */         return this.permitFinalPOSStandAlone.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */       }
/* 427 */       if (permitToUse.getPermit().getPermitType().getKey().equals("110"))
/*     */       {
/* 429 */         return this.permitProvisionalPOSStandAlone.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */       }
/*     */     }
/* 432 */     else if (machineType.equals("POS_055_WSC")) {
/*     */       
/* 434 */       if (permitToUse.getPermit().getPermitType().getKey().equals("109"))
/*     */       {
/* 436 */         return this.permitFinalPOSServerCons.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */       }
/* 438 */       if (permitToUse.getPermit().getPermitType().getKey().equals("110"))
/*     */       {
/* 440 */         return this.permitProvisionalPOSServerCons.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */       }
/*     */     }
/* 443 */     else if (machineType.equals("POS_055_WTG")) {
/*     */       
/* 445 */       if (permitToUse.getPermit().getPermitType().getKey().equals("109"))
/*     */       {
/*     */         
/* 448 */         return this.permitFinalPOSGlobal.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */       }
/*     */       
/* 451 */       if (permitToUse.getPermit().getPermitType().getKey().equals("110"))
/*     */       {
/*     */         
/* 454 */         return this.permitProvisionalPOSGlobal.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */       
/*     */       }
/*     */     
/*     */     }
/* 459 */     else if (machineType.equals("POS_055_WTD")) {
/*     */       
/* 461 */       if (permitToUse.getPermit().getPermitType().getKey().equals("109"))
/*     */       {
/* 463 */         return this.permitFinalPOSDecentralized.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */       }
/*     */ 
/*     */       
/* 467 */       if (permitToUse.getPermit().getPermitType().getKey().equals("110"))
/*     */       {
/* 469 */         return this.permitProvisionalPOSDecentralized.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */       }
/*     */     } 
/*     */ 
/*     */     
/* 474 */     return null;
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public ReportContainer printPermitToUseOfGlobal(String machineType, PermitToUse permitToUse, String barCode, String printAsOriginal, AuditTrail auditTrail, String machineMin) {
/* 480 */     BarcodePDF417 barPdf = new BarcodePDF417();
/* 481 */     barPdf.setText(barCode);
/* 482 */     barPdf.setYHeight(2.0F);
/* 483 */     Image barcodeImage = barPdf.createAwtImage(Color.BLACK, Color.WHITE);
/*     */     
/* 485 */     Map<String, Object> permitMap = new HashMap<String, Object>();
/*     */     
/* 487 */     String hashCode = "";
/* 488 */     Boolean reprinted = Boolean.valueOf(false);
/*     */     
/* 490 */     List<PermitToUse> permitToUseList = new ArrayList<PermitToUse>();
/* 491 */     permitToUseList.add(permitToUse);
/*     */     
/* 493 */     Map<String, Object> hashCodeMap = new HashMap<String, Object>();
/* 494 */     hashCodeMap.put("AUDIT_TRAIL", auditTrail);
/* 495 */     hashCodeMap.put("REPORT_NO", permitToUse.getPermit().getPermitCode());
/*     */     
/* 497 */     ParameterizedObject terminalStatus = this.registrationDao.getStatusOfGlobalTerminal(machineMin);
/* 498 */     updatePermitStatus(permitToUse.getPermit().getPermitCode(), "106", auditTrail);
/* 499 */     if (!auditTrail.getUserSecurity().getProfile().getProfileCode().equals("PRF0000008") || !auditTrail.getUserSecurity().getProfile().getProfileCode().equals("PRF0000013")) {
/*     */ 
/*     */       
/* 502 */       if (printAsOriginal.equals("true"))
/*     */       {
/* 504 */         hashCodeMap.put("IS_REPRINT", "N");
/*     */         
/* 506 */         hashCode = this.permitDao.getPermitHashCodeFromDB(permitToUse.getPermit().getPermitCode());
/*     */         
/* 508 */         if (null == hashCode) {
/*     */           
/* 510 */           hashCode = generateHashCode(machineType);
/*     */           
/* 512 */           hashCodeMap.put("REPORT_CD", "093");
/* 513 */           hashCodeMap.put("PRINT_COUNTER", "1");
/* 514 */           hashCodeMap.put("PRINT_AS_ORIG_CTR", "1");
/* 515 */           hashCodeMap.put("REPORT_HASH_CD", hashCode);
/*     */           
/* 517 */           hashCodeMap.put("TRANS_TYPE", "040");
/* 518 */           this.permitDao.insertPermitHashCodeToDB(hashCodeMap);
/*     */         }
/*     */         else {
/*     */           
/* 522 */           hashCode = generateHashCode(machineType);
/* 523 */           hashCodeMap.put("REPORT_HASH_CD", hashCode);
/* 524 */           hashCodeMap.put("TRANS_TYPE", "041");
/* 525 */           this.permitDao.updatePermitHashCodeInDB(hashCodeMap);
/*     */         } 
/*     */         
/* 528 */         updatePermitStatusGlobal(permitToUse.getPermit().getPermitCode(), "106", auditTrail, machineMin);
/* 529 */         reprinted = Boolean.valueOf(false);
/*     */       
/*     */       }
/*     */       else
/*     */       {
/* 534 */         hashCode = this.permitDao.getPermitHashCodeFromDB(permitToUse.getPermit().getPermitCode());
/* 535 */         hashCodeMap.put("IS_REPRINT", "Y");
/* 536 */         hashCodeMap.put("TRANS_TYPE", "041");
/* 537 */         this.permitDao.updatePermitHashCodeInDB(hashCodeMap);
/* 538 */         reprinted = Boolean.valueOf(true);
/*     */       }
/*     */     
/*     */     }
/*     */     else {
/*     */       
/* 544 */       hashCode = this.permitDao.getPermitHashCodeFromDB(permitToUse.getPermit().getPermitCode());
/*     */       
/* 546 */       if (terminalStatus.getKey().equals("105") && null == hashCode) {
/*     */         
/* 548 */         hashCode = generateHashCode(machineType);
/* 549 */         hashCodeMap.put("REPORT_CD", "093");
/* 550 */         hashCodeMap.put("PRINT_COUNTER", "1");
/* 551 */         hashCodeMap.put("PRINT_AS_ORIG_CTR", "1");
/* 552 */         hashCodeMap.put("REPORT_HASH_CD", hashCode);
/* 553 */         hashCodeMap.put("IS_REPRINT", "N");
/* 554 */         hashCodeMap.put("TRANS_TYPE", "040");
/* 555 */         this.permitDao.insertPermitHashCodeToDB(hashCodeMap);
/*     */         
/* 557 */         updatePermitStatusGlobal(permitToUse.getPermit().getPermitCode(), "106", auditTrail, machineMin);
/* 558 */         reprinted = Boolean.valueOf(false);
/*     */       }
/*     */       else {
/*     */         
/* 562 */         hashCodeMap.put("IS_REPRINT", "Y");
/* 563 */         hashCodeMap.put("TRANS_TYPE", "041");
/* 564 */         this.permitDao.updatePermitHashCodeInDB(hashCodeMap);
/* 565 */         hashCode = this.permitDao.getPermitHashCodeFromDB(permitToUse.getPermit().getPermitCode());
/* 566 */         reprinted = Boolean.valueOf(true);
/*     */       } 
/*     */     } 
/*     */     
/* 570 */     permitMap.put("OFFICE", permitToUse.getBranch().getBranchCode().equals("000") ? "HEAD OFFICE" : "BRANCH");
/* 571 */     permitMap.put("BAR_CODE", barcodeImage);
/* 572 */     permitMap.put("HASH_CODE", hashCode);
/* 573 */     permitMap.put("REPRINTED", reprinted);
/* 574 */     permitMap.put("PRINT_STATUS", reprinted.booleanValue() ? "Reprint" : "Original Copy");
/* 575 */     permitMap.put("SIGNATORY_POS", permitToUse.getSignatory_pos());
/* 576 */     String rrkey = permitToUse.getRdo_code();
/* 577 */     if (rrkey.equals("116") || rrkey.equals("121") || rrkey.equals("122") || rrkey.equals("123") || rrkey.equals("124") || rrkey.equals("125") || rrkey.equals("126")) {
/*     */       
/* 579 */       permitMap.put("RR_CODE", "LARGE TAXPAYERS SERVICE ");
/*     */     }
/*     */     else {
/*     */       
/* 583 */       permitMap.put("RR_CODE", permitToUse.getRr_code() + "-" + permitToUse.getRr_desc());
/*     */     } 
/*     */     
/* 586 */     this.permitDao.insertPermitHashCodeToHistory(hashCodeMap);
/*     */     
/* 588 */     if (permitToUse.getPermit().getPermitType().getKey().equals("109"))
/*     */     {
/*     */       
/* 591 */       return this.permitFinalPOSGlobal.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */     }
/*     */     
/* 594 */     if (permitToUse.getPermit().getPermitType().getKey().equals("110"))
/*     */     {
/*     */       
/* 597 */       return this.permitProvisionalPOSGlobal.generateReport(permitMap, (JRDataSource)new JRBeanCollectionDataSource(permitToUseList));
/*     */     }
/*     */ 
/*     */ 
/*     */     
/* 602 */     return null;
/*     */   }
/*     */ 
/*     */   
/*     */   public void updatePermitStatusGlobal(String permitCode, String permitStatus, AuditTrail auditTrail, String machineMin) {
/* 607 */     Map<String, Object> updateMap = new HashMap<String, Object>();
/* 608 */     updateMap.put("AUDIT_TRAIL", auditTrail);
/* 609 */     updateMap.put("PERMIT_STATUS", "106");
/* 610 */     updateMap.put("PERMIT_CODE", permitCode);
/* 611 */     this.permitDao.updatePermitStatus(updateMap);
/* 612 */     updateMap.put("TRANS_TYPE", "041");
/* 613 */     this.permitDao.insertPermitHist(updateMap);
/* 614 */     this.permitDao.updateMachinePermitStatus(updateMap);
/*     */ 
/*     */     
/* 617 */     RegistrationViewForm regFormContainer = this.registrationDao.getPermitDetailsByPermitCode(permitCode);
/*     */     
/* 619 */     Map<String, Object> regMap = new HashMap<String, Object>();
/* 620 */     regMap.put("REGFORM", regFormContainer);
/* 621 */     regMap.put("AUDIT_TRAIL", auditTrail);
/* 622 */     this.registrationDao.updateSLSMachineStatus(regMap);
/*     */     
/* 624 */     String machineType = regFormContainer.getMachineGroup().getMachineType().getKey();
/* 625 */     String global = "";
/*     */     
/* 627 */     if (machineType.equals("052")) {
/*     */       
/* 629 */       String posType = regFormContainer.getMachineGroup().getPosType().getKey();
/* 630 */       if (posType.equals("055") && regFormContainer.getMachineGroup().isGlobal()) {
/*     */ 
/*     */         
/* 633 */         global = "117";
/* 634 */         Map<String, Object> paramMap = new HashMap<String, Object>();
/* 635 */         paramMap.put("REGFORM", regFormContainer);
/* 636 */         paramMap.put("AUDIT_TRAIL", auditTrail);
/* 637 */         paramMap.put("MACHINE_MIN", machineMin);
/* 638 */         this.registrationDao.updateSLSMachineStatusGlobal(paramMap);
/*     */       } 
/*     */     } 
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
/*     */ 
/*     */ 
/*     */ 
/*     */ 
/*     */   
/*     */   public void updatePermitStatus(String permitCode, String permitStatus, AuditTrail auditTrail) {
/* 658 */     Map<String, Object> updateMap = new HashMap<String, Object>();
/* 659 */     updateMap.put("AUDIT_TRAIL", auditTrail);
/* 660 */     updateMap.put("PERMIT_STATUS", "106");
/* 661 */     updateMap.put("PERMIT_CODE", permitCode);
/* 662 */     this.permitDao.updatePermitStatus(updateMap);
/*     */     
/* 664 */     updateMap.put("TRANS_TYPE", "041");
/* 665 */     this.permitDao.insertPermitHist(updateMap);
/* 666 */     this.permitDao.updateMachinePermitStatus(updateMap);
/*     */   }
/*     */ 
/*     */ 
/*     */   
/*     */   public String generateHashCode(String machineType) {
/* 672 */     SimpleDateFormat sdfDate = new SimpleDateFormat("ddMMyyyy");
/* 673 */     Date date = new Date();
/* 674 */     String strDate = sdfDate.format(date);
/*     */     
/* 676 */     Map<String, Object> transTemp = this.sequenceDao.getTransactionNo("RHC");
/*     */     
/* 678 */     int trans_number = Integer.parseInt(transTemp.get("SEQUENCE_NUMBER").toString().trim());
/* 679 */     trans_number = (trans_number + 1 > 99999) ? 1 : (trans_number + 1);
/*     */     
/* 681 */     this.sequenceDao.updateTransactionNo(String.valueOf(trans_number), "RHC");
/*     */     
/* 683 */     return machineType.substring(0, 3) + strDate + sequenceNumberFormatter(trans_number);
/*     */   }
/*     */ 
/*     */   
/*     */   private String sequenceNumberFormatter(int sequence_number) {
/* 688 */     String cont = String.valueOf(sequence_number);
/* 689 */     String padValue = "0";
/* 690 */     StringBuilder resultContainer = new StringBuilder("");
/*     */     
/* 692 */     for (int x = cont.length(); x < 5; x++)
/*     */     {
/* 694 */       resultContainer.append(padValue);
/*     */     }
/*     */     
/* 697 */     resultContainer.append(cont);
/*     */     
/* 699 */     return resultContainer.toString();
/*     */   }
/*     */ 
/*     */   
/*     */   public PermitToUse getPermitToUseDetailsByPermitCodeAndMIN(String permitCode, String machineMIN) {
/* 704 */     return this.permitDao.getPermitToUseDetailsByPermitCodeAndMIN(permitCode, machineMIN);
/*     */   }
/*     */ 
/*     */   
/*     */   public void setPermitFinalCRMGenerator(ReportGenerator permitFinalCRMGenerator) {
/* 709 */     this.permitFinalCRMGenerator = permitFinalCRMGenerator;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setPermitProvisionalCRMGenerator(ReportGenerator permitProvisionalCRMGenerator) {
/* 714 */     this.permitProvisionalCRMGenerator = permitProvisionalCRMGenerator;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setPermitFinalOSMGenerator(ReportGenerator permitFinalOSMGenerator) {
/* 719 */     this.permitFinalOSMGenerator = permitFinalOSMGenerator;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setPermitProvisionalOSMGenerator(ReportGenerator permitProvisionalOSMGenerator) {
/* 724 */     this.permitProvisionalOSMGenerator = permitProvisionalOSMGenerator;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setPermitSPMGenerator(ReportGenerator permitSPMGenerator) {
/* 729 */     this.permitSPMGenerator = permitSPMGenerator;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setPermitFinalPOSDecentralized(ReportGenerator permitFinalPOSDecentralized) {
/* 734 */     this.permitFinalPOSDecentralized = permitFinalPOSDecentralized;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setPermitFinalPOSGlobal(ReportGenerator permitFinalPOSGlobal) {
/* 739 */     this.permitFinalPOSGlobal = permitFinalPOSGlobal;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setPermitFinalPOSServerCons(ReportGenerator permitFinalPOSServerCons) {
/* 744 */     this.permitFinalPOSServerCons = permitFinalPOSServerCons;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setPermitFinalPOSStandAlone(ReportGenerator permitFinalPOSStandAlone) {
/* 749 */     this.permitFinalPOSStandAlone = permitFinalPOSStandAlone;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setPermitProvisionalPOSDecentralized(ReportGenerator permitProvisionalPOSDecentralized) {
/* 754 */     this.permitProvisionalPOSDecentralized = permitProvisionalPOSDecentralized;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setPermitProvisionalPOSGlobal(ReportGenerator permitProvisionalPOSGlobal) {
/* 759 */     this.permitProvisionalPOSGlobal = permitProvisionalPOSGlobal;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setPermitProvisionalPOSServerCons(ReportGenerator permitProvisionalPOSServerCons) {
/* 764 */     this.permitProvisionalPOSServerCons = permitProvisionalPOSServerCons;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setPermitProvisionalPOSStandAlone(ReportGenerator permitProvisionalPOSStandAlone) {
/* 769 */     this.permitProvisionalPOSStandAlone = permitProvisionalPOSStandAlone;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setSequenceDao(SequenceDao sequenceDao) {
/* 774 */     this.sequenceDao = sequenceDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public void setRegistrationDao(RegistrationDao registrationDao) {
/* 779 */     this.registrationDao = registrationDao;
/*     */   }
/*     */ 
/*     */   
/*     */   public ParameterizedObject getStatusOfGlobalTerminal(String machineMin) {
/* 784 */     return this.registrationDao.getStatusOfGlobalTerminal(machineMin);
/*     */   }
/*     */ }


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\main\registration\PermitServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */