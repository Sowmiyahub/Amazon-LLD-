import java.util.*;

/*
 * File Management System
 *
 * 1. FileManagementSystem - create, read, delete, search.
 * 2. File - stores file name and content.
 * 3. Folder - stores files and subfolders.
 *
 * Folder contains other folders and files.
 * HashMap provides O(1) average child lookup.
 * Supports creating folders/files, reading, deleting, and searching.
 */

public class Main {
    public static void main(String[] args) {
        FileManagementSystem fs = new FileManagementSystem();

        fs.createFolder("/docs");
        fs.createFolder("/docs/projects");
        fs.createFile("/docs/notes.txt", "Hello World");
        fs.createFile("/docs/projects/design.txt", "LLD notes");

        System.out.println(fs.readFile("/docs/notes.txt"));
        System.out.println(fs.search("design.txt"));

        fs.delete("/docs/notes.txt");
    }
}

class FileManagementSystem {
    private Folder root = new Folder("");

    public void createFolder(String path) {
        Folder current = root;

        for (String part : parts(path)) {
            if (current.children.containsKey(part)) {
                if (!(current.children.get(part) instanceof Folder))
                    throw new IllegalArgumentException("File exists");
                current = (Folder) current.children.get(part);
            } else {
                Folder folder = new Folder(part);
                current.children.put(part, folder);
                current = folder;
            }
        }
    }

    public void createFile(String path, String content) {
        Folder parent = getFolder(parentPath(path));
        String name = fileName(path);

        if (parent.children.containsKey(name))
            throw new IllegalArgumentException("Already exists");

        parent.children.put(name, new File(name, content));
    }

    public String readFile(String path) {
        FileSystemItem item = getItem(path);

        if (!(item instanceof File))
            throw new IllegalArgumentException("Not a file");

        return ((File) item).content;
    }

    public void delete(String path) {
        Folder parent = getFolder(parentPath(path));
        String name = fileName(path);

        if (parent.children.remove(name) == null)
            throw new IllegalArgumentException("Item not found");
    }

    public List<String> search(String name) {
        List<String> result = new ArrayList<>();
        search(root, "", name, result);
        return result;
    }

    private void search(Folder folder, String path, String name,
                        List<String> result) {
        for (FileSystemItem item : folder.children.values()) {
            String currentPath = path + "/" + item.name;

            if (item.name.equals(name))
                result.add(currentPath);

            if (item instanceof Folder)
                search((Folder) item, currentPath, name, result);
        }
    }

    private FileSystemItem getItem(String path) {
        FileSystemItem current = root;

        for (String part : parts(path)) {
            if (!(current instanceof Folder))
                throw new IllegalArgumentException("Invalid path");

            current = ((Folder) current).children.get(part);

            if (current == null)
                throw new IllegalArgumentException("Path not found");
        }

        return current;
    }

    private Folder getFolder(String path) {
        FileSystemItem item = getItem(path);

        if (!(item instanceof Folder))
            throw new IllegalArgumentException("Not a folder");

        return (Folder) item;
    }

    private String[] parts(String path) {
        return Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .toArray(String[]::new);
    }

    private String parentPath(String path) {
        int index = path.lastIndexOf('/');
        return index <= 0 ? "/" : path.substring(0, index);
    }

    private String fileName(String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }
}

abstract class FileSystemItem {
    String name;

    FileSystemItem(String name) {
        this.name = name;
    }
}

class File extends FileSystemItem {
    String content;

    File(String name, String content) {
        super(name);
        this.content = content;
    }
}

class Folder extends FileSystemItem {
    Map<String, FileSystemItem> children = new HashMap<>();

    Folder(String name) {
        super(name);
    }
}