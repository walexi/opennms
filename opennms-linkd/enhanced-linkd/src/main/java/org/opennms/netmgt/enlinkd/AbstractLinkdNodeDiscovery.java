/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.enlinkd;

import static org.opennms.core.utils.InetAddressUtils.str;

import java.net.InetAddress;

import org.opennms.core.utils.LogUtils;
import org.opennms.netmgt.linkd.scheduler.ReadyRunnable;
import org.opennms.netmgt.linkd.scheduler.Scheduler;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.snmp.SnmpAgentConfig;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session
 * creating and collection occurs in the main run method of the instance. This
 * allows the collection to occur in a thread if necessary.
 */
public abstract class AbstractLinkdNodeDiscovery implements ReadyRunnable {

    /**
     * The SnmpPeer object used to communicate via SNMP with the remote host.
     */
    protected SnmpAgentConfig m_agentConfig;

    public SnmpAgentConfig getAgentConfig() {
		return m_agentConfig;
	}

	public void setAgentConfig(SnmpAgentConfig agentConfig) {
		m_agentConfig = agentConfig;
	}

	/**
     * The node ID of the system used to collect the SNMP information
     */
    protected final int m_nodeid;

    /**
     * The IP address used to collect the SNMP information
     */
    protected final InetAddress m_address;

    /**
     * The scheduler object
     */
    private Scheduler m_scheduler;

    /**
     * The interval, default value 30 minutes
     */
    private long poll_interval = 1800000;

    /**
     * The initial sleep time, default value 5 minutes
     */
    private long initial_sleep_time = 600000;

    private boolean suspendCollection = false;

    private boolean runned = false;

    private String packageName;

    protected final EnhancedLinkd m_linkd;

    /**
     * Constructs a new SNMP collector for a node using the passed interface
     * as the collection point. The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
     * @param nodeid
     * @param config
     *            The SnmpPeer object to collect from.
     */
    public AbstractLinkdNodeDiscovery(final EnhancedLinkd linkd, final int nodeid,
            final SnmpAgentConfig config) {
        m_linkd = linkd;
        m_agentConfig = config;
        m_nodeid = nodeid;
        m_address = m_agentConfig.getEffectiveAddress();
    }

    /**
     * <p>
     * Performs the collection for the targeted IP address. The success or
     * failure of the collection should be tested via the <code>failed</code>
     * method.
     * </p>
     * <p>
     * No synchronization is performed, so if this is used in a separate
     * thread context synchronization must be added.
     * </p>
     */
    public void run() {
    	EventBuilder builder;
        if (suspendCollection) {
            builder = new EventBuilder(
                    "uei.opennms.org/internal/linkd/nodeLinkDiscoverySuspended",
                    "EnhancedLinkd");
            builder.setNodeid(m_nodeid);
            builder.setInterface(m_address);
            m_linkd.getEventForwarder().sendNow(builder.getEvent());
            LogUtils.debugf(this, "run: address: %s Suspended!",
                            str(m_address));
        } else {
            builder = new EventBuilder(
                    "uei.opennms.org/internal/linkd/nodeLinkDiscoveryStarted",
                    "EnhancedLinkd");
            builder.setNodeid(m_nodeid);
            builder.setInterface(m_address);
            m_linkd.getEventForwarder().sendNow(builder.getEvent());
            
            runCollection();
            
            builder = new EventBuilder(
                    "uei.opennms.org/internal/linkd/nodeLinkDiscoveryCompleted",
                    "EnhancedLinkd");
            builder.setNodeid(m_nodeid);
            builder.setInterface(m_address);
            m_linkd.getEventForwarder().sendNow(builder.getEvent());

        }
        runned = true;
        reschedule();
    }
    
    protected abstract void runCollection(); 
    /**
     * <p>
     * getScheduler
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.linkd.scheduler.Scheduler} object.
     */
    public Scheduler getScheduler() {
        return m_scheduler;
    }

    /**
     * <p>
     * setScheduler
     * </p>
     * 
     * @param scheduler
     *            a {@link org.opennms.netmgt.linkd.scheduler.Scheduler}
     *            object.
     */
    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    /**
     * <p>
     * getInitialSleepTime
     * </p>
     * 
     * @return Returns the initial_sleep_time.
     */
    public long getInitialSleepTime() {
        return initial_sleep_time;
    }

    /**
     * <p>
     * setInitialSleepTime
     * </p>
     * 
     * @param initial_sleep_time
     *            The initial_sleep_timeto set.
     */
    public void setInitialSleepTime(long initial_sleep_time) {
        this.initial_sleep_time = initial_sleep_time;
    }

    /**
     * <p>
     * getPollInterval
     * </p>
     * 
     * @return Returns the initial_sleep_time.
     */
    public long getPollInterval() {
        return poll_interval;
    }

    /**
     * <p>
     * setPollInterval
     * </p>
     * 
     * @param interval
     *            a long.
     */
    public void setPollInterval(long interval) {
        this.poll_interval = interval;
    }

    /**
     * <p>
     * schedule
     * </p>
     */
    public void schedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "Cannot schedule a service whose scheduler is set to null");
        m_scheduler.schedule(initial_sleep_time, this);
    }

    /**
	 * 
	 */
    private void reschedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "Cannot schedule a service whose scheduler is set to null");
        m_scheduler.schedule(poll_interval, this);
    }

    /**
     * <p>
     * isReady
     * </p>
     * 
     * @return a boolean.
     */
    public boolean isReady() {
        return true;
    }

    /**
     * <p>
     * isSuspended
     * </p>
     * 
     * @return Returns the suspendCollection.
     */
    public boolean isSuspended() {
        return suspendCollection;
    }

    /**
     * <p>
     * suspend
     * </p>
     */
    public void suspend() {
        this.suspendCollection = true;
    }

    /**
     * <p>
     * wakeUp
     * </p>
     */
    public void wakeUp() {
    	setAgentConfig(m_linkd.getSnmpAgentConfig(m_address));
        this.suspendCollection = false;
    }

    /**
     * <p>
     * unschedule
     * </p>
     */
    public void unschedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "rescedule: Cannot schedule a service whose scheduler is set to null");
        if (runned) {
            m_scheduler.unschedule(this, poll_interval);
        } else {
            m_scheduler.unschedule(this, initial_sleep_time);
        }
    }


    /**
     * Returns the target address that the collection occurred for.
     * 
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getTarget() {
        return m_address;
    }

    /**
     * <p>
     * getReadCommunity
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getReadCommunity() {
        return m_agentConfig.getReadCommunity();
    }

    /**
     * <p>
     * getPeer
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.snmp.SnmpAgentConfig} object.
     */
    public SnmpAgentConfig getPeer() {
        return m_agentConfig;
    }

    /**
     * <p>
     * getPort
     * </p>
     * 
     * @return a int.
     */
    public int getPort() {
        return m_agentConfig.getPort();
    }

    /** {@inheritDoc} */
    public boolean equals(ReadyRunnable run) {
        if (run instanceof AbstractLinkdNodeDiscovery
                && this.getPackageName().equals(run.getPackageName())) {
            AbstractLinkdNodeDiscovery c = (AbstractLinkdNodeDiscovery) run;
            if (c.getTarget().equals(m_address))
                return true;
        }
        return false;
    }

    /**
     * <p>
     * getInfo
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public abstract String getInfo();

    /**
     * <p>
     * Getter for the field <code>packageName</code>.
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getPackageName() {
        return packageName;
    }

	@Override
	public void setPackageName(String pkg) {
		packageName=pkg;
	}

}