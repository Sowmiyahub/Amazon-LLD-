import java.util.*;

/*
 * Stack Overflow - 25-minute LLD
 *
 * 1. StackOverflowService - manages questions, answers, and voting.
 * 2. User - represents a registered user.
 * 3. Question - stores question details, tags, answers, and votes.
 * 4. Answer - stores an answer and its votes.
 * 5. Comment - stores comments on questions or answers.
 *
 * Flow:
 * Ask question -> Answer -> Vote -> Accept answer.
 *
 * Simplification:
 * In-memory storage, basic voting, no authentication.
 */

public class Main {
    public static void main(String[] args) {
        StackOverflowService service = new StackOverflowService();

        User alice = new User("U1", "Alice");
        User bob = new User("U2", "Bob");

        Question question = service.askQuestion(
            alice, "How does HashMap work?",
            "Explain internal implementation",
            Arrays.asList("Java", "Data Structures")
        );

        Answer answer = service.addAnswer(
            question, bob, "HashMap uses hashing and buckets."
        );

        service.voteQuestion(question, bob, 1);
        service.voteAnswer(answer, alice, 1);
        service.acceptAnswer(question, answer);

        service.addComment(question, bob, "Interesting question!");

        service.showQuestion(question);
    }
}

class StackOverflowService {
    private Map<String, Question> questions = new HashMap<>();
    private int questionCounter = 1;
    private int answerCounter = 1;
    private int commentCounter = 1;

    public Question askQuestion(User author, String title,
                                String body, List<String> tags) {
        Question question = new Question(
            "Q" + questionCounter++, author, title, body, tags
        );
        questions.put(question.getId(), question);
        return question;
    }

    public Answer addAnswer(Question question, User author, String body) {
        Answer answer = new Answer("A" + answerCounter++, author, body);
        question.addAnswer(answer);
        return answer;
    }

    public void voteQuestion(Question question, User user, int vote) {
        question.vote(user, vote);
    }

    public void voteAnswer(Answer answer, User user, int vote) {
        answer.vote(user, vote);
    }

    public void acceptAnswer(Question question, Answer answer) {
        question.acceptAnswer(answer);
    }

    public void addComment(Question question, User author, String body) {
        question.addComment(new Comment(
            "C" + commentCounter++, author, body
        ));
    }

    public void showQuestion(Question question) {
        System.out.println("Question: " + question.getTitle());
        System.out.println("Body: " + question.getBody());
        System.out.println("Tags: " + question.getTags());
        System.out.println("Votes: " + question.getVoteCount());

        for (Answer answer : question.getAnswers()) {
            System.out.println("Answer: " + answer.getBody()
                + " | Votes: " + answer.getVoteCount()
                + (answer.isAccepted() ? " | Accepted" : ""));
        }

        for (Comment comment : question.getComments()) {
            System.out.println("Comment: " + comment.getBody());
        }
    }
}

class User {
    private String id;
    private String name;
    private int reputation;

    public User(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void updateReputation(int points) {
        reputation += points;
    }
}

class Question {
    private String id;
    private User author;
    private String title;
    private String body;
    private List<String> tags;
    private List<Answer> answers = new ArrayList<>();
    private List<Comment> comments = new ArrayList<>();
    private Map<String, Integer> votes = new HashMap<>();
    private Answer acceptedAnswer;

    public Question(String id, User author, String title,
                    String body, List<String> tags) {
        this.id = id;
        this.author = author;
        this.title = title;
        this.body = body;
        this.tags = new ArrayList<>(tags);
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

    public void addComment(Comment comment) {
        comments.add(comment);
    }

    public void vote(User user, int value) {
        if (value != 1 && value != -1)
            throw new IllegalArgumentException("Vote must be +1 or -1");

        votes.put(user.getName(), value);
    }

    public void acceptAnswer(Answer answer) {
        if (!answers.contains(answer))
            throw new IllegalArgumentException("Answer does not belong here");

        if (acceptedAnswer != null)
            acceptedAnswer.setAccepted(false);

        acceptedAnswer = answer;
        answer.setAccepted(true);
    }

    public int getVoteCount() {
        return votes.values().stream().mapToInt(Integer::intValue).sum();
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getBody() { return body; }
    public List<String> getTags() { return tags; }
    public List<Answer> getAnswers() { return answers; }
    public List<Comment> getComments() { return comments; }
}

class Answer {
    private String id;
    private User author;
    private String body;
    private boolean accepted;
    private Map<String, Integer> votes = new HashMap<>();

    public Answer(String id, User author, String body) {
        this.id = id;
        this.author = author;
        this.body = body;
    }

    public void vote(User user, int value) {
        if (value != 1 && value != -1)
            throw new IllegalArgumentException("Vote must be +1 or -1");

        votes.put(user.getName(), value);
    }

    public int getVoteCount() {
        return votes.values().stream().mapToInt(Integer::intValue).sum();
    }

    public void setAccepted(boolean accepted) {
        this.accepted = accepted;
    }

    public boolean isAccepted() { return accepted; }
    public String getBody() { return body; }
}

class Comment {
    private String id;
    private User author;
    private String body;

    public Comment(String id, User author, String body) {
        this.id = id;
        this.author = author;
        this.body = body;
    }

    public String getBody() {
        return body;
    }
}