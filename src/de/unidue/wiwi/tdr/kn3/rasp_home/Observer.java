package de.unidue.wiwi.tdr.kn3.rasp_home;

interface Observer<T> {
	public void update(Observable<T> o, T arg);
}