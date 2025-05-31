package crawl.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import crawl.bean.JobShareInfo;
import crawl.dto.JobInterviewInfoDTO;
import crawl.dto.WorkReviewInfoDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
@Service
public class CrawlCleanService {
	
	
	
	JobShareInfo jobBean=new JobShareInfo();
	

	public Object ReturnService(String url) {
		return formatData(url);	
	}
	
	
	public Object formatData(String url) {
	    System.out.println("in formatData");

	    try {
	        Document doc = Jsoup.connect(url).get();
	        Elements scripts = doc.select("script[type=application/ld+json]");

	        for (Element script : scripts) {
	            String json = script.html();

	            ObjectMapper mapper = new ObjectMapper();
	            JsonNode root = mapper.readTree(json);

	            String type = root.get("@type").asText();
	            if (!"Article".equals(type)) continue;

	            JsonNode headlineNode = root.get("headline");
	            JsonNode datePublishedNode = root.get("datePublished");
	            JsonNode descriptionNode = root.get("description");

	            WorkReviewInfoDTO wDto = new WorkReviewInfoDTO();
	            wDto.setPageTitle(headlineNode.asText());
	            wDto.setShareDate(datePublishedNode.asText());
	            wDto.setShareContent(descriptionNode.asText());
	            wDto.setShareType("工作心得");

	            // 從 description 拆欄位（例如：待遇、地區等）
	            parseFromDescription(descriptionNode.asText(), wDto);

	            return wDto;
	        }

	    } catch (IOException e) {
	        e.printStackTrace();
	    }

	    System.out.println("end formatData");
	    return "not found";
	}
	
	private void parseFromDescription(String description, WorkReviewInfoDTO wDto) {
	    if (description.contains("工作地區：")) {
	        String area = extractBetween(description, "工作地區：", "。");
	        wDto.setArea(area);
	    }

	    if (description.contains("相關職務經驗：")) {
	        String yearStr = extractBetween(description, "相關職務經驗：", "。").replaceAll("\\D+", "");
	        if (!yearStr.isEmpty()) {
	            wDto.setRelativeExperienceYear(Float.parseFloat(yearStr));
	        }
	    }

	    if (description.contains("每週工時：")) {
	        String hoursStr = extractBetween(description, "每週工時：", "小時");
	        if (!hoursStr.isEmpty()) {
	            wDto.setPerWeekWorkHours(Integer.parseInt(hoursStr));
	        }
	    }

	    if (description.contains("薪水：")) {
	        String salary = extractBetween(description, "薪水：", "。");
	        wDto.setTreatment(salary);
	    }

	    if (description.contains("可以考慮")) {
	        wDto.setIsGood(1); // 推薦
	    } else {
	        wDto.setIsGood(0); // 不推薦或不明
	    }
	}

	//取得關鍵字段
	private String extractBetween(String text, String start, String end) {
	    int startIndex = text.indexOf(start);
	    if (startIndex == -1) return "";
	    startIndex += start.length();
	    int endIndex = text.indexOf(end, startIndex);
	    if (endIndex == -1) return text.substring(startIndex).trim();
	    return text.substring(startIndex, endIndex).trim();
	}

	
	public JobInterviewInfoDTO formatInterviewInfo(Document doc) {
		System.out.println("in formatInterviewInfo");
		JobInterviewInfoDTO jDto=new JobInterviewInfoDTO();
		jDto.setShareType(doc.select(".src-components-ExperienceDetail-Heading-__Heading-module___badge").html());
		jDto.setShareDate(doc.select(".src-components-ExperienceDetail-Article-__Article-module___date").html());
		jDto.setPageTitle(doc.select(".src-components-common-base-__Heading-module___l").get(0).text());
		Elements contents=doc.select(".src-components-ExperienceDetail-Article-__InfoBlock-module___content");
		//先將contents前元素文字取出（標題）依序存成陣列，比對陣列文字，再使用陣列索引取得內容
		List<String> titleList=new ArrayList<>();
		for(Element con:contents) {
			titleList.add(con.previousElementSibling().text());
		}
		
		for(String title:titleList) {
			if(title.contains("公司")) {
				jDto.setCompanyName(contents.get(titleList.indexOf(title)).text());
			}else if(title.contains("面試地區")) {
				jDto.setArea(contents.get(titleList.indexOf(title)).text());
				
			}else if(title.contains("應徵職稱")) {
				jDto.setJobTitle(contents.get(titleList.indexOf(title)).text());
				
			}else if(title.contains("相關職務工作經驗")) {
				String str =contents.get(titleList.indexOf(title)).text().replaceAll("\\D+", ""); // 移除非數字字符	
				jDto.setRelativeExperienceYear(Float.parseFloat(str));
				
			}else if(title.contains("面試時間")) {

				jDto.setInterviewTime(contents.get(titleList.indexOf(title)).text());
				
			}else if(title.contains("面試結果")) {
				jDto.setResult(contents.get(titleList.indexOf(title)).text());
				
			}else if(title.contains("待遇")) {
				jDto.setTreatment(contents.get(titleList.indexOf(title)).text());
				
			}else if(title.contains("整體面試滿意度")) {
				Element ele=contents.get(titleList.indexOf(title));
				jDto.setSatisfactionScore(ele.getElementsByClass("src-components-common-button-__RateButtonElement-module___active").size());
//				System.out.println(ele.getElementsByClass("src-components-common-button-__RateButtonElement-module___active").size());
				
			}else if(title.contains("特殊問題")) {
				jDto.setOddQuestions(contents.get(titleList.indexOf(title)).text());
				
			}
			
		}
		
		Elements shareArticle = doc.select(".src-components-ExperienceDetail-Article-__Article-module___article");
		Elements BoldSections = doc.select(".src-components-common-base-__P-module___bold");
		BoldSections.addClass("fw-bold");
		BoldSections.addClass("fs-5");
		BoldSections.addClass("text-secondary");
		BoldSections.addClass("mb-2");
		BoldSections.addClass("mt-2");
		
		jDto.setShareContent(shareArticle.html());
		
		System.out.println("out formatInterviewInfo");
		return jDto;
		
	}
	
	
	public WorkReviewInfoDTO formatWorkReviewInfo(Document doc) {
		WorkReviewInfoDTO wDto=new WorkReviewInfoDTO();
		
		wDto.setShareType(doc.select(".src-components-ExperienceDetail-Heading-__Heading-module___badge").html());

		wDto.setShareDate(doc.select(".src-components-ExperienceDetail-Article-__Article-module___date").html());
		wDto.setPageTitle(doc.select(".src-components-common-base-__Heading-module___l").get(0).text());
		Elements contents=doc.select(".src-components-ExperienceDetail-Article-__InfoBlock-module___content");
		
		//先將contents前元素文字取出（標題）依序存成陣列，比對陣列文字，再使用陣列索引取得內容
				List<String> titleList=new ArrayList<>();
				for(Element con:contents) {
					titleList.add(con.previousElementSibling().text());
				}
				
				for(String title:titleList) {
					if(title.contains("工作地區")) {
						wDto.setArea(contents.get(titleList.indexOf(title)).text());
						
					}else if(title.contains("職稱")) {
						wDto.setJobTitle(contents.get(titleList.indexOf(title)).text());
						
					}else if(title.contains("相關職務工作經驗")) {
						String str =contents.get(titleList.indexOf(title)).text().replaceAll("\\D+", ""); // 移除非數字字符	
						wDto.setRelativeExperienceYear(Float.parseFloat(str));
						
					}else if(title.contains("最高學歷")) {

						wDto.setHighestDegree(contents.get(titleList.indexOf(title)).text());
						
					}else if(title.contains("一週工時")) {
						wDto.setPerWeekWorkHours(Integer.parseInt(contents.get(titleList.indexOf(title)).text()));
						
					}else if(title.contains("待遇")) {
						wDto.setTreatment(contents.get(titleList.indexOf(title)).text());
						
					}else if(title.contains("是否推薦此工作")) {
						String isGood=contents.get(titleList.indexOf(title)).text();
						if(isGood.equals("不推")) {
							wDto.setIsGood(0);
						}else {
							wDto.setIsGood(1);
						}						
					}
					
				}
				
				Elements shareArticle = doc.select(".src-components-ExperienceDetail-Article-__Article-module___article");
				Elements BoldSections = doc.select(".src-components-common-base-__P-module___bold");
				BoldSections.addClass("fw-bold");
				BoldSections.addClass("fs-5");
				BoldSections.addClass("text-secondary");
				BoldSections.addClass("mb-2");
				BoldSections.addClass("mt-2");
				
				wDto.setShareContent(shareArticle.html());
		
		
		
		
		
		return wDto;
		
		
		
	}

}
