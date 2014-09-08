package eu.cyfronoid.audio.player.component.model;

import java.util.Date;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.impl.triggers.SimpleTriggerImpl;

import com.google.common.eventbus.EventBus;

import eu.cyfronoid.audio.player.PlayerConfigurator;
import eu.cyfronoid.audio.player.event.UpdateTreeEvent;
import eu.cyfronoid.audio.player.song.library.SongLibrary;
import eu.cyfronoid.framework.scheduler.AbstractTask;
import eu.cyfronoid.framework.scheduler.Scheduler;

public class SongTreeModel extends DefaultTreeModel {
    private static final Logger logger = Logger.getLogger(SongTreeModel.class);
    private static final long serialVersionUID = 5848931979263323655L;

    public SongTreeModel(TreeNode root) {
        super(root);
        Scheduler scheduler = PlayerConfigurator.injector.getInstance(Scheduler.class);
        scheduler.startTask(new TreeStructureChangeListener());
    }

    public static class TreeStructureChangeListener extends AbstractTask {
        private static final int REPEAT_EVERY_N_SECONDS = 30;
        private EventBus eventBus = PlayerConfigurator.injector.getInstance(EventBus.class);

        @Override
        public Trigger getTrigger() {
            SimpleTriggerImpl simpleTrigger = new SimpleTriggerImpl();
            simpleTrigger.setStartTime(new Date(System.currentTimeMillis() + REPEAT_EVERY_N_SECONDS*1000));
            simpleTrigger.setRepeatCount(SimpleTrigger.REPEAT_INDEFINITELY);
            simpleTrigger.setRepeatInterval(REPEAT_EVERY_N_SECONDS*1000);
            simpleTrigger.setName("TreeStructureChangeListenerTrigger");
            return simpleTrigger;
        }

        @Override
        public void execute(JobExecutionContext context)
                throws JobExecutionException {
            logger.debug("Update tree");
            DefaultMutableTreeNode treeRoot = SongLibrary.INSTANCE.buildTree();
            eventBus.post(new UpdateTreeEvent(treeRoot));
        }

    }

}
