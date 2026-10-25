
/*
========================================================
DESIGN: INSTAGRAM / FACEBOOK
========================================================

1. SocialMediaService
   - Main service/controller.
   - Registers users, creates posts, follows users,
     likes posts, adds comments, and generates feeds.

2. User
   - Stores user ID, name, followers, and following.
   - Creates posts and interacts with other users.

3. Post
   - Stores post ID, author, content, timestamp,
     likes, and comments.

4. Comment
   - Stores comment ID, author, content, and timestamp.

5. NewsFeed
   - Collects posts from followed users.
   - Sorts posts by newest first.

6. Like
   - Represents a user's like on a post.

7. Relationship
   - FOLLOWING: Instagram-style one-way relationship.
   - FRIENDSHIP: Facebook-style mutual relationship.

8. RelationshipType (Enum)
   - FOLLOWING, FRIENDSHIP.

========================================================
CLASS RELATIONSHIPS
========================================================

SocialMediaService
  ├── manages Users
  ├── creates Posts
  └── generates NewsFeed

User
  ├── creates Posts
  ├── follows other Users
  └── interacts with Posts

Post
  ├── belongs to one User
  ├── contains Likes
  └── contains Comments

Comment
  └── belongs to one User and one Post

NewsFeed
  └── contains Posts from followed Users

========================================================
MAIN FLOW
========================================================

1. Register users.
2. Users follow other users.
3. Users create posts.
4. Users like posts and add comments.
5. Generate a feed from followed users' posts.
6. Sort the feed by newest first.

========================================================
*/

// 1. Main service/controller
import java.time.LocalDateTime;
import java.util.*;

class SocialMediaService {
    private final Map<String, User> users = new HashMap<>();
    private final Map<String, Post> posts = new HashMap<>();

    public void registerUser(User user) {
        users.put(user.getId(), user);
    }

    public Post createPost(String userId, String content) {
        User user = getUser(userId);

        Post post = new Post(
            UUID.randomUUID().toString(), user, content
        );

        user.addPost(post);
        posts.put(post.getId(), post);

        return post;
    }

    // Instagram-style one-way follow
    public void followUser(String followerId, String targetId) {
        User follower = getUser(followerId);
        User target = getUser(targetId);

        if (followerId.equals(targetId)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        follower.follow(target);
    }

    public void unfollowUser(String followerId, String targetId) {
        getUser(followerId).unfollow(targetId);
    }

    public void likePost(String userId, String postId) {
        User user = getUser(userId);
        Post post = getPost(postId);

        post.addLike(user);
    }

    public void unlikePost(String userId, String postId) {
        getUser(userId);
        getPost(postId).removeLike(userId);
    }

    public void addComment(
            String userId, String postId, String content) {
        User user = getUser(userId);
        Post post = getPost(postId);

        Comment comment = new Comment(
            UUID.randomUUID().toString(), user, content
        );

        post.addComment(comment);
    }

    public List<Post> getNewsFeed(String userId) {
        User user = getUser(userId);
        List<Post> feed = new ArrayList<>();

        // Include the user's own posts
        feed.addAll(user.getPosts());

        // Include posts from followed users
        for (User followedUser : user.getFollowing().values()) {
            feed.addAll(followedUser.getPosts());
        }

        feed.sort(
            Comparator.comparing(Post::getCreatedAt).reversed()
        );

        return feed;
    }

    // Facebook-style mutual friendship
    public void addFriend(String userId, String friendId) {
        User user = getUser(userId);
        User friend = getUser(friendId);

        if (userId.equals(friendId)) {
            throw new IllegalArgumentException("Cannot friend yourself");
        }

        user.addFriend(friend);
        friend.addFriend(user);
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
}

// 2. User entity
class User {
    private final String id;
    private final String name;

    private final List<Post> posts = new ArrayList<>();
    private final Map<String, User> following = new HashMap<>();
    private final Map<String, User> followers = new HashMap<>();
    private final Map<String, User> friends = new HashMap<>();

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void addPost(Post post) {
        posts.add(post);
    }

    public List<Post> getPosts() {
        return Collections.unmodifiableList(posts);
    }

    public Map<String, User> getFollowing() {
        return Collections.unmodifiableMap(following);
    }

    public Map<String, User> getFollowers() {
        return Collections.unmodifiableMap(followers);
    }

    public void follow(User user) {
        following.put(user.getId(), user);
        user.followers.put(id, this);
    }

    public void unfollow(String userId) {
        User user = following.remove(userId);

        if (user != null) {
            user.followers.remove(id);
        }
    }

    public void addFriend(User user) {
        friends.put(user.getId(), user);
    }

    public Map<String, User> getFriends() {
        return Collections.unmodifiableMap(friends);
    }
}

// 3. Post entity
class Post {
    private final String id;
    private final User author;
    private final String content;
    private final LocalDateTime createdAt;

    // Map prevents the same user from liking a post repeatedly
    private final Map<String, Like> likes = new HashMap<>();
    private final List<Comment> comments = new ArrayList<>();

    public Post(String id, User author, String content) {
        this.id = id;
        this.author = author;
        this.content = content;
        this.createdAt = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void addLike(User user) {
        likes.putIfAbsent(
            user.getId(), new Like(user, this)
        );
    }

    public void removeLike(String userId) {
        likes.remove(userId);
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
        return author.getName() + ": " + content
            + " | Likes: " + likes.size();
    }
}

// 4. Like entity
class Like {
    private final User user;
    private final Post post;
    private final LocalDateTime createdAt;

    public Like(User user, Post post) {
        this.user = user;
        this.post = post;
        this.createdAt = LocalDateTime.now();
    }

    public User getUser() {
        return user;
    }

    public Post getPost() {
        return post;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

// 5. Comment entity
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

    public String getId() {
        return id;
    }

    public User getAuthor() {
        return author;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

// 6. Example usage
public class Main {
    public static void main(String[] args) {
        SocialMediaService service = new SocialMediaService();

        User alice = new User("U1", "Alice");
        User bob = new User("U2", "Bob");
        User charlie = new User("U3", "Charlie");

        service.registerUser(alice);
        service.registerUser(bob);
        service.registerUser(charlie);

        service.followUser("U1", "U2");
        service.followUser("U1", "U3");

        Post bobPost = service.createPost(
            "U2", "Hello from Bob!"
        );

        service.createPost("U3", "Enjoying my weekend!");

        service.likePost("U1", bobPost.getId());
        service.addComment(
            "U1", bobPost.getId(), "Nice post!"
        );

        System.out.println("Alice's News Feed:");

        for (Post post : service.getNewsFeed("U1")) {
            System.out.println(post);
        }

        // Facebook-style mutual friendship
        service.addFriend("U1", "U2");
    }
}
