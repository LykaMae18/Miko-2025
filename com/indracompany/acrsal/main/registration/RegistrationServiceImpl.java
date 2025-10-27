/*      */ package com.indracompany.acrsal.main.registration;
/*      */ 
/*      */ import com.indracompany.acrsal.api.registration.RegistrationService;
/*      */ import com.indracompany.acrsal.dao.accreditation.AccreditationDao;
/*      */ import com.indracompany.acrsal.dao.business.BusinessDao;
/*      */ import com.indracompany.acrsal.dao.business.SequenceDao;
/*      */ import com.indracompany.acrsal.dao.business.UserDao;
/*      */ import com.indracompany.acrsal.dao.registration.RegistrationDao;
/*      */ import com.indracompany.acrsal.exception.AccredittedSoftwareForProvRegistrationException;
/*      */ import com.indracompany.acrsal.exception.ExistingRegistrationApplicationException;
/*      */ import com.indracompany.acrsal.forms.ConversionInquiryResultForm;
/*      */ import com.indracompany.acrsal.forms.MachineForm;
/*      */ import com.indracompany.acrsal.forms.MachineUploadForm;
/*      */ import com.indracompany.acrsal.forms.PermitHistoryForm;
/*      */ import com.indracompany.acrsal.forms.PermitInquiryForm;
/*      */ import com.indracompany.acrsal.forms.RegistrationPermitForm;
/*      */ import com.indracompany.acrsal.forms.RegistrationViewForm;
/*      */ import com.indracompany.acrsal.forms.SPMValidationForm;
/*      */ import com.indracompany.acrsal.forms.UploadMachineValidationForm;
/*      */ import com.indracompany.acrsal.model.accreditation.Accreditation;
/*      */ import com.indracompany.acrsal.model.accreditation.Hardware;
/*      */ import com.indracompany.acrsal.model.registration.BusinessRegistration;
/*      */ import com.indracompany.acrsal.model.registration.Machine;
/*      */ import com.indracompany.acrsal.model.registration.MachineCRMDetails;
/*      */ import com.indracompany.acrsal.model.registration.MachineGroup;
/*      */ import com.indracompany.acrsal.model.registration.MachineOSMDetails;
/*      */ import com.indracompany.acrsal.model.registration.MachinePOSDetails;
/*      */ import com.indracompany.acrsal.model.registration.MachineResult;
/*      */ import com.indracompany.acrsal.model.registration.MachineSPMDetails;
/*      */ import com.indracompany.acrsal.model.registration.Permit;
/*      */ import com.indracompany.acrsal.models.AuditTrail;
/*      */ import com.indracompany.acrsal.models.Branch;
/*      */ import com.indracompany.acrsal.models.Business;
/*      */ import com.indracompany.acrsal.models.FullName;
/*      */ import com.indracompany.acrsal.models.ParameterizedObject;
/*      */ import com.indracompany.core.mail.mailer.TemplateMailer;
/*      */ import com.indracompany.core.reporting.ReportGenerator;
/*      */ import com.indracompany.core.reporting.model.ReportContainer;
/*      */ import java.text.DateFormat;
/*      */ import java.text.DecimalFormat;
/*      */ import java.text.ParseException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import net.sf.jasperreports.engine.JRDataSource;
/*      */ import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
/*      */ import org.apache.commons.lang.StringUtils;
/*      */ import org.apache.log4j.Logger;
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ public class RegistrationServiceImpl
/*      */   implements RegistrationService
/*      */ {
/*      */   private static final long serialVersionUID = 1L;
/*      */   private static final String DATA_PARAM_SERVER = "SERVER";
/*      */   private static final String DATA_PARAM_MACHINE = "MACHINE";
/*      */   private static final String DATA_PARAM_AUDIT = "AUDIT_TRAIL";
/*      */   private static final String DATA_PARAM_TRANSNO = "TRANS_NUM";
/*      */   private static final String DATA_PARAM_TRANSTYPE = "TRANS_TYPE";
/*      */   private static final String DATA_PARAM_PARENTCD = "PARENT_CD";
/*      */   private static final String DATA_PARAM_CHILDCD = "CHILD_CD";
/*      */   private static final String DATA_PARAM_REG = "REGFORM";
/*      */   private static final String DATA_PARAM_PERMIT = "PERMIT";
/*      */   private static final String DATA_PARAM_ACCRED = "ACCREDITATION";
/*      */   private static final String DATA_PARAM_BUSINESS = "BUSINESS";
/*      */   private static final String DATA_PARAM_BRANCH = "BRANCH";
/*      */   private static final String DATA_PARAM_MACHINECD = "MACHINE_CD";
/*      */   private static final String DATA_PARAM_REMARKS = "REMARKS";
/*      */   private static final String DATA_PARAM_PERMITCD = "PERMIT_CD";
/*      */   private static final String DATA_PARAM_STATUS = "STATUS";
/*      */   private static final String DATA_PARAM_LINKED = "LINKED";
/*      */   private static final String DATA_PARAM_ADD_USER = "IS_ADDITIONAL";
/*      */   private static final String DATA_PARAM_USERNAME = "USER_NAME";
/*      */   private static final String DATA_PARAM_TIN = "BUSINESS_TIN";
/*      */   private static final String DATA_PARAM_BRANCHCODE = "BRANCH_CODE";
/*      */   private static final String DATA_PARAM_LOGINTYPE = "LOGIN_TYPE";
/*   86 */   private final Logger log = Logger.getLogger(RegistrationServiceImpl.class);
/*      */   
/*      */   private BusinessDao businessDao;
/*      */   
/*      */   private RegistrationDao registrationDao;
/*      */   
/*      */   private SequenceDao sequenceDao;
/*      */   
/*      */   private AccreditationDao accreditationDao;
/*      */   private TemplateMailer permitRegistrationMailer;
/*      */   private TemplateMailer approvedPermitRegistrationMailer;
/*      */   private TemplateMailer deniedPermitRegistrationMailer;
/*      */   private TemplateMailer cancellationOfPermitMailer;
/*      */   private TemplateMailer conversionToFinalOfPermitMailer;
/*      */   private TemplateMailer permitForUploadRegistrationMailer;
/*      */   private UserDao userDao;
/*      */   private ReportGenerator registrationCRMExportGenerator;
/*      */   private ReportGenerator registrationOSMExportGenerator;
/*      */   private ReportGenerator registrationSPMExportGenerator;
/*      */   private ReportGenerator registrationPOSStandAloneExportGenerator;
/*      */   private ReportGenerator registrationPOSConnectedExportGenerator;
/*      */   private ReportGenerator registrationLetterOfCancellationGenerator;
/*      */   private ReportGenerator uploadedCRMRegistrationExportGenerator;
/*      */   private ReportGenerator uploadedOSMRegistrationExportGenerator;
/*      */   private ReportGenerator uploadedSPMRegistrationExportGenerator;
/*      */   private ReportGenerator uploadedPOSSARegistrationExportGenerator;
/*      */   private ReportGenerator uploadedPOSWTRegistrationExportGenerator;
/*      */   
/*      */   public Business getITSBusinessInfo(Map<String, Object> paramMap) {
/*  115 */     return this.businessDao.getITSBusinessInfo(paramMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public int checkIfMachineExist(Map<String, Object> permit) {
/*  120 */     return this.registrationDao.checkIfMachineExist(permit);
/*      */   }
/*      */ 
/*      */   
/*      */   public MachineResult getMachine(Map<String, Object> permit) {
/*  125 */     return this.registrationDao.getMachine(permit);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<PermitInquiryForm> getRegistrationRecordList(Map<String, Object> permit) {
/*      */     List<PermitInquiryForm> permitContainerForms;
/*  132 */     String getUserType = (String)permit.get("IS_BIR_USER");
/*      */     
/*  134 */     if (getUserType.equalsIgnoreCase("N")) {
/*      */ 
/*      */       
/*  137 */       permitContainerForms = this.registrationDao.getRegistrationAuthorizedRecordListByTransNo(permit);
/*      */     
/*      */     }
/*      */     else {
/*      */       
/*  142 */       permitContainerForms = this.registrationDao.getRegistrationRecordListByTransNo(permit);
/*      */     } 
/*      */     
/*  145 */     return permitContainerForms;
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
/*      */   public String getRDOCodeByUserNameAndLoginType(Map<String, Object> param) {
/*  175 */     return this.businessDao.getRDOCodeByUserNameAndLoginType(param);
/*      */   }
/*      */ 
/*      */   
/*      */   public RegistrationViewForm getPermitDetailsByPermitCode(String permit_code) {
/*  180 */     return this.registrationDao.getPermitDetailsByPermitCode(permit_code);
/*      */   }
/*      */ 
/*      */   
/*      */   public List<MachineForm> getMachineListByPermitCode(String permit_code) {
/*  185 */     return this.registrationDao.getMachineListByPermitCode(permit_code);
/*      */   }
/*      */ 
/*      */   
/*      */   public List<MachineForm> getMachineListByPermitCodeGlobal(String permit_code) {
/*  190 */     return this.registrationDao.getMachineListByPermitCodeGlobal(permit_code);
/*      */   }
/*      */ 
/*      */   
/*      */   public List<PermitHistoryForm> getPermitHistoryByPermitCode(String permit_code) {
/*  195 */     return this.registrationDao.getPermitHistoryByPermitCode(permit_code);
/*      */   }
/*      */ 
/*      */   
/*      */   public List<String> getMachineRelationShipByMIN(String machine_id) {
/*  200 */     return this.registrationDao.getMachineRelationShipByMIN(machine_id);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String insertRegistrationSPMRecord(Map<String, Object> paramMap, String permitType) {
/*  207 */     BusinessRegistration businessReg = (BusinessRegistration)paramMap.get("BUSINESS");
/*      */     
/*  209 */     this.log.info("businessReg : " + businessReg);
/*      */     
/*  211 */     AuditTrail auditTrail = (AuditTrail)paramMap.get("AUDIT_TRAIL");
/*  212 */     String userName = (String)paramMap.get("USER_NAME");
/*      */     
/*  214 */     List<MachineSPMDetails> macDetails = (List<MachineSPMDetails>)paramMap.get("MACHINE_DETAILS");
/*      */     
/*  216 */     String transNo = generateTransactionNo();
/*      */     
/*  218 */     insertBusinessDetails(businessReg, auditTrail, userName, transNo);
/*      */     
/*  220 */     for (int i = 0; i < macDetails.size(); i++) {
/*      */ 
/*      */       
/*  223 */       Machine machine = new Machine();
/*  224 */       MachineGroup macGroup = new MachineGroup();
/*  225 */       Permit permit = new Permit();
/*      */       
/*  227 */       String permitCode = generatePermitNo(permitPrefix(permitType), businessReg.getBranch().getBranchCode(), businessReg.getBranch().getRdoCode().getKey());
/*  228 */       permit.setPermitCode(permitCode);
/*      */       
/*  230 */       ParameterizedObject pType = new ParameterizedObject();
/*  231 */       pType.setKey(permitType);
/*  232 */       permit.setPermitType(pType);
/*      */       
/*  234 */       ParameterizedObject pStatus = new ParameterizedObject();
/*  235 */       pStatus.setKey("104");
/*  236 */       permit.setPermitStatus(pStatus);
/*      */       
/*  238 */       permit.setApplicationDate(new Date());
/*      */       
/*  240 */       machine.setBrand(((MachineSPMDetails)macDetails.get(i)).getBrand());
/*  241 */       machine.setModel(((MachineSPMDetails)macDetails.get(i)).getModel());
/*  242 */       machine.setSerialNo(((MachineSPMDetails)macDetails.get(i)).getSerialNumber());
/*      */       
/*  244 */       machine.setLastORNo("");
/*  245 */       machine.setLastCashInvoiceNo("");
/*  246 */       machine.setLastChargeInvoiceNo("");
/*  247 */       machine.setLastTransactionNo("");
/*  248 */       machine.setServer(false);
/*  249 */       machine.setServerConsolidator(false);
/*  250 */       machine.setDateOfReading(null);
/*  251 */       machine.setPresentReading(null);
/*  252 */       machine.setMIN(generateMIN());
/*  253 */       machine.setStatus(pStatus);
/*      */       
/*  255 */       boolean isRoving = true;
/*      */       
/*  257 */       if (((MachineSPMDetails)macDetails.get(i)).getRoving().equalsIgnoreCase("No"))
/*      */       {
/*  259 */         isRoving = false;
/*      */       }
/*      */       
/*  262 */       String machineCode = generateMachineCode();
/*  263 */       ParameterizedObject mType = new ParameterizedObject();
/*  264 */       mType.setKey(((MachineSPMDetails)macDetails.get(i)).getMachineType());
/*  265 */       macGroup.setMachineType(mType);
/*  266 */       macGroup.setRoving(isRoving);
/*  267 */       macGroup.setMachineCode(machineCode);
/*  268 */       macGroup.setTransactionNo(transNo);
/*  269 */       macGroup.setDecentralized(false);
/*  270 */       macGroup.setGlobal(false);
/*  271 */       macGroup.setPosType(null);
/*  272 */       macGroup.setOsmType(null);
/*      */       
/*  274 */       Map<String, Object> regMap = new HashMap<String, Object>();
/*  275 */       regMap.put("BUSINESS", businessReg);
/*  276 */       regMap.put("BRANCH", businessReg.getBranch());
/*  277 */       regMap.put("AUDIT_TRAIL", auditTrail);
/*  278 */       regMap.put("LOGIN_TYPE", "002");
/*  279 */       regMap.put("MACHINE", machine);
/*  280 */       regMap.put("MACHINE_GROUP", macGroup);
/*  281 */       regMap.put("MACHINE_CD", machineCode);
/*  282 */       regMap.put("TRANS_NUM", transNo);
/*  283 */       regMap.put("USER_NAME", userName);
/*  284 */       regMap.put("PERMIT", permit);
/*  285 */       regMap.put("TRANS_TYPE", "040");
/*      */       
/*  287 */       this.registrationDao.insertSLSMachines(regMap);
/*  288 */       this.registrationDao.insertSLSMachinesHist(regMap);
/*  289 */       this.registrationDao.insertUserMachineGroup(regMap);
/*  290 */       this.registrationDao.insertMachineGroupRegistration(regMap);
/*  291 */       this.registrationDao.insertRegistrationPermit(regMap);
/*  292 */       this.registrationDao.insertRegistrationPermitHist(regMap);
/*  293 */       this.registrationDao.insertRegistrationMachinePermit(regMap);
/*  294 */       this.registrationDao.insertRegistrationBusinessName(regMap);
/*      */     } 
/*      */     
/*  297 */     String loginType = (String)paramMap.get("LOGIN_TYPE");
/*  298 */     sendEmailRegistration(loginType, transNo, userName, businessReg);
/*  299 */     return transNo;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public String insertRegistrationCRMRecord(Map<String, Object> paramMap, String permitType) {
/*  305 */     BusinessRegistration businessReg = (BusinessRegistration)paramMap.get("BUSINESS_REG");
/*  306 */     AuditTrail auditTrail = (AuditTrail)paramMap.get("AUDIT_TRAIL");
/*  307 */     String userName = (String)paramMap.get("USER_NAME");
/*  308 */     Accreditation accred = (Accreditation)paramMap.get("ACCREDITATION");
/*      */ 
/*      */     
/*  311 */     List<MachineCRMDetails> macDetails = (List<MachineCRMDetails>)paramMap.get("MACHINE_DETAILS");
/*      */     
/*  313 */     String transNo = generateTransactionNo();
/*      */     
/*  315 */     insertBusinessDetails(businessReg, auditTrail, userName, transNo);
/*      */     
/*  317 */     for (int i = 0; i < macDetails.size(); i++) {
/*      */ 
/*      */       
/*  320 */       Machine machine = new Machine();
/*  321 */       MachineGroup macGroup = new MachineGroup();
/*  322 */       Permit permit = new Permit();
/*      */       
/*  324 */       String permitCode = generatePermitNo(permitPrefix(permitType), businessReg.getBranch().getBranchCode(), businessReg.getBranch().getRdoCode().getKey());
/*  325 */       permit.setPermitCode(permitCode);
/*      */       
/*  327 */       ParameterizedObject pType = new ParameterizedObject();
/*  328 */       pType.setKey(permitType);
/*  329 */       permit.setPermitType(pType);
/*      */       
/*  331 */       ParameterizedObject pStatus = new ParameterizedObject();
/*  332 */       pStatus.setKey("104");
/*  333 */       permit.setPermitStatus(pStatus);
/*      */       
/*  335 */       permit.setApplicationDate(new Date());
/*      */       
/*  337 */       machine.setBrand(((MachineCRMDetails)macDetails.get(i)).getBrand());
/*  338 */       machine.setModel(((MachineCRMDetails)macDetails.get(i)).getModel());
/*  339 */       machine.setSerialNo(((MachineCRMDetails)macDetails.get(i)).getSerialNumber());
/*  340 */       machine.setLastORNo(((MachineCRMDetails)macDetails.get(i)).getLastOR());
/*  341 */       machine.setLastCashInvoiceNo(((MachineCRMDetails)macDetails.get(i)).getLastCashInvoice());
/*  342 */       machine.setLastChargeInvoiceNo(((MachineCRMDetails)macDetails.get(i)).getLastChargeInvoice());
/*  343 */       machine.setLastTransactionNo(((MachineCRMDetails)macDetails.get(i)).getLastTransaction());
/*  344 */       machine.setStatus(pStatus);
/*      */       
/*  346 */       ParameterizedObject mType = new ParameterizedObject();
/*  347 */       mType.setKey("050");
/*  348 */       macGroup.setMachineType(mType);
/*      */       
/*  350 */       machine.setServer(false);
/*  351 */       machine.setServerConsolidator(false);
/*      */       
/*  353 */       DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
/*      */ 
/*      */       
/*      */       try {
/*  357 */         Date dateOfReadingFormat = df.parse(((MachineCRMDetails)macDetails.get(i)).getDateOfReading());
/*  358 */         machine.setDateOfReading(dateOfReadingFormat);
/*      */       }
/*  360 */       catch (ParseException e) {
/*      */         
/*  362 */         this.log.info(e);
/*      */       } 
/*      */       
/*  365 */       machine.setPresentReading(((MachineCRMDetails)macDetails.get(i)).getPresentReading());
/*      */       
/*  367 */       machine.setMIN(generateMIN());
/*      */       
/*  369 */       boolean isRoving = true;
/*      */       
/*  371 */       if (((MachineCRMDetails)macDetails.get(i)).getRoving().equalsIgnoreCase("No"))
/*      */       {
/*  373 */         isRoving = false;
/*      */       }
/*      */       
/*  376 */       String machineCode = generateMachineCode();
/*      */       
/*  378 */       macGroup.setRoving(isRoving);
/*  379 */       macGroup.setMachineCode(machineCode);
/*  380 */       macGroup.setTransactionNo(transNo);
/*      */       
/*  382 */       if (permitType.equalsIgnoreCase("109"))
/*      */       {
/*  384 */         accred.setAccreditationNo(((MachineCRMDetails)macDetails.get(i)).getAccredNo());
/*      */       }
/*      */       
/*  387 */       Map<String, Object> regMap = new HashMap<String, Object>();
/*  388 */       regMap.put("BUSINESS", businessReg);
/*  389 */       regMap.put("BRANCH", businessReg.getBranch());
/*  390 */       regMap.put("AUDIT_TRAIL", auditTrail);
/*  391 */       regMap.put("LOGIN_TYPE", "002");
/*  392 */       regMap.put("MACHINE", machine);
/*  393 */       regMap.put("MACHINE_GROUP", macGroup);
/*  394 */       regMap.put("MACHINE_CD", machineCode);
/*  395 */       regMap.put("TRANS_NUM", transNo);
/*  396 */       regMap.put("USER_NAME", userName);
/*  397 */       regMap.put("PERMIT", permit);
/*  398 */       regMap.put("TRANS_TYPE", "040");
/*  399 */       regMap.put("ACCREDITATION", accred);
/*      */       
/*  401 */       this.registrationDao.insertSLSMachines(regMap);
/*  402 */       this.registrationDao.insertSLSMachinesHist(regMap);
/*  403 */       this.registrationDao.insertUserMachineGroup(regMap);
/*  404 */       this.registrationDao.insertMachineGroupRegistration(regMap);
/*  405 */       this.registrationDao.insertRegistrationPermit(regMap);
/*  406 */       this.registrationDao.insertRegistrationPermitHist(regMap);
/*  407 */       this.registrationDao.insertRegistrationMachinePermit(regMap);
/*      */ 
/*      */       
/*  410 */       this.registrationDao.insertRegistrationBusinessName(regMap);
/*      */     } 
/*      */ 
/*      */     
/*  414 */     String loginType = (String)paramMap.get("LOGIN_TYPE");
/*  415 */     sendEmailRegistration(loginType, transNo, userName, businessReg);
/*  416 */     return transNo;
/*      */   }
/*      */ 
/*      */   
/*      */   public String insertRegistrationOSMRecord(Map<String, Object> paramMap, String permitType) {
/*  421 */     BusinessRegistration businessReg = (BusinessRegistration)paramMap.get("BUSINESS_REG");
/*  422 */     AuditTrail auditTrail = (AuditTrail)paramMap.get("AUDIT_TRAIL");
/*  423 */     String userName = (String)paramMap.get("USER_NAME");
/*  424 */     Accreditation accred = (Accreditation)paramMap.get("ACCREDITATION");
/*      */ 
/*      */     
/*  427 */     List<MachineOSMDetails> macDetails = (List<MachineOSMDetails>)paramMap.get("MACHINE_DETAILS");
/*      */     
/*  429 */     String transNo = generateTransactionNo();
/*      */     
/*  431 */     insertBusinessDetails(businessReg, auditTrail, userName, transNo);
/*      */     
/*  433 */     for (int i = 0; i < macDetails.size(); i++) {
/*      */       
/*  435 */       Machine machine = new Machine();
/*  436 */       MachineGroup macGroup = new MachineGroup();
/*  437 */       Permit permit = new Permit();
/*      */       
/*  439 */       String permitCode = generatePermitNo(permitPrefix(permitType), businessReg.getBranch().getBranchCode(), businessReg.getBranch().getRdoCode().getKey());
/*  440 */       permit.setPermitCode(permitCode);
/*      */       
/*  442 */       ParameterizedObject pType = new ParameterizedObject();
/*  443 */       pType.setKey(permitType);
/*  444 */       permit.setPermitType(pType);
/*      */       
/*  446 */       ParameterizedObject pStatus = new ParameterizedObject();
/*  447 */       pStatus.setKey("104");
/*  448 */       permit.setPermitStatus(pStatus);
/*      */       
/*  450 */       permit.setApplicationDate(new Date());
/*  451 */       permit.setSoftwareName(accred.getSoftware().getSoftwareName());
/*  452 */       permit.setSoftwareVersion(accred.getSoftware().getSoftwareVersion());
/*      */       
/*  454 */       machine.setBrand(((MachineOSMDetails)macDetails.get(i)).getBrand());
/*  455 */       machine.setModel(((MachineOSMDetails)macDetails.get(i)).getModel());
/*  456 */       machine.setSerialNo(((MachineOSMDetails)macDetails.get(i)).getSerialNumber());
/*  457 */       machine.setLastORNo(((MachineOSMDetails)macDetails.get(i)).getLastOR());
/*  458 */       machine.setLastCashInvoiceNo(((MachineOSMDetails)macDetails.get(i)).getLastCashInvoice());
/*  459 */       machine.setLastChargeInvoiceNo(((MachineOSMDetails)macDetails.get(i)).getLastChargeInvoice());
/*  460 */       machine.setLastTransactionNo(((MachineOSMDetails)macDetails.get(i)).getLastTransaction());
/*  461 */       machine.setStatus(pStatus);
/*      */       
/*  463 */       ParameterizedObject mType = new ParameterizedObject();
/*  464 */       mType.setKey("053");
/*  465 */       macGroup.setMachineType(mType);
/*      */       
/*  467 */       ParameterizedObject osmType = new ParameterizedObject();
/*  468 */       osmType.setKey(((MachineOSMDetails)macDetails.get(i)).getOsmType());
/*  469 */       macGroup.setOsmType(osmType);
/*      */       
/*  471 */       machine.setServer(false);
/*  472 */       machine.setServerConsolidator(false);
/*      */       
/*  474 */       DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
/*      */ 
/*      */       
/*      */       try {
/*  478 */         Date dateOfReadingFormat = df.parse(((MachineOSMDetails)macDetails.get(i)).getDateOfReading());
/*  479 */         machine.setDateOfReading(dateOfReadingFormat);
/*      */       }
/*  481 */       catch (ParseException e) {
/*      */         
/*  483 */         this.log.info(e);
/*      */       } 
/*      */       
/*  486 */       machine.setPresentReading(((MachineOSMDetails)macDetails.get(i)).getPresentReading());
/*      */       
/*  488 */       machine.setMIN(generateMIN());
/*      */       
/*  490 */       boolean isRoving = true;
/*      */       
/*  492 */       if (((MachineOSMDetails)macDetails.get(i)).getRoving().equalsIgnoreCase("No"))
/*      */       {
/*  494 */         isRoving = false;
/*      */       }
/*      */       
/*  497 */       String machineCode = generateMachineCode();
/*      */       
/*  499 */       macGroup.setRoving(isRoving);
/*  500 */       macGroup.setMachineCode(machineCode);
/*  501 */       macGroup.setTransactionNo(transNo);
/*      */       
/*  503 */       Map<String, Object> regMap = new HashMap<String, Object>();
/*  504 */       regMap.put("BUSINESS", businessReg);
/*  505 */       regMap.put("BRANCH", businessReg.getBranch());
/*  506 */       regMap.put("AUDIT_TRAIL", auditTrail);
/*  507 */       regMap.put("LOGIN_TYPE", "002");
/*  508 */       regMap.put("MACHINE", machine);
/*  509 */       regMap.put("MACHINE_GROUP", macGroup);
/*  510 */       regMap.put("MACHINE_CD", machineCode);
/*  511 */       regMap.put("TRANS_NUM", transNo);
/*  512 */       regMap.put("USER_NAME", userName);
/*  513 */       regMap.put("PERMIT", permit);
/*  514 */       regMap.put("TRANS_TYPE", "040");
/*  515 */       regMap.put("ACCREDITATION", accred);
/*      */       
/*  517 */       this.registrationDao.insertSLSMachines(regMap);
/*  518 */       this.registrationDao.insertSLSMachinesHist(regMap);
/*  519 */       this.registrationDao.insertUserMachineGroup(regMap);
/*  520 */       this.registrationDao.insertMachineGroupRegistration(regMap);
/*  521 */       this.registrationDao.insertRegistrationPermit(regMap);
/*  522 */       this.registrationDao.insertRegistrationPermitHist(regMap);
/*  523 */       this.registrationDao.insertRegistrationMachinePermit(regMap);
/*  524 */       this.registrationDao.insertRegistrationBusinessName(regMap);
/*      */     } 
/*  526 */     String loginType = (String)paramMap.get("LOGIN_TYPE");
/*  527 */     sendEmailRegistration(loginType, transNo, userName, businessReg);
/*  528 */     return transNo;
/*      */   }
/*      */ 
/*      */   
/*      */   public String insertRegistrationPOSRecord(Map<String, Object> paramMap, String permitType) {
/*  533 */     BusinessRegistration businessReg = (BusinessRegistration)paramMap.get("BUSINESS_REG");
/*  534 */     AuditTrail auditTrail = (AuditTrail)paramMap.get("AUDIT_TRAIL");
/*  535 */     String userName = (String)paramMap.get("USER_NAME");
/*  536 */     Accreditation accred = (Accreditation)paramMap.get("ACCREDITATION");
/*      */ 
/*      */     
/*  539 */     List<MachinePOSDetails> macDetails = (List<MachinePOSDetails>)paramMap.get("MACHINE_DETAILS");
/*  540 */     List<String> macPostDetails = new ArrayList<String>();
/*  541 */     String serverMIN = "";
/*  542 */     String serverConsMIN = "";
/*      */     
/*  544 */     String transNo = generateTransactionNo();
/*  545 */     String softwareName = (String)paramMap.get("SOFTWARE_NAME");
/*  546 */     String softwareVersion = (String)paramMap.get("SOFTWARE_VER");
/*  547 */     String posType = (String)paramMap.get("POS_TYPE");
/*  548 */     String withServerWithTerminal = (String)paramMap.get("WSERVER_WTERMINAL");
/*  549 */     Boolean withServerCons = Boolean.valueOf(withServerWithTerminal.equalsIgnoreCase("withServerConsolidator"));
/*  550 */     String globalDecentralized = (String)paramMap.get("GLOBAL_DECENTRALIZED");
/*  551 */     Boolean isGlobal = Boolean.valueOf(globalDecentralized.equalsIgnoreCase("global"));
/*      */     
/*  553 */     insertBusinessDetails(businessReg, auditTrail, userName, transNo);
/*      */     
/*  555 */     String machineCode = "";
/*  556 */     String permitCode = "";
/*  557 */     Permit permit = new Permit();
/*      */     
/*  559 */     ParameterizedObject pStatus = new ParameterizedObject();
/*  560 */     pStatus.setKey("104");
/*      */     
/*  562 */     for (int i = 0; i < macDetails.size(); i++) {
/*      */ 
/*      */       
/*  565 */       Machine machine = new Machine();
/*  566 */       MachineGroup macGroup = new MachineGroup();
/*      */       
/*  568 */       if (i == 0 || posType.equalsIgnoreCase("054") || (posType.equalsIgnoreCase("055") && (withServerCons.booleanValue() || !isGlobal.booleanValue()))) {
/*      */ 
/*      */ 
/*      */ 
/*      */         
/*  573 */         permitCode = generatePermitNo(permitPrefix(permitType), businessReg.getBranch().getBranchCode(), businessReg.getBranch().getRdoCode().getKey());
/*  574 */         permit.setPermitCode(permitCode);
/*      */         
/*  576 */         ParameterizedObject pType = new ParameterizedObject();
/*  577 */         pType.setKey(permitType);
/*  578 */         permit.setPermitType(pType);
/*  579 */         permit.setSoftwareName(posType.equalsIgnoreCase("054") ? ((MachinePOSDetails)macDetails.get(i)).getSoftwareName() : softwareName);
/*  580 */         permit.setSoftwareVersion(posType.equalsIgnoreCase("054") ? ((MachinePOSDetails)macDetails.get(i)).getSoftwareVersion() : softwareVersion);
/*      */         
/*  582 */         permit.setPermitStatus(pStatus);
/*  583 */         permit.setApplicationDate(new Date());
/*      */       } 
/*      */       
/*  586 */       machine.setStatus(pStatus);
/*      */       
/*  588 */       machine.setBrand(((MachinePOSDetails)macDetails.get(i)).getBrand());
/*  589 */       machine.setModel(((MachinePOSDetails)macDetails.get(i)).getModel());
/*  590 */       machine.setSerialNo(((MachinePOSDetails)macDetails.get(i)).getSerialNumber());
/*  591 */       machine.setLastORNo(((MachinePOSDetails)macDetails.get(i)).getLastOR());
/*  592 */       machine.setLastCashInvoiceNo(((MachinePOSDetails)macDetails.get(i)).getLastCashInvoice());
/*  593 */       machine.setLastChargeInvoiceNo(((MachinePOSDetails)macDetails.get(i)).getLastChargeInvoice());
/*  594 */       machine.setLastTransactionNo(((MachinePOSDetails)macDetails.get(i)).getLastTransaction());
/*      */       
/*  596 */       ParameterizedObject mType = new ParameterizedObject();
/*  597 */       mType.setKey("052");
/*  598 */       macGroup.setMachineType(mType);
/*  599 */       ParameterizedObject pPosType = new ParameterizedObject();
/*  600 */       pPosType.setKey(posType);
/*  601 */       macGroup.setPosType(pPosType);
/*  602 */       macGroup.setWithServerCons(true);
/*  603 */       macGroup.setWithTerminal(true);
/*  604 */       macGroup.setGlobal(true);
/*  605 */       macGroup.setDecentralized(true);
/*      */       
/*  607 */       machine.setServer(((MachinePOSDetails)macDetails.get(i)).getRovingType().equalsIgnoreCase("113"));
/*  608 */       machine.setServerConsolidator(((MachinePOSDetails)macDetails.get(i)).getRovingType().equalsIgnoreCase("114"));
/*      */       
/*  610 */       DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
/*      */       
/*      */       try {
/*  613 */         Date dateOfReadingFormat = (((MachinePOSDetails)macDetails.get(i)).getDateOfReading().isEmpty() || ((MachinePOSDetails)macDetails.get(i)).getDateOfReading() == null) ? null : df.parse(((MachinePOSDetails)macDetails.get(i)).getDateOfReading());
/*  614 */         machine.setDateOfReading(dateOfReadingFormat);
/*      */       }
/*  616 */       catch (ParseException e) {
/*      */         
/*  618 */         this.log.info(e);
/*      */       } 
/*      */       
/*  621 */       machine.setPresentReading(((MachinePOSDetails)macDetails.get(i)).getPresentReading());
/*      */       
/*  623 */       machine.setMIN(generateMIN());
/*      */       
/*  625 */       boolean isRoving = true;
/*      */       
/*  627 */       if (((MachinePOSDetails)macDetails.get(i)).getRoving().equalsIgnoreCase("No") || ((MachinePOSDetails)macDetails.get(i)).getRoving().equalsIgnoreCase(""))
/*      */       {
/*  629 */         isRoving = false;
/*      */       }
/*      */       
/*  632 */       macGroup.setRoving(isRoving);
/*      */       
/*  634 */       macGroup.setWithServerCons(withServerCons.booleanValue());
/*  635 */       macGroup.setWithTerminal(!withServerCons.booleanValue());
/*  636 */       macGroup.setGlobal(!withServerCons.booleanValue() ? isGlobal.booleanValue() : false);
/*  637 */       macGroup.setDecentralized(!withServerCons.booleanValue() ? (!isGlobal.booleanValue()) : false);
/*  638 */       macGroup.setTransactionNo(transNo);
/*      */       
/*  640 */       Map<String, Object> regMap = new HashMap<String, Object>();
/*  641 */       regMap.put("BUSINESS", businessReg);
/*  642 */       regMap.put("BRANCH", businessReg.getBranch());
/*  643 */       regMap.put("AUDIT_TRAIL", auditTrail);
/*  644 */       regMap.put("LOGIN_TYPE", "002");
/*  645 */       regMap.put("MACHINE", machine);
/*  646 */       regMap.put("MACHINE_GROUP", macGroup);
/*      */       
/*  648 */       regMap.put("TRANS_NUM", transNo);
/*  649 */       regMap.put("USER_NAME", userName);
/*  650 */       regMap.put("PERMIT", permit);
/*  651 */       regMap.put("TRANS_TYPE", "040");
/*      */ 
/*      */ 
/*      */       
/*  655 */       if (posType.equalsIgnoreCase("054")) {
/*      */ 
/*      */         
/*  658 */         regMap.put("ACCREDITATION", accred);
/*      */         
/*  660 */         machineCode = generateMachineCode();
/*  661 */         macGroup.setMachineCode(machineCode);
/*      */         
/*  663 */         regMap.put("MACHINE_CD", machineCode);
/*  664 */         this.registrationDao.insertSLSMachines(regMap);
/*  665 */         this.registrationDao.insertSLSMachinesHist(regMap);
/*  666 */         this.registrationDao.insertUserMachineGroup(regMap);
/*  667 */         this.registrationDao.insertMachineGroupRegistration(regMap);
/*  668 */         this.registrationDao.insertRegistrationPermit(regMap);
/*  669 */         this.registrationDao.insertRegistrationPermitHist(regMap);
/*  670 */         this.registrationDao.insertRegistrationMachinePermit(regMap);
/*  671 */         this.registrationDao.insertRegistrationBusinessName(regMap);
/*      */       }
/*  673 */       else if (posType.equalsIgnoreCase("055")) {
/*      */ 
/*      */         
/*  676 */         regMap.put("ACCREDITATION", accred);
/*      */         
/*  678 */         if (i == 0) {
/*      */           
/*  680 */           machineCode = generateMachineCode();
/*  681 */           macGroup.setMachineCode(machineCode);
/*      */         } 
/*      */         
/*  684 */         regMap.put("MACHINE_CD", machineCode);
/*  685 */         this.registrationDao.insertSLSMachines(regMap);
/*  686 */         this.registrationDao.insertSLSMachinesHist(regMap);
/*  687 */         if (i == 0) {
/*      */           
/*  689 */           this.registrationDao.insertUserMachineGroup(regMap);
/*  690 */           this.registrationDao.insertMachineGroupRegistration(regMap);
/*      */         } 
/*      */ 
/*      */ 
/*      */         
/*  695 */         String rovingType = ((MachinePOSDetails)macDetails.get(i)).getRovingType();
/*  696 */         macPostDetails.add(rovingType + ":" + machine.getMIN());
/*      */         
/*  698 */         if (rovingType.equalsIgnoreCase("113")) {
/*      */           
/*  700 */           serverMIN = machine.getMIN();
/*      */         }
/*  702 */         else if (rovingType.equalsIgnoreCase("114")) {
/*      */           
/*  704 */           serverConsMIN = machine.getMIN();
/*  705 */           serverMIN = serverConsMIN;
/*      */         } 
/*      */ 
/*      */         
/*  709 */         if (withServerCons.booleanValue() || !isGlobal.booleanValue()) {
/*      */           
/*  711 */           if (rovingType.equalsIgnoreCase("112"))
/*      */           {
/*  713 */             this.registrationDao.insertRegistrationMachinePermit(regMap);
/*  714 */             this.registrationDao.insertRegistrationBusinessName(regMap);
/*  715 */             this.registrationDao.insertRegistrationPermit(regMap);
/*  716 */             this.registrationDao.insertRegistrationPermitHist(regMap);
/*      */           
/*      */           }
/*      */         
/*      */         }
/*  721 */         else if (rovingType.equalsIgnoreCase("113")) {
/*      */           
/*  723 */           this.registrationDao.insertRegistrationPermit(regMap);
/*  724 */           this.registrationDao.insertRegistrationPermitHist(regMap);
/*  725 */           this.registrationDao.insertRegistrationMachinePermit(regMap);
/*  726 */           this.registrationDao.insertRegistrationBusinessName(regMap);
/*      */         } 
/*      */       } 
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/*  733 */     if (posType.equalsIgnoreCase("055")) {
/*      */       
/*  735 */       Map<String, Object> machRelMap = new HashMap<String, Object>();
/*      */       
/*  737 */       machRelMap.put("TRANS_NUM", transNo);
/*  738 */       machRelMap.put("AUDIT_TRAIL", auditTrail);
/*  739 */       for (int j = 0; j < macPostDetails.size(); j++) {
/*      */ 
/*      */         
/*  742 */         if (withServerCons.booleanValue()) {
/*      */           
/*  744 */           if (((String)macPostDetails.get(j)).split(":")[0].equalsIgnoreCase("112"))
/*      */           {
/*      */             
/*  747 */             machRelMap.put("PARENT_CD", serverConsMIN);
/*  748 */             machRelMap.put("CHILD_CD", ((String)macPostDetails.get(j)).split(":")[1]);
/*      */           }
/*  750 */           else if (((String)macPostDetails.get(j)).split(":")[0].equalsIgnoreCase("114"))
/*      */           {
/*  752 */             machRelMap.put("PARENT_CD", serverMIN);
/*  753 */             machRelMap.put("CHILD_CD", ((String)macPostDetails.get(j)).split(":")[1]);
/*      */           }
/*      */         
/*      */         }
/*      */         else {
/*      */           
/*  759 */           machRelMap.put("PARENT_CD", serverMIN);
/*  760 */           machRelMap.put("CHILD_CD", ((String)macPostDetails.get(j)).split(":")[1]);
/*  761 */           machRelMap.put("TRANS_TYPE", "040");
/*      */         } 
/*      */         
/*  764 */         if (!((String)macPostDetails.get(j)).split(":")[0].equalsIgnoreCase("113")) {
/*      */           
/*  766 */           this.registrationDao.insertRegistrationMachineRel(machRelMap);
/*  767 */           this.registrationDao.insertRegistrationMachineRelHist(machRelMap);
/*      */         } 
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/*  773 */     String loginType = "002";
/*  774 */     sendEmailRegistration(loginType, transNo, userName, businessReg);
/*  775 */     return transNo;
/*      */   }
/*      */ 
/*      */   
/*      */   private void insertBusinessDetails(BusinessRegistration businessReg, AuditTrail auditTrail, String userName, String transNo) {
/*  780 */     Map<String, Object> permitMap = new HashMap<String, Object>();
/*  781 */     permitMap.put("BUSINESS", businessReg);
/*  782 */     permitMap.put("BRANCH", businessReg.getBranch());
/*  783 */     permitMap.put("AUDIT_TRAIL", auditTrail);
/*  784 */     permitMap.put("LOGIN_TYPE", "002");
/*  785 */     permitMap.put("USER_NAME", userName);
/*  786 */     permitMap.put("TRANS_NUM", transNo);
/*      */     
/*  788 */     int regBusinessCount = this.registrationDao.checkIfBusinessRegistrationExist(permitMap);
/*  789 */     int regBranchCount = this.registrationDao.checkIfBusinessBranchRegistrationExist(permitMap);
/*      */     
/*  791 */     this.log.debug("count business : " + regBusinessCount);
/*  792 */     this.log.debug("count branch : " + regBranchCount);
/*      */     
/*  794 */     if (regBusinessCount == 0) {
/*      */       
/*  796 */       this.registrationDao.insertBusinessRegistration(permitMap);
/*  797 */       this.log.debug("New Business Registered");
/*      */     } 
/*      */     
/*  800 */     if (regBranchCount == 0) {
/*      */       
/*  802 */       this.registrationDao.insertBusinessBranchRegistration(permitMap);
/*  803 */       this.log.debug("New Branch Registered");
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   private String permitPrefix(String permitType) {
/*  809 */     String prefix = "";
/*      */     
/*  811 */     if (permitType.equalsIgnoreCase("109")) {
/*      */       
/*  813 */       prefix = "FP";
/*      */     }
/*  815 */     else if (permitType.equalsIgnoreCase("110")) {
/*      */       
/*  817 */       prefix = "PR";
/*      */     }
/*  819 */     else if (permitType.equalsIgnoreCase("111")) {
/*      */       
/*  821 */       prefix = "SP";
/*      */     } 
/*      */     
/*  824 */     return prefix;
/*      */   }
/*      */ 
/*      */   
/*      */   private String generateMIN() {
/*  829 */     SimpleDateFormat sdfDate = new SimpleDateFormat("yyMMdd");
/*  830 */     SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");
/*  831 */     Date date = new Date();
/*  832 */     String strDate = sdfDate.format(date);
/*  833 */     String strTime = sdfTime.format(date);
/*      */     
/*  835 */     Map<String, Object> transTemp = this.sequenceDao.getTransactionNo("MIN");
/*      */     
/*  837 */     String prefix = transTemp.get("SEQUENCE_PREFIX").toString().trim();
/*  838 */     int trans_number = Integer.parseInt(transTemp.get("SEQUENCE_NUMBER").toString().trim());
/*      */ 
/*      */     
/*  841 */     trans_number = (trans_number + 1 > 99999) ? 1 : (trans_number + 1);
/*      */     
/*  843 */     this.sequenceDao.updateTransactionNo(String.valueOf(trans_number), "MIN");
/*      */ 
/*      */ 
/*      */ 
/*      */     
/*  848 */     return strDate + strTime + sequenceNumberFormatter(trans_number);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private String generateMachineCode() {
/*  854 */     SimpleDateFormat sdfDate = new SimpleDateFormat("yyMMdd");
/*  855 */     SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");
/*  856 */     Date date = new Date();
/*  857 */     String strDate = sdfDate.format(date);
/*  858 */     String strTime = sdfTime.format(date);
/*      */     
/*  860 */     Map<String, Object> transTemp = this.sequenceDao.getTransactionNo("MGP");
/*      */     
/*  862 */     String prefix = transTemp.get("SEQUENCE_PREFIX").toString().trim();
/*      */     
/*  864 */     int trans_number = Integer.parseInt(transTemp.get("SEQUENCE_NUMBER").toString().trim());
/*      */ 
/*      */     
/*  867 */     trans_number = (trans_number + 1 > 99999) ? 1 : (trans_number + 1);
/*      */     
/*  869 */     this.sequenceDao.updateTransactionNo(String.valueOf(trans_number), "MGP");
/*      */ 
/*      */     
/*  872 */     return prefix + strDate + strTime + sequenceNumberFormatter(trans_number);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String generatePermitNo(String permitTypePrefix, String branchCode, String rdoCode) {
/*  879 */     SimpleDateFormat sdfDate = new SimpleDateFormat("MMyyyy");
/*      */     
/*  881 */     Date date = new Date();
/*  882 */     String strDate = sdfDate.format(date);
/*  883 */     Map<String, Object> transTemp = null;
/*      */     
/*  885 */     if (permitTypePrefix.equalsIgnoreCase("FP")) {
/*      */       
/*  887 */       transTemp = this.sequenceDao.getTransactionNo("FP");
/*      */ 
/*      */     
/*      */     }
/*  891 */     else if (permitTypePrefix.equalsIgnoreCase("PR")) {
/*      */       
/*  893 */       transTemp = this.sequenceDao.getTransactionNo("PR");
/*      */     
/*      */     }
/*  896 */     else if (permitTypePrefix.equalsIgnoreCase("SP")) {
/*      */       
/*  898 */       transTemp = this.sequenceDao.getTransactionNo("SP");
/*      */     } 
/*      */ 
/*      */     
/*  902 */     String prefix = transTemp.get("SEQUENCE_PREFIX").toString().trim();
/*      */     
/*  904 */     int trans_number = Integer.parseInt(transTemp.get("SEQUENCE_NUMBER").toString().trim());
/*      */ 
/*      */     
/*  907 */     trans_number = (trans_number + 1 > 9999999) ? 1 : (trans_number + 1);
/*      */     
/*  909 */     this.sequenceDao.updateTransactionNo(String.valueOf(trans_number), permitTypePrefix);
/*  910 */     return prefix + strDate + "-" + rdoCode + "-" + sequenceNumberFormatterPermit(trans_number) + "-" + branchFormatter(branchCode);
/*      */   }
/*      */ 
/*      */   
/*      */   private String generateTransactionNo() {
/*  915 */     SimpleDateFormat sdfDate = new SimpleDateFormat("yyMMdd");
/*  916 */     SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");
/*  917 */     Date date = new Date();
/*  918 */     String strDate = sdfDate.format(date);
/*  919 */     String strTime = sdfTime.format(date);
/*      */     
/*  921 */     Map<String, Object> transTemp = this.sequenceDao.getTransactionNo("REG");
/*      */     
/*  923 */     String prefix = transTemp.get("SEQUENCE_PREFIX").toString().trim();
/*  924 */     int trans_number = Integer.parseInt(transTemp.get("SEQUENCE_NUMBER").toString().trim());
/*      */ 
/*      */     
/*  927 */     trans_number = (trans_number + 1 > 99999) ? 1 : (trans_number + 1);
/*      */     
/*  929 */     this.sequenceDao.updateTransactionNo(String.valueOf(trans_number), "REG");
/*      */ 
/*      */     
/*  932 */     return prefix + strDate + strTime + sequenceNumberFormatter(trans_number);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private String branchFormatter(String sequence_number) {
/*  938 */     String cont = String.valueOf(sequence_number);
/*  939 */     String padValue = "0";
/*  940 */     StringBuilder resultContainer = new StringBuilder("");
/*      */     
/*  942 */     for (int x = cont.length(); x < 5; x++)
/*      */     {
/*  944 */       resultContainer.append(padValue);
/*      */     }
/*      */     
/*  947 */     resultContainer.append(cont);
/*      */     
/*  949 */     return resultContainer.toString();
/*      */   }
/*      */ 
/*      */   
/*      */   private String sequenceNumberFormatterPermit(int sequence_number) {
/*  954 */     String cont = String.valueOf(sequence_number);
/*  955 */     String padValue = "0";
/*  956 */     StringBuilder resultContainer = new StringBuilder("");
/*      */     
/*  958 */     for (int x = cont.length(); x < 7; x++)
/*      */     {
/*  960 */       resultContainer.append(padValue);
/*      */     }
/*      */     
/*  963 */     resultContainer.append(cont);
/*      */     
/*  965 */     return resultContainer.toString();
/*      */   }
/*      */ 
/*      */   
/*      */   private String sequenceNumberFormatter(int sequence_number) {
/*  970 */     String cont = String.valueOf(sequence_number);
/*  971 */     String padValue = "0";
/*  972 */     StringBuilder resultContainer = new StringBuilder("");
/*      */     
/*  974 */     for (int x = cont.length(); x < 5; x++)
/*      */     {
/*  976 */       resultContainer.append(padValue);
/*      */     }
/*      */     
/*  979 */     resultContainer.append(cont);
/*      */     
/*  981 */     return resultContainer.toString();
/*      */   }
/*      */ 
/*      */   
/*      */   private void sendEmailRegistration(String loginType, String transNo, String userName, BusinessRegistration businessReg) {
/*  986 */     FullName fullName = this.userDao.getCompleteNameOfUser(userName, "002");
/*  987 */     Map<String, Object> mailMap = new HashMap<String, Object>();
/*  988 */     mailMap.put("transNum", transNo);
/*  989 */     mailMap.put("userName", userName);
/*  990 */     mailMap.put("authorizedName", fullName.getLastName());
/*      */     
/*  992 */     String taxpayerName = "";
/*  993 */     if (businessReg.getBusinessType().equalsIgnoreCase("028")) {
/*      */       
/*  995 */       taxpayerName = businessReg.getOwner().getWholeName();
/*      */     }
/*  997 */     else if (businessReg.getBusinessType().equalsIgnoreCase("029")) {
/*      */       
/*  999 */       taxpayerName = businessReg.getBusinessDesc();
/*      */     } 
/* 1001 */     mailMap.put("taxpayerName", taxpayerName);
/* 1002 */     Map<String, Object> emailList = this.userDao.getEmailAddresses(userName, loginType);
/*      */     
/* 1004 */     String emailAddress = (String)emailList.get("EMAIL_ADDRESS");
/* 1005 */     String alternateEmailAddress = (String)emailList.get("EMAIL_ALTERNATE");
/*      */     
/* 1007 */     if (!StringUtils.isEmpty(alternateEmailAddress)) {
/*      */       
/* 1009 */       String[] send = new String[2];
/* 1010 */       send[0] = emailAddress;
/* 1011 */       send[1] = alternateEmailAddress;
/* 1012 */       this.permitRegistrationMailer.send(mailMap, send);
/*      */     }
/*      */     else {
/*      */       
/* 1016 */       String[] send = new String[1];
/* 1017 */       send[0] = emailAddress;
/* 1018 */       this.permitRegistrationMailer.send(mailMap, send);
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   private void sendEmailApprovalStatus(String loginType, String transNo, String userName, String remarks, String status, RegistrationViewForm regForm) {
/* 1024 */     Map<String, Object> mailMap = new HashMap<String, Object>();
/* 1025 */     mailMap.put("transNum", transNo);
/* 1026 */     mailMap.put("userName", userName);
/* 1027 */     mailMap.put("testRemark", remarks);
/*      */     
/* 1029 */     FullName fullName = this.userDao.getCompleteNameOfUser(userName, loginType);
/* 1030 */     mailMap.put("authorizedName", fullName.getLastName());
/*      */     
/* 1032 */     String taxpayerName = "";
/* 1033 */     Business business = regForm.getBusiness();
/* 1034 */     MachineGroup machineGroup = regForm.getMachineGroup();
/*      */     
/* 1036 */     if (business.getBusinessType().equalsIgnoreCase("028")) {
/*      */       
/* 1038 */       taxpayerName = business.getOwner().getWholeName();
/*      */     }
/*      */     else {
/*      */       
/* 1042 */       taxpayerName = business.getBusinessDesc();
/*      */     } 
/* 1044 */     mailMap.put("taxpayerName", taxpayerName);
/* 1045 */     Map<String, Object> emailList = this.userDao.getEmailAddresses(userName, loginType);
/*      */     
/* 1047 */     String emailAddress = (String)emailList.get("EMAIL_ADDRESS");
/* 1048 */     String alternateEmailAddress = (String)emailList.get("EMAIL_ALTERNATE");
/*      */     
/* 1050 */     if (status.equalsIgnoreCase("105")) {
/*      */       
/* 1052 */       if (!StringUtils.isEmpty(alternateEmailAddress))
/*      */       {
/* 1054 */         String[] send = new String[2];
/* 1055 */         send[0] = emailAddress;
/* 1056 */         send[1] = alternateEmailAddress;
/* 1057 */         this.approvedPermitRegistrationMailer.send(mailMap, send);
/*      */       }
/*      */       else
/*      */       {
/* 1061 */         String[] send = new String[1];
/* 1062 */         send[0] = emailAddress;
/* 1063 */         this.approvedPermitRegistrationMailer.send(mailMap, send);
/*      */       }
/*      */     
/* 1066 */     } else if (status.equalsIgnoreCase("107")) {
/*      */       
/* 1068 */       if (!StringUtils.isEmpty(alternateEmailAddress)) {
/*      */         
/* 1070 */         String[] send = new String[2];
/* 1071 */         send[0] = emailAddress;
/* 1072 */         send[1] = alternateEmailAddress;
/* 1073 */         this.deniedPermitRegistrationMailer.send(mailMap, send);
/*      */       }
/*      */       else {
/*      */         
/* 1077 */         String[] send = new String[1];
/* 1078 */         send[0] = emailAddress;
/* 1079 */         this.deniedPermitRegistrationMailer.send(mailMap, send);
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private void sendEmailForUploadRegistration(String loginType, String userName, Map<String, Object> resultMap) {
/* 1087 */     String transList = (String)resultMap.get("TRANS_NO");
/* 1088 */     FullName fullName = this.userDao.getCompleteNameOfUser(userName, loginType);
/*      */     
/* 1090 */     Map<String, Object> mailMap = new HashMap<String, Object>();
/*      */     
/* 1092 */     mailMap.put("mapValue", resultMap);
/* 1093 */     mailMap.put("userName", userName);
/* 1094 */     mailMap.put("authorizedName", fullName.getLastName());
/*      */     
/* 1096 */     String taxpayerName = (String)resultMap.get("BUSINESS");
/* 1097 */     mailMap.put("taxpayerName", taxpayerName);
/* 1098 */     mailMap.put("transNum", transList);
/*      */     
/* 1100 */     Map<String, Object> emailList = this.userDao.getEmailAddresses(userName, loginType);
/*      */     
/* 1102 */     String emailAddress = (String)emailList.get("EMAIL_ADDRESS");
/* 1103 */     String alternateEmailAddress = (String)emailList.get("EMAIL_ALTERNATE");
/*      */     
/* 1105 */     if (!StringUtils.isEmpty(alternateEmailAddress)) {
/*      */       
/* 1107 */       String[] send = new String[2];
/* 1108 */       send[0] = emailAddress;
/* 1109 */       send[1] = alternateEmailAddress;
/* 1110 */       this.permitForUploadRegistrationMailer.send(mailMap, send);
/*      */     }
/*      */     else {
/*      */       
/* 1114 */       String[] send = new String[1];
/* 1115 */       send[0] = emailAddress;
/* 1116 */       this.permitForUploadRegistrationMailer.send(mailMap, send);
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportContainer exportCRMRegistration(Map<String, Object> paramMap, List<MachineCRMDetails> machineDetailsList) {
/* 1122 */     return this.registrationCRMExportGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(machineDetailsList));
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportContainer exportSPMRegistration(Map<String, Object> paramMap, List<MachineSPMDetails> machineDetailsList) {
/* 1127 */     return this.registrationSPMExportGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(machineDetailsList));
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportContainer exportOSMRegistration(Map<String, Object> paramMap, List<MachineOSMDetails> machineDetailsList) {
/* 1132 */     return this.registrationOSMExportGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(machineDetailsList));
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportContainer exportPOSStandAloneRegistration(Map<String, Object> paramMap, List<MachinePOSDetails> machineDetailsList) {
/* 1137 */     return this.registrationPOSStandAloneExportGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(machineDetailsList));
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportContainer exportPOSConnectedRegistration(Map<String, Object> paramMap, List<MachinePOSDetails> machineDetailsList) {
/* 1142 */     return this.registrationPOSConnectedExportGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(machineDetailsList));
/*      */   }
/*      */ 
/*      */   
/*      */   public List<Business> getBusinessByRDO(Map<String, Object> paramMap) {
/* 1147 */     return this.businessDao.getBusinessByRDO(paramMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public int checkIfAccreditationNoExist(Map<String, Object> permit) {
/* 1152 */     return this.registrationDao.checkIfAccreditaionNoExist(permit);
/*      */   }
/*      */ 
/*      */   
/*      */   public int checkIfMachineExistsUpload(Map<String, Object> paramMap) {
/* 1157 */     return this.registrationDao.checkIfMachineExistsUpload(paramMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSequenceDao(SequenceDao sequenceDao) {
/* 1162 */     this.sequenceDao = sequenceDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setBusinessDao(BusinessDao businessDao) {
/* 1167 */     this.businessDao = businessDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setRegistrationDao(RegistrationDao registrationDao) {
/* 1172 */     this.registrationDao = registrationDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public TemplateMailer getPermitRegistrationMailer() {
/* 1177 */     return this.permitRegistrationMailer;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setPermitRegistrationMailer(TemplateMailer permitRegistrationMailer) {
/* 1182 */     this.permitRegistrationMailer = permitRegistrationMailer;
/*      */   }
/*      */ 
/*      */   
/*      */   public UserDao getUserDao() {
/* 1187 */     return this.userDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setUserDao(UserDao userDao) {
/* 1192 */     this.userDao = userDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void insertUserToBusinessRegistration(Map<String, Object> paramMap) {
/* 1197 */     paramMap.put("TRANS_TYPE", "040");
/* 1198 */     this.businessDao.insertUserToBusinessRegistration(paramMap);
/* 1199 */     this.businessDao.insertUserToBusinessRegistrationToHist(paramMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public List<List<String>> getAuthorizedUserOfBusiness(Map<String, Object> regMap) {
/* 1204 */     return this.businessDao.getAuthorizedUserOfBusiness(regMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void updateUserStatusInRegistration(Map<String, Object> param) {
/* 1210 */     String checkStatus = (String)param.get("STATUS");
/*      */     
/* 1212 */     if (checkStatus.equalsIgnoreCase("120"))
/*      */     {
/* 1214 */       param.put("TRANS_TYPE", "040");
/*      */     }
/*      */     
/* 1217 */     if (checkStatus.equalsIgnoreCase("121"))
/*      */     {
/* 1219 */       param.put("TRANS_TYPE", "042");
/*      */     }
/*      */     
/* 1222 */     this.businessDao.updateUserStatusBusiness(param);
/* 1223 */     this.businessDao.insertUserToBusinessRegistrationToHist(param);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public int checkIfUserExist(Map<String, Object> registrationMap) {
/* 1229 */     return this.businessDao.checkIfUserExist(registrationMap);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String checkIfAuthUserExist(Map<String, Object> regMap) {
/* 1236 */     List<Map<String, Object>> testMap = this.registrationDao.checkIfAuthUserExist(regMap);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1242 */     if (testMap.size() > 0) {
/*      */       
/* 1244 */       String bTin = (String)regMap.get("BUSINESS_TIN");
/* 1245 */       String bCode = (String)regMap.get("BRANCH_CD");
/* 1246 */       String login_type = (String)regMap.get("LOGIN_TYPE");
/*      */       
/* 1248 */       String dbBTin = (String)((Map)testMap.get(0)).get("BUSINESS_TIN");
/* 1249 */       String dbBCode = (String)((Map)testMap.get(0)).get("BRANCH_CD");
/* 1250 */       String dbLoginType = (String)((Map)testMap.get(0)).get("LOGIN_TYPE");
/*      */       
/* 1252 */       if (bTin.equals(dbBTin) && bCode.equals(dbBCode) && login_type.equals(dbLoginType))
/*      */       {
/*      */         
/* 1255 */         return "false";
/*      */       }
/*      */ 
/*      */       
/* 1259 */       int count = this.registrationDao.checkIfUserAdded(regMap);
/* 1260 */       if (count > 0)
/*      */       {
/* 1262 */         return "false";
/*      */       }
/*      */ 
/*      */ 
/*      */       
/* 1267 */       return "true";
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1274 */     return "false";
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void setRegistrationCRMExportGenerator(ReportGenerator registrationCRMExportGenerator) {
/* 1281 */     this.registrationCRMExportGenerator = registrationCRMExportGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setRegistrationOSMExportGenerator(ReportGenerator registrationOSMExportGenerator) {
/* 1286 */     this.registrationOSMExportGenerator = registrationOSMExportGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setRegistrationSPMExportGenerator(ReportGenerator registrationSPMExportGenerator) {
/* 1291 */     this.registrationSPMExportGenerator = registrationSPMExportGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setRegistrationPOSStandAloneExportGenerator(ReportGenerator registrationPOSStandAloneExportGenerator) {
/* 1296 */     this.registrationPOSStandAloneExportGenerator = registrationPOSStandAloneExportGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setRegistrationPOSConnectedExportGenerator(ReportGenerator registrationPOSConnectedExportGenerator) {
/* 1301 */     this.registrationPOSConnectedExportGenerator = registrationPOSConnectedExportGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public int checkIfRegistrationExist(Map<String, Object> paramMap) {
/* 1306 */     return this.registrationDao.checkIfRegistrationExist(paramMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public void updateRegistrationStatusPermit(Map<String, Object> regMap) {
/* 1311 */     RegistrationViewForm regFormContainer = (RegistrationViewForm)regMap.get("REGFORM");
/* 1312 */     String remarks = (String)regMap.get("REMARKS");
/* 1313 */     Permit permit = regFormContainer.getPermit();
/* 1314 */     String perKey = permit.getPermitStatus().getKey();
/* 1315 */     String userName = regFormContainer.getUserName();
/* 1316 */     String transString = regFormContainer.getTransNum();
/*      */     
/* 1318 */     regMap.put("TRANS_TYPE", "041");
/*      */     
/* 1320 */     this.registrationDao.insertRegistrationPermitHistStatus(regMap);
/* 1321 */     this.registrationDao.updateAccMachinePermitStatus(regMap);
/*      */     
/* 1323 */     this.registrationDao.updateSLSMachineStatus(regMap);
/*      */     
/* 1325 */     this.registrationDao.updateAccPermitStatus(regMap);
/*      */     
/* 1327 */     String permitCode = (String)regMap.get("PERMIT_CD");
/* 1328 */     String effDate = (String)regMap.get("EFFECTIVITY_DATE");
/*      */     
/* 1330 */     String machineType = regFormContainer.getMachineGroup().getMachineType().getKey();
/* 1331 */     String global = "";
/* 1332 */     if (machineType.equalsIgnoreCase("052")) {
/*      */       
/* 1334 */       String posType = regFormContainer.getMachineGroup().getPosType().getKey();
/* 1335 */       if (posType.equalsIgnoreCase("055") && regFormContainer.getMachineGroup().isGlobal()) {
/*      */ 
/*      */         
/* 1338 */         global = "117";
/*      */ 
/*      */         
/* 1341 */         List<MachineForm> machineList = this.registrationDao.getMachineListByPermitCode(permitCode);
/* 1342 */         AuditTrail auditTrail = (AuditTrail)regMap.get("AUDIT_TRAIL");
/* 1343 */         for (MachineForm machineForm : machineList) {
/*      */           
/* 1345 */           Map<String, Object> paramMap = new HashMap<String, Object>();
/* 1346 */           paramMap.put("REGFORM", regFormContainer);
/* 1347 */           paramMap.put("AUDIT_TRAIL", auditTrail);
/* 1348 */           paramMap.put("MACHINE_MIN", machineForm.getMachineId());
/* 1349 */           this.registrationDao.updateSLSMachineStatusGlobal(paramMap);
/*      */         } 
/*      */       } 
/*      */     } 
/*      */     
/* 1354 */     if (perKey.equalsIgnoreCase("105")) {
/*      */       
/* 1356 */       AuditTrail auditTrail = (AuditTrail)regMap.get("AUDIT_TRAIL");
/* 1357 */       this.registrationDao.updateEffDateOfMachine(permitCode, effDate, auditTrail, global);
/*      */     } 
/*      */ 
/*      */     
/* 1361 */     sendEmailApprovalStatus("002", transString, userName, remarks, perKey, regFormContainer);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void setDeniedPermitRegistrationMailer(TemplateMailer deniedPermitRegistrationMailer) {
/* 1367 */     this.deniedPermitRegistrationMailer = deniedPermitRegistrationMailer;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setApprovedPermitRegistrationMailer(TemplateMailer approvedPermitRegistrationMailer) {
/* 1372 */     this.approvedPermitRegistrationMailer = approvedPermitRegistrationMailer;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setCancellationOfPermitMailer(TemplateMailer cancellationOfPermitMailer) {
/* 1377 */     this.cancellationOfPermitMailer = cancellationOfPermitMailer;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private void sendEmailForCancellationOfPermit(String permitNo) {
/* 1383 */     String date = "";
/* 1384 */     String reason = "";
/* 1385 */     String remarks = "";
/* 1386 */     List<Map<String, Object>> emailList = this.registrationDao.getEmailListForCancellation(permitNo);
/* 1387 */     for (Map<String, Object> emailMap : emailList) {
/*      */ 
/*      */       
/* 1390 */       FullName fullName = this.userDao.getCompleteNameOfUser(emailMap.get("USER_NAME").toString(), "002");
/* 1391 */       RegistrationViewForm regForm = this.registrationDao.getPermitDetailsByPermitCode(permitNo);
/* 1392 */       Machine machine = this.registrationDao.getConvertedMachineDetails(regForm.getMin());
/*      */       
/* 1394 */       Map<String, Object> mailMap = new HashMap<String, Object>();
/* 1395 */       mailMap.put("permitNo", permitNo);
/* 1396 */       mailMap.put("userName", emailMap.get("USER_NAME"));
/* 1397 */       mailMap.put("remarks", remarks);
/* 1398 */       mailMap.put("reason", reason);
/* 1399 */       mailMap.put("effectiveDateOfCancellation", date);
/* 1400 */       mailMap.put("authorizedName", fullName.getLastName());
/* 1401 */       mailMap.put("machine", machine);
/* 1402 */       mailMap.put("min", regForm.getMin());
/*      */       
/* 1404 */       String emailAddress = (String)emailMap.get("EMAIL_ADDRESS");
/* 1405 */       String alternateEmailAddress = (String)emailMap.get("EMAIL_ALTERNATE");
/*      */       
/* 1407 */       if (!StringUtils.isEmpty(alternateEmailAddress)) {
/*      */         
/* 1409 */         String[] arrayOfString = new String[2];
/* 1410 */         arrayOfString[0] = emailAddress;
/* 1411 */         arrayOfString[1] = alternateEmailAddress;
/* 1412 */         this.cancellationOfPermitMailer.send(mailMap, arrayOfString);
/*      */         
/*      */         continue;
/*      */       } 
/* 1416 */       String[] send = new String[1];
/* 1417 */       send[0] = emailAddress;
/* 1418 */       this.cancellationOfPermitMailer.send(mailMap, send);
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private void sendEmailConvertPermitToFinal(String permitNo, String finalNo) {
/* 1426 */     List<Map<String, Object>> listMail = this.registrationDao.getEmailListForCancellation(permitNo);
/* 1427 */     DateFormat df = new SimpleDateFormat("MMMM dd yyyy");
/* 1428 */     String conversionDate = df.format(new Date());
/* 1429 */     for (Map<String, Object> emailMap : listMail) {
/*      */       
/* 1431 */       String username = (String)emailMap.get("USER_NAME");
/* 1432 */       FullName fullName = this.userDao.getCompleteNameOfUser(username, "002");
/* 1433 */       RegistrationViewForm regForm = this.registrationDao.getPermitDetailsByPermitCode(finalNo);
/* 1434 */       Business business = regForm.getBusiness();
/*      */       
/* 1436 */       Machine machine = this.registrationDao.getConvertedMachineDetails(regForm.getMin());
/*      */       
/* 1438 */       String taxpayerName = "";
/* 1439 */       if (business.getBusinessType().equalsIgnoreCase("028")) {
/*      */         
/* 1441 */         taxpayerName = business.getOwner().getWholeName();
/*      */       }
/*      */       else {
/*      */         
/* 1445 */         taxpayerName = business.getBusinessDesc();
/*      */       } 
/* 1447 */       Map<String, Object> mailMap = new HashMap<String, Object>();
/* 1448 */       mailMap.put("permitNo", permitNo);
/* 1449 */       mailMap.put("finalNo", finalNo);
/* 1450 */       mailMap.put("userName", username);
/* 1451 */       mailMap.put("conversionDate", conversionDate);
/* 1452 */       mailMap.put("authorizedName", fullName.getLastName());
/* 1453 */       mailMap.put("taxpayerName", taxpayerName);
/* 1454 */       mailMap.put("machine", machine);
/* 1455 */       mailMap.put("min", regForm.getMin());
/*      */       
/* 1457 */       String emailAddress = (String)emailMap.get("EMAIL_ADDRESS");
/* 1458 */       String alternateEmailAddress = (String)emailMap.get("EMAIL_ALTERNATE");
/*      */       
/* 1460 */       if (!StringUtils.isEmpty(alternateEmailAddress)) {
/*      */         
/* 1462 */         String[] arrayOfString = new String[2];
/* 1463 */         arrayOfString[0] = emailAddress;
/* 1464 */         arrayOfString[1] = alternateEmailAddress;
/* 1465 */         this.conversionToFinalOfPermitMailer.send(mailMap, arrayOfString);
/*      */         
/*      */         continue;
/*      */       } 
/* 1469 */       String[] send = new String[1];
/* 1470 */       send[0] = emailAddress;
/* 1471 */       this.conversionToFinalOfPermitMailer.send(mailMap, send);
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void updatePermitToCancel(Map<String, Object> paramMap) {
/* 1480 */     List<RegistrationViewForm> regFormContainer = (List<RegistrationViewForm>)paramMap.get("REGFORM");
/* 1481 */     List<Machine> machineList = (List<Machine>)paramMap.get("MACHINE");
/* 1482 */     List<String> transNumList = (List<String>)paramMap.get("TRANS_NUM");
/* 1483 */     List<String> remarkList = (List<String>)paramMap.get("REMARKS");
/*      */     
/* 1485 */     for (int i = 0; i < regFormContainer.size(); i++) {
/*      */ 
/*      */       
/* 1488 */       RegistrationViewForm regTempForms = regFormContainer.get(i);
/* 1489 */       Machine machine = machineList.get(i);
/* 1490 */       String transNum = transNumList.get(i);
/* 1491 */       String remark = remarkList.get(i);
/*      */       
/* 1493 */       Map<String, Object> regMap = new HashMap<String, Object>();
/* 1494 */       regMap.put("MACHINE", machine);
/* 1495 */       regMap.put("REMARKS", remark);
/* 1496 */       regMap.put("REGFORM", regTempForms);
/* 1497 */       regMap.put("PERMIT_CD", regTempForms.getPermit().getPermitCode());
/* 1498 */       regMap.put("TRANS_NUM", transNum);
/* 1499 */       regMap.put("TRANS_TYPE", "042");
/* 1500 */       regMap.put("AUDIT_TRAIL", paramMap.get("AUDIT_TRAIL"));
/*      */       
/* 1502 */       this.registrationDao.insertRegistrationPermitHistStatus(regMap);
/* 1503 */       this.registrationDao.updateAccMachinePermitStatus(regMap);
/* 1504 */       this.registrationDao.updatePermitToCancel(regMap);
/* 1505 */       this.registrationDao.updateSLSMachinePermitCancel(regMap);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/* 1512 */       sendEmailForCancellationOfPermit(regTempForms.getPermit().getPermitCode());
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void updateProvToFinalRegistration(Map<String, Object> paramMap) {
/* 1519 */     Permit permit = (Permit)paramMap.get("PERMIT");
/* 1520 */     Branch branch = (Branch)paramMap.get("BRANCH");
/*      */     
/* 1522 */     paramMap.put("PROV_PERMIT_CD", permit.getPermitCode());
/* 1523 */     permit.setProvPermitCode(permit.getPermitCode());
/*      */     
/* 1525 */     String newPermitCode = generatePermitNo(permitPrefix("109"), branch.getBranchCode(), branch.getRdoCode().getKey());
/* 1526 */     permit.setPermitCode(newPermitCode);
/*      */     
/* 1528 */     ParameterizedObject pStatus = new ParameterizedObject();
/* 1529 */     pStatus.setKey("105");
/* 1530 */     permit.setPermitStatus(pStatus);
/*      */     
/* 1532 */     ParameterizedObject pTypeStatus = new ParameterizedObject();
/* 1533 */     pTypeStatus.setKey("109");
/* 1534 */     permit.setPermitType(pTypeStatus);
/*      */     
/* 1536 */     paramMap.put("TRANS_TYPE", "040");
/*      */     
/* 1538 */     this.registrationDao.insertConvertedPermit(paramMap);
/* 1539 */     this.registrationDao.insertConvertedPermitHist(paramMap);
/* 1540 */     this.registrationDao.insertRegistrationBusinessName(paramMap);
/* 1541 */     this.registrationDao.updateProvToFinalMachinePermit(paramMap);
/* 1542 */     this.registrationDao.updateConversionMachinePermit(paramMap);
/* 1543 */     this.registrationDao.updateProvConvertedPermit(paramMap);
/*      */     
/* 1545 */     sendEmailConvertPermitToFinal(permit.getProvPermitCode(), newPermitCode);
/*      */     
/* 1547 */     this.log.debug("newPermitCode : " + newPermitCode);
/*      */   }
/*      */ 
/*      */   
/*      */   public TemplateMailer getConversionToFinalOfPermitMailer() {
/* 1552 */     return this.conversionToFinalOfPermitMailer;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setConversionToFinalOfPermitMailer(TemplateMailer conversionToFinalOfPermitMailer) {
/* 1557 */     this.conversionToFinalOfPermitMailer = conversionToFinalOfPermitMailer;
/*      */   }
/*      */ 
/*      */   
/*      */   public List<ConversionInquiryResultForm> getRegistrationRecordListForConversion(Map<String, Object> permit) {
/* 1562 */     return this.registrationDao.getRegistrationRecordListForConversion(permit);
/*      */   }
/*      */ 
/*      */   
/*      */   public void setRegistrationLetterOfCancellationGenerator(ReportGenerator registrationLetterOfCancellationGenerator) {
/* 1567 */     this.registrationLetterOfCancellationGenerator = registrationLetterOfCancellationGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportContainer printLetterOfCancellation(Map<String, Object> paramMap) {
/* 1572 */     Map<String, Object> reportMap = new HashMap<String, Object>();
/*      */     
/* 1574 */     RegistrationViewForm registrationViewDTO = (RegistrationViewForm)paramMap.get("REGISTRATION_DETAILS");
/*      */     
/* 1576 */     MachineGroup machineGroup = registrationViewDTO.getMachineGroup();
/* 1577 */     List<Machine> machineList = machineGroup.getListOfMachines();
/* 1578 */     Machine machine = machineList.get(0);
/*      */     
/* 1580 */     DateFormat df = new SimpleDateFormat("MMMM dd, yyyy");
/* 1581 */     String effectiveCancellationDateInString = df.format(machine.getEffectiveDateOfCancellation());
/* 1582 */     Business business = registrationViewDTO.getBusiness();
/*      */     
/* 1584 */     String taxPayerName = "";
/* 1585 */     if (business.getBusinessType().equalsIgnoreCase("028")) {
/*      */       
/* 1587 */       taxPayerName = business.getOwner().getWholeName();
/*      */     }
/* 1589 */     else if (business.getBusinessType().equalsIgnoreCase("029")) {
/*      */       
/* 1591 */       taxPayerName = business.getBusinessDesc();
/*      */     } 
/*      */     
/* 1594 */     DecimalFormat decimalFormatter = new DecimalFormat();
/* 1595 */     decimalFormatter.setMaximumFractionDigits(2);
/* 1596 */     decimalFormatter.setMinimumFractionDigits(2);
/* 1597 */     decimalFormatter.setGroupingUsed(true);
/* 1598 */     String accumulatedSales = decimalFormatter.format((machine.getAccumulatedTotalSales() != null) ? machine.getAccumulatedTotalSales() : Integer.valueOf(0));
/*      */     
/* 1600 */     if (taxPayerName.equalsIgnoreCase(registrationViewDTO.getBusiness().getBusinessName())) {
/*      */       
/* 1602 */       reportMap.put("REGISTERED_NAME", null);
/* 1603 */       reportMap.put("BUSINESS_NAME", taxPayerName);
/*      */     
/*      */     }
/*      */     else {
/*      */       
/* 1608 */       reportMap.put("REGISTERED_NAME", taxPayerName);
/* 1609 */       reportMap.put("BUSINESS_NAME", registrationViewDTO.getBusiness().getBusinessName());
/*      */     } 
/*      */     
/* 1612 */     reportMap.put("BUSINESS_ADDRESS", registrationViewDTO.getBranch().getAddress());
/* 1613 */     reportMap.put("TYPE_OF_MACHINE", machineGroup.getMachineType().getValue());
/* 1614 */     reportMap.put("EFFECTIVE_DATE_CANCELLATION", effectiveCancellationDateInString);
/* 1615 */     reportMap.put("CANCELLATION_REASON", (machine.getReasonForCancellation() != null && machine.getReasonForCancellation().getValue() != null) ? machine.getReasonForCancellation().getValue() : "");
/* 1616 */     reportMap.put("CURRENT_DATE", df.format(new Date()));
/* 1617 */     reportMap.put("REMARKS", machine.getRemarks());
/* 1618 */     reportMap.put("TOTAL_SALES", accumulatedSales);
/* 1619 */     reportMap.put("SIGNATORY", paramMap.get("SIGNATORY"));
/* 1620 */     reportMap.put("SIGNATORY_POS", paramMap.get("SIGNATORY_POS"));
/* 1621 */     reportMap.put("PERMIT_NO", registrationViewDTO.getPermit().getPermitCode());
/* 1622 */     reportMap.put("TIN", registrationViewDTO.getBusiness().getBusinessTIN());
/* 1623 */     reportMap.put("BRANCH", registrationViewDTO.getBranch().getBranchCode());
/* 1624 */     reportMap.put("MIN", registrationViewDTO.getMin());
/*      */     
/* 1626 */     return this.registrationLetterOfCancellationGenerator.generateReport(reportMap, (JRDataSource)new JRBeanCollectionDataSource(machineList));
/*      */   }
/*      */ 
/*      */   
/*      */   public RegistrationViewForm getPermitDetailsForCancellation(String permitCode) {
/* 1631 */     return this.registrationDao.getPermitDetailsForCancellation(permitCode);
/*      */   }
/*      */ 
/*      */   
/*      */   public String getReportRDOHeadName(String businessTIN, String branchCode) {
/* 1636 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/* 1637 */     String rdoCode = this.businessDao.getRDOCodeFromITS(businessTIN, branchCode);
/* 1638 */     paramMap.put("RDO_CD", rdoCode);
/* 1639 */     return this.registrationDao.getReportRDOHeadName(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void addMachineManagementOfDumbTerminal(Map<String, Object> param) {
/* 1650 */     RegistrationViewForm registration = (RegistrationViewForm)param.get("REGFORM");
/* 1651 */     Machine baseServerMachine = (Machine)param.get("SERVER");
/* 1652 */     Machine additionalMachine = (Machine)param.get("MACHINE");
/* 1653 */     additionalMachine.setMIN(generateMIN());
/* 1654 */     additionalMachine.setMachineCode(baseServerMachine.getMachineCode());
/* 1655 */     additionalMachine.setStatus(new ParameterizedObject("", "105", ""));
/* 1656 */     additionalMachine.setServer(false);
/* 1657 */     additionalMachine.setServerConsolidator(false);
/*      */     
/* 1659 */     Map<String, Object> machineParameter = new HashMap<String, Object>();
/* 1660 */     machineParameter.put("MACHINE_GROUP", registration.getMachineGroup());
/* 1661 */     machineParameter.put("MACHINE", additionalMachine);
/* 1662 */     machineParameter.put("AUDIT_TRAIL", param.get("AUDIT_TRAIL"));
/* 1663 */     machineParameter.put("TRANS_NUM", registration.getTransNum());
/* 1664 */     machineParameter.put("TRANS_TYPE", "040");
/* 1665 */     machineParameter.put("PARENT_CD", baseServerMachine.getMIN());
/* 1666 */     machineParameter.put("CHILD_CD", additionalMachine.getMIN());
/* 1667 */     machineParameter.put("MACHINE_CD", param.get("MACHINE_CD"));
/*      */ 
/*      */ 
/*      */     
/* 1671 */     this.registrationDao.insertSLSMachines(machineParameter);
/*      */     
/* 1673 */     this.registrationDao.insertSLSMachinesHist(machineParameter);
/* 1674 */     this.registrationDao.insertRegistrationMachineRel(machineParameter);
/* 1675 */     this.registrationDao.insertRegistrationMachineRelHist(machineParameter);
/*      */     
/* 1677 */     Map<String, Object> permitParameter = new HashMap<String, Object>();
/* 1678 */     Permit permit = registration.getPermit();
/* 1679 */     Accreditation accreditation = new Accreditation();
/* 1680 */     accreditation.setAccreditationNo(permit.getAccredCode());
/* 1681 */     permit.setPermitStatus(new ParameterizedObject("", "105", ""));
/*      */     
/* 1683 */     permitParameter.put("PERMIT", permit);
/* 1684 */     permitParameter.put("ACCREDITATION", accreditation);
/* 1685 */     permitParameter.put("BUSINESS", registration.getBusiness());
/* 1686 */     permitParameter.put("BRANCH", registration.getBranch());
/* 1687 */     permitParameter.put("MACHINE_CD", param.get("MACHINE_CD"));
/* 1688 */     permitParameter.put("TRANS_NUM", registration.getTransNum());
/* 1689 */     permitParameter.put("AUDIT_TRAIL", param.get("AUDIT_TRAIL"));
/* 1690 */     permitParameter.put("TRANS_TYPE", "041");
/* 1691 */     permitParameter.put("REMARKS", this.registrationDao.getPermitRemarksByPermitCode(permit.getPermitCode()));
/* 1692 */     permitParameter.put("REGFORM", registration);
/* 1693 */     permitParameter.put("MACHINE", additionalMachine);
/*      */     
/* 1695 */     if (registration.getMachineGroup().isGlobal()) {
/*      */       
/* 1697 */       permitParameter.put("PERMIT_CD", permit.getPermitCode());
/* 1698 */       this.registrationDao.insertRegistrationPermitHist(permitParameter);
/* 1699 */       this.registrationDao.updateAccMachinePermitStatus(permitParameter);
/*      */ 
/*      */       
/* 1702 */       this.registrationDao.updateAccPermitStatusGlobal(permitParameter);
/* 1703 */       this.registrationDao.modifyPermitUpdateDetails(permitParameter);
/*      */     
/*      */     }
/* 1706 */     else if (registration.getMachineGroup().isDecentralized()) {
/*      */ 
/*      */       
/* 1709 */       if (registration.getPermit().getPermitType().getKey().equalsIgnoreCase("109"))
/*      */       {
/* 1711 */         permitParameter.put("PERMIT_CD", generatePermitNo("FP", registration.getBranch().getBranchCode(), registration.getRdoCode()));
/*      */       }
/* 1713 */       if (registration.getPermit().getPermitType().getKey().equalsIgnoreCase("110"))
/*      */       {
/* 1715 */         permitParameter.put("PERMIT_CD", generatePermitNo("PR", registration.getBranch().getBranchCode(), registration.getRdoCode()));
/*      */       }
/*      */       
/* 1718 */       this.registrationDao.insertRegistrationPermitHist(permitParameter);
/* 1719 */       this.registrationDao.insertAccPermitForDecentralized(permitParameter);
/* 1720 */       this.registrationDao.insertAccMachinePermitForDecentralized(permitParameter);
/* 1721 */       this.registrationDao.insertRegistrationBusinessNameForDecentralized(permitParameter);
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
/*      */   public void removeOrReAddMachineManagementOfDumbTerminal(Map<String, Object> param) {
/* 1736 */     String MACHINE_PARAMETER_STATUS = "NEW_STATUS";
/* 1737 */     String MACHINE_PARAMETER_TRANS_TYPE = "TRANS_TYPE";
/* 1738 */     String MACHINE_REL_PARAMETER_IS_LINKED = "IS_LINKED";
/*      */     
/* 1740 */     Map<String, Object> machineParameter = new HashMap<String, Object>();
/* 1741 */     machineParameter.put("PARENT_CD", ((Machine)param.get("SERVER")).getMIN());
/*      */     
/* 1743 */     machineParameter.put("MACHINE", param.get("MACHINE"));
/*      */     
/* 1745 */     machineParameter.put("AUDIT_TRAIL", param.get("AUDIT_TRAIL"));
/* 1746 */     machineParameter.put("NEW_STATUS", param.get("STATUS"));
/* 1747 */     machineParameter.put("TRANS_TYPE", "041");
/* 1748 */     machineParameter.put("IS_LINKED", param.get("LINKED"));
/*      */     
/* 1750 */     this.registrationDao.updateDumbMachineStatus(machineParameter);
/* 1751 */     this.registrationDao.insertDumbMachineToHistory(machineParameter);
/* 1752 */     this.registrationDao.updateMachineRelationshipOfDumbMachine(machineParameter);
/* 1753 */     this.registrationDao.insertDumbMachineRelationshipToHistory(machineParameter);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void manageDumbTerminals(String transNum, String permitCode, Machine serverMachine, List<Machine> machineList, AuditTrail auditTrail) {
/* 1760 */     this.log.info("##I AM AT manageDumbTerminals()");
/*      */     
/* 1762 */     RegistrationViewForm registration = getPermitDetailsByPermitCode(permitCode);
/*      */ 
/*      */     
/* 1765 */     Map<String, Object> updateMap = new HashMap<String, Object>();
/* 1766 */     updateMap.put("AUDIT_TRAIL", auditTrail);
/* 1767 */     updateMap.put("PERMIT_CD", permitCode);
/*      */ 
/*      */     
/* 1770 */     Map<String, Object> curMap = new HashMap<String, Object>();
/* 1771 */     curMap.put("PERMIT_NO", permitCode);
/* 1772 */     curMap.put("REMOVED_MACHINE_STATUS", "137");
/* 1773 */     List<Machine> currMachineList = this.registrationDao.getDumbMachineDetails(curMap);
/*      */ 
/*      */     
/* 1776 */     Map<String, Object> remMap = new HashMap<String, Object>();
/* 1777 */     remMap.put("MACHINE_STATUS", "137");
/* 1778 */     List<Machine> removedMachineList = this.registrationDao.getDumbMachineDetails(remMap);
/*      */ 
/*      */ 
/*      */     
/* 1782 */     List<Machine> currentMachines = this.registrationDao.getMachinesByTransactionNumber(transNum);
/* 1783 */     this.log.info("##removedMachineList.size()=" + removedMachineList);
/* 1784 */     if (registration.getMachineGroup().isDecentralized()) {
/*      */       
/* 1786 */       this.log.info("I AM INSIDE DECENTRALIZED");
/*      */ 
/*      */       
/* 1789 */       for (Machine machine : machineList) {
/*      */         
/* 1791 */         Boolean isExist = Boolean.valueOf(false);
/*      */         
/* 1793 */         for (Machine machineFromDB : currentMachines) {
/*      */           
/* 1795 */           if (machine.getMIN().equalsIgnoreCase(machineFromDB.getMIN())) {
/*      */ 
/*      */             
/* 1798 */             isExist = Boolean.valueOf(true);
/*      */             
/*      */             break;
/*      */           } 
/*      */         } 
/* 1803 */         if (!isExist.booleanValue() && machine.getMIN().equalsIgnoreCase("new")) {
/*      */           
/* 1805 */           Map<String, Object> newMap = new HashMap<String, Object>();
/*      */           
/* 1807 */           newMap.put("MACHINE_CD", registration.getMachineGroup().getMachineCode());
/*      */           
/* 1809 */           newMap.put("REGFORM", registration);
/* 1810 */           newMap.put("SERVER", serverMachine);
/* 1811 */           newMap.put("MACHINE", machine);
/* 1812 */           newMap.put("AUDIT_TRAIL", auditTrail);
/*      */           
/* 1814 */           addMachineManagementOfDumbTerminal(newMap);
/*      */         } 
/*      */       } 
/*      */ 
/*      */ 
/*      */       
/* 1820 */       for (Machine machine : currentMachines) {
/*      */         
/* 1822 */         boolean deleteMachine = true;
/* 1823 */         for (Machine newMachineList : machineList) {
/*      */           
/* 1825 */           if (machine.getMIN().equalsIgnoreCase(newMachineList.getMIN()) || newMachineList.equals("new"))
/*      */           {
/* 1827 */             deleteMachine = false;
/*      */           }
/*      */         } 
/*      */ 
/*      */         
/* 1832 */         if (deleteMachine) {
/*      */ 
/*      */ 
/*      */           
/* 1836 */           Map<String, Object> removeMap = new HashMap<String, Object>();
/*      */           
/* 1838 */           removeMap.put("SERVER", serverMachine);
/* 1839 */           removeMap.put("MACHINE", machine);
/* 1840 */           removeMap.put("AUDIT_TRAIL", auditTrail);
/* 1841 */           removeMap.put("STATUS", "137");
/* 1842 */           removeMap.put("LINKED", "0");
/* 1843 */           removeOrReAddMachineManagementOfDumbTerminal(removeMap);
/* 1844 */           this.registrationDao.updateAccMachinePermitStatusDecentralized(removeMap);
/* 1845 */           this.registrationDao.modifyPermitUpdateDetails(updateMap);
/*      */         } 
/*      */       } 
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/* 1852 */     if (registration.getMachineGroup().isGlobal()) {
/*      */ 
/*      */       
/* 1855 */       for (Machine currMachine : currMachineList) {
/*      */         
/* 1857 */         if (null == checkIfMachineExists(currMachine, machineList)) {
/*      */ 
/*      */           
/* 1860 */           Map<String, Object> removeMap = new HashMap<String, Object>();
/*      */           
/* 1862 */           removeMap.put("SERVER", serverMachine);
/* 1863 */           removeMap.put("MACHINE", currMachine);
/* 1864 */           removeMap.put("AUDIT_TRAIL", auditTrail);
/* 1865 */           removeMap.put("STATUS", "137");
/* 1866 */           removeMap.put("LINKED", "0");
/* 1867 */           removeOrReAddMachineManagementOfDumbTerminal(removeMap);
/* 1868 */           this.registrationDao.modifyPermitUpdateDetails(updateMap);
/*      */         } 
/*      */       } 
/*      */ 
/*      */ 
/*      */       
/* 1874 */       for (Machine machine : machineList) {
/*      */         
/* 1876 */         if (null == checkIfMachineExists(machine, currMachineList)) {
/*      */           
/* 1878 */           String removedMachineMIN = checkIfMachineExists(machine, removedMachineList);
/* 1879 */           if (null != removedMachineMIN) {
/*      */ 
/*      */             
/* 1882 */             Map<String, Object> returnMap = new HashMap<String, Object>();
/*      */             
/* 1884 */             returnMap.put("SERVER", serverMachine);
/* 1885 */             machine.setMIN(removedMachineMIN);
/* 1886 */             returnMap.put("MACHINE", machine);
/* 1887 */             returnMap.put("AUDIT_TRAIL", auditTrail);
/* 1888 */             returnMap.put("STATUS", "105");
/* 1889 */             returnMap.put("LINKED", "1");
/* 1890 */             removeOrReAddMachineManagementOfDumbTerminal(returnMap);
/* 1891 */             this.registrationDao.modifyPermitUpdateDetails(updateMap);
/*      */ 
/*      */             
/*      */             continue;
/*      */           } 
/*      */           
/* 1897 */           Map<String, Object> newMap = new HashMap<String, Object>();
/* 1898 */           newMap.put("MACHINE_CD", registration.getMachineGroup().getMachineCode());
/* 1899 */           newMap.put("REGFORM", registration);
/* 1900 */           newMap.put("SERVER", serverMachine);
/* 1901 */           newMap.put("MACHINE", machine);
/* 1902 */           newMap.put("AUDIT_TRAIL", auditTrail);
/*      */           
/* 1904 */           addMachineManagementOfDumbTerminal(newMap);
/* 1905 */           this.registrationDao.modifyPermitUpdateDetails(updateMap);
/*      */         } 
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public String checkIfMachineExists(Machine mach1, List<Machine> machMapList) {
/* 1914 */     for (Machine machine : machMapList) {
/*      */       
/* 1916 */       if (mach1.getSerialNo().equalsIgnoreCase(machine.getSerialNo()) && mach1.getBrand().equalsIgnoreCase(machine.getBrand()) && mach1.getModel().equalsIgnoreCase(machine.getModel()))
/*      */       {
/*      */ 
/*      */         
/* 1920 */         return machine.getMIN();
/*      */       }
/*      */     } 
/* 1923 */     return null;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<PermitInquiryForm> getPermitsWithDumbTerminals(Map<String, Object> paramMap) {
/* 1929 */     List<String> rdoAccessList = this.registrationDao.getUserRDOAccess(paramMap);
/* 1930 */     paramMap.put("RDO_ACCESS", rdoAccessList);
/*      */     
/* 1932 */     return this.registrationDao.getPermitsWithDumbTerminals(paramMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public List<Map<String, Object>> getRegistrationRecordListForCancellation(Map<String, Object> permit) {
/* 1937 */     return this.registrationDao.getRegistrationRecordListForCancellation(permit);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<ParameterizedObject> getBIRRDOAccessByUserNameAndLoginType(Map<String, Object> param) {
/* 1943 */     return this.businessDao.getBIRRDOAccessByUserNameAndLoginType(param);
/*      */   }
/*      */ 
/*      */   
/*      */   public MachineForm getMachineDetailsByPermitCodeAndMIN(Map<String, Object> regMap) {
/* 1948 */     return this.registrationDao.getMachineDetailsByPermitCodeAndMIN(regMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public void modifyMachineDetails(Map<String, Object> regMap) {
/* 1953 */     this.registrationDao.modifyMachineDetails(regMap);
/* 1954 */     this.registrationDao.insertSLSMachinesHist(regMap);
/* 1955 */     this.registrationDao.modifyPermitUpdateDetails(regMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public int checkMachineDetails(Map<String, Object> regMap) {
/* 1960 */     return this.registrationDao.checkMachineDetails(regMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean isUserAuthorizedToPreviewPermit(String businessTin, String branchCode, String userName, String loginType) {
/* 1965 */     Map<String, Object> param = null;
/*      */ 
/*      */     
/* 1968 */     param = new HashMap<String, Object>();
/* 1969 */     param.put("BUSINESS_TIN", businessTin);
/* 1970 */     param.put("BRANCH_CODE", branchCode);
/* 1971 */     param.put("USER_NAME", userName);
/* 1972 */     param.put("LOGIN_TYPE", loginType);
/* 1973 */     param.put("IS_ADDITIONAL", null);
/* 1974 */     int isAuthorizedUser = this.businessDao.isUserAuthorizedToPreviewPermit(param);
/*      */     
/* 1976 */     this.log.info("is logged user authorized user? " + ((isAuthorizedUser > 0) ? 1 : 0));
/* 1977 */     if (isAuthorizedUser > 0)
/*      */     {
/* 1979 */       return true;
/*      */     }
/*      */ 
/*      */     
/* 1983 */     param = new HashMap<String, Object>();
/* 1984 */     param.put("BUSINESS_TIN", businessTin);
/* 1985 */     param.put("BRANCH_CODE", branchCode);
/* 1986 */     param.put("USER_NAME", userName);
/* 1987 */     param.put("LOGIN_TYPE", loginType);
/* 1988 */     param.put("IS_ADDITIONAL", "Y");
/* 1989 */     isAuthorizedUser = this.businessDao.isUserAuthorizedToPreviewPermit(param);
/*      */     
/* 1991 */     this.log.info("is logged user additional user? " + ((isAuthorizedUser > 0) ? 1 : 0));
/* 1992 */     if (isAuthorizedUser > 0)
/*      */     {
/*      */       
/* 1995 */       return true;
/*      */     }
/*      */     
/* 1998 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean SPMCheckIfValid(Map<String, Object> permitMap) {
/* 2003 */     boolean testReturn = true;
/* 2004 */     String businessTin = (String)permitMap.get("BUSINESSTIN");
/* 2005 */     String branchCode = (String)permitMap.get("BRANCH_CD");
/* 2006 */     String brand = (String)permitMap.get("BRAND");
/* 2007 */     String model = (String)permitMap.get("MODEL");
/* 2008 */     List<SPMValidationForm> spmDetails = this.registrationDao.getSPMValidationDetails(permitMap);
/*      */     
/* 2010 */     if (spmDetails.size() != 0) {
/*      */ 
/*      */       
/* 2013 */       for (SPMValidationForm lineDetails : spmDetails) {
/*      */         
/* 2015 */         if (lineDetails.getBrand().equalsIgnoreCase(brand) || lineDetails.getModel().equalsIgnoreCase(model)) {
/*      */           
/* 2017 */           if (lineDetails.getBrand().equalsIgnoreCase(brand) && lineDetails.getModel().equalsIgnoreCase(model)) {
/*      */             
/* 2019 */             if (businessTin.equalsIgnoreCase(lineDetails.getBusinessTIN()) && branchCode.equalsIgnoreCase(lineDetails.getBranchCode())) {
/*      */               
/* 2021 */               if ("111".equalsIgnoreCase(lineDetails.getPermitType().getKey())) {
/*      */ 
/*      */                 
/* 2024 */                 testReturn = false;
/*      */ 
/*      */                 
/*      */                 break;
/*      */               } 
/*      */ 
/*      */               
/*      */               continue;
/*      */             } 
/*      */             
/* 2034 */             testReturn = false;
/*      */             break;
/*      */           } 
/* 2037 */           testReturn = false;
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           break;
/*      */         } 
/*      */       } 
/*      */     } else {
/* 2046 */       testReturn = true;
/*      */     } 
/* 2048 */     return testReturn;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean checkBrandModelForSoftwareBundled(Map<String, Object> permitMap) {
/* 2054 */     String accredNo = (String)permitMap.get("ACCRED_CD");
/* 2055 */     String brand = (String)permitMap.get("BRAND");
/* 2056 */     String model = (String)permitMap.get("MODEL");
/* 2057 */     Accreditation accreditation = this.accreditationDao.getAccreditationByAccredNo(accredNo);
/* 2058 */     if (accreditation != null) {
/*      */       
/* 2060 */       Date approvalDate = accreditation.getApprovalDate();
/*      */       
/* 2062 */       if (approvalDate != null) {
/*      */         
/* 2064 */         String accredType = accreditation.getProductType().getKey();
/*      */         
/* 2066 */         if (accredType.equalsIgnoreCase("033") || accredType.equalsIgnoreCase("031")) {
/*      */ 
/*      */           
/* 2069 */           String accBrand = accreditation.getHardware().getBrand();
/* 2070 */           String accModel = accreditation.getHardware().getModel();
/* 2071 */           if (!brand.equalsIgnoreCase(accBrand) || !model.equalsIgnoreCase(accModel))
/*      */           {
/* 2073 */             return false;
/*      */           }
/*      */ 
/*      */           
/* 2077 */           return true;
/*      */         } 
/*      */         
/* 2080 */         if (accredType.equalsIgnoreCase("030"))
/*      */         {
/* 2082 */           return true;
/*      */         }
/*      */       } 
/*      */     } 
/*      */     
/* 2087 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   public String OSMValidationSoftwareAndBundled(Map<String, Object> permitMap) {
/* 2092 */     String accredNo = (String)permitMap.get("ACCRED_CD");
/* 2093 */     String brand = (String)permitMap.get("BRAND");
/* 2094 */     String model = (String)permitMap.get("MODEL");
/*      */     
/* 2096 */     Accreditation accreditation = this.accreditationDao.getAccreditationByAccredNo(accredNo);
/* 2097 */     if (accreditation != null) {
/*      */       
/* 2099 */       Date approvalDate = accreditation.getApprovalDate();
/*      */       
/* 2101 */       if (approvalDate != null) {
/*      */         
/* 2103 */         String accredType = accreditation.getProductType().getKey();
/*      */         
/* 2105 */         if (accredType.equalsIgnoreCase("033") || accredType.equalsIgnoreCase("031")) {
/*      */           
/* 2107 */           String accBrand = accreditation.getHardware().getBrand();
/* 2108 */           String accModel = accreditation.getHardware().getModel();
/*      */           
/* 2110 */           if (!brand.equalsIgnoreCase(accBrand) || !model.equalsIgnoreCase(accModel))
/*      */           {
/* 2112 */             return "ERROR";
/*      */           }
/*      */ 
/*      */           
/* 2116 */           return "BUNDLED";
/*      */         } 
/*      */         
/* 2119 */         if (accredType.equalsIgnoreCase("030"))
/*      */         {
/* 2121 */           return "SOFTWAREONLY";
/*      */         }
/*      */       } 
/*      */     } 
/*      */     
/* 2126 */     return "ERROR";
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean checkIfValidOSMFinal(Map<String, Object> permitMap) {
/* 2131 */     String accredNo = (String)permitMap.get("ACCRED_CD");
/* 2132 */     Accreditation accreditation = this.accreditationDao.getAccreditationByAccredNo(accredNo);
/*      */ 
/*      */     
/* 2135 */     if (accreditation != null) {
/*      */ 
/*      */ 
/*      */ 
/*      */       
/* 2140 */       String accredType = accreditation.getProductType().getKey();
/* 2141 */       if ((accredType.equalsIgnoreCase("033") || accredType.equalsIgnoreCase("031") || accredType.equalsIgnoreCase("030")) && validateWithSimilarMachineDetails(permitMap) && validateWithDifferentMachineModel(permitMap) && validateWithDifferentMachineBrand(permitMap))
/*      */       {
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/* 2149 */         return true;
/*      */       }
/*      */     } 
/*      */ 
/*      */     
/* 2154 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean checkIfAccreditatedBundled(Map<String, Object> permitMap) {
/* 2159 */     String brand = (String)permitMap.get("BRAND");
/* 2160 */     String model = (String)permitMap.get("MODEL");
/*      */     
/* 2162 */     List<Accreditation> accredList = this.accreditationDao.getAccreditationByNameAndVersion(permitMap);
/*      */     
/* 2164 */     Iterator<Accreditation> i$ = accredList.iterator(); if (i$.hasNext()) { Accreditation accreditationDetail = i$.next();
/*      */ 
/*      */ 
/*      */       
/* 2168 */       Hardware hardware = accreditationDetail.getHardware();
/* 2169 */       if (hardware.getBrand().equalsIgnoreCase(brand) && hardware.getModel().equalsIgnoreCase(model)) {
/*      */         
/* 2171 */         Date approvalDate = accreditationDetail.getApprovalDate();
/* 2172 */         Date today = new Date();
/*      */         
/* 2174 */         if (accreditationDetail != null && approvalDate != null && approvalDate.before(today))
/*      */         {
/* 2176 */           return true;
/*      */         }
/* 2178 */         return false;
/*      */       } 
/*      */ 
/*      */       
/* 2182 */       return false; }
/*      */ 
/*      */ 
/*      */     
/* 2186 */     return false;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean checkIfValidOSMProvisional(Map<String, Object> permitMap) {
/* 2192 */     if (validateWithSimilarMachineDetails(permitMap) && validateWithDifferentMachineModel(permitMap) && validateWithDifferentMachineBrand(permitMap))
/*      */     {
/*      */ 
/*      */       
/* 2196 */       return true;
/*      */     }
/*      */ 
/*      */     
/* 2200 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean validateWithSimilarMachineDetails(Map<String, Object> permitMap) {
/* 2205 */     String businessTIN = (String)permitMap.get("BUSINESS_TIN");
/* 2206 */     String businessBranch = (String)permitMap.get("BRANCH_CD");
/* 2207 */     List<RegistrationViewForm> osmDetails = this.registrationDao.getMachineDetailsForOSMValidation(permitMap);
/*      */     
/* 2209 */     Iterator<RegistrationViewForm> i$ = osmDetails.iterator(); if (i$.hasNext()) { RegistrationViewForm osm = i$.next();
/*      */       
/* 2211 */       Permit permit = osm.getPermit();
/* 2212 */       Business business = osm.getBusiness();
/* 2213 */       Branch branch = osm.getBranch();
/*      */       
/* 2215 */       if (business.getBusinessTIN().equals(businessTIN)) {
/*      */         
/* 2217 */         if (!permit.getPermitType().getKey().equalsIgnoreCase("111"))
/*      */         {
/* 2219 */           return false;
/*      */         }
/*      */ 
/*      */         
/* 2223 */         if (!branch.getBranchCode().equals(businessBranch))
/*      */         {
/* 2225 */           return false;
/*      */         }
/*      */ 
/*      */         
/* 2229 */         return true;
/*      */       } 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/* 2235 */       return false; }
/*      */ 
/*      */     
/* 2238 */     return true;
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean validateWithDifferentMachineModel(Map<String, Object> permitMap) {
/* 2243 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/* 2244 */     paramMap.put("NE_MODEL", permitMap.get("MODEL"));
/* 2245 */     paramMap.put("SERIAL_NO", permitMap.get("SERIAL_NO"));
/* 2246 */     paramMap.put("BRAND", permitMap.get("BRAND"));
/* 2247 */     List<RegistrationViewForm> osmDetails = this.registrationDao.getMachineDetailsForOSMValidation(paramMap);
/*      */     
/* 2249 */     if (osmDetails.size() > 0)
/*      */     {
/* 2251 */       return false;
/*      */     }
/* 2253 */     return true;
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean validateWithDifferentMachineBrand(Map<String, Object> permitMap) {
/* 2258 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/* 2259 */     paramMap.put("MODEL", permitMap.get("MODEL"));
/* 2260 */     paramMap.put("SERIAL_NO", permitMap.get("SERIAL_NO"));
/* 2261 */     paramMap.put("NE_BRAND", permitMap.get("BRAND"));
/* 2262 */     List<RegistrationViewForm> osmDetails = this.registrationDao.getMachineDetailsForOSMValidation(paramMap);
/*      */     
/* 2264 */     if (osmDetails.size() > 0)
/*      */     {
/* 2266 */       return false;
/*      */     }
/* 2268 */     return true;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setAccreditationDao(AccreditationDao accreditationDao) {
/* 2273 */     this.accreditationDao = accreditationDao;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean isPOSRegistrationValid(List<UploadMachineValidationForm> registeredMachines, String permitType, String businessTIN, String branchCode, String swName, String swVersion, String serialNo, String brand, String model, String[] posProvErrorCode) throws ExistingRegistrationApplicationException, AccredittedSoftwareForProvRegistrationException {
/* 2279 */     boolean registrationValid = true;
/*      */     
/* 2281 */     for (UploadMachineValidationForm currMachine : registeredMachines) {
/*      */ 
/*      */       
/* 2284 */       if (currMachine.getSerialNo().trim().equalsIgnoreCase(serialNo.trim())) {
/*      */ 
/*      */ 
/*      */         
/* 2288 */         if ((currMachine.getBrand().trim().equalsIgnoreCase(brand.trim()) ^ currMachine.getModel().trim().equalsIgnoreCase(model.trim())) != 0) {
/*      */ 
/*      */ 
/*      */           
/* 2292 */           if (currMachine.getPermitType().equalsIgnoreCase("110") || currMachine.getPermitType().equalsIgnoreCase("111")) {
/*      */ 
/*      */             
/* 2295 */             this.log.info("#####ERROR 1:" + currMachine.toString());
/*      */             
/* 2297 */             registrationValid = false;
/* 2298 */             throw new ExistingRegistrationApplicationException("registration already exists.. throwing ExistingRegistrationApplicationException");
/*      */           } 
/*      */ 
/*      */           
/* 2302 */           if (currMachine.getPermitType().equalsIgnoreCase("109")) {
/*      */ 
/*      */             
/* 2305 */             if (currMachine.getSoftwareName().trim().equalsIgnoreCase(swName.trim()) && currMachine.getSoftwareVersion().trim().equalsIgnoreCase(swVersion.trim())) {
/*      */ 
/*      */               
/* 2308 */               this.log.info("#####ERROR 2:" + currMachine.toString());
/* 2309 */               registrationValid = false;
/* 2310 */               throw new AccredittedSoftwareForProvRegistrationException("software is already accredited.. throwing AccredittedSoftwareForProvRegistration");
/*      */             } 
/*      */ 
/*      */             
/* 2314 */             this.log.info("#####ERROR 3:" + currMachine.toString());
/* 2315 */             registrationValid = false;
/* 2316 */             throw new ExistingRegistrationApplicationException("registration already exists.. throwing ExistingRegistrationApplicationException");
/*      */           } 
/*      */           
/*      */           continue;
/*      */         } 
/* 2321 */         if (currMachine.getBrand().trim().equalsIgnoreCase(brand.trim()) && currMachine.getModel().trim().equalsIgnoreCase(model.trim())) {
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
/* 2353 */           if (currMachine.getMachineStatus().equalsIgnoreCase("107")) {
/*      */             
/* 2355 */             this.log.info("MAchine Status is denied and can be added again");
/* 2356 */             return registrationValid;
/*      */           } 
/* 2358 */           if (null != currMachine.getSoftwareName() && null != currMachine.getSoftwareVersion() && currMachine.getSoftwareName().trim().equalsIgnoreCase(swName.trim()) && currMachine.getSoftwareVersion().trim().equalsIgnoreCase(swVersion.trim())) {
/*      */ 
/*      */ 
/*      */             
/* 2362 */             if (!currMachine.getPermitType().equalsIgnoreCase("110")) {
/*      */               
/* 2364 */               this.log.info("#####ERROR 6:" + currMachine.toString());
/* 2365 */               registrationValid = false;
/* 2366 */               throw new AccredittedSoftwareForProvRegistrationException("software is already accredited.. throwing AccredittedSoftwareForProvRegistration");
/*      */             } 
/*      */             
/*      */             continue;
/*      */           } 
/* 2371 */           this.log.info("#####ERROR 7:" + currMachine.toString());
/*      */           
/* 2373 */           registrationValid = false;
/* 2374 */           throw new ExistingRegistrationApplicationException("registration already exists.. throwing ExistingRegistrationApplicationException");
/*      */         } 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/*      */         continue;
/*      */       } 
/*      */ 
/*      */ 
/*      */       
/* 2385 */       if (currMachine.getPermitType().equalsIgnoreCase("109") && currMachine.getBrand().equalsIgnoreCase(brand) && currMachine.getModel().equalsIgnoreCase(model) && currMachine.getSoftwareName().equalsIgnoreCase(swName) && currMachine.getSoftwareVersion().equalsIgnoreCase(swVersion)) {
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/* 2391 */         this.log.info("#####ERROR 8:" + currMachine.toString());
/* 2392 */         registrationValid = false;
/* 2393 */         throw new AccredittedSoftwareForProvRegistrationException("software is already accredited.. throwing AccredittedSoftwareForProvRegistration");
/*      */       } 
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/* 2399 */     return registrationValid;
/*      */   }
/*      */ 
/*      */   
/*      */   public int checkIfSerialExist(Map<String, Object> permitMap) {
/* 2404 */     return this.registrationDao.checkIfSerialExist(permitMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public MachineResult getMachineBySerial(Map<String, Object> permitMap) {
/* 2409 */     return this.registrationDao.getMachineBySerial(permitMap);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean CRMCheckIfValid(Map<String, Object> param) {
/* 2416 */     boolean isDataValid = true;
/* 2417 */     String businessTIN = (String)param.get("BUSINESS_TIN");
/* 2418 */     String branchCode = (String)param.get("BRANCH_CD");
/* 2419 */     String brand = (String)param.get("BRANCH_CD");
/* 2420 */     String model = (String)param.get("BRANCH_CD");
/* 2421 */     String serialNo = (String)param.get("BRANCH_CD");
/* 2422 */     String permitType = (String)param.get("PERMIT_TYPE");
/*      */     
/* 2424 */     int countSerial = checkIfSerialExist(param);
/*      */     
/* 2426 */     if (countSerial >= 1) {
/*      */       
/* 2428 */       MachineResult regMachine = getMachineBySerial(param);
/*      */       
/* 2430 */       this.log.info("Machine Status: " + regMachine.getStatus().getValue());
/* 2431 */       this.log.info(" regMachine.getPermitType() : " + regMachine.getPermitType().getValue());
/*      */       
/* 2433 */       if (regMachine.getPermitType().getKey().equals("109") || regMachine.getPermitType().getKey().equals("110")) {
/*      */ 
/*      */         
/* 2436 */         if (regMachine.getStatus().getKey().equals("104") || regMachine.getStatus().getKey().equals("106") || regMachine.getStatus().getKey().equals("105"))
/*      */         {
/*      */ 
/*      */           
/* 2440 */           isDataValid = false;
/*      */         }
/*      */         else
/*      */         {
/* 2444 */           isDataValid = true;
/*      */         }
/*      */       
/* 2447 */       } else if (regMachine.getPermitType().getKey().equals("111")) {
/*      */ 
/*      */         
/* 2450 */         this.log.info(" tin : " + businessTIN.replaceAll("-", "") + " : " + regMachine.getBusinessTin());
/* 2451 */         this.log.info(" branch : " + branchCode + " : " + regMachine.getBranchCode());
/* 2452 */         this.log.info(" brand : " + brand + " : " + regMachine.getBrand());
/* 2453 */         this.log.info(" serial : " + serialNo + " : " + regMachine.getSerialNumber());
/* 2454 */         this.log.info(" model : " + model + " : " + regMachine.getModel());
/*      */         
/* 2456 */         if (regMachine.getStatus().getKey().equals("104") || regMachine.getStatus().getKey().equals("106") || regMachine.getStatus().getKey().equals("105")) {
/*      */ 
/*      */ 
/*      */ 
/*      */           
/* 2461 */           if (permitType.equals("109")) {
/*      */             
/* 2463 */             if (regMachine.getBusinessTin().equals(businessTIN.replaceAll("-", "")))
/*      */             {
/* 2465 */               this.log.info("checkTin");
/*      */             }
/* 2467 */             if (regMachine.getBranchCode().equals(branchCode))
/*      */             {
/* 2469 */               this.log.info("branch");
/*      */             }
/* 2471 */             if (regMachine.getBrand().equalsIgnoreCase(brand))
/*      */             {
/* 2473 */               this.log.info("brand");
/*      */             }
/* 2475 */             if (regMachine.getModel().equalsIgnoreCase(model))
/*      */             {
/* 2477 */               this.log.info("model");
/*      */             }
/* 2479 */             if (regMachine.getSerialNumber().equalsIgnoreCase(serialNo))
/*      */             {
/* 2481 */               this.log.info("serialNo");
/*      */             }
/*      */             
/* 2484 */             if (regMachine.getBusinessTin().equals(businessTIN.replaceAll("-", "")) && regMachine.getBranchCode().equals(branchCode) && regMachine.getBrand().equalsIgnoreCase(brand) && regMachine.getModel().equalsIgnoreCase(model) && regMachine.getSerialNumber().equalsIgnoreCase(serialNo))
/*      */             {
/*      */ 
/*      */ 
/*      */ 
/*      */               
/* 2490 */               isDataValid = true;
/*      */             }
/*      */             else
/*      */             {
/* 2494 */               isDataValid = false;
/*      */             }
/*      */           
/*      */           } else {
/*      */             
/* 2499 */             isDataValid = false;
/*      */           }
/*      */         
/*      */         } else {
/*      */           
/* 2504 */           isDataValid = true;
/*      */         }
/*      */       
/*      */       }
/*      */     
/*      */     } else {
/*      */       
/* 2511 */       isDataValid = true;
/*      */     } 
/* 2513 */     return isDataValid;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public String OSMCheckIfValid(Map<String, Object> param) {
/* 2519 */     String finalValidation = "ERROR";
/*      */ 
/*      */ 
/*      */     
/* 2523 */     String softwareName = (String)param.get("SOFTWARE_NAME");
/* 2524 */     String softwareVersion = (String)param.get("SOFTWARE_VERSION");
/* 2525 */     String permitType = (String)param.get("PERMIT_TYPE");
/*      */     
/* 2527 */     Permit permitItem = new Permit();
/* 2528 */     permitItem.setSoftwareName(softwareName);
/* 2529 */     permitItem.setSoftwareVersion(softwareVersion);
/*      */     
/* 2531 */     Map<String, Object> permitMap = new HashMap<String, Object>();
/* 2532 */     permitMap.put("PERMIT", permitItem);
/*      */     
/* 2534 */     String accredNoString = this.accreditationDao.getProductAccreditationNo(param);
/* 2535 */     param.put("ACCRED_CD", accredNoString);
/* 2536 */     if (accredNoString != null) {
/*      */       boolean validOSM; String accredited;
/* 2538 */       if (permitType.equals("109")) {
/*      */ 
/*      */         
/* 2541 */         accredited = OSMValidationSoftwareAndBundled(param);
/* 2542 */         validOSM = checkIfValidOSMFinal(param);
/*      */       
/*      */       }
/*      */       else {
/*      */         
/* 2547 */         Map<String, Object> paramMap = new HashMap<String, Object>();
/* 2548 */         Permit permit = new Permit();
/* 2549 */         permit.setSoftwareName(softwareName);
/* 2550 */         permit.setSoftwareVersion(softwareVersion);
/* 2551 */         paramMap.put("PERMIT", permit);
/* 2552 */         int result = checkIfAccreditationNoExist(paramMap);
/* 2553 */         if (result == 1) {
/*      */           
/* 2555 */           accredited = "TRUE";
/*      */         }
/*      */         else {
/*      */           
/* 2559 */           accredited = "FALSE";
/*      */         } 
/* 2561 */         validOSM = checkIfValidOSMProvisional(param);
/*      */       } 
/*      */       
/* 2564 */       finalValidation = OSMValidationReturn(permitType, accredited, validOSM);
/*      */     
/*      */     }
/*      */     else {
/*      */       
/* 2569 */       finalValidation = "NO_ACCRED_EXIST";
/*      */     } 
/*      */     
/* 2572 */     return finalValidation;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private String OSMValidationReturn(String permitType, String accredited, boolean validOSM) {
/* 2578 */     String finalValidation = null;
/* 2579 */     if (permitType.equals("109"))
/*      */     {
/*      */       
/* 2582 */       if (validOSM) {
/*      */         
/* 2584 */         if (accredited.equalsIgnoreCase("SOFTWARE")) {
/*      */           
/* 2586 */           finalValidation = "SUCCESS";
/* 2587 */           return finalValidation;
/*      */         } 
/*      */         
/* 2590 */         if (accredited.equalsIgnoreCase("BUNDLED")) {
/*      */           
/* 2592 */           finalValidation = "SUCCESS";
/* 2593 */           return finalValidation;
/*      */         } 
/*      */         
/* 2596 */         if (accredited.equalsIgnoreCase("ERROR"))
/*      */         {
/* 2598 */           finalValidation = "INVALID_DETAILS";
/*      */         
/*      */         }
/*      */         else
/*      */         {
/* 2603 */           finalValidation = "INVALID_DETAILS";
/* 2604 */           return finalValidation;
/*      */         }
/*      */       
/*      */       } else {
/*      */         
/* 2609 */         finalValidation = "INVALID_DETAILS";
/* 2610 */         return finalValidation;
/*      */       } 
/*      */     }
/*      */ 
/*      */     
/* 2615 */     if (permitType.equals("110") && validOSM) {
/*      */       
/* 2617 */       if (accredited.equals("FALSE")) {
/*      */         
/* 2619 */         finalValidation = "SUCCESS";
/* 2620 */         return finalValidation;
/*      */       } 
/*      */ 
/*      */       
/* 2624 */       finalValidation = "ACCRED_EXISTING";
/* 2625 */       return finalValidation;
/*      */     } 
/*      */ 
/*      */ 
/*      */     
/* 2630 */     return finalValidation;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public String getAccreditationDetailsExist(Map<String, Object> param) {
/* 2636 */     return this.accreditationDao.getProductAccreditationNo(param);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public Map<String, List<MachineUploadForm>> groupRegistrationByBusinessName(List<MachineUploadForm> uploadList) {
/* 2642 */     Map<String, List<MachineUploadForm>> map = new HashMap<String, List<MachineUploadForm>>();
/*      */     
/* 2644 */     for (MachineUploadForm form : uploadList) {
/*      */ 
/*      */       
/* 2647 */       String tempBusinessName = form.getBusinessName().trim();
/* 2648 */       if (map.get(tempBusinessName) == null)
/*      */       {
/* 2650 */         map.put(tempBusinessName, new ArrayList<MachineUploadForm>());
/*      */       }
/*      */       
/* 2653 */       ((List<MachineUploadForm>)map.get(tempBusinessName)).add(form);
/*      */     } 
/* 2655 */     return map;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public Map<String, Object> insertUploadedRegistrationCRMRecord(Map<String, Object> paramMap, String permitType) {
/* 2661 */     Map<String, Object> resultMap = new HashMap<String, Object>();
/* 2662 */     AuditTrail auditTrail = (AuditTrail)paramMap.get("AUDIT_TRAIL");
/* 2663 */     String userName = (String)paramMap.get("USER_NAME");
/* 2664 */     String loginType = (String)paramMap.get("LOGIN_TYPE");
/* 2665 */     Map<String, List<MachineUploadForm>> crmMap = (Map<String, List<MachineUploadForm>>)paramMap.get("CRM_DETAILS");
/*      */     
/* 2667 */     Business activeBusiness = (Business)paramMap.get("USER_BUSINESS");
/* 2668 */     Branch activeBranch = (Branch)paramMap.get("USER_BRANCH");
/* 2669 */     Set<String> businessNames = crmMap.keySet();
/* 2670 */     String taxpayerName = "";
/* 2671 */     String transList = "";
/* 2672 */     int i = 0;
/*      */     
/* 2674 */     List<String> listOfBusiness = new ArrayList<String>();
/* 2675 */     List<String> listOfTransNo = new ArrayList<String>();
/*      */     
/* 2677 */     for (String businessName : businessNames) {
/*      */       
/* 2679 */       List<MachineUploadForm> resultList = crmMap.get(businessName);
/* 2680 */       String transNo = generateTransactionNo();
/*      */       
/* 2682 */       transList = (i == 0) ? (new StringBuilder(transNo)).toString() : (transList + ", " + transNo);
/* 2683 */       i++;
/*      */       
/* 2685 */       for (MachineUploadForm record : resultList) {
/*      */         
/* 2687 */         BusinessRegistration businessReg = new BusinessRegistration();
/*      */         
/* 2689 */         Map<String, Object> resultMapITS = new HashMap<String, Object>();
/* 2690 */         resultMapITS.put("BUSINESS_TIN", record.getBusinessTin());
/* 2691 */         resultMapITS.put("BRANCH_CODE", record.getBranchCode());
/*      */         
/* 2693 */         Business business = this.businessDao.getITSBusinessInfo(resultMapITS);
/* 2694 */         Branch branch = business.getBranchList().get(0);
/*      */         
/* 2696 */         if (business.getBusinessType().equals("I")) {
/*      */           
/* 2698 */           FullName owner = new FullName();
/* 2699 */           owner.setExtensionName(business.getOwner().getExtensionName());
/* 2700 */           owner.setFirstName(business.getOwner().getFirstName());
/* 2701 */           owner.setLastName(business.getOwner().getLastName());
/* 2702 */           owner.setMiddleName(business.getOwner().getMiddleName());
/* 2703 */           taxpayerName = business.getOwner().getWholeName();
/* 2704 */           businessReg.setOwner(owner);
/* 2705 */           businessReg.setBusinessType("028");
/*      */         }
/* 2707 */         else if (business.getBusinessType().equals("N")) {
/*      */           
/* 2709 */           businessReg.setBusinessDesc(business.getBusinessDesc());
/* 2710 */           taxpayerName = business.getBusinessDesc();
/* 2711 */           businessReg.setBusinessType("029");
/*      */         } 
/* 2713 */         businessReg.setBusinessTIN(business.getBusinessTIN());
/* 2714 */         businessReg.setBusinessName(businessName);
/* 2715 */         businessReg.setBranch(branch);
/*      */         
/* 2717 */         insertBusinessDetails(businessReg, auditTrail, userName, transNo);
/*      */         
/* 2719 */         Machine machine = new Machine();
/* 2720 */         MachineGroup macGroup = new MachineGroup();
/* 2721 */         Permit permit = new Permit();
/*      */         
/* 2723 */         String permitCode = generatePermitNo(permitPrefix(permitType), businessReg.getBranch().getBranchCode(), businessReg.getBranch().getRdoCode().getKey());
/* 2724 */         permit.setPermitCode(permitCode);
/*      */         
/* 2726 */         ParameterizedObject pType = new ParameterizedObject();
/* 2727 */         pType.setKey(permitType);
/* 2728 */         permit.setPermitType(pType);
/*      */         
/* 2730 */         ParameterizedObject pStatus = new ParameterizedObject();
/* 2731 */         pStatus.setKey("104");
/* 2732 */         permit.setPermitStatus(pStatus);
/*      */         
/* 2734 */         permit.setApplicationDate(new Date());
/*      */         
/* 2736 */         machine.setBrand(record.getBrand());
/* 2737 */         machine.setModel(record.getModel());
/* 2738 */         machine.setSerialNo(record.getSerialNumber());
/* 2739 */         machine.setLastORNo(record.getLastOrNumber());
/* 2740 */         machine.setLastCashInvoiceNo(record.getLastCashInvoice());
/* 2741 */         machine.setLastChargeInvoiceNo(record.getLastChargeInvoice());
/* 2742 */         machine.setLastTransactionNo(record.getLastTransactionNo());
/* 2743 */         machine.setStatus(pStatus);
/*      */         
/* 2745 */         ParameterizedObject mType = new ParameterizedObject();
/* 2746 */         mType.setKey("050");
/* 2747 */         macGroup.setMachineType(mType);
/*      */         
/* 2749 */         machine.setServer(false);
/* 2750 */         machine.setServerConsolidator(false);
/*      */         
/* 2752 */         machine.setDateOfReading(record.getDateOfReading());
/* 2753 */         machine.setPresentReading(record.getPresentReading());
/* 2754 */         machine.setMIN(generateMIN());
/*      */         
/* 2756 */         boolean isRoving = true;
/*      */         
/* 2758 */         if (record.getRoving().substring(0, 1).equals("N"))
/*      */         {
/* 2760 */           isRoving = false;
/*      */         }
/*      */         
/* 2763 */         String machineCode = generateMachineCode();
/*      */         
/* 2765 */         macGroup.setRoving(isRoving);
/* 2766 */         macGroup.setMachineCode(machineCode);
/* 2767 */         macGroup.setTransactionNo(transNo);
/*      */         
/* 2769 */         Accreditation accred = new Accreditation();
/* 2770 */         String accredCode = getAccreditationNoOfMachine("050", activeBusiness.getBusinessTIN(), activeBranch.getBranchCode(), record.getBrand(), record.getModel());
/*      */ 
/*      */         
/* 2773 */         if (permitType.equals("109"))
/*      */         {
/* 2775 */           accred.setAccreditationNo(accredCode);
/*      */         }
/*      */         
/* 2778 */         Map<String, Object> regMap = new HashMap<String, Object>();
/* 2779 */         regMap.put("BUSINESS", businessReg);
/* 2780 */         regMap.put("BRANCH", businessReg.getBranch());
/* 2781 */         regMap.put("AUDIT_TRAIL", auditTrail);
/* 2782 */         regMap.put("LOGIN_TYPE", "002");
/* 2783 */         regMap.put("MACHINE", machine);
/* 2784 */         regMap.put("MACHINE_GROUP", macGroup);
/* 2785 */         regMap.put("MACHINE_CD", machineCode);
/* 2786 */         regMap.put("TRANS_NUM", transNo);
/* 2787 */         regMap.put("USER_NAME", userName);
/* 2788 */         regMap.put("PERMIT", permit);
/* 2789 */         regMap.put("TRANS_TYPE", "040");
/* 2790 */         regMap.put("ACCREDITATION", accred);
/*      */         
/* 2792 */         this.registrationDao.insertSLSMachines(regMap);
/* 2793 */         this.registrationDao.insertSLSMachinesHist(regMap);
/* 2794 */         this.registrationDao.insertUserMachineGroup(regMap);
/* 2795 */         this.registrationDao.insertMachineGroupRegistration(regMap);
/* 2796 */         this.registrationDao.insertRegistrationPermit(regMap);
/* 2797 */         this.registrationDao.insertRegistrationPermitHist(regMap);
/* 2798 */         this.registrationDao.insertRegistrationMachinePermit(regMap);
/* 2799 */         this.registrationDao.insertRegistrationBusinessName(regMap);
/*      */       } 
/*      */       
/* 2802 */       resultMap.put(businessName, transNo);
/* 2803 */       resultMap.put("BUSINESS", taxpayerName);
/* 2804 */       resultMap.put("TRANS_NO", transList);
/*      */       
/* 2806 */       listOfBusiness.add(businessName);
/* 2807 */       listOfTransNo.add(transNo);
/*      */     } 
/*      */     
/* 2810 */     sendEmailForUploadRegistration(loginType, userName, resultMap);
/*      */     
/* 2812 */     resultMap.put("LIST_BUSNAMES", listOfBusiness);
/* 2813 */     resultMap.put("LIST_TRANSNO", listOfTransNo);
/*      */     
/* 2815 */     return resultMap;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public Map<String, Object> insertUploadedRegistrationOSMRecord(Map<String, Object> paramMap, String permitType) {
/* 2821 */     Map<String, Object> resultMap = new HashMap<String, Object>();
/* 2822 */     AuditTrail auditTrail = (AuditTrail)paramMap.get("AUDIT_TRAIL");
/* 2823 */     String userName = (String)paramMap.get("USER_NAME");
/* 2824 */     Map<String, List<MachineUploadForm>> osmMap = (Map<String, List<MachineUploadForm>>)paramMap.get("OSM_DETAILS");
/* 2825 */     String loginType = (String)paramMap.get("LOGIN_TYPE");
/*      */     
/* 2827 */     Business activeBusiness = (Business)paramMap.get("USER_BUSINESS");
/* 2828 */     Branch activeBranch = (Branch)paramMap.get("USER_BRANCH");
/* 2829 */     Set<String> businessNames = osmMap.keySet();
/* 2830 */     String taxpayerName = "";
/* 2831 */     String transList = "";
/*      */     
/* 2833 */     int i = 0;
/*      */     
/* 2835 */     List<String> listOfBusiness = new ArrayList<String>();
/* 2836 */     List<String> listOfTransNo = new ArrayList<String>();
/*      */     
/* 2838 */     for (String businessName : businessNames) {
/*      */       
/* 2840 */       List<MachineUploadForm> resultList = osmMap.get(businessName);
/* 2841 */       String transNo = generateTransactionNo();
/*      */       
/* 2843 */       transList = (i == 0) ? (new StringBuilder(transNo)).toString() : (transList + ", " + transNo);
/* 2844 */       i++;
/*      */       
/* 2846 */       for (MachineUploadForm record : resultList) {
/*      */         
/* 2848 */         BusinessRegistration businessReg = new BusinessRegistration();
/*      */         
/* 2850 */         Map<String, Object> resultMapITS = new HashMap<String, Object>();
/* 2851 */         resultMapITS.put("BUSINESS_TIN", record.getBusinessTin());
/* 2852 */         resultMapITS.put("BRANCH_CODE", record.getBranchCode());
/*      */         
/* 2854 */         Business business = this.businessDao.getITSBusinessInfo(resultMapITS);
/* 2855 */         Branch branch = business.getBranchList().get(0);
/*      */         
/* 2857 */         if (business.getBusinessType().equals("I")) {
/*      */           
/* 2859 */           FullName owner = new FullName();
/* 2860 */           owner.setExtensionName(business.getOwner().getExtensionName());
/* 2861 */           owner.setFirstName(business.getOwner().getFirstName());
/* 2862 */           owner.setLastName(business.getOwner().getLastName());
/* 2863 */           owner.setMiddleName(business.getOwner().getMiddleName());
/* 2864 */           businessReg.setOwner(owner);
/* 2865 */           businessReg.setBusinessType("028");
/* 2866 */           taxpayerName = business.getOwner().getWholeName();
/*      */         }
/* 2868 */         else if (business.getBusinessType().equals("N")) {
/*      */           
/* 2870 */           businessReg.setBusinessDesc(business.getBusinessDesc());
/* 2871 */           businessReg.setBusinessType("029");
/* 2872 */           taxpayerName = business.getBusinessDesc();
/*      */         } 
/*      */         
/* 2875 */         businessReg.setBusinessTIN(business.getBusinessTIN());
/* 2876 */         businessReg.setBusinessName(businessName);
/* 2877 */         businessReg.setBranch(branch);
/*      */         
/* 2879 */         insertBusinessDetails(businessReg, auditTrail, userName, transNo);
/*      */         
/* 2881 */         Machine machine = new Machine();
/* 2882 */         MachineGroup macGroup = new MachineGroup();
/* 2883 */         Permit permit = new Permit();
/*      */         
/* 2885 */         String permitCode = generatePermitNo(permitPrefix(permitType), businessReg.getBranch().getBranchCode(), businessReg.getBranch().getRdoCode().getKey());
/* 2886 */         permit.setPermitCode(permitCode);
/*      */         
/* 2888 */         ParameterizedObject pType = new ParameterizedObject();
/* 2889 */         pType.setKey(permitType);
/* 2890 */         permit.setPermitType(pType);
/*      */         
/* 2892 */         ParameterizedObject pStatus = new ParameterizedObject();
/* 2893 */         pStatus.setKey("104");
/* 2894 */         permit.setPermitStatus(pStatus);
/*      */         
/* 2896 */         permit.setApplicationDate(new Date());
/*      */         
/* 2898 */         machine.setBrand(record.getBrand());
/* 2899 */         machine.setModel(record.getModel());
/* 2900 */         machine.setSerialNo(record.getSerialNumber());
/* 2901 */         machine.setLastORNo(record.getLastOrNumber());
/* 2902 */         machine.setLastCashInvoiceNo(record.getLastCashInvoice());
/* 2903 */         machine.setLastChargeInvoiceNo(record.getLastChargeInvoice());
/* 2904 */         machine.setLastTransactionNo(record.getLastTransactionNo());
/* 2905 */         machine.setStatus(pStatus);
/*      */         
/* 2907 */         ParameterizedObject mType = new ParameterizedObject();
/* 2908 */         mType.setKey("053");
/* 2909 */         macGroup.setMachineType(mType);
/*      */         
/* 2911 */         ParameterizedObject osmType = new ParameterizedObject();
/* 2912 */         osmType.setKey(record.getOsmType().getKey());
/* 2913 */         macGroup.setOsmType(osmType);
/*      */         
/* 2915 */         machine.setServer(false);
/* 2916 */         machine.setServerConsolidator(false);
/*      */         
/* 2918 */         machine.setDateOfReading(record.getDateOfReading());
/* 2919 */         machine.setPresentReading(record.getPresentReading());
/* 2920 */         machine.setMIN(generateMIN());
/*      */         
/* 2922 */         boolean isRoving = true;
/*      */         
/* 2924 */         if (record.getRoving().substring(0, 1).equals("N"))
/*      */         {
/* 2926 */           isRoving = false;
/*      */         }
/*      */         
/* 2929 */         String machineCode = generateMachineCode();
/*      */         
/* 2931 */         macGroup.setRoving(isRoving);
/* 2932 */         macGroup.setMachineCode(machineCode);
/* 2933 */         macGroup.setTransactionNo(transNo);
/*      */         
/* 2935 */         Accreditation accred = new Accreditation();
/* 2936 */         String accredCode = getAccreditationNoOfMachine("053", activeBusiness.getBusinessTIN(), activeBranch.getBranchCode(), record.getSoftwareName(), record.getSoftwareVersion());
/*      */ 
/*      */         
/* 2939 */         if (permitType.equals("109"))
/*      */         {
/* 2941 */           accred.setAccreditationNo(accredCode);
/*      */         }
/*      */         
/* 2944 */         permit.setSoftwareName(record.getSoftwareName());
/* 2945 */         permit.setSoftwareVersion(record.getSoftwareVersion());
/*      */         
/* 2947 */         Map<String, Object> regMap = new HashMap<String, Object>();
/* 2948 */         regMap.put("BUSINESS", businessReg);
/* 2949 */         regMap.put("BRANCH", businessReg.getBranch());
/* 2950 */         regMap.put("AUDIT_TRAIL", auditTrail);
/* 2951 */         regMap.put("LOGIN_TYPE", "002");
/* 2952 */         regMap.put("MACHINE", machine);
/* 2953 */         regMap.put("MACHINE_GROUP", macGroup);
/* 2954 */         regMap.put("MACHINE_CD", machineCode);
/* 2955 */         regMap.put("TRANS_NUM", transNo);
/* 2956 */         regMap.put("USER_NAME", userName);
/* 2957 */         regMap.put("PERMIT", permit);
/* 2958 */         regMap.put("TRANS_TYPE", "040");
/* 2959 */         regMap.put("ACCREDITATION", accred);
/*      */         
/* 2961 */         this.registrationDao.insertSLSMachines(regMap);
/* 2962 */         this.registrationDao.insertSLSMachinesHist(regMap);
/* 2963 */         this.registrationDao.insertUserMachineGroup(regMap);
/* 2964 */         this.registrationDao.insertMachineGroupRegistration(regMap);
/* 2965 */         this.registrationDao.insertRegistrationPermit(regMap);
/* 2966 */         this.registrationDao.insertRegistrationPermitHist(regMap);
/* 2967 */         this.registrationDao.insertRegistrationMachinePermit(regMap);
/* 2968 */         this.registrationDao.insertRegistrationBusinessName(regMap);
/*      */       } 
/* 2970 */       resultMap.put(businessName, transNo);
/* 2971 */       resultMap.put("TRANS_NO", transList);
/* 2972 */       resultMap.put("BUSINESS", taxpayerName);
/*      */       
/* 2974 */       listOfBusiness.add(businessName);
/* 2975 */       listOfTransNo.add(transNo);
/*      */     } 
/*      */     
/* 2978 */     sendEmailForUploadRegistration(loginType, userName, resultMap);
/*      */     
/* 2980 */     resultMap.put("LIST_BUSNAMES", listOfBusiness);
/* 2981 */     resultMap.put("LIST_TRANSNO", listOfTransNo);
/*      */     
/* 2983 */     return resultMap;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Map<String, Object> insertUploadedRegistrationSPMRecord(Map<String, Object> paramMap, String permitType) {
/* 2990 */     Map<String, Object> resultMap = new HashMap<String, Object>();
/* 2991 */     AuditTrail auditTrail = (AuditTrail)paramMap.get("AUDIT_TRAIL");
/* 2992 */     String userName = (String)paramMap.get("USER_NAME");
/* 2993 */     Map<String, List<MachineUploadForm>> spmMap = (Map<String, List<MachineUploadForm>>)paramMap.get("SPM_DETAILS");
/* 2994 */     String loginType = (String)paramMap.get("LOGIN_TYPE");
/*      */     
/* 2996 */     Set<String> businessNames = spmMap.keySet();
/* 2997 */     String taxpayerName = "";
/* 2998 */     String transList = "";
/*      */     
/* 3000 */     int i = 0;
/*      */     
/* 3002 */     List<String> listOfBusiness = new ArrayList<String>();
/* 3003 */     List<String> listOfTransNo = new ArrayList<String>();
/*      */     
/* 3005 */     for (String businessName : businessNames) {
/*      */       
/* 3007 */       List<MachineUploadForm> resultList = spmMap.get(businessName);
/* 3008 */       String transNo = generateTransactionNo();
/*      */       
/* 3010 */       transList = (i == 0) ? (new StringBuilder(transNo)).toString() : (transList + ", " + transNo);
/* 3011 */       i++;
/* 3012 */       for (MachineUploadForm record : resultList) {
/*      */         
/* 3014 */         BusinessRegistration businessReg = new BusinessRegistration();
/*      */         
/* 3016 */         Map<String, Object> resultMapITS = new HashMap<String, Object>();
/* 3017 */         resultMapITS.put("BUSINESS_TIN", record.getBusinessTin());
/* 3018 */         resultMapITS.put("BRANCH_CODE", record.getBranchCode());
/*      */         
/* 3020 */         Business business = this.businessDao.getITSBusinessInfo(resultMapITS);
/* 3021 */         Branch branch = business.getBranchList().get(0);
/*      */         
/* 3023 */         if (business.getBusinessType().equals("I")) {
/*      */           
/* 3025 */           FullName owner = new FullName();
/* 3026 */           owner.setExtensionName(business.getOwner().getExtensionName());
/* 3027 */           owner.setFirstName(business.getOwner().getFirstName());
/* 3028 */           owner.setLastName(business.getOwner().getLastName());
/* 3029 */           owner.setMiddleName(business.getOwner().getMiddleName());
/* 3030 */           businessReg.setOwner(owner);
/* 3031 */           businessReg.setBusinessType("028");
/* 3032 */           taxpayerName = business.getOwner().getWholeName();
/*      */         }
/* 3034 */         else if (business.getBusinessType().equals("N")) {
/*      */           
/* 3036 */           businessReg.setBusinessDesc(business.getBusinessDesc());
/* 3037 */           businessReg.setBusinessType("029");
/* 3038 */           taxpayerName = business.getBusinessDesc();
/*      */         } 
/*      */         
/* 3041 */         businessReg.setBusinessTIN(business.getBusinessTIN());
/* 3042 */         businessReg.setBusinessName(businessName);
/* 3043 */         businessReg.setBranch(branch);
/*      */         
/* 3045 */         insertBusinessDetails(businessReg, auditTrail, userName, transNo);
/*      */         
/* 3047 */         Machine machine = new Machine();
/* 3048 */         MachineGroup macGroup = new MachineGroup();
/* 3049 */         Permit permit = new Permit();
/*      */         
/* 3051 */         String permitCode = generatePermitNo(permitPrefix(permitType), businessReg.getBranch().getBranchCode(), businessReg.getBranch().getRdoCode().getKey());
/* 3052 */         permit.setPermitCode(permitCode);
/*      */         
/* 3054 */         ParameterizedObject pType = new ParameterizedObject();
/* 3055 */         pType.setKey(permitType);
/* 3056 */         permit.setPermitType(pType);
/*      */         
/* 3058 */         ParameterizedObject pStatus = new ParameterizedObject();
/* 3059 */         pStatus.setKey("104");
/* 3060 */         permit.setPermitStatus(pStatus);
/*      */         
/* 3062 */         permit.setApplicationDate(new Date());
/* 3063 */         permit.setRemarks(record.getUploadRemarks());
/*      */         
/* 3065 */         machine.setBrand(record.getBrand());
/* 3066 */         machine.setModel(record.getModel());
/* 3067 */         machine.setSerialNo(record.getSerialNumber());
/*      */         
/* 3069 */         machine.setLastORNo("");
/* 3070 */         machine.setLastCashInvoiceNo("");
/* 3071 */         machine.setLastChargeInvoiceNo("");
/* 3072 */         machine.setLastTransactionNo("");
/* 3073 */         machine.setServer(false);
/* 3074 */         machine.setServerConsolidator(false);
/* 3075 */         machine.setDateOfReading(null);
/* 3076 */         machine.setPresentReading(null);
/* 3077 */         machine.setMIN(generateMIN());
/* 3078 */         machine.setStatus(pStatus);
/*      */         
/* 3080 */         boolean isRoving = true;
/*      */         
/* 3082 */         if (record.getRoving().substring(0, 1).equals("N"))
/*      */         {
/* 3084 */           isRoving = false;
/*      */         }
/*      */         
/* 3087 */         String machineCode = generateMachineCode();
/* 3088 */         ParameterizedObject mType = new ParameterizedObject();
/* 3089 */         mType.setKey(record.getMachineType().getKey());
/* 3090 */         macGroup.setMachineType(mType);
/* 3091 */         macGroup.setRoving(isRoving);
/* 3092 */         macGroup.setMachineCode(machineCode);
/* 3093 */         macGroup.setTransactionNo(transNo);
/* 3094 */         macGroup.setDecentralized(false);
/* 3095 */         macGroup.setGlobal(false);
/* 3096 */         macGroup.setPosType(null);
/* 3097 */         macGroup.setOsmType(null);
/*      */         
/* 3099 */         Map<String, Object> regMap = new HashMap<String, Object>();
/* 3100 */         regMap.put("BUSINESS", businessReg);
/* 3101 */         regMap.put("BRANCH", businessReg.getBranch());
/* 3102 */         regMap.put("AUDIT_TRAIL", auditTrail);
/* 3103 */         regMap.put("LOGIN_TYPE", "002");
/* 3104 */         regMap.put("MACHINE", machine);
/* 3105 */         regMap.put("MACHINE_GROUP", macGroup);
/* 3106 */         regMap.put("MACHINE_CD", machineCode);
/* 3107 */         regMap.put("TRANS_NUM", transNo);
/* 3108 */         regMap.put("USER_NAME", userName);
/* 3109 */         regMap.put("PERMIT", permit);
/* 3110 */         regMap.put("TRANS_TYPE", "040");
/*      */         
/* 3112 */         this.registrationDao.insertSLSMachines(regMap);
/* 3113 */         this.registrationDao.insertSLSMachinesHist(regMap);
/* 3114 */         this.registrationDao.insertUserMachineGroup(regMap);
/* 3115 */         this.registrationDao.insertMachineGroupRegistration(regMap);
/* 3116 */         this.registrationDao.insertRegistrationPermit(regMap);
/* 3117 */         this.registrationDao.insertRegistrationPermitHist(regMap);
/* 3118 */         this.registrationDao.insertRegistrationMachinePermit(regMap);
/* 3119 */         this.registrationDao.insertRegistrationBusinessName(regMap);
/*      */       } 
/*      */       
/* 3122 */       resultMap.put(businessName, transNo);
/* 3123 */       resultMap.put("BUSINESS", taxpayerName);
/* 3124 */       resultMap.put("TRANS_NO", transList);
/*      */       
/* 3126 */       listOfBusiness.add(businessName);
/* 3127 */       listOfTransNo.add(transNo);
/*      */     } 
/*      */     
/* 3130 */     sendEmailForUploadRegistration(loginType, userName, resultMap);
/*      */     
/* 3132 */     resultMap.put("LIST_BUSNAMES", listOfBusiness);
/* 3133 */     resultMap.put("LIST_TRANSNO", listOfTransNo);
/*      */     
/* 3135 */     return resultMap;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Map<String, Object> insertUploadedRegistrationPOSRecord(Map<String, Object> paramMap, String permitType) {
/* 3143 */     String posType = (String)paramMap.get("POS_TYPE");
/* 3144 */     Map<String, Object> resultMap = new HashMap<String, Object>();
/* 3145 */     AuditTrail auditTrail = (AuditTrail)paramMap.get("AUDIT_TRAIL");
/* 3146 */     String userName = (String)paramMap.get("USER_NAME");
/* 3147 */     Map<String, List<MachineUploadForm>> posMap = (Map<String, List<MachineUploadForm>>)paramMap.get("POS_DETAILS");
/* 3148 */     String loginType = "002";
/*      */     
/* 3150 */     Business activeBusiness = (Business)paramMap.get("USER_BUSINESS");
/* 3151 */     Branch activeBranch = (Branch)paramMap.get("USER_BRANCH");
/*      */     
/* 3153 */     boolean withServerWithTerminal = ((Boolean)paramMap.get("WSERVER_WTERMINAL")).booleanValue();
/* 3154 */     boolean withServerCons = ((Boolean)paramMap.get("WSERVER_CONSOLIDATOR")).booleanValue();
/* 3155 */     boolean globalDecentralized = ((Boolean)paramMap.get("GLOBAL_DECENTRALIZED")).booleanValue();
/*      */     
/* 3157 */     boolean isGlobal = false;
/* 3158 */     if (!globalDecentralized)
/*      */     {
/* 3160 */       isGlobal = true;
/*      */     }
/* 3162 */     this.log.info(withServerWithTerminal + "---" + withServerCons + "---" + globalDecentralized);
/* 3163 */     List<String> macPostDetails = new ArrayList<String>();
/*      */     
/* 3165 */     Set<String> businessNames = posMap.keySet();
/* 3166 */     String taxpayerName = "";
/* 3167 */     String transList = "";
/* 3168 */     int k = 0;
/*      */     
/* 3170 */     List<String> listOfBusiness = new ArrayList<String>();
/* 3171 */     List<String> listOfTransNo = new ArrayList<String>();
/* 3172 */     for (String businessName : businessNames) {
/*      */       
/* 3174 */       List<MachineUploadForm> resultList = posMap.get(businessName);
/* 3175 */       String transNo = generateTransactionNo();
/* 3176 */       transList = (k == 0) ? (new StringBuilder(transNo)).toString() : (transList + ", " + transNo);
/* 3177 */       k++;
/*      */       
/* 3179 */       MachineGroup macGroup = new MachineGroup();
/* 3180 */       int i = 0;
/* 3181 */       String serverMIN = "";
/* 3182 */       String serverConsMIN = "";
/* 3183 */       String machineCode = "";
/*      */       
/* 3185 */       boolean hasTerminal = false;
/*      */       
/* 3187 */       boolean isRoving = false;
/*      */       
/* 3189 */       if (posType.equals("055"))
/*      */       {
/* 3191 */         for (MachineUploadForm checkRoving : resultList) {
/*      */           
/* 3193 */           if (checkRoving.getMachineTitle().getKey().equals("112") && checkRoving.getRoving().substring(0, 1).equals("Y"))
/*      */           {
/*      */             
/* 3196 */             isRoving = true;
/*      */           }
/*      */         } 
/*      */       }
/*      */ 
/*      */       
/* 3202 */       for (MachineUploadForm record : resultList) {
/*      */ 
/*      */         
/* 3205 */         this.log.info("======================aaaaa=============================" + record.getMachineTitle());
/* 3206 */         Machine machine = new Machine();
/* 3207 */         Permit permit = new Permit();
/* 3208 */         BusinessRegistration businessReg = new BusinessRegistration();
/*      */         
/* 3210 */         Map<String, Object> resultMapITS = new HashMap<String, Object>();
/* 3211 */         resultMapITS.put("BUSINESS_TIN", record.getBusinessTin());
/* 3212 */         resultMapITS.put("BRANCH_CODE", record.getBranchCode());
/*      */         
/* 3214 */         Business business = this.businessDao.getITSBusinessInfo(resultMapITS);
/* 3215 */         Branch branch = business.getBranchList().get(0);
/*      */         
/* 3217 */         if (business.getBusinessType().equals("I")) {
/*      */           
/* 3219 */           FullName owner = new FullName();
/* 3220 */           owner.setExtensionName(business.getOwner().getExtensionName());
/* 3221 */           owner.setFirstName(business.getOwner().getFirstName());
/* 3222 */           owner.setLastName(business.getOwner().getLastName());
/* 3223 */           owner.setMiddleName(business.getOwner().getMiddleName());
/* 3224 */           businessReg.setOwner(owner);
/* 3225 */           businessReg.setBusinessType("028");
/* 3226 */           taxpayerName = business.getOwner().getWholeName();
/*      */         }
/* 3228 */         else if (business.getBusinessType().equals("N")) {
/*      */           
/* 3230 */           businessReg.setBusinessDesc(business.getBusinessDesc());
/* 3231 */           taxpayerName = business.getBusinessDesc();
/* 3232 */           businessReg.setBusinessType("029");
/*      */         } 
/* 3234 */         businessReg.setBusinessTIN(business.getBusinessTIN());
/* 3235 */         businessReg.setBusinessName(businessName);
/* 3236 */         businessReg.setBranch(branch);
/*      */         
/* 3238 */         insertBusinessDetails(businessReg, auditTrail, userName, transNo);
/* 3239 */         ParameterizedObject pStatus = new ParameterizedObject();
/* 3240 */         pStatus.setKey("104");
/* 3241 */         if (posType.equals("054") || (posType.equals("055") && (withServerCons || !isGlobal))) {
/*      */ 
/*      */ 
/*      */           
/* 3245 */           String permitCode = generatePermitNo(permitPrefix(permitType), businessReg.getBranch().getBranchCode(), businessReg.getBranch().getRdoCode().getKey());
/* 3246 */           permit.setPermitCode(permitCode);
/*      */           
/* 3248 */           ParameterizedObject pType = new ParameterizedObject();
/* 3249 */           pType.setKey(permitType);
/* 3250 */           permit.setPermitType(pType);
/* 3251 */           permit.setSoftwareName(record.getSoftwareName());
/* 3252 */           permit.setSoftwareVersion(record.getSoftwareVersion());
/*      */           
/* 3254 */           permit.setPermitStatus(pStatus);
/* 3255 */           permit.setApplicationDate(new Date());
/* 3256 */           permit.setRemarks(record.getUploadRemarks());
/*      */           
/* 3258 */           macGroup = new MachineGroup();
/*      */ 
/*      */         
/*      */         }
/* 3262 */         else if (isGlobal && record.getMachineTitle().getKey().equals("113")) {
/*      */           
/* 3264 */           String permitCode = generatePermitNo(permitPrefix(permitType), businessReg.getBranch().getBranchCode(), businessReg.getBranch().getRdoCode().getKey());
/* 3265 */           permit.setPermitCode(permitCode);
/*      */           
/* 3267 */           ParameterizedObject pType = new ParameterizedObject();
/* 3268 */           pType.setKey(permitType);
/* 3269 */           permit.setPermitType(pType);
/* 3270 */           permit.setSoftwareName(record.getSoftwareName());
/* 3271 */           permit.setSoftwareVersion(record.getSoftwareVersion());
/*      */           
/* 3273 */           permit.setPermitStatus(pStatus);
/* 3274 */           permit.setApplicationDate(new Date());
/* 3275 */           permit.setRemarks(record.getUploadRemarks());
/*      */           
/* 3277 */           macGroup = new MachineGroup();
/*      */         } 
/* 3279 */         machine.setStatus(pStatus);
/* 3280 */         machine.setBrand(record.getBrand());
/* 3281 */         machine.setModel(record.getModel());
/* 3282 */         machine.setSerialNo(record.getSerialNumber());
/* 3283 */         machine.setLastORNo(record.getLastOrNumber());
/* 3284 */         machine.setLastCashInvoiceNo(record.getLastCashInvoice());
/* 3285 */         machine.setLastChargeInvoiceNo(record.getLastChargeInvoice());
/* 3286 */         machine.setLastTransactionNo(record.getLastTransactionNo());
/*      */         
/* 3288 */         ParameterizedObject mType = new ParameterizedObject();
/* 3289 */         mType.setKey("052");
/* 3290 */         macGroup.setMachineType(mType);
/*      */         
/* 3292 */         ParameterizedObject pPosType = new ParameterizedObject();
/* 3293 */         pPosType.setKey(posType);
/* 3294 */         macGroup.setPosType(pPosType);
/*      */         
/* 3296 */         machine.setDateOfReading(record.getDateOfReading());
/* 3297 */         machine.setPresentReading(record.getPresentReading());
/* 3298 */         machine.setMIN(generateMIN());
/*      */         
/* 3300 */         if (posType.equals("054")) {
/*      */           
/* 3302 */           isRoving = true;
/*      */           
/* 3304 */           if (record.getRoving().substring(0, 1).equals("N"))
/*      */           {
/* 3306 */             isRoving = false;
/*      */           }
/*      */         } 
/*      */         
/* 3310 */         macGroup.setRoving(isRoving);
/* 3311 */         macGroup.setTransactionNo(transNo);
/*      */         
/* 3313 */         macGroup.setWithServerCons(withServerCons);
/* 3314 */         macGroup.setWithTerminal(!withServerCons);
/* 3315 */         macGroup.setGlobal(false);
/* 3316 */         macGroup.setDecentralized(false);
/* 3317 */         macGroup.setTransactionNo(transNo);
/* 3318 */         machine.setServer(false);
/* 3319 */         machine.setServerConsolidator(false);
/*      */         
/* 3321 */         Map<String, Object> regMap = new HashMap<String, Object>();
/* 3322 */         regMap.put("BUSINESS", businessReg);
/* 3323 */         regMap.put("BRANCH", businessReg.getBranch());
/* 3324 */         regMap.put("AUDIT_TRAIL", auditTrail);
/* 3325 */         regMap.put("LOGIN_TYPE", "002");
/* 3326 */         regMap.put("MACHINE", machine);
/* 3327 */         regMap.put("MACHINE_GROUP", macGroup);
/*      */         
/* 3329 */         regMap.put("TRANS_NUM", transNo);
/* 3330 */         regMap.put("USER_NAME", userName);
/* 3331 */         regMap.put("PERMIT", permit);
/* 3332 */         regMap.put("TRANS_TYPE", "040");
/*      */ 
/*      */         
/* 3335 */         if (posType.equals("054")) {
/*      */           
/* 3337 */           if (permitType.equals("109")) {
/*      */             
/* 3339 */             Accreditation accred = new Accreditation();
/* 3340 */             String accredCode = getAccreditationNoOfMachine("052", activeBusiness.getBusinessTIN(), activeBranch.getBranchCode(), record.getSoftwareName(), record.getSoftwareVersion());
/*      */ 
/*      */             
/* 3343 */             accred.setAccreditationNo(accredCode);
/* 3344 */             regMap.put("ACCREDITATION", accred);
/*      */           } 
/*      */           
/* 3347 */           machineCode = generateMachineCode();
/* 3348 */           macGroup.setMachineCode(machineCode);
/*      */           
/* 3350 */           regMap.put("MACHINE_CD", machineCode);
/* 3351 */           this.registrationDao.insertSLSMachines(regMap);
/* 3352 */           this.registrationDao.insertSLSMachinesHist(regMap);
/* 3353 */           this.registrationDao.insertUserMachineGroup(regMap);
/* 3354 */           this.registrationDao.insertMachineGroupRegistration(regMap);
/* 3355 */           this.registrationDao.insertRegistrationPermit(regMap);
/* 3356 */           this.registrationDao.insertRegistrationPermitHist(regMap);
/* 3357 */           this.registrationDao.insertRegistrationMachinePermit(regMap);
/* 3358 */           this.registrationDao.insertRegistrationBusinessName(regMap);
/*      */         }
/* 3360 */         else if (posType.equals("055")) {
/*      */ 
/*      */           
/* 3363 */           machine.setServer(record.getMachineTitle().getKey().equals("113"));
/* 3364 */           machine.setServerConsolidator(record.getMachineTitle().getKey().equals("114"));
/* 3365 */           if (i == 0)
/*      */           {
/* 3367 */             machineCode = generateMachineCode();
/*      */           }
/*      */           
/* 3370 */           if (permitType.equals("109")) {
/*      */             
/* 3372 */             Accreditation accred = new Accreditation();
/* 3373 */             String accredCode = getAccreditationNoOfMachine("052", activeBusiness.getBusinessTIN(), activeBranch.getBranchCode(), record.getSoftwareName(), record.getSoftwareVersion());
/*      */ 
/*      */             
/* 3376 */             accred.setAccreditationNo(accredCode);
/* 3377 */             regMap.put("ACCREDITATION", accred);
/*      */           } 
/* 3379 */           macGroup.setMachineCode(machineCode);
/* 3380 */           macGroup.setGlobal(isGlobal);
/* 3381 */           macGroup.setDecentralized(!isGlobal);
/* 3382 */           regMap.put("MACHINE_CD", machineCode);
/*      */           
/* 3384 */           if (i == 0) {
/*      */             
/* 3386 */             this.registrationDao.insertUserMachineGroup(regMap);
/* 3387 */             this.registrationDao.insertMachineGroupRegistration(regMap);
/*      */           } 
/*      */ 
/*      */ 
/*      */           
/* 3392 */           this.registrationDao.insertSLSMachines(regMap);
/* 3393 */           this.registrationDao.insertSLSMachinesHist(regMap);
/*      */           
/* 3395 */           String machineTitle = record.getMachineTitle().getKey();
/* 3396 */           macPostDetails.add(machineTitle + ":" + machine.getMIN());
/*      */           
/* 3398 */           this.log.info("||                 machineTitle : " + machineTitle);
/* 3399 */           if (machineTitle.equals("113")) {
/*      */ 
/*      */             
/* 3402 */             serverMIN = machine.getMIN();
/*      */           }
/* 3404 */           else if (machineTitle.equals("114")) {
/*      */             
/* 3406 */             serverConsMIN = machine.getMIN();
/*      */           } 
/*      */           
/* 3409 */           if (withServerCons || !isGlobal) {
/*      */             
/* 3411 */             if (machineTitle.equals("112"))
/*      */             {
/*      */               
/* 3414 */               this.registrationDao.insertRegistrationMachinePermit(regMap);
/* 3415 */               this.registrationDao.insertRegistrationPermit(regMap);
/* 3416 */               this.registrationDao.insertRegistrationPermitHist(regMap);
/* 3417 */               this.registrationDao.insertRegistrationBusinessName(regMap);
/*      */ 
/*      */             
/*      */             }
/*      */ 
/*      */           
/*      */           }
/* 3424 */           else if (machineTitle.equals("113")) {
/*      */             
/* 3426 */             this.registrationDao.insertRegistrationMachinePermit(regMap);
/* 3427 */             this.registrationDao.insertRegistrationPermit(regMap);
/* 3428 */             this.registrationDao.insertRegistrationPermitHist(regMap);
/* 3429 */             this.registrationDao.insertRegistrationBusinessName(regMap);
/*      */           } 
/*      */         } 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/* 3436 */         i++;
/*      */       } 
/*      */ 
/*      */       
/* 3440 */       if (posType.equals("055")) {
/*      */         
/* 3442 */         Map<String, Object> machRelMap = new HashMap<String, Object>();
/*      */         
/* 3444 */         machRelMap.put("TRANS_NUM", transNo);
/* 3445 */         machRelMap.put("AUDIT_TRAIL", auditTrail);
/* 3446 */         for (int j = 0; j < macPostDetails.size(); j++) {
/*      */           
/* 3448 */           this.log.info("-------------------------------------------");
/* 3449 */           this.log.info("------------" + (String)macPostDetails.get(j) + "------------");
/* 3450 */           this.log.info("-------------------------------------------");
/*      */           
/* 3452 */           if (withServerCons) {
/*      */             
/* 3454 */             if (((String)macPostDetails.get(j)).split(":")[0].equals("112"))
/*      */             {
/* 3456 */               machRelMap.put("PARENT_CD", serverConsMIN);
/* 3457 */               machRelMap.put("CHILD_CD", ((String)macPostDetails.get(j)).split(":")[1]);
/*      */             }
/* 3459 */             else if (((String)macPostDetails.get(j)).split(":")[0].equals("114"))
/*      */             {
/*      */               
/* 3462 */               machRelMap.put("PARENT_CD", ((String)macPostDetails.get(j)).split(":")[1]);
/* 3463 */               machRelMap.put("CHILD_CD", ((String)macPostDetails.get(j)).split(":")[1]);
/*      */             
/*      */             }
/*      */           
/*      */           }
/*      */           else {
/*      */             
/* 3470 */             machRelMap.put("PARENT_CD", serverMIN);
/* 3471 */             machRelMap.put("CHILD_CD", ((String)macPostDetails.get(j)).split(":")[1]);
/* 3472 */             machRelMap.put("TRANS_TYPE", "040");
/*      */           } 
/*      */ 
/*      */           
/* 3476 */           if (!((String)macPostDetails.get(j)).split(":")[0].equals("113")) {
/*      */ 
/*      */             
/* 3479 */             this.registrationDao.insertRegistrationMachineRel(machRelMap);
/* 3480 */             this.registrationDao.insertRegistrationMachineRelHist(machRelMap);
/*      */           } 
/*      */         } 
/*      */       } 
/* 3484 */       resultMap.put(businessName, transNo);
/* 3485 */       resultMap.put("BUSINESS", taxpayerName);
/* 3486 */       resultMap.put("TRANS_NO", transList);
/*      */       
/* 3488 */       listOfBusiness.add(businessName);
/* 3489 */       listOfTransNo.add(transNo);
/*      */     } 
/*      */     
/* 3492 */     resultMap.put("LIST_BUSNAMES", listOfBusiness);
/* 3493 */     resultMap.put("LIST_TRANSNO", listOfTransNo);
/*      */     
/* 3495 */     sendEmailForUploadRegistration(loginType, userName, resultMap);
/* 3496 */     return resultMap;
/*      */   }
/*      */ 
/*      */   
/*      */   private String getAccreditationNoOfMachine(String machineType, String activeTin, String activeBranchCode, String swNameOrBrand, String swVerOrModel) {
/*      */     String accredProduct;
/* 3502 */     if (machineType.equals("050")) {
/*      */       
/* 3504 */       accredProduct = "MACHINE";
/*      */     }
/*      */     else {
/*      */       
/* 3508 */       accredProduct = "SOFTWARE";
/*      */     } 
/*      */     
/* 3511 */     Map<String, Object> accreditationNoParam = new HashMap<String, Object>();
/* 3512 */     accreditationNoParam.put("BUSINESS_TIN", activeTin);
/* 3513 */     accreditationNoParam.put("BRANCH_CD", activeBranchCode);
/* 3514 */     accreditationNoParam.put("ACCRED_PRODUCT", accredProduct);
/* 3515 */     accreditationNoParam.put("SWNAME_OR_BRAND", swNameOrBrand.toUpperCase());
/* 3516 */     accreditationNoParam.put("SWVER_OR_MODEL", swVerOrModel.toUpperCase());
/* 3517 */     return this.accreditationDao.getProductAccreditationNo(accreditationNoParam);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public List<UploadMachineValidationForm> getAllActiveRegisteredMachines() {
/* 3524 */     return this.registrationDao.getAllActiveRegisteredMachines();
/*      */   }
/*      */ 
/*      */   
/*      */   public void setPermitForUploadRegistrationMailer(TemplateMailer permitForUploadRegistrationMailer) {
/* 3529 */     this.permitForUploadRegistrationMailer = permitForUploadRegistrationMailer;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<UploadMachineValidationForm> getAllActiveRegisteredMachinesLikeSerial(String serial) {
/* 3535 */     if (null == serial || StringUtils.isEmpty(serial))
/*      */     {
/* 3537 */       return null;
/*      */     }
/*      */ 
/*      */     
/* 3541 */     return this.registrationDao.getAllActiveRegisteredMachinesLikeSerial(serial.substring(0, 1));
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer exportUploadedCRMRegistration(Map<String, Object> paramMap, List<MachineUploadForm> machineDetailsList) {
/* 3548 */     return this.uploadedCRMRegistrationExportGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(machineDetailsList));
/*      */   }
/*      */ 
/*      */   
/*      */   public void setUploadedCRMRegistrationExportGenerator(ReportGenerator uploadedCRMRegistrationExportGenerator) {
/* 3553 */     this.uploadedCRMRegistrationExportGenerator = uploadedCRMRegistrationExportGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportContainer exportUploadedOSMRegistration(Map<String, Object> paramMap, List<MachineUploadForm> machineDetailsList) {
/* 3558 */     return this.uploadedOSMRegistrationExportGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(machineDetailsList));
/*      */   }
/*      */ 
/*      */   
/*      */   public void setUploadedOSMRegistrationExportGenerator(ReportGenerator uploadedOSMRegistrationExportGenerator) {
/* 3563 */     this.uploadedOSMRegistrationExportGenerator = uploadedOSMRegistrationExportGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportContainer exportUploadedSPMRegistration(Map<String, Object> paramMap, List<MachineUploadForm> machineDetailsList) {
/* 3568 */     return this.uploadedSPMRegistrationExportGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(machineDetailsList));
/*      */   }
/*      */ 
/*      */   
/*      */   public void setUploadedSPMRegistrationExportGenerator(ReportGenerator uploadedSPMRegistrationExportGenerator) {
/* 3573 */     this.uploadedSPMRegistrationExportGenerator = uploadedSPMRegistrationExportGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportContainer exportUploadedPOSSARegistration(Map<String, Object> paramMap, List<MachineUploadForm> machineDetailsList) {
/* 3578 */     return this.uploadedPOSSARegistrationExportGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(machineDetailsList));
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportContainer exportUploadedPOSWTRegistration(Map<String, Object> paramMap, List<MachineUploadForm> machineDetailsList) {
/* 3583 */     return this.uploadedPOSWTRegistrationExportGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(machineDetailsList));
/*      */   }
/*      */ 
/*      */   
/*      */   public void setUploadedPOSSARegistrationExportGenerator(ReportGenerator uploadedPOSSARegistrationExportGenerator) {
/* 3588 */     this.uploadedPOSSARegistrationExportGenerator = uploadedPOSSARegistrationExportGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setUploadedPOSWTRegistrationExportGenerator(ReportGenerator uploadedPOSWTRegistrationExportGenerator) {
/* 3593 */     this.uploadedPOSWTRegistrationExportGenerator = uploadedPOSWTRegistrationExportGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public ParameterizedObject getStatusOfGlobalTerminal(String machineMin) {
/* 3598 */     return this.registrationDao.getStatusOfGlobalTerminal(machineMin);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public RegistrationPermitForm getPermitDetailsByTransNum(String TRANS_NUM) {
/* 3604 */     return this.registrationDao.getPermitDetailsByTransNum(TRANS_NUM);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public RegistrationPermitForm getPermitDetailsByMIN(String MIN) {
/* 3610 */     return this.registrationDao.getPermitDetailsByMIN(MIN);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public MachineForm getMachineDetailsByMIN(String MIN) {
/* 3616 */     return this.registrationDao.getMachineDetailsByMIN(MIN);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public Business getBusinessDetailsFromITS(String businessTIN, String branchCode) {
/* 3622 */     return this.businessDao.getBusinessDetailsFromITS(businessTIN, branchCode);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public String getBusinessNameFromITS(String trim, String trim2) {
/* 3628 */     return this.businessDao.getBusinessNameFromITS(trim, trim2);
/*      */   }
/*      */ }


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\main\registration\RegistrationServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */