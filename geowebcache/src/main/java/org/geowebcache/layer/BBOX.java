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
 * @author Chris Whitney
 *  
 */
package org.geowebcache.layer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class BBOX {
	private static Log log = LogFactory.getLog(org.geowebcache.layer.BBOX.class);
	private static String DELIMITER = ",";
	private static double equalityThreshold = 0.01;

	//minx, miny, maxx, maxy
	protected double[] coords = new double[4];

	public BBOX(String BBOX) {
		setFromBBOXString(BBOX);
		if(log.isTraceEnabled()) {
			log.trace("Created BBOX: " + getReadableString());
		}
	}

	public BBOX(String[] BBOX) {
		setFromStringArray(BBOX);
		if(log.isTraceEnabled()) {
			log.trace("Created BBOX: " + getReadableString());
		}
	}

	public BBOX(double minx, double miny, double maxx, double maxy) {
		coords[0] = minx;
		coords[1] = miny;
		coords[2] = maxx;
		coords[3] = maxy;

		if(log.isTraceEnabled()) {
			log.trace("Created BBOX: " + getReadableString());
		}
	}

	/**
	 * Sets from an array of strings
	 * @param BBOX
	 */
	public void setFromStringArray(String[] BBOX) {
		if(BBOX.length == 4) {
			coords[0] = Double.valueOf(BBOX[0]).doubleValue();
			coords[1] = Double.valueOf(BBOX[1]).doubleValue();
			coords[2] = Double.valueOf(BBOX[2]).doubleValue();
			coords[3] = Double.valueOf(BBOX[3]).doubleValue();

		} else if(BBOX.length == 1) {
			setFromBBOXString(BBOX[0]);
		}
	}

	/**
	 * Parses the BBOX parameters from a comma separted value list
	 * @param BBOX
	 */
	public void setFromBBOXString(String BBOX) {
		String[] tokens = BBOX.split(DELIMITER);
		setFromStringArray(tokens);
	}

	/**
	 * Outputs a string suitable for logging and other human-readable tasks
	 * @return a readable string
	 */
	public String getReadableString() {
		return "Min X: " + coords[0] + " Min Y: " + coords[1] +
		" Max X: " + coords[2] + " Max Y: " + coords[3];
	}

	/**
	 * Returns a comma separated value String suitable for URL output
	 */
	public String toString() {
		StringBuffer buff = new StringBuffer(40);
		buff.append(coords[0]);
		buff.append(',');
		buff.append(coords[1]);
		buff.append(',');
		buff.append(coords[2]);
		buff.append(',');
		buff.append(coords[3]);
		return buff.toString();
	}

	/**
	 * Comparing whether the differences between the bounding boxes can be ignored.
	 * 
	 * @param other 
	 * @return whether the boxes are equal
	 */
	public boolean equals(BBOX other) {
		return (coords[0] - other.coords[0])/(coords[0] + other.coords[0]) < equalityThreshold  
		&& (coords[1] - other.coords[1])/(coords[1] + other.coords[1]) < equalityThreshold
		&& (coords[2] - other.coords[2])/(coords[2] + other.coords[2]) < equalityThreshold
		&& (coords[3] - other.coords[3])/(coords[3] + other.coords[3]) < equalityThreshold;
	}


	/**
	 * Check whether this bbox contains the bbox
	 * 
	 * @param other 
	 * @return whether other is contained by this
	 */
	public boolean contains(BBOX other) {
		return (coords[0] <= other.coords[0] && coords[1] <= other.coords[1] &&
				coords[2] >= other.coords[2] && coords[3] >= other.coords[3]);
	}


	/**
	 * Minimal sanity check
	 * @return whether min x < max x, min y < max y
	 */
	public boolean isSane() {
		return (coords[0] < coords[2] && coords[1] < coords[3]);
	}
	
	public int hashCode() {
		return  Float.floatToIntBits((float) coords[0]) ^ Float.floatToIntBits((float) coords[2]);
	}
}