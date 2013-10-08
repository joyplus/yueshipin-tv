package com.joyplus.sub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public abstract class JoyplusURIList extends SubModel implements List<SubURI>{
	protected int MAX = 1;
	private final ArrayList<SubURI> mMedia = new ArrayList<SubURI>();//It always only one item.
	//Interface of get next suburi which need to instance.
	public SubURI getNextInstance(){
		if(mMedia == null)return null;
		for(SubURI suburi:mMedia){
			if(!suburi.Instanced)return suburi;
		}
		return null;
	}
	private void internalAdd(SubURI media) throws IllegalStateException {
        if (media == null || media.Uri == null || "".equals(media.Uri)){
            // Don't add null value into the list.
            return;
        }
        for(SubURI sub : mMedia){        	
        	if(sub.Uri.equals(media.Uri))return;
        }
        internalAddOrReplace(null,media);
	}
	private void internalAddOrReplace(SubURI old, SubURI media) {
    	if(old == null){
    		mMedia.add(media);
    	}else{
    		mMedia.set(mMedia.indexOf(old), media);
    		old.unregisterAllModelChangedObservers();
    	}
    	for (ISubModelChangedObserver observer : mSubModelChangedObservers) {
            media.registerModelChangedObserver(observer);
        }
    }
	private boolean internalRemove(Object object) {
        if (mMedia.remove(object)) { 
            ((SubURI) object).unregisterAllModelChangedObservers();
            return true;
        }
        return false;
    }
	@Override
	public boolean add(SubURI object) {
		// TODO Auto-generated method stub
		if(this.size() >= MAX)return false;
		internalAdd(object);
        notifyModelChanged(true);		
		return false;
	}

	@Override
	public void add(int location, SubURI object) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Operation not supported.");
	}

	@Override
	public boolean addAll(Collection<? extends SubURI> arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Operation not supported.");
	}

	@Override
	public boolean addAll(int arg0, Collection<? extends SubURI> arg1) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Operation not supported.");
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub
		if (mMedia.size() > 0) {
            for (SubURI media : mMedia) {
                media.unregisterAllModelChangedObservers();
            }
            mMedia.clear();           
            notifyModelChanged(true);
        }
	}

	@Override
	public boolean contains(Object object) {
		// TODO Auto-generated method stub
		return mMedia.contains(object);
	}

	@Override
	public boolean containsAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return mMedia.containsAll(arg0);
	}

	@Override
	public SubURI get(int location) {
		// TODO Auto-generated method stub
		if (mMedia.size() == 0) {
	        return null;
	    }
	    return mMedia.get(location);
	}

	@Override
	public int indexOf(Object object) {
		// TODO Auto-generated method stub
		return mMedia.indexOf(object);
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return mMedia.isEmpty();
	}

	@Override
	public Iterator<SubURI> iterator() {
		// TODO Auto-generated method stub
		return mMedia.iterator();
	}

	@Override
	public int lastIndexOf(Object object) {
		// TODO Auto-generated method stub
		return mMedia.lastIndexOf(object);
	}

	@Override
	public ListIterator<SubURI> listIterator() {
		// TODO Auto-generated method stub
		return mMedia.listIterator();
	}

	@Override
	public ListIterator<SubURI> listIterator(int location) {
		// TODO Auto-generated method stub
		return mMedia.listIterator(location);
	}

	@Override
	public SubURI remove(int location) {
		// TODO Auto-generated method stub
		SubURI media = mMedia.get(location);
        if ((media != null) && internalRemove(media)) {
            notifyModelChanged(true);
        }
        return media;
	}

	@Override
	public boolean remove(Object object) {
		// TODO Auto-generated method stub
		if ((object != null) && (object instanceof SubURI)
                && internalRemove(object)) {
            notifyModelChanged(true);
            return true;
        }
        return false;
	}

	@Override
	public boolean removeAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean retainAll(Collection<?> arg0) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Operation not supported.");
	}

	@Override
	public SubURI set(int location, SubURI object) {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Operation not supported.");
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return mMedia.size();
	}

	@Override
	public List<SubURI> subList(int start, int end) {
		// TODO Auto-generated method stub
		return mMedia.subList(start, end);
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return mMedia.toArray();
	}

	@Override
	public <T> T[] toArray(T[] array) {
		// TODO Auto-generated method stub
		return mMedia.toArray(array);
	}
}
