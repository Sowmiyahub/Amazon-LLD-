import java.time.LocalDateTime;
import java.util.*;

/*
 * Google Docs - 25-minute LLD
 *
 * 1. DocumentService - creates, edits, shares, and restores documents.
 * 2. Document - stores content, owner, permissions, and versions.
 * 3. User - represents a document user.
 * 4. DocumentVersion - stores a snapshot of document content.
 * 5. Permission - defines VIEW or EDIT access.
 *
 * Flow:
 * Create document -> Share document -> Edit -> View/restore versions.
 *
 * Simplification:
 * In-memory storage, whole-document content, no real-time collaboration.
 */

public class Main {
    public static void main(String[] args) {
        DocumentService service = new DocumentService();

        User alice = new User("U1", "Alice");
        User bob = new User("U2", "Bob");

        Document doc = service.createDocument("D1", "Meeting Notes", alice);

        service.shareDocument(doc, bob, Permission.EDIT);
        service.editDocument(doc, bob, "Updated meeting notes");

        System.out.println(service.readDocument(doc, alice));

        service.showVersions(doc);
        service.restoreVersion(doc, alice, 1);

        System.out.println("After restore: " + service.readDocument(doc, alice));
    }
}

class DocumentService {
    private Map<String, Document> documents = new HashMap<>();

    public Document createDocument(String id, String title, User owner) {
        Document doc = new Document(id, title, owner);
        documents.put(id, doc);
        return doc;
    }

    public void shareDocument(Document doc, User user, Permission permission) {
        doc.share(user, permission);
    }

    public void editDocument(Document doc, User user, String content) {
        doc.edit(user, content);
    }

    public String readDocument(Document doc, User user) {
        return doc.read(user);
    }

    public void showVersions(Document doc) {
        doc.showVersions();
    }

    public void restoreVersion(Document doc, User user, int versionNumber) {
        doc.restoreVersion(user, versionNumber);
    }
}

class Document {
    private String id;
    private String title;
    private String content = "";
    private User owner;

    private Map<String, Permission> permissions = new HashMap<>();
    private List<DocumentVersion> versions = new ArrayList<>();

    public Document(String id, String title, User owner) {
        this.id = id;
        this.title = title;
        this.owner = owner;
        permissions.put(owner.getId(), Permission.EDIT);
        saveVersion();
    }

    public void share(User user, Permission permission) {
        permissions.put(user.getId(), permission);
    }

    public void edit(User user, String newContent) {
        checkEditPermission(user);
        content = newContent;
        saveVersion();
    }

    public String read(User user) {
        if (!permissions.containsKey(user.getId())) {
            throw new IllegalStateException("Access denied");
        }
        return content;
    }

    public void restoreVersion(User user, int versionNumber) {
        checkEditPermission(user);

        if (versionNumber < 1 || versionNumber > versions.size()) {
            throw new IllegalArgumentException("Invalid version");
        }

        content = versions.get(versionNumber - 1).getContent();
        saveVersion();
    }

    public void showVersions() {
        for (DocumentVersion version : versions) {
            System.out.println(
                "Version " + version.getVersionNumber()
                + " | " + version.getCreatedAt()
            );
        }
    }

    private void checkEditPermission(User user) {
        Permission permission = permissions.get(user.getId());

        if (permission != Permission.EDIT) {
            throw new IllegalStateException("Edit permission required");
        }
    }

    private void saveVersion() {
        versions.add(new DocumentVersion(
            versions.size() + 1, content, LocalDateTime.now()
        ));
    }
}

class DocumentVersion {
    private int versionNumber;
    private String content;
    private LocalDateTime createdAt;

    public DocumentVersion(int versionNumber, String content,
                           LocalDateTime createdAt) {
        this.versionNumber = versionNumber;
        this.content = content;
        this.createdAt = createdAt;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

class User {
    private String id;
    private String name;

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }
}

enum Permission {
    VIEW, EDIT
}