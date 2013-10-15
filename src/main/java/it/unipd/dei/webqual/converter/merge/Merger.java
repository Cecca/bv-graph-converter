package it.unipd.dei.webqual.converter.merge;

/**
 * Created with IntelliJ IDEA.
 * User: matteo
 * Date: 15/10/13
 * Time: 15.12
 * To change this template use File | Settings | File Templates.
 */
public interface Merger<T> {

  public T merge(T first, T second);

}
