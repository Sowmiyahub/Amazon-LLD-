
/*
========================================================
DESIGN: LINKEDIN
========================================================

1. LinkedInService
   - Main service/controller.
   - Registers users, manages connections, creates posts,
     searches jobs, applies for jobs, and generates feeds.

2. User
   - Stores user details, professional headline, skills,
     connections, and posts.

3. Connection
   - Represents a professional connection between two users.
   - Supports PENDING, ACCEPTED, and REJECTED states.

4. Post
   - Stores post ID, author, content, timestamp, likes,
     and comments.

5. Comment
   - Represents a comment on a post.

6. Job
   - Stores job title, company, location, description,
     and required skills.

7. JobApplication
   - Represents a user's application for a job.
   - Tracks application status and application time.

8. Feed
   - Collects posts from the user and their connections.
   - Sorts posts by newest first.

9. ConnectionStatus (Enum)
   - PENDING, ACCEPTED, REJECTED.

10. ApplicationStatus (Enum)
    - APPLIED, UNDER_REVIEW, SHORTLISTED, REJECTED, HIRED.

========================================================
CLASS RELATIONSHIPS
========================================================

LinkedInService
  ├── manages Users
  ├── manages Jobs
  ├── manages JobApplications
  └── generates Feeds

User
  ├── has Connections
  ├── creates Posts
  └── applies for Jobs

Connection
  └── connects two Users

Post
  ├── belongs to one User
  ├── contains Likes
  └── contains Comments

Job
  └── receives JobApplications

JobApplication
  ├── belongs to one User
  └── references one Job

========================================================
MAIN FLOW
========================================================

1. Register users with professional profiles.
2. Send and accept connection requests.
3. Create professional posts.
4. Like and comment on posts.
5. View a feed containing posts from connections.
6. Recruiters publish job openings.
7. Users search for jobs and apply.

========================================================
*/

// 1. Main service/controller
import java.time.LocalDateTime;
import java.util.*;

class LinkedInService {
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Post> posts = new HashMap<>();
    private final Map<String, Job> jobs = new HashMap<>();
    private final Map<String, JobApplication> applications =
        new HashMap<>();

    // Register a user
    public void registerUser(User user) {
        users.put(user.getId(), user);
    }

    // Send a connection request
    public Connection sendConnectionRequest(
            String senderId, String receiverId) {
        User sender = getUser(senderId);
        User receiver = getUser(receiverId);

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException(
                "Cannot connect to yourself"
            );
        }

        if (sender.getConnections().containsKey(receiverId)) {
            throw new IllegalStateException(
                "Connection request already exists"
            );
        }

        Connection connection = new Connection(sender, receiver);
        sender.getConnections().put(receiverId, connection);

        return connection;
    }

    // Accept a pending connection request
    public void acceptConnectionRequest(
            String receiverId, String senderId) {
        User receiver = getUser(receiverId);
        User sender = getUser(senderId);

        Connection connection =
            sender.getConnections().get(receiverId);

        if (connection == null ||
                connection.getStatus() != ConnectionStatus.PENDING) {
            throw new IllegalStateException(
                "No pending connection request"
            );
        }

        connection.setStatus(ConnectionStatus.ACCEPTED);

        // Both users now see the accepted connection
        receiver.getConnections().put(senderId, connection);
    }

    // Create a professional post
    public Post createPost(String userId, String content) {
        User user = getUser(userId);

        Post post = new Post(
            UUID.randomUUID().toString(), user, content
        );

        user.addPost(post);
        posts.put(post.getId(), post);

        return post;
    }

    // Like a post
    public void likePost(String userId, String postId) {
        User user = getUser(userId);
        Post post = getPost(postId);

        post.addLike(user);
    }

    // Comment on a post
    public void commentOnPost(
            String userId, String postId, String content) {
        User user = getUser(userId);
        Post post = getPost(postId);

        Comment comment = new Comment(
            UUID.randomUUID().toString(), user, content
        );

        post.addComment(comment);
    }

    // Generate a feed from own posts and accepted connections
    public List<Post> getFeed(String userId) {
        User user = getUser(userId);
        List<Post> feed = new ArrayList<>();

        feed.addAll(user.getPosts());

        for (Connection connection :
                user.getConnections().values()) {
            if (connection.getStatus() == ConnectionStatus.ACCEPTED) {
                User other = connection.getOtherUser(userId);
                feed.addAll(other.getPosts());
            }
        }

        feed.sort(
            Comparator.comparing(Post::getCreatedAt).reversed()
        );

        return feed;
    }

    // Publish a job opening
    public void postJob(String recruiterId, Job job) {
        User recruiter = getUser(recruiterId);
        job.setRecruiter(recruiter);
        jobs.put(job.getId(), job);
    }

    // Search jobs by title
    public List<Job> searchJobs(String keyword) {
        List<Job> result = new ArrayList<>();

        for (Job job : jobs.values()) {
            if (job.getTitle().toLowerCase()
                    .contains(keyword.toLowerCase())) {
                result.add(job);
            }
        }

        return result;
    }

    // Apply for a job
    public JobApplication applyForJob(
            String userId, String jobId) {
        User user = getUser(userId);
        Job job = getJob(jobId);

        boolean alreadyApplied = applications.values().stream()
            .anyMatch(application ->
                application.getApplicant().getId().equals(userId)
                && application.getJob().getId().equals(jobId)
            );

        if (alreadyApplied) {
            throw new IllegalStateException(
                "Already applied for this job"
            );
        }

        JobApplication application = new JobApplication(
            UUID.randomUUID().toString(), user, job
        );

        applications.put(application.getId(), application);
        return application;
    }

    private User getUser(String userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }
        return user;
    }

    private Post getPost(String postId) {
        Post post = posts.get(postId);
        if (post == null) {
            throw new IllegalArgumentException("Post not found");
        }
        return post;
    }

    private Job getJob(String jobId) {
        Job job = jobs.get(jobId);
        if (job == null) {
            throw new IllegalArgumentException("Job not found");
        }
        return job;
    }
}

// 2. User / professional profile
class User {
    private final String id;
    private final String name;
    private String headline;
    private final Set<String> skills = new HashSet<>();
    private final List<Post> posts = new ArrayList<>();

    // Other user ID -> connection
    private final Map<String, Connection> connections =
        new HashMap<>();

    public User(String id, String name, String headline) {
        this.id = id;
        this.name = name;
        this.headline = headline;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getHeadline() { return headline; }

    public void setHeadline(String headline) {
        this.headline = headline;
    }

    public void addSkill(String skill) {
        skills.add(skill);
    }

    public Set<String> getSkills() {
        return Collections.unmodifiableSet(skills);
    }

    public void addPost(Post post) {
        posts.add(post);
    }

    public List<Post> getPosts() {
        return Collections.unmodifiableList(posts);
    }

    public Map<String, Connection> getConnections() {
        return connections;
    }
}

// 3. Connection between two professionals
class Connection {
    private final User sender;
    private final User receiver;
    private ConnectionStatus status;

    public Connection(User sender, User receiver) {
        this.sender = sender;
        this.receiver = receiver;
        this.status = ConnectionStatus.PENDING;
    }

    public User getSender() { return sender; }
    public User getReceiver() { return receiver; }
    public ConnectionStatus getStatus() { return status; }

    public void setStatus(ConnectionStatus status) {
        this.status = status;
    }

    public User getOtherUser(String userId) {
        if (sender.getId().equals(userId)) {
            return receiver;
        }
        return sender;
    }
}

// 4. Professional post
class Post {
    private final String id;
    private final User author;
    private final String content;
    private final LocalDateTime createdAt;
    private final Map<String, User> likes = new HashMap<>();
    private final List<Comment> comments = new ArrayList<>();

    public Post(String id, User author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public User getAuthor() { return author; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void addLike(User user) {
        likes.putIfAbsent(user.getId(), user);
    }

    public int getLikeCount() {
        return likes.size();
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public List<Comment> getComments() {
        return Collections.unmodifiableList(comments);
    }

    @Override
    public String toString() {
        return author.getName() + " | " + content
            + " | Likes: " + likes.size();
    }
}

// 5. Comment on a post
class Comment {
    private final String id;
    private final User author;
    private final String content;
    private final LocalDateTime createdAt;

    public Comment(String id, User author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() { return id; }
    public User getAuthor() { return author; }
    public String getContent() { return content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}

// 6. Job listing
class Job {
    private final String id;
    private final String title;
    private final String company;
    private final String location;
    private final String description;
    private User recruiter;

    public Job(String id, String title, String company,
               String location, String description) {
        this.id = id;
        this.title = title;
        this.company = company;
        this.location = location;
        this.description = description;
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getCompany() { return company; }
    public String getLocation() { return location; }
    public String getDescription() { return description; }
    public User getRecruiter() { return recruiter; }

    public void setRecruiter(User recruiter) {
        this.recruiter = recruiter;
    }

    @Override
    public String toString() {
        return title + " at " + company + " (" + location + ")";
    }
}

// 7. Job application
class JobApplication {
    private final String id;
    private final User applicant;
    private final Job job;
    private final LocalDateTime appliedAt;
    private ApplicationStatus status;

    public JobApplication(String id, User applicant, Job job) {
        this.id = id;
        this.applicant = applicant;
        this.job = job;
        this.appliedAt = LocalDateTime.now();
        this.status = ApplicationStatus.APPLIED;
    }

    public String getId() { return id; }
    public User getApplicant() { return applicant; }
    public Job getJob() { return job; }
    public LocalDateTime getAppliedAt() { return appliedAt; }
    public ApplicationStatus getStatus() { return status; }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }
}

// 8. Enums
enum ConnectionStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}

enum ApplicationStatus {
    APPLIED,
    UNDER_REVIEW,
    SHORTLISTED,
    REJECTED,
    HIRED
}

// 9. Example usage
public class Main {
    public static void main(String[] args) {
        LinkedInService service = new LinkedInService();

        User alice = new User(
            "U1", "Alice", "Software Engineer"
        );
        User bob = new User(
            "U2", "Bob", "Engineering Manager"
        );
        User recruiter = new User(
            "U3", "Charlie", "Technical Recruiter"
        );

        alice.addSkill("Java");
        alice.addSkill("Spring Boot");

        service.registerUser(alice);
        service.registerUser(bob);
        service.registerUser(recruiter);

        service.sendConnectionRequest("U1", "U2");
        service.acceptConnectionRequest("U2", "U1");

        service.createPost("U2", "We are hiring engineers!");

        Post post = service.createPost(
            "U1", "Excited to share my latest project!"
        );

        service.likePost("U2", post.getId());
        service.commentOnPost(
            "U2", post.getId(), "Congratulations!"
        );

        System.out.println("Alice's feed:");
        for (Post feedPost : service.getFeed("U1")) {
            System.out.println(feedPost);
        }

        Job job = new Job(
            "J1",
            "Software Engineer",
            "Example Company",
            "Chennai",
            "Java, DSA and backend development"
        );

        service.postJob("U3", job);

        System.out.println(service.searchJobs("Software"));

        JobApplication application =
            service.applyForJob("U1", "J1");

        System.out.println(
            "Application status: " + application.getStatus()
        );
    }
}
