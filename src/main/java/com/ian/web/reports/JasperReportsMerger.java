package com.ian.web.reports;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.util.JRLoader;

public class JasperReportsMerger {

    public static void main(String[] args) throws Exception {
        // Load compiled JasperReports files (.jasper)
    	 JasperPrint jasperPrint1 = loadAndFillJasperPrint("/jasper/reports/PDS FORM 1.jasper", createParametersForReport1());
         JasperPrint jasperPrint2 = loadAndFillJasperPrint("/jasper/reports/PDS FORM 2.jasper", createParametersForReport2());
//        JasperPrint jasperPrint3 = loadJasperPrint("/jasper/reports/report3.jasper");
//        JasperPrint jasperPrint4 = loadJasperPrint("/jasper/reports/report4.jasper");
        

        // Add each JasperPrint object to a list
        List<JasperPrint> jasperPrintList = new ArrayList<>();
        jasperPrintList.add(jasperPrint1);
        jasperPrintList.add(jasperPrint2);
//        jasperPrintList.add(jasperPrint3);
//        jasperPrintList.add(jasperPrint4);

        // Merge JasperPrint objects into a single JasperPrint object
        JasperPrint mergedJasperPrint = mergeJasperPrints(jasperPrintList);

        // Export the merged JasperPrint to a PDF file
        exportToPdf(mergedJasperPrint, "merged_report.pdf");
    }

    private static JasperPrint loadJasperPrint(String jasperFile) throws JRException {
        // Load compiled JasperReports file (.jasper)
        return JasperFillManager.fillReport(JasperReportsMerger.class.getResourceAsStream(jasperFile), null, new JREmptyDataSource());
    }

    private static JasperPrint mergeJasperPrints(List<JasperPrint> jasperPrintList) throws JRException {
        // Merge JasperPrint objects into a single JasperPrint object
        JasperPrint mergedJasperPrint = new JasperPrint();
        for (JasperPrint jasperPrint : jasperPrintList) {
            for (JRPrintPage page : jasperPrint.getPages()) {
                mergedJasperPrint.addPage(page);
            }
        }
        return mergedJasperPrint;
    }

    private static void exportToPdf(JasperPrint jasperPrint, String outputFileName) throws JRException {
        // Export the JasperPrint to a PDF file
        String absolutePath = new File(outputFileName).getAbsolutePath();
        System.out.println("PDF file saved to: " + absolutePath);
        JasperExportManager.exportReportToPdfFile(jasperPrint, outputFileName);
    }
    
    private static JasperPrint loadAndFillJasperPrint(String jasperFile, Map<String, Object> parameters) throws JRException {
        // Load compiled JasperReports file (.jasper)
        JasperReport jasperReport = (JasperReport) JRLoader.loadObject(JasperReportsMerger.class.getResourceAsStream(jasperFile));

        // Fill the JasperReport with parameters
        return JasperFillManager.fillReport(jasperReport, parameters, new JREmptyDataSource());
    }
    
    private static Map<String, Object> createParametersForReport1() {
        // Create parameters for Report 1
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("I.PI_Surname", "Orozco");
        parameters.put("I.PI_Firstname", "Ian Alfred");
        return parameters;
    }

    private static Map<String, Object> createParametersForReport2() {
        // Create parameters for Report 2
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("IV.CSE_Career_Service_RA_1080_1", "xxxxxxxxxxxxx");
        parameters.put("IV.CSE_Rating1", "BBBBBBBBBBBBB");
        return parameters;
    }
}
