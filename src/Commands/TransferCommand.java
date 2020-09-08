package Commands;

import java.util.List;

public class TransferCommand<T> implements Command {
    private final T element;
    private final List<T> a, b;

    public TransferCommand(T element, List<T> a, List<T> b) {
        this.element = element;
        this.a = a;
        this.b = b;
    }

    @Override
    public void execute() {
        a.add(element);
        b.remove(element);
    }

    @Override
    public void cancel() {
        a.remove(element);
        b.add(element);
    }
}
