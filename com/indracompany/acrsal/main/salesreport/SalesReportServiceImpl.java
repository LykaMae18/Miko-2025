/*      */ package com.indracompany.acrsal.main.salesreport;
/*      */ 
/*      */ import com.indracompany.acrsal.api.salesreport.SalesReportService;
/*      */ import com.indracompany.acrsal.dao.business.BusinessDao;
/*      */ import com.indracompany.acrsal.dao.business.SequenceDao;
/*      */ import com.indracompany.acrsal.dao.business.UserDao;
/*      */ import com.indracompany.acrsal.dao.salesreport.SalesReportDao;
/*      */ import com.indracompany.acrsal.exception.NoRecordFoundException;
/*      */ import com.indracompany.acrsal.forms.UploadSalesReport;
/*      */ import com.indracompany.acrsal.model.registration.Machine;
/*      */ import com.indracompany.acrsal.model.sales.SalesReport;
/*      */ import com.indracompany.acrsal.models.AuditTrail;
/*      */ import com.indracompany.acrsal.models.AuthorizedUser;
/*      */ import com.indracompany.acrsal.models.Branch;
/*      */ import com.indracompany.acrsal.models.Business;
/*      */ import com.indracompany.acrsal.models.FileListFilter;
/*      */ import com.indracompany.acrsal.models.FullName;
/*      */ import com.indracompany.acrsal.models.ParameterizedObject;
/*      */ import com.indracompany.acrsal.models.SecurityProfile;
/*      */ import com.indracompany.core.mail.mailer.TemplateMailer;
/*      */ import com.indracompany.core.mail.model.EmailDetail;
/*      */ import com.indracompany.core.mail.model.MailAttachment;
/*      */ import com.indracompany.core.mail.receiver.MailReceiver;
/*      */ import com.indracompany.core.util.FTPFileConnection;
/*      */ import java.io.BufferedReader;
/*      */ import java.io.BufferedWriter;
/*      */ import java.io.File;
/*      */ import java.io.FileNotFoundException;
/*      */ import java.io.FileOutputStream;
/*      */ import java.io.FileReader;
/*      */ import java.io.FileWriter;
/*      */ import java.io.FilenameFilter;
/*      */ import java.io.IOException;
/*      */ import java.math.BigDecimal;
/*      */ import java.net.InetAddress;
/*      */ import java.net.UnknownHostException;
/*      */ import java.text.DateFormat;
/*      */ import java.text.ParseException;
/*      */ import java.text.SimpleDateFormat;
/*      */ import java.util.ArrayDeque;
/*      */ import java.util.ArrayList;
/*      */ import java.util.Arrays;
/*      */ import java.util.Calendar;
/*      */ import java.util.Collection;
/*      */ import java.util.Collections;
/*      */ import java.util.Date;
/*      */ import java.util.Deque;
/*      */ import java.util.HashMap;
/*      */ import java.util.HashSet;
/*      */ import java.util.Iterator;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Set;
/*      */ import java.util.regex.Matcher;
/*      */ import java.util.regex.Pattern;
/*      */ import org.apache.commons.io.FileUtils;
/*      */ import org.apache.commons.io.comparator.NameFileComparator;
/*      */ import org.apache.commons.lang.StringUtils;
/*      */ import org.apache.log4j.Logger;
/*      */ import org.springframework.security.GrantedAuthority;
/*      */ import org.springframework.security.GrantedAuthorityImpl;
/*      */ 
/*      */ 
/*      */ public class SalesReportServiceImpl
/*      */   implements SalesReportService
/*      */ {
/*      */   private static final long serialVersionUID = 1L;
/*   61 */   private final Logger logger = Logger.getLogger(SalesReportServiceImpl.class);
/*      */
/*      */   private static final Pattern NUMERIC_PATTERN = Pattern.compile("^[-+]?[0-9]*\\.?[0-9]+$");
/*      */
/*      */   private static final String SALESREPORT = "salesReport";
/*      */
/*      */   private static final int DEFAULT_SEQUENCE_BLOCK_SIZE = 32;
/*      */
/*      */   private static final String BUSINESS_TIN = "BUSINESS_TIN";
/*      */   
/*      */   private static final String USER_NAME = "USER_NAME";
/*      */   
/*      */   private static final String LOGIN_TYPE = "LOGIN_TYPE";
/*      */   
/*      */   private static final String BUSINESS = "BUSINESS";
/*      */   
/*      */   private static final String BRANCH = "BRANCH";
/*      */   
/*      */   private static final String MSG_EMAIL = " (via E-mail)";
/*      */
/*      */   private static final List<String> DEFAULT_MIN_STATUS_LIST = Arrays.asList(new String[] { "106", "105", "108" });
/*      */
/*      */   private SalesReportDao salesReportDao;
/*      */   
/*      */   private SequenceDao sequenceDao;
/*      */   private BusinessDao businessDao;
/*      */   private UserDao userDao;
/*      */   private String salesReportUpload;
/*      */   private String salesReportUploadProcess;
/*      */   private String salesReportUploadArchive;
/*      */   private String smsOutgoingArchive;
/*      */   private String salesSMSUpload;
/*      */   private String salesSMSProcess;
/*      */   private String salesSMSArchive;
/*      */   private String salesSMSFormat;
/*      */   private TemplateMailer salesUploadMailer;
/*      */   private FTPFileConnection birFtpFileConnection;
/*      */   private TemplateMailer salesReportUploadInvalidEmailMailer;
/*      */   private MailReceiver salesMailReceiver;
/*      */   private String sourceFolder;
/*      */   private String smsReplyTransactionFailed;
/*      */   private String smsReplyReportFormat;
/*      */   private String smsReplyUnauthorized;
/*      */
/*      */   private final ThreadLocal<SalesReportProcessingContext> processingContextHolder = new ThreadLocal<SalesReportProcessingContext>();
/*      */
/*      */   private final ThreadLocal<Map<String, Deque<Integer>>> sequencePoolHolder = new ThreadLocal<Map<String, Deque<Integer>>>();
/*      */
/*      */   private String generateSalesReportNumber(Business business, Branch branch) {
/*  100 */     return generateSalesReportNumber(business, branch, getNextSequenceValue("SRN"));
/*      */   }
/*      */
/*      */
/*      */   private String generateSalesReportNumber(Business business, Branch branch, int sequenceNumber) {
/*  104 */     SimpleDateFormat sdfDate = new SimpleDateFormat("MMddyyyy");
/*  105 */     SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");
/*  106 */     Date date = new Date();
/*  107 */     String strDate = sdfDate.format(date);
/*  108 */     String strTime = sdfTime.format(date);
/*      */
/*  110 */     String rdoCode = getRdoCodeFromITS(business.getBusinessTIN(), branch.getBranchCode());
/*      */
/*  112 */     return strDate + rdoCode + strTime + sequenceNumberFormatter(sequenceNumber);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private String generateSalesReportTransNumber() {
/*  120 */     SimpleDateFormat sdfDate = new SimpleDateFormat("MMddyyyy");
/*  121 */     SimpleDateFormat sdfTime = new SimpleDateFormat("HHmmss");
/*  122 */     Date date = new Date();
/*  123 */     String strDate = sdfDate.format(date);
/*  124 */     String strTime = sdfTime.format(date);
/*      */
/*  126 */     int transNumber = getNextSequenceValue("STN");
/*      */
/*  128 */     return "STN" + strDate + strTime + sequenceNumberFormatter(transNumber);
/*      */   }
/*      */
/*      */
/*      */   private int getNextSequenceValue(String sequenceCode) {
/*  132 */     SalesReportProcessingContext context = getProcessingContext();
/*  133 */     Map<String, Deque<Integer>> cache = (context != null) ? context.sequenceBlockCache : getOrCreateSequencePool();
/*  134 */     Deque<Integer> pool = cache.get(sequenceCode);
/*  135 */     if (pool == null) {
/*  136 */       pool = new ArrayDeque<Integer>();
/*  137 */       cache.put(sequenceCode, pool);
/*      */     }
/*  139 */     if (pool.isEmpty()) {
/*  140 */       int blockSize = (context != null) ? Math.max(1, context.sequencePrefetchSize) : DEFAULT_SEQUENCE_BLOCK_SIZE;
/*  141 */       List<Integer> reserved = reserveSequenceBlock(sequenceCode, blockSize);
/*  142 */       if (reserved != null && !reserved.isEmpty()) {
/*  143 */         for (Integer value : reserved) {
/*  144 */           pool.addLast(value);
/*      */         }
/*      */       }
/*      */     }
/*  148 */     Integer nextValue = pool.pollFirst();
/*  149 */     if (nextValue != null) {
/*  150 */       return nextValue.intValue();
/*      */     }
/*  152 */     return fetchNextSequenceValueDirect(sequenceCode);
/*      */   }
/*      */
/*      */
/*      */   private int fetchNextSequenceValueDirect(String sequenceCode) {
/*  156 */     Map<String, Object> transTemp = this.sequenceDao.getTransactionNo(sequenceCode);
/*  157 */     int transNumber = Integer.parseInt(transTemp.get("SEQUENCE_NUMBER").toString().trim());
/*  158 */     transNumber = (transNumber + 1 > 99999) ? 1 : (transNumber + 1);
/*  159 */     this.sequenceDao.updateTransactionNo(String.valueOf(transNumber), sequenceCode);
/*  160 */     return transNumber;
/*      */   }
/*      */
/*      */
/*      */   private List<Integer> reserveSequenceBlock(String sequenceCode, int size) {
/*  140 */     if (size <= 0) {
/*  141 */       return new ArrayList<Integer>(0);
/*      */     }
/*  143 */     List<Integer> reserved = null;
/*  144 */     if (this.sequenceDao instanceof SequenceDaoBlockSupport) {
/*  145 */       reserved = ((SequenceDaoBlockSupport)this.sequenceDao).getTransactionBlock(sequenceCode, size);
/*      */     }
/*  147 */     if (reserved != null && !reserved.isEmpty()) {
/*  148 */       return reserved;
/*      */     }
/*  150 */     Map<String, Object> transTemp = this.sequenceDao.getTransactionNo(sequenceCode);
/*  151 */     int current = Integer.parseInt(transTemp.get("SEQUENCE_NUMBER").toString().trim());
/*  152 */     reserved = new ArrayList<Integer>(size);
/*  153 */     for (int i = 0; i < size; i++) {
/*  154 */       current = (current + 1 > 99999) ? 1 : (current + 1);
/*  155 */       reserved.add(Integer.valueOf(current));
/*      */     }
/*  157 */     this.sequenceDao.updateTransactionNo(String.valueOf(current), sequenceCode);
/*  158 */     return reserved;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private String sequenceNumberFormatter(int sequence_number) {
/*  146 */     String cont = String.valueOf(sequence_number);
/*  147 */     String padValue = "0";
/*  148 */     StringBuilder resultContainer = new StringBuilder("");
/*      */     
/*  150 */     for (int x = cont.length(); x < 5; x++)
/*      */     {
/*  152 */       resultContainer.append(padValue);
/*      */     }
/*      */     
/*  155 */     resultContainer.append(cont);
/*      */     
/*  157 */     return resultContainer.toString();
/*      */   }
/*      */ 
/*      */
/*      */   private SalesReportProcessingContext getProcessingContext() {
/*  160 */     return (SalesReportProcessingContext)this.processingContextHolder.get();
/*      */   }
/*      */
/*      */   private Map<String, Deque<Integer>> getOrCreateSequencePool() {
/*  164 */     Map<String, Deque<Integer>> sequencePool = (Map<String, Deque<Integer>>)this.sequencePoolHolder.get();
/*  165 */     if (sequencePool == null) {
/*  166 */       sequencePool = new HashMap<String, Deque<Integer>>();
/*  167 */       this.sequencePoolHolder.set(sequencePool);
/*      */     }
/*  169 */     return sequencePool;
/*      */   }
/*      */
/*      */   private int getBusinessBranchCount(String tin, String branchCode) {
/*  164 */     SalesReportProcessingContext context = getProcessingContext();
/*  165 */     String key = tin + '|' + ((branchCode == null) ? "" : branchCode);
/*  166 */     Integer cached = null;
/*  167 */     if (context != null) {
/*  168 */       cached = (Integer)context.businessBranchCountCache.get(key);
/*  169 */       if (cached != null || context.businessBranchCountCache.containsKey(key)) {
/*  170 */         return (cached == null) ? 0 : cached.intValue();
/*      */       }
/*      */     }
/*  173 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/*  174 */     paramMap.put("BUSINESS_TIN", tin);
/*  175 */     if (branchCode != null) {
/*  176 */       paramMap.put("BRANCH_CODE", branchCode);
/*      */     }
/*  178 */     int count = this.businessDao.checkBusinessBranchFromITS(paramMap);
/*  179 */     if (context != null) {
/*  180 */       context.businessBranchCountCache.put(key, Integer.valueOf(count));
/*      */     }
/*  182 */     return count;
/*      */   }
/*      */
/*      */   private Business getBusinessDetailsOfTin(String tin) {
/*  186 */     SalesReportProcessingContext context = getProcessingContext();
/*  187 */     if (context != null) {
/*  188 */       Business cached = (Business)context.businessByTinCache.get(tin);
/*  189 */       if (cached != null || context.businessByTinCache.containsKey(tin)) {
/*  190 */         return cached;
/*      */       }
/*      */     }
/*  193 */     Map<String, Object> parameterMap = new HashMap<String, Object>();
/*  194 */     parameterMap.put("BUSINESS_TIN", tin);
/*  195 */     Business business = this.salesReportDao.getBusinessDetailsOfTINandBranch(parameterMap);
/*  196 */     if (context != null) {
/*  197 */       context.businessByTinCache.put(tin, business);
/*      */     }
/*  199 */     return business;
/*      */   }
/*      */
/*      */   private Business getBusinessDetailsOfTinAndBranch(String tin, String branch) {
/*  203 */     SalesReportProcessingContext context = getProcessingContext();
/*  204 */     String key = tin + '|' + branch;
/*  205 */     if (context != null) {
/*  206 */       Business cached = (Business)context.businessByTinBranchCache.get(key);
/*  207 */       if (cached != null || context.businessByTinBranchCache.containsKey(key)) {
/*  208 */         return cached;
/*      */       }
/*      */     }
/*  211 */     Map<String, Object> parameterMap = new HashMap<String, Object>();
/*  212 */     parameterMap.put("BUSINESS_TIN", tin);
/*  213 */     parameterMap.put("BRANCH_CODE", branch);
/*  214 */     Business business = this.salesReportDao.getBusinessDetailsOfTINandBranch(parameterMap);
/*  215 */     if (context != null) {
/*  216 */       context.businessByTinBranchCache.put(key, business);
/*      */     }
/*  218 */     return business;
/*      */   }
/*      */
/*      */   private Business getBusinessDetailsForUser(String userName) {
/*  222 */     SalesReportProcessingContext context = getProcessingContext();
/*  223 */     if (context != null) {
/*  224 */       Business cached = (Business)context.userBusinessCache.get(userName);
/*  225 */       if (cached != null || context.userBusinessCache.containsKey(userName)) {
/*  226 */         return cached;
/*      */       }
/*      */     }
/*  229 */     Map<String, Object> parameterMap = new HashMap<String, Object>();
/*  230 */     parameterMap.put("USER_NAME", userName);
/*  231 */     Business business = this.salesReportDao.getBusinessDetailsOfUser(parameterMap);
/*  232 */     if (context != null) {
/*  233 */       context.userBusinessCache.put(userName, business);
/*      */     }
/*  235 */     return business;
/*      */   }
/*      */
/*      */   private List<Branch> getBusinessBranchesForUser(String userName, String businessTin) {
/*  239 */     SalesReportProcessingContext context = getProcessingContext();
/*  240 */     String key = userName + '|' + businessTin;
/*  241 */     if (context != null && context.userBranchListCache.containsKey(key)) {
/*  242 */       return (List)context.userBranchListCache.get(key);
/*      */     }
/*  244 */     Map<String, Object> parameterMap = new HashMap<String, Object>();
/*  245 */     parameterMap.put("USER_NAME", userName);
/*  246 */     parameterMap.put("BUSINESS_TIN", businessTin);
/*  247 */     List<Branch> branchList = this.salesReportDao.getBusinessBranchesOfUser(parameterMap);
/*  248 */     if (context != null) {
/*  249 */       context.userBranchListCache.put(key, branchList);
/*      */     }
/*  251 */     return branchList;
/*      */   }
/*      */
/*      */   private boolean isTinAuthorizedForUser(String userName, String tin) {
/*  255 */     SalesReportProcessingContext context = getProcessingContext();
/*  256 */     String key = userName + '|' + tin;
/*  257 */     if (context != null) {
/*  258 */       Boolean cached = (Boolean)context.userTinAuthorizationCache.get(key);
/*  259 */       if (cached != null) {
/*  260 */         return cached.booleanValue();
/*      */       }
/*      */     }
/*  263 */     Business business = getBusinessDetailsForUser(userName);
/*  264 */     boolean result = (business != null && !tin.isEmpty() && tin.equals(business.getBusinessTIN()));
/*  265 */     if (context != null) {
/*  266 */       context.userTinAuthorizationCache.put(key, Boolean.valueOf(result));
/*      */     }
/*  268 */     return result;
/*      */   }
/*      */
/*      */   private boolean isBranchAuthorizedForUser(String userName, String branchCode, String businessTin) {
/*  272 */     SalesReportProcessingContext context = getProcessingContext();
/*  273 */     String key = userName + '|' + businessTin + '|' + branchCode;
/*  274 */     if (context != null) {
/*  275 */       Boolean cached = (Boolean)context.userBranchAuthorizationCache.get(key);
/*  276 */       if (cached != null) {
/*  277 */         return cached.booleanValue();
/*      */       }
/*      */     }
/*  280 */     List<Branch> branchList = getBusinessBranchesForUser(userName, businessTin);
/*  281 */     boolean result = false;
/*  282 */     if (branchList != null) {
/*  283 */       for (Branch branch : branchList) {
/*  284 */         if (branch.getBranchCode().equals("000") || branch.getBranchCode().equals(branchCode)) {
/*  285 */           result = true;
/*  286 */           break;
/*      */         }
/*      */       }
/*      */     }
/*  290 */     if (context != null) {
/*  291 */       context.userBranchAuthorizationCache.put(key, Boolean.valueOf(result));
/*      */     }
/*  293 */     return result;
/*      */   }
/*      */
/*      */   private boolean isValidMinForBusiness(String min, String businessTIN, String branchCode, String userName, String userBranchType, String mainBranchFlag) {
/*  297 */     SalesReportProcessingContext context = getProcessingContext();
/*  298 */     String key = min + '|' + businessTIN + '|' + branchCode + '|' + userName + '|' + userBranchType + '|' + mainBranchFlag;
/*  299 */     if (context != null) {
/*  300 */       Boolean cached = (Boolean)context.minBusinessValidationCache.get(key);
/*  301 */       if (cached != null) {
/*  302 */         return cached.booleanValue();
/*      */       }
/*      */     }
/*  305 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/*  306 */     paramMap.put("BUSINESS_TIN", businessTIN);
/*  307 */     if (userBranchType.toUpperCase().contains("SB")) {
/*  308 */       paramMap.put("BRANCH_CODE", branchCode);
/*  309 */       paramMap.put("MACHINE_ID", min);
/*      */     }
/*  311 */     paramMap.put("HEAD_OFFICE", mainBranchFlag);
/*  312 */     paramMap.put("USER_NAME", userName);
/*  313 */     int count = this.salesReportDao.checkIfValidMINForBusiness(paramMap).intValue();
/*  314 */     boolean result = (count > 0);
/*  315 */     if (context != null) {
/*  316 */       context.minBusinessValidationCache.put(key, Boolean.valueOf(result));
/*      */     }
/*  318 */     return result;
/*      */   }
/*      */
/*      */   private Date getEffectivityDateOfPermitCached(Map<String, Object> paramMap) {
/*  322 */     SalesReportProcessingContext context = getProcessingContext();
/*  323 */     if (context != null) {
/*  324 */       Object min = paramMap.get("MIN");
/*  325 */       Object statuses = paramMap.get("STATUS_LIST");
/*  326 */       if (min instanceof String && statuses instanceof List && DEFAULT_MIN_STATUS_LIST.equals(statuses)) {
/*  327 */         String key = (String)min;
/*  328 */         if (context.minEffectivityCache.containsKey(key)) {
/*  329 */           return (Date)context.minEffectivityCache.get(key);
/*      */         }
/*  331 */         if (context.minEffectivityNullCache.containsKey(key)) {
/*  332 */           return null;
/*      */         }
/*  334 */         Date result = this.salesReportDao.getEffectivityDateOfPermit(paramMap);
/*  335 */         if (result != null) {
/*  336 */           context.minEffectivityCache.put(key, result);
/*      */         }
/*      */         else {
/*  339 */           context.minEffectivityNullCache.put(key, Boolean.TRUE);
/*      */         }
/*  341 */         return result;
/*      */       }
/*      */     }
/*  344 */     return this.salesReportDao.getEffectivityDateOfPermit(paramMap);
/*      */   }
/*      */
/*      */   private Date getCancellationDateCached(String min) {
/*  348 */     SalesReportProcessingContext context = getProcessingContext();
/*  349 */     if (context != null) {
/*  350 */       if (context.minCancellationCache.containsKey(min)) {
/*  351 */         return (Date)context.minCancellationCache.get(min);
/*      */       }
/*  353 */       if (context.minCancellationNullCache.containsKey(min)) {
/*  354 */         return null;
/*      */       }
/*  356 */       Date result = this.salesReportDao.getCancellationDate(min);
/*  357 */       if (result != null) {
/*  358 */         context.minCancellationCache.put(min, result);
/*      */       }
/*      */       else {
/*  361 */         context.minCancellationNullCache.put(min, Boolean.TRUE);
/*      */       }
/*  363 */       return result;
/*     */     }
/*  365 */     return this.salesReportDao.getCancellationDate(min);
/*      */   }
/*      */
/*      */   private String getRdoCodeFromITS(String tin, String branchCode) {
/*  369 */     SalesReportProcessingContext context = getProcessingContext();
/*  370 */     String key = tin + '|' + branchCode;
/*  371 */     if (context != null) {
/*  372 */       if (context.rdoCodeCache.containsKey(key)) {
/*  373 */         return (String)context.rdoCodeCache.get(key);
/*      */       }
/*  375 */       String rdoCode = this.businessDao.getRDOCodeFromITS(tin, branchCode);
/*  376 */       context.rdoCodeCache.put(key, rdoCode);
/*  377 */       return rdoCode;
/*      */     }
/*  379 */     return this.businessDao.getRDOCodeFromITS(tin, branchCode);
/*      */   }
/*      */
/*      */   private void preloadUploadMetadata(List<String> lineContentList, String channelKey, String userName, String userBranchType, String userMainBranchCode) {
/*  382 */     SalesReportProcessingContext context = getProcessingContext();
/*  383 */     if (context == null || lineContentList == null || lineContentList.isEmpty()) {
/*  384 */       return;
/*      */     }
/*  386 */     Set<String> tinSet = new HashSet<String>();
/*  387 */     Set<String> tinBranchSet = new HashSet<String>();
/*  388 */     Set<String> minSet = new HashSet<String>();
/*  389 */     Set<String> minPeriodSet = new HashSet<String>();
/*  390 */     int lineNumber = 0;
/*  391 */     for (String lineContent : lineContentList) {
/*  392 */       if (lineNumber++ == 0) {
/*  393 */         continue;
/*      */       }
/*  395 */       if (lineContent == null || lineContent.trim().isEmpty()) {
/*  396 */         continue;
/*      */       }
/*  398 */       String[] splitContent = null;
/*  399 */       if ("045".equals(channelKey)) {
/*  400 */         splitContent = StringUtils.split(lineContent, ' ');
/*      */       }
/*      */       else {
/*  403 */         splitContent = StringUtils.split(lineContent, ',');
/*      */       }
/*  405 */       if (splitContent == null || splitContent.length != 10) {
/*  406 */         continue;
/*      */       }
/*  408 */       for (int idx = 0; idx < splitContent.length; idx++) {
/*  409 */         splitContent[idx] = (splitContent[idx] == null) ? "" : splitContent[idx].trim();
/*      */       }
/*  411 */       String tin = splitContent[0];
/*  412 */       String branch = splitContent[1];
/*  413 */       String monthString = splitContent[2];
/*  414 */       String yearString = splitContent[3];
/*  415 */       String min = splitContent[4];
/*  416 */       if (!StringUtils.isEmpty(tin)) {
/*  417 */         tinSet.add(tin);
/*  418 */         if (!StringUtils.isEmpty(branch)) {
/*  419 */           tinBranchSet.add(tin + '|' + branch);
/*      */         }
/*      */       }
/*  422 */       if (!StringUtils.isEmpty(min)) {
/*  423 */         minSet.add(min);
/*      */         try {
/*  425 */           int month = Integer.parseInt(monthString);
/*  426 */           int year = Integer.parseInt(yearString);
/*  427 */           if (month > 0 && year > 0) {
/*  428 */             minPeriodSet.add(buildMinPeriodKey(min, month, year));
/*      */           }
/*      */         }
/*  431 */         catch (NumberFormatException e) {
/*  432 */           this.logger.debug("Skipping MIN period preload due to invalid month/year: " + monthString + '/' + yearString);
/*      */         }
/*      */       }
/*      */     }
/*  436 */     for (String tin : tinSet) {
/*  437 */       getBusinessBranchCount(tin, null);
/*  438 */       getBusinessDetailsOfTin(tin);
/*  439 */       isTinAuthorizedForUser(userName, tin);
/*      */     }
/*  441 */     for (String key : tinBranchSet) {
/*  442 */       String[] keyParts = splitTinBranchKey(key);
/*  443 */       String tin = keyParts[0];
/*  444 */       String branch = keyParts[1];
/*  445 */       getBusinessBranchCount(tin, branch);
/*  446 */       getBusinessDetailsOfTinAndBranch(tin, branch);
/*  447 */       isBranchAuthorizedForUser(userName, branch, tin);
/*      */     }
/*  449 */     preloadMinCaches(minSet, minPeriodSet);
/*      */   }
/*      */
/*      */   private void preloadMinCaches(Set<String> minSet, Set<String> minPeriodSet) {
/*  453 */     if ((minSet == null || minSet.isEmpty()) && (minPeriodSet == null || minPeriodSet.isEmpty())) {
/*  454 */       return;
/*      */     }
/*  456 */     Map<String, Date> effectivityMap = fetchEffectivityDates(minSet);
/*  457 */     SalesReportProcessingContext context = getProcessingContext();
/*  458 */     if (context != null && effectivityMap != null) {
/*  459 */       for (String min : minSet) {
/*  460 */         if (effectivityMap.containsKey(min)) {
/*  461 */           Date effectivity = (Date)effectivityMap.get(min);
/*  462 */           if (effectivity != null) {
/*  463 */             context.minEffectivityCache.put(min, effectivity);
/*      */           }
/*      */           else {
/*  466 */             context.minEffectivityNullCache.put(min, Boolean.TRUE);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*  471 */     Map<String, Date> cancellationMap = fetchCancellationDates(minSet);
/*  472 */     if (context != null && cancellationMap != null) {
/*  473 */       for (String min : minSet) {
/*  474 */         if (cancellationMap.containsKey(min)) {
/*  475 */           Date cancellation = (Date)cancellationMap.get(min);
/*  476 */           if (cancellation != null) {
/*  477 */             context.minCancellationCache.put(min, cancellation);
/*      */           }
/*      */           else {
/*  480 */             context.minCancellationNullCache.put(min, Boolean.TRUE);
/*      */           }
/*      */         }
/*      */       }
/*      */     }
/*  485 */     if (minPeriodSet != null) {
/*  486 */       for (String minPeriodKey : minPeriodSet) {
/*  487 */         int separatorIndex = minPeriodKey.indexOf('|');
/*  488 */         int secondSeparatorIndex = minPeriodKey.indexOf('|', separatorIndex + 1);
/*  489 */         if (separatorIndex <= 0 || secondSeparatorIndex <= separatorIndex) {
/*  490 */           continue;
/*      */         }
/*  492 */         String min = minPeriodKey.substring(0, separatorIndex);
/*  493 */         String monthValue = minPeriodKey.substring(separatorIndex + 1, secondSeparatorIndex);
/*  494 */         String yearValue = minPeriodKey.substring(secondSeparatorIndex + 1);
/*      */         try {
/*  496 */           int month = Integer.parseInt(monthValue);
/*  497 */           int year = Integer.parseInt(yearValue);
/*  498 */           isMinEligibleForReporting(min, month, year);
/*  499 */           isMinNotCancelledForPeriod(min, month, year);
/*      */         }
/*  501 */         catch (NumberFormatException e) {
/*  502 */           this.logger.debug("Skipping MIN eligibility cache due to invalid month/year: " + monthValue + '/' + yearValue);
/*      */         }
/*      */       }
/*      */     }
/*      */   }
/*      */
/*      */   private Map<String, Date> fetchEffectivityDates(Collection<String> minSet) {
/*  509 */     if (minSet == null || minSet.isEmpty()) {
/*  510 */       return Collections.emptyMap();
/*      */     }
/*  512 */     if (this.salesReportDao instanceof SalesReportDaoBatchSupport) {
/*  513 */       Map<String, Date> batchResult = ((SalesReportDaoBatchSupport)this.salesReportDao).getEffectivityDatesOfPermits(minSet);
/*  514 */       return (batchResult != null) ? batchResult : Collections.<String, Date>emptyMap();
/*      */     }
/*  516 */     Map<String, Date> fallback = new HashMap<String, Date>();
/*  517 */     for (String min : minSet) {
/*  518 */       Map<String, Object> paramMap = new HashMap<String, Object>();
/*  519 */       paramMap.put("MIN", min);
/*  520 */       paramMap.put("STATUS_LIST", DEFAULT_MIN_STATUS_LIST);
/*  521 */       fallback.put(min, this.salesReportDao.getEffectivityDateOfPermit(paramMap));
/*      */     }
/*  523 */     return fallback;
/*      */   }
/*      */
/*      */   private Map<String, Date> fetchCancellationDates(Collection<String> minSet) {
/*  527 */     if (minSet == null || minSet.isEmpty()) {
/*  528 */       return Collections.emptyMap();
/*      */     }
/*  530 */     if (this.salesReportDao instanceof SalesReportDaoBatchSupport) {
/*  531 */       Map<String, Date> batchResult = ((SalesReportDaoBatchSupport)this.salesReportDao).getCancellationDates(minSet);
/*  532 */       return (batchResult != null) ? batchResult : Collections.<String, Date>emptyMap();
/*      */     }
/*  534 */     Map<String, Date> fallback = new HashMap<String, Date>();
/*  535 */     for (String min : minSet) {
/*  536 */       fallback.put(min, this.salesReportDao.getCancellationDate(min));
/*      */     }
/*  538 */     return fallback;
/*      */   }
/*      */
/*      */   private String[] splitTinBranchKey(String key) {
/*  542 */     String[] keyParts = new String[2];
/*  543 */     int separator = key.indexOf('|');
/*  544 */     if (separator < 0) {
/*  545 */       keyParts[0] = key;
/*  546 */       keyParts[1] = "";
/*      */     }
/*      */     else {
/*  549 */       keyParts[0] = key.substring(0, separator);
/*  550 */       keyParts[1] = key.substring(separator + 1);
/*      */     }
/*  552 */     return keyParts;
/*      */   }
/*      */
/*      */   private String buildMinPeriodKey(String min, int month, int year) {
/*  556 */     StringBuilder builder = new StringBuilder(min);
/*  557 */     builder.append('|').append(month).append('|').append(year);
/*  558 */     return builder.toString();
/*      */   }
/*      */
/*      */   private boolean isMinEligibleForReporting(String min, int month, int year) {
/*  562 */     SalesReportProcessingContext context = getProcessingContext();
/*  563 */     String key = buildMinPeriodKey(min, month, year);
/*  564 */     if (context != null && context.minReportingEligibilityCache.containsKey(key)) {
/*  565 */       return ((Boolean)context.minReportingEligibilityCache.get(key)).booleanValue();
/*      */     }
/*  567 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/*  568 */     paramMap.put("MIN", min);
/*  569 */     paramMap.put("STATUS_LIST", DEFAULT_MIN_STATUS_LIST);
/*  570 */     Date effectiveDateOfPermit = getEffectivityDateOfPermitCached(paramMap);
/*  571 */     boolean result = true;
/*  572 */     if (effectiveDateOfPermit == null) {
/*  573 */       result = false;
/*      */     }
/*  575 */     if (context != null) {
/*  576 */       context.minReportingEligibilityCache.put(key, Boolean.valueOf(result));
/*      */     }
/*  578 */     return result;
/*      */   }
/*      */
/*      */   private boolean isMinNotCancelledForPeriod(String min, int month, int year) {
/*  582 */     SalesReportProcessingContext context = getProcessingContext();
/*  583 */     String key = buildMinPeriodKey(min, month, year);
/*  584 */     if (context != null && context.minCancellationEligibilityCache.containsKey(key)) {
/*  585 */       return ((Boolean)context.minCancellationEligibilityCache.get(key)).booleanValue();
/*      */     }
/*  587 */     Date dateCancelled = getCancellationDate(min);
/*  588 */     boolean result = true;
/*  589 */     if (dateCancelled != null) {
/*  590 */       SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
/*  591 */       SimpleDateFormat sdfDay = new SimpleDateFormat("dd");
/*  592 */       SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
/*  593 */       int cancelDay = Integer.parseInt(sdfDay.format(dateCancelled));
/*  594 */       int cancelMonth = Integer.parseInt(sdfMonth.format(dateCancelled));
/*  595 */       int cancelYear = Integer.parseInt(sdfYear.format(dateCancelled));
/*  596 */       int validMonth = cancelMonth;
/*  597 */       int validYear = cancelYear;
/*  598 */       if (cancelDay == 1) {
/*  599 */         validMonth = cancelMonth - 1;
/*  600 */         if (cancelMonth == 1) {
/*  601 */           validMonth = 12;
/*  602 */           validYear = cancelYear - 1;
/*      */         }
/*      */       }
/*  605 */       if (validYear < year || (validYear == year && validMonth < month)) {
/*  606 */         result = false;
/*      */       }
/*      */     }
/*  609 */     if (context != null) {
/*  610 */       context.minCancellationEligibilityCache.put(key, Boolean.valueOf(result));
/*      */     }
/*  612 */     return result;
/*      */   }

/*      */   public List<ParameterizedObject> generateStatus(Date dateSubmitted, List<String> machineNoList, List<Date> salesDateList) {
/*  162 */     Calendar submissionCal = Calendar.getInstance();
/*  163 */     submissionCal.clear();
/*  164 */     submissionCal.setTime(dateSubmitted);
/*      */     
/*  166 */     int submissionDate = submissionCal.get(5);
/*  167 */     int submissionMonth = submissionCal.get(2) + 1;
/*  168 */     int submissionYear = submissionCal.get(1);
/*      */     
/*  170 */     int existingSales = 0;
/*  171 */     int deadLine = 10;
/*      */ 
/*      */     
/*  174 */     List<ParameterizedObject> salesStatusList = new ArrayList<ParameterizedObject>();
/*      */     
/*  176 */     for (int salesCtr = 0; salesCtr < machineNoList.size(); salesCtr++) {
/*      */       
/*  178 */       HashMap<String, Object> salesMap = new HashMap<String, Object>();
/*  179 */       salesMap.put("MACHINE_CD", machineNoList.get(salesCtr));
/*      */       
/*  181 */       ParameterizedObject pSalesStatus = new ParameterizedObject();
/*      */       
/*  183 */       Calendar salesCal = Calendar.getInstance();
/*  184 */       salesCal.setTime(salesDateList.get(salesCtr));
/*  185 */       int salesMonth = salesCal.get(2) + 1;
/*  186 */       int salesYear = salesCal.get(1);
/*      */       
/*  188 */       salesMap.put("SALES_MONTH", Integer.valueOf(salesMonth));
/*  189 */       salesMap.put("SALES_YEAR", Integer.valueOf(salesYear));
/*      */       
/*  191 */       existingSales = this.salesReportDao.checkIfAmended(salesMap);
/*      */       
/*  193 */       if (existingSales == 0) {
/*      */         
/*  195 */         if ((salesYear == submissionYear && (salesMonth == submissionMonth || (salesMonth + 1 == submissionMonth && submissionDate <= deadLine))) || (salesYear == submissionYear - 1 && salesMonth == submissionMonth + 11 && submissionDate <= deadLine))
/*      */         {
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*  206 */           pSalesStatus.setKey("048");
/*  207 */           pSalesStatus.setValue("SUBMITTED");
/*      */         }
/*      */         else
/*      */         {
/*  211 */           pSalesStatus.setKey("047");
/*  212 */           pSalesStatus.setValue("LATE");
/*      */         }
/*      */       
/*      */       } else {
/*      */         
/*  217 */         pSalesStatus.setKey("046");
/*  218 */         pSalesStatus.setValue("AMENDED");
/*      */       } 
/*      */       
/*  221 */       this.logger.info(" pSalesStatus : " + pSalesStatus.getValue());
/*      */       
/*  223 */       salesStatusList.add(pSalesStatus);
/*      */     } 
/*  225 */     return salesStatusList;
/*      */   }
/*      */ 
/*      */   
/*      */   public Machine getSalesReportBySRN(String salesReportNumber) {
/*  230 */     return this.salesReportDao.getSalesReportBySRN(salesReportNumber);
/*      */   }
/*      */ 
/*      */   
/*      */   public void addSalesReport(Map<String, Object> mapContainer) {
/*  235 */     Business business = (Business)mapContainer.get("BUSINESS");
/*  236 */     Branch branch = (Branch)mapContainer.get("BRANCH");
/*      */
/*  238 */     String rdoCode = getRdoCodeFromITS(business.getBusinessTIN(), branch.getBranchCode());
/*      */
/*  240 */     SalesReport salesReport = (SalesReport)mapContainer.get("salesReport");
/*  241 */     salesReport.setTransactionNo("");
/*  242 */     salesReport.setSalesReportNumber(generateSalesReportNumber(business, branch));
/*  243 */     salesReport.setRdoCode(rdoCode);
/*  244 */     mapContainer.put("salesReport", salesReport);
/*  245 */     mapContainer.put("TRANS_TYPE", "040");
/*  246 */     this.salesReportDao.insertSalesReportToMain(mapContainer);
/*  247 */     this.salesReportDao.insertSalesReportToHist(mapContainer);
/*      */   }
/*      */ 
/*      */   
/*      */   public List<Map<String, Object>> batchInsertOfSalesReport(List<Map<String, Object>> dbParameterList) {
/*  252 */     List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
/*  253 */     List<Integer> reservedSequence = (dbParameterList.size() > 1) ? reserveSequenceBlock("SRN", dbParameterList.size()) : Collections.emptyList();
/*  254 */     Iterator<Integer> sequenceIterator = reservedSequence.iterator();
/*      */
/*  256 */     for (Map<String, Object> temp : dbParameterList) {
/*      */
/*  258 */       Business business = (Business)temp.get("business");
/*  259 */       Branch branch = (Branch)temp.get("branch");
/*  260 */       SalesReport salesReport = (SalesReport)temp.get("salesReport");
/*      */
/*  262 */       int sequenceNumber = sequenceIterator.hasNext() ? ((Integer)sequenceIterator.next()).intValue() : getNextSequenceValue("SRN");
/*  263 */       salesReport.setSalesReportNumber(generateSalesReportNumber(business, branch, sequenceNumber));
/*      */
/*  265 */       String rdoCode = salesReport.getRdoCode();
/*  266 */       if (StringUtils.isEmpty(rdoCode)) {
/*  267 */         Object cachedRdo = temp.get("rdoCode");
/*  268 */         if (cachedRdo instanceof String) {
/*  269 */           rdoCode = (String)cachedRdo;
/*      */         }
/*      */       }
/*  272 */       if (StringUtils.isEmpty(rdoCode)) {
/*  273 */         rdoCode = getRdoCodeFromITS(business.getBusinessTIN(), branch.getBranchCode());
/*      */       }
/*  275 */       salesReport.setRdoCode(rdoCode);
/*  276 */       temp.put("rdoCode", rdoCode);
/*  277 */       temp.put("salesReport", salesReport);
/*  271 */       temp.put("TRANS_TYPE", "040");
/*  272 */       this.salesReportDao.insertSalesReportToMain(temp);
/*  273 */       this.salesReportDao.insertSalesReportToHist(temp);
/*  274 */       resultList.add(temp);
/*      */     }
/*      */
/*  277 */     return resultList.isEmpty() ? null : resultList;
/*      */   }
/*      */ 
/*      */   
/*      */   public SequenceDao getSequenceDao() {
/*  277 */     return this.sequenceDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSequenceDao(SequenceDao sequenceDao) {
/*  282 */     this.sequenceDao = sequenceDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public BusinessDao getBusinessDao() {
/*  287 */     return this.businessDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setBusinessDao(BusinessDao businessDao) {
/*  292 */     this.businessDao = businessDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public SalesReportDao getSalesReportDao() {
/*  297 */     return this.salesReportDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSalesReportDao(SalesReportDao salesReportDao) {
/*  302 */     this.salesReportDao = salesReportDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public List<Machine> getSalesReportList(Map<String, Object> mapContainer) throws NoRecordFoundException {
/*  307 */     List<Machine> checkList = this.salesReportDao.getSalesReportList(mapContainer);
/*  308 */     if (checkList.size() > 0)
/*      */     {
/*  310 */       return checkList;
/*      */     }
/*      */ 
/*      */     
/*  314 */     throw new NoRecordFoundException("No Record Found");
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   public List<Business> getSalesReportListByCompany(Map<String, Object> mapContainer) throws NoRecordFoundException {
/*  322 */     List<Business> checkList = this.salesReportDao.getSalesReportListByCompany(mapContainer);
/*      */     
/*  324 */     if (checkList.size() > 0)
/*      */     {
/*  326 */       return checkList;
/*      */     }
/*      */ 
/*      */     
/*  330 */     throw new NoRecordFoundException("No Record Found");
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public Business getBusinessDetails(String tin, String branchCode) {
/*  336 */     Map<String, Object> paramMap = new HashMap<String, Object>();
/*  337 */     paramMap.put("BUSINESS_TIN", tin);
/*  338 */     paramMap.put("BRANCH_CODE", branchCode);
/*      */     
/*  340 */     return this.salesReportDao.getBusinessDetails(paramMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<String> getRDO() {
/*  346 */     return this.salesReportDao.getRDO();
/*      */   }
/*      */ 
/*      */   
/*      */   public List<ParameterizedObject> getRDOWithDesc(Map<String, Object> paramMap) {
/*  351 */     return this.businessDao.getBIRRDOAccessByUserNameAndLoginType(paramMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public List<Branch> getBranchListOfSales(AuthorizedUser authorizedUser) {
/*  356 */     Map<String, Object> paraMap = new HashMap<String, Object>();
/*  357 */     paraMap.put("USER", authorizedUser);
/*      */     
/*  359 */     return this.salesReportDao.getBranchListOfSales(paraMap);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public List<Map<String, Object>> getMachineListByUser(String username, String loginType, String businessTin, String businessBranchCode) {
/*  365 */     Map<String, Object> param = new HashMap<String, Object>();
/*  366 */     param.put("USER_NAME", username);
/*  367 */     param.put("LOGIN_TYPE", loginType);
/*  368 */     param.put("BUSINESS_TIN", businessTin);
/*  369 */     param.put("BRANCH_CD", businessBranchCode);
/*      */     
/*  371 */     return this.salesReportDao.getMachineList(param);
/*      */   }
/*      */ 
/*      */   
/*      */   public Date getCancellationDate(String min) {
/*  376 */     return getCancellationDateCached(min);
/*      */   }
/*      */ 
/*      */   
/*      */   public List<Map<String, Object>> getInitMachineListByUser(String username, String loginType, String businessTin, String branchCode) {
/*  381 */     Map<String, Object> param = new HashMap<String, Object>();
/*  382 */     param.put("USER_NAME", username);
/*  383 */     param.put("LOGIN_TYPE", loginType);
/*  384 */     param.put("BUSINESS_TIN", businessTin);
/*  385 */     param.put("BRANCH_CODE", branchCode);
/*      */     
/*  387 */     return this.salesReportDao.getInitMachineList(param);
/*      */   }
/*      */ 
/*      */   
/*      */   public String uploadSalesReport(byte[] transfer, String fileName, String userName, String branchType) throws IOException {
/*  392 */     this.logger.debug("TRACE : PASSED fileName : " + fileName);
/*  393 */     int fileCount = 1;
/*  394 */     Date dateToday = new Date();
/*  395 */     SimpleDateFormat sdfDate = new SimpleDateFormat("MMddyyyy");
/*  396 */     String dateInString = sdfDate.format(dateToday);
/*  397 */     String pathHolder = basePathConfigurer(this.salesReportUpload + File.separator);
/*  398 */     String transNumber = generateSalesReportTransNumber();
/*      */     
/*      */     while (true) {
/*  401 */       File initialName = new File(pathHolder + dateInString + "_" + userName + "_" + "043" + "_" + transNumber + "_" + fileCount + "_" + branchType + ".csv");
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
/*  413 */       if (!initialName.exists()) {
/*      */ 
/*      */         
/*  416 */         FileOutputStream fileNameStreamer = new FileOutputStream(new File(pathHolder + dateInString + "_" + userName + "_" + "043" + "_" + transNumber + "_" + fileCount + "_" + branchType + ".csv"));
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
/*  429 */         this.logger.debug("TRACE : TO WRITE fileName : " + pathHolder + dateInString + "_" + userName + "_" + "043" + "_" + transNumber + "_" + fileCount + "_" + branchType + ".csv");
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
/*  442 */         fileNameStreamer.write(transfer);
/*  443 */         fileNameStreamer.close();
/*      */         
/*      */         break;
/*      */       } 
/*  447 */       fileCount++;
/*      */     } 
/*  449 */     return transNumber;
/*      */   }
/*      */ 
/*      */   
/*      */   public String getSalesReportUpload() {
/*  454 */     return this.salesReportUpload;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSalesReportUpload(String salesReportUpload) {
/*  459 */     this.salesReportUpload = salesReportUpload;
/*      */   }
/*      */ 
/*      */   
/*      */   private String basePathConfigurer(String basePath) {
/*  464 */     StringBuilder pathContainer = new StringBuilder("");
/*  465 */     char[] c = basePath.toCharArray();
/*  466 */     boolean isUnixTag = false;
/*      */     
/*  468 */     for (char temp : c) {
/*      */       
/*  470 */       if (temp == '/')
/*      */       {
/*  472 */         isUnixTag = true;
/*      */       }
/*      */     } 
/*      */     
/*  476 */     if (isUnixTag) {
/*      */       
/*  478 */       pathContainer.append(basePath.replace("/", File.separator));
/*      */     }
/*      */     else {
/*      */       
/*  482 */       pathContainer.append(basePath.replace("\\", "\\\\"));
/*      */     } 
/*      */     
/*  485 */     return pathContainer.toString();
/*      */   }
/*      */ 
/*      */   
/*      */   public File getFileToProcess() {
/*  490 */     File srcFile = null;
/*  491 */     File destFile = null;
/*      */     
/*  493 */     String sourcePathHolder = basePathConfigurer(this.salesReportUpload + File.separator);
/*  494 */     String processPathHolder = basePathConfigurer(this.salesReportUploadProcess + File.separator);
/*      */     
/*  496 */     File sourceDirectory = new File(sourcePathHolder);
/*  497 */     File processDirectory = new File(processPathHolder);
/*  498 */     FileListFilter fileListFilter = new FileListFilter("csv");
/*      */     
/*  500 */     if (processDirectory.exists() && (processDirectory.listFiles((FilenameFilter)fileListFilter)).length > 0) {
/*      */       
/*  502 */       File[] dirContents = processDirectory.listFiles((FilenameFilter)fileListFilter);
/*  503 */       Arrays.sort(dirContents, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
/*  504 */       return dirContents[0];
/*      */     } 
/*      */     
/*  507 */     if (sourceDirectory.exists()) {
/*      */       
/*  509 */       File[] dirContents = sourceDirectory.listFiles((FilenameFilter)fileListFilter);
/*  510 */       Arrays.sort(dirContents, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
/*  511 */       if (dirContents != null && dirContents.length > 0) {
/*      */         
/*  513 */         srcFile = new File(dirContents[0].getAbsolutePath());
/*  514 */         String destPath = basePathConfigurer(processPathHolder + srcFile.getName());
/*  515 */         destFile = new File(destPath);
/*      */ 
/*      */         
/*      */         try {
/*  519 */           FileUtils.moveFile(srcFile, destFile);
/*      */         }
/*  521 */         catch (IOException e) {
/*      */           
/*  523 */           this.logger.error(e.getMessage());
/*      */         } 
/*      */       } 
/*      */     } 
/*  527 */     return destFile;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void processUploadFile() {
/*  533 */     this.logger.info("BOIR-2878 TRACE : processUploadFile I");
/*  534 */     String archivePathHolder = basePathConfigurer(this.salesReportUploadArchive + File.separator);
/*      */     
/*  536 */     this.logger.info("BOIR-BOIR-3026 TRACE : BEFORE getFileToProcess : ");
/*  537 */     File fileToProcess = getFileToProcess();
/*  538 */     this.logger.info("BOIR-fBOIR-3026 TRACE : fileToProcess I: " + fileToProcess);
/*  539 */     if (fileToProcess != null) {
/*      */       
/*  541 */       String destPath = basePathConfigurer(archivePathHolder + fileToProcess.getName());
/*  542 */       File destFile = new File(destPath);
/*  543 */       this.logger.info("BOIR-2878 TRACE : PROCESSING I: " + fileToProcess.getName());
/*  544 */       if (fileToProcess.getName().matches("\\d+[_].+[_].+[_].+"))
/*      */       {
/*  546 */         processUploadSales(fileToProcess);
/*      */       }
/*      */       
/*  549 */       if (destFile.exists())
/*      */       {
/*  551 */         destFile = renameFile(destFile, archivePathHolder, ".csv");
/*      */       }
/*      */ 
/*      */       
/*      */       try {
/*  556 */         FileUtils.moveFile(fileToProcess, destFile);
/*      */       }
/*  558 */       catch (IOException e) {
/*      */         
/*  560 */         this.logger.error(e.getMessage());
/*      */       } 
/*  562 */       this.logger.info("BOIR-2878 TRACE : PROCESSING F: " + fileToProcess.getName());
/*      */     } 
/*  564 */     this.logger.info("BOIR-2878 TRACE : processUploadFile F");
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean isUserHaveMain(String userName) {
/*  569 */     Map<String, Object> parameterMap = new HashMap<String, Object>();
/*  570 */     parameterMap.put("USER_NAME", userName);
/*  571 */     parameterMap.put("BRANCH_CODE", "000");
/*      */     
/*  573 */     Business business = this.salesReportDao.getBusinessDetailsOfUser(parameterMap);
/*  574 */     return (business != null);
/*      */   }
/*      */ 
/*      */   
/*      */   private void processUploadSales(File file) {
/*  579 */     String filename = file.getName();
/*  580 */     String[] fileNameContent = StringUtils.split(filename, '_');
/*  581 */     String userName = fileNameContent[1];
/*  582 */     String dateSubmittedInString = fileNameContent[0];
/*  583 */     String channelKey = fileNameContent[2];
/*  584 */     String transNumber = "";
/*  585 */     String userBranchType = "";
/*      */     
/*  587 */     String userMainBranchCode = "";
/*      */     
/*  589 */     if (channelKey.equals("043")) {
/*      */       
/*  591 */       transNumber = fileNameContent[3];
/*  592 */       userBranchType = fileNameContent[5].toUpperCase().contains("MB") ? "MB" : "SB";
/*      */     }
/*      */     else {
/*      */       
/*  596 */       userBranchType = isUserHaveMain(userName) ? "MB" : "SB";
/*      */     } 
/*      */     
/*  599 */     if (channelKey.equals("044"))
/*      */     {
/*  601 */       transNumber = generateSalesReportTransNumber();
/*      */     }
/*      */     
/*  604 */     DateFormat formatter = new SimpleDateFormat("MMddyyyy");
/*  605 */     Date dateSubmitted = null;
/*      */     
/*  607 */     AuthorizedUser user = getUserDetails(userName);
/*  608 */     this.logger.debug("BOIR-2878 TRACE : USERNAME FROM FILE NAME : " + user);
/*  609 */     this.logger.debug("BOIR-2878 TRACE : userBranchType : " + userBranchType);
/*      */     
/*      */     try {
/*  612 */       dateSubmitted = formatter.parse(dateSubmittedInString);
/*      */     }
/*  614 */     catch (ParseException e1) {
/*      */       
/*  616 */       this.logger.error(e1.getMessage());
/*      */     } 
/*      */     
/*  619 */     if (user != null) {
/*      */
/*  621 */       SalesReportProcessingContext processingContext = new SalesReportProcessingContext();
/*  622 */       this.processingContextHolder.set(processingContext);
/*      */       try {
/*  624 */         String password = "DUMMY_PASSWORD";
/*      */       
/*  623 */       int lineNumber = 0;
/*      */       
/*  625 */       Map<String, Object> dbParameter = null;
/*      */       
/*  627 */       List<Map<String, Object>> uploadedRecordList = new ArrayList<Map<String, Object>>();
/*      */       
/*  629 */       ParameterizedObject loginType = new ParameterizedObject();
/*  630 */       loginType.setKey("003");
/*      */       
/*  632 */       this.logger.info("Getting branch list for username: " + user.getSecurityProfile().getUsername());
/*  633 */       Map<String, Object> paramMap = new HashMap<String, Object>();
/*  634 */       paramMap.put("USER", user);
/*  635 */       List<Branch> userBranchList = this.userDao.getBranchListOfUser(paramMap);
/*      */       
/*  637 */       this.logger.info("Branches for user: " + userBranchList.toString());
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*  645 */       for (Branch currentBranch : userBranchList) {
/*  646 */         this.logger.info("Current branch code: " + currentBranch.getBranchCode());
/*  647 */         if (currentBranch.getBranchCode().equals("000")) {
/*  648 */           userMainBranchCode = "000";
/*  649 */           this.logger.info("User is head office: " + userMainBranchCode);
/*      */           
/*      */           break;
/*      */         } 
/*      */       } 
/*  654 */       List<UploadSalesReport> recordList = new ArrayList<UploadSalesReport>();
/*      */       
/*      */       try {
/*  657 */         List<String> lineContentList = FileUtils.readLines(file);
/*  658 */         boolean withErrorInColum = false;
/*  659 */         if (lineContentList.size() > 0)
/*      */         {
/*  661 */           preloadUploadMetadata(lineContentList, channelKey, userName, userBranchType, userMainBranchCode);
/*  662 */           for (String lineContent : lineContentList) {
/*      */ 
/*      */             
/*  664 */             List<Map<String, Object>> dbParameterList = new ArrayList<Map<String, Object>>();
/*  665 */             UploadSalesReport uploadRecord = new UploadSalesReport();
/*  666 */             if (lineNumber <= 0) {
/*      */               
/*  668 */               if (lineNumber == 0) {
/*      */                 
/*  670 */                 if (lineContent.trim().equals("")) {
/*      */                   
/*  672 */                   withErrorInColum = true;
/*      */                 }
/*      */                 else {
/*      */                   
/*  676 */                   String[] splitContent = null;
/*  677 */                   if (channelKey.equals("045")) {
/*      */                     
/*  679 */                     splitContent = StringUtils.split(lineContent, ' ');
/*      */                   }
/*      */                   else {
/*      */                     
/*  683 */                     splitContent = StringUtils.split(lineContent, ',');
/*      */                   } 
/*  685 */                   if (splitContent.length != 10) {
/*      */                     
/*  687 */                     withErrorInColum = true;
/*      */                   }
/*      */                   else {
/*      */                     
/*  691 */                     withErrorInColum = !lineContent.trim().equalsIgnoreCase("TIN,Branch,Month,Year,MIN,Last OR,Vatable Sales,Vat Zero Rated Sales,Vat Exempt Sales,Sales Subject to other percentage taxes");
/*      */                   } 
/*      */                 } 
/*      */ 
/*      */                 
/*  696 */                 if (withErrorInColum) {
/*      */                   
/*  698 */                   this.logger.info("ERROR IN TEMPLATE COLUMNS");
/*      */                   break;
/*      */                 } 
/*      */               } 
/*  702 */               lineNumber++;
/*      */               
/*      */               continue;
/*      */             } 
/*  706 */             uploadRecord = processSalesReport(lineContent, fileNameContent[1], "043", userBranchType, userMainBranchCode);
/*  707 */             uploadRecord.setDateSubmitted(dateSubmitted);
/*      */             
/*  709 */             if (uploadRecord.isValidRecord()) {
/*      */               
/*  711 */               Machine machine = new Machine();
/*  712 */               machine = convertToSalesReport(uploadRecord, channelKey);
/*      */               
/*  714 */               SalesReport salesReport = machine.getSalesReportList().get(0);
/*      */               
/*  716 */               salesReport.setTransactionNo(transNumber);
/*      */               
/*  718 */               Business business = new Business();
/*  719 */               business.setBusinessTIN(uploadRecord.getBusinessTIN());
/*      */               
/*  721 */               Branch branch = new Branch();
/*  722 */               branch.setBranchCode(uploadRecord.getBranchCode());
/*      */               
/*  724 */               AuditTrail auditTrail = new AuditTrail();
/*  725 */               auditTrail.setDateStamp(new Date());
/*      */               
/*  727 */               SecurityProfile secProfile = new SecurityProfile(userName, password, (GrantedAuthority[])new GrantedAuthorityImpl[] { new GrantedAuthorityImpl("ROLE_ANONYMOUS") });
/*      */ 
/*      */ 
/*      */ 
/*      */               
/*      */               try {
/*  733 */                 InetAddress ipAddress = InetAddress.getLocalHost();
/*  734 */                 auditTrail.setIp(ipAddress.getHostAddress());
/*      */               }
/*  736 */               catch (UnknownHostException e) {
/*      */                 
/*  738 */                 this.logger.error(e.getMessage());
/*      */               } 
/*      */               
/*  741 */               auditTrail.setLoginType(loginType);
/*  742 */               auditTrail.setUserSecurity(secProfile);
/*      */               
/*  744 */               dbParameter = new HashMap<String, Object>();
/*  745 */               dbParameter.put("salesReport", salesReport);
/*  746 */               dbParameter.put("machine", machine);
/*  747 */               dbParameter.put("auditTrail", auditTrail);
/*  748 */               dbParameter.put("business", business);
/*  749 */               dbParameter.put("branch", branch);
/*  750 */               String rdoCode = salesReport.getRdoCode();
/*  751 */               if (StringUtils.isEmpty(rdoCode)) {
/*  752 */                 rdoCode = getRdoCodeFromITS(business.getBusinessTIN(), branch.getBranchCode());
/*  753 */                 salesReport.setRdoCode(rdoCode);
/*      */               }
/*  755 */               dbParameter.put("rdoCode", rdoCode);
/*  750 */               dbParameter.put("dateSubmitted", dateSubmitted);
/*  751 */               dbParameterList.add(dbParameter);
/*      */               
/*  753 */               uploadedRecordList = batchInsertOfSalesReport(dbParameterList);
/*  754 */               salesReport = (SalesReport)((Map)uploadedRecordList.get(0)).get("salesReport");
/*  755 */               uploadRecord.setSalesReportNumber(salesReport.getSalesReportNumber());
/*  756 */               uploadRecord.setSalesStatus(salesReport.getSalesStatus());
/*      */               
/*  758 */               ParameterizedObject pChannel = new ParameterizedObject();
/*  759 */               pChannel.setKey(channelKey);
/*  760 */               uploadRecord.setChannel(pChannel);
/*      */             } 
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
/*  785 */             uploadRecord.setLineNumber(lineNumber);
/*  786 */             recordList.add(uploadRecord);
/*  787 */             lineNumber++;
/*      */           } 
/*  789 */           sendEmailNotification(recordList, userName, transNumber, withErrorInColum ? 1 : 0);
/*      */         }
/*      */       
/*  792 */       } catch (IOException e1) {
/*      */
/*  794 */         this.logger.error(e1.getMessage());
/*      */       } finally {
/*  796 */         this.processingContextHolder.remove();
/*  797 */         this.sequencePoolHolder.remove();
/*      */       }
/*      */     }
/*      */   }
/*      */ 
/*      */   
/*      */   private SalesReport checkIfSameSalesReportExists(String machineID, SalesReport salesReport) {
/*  801 */     SalesReport similarSalesReport = null;
/*  802 */     Map<String, Object> map = new HashMap<String, Object>();
/*  803 */     map.put("MACHINE_CD", machineID);
/*  804 */     map.put("SALES_MONTH", Integer.valueOf(salesReport.getMonth()));
/*  805 */     map.put("SALES_YEAR", Integer.valueOf(salesReport.getYear()));
/*      */     
/*  807 */     int result = this.salesReportDao.checkIfAmended(map);
/*      */     
/*  809 */     if (result > 1) {
/*      */       
/*  811 */       Map<String, Object> paramMap = new HashMap<String, Object>();
/*  812 */       paramMap.put("SALES_REPORT", salesReport);
/*  813 */       paramMap.put("MIN", machineID);
/*  814 */       List<SalesReport> salesReportList = this.salesReportDao.getAmendmentsOfSalesReport(paramMap);
/*      */       
/*  816 */       if (sameSalesReport(salesReport, salesReportList.get(0)))
/*      */       {
/*  818 */         similarSalesReport = salesReportList.get(0);
/*      */       }
/*      */     } 
/*      */     
/*  822 */     return similarSalesReport;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private void sendEmailNotification(List<UploadSalesReport> processedRecordList, String userName, String transNumber, int validTemplate) {
/*  828 */     FullName fullName = this.userDao.getCompleteNameOfUser(userName, "003");
/*  829 */     AuthorizedUser user = getUserDetails(userName);
/*  830 */     Map<String, Object> mailMap = new HashMap<String, Object>();
/*  831 */     mailMap.put("userName", userName);
/*  832 */     mailMap.put("salesReportList", processedRecordList);
/*  833 */     mailMap.put("channel", (processedRecordList.size() != 0) ? ("044".equals((((UploadSalesReport)processedRecordList.get(0)).getChannel() != null) ? ((UploadSalesReport)processedRecordList.get(0)).getChannel().getKey() : "") ? " (via E-mail)" : "") : "");
/*      */ 
/*      */     
/*  836 */     mailMap.put("authorizedName", fullName.getLastName());
/*  837 */     mailMap.put("transNumber", (transNumber == null) ? "" : transNumber);
/*  838 */     mailMap.put("validTemplate", Integer.valueOf(validTemplate));
/*      */     
/*  840 */     if (!StringUtils.isEmpty(user.getContactDetail().getAlternateEmailAddress())) {
/*      */       
/*  842 */       String[] send = new String[2];
/*  843 */       send[0] = user.getContactDetail().getEmailAddress();
/*  844 */       send[1] = user.getContactDetail().getAlternateEmailAddress();
/*  845 */       this.salesUploadMailer.send(mailMap, send);
/*      */     }
/*      */     else {
/*      */       
/*  849 */       String[] send = new String[1];
/*  850 */       send[0] = user.getContactDetail().getEmailAddress();
/*  851 */       this.salesUploadMailer.send(mailMap, send);
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   private AuthorizedUser getUserDetails(String userName) {
/*  857 */     AuthorizedUser authorizedUser = new AuthorizedUser();
/*  858 */     SecurityProfile secProfile = new SecurityProfile(userName, "DUMMY_PASSWORD", (GrantedAuthority[])new GrantedAuthorityImpl[] { new GrantedAuthorityImpl("ROLE_ANONYMOUS") });
/*      */ 
/*      */ 
/*      */     
/*  862 */     authorizedUser.setSecurityProfile(secProfile);
/*      */     
/*  864 */     Map<String, Object> param = new HashMap<String, Object>();
/*  865 */     param.put("LOGIN_TYPE", "003");
/*  866 */     param.put("USER", authorizedUser);
/*      */     
/*  868 */     AuthorizedUser user = this.userDao.getUserFromMainByUserNameAndLoginType(param);
/*      */     
/*  870 */     return user;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private UploadSalesReport processSalesReport(String lineContent, String userName, String channel, String branchType, String mainBranchFlag) {
/*  876 */     this.logger.info("Reported user is head office: " + mainBranchFlag);
/*  877 */     String messageContent = lineContent.replaceAll(",", " , ");
/*      */     
/*  879 */     UploadSalesReport uploadSalesReport = new UploadSalesReport();
/*  880 */     BigDecimal totalSales = new BigDecimal("0.00");
/*      */     
/*  882 */     Date dateToday = new Date();
/*  883 */     Calendar calendarToday = Calendar.getInstance();
/*  884 */     calendarToday.clear();
/*  885 */     calendarToday.setTime(dateToday);
/*      */     
/*  887 */     List<String> errorMessageList = new ArrayList<String>();
/*  888 */     String[] splitContent = null;
/*  889 */     if (channel.equals("045")) {
/*      */
/*  891 */       splitContent = StringUtils.split(messageContent, ' ');
/*      */     }
/*      */     else {
/*      */
/*  895 */       splitContent = StringUtils.split(messageContent, ',');
/*      */     }
/*  897 */     for (int idx = 0; idx < splitContent.length; idx++) {
/*  898 */       splitContent[idx] = (splitContent[idx] == null) ? "" : splitContent[idx].trim();
/*      */     }
/*      */
/*  901 */     boolean result = true;
/*  899 */     boolean validDecimal = true;
/*  900 */     boolean validMonthAndYear = true;
/*  901 */     boolean isTinBlank = false;
/*  902 */     boolean isBranchBlank = false;
/*  903 */     boolean isMonthBlank = false;
/*  904 */     boolean isYearBlank = false;
/*  905 */     boolean isDecember = false;
/*  906 */     if (splitContent.length != 10) {
/*      */       
/*  908 */       this.logger.info("ERROR IN NUMBER OF COLUMNS");
/*  909 */       result = false;
/*  910 */       validDecimal = false;
/*  911 */       errorMessageList.add("Incorrect Format.");
/*      */     }
/*      */     else {
/*      */       
/*  915 */       final String tin = splitContent[0];
/*  916 */       final String branchCode = splitContent[1];
/*  917 */       final String monthValue = splitContent[2];
/*  918 */       final String yearValue = splitContent[3];
/*  919 */       final String min = splitContent[4];
/*      */       
/*  921 */       final String lastReceiptIssued = splitContent[5];
/*  922 */       final String vatableSales = splitContent[6];
/*  923 */       final String vatZeroRatedSales = splitContent[7];
/*  924 */       final String vatExemptSales = splitContent[8];
/*  925 */       final String otherTaxableSales = splitContent[9];
/*      */       
/*  927 */       uploadSalesReport.setBusinessTIN(tin);
/*  928 */       uploadSalesReport.setBranchCode(branchCode);
/*  929 */       uploadSalesReport.setMonthInString(monthValue);
/*  930 */       uploadSalesReport.setYearInString(yearValue);
/*  931 */       uploadSalesReport.setMIN(min);
/*      */       
/*  933 */       uploadSalesReport.setVatableSales(vatableSales);
/*  934 */       uploadSalesReport.setVatZeroRatedSales(vatZeroRatedSales);
/*  935 */       uploadSalesReport.setVatExemptSales(vatExemptSales);
/*  936 */       uploadSalesReport.setSalesSubjToOtherTax(otherTaxableSales);
/*  925 */       this.logger.info("VALIDATION : PROCESSING  : " + messageContent);
/*  926 */       for (int i = 0; i < splitContent.length; i++) {
/*      */         Map<String, Object> paramMap; boolean validMIN; int count;
/*      */         boolean validMINForReporting;
/*      */         boolean validMinNotCancelled;
/*      */
/*  932 */         switch (i) {
/*      */           
/*      */           case 0:
/*  937 */             this.logger.info("VALIDATION : CHECKING TIN");
/*  938 */             if (StringUtils.isEmpty(tin)) {
/*      */               
/*  940 */               this.logger.info("Tin is blank");
/*  941 */               result = false;
/*  942 */               errorMessageList.add("TIN is blank.");
/*  943 */               isTinBlank = true;
/*      */               
/*      */               
/*      */             } 
/*  947 */             if ("-".equalsIgnoreCase(tin)) {
/*  948 */               this.logger.info("INVALID TIN");
/*  949 */               result = false;
/*  950 */               errorMessageList.add("Please enter a valid TIN."); break;
/*      */             } 
/*  952 */             if (tin.length() != 9) {
/*      */               
/*  954 */               this.logger.info("ERROR IN TIN LENGTH");
/*  955 */               result = false;
/*  956 */               errorMessageList.add("User is not authorized to report for this TIN.");
/*      */               
/*      */               break;
/*      */             } 
/*  960 */             count = getBusinessBranchCount(tin, null);
/*  963 */             if (count <= 0) {
/*      */               
/*  965 */               this.logger.info("ERROR IN TIN : NOT FOUND");
/*  966 */               result = false;
/*  967 */               errorMessageList.add("User is not authorized to report for this TIN."); break;
/*      */             } 
/*  969 */             if (!checkIfValidTINForUser(userName, tin)) {
/*      */               
/*  971 */               this.logger.info("ERROR IN TIN");
/*  972 */               result = false;
/*  973 */               errorMessageList.add("User is not authorized to report for this TIN.");
/*      */             } 
/*      */             break;
/*      */ 
/*      */ 
/*      */           
/*      */           case 1:
/*  980 */             this.logger.info("VALIDATION : CHECKING BRANCH");
/*  981 */             if (StringUtils.isEmpty(branchCode)) {
/*      */               
/*  983 */               this.logger.info("ERROR IN BRANCH EMPTY");
/*  984 */               result = false;
/*  985 */               errorMessageList.add("Branch Code is blank.");
/*  986 */               isBranchBlank = true;
/*      */
/*      */               
/*      */             } 
/*  989 */             if (branchCode.length() < 3 || branchCode.length() > 5) {
/*      */               
/*  991 */               this.logger.info("ERROR IN BRANCH LENGTH");
/*  992 */               result = false;
/*  993 */               errorMessageList.add("Invalid Data/Format on Branch Code");
/*      */               
/*      */               break;
/*      */             } 
/*  997 */             count = getBusinessBranchCount(tin, branchCode);
/* 1001 */             if (count <= 0) {
/*      */               
/* 1003 */               this.logger.info("ERROR IN TIN AND BRANCH : NOT FOUND");
/* 1004 */               result = false;
/* 1005 */               errorMessageList.add("Please enter a valid Branch Code."); break;
/*      */             } 
/* 1007 */             if (!checkIfValidBranchForUser(userName, branchCode, tin)) {
/*      */               
/* 1009 */               this.logger.info("ERROR IN BRANCH");
/* 1010 */               result = false;
/*      */               
/* 1012 */               errorMessageList.add("Please enter a valid Branch Code.");
/*      */             } 
/*      */             break;
/*      */ 
/*      */ 
/*      */ 
/*      */           
/*      */           case 2:
/* 1020 */             this.logger.info("VALIDATION : CHECKING REPORT MONTH");
/* 1021 */             isDecember = false;
/* 1022 */             if (StringUtils.isEmpty(yearValue)) {
/*      */               
/* 1024 */               this.logger.info("ERROR IN MONTH YEAR IS BLANK");
/* 1025 */               result = false;
/* 1026 */               validMonthAndYear = false;
/* 1027 */               errorMessageList.add("Year is blank");
/* 1028 */               isYearBlank = true;
/*      */
/*      */               break;
/*      */             } 
/*      */             try {
/* 1032 */               int tmpYear = Integer.parseInt(yearValue.isEmpty() ? "0" : yearValue);
/* 1033 */               if ("12".equalsIgnoreCase(monthValue))
/* 1034 */                 isDecember = true; 
/* 1035 */               if (StringUtils.isEmpty(monthValue)) {
/*      */                 
/* 1037 */                 this.logger.info("ERROR IN MONTH");
/* 1038 */                 result = false;
/* 1039 */                 validMonthAndYear = false;
/* 1040 */                 errorMessageList.add("Month is blank");
/* 1041 */                 isMonthBlank = true;
/*      */                 
/*      */                 
/*      */               } 
/* 1045 */               if (!checkValidMonth(monthValue, tmpYear)) {
/*      */                 
/* 1047 */                 this.logger.info("ERROR IN MONTH");
/* 1048 */                 result = false;
/* 1049 */                 validMonthAndYear = false;
/* 1050 */                 errorMessageList.add("Month is invalid.");
/*      */                 
/*      */                 break;
/*      */               } 
/* 1054 */               uploadSalesReport.setMonth(Integer.parseInt(monthValue));
/*      */             
/*      */             }
/* 1057 */             catch (NumberFormatException e) {
/* 1058 */               this.logger.info("ERROR IN MONTH");
/* 1059 */               result = false;
/* 1060 */               validMonthAndYear = false;
/* 1061 */               errorMessageList.add("Month is invalid.");
/*      */             } 
/*      */             break;
/*      */           
/*      */           case 3:
/* 1066 */             this.logger.info("VALIDATION : CHECKING REPORT YEAR");
/* 1067 */             if (StringUtils.isEmpty(yearValue)) {
/*      */               
/* 1069 */               this.logger.info("ERROR IN YEAR");
/* 1070 */               result = false;
/* 1071 */               validMonthAndYear = false;
/*      */               
/* 1073 */               if (!errorMessageList.contains("Year is blank")) {
/* 1074 */                 errorMessageList.add("Year is blank");
/*      */               }
/* 1076 */               isYearBlank = true;
/*      */               
/*      */               break;
/*      */             } 
/* 1080 */             if (isReportYearValid(yearValue, isDecember)) {
/*      */ 
/*      */               
/*      */               try {
/* 1084 */                 uploadSalesReport.setYear(Integer.parseInt(yearValue));
/*      */               }
/* 1086 */               catch (NumberFormatException e) {
/*      */                 
/* 1088 */                 this.logger.info("ERROR IN YEAR: " + e.getMessage(), e);
/* 1089 */                 result = false;
/* 1090 */                 validMonthAndYear = false;
/* 1091 */                 errorMessageList.add("Year is invalid.");
/*      */               } 
/*      */               
/*      */               break;
/*      */             } 
/* 1096 */             this.logger.info("ERROR IN YEAR");
/* 1097 */             result = false;
/* 1098 */             validMonthAndYear = false;
/* 1099 */             errorMessageList.add("Year is invalid.");
/*      */             break;
/*      */ 
/*      */           
/*      */           case 4:
/* 1104 */             this.logger.info("VALIDATION : CHECKING MIN");
/* 1105 */             if (StringUtils.isEmpty(min)) {
/*      */               
/* 1107 */               this.logger.info("ERROR IN MIN");
/* 1108 */               result = false;
/* 1109 */               errorMessageList.add("MIN is blank");
/*      */               
/*      */               
/*      */             } 
/* 1113 */             validMIN = true;
/* 1114 */             validMINForReporting = true;
/* 1115 */             validMinNotCancelled = true;
/* 1116 */             if (!isTinBlank && !isBranchBlank && !isMonthBlank && !isYearBlank) {
/*      */               
/* 1118 */               if (min.isEmpty()) {
/*      */                 
/* 1120 */                 this.logger.info("ERROR IN MIN - MIN IS INVALID");
/* 1121 */                 result = false;
/* 1122 */                 errorMessageList.add("MIN is invalid.");
/*      */                 
/*      */                 break;
/*      */               } 
/* 1126 */               if ("-".equalsIgnoreCase(min)) {
/* 1127 */                 this.logger.info("INVALID MIN");
/* 1128 */                 result = false;
/* 1129 */                 errorMessageList.add("MIN is invalid.");
/*      */                 
/*      */                 break;
/*      */               } 
/* 1133 */               if (validMonthAndYear) {
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */                 
/* 1140 */                 this.logger.info("Processing Head Office: " + mainBranchFlag);
/* 1141 */                 validMIN = validMINForBusiness(uploadSalesReport.getMIN(), uploadSalesReport.getBusinessTIN(), uploadSalesReport.getBranchCode(), userName, branchType, mainBranchFlag);
/* 1142 */                 validMINForReporting = validMINForReporting(min, Integer.parseInt(monthValue), Integer.parseInt(yearValue));
/* 1143 */                 validMinNotCancelled = isMinNotCancelled(min, monthValue, yearValue);
/*      */               }
/*      */               else {
/*      */                 
/* 1147 */                 validMinNotCancelled = true;
/* 1148 */                 validMINForReporting = true;
/*      */               } 
/*      */               
/* 1151 */               if (!validMIN) {
/*      */                 
/* 1153 */                 this.logger.info("ERROR IN MIN - NOT ASSOCIATED TO USER AND BUSINESS");
/* 1154 */                 result = false;
/* 1155 */                 errorMessageList.add("User is not authorized to report for this MIN.");
/*      */               } 
/* 1157 */               this.logger.info("TEST : validMIN: " + validMIN + " validMINForReporting: " + validMINForReporting);
/* 1158 */               if (validMIN && !validMINForReporting) {
/*      */                 
/* 1160 */                 this.logger.info("ERROR IN MIN - NOT ELIGIBLE FOR SALES REPORTING: PERMIT NOT YET EFFECTIVE");
/* 1161 */                 result = false;
/* 1162 */                 errorMessageList.add("Sales report date is invalid. MIN/Permit to Use is not yet active.");
/*      */               } 
/*      */               
/* 1165 */               if (validMIN && validMINForReporting && !validMinNotCancelled) {
/*      */                 
/* 1167 */                 this.logger.info("ERROR IN MIN - NOT ELIGIBLE FOR SALES REPORTING: CANCELLED");
/* 1168 */                 result = false;
/* 1169 */                 errorMessageList.add("Sales report date is invalid. MIN/Permit to Use is not yet active.");
/*      */               } 
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case 5:
/* 1176 */             this.logger.info("VALIDATION : CHECKING LAST OR ISSUED");
/* 1177 */             if (StringUtils.isEmpty(lastReceiptIssued)) {
/*      */               
/* 1179 */               this.logger.info("ERROR IN FORMAT OF LAST OR ISSUED");
/* 1180 */               result = false;
/* 1181 */               errorMessageList.add("Last Receipt Issued is blank");
/*      */               
/*      */               break;
/*      */             } 
/* 1185 */             if (validORFormat(lastReceiptIssued)) {
/*      */               
/* 1187 */               String charChecker = lastReceiptIssued.substring(0, 1);
/* 1188 */               String receiptNo = parseReceipt(lastReceiptIssued);
/* 1189 */               if (charChecker.equalsIgnoreCase("O")) {
/*      */                 
/* 1191 */                 uploadSalesReport.setLastORNo(receiptNo); break;
/*      */               } 
/* 1193 */               if (charChecker.equalsIgnoreCase("T")) {
/*      */                 
/* 1195 */                 uploadSalesReport.setLastTransactionNo(receiptNo); break;
/*      */               } 
/* 1197 */               if (charChecker.equalsIgnoreCase("S")) {
/*      */                 
/* 1199 */                 String[] arrayOfString = StringUtils.split(receiptNo, ';');
/* 1200 */                 uploadSalesReport.setLastCashInvoiceNo(arrayOfString[0]);
/* 1201 */                 if (arrayOfString.length == 2)
/*      */                 {
/* 1203 */                   uploadSalesReport.setLastChargeInvoiceNo(arrayOfString[1]);
/*      */                 }
/*      */                 
/*      */                 break;
/*      */               } 
/* 1208 */               String[] invoiceContent = StringUtils.split(receiptNo, ';');
/* 1209 */               uploadSalesReport.setLastChargeInvoiceNo(invoiceContent[0]);
/* 1210 */               if (invoiceContent.length == 2)
/*      */               {
/* 1212 */                 uploadSalesReport.setLastCashInvoiceNo(invoiceContent[1]);
/*      */               }
/*      */               
/*      */               break;
/*      */             } 
/*      */             
/* 1218 */             this.logger.info("ERROR IN FORMAT OF LAST OR ISSUED");
/* 1219 */             result = false;
/* 1220 */             errorMessageList.add("Last Receipt issued is invalid.");
/*      */             break;
/*      */ 
/*      */           
/*      */           case 6:
/* 1225 */             this.logger.info("VALIDATION : CHECKING VATABLE SALES");
/* 1226 */             if (StringUtils.isEmpty(vatableSales)) {
/*      */               
/* 1228 */               this.logger.info("ERROR IN VATABLE SALES");
/* 1229 */               result = false;
/* 1230 */               validDecimal = false;
/* 1231 */               errorMessageList.add("Vatable Sales is blank"); break;
/*      */             } 
/* 1233 */             if ("-".equalsIgnoreCase(vatableSales)) {
/* 1234 */               this.logger.info("INVALID VATABLE SALES");
/* 1235 */               result = false;
/* 1236 */               errorMessageList.add("Vatable Sales is invalid.");
/*      */               
/*      */               break;
/*      */             } 
/* 1240 */             if (!isNumericValue(vatableSales) || !isNumericLengthValid(vatableSales)) {
/*      */               
/* 1242 */               this.logger.info("ERROR IN VATABLE SALES");
/* 1243 */               result = false;
/* 1244 */               validDecimal = false;
/* 1245 */               errorMessageList.add("Vatable Sales is invalid.");
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case 7:
/* 1251 */             this.logger.info("VALIDATION : CHECKING ZERO VATABLE SALES");
/* 1252 */             if (StringUtils.isEmpty(vatZeroRatedSales)) {
/*      */               
/* 1254 */               this.logger.info("ERROR IN ZERO VATABLE SALES");
/* 1255 */               result = false;
/* 1256 */               validDecimal = false;
/* 1257 */               errorMessageList.add("Vat Zero Rated Sales is blank"); break;
/*      */             } 
/* 1259 */             if ("-".equalsIgnoreCase(vatZeroRatedSales)) {
/* 1260 */               this.logger.info("INVALID ZERO VATABLE SALES");
/* 1261 */               result = false;
/* 1262 */               errorMessageList.add("Vat Zero-Rated Sales is invalid.");
/*      */               
/*      */               break;
/*      */             } 
/* 1266 */             if (!isNumericValue(vatZeroRatedSales) || !isNumericLengthValid(vatZeroRatedSales)) {
/*      */               
/* 1268 */               this.logger.info("ERROR IN VAT ZERO RATED SALES");
/* 1269 */               result = false;
/* 1270 */               validDecimal = false;
/* 1271 */               errorMessageList.add("Vat Zero-Rated Sales is invalid.");
/*      */             } 
/*      */             break;
/*      */ 
/*      */ 
/*      */           
/*      */           case 8:
/* 1278 */             this.logger.info("VALIDATION : CHECKING VATABLE EXCEMPT SALES");
/* 1279 */             if (StringUtils.isEmpty(vatExemptSales)) {
/*      */               
/* 1281 */               this.logger.info("ERROR IN VATABLE EXCEMPT SALES");
/* 1282 */               result = false;
/* 1283 */               validDecimal = false;
/* 1284 */               errorMessageList.add("Vat Exempt Sales is blank"); break;
/*      */             } 
/* 1286 */             if ("-".equalsIgnoreCase(vatExemptSales)) {
/* 1287 */               this.logger.info("INVALID VATABLE EXEMPT SALES");
/* 1288 */               result = false;
/* 1289 */               errorMessageList.add("Vat Exempt Sales is invalid.");
/*      */               
/*      */               break;
/*      */             } 
/* 1293 */             if (!isNumericValue(vatExemptSales) || !isNumericLengthValid(vatExemptSales)) {
/*      */               
/* 1295 */               this.logger.info("ERROR IN VAT EXEMPT");
/* 1296 */               result = false;
/* 1297 */               validDecimal = false;
/* 1298 */               errorMessageList.add("Vat Exempt Sales is invalid.");
/*      */             } 
/*      */             break;
/*      */ 
/*      */           
/*      */           case 9:
/* 1304 */             this.logger.info("VALIDATION : CHECKING OTHER TAXABLE SALES");
/* 1305 */             if (StringUtils.isEmpty(otherTaxableSales)) {
/*      */               
/* 1307 */               this.logger.info("ERROR IN OTHER TAXABLE SALES");
/* 1308 */               result = false;
/* 1309 */               validDecimal = false;
/* 1310 */               errorMessageList.add("Sales Subject to other Percentage tax is blank"); break;
/*      */             } 
/* 1312 */             if ("-".equalsIgnoreCase(otherTaxableSales)) {
/* 1313 */               this.logger.info("INVALID OTHER TAXABLE SALES");
/* 1314 */               result = false;
/* 1315 */               errorMessageList.add("Sales subject to other tax is invalid.");
/*      */               
/*      */               break;
/*      */             } 
/* 1319 */             if (!isNumericValue(otherTaxableSales) || !isNumericLengthValid(otherTaxableSales)) {
/*      */               
/* 1321 */               this.logger.info("ERROR IN SALES SUBJ TO OTHER TAX");
/* 1322 */               result = false;
/* 1323 */               validDecimal = false;
/* 1324 */               errorMessageList.add("Sales subject to other tax is invalid.");
/*      */             } 
/*      */             break;
/*      */         }
/*      */
/*      */         if (!result) {
/*      */           break;
/*      */         }
/*      */
/*      */       }
/* 1331 */       this.logger.info("VALIDATION : END OF PROCESSING  : " + splitContent);
/*      */     } 
/*      */     
/* 1334 */     if (validDecimal && result) {
/*      */       
/* 1336 */       totalSales = totalSales.add(new BigDecimal(uploadSalesReport.getVatableSales().replace(",", "").replace(" ", "")));
/* 1337 */       totalSales = totalSales.add(new BigDecimal(uploadSalesReport.getVatExemptSales().replace(",", "").replace(" ", "")));
/* 1338 */       totalSales = totalSales.add(new BigDecimal(uploadSalesReport.getVatZeroRatedSales().replace(",", "").replace(" ", "")));
/* 1339 */       totalSales = totalSales.add(new BigDecimal(uploadSalesReport.getSalesSubjToOtherTax().replace(",", "").replace(" ", "")));
/*      */     } 
/*      */     
/* 1342 */     uploadSalesReport.setTotalSales(totalSales);
/* 1343 */     uploadSalesReport.setErrorMessageList(errorMessageList);
/* 1344 */     uploadSalesReport.setValidRecord(result);
/*      */     
/* 1346 */     return uploadSalesReport;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private boolean checkIfValidTIN(String tin) {
/* 1352 */     boolean result = false;
/* 1353 */     Business business = getBusinessDetailsOfTin(tin);
/* 1354 */     if (!tin.isEmpty() && business != null && tin.equals(business.getBusinessTIN()))
/*      */     {
/* 1358 */       result = true;
/*      */     }
/*      */     
/* 1361 */     return result;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private boolean checkIfValidTINandBranch(String tin, String branch) {
/* 1367 */     boolean result = false;
/* 1368 */     Business business = getBusinessDetailsOfTinAndBranch(tin, branch);
/* 1369 */     if (!tin.isEmpty() && business != null && tin.equals(business.getBusinessTIN()))
/*      */     {
/* 1374 */       result = true;
/*      */     }
/*      */     
/* 1377 */     return result;
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean checkIfValidTINForUser(String userName, String tin) {
/* 1382 */     boolean result = isTinAuthorizedForUser(userName, tin);
/* 1383 */     return result;
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean checkIfValidBranchForUser(String userName, String branchCode, String businessTIN) {
/* 1396 */     boolean result = false;
/* 1397 */     if (branchCode.length() >= 3 && branchCode.length() <= 5) {
/* 1398 */       result = isBranchAuthorizedForUser(userName, branchCode, businessTIN);
/*      */     }
/* 1400 */     return result;
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean validMINForBusiness(String min, String businessTIN, String branchCode, String userName, String userBranchType, String mainBranchFlag) {
/* 1428 */     this.logger.debug("BOIR-2878 TRACE : validMINForBusiness : " + min + ":" + ":" + businessTIN + ":" + branchCode + ":" + userName + ":" + userBranchType + ":" + mainBranchFlag);
/* 1429 */     boolean result = true;
/*      */
/* 1431 */     if (min.isEmpty()) {
/*      */
/* 1433 */       result = false;
/*      */
/*      */     }
/*      */     else {
/*      */
/* 1438 */       result = isValidMinForBusiness(min, businessTIN, branchCode, userName, userBranchType, mainBranchFlag);
/*      */     }
/* 1453 */     this.logger.debug("BOIR-2878 TRACE : validMINForBusiness : result : " + result);
/* 1454 */     return result;
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean validMINForReporting(String min, int month, int year) {
/* 1459 */     return isMinEligibleForReporting(min, month, year);
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean minNotCancelled(String min, int month, int year) {
/* 1498 */     return isMinNotCancelledForPeriod(min, month, year);
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean isMinNotCancelled(String min, String reportMonth, String reportYear) {
/* 1521 */     try {
/* 1522 */       int reportedMonth = Integer.parseInt(reportMonth);
/* 1523 */       int reportedYear = Integer.parseInt(reportYear);
/* 1524 */       return isMinNotCancelledForPeriod(min, reportedMonth, reportedYear);
/*      */     }
/* 1526 */     catch (NumberFormatException e) {
/* 1527 */       this.logger.debug("Unable to parse reporting period for MIN cancellation check: " + reportMonth + '/' + reportYear);
/* 1528 */       return false;
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private boolean checkValidMonth(String monthInString, int year) {
/* 1561 */     this.logger.debug("monthInString:monthInString:" + monthInString);
/* 1562 */     boolean result = false;
/* 1563 */     Date dateToday = new Date();
/* 1564 */     SimpleDateFormat sdf = new SimpleDateFormat("MM");
/*      */     
/* 1566 */     if (!monthInString.isEmpty()) {
/*      */       
/* 1568 */       int month = 0;
/* 1569 */       int monthToday = 0;
/* 1570 */       int currentYear = 0;
/*      */       
/*      */       try {
/* 1573 */         month = Integer.parseInt(monthInString);
/* 1574 */         monthToday = Integer.parseInt(sdf.format(dateToday));
/* 1575 */         currentYear = Integer.parseInt((new SimpleDateFormat("yyyy")).format(dateToday));
/*      */         
/* 1577 */         if (currentYear == year)
/*      */         {
/* 1579 */           if (monthToday > month && month > 0)
/*      */           {
/* 1581 */             result = true;
/*      */           }
/*      */           else
/*      */           {
/* 1585 */             result = false;
/*      */           }
/*      */         
/*      */         }
/* 1589 */         else if (currentYear > year && month > 0)
/*      */         {
/* 1591 */           result = true;
/*      */         }
/*      */         else
/*      */         {
/* 1595 */           return false;
/*      */         }
/*      */       
/* 1598 */       } catch (NumberFormatException e) {
/*      */         
/* 1600 */         this.logger.info("ERROR IN MONTH: " + e.getMessage(), e);
/* 1601 */         return false;
/*      */       } 
/*      */     } 
/*      */ 
/*      */     
/* 1606 */     return result;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private boolean isReportYearValid(String reportYearInString, boolean isDecember) {
/* 1613 */     if (!reportYearInString.isEmpty() && reportYearInString.length() == 4) {
/*      */       
/* 1615 */       Date dateToday = new Date();
/* 1616 */       SimpleDateFormat sdfMonth = new SimpleDateFormat("MM");
/* 1617 */       SimpleDateFormat sdfYear = new SimpleDateFormat("yyyy");
/*      */       
/* 1619 */       int monthToday = 0;
/* 1620 */       int yearToday = 0;
/* 1621 */       int year = 0;
/* 1622 */       int reportYear = 0;
/*      */ 
/*      */       
/*      */       try {
/* 1626 */         year = Integer.parseInt(reportYearInString);
/* 1627 */         monthToday = Integer.parseInt(sdfMonth.format(dateToday));
/* 1628 */         yearToday = Integer.parseInt(sdfYear.format(dateToday));
/* 1629 */         reportYear = yearToday;
/*      */         
/* 1631 */         if (isDecember)
/*      */         {
/* 1633 */           return (yearToday > year);
/*      */         }
/*      */ 
/*      */         
/* 1637 */         return (yearToday == year);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/*      */       }
/* 1644 */       catch (NumberFormatException e) {
/*      */         
/* 1646 */         this.logger.info("ERROR IN YEAR: " + e.getMessage(), e);
/* 1647 */         return false;
/*      */       } 
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1655 */     return false;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean validORFormat(String value) {
/* 1660 */     boolean result = false;
/* 1661 */     if (!value.isEmpty()) {
/*      */       
/* 1663 */       String[] invoiceContent = StringUtils.split(value, ';');
/* 1664 */       String charChecker = value.substring(0, 1);
/*      */       
/* 1666 */       if (invoiceContent.length == 2) {
/*      */         
/* 1668 */         String invoiceChecker = invoiceContent[1].substring(0, 1);
/* 1669 */         if ((charChecker.equalsIgnoreCase("S") && invoiceChecker.equalsIgnoreCase("R")) || (charChecker.equalsIgnoreCase("R") && invoiceChecker.equalsIgnoreCase("S")))
/*      */         {
/*      */ 
/*      */           
/* 1673 */           result = true;
/*      */         
/*      */         }
/*      */       
/*      */       }
/* 1678 */       else if (charChecker.equalsIgnoreCase("O") || charChecker.equalsIgnoreCase("T") || charChecker.equalsIgnoreCase("R") || charChecker.equalsIgnoreCase("S")) {
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */         
/* 1684 */         result = true;
/*      */       } 
/*      */     } 
/*      */     
/* 1688 */     return result;
/*      */   }
/*      */ 
/*      */   
/*      */   public String parseReceipt(String value) {
/* 1693 */     String receipt = "";
/*      */     
/* 1695 */     String charChecker = value.substring(0, 1);
/*      */     
/* 1697 */     if (charChecker.equalsIgnoreCase("O") || charChecker.equalsIgnoreCase("T")) {
/*      */       
/* 1699 */       receipt = value.substring(1);
/*      */     }
/*      */     else {
/*      */       
/* 1703 */       String[] invoiceContent = StringUtils.split(value, ';');
/*      */       
/* 1705 */       if (invoiceContent.length == 2) {
/*      */         
/* 1707 */         receipt = invoiceContent[0].substring(1) + ";" + invoiceContent[1].substring(1);
/*      */       }
/*      */       else {
/*      */         
/* 1711 */         receipt = invoiceContent[0].substring(1);
/*      */       } 
/*      */     } 
/*      */     
/* 1715 */     return receipt;
/*      */   }
/*      */ 
/*      */   
/*      */   private Machine convertToSalesReport(UploadSalesReport uploadSalesReport, String channelKey) {
/* 1720 */     Machine machine = new Machine();
/* 1721 */     machine.setMIN(uploadSalesReport.getMIN());
/*      */     
/* 1723 */     List<SalesReport> salesReportList = new ArrayList<SalesReport>();
/*      */     
/* 1725 */     SalesReport salesReport = new SalesReport();
/* 1726 */     salesReport.setBusinessTIN(uploadSalesReport.getBusinessTIN());
/* 1727 */     salesReport.setBranchCode(uploadSalesReport.getBranchCode());
/* 1728 */     salesReport.setRdoCode(getRdoCodeFromITS(uploadSalesReport.getBusinessTIN(), uploadSalesReport.getBranchCode()));
/* 1729 */     salesReport.setMonth(uploadSalesReport.getMonth());
/* 1730 */     salesReport.setYear(uploadSalesReport.getYear());
/*      */     
/* 1732 */     salesReport.setDateSubmitted(uploadSalesReport.getDateSubmitted());
/*      */     
/* 1734 */     salesReport.setVatableSales(convertToBigDecimal(uploadSalesReport.getVatableSales()));
/* 1735 */     salesReport.setVatExemptSales(convertToBigDecimal(uploadSalesReport.getVatExemptSales()));
/* 1736 */     salesReport.setVatZeroRatedSales(convertToBigDecimal(uploadSalesReport.getVatZeroRatedSales()));
/* 1737 */     salesReport.setSalesSubjToOtherTax(convertToBigDecimal(uploadSalesReport.getSalesSubjToOtherTax()));
/*      */     
/* 1739 */     salesReport.setTotalSales(uploadSalesReport.getTotalSales());
/* 1740 */     salesReport.setLastCashInvoiceNo(uploadSalesReport.getLastCashInvoiceNo());
/* 1741 */     salesReport.setLastChargeInvoiceNo(uploadSalesReport.getLastChargeInvoiceNo());
/* 1742 */     salesReport.setLastORNo(uploadSalesReport.getLastORNo());
/* 1743 */     salesReport.setLastTransactionNo(uploadSalesReport.getLastTransactionNo());
/*      */     
/* 1745 */     ParameterizedObject channel = new ParameterizedObject();
/* 1746 */     channel.setKey(channelKey);
/* 1747 */     salesReport.setChannel(channel);
/*      */     
/* 1749 */     List<String> minList = new ArrayList<String>();
/* 1750 */     minList.add(machine.getMIN());
/*      */     
/* 1752 */     List<Date> salesDateList = new ArrayList<Date>();
/* 1753 */     Calendar calendar = Calendar.getInstance();
/* 1754 */     calendar.clear();
/*      */     
/* 1756 */     calendar.set(uploadSalesReport.getYear(), uploadSalesReport.getMonth() - 1, 1);
/*      */     
/* 1758 */     salesDateList.add(calendar.getTime());
/*      */     
/* 1760 */     List<ParameterizedObject> statusList = new ArrayList<ParameterizedObject>();
/*      */     
/* 1762 */     Date dateSubmitted = new Date();
/*      */     
/*      */     try {
/* 1765 */       dateSubmitted = uploadSalesReport.getDateSubmitted();
/*      */     }
/* 1767 */     catch (Exception e) {}
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1774 */     statusList = generateStatus(dateSubmitted, minList, salesDateList);
/*      */ 
/*      */     
/* 1777 */     salesReport.setSalesStatus(statusList.get(0));
/* 1778 */     salesReportList.add(salesReport);
/* 1779 */     machine.setSalesReportList(salesReportList);
/*      */     
/* 1781 */     return machine;
/*      */   }
/*      */ 
/*      */   
/*      */   public static boolean isNumericLengthValid(String number) {
/* 1786 */     boolean hasDecimal = false;
/* 1787 */     for (char ch : number.toCharArray()) {
/*      */       
/* 1789 */       if (ch == '.')
/*      */       {
/* 1791 */         hasDecimal = true;
/*      */       }
/*      */     } 
/*      */     
/* 1795 */     if (hasDecimal) {
/*      */       
/* 1797 */       int decimalPlace = number.indexOf(".");
/*      */       
/* 1799 */       if (number.substring(decimalPlace, number.length()).length() > 3)
/*      */       {
/* 1801 */         return false;
/*      */       }
/*      */     } 
/*      */     
/* 1805 */     return true;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public static boolean isNumericValue(String number) {
/* 1811 */     if (null == number)
/*      */     {
/* 1813 */       return false;
/*      */     }
/* 1815 */     if (number.length() == 0)
/*      */     {
/* 1817 */       return false;
/*      */     }
/* 1819 */     for (char ch : number.toCharArray()) {
/*      */       
/* 1821 */       if (!Character.isDigit(ch) && ch != '.' && ch != '+' && ch != '-')
/*      */       {
/* 1823 */         return false;
/*      */       }
/*      */     } 
/*      */     
/* 1827 */     return true;
/*      */   }
/*      */ 
/*      */   
/*      */   public static boolean isNumeric(String number) {
/* 1832 */     if (number == null) {
/*      */
/* 1834 */       return false;
/*      */     }
/*      */
/* 1837 */     Matcher matcher = NUMERIC_PATTERN.matcher(number);
/* 1838 */     return matcher.matches();
/*      */   }
/*      */
/*      */   private BigDecimal convertToBigDecimal(String amountInString) {
/* 1849 */     BigDecimal returnValue = new BigDecimal("0.00");
/*      */     
/*      */     try {
/* 1852 */       returnValue = new BigDecimal(Double.parseDouble(amountInString.replace(",", "").replace(" ", "")));
/*      */     }
/* 1854 */     catch (Exception e) {}
/*      */ 
/*      */ 
/*      */     
/* 1858 */     return returnValue;
/*      */   }
/*      */ 
/*      */   
/*      */   private File renameFile(File sourceFile, String pathHolderName, String fileType) {
/* 1863 */     int fileCount = 1;
/* 1864 */     String pathHolder = basePathConfigurer(pathHolderName + File.separator);
/* 1865 */     String sourceFileName = sourceFile.getName();
/* 1866 */     String destFileName = pathHolder + sourceFileName;
/*      */     
/* 1868 */     destFileName = destFileName.substring(0, destFileName.length() - 4);
/*      */     
/* 1870 */     File destFile = new File(destFileName + "_" + fileCount + fileType);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 1878 */     while (destFile.exists()) {
/*      */ 
/*      */ 
/*      */       
/* 1882 */       destFile = new File(destFileName + "_" + fileCount + fileType);
/* 1883 */       fileCount++;
/*      */     } 
/*      */     
/* 1886 */     return destFile;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public static Date convertStringToDate(String strDate) throws ParseException {
/* 1892 */     SimpleDateFormat formatter = new SimpleDateFormat("mmddyyyy");
/* 1893 */     Date date = formatter.parse(strDate);
/*      */     
/* 1895 */     return date;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void processSMSFile() {
/*      */     try {
/* 1902 */       downloadSMSFile();
/*      */     }
/* 1904 */     catch (Exception e) {
/*      */       
/* 1906 */       this.logger.error(e.getMessage());
/*      */     } 
/*      */     
/* 1909 */     String archivePathHolder = basePathConfigurer(this.salesSMSArchive + File.separator);
/*      */     
/* 1911 */     File[] fileToProcess = getSMSFileToProcess();
/*      */ 
/*      */ 
/*      */     
/* 1915 */     if (fileToProcess != null)
/*      */     {
/* 1917 */       for (File fileContent : fileToProcess) {
/*      */ 
/*      */         
/* 1920 */         String destPath = basePathConfigurer(archivePathHolder + fileContent.getName());
/*      */         
/* 1922 */         File destFile = new File(destPath);
/*      */         
/* 1924 */         this.logger.info("file processing.. fileContent : " + fileContent.getName());
/* 1925 */         processSMSSales(fileContent);
/*      */         
/* 1927 */         if (destFile.exists()) {
/*      */           
/* 1929 */           this.logger.info("file exists..");
/* 1930 */           destFile = renameFile(destFile, archivePathHolder, ".txt");
/*      */         } 
/*      */ 
/*      */         
/*      */         try {
/* 1935 */           FileUtils.moveFile(fileContent, destFile);
/* 1936 */           this.logger.info("moving file.. " + destFile);
/*      */         }
/* 1938 */         catch (IOException e) {
/*      */           
/* 1940 */           this.logger.error(e.getMessage());
/*      */         } 
/*      */       } 
/*      */     }
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private void processSMSSales(File textFile) {
/*      */     try {
/* 1950 */       BufferedReader br = new BufferedReader(new FileReader(textFile));
/*      */       
/* 1952 */       List<String> rowMessages = new ArrayList<String>();
/*      */       
/*      */       String line;
/* 1955 */       while ((line = br.readLine()) != null) {
/*      */ 
/*      */         
/* 1958 */         rowMessages.add(line);
/* 1959 */         this.logger.info("Line : " + line);
/*      */       } 
/*      */       
/* 1962 */       br.close();
/*      */       
/* 1964 */       this.logger.info("rowMessages :" + rowMessages.size());
/* 1965 */       parseRowText(rowMessages);
/*      */     }
/* 1967 */     catch (FileNotFoundException e) {
/*      */       
/* 1969 */       this.logger.error(e.getMessage());
/*      */     }
/* 1971 */     catch (IOException e) {
/*      */       
/* 1973 */       this.logger.error(e.getMessage());
/*      */     } 
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private static String configureMobileNumber(String mobile) {
/* 1980 */     String result = "";
/*      */     
/* 1982 */     if (mobile.length() == 13) {
/*      */       
/* 1984 */       result = "0" + mobile.substring(3, mobile.length());
/* 1985 */       return result;
/*      */     } 
/*      */ 
/*      */     
/* 1989 */     return mobile;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private void parseRowText(List<String> rowMessages) {
/* 1995 */     String messageContent = "";
/*      */     
/* 1997 */     int counter = 0;
/* 1998 */     int addToCounter = 0;
/* 1999 */     for (int x = counter + addToCounter; x < rowMessages.size(); x++) {
/*      */ 
/*      */       
/* 2002 */       this.logger.info("x : " + x);
/*      */       
/* 2004 */       String[] splitText = ((String)rowMessages.get(x)).split("\\|");
/*      */       
/* 2006 */       String mobileNumber = configureMobileNumber(splitText[0].trim());
/*      */       
/* 2008 */       Map<String, Object> mapSales = new HashMap<String, Object>();
/* 2009 */       mapSales.put("MOBILE_NUM", mobileNumber);
/*      */       
/* 2011 */       if (splitText.length == 4) {
/*      */         
/* 2013 */         messageContent = splitText[2];
/* 2014 */         addToCounter = 0;
/*      */         
/* 2016 */         this.logger.debug("TRACE : messageContent : " + messageContent);
/*      */         
/* 2018 */         if (x + 1 <= rowMessages.size() - 1) {
/*      */           
/* 2020 */           int loopCounter = x + 1;
/*      */           
/* 2022 */           for (int i = loopCounter; i < rowMessages.size(); i++) {
/*      */             
/* 2024 */             if (((String)rowMessages.get(loopCounter)).matches("^.*(SLS).*$")) {
/*      */               break;
/*      */             }
/*      */ 
/*      */ 
/*      */             
/* 2030 */             String[] splitText2 = ((String)rowMessages.get(loopCounter)).split("\\|");
/* 2031 */             if (splitText2.length > 1)
/* 2032 */               messageContent = messageContent + splitText2[2]; 
/* 2033 */             addToCounter++;
/* 2034 */             loopCounter++;
/*      */           } 
/*      */         } 
/*      */       } 
/*      */ 
/*      */       
/* 2040 */       this.logger.info("messageContent: " + messageContent);
/* 2041 */       x += addToCounter;
/*      */       
/* 2043 */       boolean invalidFormat = false;
/*      */       
/* 2045 */       if (messageContent.trim().isEmpty() || !messageContent.substring(0, 4).equalsIgnoreCase("SLS ")) {
/*      */         
/* 2047 */         invalidFormat = true;
/* 2048 */         List<String> errorMessageList = new ArrayList<String>();
/* 2049 */         UploadSalesReport uploadRecord = new UploadSalesReport();
/*      */         
/* 2051 */         errorMessageList.add("You have sent sales report with incorrect format.");
/* 2052 */         uploadRecord.setValidRecord(false);
/* 2053 */         uploadRecord.setErrorMessageList(errorMessageList);
/* 2054 */         mapSales.put("SALES_REPORT", uploadRecord);
/* 2055 */         mapSales.put("INVALID_NO", Boolean.valueOf(false));
/*      */       } 
/*      */       
/* 2058 */       if (!invalidFormat) {
/*      */ 
/*      */         
/* 2061 */         mapSales.put("MESSAGE", messageContent);
/*      */         
/* 2063 */         int splitSize = messageContent.length();
/* 2064 */         String splitMessage = messageContent.substring(4, splitSize);
/*      */         
/* 2066 */         this.logger.info("splitMessage :" + splitMessage);
/*      */         
/* 2068 */         mapSales.put("DATE_SUBMITTED", splitText[3]);
/*      */         
/* 2070 */         int userCount = this.salesReportDao.getCountUsersByMobileNumber(mapSales);
/* 2071 */         this.logger.info("userCount :" + userCount);
/*      */ 
/*      */         
/* 2074 */         Date dateSubmitted = new Date();
/*      */         
/* 2076 */         if (userCount > 0) {
/*      */ 
/*      */           
/* 2079 */           String password = "DUMMY_PASSWORD";
/*      */           
/* 2081 */           Map<String, Object> dbParameter = null;
/*      */           
/* 2083 */           List<Map<String, Object>> uploadedRecordList = new ArrayList<Map<String, Object>>();
/*      */ 
/*      */           
/* 2086 */           ParameterizedObject loginType = new ParameterizedObject();
/* 2087 */           loginType.setKey("003");
/*      */           
/* 2089 */           List<Map<String, Object>> dbParameterList = new ArrayList<Map<String, Object>>();
/*      */ 
/*      */           
/* 2092 */           UploadSalesReport uploadRecord = new UploadSalesReport();
/* 2093 */           uploadRecord = processSMSSalesReport(splitMessage, mobileNumber);
/* 2094 */           uploadRecord.setDateSubmitted(dateSubmitted);
/*      */           
/* 2096 */           if (uploadRecord.isValidRecord()) {
/*      */ 
/*      */             
/* 2099 */             Machine machine = new Machine();
/* 2100 */             machine = convertToSalesReport(uploadRecord, "045");
/*      */             
/* 2102 */             SalesReport salesReport = machine.getSalesReportList().get(0);
/*      */             
/* 2104 */             Business business = new Business();
/* 2105 */             business.setBusinessTIN(uploadRecord.getBusinessTIN());
/*      */             
/* 2107 */             Branch branch = new Branch();
/* 2108 */             branch.setBranchCode(uploadRecord.getBranchCode());
/*      */             
/* 2110 */             AuditTrail auditTrail = new AuditTrail();
/* 2111 */             auditTrail.setDateStamp(new Date());
/*      */             
/* 2113 */             SecurityProfile secProfile = new SecurityProfile(uploadRecord.getUsername(), password, (GrantedAuthority[])new GrantedAuthorityImpl[] { new GrantedAuthorityImpl("ROLE_ANONYMOUS") });
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */             
/*      */             try {
/* 2120 */               InetAddress ipAddress = InetAddress.getLocalHost();
/* 2121 */               auditTrail.setIp(ipAddress.getHostAddress());
/*      */             }
/* 2123 */             catch (UnknownHostException e) {
/*      */               
/* 2125 */               this.logger.error(e.getMessage());
/*      */             } 
/*      */             
/* 2128 */             auditTrail.setLoginType(loginType);
/* 2129 */             auditTrail.setUserSecurity(secProfile);
/*      */             
/* 2131 */             dbParameter = new HashMap<String, Object>();
/* 2132 */             dbParameter.put("salesReport", salesReport);
/* 2133 */             dbParameter.put("machine", machine);
/* 2134 */             dbParameter.put("auditTrail", auditTrail);
/* 2135 */             dbParameter.put("business", business);
/* 2136 */             dbParameter.put("branch", branch);
/* 2137 */             String rdoCode = salesReport.getRdoCode();
/* 2138 */             if (StringUtils.isEmpty(rdoCode)) {
/* 2139 */               rdoCode = getRdoCodeFromITS(business.getBusinessTIN(), branch.getBranchCode());
/* 2140 */               salesReport.setRdoCode(rdoCode);
/*      */             }
/* 2142 */             dbParameter.put("rdoCode", rdoCode);
/* 2137 */             dbParameter.put("dateSubmitted", dateSubmitted);
/* 2138 */             dbParameterList.add(dbParameter);
/*      */             
/* 2140 */             uploadedRecordList = batchInsertOfSalesReport(dbParameterList);
/* 2141 */             salesReport = (SalesReport)((Map)uploadedRecordList.get(0)).get("salesReport");
/* 2142 */             uploadRecord.setSalesReportNumber(salesReport.getSalesReportNumber());
/* 2143 */             uploadRecord.setSalesStatus(salesReport.getSalesStatus());
/*      */           } 
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
/* 2166 */           mapSales.put("SALES_REPORT", uploadRecord);
/* 2167 */           mapSales.put("INVALID_NO", Boolean.valueOf(false));
/*      */         }
/*      */         else {
/*      */           
/* 2171 */           mapSales.put("INVALID_NO", Boolean.valueOf(true));
/*      */         } 
/*      */       } 
/*      */       
/*      */       try {
/* 2176 */         composeOutgoingMessageInArchive(mapSales);
/*      */       }
/* 2178 */       catch (IOException e) {
/*      */         
/* 2180 */         this.logger.error(e.getMessage());
/*      */       } 
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   public File[] getSMSFileToProcess() {
/* 2187 */     File srcFile = null;
/* 2188 */     File[] destFile = null;
/*      */     
/* 2190 */     String sourcePathHolder = basePathConfigurer(this.salesSMSUpload + File.separator);
/* 2191 */     String processPathHolder = basePathConfigurer(this.salesSMSProcess + File.separator);
/*      */     
/* 2193 */     this.logger.info("sourcePathHolder : " + sourcePathHolder);
/* 2194 */     this.logger.info("processPathHolder : " + processPathHolder);
/*      */     
/* 2196 */     File sourceDirectory = new File(sourcePathHolder);
/* 2197 */     File processDirectory = new File(processPathHolder);
/* 2198 */     FileListFilter fileListFilter = new FileListFilter("txt");
/*      */     
/* 2200 */     if (processDirectory.exists() && (processDirectory.listFiles((FilenameFilter)fileListFilter)).length > 0) {
/*      */       
/* 2202 */       File[] dirContents = processDirectory.listFiles((FilenameFilter)fileListFilter);
/* 2203 */       Arrays.sort(dirContents, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
/* 2204 */       this.logger.info("count : " + dirContents.length);
/* 2205 */       return dirContents;
/*      */     } 
/*      */     
/* 2208 */     if (sourceDirectory.exists()) {
/*      */       
/* 2210 */       File[] dirContents = sourceDirectory.listFiles((FilenameFilter)fileListFilter);
/* 2211 */       Arrays.sort(dirContents, NameFileComparator.NAME_INSENSITIVE_COMPARATOR);
/* 2212 */       if (dirContents != null && dirContents.length > 0) {
/*      */         
/* 2214 */         int x = 0;
/* 2215 */         destFile = new File[dirContents.length];
/* 2216 */         for (File fileContent : dirContents) {
/*      */           
/* 2218 */           srcFile = new File(fileContent.getAbsolutePath());
/* 2219 */           String destPath = basePathConfigurer(processPathHolder + srcFile.getName());
/* 2220 */           destFile[x] = new File(destPath);
/*      */ 
/*      */           
/*      */           try {
/* 2224 */             FileUtils.moveFile(srcFile, destFile[x]);
/*      */           }
/* 2226 */           catch (IOException e) {
/*      */             
/* 2228 */             this.logger.error(e.getMessage());
/*      */           } 
/*      */           
/* 2231 */           x++;
/*      */         } 
/*      */       } 
/*      */     } 
/* 2235 */     return destFile;
/*      */   }
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */   
/*      */   private void downloadSMSFile() {
/*      */     try {
/* 2244 */       this.birFtpFileConnection.downloadFiles();
/* 2245 */       this.logger.info("downloading file..");
/*      */     }
/* 2247 */     catch (IOException e) {
/*      */       
/* 2249 */       this.logger.error(e.getMessage());
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   private void composeOutgoingMessageInArchive(Map<String, Object> param) throws IOException {
/* 2255 */     String numberOfSender = param.get("MOBILE_NUM").toString();
/* 2256 */     UploadSalesReport salesReport = (UploadSalesReport)param.get("SALES_REPORT");
/*      */     
/* 2258 */     File fileToSend = generateSMSFileName(numberOfSender);
/* 2259 */     BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(fileToSend));
/* 2260 */     String message = "";
/*      */     
/* 2262 */     boolean invalidNo = Boolean.parseBoolean(param.get("INVALID_NO").toString());
/* 2263 */     if (invalidNo) {
/*      */       
/* 2265 */       StringBuilder stringBuilder = new StringBuilder(this.smsReplyTransactionFailed);
/* 2266 */       stringBuilder.append(" ");
/* 2267 */       stringBuilder.append(this.smsReplyUnauthorized);
/* 2268 */       message = stringBuilder.toString();
/*      */ 
/*      */     
/*      */     }
/* 2272 */     else if (salesReport.isValidRecord()) {
/*      */       
/* 2274 */       Calendar salesCal = Calendar.getInstance();
/* 2275 */       salesCal.clear();
/* 2276 */       salesCal.set(salesReport.getYear(), salesReport.getMonth() - 1, 1);
/*      */       
/* 2278 */       this.logger.info("==================== " + salesCal.getTime().toString() + " =================================");
/*      */       
/* 2280 */       DateFormat formatter = new SimpleDateFormat("MMMM yyyy");
/* 2281 */       StringBuilder stringBuilder = new StringBuilder("You have successfully submitted your sales report for the month of");
/* 2282 */       stringBuilder.append(" ").append(formatter.format(salesCal.getTime())).append(". ").append("Thank you for using SMS facility of eSales System. SRN: ").append(salesReport.getSalesReportNumber()).append(" Sales Report Status: ").append(salesReport.getSalesStatus().getValue());
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/* 2289 */       message = stringBuilder.toString();
/*      */     }
/*      */     else {
/*      */       
/* 2293 */       StringBuilder stringBuilder = new StringBuilder(this.smsReplyTransactionFailed);
/* 2294 */       for (String temp : salesReport.getErrorMessageList()) {
/*      */         
/* 2296 */         stringBuilder.append(" ");
/* 2297 */         stringBuilder.append(temp);
/*      */       } 
/* 2299 */       stringBuilder.append(" ");
/* 2300 */       stringBuilder.append(this.smsReplyReportFormat);
/* 2301 */       message = stringBuilder.toString();
/*      */     } 
/*      */ 
/*      */     
/*      */     try {
/* 2306 */       bufferedWriter.write(message);
/* 2307 */       bufferedWriter.newLine();
/* 2308 */       bufferedWriter.write("Subject\\|Footer");
/* 2309 */       bufferedWriter.newLine();
/* 2310 */       bufferedWriter.write("N");
/* 2311 */       bufferedWriter.newLine();
/* 2312 */       bufferedWriter.write(numberOfSender);
/* 2313 */       bufferedWriter.newLine();
/* 2314 */       bufferedWriter.write("N,N");
/* 2315 */       bufferedWriter.newLine();
/* 2316 */       bufferedWriter.write("~");
/*      */     }
/*      */     finally {
/*      */       
/* 2320 */       bufferedWriter.close();
/*      */     } 
/*      */     
/* 2323 */     this.birFtpFileConnection.uploadFile(fileToSend);
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private File generateSMSFileName(String sender) {
/* 2329 */     String smsOutgoingArchivePathHolder = basePathConfigurer(this.smsOutgoingArchive + File.separator);
/*      */     
/* 2331 */     SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMdd");
/* 2332 */     SimpleDateFormat timeFormat = new SimpleDateFormat("HHmmss");
/*      */     
/* 2334 */     File initialName = null;
/* 2335 */     File sendToFile = null;
/*      */     
/* 2337 */     if (!StringUtils.isEmpty(sender)) {
/*      */       
/* 2339 */       initialName = new File(sender + "-" + dateFormat.format(new Date()) + timeFormat.format(new Date()) + ".txt");
/*      */ 
/*      */ 
/*      */       
/* 2343 */       sendToFile = renameFile(initialName, smsOutgoingArchivePathHolder, ".txt");
/*      */     } 
/*      */     
/* 2346 */     return sendToFile;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public Date getEffectivityDateOfPermit(Map<String, Object> paramMap) {
/* 2352 */     return getEffectivityDateOfPermitCached(paramMap);
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSalesUploadMailer(TemplateMailer salesUploadMailer) {
/* 2357 */     this.salesUploadMailer = salesUploadMailer;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setUserDao(UserDao userDao) {
/* 2362 */     this.userDao = userDao;
/*      */   }
/*      */ 
/*      */   
/*      */   public String getSalesReportUploadProcess() {
/* 2367 */     return this.salesReportUploadProcess;
/*      */   }
/*      */ 
/*      */   
/*      */   public String getSalesReportUploadArchive() {
/* 2372 */     return this.salesReportUploadArchive;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSalesReportUploadProcess(String salesReportUploadProcess) {
/* 2377 */     this.salesReportUploadProcess = salesReportUploadProcess;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSalesReportUploadArchive(String salesReportUploadArchive) {
/* 2382 */     this.salesReportUploadArchive = salesReportUploadArchive;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setBirFtpFileConnection(FTPFileConnection birFtpFileConnection) {
/* 2387 */     this.birFtpFileConnection = birFtpFileConnection;
/*      */   }
/*      */ 
/*      */   
/*      */   public String getSalesSMSUpload() {
/* 2392 */     return this.salesSMSUpload;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSalesSMSUpload(String salesSMSUpload) {
/* 2397 */     this.salesSMSUpload = salesSMSUpload;
/*      */   }
/*      */ 
/*      */   
/*      */   public String getSalesSMSProcess() {
/* 2402 */     return this.salesSMSProcess;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSalesSMSProcess(String salesSMSProcess) {
/* 2407 */     this.salesSMSProcess = salesSMSProcess;
/*      */   }
/*      */ 
/*      */   
/*      */   public String getSalesSMSArchive() {
/* 2412 */     return this.salesSMSArchive;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSalesSMSArchive(String salesSMSArchive) {
/* 2417 */     this.salesSMSArchive = salesSMSArchive;
/*      */   }
/*      */ 
/*      */   
/*      */   public String getSalesSMSFormat() {
/* 2422 */     return this.salesSMSFormat;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSalesSMSFormat(String salesSMSFormat) {
/* 2427 */     this.salesSMSFormat = salesSMSFormat;
/*      */   }
/*      */ 
/*      */   
/*      */   public FTPFileConnection getBirFtpFileConnection() {
/* 2432 */     return this.birFtpFileConnection;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSmsOutgoingArchive(String smsOutgoingArchive) {
/* 2437 */     this.smsOutgoingArchive = smsOutgoingArchive;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private UploadSalesReport processSMSSalesReport(String lineContent, String mobileNumber) {
/* 2443 */     UploadSalesReport uploadSalesReport = new UploadSalesReport();
/* 2444 */     BigDecimal totalSales = new BigDecimal("0.00");
/*      */     
/* 2446 */     Date dateToday = new Date();
/* 2447 */     Calendar calendarToday = Calendar.getInstance();
/* 2448 */     calendarToday.clear();
/* 2449 */     calendarToday.setTime(dateToday);
/*      */     
/* 2451 */     List<String> errorMessageList = new ArrayList<String>();
/* 2452 */     String[] splitContent = StringUtils.split(lineContent, ' ');
/* 2453 */     if (splitContent == null) {
/* 2454 */       splitContent = new String[0];
/*      */     }
/* 2456 */     for (int idx = 0; idx < splitContent.length; idx++) {
/* 2457 */       splitContent[idx] = (splitContent[idx] == null) ? "" : splitContent[idx].trim();
/*      */     }
/* 2460 */     final String tin = (splitContent.length > 0) ? splitContent[0] : "";
/* 2461 */     final String branchCode = (splitContent.length > 1) ? splitContent[1] : "";
/* 2462 */     final String reportDateToken = (splitContent.length > 2) ? splitContent[2] : "";
/* 2463 */     final String minToken = (splitContent.length > 3) ? splitContent[3] : "";
/* 2464 */     final String receiptToken = (splitContent.length > 4) ? splitContent[4] : "";
/*      */
/* 2466 */     boolean result = true;
/* 2455 */     boolean validDecimal = true;
/* 2456 */     boolean validMonthAndYear = true;
/* 2457 */     boolean continueProcessingMsg = true;
/* 2458 */     boolean isDecember = false;
/*      */     
/* 2460 */     if (splitContent.length < 4) {
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */       
/* 2469 */       this.logger.info("ERROR IN FORMAT : " + splitContent.length);
/* 2470 */       result = false;
/* 2471 */       validDecimal = false;
/* 2472 */       errorMessageList.add("You have sent sales report with incorrect format.");
/*      */     }
/*      */     else {
/*      */       
/* 2476 */       for (int i = 0; i < splitContent.length && continueProcessingMsg; i++) {
/*      */
/* 2478 */         if (splitContent[i].isEmpty()) {
/*      */
/* 2480 */           result = false;
/* 2481 */           errorMessageList.add("You have sent sales report with incorrect format.");
/* 2482 */           continueProcessingMsg = false;
/* 2483 */           validDecimal = false;
/*      */         }
/*      */       }
/*      */
/* 2487 */       Map<String, Object> userParam = new HashMap<String, Object>();
/* 2488 */       userParam.put("MOBILE_NUM", mobileNumber);
/* 2489 */       userParam.put("BUSINESS_TIN", tin);
/* 2490 */       userParam.put("BRANCH_CODE", branchCode);
/*      */       
/* 2492 */       String userBranchType = "";
/*      */       
/* 2494 */       String userName = this.salesReportDao.getUserNameByNumber(userParam);
/*      */       
/* 2496 */       if (continueProcessingMsg && result)
/*      */       {
/*      */         
/* 2499 */         if (StringUtils.isEmpty(userName)) {
/*      */           
/* 2501 */           this.logger.info("NO USERNAME FOR MOBILE NO, TIN, BRANCH COMBINATION.");
/* 2502 */           errorMessageList.add("User is not authorized.");
/* 2503 */           continueProcessingMsg = false;
/* 2504 */           result = false;
/* 2505 */           validDecimal = false;

/*      */         }
/*      */         else {
/*      */           
/* 2509 */           uploadSalesReport.setUsername(userName);
/* 2510 */           userBranchType = "000".equals(receiptToken) ? "MB" : "SB";
/* 2511 */           String valueToProcess = "";
/* 2512 */           this.logger.info("PROCESSING SMS CONTENT");
/* 2513 */           int withVS = 0, withVE = 0, withZS = 0, withOT = 0;
/* 2514 */           int inputYear = 0, inputMonth = 0;
/* 2515 */           for (int ii = 0; ii < splitContent.length && result; ii++) {
/*      */             
/* 2517 */             valueToProcess = splitContent[ii];
/* 2518 */             this.logger.info("PRESSING : VALUE : " + valueToProcess);
/*      */             
/* 2520 */             int j = ii;
/* 2521 */             if (ii >= 5) {
/* 2522 */               j = getInputIndex(splitContent[ii]);
/*      */             }
/* 2524 */             if (j < 5 && ii >= 5) {
/*      */               
/* 2526 */               errorMessageList.add("You have sent sales report with incorrect format.");
/*      */             } else {
/*      */               String inputDate; boolean monthError; boolean yearError; boolean validMIN; boolean validMINForReporting; boolean validMinNotCancelled; String minTemp;
/*      */               String orTemp;
/* 2530 */               switch (j) {
/*      */                 
/*      */                 case 0:
/* 2533 */                   if (splitContent[0].length() != 9) {
/*      */                     
/* 2535 */                     this.logger.info("ERROR IN TIN LENGTH");
/* 2536 */                     result = false;
/* 2537 */                     errorMessageList.add("Please enter a valid TIN.");
/*      */ 
/*      */                   
/*      */                   }
/* 2541 */                   else if (!checkIfValidTIN(splitContent[0])) {
/*      */                     
/* 2543 */                     this.logger.info("ERROR IN TIN : CANNOT BE FOUND");
/* 2544 */                     result = false;
/* 2545 */                     errorMessageList.add("Please enter a valid TIN.");
/*      */ 
/*      */                   
/*      */                   }
/* 2549 */                   else if (!checkIfValidTINForUser(userName, splitContent[0])) {
/*      */                     
/* 2551 */                     this.logger.info("ERROR IN TIN");
/* 2552 */                     result = false;
/* 2553 */                     errorMessageList.add("User is not authorized to report for this TIN.");
/*      */                   } 
/*      */ 
/*      */                   
/* 2557 */                   uploadSalesReport.setBusinessTIN(splitContent[0]);
/*      */                   break;
/*      */                 case 1:
/* 2560 */                   if (splitContent[1].length() < 3 || splitContent[1].length() > 5) {
/*      */                     
/* 2562 */                     this.logger.info("ERROR IN BRANCH LENGTH");
/* 2563 */                     result = false;
/* 2564 */                     errorMessageList.add("Please enter a valid Branch Code.");
/*      */ 
/*      */                   
/*      */                   }
/* 2568 */                   else if (!checkIfValidTINandBranch(splitContent[0], splitContent[1])) {
/*      */                     
/* 2570 */                     this.logger.info("ERROR IN TIN AND BRANCH: CANNOT BE FOUND");
/* 2571 */                     result = false;
/* 2572 */                     errorMessageList.add("Please enter a valid Branch Code.");
/*      */ 
/*      */                   
/*      */                   }
/* 2576 */                   else if (!checkIfValidBranchForUser(userName, splitContent[1], splitContent[0])) {
/*      */                     
/* 2578 */                     this.logger.info("ERROR IN BRANCH");
/* 2579 */                     result = false;
/* 2580 */                     errorMessageList.add("Please enter a valid Branch Code.");
/*      */                   } 
/*      */ 
/*      */                   
/* 2584 */                   uploadSalesReport.setBranchCode(branchCode);
/*      */                   break;
/*      */
/*      */                 case 2:
/* 2588 */                   inputDate = reportDateToken;
/* 2589 */                   monthError = false;
/* 2590 */                   yearError = false;
/* 2591 */                   this.logger.debug("TRACE : inputDate :" + inputDate);
/* 2592 */                   if (inputDate.isEmpty() || inputDate.length() != 6) {
/*      */
/* 2594 */                     this.logger.info("ERROR DATE IS BLANK/ INVALID FORMAT ");
/* 2595 */                     result = false;
/* 2596 */                     validMonthAndYear = false;
/* 2597 */                     monthError = true;
/* 2598 */                     yearError = true;
/*      */                     
/* 2600 */                     errorMessageList.add("Month is invalid. Year is invalid.");
/*      */                     
/*      */                     break;
/*      */                   } 
/* 2604 */                   this.logger.debug("TRACE : processing inputMonth : 1");
/*      */                   
/*      */                   try {
/* 2607 */                     this.logger.debug("TRACE : processing inputMonth : 1 :" + inputDate.substring(0, 2));
/* 2608 */                     inputMonth = Integer.parseInt(inputDate.substring(0, 2));
/* 2609 */                     if (inputMonth <= 0 || inputMonth >= 13)
/*      */                     {
/* 2611 */                       this.logger.info("ERROR IN MONTH ");
/* 2612 */                       result = false;
/* 2613 */                       validMonthAndYear = false;
/*      */                       
/* 2615 */                       monthError = true;
/*      */                     }
/*      */                   
/* 2618 */                   } catch (Exception e) {
/*      */                     
/* 2620 */                     this.logger.info("ERROR IN MONTH " + e.getMessage(), e);
/* 2621 */                     result = false;
/* 2622 */                     validMonthAndYear = false;
/*      */                     
/* 2624 */                     monthError = true;
/*      */                   } 
/* 2626 */                   this.logger.debug("TRACE : inputMonth : 1 :" + inputMonth);
/* 2627 */                   this.logger.debug("TRACE : processing year : 1");
/*      */ 
/*      */                   
/*      */                   try {
/* 2631 */                     if (inputDate.substring(2, inputDate.length()).length() != 4)
/*      */                     {
/* 2633 */                       this.logger.info("ERROR IN YEAR: LEN ");
/* 2634 */                       result = false;
/* 2635 */                       validMonthAndYear = false;
/*      */                       
/* 2637 */                       yearError = true;
/*      */                     }
/*      */                     else
/*      */                     {
/* 2641 */                       inputYear = Integer.parseInt(inputDate.substring(2, inputDate.length()));
/*      */                     }
/*      */                   
/* 2644 */                   } catch (NumberFormatException e) {
/*      */                     
/* 2646 */                     this.logger.info("ERROR IN YEAR: " + e.getMessage(), e);
/* 2647 */                     result = false;
/* 2648 */                     validMonthAndYear = false;
/*      */                     
/* 2650 */                     yearError = true;
/*      */                   } 
/* 2652 */                   this.logger.debug("TRACE : processing inputYear : 2:" + inputYear);
/*      */                   
/* 2654 */                   if (validMonthAndYear) {
/*      */                     
/* 2656 */                     isDecember = (inputMonth == 12);
/* 2657 */                     if (!isReportYearValid(inputYear + "", isDecember)) {
/*      */                       
/* 2659 */                       this.logger.info("ERROR IN YEAR");
/* 2660 */                       result = false;
/* 2661 */                       validMonthAndYear = false;
/*      */                       
/* 2663 */                       yearError = true;
/*      */                     }
/*      */                     else {
/*      */                       
/* 2667 */                       uploadSalesReport.setYear(inputYear);
/*      */                     } 
/*      */                   } 
/* 2670 */                   this.logger.debug("TRACE : processing inputMonth : 2");
/* 2671 */                   if (validMonthAndYear)
/*      */                   {
/* 2673 */                     if (!checkValidMonth(inputMonth + "", inputYear)) {
/*      */                       
/* 2675 */                       this.logger.info("ERROR IN MONTH");
/* 2676 */                       result = false;
/* 2677 */                       validMonthAndYear = false;
/*      */                       
/* 2679 */                       monthError = true;
/*      */                     }
/*      */                     else {
/*      */                       
/* 2683 */                       uploadSalesReport.setMonth(inputMonth);
/*      */                     } 
/*      */                   }
/* 2686 */                   this.logger.debug("monthError && yearError:" + monthError + ":" + yearError);
/* 2687 */                   if (monthError && yearError) {
/* 2688 */                     errorMessageList.add("Month is invalid. Year is invalid."); break;
/* 2689 */                   }  if (monthError) {
/* 2690 */                     errorMessageList.add("Month is invalid."); break;
/* 2691 */                   }  if (yearError) {
/* 2692 */                     errorMessageList.add("Year is invalid.");
/*      */                   }
/*      */                   break;
/*      */                 
/*      */                 case 3:
/* 2697 */                   validMIN = true;
/* 2698 */                   validMINForReporting = true;
/* 2699 */                   validMinNotCancelled = true;
/* 2700 */                   minTemp = minToken;
/* 2701 */                   if (receiptToken.isEmpty()) {
/*      */
/* 2703 */                     this.logger.info("ERROR IN MIN - MIN IS INVALID");
/* 2704 */                     result = false;
/* 2705 */                     errorMessageList.add("MIN is invalid.");
/*      */                     
/*      */                     break;
/*      */                   } 
/* 2709 */                   if (validMonthAndYear) {
/*      */                     
/* 2711 */                     validMIN = validMINForBusiness(minTemp, tin, branchCode, userName, userBranchType, "");
/* 2712 */                     validMINForReporting = validMINForReporting(minTemp, inputMonth, inputYear);
/* 2713 */                     validMinNotCancelled = isMinNotCancelled(minTemp, inputMonth + "", inputYear + "");
/*      */                   }
/*      */                   else {
/*      */                     
/* 2717 */                     validMinNotCancelled = true;
/* 2718 */                     validMINForReporting = true;
/*      */                   } 
/*      */                   
/* 2721 */                   if (!validMIN) {
/*      */                     
/* 2723 */                     this.logger.info("ERROR IN MIN - NOT ASSOCIATED TO USER AND BUSINESS");
/* 2724 */                     result = false;
/* 2725 */                     errorMessageList.add("User is not authorized to report for this MIN.");
/*      */                   } 
/*      */                   
/* 2728 */                   if (validMIN && !validMINForReporting) {
/*      */                     
/* 2730 */                     this.logger.info("ERROR IN MIN - NOT ELIGIBLE FOR SALES REPORTING: PERMIT NOT YET EFFECTIVE");
/* 2731 */                     result = false;
/* 2732 */                     errorMessageList.add("Sales report date is invalid. MIN/Permit to Use is not yet active.");
/*      */                   } 
/*      */                   
/* 2735 */                   if (validMIN && validMINForReporting && !validMinNotCancelled) {
/*      */                     
/* 2737 */                     this.logger.info("ERROR IN MIN - NOT ELIGIBLE FOR SALES REPORTING: CANCELLED");
/* 2738 */                     result = false;
/* 2739 */                     errorMessageList.add("Sales report date is invalid. MIN/Permit to Use is not yet active.");
/*      */                   } 
/* 2741 */                   uploadSalesReport.setMIN(minTemp);
/*      */                   break;
/*      */                 case 4:
/* 2744 */                   orTemp = splitContent[4];
/* 2745 */                   this.logger.info("STEVE TRACE : orTemp :" + orTemp + ":" + validSMSORFormat(orTemp));
/* 2746 */                   this.logger.info("STEVE TRACE :getReceiptPrefix(orTemp) :" + getReceiptPrefix(orTemp));
/* 2747 */                   if (validSMSORFormat(orTemp)) {
/*      */ 
/*      */                     
/* 2750 */                     String issuedReceipt = getReceiptPrefix(orTemp);
/* 2751 */                     String receiptNo = parseSMSReceipt(orTemp);
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */                     
/* 2757 */                     if (issuedReceipt.equalsIgnoreCase("O")) {
/*      */                       
/* 2759 */                       uploadSalesReport.setLastORNo(receiptNo); break;
/*      */                     } 
/* 2761 */                     if (issuedReceipt.equalsIgnoreCase("T")) {
/*      */                       
/* 2763 */                       uploadSalesReport.setLastTransactionNo(receiptNo); break;
/*      */                     } 
/* 2765 */                     if (issuedReceipt.equalsIgnoreCase("ICS") || issuedReceipt.equalsIgnoreCase("ICR")) {
/*      */ 
/*      */                       
/* 2768 */                       String[] invoiceContent = StringUtils.split(receiptNo, ';');
/* 2769 */                       this.logger.debug("STEVE TRACE : invoiceContent[1] invoiceContent[0] :receiptNo+::" + invoiceContent[1] + ":" + invoiceContent[0] + ":" + receiptNo);
/* 2770 */                       int icrCount = 0, icsCount = 0;
/* 2771 */                       for (String ICSICR : invoiceContent) {
/*      */                         
/* 2773 */                         if (getReceiptPrefix(ICSICR).equalsIgnoreCase("ICS")) {
/*      */                           
/* 2775 */                           icsCount++;
/* 2776 */                           if (icsCount == 1) {
/* 2777 */                             uploadSalesReport.setLastCashInvoiceNo(ICSICR);
/*      */                           } else {
/*      */                             
/* 2780 */                             this.logger.info("ERROR IN FORMAT OF LAST OR ISSUED F");
/* 2781 */                             result = false;
/* 2782 */                             errorMessageList.add("Incorrect Format.");
/*      */                           }
/*      */                         
/* 2785 */                         } else if (getReceiptPrefix(ICSICR).equalsIgnoreCase("ICR")) {
/*      */                           
/* 2787 */                           icrCount++;
/* 2788 */                           if (icrCount == 1) {
/* 2789 */                             uploadSalesReport.setLastChargeInvoiceNo(ICSICR);
/*      */                           } else {
/*      */                             
/* 2792 */                             this.logger.info("ERROR IN FORMAT OF LAST OR ISSUED D");
/* 2793 */                             result = false;
/* 2794 */                             errorMessageList.add("Incorrect Format.");
/*      */                           } 
/*      */                         } 
/*      */                       } 
/*      */ 
/*      */                       
/*      */                       break;
/*      */                     } 
/*      */                     
/* 2803 */                     this.logger.info("ERROR IN FORMAT OF LAST OR ISSUED D");
/* 2804 */                     result = false;
/* 2805 */                     errorMessageList.add("Incorrect Format.");
/*      */                     
/*      */                     break;
/*      */                   } 
/*      */                   
/* 2810 */                   this.logger.info("ERROR IN FORMAT OF LAST OR ISSUED 3");
/* 2811 */                   result = false;
/* 2812 */                   errorMessageList.add("Last Receipt issued is invalid.");
/*      */                   break;
/*      */ 
/*      */                 
/*      */                 case 5:
/* 2817 */                   if (withVS > 0 || valueToProcess.isEmpty() || valueToProcess.length() < 3) {
/*      */                     
/* 2819 */                     this.logger.info("ERROR IN VATABLE SALES : FORMAT");
/* 2820 */                     result = false;
/* 2821 */                     validDecimal = false;
/* 2822 */                     errorMessageList.add("Incorrect Format.");
/*      */                   }
/*      */                   else {
/*      */                     
/* 2826 */                     String vatableAmount = valueToProcess;
/* 2827 */                     vatableAmount = vatableAmount.substring("VS".length());
/* 2828 */                     if (!isNumericValue(vatableAmount) || !isNumericLengthValid(vatableAmount)) {
/*      */                       
/* 2830 */                       this.logger.info("ERROR IN VATABLE SALES");
/* 2831 */                       result = false;
/* 2832 */                       validDecimal = false;
/* 2833 */                       errorMessageList.add("Vatable Sales is invalid.");
/*      */                     } 
/*      */                   } 
/* 2836 */                   uploadSalesReport.setVatableSales(valueToProcess.replace("VS", ""));
/* 2837 */                   withVS++;
/* 2838 */                   if (validDecimal)
/*      */                   {
/* 2840 */                     totalSales = totalSales.add(new BigDecimal(uploadSalesReport.getVatableSales().replace(",", "")));
/*      */                   }
/*      */                   break;
/*      */                 case 6:
/* 2844 */                   if (withZS > 0 || valueToProcess.isEmpty() || valueToProcess.length() < 3) {
/*      */                     
/* 2846 */                     this.logger.info("ERROR IN VAT ZERO RATED SALES : FORMAT");
/* 2847 */                     result = false;
/* 2848 */                     validDecimal = false;
/* 2849 */                     errorMessageList.add("Incorrect Format.");
/*      */                   }
/*      */                   else {
/*      */                     
/* 2853 */                     String vatZeroAmount = valueToProcess;
/* 2854 */                     vatZeroAmount = vatZeroAmount.substring("VZ".length());
/* 2855 */                     if (!isNumericValue(vatZeroAmount) || !isNumericLengthValid(vatZeroAmount)) {
/*      */                       
/* 2857 */                       this.logger.info("ERROR IN VAT ZERO RATED SALES");
/* 2858 */                       result = false;
/* 2859 */                       validDecimal = false;
/* 2860 */                       errorMessageList.add("Vat Zero-Rated Sales is invalid.");
/*      */                     } 
/*      */                   } 
/* 2863 */                   uploadSalesReport.setVatZeroRatedSales(valueToProcess.replace("VZ", ""));
/* 2864 */                   withZS++;
/* 2865 */                   if (validDecimal) {
/*      */                     
/* 2867 */                     this.logger.info("uploadSalesReport.getVatZeroRatedSales()" + uploadSalesReport.getVatZeroRatedSales());
/* 2868 */                     totalSales = totalSales.add(new BigDecimal(uploadSalesReport.getVatZeroRatedSales().replace(",", "")));
/*      */                   } 
/*      */                   break;
/*      */                 case 7:
/* 2872 */                   if (withVE > 0 || valueToProcess.isEmpty() || valueToProcess.length() < 3) {
/*      */                     
/* 2874 */                     this.logger.info("ERROR IN VAT EXEMPT : FORMAT");
/* 2875 */                     result = false;
/* 2876 */                     validDecimal = false;
/* 2877 */                     errorMessageList.add("Incorrect Format.");
/*      */                   }
/*      */                   else {
/*      */                     
/* 2881 */                     String vatExcemptAmount = valueToProcess;
/* 2882 */                     vatExcemptAmount = vatExcemptAmount.substring("VE".length());
/* 2883 */                     if (!isNumericValue(vatExcemptAmount) || !isNumericLengthValid(vatExcemptAmount)) {
/*      */                       
/* 2885 */                       this.logger.info("ERROR IN VAT EXEMPT");
/* 2886 */                       result = false;
/* 2887 */                       validDecimal = false;
/* 2888 */                       errorMessageList.add("Vat Exempt Sales is invalid.");
/*      */                     } 
/*      */                   } 
/* 2891 */                   uploadSalesReport.setVatExemptSales(valueToProcess.replace("VE", ""));
/* 2892 */                   withVE++;
/* 2893 */                   if (validDecimal)
/*      */                   {
/* 2895 */                     totalSales = totalSales.add(new BigDecimal(uploadSalesReport.getVatExemptSales().replace(",", "")));
/*      */                   }
/*      */                   break;
/*      */                 
/*      */                 case 8:
/* 2900 */                   if (withOT > 0 || valueToProcess.isEmpty() || valueToProcess.length() < 3) {
/*      */                     
/* 2902 */                     this.logger.info("ERROR IN SALES SUBJ TO OTHER TAX : FORMAT");
/* 2903 */                     result = false;
/* 2904 */                     validDecimal = false;
/* 2905 */                     errorMessageList.add("Incorrect Format.");
/*      */                   }
/*      */                   else {
/*      */                     
/* 2909 */                     String subjectToOtherAmount = valueToProcess;
/* 2910 */                     subjectToOtherAmount = subjectToOtherAmount.substring("SO".length());
/* 2911 */                     if (!isNumericValue(subjectToOtherAmount) || !isNumericLengthValid(subjectToOtherAmount)) {
/*      */                       
/* 2913 */                       this.logger.info("ERROR IN SALES SUBJ TO OTHER TAX");
/* 2914 */                       result = false;
/* 2915 */                       validDecimal = false;
/* 2916 */                       errorMessageList.add("Sales subject to other tax is invalid.");
/*      */                     } 
/*      */                   } 
/* 2919 */                   uploadSalesReport.setSalesSubjToOtherTax(valueToProcess.replace("SO", ""));
/* 2920 */                   withOT++;
/* 2921 */                   if (validDecimal)
/*      */                   {
/* 2923 */                     totalSales = totalSales.add(new BigDecimal(uploadSalesReport.getSalesSubjToOtherTax().replace(",", "")));
/*      */                   }
/*      */                   break;
/*      */               } 
/*      */ 
/*      */             
/*      */             } 
/*      */           } 
/*      */         } 
/*      */       }
/*      */     } 
/* 2934 */     uploadSalesReport.setTotalSales(totalSales);
/* 2935 */     uploadSalesReport.setErrorMessageList(errorMessageList);
/* 2936 */     uploadSalesReport.setValidRecord(result);
/*      */     
/* 2938 */     return uploadSalesReport;
/*      */   }
/*      */ 
/*      */   
/*      */   private int getInputIndex(String inputValue) {
/* 2943 */     int returnValue = 0;
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
/* 2960 */     if (inputValue.contains("VS")) {
/* 2961 */       returnValue = 5;
/* 2962 */     } else if (inputValue.contains("VZ")) {
/* 2963 */       returnValue = 6;
/* 2964 */     } else if (inputValue.contains("VE")) {
/* 2965 */       returnValue = 7;
/* 2966 */     } else if (inputValue.contains("SO")) {
/* 2967 */       returnValue = 8;
/* 2968 */     }  return returnValue;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSalesMailReceiver(MailReceiver salesMailReceiver) {
/* 2973 */     this.salesMailReceiver = salesMailReceiver;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSalesReportUploadInvalidEmailMailer(TemplateMailer salesReportUploadInvalidEmailMailer) {
/* 2978 */     this.salesReportUploadInvalidEmailMailer = salesReportUploadInvalidEmailMailer;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSourceFolder(String sourceFolder) {
/* 2983 */     this.sourceFolder = sourceFolder;
/*      */   }
/*      */ 
/*      */   
/*      */   public void processEmailAttachment() {
/* 2988 */     this.logger.debug("BOIR-2878 TRACE : getEmailAttachment : I");
/* 2989 */     this.logger.debug("BOIR-2878 TRACE : sourceFolder : " + this.sourceFolder);
/* 2990 */     List<EmailDetail> emailDetailList = this.salesMailReceiver.getUnreadEmails(this.sourceFolder);
/* 2991 */     this.logger.debug("BOIR-2878 TRACE : number of emails : " + emailDetailList.size());
/* 2992 */     for (EmailDetail emailDetail : emailDetailList) {
/*      */       
/* 2994 */       List<String> userNameList = this.salesReportDao.getUserNameByEmailAddress(parseEmailAddress(emailDetail.getSenderAddress()));
/* 2995 */       this.logger.debug("BOIR-2878 TRACE : userNameList.size() " + userNameList.size());
/* 2996 */       if (userNameList.size() > 0) {
/*      */         
/* 2998 */         List<MailAttachment> attachmentList = emailDetail.getAttachments();
/* 2999 */         for (MailAttachment attachment : attachmentList) {
/*      */           
/* 3001 */           String fileType = attachment.getFileType();
/* 3002 */           this.logger.debug("BOIR-2878 TRACE : attachment.getFilename(); " + attachment.getFilename());
/* 3003 */           if (fileType.equalsIgnoreCase("csv"))
/*      */           {
/* 3005 */             boolean invalidFileName = true;
/* 3006 */             String fileName = attachment.getFilename();
/* 3007 */             if (validFileName(userNameList, fileName.substring(0, fileName.length() - 4))) {
/*      */               
/* 3009 */               this.logger.debug("BOIR-2878 TRACE : validFileName TRUE");
/* 3010 */               invalidFileName = false;
/* 3011 */               processAttachment(attachment, emailDetail.getReceivedDate());
/*      */             } 
/*      */             
/* 3014 */             if (invalidFileName)
/*      */             {
/*      */               
/* 3017 */               this.logger.debug("email attachment has invalid filename.");
/* 3018 */               this.logger.debug("ignoring file: " + fileName);
/*      */             }
/*      */           
/*      */           }
/*      */         
/*      */         } 
/*      */       } else {
/*      */         
/* 3026 */         String[] send = new String[1];
/* 3027 */         send[0] = parseEmailAddress(emailDetail.getSenderAddress());
/* 3028 */         this.salesReportUploadInvalidEmailMailer.send(null, send);
/*      */       } 
/*      */       
/* 3031 */       this.salesMailReceiver.markRead(emailDetail, this.sourceFolder);
/* 3032 */       this.logger.debug("BOIR-2878 TRACE : getEmailAttachment : F");
/*      */     } 
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean validFileName(List<String> userNameList, String fileName) {
/* 3038 */     for (String userName : userNameList) {
/*      */       
/* 3040 */       if (fileName.equalsIgnoreCase(userName.trim()))
/*      */       {
/* 3042 */         return true;
/*      */       }
/*      */     } 
/* 3045 */     return false;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   private void processAttachment(MailAttachment attachment, Date dateSubmitted) {
/* 3051 */     this.logger.info("===============PROCESSING ATTACHMENT============================");
/*      */     
/* 3053 */     SimpleDateFormat sdfDate = new SimpleDateFormat("MMddyyyy");
/* 3054 */     String dateInString = sdfDate.format(dateSubmitted);
/* 3055 */     String pathHolder = basePathConfigurer(this.salesReportUpload + File.separator);
/*      */     
/* 3057 */     String attachmentName = attachment.getFilename();
/* 3058 */     String fileName = attachmentName.substring(0, attachmentName.length() - 4);
/* 3059 */     File initialName = new File(pathHolder + dateInString + "_" + fileName + "_" + "044" + ".csv");
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 3067 */     File tempFile = renameFile(initialName, pathHolder, ".csv");
/*      */ 
/*      */     
/*      */     try {
/* 3071 */       FileUtils.writeByteArrayToFile(tempFile, attachment.getFile());
/*      */     }
/* 3073 */     catch (IOException e) {
/*      */       
/* 3075 */       this.logger.error(e.getMessage());
/*      */     } 
/* 3077 */     this.logger.info("===============PROCESSING ATTACHMENT======" + fileName + "=====DONE=================");
/*      */   }
/*      */ 
/*      */   
/*      */   private String parseEmailAddress(String senderAddress) {
/* 3082 */     String emailAddress = senderAddress;
/*      */     
/* 3084 */     int firstPos = senderAddress.indexOf('<', 0);
/*      */     
/* 3086 */     int lastPos = senderAddress.lastIndexOf('>');
/*      */     
/* 3088 */     if (firstPos != -1 && lastPos != -1)
/*      */     {
/* 3090 */       emailAddress = senderAddress.substring(firstPos + 1, lastPos);
/*      */     }
/* 3092 */     return emailAddress;
/*      */   }
/*      */ 
/*      */   
/*      */   private boolean sameSalesReport(SalesReport salesReport, SalesReport other) {
/* 3097 */     String lastOR = salesReport.getLastORNo();
/* 3098 */     String lastChargeNo = salesReport.getLastChargeInvoiceNo();
/* 3099 */     String lastCashNo = salesReport.getLastCashInvoiceNo();
/* 3100 */     String lastTransNo = salesReport.getLastTransactionNo();
/* 3101 */     BigDecimal vatableSales = salesReport.getVatableSales();
/* 3102 */     BigDecimal vatZeroRatedSales = salesReport.getVatZeroRatedSales();
/* 3103 */     BigDecimal vatExemptSales = salesReport.getVatExemptSales();
/* 3104 */     BigDecimal salesSubjToOtherTax = salesReport.getSalesSubjToOtherTax();
/*      */     
/* 3106 */     if (salesReport.getBusinessTIN().equals(other.getBusinessTIN()) && salesReport.getBranchCode().equals(other.getBranchCode()) && salesReport.getMonth() == other.getMonth() && salesReport.getYear() == other.getYear() && (lastOR == other.getLastORNo() || (lastOR != null && lastOR.equals(other.getLastORNo()))) && (lastChargeNo == other.getLastChargeInvoiceNo() || (lastChargeNo != null && lastChargeNo.equals(other.getLastChargeInvoiceNo()))) && (lastCashNo == other.getLastCashInvoiceNo() || (lastCashNo != null && lastCashNo.equals(other.getLastCashInvoiceNo()))) && (lastTransNo == other.getLastTransactionNo() || (lastTransNo != null && lastTransNo.equals(other.getLastTransactionNo()))) && vatableSales.doubleValue() == other.getVatableSales().doubleValue() && vatZeroRatedSales.doubleValue() == other.getVatZeroRatedSales().doubleValue() && vatExemptSales.doubleValue() == other.getVatExemptSales().doubleValue() && salesSubjToOtherTax.doubleValue() == other.getSalesSubjToOtherTax().doubleValue())
/*      */     {
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
/* 3123 */       return true;
/*      */     }
/*      */ 
/*      */     
/* 3127 */     return false;
/*      */   }
/*      */ 
/*      */ 
/*      */   
/*      */   public void setSmsReplyTransactionFailed(String smsReplyTransactionFailed) {
/* 3133 */     this.smsReplyTransactionFailed = smsReplyTransactionFailed;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSmsReplyReportFormat(String smsReplyReportFormat) {
/* 3138 */     this.smsReplyReportFormat = smsReplyReportFormat;
/*      */   }
/*      */ 
/*      */   
/*      */   public void setSmsReplyUnauthorized(String smsReplyUnauthorized) {
/* 3143 */     this.smsReplyUnauthorized = smsReplyUnauthorized;
/*      */   }
/*      */ 
/*      */   
/*      */   private String getReceiptPrefix(String value) {
/* 3148 */     String returnValue = "";
/* 3149 */     if (!value.isEmpty())
/*      */     {
/*      */       
/* 3152 */       if (value.subSequence(0, "O".length()).equals("O")) {
/* 3153 */         returnValue = "O";
/*      */       }
/* 3155 */       else if (value.subSequence(0, "T".length()).equals("T")) {
/* 3156 */         returnValue = "T";
/*      */       }
/* 3158 */       else if (value.subSequence(0, "ICR".length()).equals("ICR")) {
/* 3159 */         returnValue = "ICR";
/*      */       }
/* 3161 */       else if (value.subSequence(0, "ICS".length()).equals("ICS")) {
/* 3162 */         returnValue = "ICS";
/*      */       }  } 
/* 3164 */     return returnValue;
/*      */   }
/*      */ 
/*      */   
/*      */   public boolean validSMSORFormat(String value) {
/* 3169 */     boolean result = false;
/* 3170 */     if (!value.isEmpty()) {
/*      */       
/* 3172 */       String[] invoiceContent = StringUtils.split(value, ';');
/*      */       
/* 3174 */       if (invoiceContent.length == 2) {
/*      */         
/* 3176 */         String invoiceChecker = getReceiptPrefix(invoiceContent[0]);
/*      */         
/* 3178 */         if (invoiceChecker.equalsIgnoreCase("ICS"))
/*      */         {
/* 3180 */           result = getReceiptPrefix(invoiceContent[1]).equalsIgnoreCase("ICR");
/*      */         }
/* 3182 */         else if (invoiceChecker.equalsIgnoreCase("ICR"))
/*      */         {
/* 3184 */           result = getReceiptPrefix(invoiceContent[1]).equalsIgnoreCase("ICS");
/*      */         }
/* 3186 */         else if (invoiceChecker.equalsIgnoreCase("O") || invoiceChecker.equalsIgnoreCase("T"))
/*      */         {
/*      */           
/* 3189 */           result = (invoiceChecker.equalsIgnoreCase("O") || invoiceChecker.equalsIgnoreCase("T"));
/*      */         
/*      */         }
/*      */       
/*      */       }
/*      */       else {
/*      */         
/* 3196 */         String invoiceChecker = getReceiptPrefix(value);
/* 3197 */         if (invoiceChecker.equalsIgnoreCase("O") || invoiceChecker.equalsIgnoreCase("ICS") || invoiceChecker.equalsIgnoreCase("ICR") || invoiceChecker.equalsIgnoreCase("T"))
/*      */         {
/*      */ 
/*      */ 
/*      */           
/* 3202 */           result = (invoiceChecker.equalsIgnoreCase("O") || invoiceChecker.equalsIgnoreCase("ICS") || invoiceChecker.equalsIgnoreCase("ICR") || invoiceChecker.equalsIgnoreCase("T"));
/*      */         }
/*      */       } 
/*      */     } 
/*      */ 
/*      */ 
/*      */ 
/*      */     
/* 3210 */     return result;
/*      */   }
/*      */ 
/*      */   
/*      */   public String parseSMSReceipt(String value) {
/* 3215 */     String receipt = "";
/*      */
/* 3217 */     String receiptReference = getReceiptPrefix(value);
/*      */
/* 3219 */     if (receiptReference.equalsIgnoreCase("O") || receiptReference.equalsIgnoreCase("T")) {
/*      */
/*      */
/* 3222 */       receipt = value.substring(receiptReference.length());
/*      */     }
/* 3224 */     else if (receiptReference.equalsIgnoreCase("ICS") || receiptReference.equalsIgnoreCase("ICR")) {
/*      */
/*      */
/*      */
/* 3228 */       String[] invoiceContent = StringUtils.split(value, ';');
/* 3229 */       String tPrefix = "";
/* 3230 */       if (invoiceContent.length == 2) {
/*      */
/* 3232 */         tPrefix = getReceiptPrefix(invoiceContent[0]);
/* 3233 */         receipt = invoiceContent[0].substring(receipt.length()) + ";";
/*      */
/* 3235 */         tPrefix = getReceiptPrefix(invoiceContent[1]);
/* 3236 */         receipt = receipt + invoiceContent[1].substring(tPrefix.length());
/*      */       }
/*      */       else {
/*      */
/* 3240 */         tPrefix = getReceiptPrefix(invoiceContent[0]);
/* 3241 */         receipt = invoiceContent[0].substring(receipt.length());
/*      */       }
/*      */     }
/*      */
/* 3245 */     return receipt;
/*      */   }
/*      */
/*      */   private static final class SalesReportProcessingContext
/*      */   {
/* 3249 */     private final Map<String, Integer> businessBranchCountCache = new HashMap<String, Integer>();
/* 3250 */     private final Map<String, Business> businessByTinCache = new HashMap<String, Business>();
/* 3251 */     private final Map<String, Business> businessByTinBranchCache = new HashMap<String, Business>();
/* 3252 */     private final Map<String, Business> userBusinessCache = new HashMap<String, Business>();
/* 3253 */     private final Map<String, List<Branch>> userBranchListCache = new HashMap<String, List<Branch>>();
/* 3254 */     private final Map<String, Boolean> userTinAuthorizationCache = new HashMap<String, Boolean>();
/* 3255 */     private final Map<String, Boolean> userBranchAuthorizationCache = new HashMap<String, Boolean>();
/* 3256 */     private final Map<String, Boolean> minBusinessValidationCache = new HashMap<String, Boolean>();
/* 3257 */     private final Map<String, Date> minEffectivityCache = new HashMap<String, Date>();
/* 3258 */     private final Map<String, Boolean> minEffectivityNullCache = new HashMap<String, Boolean>();
/* 3259 */     private final Map<String, Date> minCancellationCache = new HashMap<String, Date>();
/* 3260 */     private final Map<String, Boolean> minCancellationNullCache = new HashMap<String, Boolean>();
/* 3261 */     private final Map<String, String> rdoCodeCache = new HashMap<String, String>();
/* 3262 */     private final Map<String, Boolean> minReportingEligibilityCache = new HashMap<String, Boolean>();
/* 3263 */     private final Map<String, Boolean> minCancellationEligibilityCache = new HashMap<String, Boolean>();
/* 3264 */     private final Map<String, Deque<Integer>> sequenceBlockCache = new HashMap<String, Deque<Integer>>();
/* 3265 */     private final int sequencePrefetchSize;
/*      */
/*      */     private SalesReportProcessingContext() {
/* 3268 */       this.sequencePrefetchSize = DEFAULT_SEQUENCE_BLOCK_SIZE;
/*      */     }

/*      */   }

/*      */   private static interface SalesReportDaoBatchSupport
/*      */   {
/*      */     Map<String, Date> getEffectivityDatesOfPermits(Collection<String> paramCollection);
/*      */
/*      */     Map<String, Date> getCancellationDates(Collection<String> paramCollection);
/*      */   }

/*      */   private static interface SequenceDaoBlockSupport
/*      */   {
/*      */     List<Integer> getTransactionBlock(String paramString, int paramInt);
/*      */   }
/*      */ }
}


/* Location:              C:\Users\asd.user.BIR-ADNEW\Downloads\eARS_WAR\eARS_WAR\SALES.war!\WEB-INF\lib\acrsal-api-1.6.0-SNAPSHOT.jar!\com\indracompany\acrsal\main\salesreport\SalesReportServiceImpl.class
 * Java compiler version: 6 (50.0)
 * JD-Core Version:       1.1.3
 */