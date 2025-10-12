
package com.belisty.function.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AzureCostEmailBuilder {

	private AzureCostEmailBuilder() {
		// util class only
	}

	public static class EmailContent {
		private final String subject;
		private final String body;

		public EmailContent(String subject, String body) {
			this.subject = subject;
			this.body = body;
		}

		public String getSubject() {
			return subject;
		}

		public String getBody() {
			return body;
		}
	}

	public static EmailContent buildDailyCostEmail(double perHourRate, List<Map<String, Object>> mappedResults) {
		// Yesterday’s date in Azure’s format (yyyyMMdd)
		String yesterday = LocalDate.now().minusDays(1).format(DateTimeFormatter.BASIC_ISO_DATE);

		StringBuilder tableBuilder = new StringBuilder();
		boolean hasData = false;
		String subjectCost = "0";
		String subjectCurrency = "";

		tableBuilder.append("<html><body>").append("<h2>Azure Daily Cost Report</h2>")
		        .append("<p><b>Date:</b> ")
				.append(yesterday).append("</p>")
				.append("<p><b>API request per hour:</b> ")
				.append(perHourRate).append("</p>")
				.append("<table border='1' cellpadding='5' cellspacing='0'>")
				.append("<tr>");

		// Add table header (from first record keys)
		if (!mappedResults.isEmpty()) {
			for (String key : mappedResults.get(0).keySet()) {
				tableBuilder.append("<th>").append(key).append("</th>");
			}
			tableBuilder.append("</tr>");
		}

		// Add table rows
		for (Map<String, Object> costRecord : mappedResults) {
			String usageDate = String.valueOf(costRecord.get("UsageDate"));
			if (yesterday.equals(usageDate)) {
				hasData = true;
				subjectCost = String.valueOf(costRecord.getOrDefault("PreTaxCost", "0"));
				subjectCurrency = String.valueOf(costRecord.getOrDefault("Currency", ""));
			}
			tableBuilder.append("<tr>");
			for (Object value : costRecord.values()) {
				tableBuilder.append("<td>").append(value).append("</td>");
			}
			tableBuilder.append("</tr>");

		}

		tableBuilder.append("</table>").append("<p style='margin-top:10px;'>Generated on: ").append(LocalDate.now())
				.append("</p>").append("</body></html>");

		String subject = hasData ? subjectCost + " Azure Daily Cost Report - " + yesterday + " - " + subjectCurrency
				: subjectCost + " Azure Daily Cost Report - " + yesterday + " (No Data)";

		return new EmailContent(subject, tableBuilder.toString());
	}
}
