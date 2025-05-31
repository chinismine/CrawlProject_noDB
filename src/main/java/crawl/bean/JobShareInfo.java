package crawl.bean;

import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JobShareInfo {
	/**共用欄位**/
	
		private String jobId;
		
		private String shareDate;
		
		private String pageTitle;
		
		private String shareType;
		
//		公司：
		private String companyName;
		
//		工作/面試地區：
		private String area;
		
//		應徵職稱：
		private String jobTitle;
		
//		相關職務工作經驗：
		private Float relativeExperienceYear;
		
//		查詢時間
		private Date searchTime;

//		被查詢次數
		
		private Integer searchedCount;
		
//		分享內容
		private String shareContent;
		
		
		
		/**面試欄位**/
		
//		面試時間：
		private String interviewTime;
		
//		面試結果：
		private String result;
		
//		整體面試滿意度：?/5
		private Integer satisfactionScore;
		
//		特殊問題
		private String oddQuestions;
		
		/**工作分享欄位**/

//		最高學歷：
		private String highestDegree;

//		一週工時：
		private Integer perWeekWorkHours;

//		待遇		
		private String treatment;

//		是否推薦此工作：
		private Integer isGood;


}
