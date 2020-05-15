-- Player progress counts.
-- Find the number of people who have been offered the orientation video, the number who have played the video
-- and the numbers by time groups of how much of the video people actually played.
--
set heading off
set echo off
set feedback off

select 'As of ' || to_char(sysdate, 'hh:mi PM') || ' today, ' || to_char(sysdate, 'mm/dd/yyyy') || ':' from dual;

with total_cnts as (select count(*) cnt_off from patient_attribute where data_name = 'orientationVideo' and data_value = 'Y')
select cnt_off || ' patients have been offered the orientation video upon completing their initial survey '
from total_cnts;

select count(distinct survey_token_id) || ' of the patients offered the video hit play'
from survey_player_progress;

with completions as (
        select survey_token_id from survey_player_progress where player_action = 'Completed'
     ), last_time as (
        select survey_token_id, player_action, trunc(player_time_millis/1000) as seconds from REGISTRY.SURVEY_PLAYER_PROGRESS spp
        where posted_time = (select max(posted_time) from REGISTRY.SURVEY_PLAYER_PROGRESS spp2
                             where spp2.survey_token_id = spp.survey_token_id)
     ), hrs as (
        select survey_token_id, seconds, trunc(seconds / 60/60) as h from last_time
     ), mins as (
        select survey_token_id, seconds, h, trunc((seconds - h * 60 * 60 ) / 60) as m from hrs
     ), time_groups as ( select (case  when seconds < 10 then 'a.Watched for < 10 seconds'
                             when seconds >= 10 and seconds < 60 then 'b.Watched 10 to 59 seconds'
                             when seconds >= 60 and seconds < 300 then 'c.Watched at least 1 minute but < 5 '
                             when seconds>= 300 and seconds < 540 then 'd.Watched at least 5 minutes but < 9 '
                             when seconds>=540 and survey_token_id in (select survey_token_id from completions) then 'e.Watched the entire video'
                             else 'e.Watched 9 minutes or more but not completely' end ) as group_name
                         from mins order by group_name
    ), final_report as (
        select substr(group_name,0,2) as order_value, substr(group_name,3) as gname, count(*) as patients
        from time_groups group by substr(group_name, 0, 2), substr(group_name, 3)
    )
select patients , gname from final_report order by order_value;
