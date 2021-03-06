/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Arne Kepp / The Open Planning Project 2009
 *  
 */
package org.geowebcache.conveyor;

import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.geowebcache.GeoWebCacheException;
import org.geowebcache.storage.StorageBroker;
import org.geowebcache.storage.StorageException;
import org.geowebcache.storage.WFSObject;

public class ConveyorWFS extends Conveyor {
    
    WFSObject stObj = null;
    
    public ConveyorWFS(StorageBroker sb, String parameters, byte[] queryBlob, 
            HttpServletRequest srq, HttpServletResponse srp) {
        super(sb, srq, srp);
        super.setRequestHandler(Conveyor.RequestHandler.SERVICE);
        
        if(queryBlob != null) {
            stObj = WFSObject.createQueryWFSObject(queryBlob);
        } else if(parameters != null) {
            stObj = WFSObject.createQueryWFSObject(parameters);
        }
    }
    
    public byte[] getQueryBlob() {
        return stObj.getQueryBlob();
    }
    
    public boolean persist() throws GeoWebCacheException {
        return storageBroker.put(stObj);
    }
    
    public String getMimeTypeString() {
        return stObj.getBlobFormat();
    }
    
    public void setMimeTypeString(String mimeType) {
        stObj.setBlobFormat(mimeType);
    }
    
    public boolean retrieve(int maxAge) throws GeoWebCacheException {
        try {
            return storageBroker.get(stObj);
        } catch (StorageException se) {
            throw new GeoWebCacheException(se.getMessage());
        }
    }
    
    public InputStream getInputStream() {
        return stObj.getInputStream();
    }
    
    public void setInputStream(InputStream is) {
        stObj.setInputStream(is);
    }
}
