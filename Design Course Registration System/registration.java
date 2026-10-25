import java.util.*;

/*
 * Course Registration System - 25-minute LLD
 *
 * 1. CourseRegistrationService - manages courses and registrations.
 * 2. Student - represents a student.
 * 3. Course - stores course details, capacity, and enrolled students.
 *
 * Flow:
 * Add course -> Register student -> Drop course -> View registrations.
 *
 * Rules:
 * - A student cannot register twice for the same course.
 * - A course cannot exceed its capacity.
 * - A student can drop a registered course.
 *
 * Data structures:
 * - HashMap for course lookup.
 * - Set for enrolled students and duplicate prevention.
 */

class CourseRegistrationService {
    private Map<String, Course> courses = new HashMap<>();

    public void addCourse(Course course) {
        courses.put(course.getId(), course);
    }

    public void registerStudent(String courseId, Student student) {
        Course course = courses.get(courseId);

        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        course.register(student);
    }

    public void dropCourse(String courseId, Student student) {
        Course course = courses.get(courseId);

        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        course.drop(student);
    }

    public void showEnrolledStudents(String courseId) {
        Course course = courses.get(courseId);

        if (course == null) {
            throw new IllegalArgumentException("Course not found");
        }

        System.out.println("Students in " + course.getName() + ":");
        for (Student student : course.getStudents()) {
            System.out.println(student.getName());
        }
    }
}

class Course {
    private String id;
    private String name;
    private int capacity;
    private Set<Student> enrolledStudents = new HashSet<>();

    public Course(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
    }

    public void register(Student student) {
        if (enrolledStudents.contains(student)) {
            throw new IllegalStateException("Already registered");
        }

        if (enrolledStudents.size() >= capacity) {
            throw new IllegalStateException("Course is full");
        }

        enrolledStudents.add(student);
    }

    public void drop(Student student) {
        if (!enrolledStudents.remove(student)) {
            throw new IllegalStateException("Student not registered");
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Student> getStudents() {
        return Collections.unmodifiableSet(enrolledStudents);
    }
}

class Student {
    private String id;
    private String name;

    public Student(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Student)) return false;

        Student other = (Student) obj;
        return Objects.equals(id, other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

public class Main {
    public static void main(String[] args) {
        CourseRegistrationService service = new CourseRegistrationService();

        Student alice = new Student("S1", "Alice");
        Student bob = new Student("S2", "Bob");

        Course java = new Course("C1", "Java Programming", 2);
        service.addCourse(java);

        service.registerStudent("C1", alice);
        service.registerStudent("C1", bob);

        service.showEnrolledStudents("C1");

        service.dropCourse("C1", alice);

        System.out.println("After dropping:");
        service.showEnrolledStudents("C1");
    }
}