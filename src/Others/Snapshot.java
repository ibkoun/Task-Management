package Others;

/**
 * Memento interface.
 */
public interface Snapshot {
    /**
     * Load the stored state of an object.
     * */
    void restore();
}
