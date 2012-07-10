package com.villemos.ispace.assembler.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.camel.Exchange;
import org.apache.commons.logging.Log;

import org.apache.commons.logging.LogFactory;

import com.villemos.ispace.aperture.InformationObject;

public class ReferenceIdBodyEnricher {

	/** The logger. */
	private static final Log LOG = LogFactory.getLog(ReferenceIdBodyEnricher.class);

	protected Integer delta = 1;

	protected List<String> dontPrint = new ArrayList<String>();
	{
		dontPrint.add("ALOS-ARC/arc.doc");
		dontPrint.add("ALOS-CB/cb.doc");
		dontPrint.add("ALOS-CS/cs.doc");
		dontPrint.add("ALOS-DRTF/drtf.doc");
		dontPrint.add("ALOS-FS/fs.doc");
		dontPrint.add("ALOS-ING/ing.doc");
		dontPrint.add("ALOS-MCS/mcs.doc");
		dontPrint.add("ALOS-PD/pd.doc");
		dontPrint.add("ALOS-SP/sp.doc");
		dontPrint.add("CARE-CMS/OIVV Test Report CARE-CMS 3.1.0 v1.0.doc");
		dontPrint.add("CARE-CMS/OIVV Test Report CARE-CMS 3.2.0 v1.0.doc");
		//dontPrint.add("CARE-CMS/OIVV-CARECMS-v3.0.1v0.1-TVR.doc");
		dontPrint.add("CDS-CI-MC2/ProductDeletionNotificationInterface.doc");		
		dontPrint.add("CDS-SCI-GEST/OIVV-GEST-v2 4 5-TVR v1.doc");
		dontPrint.add("CDS-SCI-SPDM/OIVV-SPDM-v2 7 1-TVR v1 .doc");
		dontPrint.add("CDS-SCI-SPDM/OIVV-SPDM-v2.8-TVR v1.doc");
		dontPrint.add("CDS-SPR-AR/MS901 activation key.pdf");
		dontPrint.add("CDS-SPR-AR/TP-07.05 Statistics validation and distribution.doc");
		dontPrint.add("Cryosat-PDS-SDF/C2-RN-ACS-GS-0405_(RRN)_PDS_1.2.5_v1.1.pdf");		
		dontPrint.add("Cryosat-PDS-SDF/PDS UpdateProcedure_1_2_5.pdf");
		dontPrint.add("CS-DIS/OIVV-CS-DIS-1.11.00-v1.0TVR.doc");
		dontPrint.add("CS-DIS/[SRN] EFC4-ACS-SR-12-10316-217_v1_1.doc"); // Should be parsable...?
		dontPrint.add("CUS/CUSTools-STD-INTC-0054 v1.0 (CUSTools v4.0.24 STD SSDD).doc"); // Should be parsable...?		
		dontPrint.add("CUT/Test Validation Report CUT 2 0 5 v1.0.doc");
		dontPrint.add("DAIL/OIVV-DAIL-v6 3 2 1-TVR v1.doc");
		dontPrint.add("DAIL/OIVV-DAIL-v6.3.4-TV v1.doc");
		dontPrint.add("DDS-TDDM/FilterReport.pdf");
		dontPrint.add("DESCW/DESCW_SAOM.doc");
		dontPrint.add("DESCW/OIVV_Test_Report_DESCW_4.74 v1.doc");
		dontPrint.add("DESCW/OIVV_Test_Report_DESCW_4.74_v2.1.doc");
		dontPrint.add("DESCW/OIVV_Test_Report_DESCW_4.74_v2.2.doc");
		dontPrint.add("DESCW/OIVV_Test_Report_DESCW_4.75 V1.doc");
		dontPrint.add("DRS/OIVV-DRS-1.3.0-TVR v1 doc.doc");
		dontPrint.add("E-OA/eoa installation.doc");
		dontPrint.add("E-OA/OIVV-E-OA-1.02.00-TVR-V1.1.doc");
		dontPrint.add("E-OA/[srn] efc2-acs-sr-09-03495-172.doc");
		dontPrint.add("E-OA/[SRN] EFC2-ACS-SR-10-4439-183.doc");
		dontPrint.add("E-OA/[SRN] EFC4-ACS-SR-09-3431-0168.doc");
		dontPrint.add("E-OA/[SRN]_EFC4-ACS-SR-E-OA-0163.doc");
		dontPrint.add("EOLI-SA/EOLI_Server_SAOM_4.0.doc");
		
		dontPrint.add("EOLI-SA/OIVV Test Report EOLI SA 9 1 2 v1.doc");
		dontPrint.add("EOLI-SA/OIVV Test Report EOLI SA 9.1.0 v1 .doc");
		dontPrint.add("EOLI-SA/OIVV Test Report EOLI SA 9.1.3 V1.doc");
		dontPrint.add("EOLI-SA/OIVV Test Report EOLI SA 9.1.4V1.doc");
		dontPrint.add("EOLI-SA/OIVV Test Report EOLI0VSA 09.1.1V1.doc");
		dontPrint.add("EOLI-SA/OIVV Test Report EOLISA 9.0.0 V.1.doc");
		dontPrint.add("EOLI-SA/OIVV-EOLISSA-v7.2.2-TVR 1-0.doc");
		dontPrint.add("EOLI-SA/VEGA-EOLI-SA-SRN-226.doc");
		dontPrint.add("EOLI-SA/VEGA-EOLI-SA-SRN-263.doc");
		dontPrint.add("EOLI-SA/VEGA-EOLI-SA-SRS_v7.2.1.doc");

		dontPrint.add("EOLI-Server/eoli-server_patch_2_9_1_installation.doc");
		
		dontPrint.add("EOLI-Server/OIVV Test Report EOLI Server 2.10.4.doc");
		dontPrint.add("EOLI-Server/OIVV_Test_Report_EOLI_Server_2_10.doc");
		dontPrint.add("EOLI-Server/OIVV_Test_Report_EOLI_Server_3.0.3v1.doc");
		dontPrint.add("EOLI-Server/OIVV_Test_Report_EOLI_Server_3.0.V1.2.doc");
		dontPrint.add("EOLI-Server/OIVV_Test_Report_EOLI_Server_3.0.v1.doc");
		dontPrint.add("EOLI-Server/OIVV_Test_Report_EOLI_Server_4.0.1 v1.doc");
		dontPrint.add("EOLI-Server/OIVV_Test_Report_EOLI_Server_4.0.2 V1.doc");
		dontPrint.add("EOLI-Server/OIVV_Test_Report_EOLI_Server_4.0.3[1].doc");
		dontPrint.add("EOLI-Server/OIVV_Test_Report_EOLI_Server_4.0.4_v1.doc");
		dontPrint.add("EOLI-Server/OIVV_Test_Report_EOLI_Server_4.0.5_v1.0.doc");
		dontPrint.add("EOLI-Server/OIVV_Test_Report_EOLI_Server_4.0_v1.doc");
		dontPrint.add("EOLI-Server/vega-eoli-server-srn-182.doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-232(1).doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-235.doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-244.doc");
		
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-247.doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-249.doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-260.doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-275_v3.0.doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-287_v3.0.1.doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-299_v3.0.2.doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-303_v4.0.doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-304_v3.0.3.doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-312_v3.0.4.doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-318_v4.0.1.doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-323_v4.0.2.doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-327_v4.0.3.doc");
		dontPrint.add("EOLI-Server/VEGA-EOLI-Server-SRN-328_v4.0.4.doc");
		
		dontPrint.add("EXTPS/envisat_2010_unittest.pdf");
		dontPrint.add("HigherLevel/AE-ID-ESA-GS-001_Master_ICD_2.2C.pdf");		
		dontPrint.add("GSA/gsa_sdd.1.1.pdf"); // Cant figure out pattern...
		dontPrint.add("GSA/gsa_srd.1.5.doc"); // Cant figure out pattern...
		
		dontPrint.add("HigherLevel/aden-gs-veg-id-0028-02_2.pdf");		
		dontPrint.add("Cryosat-PDS-ARF/ESA-MA-ACS-GS-0105_[SUM-ARF]_v1.1.pdf"); // Unreadable
		dontPrint.add("Cryosat-PDS-IPF/ESA-MA-ACS-GS-0106_[SUM-IPF]_v1.1.pdf"); // Unreadable
		dontPrint.add("Cryosat-PDS-MCF/ESA-MA-ACS-GS-0103_[SUM-MCF]_v1.1.pdf"); // Unreadable
		dontPrint.add("Cryosat-PDS-SDF/ESA-MA-ACS-GS-0104_[SUM-SDF]_v1.1.pdf"); // Unreadable
		dontPrint.add("CS-DIS/efc4-acs-tp-mdpscs-0117.pdf"); // Unreadable
		
		dontPrint.add("HigherLevel/AE-ID-ESC-FS-3000_FOS_MMPF_ICD_2.3.pdf");				

		dontPrint.add("HigherLevel/ch3_17.doc");
		dontPrint.add("HigherLevel/ch4_17.doc");
		dontPrint.add("HigherLevel/descw_if.pdf");
		dontPrint.add("HigherLevel/GSC-IC-52-8920 R2-CDS ICD 2-2.pdf");
		dontPrint.add("HigherLevel/INFEO_NG - Core ICD.pdf");
		dontPrint.add("HigherLevel/IPF_System_Status.pdf");
		dontPrint.add("HigherLevel/N7950-SPOT-VGT-CCN3-CDS IF ICD Tailoring-v1.4.pdf");
		dontPrint.add("HigherLevel/N7950-SPOT-VGT-CCN3-HMA Catalogue ICD Tailoring-v1.4.pdf");
		dontPrint.add("HigherLevel/N7950-SPOT-VGT-CCN3-HMA Ordering ICD Tailoring-v1.4.pdf");
		dontPrint.add("HigherLevel/OES01-99073-SDS-ICD.pdf");
		dontPrint.add("HigherLevel/OSMV-OPMT-EOPG-TN-10-0001 v1.1 (EOP-G Technical Baseline).doc");
		
		dontPrint.add("HigherLevel/Taitus Generic Schema_1.1.doc");
		dontPrint.add("HigherLevel/TMapsICD.pdf");
		dontPrint.add("HigherLevel/TTS-IND-ICD-001- MMS ICD.pdf");
		
		dontPrint.add("HigherLevel/VEGA-EOLI-Server-ICD-195-1.3.doc");
		dontPrint.add("INFEO-EOLI/EOLI-INFEO-EVO-Installation_Procedure.doc");
		dontPrint.add("INFEO-ING/ING-Installation_Procedure.doc");
		
		dontPrint.add("INFEO-ING/SOE-ING-ADD-Annex-Javadoc.pdf");
		dontPrint.add("INFEO-ING/SOE-ING-SOAM-Annex-ECHO_OA.pdf");
		
		dontPrint.add("LI/2008-07-22_li_training_esrin.pdf");
		dontPrint.add("LI/2008-07-23_li_training_esrin.pdf");
		dontPrint.add("LI/2008-07-24_li_training_esrin.pdf");
		
		dontPrint.add("LI/OIVV-PL-LI-1 01 01-TVR v1.doc");
		dontPrint.add("LI/pl_oql_um.pdf");

		dontPrint.add("LI/PL_Service_Manual-1.3.pdf");
		dontPrint.add("LI/PL_User_Manual-1.2.pdf");
				
		dontPrint.add("MACH/MACH-D06-SystemTestPlan1.2.doc");
		dontPrint.add("MACH/MACH-D11-OT_ArchitectureDocument.pdf");
		dontPrint.add("MACH/MACH-D12-OT_SoftwareUserManual.pdf");
		dontPrint.add("MERCI/merci-atp-1.9.1.doc");
		dontPrint.add("MERCI/MERCI-ATP-1.9.2.doc");
		dontPrint.add("MERCI/MERCI-ATP-1.9.3.doc");
		dontPrint.add("MERCI/MERCI-ATP-2.0.0.doc");
		dontPrint.add("MERCI/MERCI-FAT-REPORT-1.9.3.doc");
		dontPrint.add("MERCI/MERCI-FAT-REPORT-2.0.0.doc");
		dontPrint.add("MERCI/merci-saom-1.9.1.doc");
		dontPrint.add("MERCI/MERCI-SRN-1.9.3.doc");
		dontPrint.add("MERCI/MERCI-SRN-2.0.0.doc");
		dontPrint.add("MERCI/MERCI_Manual_1_9_2.doc");
		dontPrint.add("MERCI/MERCI_Manual_1_9_3.doc");
		dontPrint.add("MERCI/MERCI_Manual_2_0_0.doc");
		dontPrint.add("MMOHS/Test procedure for MMOHS-CR-10-01212.doc");
		dontPrint.add("OT/SRN-OT-MMFI-1.01.00.pdf");
		dontPrint.add("OT/sum-32.5.7.pdf");
		dontPrint.add("PFD/pfd_ten_mmfi_0535_eads.doc");
		dontPrint.add("RSE/RSE_SRD_1-0.doc");
		dontPrint.add("Savoir/OSME-USMP-SEDA-RS-07-1373 v4.0 (SaVoir Visualisation Tool Requirements Specification).doc");
		dontPrint.add("SDS/DOX-RBE-#41390-v1-Station_Data_Server-Software_Installation_Manual.pdf");
		dontPrint.add("SDS/DOX-RBE-#41391-v1-Station_Data_Server_-Software_User_Manual.pdf");
		dontPrint.add("StatRep/CRQ4452 - Support for RSIF loading on Statrep2.doc");
		dontPrint.add("StatRep/DWHE-MISSION-INTEGRATION-GUIDE-TN-3200-INT-1.1.doc");
		
		dontPrint.add("ULS/DIMS_UL-User-Manual_1.0.pdf");
		dontPrint.add("UM-SSO/SIE-EO-OP-UM-SSO-SRD-2.2.1_Signed.pdf");
		dontPrint.add("UM-SSO/updatefrom181to1811.doc");
		dontPrint.add("VSSGS/VSSGS Administrator Manual.doc");
		dontPrint.add("VSSGS/vssgs.pdf");
		dontPrint.add("WMS2EOS/SM-WMS2EOS-SAOMTD-13.doc");
		dontPrint.add("WMS2EOS/SM-WMS2EOS-SAOMTD-14.doc");
		dontPrint.add("WMS2EOS/SM-WMS2EOS-SDD-12.doc");
		
	}


	public class PatternEntry {

		public PatternEntry(Integer group, String name) {
			super();
			this.group = group;
			this.name = name;
		}

		public Integer group;
		public String name;
	}

	protected Map<Pattern, List<PatternEntry>> patterns = new HashMap<Pattern, List<PatternEntry>>();
	{
		patterns.put(Pattern.compile("Title:{0,1}(.{0,100}?)Contract Ref\\.:{0,1}(.{0,100}?)Issue:{0,1}(.{0,10}?)Rev\\.:{0,1}(.{0,10}?)Document Ref\\.:{0,1}(.{0,100}?)Consortium Reference(.{0,100}?)Date:{0,1}", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(5, "body doc ref")); add(new PatternEntry(6, "body misc 1"));}});
		patterns.put(Pattern.compile("Title:{0,1}(.{0,100}?)Contract Ref\\.:{0,1}(.{0,100}?)Issue:{0,1}(.{0,10}?)Rev\\.:{0,1}(.{0,10}?)Consortium Ref\\.:{0,1}(.{0,100}?)Date:{0,1}", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(5, "body doc ref"));}});		
		patterns.put(Pattern.compile("Title:{0,1}(.{0,200}?)Contract Ref\\.:{0,1}(.{0,100}?)Doc\\. Ref\\.:{0,1}(.{0,100}?)Issue:{0,1}(.{0,10}?)Rev\\.:{0,1}(.{0,10}?)Consortium(.{0,10}?)Reference(.{0,100}?)Date:{0,1}", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref")); add(new PatternEntry(7, "body misc 1"));}});		
		patterns.put(Pattern.compile("Title:{0,1}(.{0,100}?)Contract Ref\\.:{0,1}(.{0,100}?)Issue:{0,1}(.{0,10}?)Rev\\.:{0,1}(.{0,10}?)Document Ref\\.:{0,1}(.{0,100}?)Date:{0,1}", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(5, "body doc ref"));}});		
		patterns.put(Pattern.compile("Title:{0,1}(.{0,100}?)Contract Ref\\.:{0,1}(.{0,100}?)Consortium Ref\\.:{0,1}(.{0,100}?)Issue:{0,1}(.{0,10}?)Rev\\.:{0,1}(.{0,10}?)Date:{0,1}", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref"));}});
		patterns.put(Pattern.compile("Title:{0,1}(.{0,200}?)Contract Ref\\.:{0,1}(.{0,100}?)Doc\\. Ref\\.:{0,1}(.{0,100}?)Consortium(.{0,10}?)Reference(.{0,100}?)Issue:{0,1}(.{0,10}?)Rev\\.:{0,1}(.{0,10}?)Date:{0,1}", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref")); add(new PatternEntry(5, "body misc 1"));}});
		patterns.put(Pattern.compile("Title:{0,1}(.{0,100}?)Contract Ref\\.:{0,1}(.{0,100}?)Consortium Reference(.{0,100}?)Issue:{0,1}(.{0,10}?)Rev\\.:{0,1}(.{0,10}?)Date:{0,1}", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref"));}});
		patterns.put(Pattern.compile("Title:{0,1}(.{0,100}?)Contract Ref\\.:{0,1}(.{0,100}?)Issue:{0,1}(.{0,10}?)Rev\\.:{0,1}(.{0,10}?)Consortium Reference(.{0,100}?)Date:{0,1}", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(5, "body doc ref"));}});                       		      
		patterns.put(Pattern.compile("Title:{0,1}(.{0,100}?)Issue:{0,1}(.{0,100}?)Rev\\.:{0,1}(.{0,100}?)Consortium Ref\\.:{0,1}(.{0,100}?)Date:{0,1}", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(4, "body doc ref"));}});
		patterns.put(Pattern.compile("Filename:(.{0,100}?)page(.{0,100}?)Last update:(.{0,100}?)Title:(.{0,100}?)\\((.{0,100}?)\\)\\s+by", Pattern.DOTALL), new ArrayList() {{ add(new PatternEntry(5, "body doc ref"));}});
		patterns.put(Pattern.compile("Contract No	:(.{0,100}?)WP No	:(.{0,100}?)Document Ref	:(.{0,100}?)Issue Date	:(.{0,100}?)Issue	:(.{0,100}?)$", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(5, "body doc ref"));}});
		patterns.put(Pattern.compile("DocRef: (.{0,100}?)Sub-Contractor:(.{0,100}?)Issue:(.{0,100}?)Procedure Name:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Code(.{0,5}?):(.{0,100}?)Issue(.{0,5}?):(.{0,100}?)Date(.{0,5}?):(.{0,100}?)", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Doc\\. No\\.:(.{0,100}?)Issue:{0,1}(.{0,10}?)Date:{0,1}(.{0,10}?)Page(.{0,5}?):(.{0,5}?)$(.{0,100}?)$", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(6, "body doc ref")); }});
		patterns.put(Pattern.compile("Doc\\. No\\.:(.{0,100}?)Issue:{0,1}(.{0,10}?)Date:{0,1}(.{0,10}?)Page(.{0,5}?):(.{5,100}?)$", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(5, "body doc ref")); }});
		patterns.put(Pattern.compile("Doc\\. No\\.:(.{5,100}?)Issue:{0,1}(.{0,10}?)Date:{0,1}(.{0,50}?)Page", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});		
		patterns.put(Pattern.compile("prepared by/préparé par(.{0,200}?)reference/réference(.{0,100}?)issue/édition(.{0,100}?)revision/révision(.{0,100}?)date of issue/date d’édition(.{0,100}?)", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(2, "body doc ref")); }});
		patterns.put(Pattern.compile("prepared by(.{0,100}?)workpackage(.{0,100}?)reference(.{0,100}?)issue(.{0,10}?)revision(.{0,10}?)date of issue(.{0,100}?)Reviewed by(.{0,100}?)Document type(.{0,100}?)Distribution", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref")); }});
		patterns.put(Pattern.compile("DOC:(.{0,100}?)VER:(.{0,100}?)DATE:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Document Reference:(.{0,100}?)Issue / revision:(.{0,100}?)Issue date:(.{0,100}?)Document author:(.{0,100}?)Document approver:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Issue:(.{0,100}?)Date:(.{0,100}?)ID:(.{0,100}?)$", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref")); }});
		patterns.put(Pattern.compile("Customer:(.{0,100}?)Contract number:(.{0,100}?)Proposal number:(.{0,100}?)Business/service number:(.{0,100}?)Service Delivery Manager:(.{0,100}?)Reporting to:(.{0,100}?)Service delivery/document reference:(.{0,100}?)Issue:(.{0,10}?)Issue date:(.{0,50}?)Period of validity:(.{0,100}?)", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(7, "body doc ref")); }});
		
		patterns.put(Pattern.compile("Implementation(.{0,100}?)Issue(.{0,10}?)Revision(.{0,10}?)Page(.{0,100}?)Date", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Implementation(.{0,100}?)Issue(.{0,30}?)Page(.{0,100}?)Date", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("EO DAIL(.{0,100}?)Issue(.{0,30}?)Page(.{0,10}?)of", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("reference(.{0,100}?)issue(.{0,10}?)date of issue(.{0,30}?)status(.{0,30}?)Document type(.{0,100}?)Distribution", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});		
		patterns.put(Pattern.compile("Document Id:(.{0,100}?)Issue:(.{0,100}?)Revision:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("reference(.{0,100}?)issue(.{0,20}?)date of issue(.{0,50}?)", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Title:(.{0,100}?)Doc Id:(.{0,100}?)Issue:(.{0,100}?)Rev\\.:(.{0,100}?)Proj\\. Ref\\.:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(2, "body doc ref")); }});
		patterns.put(Pattern.compile("Doc\\. Id:(.{0,100}?)Issue:(.{0,100}?)Date:(.{0,100}?)Page:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Title:(.{0,100}?)Contract Ref\\.:(.{0,100}?)Document Reference(.{0,100}?)Issue:(.{0,100}?)Rev\\.:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref")); }});
		patterns.put(Pattern.compile("Document Ref(\\s*):(.{0,100}?)Issue Date(\\s*):(.{0,100}?)Issue(\\s*):(.{0,10}?)Title", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(2, "body doc ref")); }});		
		patterns.put(Pattern.compile("CR Reference(.{0,100}?)Task(.{0,100}?)ESA WP Manager", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});		
		patterns.put(Pattern.compile("Doc No:(.{0,100}?)Issue:(.{0,100}?)Date:(.{0,100}?)Page:(.{0,100}?)^(.{0,100}?)$", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(5, "body doc ref")); }});
		patterns.put(Pattern.compile("reference(.{0,10}?)issue(.{0,10}?)date(.{0,10}?)page(.{0,10}?)^(.{0,100}?)$", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(5, "body doc ref")); }});
		patterns.put(Pattern.compile("Ref\\.:(.{0,100}?)Issue/Revision:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Document Number:(.{0,100}?)Issue/Revision:(.{0,100}?)^", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Short Title(.{0,100}?)Prepared by(.{0,100}?)Approved by(.{0,100}?)Reference(.{0,100}?)Issue(.{0,10}?)Revision(.{0,10}?)Date of issue(.{0,100}?)Status", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(4, "body doc ref")); }});
		patterns.put(Pattern.compile("document:(.{0,100}?)Version:(.{0,20}?)Category:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Advanced Computer Systems Name :(.{0,100}?)Version :(.{0,100}?)Date :(.{0,100}?)Page :", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Document No  Issue/Rev. No Date Page : (.{0,100}?) : Issue (.{0,100}?) :", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Document Number:(.{0,100}?)Issue Date:(.{0,100}?)Issue:(.{0,100}?)Revision:(.{0,100}?)Distribution:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Doc\\. No\\.:(.{0,100}?)Issue:(.{0,100}?)Revision:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Doc\\. Title:(.{0,100}?)Issue:(.{0,100}?)Doc\\. Ref:(.{0,100}?)Rev\\.:(.{0,100}?)Date:(.{0,100}?)Page:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref")); }});
		patterns.put(Pattern.compile("Reference :(.{0,100}?)Version :(.{0,100}?)Date :", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Doc\\. no:(.{0,100}?), Rev:(.{0,100}?)Page", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});		
		patterns.put(Pattern.compile("Reference:(.{0,100}?)Issue:(.{0,100}?)Revision:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("RÉFÉRENCE :(.{0,100}?)DATE :(.{0,100}?)^(.{0,100}?)$", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref")); }});
		patterns.put(Pattern.compile("Document No(.{0,100}?)Issue/Rev. No(.{0,100}?)Date(.{0,100}?)Page(.{0,100}?):(.{0,100}?):(.{0,100}?):", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(5, "body doc ref")); }});
		patterns.put(Pattern.compile("Document Id:(.{0,100}?)Issue:(.{0,100}?)Date:(.{0,100}?)Page:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Reference:(.{0,100}?)Issue:(.{0,100}?)Revision:(.{0,100}?)Distribution Code:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Title:(.{0,100}?)Contract Ref.:(.{0,100}?)Int. Ref.:(.{0,100}?)Issue:(.{0,100}?)Rev\\.:(.{0,100}?)Proj\\. Ref\\.:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref")); }});
		patterns.put(Pattern.compile("Document Reference:(.{0,100}?)Document Status:(.{0,100}?)Prepared By:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Client:(.{0,100}?)Project Reference:(.{0,100}?)Document Reference:(.{0,100}?)File Name:(.{0,100}?)Issue:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref")); }});
		patterns.put(Pattern.compile("Client:(.{0,100}?)Solenix Project Reference:(.{0,100}?)Document Reference:(.{0,100}?)Version:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref")); }});
		patterns.put(Pattern.compile("Ref\\.:(.{0,100}?)$(.{0,100}?)Release:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Title:(.{0,100}?)Contract Ref.:(.{0,100}?)Consortium(.{0,100}?)Reference(.{0,100}?)Issue:(.{0,10}?)Rev.:(.{0,10}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(4, "body doc ref")); }});		 
		patterns.put(Pattern.compile("Document Ref\\.:(.{0,100})Issue:(.{0,100})Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});		
		patterns.put(Pattern.compile("Ref :(.{0,100})Issue :(.{0,100})Date :", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});		
		patterns.put(Pattern.compile("Ref:(.{0,100})Issue/Revision:(.{0,100})Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});		
		patterns.put(Pattern.compile("Doc No :(.{0,100})Issue :(.{0,100})Date :", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});		 
		patterns.put(Pattern.compile("Ref\\. :(.{0,100})Is\\. :(.{0,100})Rev\\. :(.{0,100})Date :(.{0,100})Page :", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});		
		patterns.put(Pattern.compile("Doc No:(.{0,100})Issue(.{0,100})Date :(.{0,100})Page :", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});		
		patterns.put(Pattern.compile("Doc No :(.{0,100}?)$", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});		
		patterns.put(Pattern.compile("Ref\\.(.{0,100}?)Issue(.{0,100}?)Rev.(.{0,100}?)Page(.{0,100}?):(.{0,100}?):(.{0,100}?):", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(5, "body doc ref")); }});
		patterns.put(Pattern.compile("Workpackage:(.{0,100}?)Doc. Ref.:(.{0,100}?)Version:(.{0,100}?)Status:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(2, "body doc ref")); }});
		patterns.put(Pattern.compile("Doc\\. Ref\\.:(.{0,100}?)Version:(.{0,100}?)Status:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Document id(.{0,100}?)Author(.{0,100}?)Version(.{0,100}?)Issue date", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Ref\\.  :(.{0,100}?)Issue :(.{0,100}?)Date :", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Ref\\. :(.{0,100}?)Iss\\./Rev\\. :(.{0,100}?)Date:(.{0,100}?)^(.{0,100}?)$", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(4, "body doc ref")); }});
		patterns.put(Pattern.compile("Prepared by(.{0,100}?)Reference(.{0,100}?)Issue(.{0,100}?)Revision(.{0,100}?)Status(.{0,100}?)Date of issue", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(2, "body doc ref")); }});
		patterns.put(Pattern.compile("Prepared by(.{0,100}?)Reference(.{0,100}?)Issue(.{0,100}?)Revision(.{0,100}?)Date of Issue", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(2, "body doc ref")); }});		 
		patterns.put(Pattern.compile("Référence :(.{0,100}?)Version  :(.{0,100}?)Date :(.{0,100}?)Page :", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});
		patterns.put(Pattern.compile("Rev\\. :(.{0,100}?)Date  :(.{0,100}?)Reference :(.{0,100}?)Page", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref")); }});		 
		patterns.put(Pattern.compile("Issue :(.{0,100}?)Date :(.{0,100}?)Revision :(.{0,100}?)Date :(.{0,100}?)Ref.  :(.{0,100}?)$", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(5, "body doc ref")); }});		
		patterns.put(Pattern.compile("Project:(.{0,100}?)Doc\\.Ref\\.:(.{0,100}?)Issue:(.{0,100}?)Date:(.{0,100}?)Status:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(2, "body doc ref")); }});
		patterns.put(Pattern.compile("Service Delivery Manager:(.{0,100}?)Reporting to:(.{0,100}?)Service delivery/document reference:(.{0,100}?)Issue:(.{0,100}?)First Issue date", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref")); }});
		patterns.put(Pattern.compile("Contract Number(.{0,100}?)Service  Delivery Document Reference(.{0,100}?)Service Delivery Manager", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(2, "body doc ref")); }});		
		patterns.put(Pattern.compile("Contract Number(.{0,100}?)Service  Delivery Document Reference(.{0,100}?)Service Delivery Manager", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(2, "body doc ref")); }});		
		patterns.put(Pattern.compile("Title:(.{0,100}?)Contract Ref\\.:(.{0,100}?)Issue:(.{0,100}?)Rev\\.:(.{0,100}?)Proj\\. Ref\\.:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(5, "body doc ref")); }});
		patterns.put(Pattern.compile("Title:(.{0,100}?)Contract Ref\\.:(.{0,100}?)Issue:(.{0,100}?)Rev\\.:(.{0,100}?)DocumentConsortium Ref.:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(5, "body doc ref")); }});
		patterns.put(Pattern.compile("Document ID:(.{0,100}?)Title:(.{0,100}?)Issue:(.{0,100}?)Issue Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref")); }});		  		
		patterns.put(Pattern.compile("Contract Ref\\.:(.{0,100}?)Doc\\. Ref\\.:(.{0,100}?)Issue:(.{0,100}?)Rev\\.:(.{0,100}?)ConsortiumReference:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(2, "body doc ref")); add(new PatternEntry(5, "body misc 1"));}});		
		patterns.put(Pattern.compile("Title:(.{0,100}?)Contract Ref\\.:(.{0,100}?)Issue:(.{0,100}?)Rev\\.:(.{0,100}?)Consortium(.{0,10}?)Ref\\.:(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(6, "body doc ref")); }});		 
		patterns.put(Pattern.compile("Title:(.{0,100}?)Date:(.{0,100}?)Doc\\. Ref\\.:(.{0,100}?)Issue:(.{0,100}?)Rev:(.{0,100}?)Consortium Ref\\.:(.{0,100}?)Contract Ref\\.:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref")); add(new PatternEntry(6, "body misc 1"));}});		
		patterns.put(Pattern.compile("Ref :(.{0,100}?)Issue :", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref"));}});
		patterns.put(Pattern.compile("Reference:(.{0,100}?)Issue:(.{0,100}?)Revision:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(1, "body doc ref"));}});		
		patterns.put(Pattern.compile("Title:(.{0,100}?)Contract Ref\\.:(.{0,100}?)Issue:(.{0,100}?)Rev\\.:(.{0,100}?)Doc\\. Ref\\.:(.{0,100}?)Date:(.{0,100}?)Consortium Ref.:(.{0,100}?)$", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(5, "body doc ref")); add(new PatternEntry(7, "body misc 1"));}});
		
		patterns.put(Pattern.compile("Title:(.{0,100}?)Contract Ref\\.:(.{0,100}?)Consortium Reference(.{0,100}?)Date:", Pattern.DOTALL | Pattern.MULTILINE), new ArrayList() {{ add(new PatternEntry(3, "body doc ref")); }});
		
		
	}

	protected long counter = 0;

	public void process(Exchange exchange) {

		InformationObject io = (InformationObject) exchange.getIn().getBody();
		
		counter++;

		if (io.hasTitle.startsWith("zip:")) {
			LOG.warn("Found entry that is a ZIP file '" + io.hasTitle + "'.");
			exchange.setProperty(Exchange.ROUTE_STOP, Boolean.TRUE);
			return;
		}
		
		long emptyReference = 0;

		/** Check whether the raw text of this io is readable. */

		String deli = "\\\\";
		String[] elements = io.hasUri.split(deli);
		String application = elements[elements.length - 2];

		boolean found = false;

		/** See if the reference is in the document properties. */
		String propertyRef = (String) io.metadata.get("DocReference");
		if (propertyRef != null && propertyRef.equals("") == false) {
			//found = true;
			io.metadata.put("Reference ID (property doc ref)", propertyRef);
			LOG.info(counter + ". Found reference ID (in properties) '" + propertyRef + "' for document '" + application + "/" + io.hasTitle + "'.");
		}

		if (io.metadata.get("Language").equals("en") == false || (Double) io.metadata.get("Language Probability") < 0.9d) {
			LOG.warn("Ignoring '" + io.hasTitle + "' because it holds no text.");
		}
		else {

			/** First use the patterns. */
			Iterator<Entry<Pattern, List<PatternEntry>>> it = patterns.entrySet().iterator();
			while (it.hasNext()) {
				Entry<Pattern, List<PatternEntry>> entry = it.next();

				Matcher matcher = entry.getKey().matcher(io.withRawText);
				if (matcher.find()) {

					for (PatternEntry patternEntry : entry.getValue()) {

						String id = matcher.group(patternEntry.group).trim().toUpperCase();
						
						/** TODO: Proper fix. This is a dirty quick fix.*/
						id = id.replaceAll("CONSORTIUM REFERENCE", "").trim();

						found = true;
						if (id.equals("") || id.equalsIgnoreCase("insert reference")) {
							LOG.warn(counter + ". Found empty reference ID for Pattern '" + patternEntry.name + "'.");
							emptyReference++;
						}
						else {
							LOG.info(counter + ". Found reference ID '" + id + "' for document '" + application + "/" + io.hasTitle + "' using pattern '" + entry.getKey().pattern() + "'.");
						}
						io.metadata.put("Reference ID (" + patternEntry.name + ")", id);
					}
				}
			}

			/** If not found in header. */
			if (found == false) {
				if (dontPrint.contains(application + "/" + io.hasTitle) == false) {
					// System.out.println(io.withRawText);
					// System.out.println("Document: " + application + "/" + io.hasTitle);
				}
			}

			/** Then do a count of hits. */
			Pattern pattern = Pattern.compile("(\\p{Alpha}\\p{Alnum}{1,}-\\p{Alpha}{2,}-\\p{Alpha}{2,}-\\p{Alpha}{2,}-\\d{2,}(-\\d{2,})*(-\\p{Alpha}{2,})*)");
			Matcher matcher = pattern.matcher(io.withRawText);
			Map<String, Integer> hits = new HashMap<String, Integer>();			
			while (matcher.find()) {
				if (hits.containsKey(matcher.group(1))) {
					Integer count = hits.get(matcher.group(1)) + 1;
					hits.put(matcher.group(1), count);
				}
				else {
					hits.put(matcher.group(1), 1);
				}
			}

			/** Find highest count and second highest count. */
			Integer largest = 0;
			Integer secondLargest = 0;
			String id = "";

			Iterator<Entry<String, Integer>> it2 = hits.entrySet().iterator();
			while (it2.hasNext()) {
				Entry<String, Integer> entry = it2.next();
				if (largest == 0 || largest < entry.getValue()) {
					secondLargest = largest;
					largest = entry.getValue();
					id = entry.getKey();
				}
			}

			id = id.toUpperCase();

			if (largest - delta >= secondLargest) {
				found = true;
				io.metadata.put("Reference ID (body hits)", id);
				LOG.info(counter + ". Using maxCount method, found reference ID '" + id + "' for document '" + application + "/" + io.hasTitle + "'. Had " + largest + " counts, where as second best had " + secondLargest + " counts.");			
			}
			
			
			if (found == false) {
				LOG.warn(counter + ". Failed utterly to find reference ID for document '" + application + "/" + io.hasTitle + "'.");
				// LOG.warn(io.withRawText);
				io.metadata.put("rawtext", io.withRawText);
			}
		}
	}
}	
