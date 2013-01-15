package de.unidue.wiwi.tdr.kn3.rasp_home;

import java.util.ArrayList;
import java.util.List;

public class Observable<T> {
	private final List<Observer<T>> observers = new ArrayList<Observer<T>>();

	public void addObserver(Observer<T> observer) {
		if (!observers.contains(observer))
			observers.add(observer);
	}

	public void deleteObserver(Observer<?> observer) {
		observers.remove(observer);
	}

	public void notifyObservers(T arg) {
		for (Observer<T> observer : observers)
			observer.update(this, arg);
	}
}