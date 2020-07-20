package com.xxl.job.admin.core.alarm.impl;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiRobotSendRequest;
import com.dingtalk.api.response.OapiRobotSendResponse;
import com.taobao.api.ApiException;
import com.xxl.job.admin.core.alarm.JobAlarm;
import com.xxl.job.admin.core.conf.XxlJobAdminConfig;
import com.xxl.job.admin.core.model.XxlJobGroup;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.admin.core.util.I18nUtil;
import com.xxl.job.core.biz.model.ReturnT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.internet.MimeMessage;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * job alarm by email
 *
 * @author xuxueli 2020-01-19
 */
@Component
public class DingTalkJobAlarm implements JobAlarm {
    private static Logger logger = LoggerFactory.getLogger(DingTalkJobAlarm.class);

    /**
     * fail alarm
     *
     * @param jobLog
     */
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog){
        boolean alarmResult = true;

        // send monitor email
        if (info!=null && info.getAlarmEmail()!=null && info.getAlarmEmail().trim().length()>0) {

            // alarmContent
            String alarmContent = "Alarm Job LogId=" + jobLog.getId();
            if (jobLog.getTriggerCode() != ReturnT.SUCCESS_CODE) {
                alarmContent += "<br>TriggerMsg=<br>" + jobLog.getTriggerMsg();
            }
            if (jobLog.getHandleCode()>0 && jobLog.getHandleCode() != ReturnT.SUCCESS_CODE) {
                alarmContent += "<br>HandleCode=" + jobLog.getHandleMsg();
            }

            // email info
            XxlJobGroup group = XxlJobAdminConfig.getAdminConfig().getXxlJobGroupDao().load(Integer.valueOf(info.getJobGroup()));
            String personal = I18nUtil.getString("admin_name_full");
            String title = I18nUtil.getString("jobconf_monitor");
            String content = MessageFormat.format(loadEmailJobAlarmTemplate(),
                    group!=null?group.getTitle():"null",
                    info.getId(),
                    info.getJobDesc(),
                    alarmContent);

            DingTalkClient client = new DefaultDingTalkClient("https://oapi.dingtalk.com/robot/send?access_token=d15a147cdc506dac616c7dd69b1ba58a4cd4bb0794752c88d9334f88b14879af");
			OapiRobotSendRequest request = new OapiRobotSendRequest();
			request.setMsgtype("text");
			OapiRobotSendRequest.Text text = new OapiRobotSendRequest.Text();
			text.setContent(content);
			request.setText(text);
			
			OapiRobotSendRequest.At at = new OapiRobotSendRequest.At();
			at.setAtMobiles(Arrays.asList("14797407238"));
			at.setIsAtAll(false);
			request.setAt(at);
			try {
				OapiRobotSendResponse response = client.execute(request);
			} catch (ApiException e) {
				e.printStackTrace();
			}
        }

        return alarmResult;
    }

    /**
     * load email job alarm template
     *
     * @return
     */
    private static final String loadEmailJobAlarmTemplate(){
        String mailBodyTemplate = "<h5>" + I18nUtil.getString("jobconf_monitor_detail") + "：</span>" +
                "<table border=\"1\" cellpadding=\"3\" style=\"border-collapse:collapse; width:80%;\" >\n" +
                "   <thead style=\"font-weight: bold;color: #ffffff;background-color: #ff8c00;\" >" +
                "      <tr>\n" +
                "         <td width=\"20%\" >"+ I18nUtil.getString("jobinfo_field_jobgroup") +"</td>\n" +
                "         <td width=\"10%\" >"+ I18nUtil.getString("jobinfo_field_id") +"</td>\n" +
                "         <td width=\"20%\" >"+ I18nUtil.getString("jobinfo_field_jobdesc") +"</td>\n" +
                "         <td width=\"10%\" >"+ I18nUtil.getString("jobconf_monitor_alarm_title") +"</td>\n" +
                "         <td width=\"40%\" >"+ I18nUtil.getString("jobconf_monitor_alarm_content") +"</td>\n" +
                "      </tr>\n" +
                "   </thead>\n" +
                "   <tbody>\n" +
                "      <tr>\n" +
                "         <td>{0}</td>\n" +
                "         <td>{1}</td>\n" +
                "         <td>{2}</td>\n" +
                "         <td>"+ I18nUtil.getString("jobconf_monitor_alarm_type") +"</td>\n" +
                "         <td>{3}</td>\n" +
                "      </tr>\n" +
                "   </tbody>\n" +
                "</table>";

        return mailBodyTemplate;
    }

}
