/*      */ package com.indracompany.acrsal.main.accreditation;
/*      */ 
/*      */ import com.indracompany.acrsal.api.accreditation.AccreditationService;
/*      */ import com.indracompany.acrsal.dao.accreditation.AccreditationDao;
/*      */ import com.indracompany.acrsal.dao.business.BusinessDao;
/*      */ import com.indracompany.acrsal.dao.business.ReferenceDao;
/*      */ import com.indracompany.acrsal.dao.business.SequenceDao;
/*      */ import com.indracompany.acrsal.dao.business.UserDao;
/*      */ import com.indracompany.acrsal.exception.DuplicateProductException;
/*      */ import com.indracompany.acrsal.forms.UploadSoftwareValidationForm;
/*      */ import com.indracompany.acrsal.model.accreditation.Accreditation;
/*      */ import com.indracompany.acrsal.model.accreditation.Checklist;
/*      */ import com.indracompany.acrsal.models.AuditTrail;
/*      */ import com.indracompany.acrsal.models.AuthorizedRepresentative;
/*      */ import com.indracompany.acrsal.models.AuthorizedUser;
/*      */ import com.indracompany.acrsal.models.Branch;
/*      */ import com.indracompany.acrsal.models.Business;
/*      */ import com.indracompany.acrsal.models.FullName;
/*      */ import com.indracompany.acrsal.models.ParameterizedObject;
/*      */ import com.indracompany.core.mail.mailer.TemplateMailer;
/*      */ import com.indracompany.core.reporting.ReportGenerator;
/*      */ import com.indracompany.core.reporting.model.ReportContainer;
/*      */ import com.lowagie.text.pdf.BarcodePDF417;
/*      */ import java.awt.Color;
/*      */ import java.awt.Image;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Calendar;
/*      */ import java.util.Date;
/*      */ import java.util.HashMap;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import net.sf.jasperreports.engine.JRDataSource;
/*      */ import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
/*      */ import org.apache.commons.lang.StringUtils;
/*      */ import org.apache.log4j.Logger;
/*      */ 
/*      */ 
/*      */ 
/*      */ public class AccreditationServiceImpl
/*      */   implements AccreditationService
/*      */ {
/*      */   private static final long serialVersionUID = 1L;
/*   44 */   private final Logger logger = Logger.getLogger(AccreditationService.class);
/*      */   
/*      */   private static final String CURRENT_DATE = "CURRENT_DATE";
/*      */   
/*      */   private static final String HASH_CODE = "HASH_CODE";
/*      */   
/*      */   private static final String REPORT_CODE = "REPORT_CODE";
/*      */   
/*      */   private static final String REPORT_NO = "REPORT_NO";
/*      */   
/*      */   private static final String USER_NAME = "USER_NAME";
/*      */   
/*      */   private SequenceDao sequenceDao;
/*      */   
/*      */   private AccreditationDao accreditationDao;
/*      */   
/*      */   private BusinessDao businessDao;
/*      */   
/*      */   private UserDao userDao;
/*      */   
/*      */   private ReferenceDao referenceDao;
/*      */   private TemplateMailer accreditationMailer;
/*      */   private TemplateMailer accreditationApprovalMailer;
/*      */   private TemplateMailer accreditationDenialMailer;
/*      */   private TemplateMailer revokeAccreditationMailer;
/*      */   private ReportGenerator letterOfDenialReportGenerator;
/*      */   private ReportGenerator letterOfRevocationReportGenerator;
/*      */   private ReportGenerator accreditationCertificateGenerator;
/*      */   private ReportGenerator accreditationCertificateGeneratorSO;
/*      */   private ReportGenerator letterOfRevocationReportCRMGenerator;
/*      */   private ReportGenerator letterOfRevocationReportPOSandOthersGenerator;
/*      */   private ReportGenerator letterOfRevocationReportSoftwareGenerator;
/*      */   private ReportGenerator accreditationSOExportGenerator;
/*      */   private ReportGenerator accreditationCRMExportGenerator;
/*      */   private ReportGenerator accreditationPOSExportGenerator;
/*      */   private String eAccRegLink;
/*      */   
/*      */   public String insertAccreditation(Map<String, Object> accreditationMap) {
/*   82 */     String transNo = generateAccreditationTransNo();
/*   83 */     String accNo = generateAccNo(accreditationMap);
/*   84 */     String prodNo = generateProductNo();
/*      */     
/*   86 */     accreditationMap.put("PRODUCT_CODE", prodNo);
/*   87 */     accreditationMap.put("TRANS_NUM", transNo);
/*   88 */     accreditationMap.put("ACC_NO", accNo);
/*   89 */     accreditationMap.put("TRANS_TYPE", "040");
/*      */     
/*   91 */     this.businessDao.insertIntoBusinessRepMain(accreditationMap);
/*   92 */     this.businessDao.insertIntoBusinessRepHist(accreditationMap);
/*      */     
/*   94 */     this.accreditationDao.insertAccreditation(accreditationMap);
/*   95 */     this.accreditationDao.insertAccreditationHist(accreditationMap);
/*      */     
/*   97 */     this.accreditationDao.insertProductBusiness(accreditationMap);
/*   98 */     this.accreditationDao.insertProductBusinessHist(accreditationMap);
/*   99 */     this.accreditationDao.insertAccreditationBusinessName(accreditationMap);
/*      */     
/*  101 */     Business business = (Business)accreditationMap.get("BUSINESS");
/*  102 */     String taxpayerName = "";
/*  103 */     if (business.getBusinessType().equals("028")) {
/*      */       
/*  105 */       taxpayerName = business.getOwner().getWholeName();
/*      */     }
/*  107 */     else if (business.getBusinessType().equals("029")) {
/*      */       
/*  109 */       taxpayerName = business.getBusinessName();
/*      */     } 
/*  111 */     AuthorizedUser user = (AuthorizedUser)accreditationMap.get("USER");
/*      */     
/*  113 */     Map<String, Object> mailMap = new HashMap<String, Object>();
/*  114 */     mailMap.put("transNo", transNo);
/*  115 */     mailMap.put("authorizedName", user.getFullName().getLastName());
/*  116 */     mailMap.put("taxpayerName", taxpayerName);
/*  117 */     mailMap.put("userName", user.getSecurityProfile().getUsername().trim());
/*      */     
/*  119 */     Accreditation accreditation = (Accreditation)accreditationMap.get("ACCREDITATION");
/*      */     
/*  121 */     if ("034".equals(accreditation.getStatus().getKey())) {
/*      */       
/*  123 */       mailMap.put("message1", "Your application for accreditation in behalf of ");
/*  124 */       mailMap.put("message2", " has been successfully forwarded to your RDO for processing.");
/*  125 */       mailMap.put("message3", "Transaction Number:  ");
/*  126 */       mailMap.put("message4", "Please submit the complete requirements on or before the systems evaluation and coordinate with your RDO for the schedule of  the systems demonstration. Failure to submit within the required time will forfeit your application.");
/*  127 */       mailMap.put("status", accreditation.getStatus().getValue());
/*      */     } 
/*      */     
/*  130 */     if (!StringUtils.isEmpty(user.getContactDetail().getAlternateEmailAddress())) {
/*      */       
/*  132 */       String[] send = new String[2];
/*  133 */       send[0] = user.getContactDetail().getEmailAddress();
/*  134 */       send[1] = user.getContactDetail().getAlternateEmailAddress();
/*  135 */       this.accreditationMailer.send(mailMap, send);
/*      */     }
/*      */     else {
/*      */       
/*  139 */       String[] send = new String[1];
/*  140 */       send[0] = user.getContactDetail().getEmailAddress();
/*  141 */       this.accreditationMailer.send(mailMap, send);
/*      */     } 
/*      */     
/*  144 */     return transNo;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public String updateAccreditation(Map<String, Object> accreditationMap) {
/*  150 */     accreditationMap.put("TRANS_TYPE", "041");
/*      */     
/*  152 */     this.accreditationDao.updateAccreditationDetails(accreditationMap);
/*  153 */     this.accreditationDao.insertAccreditationHist(accreditationMap);
/*      */     
/*  155 */     this.accreditationDao.updateProductBusiness(accreditationMap);
/*  156 */     this.accreditationDao.insertProductBusinessHist(accreditationMap);
/*      */     
/*  158 */     return null;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private String generateAccNo(Map<String, Object> accreditationMap) {
/*  167 */     String rdoCode = ((Accreditation)accreditationMap.get("ACCREDITATION")).getRdoCode();
/*  168 */     String TIN = ((Business)accreditationMap.get("BUSINESS")).getBusinessTIN();
/*      */     
/*  170 */     Calendar now = Calendar.getInstance();
/*  171 */     Integer year = Integer.valueOf(now.get(1));
/*  172 */     Integer month = Integer.valueOf(now.get(2) + 1);
/*      */     
/*  174 */     String strMonth = Integer.toString(month.intValue());
/*      */     
/*  176 */     if (strMonth.length() == 1)
/*      */     {
/*  178 */       strMonth = "0" + strMonth;
/*      */     }
/*      */     
/*  181 */     Map<String, Object> transTemp = this.sequenceDao.getTransactionNo("ACC");
/*  182 */     int trans_number = Integer.parseInt(transTemp.get("SEQUENCE_NUMBER").toString().trim());
/*  183 */     trans_number = (trans_number + 1 > 99999) ? 1 : (trans_number + 1);
/*  184 */     this.sequenceDao.updateTransactionNo(String.valueOf(trans_number), "ACC");
/*      */     
/*  186 */     String seqNo = sequenceNumberFormatter(trans_number);
/*      */     
/*  188 */     return rdoCode + TIN + year + strMonth + seqNo;
/*      */   }
/*      */ 
/*      */   
/*      */   private String generateAccreditationTransNo() {
/*  193 */     SimpleDateFormat sdfDate = new SimpleDateFormat("yyMMdd");
/*  194 */     SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");
/*  195 */     Date date = new Date();
/*  196 */     String strDate = sdfDate.format(date);
/*  197 */     String strTime = sdfTime.format(date);
/*      */     
/*  199 */     Map<String, Object> transTemp = this.sequenceDao.getTransactionNo("TRN");
/*      */     
/*  201 */     String prefix = transTemp.get("SEQUENCE_PREFIX").toString().trim();
/*  202 */     int trans_number = Integer.parseInt(transTemp.get("SEQUENCE_NUMBER").toString().trim());
/*      */     
/*  204 */     trans_number = (trans_number + 1 > 99999) ? 1 : (trans_number + 1);
/*  205 */     this.sequenceDao.updateTransactionNo(String.valueOf(trans_number), "TRN");
/*      */ 
/*      */     
/*  208 */     return prefix + strDate + strTime + sequenceTransNumberFormatter(trans_number);
/*      */   }
/*      */ 
/*      */   
/*      */   private String generateProductNo() {
/*  213 */     SimpleDateFormat sdfDate = new SimpleDateFormat("yyMMdd");
/*  214 */     SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");
/*  215 */     Date date = new Date();
/*  216 */     String strDate = sdfDate.format(date);
/*  217 */     String strTime = sdfTime.format(date);
/*      */     
/*  219 */     Map<String, Object> transTemp = this.sequenceDao.getTransactionNo("PRD");
/*      */     
/*  221 */     String prefix = transTemp.get("SEQUENCE_PREFIX").toString().trim();
/*  222 */     int trans_number = Integer.parseInt(transTemp.get("SEQUENCE_NUMBER").toString().trim());
/*      */     
/*  224 */     trans_number = (trans_number + 1 > 99999) ? 1 : (trans_number + 1);
/*  225 */     this.sequenceDao.updateTransactionNo(String.valueOf(trans_number), "PRD");
/*      */ 
/*      */     
/*  228 */     return prefix + strDate + strTime + sequenceProdNumberFormatter(trans_number);
/*      */   }
/*      */ 
/*      */   
/*      */   private String sequenceNumberFormatter(int sequence_number) {
/*  233 */     String cont = String.valueOf(sequence_number);
/*  234 */     String padValue = "0";
/*  235 */     StringBuilder resultContainer = new StringBuilder("");
/*      */     
/*  237 */     for (int x = cont.length(); x < 4; x++)
/*      */     {
/*  239 */       resultContainer.append(padValue);
/*      */     }
/*      */     
/*  242 */     resultContainer.append(cont);
/*      */     
/*  244 */     return resultContainer.toString();
/*      */   }
/*      */ 
/*      */   
/*      */   private String sequenceTransNumberFormatter(int sequence_number) {
/*  249 */     String cont = String.valueOf(sequence_number);
/*  250 */     String padValue = "0";
/*  251 */     StringBuilder resultContainer = new StringBuilder("");
/*      */     
/*  253 */     for (int x = cont.length(); x < 5; x++)
/*      */     {
/*  255 */       resultContainer.append(padValue);
/*      */     }
/*      */     
/*  258 */     resultContainer.append(cont);
/*      */     
/*  260 */     return resultContainer.toString();
/*      */   }
/*      */ 
/*      */   
/*      */   private String sequenceProdNumberFormatter(int sequence_number) {
/*  265 */     String cont = String.valueOf(sequence_number);
/*  266 */     String padValue = "0";
/*  267 */     StringBuilder resultContainer = new StringBuilder("");
/*      */     
/*  269 */     for (int x = cont.length(); x < 5; x++)
/*      */     {
/*  271 */       resultContainer.append(padValue);
/*      */     }
/*      */     
/*  274 */     resultContainer.append(cont);
/*      */     
/*  276 */     return resultContainer.toString();
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<Map<String, Object>> getAccreditationList(Map<String, Object> param) {
/*  282 */     List<Map<String, Object>> accreditationList = new ArrayList<Map<String, Object>>();
/*  283 */     accreditationList = this.accreditationDao.getAccreditationList(param);
/*  284 */     return accreditationList;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<Map<String, Object>> getAccreditationListForRevocation(Map<String, Object> param) {
/*  290 */     List<Map<String, Object>> accreditationList = new ArrayList<Map<String, Object>>();
/*  291 */     accreditationList = this.accreditationDao.getAccreditationListForRevocation(param);
/*  292 */     return accreditationList;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public boolean checkIfAccreditationExist(Map<String, Object> productBusinessMap) throws DuplicateProductException {
/*  299 */     int accreditationCount = this.accreditationDao.checkIfAccreditationExist(productBusinessMap);
/*      */     
/*  301 */     String mode = (String)productBusinessMap.get("MODE");
/*      */     
/*  303 */     int refCount = "dataFromModify".equals(mode) ? 1 : 0;
/*      */     
/*  305 */     if (accreditationCount > refCount)
/*      */     {
/*  307 */       throw new DuplicateProductException("AccreditationCreateAction.error.duplicate.application");
/*      */     }
/*      */ 
/*      */     
/*  311 */     return false;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Accreditation getAccreditationByAccredNo(String accreditationNo) {
/*  320 */     return this.accreditationDao.getAccreditationByAccredNo(accreditationNo);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Accreditation getAccreditationByTransactionNo(String transactionNo) {
/*  327 */     return this.accreditationDao.getAccreditationByTransactionNo(transactionNo);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public List<Map<String, Object>> getAccreditationHistory(String accreditationNo) {
/*  334 */     return this.accreditationDao.getAccreditationHistory(accreditationNo);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public List<Checklist> getAccreditationCheckList(String accreditationNo) {
/*  341 */     return this.accreditationDao.getAccreditationCheckList(accreditationNo);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public String getRDOCodeByUserNameAndLoginType(Map<String, Object> param) {
/*  347 */     return this.businessDao.getRDOCodeByUserNameAndLoginType(param);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public List<ParameterizedObject> getBIRRDOAccessByUserNameAndLoginType(Map<String, Object> param) {
/*  354 */     return this.businessDao.getBIRRDOAccessByUserNameAndLoginType(param);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer exportAccreditationApplication(Map<String, Object> paramMap) {
/*  360 */     Map<String, Object> reportMap = new HashMap<String, Object>();
/*      */     
/*  362 */     Accreditation accreditation = this.accreditationDao.getAccreditationByTransactionNo((String)paramMap.get("TRANSACTION_NO"));
/*      */     
/*  364 */     AuthorizedRepresentative authorizedRep = accreditation.getAuthorizedRep();
/*      */     
/*  366 */     reportMap.put("CURRENT_DATE", paramMap.get("CURRENT_DATE"));
/*  367 */     reportMap.put("USER_NAME", paramMap.get("USER_NAME"));
/*  368 */     reportMap.put("REP_NAME", authorizedRep.getName().getWholeName());
/*      */     
/*  370 */     List<Accreditation> accredList = new ArrayList<Accreditation>();
/*  371 */     accredList.add(accreditation);
/*      */     
/*  373 */     if (accreditation.getProductType().getKey().equals("030"))
/*      */     {
/*  375 */       return this.accreditationSOExportGenerator.generateReport(reportMap, (JRDataSource)new JRBeanCollectionDataSource(accredList));
/*      */     }
/*  377 */     if (accreditation.getProductType().getKey().equals("032"))
/*      */     {
/*  379 */       return this.accreditationCRMExportGenerator.generateReport(reportMap, (JRDataSource)new JRBeanCollectionDataSource(accredList));
/*      */     }
/*  381 */     if (accreditation.getProductType().getKey().equals("031") || accreditation.getProductType().getKey().equals("033"))
/*      */     {
/*      */       
/*  384 */       return this.accreditationPOSExportGenerator.generateReport(reportMap, (JRDataSource)new JRBeanCollectionDataSource(accredList));
/*      */     }
/*      */     
/*  387 */     return null;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer printDenialLetter(Map<String, Object> paramMap) {
/*  393 */     Map<String, Object> reportMap = new HashMap<String, Object>();
/*      */     
/*  395 */     reportMap.put("SIGNATORY", paramMap.get("SIGNATORY"));
/*  396 */     reportMap.put("SIGNATORY_POS", paramMap.get("SIGNATORY_POS"));
/*  397 */     reportMap.put("PRODUCT_INFO", paramMap.get("PRODUCT_INFO"));
/*  398 */     reportMap.put("CURRENT_DATE", paramMap.get("CURRENT_DATE"));
/*      */     
/*  400 */     List<Accreditation> accredList = new ArrayList<Accreditation>();
/*  401 */     accredList.add((Accreditation)paramMap.get("ACCREDITATION"));
/*  402 */     return this.letterOfDenialReportGenerator.generateReport(reportMap, (JRDataSource)new JRBeanCollectionDataSource(accredList));
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer generateAccreditationCertificate(String accreditationNo, String BarCode, String hashCode, String printStatus, String branchCode) {
/*  410 */     BarcodePDF417 barPdf = new BarcodePDF417();
/*  411 */     barPdf.setText(BarCode);
/*  412 */     Image barcodeImage = barPdf.createAwtImage(Color.BLACK, Color.WHITE);
/*      */     
/*  414 */     Map<String, Object> reportMap = new HashMap<String, Object>();
/*  415 */     Accreditation accreditation = this.accreditationDao.getAccreditationByAccredNo(accreditationNo);
/*  416 */     Business business = this.businessDao.getBusinessDetailsOfTINBranchAccredCd(accreditation.getBusinessTIN(), accreditation.getBranchCode(), accreditation.getAccreditationNo());
/*      */ 
/*      */     
/*  419 */     String supplierName = null;
/*  420 */     String printLabel = null;
/*  421 */     Date dateToday = accreditation.getApprovalDate();
/*      */     
/*  423 */     SimpleDateFormat sdfDate = new SimpleDateFormat("MMMM dd, yyyy");
/*  424 */     String approvalDate = sdfDate.format(dateToday);
/*      */     
/*  426 */     String signatory = getReportRDOHeadName(accreditation.getRdoCode());
/*      */     
/*  428 */     if (business.getBusinessType().equals("028")) {
/*      */       
/*  430 */       String middleName = (business.getOwner().getMiddleName() != null) ? business.getOwner().getMiddleName() : ".";
/*  431 */       supplierName = business.getOwner().getFirstName() + " " + middleName + " " + business.getOwner().getLastName();
/*      */     }
/*      */     else {
/*      */       
/*  435 */       supplierName = business.getBusinessDesc();
/*      */     } 
/*      */     
/*  438 */     if (printStatus.equals("Original")) {
/*      */       
/*  440 */       printLabel = "ORIGINAL COPY";
/*      */     
/*      */     }
/*      */     else {
/*      */       
/*  445 */       printLabel = "REPRINT";
/*      */     } 
/*      */     
/*  448 */     String rdoValue = ((Branch)business.getBranchList().get(0)).getRdoCode().getKey() + " " + ((Branch)business.getBranchList().get(0)).getRdoCode().getValue();
/*  449 */     ParameterizedObject rr_desc = getRRDescription(((Branch)business.getBranchList().get(0)).getRdoCode().getKey());
/*      */     
/*  451 */     Date date = new Date();
/*  452 */     String modifiedDate = (new SimpleDateFormat("yyyy-MM-dd")).format(date);
/*      */     
/*  454 */     modifiedDate = "Date Printed : " + modifiedDate;
/*      */     
/*  456 */     reportMap.put("SUPPLIER_NAME", supplierName);
/*  457 */     reportMap.put("ACCREDITATION_NO", accreditation.getAccreditationNo());
/*  458 */     reportMap.put("RDO_CODE", rdoValue);
/*  459 */     reportMap.put("BUSINESS_TIN", business.getBusinessTIN());
/*  460 */     reportMap.put("BUSINESS_NAME", business.getBusinessName());
/*  461 */     reportMap.put("ADDRESS", ((Branch)business.getBranchList().get(0)).getAddress());
/*  462 */     reportMap.put("HASH_CODE", hashCode);
/*  463 */     reportMap.put("SIGNATORY", signatory);
/*  464 */     reportMap.put("BAR_CODE", barcodeImage);
/*  465 */     reportMap.put("DATE_TODAY", approvalDate);
/*  466 */     reportMap.put("PRINT_STATUS", printLabel);
/*  467 */     reportMap.put("BRANCH_CODE", branchCode);
/*  468 */     reportMap.put("PRINT_DATE", modifiedDate);
/*      */     
/*  470 */     String rrkey = ((Branch)business.getBranchList().get(0)).getRdoCode().getKey();
/*  471 */     if (rrkey.equals("116") || rrkey.equals("121") || rrkey.equals("122") || rrkey.equals("123") || rrkey.equals("124") || rrkey.equals("125") || rrkey.equals("126")) {
/*      */       
/*  473 */       reportMap.put("RR_NUM", "LARGE TAXPAYERS SERVICE ");
/*      */     }
/*      */     else {
/*      */       
/*  477 */       reportMap.put("RR_NUM", rr_desc.getKey() + " " + rr_desc.getValue());
/*      */     } 
/*  479 */     ParameterizedObject signatoryPos = this.referenceDao.getReference("SCP").get(0);
/*  480 */     reportMap.put("SIGNATORY_POS", signatoryPos.getValue());
/*      */     
/*  482 */     List<Accreditation> accredList = new ArrayList<Accreditation>();
/*  483 */     accredList.add(accreditation);
/*      */     
/*  485 */     if (accreditation.getProductType().getKey().equals("030"))
/*      */     {
/*  487 */       return this.accreditationCertificateGeneratorSO.generateReport(reportMap, (JRDataSource)new JRBeanCollectionDataSource(accredList));
/*      */     }
/*      */ 
/*      */     
/*  491 */     return this.accreditationCertificateGenerator.generateReport(reportMap, (JRDataSource)new JRBeanCollectionDataSource(accredList));
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String generateHashCode(String productType) {
/*  499 */     SimpleDateFormat sdfDate = new SimpleDateFormat("ddMMyyyy");
/*  500 */     Date date = new Date();
/*  501 */     String strDate = sdfDate.format(date);
/*      */     
/*  503 */     Map<String, Object> transTemp = this.sequenceDao.getTransactionNo("AHC");
/*      */     
/*  505 */     int trans_number = Integer.parseInt(transTemp.get("SEQUENCE_NUMBER").toString().trim());
/*  506 */     trans_number = (trans_number + 1 > 99999) ? 1 : (trans_number + 1);
/*      */     
/*  508 */     this.sequenceDao.updateTransactionNo(String.valueOf(trans_number), "AHC");
/*      */     
/*  510 */     return productType + strDate + sequenceNumberFormatter(trans_number);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void setAccreditationDao(AccreditationDao accreditationDao) {
/*  516 */     this.accreditationDao = accreditationDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSequenceDao(SequenceDao sequenceDao) {
/*  521 */     this.sequenceDao = sequenceDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setBusinessDao(BusinessDao businessDao) {
/*  526 */     this.businessDao = businessDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setAccreditationMailer(TemplateMailer accreditationMailer) {
/*  531 */     this.accreditationMailer = accreditationMailer;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void setAccreditationApprovalMailer(TemplateMailer accreditationApprovalMailer) {
/*  537 */     this.accreditationApprovalMailer = accreditationApprovalMailer;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void setAccreditationDenialMailer(TemplateMailer accreditationDenialMailer) {
/*  543 */     this.accreditationDenialMailer = accreditationDenialMailer;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public List<ParameterizedObject> getAccreditationStatus() {
/*  550 */     return this.accreditationDao.getAccreditationStatus();
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void updateApprovalStatus(Map<String, Object> accreditationMap) {
/*  557 */     this.accreditationDao.updateApprovalStatus(accreditationMap);
/*      */     
/*  559 */     Accreditation accred = (Accreditation)accreditationMap.get("ACCREDITATION");
/*      */     
/*  561 */     accred.getStatus().getKey();
/*      */     
/*  563 */     if (accreditationMap.get("CHECKLIST") != null || accred.getStatus().getKey().equals("APPROVED")) {
/*      */       
/*  565 */       this.accreditationDao.deleteCheckList(accreditationMap);
/*  566 */       this.accreditationDao.insertCheckListApproval(accreditationMap);
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void saveAccreditationRequirements(Map<String, Object> accreditationMap) {
/*  574 */     this.accreditationDao.updateProductBusinessDetails(accreditationMap);
/*  575 */     Accreditation accred = (Accreditation)accreditationMap.get("ACCREDITATION");
/*      */     
/*  577 */     accred.getStatus().getKey();
/*      */     
/*  579 */     if (accreditationMap.get("CHECKLIST") != null) {
/*      */       
/*  581 */       this.accreditationDao.deleteCheckList(accreditationMap);
/*  582 */       this.accreditationDao.insertCheckListApproval(accreditationMap);
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void insertAccreditationReportCounter(String reportCode, String reportNo, String hashCode, AuditTrail auditTrail) {
/*  590 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/*  591 */     paramMap.put("REPORT_CODE", reportCode);
/*  592 */     paramMap.put("REPORT_NO", reportNo);
/*  593 */     paramMap.put("HASH_CODE", hashCode);
/*  594 */     paramMap.put("AUDIT_TRAIL", auditTrail);
/*  595 */     this.accreditationDao.insertAccreditationReportCounter(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void updateAccreditationReportCopyCounter(String reportCode, String reportNo, String hashCode, AuditTrail auditTrail) {
/*  601 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/*  602 */     paramMap.put("REPORT_CODE", reportCode);
/*  603 */     paramMap.put("REPORT_NO", reportNo);
/*  604 */     paramMap.put("HASH_CODE", hashCode);
/*  605 */     paramMap.put("AUDIT_TRAIL", auditTrail);
/*  606 */     this.accreditationDao.updateAccreditationReportCopyCounter(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void updateAccreditationReportAsOrigCounter(String reportCode, String reportNo, String hashCode, AuditTrail auditTrail) {
/*  612 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/*  613 */     paramMap.put("REPORT_CODE", reportCode);
/*  614 */     paramMap.put("REPORT_NO", reportNo);
/*  615 */     paramMap.put("HASH_CODE", hashCode);
/*  616 */     paramMap.put("AUDIT_TRAIL", auditTrail);
/*  617 */     this.accreditationDao.updateAccreditationReportAsOrigCounter(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public Integer getReportCounter(String reportCode, String reportNo) {
/*  623 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/*  624 */     paramMap.put("REPORT_CODE", reportCode);
/*  625 */     paramMap.put("REPORT_NO", reportNo);
/*  626 */     return this.accreditationDao.getReportCounter(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public String getReportAccreditationHashCode(String reportCode, String reportNo) {
/*  632 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/*  633 */     paramMap.put("REPORT_CODE", reportCode);
/*  634 */     paramMap.put("REPORT_NO", reportNo);
/*  635 */     return this.accreditationDao.getReportAccreditationHashCode(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public String getReportRDOHeadName(String rdoCode) {
/*  642 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/*  643 */     paramMap.put("RDO_CD", rdoCode);
/*  644 */     return this.accreditationDao.getReportRDOHeadName(paramMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public String getRRNumber(String rdoCode) {
/*  649 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/*  650 */     paramMap.put("RDO_CD", rdoCode);
/*  651 */     return this.accreditationDao.getRRNumber(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public ReportContainer exportLetterOfRevocation(Map<String, Object> paramMap) {
/*  659 */     Accreditation accred = (Accreditation)paramMap.get("ACCREDITATION");
/*      */     
/*  661 */     List<Accreditation> accredList = new ArrayList<Accreditation>();
/*  662 */     accredList.add(accred);
/*      */     
/*  664 */     if (accred.getProductType().getKey().equals("032"))
/*      */     {
/*  666 */       return this.letterOfRevocationReportCRMGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(accredList));
/*      */     }
/*  668 */     if (accred.getProductType().getKey().equals("033") || accred.getProductType().getKey().equals("031"))
/*      */     {
/*      */       
/*  671 */       return this.letterOfRevocationReportPOSandOthersGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(accredList));
/*      */     }
/*  673 */     if (accred.getProductType().getKey().equals("030"))
/*      */     {
/*  675 */       return this.letterOfRevocationReportSoftwareGenerator.generateReport(paramMap, (JRDataSource)new JRBeanCollectionDataSource(accredList));
/*      */     }
/*      */     
/*  678 */     return null;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void setLetterOfDenialReportGenerator(ReportGenerator letterOfDenialReportGenerator) {
/*  684 */     this.letterOfDenialReportGenerator = letterOfDenialReportGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setAccreditationCertificateGenerator(ReportGenerator accreditationCertificateGenerator) {
/*  689 */     this.accreditationCertificateGenerator = accreditationCertificateGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setAccreditationSOExportGenerator(ReportGenerator accreditationSOExportGenerator) {
/*  694 */     this.accreditationSOExportGenerator = accreditationSOExportGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setAccreditationCRMExportGenerator(ReportGenerator accreditationCRMExportGenerator) {
/*  699 */     this.accreditationCRMExportGenerator = accreditationCRMExportGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setAccreditationPOSExportGenerator(ReportGenerator accreditationPOSExportGenerator) {
/*  704 */     this.accreditationPOSExportGenerator = accreditationPOSExportGenerator;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public Business getBusinessFromProduct(Map<String, Object> paramMap) {
/*  711 */     return this.businessDao.getBusinessFromProduct(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public void testDeleteInsertStatus(Map<String, Object> accreditationMap) {
/*  718 */     this.accreditationDao.deleteCheckList(accreditationMap);
/*  719 */     this.accreditationDao.insertCheckListApproval(accreditationMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void revokeAccreditation(Map<String, Object> revokeMap) {
/*  725 */     this.accreditationDao.updateAccreditationStatusToRevoked(revokeMap);
/*  726 */     this.accreditationDao.insertIntoBusinessesProductsHistUponRevocation(revokeMap);
/*      */     
/*  728 */     List<Map<String, Object>> createUserList = new ArrayList<Map<String, Object>>();
/*  729 */     createUserList = this.accreditationDao.getCreateUserContactListByAccredNo(revokeMap);
/*      */     
/*  731 */     List<Accreditation> accreditationList = null;
/*      */     
/*  733 */     String userTemp = "", prevEmailTemp = "", prevAltEmailTemp = "";
/*  734 */     Accreditation accreditation = null;
/*  735 */     for (int i = 0; i < createUserList.size(); i++) {
/*      */       
/*  737 */       if (!StringUtils.isEmpty(userTemp) && !userTemp.equals(((Map)createUserList.get(i)).get("USER_NAME"))) {
/*      */ 
/*      */         
/*  740 */         Map<String, Object> mailMap = new HashMap<String, Object>();
/*  741 */         mailMap.put("userName", userTemp);
/*  742 */         mailMap.put("accreditationList", accreditationList);
/*      */         
/*  744 */         sendEmail(mailMap, prevEmailTemp, prevAltEmailTemp);
/*  745 */         mailMap.clear();
/*      */         
/*  747 */         accreditationList = null;
/*      */       } 
/*  749 */       if (accreditationList == null)
/*      */       {
/*  751 */         accreditationList = new ArrayList<Accreditation>();
/*      */       }
/*      */       
/*  754 */       accreditation = new Accreditation();
/*      */       
/*  756 */       String accredCode = (String)((Map)createUserList.get(i)).get("ACCRED_CD");
/*  757 */       String userName = (String)((Map)createUserList.get(i)).get("USER_NAME");
/*  758 */       String prevEmail = (String)((Map)createUserList.get(i)).get("EMAIL_ADD");
/*  759 */       String prevAltEmail = (String)((Map)createUserList.get(i)).get("ALTERNATE_EMAIL_ADD");
/*      */       
/*  761 */       String softwareName = (String)((Map)createUserList.get(i)).get("SOFTWARE_NAME");
/*  762 */       String softwareVer = (String)((Map)createUserList.get(i)).get("SOFTWARE_VER");
/*  763 */       String machineBrand = (String)((Map)createUserList.get(i)).get("MACHINE_BRAND");
/*  764 */       String machineModel = (String)((Map)createUserList.get(i)).get("MACHINE_MODEL");
/*  765 */       String reason = (String)((Map)createUserList.get(i)).get("REVOKE_REASON");
/*  766 */       String name = (String)((Map)createUserList.get(i)).get("NAME");
/*  767 */       String accredDetailsEmail = (!StringUtils.isEmpty(softwareName) ? (softwareName + " ") : "") + (!StringUtils.isEmpty(softwareVer) ? (softwareVer + " ") : "") + (!StringUtils.isEmpty(machineBrand) ? (machineBrand + " ") : "") + (!StringUtils.isEmpty(machineModel) ? machineModel : "");
/*      */ 
/*      */       
/*  770 */       accreditation.setAccreditationNo(accredCode);
/*  771 */       accreditation.setRemarks(accredDetailsEmail);
/*      */       
/*  773 */       ParameterizedObject revokeReason = new ParameterizedObject();
/*  774 */       revokeReason.setValue(reason);
/*  775 */       accreditation.setRevokeReason(revokeReason);
/*      */       
/*  777 */       accreditationList.add(accreditation);
/*      */       
/*  779 */       userTemp = userName;
/*  780 */       prevEmailTemp = prevEmail;
/*  781 */       prevAltEmailTemp = prevAltEmail;
/*  782 */       if (i == createUserList.size() - 1) {
/*      */ 
/*      */         
/*  785 */         Map<String, Object> mailMap = new HashMap<String, Object>();
/*  786 */         mailMap.put("userName", userTemp);
/*  787 */         mailMap.put("accreditationList", accreditationList);
/*  788 */         mailMap.put("taxpayerRegisteredName", name.trim().replaceAll(" +", " "));
/*      */         
/*  790 */         sendEmail(mailMap, prevEmailTemp, prevAltEmailTemp);
/*  791 */         mailMap.clear();
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void processAccreditation(Map<String, Object> accreditationMap) {
/*  799 */     String transNo = (String)accreditationMap.get("TRANS_NUM");
/*  800 */     Map<String, Object> mailMap = new HashMap<String, Object>();
/*  801 */     String accNo = (String)accreditationMap.get("ACC_NO");
/*  802 */     List<Map<String, Object>> createUserList = new ArrayList<Map<String, Object>>();
/*      */     
/*  804 */     accreditationMap.put("ACCRED_NO_LIST", "'" + accNo.trim() + "'");
/*  805 */     createUserList = this.accreditationDao.getCreateUserContactListByAccredNo(accreditationMap);
/*      */     
/*  807 */     String prevUserName = (String)((Map)createUserList.get(0)).get("USER_NAME");
/*  808 */     String prevEmail = (String)((Map)createUserList.get(0)).get("EMAIL_ADD");
/*  809 */     String prevAltEmail = (String)((Map)createUserList.get(0)).get("ALTERNATE_EMAIL_ADD");
/*      */     
/*  811 */     mailMap.put("transNo", transNo);
/*  812 */     mailMap.put("userName", prevUserName);
/*      */     
/*  814 */     FullName fullName = this.userDao.getCompleteNameOfUser(prevUserName, "002");
/*      */     
/*  816 */     Accreditation accreditation = (Accreditation)accreditationMap.get("ACCREDITATION");
/*      */     
/*  818 */     if ("039".equals(accreditation.getStatus().getKey())) {
/*      */       
/*  820 */       mailMap.put("message1", "Your application for accreditation has been approved. ");
/*  821 */       mailMap.put("message2", "Transaction Number: ");
/*  822 */       mailMap.put("remarks", "");
/*  823 */       mailMap.put("loginTypeLink", this.eAccRegLink);
/*  824 */       mailMap.put("message3", "You may now access your account via ");
/*  825 */       mailMap.put("message4", " to file applications for Registration of Software and/or Sales Machines of your clienteles.");
/*  826 */       mailMap.put("status", "Approved");
/*  827 */       mailMap.put("authorizedName", fullName.getLastName());
/*      */       
/*  829 */       if (!StringUtils.isEmpty(prevAltEmail))
/*      */       {
/*  831 */         String[] send = new String[2];
/*  832 */         send[0] = prevEmail;
/*  833 */         send[1] = prevAltEmail;
/*  834 */         this.accreditationApprovalMailer.send(mailMap, send);
/*      */       }
/*      */       else
/*      */       {
/*  838 */         String[] send = new String[1];
/*  839 */         send[0] = prevEmail;
/*  840 */         this.accreditationApprovalMailer.send(mailMap, send);
/*      */       }
/*      */     
/*      */     }
/*      */     else {
/*      */       
/*  846 */       Business business = (Business)accreditationMap.get("BUSINESS");
/*  847 */       String taxpayerName = "";
/*  848 */       if (business.getBusinessType().equals("028")) {
/*      */         
/*  850 */         taxpayerName = business.getOwner().getWholeName();
/*      */       }
/*  852 */       else if (business.getBusinessType().equals("029")) {
/*      */         
/*  854 */         taxpayerName = business.getBusinessName();
/*      */       } 
/*  856 */       mailMap.put("message1", "The application for accreditation of ");
/*  857 */       mailMap.put("message2", "with transaction number ");
/*  858 */       mailMap.put("message3", " has been denied  due to ");
/*  859 */       mailMap.put("remarks", accreditation.getApprovalRemarks());
/*  860 */       mailMap.put("taxpayerName", taxpayerName);
/*  861 */       mailMap.put("authorizedName", fullName.getLastName());
/*      */       
/*  863 */       mailMap.put("status", "Denied");
/*      */       
/*  865 */       if (!StringUtils.isEmpty(prevAltEmail)) {
/*      */         
/*  867 */         String[] send = new String[2];
/*  868 */         send[0] = prevEmail;
/*  869 */         send[1] = prevAltEmail;
/*  870 */         this.accreditationDenialMailer.send(mailMap, send);
/*      */       }
/*      */       else {
/*      */         
/*  874 */         String[] send = new String[1];
/*  875 */         send[0] = prevEmail;
/*  876 */         this.accreditationDenialMailer.send(mailMap, send);
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void sendEmail(Map<String, Object> mailMap, String email, String altEmail) {
/*  884 */     if (!StringUtils.isEmpty(altEmail)) {
/*      */       
/*  886 */       String[] send = new String[2];
/*  887 */       send[0] = email;
/*  888 */       send[1] = altEmail;
/*  889 */       this.revokeAccreditationMailer.send(mailMap, send);
/*      */     }
/*      */     else {
/*      */       
/*  893 */       String[] send = new String[1];
/*  894 */       send[0] = email;
/*  895 */       this.revokeAccreditationMailer.send(mailMap, send);
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public void setRevokeAccreditationMailer(TemplateMailer revokeAccreditationMailer) {
/*  901 */     this.revokeAccreditationMailer = revokeAccreditationMailer;
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportGenerator getLetterOfRevocationReportGenerator() {
/*  906 */     return this.letterOfRevocationReportGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setLetterOfRevocationReportGenerator(ReportGenerator letterOfRevocationReportGenerator) {
/*  911 */     this.letterOfRevocationReportGenerator = letterOfRevocationReportGenerator;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<Map<String, Object>> getAccredittedProductDetailsList(Map<String, Object> param, String machineType, String softwareNameOrBrand) {
/*  917 */     if (machineType.equals("050")) {
/*      */       
/*  919 */       if (null != softwareNameOrBrand && !softwareNameOrBrand.isEmpty()) {
/*      */         
/*  921 */         param.put("SWNAME_OR_BRAND", softwareNameOrBrand);
/*  922 */         return this.accreditationDao.getAccredittedMachineModelList(param);
/*      */       } 
/*      */       
/*  925 */       return this.accreditationDao.getAccredittedMachineBrandList(param);
/*      */     } 
/*  927 */     if (machineType.equals("053")) {
/*      */       
/*  929 */       if (null != softwareNameOrBrand && !softwareNameOrBrand.isEmpty()) {
/*      */         
/*  931 */         param.put("SWNAME_OR_BRAND", softwareNameOrBrand);
/*  932 */         return this.accreditationDao.getAccredittedSoftwareVersionList(param);
/*      */       } 
/*      */       
/*  935 */       return this.accreditationDao.getAccredittedSoftwareNameList(param);
/*      */     } 
/*  937 */     if (machineType.equals("052")) {
/*      */       
/*  939 */       if (null != softwareNameOrBrand && !softwareNameOrBrand.isEmpty()) {
/*      */         
/*  941 */         param.put("SWNAME_OR_BRAND", softwareNameOrBrand);
/*  942 */         return this.accreditationDao.getAccredittedSoftwareVersionList(param);
/*      */       } 
/*      */       
/*  945 */       return this.accreditationDao.getAccredittedSoftwareNameList(param);
/*      */     } 
/*      */     
/*  948 */     return null;
/*      */   }
/*      */ 
/*      */   
/*      */   public String getProductAccreditationNo(Map<String, Object> param) {
/*  953 */     return this.accreditationDao.getProductAccreditationNo(param);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<UploadSoftwareValidationForm> getAllAccreditedSoftware(String businessTin) {
/*  959 */     return this.accreditationDao.getAllAccreditedSoftware(businessTin);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public UploadSoftwareValidationForm getAllAccreditedDetailByAccreditationNo(String accredCode) {
/*  965 */     return this.accreditationDao.getAllAccreditedDetailByAccreditationNo(accredCode);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public Integer isBusinessNameRegistered(Map<String, Object> paramMap) {
/*  971 */     return this.accreditationDao.checkBusinessNameRegistration(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<String> getAuthRepInfo(Business businessInfoFromITS, String transNo, boolean validRepsTIN) {
/*  977 */     SimpleDateFormat dateformat = new SimpleDateFormat("EEE MMM dd yyyy");
/*  978 */     String newIncDate = null;
/*  979 */     if (businessInfoFromITS.getIncDate() != null)
/*      */     {
/*  981 */       newIncDate = dateformat.format(businessInfoFromITS.getIncDate());
/*      */     }
/*      */ 
/*      */     
/*  985 */     List<String> ITSbusinessInfoArray = new ArrayList<String>();
/*  986 */     if (businessInfoFromITS != null && validRepsTIN) {
/*      */       
/*  988 */       ITSbusinessInfoArray.add(transNo);
/*  989 */       ITSbusinessInfoArray.add((((Branch)businessInfoFromITS.getBranchList().get(0)).getRdoCode() != null) ? ((Branch)businessInfoFromITS.getBranchList().get(0)).getRdoCode().getValue() : "");
/*  990 */       ITSbusinessInfoArray.add(businessInfoFromITS.getBusinessTIN());
/*  991 */       ITSbusinessInfoArray.add(((Branch)businessInfoFromITS.getBranchList().get(0)).getBranchCode());
/*  992 */       ITSbusinessInfoArray.add(businessInfoFromITS.getBusinessDesc().trim().replace("'", "\\'"));
/*  993 */       ITSbusinessInfoArray.add((businessInfoFromITS.getBusinessName() != null) ? businessInfoFromITS.getBusinessName().trim().replace("'", "\\'") : "");
/*  994 */       ITSbusinessInfoArray.add(businessInfoFromITS.getOwner().getFirstName().trim().replace("'", "\\'"));
/*  995 */       ITSbusinessInfoArray.add(businessInfoFromITS.getOwner().getMiddleName().trim().replace("'", "\\'"));
/*  996 */       ITSbusinessInfoArray.add(businessInfoFromITS.getOwner().getLastName().trim().replace("'", "\\'"));
/*  997 */       ITSbusinessInfoArray.add("");
/*      */ 
/*      */       
/* 1000 */       ITSbusinessInfoArray.add((newIncDate != null) ? newIncDate : "");
/* 1001 */       ITSbusinessInfoArray.add(businessInfoFromITS.getLineOfBusiness().getValue());
/*      */     } 
/*      */     
/* 1004 */     if (ITSbusinessInfoArray.size() < 1)
/*      */     {
/* 1006 */       for (int i = 0; i < 12; i++)
/*      */       {
/* 1008 */         ITSbusinessInfoArray.add("");
/*      */       }
/*      */     }
/*      */     
/* 1012 */     return ITSbusinessInfoArray;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void updateBusinessReps(Map<String, Object> param) {
/* 1018 */     this.businessDao.updateBusinessReps(param);
/*      */   }
/*      */ 
/*      */   
/*      */   public void setLetterOfRevocationReportCRMGenerator(ReportGenerator letterOfRevocationReportCRMGenerator) {
/* 1023 */     this.letterOfRevocationReportCRMGenerator = letterOfRevocationReportCRMGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setLetterOfRevocationReportPOSandOthersGenerator(ReportGenerator letterOfRevocationReportPOSandOthersGenerator) {
/* 1028 */     this.letterOfRevocationReportPOSandOthersGenerator = letterOfRevocationReportPOSandOthersGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setLetterOfRevocationReportSoftwareGenerator(ReportGenerator letterOfRevocationReportSoftwareGenerator) {
/* 1033 */     this.letterOfRevocationReportSoftwareGenerator = letterOfRevocationReportSoftwareGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportGenerator getLetterOfRevocationReportCRMGenerator() {
/* 1038 */     return this.letterOfRevocationReportCRMGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportGenerator getLetterOfRevocationReportPOSandOthersGenerator() {
/* 1043 */     return this.letterOfRevocationReportPOSandOthersGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public ReportGenerator getLetterOfRevocationReportSoftwareGenerator() {
/* 1048 */     return this.letterOfRevocationReportSoftwareGenerator;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setUserDao(UserDao userDao) {
/* 1053 */     this.userDao = userDao;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public ParameterizedObject getRRDescription(String rdoCode) {
/* 1059 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/* 1060 */     paramMap.put("RDO_CD", rdoCode);
/* 1061 */     return this.accreditationDao.getRRDescription(paramMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public void seteAccRegLink(String eAccRegLink) {
/* 1066 */     this.eAccRegLink = eAccRegLink;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setReferenceDao(ReferenceDao referenceDao) {
/* 1071 */     this.referenceDao = referenceDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public ReferenceDao getReferenceDao() {
/* 1076 */     return this.referenceDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setAccreditationCertificateGeneratorSO(ReportGenerator accreditationCertificateGeneratorSO) {
/* 1081 */     this.accreditationCertificateGeneratorSO = accreditationCertificateGeneratorSO;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<Map<String, Object>> getApprovedAccreditationList() {
/* 1087 */     List<Map<String, Object>> accreditationList = new ArrayList<Map<String, Object>>();
/* 1088 */     accreditationList = this.accreditationDao.getApprovedAccreditationList();
/* 1089 */     return accreditationList;
/*      */   }
/*      */ }


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\main\accreditation\AccreditationServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */