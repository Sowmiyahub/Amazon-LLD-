import java.util.*;

class TextEditor {
    private StringBuilder text = new StringBuilder();
    private Stack<Command> undoStack = new Stack<>();
    private Stack<Command> redoStack = new Stack<>();

    public void insert(int position, String value) {
        Command command = new InsertCommand(this, position, value);
        command.execute();
        undoStack.push(command);
        redoStack.clear();
    }

    public void undo() {
        if (undoStack.isEmpty()) return;

        Command command = undoStack.pop();
        command.undo();
        redoStack.push(command);
    }

    public void redo() {
        if (redoStack.isEmpty()) return;

        Command command = redoStack.pop();
        command.execute();
        undoStack.push(command);
    }

    void insertText(int position, String value) {
        text.insert(position, value);
    }

    void deleteText(int position, int length) {
        text.delete(position, position + length);
    }

    public String getText() {
        return text.toString();
    }
}

interface Command {
    void execute();
    void undo();
}

class InsertCommand implements Command {
    private TextEditor editor;
    private int position;
    private String value;

    InsertCommand(TextEditor editor, int position, String value) {
        this.editor = editor;
        this.position = position;
        this.value = value;
    }

    public void execute() {
        editor.insertText(position, value);
    }

    public void undo() {
        editor.deleteText(position, value.length());
    }
}

public class Main {
    public static void main(String[] args) {
        TextEditor editor = new TextEditor();

        editor.insert(0, "Hello");
        editor.insert(5, " World");
        System.out.println(editor.getText()); // Hello World

        editor.undo();
        System.out.println(editor.getText()); // Hello

        editor.redo();
        System.out.println(editor.getText()); // Hello World
    }
}