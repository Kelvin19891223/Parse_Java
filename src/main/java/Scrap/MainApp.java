/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package Scrap;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import org.apache.log4j.Appender;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;
import org.apache.log4j.SimpleLayout;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

public class MainApp {
	
	static Logger log = Logger.getLogger(MainApp.class);
	
	public void run() throws Exception {
		// Read setting file
		Settings.init();

		SimpleLayout layout = new SimpleLayout();       
		RollingFileAppender appender = new RollingFileAppender(layout, Settings.param.get("logfile").toString(), false);
	    log.addAppender((Appender)appender);
	    
	    // First we must get a reference to a scheduler
	    SchedulerFactory sf = new StdSchedulerFactory();
	    Scheduler sched = sf.getScheduler();
	    
	    JobDetail job = newJob(MyJob.class).withIdentity("job1", "group1").build();
	    CronTrigger trigger = newTrigger().withIdentity("trigger1", "group1").withSchedule(cronSchedule(Settings.param.get("daily.download.cron").toString())).build();	   
	    
	    sched.scheduleJob(job, trigger);
	    
	    sched.start();
	}

	public static void main(String[] args)  throws Exception {		
		
		MainApp example = new MainApp();
	    example.run();
	}
}