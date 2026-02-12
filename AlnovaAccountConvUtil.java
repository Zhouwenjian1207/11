package com.hkbea.microservice.ich.core.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;

import com.hkbea.util.xaa.log.Logger;
import com.hkbea.util.xaa.log.LoggerManager;

public class AlnovaAccountConvUtil {

	 /*
	   AccountConvUtil is copied from TC3 function TC6CHA10, but oAccountConvUtilc format between BEA external format and Alnova format.
	   If conversion result is empty, please call TCECHA10Service to get real-time result from TC3
	 */
	private static final Logger logger = LoggerManager.getLogger(AlnovaAccountConvUtil.class);

	public final static String AIO_TYPE_AIO_ACC = "10";
	public final static String AIO_TYPE_SSA_ACC = "25";
	public final static String AIO_TYPE_TDS_ACC = "13";

	// Valid Length of Account Type
	public final static int SW_EXP_LEN_EXT_OTHER = 14;
	public final static int SW_EXP_LEN_EXT_ND = 15;
	public final static int SW_EXP_LEN_EXT_NDS = 16;
	public final static int SW_EXP_LEN_ALN = 18;

	// Valid Account Type
	public final static String ACCT_FMT_EXT = "EXT";
	public final static String ACCT_FMT_EXT_OTHER = "EXT_OTHER";
	public final static String ACCT_FMT_EXT_ND = "EXT_ND";
	public final static String ACCT_FMT_EXT_NDS = "EXT_NDS";
	public final static String ACCT_FMT_ALN = "ALN";

	// Valid Target AC Type
	public final static String EHA10_SW_TAR_TYP_MAS = "10";
	public final static String EHA10_SW_TAR_TYP_UCU = "11";
	public final static String EHA10_SW_TAR_TYP_CUR = "12";
	public final static String EHA10_SW_TAR_TYP_TDS = "13";
	public final static String EHA10_SW_TAR_TYP_SSA = "15";
	
	// Valid AIO Type
	public final static String EHA10_SW_AIO_MAS = "10";
	public final static String EHA10_SW_AIO_UCU = "50";
	public final static String EHA10_SW_AIO_CUR = "40";
	public final static String EHA10_SW_AIO_TDS = "13";
	public final static String EHA10_SW_AIO_SSA = "25";

	// Valid AC Format
	public final static String EHA30_SW_ACC_FMT_ALN = "01";
	public final static String EHA30_SW_ACC_FMT_INT = "02";
	public final static String EHA30_SW_ACC_FMT_EXT = "03";
	public final static String EHA30_SW_ACC_FMT_ATM = "04";
	public final static String EHA30_SW_ACC_FMT_ECG = "05";
	public final static String EHA30_SW_ACC_FMT_CLG = "06";

	public final static String EHA30_EXISTING_DEPOSIT = "01";
	public final static String EHA30_EXISTING_AIO = "02";
	public final static String EHA30_NEW_DEPOSIT = "03";
	public final static String EHA30_NEW_AIO = "04";
	
	//*** TCWCHA30 AIO Product Code Tablle
	public final static String SW_PROD_CODE_CYB_85 = "85";    //AIO - RESERVED
	public final static String SW_PROD_CODE_CYB_87 = "87";    //AIO - RESERVED
	public final static String SW_PROD_CODE_CYB_89 = "89";    //AIO - RESERVED
	public final static String SW_PROD_CODE_CYB_93 = "93";    //AIO - RESERVED
	public final static String SW_PROD_CODE_CYB_95 = "95";    //AIO - RESERVED
	public final static String SW_PROD_CODE_CYB_90 = "90";    //ATM CHIP CARD A/C

	public static Map<String, String> convert(String origAcc, String origAccFmt, String targetAccFmt, String targetAccType) {
		
		Map<String, String> accountConvResult = new HashMap<String, String>();
		
		accountConvResult.put("errMsg", "");
		// Check Original Account Format
		if (!(origAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_ALN) 
				|| origAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_EXT))) {
			accountConvResult.put("errMsg", "invalid origAccFmt " + origAccFmt + ", it must be " + EHA30_SW_ACC_FMT_ALN + " or " + EHA30_SW_ACC_FMT_EXT);
			return accountConvResult;
		}

		// Check Target Account Format
		if (!(targetAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_ALN)
				|| targetAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_EXT))) {
			accountConvResult.put("errMsg", "invalid targetAccFmt " + targetAccFmt + ", it must be " + EHA30_SW_ACC_FMT_ALN + " or " + EHA30_SW_ACC_FMT_EXT);
			return accountConvResult;
		}

		// Check Target Account Format
		if (origAccFmt.equalsIgnoreCase(targetAccFmt)){
			accountConvResult.put("errMsg", "origAccFmt " + origAccFmt + " is same with  targetAccFmt " + targetAccFmt);
			return accountConvResult;
		}
		
		// Check Orginal Account
		if(!StringUtils.isNumeric(StringUtils.trim(origAcc))) {
			accountConvResult.put("errMsg", "origAcc is not numeric");
			return accountConvResult;
		}
		
		//from CMHCNVCM, skip to convert non "015" acct
		if (origAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_EXT) && targetAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_ALN)){
			String bankCode = StringUtils.trim(origAcc).substring(0, 3);
			if (!"015".equalsIgnoreCase(bankCode)) {
				accountConvResult.put("tarAccAioInd","N");
				accountConvResult.put("tarAccTyp", "");
				accountConvResult.put("tarAcc",origAcc);
				return accountConvResult;
			}
		}
		
		//from CMHCNVCM, skip to convert non "0015" acct
		if (origAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_ALN) && targetAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_EXT)){
			String bankCode = StringUtils.trim(origAcc).substring(0, 4);
			if (!"0015".equalsIgnoreCase(bankCode)) {
				accountConvResult.put("tarAccAioInd","N");
				accountConvResult.put("tarAccTyp", "");
				accountConvResult.put("tarAcc",origAcc);
				return accountConvResult;
			}
		}

		int origAccLen = StringUtils.trim(origAcc).length();
		if (!(origAccLen == SW_EXP_LEN_EXT_OTHER||origAccLen == SW_EXP_LEN_EXT_ND||origAccLen == SW_EXP_LEN_EXT_NDS||origAccLen == SW_EXP_LEN_ALN)) {
			accountConvResult.put("errMsg", "invalid length of origAcc is " + origAccLen );
			return accountConvResult;
		}
		
		if (origAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_ALN)){
			if (origAccLen != SW_EXP_LEN_ALN) {
				accountConvResult.put("errMsg", "invalid length of alnova account is " + origAccLen );
				return accountConvResult;
			}
		}

		origAcc = (StringUtils.trim(origAcc) + "                  ").substring(0, 18);
		
		// *** FROM TC6CHA10
		String targetAcc = "";

		// get Alnova Acct Type in TC8CHA30
		String alnovaAcctType = getAccType(origAcc, origAccFmt);
		if (null == alnovaAcctType || alnovaAcctType.isEmpty()) {
			accountConvResult.put("errMsg", "invalid AlnovaAcctType for a/c " + origAcc + " and fmt " + origAccFmt);
			return accountConvResult;
		}
		
		logger.info("convert() : alnovaAcctType : " + alnovaAcctType + "\n");

		// Check account length
		// Alnova acc : 18
		// BEA External Acc of new deposit : 15
		// BEA External Acc of new supressed deposit : 16
		// BEA External acc : 14

		if (!checkOrigAccLen(origAcc, origAccFmt, alnovaAcctType)) {
			accountConvResult.put("errMsg", "invalid Orig Acc Len\n");
			return accountConvResult;
		}

		// Check Target Account Type
		if (targetAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_ALN)) {
			if (alnovaAcctType.equalsIgnoreCase(EHA30_EXISTING_AIO) || alnovaAcctType.equalsIgnoreCase(EHA30_NEW_AIO)) {
				if (!checkTargetAccType(targetAccType)) {
					accountConvResult.put("errMsg", "invalid Target Acc Type");
					return accountConvResult;
				}
			}
		}

		// *** 310000-CONVERSION
		if (origAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_ALN) && targetAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_EXT)) {
			if (alnovaAcctType.equalsIgnoreCase(EHA30_NEW_DEPOSIT)) {
				accountConvResult.put("errMsg", "EHA30_NEW_DEPOSIT cannot identify NDS or ND account from alnova format, skip conversion");
				return accountConvResult;
			}
			targetAcc = aln2ext(origAcc, alnovaAcctType);
		}

		if (origAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_EXT) && targetAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_ALN)) {
			targetAcc = ext2aln(origAcc, alnovaAcctType, targetAccType);
			// *** 390000-SET-AIO-SUB-ACC-TYP
			if (alnovaAcctType.equalsIgnoreCase(EHA30_EXISTING_AIO) || alnovaAcctType.equalsIgnoreCase(EHA30_NEW_AIO)) {
				targetAcc = setAIOSubAccType(targetAcc);
			}
		}
		
		if (alnovaAcctType.equalsIgnoreCase(EHA30_EXISTING_AIO) || alnovaAcctType.equalsIgnoreCase(EHA30_NEW_AIO)) {
			accountConvResult.put("tarAccAioInd","Y");
			if (origAccFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_ALN)) {
				accountConvResult.put("tarAccTyp", getTarAccTyp(origAcc));
			}else {
				accountConvResult.put("tarAccTyp", getTarAccTyp(targetAcc));
			}
		}else {
			accountConvResult.put("tarAccAioInd","N");
			accountConvResult.put("tarAccTyp", "");
		}

		accountConvResult.put("tarAcc",targetAcc);
		return accountConvResult;
	}

	public static String getTarTypeByAioType(String aioType) {
		String tarType = "";
		if (aioType.equalsIgnoreCase(EHA10_SW_AIO_MAS)) {
			return EHA10_SW_TAR_TYP_MAS;
		}
		
		if (aioType.equalsIgnoreCase(EHA10_SW_AIO_UCU)) {
			return EHA10_SW_TAR_TYP_UCU;
		}

		if (aioType.equalsIgnoreCase(EHA10_SW_AIO_CUR)) {
			return EHA10_SW_TAR_TYP_CUR;
		}
		
		if (aioType.equalsIgnoreCase(EHA10_SW_AIO_TDS)) {
			return EHA10_SW_TAR_TYP_TDS;
		}
		
		if (aioType.equalsIgnoreCase(EHA10_SW_AIO_SSA)) {
			return EHA10_SW_TAR_TYP_SSA;
		}

		return tarType;
	}
	
	private static boolean checkOrigAccLen(String acc, String accFmt, String accType) {
		// *** FROM TC6CHA10
		// 240000-ORG-ACC-LEN
		boolean result = false;
		if (accFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_ALN)) {
			return checkAlnAccLen(acc);
		}

		if (accFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_EXT)) {
			return checkExtAccLen(acc, accType);
		}

		return result;
	}

	private static boolean checkAlnAccLen(String acc) {
		boolean result = false;
		if (acc.length() == SW_EXP_LEN_ALN) {
			result = true;
		}
		return result;
	}

	private static boolean checkExtAccLen(String acc, String accType) {
		boolean result = false;
		if (accType.equalsIgnoreCase(EHA30_NEW_DEPOSIT)) {
			if (acc.substring(4, 4).equals("1") && acc.trim().length() == SW_EXP_LEN_EXT_NDS) {
				result = true;
			} else if (acc.trim().length() == SW_EXP_LEN_EXT_ND) {
				result = true;
			}
		} else {
			if (acc.trim().length() == SW_EXP_LEN_EXT_OTHER) {
				result = true;
			}
		}
		return result;
	}


	private static String getAccType(String acc, String accFmt) {
		// *** FROM TC8CHA30
		// 320000-CALL-TC8CHA30
		return getAccTypeInTC8CHA30(acc, accFmt);

	}

	private static String getAccTypeInTC8CHA30(String acc, String accFmt) {
		// *** FROM TC8CHA30
		// *** 300000-SPECIFIC-PROCESS
		if (accFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_ALN)) {
			return getAlnAccTypeInTC8CHA30(acc);
		}

		if (accFmt.equalsIgnoreCase(EHA30_SW_ACC_FMT_EXT)) {
			return getExtAccTypeInTC8CHA30(acc);
		}

		return "";

	}

	private static String getAlnAccTypeInTC8CHA30(String acc) {
		// *** FROM TC8CHA30
		// *** 310000-ALN-INTERNAL-FMT
		
		if (acc.substring(8, 10).equals("00")) {
			return EHA30_EXISTING_DEPOSIT;
		}

		if (acc.substring(10, 11).equals("0")) {
			return EHA30_NEW_DEPOSIT;
		}
		// TBD
		if (!acc.substring(4, 8).equals("0000")) {
			String alnovaFamilyCodeIndicator = getAlnovaFamilyCodeIndicator(acc.substring(8, 10));
			if ("N".equalsIgnoreCase(alnovaFamilyCodeIndicator)) {
				return EHA30_EXISTING_AIO;
			}
			if ("Y".equalsIgnoreCase(alnovaFamilyCodeIndicator)) {
				return EHA30_NEW_AIO;
			}
		}
		return "";
	}

	private static String getExtAccTypeInTC8CHA30(String acc) {
		// *** FROM TC8CHA30
		// *** 330000-BEA-EXTERNAL-FMT
		logger.info("acc.substring(14, 18) = " + acc.substring(14, 18) + "\n");
		if (!acc.substring(14, 18).equalsIgnoreCase("    ")) {
			return EHA30_NEW_DEPOSIT;
		}
		// TBD
		if (!acc.substring(4, 7).equals("000")) {
			System.out.print("acc.substring(6, 8) = " + acc.substring(6, 8) + "\n");
			String alnovaFamilyCodeIndicator = getAlnovaFamilyCodeIndicator(acc.substring(6, 8));
			if ("N".equalsIgnoreCase(alnovaFamilyCodeIndicator)) {
				return EHA30_EXISTING_AIO;
			}
			if ("Y".equalsIgnoreCase(alnovaFamilyCodeIndicator)) {
				return EHA30_NEW_AIO;
			}
		}
		return EHA30_EXISTING_DEPOSIT;

	}

	private static String aln2ext(String alnAcc, String accType) {
		String extAcct = "";
		logger.info("alnAcc = " + alnAcc + "\n");
		if (accType.equalsIgnoreCase(EHA30_EXISTING_DEPOSIT)) {
			extAcct = aln2extForExistDeposit(alnAcc);
		} else if (accType.equalsIgnoreCase(EHA30_NEW_DEPOSIT)) {
			logger.info("EHA30_NEW_DEPOSIT cannot identify NDS or ND account from alnova format, skip conversion");
		} else if (accType.equalsIgnoreCase(EHA30_EXISTING_AIO) || accType.equalsIgnoreCase(EHA30_NEW_AIO)) {
			extAcct = aln2extForAIO(alnAcc);
		}
		return extAcct;
	}

	public static String ext2aln(String extAcc, String accType, String targetAccType) {
		String alnAcc = "";

		if (accType.equalsIgnoreCase(EHA30_EXISTING_DEPOSIT)) {
			alnAcc = ext2alnForExistDeposit(extAcc);
		} else if (accType.equalsIgnoreCase(EHA30_NEW_DEPOSIT)) {
			if (alnAcc.length() == 16) {
				alnAcc = ext2alnForNewSuspensDeposit(extAcc);
			} else {
				alnAcc = ext2alnForNewDeposit(extAcc);
			}
		} else if (accType.equalsIgnoreCase(EHA30_EXISTING_AIO) || accType.equalsIgnoreCase(EHA30_NEW_AIO)) {
			alnAcc = ext2alnForAIO(extAcc, targetAccType);
		}
		return alnAcc;
	}

	private static String aln2extForNewSuspensDeposit(String acct) {
		// *** FROM TC6CHA10
		// *** NEW DEPOSIT SUSPENSE ACCOUNT IN BEA EXTERNAL FORMAT (3-4-2-7)
		// *** LENGTH = 16
		StringBuilder extAcct = new StringBuilder();
		extAcct.append(acct.substring(1, 4));
		extAcct.append(acct.substring(4, 8));
		extAcct.append(acct.substring(8, 10));
		extAcct.append(acct.substring(11, 18));
		logger.info("acct.substring(1, 4) = " + acct.substring(1, 4)+ "\n");
		logger.info("acct.substring(4, 8) = " + acct.substring(4, 8)+ "\n");
		logger.info("acct.substring(8, 10) = " + acct.substring(8, 10)+ "\n");
		logger.info("acct.substring(11, 18) = " + acct.substring(11, 18)+ "\n");
		

		return extAcct.toString();
	}

	private static String ext2alnForNewSuspensDeposit(String acct) {
		// *** FROM TC6CHA10
		// *** NEW DEPOSIT SUSPENSE ACCOUNT IN BEA EXTERNAL FORMAT (3-4-2-7)
		// *** LENGTH = 16

		StringBuilder alnAcct = new StringBuilder();
		alnAcct.append("0" + acct.substring(0, 3));
		alnAcct.append(acct.substring(3, 7));
		alnAcct.append(acct.substring(7, 9));
		alnAcct.append("0" + acct.substring(9, 16));

		return alnAcct.toString();
	}

	private static String aln2extForNewDeposit(String acct) {
		// *** FROM TC6CHA10
		// *** NEW DEPOSIT ACCOUNT IN BEA EXTERNAL FORMAT (3-3-2-7)
		// *** LENGTH = 15

		StringBuilder extAcct = new StringBuilder();
		extAcct.append(acct.substring(1, 4));
		extAcct.append(acct.substring(5, 8));
		extAcct.append(acct.substring(8, 10));
		extAcct.append(acct.substring(11, 18));

		return extAcct.toString();
	}

	private static String ext2alnForNewDeposit(String acct) {
		// *** FROM TC6CHA10
		// *** NEW DEPOSIT ACCOUNT IN BEA EXTERNAL FORMAT (3-3-2-7)
		// *** LENGTH = 15

		StringBuilder alnAcct = new StringBuilder();
		alnAcct.append("0" + acct.substring(0, 3));
		alnAcct.append("0" + acct.substring(3, 6));
		alnAcct.append(acct.substring(6, 8));
		alnAcct.append("0" + acct.substring(8, 15));

		return alnAcct.toString();
	}

	private static String aln2extForExistDeposit(String acct) {
		// *** FROM TC6CHA10
		// *** EXISTING DEPOSIT ACCOUNT IN BEA EXTERNAL FORMAT (3-3-8)
		// *** LENGTH = 14

		StringBuilder extAcct = new StringBuilder();
		extAcct.append(acct.substring(1, 4));
		extAcct.append(acct.substring(5, 8));
		extAcct.append(acct.substring(10, 18));

		return extAcct.toString();
	}

	private static String ext2alnForExistDeposit(String acct) {
		// *** FROM TC6CHA10
		// *** EXISTING DEPOSIT ACCOUNT IN BEA EXTERNAL FORMAT (3-3-8)
		// *** LENGTH = 14

		StringBuilder alnAcct = new StringBuilder();
		alnAcct.append("0" + acct.substring(0, 3));
		alnAcct.append("0" + acct.substring(3, 6));
		alnAcct.append("00");
		alnAcct.append(acct.substring(6, 14));

		return alnAcct.toString();
	}

	private static String aln2extForAIO(String acct) {
		// *** FROM TC6CHA10
		// *** AIO ACCOUNT IN BEA EXTERNAL FORMAT (3-3-2-6)
		// *** LENGTH = 14

		StringBuilder extAcct = new StringBuilder();
		extAcct.append(acct.substring(1, 4));
		extAcct.append(acct.substring(5, 8));
		extAcct.append(acct.substring(8, 10));
		extAcct.append(acct.substring(12, 18));

		return extAcct.toString();
	}

	private static String ext2alnForAIO(String acct, String acctType) {
		// *** FROM TC6CHA10
		// *** AIO ACCOUNT IN BEA EXTERNAL FORMAT (3-3-2-6)
		// *** LENGTH = 14

		StringBuilder alnAcct = new StringBuilder();
		alnAcct.append("0" + acct.substring(0, 3));
		alnAcct.append("0" + acct.substring(3, 6));
		alnAcct.append(acct.substring(6, 8));
		alnAcct.append(acctType + acct.substring(8, 14));

		return alnAcct.toString();
	}

	private static String getAlnovaFamilyCodeIndicator(String prodCode) {
		//*** TCWCHA30
		String result = "";
		HashMap<String,String> alnovaAioTable = loadAlnovaAioProdTable();
		if (alnovaAioTable.containsKey(prodCode)) {
			return alnovaAioTable.get(prodCode).substring(2, 3);
		}
		return result;
	}

	private static HashMap<String,String> loadAlnovaAioProdTable() {
		//*** ENDV.MF03.CBSP.COPYLIB(TCWCHA30)
		HashMap<String,String> alnovaAioTable = new HashMap<String,String>();
		alnovaAioTable.put("33", "33NAIO - SUPREME GOLD ACCOUNT    ");
		alnovaAioTable.put("78", "78NAIO - I-ACCOUNT               ");
		alnovaAioTable.put("79", "79NAIO - I-ACCOUNT (RESERVED)    ");
		alnovaAioTable.put("81", "81NBEA PRIVATE BANKING ACCOUNT   ");
		alnovaAioTable.put("82", "82NAIO - SUPREME ACCOUNT         ");
		alnovaAioTable.put("85", "85NAIO - RESERVED                ");
		alnovaAioTable.put("87", "87NAIO - RESERVED                ");
		alnovaAioTable.put("89", "89NAIO - RESERVED                ");
		alnovaAioTable.put("93", "93NAIO - RESERVED                ");
		alnovaAioTable.put("88", "88YAIO                           ");
		alnovaAioTable.put("68", "68YCORPORATE AIO                 ");
		alnovaAioTable.put("95", "95NAIO - RESERVED                ");
		alnovaAioTable.put("90", "90NAIO - RESERVED                ");
		return alnovaAioTable;
	}
	

	public static boolean checkTargetAccType(String targetAccType) {
		// *** FROM TC6CHA10
		// ***390000-SET-AIO-SUB-ACC-TYP
		boolean result = false;
		List<String> accTypeArray = new ArrayList<String>();
		accTypeArray.add(EHA10_SW_TAR_TYP_MAS);
		accTypeArray.add(EHA10_SW_TAR_TYP_UCU);
		accTypeArray.add(EHA10_SW_TAR_TYP_CUR);
		accTypeArray.add(EHA10_SW_TAR_TYP_TDS);
		accTypeArray.add(EHA10_SW_TAR_TYP_SSA);
		for (String accType : accTypeArray) {
			if (targetAccType.equals(accType)) {
				return true;
			}
		}
		return result;
	}

	private static String setAIOSubAccType(String targetAcc) {
		// *** FROM TC6CHA10
		// ***390000-SET-AIO-SUB-ACC-TYP
		String prodCode = "";
		String branchCode = "";
		String newtargetAcc = targetAcc;
		prodCode = targetAcc.substring(8, 10);
		branchCode = targetAcc.substring(4, 8);
		if (checkCybProd(prodCode)) {
			if (!branchCode.equalsIgnoreCase("0000")) {
				newtargetAcc = "" + targetAcc.substring(0, 10) + "10" + targetAcc.substring(12, 18);
			}
		}
		return newtargetAcc;
	}
	
	private static String getTarAccTyp(String alnovaAcc) {
		// *** FROM TC6CHA10
		// ***390000-SET-AIO-SUB-ACC-TYP
		return alnovaAcc.substring(10, 12);
	}
	

	private static boolean checkCybProd(String prodCode) {
		boolean result = false;
		List<String> cybProdArray = new ArrayList<String>();
		cybProdArray.add(SW_PROD_CODE_CYB_85);
		cybProdArray.add(SW_PROD_CODE_CYB_87);
		cybProdArray.add(SW_PROD_CODE_CYB_89);
		cybProdArray.add(SW_PROD_CODE_CYB_93);
		cybProdArray.add(SW_PROD_CODE_CYB_95);
		cybProdArray.add(SW_PROD_CODE_CYB_90);
		for (String cybProdCode : cybProdArray) {
			if (prodCode.equals(cybProdCode)) {
				return true;
			}
		}
		return result;
	}
	
	private static String removeTrailingSpaces(String str) {
		return str.replaceAll("\\s+$", "");
	}
}


