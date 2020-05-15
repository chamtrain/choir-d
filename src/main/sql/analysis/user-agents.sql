-- Step counts by ip and ua (to figure out which ips are the clinic)
select cnt, client_ip_address, user_agent_str from (
 select count(*) cnt, client_ip_address, user_agent_id from survey_progress group by client_ip_address, user_agent_id
) g join survey_user_agent a on (g.user_agent_id=a.survey_user_agent_id)
order by 1 desc
;
-- looks like all clinic tablets are 68.65.175.22

-- Breakdown of all survey accesses by browser/device, separated by clinic/home
select 'At Home' location,
sum(cnt) total,
sum(case when user_agent_str like '%CHROME%' and user_agent_str not like '%ANDROID%'
                                             and user_agent_str not like '%IPAD%'
                                             and user_agent_str not like '%IPHONE%'
                                             and user_agent_str not like '%MOBILE%' then cnt else 0 end) Chrome_PC,
sum(case when user_agent_str like '%FIREFOX%' and user_agent_str not like '%ANDROID%'
                                              and user_agent_str not like '%IPAD%'
                                              and user_agent_str not like '%IPHONE%'
                                              and user_agent_str not like '%MOBILE%' then cnt else 0 end) Firefox_PC,
sum(case when user_agent_str like '%SAFARI%' and user_agent_str not like '%ANDROID%'
                                             and user_agent_str not like '%IPAD%'
                                             and user_agent_str not like '%IPHONE%'
                                             and user_agent_str not like '%MOBILE%'
                                             and user_agent_str not like '%CHROME%'
                                             and user_agent_str not like '%LINUX%' then cnt else 0 end) Safari_PC,
sum(case when (user_agent_str like '% MSIE %' or user_agent_str like '% TRIDENT/7.%')
    and user_agent_str not like '% IEMOBILE%' then cnt else 0 end) IE_PC,
sum(case when user_agent_str like '% MSIE 6%' and user_agent_str not like '% IEMOBILE%' then cnt else 0 end) IE6,
sum(case when user_agent_str like '% MSIE 7%' and user_agent_str not like '% IEMOBILE%' then cnt else 0 end) IE7,
sum(case when user_agent_str like '% MSIE 8%' and user_agent_str not like '% IEMOBILE%' then cnt else 0 end) IE8,
sum(case when user_agent_str like '% MSIE 9%' and user_agent_str not like '% IEMOBILE%' then cnt else 0 end) IE9,
sum(case when user_agent_str like '% MSIE 10%' and user_agent_str not like '% IEMOBILE%' then cnt else 0 end) IE10,
sum(case when user_agent_str like '% TRIDENT/7.%' and user_agent_str not like '% IEMOBILE%' then cnt else 0 end) IE11,
sum(case when user_agent_str like '%IPHONE%' then cnt else 0 end) iPhone,
sum(case when user_agent_str like '%IPAD%' then cnt else 0 end) iPad,
sum(case when user_agent_str like '%ANDROID%' then cnt else 0 end) Android,
sum(case when user_agent_str like '% IEMOBILE%' then cnt else 0 end) WINDOWS_PHONE,
sum(case when user_agent_str like '%BLACKBERRY%' then cnt else 0 end) BLACKBERRY,
sum(case when user_agent_str like '%NEXUS%' then cnt else 0 end) Nexus,
sum(case when user_agent_str like '% LG-%' then cnt else 0 end) LG,
sum(case when user_agent_str like '%SAMSUNG%' then cnt else 0 end) SAMSUNG,
sum(case when user_agent_str like '% HTC%' then cnt else 0 end) HTC,
sum(case when user_agent_str like '% DROID%' then cnt else 0 end) DROID,
sum(case when user_agent_str like '%SILK%' then cnt else 0 end) Kindle
from (
select count(distinct survey_token_id) as cnt, upper(to_char(user_agent_str)) user_agent_str
from survey_user_agent a join survey_progress p on (a.survey_user_agent_id = p.user_agent_id)
where p.client_ip_address != '68.65.175.22' and p.survey_site_id=1 and p.survey_token_id not in (
select survey_token_id from patient_study s join survey_token t on (to_char(s.token)=t.survey_token)
join patient p on (s.patient_id=p.patient_id)
where p.last_name='Test-Patient'
)
group by upper(to_char(user_agent_str))
)
union all
select 'In Clinic' location,
sum(cnt) total,
sum(case when user_agent_str like '%CHROME%' and user_agent_str not like '%ANDROID%'
                                             and user_agent_str not like '%IPAD%'
                                             and user_agent_str not like '%IPHONE%'
                                             and user_agent_str not like '%MOBILE%' then cnt else 0 end) Chrome_PC,
sum(case when user_agent_str like '%FIREFOX%' and user_agent_str not like '%ANDROID%'
                                              and user_agent_str not like '%IPAD%'
                                              and user_agent_str not like '%IPHONE%'
                                              and user_agent_str not like '%MOBILE%' then cnt else 0 end) Firefox_PC,
sum(case when user_agent_str like '%SAFARI%' and user_agent_str not like '%ANDROID%'
                                             and user_agent_str not like '%IPAD%'
                                             and user_agent_str not like '%IPHONE%'
                                             and user_agent_str not like '%MOBILE%'
                                             and user_agent_str not like '%CHROME%'
                                             and user_agent_str not like '%LINUX%' then cnt else 0 end) Safari_PC,
sum(case when (user_agent_str like '% MSIE %' or user_agent_str like '% TRIDENT/7.%')
    and user_agent_str not like '% IEMOBILE%' then cnt else 0 end) IE_PC,
sum(case when user_agent_str like '% MSIE 6%' and user_agent_str not like '% IEMOBILE%' then cnt else 0 end) IE6,
sum(case when user_agent_str like '% MSIE 7%' and user_agent_str not like '% IEMOBILE%' then cnt else 0 end) IE7,
sum(case when user_agent_str like '% MSIE 8%' and user_agent_str not like '% IEMOBILE%' then cnt else 0 end) IE8,
sum(case when user_agent_str like '% MSIE 9%' and user_agent_str not like '% IEMOBILE%' then cnt else 0 end) IE9,
sum(case when user_agent_str like '% MSIE 10%' and user_agent_str not like '% IEMOBILE%' then cnt else 0 end) IE10,
sum(case when user_agent_str like '% TRIDENT/7.%' and user_agent_str not like '% IEMOBILE%' then cnt else 0 end) IE11,
sum(case when user_agent_str like '%IPHONE%' then cnt else 0 end) iPhone,
sum(case when user_agent_str like '%IPAD%' then cnt else 0 end) iPad,
sum(case when user_agent_str like '%ANDROID%' then cnt else 0 end) Android,
sum(case when user_agent_str like '% IEMOBILE%' then cnt else 0 end) WINDOWS_PHONE,
sum(case when user_agent_str like '%BLACKBERRY%' then cnt else 0 end) BLACKBERRY,
sum(case when user_agent_str like '%NEXUS%' then cnt else 0 end) Nexus,
sum(case when user_agent_str like '% LG-%' then cnt else 0 end) LG,
sum(case when user_agent_str like '%SAMSUNG%' then cnt else 0 end) SAMSUNG,
sum(case when user_agent_str like '% HTC%' then cnt else 0 end) HTC,
sum(case when user_agent_str like '% DROID%' then cnt else 0 end) DROID,
sum(case when user_agent_str like '%SILK%' then cnt else 0 end) Kindle
from (
select count(distinct survey_token_id) as cnt, upper(to_char(user_agent_str)) user_agent_str
from survey_user_agent a join survey_progress p on (a.survey_user_agent_id = p.user_agent_id)
where p.client_ip_address = '68.65.175.22' and p.survey_site_id=1 and p.survey_token_id not in (
select survey_token_id from patient_study s join survey_token t on (to_char(s.token)=t.survey_token)
join patient p on (s.patient_id=p.patient_id)
where p.last_name='Test-Patient'
)
group by upper(to_char(user_agent_str))
)
;
