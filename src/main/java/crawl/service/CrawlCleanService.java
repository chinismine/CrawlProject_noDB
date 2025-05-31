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

@Service
public class CrawlCleanService {
	
	
	
	JobShareInfo jobBean=new JobShareInfo();
	

	public Object ReturnService(String url) {
		return formatData(url);	
	}
	
	
	//決定要用哪個方法（面試或工作經驗）
	public Object formatData(String url) {
		System.out.println("in formatData");
		
		String jid=url.replace("https://www.goodjob.life/experiences/", "");
		try {
			Document doc=Jsoup.connect(url).get();
			Elements type=doc.select(".src-components-ExperienceDetail-Heading-__Heading-module___badge");
			
			if("面試經驗".equals(type.html())) {
				JobInterviewInfoDTO interview = formatInterviewInfo(doc);
				
				return interview;
			}else if("工作心得".equals(type.html())) {
				WorkReviewInfoDTO review = formatWorkReviewInfo(doc);
				return review;
			}
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("end formatData");
		return "nooooooo";
		
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
