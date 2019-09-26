package job.service.impl;


import job.core.enumType.AutoTaskJobStatusEnum;
import job.core.manager.QuartzManager;
import job.pojo.vo.ScheduleJobVo;
import job.service.CoreAutoTaskService;
import job.service.ScheduleJobService;
import job.util.SpringContextUtil;
import job.util.ToolsUtils;
import org.quartz.CronScheduleBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CoreAutoTaskServiceImpl implements CoreAutoTaskService {

    @Autowired
    private QuartzManager quartzManager;
    @Autowired
    private ScheduleJobService scheduleJobService;

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void updateJobStatus(ScheduleJobVo dto) throws Exception {
        ScheduleJobVo scheduleJobVo = scheduleJobService.selectByPK(dto.getId());

        if (dto.getValidInd() != null) {
            scheduleJobVo.setValidInd(dto.getValidInd());
            if (scheduleJobVo.getValidInd()) {
                // 有效状态时，job 的状态保持不变
                dto.setJobStatus(scheduleJobVo.getJobStatus());
            } else {
                // 无效时，暂停job
                dto.setJobStatus(AutoTaskJobStatusEnum.JOB_STATUS_PAUSE.getStatus());
            }
        }

        String jobStatus = dto.getJobStatus();
        if (AutoTaskJobStatusEnum.JOB_STATUS_RESUME.getStatus().equals(jobStatus)) {
            quartzManager.resumeJob(scheduleJobVo);
        } else if (AutoTaskJobStatusEnum.JOB_STATUS_PAUSE.getStatus().equals(jobStatus)) {
            quartzManager.pauseJob(scheduleJobVo);
        }
        scheduleJobVo.setJobStatus(jobStatus);
        scheduleJobService.updateByPk(scheduleJobVo);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void deleteAutoTask(Long jobId) throws Exception {
        ScheduleJobVo scheduleJobVo = scheduleJobService.selectByPK(jobId);
        quartzManager.deleteJob(scheduleJobVo);
        scheduleJobService.deleteByPK(jobId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public void saveAutoTask(ScheduleJobVo scheduleJobVo) throws Exception {
        if (null != scheduleJobVo.getId()) {
            // 如果是更新就直接删除旧的任务，重新添加（主要是更新JobDataMap）
            ScheduleJobVo jobVo = new ScheduleJobVo();
            jobVo.setJobName(scheduleJobVo.getOldJobName());
            jobVo.setJobGroup(scheduleJobVo.getOldJobGroup());
            quartzManager.deleteJob(jobVo);
        }

        if (!scheduleJobVo.getValidInd()) {
            scheduleJobVo.setJobStatus(AutoTaskJobStatusEnum.JOB_STATUS_PAUSE.getStatus());
        } else {
            quartzManager.addJob(scheduleJobVo);
            if (AutoTaskJobStatusEnum.JOB_STATUS_PAUSE.getStatus().equals(scheduleJobVo.getJobStatus())) {
                quartzManager.pauseJob(scheduleJobVo);
            }
        }
        scheduleJobService.save(scheduleJobVo);
    }

    @Override
    public ScheduleJobVo checkAutoTask(ScheduleJobVo scheduleJobVo) throws Exception {

        String message = "";

        String jobStatus = scheduleJobVo.getJobStatus();
        String cronExpression = scheduleJobVo.getCronExpression();
        String springId = scheduleJobVo.getSpringId();
        String beanClass = scheduleJobVo.getBeanClass();
        String methodName = scheduleJobVo.getMethodName();
        String jobName = scheduleJobVo.getJobName();
        String jobGroup = scheduleJobVo.getJobGroup();
        String oldJobName = scheduleJobVo.getOldJobName();
        String oldJobGroup = scheduleJobVo.getOldJobGroup();

        if ((ToolsUtils.isEmpty(oldJobName) && ToolsUtils.isEmpty(oldJobGroup))
                || !oldJobName.equals(jobName) || !oldJobGroup.equals(jobGroup)) {
            int count = scheduleJobService.countByJobNameAndJobGroup(jobName, jobGroup);
            if (count > 0) {
                message += "自动任务已经存在！<br>";
                scheduleJobVo.setMessage(message);
                return scheduleJobVo;
            }
        }

        if (ToolsUtils.isNotEmpty(cronExpression)) {
            try {
                CronScheduleBuilder.cronSchedule(cronExpression);
            } catch (Exception e) {
                message += "cron表达式错误，请查证！<br>";
            }
        }
        if (AutoTaskJobStatusEnum.JOB_STATUS_RESUME.getStatus().equals(jobStatus)) {
            // bean能否获取标识
            boolean beanFlag = true;
            Class<?> clazz = null;
            // bean相关检查
            if (ToolsUtils.isNotEmpty(springId)) {
                try {
                    Object bean = SpringContextUtil.getBean(springId);
                    clazz = bean.getClass();
                } catch (Exception e) {
                    message += "springId错误，未获取相关Bean！<br>";
                    beanFlag = false;
                }
            } else {
                try {
                    clazz = Class.forName(beanClass);
                    clazz.newInstance();
                } catch (Exception e) {
                    message += "beanClass错误，未获取相关类！<br>";
                    beanFlag = false;
                }
            }
            if (!beanFlag) {
                scheduleJobVo.setMessage(message);
                return scheduleJobVo;
            }
            // 对执行方法检查（bean可以获取）
            if (ToolsUtils.isNotEmpty(methodName)) {
                try {
                    clazz.getDeclaredMethod(methodName);
                } catch (NoSuchMethodException | SecurityException e) {
                    message += "方法：" + methodName + "，未获取！<br>";
                }
            }
        }
        scheduleJobVo.setMessage(message);
        return scheduleJobVo;
    }

    @Override
    public void immediateExecutionTask(ScheduleJobVo scheduleJobVo) throws Exception {
        quartzManager.runAJobNow(scheduleJobVo);
    }

//    @Override
//    public void insertAutoTaskException(JobExecutionContext jobExecutionContext, Exception exception) throws Exception {
//        ScheduleJobVo scheduleJob = (ScheduleJobVo) jobExecutionContext.getMergedJobDataMap().get(JobDataCst.SCHEDULE_JOB_VO);
//        ScheduleJobVo jobdto = new ScheduleJobVo();
//        jobdto.setId(scheduleJob.getId());
//        jobdto.setJobStatus(AutoTaskJobStatusEnum.JOB_STATUS_EXCEPTION.getStatus());
//        scheduleJobService.updateJobStatus(jobdto);
//        if (exception != null) {
//            StringWriter sw = new StringWriter();
//            exception.printStackTrace(new PrintWriter(sw, true));
//            ScheduleJobLogVo logDto = new ScheduleJobLogVo();
////            logDto.setId(UUID.generate());
//            logDto.setId(scheduleJob.getId());
//            logDto.setJobGroup(scheduleJob.getJobGroup());
//            logDto.setJobName(scheduleJob.getJobName());
//            logDto.setCreateTime(new Date());
//            logDto.setInfo(sw.toString());
//            scheduleJobLogService.insert(logDto);
//        }
//    }
}