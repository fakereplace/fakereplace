package org.fakereplace.test.coverage.report;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.fakereplace.test.coverage.ChangeTestType;
import org.fakereplace.test.coverage.CodeChangeType;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlSuite;

public class CoverageReporter implements IReporter
{

   private static final Logger L = Logger.getLogger(CoverageReporter.class);
   // ~ Instance fields ------------------------------------------------------

   private PrintWriter m_out;

   // ~ Methods --------------------------------------------------------------

   /** Creates summary of the run */
   public void generateReport(List<XmlSuite> xml, List<ISuite> suites, String outdir)
   {
      try
      {
         m_out = createWriter(outdir);
      }
      catch (IOException e)
      {
         L.error("output file", e);
         return;
      }
      startHtml(m_out);
      m_out.write("Hover over passed overage to see test that covers assertion");
      createTable("Public members", CoverageListener.getPublictest());
      createTable("Private members", CoverageListener.getPrivatetest());

      endHtml(m_out);
      m_out.flush();
      m_out.close();
   }

   public void createTable(String name, Map<CodeChangeType, Map<ChangeTestType, Set<String>>> data)
   {
      m_out.write("<h1>" + name + "</h1> <p/>");
      m_out.write("<table border='1' ><tr><td/>");
      for (ChangeTestType t : ChangeTestType.values())
      {
         m_out.write("<td>" + t.getLabel() + "</td>");
      }
      for (CodeChangeType i : CodeChangeType.values())
      {
         m_out.write("<tr>");
         m_out.write("<td>" + i.getLabel() + "</td>");
         for (ChangeTestType t : ChangeTestType.values())
         {
            String val = "<td style='background-color:red;'>X</td>";
            if (data.containsKey(i))
            {
               Map<ChangeTestType, Set<String>> v = data.get(i);
               if (v.containsKey(t))
               {
                  Set<String> s = v.get(t);
                  StringBuilder cell = new StringBuilder("<td style='background-color:green;' title='");
                  for (String test : s)
                  {
                     cell.append(test);
                     cell.append("\n");
                  }
                  cell.append("' >Y(");
                  cell.append(+s.size());
                  cell.append(")</td>");
                  val = cell.toString();
               }
            }
            m_out.write(val);
         }
      }
      m_out.write("</tr>");

      m_out.write("</table>");
   }

   protected PrintWriter createWriter(String outdir) throws IOException
   {
      return new PrintWriter(new BufferedWriter(new FileWriter(new File(outdir, "fakereplace-test-coverage.html"))));
   }

   private static String escape(String string)
   {
      if (null == string)
         return string;
      return string.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
   }

   /** Starts HTML stream */
   protected void startHtml(PrintWriter out)
   {
      out.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
      out.println("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
      out.println("<head>");
      out.println("<title>TestNG:  Unit Test</title>");
      out.println("<style type=\"text/css\">");
      out.println("table caption,table.info_table,table.param,table.passed,table.failed {margin-bottom:10px;border:1px solid #000099;border-collapse:collapse;empty-cells:show;}");
      out.println("table.info_table td,table.info_table th,table.param td,table.param th,table.passed td,table.passed th,table.failed td,table.failed th {");
      out.println("border:1px solid #000099;padding:.25em .5em .25em .5em");
      out.println("}");
      out.println("table.param th {vertical-align:bottom}");
      out.println("td.numi,th.numi,td.numi_attn {");
      out.println("text-align:right");
      out.println("}");
      out.println("tr.total td {font-weight:bold}");
      out.println("table caption {");
      out.println("text-align:center;font-weight:bold;");
      out.println("}");
      out.println("table.passed tr.stripe td,table tr.passedodd td {background-color: #00AA00;}");
      out.println("table.passed td,table tr.passedeven td {background-color: #33FF33;}");
      out.println("table.passed tr.stripe td,table tr.skippedodd td {background-color: #cccccc;}");
      out.println("table.passed td,table tr.skippedodd td {background-color: #dddddd;}");
      out.println("table.failed tr.stripe td,table tr.failedodd td,table.param td.numi_attn {background-color: #FF3333;}");
      out.println("table.failed td,table tr.failedeven td,table.param tr.stripe td.numi_attn {background-color: #DD0000;}");
      out.println("tr.stripe td,tr.stripe th {background-color: #E6EBF9;}");
      out.println("p.totop {font-size:85%;text-align:center;border-bottom:2px black solid}");
      out.println("div.shootout {padding:2em;border:3px #4854A8 solid}");
      out.println("</style>");
      out.println("</head>");
      out.println("<body>");
   }

   /** Finishes HTML stream */
   protected void endHtml(PrintWriter out)
   {
      out.println("</body></html>");
   }

}
