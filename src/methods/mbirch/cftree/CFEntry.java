package methods.mbirch.cftree;

import java.util.*;

import config.FeatureManager;
import object.DigitalObject;

/**
 * 
 * @author Roberto Perdisci (roberto.perdisci@gmail.com)
 *
 */

public class CFEntry {
	
private static final String LINE_SEP = System.getProperty("line.separator");
	
	private int n = 0; // number of patterns summarized by this entry
	private DigitalObject medoid = null;
	private ArrayList<DigitalObject> clusterEntry = null;
	private CFNode child = null;
	private ArrayList<Integer> indexList = null;
	private int subclusterID = -1; // the unique id the describes a subcluster (valid only for leaf entries)
	
	public CFEntry() {
	}
	
	public CFEntry(DigitalObject x) {
		this(x,0);
	}
	
	public CFEntry(DigitalObject x, int index) {
		this.n = 1;
		this.medoid = x;
		
		clusterEntry = new ArrayList<DigitalObject>();
		clusterEntry.add(x);
		indexList = new ArrayList<Integer>();
		indexList.add(index);
	}
	
	/**
	 * This makes a deep copy of the CFEntry e.
	 * WARNING: we do not make a deep copy of the child!!!
	 * 
	 * @param e the entry to be cloned
	 */
	public CFEntry(CFEntry e) {
		this.n = e.n;
		this.medoid = e.medoid;
		this.child = e.child; // WARNING: we do not make a deep copy of the child!!!
		
		this.indexList = new ArrayList<Integer>();
		for(int i : e.getIndexList()) // this makes sure we get a deep copy of the indexList
			this.indexList.add(i);
		
		this.clusterEntry = new ArrayList<DigitalObject>();
		for(DigitalObject db : e.getClusterEntry())
			this.clusterEntry.add(db);
	}
	
	protected DigitalObject getMedoid() {
		return medoid;
	}
	
	protected ArrayList<Integer> getIndexList() {
		return indexList;
	}
	
	protected ArrayList<DigitalObject> getClusterEntry() {
		return clusterEntry;
	}
	
	protected boolean hasChild() {
		return(child!=null);
	}
	
	protected CFNode getChild() {
		return child;
	}
	
	protected int getChildSize() {
		return child.getEntries().size();
	}
	
	protected void setChild(CFNode n) {
		child = n;
		indexList = null; // we don't keep this if this becomes a non-leaf entry
	}
	
	protected void setSubclusterID(int id) {
		subclusterID = id;
	}
	
	protected int getSubclusterID() {
		return subclusterID;
	}
	
	protected void update(CFEntry e, FeatureManager fm) {
		this.n += e.n;
		
		if(this.clusterEntry == null) {
			this.clusterEntry = new ArrayList<DigitalObject>();
			for(DigitalObject db : e.getClusterEntry()) {
				this.clusterEntry.add(db);
			}
		} else {
			this.clusterEntry.addAll(e.getClusterEntry());
		}
				
		if(medoid == null) {
			this.medoid = e.medoid;
		} else {
			
			double maxDist = Double.NEGATIVE_INFINITY;
			int newMedoidIndex = 0;
			
			for(int i = 0; i < this.clusterEntry.size(); i++) {
				maxDist = Math.max(maxDist, fm.getAvgDistance(medoid, clusterEntry.get(i)));
			}
			
			double minMaxDist = maxDist;
			
			for(int j = 0; j < clusterEntry.size(); j++) {
				DigitalObject candidate = clusterEntry.get(j);
				
				for(int k = 0; k < clusterEntry.size(); k++)
					maxDist = Math.max(maxDist, fm.getAvgDistance(candidate, clusterEntry.get(j)));
				
				if(maxDist < minMaxDist) {
					minMaxDist = maxDist;
					newMedoidIndex = j;
				}
			}
			
			DigitalObject newMedoid = clusterEntry.remove(newMedoidIndex);
			clusterEntry.add(0, newMedoid);
			this.medoid = clusterEntry.get(0);
		}
		
		
		if(!this.hasChild()) { // we keep indexList only if we are at a leaf
			if(this.indexList!=null && e.indexList!=null)
				this.indexList.addAll(e.indexList);
			else if(this.indexList==null && e.indexList!=null)
				this.indexList = (ArrayList<Integer>)e.indexList.clone();
		}		
	}
	
	protected void addToChild(CFEntry e) {
		// adds directly to the child node
		child.getEntries().add(e);
	}
	
	protected boolean isWithinThreshold(CFEntry e, double threshold, FeatureManager fm) {
		double dist = distance(e, fm);
		
		if(dist==0 || dist<=threshold) // read the comments in function d0() about differences with implementation in R
			return true;
		
		return false;
	}
	
	/**
	 * 
	 * @param e
	 * @return the distance between this entry and e
	 */
	protected double distance(CFEntry e, FeatureManager fm) {
		double dist = Double.MAX_VALUE;
		
		dist = fm.getAvgDistance(this.medoid, e.medoid);
		
		return dist;
	}
	
	public boolean equals(Object o) {
		CFEntry e = (CFEntry)o;
		
		if(this.n != e.n)
			return false;
		
		if(this.child!=null && e.child==null)
			return false;
		
		if(this.child==null && e.child!=null)
			return false;
		
		if(this.child!=null && !this.child.equals(e.child))
			return false;
		
		if(this.indexList==null && e.indexList!=null)
			return false;
		
		if(this.indexList!=null && e.indexList==null)
			return false;
		
		if(this.indexList!=null && !this.indexList.equals(e.indexList))
			return false;
		
		if(this.clusterEntry!=null && !this.clusterEntry.equals(e.clusterEntry))
			return false;
		
		return true;
	}
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append(" ");
		
		if(this.indexList!=null) {
			buff.append("( ");
			for(int i : indexList) {
				buff.append(i+" ");
			}
			buff.append(")");
		}
		if(this.hasChild()) {
			buff.append(LINE_SEP);
			buff.append("||" + LINE_SEP);
			buff.append("||" + LINE_SEP);
			buff.append(this.getChild());
		}		
		
		return buff.toString();
	}
}