package de.flashheart.ocfflag.misc;

public interface Observer<ObservedType> {
    public void update(Observable<ObservedType> object, ObservedType data);
}