package de.flashheart.ocfflag.misc;

/**
 * https://stackoverflow.com/questions/13362636/a-generic-observer-pattern-in-java
 */
import java.util.LinkedList;
import java.util.List;

public class Observable<ObservedType> {

    private List<Observer<ObservedType>> _observers =
      new LinkedList<Observer<ObservedType>>();

    public void addObserver(Observer<ObservedType> obs) {
        if (obs == null) {
            throw new IllegalArgumentException("Tried to add a null observer");
        }
        if (_observers.contains(obs)) {
            return;
        }
        _observers.add(obs);
    }

    public void notifyObservers(ObservedType data) {
        for (Observer<ObservedType> obs : _observers) {
            obs.update(this, data);
        }
    }
}