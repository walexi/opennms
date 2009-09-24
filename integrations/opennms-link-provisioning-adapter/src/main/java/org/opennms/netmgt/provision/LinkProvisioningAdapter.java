/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: September 24, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.provision;


import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;


/**
 * This adapter automatically creates links between nodes based on an expression applied
 * to the node label (hostname)
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class LinkProvisioningAdapter extends SimplerQueuedProvisioningAdapter {

    private static final String ADAPTER_NAME = "LinkAdapter";
    
    public LinkProvisioningAdapter() {
        super(ADAPTER_NAME);
    }

    public void doAddNode(int nodeid) {
        
    }
    
    public void doUpdateNode(int nodeid) {
        
    }
    
    public void doDeleteNode(int nodeid) {
        
    }
    
    public void doNotifyConfigChange(int nodeid) {
        
    }
    
    
    private static Category log() {
        return ThreadCategory.getInstance(LinkProvisioningAdapter.class);
    }

}
